package util;

import java.util.*;

/**
 * Utility class containing some hashing utilities
 * <br>
 * Uses 64-bit modified Rabin fingerprint for performance.
 * Keep the full 64-bit value if you can, otherwise use
 * <i>compress()</i> to make it a 32-bit value compatible with
 * standard Java libraries. The methods that aren't prefixed with
 * <i>l</i> do the compression for you. The starting value is not
 * 0, so if you need a rolling hash you should implement it yourself.
 * <br>
 * There is no contract here that hashes for all data types
 * return the same value for the same data. If hashes are needed
 * as a universal unique identifier, remove all ambiguity to avoid
 * method overloading problems.
 * <br>
 * Legend has it that numbers which are max-length generators for
 * the word length and all lower powers of 2 are best multipliers.
 * So all values here have that property.
 * 
 * @author EPICI
 * @version 1.0
 */
public final class Hash {
	
	/**
	 * The multiplier and starting value used for primitive hashes
	 */
	public static final long PRIM_MULT = 0xaaaaaaaaaaaaaaabL;
	/**
	 * The value substituted for true
	 */
	public static final long BOOL_TRUE = 0x555555555555555bL;
	/**
	 * The value substituted for false
	 */
	public static final long BOOL_FALSE = 0x5555555555555553L;
	/**
	 * The default multiplier and starting value used for object hashes
	 */
	public static final long OBJ_MULT = PRIM_MULT;
	/**
	 * The value substituted for null
	 */
	public static final long OBJ_NULL = 0x5555555555555533L;
	/**
	 * The default multiplier and starting value used for array hashes
	 */
	public static final long ARRAY_MULT = 0xaaaaaaaaaaaaaaa3L;
	/**
	 * Shift value used for quick scramble
	 */
	public static final int LONG_SHIFT = 23;
	
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
	
	public static int ofobj(Object... objects){
		return compress(lofobj(objects));
	}
	
	/**
	 * Shortcut for generic hashing
	 * <br>
	 * Slower than direct hashing, but easier to use
	 * 
	 * @param objects objects to hash
	 * @return the hash value
	 */
	public static long lofobj(Object... objects){
		return lof(OBJ_MULT,OBJ_MULT,objects);
	}
	
	public static int ofobj(long root,long mult,Object... objects){
		return compress(lof(root,mult,objects));
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
	public static long lof(long root,long mult,Object... objects){
		for(Object o:objects){
			root = root*mult+(o==null?OBJ_NULL:o.hashCode());
		}
		return root;
	}
	
	public static int array(Object... objects){
		return compress(larray(objects));
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
	public static long larray(Object... objects){
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
				root=ARRAY_MULT*root+lof((long[])obj);
			}else if(obj instanceof double[]){
				root=ARRAY_MULT*root+lof((double[])obj);
			}else if(obj instanceof byte[]){
				root=ARRAY_MULT*root+lof((byte[])obj);
			}else if(obj instanceof int[]){
				root=ARRAY_MULT*root+lof((int[])obj);
			}else if(obj instanceof char[]){
				root=ARRAY_MULT*root+lof((char[])obj);
			}else if(obj instanceof boolean[]){
				root=ARRAY_MULT*root+lof((boolean[])obj);
			}else if(obj instanceof short[]){
				root=ARRAY_MULT*root+lof((short[])obj);
			}else if(obj instanceof float[]){
				root=ARRAY_MULT*root+lof((float[])obj);
			}
			root = Long.rotateRight(root, LONG_SHIFT);
		}
		return root;
	}
	
