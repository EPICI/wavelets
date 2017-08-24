package ui;

import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.skin.*;

import util.math.*;

/**
 * A circular slider.
 * Otherwise known informally as a dial or knob.
 * <br>
 * Note to API users: all angles are in radians
 * 
 * @author EPICI
 * @version 1.0
 */
public class CircularSlider extends Slider {
	
	/**
	 * The default minimum rotation
	 * <br>
	 * Intentionally <i>not</i> exactly -pi
	 */
	public static final double DEFAULT_MIN_ANGLE = -3.14159d;
	/**
	 * The default maximum rotation
	 * <br>
	 * Intentionally <i>not</i> exactly pi
	 */
	public static final double DEFAULT_MAX_ANGLE = 3.14159d;
	// No default current rotation because we load it from the set value
	
	/**
	 * The minimum rotation
	 */
	protected double minAngle;
	/**
	 * The maximum rotation
	 */
	protected double maxAngle;
	/**
	 * The current angle
	 */
	protected double curAngle;

	public CircularSlider() {
		minAngle = DEFAULT_MIN_ANGLE;
		maxAngle = DEFAULT_MAX_ANGLE;
		initFromSuper();
	}

	public CircularSlider(Orientation orientation) {
		super(orientation);
		minAngle = DEFAULT_MIN_ANGLE;
		maxAngle = DEFAULT_MAX_ANGLE;
		initFromSuper();
	}
	
	/**
	 * To be called at the end of the constructor
	 * <br>
	 * Loads necessary data
	 */
	protected void initFromSuper(){
		double start = getStart(), end = getEnd(), value = getValue();
		double interp = (value-start)/(end-start);
		curAngle = Angles.limit(minAngle+interp*Angles.limitp(maxAngle-minAngle));
	}

	/**
	 * Gets the minimum rotation
	 * 
	 * @return the minimum rotation
	 */
	public double getMinAngle() {
		return minAngle;
	}

	/**
	 * Sets the minimum rotation
	 * <br>
	 * Will fail if too close to the maximum rotation
	 * 
	 * @param newAngle the angle to attempt to set the minimum rotation to
	 */
	public void setMinAngle(double newAngle) {
		if(Floats.isNear(Angles.limit(maxAngle-newAngle),0))throw new IllegalArgumentException("Angles equal or too close");
		minAngle = newAngle;
		onAngleChanged();
	}

	/**
	 * Gets the maximum rotation
	 * 
	 * @return the maximum rotation
	 */
	public double getMaxAngle() {
		return maxAngle;
	}

	/**
	 * Sets the maximum rotation
	 * <br>
	 * Will fail if too close to the minimum rotation
	 * 
	 * @param newAngle the angle to attempt to set the maximum rotation to
	 */
	public void setMaxAngle(double newAngle) {
		if(Floats.isNear(Angles.limit(minAngle-newAngle),0))throw new IllegalArgumentException("Angles equal or too close");
		maxAngle = newAngle;
		onAngleChanged();
	}

	/**
	 * Gets the current rotation
	 * 
	 * @return the current rotation
	 */
	public double getCurAngle() {
		return curAngle;
	}

	/**
	 * Sets the current rotation
	 * <br>
	 * If out of bounds, snaps to the nearest
	 * 
	 * @param newAngle the angle to attempt to set the maximum angle to
	 */
	public void setCurAngle(double newAngle) {
		curAngle = Angles.constrain(newAngle, minAngle, maxAngle);
		onAngleChanged();
	}
	
	/**
	 * Get the slider value to double precision
	 * 
	 * @return the unrounded slider value
	 */
	public double getValueExact(){
		double interp = Angles.limitp(curAngle-minAngle)/Angles.limitp(maxAngle-minAngle);
		int start = getStart(), end = getEnd();
		return Bezier.bezier2(start, end, interp);
	}
	
	/**
	 * Set the slider value to double precision
	 * <br>
	 * Constrained to bounds
	 * 
	 * @param newValue new value to set to
	 */
	public void setValueExact(double newValue){
		int start = getStart(), end = getEnd();
		if(newValue<start)newValue = start;
		if(newValue>end)newValue = end;
		setValue((int)Math.round(newValue));
		onValueChanged();
	}
	
	/**
	 * Called upon rotation or angle change
	 */
	public void onAngleChanged(){
		setValue((int)Math.round(getValueExact()));
	}
	
	/**
	 * Called upon outside value change
	 */
	public void onValueChanged(){
		double start = getStart(), end = getEnd(), value = getValue();
		double interp = (value-start)/(end-start);
		curAngle = Angles.limit(minAngle+interp*Angles.limitp(maxAngle-minAngle));
	}

}
