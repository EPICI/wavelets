package core;

import java.util.*;

/**
 * Cloneable doesn't work. It's just plain terrible.
 * <br>
 * This interface is meant to replace it.
 * 
 * @author EPICI
 * @version 1.1
 *
 * @param <Self> itself. Hacky way of enforcing it.
 */
public interface BetterClone<Self extends BetterClone<Self>> {
	/**
	 * Copy to a certain depth.
	 * <br>
	 * The depth is not necessarily the reference depth. For example, consider
	 * a tree list. A proper depth 0 copy requires duplicating the entire tree
	 * structure, which is possibly several references away from the root,
	 * while the objects stored by the list should be the same as the original.
	 * A depth 1 copy would use depth 0 copies of the list items.
	 * <br>
	 * Common options:
	 * <ul>
	 * <li>&quot;replace&quot; is an identity map of already copied objects;
	 * if an object that would be copied is in the map, the mapped value is used
	 * in place of the copy; the implementor will also modify the map in-place
	 * to include new copied objects; this is required for a true deep copy</li>
	 * <li>&quot;blacklist&quot; is some collection of strings where
	 * if the name of a field is or is similar to a string in the collection,
	 * it should not be copied regardless of whether the depth would allow it;
	 * prefix with &quot;*&quot; to blacklist a class instead of individual fields</li>
	 * <li>&quot;whitelist&quot; is some collection of strings where
	 * if the name of a field is or is similar to a string in the collection,
	 * it should be copied regardless of whether the depth would allow it</li>
	 * <li>&quot;set&quot; is a string-object map which says specific fields should
	 * be changed in the copy; this is useful for modifying immutable objects</li>
	 * </ul>
	 * 
	 * @param depth 0 for shallow, some other number for a depth limit
	 * @param options if provided, contains additional named options
	 * which can affect the behaviour
	 * @return
	 */
	public Self copy(int depth,Map<String,Object> options);
	
	/**
	 * Put any <i>options</i> parameter through this first, it will fix
	 * nulls and add blanks for missing common values.
	 * 
	 * @param options copy options
	 * @return
	 */
	public static Map<String,Object> fixOptions(Map<String,Object> options){
		if(options==null){
			options = new HashMap<>();
		}
		Map<Object,Object> replace = (Map<Object,Object>)options.get("replace");
		if(replace==null){
			options.put("replace",
					new IdentityHashMap<>());
		}
		Collection<String> blacklist = (Collection<String>)options.get("blacklist");
		if(blacklist==null){
			options.put("blacklist",
					new TreeSet<>());
		}
		Collection<String> whitelist = (Collection<String>)options.get("whitelist");
		if(whitelist==null){
			options.put("whitelist",
					new TreeSet<>());
		}
		Map<String,Object> set = (Map<String,Object>)options.get("set");
		if(set==null){
			options.put("set",
					new HashMap<>());
		}
		return options;
	}
	
	/**
	 * Useful for whitelist and blacklist: check if the field name or class
	 * is in the collection.
	 * 
	 * @param collection collection to search in
	 * @param oclass class of value to check for
	 * @param name if provided, also check for this exact string
	 * @return
	 */
	public static boolean fieldIncluded(Collection<String> collection,Class<?> oclass,String name){
		if(name!=null && collection.contains(name))return true;
		while(oclass!=null && oclass!=Object.class){
			String check = "*"+oclass.getCanonicalName();
			if(collection.contains(check))return true;
			oclass = oclass.getSuperclass();
		}
		return false;
	}
	
	/**
	 * Copies <i>source</i>, with safety. Special cases taken care of by this method:
	 * <ul>
	 * <li>If <i>source</i> is null, returns null</li>
	 * <li>If <i>depth</i> is negative, returns <i>source</i> uncopied</li>
	 * <li>If <i>source</i> is in the &quot;replace&quot; in <i>options</i>,
	 * returns the mapped value</li>
	 * <li>If the class of <i>source</i> is in the &quot;blacklist&quot;
	 * in <i>options</i>, returns <i>source</i> uncopied</li>
	 * <li>If <i>options</i> is null, it will be substituted; however this
	 * will lose all the data in the options so don't do this</li>
	 * </ul>
	 * 
	 * @param source object to copy
	 * @param depth
	 * @param options
	 * @return
	 */
	public static <T extends BetterClone<T>> T copy(T source,int depth,Map<String,Object> options){
		if(source==null)return null;
		if(depth<0)return source;
		options = fixOptions(options);
		Class<?> sourceClass = source.getClass();
		Collection<String> blacklist = (Collection<String>)options.get("blacklist");
		if(blacklist.contains("*"+sourceClass.getCanonicalName())){
			// We only accept the full name to avoid ambiguity
			return source;
		}
		Map<Object,Object> replace = (Map<Object,Object>)options.get("replace");
		Object replaceWith = replace.get(source);
		if(replaceWith!=null){
			return (T)replaceWith;
		}
		T result = source.copy(depth, options);
		replace.put(source, result);
		return result;
	}
}
