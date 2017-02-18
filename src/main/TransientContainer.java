package main;

import java.io.Serializable;

/**
 * All objects with transient fields that need to be initialized
 * when the program starts or whenever they get deserialized/loaded
 * 
 * @author EPICI
 * @param <T> parent type
 */
public interface TransientContainer<T> extends Serializable {
	/**
	 * Initialize anything that needs to be initialized
	 * 
	 * @param parent the object's parent, nobody should need to use reflection
	 */
	public void initTransient(T parent);
}
