package main;

import java.util.*;

/**
 * An actual composition, the parent of everything else
 * 
 * @author EPICI
 * @version 1.0
 */
public class Composition implements TransientContainer<Object>, TLCParent, Destructable{
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
	 * Current session
	 */
	public transient Session currentSession;

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
	public void initTransient(Object parent) {
		// TODO Auto-generated method stub
		
	}

}
