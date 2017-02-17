package main;

import java.util.Arrays;
import utils.*;

//Sample data
public class Samples implements Curve {
	private static final long serialVersionUID = 1L;
	
	public int sampleRate;
	public double[] sampleData;
	public transient double[] spectrumReal;
	public transient double[] spectrumImag;
	public transient int sampleHash = 0;
	public transient int spectrumHash = 0;
	
	public Samples(int samplerate, double[] sampledata){
		sampleRate = samplerate;
		sampleData = sampledata;
	}
	
	public Samples(Samples copyFrom){
		sampleRate = copyFrom.sampleRate;
		sampleData = Arrays.copyOf(copyFrom.sampleData, copyFrom.sampleData.length);
	}
	
	public double valueAtPosition(double position){
		if(Double.isFinite(position)){
			if(position<0){
				return 0d;
			}else{
				double index = position*sampleRate;
				int cap = sampleData.length-1;
				if(index>cap){
					return 0d;
				}else{
					int left = (int) position;
					if(MathUtils.isNear(left, position)){
						return sampleData[left];
					}else{
						return MathUtils.bezier2to4(sampleData[left], sampleData[left+1], position-left);
					}
				}
			}
		}else{
			throw new IllegalArgumentException(Double.toString(position)+" is not finite.");
		}
	}
	
	/*
	 * Layer another sample onto this one lazily
	 * Offsets are in values
	 * First offset is offset in this
	 * Second offset is offset in samples to layer
	 */
	public synchronized void layerOnThisLazy(Samples toLayer){
		layerOnThisLazy(toLayer,0,0);
	}
	public synchronized void layerOnThisLazy(Samples toLayer,int offset1,int offset2){
		double[] layerData = toLayer.sampleData;
		int combined = offset2-offset1;
		int cap = Math.min(layerData.length-combined, sampleData.length);
		for(int i=offset1;i<cap;i++){
			sampleData[i]+=layerData[i+combined];
		}
	}
	/*
	 * Layer another sample onto this one
	 * Offset is time in seconds
	 */
	public synchronized void layerOnThis(Samples toLayer){
		layerOnThis(toLayer,0d,0d);
	}
	public synchronized void layerOnThis(Samples toLayer,double offset1,double offset2){
		if(toLayer.sampleRate==sampleRate){
			layerOnThisLazy(toLayer,(int)(offset1*sampleRate),(int)(offset2*sampleRate));
		}else{
			double thisRateCopy = sampleRate;
			double layerRateCopy = toLayer.sampleRate;
			double invRate = 1d/layerRateCopy;
			int position = (int)(offset1*thisRateCopy);
			//Possible room for micro-optimization here?
			int cap = position+(int)(thisRateCopy*Math.min(sampleData.length/thisRateCopy-offset1, toLayer.sampleData.length/layerRateCopy-offset2));
			for(int i=position;i<cap;i++){
				double doublePosition = (i-position)*invRate+offset2;
				sampleData[i]+=toLayer.valueAtPosition(doublePosition);
			}
		}
	}
	
	public int sampleHash(){
		return Arrays.hashCode(sampleData);
	}
	
	public int spectrumHash(){
		return Arrays.hashCode(spectrumReal)*268435459+Arrays.hashCode(spectrumImag)*8388617;
	}
	
	public synchronized double[][] getSpectrum(){
		int newHash = sampleHash();
		if(sampleHash!=newHash){
			fft(newHash);
		}
		return new double[][]{spectrumReal,spectrumImag};
	}
	
	/*
	 * Check for discrepancy, automatically choose FFT or IFFT
	 * Use via API is discouraged, know whether you want FFT or IFFT and call that instead
	 */
	public void autoUpdate(){
		int newSampleHash = sampleHash();
		int newSpectrumHash = spectrumHash();
		if(newSampleHash!=sampleHash){
			if(newSpectrumHash==spectrumHash){
				fft(newSampleHash);
			}
		}else if(newSpectrumHash!=spectrumHash){
			ifft(newSpectrumHash);
		}
	}
	
	/*
	 * Check if samples changed, do FFT if so
	 */
	public void checkFft(){
		int newHash = sampleHash();
		if(newHash!=sampleHash){
			fft(newHash);
		}
	}
	
	/*
	 * Check if spectrum changed, do IFFT if so
	 */
	public void checkIfft(){
		int newHash = spectrumHash();
		if(newHash!=spectrumHash){
			ifft(newHash);
		}
	}
	
