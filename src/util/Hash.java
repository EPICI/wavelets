package util;

import java.util.*;

/**
 * Utility class containing some hashing utilities
 * <br>
 * Produces higher quality hashes than the standard library
 * methods at next to no extra cost.
 * <br>
 * There is no contract here that hashes for all data types
 * return the same value for the same data. If hashes are needed
 * as a universal unique identifier, remove all ambiguity to avoid
 * method overloading problems.
 * 
 * @author EPICI
 * @version 1.0
 */
public final class Hash {
	
	/**
	 * The starting value used for primitive hashes
	 */
	public static final long PRIM_ROOT = 0x2846e5d24df26373L;
	/**
	 * The multiplier used for primitive hashes
	 */
	public static final long PRIM_MULT = 0x34824da7f9201689L;
	/**
	 * The value substituted for true
	 */
	public static final long BOOL_TRUE = 0x7fa0c8d2acdfa6c9L;
	/**
	 * The value substituted for false
	 */
	public static final long BOOL_FALSE = BOOL_TRUE-32;
	/**
	 * The default starting value used for object hashes
	 */
	public static final long OBJ_ROOT = PRIM_ROOT;
	/**
	 * The default multiplier used for object hashes
	 */
	public static final long OBJ_MULT = PRIM_MULT;
	/**
	 * The value substituted for null
	 */
	public static final long OBJ_NULL = 0x217858a65700db5dL;
	/**
	 * The default starting value used for array hashes
	 */
	public static final long ARRAY_ROOT = PRIM_ROOT+2;
	/**
	 * The default multiplier used for array hashes
	 */
	public static final long ARRAY_MULT = PRIM_MULT+2;
	/**
	 * Shift value used for quick scramble
	 */
	public static final long LONG_SHIFT = 11;
	
	//Disallow invoking constructor
	private Hash(){}
	
	/**
	 * Compress a long into an int with minimal loss of information
	 * <br>
	 * This should be inlined
	 * 
	 * @param single the long to compress
	 * @return an int value which can be considered the hash of the long
	 */
	public static int compress(long single){
		return (int)((single>>32)+single);
	}
	
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
	public static int of(long root,long mult,Object... objects){
		for(Object o:objects){
			root = root*mult+(o==null?OBJ_NULL:o.hashCode());
		}
		return compress(root);
	}
	
	/**
	 * Shortcut for generic hashing
	 * <br>
	 * Slower than direct hashing, but easier to use
	 * <br>
	 * Can hash arrays, unless an array contains itself
	 * <br>
	 * Be careful when passing an array of objects by itself to this method
	 * 
	 * @param objects objects to hash
	 * @return the hash value
	 */
	public static int array(Object... objects){
		if(objects==null)return compress(OBJ_NULL);
		long root = ARRAY_ROOT;
		ArrayDeque<Object> deque = new ArrayDeque<>();
		deque.addLast(objects);
		while(!deque.isEmpty()){
			Object obj = deque.pollFirst();
			if(obj instanceof Object[]){
				for(Object sub:(Object[])obj){
					if(sub==null){
						root=ARRAY_MULT*root+OBJ_NULL;
					}else if(sub.getClass().isArray()){
						deque.addLast(sub);
					}else{
						root=ARRAY_MULT*root+sub.hashCode();
					}
				}
			}else if(obj instanceof long[]){
				root=ARRAY_MULT*root+of((long[])obj);
			}else if(obj instanceof double[]){
				root=ARRAY_MULT*root+of((double[])obj);
			}else if(obj instanceof byte[]){
				root=ARRAY_MULT*root+of((byte[])obj);
			}else if(obj instanceof int[]){
				root=ARRAY_MULT*root+of((int[])obj);
			}else if(obj instanceof char[]){
				root=ARRAY_MULT*root+of((char[])obj);
			}else if(obj instanceof boolean[]){
				root=ARRAY_MULT*root+of((boolean[])obj);
			}else if(obj instanceof short[]){
				root=ARRAY_MULT*root+of((short[])obj);
			}else if(obj instanceof float[]){
				root=ARRAY_MULT*root+of((float[])obj);
			}
			root = (root>>>LONG_SHIFT)|(root<<(64-LONG_SHIFT));
		}
		return compress(root);
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int of(int... prims){
		long root = PRIM_ROOT;
		for(long i:prims)
			root = PRIM_MULT*root+i;
		return compress(root);
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int of(long... prims){
		long root = PRIM_ROOT;
		for(long i:prims)
			root = PRIM_MULT*root+i;
		return compress(root);
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int of(short... prims){
		long root = PRIM_ROOT;
		for(short i:prims)
			root = PRIM_MULT*root+i;
		return compress(root);
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int of(byte... prims){
		long root = PRIM_ROOT;
		for(byte i:prims)
			root = PRIM_MULT*root+i;
		return compress(root);
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int of(boolean... prims){
		long root = PRIM_ROOT;
		for(boolean i:prims)
			root = PRIM_MULT*root+(i?BOOL_TRUE:BOOL_FALSE);
		return compress(root);
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int of(float... prims){
		long root = PRIM_ROOT;
		for(float i:prims)
			root = PRIM_MULT*root+Float.floatToIntBits(i);
		return compress(root);
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static int of(double... prims){
		long root = PRIM_ROOT;
		for(double i:prims)
			root = PRIM_MULT*root+Double.doubleToLongBits(i);
		return compress(root);
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
		long n = prims.length;
		long gap = 0;
		long root = PRIM_ROOT;
		for(int index=0;index<n;index+=++gap){
			int i = prims[index];
			root = PRIM_MULT*root+i;
		}
		return compress(root);
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
		long n = prims.length;
		long gap = 0;
		long root = PRIM_ROOT;
		for(int index=0;index<n;index+=++gap){
			long i = prims[index];
			root = PRIM_MULT*root+i;
		}
		return compress(root);
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
		long n = prims.length;
		long gap = 0;
		long root = PRIM_ROOT;
		for(int index=0;index<n;index+=++gap){
			short i = prims[index];
			root = PRIM_MULT*root+i;
		}
		return compress(root);
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
		long n = prims.length;
		long gap = 0;
		long root = PRIM_ROOT;
		for(int index=0;index<n;index+=++gap){
			byte i = prims[index];
			root = PRIM_MULT*root+i;
		}
		return compress(root);
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
		long n = prims.length;
		long gap = 0;
		long root = PRIM_ROOT;
		for(int index=0;index<n;index+=++gap){
			boolean i = prims[index];
			root = PRIM_MULT*root+(i?BOOL_TRUE:BOOL_FALSE);
		}
		return compress(root);
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
		long n = prims.length;
		long gap = 0;
		long root = PRIM_ROOT;
		for(int index=0;index<n;index+=++gap){
			float i = prims[index];
			root = PRIM_MULT*root+Float.floatToIntBits(i);
		}
		return compress(root);
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
		long n = prims.length;
		long gap = 0;
		long root = PRIM_ROOT;
		for(int index=0;index<n;index+=++gap){
			double i = prims[index];
			root = PRIM_MULT*root+Double.doubleToLongBits(i);
		}
		return compress(root);
	}
}