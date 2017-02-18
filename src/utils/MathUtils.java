package utils;

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
	 * Uses De Casteljau's to do it in O(n^2) instead of O(2^n)
	 * <br>
	 * https://en.wikipedia.org/wiki/De_Casteljau's_algorithm"
	 * 
	 * @param t interpolation value, must be between 0 and 1
	 * @param ds values to interpolate between
	 * @return the interpolated value
	 */
	public static double bezier(double[] ds,double t){
		int count = ds.length;
		switch(count){
		case 0:throw new IllegalArgumentException("Must have at least two items to interpolate between");
		case 1:return ds[0];
		case 2:return bezier2(ds[0],ds[1],t);
		case 3:{
			double a = ds[0];
			double b = ds[1];
			double c = ds[2];
			double t1 = 1d - t;
			/*
			 * Hardcoded for n=3
			 * Total of 8+1=9 floating point operations
			 *       1  2  3 4  5  6 7 8
			 */
			return (a*t1+2d*b*t)*t1+c*t*t;
		}
		case 4:{
			double a = ds[0];
			double b = ds[1];
			double c = ds[2];
			double d = ds[3];
			double t1 = 1d - t;
			/*
			 * Hardcoded for n=4
			 * Total of 13+1=9 floating point operations
			 *       1  2  3 4  5  6  7   8 9 10 11 12 13
			 */
			return (a*t1+3d*b*t)*t1*t1+(3d*c*t1+d*t)*t*t;
		}
		case 5:{
			double a = ds[0];
			double b = ds[1];
			double c = ds[2];
			double d = ds[3];
			double e = ds[4];
			double t1 = 1d - t;
			double i = t1*t1;
			double j = t*t;
			double k = t1*t;
			/*
			 * Hardcoded for n=4
			 * Total of 14+4=18 floating point operations
			 *       1  2   3 4  5   6 7   8   9   10 11 12 13 14
			 */
			return (a*i + 4d*b*k + 6d*c*j) * i + (4d*d*k + e*j) * j;
		}
		default:{
			double t1 = 1d - t;
			int n1 = count - 1;
			int halfn = (n1>>1)+1;
			double[] choose = chooseDoubleRange(n1,halfn);
			double[] terms = new double[count];
			double p = 1d;
			for(int i=0;i<halfn;i++){
				terms[i] = ds[i] * choose[i] * p;
				p *= t;
			}
			for(int i=halfn;i<count;i++){
				terms[i] = ds[i] * choose[n1-i] * p;
				p *= t;
			}
			p = t1;
			for(int i=1;i<count;i++){
				terms[n1-i] *= p;
				p *= t1;
			}
			double sum = 0d;
			for(double v:terms){
				sum += v;
			}
			return sum;
		}
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
	 * Calculates n choose k in O(n) time
	 * 
	 * @param n n
	 * @param k k
	 * @return n choose k
	 */
	public static double chooseDouble(int n,int k){
		double numerator = 1d;
		double demoninator = 1d;
		for(int i=1;i<=k;i++){
			numerator *= n + 1 - i;
			demoninator *= i;
		}
		return numerator/demoninator;
	}
	
	/**
	 * Calculates n choose p for 0 &#x2264; p &#x2264; k
	 * 
	 * @param n n
	 * @param k k
	 * @return values for that range
	 */
	public static double[] chooseDoubleRange(int n,int k){
		int n1 = n+1;
		double product = 1d;
		double[] result = new double[k+1];
		result[0] = 1d;
		for(int i=1;i<=k;i++){
			product *= (n1-i)/i;
			result[i] = product;
		}
		return result;
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
	 * @return true if |a-b|&lt;epsilon
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
	 * @return true if |a-b|&lt;epsilon
	 */
	public static boolean isNear(float a,float b,float epsilon){
		return Math.abs(a-b)<epsilon;
	}
}