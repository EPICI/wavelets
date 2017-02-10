package main;

//Better clone interface, where metadata can be specified
public interface BetterClone<MetaType,Self extends BetterClone<MetaType,Self>> {
	/*
	 * Shallow copy, generally returns a different object with copied fields
	 * If metaData is null and an object is expected, use some form of default
	 */
	public Self shallowCopy(MetaType metaData);
	/*
	 * Do its best to copy everything
	 * Standard convention is if an object also implements BetterClone, then
	 * its deepCopy method is called if possible
	 * If metaData is null and an object is expected, use some form of default
	 */
	public Self deepCopy(MetaType metaData);
}
