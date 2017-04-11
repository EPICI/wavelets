package utils;

import org.apache.pivot.*;
import org.apache.pivot.collections.*;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.*;
import org.apache.pivot.beans.*;

import java.io.IOException;

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
	
	//Disallow invoking constructor
	private PivotSwingUtils(){}
	
	static{
		new ApplicationContext(){
			{
				createTimer();
			}
		};
	}
	
	/**
	 * Convenient shortcut for loading BXML
	 * 
	 * @param source a class, resource should be in the same folder
	 * @param filename the name of the file to open
	 * @return an object of the correct type if successful, otherwise null
	 */
	public static <T> T loadBxml(Class<?> source,String filename){
		return loadBxml(source,filename,new BXMLSerializer());
	}
	/**
	 * Convenient shortcut for loading BXML
	 * <br>
	 * Uses the given {@link BXMLSerializer} instead of a new instance,
	 * which has its uses
	 * <br>
	 * Note that if it fails, the {@link BXMLSerializer} gets messed up,
	 * so if this returns null and the {@link BXMLSerializer} is intended
	 * to be reused, make a new one and be sure to copy all the data from
	 * the original one
	 * 
	 * @param source a class, resource should be in the same folder
	 * @param filename the name of the file to open
	 * @param serializer the serializer object to use
	 * @return  an object of the correct type if successful, otherwise null
	 */
	public static <T> T loadBxml(Class<?> source,String filename,BXMLSerializer serializer){
		try {
			return (T) serializer.readObject(source,filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SerializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Pops open the frame, if it exists
	 * 
	 * @param frame the {@link JInternalFrame} to show
	 * @return true if successful
	 */
	public static boolean showFrameDefault(JInternalFrame frame){
		if(frame==null)return false;
		frame.setLocation(60, 60);
		frame.setSize(1280, 720);
		frame.setVisible(true);
		frame.setResizable(true);
		frame.toFront();
		frame.requestFocusInWindow();
		return true;
	}
	
	/**
	 * Creates a JInternalFrame with the content
	 * of the Pivot Window or something like that.
	 * <br>
	 * Magic.
	 * 
	 * @param pivotWindow an Apache Pivot {@link Window}
	 * @return a Swing {@link JInternalFrame}
	 */
	public static JInternalFrame wrapPivotWindow(Window pivotWindow){
		JInternalFrame result = new JInternalFrame(pivotWindow.getTitle());
		ApplicationContext.DisplayHost displayHost = new ApplicationContext.DisplayHost();
		result.add(displayHost);
		Display display = displayHost.getDisplay();
		ApplicationContext.getDisplays().add(display);
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
	public static double doubleFrom(JTextField field,double defaultValue){
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
	public static int intFrom(JTextField field,int defaultValue){
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
	public static double doubleFrom(TextInput field,double defaultValue){
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
	public static int intFrom(TextInput field,int defaultValue){
		try{
			return Integer.parseInt(field.getText());
		}catch(Exception e){
			return defaultValue;
		}
	}
}
