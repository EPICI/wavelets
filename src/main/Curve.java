package main;

import java.io.Serializable;

//Curve type objects
public interface Curve extends Serializable {
	public double valueAtPosition(double position);
}
