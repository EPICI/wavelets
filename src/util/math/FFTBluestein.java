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
import java.util.*;

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
	 * Determines the maximum number of Bluestein FFT objects to keep
	 * even if they aren't being used, because they might be common
	 * <br>
	 * After this many are reached, the least used ones are removed
	 * <br>
	 * Higher = more memory used and slower to access, but less redundant
	 * work done because memoization
	 */
	public static final int REMOVE_THRESHOLD = 1<<6;
	
	/**
	 * The maximum to leave after a default removal
	 */
	public static final int KEEP_LIMIT = REMOVE_THRESHOLD>>1;
	
	/**
	 * Shared, cached FFTs
	 * <br>
	 * Loosely maintained to length at most KEEP_LIMIT by deleting stuff
	 * when there are at least REMOVE_THRESHOLD objects
	 */
	private static HashMap<Integer,Keyed> sharedFfts = new HashMap<>();
	
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
			Keyed keyed = sharedFfts.get(n);
			if(keyed==null){
				keyed = new Keyed(getNewFft(n));
				sharedFfts.put(n, keyed);
			}
			if(sharedFfts.size()>=REMOVE_THRESHOLD)trimTo(KEEP_LIMIT);
			keyed.key++;
			return keyed.fft;
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
		sharedFfts.remove(n);
	}
	
	/**
	 * Keep up to <i>limit</i> shared instances, and dereference the others
	 * <br>
	 * After this, the size of the list is guaranteed to be at most <i>limit</i>
	 * 
	 * @param limit the maximum to keep
	 */
	public static void trimTo(int limit){
		if(limit<0)throw new IllegalArgumentException("Limit ("+limit+") cannot be negative");
		HashMap<Integer,Keyed> cache = sharedFfts;
		Keyed[] array = cache.values().toArray(new Keyed[0]);
		int csize = array.length;
		if(csize<=limit)return;
		Arrays.sort(array,I_KEYED_COMPARE);
		HashMap<Integer,Keyed> replace = new HashMap<>();
		for(int i=0;i<limit;i++){
			Keyed k = array[i];
			replace.put(k.fft.n, k);
		}
		sharedFfts = replace;
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
	 * Inverse keyed FFT comparator
	 */
	private static final Comparator<Keyed> I_KEYED_COMPARE = new Comparator<Keyed>(){
		public int compare(Keyed a,Keyed b){
			return Integer.compare(b.key, a.key);
		}
	};
	
	/**
	 * Keyed FFT used by cache
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	private static class Keyed{
		/**
		 * Retrieve count
		 */
		public int key;
		/**
		 * Referenced FFT
		 */
		public FFTBluestein fft;
		/**
		 * Convenience constructor
		 * 
		 * @param given the FFT to track
		 */
		public Keyed(FFTBluestein given){
			key=0;
			fft=given;
		}
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
			j-=modulus&((j-n)>>-1);//Branchless
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
