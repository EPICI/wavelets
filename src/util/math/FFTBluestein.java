/* 
 * Free FFT and convolution (Java)
 * 
 * Copyright (c) 2017 Project Nayuki
 * https://www.nayuki.io/page/free-small-fft-in-multiple-languages
 * 
 * (MIT License)
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * - The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 * - The Software is provided "as is", without warranty of any kind, express or
 *   implied, including but not limited to the warranties of merchantability,
 *   fitness for a particular purpose and noninfringement. In no event shall the
 *   authors or copyright holders be liable for any claim, damages or other
 *   liability, whether in an action of contract, tort or otherwise, arising from,
 *   out of or in connection with the Software or the use or other dealings in the
 *   Software.
 */

package util.math;

import util.*;
import org.apache.commons.collections4.list.*;

/**
 * Bluestein FFT for arbitrary sizes
 * <br>
 * From: https://www.nayuki.io/res/free-small-fft-in-multiple-languages/Fft.java
 * <br>
 * Note that this is many times slower than {@link FFTRadix2},
 * it's only good because it can handle any size
 * 
 * @author Nayuki
 * @author EPICI
 * @version 1.0
 */
public final class FFTBluestein extends FFT {
	
	/**
	 * The maximum number of Bluestein FFT objects to keep
	 * even if they aren't being used, because they might be common
	 * <br>
	 * Higher = more memory used and slower to access, but less redundant
	 * work done because memoization
	 */
	public static final int MAX_SHARED_FFTS = 1<<6;
	
	/**
	 * The initial key used for priority tracking, set as low as possible
	 * to last as long as possible without overflowing, which is far beyond
	 * any reasonable usage anyways
	 */
	private static final int INITIAL_KEY = Integer.MIN_VALUE;
	
	/**
	 * Shared, cached FFTs
	 * <br>
	 * Maintained to length at most MAX_SHARED_FFTS and in descending
	 * order by keys to allow for faster access
	 */
	private static TreeList<Any.Keyed<Integer, FFTBluestein>> sharedFfts = new TreeList<>();
	
	/**
	 * Get shared FFT object for any length
	 * 
	 * @param n the desired FFT length
	 * @return an FFT object which can process arrays of length n
	 */
	public static FFTBluestein getFft(int n){
		if(n<2){
			throw new IllegalArgumentException("FFT length "+n+" is too small. Minimum value is 2.");
		}else if(n>((1<<30)-1)){
			throw new IllegalArgumentException("FFT length "+n+" is too large. Maximum value is 2^30-1.");
		}else{
			int len = sharedFfts.size();
			for(int i=0;i<len;i++){
				Any.Keyed<Integer,FFTBluestein> keyed = sharedFfts.get(i);
				FFTBluestein fft = keyed.value;
				if(fft.n==n){
					int nkey = ++keyed.key;
					
					//Backwards insertion sort
					int j;
					sharedFfts.remove(i);
					while(i>0 && sharedFfts.get(j=i-1).key<nkey)i=j;
					sharedFfts.add(i,keyed);
					
					return fft;
				}
			}
			FFTBluestein result = getNewFft(n);
			Any.Keyed<Integer,FFTBluestein> keyed = new Any.Keyed<>(INITIAL_KEY,result);
			int i=len;
			if(len==MAX_SHARED_FFTS)
				sharedFfts.set(i=len-1, keyed);
			else
				sharedFfts.add(keyed);
			
			//Backwards insertion sort, moves further back to protect against immediate deletion
			int j;
			sharedFfts.remove(i);
			while(i>0 && sharedFfts.get(j=i-1).key<=INITIAL_KEY)i=j;
			sharedFfts.add(i,keyed);
			
			return result;
		}
	}
	
	/**
	 * Remove a shared FFT object if present
	 * <br>
	 * Only really used to free up memory
	 * 
	 * @param n the length
	 */
	public static void removeFft(int n){
		for(int i=0;i<sharedFfts.size();i++){
			Any.Keyed<Integer,FFTBluestein> keyed = sharedFfts.get(i);
			if(keyed.value.n==n){
				sharedFfts.remove(i);
				break;
			}
		}
	}
	
	/**
	 * Keep up to <i>limit</i> shared instances, and dereference the others
	 * <br>
	 * After this, the size of the list is guaranteed to be at most <i>limit</i>
	 * 
	 * @param limit the maximum to keep
	 */
	public static void trimTo(int limit){
		for(int i=sharedFfts.size()-1;i>=limit;i--)
			sharedFfts.remove(i);
	}
	
	/**
	 * Gets a new FFT object for any length
	 * 
	 * @param n the length
	 * @return an FFT object which can process arrays of length n
	 */
	public static FFTBluestein getNewFft(int n){
		return new FFTBluestein(n);
	}
	
	/**
	 * The length this instance is for
	 */
	public final int n;
	/**
	 * The convolution length
	 */
	public final int m;
	
	private final double[] cos,sin,kr,ki;
	
	public FFTBluestein(int n){
		this.n=n;
		if(n<3) throw new IllegalArgumentException("FFT length too short");
		m = Bits.gtPo2(n<<1);
		if(m<n) throw new IllegalArgumentException("FFT length too large; caused overflow");
		
		// Make sine and cosine tables, as well as convolution kernel
		cos = new double[n];
		sin = new double[n];
		long modulus = n<<1;
		double inc = Math.PI/n;
		kr = new double[m];
		ki = new double[m];
		for(int i=1,j=0;i<n;i++){
			j += (i<<1)-1;
			if(j>n)j-=modulus;
			double a = j*inc;
			kr[i] = kr[m-i] = cos[i] = Math.cos(a);
			ki[i] = ki[m-i] = sin[i] = Math.sin(a);
		}
		cos[0] = 1d;sin[0] = 0d;
		kr[0] = cos[0];ki[0] = sin[0];
		FFTRadix2.getFft(Bits.binLog(m)).fftUnsafe(kr, ki);
	}

	@Override
	public boolean checkBounds(int length) {
		return length==n;
	}
	
	@Override
	protected void scale(double[] real,double[] imaginary,int iterations) {
		double mult = Math.pow(n*(double)m*m, -0.5d*iterations);
		for(int i=0;i<n;i++){
			real[i] *= mult;
			imaginary[i] *= mult;
		}
	}

	@Override
	public void fftUnsafe(double[] real, double[] imaginary) {
		// Preliminary transform
		double[] ar = new double[m], ai = new double[m];
		for(int i=0;i<n;i++){
			double x = real[i], y = imaginary[i], cs = cos[i], sn = sin[i];
			ar[i] = x*cs+y*sn;
			ai[i] = y*cs-x*sn;
		}
		// Convolution
		FFTRadix2 transformer = FFTRadix2.getFft(Bits.binLog(m));
		transformer.fftUnsafe(ar, ai);
		for(int i=0;i<m;i++){
			double xr = ar[i], xi = ai[i], yr = kr[i], yi = ki[i];
			ar[i] = xr*yr-xi*yi;
			ai[i] = xi*yr+xr*yi;
		}
		transformer.fftUnsafe(ai, ar);
		// Copy back
		for(int i=0;i<n;i++){
			double x = ar[i], y = ai[i], cs = cos[i], sn = sin[i];
			real[i] = x*cs+y*sn;
			imaginary[i] = y*cs-x*sn;
		}
	}
	
	public String toString(){
		return "<Bluestein FFT for N="+n+">";
	}

}
