package main;

import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.*;
import components.*;
import java.awt.*;
import java.awt.event.*;

//An individual clip
public class Clip implements Serializable {
	
	//Constants
	public static final String START_TIME = "Start time";
	public static final String END_TIME = "End time";
	
	//Parent layer
	public Layer parentLayer;
	//Start and end
	public double startTime;
	public double endTime;
	//Length
	public int length;
	//Node network used to get waveform
	public String nodesName;
	public transient Nodes nodeNetwork;
	//Inputs
	public HashMap<String,Double> inputs = new HashMap<String,Double>();
	public boolean inputsRegistered = false;
	//Cache audio
	public boolean cacheUpdated = false;
	public double[] cacheValues;
	//Cache frequency
	public boolean freqCacheUpdated = false;
	public double[] freqCacheValues;
	
	//Display
	public transient JPanel parentPanel;
	public transient JPanel infoPanel;
	public transient JPanel inputPanel;
	public transient JPanel actionPanel;
	public transient JLabel infoNodeLabel;
	public transient JComboBox<String> infoNodeSelector;
	public transient JLabel infoInputLabel;
	public transient JButton actionSave;
	public transient JButton actionPlay;
	//Safety bit
	public transient boolean nodeSelectorChanging = false;
	
	public Composition parentComposition(){
		return parentLayer.parentComposition;
	}
	
	public void initTransient(){
		//Create objects
		parentPanel = new JPanel(new BorderLayout());
		infoPanel = new JPanel(new BorderLayout());
		inputPanel = new JPanel(new GridLayout(0,2));
		actionPanel = new JPanel(new GridLayout(1,2));//Not final
		infoNodeLabel = new JLabel("Select nodes");
		infoNodeSelector = new JComboBox<String>();
		infoInputLabel = new JLabel("Inputs");
		actionSave = new JButton("Save");
		actionPlay = new JButton("Play");
		actionPlay.setEnabled(false);
		//Add to parents
		infoPanel.add(infoNodeLabel, BorderLayout.PAGE_START);
		infoPanel.add(infoNodeSelector, BorderLayout.CENTER);
		infoPanel.add(infoInputLabel, BorderLayout.PAGE_END);
		actionPanel.add(actionSave);
		actionPanel.add(actionPlay);
		parentPanel.add(infoPanel,BorderLayout.PAGE_START);
		parentPanel.add(inputPanel,BorderLayout.CENTER);
		parentPanel.add(actionPanel,BorderLayout.PAGE_END);
		//Listeners
		infoNodeSelector.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent event) {
				if(!nodeSelectorChanging){
					if(event.getStateChange()==ItemEvent.SELECTED){
						nodesName = (String) event.getItem();
						refreshNodes();
					}
				}
			}
			
		});
		actionSave.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				updateInputs();
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
		actionPlay.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				double[] soundDouble = getAudio();
				short[] soundShort = Waveform.quickShort(soundDouble);
				Wavelets.mainPlayer.playSound(soundShort);
				Wavelets.updateDisplay();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		//Final
		refreshNodeSelector();
		refreshNodes();
	}
	
	public void updateLength(){
		length = (int) Math.round((endTime-startTime)*parentComposition().samplesPerSecond);
	}
	
	public void refreshNodeSelector(){
		nodeSelectorChanging = true;
		infoNodeSelector.removeAllItems();
		for(String name:parentComposition().nodesKeysArray){
			infoNodeSelector.addItem(name);
		}
		nodeSelectorChanging = false;
	}
	
	//Refresh node network
	public void refreshNodes(){
		HashMap<String, Nodes> compNodes = parentComposition().nodes;
		if(compNodes.containsKey(nodesName)){
			nodeNetwork = parentComposition().nodes.get(nodesName);
			refreshInputs();
		}
	}
	
	//Refresh inputs
	public void refreshInputs(){
		HashMap<String, Nodes> compNodes = parentComposition().nodes;
		if(compNodes.containsKey(nodesName)){
			nodeNetwork.refreshInputs();
			ArrayList<String> inputRequests = new ArrayList<String>(nodeNetwork.inputRequests);
			inputRequests.add(0,START_TIME);
			inputRequests.add(1,END_TIME);
			inputPanel.removeAll();
			for(String inputName:inputRequests){
				inputPanel.add(new JLabel(inputName));
				JTextField inField = new JTextField(10);
				inputPanel.add(inField);
				if(inputs.containsKey(inputName)){
					Wavelets.placeDoubleTextInField(inField,10,inputs.get(inputName));
				}else if(inputName.equals(START_TIME)){
					Wavelets.placeDoubleTextInField(inField,10,startTime);
				}else if(inputName.equals(END_TIME)){
					Wavelets.placeDoubleTextInField(inField,10,endTime);
				}
			}
		}
		inputsRegistered = false;
		freqCacheUpdated = false;
		cacheUpdated = false;
		actionPlay.setEnabled(false);
	}
	
	//Update inputs
	public void updateInputs(){
		inputs = new HashMap<String,Double>();
		int numRows = inputPanel.getComponentCount()/2;
		for(int i=0;i<numRows;i++){
			int baseIndex = i*2;
			String name = ((JLabel) inputPanel.getComponent(baseIndex)).getText();
			String valueString = ((JTextField) inputPanel.getComponent(baseIndex+1)).getText();
			double value = Double.valueOf(valueString);
			//System.out.println(name+":"+value);
			switch(name){
			case(START_TIME):{
				startTime = value;
				break;
			}case(END_TIME):{
				endTime = value;
				break;
			}default:{
				inputs.put(name, value);
				break;
			}
			}
		}
		updateLength();
		inputsRegistered = true;
		freqCacheUpdated = false;
		cacheUpdated = false;
		actionPlay.setEnabled(true);
	}
	
	//Get audio data
	public double[] getAudio(){
		updateFreq();
		if(!cacheUpdated){
			nodeNetwork.user = this;
			cacheValues = new double[length];
			nodeNetwork.phase = 0.0;
			for(int i=0;i<length;i++){
				for(Node j : nodeNetwork.nodes.values()){
					j.updateFrameCache();
				}
				nodeNetwork.position = i/parentComposition().samplesPerSecond;
				double currentTime = startTime+nodeNetwork.position;
				nodeNetwork.time = currentTime;
				nodeNetwork.rate = ((double) i)/length;
				nodeNetwork.phase += freqCacheValues[i]/parentComposition().samplesPerSecond;
				cacheValues[i] = nodeNetwork.getValueOf("output");//Designated name
			}
		}
		return cacheValues;
	}
	
	//Ensure frequency data is updated
	public void updateFreq(){
		refreshNodes();
		nodeNetwork.forceUpdateAll();
		if(!freqCacheUpdated){
			nodeNetwork.user = this;
			freqCacheValues = new double[length];
			for(int i=0;i<length;i++){
				for(Node j : nodeNetwork.nodes.values()){
					j.updateFrameCache();
				}
				nodeNetwork.position = i/parentComposition().samplesPerSecond;
				double currentTime = startTime+nodeNetwork.position;
				nodeNetwork.time = currentTime;
				nodeNetwork.rate = ((double) i)/length;
				freqCacheValues[i] = nodeNetwork.getValueOf("frequency");//Designated name
			}
			freqCacheUpdated = true;
		}
	}
	
	//Get frequency data
	public double[] getFreq(){
		updateFreq();
		return freqCacheValues;
	}
	public double getInput(String inputID){
		return inputs.get(inputID);
	}
		
	public ArrayList<String> getInputs(){
		return new ArrayList<String>(inputs.keySet());
	}

	public static void main(String[] args) {
		//Leave empty
	}

}
