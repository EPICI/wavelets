package util.jython;

import java.io.Serializable;
import org.python.core.PyObject;

/**
 * A factory, meant for Jython
 * 
 * @author EPICI
 * @version 1.0
 * @param <T> the type to create
 */
public interface Factory<T> extends Serializable {

	/**
	 * Create a new T instance
	 * 
	 * @param args Python args and keyword args
	 * @param keywords Python keywords
	 * @return a new T object
	 */
	public T create(PyObject[] args,String[] keywords);
}
