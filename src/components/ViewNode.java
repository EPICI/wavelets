package components;

import java.util.ArrayList;
import main.Node;

//Some data about an individual node
public class ViewNode {
	//Additional information for some, otherwise just the name and type
	public ArrayList<String> comments;
	//General classification
	public String type;
	//How far back it is in the tree
	public int position;
	//Counter, node index
	public int count;
	//Vertical index, determined later
	public int index;
	//Prevent repeats
	public double lower;
	public double upper;
	//Backward links
	public ArrayList<String> links;
	//Draw location
	public int drawx;
	public int drawy;
	
	public ViewNode(Node original){//Use only with known outputs
		comments = new ArrayList<String>();
		comments.add(original.name);
		comments.add(original.type);
		type = "output";
		position = 1;
		switch(original.name){
		case("output"):{
			lower = 0.0d;
			upper = 0.5d;
			break;
		}case("frequency"):{
			lower = 0.5d;
			upper= 1.0d;
			break;
		}
		}
		switch(original.type){
		//Ignoring constants and inputs
		case("curve"):{
			comments.add(original.args.get(0));
			links = new ArrayList<String>();
			links.add(original.args.get(1));
			break;
		}case("curf"):{
			comments.add(original.args.get(0));
			links = new ArrayList<String>();
			links.add(original.args.get(1));
			break;
		}default:{
			links = original.nodeArgs();
		}
		}
	}
	
	public ViewNode(String name,ViewNode parent){//Use only with known inputs
		comments = new ArrayList<String>();
		comments.add(name);
		type = "input";
		position = 1;
		links = new ArrayList<String>();
	}
	
	public ViewNode(Node original,ViewNode parent){//For the rest of the tree
		comments = new ArrayList<String>();
		comments.add(original.name);
		comments.add(original.type);
		position = parent.position+1;
		switch(original.type){
		case("curve"):{
			type = "inter";
			comments.add(original.args.get(0));
			links = new ArrayList<String>();
			links.add(original.args.get(1));
			break;
		}case("curf"):{
			type = "inter";
			comments.add(original.args.get(0));
			links = new ArrayList<String>();
			links.add(original.args.get(1));
			break;
		}case("constant"):{
			type = "input";
			comments.add(original.args.get(0));
			links = new ArrayList<String>();
			break;
		}case("input"):{
			type = "input";
			comments.add(original.args.get(0));
			links = new ArrayList<String>();
			break;
		}default:{
			type = "inter";
			links = original.nodeArgs();
		}
		}
	}
	
	public void pushTo(int newPosition){
		if(newPosition>position){
			position=newPosition;
		}
	}
	
	public static double mapTo(double a, double b, double c, double d, double p){
		return (p-a)/(b-a)*(d-c)+c;
	}
}
