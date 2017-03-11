 /*
 *	Copyright 2006-2007 Columbia University.
 *
 *	This file is part of MEAPsoft.
 *
 *	MEAPsoft is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License version 2 as
 *	published by the Free Software Foundation.
 *
 *	MEAPsoft is distributed in the hope that it will be useful, but
 *	WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with MEAPsoft; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *	02110-1301 USA
 *
 *	See the file "COPYING" for the text of the license.
 */

package utils;

import java.util.*;//Only used for testing

/**
 * Fast Fourier Transform
 * <br>
 * From: https://www.ee.columbia.edu/~ronw/code/MEAPsoft/doc/html/FFT_8java-source.html
 * <br>
 * Optimized further by EPICI
 * 
 * @author Columbia University (see license)
 * @version 1.0
 */
public class FFT {
	
	private static FFT[] sharedFFTs = new FFT[29];
	
	/**
	 * Get shared FFT object for known power of 2
	 * 
	 * @param m the exponent of 2
	 * @return an FFT object which can process arrays of length 1&lt;&lt;m
	 */
	public static FFT getFft(int m){
		if(m<1){
			throw new IllegalArgumentException("FFT length exponent "+Integer.toString(m)+" is too small. Minimum value is 1.");
		}else if(m>30){
			throw new IllegalArgumentException("FFT length exponent "+Integer.toString(m)+" is too large. Minimum value is 30.");
		}else{
			int index = m-1;
			if(sharedFFTs[index]==null){
				sharedFFTs[index]=getNewFft(m);
			}
			return sharedFFTs[index];
		}
	}
	
	/**
	 * Dereference a shared FFT object
	 * Only really used to free up memory
	 * 
	 * @param m the exponent of 2
	 */
	public static void removeFft(int m){
		//Fail-safe
		if(m>=1&&m<=30){
			sharedFFTs[m-1]=null;
		}
	}
	
	/**
	 * Gets a new FFT object for a known power of 2
	 * Assumed valid (checks should be done elsewhere)
	 * 
	 * @param m the exponent of 2
	 * @return an FFT object which can process arrays of length 1&lt;&lt;m
	 */
	public static FFT getNewFft(int m){
		return new FFT(1<<m);
	}

	private int n, m;
	
	// Lookup tables.	Only need to recompute when size of FFT changes.
	private double[] cos;
	private double[] sin;
	private int[] bitreversea;
	private int[] bitreverseb;
	private int bitreversecount;

	//private double[] window;
	
	public FFT(int n) {
		this.n = n;

		/*
		 * Make sure n is a power of 2
		 * Also make sure it's 2 or larger
		 */
		if(n<2 || !BitUtils.isPo2(n))
			throw new IllegalArgumentException("FFT length must be power of 2");
		
		this.m = BitUtils.binLog(n);//Modified to be faster

		// precompute tables
		cos = new double[n/2];
		sin = new double[n/2];

//		 for(int i=0; i<n/4; i++) {
//			 cos[i] = Math.cos(-2*Math.PI*i/n);
//			 sin[n/4-i] = cos[i];
//			 cos[n/2-i] = -cos[i];
//			 sin[n/4+i] = cos[i];
//			 cos[n/2+i] = -cos[i];
//			 sin[n*3/4-i] = -cos[i];
//			 cos[n-i]	 = cos[i];
//			 sin[n*3/4+i] = -cos[i];				
//		 }

		for(int i=0; i<n/2; i++) {
			cos[i] = Math.cos(-2*Math.PI*i/n);
			sin[i] = Math.sin(-2*Math.PI*i/n);
		}
		
		int[] positions = new int[n];
		int i;
		for(i=0;i<n;i++){
			positions[i]=i;
		}
		
		/*
		 * Bit reverse would normally take O(nlogn) or O(nloglogn) at best
		 * Doing it once and creating a lookup table means the first part of FFT,
		 * the bit reverse, is O(n), which is a pretty big deal
		 */
		int j,n1,n2,si,sj;
		j = 0;
		n2 = n/2;
		for (i=1; i < n - 1; i++) {
			n1 = n2;
			while ( j >= n1 ) {
				j = j - n1;
				n1 = n1/2;
			}
			j = j + n1;
		
			if (i < j) {
				si = positions[i];
				sj = positions[j];
				positions[i] = sj;
				positions[j] = si;
			}
		}
		bitreversecount = 0;
		for(i=0;i<n;i++){
			j = positions[i];
			if(i<j){
				bitreversecount++;
			}
		}
		bitreversea = new int[bitreversecount];
		bitreverseb = new int[bitreversecount];
		int index = 0;
		for(i=0;i<n;i++){
			j = positions[i];
			if(i<j){
				bitreversea[index]=i;
				bitreverseb[index]=j;
				index++;
			}
		}

		//makeWindow();
	}

