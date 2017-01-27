package main;

import java.util.ArrayList;
import org.python.core.*;

//A standard pattern
public class Pattern implements Destructable,TransientContainer<TrackLayerSimple> {
	private static final long serialVersionUID = 1L;
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
	/*
	 * PyVoiceFactory object used to generate voices
	 */
	public transient PyVoiceFactory pvf;
	/*
	 * Basically, number of measures
	 */
	public transient int length;
	
	//TODO everything
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroySelf() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initTransient(TrackLayerSimple parent) {
		length = 0;
		for(int[] clip:clips){
			length=Math.max(length, (clip[0]+clip[1])/divisions);
		}
		setDefaultPvf();
	}
	
	public void setDefaultPvf(){
		PyObject pyData = synthesizer.getPvfInfo();
		PyString stra = pyData.__getitem__(0).__str__();
		PyString strb = pyData.__getitem__(1).__str__();
		PyObject bool = pyData.__getitem__(2);
		setPvf(stra.asString(),strb.asString(),bool.asInt()!=0);
	}
	
	public void setPvf(String a,String b,boolean asScript){
		pvf = new PyVoiceFactory(a,b,asScript);
	}
	
	public PyVoiceFactory getPvf(){
		if(pvf==null){
			setDefaultPvf();
		}
		return pvf;
	}

}
