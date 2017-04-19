package util.math;

/**
 * Some generic angle math utilities
 * <br>
 * All use radians
 * 
 * @author EPICI
 * @version 1.0
 */
public final class Angles {
	
	private Angles(){}
	
	/**
	 * Returns a value between -pi (inclusive) and pi (exclusive)
	 * congruent to the given value
	 * <br>
	 * Use this with plus and minus to get actual sums or differences
	 * 
	 * @param angle the original angle
	 * @return the constrained angle
	 */
	public static double limit(double angle){
		angle %= 2d*Math.PI;
		if(angle<=-Math.PI)return angle+2d*Math.PI;
		if(angle>Math.PI)return angle-2d*Math.PI;
		return angle;
	}
	
	/**
	 * Returns a value between 0 (inclusive) and 2pi (exclusive)
	 * congruent to the given value
	 * <br>
	 * Use this with plus and minus to get actual sums or differences
	 * 
	 * @param angle the original angle
	 * @return the constrained angle
	 */
	public static double limitp(double angle){
		final double m = 2d*Math.PI;
		return angle-m*Math.floor(angle/m);
	}
	
	/**
	 * Shortcut for comparing angles
	 * 
	 * @param a the first angle
	 * @param b the second angle
	 * @return signum of a-b
	 */
	public static int compare(double a,double b){
		// Hopefully this gets inlined
		return Double.compare(limit(a-b), 0);
	}
	
	/**
	 * Constrain the angle accurately between the two bounds
	 * <br>
	 * When outside the bounds, return whichever is closer, and favours
	 * the lower bound in case of a tie
	 * 
	 * @param angle the angle to constrain
	 * @param min the minimum angle
	 * @param max the maximum angle
	 * @return the constrained value
	 */
	public static double constrain(double angle,double min,double max){
		double d1 = limitp(angle-min), d2 = limitp(max-angle), d3 = limitp(max-min);
		if(d1+d2-Floats.D_EPSILON>d3){//Outside of range
			d1 = d1>Math.PI?2d*Math.PI-d1:d1;
			d2 = d2>Math.PI?2d*Math.PI-d2:d2;
			return d1<=d2?min:max;
		}
		return angle;
	}
	
	/**
	 * Testing
	 * 
	 * @param args ignored
	 */
	public static void main(String[] args){
		System.out.println(limit(-Math.PI));
		System.out.println(limit(0));
		System.out.println(limit(Math.PI));
		System.out.println(limit(1.5d*Math.PI));
		System.out.println(limit(2.5d*Math.PI));
		System.out.println(limit(-3.5d*Math.PI));
		System.out.println(compare(0.3,0.6));
		System.out.println(compare(0.4,3.6));
		System.out.println(compare(41.3,-33.4));
		System.out.println(constrain(2.4,2.1,1.8));
		System.out.println(constrain(2.0,2.1,1.8));
		System.out.println(constrain(1.9,2.1,1.8));
		System.out.println(constrain(1.5,2.1,1.8));
	}
}
