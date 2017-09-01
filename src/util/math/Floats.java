package util.math;

/**
 * Some generic floating point utilities
 * 
 * @author EPICI
 * @version 1.0
 */
public final class Floats {
	
	private Floats(){}
	
	/**
	 * Default comparison epsilon value used for doubles
	 * <br>
	 * Double has 53 mantissa bits, 2^-53 is around 1.1x10^-16, so 10^-12 is a safe value to use
	 */
	public static final double D_EPSILON = 1e-12;
	/**
	 * A tiny value that is still large enough to not mess up math
	 */
	public static final double D_TINY = 1e-30;
	/**
	 * Opposite of tiny: ridiculously large value
	 * <br>
	 * The number here is chosen so that this multiplied by the tiny value is still a very small value
	 */
	public static final double ID_TINY = 1e12;
	/**
	 * Default epsilon value used for floats
	 * <br>
	 * Float has 24 mantissa bits, 2^-24 is around 6.0x10^-8, so 10^-6 is a safe value to use
	 */
	public static final double F_EPSILON = 1e-6;
	
	/**
	 * Checks if two values are absolutely or relatively near,
	 * uses default double epsilon value
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
	 * Checks if two values are absolutely or relatively near,
	 * uses given double epsilon value
	 * <br>
	 * Use this instead of equality check
	 * <br>
	 * The order will only matter in edge cases
	 * 
	 * @param a the first value
	 * @param b the second value
	 * @param epsilon custom epsilon value
	 * @return true if they are absolutely or relatively close
	 */
	public static boolean isNear(double a,double b,double epsilon){
		if(Math.abs(a-b)<epsilon)return true;
		return Math.abs(a/b-1)<epsilon;
	}
	/**
	 * Checks if two values are absolutely or relatively near,
	 * uses default float epsilon value
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
	 * Checks if two values are absolutely or relatively near,
	 * uses default float epsilon value
	 * <br>
	 * Use this instead of equality check
	 * <br>
	 * The order will only matter in edge cases
	 * 
	 * @param a the first value
	 * @param b the second value
	 * @param epsilon custom epsilon value
	 * @return true if they are absolutely or relatively close
	 */
	public static boolean isNear(float a,float b,float epsilon){
		if(Math.abs(a-b)<epsilon)return true;
		return Math.abs(a/b-1)<epsilon;
	}
}
