package main;

import org.python.core.PyObject;

/**
 * Factory pattern for voices
 * <br>
 * Parent of both Java and Python implementations
 * 
 * @author EPICI
 * @version 1.0
 */
public interface VoiceFactory {
	/**
	 * Makes object creation easy with Python but hell with Java.
	 * <br>
	 * http://www.jython.org/javadoc/org/python/core/PyObject.html#__call__(org.python.core.PyObject[], java.lang.String[])
	 * <br>
	 * This works because in Python, you're not calling the constructor, you're calling the class
	 * 
	 * @param args Python args
	 * @param keywords Python keywords
	 * @return a new Voice object
	 */
	public Voice create(PyObject[] args,String[] keywords);
}
