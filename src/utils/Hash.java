package utils;

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
	public static final int PRIM_ROOT = 0xadded;
	/**
	 * The multiplier used for primitive hashes
	 */
	public static final int PRIM_MULT = 7;
	/**
	 * The value substituted for true
	 */
	public static final int BOOL_TRUE = 0x5a56f1ef;
	/**
	 * The value substituted for false
	 */
	public static final int BOOL_FALSE = 0x9e4d7c8d;
	/**
	 * The default starting value used for object hashes
	 */
	public static final int OBJ_ROOT = 0xadded;
	/**
	 * The default multiplier used for object hashes
	 */
	public static final int OBJ_MULT = 7;
	/**
	 * For arrays smaller than this, fall back to classic hash
	 */
	public static final int SKIP_THRESHOLD = 1<<6;
	
	//Disallow invoking constructor
	private Hash(){}
	
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
			root = root*mult+o.hashCode();
		}
		return root;
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
			root = (int)(PRIM_MULT*root+(i>>32)+i);
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
	 * Only considers sqrt(N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int lazy(int[] prims){
		int n = prims.length;
		if(n<=SKIP_THRESHOLD)return of(prims);
		int gap = 1 + (int) Math.sqrt(n);
		int root = PRIM_ROOT;
		for(int index=1;index<n;index+=gap){
			int i = prims[index];
			root = PRIM_MULT*root+i;
		}
		return root;
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers sqrt(N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int lazy(long[] prims){
		int n = prims.length;
		if(n<=SKIP_THRESHOLD)return of(prims);
		int gap = 1 + (int) Math.sqrt(n);
		int root = PRIM_ROOT;
		for(int index=1;index<n;index+=gap){
			long i = prims[index];
			root = (int)(PRIM_MULT*root+(i>>32)+i);
		}
		return root;
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers sqrt(N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int lazy(short[] prims){
		int n = prims.length;
		if(n<=SKIP_THRESHOLD)return of(prims);
		int gap = 1 + (int) Math.sqrt(n);
		int root = PRIM_ROOT;
		for(int index=1;index<n;index+=gap){
			short i = prims[index];
			root = PRIM_MULT*root+i;
		}
		return root;
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers sqrt(N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int lazy(byte[] prims){
		int n = prims.length;
		if(n<=SKIP_THRESHOLD)return of(prims);
		int gap = 1 + (int) Math.sqrt(n);
		int root = PRIM_ROOT;
		for(int index=1;index<n;index+=gap){
			byte i = prims[index];
			root = PRIM_MULT*root+i;
		}
		return root;
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers sqrt(N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int lazy(boolean[] prims){
		int n = prims.length;
		if(n<=SKIP_THRESHOLD)return of(prims);
		int gap = 1 + (int) Math.sqrt(n);
		int root = PRIM_ROOT;
		for(int index=1;index<n;index+=gap){
			boolean i = prims[index];
			root = PRIM_MULT*root+(i?BOOL_TRUE:BOOL_FALSE);
		}
		return root;
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers sqrt(N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int lazy(float[] prims){
		int n = prims.length;
		if(n<=SKIP_THRESHOLD)return of(prims);
		int gap = 1 + (int) Math.sqrt(n);
		int root = PRIM_ROOT;
		for(int index=1;index<n;index+=gap){
			float i = prims[index];
			root = PRIM_MULT*root+Float.hashCode(i);
		}
		return root;
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers sqrt(N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int lazy(double[] prims){
		int n = prims.length;
		if(n<=SKIP_THRESHOLD)return of(prims);
		int gap = 1 + (int) Math.sqrt(n);
		int root = PRIM_ROOT;
		for(int index=1;index<n;index+=gap){
			double i = prims[index];
			root = PRIM_MULT*root+Double.hashCode(i);
		}
		return root;
	}
	
}
