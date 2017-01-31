package main;

//Real time audio player
public interface Player extends Destructable {
	//Set to play a track
	public void playTrack(Track track,boolean loop);
	//Set to play samples
	public void playSamples(Samples samples,boolean loop);
	//Stop all playing
	public void stopPlay();
}
