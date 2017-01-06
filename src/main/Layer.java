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
		buttonRename.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent arg0) {
				edit();
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
		buttonEdit.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent arg0) {
				parentComposition.layerSelection = name;
				JPanel addTo = Wavelets.composerRightInPanel;
				addTo.removeAll();
				addTo.add(editPanel);
				addTo = Wavelets.composerBottomPanel;
				addTo.removeAll();
				addTo.add(filterScroll,BorderLayout.CENTER);
				Wavelets.updateDisplay();
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
		buttonDelete.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				parentComposition.removeLayer(self);
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
		clipDelete.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				removeClip(selectedClip);
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
		clipAdd.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				addClip();
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
		clipDupli.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				dupliClip(selectedClip);
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
		filterAdd.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				Filter toAdd = filterGenerators.get(filterTypeSelector.getSelectedItem()).createNew();
				filters.add(toAdd);
				refreshFilters();
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
			swapPrev.addMouseListener(new MouseListener(){

				@Override
				public void mouseClicked(MouseEvent arg0) {
					Collections.swap(filters, index-1, index);
					refreshFilters();
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
			JButton swapNext = new JButton("\u25bc");//Down arrow
			swapNext.addMouseListener(new MouseListener(){

				@Override
				public void mouseClicked(MouseEvent arg0) {
					Collections.swap(filters, index, index+1);
					refreshFilters();
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
			JButton delete = new JButton("\u00d7");//Multiplication sign
			delete.addMouseListener(new MouseListener(){

				@Override
				public void mouseClicked(MouseEvent arg0) {
					filters.remove(index);
					refreshFilters();
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
	
	public double[] getAudio(){
		int newHash = hashCode();
		if(newHash!=cacheHash){
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
