package main;

import org.python.core.*;
import org.python.util.*;

/**
 * Implementation of VoiceFactory meant for Python
 * 
 * @author EPICI
 * @version 1.0
 */
public class PyVoiceFactory implements VoiceFactory{
	public static final Class<Voice> voiceClass = Voice.class;
	
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
	public PyVoiceFactory(String a,String b,boolean asScript){
		PythonInterpreter interpreter = new PythonInterpreter();
		//Import the necessary classes, scripters are expected to know about and import anything else they need
		interpreter.exec("from main import Samples");
		interpreter.exec("from main import Voice");
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
	 * @return a new voice factory instance
	 */
	public static PyVoiceFactory importFactory(String moduleName,String className){
		return new PyVoiceFactory(moduleName,className,false);
	}
	/**
	 * Factory method
	 * 
	 * @param script script to execute
	 * @param className class name
	 * @return a new voice factory instance
	 */
	public static PyVoiceFactory scriptFactory(String script,String className){
		return new PyVoiceFactory(script,className,true);
	}
	
	/**
	 * Create a new {@link Voice} with the specified args and keyword args
	 * 
	 * @param args Python args and keyword args
	 * @param keywords Python keywords
	 * @return a new {@link Voice} instance
	 */
	public Voice create(PyObject[] args,String[] keywords){
		return (Voice)(instanceClass.__call__(args,keywords)).__tojava__(voiceClass);
	}
}
