package main;

import java.io.Serializable;

//All objects with transient fields that need to be initialized
public interface TransientContainer<T> extends Serializable {
	//Initialize anything that needs to be initialized, parent is given
	public void initTransient(T parent);
}
