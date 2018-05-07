package core;

import java.io.Serializable;
import java.util.*;

import ui.PatternEditor;
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
public class Clip implements BetterClone<Clip>, Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * A rather safe default value used for extending the
	 * properties list and fetching if the list is not long enough
	 */
	public static final double DEFAULT_VALUE = 0.5d;
	
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
				// Work with only this range
				List<Double> slice = properties.subList(from, to);
				ArrayList<Double> copy = new ArrayList<>();
				// Make a rotated copy
				copy.addAll(slice.subList(length-by, length));
				copy.addAll(slice.subList(0, length-by));
				// Copy back to slice
				Collections.copy(slice, copy);
				return true;
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
	
	private static final String CLIP_CLASS_NAME = Clip.class.getCanonicalName();
	@Override
	public Clip copy(int depth,Map<String,Object> options){
		int newDelay = delay,
				newLength = length,
				newPitch = pitch;
		double newVolume = volume;
		// avoid redundant copy, so set null for now
		ArrayList<Double> newProperties = null;
		Map<String,Object> set = (Map<String,Object>)options.get("set");
		if(set!=null){
			Number val;
			val = (Number) set.get(CLIP_CLASS_NAME+".delay");
			if(val!=null)newDelay = val.intValue();
			val = (Number) set.get(CLIP_CLASS_NAME+".length");
			if(val!=null)newLength = val.intValue();
			val = (Number) set.get(CLIP_CLASS_NAME+".pitch");
			if(val!=null)newPitch = val.intValue();
			val = (Number) set.get(CLIP_CLASS_NAME+".volume");
			if(val!=null)newVolume = val.doubleValue();
			// any iterable is allowed, valid values override old ones
			Iterable<?> lval = (Iterable<?>) set.get(CLIP_CLASS_NAME+".properties");
			if(lval!=null){
				// make copy
				newProperties = new ArrayList<>(properties);
				int size = newProperties.size();
				Iterator<?> iter = lval.iterator();
				// exhaust as many items as possible which don't need extending the list
				for(int i=0;i<size && iter.hasNext();i++){
					Object value = iter.next();
					if(value!=null && (value instanceof Number)){
						newProperties.set(i, ((Number)value).doubleValue());
					}
				}
				// do remaining items, if any
				while(iter.hasNext()){
					Object value = iter.next();
					if(value!=null && (value instanceof Number)){
						newProperties.add(((Number)value).doubleValue());
					}
				}
			}
		}
		Clip result = new Clip(newDelay,newLength,newPitch,newVolume);
		// if null, then no changes, so use original
		result.properties.addAll(newProperties==null?properties:newProperties);
		return result;
	}
	
	/**
	 * A clip template which defines additional properties
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public static class Template implements Named, Serializable {
		private static final long serialVersionUID = 1L;
		
		/**
		 * List of properties
		 */
		public final ArrayList<Property> properties;
		/**
		 * The name of this template. Use getter and setter rather than direct access.
		 */
		public String name;
		
		/**
		 * Blank constructor
		 */
		public Template(){
			properties = new ArrayList<>();
		}
		
		/*
		 * delaying implementation of copy methods for now
		 * since template is a named object and it needs to be given
		 * a different name, renaming is specified by NamedMap
		 * and needs knowledge of session
		 */
		
		@Override
		public String getName(){
			if(name==null || name.length()==0)return Track.defaultNameAny("Template", this);
			return name;
		}
		
		@Override
		public boolean setName(String newName){
			name = newName;
			return true;
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
		public static class Property implements BetterClone<Property>, Serializable {
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
				this(
						"",
						-Double.MAX_VALUE,
						DEFAULT_VALUE,
						Double.MAX_VALUE,
						PatternEditor.LinkedEditorPane.STEP_MODE_NAME_LINEAR,
						PatternEditor.LinkedEditorPane.UPDATE_MODE_NAME_DIFFERENCE_ADD
						);
			}
			
			/**
			 * Constructor providing all values. Unchecked.
			 * 
			 * @param name
			 * @param min
			 * @param base
			 * @param max
			 * @param step
			 * @param update
			 */
			public Property(String name,double min,double base,double max,String step,String update){
				this.name = name;
				this.min = min;
				this.base = base;
				this.max = max;
				this.step = step;
				this.update = update;
			}
			
			/**
			 * Copy the data of another property object.
			 * 
			 * @param source
			 */
			public void copyFrom(Property source){
				name = source.name;
				min = source.min;
				base = source.base;
				max = source.max;
				step = source.step;
				update = source.update;
			}
			
			/**
			 * Make a property with the same data as this instance.
			 * 
			 * @return copy
			 */
			public Property copy(){
				Property result = new Property();
				result.copyFrom(this);
				return result;
			}
			
			private static final String PROPERTY_CLASS_NAME = Property.class.getCanonicalName();
			@Override
			public Property copy(int depth,Map<String,Object> options){
				double newMin = min,
						newBase = base,
						newMax = max;
				String newName = name,
						newStep = step,
						newUpdate = update;
				Map<String,Object> set = (Map<String,Object>)options.get("set");
				if(set!=null){
					Number val;
					val = (Number) set.get(PROPERTY_CLASS_NAME+".min");
					if(val!=null)newMin = val.doubleValue();
					val = (Number) set.get(PROPERTY_CLASS_NAME+".base");
					if(val!=null)newBase = val.doubleValue();
					val = (Number) set.get(PROPERTY_CLASS_NAME+".max");
					if(val!=null)newMax = val.doubleValue();
					CharSequence sval;
					sval = (CharSequence) set.get(PROPERTY_CLASS_NAME+".name");
					if(sval!=null)newName = sval.toString();
					sval = (CharSequence) set.get(PROPERTY_CLASS_NAME+".step");
					if(sval!=null)newStep = sval.toString();
					sval = (CharSequence) set.get(PROPERTY_CLASS_NAME+".update");
					if(sval!=null)newUpdate = sval.toString();
				}
				Property result = new Property(newName,newMin,newBase,newMax,newStep,newUpdate);
				return result;
			}
		}
		
	}
	
}
