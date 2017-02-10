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
	 * Request UI with metadata
	 */
	public MetaComponent<? extends JComponent> getUI();
}
