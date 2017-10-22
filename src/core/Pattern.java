package core;

import java.util.ArrayList;
import org.python.core.*;
import util.jython.*;

/**
 * A standard pattern containing clips
 * 
 * @author EPICI
 * @version 1.0
 */
public class Pattern implements Destructable,TransientContainer<Composition> {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Basically, how many steps in a measure
	 */
	public int divisions;
	/**
	 * Basically, number of measures
	 */
	public transient int length;
	/**
	 * Contains int arrays with length 3 containing:
	 * <ol>
	 * <li>Delay, which will be multiplied later</li>
	 * <li>Length, which will be multiplied later</li>
	 * <li>Pitch as semitones from A4 for convenience</li>
	 * <li>More optional parameters...</li>
	 * </ol>
	 */
	public final ArrayList<int[]> clips;
	
	/** 
	 * Int encoding:
	 * <br>
	 * 0 is unset
	 * <br>
	 * Negative is system managed:
	 * <ul>
	 * <li>-1 is built in (Java)</li>
	 * <li>-2 is included (Python)</li>
	 * <li>-3 is project specific (Python) with file path relative to project file</li>
	 * </ul>
	 * <br>
	 * Positive is user managed:
	 * <ul>
	 * <li>1-999999 is reserved for user defined libraries (Python),
	 * the number determines where to look with file path relative to executable</li>
	 * </ul>
	 */
	protected int synthType;
	/**
	 * The synthesizer to be used for everything
	 * <br>
	 * This may be shared
	 * <br>
	 * Based on int code:
	 * if built in, string is the name, otherwise it's a filename
	 */
	protected String synthName;
	/**
	 * Synthesizer instance
	 */
	protected transient Synthesizer synthesizer;
	/**
	 * VoiceFactory object used to generate voices
	 * <br>
	 * Not a PyVoiceFactory because there are some Java synths
	 */
	protected transient Factory<Voice> voiceFactory;
	
	/**
	 * Standard constructor where everything is specified
	 * 
	 * @param length length in measures
	 * @param divisions number of steps per measure
	 * @param synthType synthesizer type code
	 * @param synthName synthesizer name/filename depending on type
	 * @param composition parent
	 */
	public Pattern(int length,int divisions,byte synthType,String synthName,Composition composition){
		this.length = length;
		this.divisions = divisions;
		this.synthType = synthType;
		this.synthName = synthName;
		clips = new ArrayList<>();
		initTransient(composition);
	}
	
	//TODO everything
	
	@Override
	public void destroy() {
		clips.clear();
		synthesizer=null;
	}

	@Override
	public void destroySelf() {
		
	}

	@Override
	public void initTransient(Composition parent) {
		length = 0;
		for(int[] clip:clips){
			length=Math.max(length, (clip[0]+clip[1])/divisions);
		}
		//TODO load synthesizer
		setDefaultVoiceFactory();
	}
	
	/**
	 * Set voice factory to default value
	 */
	protected void setDefaultVoiceFactory(){
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
	
	/**
	 * Sets voice factory to a new {@link PyVoiceFactory} instance
	 * 
	 * @param a passed to constructor
	 * @param b passed to constructor
	 * @param asScript passed to constructor
	 * @see PyVoiceFactory
	 */
	protected void setPyVoiceFactory(String a,String b,boolean asScript){
		voiceFactory = new PyFactory<Voice>(Voice.class,a,b,asScript);
	}
	
	/**
	 * @return voice factory object
	 */
	public Factory<Voice> getVoiceFactory(){
		if(voiceFactory==null){
			setDefaultVoiceFactory();
		}
		return voiceFactory;
	}
	
	/**
	 * @return synthesizer object
	 */
	public Synthesizer getSynthesizer(){
		return synthesizer;
	}

}
