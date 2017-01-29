package main;

import org.python.core.*;

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
	 * TODO request UI method
	 * This should request a data class with information on the UI,
	 * rather than show the UI
	 * That way the program can handle the display
	 */
	
	/*
	 * Get a list, tuple, or dict with information needed to make a PyVoiceFactory
	 */
	public PyObject getPvfInfo();
}
