package core;

import java.util.*;
import java.util.function.Predicate;

import org.python.core.*;

import core.synth.SynthNOsc;
import util.jython.*;

/**
 * A standard pattern containing clips
 * 
 * @author EPICI
 * @version 1.0
 */
public class Pattern implements Destructable, TransientContainer<Composition>, Named, BetterClone<Pattern> {
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
	 * The name of this instance.
	 * Please use the getter and setter instead.
	 */
	public String name;
	
	/**
	 * Destroyed yet?
	 */
	protected transient boolean destroyed = false;
	
	/**
	 * Standard constructor where everything is specified
	 * 
	 * @param divisions number of steps per measure
	 * @param synthName synthesizer name
	 * @param composition parent
	 */
	public Pattern(int divisions,String synthName,Composition composition){
		this.divisions = divisions;
		this.synthName = synthName;
		clips = new ArrayList<>();
		initTransient(composition);
	}
	
	/**
	 * Create a pattern object according to default settings.
	 * In the current implementation this will be named
	 * like &quot;Pattern&quot; using
	 * {@link util.ds.NamedMap#nextName(String, int, boolean, Session)}.
	 * <br>
	 * Note that for the default synthesizer this creates an instance of
	 * {@link SynthNOsc} and adds it to the composition's synthesizers for use.
	 * 
	 * @param session
	 * @return
	 */
	public static Pattern makeDefaultPattern(Session session){
		// synth
		Synthesizer synth = SynthNOsc.makeDefaultSynth(session);
		String synthName = synth.getName();
		Synthesizer.Specification synthSpec = Synthesizer.specWrap(synth);
		session.composition.addSynth(synthName, synth, synthSpec);
		// create the object
		Pattern result = new Pattern(
				8,
				synthName,
				session.composition);
		// set name
		result.setName(
				session.composition.patterns.nextName(
						session.getCommonName(Pattern.class),
						0, false, session));
		// return
		return result;
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
		synthesizer = parent.synths.dualMap.get(synthName);
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

	@Override
	public boolean setName(String newName) {
		name = newName;
		return true;
	}
	
	private static final String PATTERN_CLASS_NAME = Pattern.class.getCanonicalName();
	@Override
	public Pattern copy(int depth, Map<String,Object> options){
		int nextDepth = depth-1;
		int newDivisions = divisions;
		String newName = name;
		String newSynthName = synthName;
		// the list will be modified anyway
		ArrayList<Clip> newClips = new ArrayList<>(clips);
		Map<String,Object> set = (Map<String,Object>)options.get("set");
		Number val;
		CharSequence sval;
		val = (Number) set.get(PATTERN_CLASS_NAME+".divisions");
		if(val!=null)divisions = val.intValue();
		sval = (CharSequence) set.get(PATTERN_CLASS_NAME+".name");
		if(sval!=null)newName = sval.toString();
		sval = (CharSequence) set.get(PATTERN_CLASS_NAME+".synthName");
		if(sval!=null)newSynthName = sval.toString();
		// filter
		Predicate<? super Clip> pval = (Predicate<? super Clip>) set.get(PATTERN_CLASS_NAME+".clips.filter");
		if(pval!=null){
			newClips.removeIf(pval.negate());
		}
		// any iterable is allowed, valid values override old ones
		Iterable<?> lval = (Iterable<?>) set.get(PATTERN_CLASS_NAME+".clips");
		if(lval!=null){
			int size = newClips.size();
			Iterator<?> iter = lval.iterator();
			// exhaust as many items as possible which don't need extending the list
			int i;
			for(i=0;i<size && iter.hasNext();i++){
				Object value = iter.next();
				if(value!=null && (value instanceof Clip)){
					newClips.set(i, (Clip)value);
				}
			}
			// do remaining items, if any
			while(iter.hasNext()){
				Object value = iter.next();
				if(value!=null && (value instanceof Clip)){
					newClips.add((Clip)value);
				}
			}
			// need to limit length?
			int j;
			if(i<size 
					&& (val = (Number) set.get(PATTERN_CLASS_NAME+".clips.size"))!=null
					&& (j = val.intValue())<i){
				for(size--;size>=j;size--){
					newClips.remove(size);
				}
			}
		}
		Session session = (Session) options.get("session");
		Composition composition = session.composition;
		// correct name
		newName = composition.patterns.nextName(newName, -1, false, session);
		// copy properties as needed
		int size = newClips.size();
		for(int i=0;i<size;i++){
			Clip value = newClips.get(i);
			value = BetterClone.copy(value, nextDepth, options);
			newClips.set(i, value);
		}
		// make the copied object
		Pattern result = new Pattern(newDivisions,newSynthName,composition);
		result.setName(newName);
		result.clips.addAll(newClips);
		return result;
	}
	
	@Override
	public boolean equals(Object o){
		if(o==this)return true;
		if(o==null || !(o instanceof Pattern))return false;
		Pattern other = (Pattern) o;
		return divisions==other.divisions
				&& synthName.equals(other.synthName)
				&& clips.equals(other.clips);
	}

}