	public static int of(int... prims){
		return compress(lof(prims));
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static long lof(int... prims){
		long root = PRIM_MULT;
		for(long i:prims)
			root = PRIM_MULT*root+i;
		return root;
	}
	
	public static int of(long... prims){
		return compress(lof(prims));
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static long lof(long... prims){
		long root = PRIM_MULT;
		for(long i:prims)
			root = Long.rotateRight(PRIM_MULT*root+i,LONG_SHIFT);
		return root;
	}
	
	public static int of(short... prims){
		return compress(lof(prims));
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static long lof(short... prims){
		long root = PRIM_MULT;
		for(short i:prims)
			root = PRIM_MULT*root+i;
		return root;
	}
	
	public static int of(byte... prims){
		return compress(lof(prims));
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static long lof(byte... prims){
		long root = PRIM_MULT;
		for(byte i:prims)
			root = PRIM_MULT*root+i;
		return root;
	}
	
	public static int of(boolean... prims){
		return compress(lof(prims));
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static long lof(boolean... prims){
		long root = PRIM_MULT;
		for(boolean i:prims)
			root = PRIM_MULT*root+(i?BOOL_TRUE:BOOL_FALSE);
		return root;
	}
	
	public static int of(char... prims){
		return compress(lof(prims));
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static long lof(char... prims){
		long root = PRIM_MULT;
		for(char i:prims)
			root = PRIM_MULT*root+i;
		return root;
	}
	
	public static int of(float... prims){
		return compress(lof(prims));
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static long lof(float... prims){
		long root = PRIM_MULT;
		for(float i:prims)
			root = PRIM_MULT*root+Float.floatToIntBits(i);
		return root;
	}
	
	public static int of(double... prims){
		return compress(lof(prims));
	}
	
	/**
	 * Quick hash for primitives
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static long lof(double... prims){
		long root = PRIM_MULT;
		for(double i:prims)
			root = PRIM_MULT*root+Double.doubleToLongBits(i);
		return root;
	}
	
	public static int lazy(int[] prims){
		return compress(llazy(prims));
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers up to sqrt(2N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static long llazy(int[] prims){
		long n = prims.length;
		long gap = 0;
		long root = PRIM_MULT+n;
		for(int index=0;index<n;index+=++gap){
			int i = prims[index];
			root = PRIM_MULT*root+i;
		}
		return root;
	}
	
	public static int lazy(long[] prims){
		return compress(llazy(prims));
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers up to sqrt(2N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static long llazy(long[] prims){
		long n = prims.length;
		long gap = 0;
		long root = PRIM_MULT+n;
		for(int index=0;index<n;index+=++gap){
			long i = prims[index];
			root = Long.rotateRight(PRIM_MULT*root+i,LONG_SHIFT);
		}
		return root;
	}
	
	public static int lazy(short[] prims){
		return compress(llazy(prims));
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers up to sqrt(2N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static long llazy(short[] prims){
		long n = prims.length;
		long gap = 0;
		long root = PRIM_MULT+n;
		for(int index=0;index<n;index+=++gap){
			short i = prims[index];
			root = PRIM_MULT*root+i;
		}
		return root;
	}
	
	public static int lazy(byte[] prims){
		return compress(llazy(prims));
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers up to sqrt(2N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static long llazy(byte[] prims){
		long n = prims.length;
		long gap = 0;
		long root = PRIM_MULT+n;
		for(int index=0;index<n;index+=++gap){
			byte i = prims[index];
			root = PRIM_MULT*root+i;
		}
		return root;
	}
	
	public static int lazy(boolean[] prims){
		return compress(llazy(prims));
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers up to sqrt(2N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static long llazy(boolean[] prims){
		long n = prims.length;
		long gap = 0;
		long root = PRIM_MULT+n;
		for(int index=0;index<n;index+=++gap){
			boolean i = prims[index];
			root = PRIM_MULT*root+(i?BOOL_TRUE:BOOL_FALSE);
		}
		return root;
	}
	
	public static int lazy(char[] prims){
		return compress(llazy(prims));
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers up to sqrt(2N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static long llazy(char[] prims){
		long n = prims.length;
		long gap = 0;
		long root = PRIM_MULT+n;
		for(int index=0;index<n;index+=++gap){
			char i = prims[index];
			root = PRIM_MULT*root+i;
		}
		return root;
	}
	
	public static int lazy(float[] prims){
		return compress(llazy(prims));
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers up to sqrt(2N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static long llazy(float[] prims){
		long n = prims.length;
		long gap = 0;
		long root = PRIM_MULT+n;
		for(int index=0;index<n;index+=++gap){
			float i = prims[index];
			root = PRIM_MULT*root+Float.floatToIntBits(i);
		}
		return root;
	}
	
	public static int lazy(double[] prims){
		return compress(llazy(prims));
	}
	
	/**
	 * Gapped hash for primitives
	 * <br>
	 * Only considers up to sqrt(2N) values
	 * 
	 * @param prims primitives to hash
	 * @return a generic hash value for those primitives
	 */
	public static long llazy(double[] prims){
		long n = prims.length;
		long gap = 0;
		long root = PRIM_MULT+n;
		for(int index=0;index<n;index+=++gap){
			double i = prims[index];
			root = Long.rotateRight(PRIM_MULT*root+Double.doubleToLongBits(i),LONG_SHIFT);
		}
		return root;
	}
}