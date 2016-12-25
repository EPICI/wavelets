package components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JPanel;
import main.Nodes;
import main.Node;
import main.Wavelets;

//Used to view a node network
public class WNodesViewerPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	//Built in inputs
	public static final String[] codedInputsArray = {"time","position","rate","phase","start","end"};
	public static final ArrayList<String> codedInputs = new ArrayList<String>(Arrays.asList(codedInputsArray));
	//ViewNode comparator
	public static final Comparator<ViewNode> vnCompare = new Comparator<ViewNode>() {

		@Override
		public int compare(ViewNode a, ViewNode b) {
			return b.count-a.count;
		}
		
	};
	
	public Nodes trackNodes;
	private int nodeCounter;
	//Cached node tree
	public HashMap<String,ViewNode> cachedNodeTree;
	public int cacheHash = 0;
	
	//Constructor
	public WNodesViewerPanel(Nodes nodes){
		super();
		trackNodes = nodes;
	}
	
	//Custom painting
	@Override
	public void paintComponent(Graphics g){
		//Get object boundaries
		//Rectangle clipBounds = g.getClipBounds();
		Dimension dimensions = getSize();
		Rectangle clipBounds = new Rectangle(0,0,dimensions.width,dimensions.height);
		//Create colour object from theme colours
		Color colOuter = Draw.colorFromArray(Wavelets.rgbaOuter);
		Color colInner = Draw.colorFromArray(Wavelets.rgbaInner);
		//Blank background
		g.setColor(colOuter);
		g.fillRect(0,0,clipBounds.width,clipBounds.height);
		g.setColor(colInner);
		g.fillRect(2,2,clipBounds.width-4,clipBounds.height-4);
		//Quick check
		Set<String> names = trackNodes.getNodes().keySet();
		if(names.contains("output")&&names.contains("frequency")){
			HashMap<String,ViewNode> nodeTree;
			//Check if it's cached
			int newHash = trackNodes.getNodes().hashCode();
			if(newHash==cacheHash){
				//Setup tree
				nodeTree = cachedNodeTree;
			}else{
				//Setup tree
				nodeTree = buildTree();
				//Update cache
				cachedNodeTree = nodeTree;
				cacheHash = newHash;
			}
			//Radius for icons
			int nodeRadius = Math.min(clipBounds.width, clipBounds.height)/(5*nodeTree.size()+4);
			//Call draw method
			drawTree(g,nodeTree,nodeRadius);
		}
	}
	
	//Used to build the tree
	public HashMap<String,ViewNode> buildTree(){
		HashMap<String,ViewNode> nodeTree = new HashMap<String,ViewNode>();
		//Add known outputs
		nodeCounter = 2;
		ViewNode vnOutput = new ViewNode(trackNodes.getNodes().get("output"));
		vnOutput.count = 0;
		nodeTree.put("output",vnOutput);
		ViewNode vnFrequency = new ViewNode(trackNodes.getNodes().get("frequency"));
		vnFrequency.count = 1;
		nodeTree.put("frequency",vnFrequency);
		//Build tree
		nodeTree = treeFrom(nodeTree,trackNodes.getNodes().get("output"));
		nodeTree = treeFrom(nodeTree,trackNodes.getNodes().get("frequency"));
		//Find vertical positions
		HashMap<Integer,Integer> layerCounts = new HashMap<Integer,Integer>();
		HashMap<Integer,ArrayList<ViewNode>> layers = new HashMap<Integer,ArrayList<ViewNode>>();
		for(ViewNode current:nodeTree.values()){
			int layer = current.position;
			if(!layers.containsKey(layer)){
				layers.put(layer, new ArrayList<ViewNode>());
			}
			layers.get(layer).add(current);
		}
		for(ArrayList<ViewNode> currentLayer:layers.values()){
			Collections.sort(currentLayer,Collections.reverseOrder(vnCompare));
			for(ViewNode current:currentLayer){
				int layer = current.position;
				if(layerCounts.containsKey(layer)){
					layerCounts.put(layer, layerCounts.get(layer)+1);
				}else{
					layerCounts.put(layer, 1);
				}
				current.index = layerCounts.get(layer)-1;
			}
		}
		return nodeTree;
	}
	
	//Used to draw the tree
	public void drawTree(Graphics g,HashMap<String,ViewNode> nodeTree,int nodeRadius){//Find draw locations
		//Get object boundaries
		//Rectangle clipBounds = g.getClipBounds();
		Dimension dimensions = getSize();
		Rectangle clipBounds = new Rectangle(0,0,dimensions.width,dimensions.height);
		//Find horizontal bounds
		double highest = 1;
		for(ViewNode current:nodeTree.values()){
			if(current.position>highest){
				highest = current.position;
			}
		}
		highest++;
		//Margin for vetical boundaries
		double margin = 0.5;
		//Find vertical positions
		HashMap<Integer,Integer> layerCounts = new HashMap<Integer,Integer>();
		for(ViewNode current:nodeTree.values()){
			int layer = current.position;
			if(layerCounts.containsKey(layer)){
				layerCounts.put(layer, layerCounts.get(layer)+1);
			}else{
				layerCounts.put(layer, 1);
			}
			current.index = layerCounts.get(layer)-1;
		}
		//Find locations
		for(ViewNode current:nodeTree.values()){
			current.drawx = Draw.mapToRound(highest, 0, 2, clipBounds.width-4, current.position);
			current.drawy = Draw.mapToRound(-margin, layerCounts.get(current.position)+margin-1, 2, clipBounds.height-4, current.index);
		}
		//Get colours
		Color colInner = Draw.colorFromArray(Wavelets.rgbaInner);
		Color colGridLine = Draw.colorFromArray(Wavelets.rgbaGridLine);
		Color colHighlight = Draw.colorFromArray(Wavelets.rgbaHighlight);
		Color colInput = Draw.colorFromArray(Wavelets.rgbaNodeInput);
		Color colInter = Draw.colorFromArray(Wavelets.rgbaNodeInter);
		Color colOutput = Draw.colorFromArray(Wavelets.rgbaNodeOutput);
		Color colText = Draw.colorFromArray(Wavelets.rgbaText);
		//Draw lines for all of them
		g.setColor(colHighlight);
		for(ViewNode first:nodeTree.values()){
			//System.out.println(first.links);
			for(String secondKey:first.links){
				ViewNode second = nodeTree.get(secondKey);
				g.drawLine(first.drawx,first.drawy,second.drawx,second.drawy);
			}
		}
		//Draw icons for all of them
		for(ViewNode current:nodeTree.values()){
			Color outCol;
			switch(current.type){
			case "input":{
				outCol = colInput;
				break;
			}case "inter":{
				outCol = colInter;
				break;
			}case "output":{
				outCol = colOutput;
				break;
			}default:{
				outCol = colGridLine;
			}
			}
			Color inCol = Draw.interpColors(outCol, colInner, 0.5);
			g.setColor(outCol);
			g.fillOval(current.drawx-nodeRadius-2, current.drawy-nodeRadius-2, 2*nodeRadius+4, 2*nodeRadius+4);
			g.setColor(inCol);
			g.fillOval(current.drawx-nodeRadius, current.drawy-nodeRadius, 2*nodeRadius, 2*nodeRadius);
		}
		//Show text on top
		g.setColor(colText);
		for(ViewNode current:nodeTree.values()){
			int count = current.comments.size();
			for(int i=0;i<count;i++){
				String comment = current.comments.get(i);
				if(comment!=null){
					Draw.centeredString(g, comment, current.drawx, current.drawy+8*(2*i-count+1));
				}
			}
		}
	}
	
	//Used recursively to build a tree of the node network
	public HashMap<String,ViewNode> treeFrom(HashMap<String,ViewNode> original,Node parent){
		HashMap<String,ViewNode> result = new HashMap<String,ViewNode>(original);
		ViewNode associated = result.get(parent.name);
		for(String key:parent.nodeArgs()){
			ViewNode current;
			if(codedInputs.contains(key)){
				if(result.containsKey(key)){
					current = result.get(key);
					current.pushTo(associated.position+1);
				}else{
					current = new ViewNode(key,associated);
					result.put(key,current);
					result.get(key).pushTo(associated.position+1);
				}
			}else{
				Node child = trackNodes.getNodes().get(key);
				if(result.containsKey(key)){
					current = result.get(key);
					current.pushTo(associated.position+1);
				}else{
					current = new ViewNode(child,associated);
					result.put(child.name,current);
				}
				result = treeFrom(result,child);
			}
			current.count = nodeCounter;
			nodeCounter++;
		}
		return result;
	}
}
