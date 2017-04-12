package util.math;

import util.Any;
import util.Any.O2;

/**
 * A Fast Fourier Transform object
 * <br>
 * Depending on the algorithm, it may not always work,
 * but when it does, it guarantees O(NlogN) running time,
 * though the constant may be bad
 * 
 * @author EPICI
 * @version 1.0
 */
public abstract class FFT {
	
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
	 * In-place FFT
	 * <br>
	 * Both array lengths are expected to be correct
	 * 
	 * @param real the real array
	 * @param imaginary the matching imaginary array
	 */
	protected abstract void fftInternal(double[] real,double[] imaginary);
	
	/**
	 * Visible in-place FFT
	 * <br>
	 * Will throw an {@link IllegalArgumentException} if the FFT cannot process the data
	 * 
	 * @param real the real array
	 * @param imaginary the matching imaginary array
	 */
	public void fft(double[] real,double[] imaginary){
		if(checkBounds(real.length) && checkBounds(imaginary.length))
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
}
