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
	public static final float F_EPSILON = 1e-6f;
	
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
	 * Edge case: smaller of (a,b) in magnitude multiplied by
	 * 1+epsilon is the larger, in that case negative will be
	 * true and positive will be false
	 * 
	 * @param a the first value
	 * @param b the second value
	 * @param epsilon custom epsilon value
	 * @return true if they are absolutely or relatively close
	 */
	public static boolean isNear(double a,double b,double epsilon){
		double eps1 = epsilon+1;
		return Math.abs(a-b)<epsilon || !((a*eps1>b)^(b*eps1>a));// Symmetric and simple!
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
	 * uses given float epsilon value
	 * <br>
	 * Use this instead of equality check
	 * <br>
	 * Edge case: smaller of (a,b) in magnitude multiplied by
	 * 1+epsilon is the larger, in that case negative will be
	 * true and positive will be false
	 * 
	 * @param a the first value
	 * @param b the second value
	 * @param epsilon custom epsilon value
	 * @return true if they are absolutely or relatively close
	 */
	public static boolean isNear(float a,float b,float epsilon){
		float eps1 = epsilon+1;
		return Math.abs(a-b)<epsilon || !((a*eps1>b)^(b*eps1>a));
	}
	
	/**
	 * The median value of a,b,c
	 * <br>
	 * More versatile than specific variations like constrain
	 * <br>
	 * If the ordering is somewhat predictable (ex. with constants)
	 * aim for a<b<c
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public static float median(float a,float b,float c){
		if(a>c){float d=a;a=c;c=d;}
		return Math.max(a, Math.min(c, b));
	}
	
	/**
	 * The median value of a,b,c
	 * <br>
	 * More versatile than specific variations like constrain
	 * <br>
	 * If the ordering is somewhat predictable (ex. with constants)
	 * aim for a<b<c
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public static double median(double a,double b,double c){
		if(a>c){double d=a;a=c;c=d;}
		return Math.max(a, Math.min(c, b));
	}
	
	/**
	 * Main method, used only for testing
	 * 
	 * @param args ignored
	 */
	public static void main(String[] args){
		//Near equals sanity tests
		System.out.println("--- Near equals, doubles ---");
		System.out.println("1e100\u22481e100+1e85="+isNear(1e100d,1e100d+1e85d)+" (expected true)");
		System.out.println("1e100\u22481e100+1e90="+isNear(1e100d,1e100d+1e90d)+" (expected false)");
		System.out.println("1e-15\u2248-1e-15="+isNear(1e-15d,-1e-15d)+" (expected true)");
		System.out.println("1e-10\u2248-1e-10="+isNear(1e-10d,-1e-10d)+" (expected false)");
		System.out.println("--- Near equals, floats ---");
		System.out.println("1e30\u22481e30+1e20="+isNear(1e30f,1e30f+1e20f)+" (expected true)");
		System.out.println("1e30\u22481e30+1e25="+isNear(1e30f,1e30f+1e25f)+" (expected false)");
		System.out.println("1e-10\u2248-1e-10="+isNear(1e-10f,-1e-10f)+" (expected true)");
		System.out.println("1e-5\u2248-1e-5="+isNear(1e-5f,-1e-5f)+" (expected false)");
		System.out.println("--- Median, doubles ---");
		System.out.println("median(1,2,3)="+median(1d,2d,3d)+","+median(1d,3d,2d)+","+median(2d,1d,3d)+","+median(2d,3d,1d)+","+median(3d,1d,2d)+","+median(3d,2d,1d)+" (expected 2,2,2,2,2,2)");
		System.out.println("median(-1e307,-1,9)="+median(-1e307d,-1d,9d)+","+median(-1e307d,9d,-1d)+","+median(-1d,-1e307d,9d)+","+median(-1d,9d,-1e307d)+","+median(9d,-1e307d,-1d)+","+median(9d,-1d,-1e307d)+" (expected -1,-1,-1,-1,-1,-1)");
		System.out.println("median(0,0,1e-307)="+median(0d,0d,1e-307d)+","+median(0d,1e-307d,0d)+","+median(1e-307d,0d,0d)+" (expected 0,0,0)");
		System.out.println("--- Median, floats ---");
		System.out.println("median(1,2,3)="+median(1f,2f,3f)+","+median(1f,3f,2f)+","+median(2f,1f,3f)+","+median(2f,3f,1f)+","+median(3f,1f,2f)+","+median(3f,2f,1f)+" (expected 2,2,2,2,2,2)");
		System.out.println("median(-1e38,-1,9)="+median(-1e38f,-1f,9f)+","+median(-1e38f,9f,-1f)+","+median(-1f,-1e38f,9f)+","+median(-1f,9f,-1e38f)+","+median(9f,-1e38f,-1f)+","+median(9f,-1f,-1e38f)+" (expected -1,-1,-1,-1,-1,-1)");
		System.out.println("median(0,0,1e-37)="+median(0f,0f,1e-37f)+","+median(0f,1e-37f,0f)+","+median(1e-37f,0f,0f)+" (expected 0,0,0)");
	}
}