	/*protected void makeWindow() {
		// Make a blackman window:
		// w(n)=0.42-0.5cos{(2*PI*n)/(N-1)}+0.08cos{(4*PI*n)/(N-1)};
		window = new double[n];
		for(int i = 0; i < window.length; i++)
			window[i] = 0.42 - 0.5 * Math.cos(2*Math.PI*i/(n-1)) 
				+ 0.08 * Math.cos(4*Math.PI*i/(n-1));
	}
	
	public double[] getWindow() {
		return window;
	}*/


	/***************************************************************
	* fft.c
	* Douglas L. Jones 
	* University of Illinois at Urbana-Champaign 
	* January 19, 1992 
	* http://cnx.rice.edu/content/m12016/latest/
	* 
	*	 fft: in-place radix-2 DIT DFT of a complex input 
	* 
	*	 input: 
	* n: length of FFT: must be a power of two 
	* m: n = 2**m 
	*	 input/output 
	* x: double array of length n with real part of data 
	* y: double array of length n with imag part of data 
	* 
	*	 Permission to copy and use this program is granted 
	*	 as long as this header is included. 
	*
	* @param x the real array
	* @param y the matching imaginary array
	****************************************************************/
	public void fft(double[] x, double[] y)
	{
		int i,j,k,n1,n2,a;
		double c,s,t1,t2;
	
	
		// Bit-reverse
		/*j = 0;
		n2 = n/2;
		for (i=1; i < n - 1; i++) {
			n1 = n2;
			while ( j >= n1 ) {
				j = j - n1;
				n1 = n1/2;
			}
			j = j + n1;
		
			if (i < j) {
				t1 = x[i];
				x[i] = x[j];
				x[j] = t1;
				t1 = y[i];
				y[i] = y[j];
				y[j] = t1;
			}
		}*/
		for(i=0;i<bitreversecount;i++){
			int ia = bitreversea[i];
			int ib = bitreverseb[i];
			t1 = x[ia];
			t2 = x[ib];
			x[ia] = t2;
			x[ib] = t1;
			t1 = y[ia];
			t2 = y[ib];
			y[ia] = t2;
			y[ib] = t1;
		}

		// FFT
		n1 = 0;
		n2 = 1;
	
		for (i=0; i < m; i++) {
			n1 = n2;
			n2 <<= 1;
			a = 0;
		
			for (j=0; j < n1; j++) {
				c = cos[a];
				s = sin[a];
				a +=	1 << (m-i-1);

				for (k=j; k < n; k=k+n2) {
					t1 = c*x[k+n1] - s*y[k+n1];
					t2 = s*x[k+n1] + c*y[k+n1];
					x[k+n1] = x[k] - t1;
					y[k+n1] = y[k] - t2;
					x[k] = x[k] + t1;
					y[k] = y[k] + t2;
				}
			}
		}
		
		/*
		 * After two iterations, value would normally be n times larger
		 * Dividing by square root of n each time will make it so that
		 * inverse FFT after FFT should give the original data,
		 * with floating point rounding errors of course
		 */
		double mult = Math.pow(n, -0.5d);
		for(i=0;i<n;i++){
			x[i]*=mult;
			y[i]*=mult;
		}
	}
	/**
	 * Inverse FFT
	 * <br>
	 * Conveniently swapping the real and imaginary parts will do this
	 * 
	 * @param x the real array
	 * @param y the matching imaginary array
	 */
	public void ifft(double[] x,double[] y){
		fft(y,x);
	}
	/**
	 * FFT shorthand
	 * 
	 * @param xy an array containing two other arrays, first the real, then the imaginary
	 */
	public void fft(double[][] xy){//Convenience method
		fft(xy[0],xy[1]);
	}
	/**
	 * Inverse FFT shorthand
	 * 
	 * @param xy an array containing two other arrays, first the real, then the imaginary
	 */
	public void ifft(double[][] xy){//Convenience method
		ifft(xy[0],xy[1]);
	}




