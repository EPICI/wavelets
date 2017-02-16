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
	 * Byte encoding:
	 * -1  - built in (Java)
	 * -2  - included (Python)
	 * -3  - project specific (Python)
	 * 64+ - reserved for user defined libraries (Python)
	 * If built in, string is the name, otherwise it's a filename
	 */
	public byte synthType;
	public String synthName;
	public transient Synthesizer synthesizer;
	/*
	 * VoiceFactory object used to generate voices
	 * Not a PyVoiceFactory because there are some Java synths
	 */
	public transient VoiceFactory voiceFactory;
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
		//TODO load synthesizer
		setDefaultVoiceFactory();
	}
	
	public void setDefaultVoiceFactory(){
		if(synthesizer.isPython()){
			PyObject pyData = synthesizer.getPvfInfo();
			PyString stra = pyData.__getitem__(0).__str__();
			PyString strb = pyData.__getitem__(1).__str__();
			PyObject bool = pyData.__getitem__(2);
			setPyVoiceFactory(stra.asString(),strb.asString(),bool.asInt()!=0);
		}else{
			voiceFactory = synthesizer.getVoiceFactory();
		}
	}
	
	public void setPyVoiceFactory(String a,String b,boolean asScript){
		voiceFactory = new PyVoiceFactory(a,b,asScript);
	}
	
	public VoiceFactory getVoiceFactory(){
		if(voiceFactory==null){
			setDefaultVoiceFactory();
		}
		return voiceFactory;
	}

}
