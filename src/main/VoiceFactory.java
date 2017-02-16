package main;

import org.python.core.PyObject;

public interface VoiceFactory {
	public Voice create(PyObject[] args,String[] keywords);
}
