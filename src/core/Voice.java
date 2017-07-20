package core;

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
	
	/**
	 * Creats a voice object which polls all the given voices
	 * 
	 * @param gvoices voices to flatten
	 * @return a single voice which wraps all the given
	 */
	public static Voice combine(Voice... gvoices){
		return new Voice(){
			
			Voice[] voices = gvoices;

			@Override
			public void destroy() {
				for(Voice voice:voices)
					voice.destroy();
				voices = null;
			}

			@Override
			public void destroySelf() {
				voices = null;
			}

			@Override
			public Samples nextSegment(int sampleCount) {
				Samples data = voices[0].nextSegment(sampleCount);
				for(int i=1;i<voices.length;i++)
					data.layerOnThisLazy(voices[i].nextSegment(sampleCount));
				return data;
			}

			@Override
			public boolean isAlive() {
				for(Voice voice:voices)
					if(voice.isAlive())
						return true;
				return false;
			}

			@Override
			public void requestKill() {
				for(Voice voice:voices)
					voice.requestKill();
			}
			
		};
	}
}
