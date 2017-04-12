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
	 * At the cost of one more operation, is more accurate
	 * 
	 * @param a the first value to interpolate between
	 * @param b the second value to interpolate between
	 * @param t interpolation value, must be between 0 and 1
	 * @return the interpolated value
	 */
	public static double accurateBezier2(double a,double b,double t){
		return (1d-t)*a+t*b;
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
	 * O(n) algorithm using cheats
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
			double l = 4d*k;
			/*
			 * Hardcoded for n=5
			 * Total of 12+5=17 floating point operations
			 *       1  2  3  4   5 6   7   8   9 10 11  12
			 */
			return (a*i + b*l + 6d*c*j) * i + (d*l + e*j) * j;
		}
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
		case 11:return decasteljauBezier(ds,t);
		default:{
			double t1 = 1d - t;
			int n1 = count - 1;
			int halfn = (n1>>1)+1;
			double[] choose = adaptiveChooseDoubleRange(n1,halfn);
			double[] terms = new double[count];
			double power = 1d;
			for(int i=0;i<halfn;i++){
				terms[i] = ds[i] * choose[i] * power;
				power *= t;
			}
			for(int i=halfn;i<count;i++){
				terms[i] = ds[i] * choose[n1-i] * power;
				power *= t;
			}
			power = t1;
			for(int i=1;i<count;i++){
				terms[n1-i] *= power;
				power *= t1;
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
	 * Actual De Casteljau's algorithm, has O(n^2) running time
	 * <br>
	 * https://en.wikipedia.org/wiki/De_Casteljau%27s_algorithm
	 * <br>
	 * Please don't use it, the only advantage it offers is stability,
	 * but honestly you shouldn't ever have hundreds of control points
	 * 
	 * @param ds double array contianing the points
	 * @param t the interpolation value, must be between 0 and 1
	 * @return the interpolated value
	 */
	public static double decasteljauBezier(double[] ds,double t){
		int n = ds.length;
		double[] result = Arrays.copyOf(ds, n);
		for(int i=n-1;i>0;i--){
			for(int j=0;j<i;j++){
				result[j]+=(result[j+1]-result[j])*t;
			}
		}
		return result[0];
	}
	
	/**
	 * More accurate but slower variant of De Casteljau's
	 * 
	 * @param ds double array contianing the points
	 * @param t the interpolation value, must be between 0 and 1
	 * @return the interpolated value
	 */
	public static double accurateDecasteljauBezier(double[] ds,double t){
		int n = ds.length;
		double[] result = Arrays.copyOf(ds, n);
		for(int i=n-1;i>0;i--){
			for(int j=0;j<i;j++){
				double a = result[j];
				double b = result[j+1];
				result[j] = (1d-t)*a+t*b;
			}
		}
		return result[0];
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
	 * <br>
	 * Breaks for very large n, under 29 is guaranteed safe
	 * 
	 * @param n n
	 * @param k k
	 * @return n choose k
	 */
	public static int chooseInt(int n,int k){
		int n1 = n+1;
		int product = 1;
		for(int i=1;i<=k;i++){
			product = product * (n1 - i) / i;
		}
		return product;
	}
	
	/**
	 * Calculates n choose p for 0 &#x2264; p &#x2264; k
	 * <br>
	 * Breaks for very large n, under 29 is guaranteed safe
	 * 
	 * @param n n
	 * @param k k
	 * @return values for that range
	 */
	public static int[] chooseIntRange(int n,int k){
		int n1 = n+1;
		int product = 1;
		int[] result = new int[k+1];
		result[0] = 1;
		for(int i=1;i<=k;i++){
			product = product*(n1-i)/i;
			result[i] = product;
		}
		return result;
	}
	
	/**
	 * Calculates n choose k in O(n) time
	 * <br>
	 * Breaks for very large n, under 60 is guaranteed safe
	 * 
	 * @param n n
	 * @param k k
	 * @return n choose k
	 */
	public static long chooseLong(int n,int k){
		int n1 = n+1;
		long product = 1;
		for(int i=1;i<=k;i++){
			product = product * (n1 - i) / i;
		}
		return product;
	}
	
	/**
	 * Calculates n choose p for 0 &#x2264; p &#x2264; k
	 * <br>
	 * Breaks for very large n, under 60 is guaranteed safe
	 * 
	 * @param n n
	 * @param k k
	 * @return values for that range
	 */
	public static long[] chooseLongRange(int n,int k){
		int n1 = n+1;
		int product = 1;
		long[] result = new long[k+1];
		result[0] = 1;
		for(int i=1;i<=k;i++){
			product = product*(n1-i)/i;
			result[i] = product;
		}
		return result;
	}
	
	/**
	 * Calculates n choose k in O(n) time
	 * <br>
	 * Always safe, but not necessarily accurate
	 * 
	 * @param n n
	 * @param k k
	 * @return n choose k
	 */
	public static double chooseDouble(int n,int k){
		int n1 = n+1;
		double product = 1d;
		for(int i=1;i<=k;i++){
			product = (product * (n1-i)) / i;
		}
		return product;
	}
	
	/**
	 * Calculates n choose p for 0 &#x2264; p &#x2264; k
	 * <br>
	 * Always safe, but not necessarily accurate
	 * 
	 * @param n n
	 * @param k k
	 * @return values for that range
	 */
	public static double[] chooseDoubleRange(int n,int k){
		int n1 = n+1;
		double product = 1d;
		double[] result = new double[k+1];
		result[0] = 1;
		for(int i=1;i<=k;i++){
			product = (product * (n1-i)) / i;
			result[i] = product;
		}
		return result;
	}
	
	/**
	 * Calculates n choose p for 0 &#x2264; p &#x2264; k
	 * <br>
	 * Adaptive
	 * 
	 * @param n n
	 * @param k k
	 * @return values for that range
	 */
	public static double[] adaptiveChooseDoubleRange(int n,int k){
		if(n>=60){
			return chooseDoubleRange(n,k);
		}else if(n>=29){
			long[] pre = chooseLongRange(n,k);
			double[] result = new double[pre.length];
			for(int i=0;i<pre.length;i++){
				result[i]=pre[i];
			}
			return result;
		}else if(n>0){
			int[] pre = chooseIntRange(n,k);
			double[] result = new double[pre.length];
			for(int i=0;i<pre.length;i++){
				result[i]=pre[i];
			}
			return result;
		}else{
			throw new IllegalArgumentException("n ("+n+") must be 1 or greater");
		}
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