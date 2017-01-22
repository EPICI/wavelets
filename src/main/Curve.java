package main;

import java.io.Serializable;

//Curve type objects
public interface Curve extends Serializable {
	//Get the value at a specific position
	public double valueAtPosition(double position);
}
