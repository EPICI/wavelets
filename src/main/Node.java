package main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
//import org.json.*;

//Generic node object used in a network
public class Node implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//Primary stuff
	public String name;
	public String type;
	public ArrayList<String> args;
	//Cache values
	public transient boolean cacheUpdated = false;
	public transient double cacheValue;
	//If it needs updating
	public transient boolean updateOnNewFrame = true;
	//Track parent objects
	public Nodes parentNodes;
	
	//Constructor
	public Node(String givenname, String giventype, ArrayList<String> givenargs){
		name = givenname;
		type = giventype;
		shortType();
		args = givenargs;
	}
	
	//Array constructor
	public Node(String givenname, String giventype, String[] givenargs){
		name = givenname;
		type = giventype;
		shortType();
		//Convenient conversion code not by me
		args = new ArrayList<String>(Arrays.asList(givenargs));
	}
	
	//Read from a string
	public Node(String rawdata){
		String[] parts = rawdata.split(";");
		args = new ArrayList<String>();
		try{
			name = parts[0];
			type = parts[1];
			shortType();
			for(int i=2;i<parts.length;i++){
				args.add(parts[i]);
			}
		}catch(Exception e){
			System.out.println("Node compiler error");//TODO redirect this to second window
			e.printStackTrace();
		}
	}
	
	//Type short forms
	public void shortType(){
		switch(type){
		case "constant":
		case "const":{
			type="c";
			break;
		}case "unit curve":{
			type="curf";
			break;
		}case "unit sine":
		case "unit sin":{
			type="sinf";
			break;
		}case "unit triangle":{
			type="trif";
			break;
		}case "unit square":{
			type="squf";
			break;
		}case "unit saw":
		case "unit sawtooth":{
			type="sawf";
			break;
		}case "sine":{
			type="sin";
			break;
		}case "cosine":{
			type="cos";
			break;
		}case "tangent":{
			type="tan";
			break;
		}case "arcsine":{
			type="asin";
			break;
		}case "arccosine":{
			type="acos";
			break;
		}case "arctangent":{
			type="atan";
			break;
		}case "hyperbolic sine":{
			type="sinh";
			break;
		}case "hyperbolic cosine":{
			type="cosh";
			break;
		}case "hyperbolic tangent":{
			type="tanh";
			break;
		}case "add":
		case "sum":{
			type="+";
			break;
		}case "subtract":{
			type="-";
			break;
		}case "multiply":
		case "product":{
			type="*";
			break;
		}case "divide":{
			type="/";
			break;
		}case "modulo":{
			type="%";
			break;
		}case "power":{
			type="^";
			break;
		}case "directional power":{
			type="d^";
			break;
		}case "direction of":{
			type="dir";
			break;
		}case "copy sign":{
			type="copydir";
			break;
		}case "to frequency":
		case "pitch to frequency":{
			type="tofreq";
			break;
		}case "bezier":{
			type="bz";
			break;
		}case "sine interpolation":{
			type="sinbz";
			break;
		}case "log":{
			type="ln";
			break;
		}case "logab":
		case "logb":{
			type="log";
			break;
		}
		}
	}
		
	//Initialize transient variables
	public void initTransient(){
		cacheUpdated = false;
		if("c".equals(type)||"input".equals(type)){
			updateOnNewFrame = false;
		}else{
			updateOnNewFrame = parentNodes.groupRequiresUpdate(nodeArgs());
		}
	}
	
	//Filters out everything except for nodes
	public ArrayList<String> nodeArgs(){
		ArrayList<String> result = new ArrayList<String>(args);//Shallow copy
		if("c".equals(type)||"curve".equals(type)||"curf".equals(type)||"input".equals(type)||"harmonic".equals(type)){
			result.remove(0);
		}
		return result;
	}
	
	//Filters further to ensure only valid nodes are returned
	public ArrayList<String> filteredNodeArgs(){
		ArrayList<String> result = new ArrayList<String>(args);//Shallow copy
		if("c".equals(type)||"curve".equals(type)||"curf".equals(type)||"input".equals(type)||"harmonic".equals(type)){
			result.remove(0);
		}
		ArrayList<String> toRemove = new ArrayList<String>();
		for(String current:result){
			if(Nodes.codedInputs.contains(current)){
				toRemove.add(current);
			}
		}
		for(String current:toRemove){
			result.remove(current);
		}
		return result;
	}
	
	//Clear cache if it needs updating
	public void updateFrameCache(){
		if(updateOnNewFrame){
			cacheUpdated = false;
		}
	}
	
	//Force update
	public void forceUpdateFrameCache(){
		cacheUpdated = false;
	}
	
	//Calculate value
	public void recalculate(){
		switch(type){
		case "c":{
			cacheValue = Double.valueOf(args.get(0));
			break;
		}case "curve":{
			cacheValue = parentNodes.parentComposition.curves.get(args.get(0)).valueAtPos(parentNodes.getValueOf(args.get(1)));
			break;
		}case "curf":{
			cacheValue = WaveUtils.curf(parentNodes.getValueOf(args.get(1)), parentNodes.parentComposition.curves.get(args.get(0)));
			break;
		}case "harmonic":{
			cacheValue = WaveUtils.harmonic(parentNodes.getValueOf(args.get(1)), parentNodes.getValueOf(args.get(2)), parentNodes.getValueOf(args.get(3)), parentNodes.getValueOf(args.get(4)), parentNodes.getValueOf(args.get(5)), parentNodes.getValueOf(args.get(6)), parentNodes.getValueOf(args.get(7)), parentNodes.getValueOf(args.get(8)), parentNodes.parentComposition.curves.get(args.get(0)));
			break;
		}case "input":{
			cacheValue = parentNodes.user.getInput(args.get(0));
			break;
		}default:{
			//Add specific operators here
			ArrayList<Double> values = new ArrayList<Double>();
			for(String currentname:args){
				values.add(parentNodes.getValueOf(currentname));
			}
			switch(type){
			case "sinf":{
				cacheValue = WaveUtils.sinf(values.get(0));
				break;
			}case "squf":{
				cacheValue = WaveUtils.squf(values.get(0));
				break;
			}case "sawf":{
				cacheValue = WaveUtils.sawf(values.get(0));
				break;
			}case "trif":{
				cacheValue = WaveUtils.trif(values.get(0));
				break;
			}case "+":{
				cacheValue = 0d;
				for(Double i:values){
					cacheValue += i;
				}
				break;
			}case "-":{
				cacheValue = values.get(0)-values.get(1);
				break;
			}case "*":{
				cacheValue = 1d;
				for(Double i:values){
					cacheValue *= i;
				}
				break;
			}case "/":{
				cacheValue = values.get(0)/values.get(1);
				break;
			}case "%":{
				cacheValue = values.get(0)%values.get(1);
				break;
			}case "^":{
				cacheValue = Math.pow(values.get(0), values.get(1));
				break;
			}case "d^":{
				cacheValue = Math.copySign(Math.pow(values.get(0), values.get(1)),values.get(0));
				break;
			}case "sin":{
				cacheValue = Math.sin(values.get(0));
				break;
			}case "cos":{
				cacheValue = Math.cos(values.get(0));
				break;
			}case "tan":{
				cacheValue = Math.tan(values.get(0));
				break;
			}case "asin":{
				cacheValue = Math.asin(values.get(0));
				break;
			}case "acos":{
				cacheValue = Math.acos(values.get(0));
				break;
			}case "atan":{
				cacheValue = Math.atan(values.get(0));
				break;
			}case "sinh":{
				cacheValue = Math.sinh(values.get(0));
				break;
			}case "cosh":{
				cacheValue = Math.cosh(values.get(0));
				break;
			}case "tanh":{
				cacheValue = Math.tanh(values.get(0));
				break;
			}case "ln":{
				cacheValue = Math.log(values.get(0));
				break;
			}case "log":{
				cacheValue = Math.log(values.get(1))/Math.log(values.get(0));
				break;
			}case "map":{
				cacheValue = (values.get(0)-values.get(1))/(values.get(2)-values.get(1))*(values.get(4)-values.get(3))+values.get(3);
				break;
			}case "envelope":{
				cacheValue = (values.get(0)+1d)/2d*(values.get(2)-values.get(1))+values.get(1);
				break;
			}case "bz":{//Values are already retrieved so optimization attempt would be useless
				int count = values.size();
				ArrayList<Double> points = new ArrayList<Double>(values);
				points.remove(count-1);
				cacheValue = Curve.lerpn(points, values.get(count-1));
				break;
			}case "sinbz":{
				int count = values.size();
				ArrayList<Double> points = new ArrayList<Double>(values);
				points.remove(count-1);
				cacheValue = Curve.sinlerpn(points, values.get(count-1));
				break;
			}case "tofreq":{//Centered at A4, 440Hz
				cacheValue = 440d*Math.pow(2d, values.get(0)/12d);
				break;
			}case "round":{
				cacheValue = Math.round(values.get(0));
				break;
			}case "floor":{
				cacheValue = Math.floor(values.get(0));
				break;
			}case "ceil":{
				cacheValue = Math.ceil(values.get(0));
				break;
			}case "min":{
				cacheValue = Math.min(values.get(0),values.get(1));
				break;
			}case "max":{
				cacheValue = Math.min(values.get(1),values.get(0));
				break;
			}case "dir":{
				if(values.get(0)>0){
					cacheValue = 1d;
				}else if(values.get(0)<0){
					cacheValue = -1d;
				}else{
					cacheValue = 0d;
				}
				break;
			}case "copydir":{
				cacheValue = Math.copySign(values.get(1),values.get(0));
				break;
			}case ">":{
				if(values.get(0)>values.get(1)){
					cacheValue = 1d;
				}else{
					cacheValue = 0d;
				}
				break;
			}case "<":{
				if(values.get(0)<values.get(1)){
					cacheValue = 1d;
				}else{
					cacheValue = 0d;
				}
				break;
			}case "=":{
				if(values.get(0)==values.get(1)){
					cacheValue = 1d;
				}else{
					cacheValue = 0d;
				}
				break;
			}case "!=":{
				if(values.get(0)!=values.get(1)){
					cacheValue = 1d;
				}else{
					cacheValue = 0d;
				}
				break;
			}case ">=":{
				if(values.get(0)>=values.get(1)){
					cacheValue = 1d;
				}else{
					cacheValue = 0d;
				}
				break;
			}case "<=":{
				if(values.get(0)<=values.get(1)){
					cacheValue = 1d;
				}else{
					cacheValue = 0d;
				}
				break;
			}case "constrain":{
				if(values.get(2)>values.get(1)){
					if(values.get(0)>values.get(2)){
						cacheValue = values.get(2);
					}else if(values.get(0)<values.get(1)){
						cacheValue = values.get(1);
					}else{
						cacheValue = values.get(0);
					}
				}else if(values.get(2)<values.get(1)){
					if(values.get(0)>values.get(1)){
						cacheValue = values.get(1);
					}else if(values.get(0)<values.get(2)){
						cacheValue = values.get(2);
					}else{
						cacheValue = values.get(0);
					}
				}else{
					cacheValue = values.get(1);
				}
				break;
			}case "relay":{
				cacheValue = values.get(0);
				break;
			}
			}
		}
		}
		cacheUpdated = true;
	}
	
	//Get value method
	public double getValue(){
		if(!cacheUpdated){
			recalculate();
		}
		return cacheValue;
	}
	
	//Cleanup
	public void destroy(){
		args = null;
	}
	
	public int hashCode(){
		return WaveUtils.quickHash(1873, type, args);
	}
}
