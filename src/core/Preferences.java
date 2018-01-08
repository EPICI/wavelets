package core;

import java.io.Serializable;
import java.util.*;
import java.io.*;
import util.*;

/**
 * A standalone global preferences object
 * 
 * @author EPICI
 * @version 1.0
 */
public class Preferences implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Subpreferences defaults
	 * <br>
	 * Made public so scripts and stuff can add their own
	 */
	public static final HashMap<String,Sub<?,?>> SUB_DEFAULTS = new HashMap<>();
	
	/*
	 * Indexes for various built-in settings
	 * 
	 * Order reflects the order in which they were added,
	 * which roughly reflects the order they were needed,
	 * which roughly reflects the order in which the consuming code was created
	 * 
	 * Order doesn't have any other significance!
	 */
	
	// Booleans (boolean)
	public static final int INDEX_TLS_ALLOW_PATTERN_CONVERT = 0;
	public static final int INDEX_TLS_ALLOW_PATTERN_MERGE = 0;
	
	// Ints (long)
	public static final int INDEX_TLS_PATTERN_BAR_GRADIENT_SHIFT = 0;
	
	// Floats (double)
	
	/**
	 * Boolean defaults
	 */
	private static final boolean[] BOOLEAN_DEFAULTS = {
			false,
			false,
	};
	/**
	 * Integer defaults
	 */
	private static final long[] INT_DEFAULTS = {
			30,
	};
	/**
	 * Float defaults
	 */
	private static final double[] FLOAT_DEFAULTS = {
			
	};
	
	/**
	 * Total number of booleans
	 */
	public static final int BOOLEAN_COUNT = BOOLEAN_DEFAULTS.length;
	/**
	 * Total number of integers
	 */
	public static final int INT_COUNT = INT_DEFAULTS.length;
	/**
	 * Total number of floats
	 */
	public static final int FLOAT_COUNT = FLOAT_DEFAULTS.length;
	
	/**
	 * Get a default boolean value
	 * @param index index, should be named
	 * @return the default associated boolean value
	 */
	public static boolean getDefaultBoolean(int index){
		return BOOLEAN_DEFAULTS[index];
	}
	
	/**
	 * Get a default integer value
	 * @param index index, should be named
	 * @return the default associated integer value
	 */
	public static long getDefaultInt(int index){
		return INT_DEFAULTS[index];
	}
	
	/**
	 * Get a default float value
	 * @param index index, should be named
	 * @return the default associated float value
	 */
	public static double getDefaultFloat(int index){
		return FLOAT_DEFAULTS[index];
	}

	/**
	 * All booleans in preferences
	 * <br>
	 * It's recommended to use ints instead since booleans
	 * only allow for one of two values, and more may be needed later
	 */
	protected BitSet booleans;
	/**
	 * The number of booleans in the bitset
	 */
	protected int localBooleanCount;
	/**
	 * All integers in preferences
	 * <br>
	 * Stored as longs instead because while ints may
	 * be convenient in certain contexts (such as indices and switches),
	 * most of the time longs can do the same thing, and have
	 * the advantage of a larger word size
	 */
	protected long[] ints;
	/**
	 * All floats in preferences, with double precision
	 */
	protected double[] floats;
	/**
	 * All subpreferences
	 */
	protected HashMap<String,Sub<?,?>> subprefs;
	
	/**
	 * Initialize with factory settings
	 */
	public Preferences(){
		subprefs = new HashMap<>();
		restoreDefaults();
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException{
		out.defaultWriteObject();
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
		in.defaultReadObject();
		extendDefaults();//May be deserialized from an older version
	}
	
	/**
	 * The current data is too short, so extend it with defaults
	 */
	protected void extendDefaults(){
		int localBooleanCount = this.localBooleanCount, localIntCount = ints.length, localFloatCount = floats.length;
		ints = Arrays.copyOf(ints, INT_COUNT);
		floats = Arrays.copyOf(floats, FLOAT_COUNT);
		for(;localBooleanCount<BOOLEAN_COUNT;localBooleanCount++)
			booleans.set(localBooleanCount,getDefaultBoolean(localBooleanCount));
		for(;localIntCount<INT_COUNT;localIntCount++)
			ints[localIntCount] = getDefaultInt(localIntCount);
		for(;localFloatCount<FLOAT_COUNT;localFloatCount++)
			floats[localFloatCount] = getDefaultFloat(localFloatCount);
		this.localBooleanCount = localBooleanCount;
		subprefs.putAll(SUB_DEFAULTS);
	}
	
	/**
	 * Set all data to default values
	 */
	public void restoreDefaults(){
		booleans = new BitSet();
		localBooleanCount = 0;
		ints = new long[0];
		floats = new double[0];
		subprefs = new HashMap<>();
		extendDefaults();
	}
	
	/**
	 * @return bit set containing all boolean preferences
	 */
	public BitSet getBooleans(){
		return booleans;
	}
	
	/**
	 * @return long array containing all integer preferences
	 */
	public long[] getLongs(){
		return ints;
	}
	
	/**
	 * @return double array containing all float preferences
	 */
	public double[] getDoubles(){
		return floats;
	}
	
	/**
	 * Get boolean for {@link Session}'s preferences, null-safe
	 * 
	 * @param session session to fetch preferences for
	 * @param index index of value to fetch
	 * @return value in preferences, or default if null given
	 */
	public static boolean getBooleanSafe(Session session,int index){
		if(session==null)return getDefaultBoolean(index);
		Preferences pref = session.preferences;
		if(pref==null)return getDefaultBoolean(index);
		return pref.booleans.get(index);
	}
	
	/**
	 * Get boolean for {@link Session}'s preferences, null-safe
	 * 
	 * @param session session to fetch preferences for
	 * @param index index of value to fetch
	 * @return value in preferences, or default if null given
	 */
	public static long getLongSafe(Session session,int index){
		if(session==null)return getDefaultInt(index);
		Preferences pref = session.preferences;
		if(pref==null)return getDefaultInt(index);
		return pref.ints[index];
	}
	
	/**
	 * Get boolean for {@link Session}'s preferences, null-safe
	 * 
	 * @param session session to fetch preferences for
	 * @param index index of value to fetch
	 * @return value in preferences, or default if null given
	 */
	public static double getDoubleSafe(Session session,int index){
		if(session==null)return getDefaultFloat(index);
		Preferences pref = session.preferences;
		if(pref==null)return getDefaultFloat(index);
		return pref.floats[index];
	}
	
	/**
	 * Get subpreferences by name
	 * <br>
	 * If null, hasn't been set
	 * 
	 * @param name the name
	 * @return a subpreferences object
	 */
	public <K,V> Sub<K,V> getSub(String name){
		return (Sub<K,V>)subprefs.get(name);
	}
	
	/**
	 * Set (may override) subpreferences by name
	 * 
	 * @param name the name
	 * @param subpref subpreferences object to set
	 */
	public void setSub(String name,Sub<?,?> subpref){
		subprefs.put(name, subpref);
	}
	
	/**
	 * Get the subpreferences map
	 * <br>
	 * This method is provided for when other operations or
	 * anything more advanced is needed
	 * 
	 * @return map with all subpreferences
	 */
	public HashMap<String,Sub<?,?>> getSubPreferences(){
		return subprefs;
	}
	
	/**
	 * Subpreferences for a specific module or something
	 * <br>
	 * Because they aren't used by the main program, fields
	 * are public to allow for fast access
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public static class Sub<K,V>{
		
		/**
		 * All booleans
		 */
		public BitSet booleans;
		/**
		 * All integers
		 */
		public long[] ints;
		/**
		 * All floats
		 */
		public double[] floats;
		/**
		 * Everything else
		 */
		public Map<K,V> objs;
	}

}
