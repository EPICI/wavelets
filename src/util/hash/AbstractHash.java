package util.hash;

import java.util.ArrayDeque;
import java.util.Objects;

/**
 * An object used to make hashes. More versatile than
 * a bunch of static methods at barely any extra cost.
 * Optimized for 64-bit implementations.
 * <br>
 * The specifications here are quite loose. It is
 * up to the implementation to decide what features to provide.
 * <br>
 * Base methods are prefixed with <i>i</i> to differentiate them.
 * Object methods are suffixed with <i>obj</i>, array methods
 * with <i>array</i>, and skipping methods with <i>skip</i>.
 * <br>
 * A sticky all 0s state (or other sticky state) is fine as long
 * as it is unlikely to occur accidentally
 * 
 * @author EPICI
 * @version 1.0
 */
public abstract class AbstractHash {
	
	/**
	 * Absorbs a single <i>long</i> into the state.
	 * 
	 * @param v up to 64 bits of data as a long
	 */
	public abstract void iabsorb(long v);
	
	/**
	 * Squeeze a single <i>long</i> from the state.
	 * <br>
	 * Generally this will be called once at the end,
	 * so we can shave off a bit of time by not
	 * permuting the state again. Subsequent calls do
	 * not even need to be unique unless the implementer
	 * decides so.
	 * 
	 * @return 64 bits of hash as a long
	 */
	public abstract long isqueeze();
	
	/*
	 * Lots of pre-implemented convenience methods here
	 * 
	 * It's a hash anyways so we don't bother standardizing
	 * endianness or fixing sign extensions
	 */
	
	/**
	 * Squeeze a single <i>int</i> from the state.
	 * 
	 * @return 32 bits of hash as int
	 */
	public int squeezeInt(){
		long r = isqueeze();
		return (int)((r>>32)^r);
	}
	
	public void absorb(long... vs){
		for(long v:vs)iabsorb(v);
	}
	
	public void absorbSkip(long[] vs){
		int n = vs.length;
		int gap = 0;
		for(int i=0;i<n;i+=++gap){
			iabsorb(vs[i]);
		}
	}
	
	public void absorb(double v){
		iabsorb(Double.doubleToLongBits(v));
	}
	
	public void absorb(double... vs){
		for(double v:vs)iabsorb(Double.doubleToLongBits(v));
	}
	
	public void absorbSkip(double[] vs){
		int n = vs.length;
		int gap = 0;
		for(int i=0;i<n;i+=++gap){
			iabsorb(Double.doubleToLongBits(vs[i]));
		}
	}
	
	public void absorb(int v){
		iabsorb(v);
	}
	
	public void absorb(int... vs){
		int n = vs.length, m = n&~1, u, v;
		for(int i=0;i<m;i+=2){
			u = vs[i];
			v = vs[i+1];
			iabsorb(u|(long)v<<32);
		}
		if(n!=m)iabsorb(vs[m]);
	}
	
	public void absorb(float v){
		iabsorb(Float.floatToIntBits(v));
	}
	
	public void absorb(float... vs){
		int n = vs.length, m = n&~1, u, v;
		for(int i=0;i<m;i+=2){
			u = Float.floatToIntBits(vs[i]);
			v = Float.floatToIntBits(vs[i+1]);
			iabsorb(u|(long)v<<32);
		}
		if(n!=m)iabsorb(Float.floatToIntBits(vs[m]));
	}
	
	public void absorb(byte v){
		iabsorb(v);
	}
	
