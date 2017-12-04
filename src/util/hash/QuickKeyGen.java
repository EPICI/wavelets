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
	 * The internal state, 4x64=256 bits
	 */
	private static long
		a = System.currentTimeMillis(),
		b = System.nanoTime(),
		c = System.identityHashCode(System.class),
		d = 1;
	
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
		a += System.nanoTime();
		mix();
		return a;
	}
	
	/**
	 * Mix the state
	 * <br>
	 * Originally using ChaCha20/12 but I decided that was
	 * needlessly complicated and slow for these purposes
	 */
	private static void mix(){
		// Round 1
		b = Long.rotateLeft(mul4(a,b), 1)^0x103;
		c = Long.rotateLeft(mul4(b,c), 3)^0x10b;
		d = Long.rotateLeft(mul4(c,d), 6)^0x113;
		a = Long.rotateLeft(mul4(d,a),10)^0x11b;
		// Round 2
		b = Long.rotateLeft(mul4(a,b),15)^0x1a3;
		c = Long.rotateLeft(mul4(b,c),21)^0x1ab;
		d = Long.rotateLeft(mul4(c,d),28)^0x1b3;
		a = Long.rotateLeft(mul4(d,a),36)^0x1bb;
	}
	
	/**
	 * a+b+4ab
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private static long mul4(long a,long b){
		return (b<<2|1)*a+b;
	}

}
