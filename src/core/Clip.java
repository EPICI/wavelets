package core;

import java.io.Serializable;
import java.util.*;
import util.math.*;
import util.hash.*;

/**
 * Represents a clip, as used by {@link Pattern}
 * <br>
 * Giving it its own class allows for some data changes
 * without changing the reference
 * 
 * @author EPICI
 * @version 1.0
 */
public class Clip implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * A rather safe default value used for extending the
	 * properties list and fetching if the list is not long enough
	 */
	public static final double DEFAULT_VALUE = 1;
	
	/**
	 * Hash key for <i>hashCode()</i>
	 */
	public static final long HK_HC = QuickKeyGen.next64();

	/**
	 * Delay, which will be multiplied later
	 */
	public int delay;
	/**
	 * Length, which will be multiplied later
	 */
	public int length;
	/**
	 * Pitch as semitones from A4 for convenience
	 */
	public int pitch;
	/**
	 * Relative volume in B
	 * <br>
	 * Synths are allowed to interpret this differently or
	 * ignore it entirely
	 */
	public double volume;
	/**
	 * Additional properties, as a list instead of a map
	 * so that properties do not need to be named/keyed
	 * <br>
	 * While it is public, direct access is not recommended.
	 * Please use the methods instead when possible.
	 */
	public final ArrayList<Double> properties;
	
	/**
	 * Blank constructor, useful if another source will immediately
	 * set the data
	 */
	protected Clip(){
		this(0,0,0);
	}
	
	/**
	 * Standard constructor using <i>volume=0</i>
	 * 
	 * @param delay
	 * @param length
	 * @param pitch
	 */
	public Clip(int delay,int length,int pitch){
		this(delay,length,pitch,0);
	}
	
	/**
	 * Standard constructor
	 * 
	 * @param delay
	 * @param length
	 * @param pitch
	 * @param volume
	 */
	public Clip(int delay,int length,int pitch,double volume){
		this.delay = delay;
		this.length = length;
		this.pitch = pitch;
		this.volume = volume;
		this.properties = new ArrayList<>();
	}
	
	/**
	 * Sets property at index <i>key</i> to <i>value</i>
	 * and extends the list if needed by adding a default value
	 * 
	 * @param key index to set at
	 * @param value value to set
	 */
	public void setProperty(int key,double value){
		setProperty(key,value,DEFAULT_VALUE);
	}
	
	/**
	 * Sets property at index <i>key</i> to <i>value</i>
	 * and extends the list if needed by adding a default value
	 * 
	 * @param key index to set at
	 * @param value value to set
	 * @param fill default value for extending the list
	 */
	public void setProperty(int key,double value,double fill){
		int n = countProperties();
		for(int i=n;i<=key;i++){
			properties.add(fill);
		}
		properties.set(key, value);
	}
	
	/**
	 * Get property at index <i>key</i>, or if the list does
	 * not include it, a default value
	 * 
	 * @param key index to fetch
	 * @return value at index if present, otherwise a default value
	 */
	public double getProperty(int key){
		return getProperty(key,DEFAULT_VALUE);
	}
	
	/**
	 * Get property at index <i>key</i>, or if the list does
	 * not include it, a default value
	 * 
	 * @param key index to fetch
	 * @param fill default value
	 * @return value at index if present, otherwise a default value
	 */
	public double getProperty(int key,double fill){
		int n = countProperties();
		if(key<n){
			return properties.get(key);
		}else{
			return fill;
		}
	}
	
	/**
	 * Trim the property list to be at most <i>key</i> long
	 * 
	 * @param key
	 * @return how many properties were removed
	 */
	public int trimProperty(int key){
		int n = countProperties();
		int diff = n-key;
		for(int i=n-1;i>=key;i--){
			properties.remove(i);
		}
		return Math.max(diff, 0);
	}
	
	/**
	 * Remove the property if it exists, shifting all properties
	 * after it back by 1
	 * 
	 * @param key
	 * @return if a property was removed
	 */
	public boolean removeProperty(int key){
		int n = countProperties();
		if(n>key){
			properties.remove(key);
			return true;
		}
		return false;
	}
	
	/**
	 * Attempt to swap two properties.
	 * 
	 * @param left
	 * @param right
	 * @return true if any modifications were made
	 */
	public boolean swapProperty(int left,int right){
		if(left==right)return false;
		// Ensure left<right
		if(left>right){int temp=left;left=right;right=temp;}
		// Range check
		if(left>=0&&right<countProperties()){
			Collections.swap(properties,left,right);
			return true;
		}
		return false;
	}
	
	/**
	 * Attempt to rotate all properties in a range.
	 * Considering the range as a subset of size <i>n</i>,
	 * index <i>i</i> will move to <i>i+by</i> modulo <i>n</i>
	 * within the subset.
	 * 
	 * @param from first index, inclusive
	 * @param to last index, exclusive
	 * @param by offset
	 * @return true if any modifications were made
	 */
	public boolean rotateProperty(int from,int to,int by){
		if(from<to&&from>=0&&to<=countProperties()){
			int length = to-from;
			by = Math.floorMod(by, length);
			if(by!=0){
				// Copy to buffer permuted
				ArrayList<Double> slice = new ArrayList<>();
				slice.addAll(properties.subList(to-by, to));
				slice.addAll(properties.subList(from,to-by));
				// Copy back
				for(int i=0;i<length;i++){
					properties.set(i+from, slice.get(i));
				}
			}
		}
		return false;
	}
	
	/**
	 * Get the length of the property list
	 * 
	 * @return
	 */
	public int countProperties(){
		return properties.size();
	}
	
	/**
	 * Fill up this clip's properties until it has
	 * all properties defined by the template.
	 * 
	 * @param template
	 */
	public void fillWith(Clip.Template template){
		fillWith(template,template.properties.size()-1);
	}
	
	/**
	 * Fill up this clip's properties until it includes <i>key</i>,
	 * using the template to define what values to add as defaults
	 * 
	 * @param template
	 * @param key
	 */
	public void fillWith(Clip.Template template,int key){
		for(int i=countProperties();i<=length;i++){
			setProperty(i, template.properties.get(i).base);
		}
	}
	
	/**
	 * Copy another clip's data
	 * 
	 * @param source
	 */
	public void copyFrom(Clip source){
		delay = source.delay;
		length = source.length;
		pitch = source.pitch;
		volume = source.volume;
		properties.clear();
		properties.addAll(source.properties);
	}
	
	/**
	 * Create a clip with the same data as this
	 * 
	 * @return
	 */
	public Clip copy(){
		Clip result = new Clip();
		result.copyFrom(this);
		return result;
	}
	
	public boolean equals(Object o){
		if(o==this)return true;
		if(o==null||!(o instanceof Clip))return false;
		Clip other = (Clip) o;
		boolean result = delay==other.delay
				&&length==other.length
				&&pitch==other.pitch
				&&Floats.isNear(volume, other.volume);
		if(!result)return false;
		int n = countProperties();
		if(n!=other.countProperties())return false;
		for(int i=0;i<n;i++){
			if(!Floats.isNear(
					getProperty(i),
					other.getProperty(i)))return false;
		}
		return true;
	}
	
	public int hashCode(){
		HashTriArx hash = new HashTriArx(HK_HC);
		hash.absorb(delay,length,pitch);
		hash.absorb(volume);
		int n = countProperties();
		for(int i=0;i<n;i++){
			hash.absorb(getProperty(i));
		}
		return hash.squeezeInt();
	}
	
	/**
	 * A clip template which defines additional properties
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public static class Template implements Serializable {
		private static final long serialVersionUID = 1L;
		
		/**
		 * List of properties
		 */
		public final ArrayList<Property> properties;
		
		/**
		 * Blank constructor
		 */
		public Template(){
			properties = new ArrayList<>();
		}
		
		/**
		 * An individual property, as used by the clip template,
		 * which defines how to treat the value but not the actual value,
		 * which is held by the clip
		 * <br>
		 * See {@link ui.PatternEditor} for how some of these are used
		 * 
		 * @author EPICI
		 * @version 1.0
		 */
		public static class Property implements Serializable {
			private static final long serialVersionUID = 1L;
			
			/**
			 * This shows in the UI, and is not a unique identifier
			 * <br>
			 * Optional
			 */
			public String name;
			/**
			 * Minimum allowed value
			 */
			public double min;
			/**
			 * Default value, used for new clips
			 */
			public double base;
			/**
			 * Maximum allowed value
			 */
			public double max;
			/**
			 * Determines how to step values
			 */
			public String step;
			/**
			 * Determines how to update clips from a value change
			 */
			public String update;
			
			/**
			 * Blank constructor, uses default values
			 */
			public Property(){
				name = "";
				min = -Double.MAX_VALUE;
				base = DEFAULT_VALUE;
				max = Double.MAX_VALUE;
				step = "Add";
				update = "Add difference";
			}
		}
		
	}
	
}
