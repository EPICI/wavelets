package main;

import org.python.core.*;
import org.python.util.*;

public class PySynthesizerFactory {
	public static final Class<Synthesizer> synthesizerClass = Synthesizer.class;
	
	//Class to instantiate
	private PyObject instanceClass;
	
	//Constructors
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
	//Factory methods
	public static PySynthesizerFactory importFactory(String moduleName,String className){
		return new PySynthesizerFactory(moduleName,className,false);
	}
	public static PySynthesizerFactory scriptFactory(String script,String className){
		return new PySynthesizerFactory(script,className,true);
	}
	
	public Synthesizer create(PyObject[] args,String[] keywords){
		return (Synthesizer)(instanceClass.__call__(args,keywords)).__tojava__(synthesizerClass);
	}
}
