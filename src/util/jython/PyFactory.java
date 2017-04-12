package util.jython;

import java.io.*;
import org.python.core.*;
import org.python.util.*;

/**
 * Factory pattern for creating instances of any Java object
 * 
 * @author EPICI
 * @version 1.0
 */
public class PyFactory<T> implements Factory<T> {
	private static final long serialVersionUID = 1L;
	
	/**
	 * The class of T, needed for coercion
	 */
	public final Class<T> javaClass;
	/**
	 * Class to instantiate by calling
	 */
	private final PyObject instanceClass;
	
	/**
	 * Combined constructors
	 * 
	 * @param javaClass the class of T
	 * @param a see below
	 * @param b the class name
	 * @param asScript if true, runs <i>a</i> as a script, otherwise
	 * attempts to load a module or package named <i>a</i>
	 */
	public PyFactory(Class<T> javaClass,String a,String b,boolean asScript){
		this.javaClass = javaClass;
		PythonInterpreter interpreter = new PythonInterpreter();
		/*
		 * Import the core classes
		 * If a scripter needs anything else, they are expected to know about
		 * and import it
		 */
		for(String packageName:new String[]{
				"main",
				"utils",
				"components"
		})
			interpreter.exec("from "+packageName+" import *");
		if(asScript){
			interpreter.exec(a);
		}else{
			interpreter.exec("from "+a+" import "+b);
		}
		instanceClass = interpreter.get(b);
		interpreter.cleanup();
		interpreter.close();
	}
	
	public T create(PyObject[] args,String[] keywords){
		return (T)(instanceClass.__call__(args,keywords)).__tojava__(javaClass);
	}
}
