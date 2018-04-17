package core;

/**
 * A double holder which can vary with offset
 * <br>
 * Preferred over primitive double fields for anything timely
 * 
 * @author EPICI
 * @version 1.0
 */
public interface VarDouble {
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
	 * Attempt to set the value field as a constant,
	 * equivalent to calling {@link #set(double, double)} with time=0
	 * <br>
	 * Behaviour depends on implementation, may do nothing
	 * 
	 * @param value a constant to set
	 */
	public default void set(double value){
		set(0,value);
	}
	/**
	 * Attempt to set the value field for a specific time
	 * <br>
	 * Behaviour depends on implementation, may do nothing
	 * 
	 * @param time time to set value for
	 * @param value a value to set
	 */
	public default void set(double time,double value){
	}
	
	/**
	 * Make a double array automatable
	 * <br>
	 * Changes to the original will not affect the new array
	 * 
	 * @param original the array to wrap
	 * @return a AutoDouble array mirroring the contents of the original
	 */
	public static VarDouble[] wrapCopy(double[] original){
		int n = original.length;
		VarDouble[] result = new VarDouble[n];
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
	public static VarDouble[] wrap(double[] original){
		int n = original.length;
		VarDouble[] result = new VarDouble[n];
		for(int i=0;i<n;i++)
			result[i] = new Array(original,i);
		return result;
	}
	
	/**
	 * A simple holder
	 * 
	 * @author EPICI
	 * @version 1.0
	 * @see VarDouble
	 */
	public class Single implements VarDouble{
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
		
		public void set(double time,double value){
			v = value;
		}
	}
	
	/**
	 * References a position in an array
	 * 
	 * @author EPICI
	 * @version 1.0
	 * @see VarDouble
	 */
	public class Array implements VarDouble{
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
		
		public void set(double time,double value){
			values[index] = value;
		}
	}
	
	/**
	 * References an entry in a map
	 * 
	 * @author EPICI
	 * @version 1.0
	 * @param <T> the key type
	 * @see VarDouble
	 */
	public class Map<T> implements VarDouble{
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
		
		public void set(double time,double value){
			map.put(key, value);
		}
	}
	
}
