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
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroySelf() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initTransient(Session parent) {
		// TODO Auto-generated method stub
		
	}

}
