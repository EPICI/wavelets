package components;

import javax.swing.JTextField;
import javax.swing.text.Document;

/*
 * The same thing as DoubleInputTextFieldSimple
 * but with bounds
 */
public class BoundedDoubleInputTextFieldSimple extends JTextField implements BoundedDoubleInputField<BoundedDoubleInputTextFieldSimple> {
	private static final long serialVersionUID = 1L;
	
	public double defaultValue = 0d;
	protected double lowerBound = 0d;
	protected double upperBound = 1d;

	public BoundedDoubleInputTextFieldSimple() {
		// TODO Auto-generated constructor stub
	}

	public BoundedDoubleInputTextFieldSimple(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public BoundedDoubleInputTextFieldSimple(int columns) {
		super(columns);
		// TODO Auto-generated constructor stub
	}

	//Convenience constructor
	public BoundedDoubleInputTextFieldSimple(int columns,double lower,double upper) {
		super(columns);
		setDBounds(lower,upper);
	}

	public BoundedDoubleInputTextFieldSimple(String text, int columns) {
		super(text, columns);
		// TODO Auto-generated constructor stub
	}

	public BoundedDoubleInputTextFieldSimple(Document doc, String text, int columns) {
		super(doc, text, columns);
		// TODO Auto-generated constructor stub
	}

	@Override
	public BoundedDoubleInputTextFieldSimple asComponent() {
		return this;
	}

	@Override
	public double getDValue() {
		try{
			return Double.parseDouble(getText());
		}catch(NumberFormatException e){
			return defaultValue;
		}
	}

	@Override
	public void setDValue(double value) {
		if(value>upperBound){
			throw new IllegalArgumentException("Value ("+value+") must be less than or equal to upper bound ("+upperBound+")");
		}
		if(value<lowerBound){
			throw new IllegalArgumentException("Value ("+value+") must be greater than or equal to lower bound ("+lowerBound+")");
		}
		setText(Double.toString(value));
	}

	@Override
	public void setDLowerBound(double lower) {
		if(lower>=upperBound){
			throw new IllegalArgumentException("Lower bound ("+lower+") must be less than upper bound ("+upperBound+")");
		}else if(!Double.isFinite(lower)){
			throw new IllegalArgumentException("Lower bound ("+lower+") must be finite");
		}
		lowerBound = lower;
	}

	@Override
	public void setDUpperBound(double upper) {
		if(upper<=lowerBound){
			throw new IllegalArgumentException("Upper bound ("+upper+") must be greater than lower bound ("+lowerBound+")");
		}else if(!Double.isFinite(upper)){
			throw new IllegalArgumentException("Upper bound ("+upper+" must be finite");
		}
		upperBound = upper;
	}

	@Override
	public void setDBounds(double lower, double upper) {
		if(Double.isFinite(lower)&&Double.isFinite(upper)){
			if(lower>=upper){
				throw new IllegalArgumentException("Invalid bounds; upper bound ("+upper+") must be greater than lower bound ("+lower+")");
			}
			lowerBound = lower;
			upperBound = upper;
		}else{
			throw new IllegalArgumentException("Both bounds ("+lower+", "+upper+") must be finite");
		}
	}

	@Override
	public double getDLowerBound() {
		return lowerBound;
	}

	@Override
	public double getDUpperBound() {
		return upperBound;
	}

	@Override
	public double[] getDBounds() {
		return new double[]{lowerBound,upperBound};
	}

}
