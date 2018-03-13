package util.hash;

/**
 * Grab a key for universal hashing purposes here
 * <br>
 * No guarantees on the nature of the algorithm used,
 * but outputs should be unrelated enough to be suitable
 * for universal hashing keys for any hash
 * <br>
 * Not a CSPRNG or good regular PRNG
 * 
 * @author EPICI
 * @version 1.0
 */
public class QuickKeyGen {
	
	private QuickKeyGen(){}
	
	/**
	 * The internal state, 2x64=128 bits
	 */
	private static long
		a = System.currentTimeMillis()+System.identityHashCode(System.class),
		b = System.nanoTime();
	
	/**
	 * Get next 32 bits of random data
	 * 
	 * @return
	 */
	public static int next32(){
		return (int)next64();
	}
	
	/**
	 * Get next 64 bits of random data
	 * 
	 * @return
	 */
	public static long next64(){
		return map(next());
	}
	
	/**
	 * Hide the bitwise nonlinearity by using multiplication
	 * which is very nonlinear and also cheap
	 * 
	 * @param value
	 * @return
	 */
	private static long map(long value){
		return Long.rotateLeft(value*0xaaaaaaaaa95555L, 32)*0x2aaaaaaaaaa55555L;
	}
	
	/**
	 * xoroshiro128+ is good enough
	 * 
	 * @return
	 */
	private static long next(){
		final long result = a+b;
		b ^= a;
		a = Long.rotateLeft(a, 55)^b^(b<<14);
		b = Long.rotateLeft(b, 36);
		return result;
	}

}
