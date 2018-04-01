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
	 * </ul>
	 * 
	 * @param depth 0 for shallow, some other number for a depth limit
	 * @param options if provided, contains additional named options
	 * which can affect the behaviour
	 * @return
	 */
	public Self copy(int depth,Map<String,Object> options);
}
