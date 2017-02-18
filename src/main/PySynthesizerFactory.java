package main;

import org.python.core.*;
import org.python.util.*;

/**
 * Factory pattern for creating instances of Python {@link Synthesizer}s
 * from scripts or text
 * 
 * @author EPICI
 * @version 1.0
 */
public class PySynthesizerFactory {
	public static final Class<Synthesizer> synthesizerClass = Synthesizer.class;
	
	/**
	 * Class to instantiate by calling
	 */
	private PyObject instanceClass;
	
	/**
	 * Combined constructors
	 * 
	 * @param a script as text if true, script file location if false
	 * @param b class name
	 * @param asScript toggle behaviour
	 */
	public PySynthesizerFactory(String a,String b,boolean asScript){
		PythonInterpreter interpreter = new PythonInterpreter();
		//Import the necessary classes, scripters are expected to know about and import anything else they need
		interpreter.exec("from main import Samples");
		interpreter.exec("from main import Voice");
		interpreter.exec("from main import Synthesizer");
		if(asScript){
			interpreter.exec(a);
		}else{
			interpreter.exec("from "+a+" import "+b);
		}
		instanceClass = interpreter.get(b);
		interpreter.cleanup();
		interpreter.close();
	}
	/**
	 * Factory method
	 * 
	 * @param moduleName file location
	 * @param className class name
	 * @return a factory object
	 */
	public static PySynthesizerFactory importFactory(String moduleName,String className){
		return new PySynthesizerFactory(moduleName,className,false);
	}
	/**
	 * Factory method
	 * 
	 * @param script script to execute
	 * @param className class name
	 * @return a factory object
	 */
	public static PySynthesizerFactory scriptFactory(String script,String className){
		return new PySynthesizerFactory(script,className,true);
	}
	
	/**
	 * Create a new {@link Synthesizer} instance
	 * 
	 * @param args Python args and keyword args
	 * @param keywords Python keywords
	 * @return a new {@link Synthesizer} object
	 */
	public Synthesizer create(PyObject[] args,String[] keywords){
		return (Synthesizer)(instanceClass.__call__(args,keywords)).__tojava__(synthesizerClass);
	}
}
