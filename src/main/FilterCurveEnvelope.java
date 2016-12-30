package main;

import java.awt.GridLayout;

import javax.swing.*;
import components.*;
import java.awt.event.*;
import java.util.Arrays;

//Multiplies by a curve which defines an envelope
public class FilterCurveEnvelope implements Filter {
	private static final long serialVersionUID = 1L;
	
	public Composition parentComposition;
	public String controlName;
	public transient Curve control;
	public transient WCurveViewerPanel viewPanel;
	public transient JPanel editPanel;
	public transient JLabel selectLabel;
	public transient JComboBox<String> selector;

	@Override
	public JPanel getPreviewPanel() {
		return viewPanel;
	}

	@Override
	public JPanel getEditingPanel() {
		return editPanel;
	}

	@Override
	public void updateUI() {
		selector.setEnabled(false);
		selector.removeAllItems();
		for(String name:parentComposition.curvesKeysArray){//TODO change this later to reference a set parent
			selector.addItem(name);
		}
		selector.setEnabled(true);
	}

	@Override
	public double[] filter(double[] inputData,double offset) {
		int count = inputData.length;
		if(parentComposition.curves.containsKey(controlName)){
			control = parentComposition.curves.get(controlName);
			double[] modified = new double[count];
			for(int i=0;i<count;i++){
				modified[i] = inputData[i]*control.valueAtPos(i/parentComposition.samplesPerSecond+offset);
			}
			return modified;
		}else{
			return Arrays.copyOf(inputData, count);
		}
	}

	@Override
	public void setParentComposition(Composition parent) {
		parentComposition = parent;
	}

	@Override
	public void initTransient() {
		//Create objects
		control = parentComposition.curves.get(controlName);
		viewPanel = new WCurveViewerPanel(control);
		editPanel = new JPanel(new GridLayout(2,1));
		selectLabel = new JLabel("Control curve");
		selector = new JComboBox<String>(parentComposition.curvesKeysArray);
		//Add components
		editPanel.add(selectLabel);
		editPanel.add(selector);
		//Listeners
		selector.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent event) {
				if(event.getStateChange()==ItemEvent.SELECTED){
					controlName = (String) event.getItem();
					control = parentComposition.curves.get(controlName);
					viewPanel.trackCurve = control;
				}
			}
			
		});
	}

	@Override
	public void destroy() {
		control = null;
		viewPanel = null;
		editPanel = null;
		selectLabel = null;
		ItemListener[] listeners = selector.getItemListeners();
		for(ItemListener i:listeners){
			selector.removeItemListener(i);
		}
		selector = null;
		parentComposition = null;
	}

	public int hashCode(){
		if(control==null){
			return 0;
		}else{
			return control.hashCode()*33;//To make it unique
		}
	}
}
