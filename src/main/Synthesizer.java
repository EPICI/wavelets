package main;

import org.python.core.PyObject;
import java.util.HashMap;
import javax.swing.JComponent;

//Generic synthesizer, can use samples or whatever, used in tandem with Patterns, designed for scripting
public interface Synthesizer extends Destructable {
	/*
	 * Use array of double arrays to indicate which clips need to be created
	 * Inner double arrays are [delay,length,pitch]
	 * The synthesizer should have the rest of the necessary data already
	 * Should add voices to the target
	 */
	public void spawnVoices(double[][] clips,TrackLayerSimple target,int sampleRate);
	
	/*
	 * Create a single live voice with the specified pitch
	 * Used for real time previews and stuff
	 */
	public Voice spawnLiveVoice(int pitch);
	
	/*
	 * Request UI with metadata
	 */
	public MetaComponent<? extends JComponent> getUI();
	
	/*
	 * Sets the variables variable so it can be manipulated or read
	 */
	public void setGlobals(HashMap<String,Object> vars);
	
	/*
	 * Get a list, tuple, or dict with information needed to make a PyVoiceFactory
	 */
	public PyObject getPvfInfo();
}
