package main;

import java.io.Serializable;

//Any track that can be in the sequence
public interface Track extends Serializable {
	/*
	 * Take the existing samples and do whatever with them
	 * Can overlay audio data, apply effects, etc.
	 * Buffer size is sample length
	 */
	public void applyTo(Samples current);
}
