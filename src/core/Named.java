package core;

/**
 * A named object.
 * 
 * @author EPICI
 * @version 1.0
 */
public interface Named {

	/**
	 * Get the name of the object, or if it has no name,
	 * a default name.
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * Attempt to change the name to some other value.
	 * If changing to that name is disallowed, returns false,
	 * and no data should change.
	 * 
	 * @param newName
	 * @return
	 */
	public boolean setName(String newName);
	
}
