package utils;

/**
 * For when you want to return multiple different values in a less awkward way
 * <br>
 * Use only with objects since autoboxing is costly
 * <br>
 * These act as generic containers
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
			return a.hashCode()*33+b.hashCode();
		}
	}
	
	/**
	 * Generic 2-object container
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
			return a.hashCode()*127+b.hashCode()*17+c.hashCode();
		}
	}
}
