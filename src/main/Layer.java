package main;

import java.util.*;
import javax.swing.*;
import org.json.*;
import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;

//A single layer, containing multiple clips
public class Layer implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//Parent composition
	public Composition parentComposition;
	//Self
	public transient Layer self;
	//All clips in the layer
	public ArrayList<Clip> clips = new ArrayList<Clip>();
	public transient int clipCount = 0;
	public transient int selectedClip = -1;
	//Layer name
	public String name = "";
	//All filters, in order they are applied
	public ArrayList<Filter> filters = new ArrayList<Filter>();
	//Cache
	public double[] cacheValues;
	public int cacheHash = 0;
	
	//Display - left area
	public transient JPanel parentPanel;
	public transient JScrollPane parentScroll;
	public transient GroupLayout layout;
	public transient JLabel nameLabel;
	public transient JTextField nameTextArea;
	public transient JButton buttonRename;
	public transient JButton buttonEdit;
	public transient JButton buttonDelete;
	public transient boolean editMode;
	//Display - properties panel
	public transient JPanel editPanel;
	public transient JPanel propertyPanel;
	public transient JPanel layerPropertyPanel;
	public transient JPanel clipActionsPanel;
	public transient JButton clipDelete;
	public transient JButton clipAdd;
	public transient JButton clipDupli;
	//Display - filters panel
	public transient JPanel filterPanel;
	public transient JScrollPane filterScroll;
	public transient JButton filterAdd;
	public transient JComboBox<String> filterTypeSelector;
	
	//Filter generators
	public static HashMap<String,FilterGenerator> filterGenerators;
	public static String[] filterGeneratorsKeysArray = new String[0];
	
	//Time manipulators
	public static HashMap<String,TimeManipulator> timeManipulators;
	public static String[] timeManipulatorsKeysArray = new String[0];
	
	public static class LayerUpdateAudioTask implements WorkThread.Task{
		
		public byte status;
		public byte phase;
		public Layer targetLayer;
		private double[] timeBounds;
		private double[] cacheValues;
		private ArrayList<Clip> usedClips;
		private ArrayList<WorkThread.Task> clipTasks; 
		private int newHash;
		
		public LayerUpdateAudioTask(Layer target){
			status = WorkThread.WAITING;
			phase = 0;
			targetLayer = target;
		}

		@Override
		public synchronized void execute() {
			status = WorkThread.WORKING;
			//IMPORTANT: THIS HAS TO MATCH THE REGULAR METHODS
			switch(phase){
			case 0:{
				//Check if it needs updating
				newHash = targetLayer.hashCode();
				if(newHash!=targetLayer.cacheHash){
					phase = 1;//Signal needs updating
				}else{
					phase = -1;//Signal finished
				}
				break;
			}case 1:{
				//Basic setup
				timeBounds = targetLayer.getTimeBounds();
				int total = (int) ((timeBounds[1]-timeBounds[0])*targetLayer.parentComposition.samplesPerSecond);
				cacheValues = new double[total];
				for(int i=0;i<total;i++){
					cacheValues[i]=0d;
				}
				phase = 2;
				break;
			}case 2:{
				//Queue up all tasks
				usedClips = new ArrayList<>();
				clipTasks = new ArrayList<>();
				for(Clip current:targetLayer.clips){
					//System.out.println("Next clip");
					if(current.inputsRegistered){
						usedClips.add(current);
						clipTasks.add(current.getUpdateAudioTask());
					}
				}
				//System.out.println("Assigning "+Integer.toString(clipTasks.size())+" clip tasks");
				Wavelets.assignTasks(clipTasks);
				//System.out.println("Assigned clip tasks");
				phase = 3;
				break;
			}case 3:{
				//Wait for them to finish
				boolean allDone = true;
				for(WorkThread.Task task:clipTasks){
					allDone = task.getStatus()==WorkThread.FINISHED && allDone;
				}
				if(allDone){
					//System.out.println("Clip tasks finished");
					phase = 4;
				}
				break;
			}case 4:{
				//Add them all
				for(Clip current:usedClips){
					//System.out.println("Next clip");
					if(current.inputsRegistered){
						double[] clipData = current.getAudio();
						int offset = (int) ((current.startTime-timeBounds[0])*targetLayer.parentComposition.samplesPerSecond);
						int target = clipData.length+offset-1;
						int cap = Math.min(cacheValues.length, target);
						for(int i=offset;i<cap;i++){
							//Double safety
							cacheValues[i]+=clipData[i-offset];
						}
						//System.out.println("Added "+(cap-offset)+" values");
					}
				}
				phase = 5;
				break;
			}case 5:{
				for(Filter current:targetLayer.filters){
					cacheValues = current.filter(cacheValues, timeBounds[0]);
				}
				targetLayer.cacheHash = newHash;
				targetLayer.cacheValues = cacheValues;
				phase = -1;//Signal done
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
		}

		@Override
		public synchronized byte getStatus() {
			return status;
		}

		@Override
		public void cancel() {
			
		}
		
	}
	
	public LayerUpdateAudioTask getUpdateAudioTask(){
		return new LayerUpdateAudioTask(self);
	}
	
	public static class LayerQuickPlayTask implements WorkThread.Task{
		
		public byte status;
		public byte phase;
		public Layer targetLayer;
		private WorkThread.Task updateTask;
		
		public LayerQuickPlayTask(Layer target){
			status = WorkThread.WAITING;
			phase = 0;
			targetLayer = target;
			if(targetLayer.cacheHash==targetLayer.hashCode()){
				phase = 2;
			}
		}

		@Override
		public synchronized void execute() {
			status = WorkThread.WORKING;
			switch(phase){
			case 0:{
				updateTask = targetLayer.getUpdateAudioTask();
				Wavelets.assignTask(updateTask);
				phase = 1;
				break;
			}case 1:{
				if(updateTask.getStatus()==WorkThread.FINISHED){
					phase = 2;
				}
				break;
			}case 2:{
				short[] audioData = WaveUtils.quickShort(targetLayer.getAudio());
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
		Wavelets.assignTask(new LayerQuickPlayTask(this));
	}
	
	public static void init(Composition parent){
		filterGenerators = new HashMap<String,FilterGenerator>();
		filterGenerators.put("Curve envelope", new FilterGenerator(){
			public Filter createNew(){
				FilterCurveEnvelope result = new FilterCurveEnvelope();
				result.setParentComposition(parent);
				result.initTransient();
				return result;
			}
		});
		filterGeneratorsKeysArray = filterGenerators.keySet().toArray(new String[0]);
		timeManipulators = new HashMap<String,TimeManipulator>();
		timeManipulators.put("Shift start by", new TimeManipulator(){
			public void applyTo(Layer inputLayer,double setting){
				for(Clip current:inputLayer.clips){
					current.startTime += setting;
					current.updateLength();
				}
			}
			public double[] transform(double[] inputData,double setting){
				return new double[]{inputData[0]+setting,inputData[1]};
			}
		});
		timeManipulators.put("Shift end by", new TimeManipulator(){
			public void applyTo(Layer inputLayer,double setting){
				for(Clip current:inputLayer.clips){
					current.endTime += setting;
					current.updateLength();
				}
			}
			public double[] transform(double[] inputData,double setting){
				return new double[]{inputData[0],inputData[1]+setting};
			}
		});
		timeManipulators.put("Fix start, multiply length by", new TimeManipulator(){
			public void applyTo(Layer inputLayer,double setting){
				for(Clip current:inputLayer.clips){
					current.endTime = current.startTime + (current.endTime-current.startTime)*setting;
					current.updateLength();
				}
			}
			public double[] transform(double[] inputData,double setting){
				return new double[]{inputData[0],inputData[0]+(inputData[1]-inputData[0])*setting};
			}
		});
		timeManipulatorsKeysArray = timeManipulators.keySet().toArray(new String[0]);
	}
	
	public interface FilterGenerator{
		public Filter createNew();
	}
	
	public interface TimeManipulator{
		public void applyTo(Layer inputLayer,double setting);
		public double[] transform(double[] inputData,double setting);
	}
	
	public Layer(){
		initTransient();
	}
	
	//Create all transient variables
	public void initTransient(){
		for(Clip currentClip:clips){
			currentClip.initTransient();
		}
		self = this;
		GridBagConstraints constraint = new GridBagConstraints();
		editPanel = new JPanel(new GridBagLayout());
		propertyPanel = new JPanel();
		constraint.gridx=0;
		constraint.gridy=0;
		editPanel.add(propertyPanel,constraint);
		layerPropertyPanel = new JPanel();
		clipActionsPanel = new JPanel(new GridBagLayout());
		clipDelete = new JButton("Delete");
		clipDelete.setEnabled(false);
		clipActionsPanel.add(clipDelete,constraint);
		clipAdd = new JButton("Create");
		constraint.gridx=1;
		clipActionsPanel.add(clipAdd,constraint);
		clipDupli = new JButton("Duplicate");
		clipDupli.setEnabled(false);
		constraint.gridx=2;
		clipActionsPanel.add(clipDupli,constraint);
		constraint.gridx=0;
		constraint.gridy=1;
		editPanel.add(clipActionsPanel,constraint);
		parentPanel = new JPanel();
		layout = new GroupLayout(parentPanel);
		nameLabel = new JLabel(name);
		nameTextArea = new JTextField(30);
		buttonRename = new JButton("Rename");
		buttonEdit = new JButton("Edit");
		buttonDelete = new JButton("Delete");
		editMode = false;
		parentPanel.setLayout(layout);
		parentScroll = new JScrollPane(parentPanel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
			.addComponent(nameLabel)
			.addComponent(buttonRename)
			.addComponent(buttonEdit)
			.addComponent(buttonDelete)
		);
		layout.setVerticalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.CENTER)
			.addComponent(nameLabel)
			.addComponent(buttonRename)
			.addComponent(buttonEdit)
			.addComponent(buttonDelete)
		);
		nameTextArea.setText(name);
		filterPanel = new JPanel(new GridBagLayout());
		filterScroll = new JScrollPane(filterPanel);
		filterAdd = new JButton("Add new");
		filterTypeSelector = new JComboBox<String>(filterGeneratorsKeysArray);
		refreshFilters();
		buttonRename.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				edit();
			}
		});
		buttonEdit.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				parentComposition.layerSelection = name;
				JPanel addTo = Wavelets.composerRightInPanel;
				addTo.removeAll();
				addTo.add(editPanel);
				addTo = Wavelets.composerBottomPanel;
				addTo.removeAll();
				addTo.add(filterScroll,BorderLayout.CENTER);
				Wavelets.updateDisplay();
			}
		});
		buttonDelete.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				parentComposition.removeLayer(self);
			}
		});
		clipDelete.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				removeClip(selectedClip);
			}
		});
		clipAdd.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				addClip();
			}
		});
		clipDupli.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				dupliClip(selectedClip);
			}
		});
		filterAdd.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				Filter toAdd = filterGenerators.get(filterTypeSelector.getSelectedItem()).createNew();
				filters.add(toAdd);
				refreshFilters();
			}
		});
		updateClipSelection();
	}
	
	public void edit(){
		if(editMode){
			String newName = nameTextArea.getText();
			if(parentComposition.layers.keySet().contains(newName)){
				if(name.equals(newName)){
					setName(newName);
					buttonRename.setText("Rename");
					//buttonRename.setToolTipText("Brings up a text field for changing the name.");
					layout.replace(nameTextArea, nameLabel);
				}else{
					editMode=!editMode;//Must cancel
				}
			}else{
				parentComposition.renameLayer(name,newName);
				setName(newName);
				buttonRename.setText("Rename");
				//buttonRename.setToolTipText("Brings up a text field for changing the name.");
				layout.replace(nameTextArea, nameLabel);
			}
		}else{
			setName(nameLabel.getText());
			buttonRename.setText("Confirm");
			//buttonRename.setToolTipText("Renames the layer. Will not work if the name is already taken.");
			layout.replace(nameLabel, nameTextArea);
		}
		editMode=!editMode;
	}
	
	public void setName(String toSet){
		name = toSet;
		nameLabel.setText(name);
		nameTextArea.setText(name);
	}
	
	public void updateClipSelection(){
		clipCount = clips.size();//Safety
		propertyPanel.removeAll();
		if(clipCount>0){
			selectedClip = Math.floorMod(selectedClip, clipCount);
			Clip selected = clips.get(selectedClip);
			propertyPanel.add(selected.parentPanel);
			if(Wavelets.composerTopPanelComponents.size()>0){
				((JLabel) Wavelets.composerTopPanelComponents.get(3).get(0)).setText(Integer.toString(selectedClip+1));
			}
			clipDelete.setEnabled(true);
			clipDupli.setEnabled(true);
		}else{
			selectedClip = -1;
			propertyPanel.add(layerPropertyPanel);
			if(Wavelets.composerTopPanelComponents.size()>0){
				((JLabel) Wavelets.composerTopPanelComponents.get(3).get(0)).setText("Layer is empty");
			}
			clipDelete.setEnabled(false);
			clipDupli.setEnabled(false);
		}
		Wavelets.updateDisplay();
	}
	
	public void addClip(){
		Clip toAdd = new Clip();
		toAdd.parentLayer = self;
		toAdd.nodesName = parentComposition.nodesSelection;
		toAdd.initTransient();
		toAdd.infoNodeSelector.setSelectedItem(parentComposition.nodesSelection);
		selectedClip = clipCount;
		clips.add(toAdd);
		updateClipSelection();
	}
	
	public void addClip(Clip toAdd){
		toAdd.parentLayer = self;
		selectedClip = clipCount;
		clips.add(toAdd);
		updateClipSelection();
	}
	
	public void dupliClip(int index){
		addClip();
		if(index<clipCount&&selectedClip!=index){//Safety
			Clip selected = clips.get(selectedClip);
			Clip source = clips.get(index);
			selected.copyFrom(source);
		}
	}
	
	public void dupliClip(Clip source){
		addClip();
		Clip selected = clips.get(selectedClip);
		selected.copyFrom(source);
	}
	
	public void removeClip(int index){
		if(index>=0&&index<clipCount){
			clips.remove(index);
		}
		updateClipSelection();
	}
	
	public void refreshFilters(){
		filterPanel.removeAll();
		GridBagConstraints constraint = new GridBagConstraints();
		constraint.weightx=1;
		constraint.weighty=1;
		constraint.gridx=0;
		constraint.gridy=0;
		constraint.gridwidth=2;
		constraint.gridheight=1;
		filterPanel.add(filterAdd,constraint);
		constraint.gridx=2;
		filterPanel.add(filterTypeSelector,constraint);
		int numFilters = filters.size();
		for(int i=0;i<numFilters;i++){
			Filter current = filters.get(i);
			JButton swapPrev = new JButton("\u25b2");//Up arrow
			final int index=i;
			swapPrev.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e){
					Collections.swap(filters, index-1, index);
					refreshFilters();
				}
			});
			JButton swapNext = new JButton("\u25bc");//Down arrow
			swapNext.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e){
					Collections.swap(filters, index, index+1);
					refreshFilters();
				}
			});
			JButton delete = new JButton("\u00d7");//Multiplication sign
			delete.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e){
					filters.remove(index);
					refreshFilters();
				}
			});
			constraint.gridx=0;
			constraint.gridy=3*i+1;
			constraint.gridwidth=1;
			filterPanel.add(swapPrev, constraint);
			constraint.gridy=3*i+2;
			filterPanel.add(delete, constraint);
			constraint.gridy=3*i+3;
			filterPanel.add(swapNext, constraint);
			constraint.gridy=3*i+1;
			constraint.gridx=1;
			constraint.gridwidth=2;
			constraint.gridheight=3;
			constraint.fill = GridBagConstraints.BOTH;
			JPanel previewPanel = current.getPreviewPanel();
			//previewPanel.setPreferredSize(new Dimension(Wavelets.mainFrame.getSize().width/2,100));//Temporary solution
			filterPanel.add(previewPanel, constraint);
			constraint.gridx=3;
			constraint.gridwidth=1;
			constraint.fill = GridBagConstraints.NONE;
			filterPanel.add(current.getEditingPanel(), constraint);
			if(i==0){
				swapPrev.setEnabled(false);
			}else if(i==numFilters-1){
				swapNext.setEnabled(false);
			}
		}
		Wavelets.updateDisplay();
	}
	
	public double[] getFreqBounds(){
		double minValue = Double.MAX_VALUE;
		double maxValue = Double.MIN_VALUE;
		for(Clip current:clips){
			double[] bounds = current.getFreqBounds();
			if(minValue>bounds[0]){
				minValue=bounds[0];
			}
			if(maxValue<bounds[1]){
				maxValue=bounds[1];
			}
		}
		return new double[]{minValue,maxValue};
	}
	
	public double[] getTimeBounds(){
		//TODO add support for "markers" which would override these values
		double minValue = Double.MAX_VALUE;
		double maxValue = Double.MIN_VALUE;
		for(Clip current:clips){
			double[] bounds = new double[]{current.startTime,current.endTime};
			if(minValue>bounds[0]){
				minValue=bounds[0];
			}
			if(maxValue<bounds[1]){
				maxValue=bounds[1];
			}
		}
		return new double[]{minValue,maxValue};
	}
	
	public void updateAudio(int newHash){
		double[] timeBounds = getTimeBounds();
		int total = (int) ((timeBounds[1]-timeBounds[0])*parentComposition.samplesPerSecond);
		//System.out.println("Generated array of length "+total);
		cacheValues = new double[total];
		for(int i=0;i<total;i++){
			cacheValues[i]=0d;
		}
		for(Clip current:clips){
			//System.out.println("Next clip");
			if(current.inputsRegistered){
				double[] clipData = current.getAudio();
				int offset = (int) ((current.startTime-timeBounds[0])*parentComposition.samplesPerSecond);
				int target = clipData.length+offset-1;
				int cap = Math.min(total, target);
				for(int i=offset;i<cap;i++){
					//Double safety
					cacheValues[i]+=clipData[i-offset];
				}
				//System.out.println("Added "+(cap-offset)+" values");
			}
		}
		for(Filter current:filters){
			cacheValues = current.filter(cacheValues, timeBounds[0]);
		}
		cacheHash=newHash;
	}
	
	public double[] getAudio(){
		int newHash = hashCode();
		if(newHash!=cacheHash){
			updateAudio(newHash);
		}
		return cacheValues;
	}
	
	public void clearCache(){
		cacheHash = 0;
		cacheValues = null;
		for(Clip current:clips){
			current.clearCache();
		}
	}
	
	//Export as JSON
	public JSONObject exportJson(){
		JSONObject result = new JSONObject();
		JSONArray clipJson = new JSONArray();
		result.put("clips", clipJson);
		for(Clip current:clips){
			clipJson.put(current.exportJson());
		}
		return result;
	}
	
	//Cleanup
	public void destroy(){
		parentComposition = null;
		self = null;
		for(Clip current:clips){
			current.destroy();
		}
		clips = null;
		filters = null;
		cacheValues = null;
	}
	
	//Cleanup layer but not clips
	public void destroyLayer(){
		parentComposition = null;
		self = null;
		clips = null;
		filters = null;
		cacheValues = null;
	}
	
	public int hashCode(){
		return WaveUtils.quickHash(1021, clips, filters);
	}
}
