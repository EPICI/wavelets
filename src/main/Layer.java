package main;

import java.util.ArrayList;
import javax.swing.*;
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
	//TODO
	
	public Layer(){
		initTransient();
		init();
	}
	
	//Create all transient variables
	public void initTransient(){
		for(Clip currentClip:clips){
			currentClip.initTransient();
		}
		self = this;
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
	}
	
	//Initialize
	public void init(){
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
	
	public static void main(String[] args){
		//Leave empty
	}
}
