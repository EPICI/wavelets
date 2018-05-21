package core;

import java.util.*;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.collections4.iterators.IteratorChain;
import org.apache.commons.collections4.trie.PatriciaTrie;

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
	 * If a key-value pair <i>k:v</i> exists in this map,
	 * v provides a way to copy objects of type k.
	 */
	static final HashMap<Class<?>,Copier<?>> copiers = new HashMap<>();
	
	/**
	 * Designate the copier which will copy a particular class.
	 * If the subclasses don't have their own copiers, this will
	 * copy for them as well.
	 * 
	 * @param cls
	 * @param copier
	 */
	public static <T> void setCopier(Class<T> cls,Copier<T> copier){
		copiers.put(cls, copier);
	}
	
	/**
	 * Get the copier designated to copy a particular class.
	 * Returns null if no copier is designated for this class.
	 * Does not consider superclasses.
	 * 
	 * @param cls
	 * @return
	 */
	public static <T> Copier<T> getCopier(Class<T> cls){
		return (Copier<T>) copiers.get(cls);
	}
	
	/**
	 * Same as {@link #getCopier(Class)} except superclasses are considered.
	 * Considers this class, then its superclass, then the super's super, etc.
	 * until it finds a copier or reaches {@link Object}.
	 * 
	 * @param cls
	 * @return
	 */
	public static Copier<?> getCopierSuper(Class<?> cls){
		List<Class<?>> superclasses = ClassUtils.getAllSuperclasses(cls);
		for(Class<?> icls:superclasses){// look for nearest class with copier
			Copier<?> copier = getCopier(icls);
			if(copier!=null)return copier;
		}
		return null;
	}
	
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
	 * it should be copied regardless of whether the depth would allow it
	 * (conventionally overrides blacklist)</li>
	 * <li>&quot;set&quot; is a string-object map which says specific fields should
	 * be changed in the copy; this is useful for modifying immutable objects</li>
	 * <li>&quot;session&quot; is the current session</li>
	 * </ul>
	 * Class names should be retrieved by {@link Class#getCanonicalName()}.
	 * <br>
	 * Special values for &quot;set&quot; in order they should be applied:
	 * <ol>
	 * <li>&quot;.filter&quot; is usable for a collection, and should be a
	 * {@link java.util.function.Predicate} which returns true if an element should
	 * be kept and false if it should be removed.</li>
	 * <li>&quot;.size&quot; is usable for a collection, and indicates it should
	 * be trimmed to at most that size, or in some cases, filled to that size.</li>
	 * </ol>
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
					Collections.newSetFromMap(new PatriciaTrie<>())
					);
		}
		Collection<String> whitelist = (Collection<String>)options.get("whitelist");
		if(whitelist==null){
			options.put("whitelist",
					Collections.newSetFromMap(new PatriciaTrie<>())
					);
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
	 * <br>
	 * If using {@link #copy(BetterClone, int, Map)} or {@link #tryCopy(Object, int, Map)},
	 * that already includes
	 * 
	 * @param collection collection to search in
	 * @param oclass if provided, class of value to check for
	 * @param name if provided, also check for this exact string
	 * @return
	 */
	public static boolean fieldIncluded(Collection<String> collection,Class<?> oclass,String name){
		// check for the name itself
		if(name!=null && collection.contains(name))return true;
		if(oclass!=null){
			// implemented interfaces
			List<Class<?>> interfaces = ClassUtils.getAllInterfaces(oclass);
			// superclasses
			List<Class<?>> superclasses = ClassUtils.getAllSuperclasses(oclass);
			// join iterators
			IteratorChain<Class<?>> iter = new IteratorChain<>(interfaces.iterator(),superclasses.iterator());
			// convert to set for performance
			HashSet<String> test = new HashSet<>();
			while(iter.hasNext()){
				Class<?> cls = iter.next();
				String matchTo = "*"+cls.getCanonicalName();
				test.add(matchTo);
			}
			// test for match
			if(!Collections.disjoint(test, collection))return true;
		}
		return false;
	}
	
	/**
	 * Attempt to copy an object even if it does not implement {@link BetterClone}.
	 * Attempts, in order, are:
	 * <ol>
	 * <li>Cast to {@link BetterClone} and use {@link #copy(int, Map)}.</li>
	 * <li>Find a {@link Copier} in {@link #copiers} (access it with
	 * {@link #setCopier(Class, Copier)} and {@link #getCopier(Class)}) and use
	 * that to make a copy.</li>
	 * </ol>
	 * On failure, returns the original object uncopied.
	 * <br>
	 * Other than additional attempts, is the same as {@link #copy(BetterClone, int, Map)}.
	 * 
	 * @param source
	 * @param depth
	 * @param options
	 * @return
	 */
	public static <T> T tryCopy(T source,int depth,Map<String,Object> options){
		// null becomes null
		if(source==null)return null;
		// depth check
		if(depth<0)return source;
		// fix options
		options = fixOptions(options);
		// fetch class
		Class<?> sourceClass = source.getClass();
		// test blacklist/whitelist
		Collection<String> blacklist = (Collection<String>)options.get("blacklist");
		Collection<String> whitelist = (Collection<String>)options.get("whitelist");
		if(fieldIncluded(blacklist,sourceClass,null)
				&&!fieldIncluded(whitelist,sourceClass,null)
				){
			return source;
		}
		// test replace
		Map<Object,Object> replace = (Map<Object,Object>)options.get("replace");
		Object replaceWith = replace.get(source);
		if(replaceWith!=null){
			return (T)replaceWith;
		}
		// null until an attempt succeeds
		T result = null;
		// attempt: use BetterClone
		if(source instanceof BetterClone<?>){
			BetterClone<?> csource = (BetterClone<?>) source;
			result = (T) csource.copy(depth,options);
		}
		// attempt: use Copier
		if(result==null){
			Copier<?> copier = getCopierSuper(sourceClass);
			if(copier!=null){
				result = (T) copier.copyCast(source, depth, options);
			}
		}
		// all attempts failed
		if(result==null)result = source;
		// put in replace map
		replace.put(source, result);
		// return
		return result;
	}
	
	/**
	 * Copies <i>source</i>, with safety. Special cases taken care of by this method:
	 * <ul>
	 * <li>If <i>source</i> is null, returns null</li>
	 * <li>If <i>depth</i> is negative, returns <i>source</i> uncopied</li>
	 * <li>If <i>source</i> is in the &quot;replace&quot; in <i>options</i>,
	 * returns the mapped value</li>
	 * <li>If the class of <i>source</i>, any of its superclasses,
	 * or any of its implemented interfaces is in the &quot;blacklist&quot;
	 * but not in the &quot;whitelist&quot;
	 * in <i>options</i>, returns <i>source</i> uncopied</li>
	 * <li>If <i>options</i> is null, it will be substituted; however this
	 * will lose all the data in the options so don't do this</li>
	 * </ul>
	 * <br>
	 * The generic <i>T</i> does not need to implement <i>BetterClone&lt;T&gt;</i>,
	 * however, the copy class needs to be <i>T</i> or a subclass or implementor of it.
	 * <br>
	 * Note: this actually redirects to {@link #tryCopy(Object, int, Map)}.
	 * 
	 * @param source object to copy
	 * @param depth
	 * @param options
	 * @return
	 */
	public static <T extends BetterClone<?>> T copy(T source,int depth,Map<String,Object> options){
		/*
		 * This is a simple redirect, so it should get inlined.
		 * The compiler should notice the BetterClone attempt always works,
		 * and will optimize away later attempts.
		 */
		return tryCopy(source,depth,options);
	}
	
	/**
	 * An interface which allows copying using a helper object
	 * which determines copy functionality. Originally meant
	 * so that classes not implementing {@link BetterClone}
	 * can copy in a similar manner, but may also be used to define
	 * multiple ways of copying the same type.
	 * 
	 * @author EPICI
	 * @version 1.0
	 *
	 * @param <T>
	 */
	public static interface Copier<T>{
		/**
		 * Like {@link BetterClone#copy(int, Map)}, but since we are copying
		 * a different object, that object is a parameter.
		 * 
		 * @param source the object to copy
		 * @param depth see {@link BetterClone#copy(int, Map)}
		 * @param options see {@link BetterClone#copy(int, Map)}
		 * @return
		 */
		public T copy(T source,int depth,Map<String,Object> options);
		
		/**
		 * Call {@link #copy(Object, int, Map)} by casting <i>source</i> to <i>T</i>.
		 * 
		 * @param source
		 * @param depth
		 * @param options
		 * @return
		 */
		public default T copyCast(Object source,int depth,Map<String,Object> options){
			return copy((T)source,depth,options);
		}
	}
}
