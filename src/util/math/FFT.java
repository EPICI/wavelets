package util.math;

import java.text.*;
import java.util.*;
import util.*;

/**
 * A Fast Fourier Transform object
 * <br>
 * Depending on the algorithm, it may not always work,
 * but when it does, it guarantees O(NlogN) running time,
 * though the constant may be bad
 * <br>
 * Not thread safe
 * <br>
 * Currently implemented/supported FFTs:
 * <ul>
 * <li>Radix-2 DIT DFT ({@link FFTRadix2}) - chosen for powers of 2</li>
 * <li>Bluestein FFT ({@link FFTBluestein}) - chosen for everything else</li>
 * </ul>
 * 
 * @author EPICI
 * @version 1.0
 */
public abstract class FFT {
	
	/**
	 * Automatically get a usable object to perform the FFT with
	 * and do the FFT in place
	 * <br>
	 * Not thread safe
	 * <br>
	 * Fastest case is for powers of 2 using radix-2 FFT, if exact bounds do
	 * not need to be preserved, it's advantageous to zero-pad to a power of 2
	 * length and use {@link FFTRadix2} instead
	 * 
	 * @param real the real array
	 * @param imaginary the matching imaginary array
	 */
	public static void adaptiveFft(double[] real,double[] imaginary){
		int nr = real.length, ni = imaginary.length;
		if(nr!=ni)throw new IllegalArgumentException("FFT length mismatch ("+nr+","+ni+")");
		getAdaptiveFft(nr).fft(real, imaginary);
	}
	
	/**
	 * Get a usable FFT object for this specific length
	 * 
	 * @param n the desired length
	 * @return an FFT object for n only
	 */
	public static FFT getAdaptiveFft(int n){
		if(n<2)throw new IllegalArgumentException("FFT length too short ("+n+"<2)");
		if(Bits.isPo2(n)){
			return FFTRadix2.getFft(Bits.binLog(n));
		}else{
			return FFTBluestein.getFft(n);
		}
	}
	
	/**
	 * Is it a valid length for this FFT?
	 * <br>
	 * If not, and the user attempts to do FFT anyways,
	 * bad things happen
	 * 
	 * @param length the length to check
	 * @return true if valid
	 */
	public abstract boolean checkBounds(int length);
	
	/**
	 * Subclasses should implement this
	 * <br>
	 * In-place FFT
	 * <br>
	 * Both array lengths are expected to be correct
	 * <br>
	 * Does not scale
	 * <br>
	 * Behaviour varies between FFTs
	 * 
	 * @param real the real array
	 * @param imaginary the matching imaginary array
	 */
	public abstract void fftUnsafe(double[] real,double[] imaginary);
	
	/**
	 * Scale both arrays
	 * <br>
	 * Subclasses with different scaling behaviour should override
	 * 
	 * @param real the real array
	 * @param imaginary the matching imaginary array
	 * @param iterations the number of "missed" FFT iterations
	 */
	protected void scale(double[] real,double[] imaginary,int iterations){
		FFT.scale(real, iterations);
		FFT.scale(imaginary, iterations);
	}
	
	/**
	 * In-place FFT
	 * <br>
	 * Both array lengths are expected to be correct
	 * <br>
	 * Calls fftUnsafe, then scale with iterations=1, the general contract is that
	 * after calling fftUnsafe N times, calling scale with iterations=N should
	 * correctly rescale the data
	 * 
	 * @param real the real array
	 * @param imaginary the matching imaginary array
	 */
	protected void fftInternal(double[] real,double[] imaginary){
		fftUnsafe(real,imaginary);
		scale(real,imaginary,1);
	}
	
	/**
	 * Visible in-place FFT
	 * <br>
	 * Will throw an {@link IllegalArgumentException} if the FFT cannot process the data
	 * 
	 * @param real the real array
	 * @param imaginary the matching imaginary array
	 */
	public void fft(double[] real,double[] imaginary){
		if(real.length==imaginary.length && checkBounds(real.length))
			fftInternal(real,imaginary);
		else
			throw new IllegalArgumentException("Invalid length for FFT ("+real.length+","+imaginary.length+")");
	}
	
