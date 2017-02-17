package components;

import javax.swing.JComponent;

/*
 * Input field, with methods to get/set double value
 * This will comprise much of the UI
 * Should only have finite values, does not have to be bounded
 */
public interface DoubleInputField<T extends JComponent> extends ComponentType<T> {
	/*
	 * Get and set double values
	 */
	public double getDValue();
	public void setDValue(double value);
}
