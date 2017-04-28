package core;

import javax.swing.*;
import java.util.*;

/**
 * JComponent holder with additional metadata to help the program
 * 
 * @author EPICI
 * @version 1.0
 */
public class MetaComponent<T extends JComponent> {
	/**
	 * Type name, where different ones with the same name are
	 * assumed to be interchangeable but not necessarily identical
	 * <br>
	 * If the name fields match, the group fields should also match
	 */
	public String name;
	/**
	 * The name for the group of similar ones
	 * <br>
	 * One will replace another rather than also being added
	 */
	public String group;
	/**
	 * The component itself
	 */
	public T component;
	/**
	 * Additional metadata
	 * <br>
	 * User should know what to do with this
	 */
	public HashMap<String,Object> metaData;
	
	/**
	 * Constructor where all are specified
	 * 
	 * @param name the type name
	 * @param group the group name
	 * @param component the component
	 * @param metaData the metadata map
	 */
	public MetaComponent(String name,String group,T component,HashMap<String,Object> metaData){
		this.name=name;
		this.group=group;
		this.component=component;
		this.metaData=metaData;
	}
	/**
	 * Constructor where the HashMap is left empty
	 * 
	 * @param name the type name
	 * @param group the group name
	 * @param component the component
	 */
	public MetaComponent(String name,String group,T component){
		this.name=name;
		this.group=group;
		this.component=component;
		this.metaData=new HashMap<>();
	}
}
