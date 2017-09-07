package util;

import java.util.*;

/**
 * Utility class containing some hashing utilities
 * <br>
 * Uses the Rabin fingerprint hash, with 64 bits instead of the regular
 * 32 bits, then compresses down to 32 using <i>x+(x>>32)</i>. This way,
 * hashes are higher quality than the standard library's at next to
 * no extra cost. The compression forfeits many of the advantages of the
 * Rabin fingerprint.
 * <br>
 * There is no contract here that hashes for all data types
 * return the same value for the same data. If hashes are needed
 * as a universal unique identifier, remove all ambiguity to avoid
 * method overloading problems.
 * <br>
 * All values here are primes, pretty much arbitrarily chosen.
 * The myth that primes give better hashes is true to some extent,
 * but most generators, prime or not, will still produce good hashes,
 * and some rare primes are exceptionally good.
 * 
 * @author EPICI
 * @version 1.0
 */
public final class Hash {
	
	/**
	 * The multiplier and starting value used for primitive hashes
	 */
	public static final long PRIM_MULT = 0xad6b2d692aa9292dL;
	/**
	 * The value substituted for true
	 */
	public static final long BOOL_TRUE = 0xaad6d6d9b2956cadL;
	/**
	 * The value substituted for false
	 */
	public static final long BOOL_FALSE = 0xb4aacb2a5592accbL;
	/**
	 * The default multiplier and starting value used for object hashes
	 */
	public static final long OBJ_MULT = PRIM_MULT;
	/**
	 * The value substituted for null
	 */
	public static final long OBJ_NULL = 0x92a565256a524cadL;
	/**
	 * The default multiplier and starting value used for array hashes
	 */
	public static final long ARRAY_MULT = 0x6ab4a95a94a64d25L;
	/**
	 * Shift value used for quick scramble
	 */
	public static final int LONG_SHIFT = 11;
	
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
		return of(OBJ_MULT,OBJ_MULT,objects);
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
		long root = ARRAY_MULT;
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
			root = Long.rotateRight(root, LONG_SHIFT);
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
		long root = PRIM_MULT;
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
		long root = PRIM_MULT;
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
		long root = PRIM_MULT;
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
		long root = PRIM_MULT;
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
		long root = PRIM_MULT;
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
		long root = PRIM_MULT;
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
		long root = PRIM_MULT;
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
		long root = PRIM_MULT;
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
		long root = PRIM_MULT;
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
		long root = PRIM_MULT;
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
		long root = PRIM_MULT;
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
		long root = PRIM_MULT;
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
		long root = PRIM_MULT;
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
		long root = PRIM_MULT;
		for(int index=0;index<n;index+=++gap){
			double i = prims[index];
			root = PRIM_MULT*root+Double.doubleToLongBits(i);
		}
		return compress(root);
	}
}