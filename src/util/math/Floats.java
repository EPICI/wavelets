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
	 * 
	 * @param a the first value
	 * @param b the second value
	 * @param epsilon custom epsilon value
	 * @return true if they are absolutely or relatively close
	 */
	public static boolean isNear(double a,double b,double epsilon){
		if(Math.abs(a-b)<epsilon)return true;
		double r = a/b;
		return (1d-epsilon)<=r && r<=(1d+epsilon);
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
	 * 
	 * @param a the first value
	 * @param b the second value
	 * @param epsilon custom epsilon value
	 * @return true if they are absolutely or relatively close
	 */
	public static boolean isNear(float a,float b,float epsilon){
		if(Math.abs(a-b)<epsilon)return true;
		float r = a/b;
		return (1f-epsilon)<=r && r<=(1f+epsilon);
	}
}
