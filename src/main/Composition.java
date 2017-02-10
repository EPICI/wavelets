package main;

import java.util.*;

public class Composition implements TransientContainer<Object>, TLCParent, Destructable{
	private static final long serialVersionUID = 1L;
	
	//Generic storage
	public HashMap<String,Object> globalsStatic;
	//Copied when playing
	public transient HashMap<String,Object> globalsDynamic;

	/*
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
