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
	//Tell it the UI needs updating
	public void updateUI();
	
	//The filter itself
	//No changes should be made, so subsequent calls should be identical
	public double[] filter(double[] inputData,double offset);
	
	//Set parent composition
	public void setParentComposition(Composition parent);
	
	//Initialize all transient variables
	public void initTransient();
	
	//Destructor, call before dereferencing
	public void destroy();
}
