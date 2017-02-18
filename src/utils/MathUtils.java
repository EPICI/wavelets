package utils;

import java.util.Arrays;

/**
 * Utility class containing everything math related
 * 
 * @author EPICI
 * @version 1.0
 */
public final class MathUtils {
	
	/**
	 * Default epsilon value used for doubles
	 * <br>
	 * Double has 53 mantissa bits, 2^-53 is around 1.1x10^-16, so 10^-12 is a safe value to use
	 */
	public static final double D_EPSILON = 1e-12;
	/**
	 * Default epsilon value used for floats
	 * <br>
	 * Float has 24 mantissa bits, 2^-24 is around 6.0x10^-8, so 10^-6 is a safe value to use
	 */
	public static final double F_EPSILON = 1e-6;
	
	//Disallow invoking constructor
	private MathUtils(){}
	
	/**
	 * Standard 2 point bezier
	 * <br>
	 * <a href="https://en.wikipedia.org/wiki/B%C3%A9zier_curve#Linear_B.C3.A9zier_curves">Wikipedia</a>
	 * 
	 * @param a the first value to interpolate between
	 * @param b the second value to interpolate between
	 * @param t interpolation value, must be between 0 and 1
	 * @return the interpolated value
	 */
	public static double bezier2(double a,double b,double t){
		//Total of 3 floating point operations
		//      1 2  3
		return a+t*(b-a);
	}
	/**
	 * Bezier with more points
	 * <br>
	 * Redirects to the one that takes an array
	 * 
	 * @param t interpolation value, must be between 0 and 1
	 * @param ds values to interpolate between
	 * @return the interpolated value
	 */
	public static double bezier(double t,double... ds){
		return bezier(ds,t);
	}
	/**
	 * Bezier with more points
	 * <br>
	 * Very inefficient, uses naive recursive approach
	 * <br>
	 * TODO please someone replace with <a href="https://en.wikipedia.org/wiki/De_Casteljau's_algorithm">De Casteljau's</a>
	 * 
	 * @param t interpolation value, must be between 0 and 1
	 * @param ds values to interpolate between
	 * @return the interpolated value
	 */
	public static double bezier(double[] ds,double t){
		int count = ds.length;
		switch(count){
		case 0:
		case 1:throw new IllegalArgumentException("Must have at least two items to interpolate between");
		case 2:return bezier2(ds[0],ds[1],t);
		default:return bezier2(bezier(Arrays.copyOfRange(ds, 0, count-1),t),bezier(Arrays.copyOfRange(ds, 1, count),t),t);
		}
	}
	/**
	 * Fast bezier4 for (a,a,b,b)
	 * <br>
	 * Polynomial expanded and refactored to do far less work
	 * 
	 * @param a the first point to interpolate between
	 * @param b the second point to interpolate between
	 * @param t the interpolation value, must be between 0 and 1
	 * @return the interpolated value
	 */
	public static double bezier2to4(double a,double b,double t){
		//Total of 7 floating point operations
		//      1 2   3 4   5  6  7
		return t*t*(2d*t-3d)*(a-b)+a;
	}

	/**
	 * Checks if two values are absolutely near, uses default double epsilon value
	 * <br>
	 * Use this instead of equality check
	 * 
	 * @param a the first value
	 * @param b the second value
	 * @return true if the two values are close
	 */
	public static boolean isNear(double a,double b){
		return isNear(a,b,D_EPSILON);
	}
	/**
	 * Checks if two values are absolutely near, uses given epsilon value
	 * <br>
	 * Use this instead of equality check
	 * 
	 * @param a the first value
	 * @param b the second value
	 * @param epsilon custom epsilon value
	 * @return true if |a-b|<epsilon
	 */
	public static boolean isNear(double a,double b,double epsilon){
		return Math.abs(a-b)<epsilon;
	}
	/**
	 * Checks if two values are absolutely near, uses default float epsilon value
	 * <br>
	 * Use this instead of equality check
	 * 
	 * @param a the first value
	 * @param b the second value
	 * @return true if the two values are close
	 */
	public static boolean isNear(float a,float b){
		return isNear(a,b,F_EPSILON);
	}
	/**
	 * Checks if two values are absolutely near, uses given epsilon value
	 * <br>
	 * Use this instead of equality check
	 * 
	 * @param a the first value
	 * @param b the second value
	 * @param epsilon custom epsilon value
	 * @return true if |a-b|<epsilon
	 */
	public static boolean isNear(float a,float b,float epsilon){
		return Math.abs(a-b)<epsilon;
	}
}