	/**
	 * Main method, only for testing
	 * 
	 * @param args ignored
	 */
	public static void main(String[] args) {
		int N = 1<<7;
		Random random = new Random();

		FFT fft = getFft(7);

		double[] re = new double[N];
		double[] im = new double[N];

		System.out.println("Impulse");
		re[0] = 1; im[0] = 0;
		for(int i=1; i<N; i++)
			re[i] = im[i] = 0;
		beforeAfter(fft, re, im);

		System.out.println("Nyquist");
		for(int i=0; i<N; i++) {
			re[i] = Math.pow(-1, i);
			im[i] = 0;
		}
		beforeAfter(fft, re, im);

		System.out.println("Sine");
		for(int i=0; i<N; i++) {
			re[i] = Math.cos(8*Math.PI*i / N);
			im[i] = 0;
		}
		beforeAfter(fft, re, im);

		System.out.println("Ramp");
		for(int i=0; i<N; i++) {
			re[i] = i;
			im[i] = 0;
		}
		beforeAfter(fft, re, im);

		System.out.println("Saw (8)");
		for(int i=0; i<N; i++) {
			re[i] = i%8-3.5d;
			im[i] = 0;
		}
		beforeAfter(fft, re, im);

		System.out.println("Saw (4)");
		for(int i=0; i<N; i++) {
			re[i] = i%4-1.5d;
			im[i] = 0;
		}
		beforeAfter(fft, re, im);

		System.out.println("Pulse (4)");
		for(int i=0; i<N; i++) {
			re[i] = i%4==0?1d:0d;
			im[i] = 0;
		}
		beforeAfter(fft, re, im);

		System.out.println("Noise");
		for(int i=0; i<N; i++) {
			re[i] = random.nextDouble()-0.5d;
			im[i] = 0;
		}
		beforeAfter(fft, re, im);
		
		removeFft(7);

		doBenchmark(4,10000000);
		doBenchmark(5,5000000);
		doBenchmark(6,2000000);
		doBenchmark(7,1000000);
		doBenchmark(8,500000);
		doBenchmark(9,200000);
		doBenchmark(12,5000);
		doBenchmark(16,200);
		doBenchmark(20,2);
		doBenchmark(22,1);
	}

	protected static void beforeAfter(FFT fft, double[] re, double[] im) {
		System.out.println("Before: ");
		printReIm(re, im);
		fft.fft(re, im);
		System.out.println("After: ");
		printReIm(re, im);
		fft.ifft(re, im);
		System.out.println("Inverse: ");
		printReIm(re, im);
	}

	protected static void printReIm(double[] re, double[] im) {
		System.out.print("Re: [");
		for(int i=0; i<re.length; i++)
			System.out.print(((int)(re[i]*1000)/1000.0) + " ");

		System.out.print("]\nIm: [");
		for(int i=0; i<im.length; i++)
			System.out.print(((int)(im[i]*1000)/1000.0) + " ");

		System.out.println("]");
	}
	
	protected static void doBenchmark(int M, double iter){
		int N = 1<<M;
		FFT fft = getFft(M);
		double[] re = new double[N];
		double[] im = new double[N];
		double modulus = M*1.77913d+0.414793d;
		double offset = (modulus-1d)*-0.5d;
		for(int i=0; i<N; i++) {
			re[i] = i%modulus-offset;
			im[i] = 0;
		}

		long time = System.currentTimeMillis();
		for(int i=0; i<iter; i++)
			fft.fft(re,im);
		time = System.currentTimeMillis() - time;
		double times = N/44100d;
		double iterms = time/iter;
		System.out.println("Averaged " + iterms + "ms per iteration (N = 2^"+M+" = "+N+", "+times+"s at 44100Hz, ratio="+(1000d*times/iterms)+")");
		removeFft(M);
	}
}