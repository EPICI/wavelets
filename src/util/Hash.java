package util;

/**
 * Utility class containing some hashing utilities
 * 
 * @author EPICI
 * @version 1.0
 */
public final class Hash {
	
	/**
	 * The starting value used for primitive hashes
	 */
	public static final int PRIM_ROOT = 524309;
	/**
	 * The multiplier used for primitive hashes
	 */
	public static final int PRIM_MULT = 7;
	/**
	 * The value substituted for true
	 */
	public static final int BOOL_TRUE = 41;
	/**
	 * The value substituted for false
	 */
	public static final int BOOL_FALSE = 37;
	/**
	 * The default starting value used for object hashes
	 */
	public static final int OBJ_ROOT = 524309;
	/**
	 * The default multiplier used for object hashes
	 */
	public static final int OBJ_MULT = 7;
	/**
	 * The value substituted for null
	 */
	public static final int OBJ_NULL = 0;
	/**
	 * The default starting value used for array hashes
	 */
	public static final int ARRAY_ROOT = 524309;
	/**
	 * The default multiplier used for array hashes
	 */
	public static final int ARRAY_MULT = 17;
	/**
	 * Shift value used for quick scramble
	 */
	public static final int INT_SHIFT = 3;
	
	//Disallow invoking constructor
	private Hash(){}
	
	/**
	 * Shortcut for generic hashing
	 * <br>
	 * Slower than direct hashing, but easier to use
	 * 
	 * @param objects objects to hash
	 * @return the hash value
	 */
	public static int of(Object... objects){
		return of(OBJ_ROOT,OBJ_MULT,objects);
	}
	
	/**
	 * Shortcut for generic hashing
	 * <br>
	 * Slower than direct hashing, but easier to use
	 * 
	 * @param root the starting value
	 * @param mult the base
	 * @param objects objects to hash
	 * @return the hash value
	 */
	public static int of(int root,int mult,Object... objects){
		for(Object o:objects){
			root = root*mult+(o==null?OBJ_NULL:o.hashCode());
		}
		return root;
	}
	
	/**
	 * Shortcut for generic hashing
	 * <br>
	 * Slower than direct hashing, but easier to use
	 * <br>
	 * Can hash arrays
	 * 
	 * @param objects objects to hash
	 * @return the hash value
	 */
	public static int array(Object... objects){
		int root = ARRAY_ROOT;
		for(Object o:objects){
			int add;
			if(o==null)
				add = 0;
			else if(o instanceof Object[])
				add = array((Object[])o);
			else if(o instanceof int[])
				add = of((int[])o);
			else if(o instanceof double[])
				add = of((double[])o);
			else if(o instanceof byte[])
				add = of((byte[])o);
			else if(o instanceof long[])
				add = of((long[])o);
			else if(o instanceof char[])
				add = of((char[])o);
			else if(o instanceof boolean[])
				add = of((boolean[])o);
			else if(o instanceof short[])
				add = of((short[])o);
			else if(o instanceof float[])
				add = of((float[])o);
			else
				add = o.hashCode();
			root = ARRAY_MULT*root+add;
		}
		return (root>>>INT_SHIFT)|(root<<(32-INT_SHIFT));
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int of(int... prims){
		int root = PRIM_ROOT;
		for(int i:prims)
			root = PRIM_MULT*root+i;
		return root;
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int of(long... prims){
		int root = PRIM_ROOT;
		for(long i:prims)
			root = PRIM_MULT*root+Long.hashCode(i);
		return root;
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int of(short... prims){
		int root = PRIM_ROOT;
		for(short i:prims)
			root = PRIM_MULT*root+i;
		return root;
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int of(byte... prims){
		int root = PRIM_ROOT;
		for(byte i:prims)
			root = PRIM_MULT*root+i;
		return root;
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int of(boolean... prims){
		int root = PRIM_ROOT;
		for(boolean i:prims)
			root = PRIM_MULT*root+(i?BOOL_TRUE:BOOL_FALSE);
		return root;
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int of(float... prims){
		int root = PRIM_ROOT;
		for(float i:prims)
			root = PRIM_MULT*root+Float.hashCode(i);
		return root;
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int of(double... prims){
		int root = PRIM_ROOT;
		for(double i:prims)
			root = PRIM_MULT*root+Double.hashCode(i);
		return root;
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers up to sqrt(2N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int lazy(int[] prims){
		int n = prims.length;
		int gap = 0;
		int root = PRIM_ROOT;
		for(int index=0;index<n;index+=++gap){
			int i = prims[index];
			root = PRIM_MULT*root+i;
		}
		return root;
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers up to sqrt(2N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int lazy(long[] prims){
		int n = prims.length;
		int gap = 0;
		int root = PRIM_ROOT;
		for(int index=0;index<n;index+=++gap){
			long i = prims[index];
			root = PRIM_MULT*root+Long.hashCode(i);
		}
		return root;
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers up to sqrt(2N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int lazy(short[] prims){
		int n = prims.length;
		int gap = 0;
		int root = PRIM_ROOT;
		for(int index=0;index<n;index+=++gap){
			short i = prims[index];
			root = PRIM_MULT*root+i;
		}
		return root;
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers up to sqrt(2N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int lazy(byte[] prims){
		int n = prims.length;
		int gap = 0;
		int root = PRIM_ROOT;
		for(int index=0;index<n;index+=++gap){
			byte i = prims[index];
			root = PRIM_MULT*root+i;
		}
		return root;
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers up to sqrt(2N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int lazy(boolean[] prims){
		int n = prims.length;
		int gap = 0;
		int root = PRIM_ROOT;
		for(int index=0;index<n;index+=++gap){
			boolean i = prims[index];
			root = PRIM_MULT*root+(i?BOOL_TRUE:BOOL_FALSE);
		}
		return root;
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers up to sqrt(2N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int lazy(float[] prims){
		int n = prims.length;
		int gap = 0;
		int root = PRIM_ROOT;
		for(int index=0;index<n;index+=++gap){
			float i = prims[index];
			root = PRIM_MULT*root+Float.hashCode(i);
		}
		return root;
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers up to sqrt(2N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int lazy(double[] prims){
		int n = prims.length;
		int gap = 0;
		int root = PRIM_ROOT;
		for(int index=0;index<n;index+=++gap){
			double i = prims[index];
			root = PRIM_MULT*root+Double.hashCode(i);
		}
		return root;
	}
	
}