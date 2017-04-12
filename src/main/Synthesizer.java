package main;

import org.python.core.PyObject;
import util.jython.*;
import java.util.HashMap;
import javax.swing.*;

/**
 * Generic synthesizer interface, shared with Java and Python
 * <br>
 * Used in tandem with patterns to generate voices which may
 * rely on the synthesizer
 * 
 * @author EPICI
 * @version 1.0
 */
public interface Synthesizer extends Destructable {
	/**
	 * Use array of double arrays to indicate which clips need to be created
	 * <br>
	 * Inner double arrays are [delay,length,pitch]
	 * <br>
	 * The synthesizer should have the rest of the necessary data already
	 * <br>
	 * Should add voices to the target
	 * 
	 * @param clips a list of clips in [delay,length,pitch] format
	 * @param target the target {@link TrackLayerSimple} to add voices to
	 * @param sampleRate sample rate in Hz
	 */
	public void spawnVoices(double[][] clips,TrackLayerSimple target,int sampleRate);
	
	/**
	 * Create a single live voice with the specified pitch
	 * <br>
	 * Used for real time previews and stuff
	 * <br>
	 * Since only one is needed, direct return is fine
	 * 
	 * @param pitch the pitch
	 * @return the newly created live voice
	 */
	public Voice spawnLiveVoice(int pitch);
	
	/**
	 * Request UI with metadata
	 * 
	 * @return frame with UI, which will be opened and tracked
	 * by the window manager along with the others
	 */
	public MetaComponent<? extends JInternalFrame> getUI();
	
	/**
	 * Sets the variables variable so it can be manipulated or read
	 * <br>
	 * Should be called before spawning voices
	 * 
	 * @param vars the variables map
	 */
	public void setGlobals(HashMap<String,Object> vars);
	
	/**
	 * @return true if this is a Python synthesizer
	 */
	public boolean isPython();
	
	/**
	 * Get voice factory object directly
	 * <br>
	 * Should only be used for Java synths, since otherwise
	 * it would actually be slower or more complicated
	 * 
	 * @return factory object for this synthesizer
	 */
	public Factory<Voice> getVoiceFactory();
	
	/**
	 * Get a list, tuple, or dict with information needed to make a PyVoiceFactory
	 * <br>
	 * Should only be used for Python synths, since otherwise
	 * it would actually be slower and more complicated
	 * 
	 * @return list, tuple or dict with the information
	 */
	public PyObject getPvfInfo();
}
