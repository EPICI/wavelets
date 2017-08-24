package core;

/**
 * Extension of {@link Curve} with some useful
 * methods added
 * 
 * @author EPICI
 * @version 1.0
 */
public abstract class AbstractCurve implements Curve {
	private static final long serialVersionUID = 1L;

	/**
	 * Sample some number of points
	 * <br>
	 * Provided here only because certain subclasses cannot
	 * gain any performance advantage with code tailored to them
	 * 
	 * @param positions x values to sample
	 * @return y value for each x
	 */
	public double[] sample(double[] positions){
		int n = positions.length;
		double[] result = new double[n];
		for(int i=0;i<n;i++)
			result[i] = valueAtPosition(positions[i]);
		return result;
	}
	
	/**
	 * Sample <i>n</i> equally spaced points in the range
	 * [<i>l</i>, <i>r</i>) such that the first point is at
	 * <i>l</i> and the <i>n+1</i>th point, if it existed,
	 * would be at <i>r</i>
	 * <br>
	 * This is not numerically stable, but is fine for
	 * any reasonable parameters
	 * <br>
	 * Provided here only because certain subclasses cannot
	 * gain any performance advantage with code tailored to them
	 * 
	 * @param l first value as described above
	 * @param r would-be last value as described above
	 * @param n number of sample points as described above
	 * @return y value for each sampled x in the range
	 */
	public double[] bake(double l,double r,int n){
		double[] result = new double[n];
		double d = (r-l)/n;
		for(int i=0;i<n;i++,l+=d)
			result[i] = valueAtPosition(l);
		return result;
	}

}
