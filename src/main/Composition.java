package main;

import java.io.Serializable;
import java.util.*;
import javax.swing.*;

//A composition
public class Composition implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//Global
	public HashMap<String,Curve> curves = new HashMap<String,Curve>();
	public HashMap<String,Nodes> nodes = new HashMap<String,Nodes>();
	public HashMap<String,Layer> layers = new HashMap<String,Layer>();
	public ArrayList<JPanel> layerPanels = new ArrayList<JPanel>();
	//Needed for current session
	public transient String[] curvesKeysArray;
	public transient String curveSelection;
	public transient String[] nodesKeysArray;
	public transient String nodesSelection;
	public transient String[] layersKeysArray;
	public transient String layerSelection;
	public transient Layer nodeLayer;
	//Audio format
	public int samplesPerSecond = 44100;
	
	//Current clip
	public Clip currentClip;
	
	//Initialize all transient
	public void initTransient(){
		nodeLayer = new Layer();
		nodeLayer.parentComposition = this;
		for(Curve currentCurve:curves.values()){
			currentCurve.initPanels();
		}
		for(Nodes currentNode:nodes.values()){
			currentNode.initPanels();
		}
		for(Layer currentLayer:layers.values()){
			currentLayer.initTransient();
		}
		curveSelection = "";
		nodesSelection = "";
		layerSelection = "";
		updateCurves();
		updateNodes();
		updateLayers();
	}
	
	//Update nodes
	public void updateNodes(){
		nodesKeysArray = nodes.keySet().toArray(new String[0]);
	}
	
	//Add a new nodes
	public void addNodes(String name){
		Nodes toAdd = new Nodes();
		nodes.put(name, toAdd);
		toAdd.parentComposition = this;
		updateNodes();
		nodesSelection = name;
		Wavelets.updateNodeSelection();
	}
	
	//Add a duplicate of a nodes
	public void dupliNodes(String name,String reference){
		Nodes toAdd = new Nodes();
		nodes.put(name, toAdd);
		toAdd.parentComposition = this;
		Nodes original = nodes.get(reference);
		toAdd.source = original.source;
		toAdd.recompile();
		updateNodes();
		nodesSelection = name;
		Wavelets.updateNodeSelection();
	}
	
	//Remove a nodes
	public void removeNodes(String name){
		nodes.remove(name);
		updateNodes();
		Wavelets.updateNodeSelection();
	}
	
	//Rename a nodes
	public void renameNodes(String oldName, String newName){
		Nodes currentNode = nodes.get(oldName);
		nodes.put(newName, currentNode);
		nodes.remove(oldName);
		updateNodes();
		nodesSelection = newName;
		Wavelets.updateNodeSelection();
	}
	
	//Update curves
	public void updateCurves(){
		curvesKeysArray = curves.keySet().toArray(new String[0]);
	}
	
	//Add a new curve
	public void addCurve(String name){
		Curve toAdd = new Curve();
		curves.put(name, toAdd);
		updateCurves();
		curveSelection = name;
		Wavelets.updateCurveSelection();
	}
	
	//Add a duplicate of a curve
	public void dupliCurve(String name,String reference){
		Curve toAdd = new Curve();
		curves.put(name, toAdd);
		Curve original = curves.get(reference);
		ArrayList<Double> origLoc = original.getLocations();
		ArrayList<Double> origVal = original.getValues();
		for(int i=0;i<original.getSize();i++){
			toAdd.addPoint(origLoc.get(i), origVal.get(i));
		}
		toAdd.selected = original.selected;
		toAdd.updateSelection();
		toAdd.setMode(original.getMode());
		updateCurves();
		curveSelection = name;
		Wavelets.updateCurveSelection();
	}
	
	//Remove a curve
	public void removeCurve(String name){
		curves.remove(name);
		updateCurves();
		Wavelets.updateCurveSelection();
	}
	
	//Rename a curve
	public void renameCurve(String oldName, String newName){
		Curve currentCurve = curves.get(oldName);
		curves.put(newName, currentCurve);
		curves.remove(oldName);
		updateCurves();
		curveSelection = newName;
		Wavelets.updateCurveSelection();
	}
	
	//Update layers
	public void updateLayers(){
		layersKeysArray = layers.keySet().toArray(new String[0]);
	}
	
	//Add a new layer
	public JPanel addLayer(){
		Layer toAdd = new Layer();
		int count = 1;
		String name = "Layer 1";
		while(layers.containsKey(name)){
			count++;
			name = "Layer "+Integer.toString(count);
		}
		toAdd.setName(name);
		toAdd.parentComposition = this;
		layers.put(name,toAdd);
		layerPanels.add(toAdd.parentPanel);
		return toAdd.parentPanel;
	}
	
	//Remove a layer
	public void removeLayer(String key){
		Layer currentLayer = layers.get(key);
		Wavelets.composerLeftInPanel.remove(currentLayer.parentPanel);
		layerPanels.remove(layers.get(key));
		layers.remove(key);
		Wavelets.updateDisplay();
	}
	
	public void removeLayer(Layer layer){
		String toRemove = "";
		for(String key : layers.keySet()){
			if(layers.get(key)==layer){
				toRemove = key;
			}
		}
		if(toRemove != ""){
			removeLayer(toRemove);
		}
	}
	
	public void renameLayer(String oldName, String newName){
		Layer currentLayer = layers.get(oldName);
		currentLayer.setName(newName);
		layers.put(newName, currentLayer);
		layers.remove(oldName);
	}

	public static void main(String[] args) {
		//Leave empty
	}

}
