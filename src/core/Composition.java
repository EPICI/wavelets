package core;

import java.util.*;
import util.ds.*;

/**
 * An actual composition, the parent of everything else
 * 
 * @author EPICI
 * @version 1.0
 */
public class Composition implements TransientContainer<Session>, TLCParent, Destructable{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Generic storage, acts like a global variables map
	 */
	public HashMap<String,Object> globalsStatic;
	/**
	 * When playback starts, copy to this, then reference this
	 * in the new MetaSamples
	 */
	public transient HashMap<String,Object> globalsDynamic;
	/**
	 * Seconds per measure
	 * <br>
	 * For converting, it is recommended to use the
	 * <i>measuresToSeconds</i> and <i>secondsToMeasures</i>
	 * methods to avoid accessing this field directly
	 * <br>
	 * This will be replaced in the far future by a special time curve
	 * (1/Hz bezier -> integral)
	 * which maps time to measures (use inverse for other way)
	 */
	public double baseSpeed;
	/**
	 * The main {@link TrackLayerCompound} which holds all tracks
	 */
	public TrackLayerCompound tracks;
	/**
	 * All clip templates, with the name as the key
	 * and the template object as the value
	 */
	public NamedMap<Clip.Template> clipTemplates;
	/**
	 * All synthesizers by name, stored by their specifications
	 * <br>
	 * Will always match <i>synths</i>
	 * <br>
	 * Please use the provided methods rather than modifying this directly
	 * for safety reasons
	 */
	public NamedMap<Synthesizer.Specification> synthSpecs;
	/**
	 * All synthesizers by name
	 * <br>
	 * Will always match <i>synthSpecs</i>
	 * <br>
	 * Please use the provided methods rather than modifying this directly
	 * for safety reasons
	 */
	public transient NamedMap<Synthesizer> synths;
	/**
	 * Current session
	 */
	public transient Session currentSession;
	
	/**
	 * Destroyed yet?
	 */
	protected transient boolean destroyed = false;
	
	/**
	 * Default constructor, creates a default composition
	 * 
	 * @param session {@link Session} object
	 */
	public Composition(Session session){
		currentSession = session;
		tracks = new TrackLayerCompound(this);
		clipTemplates = new NamedMap<>();
		synthSpecs = new NamedMap<>();
		initTransient(currentSession);
	}

	/**
	 * Deep copy static globals to dynamic
	 */
	public void pushGlobals(){
		synchronized(globalsStatic){
			globalsDynamic = new HashMap<>();
			synchronized(globalsDynamic){
				for(String k:globalsStatic.keySet()){
					Object v = globalsStatic.get(k);
					if(v instanceof BetterClone<?>){
						BetterClone<?> bcv = ((BetterClone<?>) v);
						Map<String,Object> copyOptions = BetterClone.fixOptions(null);
						currentSession.setCopyOptions(copyOptions);
						globalsDynamic.put(k, bcv.copy(Integer.MAX_VALUE, copyOptions));
					}else{
						globalsDynamic.put(k, v);
					}
				}
			}
		}
	}
	
	/**
	 * After some number of measures have passed, how many
	 * seconds in are we?
	 * 
	 * @param measures
	 * @return
	 */
	public double measuresToSeconds(double measures){
		return measures*baseSpeed;
	}
	
	/**
	 * After some number of seconds have passed, how many
	 * measures in are we?
	 * 
	 * @param seconds
	 * @return
	 */
	public double secondsToMeasures(double seconds){
		return seconds/baseSpeed;
	}
	
	/**
	 * Attempt to rename a synthesizer, return true if successful
	 * 
	 * @param oldName the current name of the synthesizer
	 * @param newName the desired new name of the synthesizer
	 * @return true if successful, indicating a change happened, otherwise false
	 */
	public boolean renameSynth(String oldName,String newName){
		boolean result = synths.rename(oldName, newName);
		if(result)synthSpecs.renameMapOnly(oldName, newName);
		return result;
	}
	
	/**
	 * Attempt to add a synthesizer, return true if successful
	 * 
	 * @param name desired name of the synthesizer
	 * @param synth synthesizer to add
	 * @param synthSpec specification for synthesizer, or null to use default
	 * @return true if successful, indicating a change happened, otherwise false
	 */
	public boolean addSynth(String name,Synthesizer synth,Synthesizer.Specification synthSpec){
		if(synth==null
				||name==null
				||synthSpecs.dualMap.containsKey(name))return false;
		if(synthSpec==null)synthSpec = Synthesizer.specWrap(synth);
		synthSpecs.dualMap.put(name, synthSpec);
		synths.dualMap.put(name, synth);
		return true;
	}
	
	// TODO remove synth method
	
	// TODO update synth specification method
	
	@Override
	public void destroy() {
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
	public void initTransient(Session parent) {
		synths = new NamedMap<>();
		for(Map.Entry<String, Synthesizer.Specification> entry:synthSpecs.dualMap.entrySet()){
			synths.dualMap.put(entry.getKey(), entry.getValue().resolve(currentSession));
		}
	}

}
