package main;

//A voice, used by audio tracks
public interface Voice extends Destructable {
	//Get the next bit of audio
	public Samples nextSegment(int sampleCount);
	//Check if the voice is still alive or if it should be removed
	public boolean isAlive();
}