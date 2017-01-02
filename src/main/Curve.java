package main;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;
import components.*;
import org.json.*;

//Curve made of samples
public class Curve implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//Curve data
	protected ArrayList<Double> locations = new ArrayList<Double>();
	protected ArrayList<Double> values = new ArrayList<Double>();
	protected int listSize = 0;
	protected int mode = 0;
	//0 for simple bezier, 1 for square interpolation, 2 for sine interpolation, 3 for autocubic
	
	//Nice names which are mapped to modes
	public static HashMap<String,Integer> modeNames = new HashMap<String,Integer>();
	public static String[] modeNamesKeys;
	public static HashMap<String,Integer> previewModes = new HashMap<String,Integer>();
	public static String[] previewModesKeys = {"As waveform - Flat tone"};
	//Mapped
	public static int selectedPreviewMode = 0;
	
	//Display
	public transient JPanel viewPanel;//View the curve
	public transient JPanel editPanel;//Edit the curve
	public transient JPanel editPanelSelector;//Point selector
	public transient JButton editPanelPrev;//Previous
	public transient JButton editPanelNext;//Next
	public transient JLabel editPanelIndex;//Show current point index
	public transient JPanel editPanelCenter;//Contains edit location and value
	public transient JPanel editPanelLocation;//Location container
	public transient JLabel editPanelLocationLabel;
	public transient JTextField editPanelLocationField;
	public transient JPanel editPanelValue;//Value container
	public transient JLabel editPanelValueLabel;
	public transient JTextField editPanelValueField;
	public transient JPanel editPanelConfirm;//Save or delete
	public transient JButton editPanelSave;//Overwrite current point
	public transient JButton editPanelDelete;//Delete current point
	public transient JButton editPanelCreate;//Create new point
	public transient int selected;//Selection index, -1 is none
	
	//Standard constructor
	public Curve(){
		initPanels();
	}
	
	public static void updateModeNames(){
		modeNamesKeys = modeNames.keySet().toArray(new String[0]);
		previewModesKeys = previewModes.keySet().toArray(new String[0]);
	}
	
	public void initPanels(){
		selected = -1;
		//Add to scroll pane
		viewPanel = new WCurveViewerPanel(this);
		//viewPanel.setToolTipText("Shows the curve with markers at every point. The selected point will have a different marker.");
		editPanel = new JPanel(new BorderLayout());
		//Add to edit panel
		editPanelSelector = new JPanel(new BorderLayout());
		editPanelCenter = new JPanel(new BorderLayout());
		editPanelConfirm = new JPanel(new BorderLayout());
		//Add to selector panel
		editPanelPrev = new JButton("\u25C4");//Left arrow
		//editPanelPrev.setToolTipText("Selects the previous point. If the leftmost point is currently selected, loops to the rightmost point.");
		editPanelNext = new JButton("\u25BA");//Right arrow
		//editPanelNext.setToolTipText("Selects the next point. If the rightmost point is currently selected, loops to the leftmost point.");
		editPanelIndex = new JLabel("",SwingConstants.CENTER);
		//editPanelIndex.setToolTipText("Shows the selected point's index.");
		//Add to center panel
		editPanelLocation = new JPanel(new BorderLayout());
		editPanelValue = new JPanel(new BorderLayout());
		//Add to confirm panel
		editPanelSave = new JButton("Save");
		//editPanelSave.setToolTipText("Saves the new position data for the selected point.");
		editPanelDelete = new JButton("Delete");
		//editPanelDelete.setToolTipText("Deletes the selected point.");
		editPanelCreate = new JButton("Create");
		//editPanelCreate.setToolTipText("Creates a new point at the specified position. If there is already a point at that location, replaces it.");
		//Add to location panel
		editPanelLocationLabel = new JLabel("Location");
		editPanelLocationField = new JTextField(10);
		//editPanelLocationField.setToolTipText("Specify or view location, or X value. Decimal only.");
		//Add to value panel
		editPanelValueLabel = new JLabel("Value");
		editPanelValueField = new JTextField(10);
		//editPanelValueField.setToolTipText("Specify or view value, or Y value. Decimal only.");
		//Add all to respective parent panels
		editPanelSelector.add(editPanelPrev,BorderLayout.LINE_START);
		editPanelSelector.add(editPanelNext,BorderLayout.LINE_END);
		editPanelSelector.add(editPanelIndex,BorderLayout.CENTER);
		editPanel.add(editPanelSelector,BorderLayout.PAGE_START);
		editPanelLocation.add(editPanelLocationLabel,BorderLayout.LINE_START);
		editPanelLocation.add(editPanelLocationField,BorderLayout.LINE_END);
		editPanelCenter.add(editPanelLocation,BorderLayout.PAGE_START);
		editPanelValue.add(editPanelValueLabel,BorderLayout.LINE_START);
		editPanelValue.add(editPanelValueField,BorderLayout.LINE_END);
		editPanelCenter.add(editPanelValue,BorderLayout.PAGE_END);
		editPanel.add(editPanelCenter,BorderLayout.CENTER);
		editPanelConfirm.add(editPanelSave,BorderLayout.LINE_START);
		editPanelConfirm.add(editPanelDelete,BorderLayout.CENTER);
		editPanelConfirm.add(editPanelCreate,BorderLayout.LINE_END);
		editPanel.add(editPanelConfirm,BorderLayout.PAGE_END);
		//Listeners
		editPanelPrev.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent arg0) {
				selected--;
				updateSelection();
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
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
		editPanelNext.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent arg0) {
				selected++;
				updateSelection();
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
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
		editPanelSave.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(listSize>0&&selected>-1){
					tryEditPoint(selected,Double.valueOf(editPanelLocationField.getText()),Double.valueOf(editPanelValueField.getText()));
					updateSelection();
				}
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
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
		editPanelDelete.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(listSize>0&&selected>-1){
					removePoint(selected);
					updateSelection();
				}
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
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
		editPanelCreate.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent arg0) {
				selected = tryAddPoint(Double.valueOf(editPanelLocationField.getText()),Double.valueOf(editPanelValueField.getText()));
				updateSelection();
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
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
		//Finish
		updateSelection();
	}
	
	public void updateSelection(){
		if(listSize>0){
			selected = Math.floorMod(selected,listSize);
			editPanelLocationField.setText(Double.toString(locations.get(selected)));
			editPanelValueField.setText(Double.toString(values.get(selected)));
			editPanelIndex.setText(Integer.toString(selected+1));
			editPanelPrev.setEnabled(true);
			editPanelNext.setEnabled(true);
			editPanelSave.setEnabled(true);
			editPanelDelete.setEnabled(true);
		}else{
			selected = -1;
			editPanelIndex.setText("Curve is empty");
			editPanelPrev.setEnabled(false);
			editPanelNext.setEnabled(false);
			editPanelSave.setEnabled(false);
			editPanelDelete.setEnabled(false);
		}
		Wavelets.updateCurveSelection();
	}
	
	public void setMode(int a){
		mode = a;
	}
	
	public int getMode(){
		return mode;
	}
	
	public int getSize(){
		return listSize;
	}
	
	public int addPoint(double location, double value){
		int index;
		boolean cont = true;
		for(index=0;index<listSize&&cont;index++){
			if(location<locations.get(index)){
				cont=false;
				index--;
			}
		}
		locations.add(index,location);
		values.add(index,value);
		listSize++;
		return index;
	}
	
	public void removePoint(int index){
		locations.remove(index);
		values.remove(index);
		listSize--;
	}
	
	public void editPoint(int index, double location, double value){
		removePoint(index);
		addPoint(location,value);
	}
	
	public int tryAddPoint(double location, double value){
		if(locations.contains(location)){
			int index = locations.indexOf(location);
			editPoint(index,location,value);
			return index;
		}else{
			return addPoint(location,value);
		}
	}
	
	public void tryEditPoint(int index, double location, double value){
		boolean locationsMatch = locations.get(index)==location;
		if((!locationsMatch)||values.get(index)!=value){
			if(locations.contains(location)){
				if(locationsMatch){
					values.set(index, value);
				}
			}else{
				editPoint(index,location,value);
			}
		}
	}
	
	public static double lerp(double a, double b, double t){
		return (1-t)*a+t*b;
	}
	
	public static double lerp3(double a, double b, double c, double t){
		return lerp(lerp(a,b,t),lerp(b,c,t),t);
	}
	
	public static double lerp4(double a, double b, double c, double d, double t){
		return lerp(lerp3(a,b,c,t),lerp3(b,c,d,t),t);
	}
	
	public static double lerpn(ArrayList<Double> points,double t){
		int numPoints = points.size();
		if(numPoints==2){
			return lerp(points.get(0),points.get(1),t);
		}else{
			ArrayList<Double> first = new ArrayList<Double>(points);
			first.remove(numPoints-1);
			ArrayList<Double> second = new ArrayList<Double>(points);
			second.remove(0);
			return lerp(lerpn(first,t),lerpn(second,t),t);
		}
	}
	
	public static double sinlerp(double a, double b, double t){
		return lerp(a,b,0.5+0.5*(Math.sin(Math.PI*(t-0.5))));
	}
	
	public static double sinlerpn(ArrayList<Double> points,double t){
		int numPoints = points.size();
		if(numPoints==2){
			return sinlerp(points.get(0),points.get(1),t);
		}else{
			ArrayList<Double> first = new ArrayList<Double>(points);
			first.remove(numPoints-1);
			ArrayList<Double> second = new ArrayList<Double>(points);
			second.remove(0);
			return sinlerp(lerpn(first,t),lerpn(second,t),t);
		}
	}
	
	public static double squarelerp(double a, double b, double t){
		if(t<0.5){
			double modt = Math.pow(t,2)*2;
			return lerp(a,b,modt);
		}else{
			double modt = 1-Math.pow(1-t,2)*2;
			return lerp(a,b,modt);
		}
	}
	
	public static double interp(int lerpmode, double a, double b, double t){
		double result;
		switch(lerpmode){
			case 0:{
				result=lerp(a,b,t);
				break;
			}case 1:{
				result=squarelerp(a,b,t);
				break;
			}case 2:{
				result=sinlerp(a,b,t);
				break;
			}default:{
				result=0;
				break;
			}
		}
		return result;
	}
	
	public static double interp(int lerpmode, double a, double b, double c, double d, double t){
		return interp(lerpmode,b,d,(t-a)/(c-a));
	}
	
	public double valueAtPos(double pos){
		if(pos<=locations.get(0)){
			return values.get(0);
		}else if(pos>=locations.get(listSize-1)){
			return values.get(listSize-1);
		}else{
			if(mode<3){
				int index;
				boolean cont = true;
				for(index=0;index<listSize&&cont;index++){
					if(pos<locations.get(index)){
						cont=false;
						index--;
					}
				}
				return interp(mode, locations.get(index-1),values.get(index-1),locations.get(index),values.get(index),pos);
			}else{//Auto cubic
				if(listSize==2){
					return lerp4(values.get(0),values.get(0),values.get(1),values.get(1),(pos-locations.get(0))/(locations.get(1)-locations.get(0)));
				}else if(listSize==3){
					double difference = (values.get(2)-values.get(0))/(locations.get(2)-locations.get(0));
					if(pos>locations.get(1)){
						double distance = locations.get(2)-locations.get(1);
						difference*=distance;
						return lerp4(values.get(1),values.get(1)+difference,values.get(2),values.get(2),(pos-locations.get(1))/(distance));
					}else{
						double distance = locations.get(1)-locations.get(0);
						difference*=distance;
						return lerp4(values.get(0),values.get(0),values.get(1)-difference,values.get(1),(pos-locations.get(0))/(distance));
					}
				}else{
					int index;
					boolean cont = true;
					for(index=0;index<listSize&&cont;index++){
						if(pos<locations.get(index)){
							cont=false;
							index--;
						}
					}
					double dif1;
					double dif2;
					double distance = locations.get(index)-locations.get(index-1);
					if(index==1){
						dif1 = 0;
						dif2 = (values.get(2)-values.get(0))/(locations.get(2)-locations.get(0));
					}else if(index==listSize-1){
						dif1 = (values.get(index)-values.get(index-2))/(locations.get(index)-locations.get(index-2));
						dif2 = 0;
					}else{
						dif1 = (values.get(index)-values.get(index-2))/(locations.get(index)-locations.get(index-2));
						dif2 = values.get(index+1)-values.get(index-1)/(locations.get(index+1)-locations.get(index-1));
					}
					//0.5 is the calibrated tested correct value
					dif1*=distance/2d;
					dif2*=distance/2d;
					return lerp4(values.get(index-1),values.get(index-1)+dif1,values.get(index)-dif2,values.get(index),(pos-locations.get(index-1))/distance);
				}
			}
		}
	}
	
	public ArrayList<Double> getLocations(){
		return locations;
	}
	
	public ArrayList<Double> getValues(){
		return values;
	}
	
	//Export as JSON
	public JSONObject exportJson(){
		JSONObject result = new JSONObject();
		result.put("locations", new JSONArray(locations));
		result.put("values", new JSONArray(values));
		result.put("mode", mode);
		return result;
	}
	
	//Cleanup
	public void destroy(){
		locations = null;
		values = null;
	}

	public int hashCode(){
		return WaveUtils.quickHash(7057, locations, values, mode);
	}
}