	/**
	 * Inverse FFT
	 * <br>
	 * Conveniently swapping the real and imaginary parts will do this
	 * <br>
	 * If the FFT does not divide all values by the square root of N,
	 * these will not equal the original values
	 * 
	 * @param real the real array
	 * @param imaginary the matching imaginary array
	 */
	public void ifft(double[] real,double[] imaginary){
		if(checkBounds(real.length) && checkBounds(imaginary.length))
			fftInternal(imaginary,real);
		else
			throw new IllegalArgumentException("Invalid length for FFT ("+real.length+","+imaginary.length+")");
	}
	
	/**
	 * Convenience method for passing both arrays bundled in one
	 * 
	 * @param both first the real array, then the imaginary array
	 */
	public void fft(double[][] both){
		fft(both[0],both[1]);
	}
	
	/**
	 * Convenience method for passing both arrays bundled in one
	 * 
	 * @param both first the real array, then the imaginary array
	 */
	public void fft(Any.O2<double[], double[]> both){
		fft(both.a,both.b);
	}
	
	/**
	 * Convenience method for passing both arrays bundled in one
	 * 
	 * @param both first the real array, then the imaginary array
	 */
	public void ifft(double[][] both){
		ifft(both[0],both[1]);
	}
	
	/**
	 * Convenience method for passing both arrays bundled in one
	 * 
	 * @param both first the real array, then the imaginary array
	 */
	public void ifft(Any.O2<double[], double[]> both){
		ifft(both.a,both.b);
	}
	
	/**
	 * Scales down the array values
	 * 
	 * @param array the array to scale
	 * @param iterations the total number of FFTs and IFFTs done prior,
	 * so after one round of FFT + IFFT, pass 2
	 */
	public static void scale(double[] array,int iterations){
		final int n = array.length;
		final double mult = Math.pow(n, -0.5d*iterations);
		for(int i=0;i<n;i++)
			array[i] *= mult;
	}
	
