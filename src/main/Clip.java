package main;

import java.io.Serializable;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import components.*;
import org.json.*;

//An individual clip
public class Clip implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//Constants
	public static final String START_TIME = "Start time";
	public static final String END_TIME = "End time";
	
	//Is it part of a layer
	public boolean isLayerClip = true;
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
	public transient boolean inputsRegistered = true;
	//Cache audio
	public transient boolean cacheUpdated = false;
	public double[] cacheValues;
	//Cache frequency
	public transient boolean freqCacheUpdated = false;
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
	public transient WGraphViewerPanel graphTarget;
	public transient boolean graphTargetSet = false;
	//Safety bit
	public transient boolean nodeSelectorChanging = false;
	
	public static class ClipUpdateAudioTask implements WorkThread.Task{
		
		public byte status;
		public Clip targetClip;
		
		public ClipUpdateAudioTask(Clip target){
			status = WorkThread.WAITING;
			targetClip = target;
		}

		@Override
		public synchronized boolean execute() {
			status = WorkThread.WORKING;
			//While it can be split up, it's better to do it in one go
			targetClip.getAudio();
			status = WorkThread.FINISHED;
			return false;
		}

		@Override
		public synchronized byte getStatus() {
			return status;
		}

		@Override
		public void cancel() {
			
		}
		
	}
	
	public ClipUpdateAudioTask getUpdateAudioTask(){
		return new ClipUpdateAudioTask(this);
	}
	
	public static class ClipQuickPlayTask implements WorkThread.Task{
		
		public byte status;
		public byte phase;
		public Clip targetClip;
		private WorkThread.Task updateTask;
		
		public ClipQuickPlayTask(Clip target){
			status = WorkThread.WAITING;
			phase = 0;
			targetClip = target;
			if(targetClip.cacheUpdated){
				phase = 2;
			}
		}

		@Override
		public synchronized boolean execute() {
			status = WorkThread.WORKING;
			switch(phase){
			case 0:{
				updateTask = targetClip.getUpdateAudioTask();
				Wavelets.assignTask(updateTask);
				phase = 1;
				break;
			}case 1:{
				if(updateTask.getStatus()==WorkThread.FINISHED){
					phase = 2;
				}
				break;
			}case 2:{
				short[] audioData = WaveUtils.quickShort(targetClip.getAudio());
				Wavelets.mainPlayer.playSound(audioData);
				phase = -1;
				status = WorkThread.FINISHED;
				break;
			}default:{
				status = WorkThread.FINISHED;
				break;
			}
			}
			if(phase!=-1){
				status = WorkThread.WAITING;
			}
			return false;
		}

		@Override
		public synchronized byte getStatus() {
			return status;
		}

		@Override
		public void cancel() {
			
		}
		
	}
	
	public void quickPlay(){
		Wavelets.assignTask(new ClipQuickPlayTask(this));
	}
	
	public Composition parentComposition(){
		return parentLayer.parentComposition;
	}
	
	public void initTransient(){
		cacheUpdated = cacheValues != null;
		freqCacheUpdated = freqCacheValues != null;
		inputsRegistered = true;
		//Create objects
		parentPanel = new JPanel(new BorderLayout());
		infoPanel = new JPanel(new BorderLayout());
		inputPanel = new JPanel(new GridBagLayout());
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
		actionSave.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				updateInputs();
			}
		});
		actionPlay.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				if(inputsRegistered){
					quickPlay();
				}
			}
		});
		//Final
		refreshNodeSelector();
		refreshNodes();
	}
	
	public void setGraphTarget(WGraphViewerPanel target){
		graphTarget = target;
		graphTargetSet = true;
	}
	
	public void removeGraphTarget(){
		graphTarget = null;
		graphTargetSet = false;
	}
	
	public void updateLength(){
		if(startTime>endTime){
			double swap = startTime;
			startTime = endTime;
			endTime = swap;
		}
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
		ArrayList<String> inputRequests;
		inputPanel.removeAll();
		if(compNodes.containsKey(nodesName)){
			nodeNetwork.refreshInputs();
			inputRequests = new ArrayList<String>(nodeNetwork.inputRequests);
		}else{
			inputRequests = new ArrayList<String>();
		}
		inputRequests.add(0,START_TIME);
		inputRequests.add(1,END_TIME);
		int total = inputRequests.size();
		GridBagConstraints constraint = new GridBagConstraints();
		for(int i=0;i<total;i++){
			String inputName = inputRequests.get(i);
			constraint.gridx=0;
			constraint.gridy=i;
			inputPanel.add(new JLabel(inputName),constraint);
			JTextField inField = new JTextField(10);
			constraint.gridx=1;
			if(inputs.containsKey(inputName)){
				WaveUtils.placeDoubleTextInField(inField,10,inputs.get(inputName));
			}else if(inputName.equals(START_TIME)){
				WaveUtils.placeDoubleTextInField(inField,10,startTime);
			}else if(inputName.equals(END_TIME)){
				WaveUtils.placeDoubleTextInField(inField,10,endTime);
			}
			inputPanel.add(inField,constraint);
		}
		boolean invalidate = false;
		for(String current:nodeNetwork.inputRequests){
			if(!inputs.containsKey(current)){
				invalidate = true;
			}
		}
		if(invalidate){
			inputsRegistered = false;
		}
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
	
	//Ensure audio data is updated
	public void updateAudio(){
		double sampleRate = parentComposition().samplesPerSecond;
		nodeNetwork.user = this;
		cacheValues = new double[length];
		nodeNetwork.phase = 0.0;
		for(int i=0;i<length;i++){
			for(Node j : nodeNetwork.nodes.values()){
				j.updateFrameCache();
			}
			nodeNetwork.position = i/sampleRate;
			double currentTime = startTime+nodeNetwork.position;
			nodeNetwork.time = currentTime;
			nodeNetwork.rate = ((double) i)/length;
			nodeNetwork.phase += freqCacheValues[i]/parentComposition().samplesPerSecond;
			cacheValues[i] = nodeNetwork.getValueOf("output");//Designated name
		}
	}
	
	//Get audio data
	public double[] getAudio(){
		updateFreq();
		if(!cacheUpdated){
			updateAudio();
		}
		return cacheValues;
	}
	
	//Ensure frequency data is updated
	public void updateFreq(){
		nodeNetwork.forceUpdateAll();
		if(!freqCacheUpdated){
			double sampleRate = parentComposition().samplesPerSecond;
			nodeNetwork.user = this;
			freqCacheValues = new double[length];
			for(int i=0;i<length;i++){
				for(Node j : nodeNetwork.nodes.values()){
					j.updateFrameCache();
				}
				nodeNetwork.position = i/sampleRate;
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
	
	//Get boundaries
	public double[] getFreqBounds(){
		updateFreq();
		DoubleSummaryStatistics stats = Arrays.stream(freqCacheValues).summaryStatistics();
		return new double[]{stats.getMin(),stats.getMax()};
	}
	
	public double getInput(String inputID){
		return inputs.get(inputID);
	}
		
	public ArrayList<String> getInputs(){
		return new ArrayList<String>(inputs.keySet());
	}
	
	public void copyFrom(Clip source){
		inputs = new HashMap<String,Double>(source.inputs);
		startTime = source.startTime;
		endTime = source.endTime;
		nodesName = source.nodesName;
		infoNodeSelector.setSelectedItem(source.nodesName);
		inputsRegistered = true;
		refreshNodes();
	}
	
	public void clearCache(){
		cacheUpdated = false;
		freqCacheUpdated = false;
		cacheValues = null;
		freqCacheValues = null;
	}
	
	//Export as JSON
	public JSONObject exportJson(){
		JSONObject result = new JSONObject();
		result.put("nodes", nodesName);
		result.put("start", startTime);
		result.put("end", endTime);
		result.put("inputs", inputs);
		return result;
	}
	
	//Cleanup
	public void destroy(){
		parentLayer = null;
		nodeNetwork = null;
		inputs = null;
		cacheValues = null;
		freqCacheValues = null;
	}

	public int hashCode(){
		return WaveUtils.quickHash(4391, nodeNetwork, inputs, startTime, endTime);
	}

}
