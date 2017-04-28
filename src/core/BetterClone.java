package core;

/**
 * Cloneable doesn't work. It's just plain terrible.
 * <br>
 * This interface is meant to replace it.
 * 
 * @author EPICI
 * @version 1.0
 *
 * @param <MetaType> the type of metadata. Even if it's a primitive,
 * it has to be wrapped in an object.
 * @param <Self> itself. Hacky way of enforcing it.
 */
public interface BetterClone<MetaType,Self extends BetterClone<MetaType,Self>> {
	/**
	 * Shallow copy, generally returns a different object with copied fields
	 * <br>
	 * If metaData is null (likely because a user didn't know)
	 * and an object is expected, use some form of default
	 * 
	 * @param metaData metadata which can influence how it's cloned
	 * @return a shallow copy
	 */
	public Self shallowCopy(MetaType metaData);
	/**
	 * Do its best to copy everything
	 * <br>
	 * Standard convention is if an object also implements BetterClone, then
	 * its deepCopy method is called if possible
	 * <br>
	 * If metaData is null (likely because a user didn't know)
	 * and an object is expected, use some form of default
	 * 
	 * @param metaData metadata which can influence how it's cloned
	 * @return a deep copy
	 */
	public Self deepCopy(MetaType metaData);
}
