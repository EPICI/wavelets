package utils;

import org.apache.pivot.*;
import org.apache.pivot.collections.*;
import org.apache.pivot.wtk.*;
import javax.swing.*;

/**
 * Utility class containing stuff to help with both
 * Swing and Pivot, and especially to make the transition
 * between them more smooth
 * 
 * @author EPICI
 * @version 1.0
 */
public class PivotSwingUtils {
	
	org.apache.pivot.collections.ArrayList<Display> displays = new org.apache.pivot.collections.ArrayList<>();
	
	//Disallow invoking constructor
	private PivotSwingUtils(){}
	
	/**
	 * Creates a JInternalFrame with the content
	 * of the Pivot Window or something like that.
	 * <br>
	 * Magic.
	 * 
	 * @param pivotWindow an Apache Pivot {@link Window}
	 * @return a Swing {@link JInternalFrame}
	 */
	public JInternalFrame wrapPivotWindow(Window pivotWindow){
		JInternalFrame result = new JInternalFrame(pivotWindow.getTitle());
		ApplicationContext.DisplayHost displayHost = new ApplicationContext.DisplayHost();
		result.add(displayHost);
		Display display = displayHost.getDisplay();
		displays.add(display);
		pivotWindow.open(display);
		return result;
	}
	
	/**
	 * Reads a single double value from a Swing JTextField instance
	 * 
	 * @param field the field to read the double from
	 * @param defaultValue the default
	 * @return the double value if it can be read/parsed,
	 * the default otherwise
	 */
	public double doubleFrom(JTextField field,double defaultValue){
		try{
			return Double.parseDouble(field.getText());
		}catch(Exception e){
			return defaultValue;
		}
	}
	
	/**
	 * Reads a single int value from a Swing JTextField instance
	 * 
	 * @param field the field to read the int from
	 * @param defaultValue the default
	 * @return the int value if it can be read/parsed,
	 * the default otherwise
	 */
	public int intFrom(JTextField field,int defaultValue){
		try{
			return Integer.parseInt(field.getText());
		}catch(Exception e){
			return defaultValue;
		}
	}
	
	/**
	 * Reads a single double value from a Pivot TextInput instance
	 * 
	 * @param field the field to read the double from
	 * @param defaultValue the default
	 * @return the double value if it can be read/parsed,
	 * the default otherwise
	 */
	public double doubleFrom(TextInput field,double defaultValue){
		try{
			return Double.parseDouble(field.getText());
		}catch(Exception e){
			return defaultValue;
		}
	}
	
	/**
	 * Reads a single int value from a Pivot TextInput instance
	 * 
	 * @param field the field to read the int from
	 * @param defaultValue the default
	 * @return the int value if it can be read/parsed,
	 * the default otherwise
	 */
	public int intFrom(TextInput field,int defaultValue){
		try{
			return Integer.parseInt(field.getText());
		}catch(Exception e){
			return defaultValue;
		}
	}
}
