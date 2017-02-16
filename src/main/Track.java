package main;

import java.io.Serializable;
import javax.swing.JComponent;

//Any track that can be in the sequence
public interface Track extends Serializable {
	/*
	 * Take the existing samples and do whatever with them
	 * Can overlay audio data, apply effects, etc.
	 * Buffer size is sample length
	 */
	public void applyTo(MetaSamples current);
	
	/*
	 * Get the track bounds as double array
	 * First element is minimum/left/start
	 * Second element is maximum/right/end
	 * All in seconds
	 */
	public double[] getTimeBounds();
	
	/*
	 * Convenience method
	 * Get parent composition, do whatever it takes to find it
	 */
	public Composition parentComposition();
	
	/*
	 * Request UI with metadata
	 */
	public MetaComponent<? extends JComponent> getUI();
	
	/*
	 * Get component to display inline in the track viewer
	 * which would be part of a TrackLayerCompound's UI
	 */
	public MetaComponent<? extends JComponent> getViewComponent();
}
