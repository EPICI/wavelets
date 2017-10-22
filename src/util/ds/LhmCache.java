package util.ds;

import java.util.*;

/**
 * Simple cache based on {@link LinkedHashMap}
 * 
 * @author EPICI
 * @version 1.0
 *
 * @param <K>
 * @param <V>
 */
public class LhmCache<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = 1L;

	/**
	 * Default load factor as passed to {@link LinkedHashMap} constructor
	 */
	public static final float LOAD_FACTOR = 1.25f;
	
	/**
	 * After this many entries exceeded, delete the oldest
	 */
	public final int cacheLimit;

	public LhmCache(int limit) {
		super(limit, LOAD_FACTOR);
		cacheLimit = limit;
	}

	public LhmCache(Map<? extends K, ? extends V> m,int limit) {
		super(m);
		cacheLimit = limit;
	}

	public LhmCache(int limit, boolean accessOrder) {
		super(limit, LOAD_FACTOR, accessOrder);
		cacheLimit = limit;
	}
	
	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> entry){
		return size()>cacheLimit;
	}

}
