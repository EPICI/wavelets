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

package util.math;

import util.Bits;

/**
 * Fast Fourier Transform
 * <br>
 * From: https://www.ee.columbia.edu/~ronw/code/MEAPsoft/doc/html/FFT_8java-source.html
 * <br>
 * Optimized further by EPICI
 * 
 * @author Columbia University (see license)
 * @author EPICI
 * @version 1.0
 */
public final class FFTRadix2 extends FFT {
	
	private static FFTRadix2[] sharedFFTs = new FFTRadix2[29];
	
	/**
	 * Get shared FFT object for known power of 2
	 * 
	 * @param m the exponent of 2
	 * @return an FFT object which can process arrays of length 1&lt;&lt;m
	 */
	public static FFTRadix2 getFft(int m){
		if(m<1){
			throw new IllegalArgumentException("FFT length exponent "+Integer.toString(m)+" is too small. Minimum value is 1.");
		}else if(m>30){
			throw new IllegalArgumentException("FFT length exponent "+Integer.toString(m)+" is too large. Maximum value is 30.");
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
	 * <br>
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
	 * <br>
	 * Assumed valid (checks should be done elsewhere)
	 * 
	 * @param m the exponent of 2
	 * @return an FFT object which can process arrays of length 1&lt;&lt;m
	 */
	public static FFTRadix2 getNewFft(int m){
		return new FFTRadix2(1<<m);
	}

	public final int n, m;
	
	// Lookup tables.	Only need to recompute when size of FFT changes.
	private double[] cos;
	private double[] sin;
	private int[] bitreversea;
	private int[] bitreverseb;
	private int bitreversecount;

	//private double[] window;
	
	public FFTRadix2(int n) {
		this.n = n;

		/*
		 * Make sure n is a power of 2
		 * Also make sure it's 2 or larger
		 */
		if(n<2 || !Bits.isPo2(n))
			throw new IllegalArgumentException("FFT length must be power of 2");
		
		this.m = Bits.binLog(n);//Modified to be faster

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
	}

	@Override
	public boolean checkBounds(int length) {
		return length==n;
	}

	/*
	**************************************************************
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
	@Override
	public void fftUnsafe(double[] x, double[] y) {
		int i,j,k,n1,n2,a;
		double c,s,t1,t2;
	
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
	}
	
	public String toString(){
		return "<Radix-2 DIT DFT for N="+n+">";
	}
}