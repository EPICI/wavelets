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
	 * <br>
	 * Computed as if the start was at 0, even if
	 * it actually starts later
	 * <br>
	 * The true length is a rational, and this is the ceiling
	 * of that rational
	 */
	public transient int length;
	/**
	 * Contains clip objects
	 */
	public final ArrayList<Clip> clips;
	
	/**
	 * The synthesizer to be used for everything, may be shared
	 * <br>
	 * Each synthesizer instance has a unique name, so this is the key
	 * which is used to resolve the instance
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
	 * The name of this instance, made public for easy access
	 */
	public String name;
	
	/**
	 * Destroyed yet?
	 */
	protected transient boolean destroyed = false;
	
	/**
	 * Standard constructor where everything is specified
	 * 
	 * @param length length in measures
	 * @param divisions number of steps per measure
	 * @param synthName synthesizer name
	 * @param composition parent
	 */
	public Pattern(int length,int divisions,String synthName,Composition composition){
		this.length = length;
		this.divisions = divisions;
		this.synthName = synthName;
		clips = new ArrayList<>();
		initTransient(composition);
	}
	
	/**
	 * Method to update the divisions and propagate necessary changes
	 * <br>
	 * Except for internal use, this is the
	 * preferred way to set the divisions
	 * 
	 * @param newDivisions
	 */
	public void setDivisions(int newDivisions){
		if(newDivisions<1)throw new IllegalArgumentException("divisions ("+newDivisions+") must be positive");
		int oldDivisions = divisions;
		if(oldDivisions==newDivisions)return;
		divisions = newDivisions;
		remakeLength();
	}
	
	/**
	 * Recalculate the field <i>length</i> based on current data.
	 * <br>
	 * Since it isn't done automatically but rather by this method,
	 * we can do lazy updating. Also, there are some times when
	 * we intentionally do not update it even if it might be incorrect.
	 */
	public void remakeLength(){
		length = 0;
		for(Clip clip:clips){
			// Offset because floor division
			length=Math.max(length, (clip.delay+clip.length-1)/divisions);
		}
		// Finally add 1 to make it ceiling
		length += 1;
	}
	
	//TODO everything
	
	/**
	 * Get the name of this instance. Will never return null.
	 * 
	 * @return
	 */
	public String getName(){
		return name==null?getDefaultName():name;
	}
	
	/**
	 * Returns a generated name.
	 * 
	 * @return
	 */
	public String getDefaultName(){
		return Track.defaultNameAny("Pattern", this);
	}
	
	@Override
	public void destroy() {
		clips.clear();
		synthesizer=null;
		destroyed = true;
	}

	@Override
	public void destroySelf() {
		destroyed = true;
	}
	
	@Override
	public boolean isDestroyed(){
		return destroyed;
	}

	@Override
	public void initTransient(Composition parent) {
		remakeLength();
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
