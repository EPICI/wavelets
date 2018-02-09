package core;

import java.io.Serializable;
import javax.swing.*;
import org.apache.pivot.wtk.*;

/**
 * Any audio track
 * 
 * @author EPICI
 * @version 1.0
 */
public interface Track extends Serializable {
	/**
	 * Take the existing samples and do whatever with them
	 * <br>
	 * Can overlay audio data, apply effects, etc.
	 * <br>
	 * Buffer size is sample length
	 * 
	 * @param current the samples so far
	 */
	public void applyTo(MetaSamples current);
	
	/**
	 * Get the track bounds as double array
	 * <br>
	 * First element is minimum/left/start
	 * <br>
	 * Second element is maximum/right/end
	 * <br>
	 * All are in seconds relative to very beginning
	 * 
	 * @return double array of size 2 containing start time and end time
	 */
	public double[] getTimeBounds();
	
	/**
	 * Convenience method
	 * Get parent composition, do whatever it takes to find it
	 * 
	 * @return parent {@link Composition} object
	 */
	public Composition parentComposition();
	
	/**
	 * Attempt to change the parent, return true on success
	 * 
	 * @param newParent requested new parent
	 */
	public boolean setParent(Object newParent);
	
	/**
	 * Request UI with metadata
	 * <br>
	 * If the returned value is an instance of {@link JInternalFrame}
	 * it will be displayed separately and tracked by the window manager
	 * 
	 * @return component with metadata
	 */
	public MetaComponent<? extends JComponent> getUI();
	
	/**
	 * Get component to display inline in the track viewer
	 * which would be part of a TrackLayerCompound's UI
	 * 
	 * @return component component to display in its own row
	 * @see TrackLayerCompound
	 */
	public ViewComponent getViewComponent();
	
	/**
	 * Name getter
	 * 
	 * @return the track's name
	 */
	public String getName();
	
	/**
	 * Name setter
	 * 
	 * @param newName the new name to (attempt to) give it
	 */
	public void setName(String newName);
	
	/**
	 * Generic type id name
	 * 
	 * @param type type name
	 * @param obj object
	 * @return a random looking name
	 */
	public static String defaultName(String type,Track obj){
		return defaultNameAny(type,obj);
	}
	
	/**
	 * Generic type id name which can handle non-track objects
	 * 
	 * @param type type name
	 * @param obj object
	 * @return a random looking name
	 */
	public static String defaultNameAny(String type,Object obj){
		int id = System.identityHashCode(obj);//Needs more scrambling to look random?
		String idstr = Integer.toString((id&Integer.MAX_VALUE)%1000000);
		StringBuilder sb = new StringBuilder();
		sb.append(type);
		sb.append(" #");
		for(int i=idstr.length();i<6;i++)sb.append('0');
		sb.append(idstr);
		return sb.toString();
	}
	
	/**
	 * View component
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public static abstract class ViewComponent extends Component{
		/**
		 * Set the horizontal scaling
		 * 
		 * @param v pixels given to one measure
		 */
		public abstract void setPixelsPerMeasure(double v);
	}
}
