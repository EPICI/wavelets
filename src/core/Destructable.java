package core;

/**
 * Basically every class that needs a finalizer
 * and doesn't want to trust the garbage collector
 * 
 * @author EPICI
 * @version 1.0
 */
public interface Destructable {
	/**
	 * Destroy or dereference everything
	 * <br>
	 * In other words, prepare to be garbage collected
	 */
	public void destroy();
	/**
	 * Gets rid of any cyclic references and dereference
	 * fields which definitely won't be shared with any other
	 * objects
	 */
	public void destroySelf();
}
