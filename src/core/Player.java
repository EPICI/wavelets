package core;

/**
 * Real time audio player
 * 
 * @author EPICI
 * @version 1.0
 */
public interface Player extends Destructable {
	/**
	 * Set to play a track
	 * 
	 * @param track the track to play
	 * @param loop whether to loop
	 */
	public void playTrack(Track track,boolean loop);
	/**
	 * Set to play samples
	 * 
	 * @param samples the {@link Samples} object to play
	 * @param loop whether to loop
	 */
	public void playSamples(Samples samples,boolean loop);
	/**
	 * Stop all audio playback
	 */
	public void stopPlay();
}
