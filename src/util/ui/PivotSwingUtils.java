package util.ui;

import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.skin.terra.*;
import org.apache.pivot.beans.*;
import org.apache.pivot.collections.*;

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
	 * @param <T> the desired type
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
	 * @param <T> the desired type
	 * @return  an object of the correct type if successful, otherwise null
	 */
	public static <T> T loadBxml(Class<?> source,String filename,BXMLSerializer serializer){
		try {
			return (T) serializer.readObject(source,filename);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SerializationException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
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
		return showFrame(frame,new int[]{60,60},new int[]{960,540});
	}
	/**
	 * Pops open the frame at the specified location, if it exists
	 * 
	 * @param frame the {@link JInternalFrame} to show
	 * @param topLeft coordinate that the top left corner of the window should be at
	 * @param dimensions width and height for the window
	 * @return true if successful
	 */
	public static boolean showFrame(JInternalFrame frame,int[] topLeft,int[] dimensions){
		if(frame==null)return false;
		frame.setLocation(topLeft[0], topLeft[1]);
		frame.setSize(dimensions[0], dimensions[1]);
		frame.setVisible(true);
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
		result.setResizable(true);
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
	
	/**
	 * If a toggle button, inverts the state
	 * <br>
	 * Presses if unpressed, unpresses if pressed
	 * 
	 * @param button
	 */
	public static void invertToggle(PushButton button){
		if(button!=null&&button.isToggleButton()){
			switch(button.getState()){
			case SELECTED:{
				button.setState(Button.State.UNSELECTED);
				break;
			}
			case UNSELECTED:{
				button.setState(Button.State.SELECTED);
				break;
			}
			}
		}
	}
	
	/**
	 * Attempt to swap two items in a sequence
	 * <br>
	 * Will not throw exceptions in normal conditions,
	 * instead returns false if it would fail, and doesn't
	 * modify the sequence
	 * <br>
	 * If a=b and the swap is possible, the swap is skipped
	 * and true is returned
	 * <br>
	 * If an exception is thrown, may be partway through the swap
	 * 
	 * @param seq sequence to swap in
	 * @param a index of first item
	 * @param b index of second item
	 * @return if they were swapped
	 */
	public static <T> boolean swap(Sequence<T> seq,int a,int b){
		if(seq!=null){
			if(a<0||b<0)return false;
			int n = seq.getLength();
			if(a>=n||b>=n)return false;
			if(a!=b){
				if(a>b){int t=a;a=b;b=t;}// Ensure a<b
				T ia = seq.get(a);
				T ib = seq.get(b);
				// doesn't support these methods so we need to remove and add instead
				//seq.update(a, ib);
				//seq.update(b, ia);
				seq.remove(b,1);
				seq.remove(a,1);
				seq.insert(ib, a);
				seq.insert(ia, b);
			}
			return true;
		}
		return false;
	}
}
