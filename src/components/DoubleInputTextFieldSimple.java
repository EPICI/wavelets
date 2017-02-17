package components;

import javax.swing.JTextField;
import javax.swing.text.Document;

/*
 * The simplest form - a text field
 * Write writes a string to the text field
 * Read reads a string from the text field
 * No cache or anything
 * TODO future programmers plz halp
 */
public class DoubleInputTextFieldSimple extends JTextField implements DoubleInputField<DoubleInputTextFieldSimple> {
	private static final long serialVersionUID = 1L;
	
	public double defaultValue = 0d;

	public DoubleInputTextFieldSimple() {
		// TODO Auto-generated constructor stub
	}

	public DoubleInputTextFieldSimple(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public DoubleInputTextFieldSimple(int columns) {
		super(columns);
		// TODO Auto-generated constructor stub
	}

	public DoubleInputTextFieldSimple(String text, int columns) {
		super(text, columns);
		// TODO Auto-generated constructor stub
	}

	public DoubleInputTextFieldSimple(Document doc, String text, int columns) {
		super(doc, text, columns);
		// TODO Auto-generated constructor stub
	}

	@Override
	public DoubleInputTextFieldSimple asComponent() {
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
		setText(Double.toString(value));
	}

}
