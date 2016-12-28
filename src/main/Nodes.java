package main;

import java.awt.BorderLayout;
import java.io.Serializable;
import java.util.HashMap;
import javax.swing.*;
import java.awt.event.*;
import components.*;
import java.util.ArrayList;
import java.util.Arrays;

//A bunch of connected nodes
public class Nodes implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//Source text
	public String source = "";
	//All nodes in the network
	protected HashMap<String,Node> nodes = new HashMap<String,Node>();
	//Inputs to request
	public ArrayList<String> inputRequests = new ArrayList<String>();
	//Parent composition
	public Composition parentComposition;
	//Current user
	public transient Clip user;
	//Time is global time
	public transient double time;
	//Position is relative to the current clip start
	public transient double position;
	//Position within the clip between 0 and 1
	public transient double rate;
	//Auto adjusts to frequency, continuous
	public transient double phase;
	
	//Built in inputs
	public static String[] codedInputsArray = {"time","position","rate","phase","start","end"};
	public static ArrayList<String> codedInputs = new ArrayList<String>(Arrays.asList(codedInputsArray));
	
	//Display
	public transient JPanel scriptPanel;//Contains script stuff
	public transient JPanel viewPanel;//TODO create a class to visualize nodes
	public transient JPanel testPanel;//Preview with a temporary clip
	public transient WGraphViewerPanel graphPanel;//View final waveform
	public transient JLabel scriptLabel;//Script
	public transient JTextArea scriptArea;//Type script here
	public transient JButton scriptSave;//Save and compile
	//For testing
	public transient Clip testClip;
	
	//Standard constructor
	public Nodes(){
		initPanels();
	}
	
	public void initPanels(){
		//Create objects
		scriptPanel = new JPanel(new BorderLayout());
		scriptLabel = new JLabel("Source");
		scriptArea = new JTextArea();
		scriptSave = new JButton("Compile");
		viewPanel = new WNodesViewerPanel(this);//TODO replace with more user friendly UI
		testPanel = new JPanel();
		graphPanel = new WGraphViewerPanel();
		//Add to parents
		scriptPanel.add(scriptLabel,BorderLayout.PAGE_START);
		scriptPanel.add(scriptArea,BorderLayout.CENTER);
		scriptPanel.add(scriptSave,BorderLayout.PAGE_END);
		//Add listeners
		scriptSave.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				source = scriptArea.getText();
				recompile();
				Wavelets.updateNodeSelection();
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	//Check if a certain group of nodes need updating
	public boolean groupRequiresUpdate(ArrayList<String> toIter){
		boolean result = false;
		for(String current:toIter){
			boolean toCompare;
			if(codedInputs.contains(current)){
				switch(current){
				case "time":{
					toCompare = true;
					break;
				}case "position":{
					toCompare = true;
					break;
				}case "rate":{
					toCompare = true;
					break;
				}case "phase":{
					toCompare = true;
					break;
				}case "start":{
					toCompare = false;
					break;
				}case "end":{
					toCompare = false;
					break;
				}default:{//Should not ever be reached
					toCompare = true;
					break;
				}
				}
			}else{
				//System.out.println(current);
				Node currentNode = nodes.get(current);
				currentNode.initTransient();
				toCompare = currentNode.updateOnNewFrame;
			}
			result=result||toCompare;
		}
		return result;
	}
	
	//Get the value of a certain node or a global value
	public double getValueOf(String name){
		switch(name){
		case("time"):{
			return time;
		}case("position"):{
			return position;
		}case("rate"):{
			return rate;
		}case("phase"):{
			return phase;
		}case("start"):{
			return user.startTime;
		}case("end"):{
			return user.endTime;
		}default:{
			return nodes.get(name).getValue();
		}
		}
	}
	
	//Add a node
	public void addNode(Node givenNode){
		givenNode.parentNodes = this;
		if(givenNode.type=="input"){
			inputRequests.add(givenNode.args.get(0));
		}
		nodes.put(givenNode.name, givenNode);
	}
	
	//Remove a node
	public void removeNode(String nodeName){
		nodes.remove(nodeName);
	}
	
	//Read an input string to create the node network
	public void importNodes(String rawdata){
		try{
			nodes = new HashMap<String,Node>();
			inputRequests = new ArrayList<String>();
			for(String nodedata:rawdata.split("\n")){
				Node toAdd = new Node(nodedata);
				toAdd.parentNodes = this;
				toAdd.initTransient();
				addNode(toAdd);
			}
		}catch(Exception e){
			System.out.println("Node compiler error");//TODO redirect this to second window
			e.printStackTrace();
		}
		//refreshInputs();
	}
	
	//Recompile using own source
	public void recompile(){
		importNodes(source);
	}
	
	public void refreshInputs(){
		inputRequests = new ArrayList<String>();
		for(Node currentNode:nodes.values()){
			if(currentNode.type.equals("input")){
				inputRequests.add(currentNode.args.get(0));
				currentNode.forceUpdateFrameCache();
			}
		}
	}
	
	public void forceUpdateAll(){
		for(Node currentNode:nodes.values()){
			currentNode.forceUpdateFrameCache();
		}
	}
	
	public double getInput(String inputID){
		return parentComposition.currentClip.getInput(inputID);
	}
	
	public HashMap<String,Node> getNodes(){
		return nodes;
	}
	
	public void refreshTestClip(){
		testClip = new Clip();
		testClip.isLayerClip = false;
		testClip.parentLayer = parentComposition.nodeLayer;
		testClip.nodesName = parentComposition.nodesSelection;
		testClip.initTransient();
		testClip.infoNodeSelector.setSelectedItem(parentComposition.nodesSelection);
		testClip.setGraphTarget(graphPanel);
		testPanel = testClip.parentPanel;
	}

	public int hashCode(){
		return nodes.hashCode();
	}
}
