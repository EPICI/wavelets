package main;

import java.util.ArrayList;

//A standard pattern
public class Pattern implements Destructable {

	/*
	 * Basically, how many steps in a measure
	 */
	public int divisions;
	/*
	 * Contains int arrays with length 3 containing:
	 * Delay, which will be multiplied later
	 * Length, which will be multiplied later
	 * Pitch as semitones from A4 for convenience
	 */
	public ArrayList<int[]> clips;
	/*
	 * The synthesizer to be used for everything
	 * This may be shared
	 */
	public Synthesizer synthesizer;
	
	//TODO everything
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroySelf() {
		// TODO Auto-generated method stub

	}

}
