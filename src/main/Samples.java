package main;

import java.util.Arrays;

//Sample data
public class Samples implements Curve {
	private static final long serialVersionUID = 1L;
	
	//Constants
	public static final double I_LOG_2 = 1d/Math.log(2);
	
	public int sampleRate;
	public double[] sampleData;
	public transient double[] spectrumReal;
	public transient double[] spectrumImag;
	public transient int sampleHash = 0;
	
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
				return sampleData[0];
			}else{
				double index = position*sampleRate;
				int cap = sampleData.length-1;
				if(index>cap){
					return sampleData[cap];
				}else{
					int left = (int) Math.floor(position);
					if(MathUtils.isNear(left, position)){
						return sampleData[left];
					}else{
						return MathUtils.bezier2(sampleData[left], sampleData[left+1], position-left);
					}
				}
			}
		}else{
			throw new IllegalArgumentException(Double.toString(position)+" is not finite.");
		}
	}
	
	public int sampleHash(){
		return Arrays.hashCode(sampleData);
	}
	
	public synchronized double[][] getSpectrum(){
		int newHash = sampleHash();
		if(sampleHash!=newHash){
			fft(newHash);
		}
		return new double[][]{spectrumReal,spectrumImag};
	}
	
	public void fft(){
		fft(sampleHash());
	}
	public synchronized void fft(int newHash){
		int total = sampleData.length;
		int nearestExp = BitUtils.binLog(total);
		int nearestPower = 1<<nearestExp;
		FFT fft = new FFT(nearestPower);
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
			for(int i=secondStart;i<nearestPower;i++){
				spectrumReal[i]=0.5d*(origFirst[i]+origSecond[i-secondStart]);
				spectrumImag[i]=0.5d*(imagFirst[i]+imagSecond[i-secondStart]);
			}
			for(int i=nearestPower;i<total;i++){
				spectrumReal[i]=origSecond[i-secondStart];
				spectrumImag[i]=imagSecond[i-secondStart];
			}
		}
		sampleHash = newHash;
	}
	
	public synchronized void ifft(){
		int total = spectrumReal.length;
		int nearestExp = BitUtils.binLog(total);
		int nearestPower = 1<<nearestExp;
		FFT fft = new FFT(nearestPower);
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
			for(int i=secondStart;i<nearestPower;i++){
				sampleData[i]=0.5d*(origFirst[i]+origSecond[i-secondStart]);
			}
			for(int i=nearestPower;i<total;i++){
				sampleData[i]=origSecond[i-secondStart];
			}
		}
		sampleHash = sampleHash();
	}
	
	public double[] blankArray(int count){
		double[] result = new double[count];
		for(int i=0;i<count;i++){
			result[i]=0d;
		}
		return result;
	}
}
