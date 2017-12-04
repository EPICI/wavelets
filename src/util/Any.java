package util;

import java.util.Objects;

import util.hash.*;

/**
 * For when you want to return multiple different values in a less awkward way
 * <br>
 * Use only with objects since autoboxing is costly
 * <br>
 * These act as generic containers
 * <br>
 * The only argument against primitive holders is that you might as well make an
 * array, and if you have multiple types, bundle them together or make your own
 * class for that purpose
 * 
 * @author EPICI
 * @version 1.0
 */
public class Any {
	//Disallow invoking constructor
	private Any(){}
	
	/**
	 * Generic 2-object container
	 * 
	 * @author EPICI
	 * @version 1.0
	 *
	 * @param <A> the first type
	 * @param <B> the second type
	 */
	public static class O2<A,B>{
		/**
		 * The first object
		 */
		public A a;
		/**
		 * The second object
		 */
		public B b;
		
		/**
		 * Hash key for <i>hashCode()</i>
		 */
		public static final long HK_HC = QuickKeyGen.next64();
		
		/**
		 * Standard constructor
		 * 
		 * @param a first object
		 * @param b second object
		 */
		public O2(A a,B b){
			this.a = a;
			this.b = b;
		}
		
		public String toString(){
			return "("+a+", "+b+")";
		}
		
		public int hashCode(){
			HashTriArx hash = new HashTriArx(HK_HC);
			hash.absorbArray(a,b);
			return hash.squeezeInt();
		}
		
		public boolean equals(Object o){
			if(this==o)return true;
			if(o==null)return false;
			if(!(o instanceof O2<?,?>))return false;
			O2<?,?> other = (O2<?,?>) o;
			return Objects.equals(a, other.a) && Objects.equals(b, other.b);
		}
	}
	
	/**
	 * The same as {@link O2}, except only the key is used for
	 * hashing and equals, allowing a second hidden value to be stored
	 * 
	 * @author EPICI
	 * @version 1.0
	 *
	 * @param <K>
	 * @param <V>
	 */
	public static class Keyed<K,V>{
		/**
		 * The key
		 */
		public K key;
		/**
		 * The value
		 */
		public V value;
		
		/**
		 * Hash key for <i>hashCode()</i>
		 */
		public static final long HK_HC = QuickKeyGen.next64();
		
		/**
		 * Standard constructor
		 * 
		 * @param key the key
		 * @param value the value
		 */
		public Keyed(K key,V value){
			this.key=key;
			this.value=value;
		}
		
		public String toString(){
			return "("+key+":"+value+")";
		}
		
		public int hashCode(){
			HashTriArx hash = new HashTriArx(HK_HC);
			hash.absorbArray(key);
			return hash.squeezeInt();
		}
		
		public boolean equals(Object o){
			if(this==o)return true;
			if(o==null)return false;
			if(!(o instanceof Keyed<?,?>))return false;
			Keyed<?,?> other = (Keyed<?,?>) o;
			return Objects.equals(key, other.key);
		}
	}
	
	/**
	 * Generic 3-object container
	 * 
	 * @author EPICI
	 * @version 1.0
	 *
	 * @param <A> the first type
	 * @param <B> the second type
	 * @param <C> the third type
	 */
	public static class O3<A,B,C>{
		/**
		 * First object
		 */
		public A a;
		/**
		 * Second object
		 */
		public B b;
		/**
		 * Third object
		 */
		public C c;
		
		/**
		 * Hash key for <i>hashCode()</i>
		 */
		public static final long HK_HC = QuickKeyGen.next64();
		
		/**
		 * Standard constructor
		 * 
		 * @param a the first object
		 * @param b the second object
		 * @param c the third object
		 */
		public O3(A a,B b,C c){
			this.a = a;
			this.b = b;
			this.c = c;
		}
		
		public String toString(){
			return "("+a+", "+b+", "+c+")";
		}
		
		public int hashCode(){
			HashTriArx hash = new HashTriArx(HK_HC);
			hash.absorbArray(a,b,c);
			return hash.squeezeInt();
		}
		
		public boolean equals(Object o){
			if(this==o)return true;
			if(o==null)return false;
			if(!(o instanceof O3<?,?,?>))return false;
			O3<?,?,?> other = (O3<?,?,?>) o;
			return Objects.equals(a, other.a) && Objects.equals(b, other.b) && Objects.equals(c, other.c);
		}
	}
}
