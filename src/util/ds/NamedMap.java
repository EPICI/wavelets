package util.ds;

import core.*;
import java.io.*;
import java.util.*;
import org.apache.commons.collections4.*;
import org.apache.commons.collections4.trie.*;
import org.apache.commons.collections4.bidimap.*;

/**
 * A collection of named objects, keyed by their names.
 * <br>
 * Various views give most functionality that should be in the
 * class itself but isn't because the creator was being lazy.
 * For example, for sorted map operations access the forward map directly.
 * 
 * @author EPICI
 * @version 1.0
 *
 * @param <T>
 */
public class NamedMap<T extends Named> implements Serializable{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Forward map.
	 */
	public PatriciaTrie<T> forwardMap;
	/**
	 * Backward map.
	 */
	public transient IdentityHashMap<T, String> backwardMap;
	/**
	 * Two way map, view of the forward and backward maps.
	 * Recommended to use this for map operations.
	 */
	public transient DualMap<T> dualMap;
	
	/**
	 * Inner class, used as the map because API weirdness in Apache Commons Collections
	 * demands it.
	 * 
	 * @author EPICI
	 * @version 1.0
	 *
	 * @param <T>
	 */
	public static class DualMap<T> extends AbstractDualBidiMap<String,T>{
		
		/**
		 * Redirect constructor.
		 * 
		 * @param forward
		 * @param backward
		 */
		public DualMap(Map<String, T> forward,Map<T, String> backward){
			super(forward,backward);
		}

		@Override
		protected BidiMap<T, String> createBidiMap(Map<T, String> forward, Map<String, T> backward, BidiMap<String, T> bidi) {
			return null;
		}
		
	}
	
	/**
	 * Blank constructor.
	 */
	public NamedMap(){
		forwardMap = new PatriciaTrie<>();
		initTransient();
	}
	
	/**
	 * Copy constructor.
	 * 
	 * @param source
	 */
	public NamedMap(NamedMap<T> source){
		this();
		dualMap.putAll(source.dualMap);
	}
	
	/**
	 * Initialize transient fields.
	 */
	public void initTransient(){
		backwardMap = new IdentityHashMap<>();
		dualMap = new DualMap<T>(forwardMap,backwardMap);
		MapIterator<String,T> iter = forwardMap.mapIterator();
		while(iter.hasNext()){
			String k = iter.next();
			T v = iter.getValue();
			backwardMap.put(v, k);
			// no need to update dualMap because it's a view
		}
	}
	
	/**
	 * Attempts a rename operation, returns true on success.
	 * If failed, nothing should change.
	 * 
	 * @param oldName the name of the object to change
	 * @param newName the new name to give to the object
	 * @return did it succeed?
	 */
	public boolean rename(String oldName,String newName){
		// Skip if the request is clearly bad
		if(oldName!=null
				&& newName!=null
				&& oldName!=newName
				&& forwardMap.containsKey(oldName)
				&&!forwardMap.containsKey(newName)){
			T value = forwardMap.get(oldName);
			// test if it will reject the new name
			if(value.setName(newName)){
				// it removes the old pairing for us, so no more work needed
				dualMap.put(newName, value);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Unsafe version of {@link #rename(String, String)}. Forces a rename,
	 * and doesn't update the value's name. Useful if multiple maps point to
	 * the same objects.
	 * 
	 * @param oldName
	 * @param newName
	 */
	public void renameMapOnly(String oldName,String newName){
		T value = forwardMap.get(oldName);
		dualMap.put(newName, value);
	}
	
	/*
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}
	*/

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
		initTransient();
	}

	/*
	 * TODO method to get next valid name
	 * Name -> Name.001 -> Name.002
	 * Name. -> Name..001
	 * Name.2 -> Name.000 -> Name.001 -> Name.003
	 * takes 2 parameters, base name and exclusion set
	 * additional 2 parameters or from preferences:
	 * number of digits (3 here)
	 * first value (0 here, but can be 1 or other)
	 * Note that empty suffix is treated as 0
	 * This is based on Blender's naming system.
	 * 
	 * implementation:
	 * use regex to get numbers, filter with help of trie
	 * create hashset with numbers of self and others disallowed
	 * get size of hashset
	 * create bitset from hashset, ignoring any at or higher than size
	 * use nextClearBit()
	 */
	
}