package main;

import org.python.core.*;
import org.python.util.*;

public class PyVoiceFactory implements VoiceFactory{
	public static final Class<Voice> voiceClass = Voice.class;
	
	//Class to instantiate
	private PyObject instanceClass;
	
	//Constructors
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
	//Factory methods
	public static PyVoiceFactory importFactory(String moduleName,String className){
		return new PyVoiceFactory(moduleName,className,false);
	}
	public static PyVoiceFactory scriptFactory(String script,String className){
		return new PyVoiceFactory(script,className,true);
	}
	
	public Voice create(PyObject[] args,String[] keywords){
		return (Voice)(instanceClass.__call__(args,keywords)).__tojava__(voiceClass);
	}
}
