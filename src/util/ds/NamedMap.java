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
 * <br>
 * The method
 * {@link #nextName(String, int, boolean, int, int, String, int)}
 * and its family are for a naming system with
 * numeric suffixes. It has a few quirks, but nonetheless it
 * is a simple to understand and robust system.
 * This idea was taken from Blender, which uses names like:
 * <ul>
 * <li>Cube &#x2192; Cube.001 &#x2192; Cube.002 &#x2192; Cube.003 &#x2192; Cube.004</li>
 * <li>Plane.000 &#x2192; Plane.001 &#x2192; Plane.002 &#x2192; Plane.003 &#x2192; Plane.004</li>
 * <li>Cone.002 &#x2192; Cone.000 &#x2192; Cone.001 &#x2192; Cone.003 &#x2192; Cone.004</li>
 * <li>Torus.99999999 &#x2192; Torus.000 &#x2192; Torus.001 &#x2192; Torus.002 &#x2192; Torus.003</li>
 * <li>Empty. &#x2192; Empty..001 &#x2192; Empty..002 &#x2192; Empty..003 &#x2192; Empty..004</li>
 * </ul>
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
	 * Attempts a rename operation. On success, returns
	 * what name the object now has, which may not be the requested name.
	 * On failure, returns null, and no data is changed.
	 * 
	 * @param oldName the name of the object to change
	 * @param newName the new name to give to the object
	 * @param session determines some nuances, optional
	 * @return actual new name if succeeded, null if failed
	 */
	public String rename(String oldName,String newName,Session session){
		// Skip if the object doesn't even exist
		if(oldName!=null
				&& newName!=null
				&& oldName!=newName
				&& forwardMap.containsKey(oldName)){
			// On failure, we will add this back
			T value = dualMap.remove(oldName);
			// Can we accept the given name?
			if(!forwardMap.containsKey(newName)
					&& value.setName(newName)){
				// it removes the old pairing for us, so no more work needed
				dualMap.put(newName, value);
				return newName;
			}
			// Preferences determines whether we should try other names
			if(Preferences.getBooleanSafe(session, Preferences.INDEX_BOOLEAN_NAMEDMAP_RENAME_USE_NEXTNAME)){
				// What name would be next?
				newName = nextName(oldName,-1,false,session);
				if(value.setName(newName)){
					dualMap.put(newName, value);
					return newName;
				}
			}
			// It failed, put the old mapping back
			dualMap.put(oldName, value);
		}
		return null;
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
	
	/**
	 * Get the string <i>nameBase</i>+<i>partsSeparator</i>+<i>nextIndex</i>,
	 * where <i>nextIndex</i> is a <i>digitsIndex</i> digit integer greater than
	 * or equal to <i>minIndex</i> and so the result is not in this map.
	 * If <i>nameIncluded</i>, then <i>nameBase</i>+<i>partsSeparator</i>+<i>nameIndex</i>
	 * cannot be the result.
	 * Different length <i>nextIndex</i> corresponding to the same
	 * integer are treated as equivalent.
	 * <br>
	 * If <i>nameIndex</i> is negative, treats <i>nameBase</i>
	 * as a full name and splits it to get a new <i>nameBase</i> and <i>nameIndex</i>.
	 * If of the form <i>nameBase</i>+<i>partsSeparator</i>+<i>nameIndex</i>,
	 * then <i>nameIndex</i> is used as the index, otherwise <i>blankIndex</i> is used.
	 * <br>
	 * If <i>blankIndex</i> is less than -1, it will be replaced by
	 * <i>minIndex</i>-<i>blankIndex</i>-2. As such, -1 should be used to
	 * completely filter out names with empty suffixes, and -2 should be used
	 * to assume the minimum for those.
	 * 
	 * @param nameBase common prefix string
	 * @param nameIndex index of current string
	 * @param nameIncluded whether to include current string as candidate
	 * @param digitsIndex requested length of index string in result
	 * @param minIndex minimum considered index string
	 * @param partsSeparator goes between prefix and index
	 * @param blankIndex assumed index for no index suffix
	 * @return
	 */
	public String nextName(String nameBase,int nameIndex,boolean nameIncluded,int digitsIndex,int minIndex,String partsSeparator,int blankIndex){
		// if negative nameIndex, split
		if(nameIndex<0){
			// convenience variable
			String name = nameBase;
			// first get our java regex with escaping
			String regex = "(.+?)"+java.util.regex.Pattern.quote(partsSeparator)+"(\\d+)";
			// make pattern for it
			java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
			java.util.regex.Matcher matcher = pattern.matcher(name);
			// no index, then assume blankIndex
			nameBase = name;
			nameIndex = blankIndex;
			// if specified, use that index
			if(matcher.matches()){
				nameBase = matcher.group(1);
				nameIndex = Integer.parseInt(matcher.group(2));
			}
		}
		// add prefix and separator
		StringBuilder sb = new StringBuilder();
		sb.append(nameBase);
		sb.append(partsSeparator);
		// get index suffix
		String suffix = Integer.toString(nextNameIndex(nameBase,nameIndex,nameIncluded,minIndex,partsSeparator,blankIndex));
		// pad to length
		for(int i=suffix.length();i<digitsIndex;i++)sb.append("0");
		sb.append(suffix);
		// result
		String result = sb.toString();
		return result;
	}
	
	/**
	 * Redirects to
	 * {@link #nextName(String, int, boolean, int, int, String, int)}
	 * using the session's {@link core.Preferences} to fill in the missing values.
	 * If the session or preferences are null, the default values will be used instead.
	 * 
	 * @param nameBase
	 * @param nameIndex
	 * @param nameIncluded
	 * @param session
	 * @return
	 */
	public String nextName(String nameBase,int nameIndex,boolean nameIncluded,Session session){
		return nextName(
				nameBase,
				nameIndex,
				nameIncluded,
				(int)Preferences.getIntSafe(session, Preferences.INDEX_INT_NAMEDMAP_NEXTNAME_DIGITS_INDEX),
				(int)Preferences.getIntSafe(session, Preferences.INDEX_INT_NAMEDMAP_NEXTNAME_MIN_INDEX),
				Preferences.getStringSafe(session, Preferences.INDEX_STRING_NAMEDMAP_NEXTNAME_PARTS_SEPARATOR),
				(int)Preferences.getIntSafe(session, Preferences.INDEX_INT_NAMEDMAP_NEXTNAME_BLANK_INDEX)
				);
	}
	
	/**
	 * Redirects to
	 * {@link #nextNameIndex(Set, String, int, boolean, int, String, int)}
	 * using the forward map to derive the set <i>candidates</i>.
	 * 
	 * @param nameBase
	 * @param nameIndex
	 * @param nameIncluded
	 * @param minIndex
	 * @param partsSeparator
	 * @param blankIndex
	 * @return
	 */
	public int nextNameIndex(String nameBase,int nameIndex,boolean nameIncluded,int minIndex,String partsSeparator,int blankIndex){
		int result = nextNameIndex(
				forwardMap.prefixMap(nameBase).keySet(),
				nameBase,
				nameIndex,
				nameIncluded,
				minIndex,
				partsSeparator,
				blankIndex);
		return result;
	}
	
	/**
	 * Gets just the index as used by
	 * {@link #nextName(String, int, boolean, int, int, String, int)}.
	 * 
	 * @param candidates possible strings to exclude
	 * @param nameBase
	 * @param nameIndex
	 * @param nameIncluded
	 * @param minIndex
	 * @param partsSeparator
	 * @param blankIndex
	 * @return
	 */
	public static int nextNameIndex(Set<String> candidates,String nameBase,int nameIndex,boolean nameIncluded,int minIndex,String partsSeparator,int blankIndex){
		// correct blankIndex
		if(blankIndex<-1)blankIndex = minIndex-blankIndex-2;
		// first get our java regex with escaping
		String regex = "(.+?)"+java.util.regex.Pattern.quote(partsSeparator)+"(\\d+)";
		// make pattern for it
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
		// initial exclusion set, items offset by -minIndex
		HashSet<Integer> exclude = new HashSet<>();
		// pre-filter by prefix, since to be equal it needs to have prefix
		for(String cand:candidates){
			java.util.regex.Matcher matcher = pattern.matcher(cand);
			// no index, then assume 0
			String candBase = cand;
			int candIndex = blankIndex;
			// if specified, use that index
			if(matcher.matches()){
				candBase = matcher.group(1);
				String candIndexString = matcher.group(2);
				// If more than 9 digits, it's insanely high and we can ignore it
				candIndex = candIndexString.length()<=9?Integer.parseInt(candIndexString):-1;
			}
			// only proceed if same name
			if(nameBase.equals(candBase)){
				// offset by -minIndex for exclusion set
				candIndex -= minIndex;
				// if below minIndex, not of interest
				if(candIndex>=0){
					exclude.add(candIndex);
				}
			}
		}
		// optionally, the request itself is included
		if(nameIncluded && nameIndex>=minIndex){
			exclude.add(nameIndex-minIndex);
		}
		// transfer to more efficient representation
		BitSet bexclude = new BitSet();
		int nexclude = exclude.size();
		/*
		 * if first n are excluded, then result is n
		 * in this case, all excluded are <n
		 * if not, then result is <n
		 * excluded >=n will never change result
		 * so we can filter n and up
		 */
		for(int candIndex:exclude){
			if(candIndex<nexclude){
				bexclude.set(candIndex);
			}
		}
		// get our result
		int result = bexclude.nextClearBit(0);
		// offset back by +minIndex
		result += minIndex;
		return result;
	}

	/*
	 * method to get next valid name
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
	 * 
	 * TODO redirect using preferences
	 */
	
}