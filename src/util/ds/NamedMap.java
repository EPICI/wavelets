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
 *
 * @param <T>
 */
public class NamedMap<T extends Named> extends AbstractDualBidiMap<String, T> implements Serializable{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Forward map.
	 */
	public PatriciaTrie<T> forwardMap;
	/**
	 * Backward map.
	 */
	public IdentityHashMap<T, String> backwardMap;
	
	/**
	 * View based constructor. For internal use only.
	 * 
	 * @param forwardMap the forward map to use (field not set)
	 * @param backwardMap the backward map to use (field not set)
	 */
	protected NamedMap(PatriciaTrie<T> forwardMap,IdentityHashMap<T, String> backwardMap){
		super(forwardMap,backwardMap);
	}
	
	/**
	 * Returns a new instance equivalent to using a blank constructor.
	 * 
	 * @return
	 */
	public static <T extends Named> NamedMap<T> create(){
		return create(new PatriciaTrie<>(),new IdentityHashMap<>());
	}
	
	/**
	 * Returns a new instance using the specified maps. For internal use only.
	 * 
	 * @param forwardMap
	 * @param backwardMap
	 * @return
	 */
	protected static <T extends Named> NamedMap<T> create(PatriciaTrie<T> forwardMap,IdentityHashMap<T, String> backwardMap){
		NamedMap<T> result = new NamedMap<>(forwardMap,backwardMap);
		result.forwardMap = forwardMap;
		result.backwardMap = backwardMap;
		return result;
	}
	
	/**
	 * Returns a new instance which is a shallow copy of the given instance.
	 * 
	 * @param source
	 * @return
	 */
	public static <T extends Named> NamedMap<T> copy(NamedMap<T> source){
		return create(source.forwardMap,source.backwardMap);
	}

	/**
	 * This method is supposed to return a new inverse map for the given maps.
	 * It's meaningless for this class, so it will return null.
	 * 
	 * @param normalMap
	 * @param reverseMap
	 * @param inverseBidiMap
	 * @return
	 */
	@Override
	protected BidiMap<T, String> createBidiMap(Map<T, String> normalMap, Map<String, T> reverseMap, BidiMap<String, T> inverseBidiMap) {
		return null;
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