	/*
	 * Single argument passed is precalculated sample hash
	 * Spectrum hash will be updated after to match
	 */
	public synchronized void fft(int newHash){
		int total = sampleData.length;
		int nearestExp = BitUtils.binLog(total);
		int nearestPower = 1<<nearestExp;
		FFT fft = FFT.getFft(nearestPower);
		if(nearestPower==total){
			spectrumReal = Arrays.copyOf(sampleData, total);
			spectrumImag = blankArray(total);
			fft.fft(spectrumReal,spectrumImag);
		}else{
			int secondStart = total-nearestPower;
			double[] origFirst = Arrays.copyOfRange(sampleData, 0, nearestPower);
			double[] origSecond = Arrays.copyOfRange(sampleData, secondStart, total);
			double[] imagFirst = blankArray(nearestPower);
			double[] imagSecond = blankArray(nearestPower);
			fft.fft(origFirst,imagFirst);
			fft.fft(origSecond,imagSecond);
			spectrumReal = blankArray(total);
			spectrumImag = blankArray(total);
			for(int i=0;i<secondStart;i++){
				spectrumReal[i]=origFirst[i];
				spectrumImag[i]=imagFirst[i];
			}
			double mult = 1d/(nearestPower-secondStart);
			for(int i=secondStart;i<nearestPower;i++){
				int j = i-secondStart;
				double t = j*mult;
				spectrumReal[i]=MathUtils.bezier2(origFirst[i],origSecond[j],t);
				spectrumImag[i]=MathUtils.bezier2(imagFirst[i],imagSecond[j],t);
			}
			for(int i=nearestPower;i<total;i++){
				spectrumReal[i]=origSecond[i-secondStart];
				spectrumImag[i]=imagSecond[i-secondStart];
			}
		}
		sampleHash = newHash;
		spectrumHash = spectrumHash();
	}
	
	/*
	 * Single argument passed is precalculated spectrum hash
	 * Sample hash will be updated after to match
	 */
	public synchronized void ifft(int newHash){
		int total = spectrumReal.length;
		int nearestExp = BitUtils.binLog(total);
		int nearestPower = 1<<nearestExp;
		FFT fft = FFT.getFft(nearestPower);
		if(nearestPower==total){
			sampleData = Arrays.copyOf(spectrumReal, total);
			double[] sampleImag = Arrays.copyOf(spectrumImag, total);
			fft.fft(sampleData,sampleImag);
		}else{
			int secondStart = total-nearestPower;
			double[] origFirst = Arrays.copyOfRange(spectrumReal, 0, nearestPower);
			double[] origSecond = Arrays.copyOfRange(spectrumReal, secondStart, total);
			double[] imagFirst = Arrays.copyOfRange(spectrumImag, 0, nearestPower);
			double[] imagSecond = Arrays.copyOfRange(spectrumImag, secondStart, total);
			fft.fft(origFirst,imagFirst);
			fft.fft(origSecond,imagSecond);
			sampleData = new double[total];
			for(int i=0;i<secondStart;i++){
				sampleData[i]=origFirst[i];
			}
			double mult = 1d/(nearestPower-secondStart);
			for(int i=secondStart;i<nearestPower;i++){
				int j = i-secondStart;
				double t = j*mult;
				sampleData[i]=MathUtils.bezier2(origFirst[i],origSecond[j],t);
			}
			for(int i=nearestPower;i<total;i++){
				sampleData[i]=origSecond[i-secondStart];
			}
		}
		spectrumHash = newHash;
		sampleHash = sampleHash();
	}
	
	//Utility methods
	public static double[] blankArray(int count){
		double[] result = new double[count];
		for(int i=0;i<count;i++){
			result[i]=0d;
		}
		return result;
	}
	public static Samples blankSamples(int samplerate,int count){
		return new Samples(samplerate,blankArray(count));
	}
	
	//Apply curve as envelope
	public static void applyCurveTo(Curve curve,double sampleRate,double[] target){
		int total = target.length;
		double rateMult = 1d/sampleRate;
		for(int i=0;i<total;i++){
			double position = rateMult*i;
			target[i]*=curve.valueAtPosition(position);
		}
	}
	public static void applyCurveToInParallel(Curve curve,double sampleRate,double[]... targets){
		int arrays = targets.length;
		int total = targets[0].length;
		double rateMult = 1d/sampleRate;
		for(int i=0;i<total;i++){
			double position = rateMult*i;
			double value = curve.valueAtPosition(position);
			for(int j=0;j<arrays;j++){
				targets[j][i]*=value;
			}
		}
	}
	
	/*
	 * Apply curve as envelope to sample data
	 */
	public synchronized void applyCurveToData(Curve curve){
		applyCurveTo(curve,sampleRate,sampleData);
	}
	/*
	 * Apply curve as envelope to spectrum correctly
	 */
	public synchronized void applyCurveToSpectrum(Curve curve){
		applyCurveToInParallel(curve,((double)sampleData.length)/sampleRate,spectrumReal,spectrumImag);
	}
}
