package main;

//A voice, used by audio tracks
public interface Voice {
	//Get the next bit of audio
	public Samples nextSegment();
	//Check if the voice is still alive or if it should be removed
	public boolean alive();
}
