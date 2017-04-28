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
