package main;

/**
 * A voice, used by audio tracks
 * 
 * @author EPICI
 * @version 1.0
 */
public interface Voice extends Destructable {
	/**
	 * Get the next bit of audio
	 * 
	 * @param sampleCount the buffer size
	 * @return a {@link Samples} object with that length
	 */
	public Samples nextSegment(int sampleCount);
	/**
	 * Check if the voice is still alive or if it should be removed
	 * 
	 * @return true if it is still alive
	 */
	public boolean isAlive();
	/**
	 * Kindly tell it the clip is over, does not need to stop immediately
	 */
	public void requestKill();
}
