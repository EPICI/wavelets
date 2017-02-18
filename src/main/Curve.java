package main;

import java.io.Serializable;

/**
 * A curve, a function, or anything else that maps
 * one x value to one y value
 * 
 * @author EPICI
 * @version 1.0
 */
public interface Curve extends Serializable {
	/**
	 * Get the value for a specific position
	 * <br>
	 * aka get f(x) for x
	 * 
	 * @param position position to get the corresponding value of
	 * @return that value
	 */
	public double valueAtPosition(double position);
}
