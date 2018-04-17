package core;

import java.io.Serializable;
import java.util.function.*;

/**
 * A curve, a function, or anything else that maps
 * one x value to one y value
 * 
 * @author EPICI
 * @version 1.0
 */
public interface Curve extends Serializable, DoubleUnaryOperator, VarDouble {
	/**
	 * Get the value for a specific position
	 * <br>
	 * aka get f(x) for x
	 * 
	 * @param position position to get the corresponding value of
	 * @return that value
	 */
	public double valueAtPosition(double position);
	
	// Standard interface that does the same thing
	@Override
	public default double applyAsDouble(double value){
		return valueAtPosition(value);
	}
	
	// Allow using a curve directly
	@Override
	public default double get(double time){
		return valueAtPosition(time);
	}
}