	// --- Testing code ---
	/**
	 * Main method, only for testing
	 * 
	 * @param args ignored
	 */
	public static void main(String[] args) {
		for(int N:new int[]{32,60,64,67}){
			Random random = new Random();

			FFT fft = getAdaptiveFft(N);
			System.out.println("\n----- N="+N+" -----");

			double[] re = new double[N];
			double[] im = new double[N];

			System.out.println("Impulse");
			re[0] = 1; im[0] = 0;
			for(int i=1; i<N; i++)
				re[i] = im[i] = 0;
			beforeAfter(fft, re, im);

			System.out.println("Impulse 4");
			for(int i=0; i<N; i++)
				re[i] = im[i] = 0;
			re[4]=1;
			beforeAfter(fft, re, im);

			System.out.println("Impulse 4,N-4");
			for(int i=0; i<N; i++)
				re[i] = im[i] = 0;
			re[4]=1;re[N-4]=1;
			beforeAfter(fft, re, im);

			System.out.println("Nyquist");
			for(int i=0; i<N; i++) {
				re[i] = Math.pow(-1, i);
				im[i] = 0;
			}
			beforeAfter(fft, re, im);

			System.out.println("Cosine 4x");
			for(int i=0; i<N; i++) {
				re[i] = Math.cos(4*Math.PI*i / N);
				im[i] = 0;
			}
			beforeAfter(fft, re, im);

			System.out.println("Cosine 8x");
			for(int i=0; i<N; i++) {
				re[i] = Math.cos(8*Math.PI*i / N);
				im[i] = 0;
			}
			beforeAfter(fft, re, im);

			System.out.println("Cosine 6x");
			for(int i=0; i<N; i++) {
				re[i] = Math.cos(6*Math.PI*i / N);
				im[i] = 0;
			}
			beforeAfter(fft, re, im);

			System.out.println("Cosine 6x+10x");
			for(int i=0; i<N; i++) {
				re[i] = Math.cos(6*Math.PI*i / N)+Math.cos(10*Math.PI*i / N);
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

			System.out.println("Saw (6)");
			for(int i=0; i<N; i++) {
				re[i] = i%6-2.5d;
				im[i] = 0;
			}
			beforeAfter(fft, re, im);

			System.out.println("Saw (7)");
			for(int i=0; i<N; i++) {
				re[i] = i%7-3d;
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
		}

		System.out.println("--- Benchmarks ---");
		System.out.println("Benchmarks also include how long the audio would be if that were sampled sound, and how many times the FFT and IFFT could be done in that timespan with these averages");
		for(int[] t:new int[][]{
			// Throw powers of 2 at radix-2
			new int[]{1<<4,0},
			new int[]{1<<5,0},
			new int[]{1<<6,0},
			new int[]{1<<7,0},
			new int[]{1<<8,0},
			new int[]{1<<9,1},
			new int[]{1<<12,2},
			new int[]{1<<16,3},
			new int[]{1<<20,4},
			new int[]{1<<22,4},
			
			// Test batch 1: near 32
			new int[]{31,1},// 31
			new int[]{32,1},// 2...
			new int[]{35,1},// 5 7
			new int[]{36,1},// 2 2 3 3
			
			// Test batch 2: near 128
			new int[]{120,2},// 2 2 2 3 5
			new int[]{121,2},// 11 11
			new int[]{125,2},// 5 5 5
			new int[]{127,2},// 127
			new int[]{128,2},// 128
			new int[]{129,2},// 3 43
			
			// Test batch 3: near 2048
			new int[]{2021,4},// 43 47
			new int[]{2023,4},// 7 17 17
			new int[]{2025,4},// 3 3 3 3 5 5
			new int[]{2027,4},// 2027
			new int[]{2042,4},// 2 1021
			new int[]{2048,4},// 2...
			new int[]{2051,4},// 7 293
		}){
			doBenchmark(t[0],(100000000/t[0])>>t[1]);
		}
	}

	private static void beforeAfter(FFT fft, double[] re, double[] im) {
		double f = re[0];
		System.out.println("Before: ");
		printReIm(re, im);
		fft.fft(re, im);
		System.out.println("After: ");
		printReIm(re, im);
		fft.ifft(re, im);
		System.out.println("Inverse: ");
		printReIm(re, im);
		double c = re[0];
		System.out.println("Error of "+Math.abs(Math.log(c/f))+" relative, "+Math.abs(c-f)+" absolute");
	}

	private static void printReIm(double[] re, double[] im) {
		System.out.print("Re: [");
		for(int i=0; i<re.length; i++)
			System.out.print((Math.round(re[i]*100d)/100d) + " ");

		System.out.print("]\nIm: [");
		for(int i=0; i<im.length; i++)
			System.out.print((Math.round(im[i]*100d)/100d) + " ");

		System.out.println("]");
		final int t=8;
		int[] is=new int[t];
		int j=0;
		for(int i=0;i<re.length;i++){
			if(Math.abs(re[i])>Floats.D_EPSILON||Math.abs(im[i])>Floats.D_EPSILON){
				if(j>=t){
					j=-1;
					break;
				}
				is[j++]=i;
			}
		}
		if(j>=0){
			for(int i=0;i<j;i++){
				System.out.print(is[i]);
				System.out.print(' ');
			}
			System.out.println();
		}
	}
	
	private static void doBenchmark(int N, double iter){
		FFT fft = getAdaptiveFft(N);
		double[] re = new double[N];
		double[] im = new double[N];
		double modulus = Math.sqrt(N)/(Math.log(N)+1d);
		double offset = -0.5d*modulus;
		
		for(int i=0; i<N; i++) {
			re[i] = i%modulus+offset;
			im[i] = 0;
		}

		long t1 = System.currentTimeMillis();
		for(int i=0; i<iter; i++){
			fft.fft(re,im);
			fft.ifft(re,im);
		}
		t1 = System.currentTimeMillis() - t1;
		
		for(int i=0; i<N; i++) {
			re[i] = i%modulus-offset;
			im[i] = 0;
		}
		
		long t2 = System.currentTimeMillis();
		for(int i=0; i<iter; i++){
			fft.fftUnsafe(re,im);
			fft.fftUnsafe(im,re);
			fft.scale(re, im, 2);
		}
		t2 = System.currentTimeMillis() - t2;
		
		double times = N/44100d;
		double iterms1 = t1/iter, iterms2 = t2/iter;
		DecimalFormat format = new DecimalFormat("#00.0000");
		System.out.println("For N="+N+" ("+format.format(times)+"s at 44100Hz with "+fft+")");
		System.out.println("> Using regular API call:");
		System.out.println("> > "+format.format(iterms1)+"ms average per FFT/IFFT iteration");
		System.out.println("> > "+format.format(1000d*times/iterms1)+"x real time");
		System.out.println("> Using lazy-like optimizations:");
		System.out.println("> > "+format.format(iterms2)+"ms average per FFT/IFFT iteration");
		System.out.println("> > "+format.format(1000d*times/iterms2)+"x real time");
		System.out.println("> "+format.format(iterms1/iterms2)+"x improvement");
	}
}
