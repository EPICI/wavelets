package main;

import java.util.ArrayList;
import javax.swing.*;
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
	public int clipCount = 0;
	public int selectedClip = -1;
	//Layer name
	public String name = "";
	//All filters, in order they are applied
	public ArrayList<Filter> filters = new ArrayList<Filter>();
	
	//Display - left area
	public transient JPanel parentPanel;
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
		buttonRename = new JButton("\u270D");//Writing hand
		//buttonRename.setToolTipText("Brings up a text field for changing the name.");
		buttonEdit = new JButton("\u3030");//Wave
		//buttonEdit.setToolTipText("Selects the layer so it can be edited.");
		buttonDelete = new JButton("\u00D7");//Multiplication sign
		//buttonDelete.setToolTipText("Deletes the layer.");
		editMode = false;
		parentPanel.setLayout(layout);
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
	}
	
	public void edit(){
		if(editMode){
			String newName = nameTextArea.getText();
			if(parentComposition.layers.keySet().contains(newName)){
				if(name.equals(newName)){
					setName(newName);
					buttonRename.setText("\u270D");//Writing hand
					//buttonRename.setToolTipText("Brings up a text field for changing the name.");
					layout.replace(nameTextArea, nameLabel);
				}else{
					editMode=!editMode;//Must cancel
				}
			}else{
				parentComposition.renameLayer(name,newName);
				setName(newName);
				buttonRename.setText("\u270D");//Writing hand
				//buttonRename.setToolTipText("Brings up a text field for changing the name.");
				layout.replace(nameTextArea, nameLabel);
			}
		}else{
			setName(nameLabel.getText());
			buttonRename.setText("\u2714");//Checkmark
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
			((JLabel) Wavelets.composerTopPanelComponents.get(3).get(0)).setText(Integer.toString(selectedClip+1));
			clipDelete.setEnabled(true);
			clipDupli.setEnabled(true);
		}else{
			selectedClip = -1;
			propertyPanel.add(layerPropertyPanel);
			((JLabel) Wavelets.composerTopPanelComponents.get(3).get(0)).setText("Layer is empty");
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
	
	public void dupliClip(int index){
		addClip();
		if(index<clipCount&&selectedClip!=index){//Safety
			Clip selected = clips.get(selectedClip);
			Clip source = clips.get(index);
			selected.copyFrom(source);
		}
		selectedClip++;//Assumed invariant
		updateClipSelection();
	}
	
	public void removeClip(int index){
		if(index>=0&&index<clipCount){
			clips.remove(index);
		}
		updateClipSelection();
	}
	
	public static void main(String[] args){
		//Leave empty
	}
}
