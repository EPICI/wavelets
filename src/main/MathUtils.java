package main;

import java.util.Arrays;

//Utility class
public final class MathUtils {
	
	//Constants used by "is near" check
	public static final double D_EPSILON = 1e-12;
	public static final double F_EPSILON = 1e-6;
	
	//Disallow invoking constructor
	private MathUtils(){}
	
	/*
	 * Standard bezier functions
	 */
	public static double bezier2(double a,double b,double t){
		return (1-t)*a+t*b;
	}
	public static double bezier(double t,double... ds){
		return bezier(ds,t);
	}
	public static double bezier(double[] ds,double t){
		int count = ds.length;
		switch(count){
		case 0:
		case 1:throw new IllegalArgumentException("Must have at least two items to interpolate between");
		case 2:return bezier2(ds[0],ds[1],t);
		default:return bezier2(bezier(Arrays.copyOfRange(ds, 0, count-1),t),bezier(Arrays.copyOfRange(ds, 1, count),t),t);
		}
	}

	/*
	 * Check if two values are near
	 */
	public static boolean isNear(double a,double b){
		return isNear(a,b,D_EPSILON);
	}
	public static boolean isNear(double a,double b,double epsilon){
		return Math.abs(a-b)<epsilon;
	}
	public static boolean isNear(float a,float b){
		return isNear(a,b,F_EPSILON);
	}
	public static boolean isNear(float a,float b,float epsilon){
		return Math.abs(a-b)<epsilon;
	}
}