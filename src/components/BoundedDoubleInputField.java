package components;

import javax.swing.JComponent;

/*
 * And most DoubleInputFields will be BoundedDoubleInputFields
 * since the value is expected to be in a specific range
 */
public interface BoundedDoubleInputField<T extends JComponent> extends DoubleInputField<T> {
	/*
	 * No need to redeclare get/set, but note
	 * the implementation should do a range check
	 */
	
	/*
	 * Set range/bounds
	 * Should constrain double value when doing so
	 * Dual get/set are not redundant, they can be useful,
	 * or just call the set/get lower and upper
	 */
	public void setDLowerBound(double lower);
	public void setDUpperBound(double upper);
	public void setDBounds(double lower,double upper);
	public double getDLowerBound();
	public double getDUpperBound();
	public double[] getDBounds();
}
