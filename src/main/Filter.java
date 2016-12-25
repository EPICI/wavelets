package main;

import java.io.Serializable;
import javax.swing.*;

//Takes double[] as audio as input, does something, and returns a modified double[]
public interface Filter extends Serializable{
	//Implementing class is responsible for its own UI
	//Preview, something should go here
	public JPanel getPreviewPanel();
	//Editing, something should go here
	public JPanel getEditingPanel();
	
	//The filter itself
	//No changes should be made, so subsequent calls should be identical
	public double[] filter(double[] inputData);
	
	//Destructor, call before dereferencing
	public void destroy();
}
