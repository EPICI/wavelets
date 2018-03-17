package core;

import java.util.*;

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
					if(v instanceof BetterClone<?,?>){
						globalsDynamic.put(k, ((BetterClone<?,?>) v).deepCopy(null));
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
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		destroyed = true;
	}

	@Override
	public void destroySelf() {
		// TODO Auto-generated method stub
		destroyed = true;
	}
	
	@Override
	public boolean isDestroyed(){
		return destroyed;
	}

	@Override
	public void initTransient(Session parent) {
		// TODO Auto-generated method stub
		
	}

}
