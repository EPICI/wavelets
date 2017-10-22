package core;

/**
 * A double holder which can vary with offset
 * <br>
 * Preferred over primitive double fields for anything timely
 * 
 * @author EPICI
 * @version 1.0
 */
public interface AutoDouble {
	/**
	 * Get the value held
	 * <br>
	 * Behaviour depends on implementation, for constants,
	 * <i>time</i> is ignored
	 * 
	 * @param time time in seconds
	 * @return the value held at that time
	 */
	public double get(double time);
	/**
	 * Attempt to set the value field as a constant
	 * <br>
	 * Behaviour depends on implementation, may do nothing
	 * 
	 * @param value a constant to set
	 */
	public void set(double value);
	
	/**
	 * Make a double array automatable
	 * <br>
	 * Changes to the original will not affect the new array
	 * 
	 * @param original the array to wrap
	 * @return a AutoDouble array mirroring the contents of the original
	 */
	public static AutoDouble[] wrapCopy(double[] original){
		int n = original.length;
		AutoDouble[] result = new AutoDouble[n];
		for(int i=0;i<n;i++)
			result[i] = new Single(original[i]);
		return result;
	}
	
	/**
	 * Make a double array automatable
	 * <br>
	 * As long as the new array isn't overwritten, changes to the original
	 * will reflect in the new array
	 * 
	 * @param original the array to wrap
	 * @return a AutoDouble array mirroring the contents of the original
	 */
	public static AutoDouble[] wrap(double[] original){
		int n = original.length;
		AutoDouble[] result = new AutoDouble[n];
		for(int i=0;i<n;i++)
			result[i] = new Array(original,i);
		return result;
	}
	
	/**
	 * A simple holder
	 * 
	 * @author EPICI
	 * @version 1.0
	 * @see AutoDouble
	 */
	public class Single implements AutoDouble{
		/**
		 * The current value
		 */
		public double v;
		
		/**
		 * Constructor where all are specified
		 * 
		 * @param v the value
		 */
		public Single(double v){
			this.v = v;
		}
		
		public double get(double time){
			return v;
		}
		
		public void set(double value){
			v = value;
		}
	}
	
	/**
	 * References a position in an array
	 * 
	 * @author EPICI
	 * @version 1.0
	 * @see AutoDouble
	 */
	public class Array implements AutoDouble{
		/**
		 * Referenced array
		 */
		public double[] values;
		/**
		 * Array index
		 */
		public int index;
		
		/**
		 * Constructor where all are specified
		 * 
		 * @param vs the array
		 * @param i the index
		 */
		public Array(double[] vs,int i){
			this.values = vs;
			this.index = i;
		}
		
		public double get(double time){
			return values[index];
		}
		
		public void set(double value){
			values[index] = value;
		}
	}
	
	/**
	 * References an entry in a map
	 * 
	 * @author EPICI
	 * @version 1.0
	 * @param <T> the key type
	 * @see AutoDouble
	 */
	public class Map<T> implements AutoDouble{
		/**
		 * Referenced map
		 */
		public java.util.Map<T,Double> map;
		/**
		 * Map key
		 */
		public T key;
		
		/**
		 * Constructor where all are specified
		 * 
		 * @param map the map
		 * @param key the key
		 */
		public Map(java.util.Map<T,Double> map,T key){
			this.map = map;
			this.key = key;
		}
		
		public double get(double time){
			return map.get(key);
		}
		
		public void set(double value){
			map.put(key, value);
		}
	}
	
	/**
	 * References a curve directly
	 * <br>
	 * Uses generics to allow stricter typing
	 * 
	 * @author EPICI
	 * @version 1.0
	 * @param <T> type or family of curve
	 */
	public class Curve<T extends core.Curve> implements AutoDouble{
		/**
		 * Referenced curve
		 */
		public T curve;
		
		/**
		 * Constructor where all are specified
		 * 
		 * @param curve the curve
		 */
		public Curve(T curve){
			this.curve = curve;
		}
		
		public double get(double time){
			return curve.valueAtPosition(time);
		}
		
		/**
		 * Doesn't do anything. A subclass may cater to a specific
		 * curve type and allow some modification.
		 */
		public void set(double value){
			
		}
	}
}
