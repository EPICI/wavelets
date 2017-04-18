package main;

import java.util.Arrays;

import util.*;
import util.math.*;

/**
 * Sampled sound
 * 
 * @author EPICI
 * @version 1.0
 */
public class Samples implements Curve {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Sample rate in Hz
	 */
	public int sampleRate;
	/**
	 * Sample data
	 */
	public double[] sampleData;
	/**
	 * Spectrum (real component)
	 */
	public transient double[] spectrumReal;
	/**
	 * Spectrum (imaginary component)
	 */
	public transient double[] spectrumImag;
	/**
	 * Hash of sample data
	 */
	protected transient int sampleHash = 0;
	/**
	 * Hash of spectrum data
	 */
	protected transient int spectrumHash = 0;
	
	/**
	 * Clean constructor
	 * 
	 * @param samplerate sample rate in Hz
	 * @param sampledata sampled sound
	 */
	public Samples(int samplerate, double[] sampledata){
		sampleRate = samplerate;
		sampleData = sampledata;
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param copyFrom the {@link Samples} object to copy from
	 */
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
					if(Floats.isNear(left, position)){
						return sampleData[left];
					}else{
						return Bezier.bezier2to4(sampleData[left], sampleData[left+1], position-left);
					}
				}
			}
		}else{
			throw new IllegalArgumentException(Double.toString(position)+" is not finite.");
		}
	}
	
	/**
	 * Layer another sample onto this one lazily
	 * <br>
	 * Redirect with 0 offset
	 * 
	 * @param toLayer the sampled data to layer
	 */
	public synchronized void layerOnThisLazy(Samples toLayer){
		layerOnThisLazy(toLayer,0,0);
	}
	/**
	 * Layer another sample onto this one lazily
	 * <br>
	 * Offsets are in values/samples, not seconds
	 * <br>
	 * First offset is offset in this
	 * <br>
	 * Second offset is offset in samples to layer
	 * <br>
	 * Tries to do full layering
	 * 
	 * @param toLayer the sampled data to layer
	 * @param offset1 the offset in this object
	 * @param offset2 the offset in the other object
	 */
	public synchronized void layerOnThisLazy(Samples toLayer,int offset1,int offset2){
		double[] layerData = toLayer.sampleData;
		int combined = offset2-offset1;
		int cap = Math.min(layerData.length-combined, sampleData.length);
		for(int i=offset1;i<cap;i++){
			sampleData[i]+=layerData[i+combined];
		}
	}
	/**
	 * Layer another sample onto this one
	 * <br>
	 * Redirect with 0 offset
	 * 
	 * @param toLayer the sampled data to layer
	 */
	public synchronized void layerOnThis(Samples toLayer){
		layerOnThis(toLayer,0d,0d);
	}
	/**
	 * Layer another sample onto this one
	 * <br>
	 * Offset is time in seconds
	 * 
	 * @param toLayer the sampled data to layer
	 * @param offset1 the offset in this object
	 * @param offset2 the offset in the other object
	 */
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
	
	/**
	 * @return hash for sample data
	 */
	public int sampleHash(){
		return Hash.lazy(sampleData);
	}
	
	/**
	 * @return hash for spectrum data
	 */
	public int spectrumHash(){
		return Hash.lazy(spectrumReal)+Hash.lazy(spectrumImag);
	}
	
	/**
	 * @return double array array containing the spectrum,
	 * first array is real component, second is imaginary
	 */
	public synchronized double[][] getSpectrum(){
		int newHash = sampleHash();
		if(sampleHash!=newHash){
			fft(newHash);
		}
		return new double[][]{spectrumReal,spectrumImag};
	}
	
	/**
	 * Check for discrepancy, automatically choose FFT or IFFT
	 * <br>
	 * <b>Use via API is discouraged, know whether you want
	 * FFT or IFFT and call that instead</b>
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
	
	/**
	 * Check if samples changed, do FFT if so
	 */
	public void checkFft(){
		int newHash = sampleHash();
		if(newHash!=sampleHash){
			fft(newHash);
		}
	}
	
	/**
	 * Check if spectrum changed, do IFFT if so
	 */
	public void checkIfft(){
		int newHash = spectrumHash();
		if(newHash!=spectrumHash){
			ifft(newHash);
		}
	}
	
	/**
	 * Single argument passed is precalculated sample hash
	 * <br>
	 * Spectrum hash will be updated after to match
	 * 
	 * @param newHash sample hash
	 */
	public synchronized void fft(int newHash){
		int total = sampleData.length;
		spectrumReal = Arrays.copyOf(sampleData, total);
		spectrumImag = blankArray(total);
		FFT.adaptiveFft(spectrumReal,spectrumImag);
		sampleHash = newHash;
		spectrumHash = spectrumHash();
	}
	
	/**
	 * Single argument passed is precalculated spectrum hash
	 * <br>
	 * Sample hash will be updated after to match
	 * 
	 * @param newHash spectrum hash
	 */
	public synchronized void ifft(int newHash){
		int total = spectrumReal.length;
		sampleData = Arrays.copyOf(spectrumReal, total);
		double[] sampleImag = Arrays.copyOf(spectrumImag, total);
		FFT.adaptiveFft(sampleData,sampleImag);
		spectrumHash = newHash;
		sampleHash = sampleHash();
	}
	
	/**
	 * Create a blank double array with the specified length
	 * 
	 * @param count length of the array
	 * @return the new array
	 */
	public static double[] blankArray(int count){
		double[] result = new double[count];
		//No need to fill because it defaults to 0d
		return result;
	}
	/**
	 * Create a blank {@link Samples} object
	 * 
	 * @param samplerate sample rate in Hz
	 * @param count length/sample count
	 * @return new {@link Samples} object
	 */
	public static Samples blankSamples(int samplerate,int count){
		return new Samples(samplerate,blankArray(count));
	}
	
	/**
	 * Apply curve to some array as an envelope
	 * 
	 * @param curve the curve to multiply pointwisely
	 * @param sampleRate sample rate in Hz, determines mapping multiplier
	 * @param target target array to modify in place
	 */
	public static void applyCurveTo(Curve curve,double sampleRate,double[] target){
		int total = target.length;
		double rateMult = 1d/sampleRate;
		for(int i=0;i<total;i++){
			double position = rateMult*i;
			target[i]*=curve.valueAtPosition(position);
		}
	}
	/**
	 * Apply curve to some arrays as an envelope
	 * 
	 * @param curve the curve to multiply pointwisely
	 * @param sampleRate sample rate in Hz, determines mapping multiplier
	 * @param targets target arrays to modify in place
	 */
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
	
	/**
	 * Apply curve as envelope to sample data
	 * 
	 * @param curve the curve to apply
	 */
	public synchronized void applyCurveToData(Curve curve){
		applyCurveTo(curve,sampleRate,sampleData);
	}
	/**
	 * Apply curve as envelope to spectrum correctly
	 * <br>
	 * <b>Doing the extra calculation is necessary, otherwise the mapping
	 * will be wrong. Do not ever pass the spectrum directly to
	 * applyCurveToInParallel. Use this method.</b>
	 * 
	 * @param curve the curve to apply
	 */
	public synchronized void applyCurveToSpectrum(Curve curve){
		applyCurveToInParallel(curve,((double)sampleData.length)/sampleRate,spectrumReal,spectrumImag);
	}
	
	/**
	 * Returns a slice of the sampled data
	 * <br>
	 * Length of copied data will be end - start
	 * 
	 * @param start the index of the item to start with, inclusive
	 * @param end the index of the item to end with, exclusive
	 * @return that particular slice
	 */
	public double[] slice(int start,int end){
		return Arrays.copyOfRange(sampleData, start, end);
	}
}
