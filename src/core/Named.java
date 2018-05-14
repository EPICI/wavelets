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
	
	/**
	 * Equivalent to calling {@link #setName(String)} on <i>obj</i>
	 * but with additional checks. If either argument is null
	 * or the new name is blank (after calling {@link String#trim()}),
	 * does nothing. Implemented as a static utility
	 * to take load off of implementors.
	 * <br>
	 * Unless dealing with internal data structures only,
	 * prefer calling this method to calling {@link #setName(String)}.
	 * 
	 * @param obj object to rename
	 * @param newName new name to give
	 * @return
	 */
	public static boolean setName(Named obj,String newName){
		if(obj==null || newName==null || newName.trim().length()==0)return false;
		return obj.setName(newName);
	}
	
}
