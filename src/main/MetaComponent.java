package main;

import javax.swing.*;
import java.util.*;

/*
 * JComponent holder with additional metadata to help the program
 */
public class MetaComponent<T extends JComponent> {
	/*
	 * Type name, where different ones with the same name are
	 * assumed to be interchangeable but not necessarily identical
	 * If the name fields match, the group fields should also match
	 */
	public String name;
	/*
	 * The name for the group of similar ones
	 * One will replace another rather than also being added
	 */
	public String group;
	/*
	 * The component itself
	 */
	public T component;
	/*
	 * Additional metadata
	 * User should know what to do with this
	 */
	public HashMap<String,Object> metaData;
	
	/*
	 * Constructor where all are specified
	 */
	public MetaComponent(String name,String group,T component,HashMap<String,Object> metaData){
		this.name=name;
		this.group=group;
		this.component=component;
		this.metaData=metaData;
	}
	/*
	 * Constructor where the HashMap is left empty
	 */
	public MetaComponent(String name,String group,T component){
		this.name=name;
		this.group=group;
		this.component=component;
		this.metaData=new HashMap<>();
	}
}
