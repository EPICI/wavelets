package main;

//Generic synthesizer, can use samples or whatever, used in tandem with Patterns, designed for scripting
public interface Synthesizer extends Destructable {
	/*
	 * Use array of double arrays to indicate which clips need to be created
	 * Inner double arrays are [delay,length,pitch]
	 * The synthesizer should have the rest of the necessary data already
	 * Should add voices to the target
	 */
	public void spawnVoices(double[][] clips,TrackLayerSimple target);
	
	/*
	 * TODO request UI method
	 * This should request a data class with information on the UI,
	 * rather than show the UI
	 * That way the program can handle the display
	 */
}