	public void absorb(byte... vs){
		int n = vs.length, m = n&~7, o = n&~3, u, v;
		for(int i=0;i<m;i+=8){
			u =     vs[i  ]    |
					vs[i+1]<< 8|
					vs[i+2]<<16|
					vs[i+3]<<24;
			v =     vs[i+4]    |
					vs[i+5]<< 8|
					vs[i+6]<<16|
					vs[i+7]<<24;
			iabsorb(u|(long)v<<32);
		}
		if(o==m){//1-3 bytes remaining
			u = 0;
			for(int i=o;i<n;i++)u |= vs[i]<<((i-o)<<3);
			iabsorb(u);
		}else{//4-7 bytes remaining
			u =     vs[m]      |
					vs[m+1]<< 8|
					vs[m+2]<<16|
					vs[m+3]<<24;
			v = 0;
			for(int i=o;i<n;i++)v |= vs[i]<<((i-o)<<3);
			iabsorb(u|(long)v<<32);
		}
	}
	
	public void absorb(short v){
		iabsorb(v);
	}
	
	public void absorb(short... vs){
		int n = vs.length, m = n&~3, o = n&~1, u, v;
		for(int i=0;i<m;i+=4){
			u =     vs[i  ]    |
					vs[i+1]<<16;
			v =     vs[i+2]    |
					vs[i+3]<<16;
			iabsorb(u|(long)v<<32);
		}
		if(o==m){//1 short remaining
			iabsorb(vs[o]);
		}else{//2-3 shorts remaining
			u =     vs[m]      |
					vs[m+1]<<16;
			v = o==n?0:vs[o];
			iabsorb(u|(long)v<<32);
		}
	}
	
	public void absorb(char v){
		iabsorb(v);
	}
	
	public void absorb(char... vs){
		int n = vs.length, m = n&~3, o = n&~1, u, v;
		for(int i=0;i<m;i+=4){
			u =     vs[i  ]    |
					vs[i+1]<<16;
			v =     vs[i+2]    |
					vs[i+3]<<16;
			iabsorb(u|(long)v<<32);
		}
		if(o==m){//1 short remaining
			iabsorb(vs[o]);
		}else{//2-3 shorts remaining
			u =     vs[m]      |
					vs[m+1]<<16;
			v = o==n?0:vs[o];
			iabsorb(u|(long)v<<32);
		}
	}
	
	public void absorb(boolean v){
		iabsorb(v?1:0);
	}
	
	public void absorb(boolean... vs){
		int n = vs.length, m = n&~63;
		long v = 0;
		for(int i=0;i<n;i++){
			int j=i&63;
			v = v|(vs[i]?1:0)<<j;
			if(j==63){
				iabsorb(v);
				v=0;
			}
		}
		if(n!=m)iabsorb(v);
	}
	
	public void absorbObj(Object v){
		iabsorb(Objects.hashCode(v));
	}
	
	public void absorbObj(Object... vs){
		int n = vs.length, m = n&~1, u, v;
		for(int i=0;i<m;i+=2){
			u = Objects.hashCode(vs[i]);
			v = Objects.hashCode(vs[i+1]);
			iabsorb(u|(long)v<<32);
		}
		if(n!=m)iabsorb(Objects.hashCode(vs[m]));
	}
	
	public void absorbArray(Object... objects){
		ArrayDeque<Object> deque = new ArrayDeque<>();
		deque.addLast(objects);
		while(!deque.isEmpty()){
			Object obj = deque.pollFirst();
			if(obj instanceof Object[]){
				for(Object sub:(Object[])obj){
					if(sub==null){
						iabsorb(0);
					}else if(sub.getClass().isArray()){
						deque.addLast(sub);
					}else{
						iabsorb(sub.hashCode());
					}
				}
			}else if(obj instanceof long[]){
				absorb((long[])obj);
			}else if(obj instanceof double[]){
				absorb((double[])obj);
			}else if(obj instanceof byte[]){
				absorb((byte[])obj);
			}else if(obj instanceof int[]){
				absorb((int[])obj);
			}else if(obj instanceof char[]){
				absorb((char[])obj);
			}else if(obj instanceof boolean[]){
				absorb((boolean[])obj);
			}else if(obj instanceof short[]){
				absorb((short[])obj);
			}else if(obj instanceof float[]){
				absorb((float[])obj);
			}
			iabsorb(-1);
		}
	}
}
