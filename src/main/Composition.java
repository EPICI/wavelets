package main;

import java.io.Serializable;
import java.util.*;
import javax.swing.*;
import org.json.*;

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
		toAdd.parentComposition = this;
		nodes.put(name, toAdd);
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
		nodes.get(name).destroy();//Call destructor
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
		curves.get(name).destroy();//Call destructor
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
	public JScrollPane addLayer(){
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
		updateLayers();
		return toAdd.parentScroll;
	}
	
	//Add a new layer
	public void addLayer(Layer toAdd){
		toAdd.parentComposition = this;
		layers.put(toAdd.name,toAdd);
		layerPanels.add(toAdd.parentPanel);
		updateLayers();
		Wavelets.addLayerScroll(toAdd.parentScroll);
	}
	
	//Remove a layer
	public void removeLayer(String key){
		Layer currentLayer = layers.get(key);
		Wavelets.composerLeftInPanel.remove(currentLayer.parentScroll);
		layerPanels.remove(layers.get(key));
		layers.get(key).destroy();//Call destructor
		layers.remove(key);
		updateLayers();
		Wavelets.updateDisplay();
	}
	public void removeLayerOnly(String key){
		Layer currentLayer = layers.get(key);
		Wavelets.composerLeftInPanel.remove(currentLayer.parentScroll);
		layerPanels.remove(layers.get(key));
		layers.get(key).destroyLayer();//Call destructor
		layers.remove(key);
		updateLayers();
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
		updateLayers();
	}
	
	public void importDataFromJson(String data,boolean replace){
		/*
		 * Reads off of a string, which could come from a file
		 * Should be in JSON format
		 * "data" should have an array of objects
		 * "type" specifies type
		 * "name" is the name
		 * "data" is all the object data specific to its type
		 */
		try{
			JSONObject json = new JSONObject(data);
			JSONObject jsonData = json.getJSONObject("data");
			JSONArray jsonCurves = jsonData.getJSONArray("curves");
			JSONArray jsonNodes = jsonData.getJSONArray("nodes");
			JSONArray jsonLayers = jsonData.getJSONArray("layers");
			int curveCount = jsonCurves.length();
			for(int i=0;i<curveCount;i++){
				JSONObject obj = jsonCurves.getJSONObject(i);
				String objName = obj.getString("name");
				JSONObject objData = obj.getJSONObject("data");
				if(curves.containsKey(objName)){
					if(replace){
						removeCurve(objName);
						tryImportCurveFromJson(objName,objData);
					}
				}else{
					tryImportCurveFromJson(objName,objData);
				}
			}
			updateCurves();
			int nodesCount = jsonNodes.length();
			for(int i=0;i<nodesCount;i++){
				JSONObject obj = jsonNodes.getJSONObject(i);
				String objName = obj.getString("name");
				JSONObject objData = obj.getJSONObject("data");
				if(nodes.containsKey(objName)){
					if(replace){
						removeNodes(objName);
						tryImportNodesFromJson(objName,objData);
					}
				}else{
					tryImportNodesFromJson(objName,objData);
				}
			}
			updateNodes();
			int layerCount = jsonLayers.length();
			for(int i=0;i<layerCount;i++){
				JSONObject obj = jsonLayers.getJSONObject(i);
				String objName = obj.getString("name");
				JSONObject objData = obj.getJSONObject("data");
				if(layers.containsKey(objName)){
					if(replace){
						removeLayer(objName);
						tryImportLayerFromJson(objName,objData);
					}
				}else{
					tryImportLayerFromJson(objName,objData);
				}
			}
			updateLayers();
		}catch(Exception e){
			System.out.println("Error importing JSON data");
			e.printStackTrace();
		}
	}
	
	public void tryImportLayerFromJson(String name,JSONObject data){
		//Attempts to read data from a JSON object and create a new layer with it
		try{
			//TODO properties
			JSONArray clipJson = data.getJSONArray("clips");
			ArrayList<Clip> clips = new ArrayList<Clip>();
			//TODO filters
			int numClips = clipJson.length();
			for(int i=0;i<numClips;i++){
				Clip toAdd = tryReadClipFromJson(clipJson.getJSONObject(i));
				if(toAdd!=null){
					clips.add(toAdd);
				}
			}
			Layer toAdd = new Layer();
			toAdd.setName(name);
			for(Clip current:clips){
				toAdd.addClip(current);
			}
			addLayer(toAdd);
		}catch(Exception e){
			System.out.println("Error importing layer from JSON data");
			e.printStackTrace();
		}
	}
	
	public void tryImportCurveFromJson(String name,JSONObject data){
		//Attempts to read data from a JSON object and create a new curve with it
		try{
			JSONArray locationsJson = data.getJSONArray("locations");
			int count = locationsJson.length();
			JSONArray valuesJson = data.getJSONArray("values");
			if(valuesJson.length()==count){
				double[] locations = new double[count];
				double[] values = new double[count];
				for(int i=0;i<count;i++){
					locations[i] = locationsJson.getDouble(i);
					values[i] = valuesJson.getDouble(i);
				}
				int mode = (int) Math.round(data.getDouble("mode"));
				if(mode>-1&&mode<4){
					Curve toAdd = new Curve();
					toAdd.setMode(mode);
					for(int i=0;i<count;i++){
						toAdd.tryAddPoint(locations[i], values[i]);
					}
					curves.put(name, toAdd);
				}
			}
		}catch(Exception e){
			System.out.println("Error importing curve from JSON data");
			e.printStackTrace();
		}
	}
	
	public void tryImportNodesFromJson(String name,JSONObject data){
		//Attempts to read data from a JSON object and create a new node network with it
		try{
			String source = data.getString("source");
			Nodes toAdd = new Nodes();
			toAdd.parentComposition = this;
			try{
				toAdd.source = source;
				toAdd.recompile();
				nodes.put(name, toAdd);
			}catch(Exception e){
				toAdd.destroy();
			}
		}catch(Exception e){
			System.out.println("Error importing nodes from JSON data");
			e.printStackTrace();
		}
	}
	
	public Clip tryReadClipFromJson(JSONObject data){
		Clip result = null;
		try{
			String nodesName = data.getString("nodes");
			double start = data.getDouble("start");
			double end = data.getDouble("end");
			JSONObject inputs = data.getJSONObject("inputs");
			Clip building = new Clip();
			building.parentLayer = nodeLayer;
			building.nodesName = nodesName;
			building.initTransient();
			building.infoNodeSelector.setSelectedItem(nodesName);
			building.startTime = start;
			building.endTime = end;
			building.updateLength();
			for(String key:inputs.keySet()){
				building.inputs.put(key, inputs.getDouble(key));
			}
			building.inputsRegistered = true;
			building.refreshNodes();
			result = building;
		}catch(Exception e){
			System.out.println("Error reading clip from JSON");
			e.printStackTrace();
		}
		return result;
	}
	
	//Export all as JSON
	public JSONObject exportJson(){
		JSONObject result = new JSONObject();
		JSONObject data = new JSONObject();
		JSONArray jsonCurves = new JSONArray();
		JSONArray jsonNodes = new JSONArray();
		JSONArray jsonLayers = new JSONArray();
		for(String name:curves.keySet()){
			Curve current = curves.get(name);
			JSONObject toAdd = new JSONObject();
			toAdd.put("name", name);
			toAdd.put("data", current.exportJson());
			jsonCurves.put(toAdd);
		}
		for(String name:nodes.keySet()){
			Nodes current = nodes.get(name);
			JSONObject toAdd = new JSONObject();
			toAdd.put("name", name);
			toAdd.put("data", current.exportJson());
			jsonNodes.put(toAdd);
		}
		for(String name:layers.keySet()){
			Layer current = layers.get(name);
			JSONObject toAdd = new JSONObject();
			toAdd.put("name", name);
			toAdd.put("data", current.exportJson());
			jsonLayers.put(toAdd);
		}
		data.put("curves", jsonCurves);
		data.put("nodes", jsonNodes);
		data.put("layers", jsonLayers);
		result.put("data", data);
		return result;
	}

	public int hashCode(){
		ArrayList<Object> settings = new ArrayList<Object>();
		settings.add(samplesPerSecond);
		ArrayList<Object> hashSource = new ArrayList<Object>();
		hashSource.add(curves);
		hashSource.add(nodes);
		hashSource.add(layers);
		hashSource.add(settings);
		return hashSource.hashCode();
	}
}
