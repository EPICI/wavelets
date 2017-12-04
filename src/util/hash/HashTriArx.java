package util.hash;

import java.nio.charset.StandardCharsets;

/**
 * A hash designed to outperform the others while
 * still being decent as a hash.
 * <br>
 * The <i>Tri</i> in the name comes from it using
 * 3 64-bit words of internal state. The <i>Arx</i>
 * is ARX, which means addition, rotate and xor. It
 * is able to make use of instruction level parallelism
 * since each operation does not rely on the result of
 * the previous, but rather the one before.
 * <br>
 * The squeeze method absorbs a constant before returning
 * the first word of state put through a mixing function.
 * This allows for a pseudorandom output stream. The mixing
 * step is necessary because differences take several iterations
 * to propagate with this construction.
 * 
 * @author EPICI
 * @version 1.0
 */
public class HashTriArx extends AbstractHash {
	
	/**
	 * The state, composed of 3 64-bit words
	 */
	public long x,y,z;
	
	/**
	 * Default constructor
	 */
	public HashTriArx(){
		/*
		 * Prime roots make good constants. They're
		 * simple to describe, easily verifiable,
		 * and are uniform.
		 */
		x = 0x428a2f98d728ae22L;// from 2^(1/3)
		y = 0x7137449123ef65cdL;// from 3^(1/3)
		z = 0x965fea53d6e3c82bL;// from 4^(1/3)
	}
	
	/**
	 * A keyed constructor
	 * <br>
	 * Keys are meant as uniquifiers only
	 * 
	 * @param key up to 64 bits of data to make this hash unique
	 */
	public HashTriArx(long key){
		/*
		 * Why make x the key?
		 * Absorbed data goes to z -> x -> y
		 * If it was z, the first long absorbed could nullify it.
		 * If it was y, it could weaken the hash since y is the
		 * most "difficult" to reach from absorbed data.
		 * At x, it affects y before it can be reached, so we
		 * get defense against clever users and stupid developers.
		 */
		x = key;
		y = 0xd12ed0af1a27ef3dL;// from 6^(1/3)
		z = 0xe9b5dba58189dbbcL;// from 7^(1/3)
	}

	@Override
	public void iabsorb(long v) {
		/*
		 * Why 29?
		 * We use xor, which is linear (and is its own inverse), as well
		 * as add, which is only weakly nonlinear. As such we should try
		 * to avoid letting it overlap with itself from a previous iteration.
		 * So we choose a shift value n for which n/64 if far from a rational
		 * with a smaller denominator. 29 fits. There are other candidates like
		 * 23 and 5, be we go with 29 because 29/64 is closer to 1/2 and that
		 * minimizes the interference from carries.
		 * 
		 * But really, it might as well be an arbitrary constant. It's not crypto.
		 */
		x = Long.rotateLeft(x, 29);
		z ^= v;
		y += x;
		x += z;
		z ^= y;
	}

	@Override
	public long isqueeze() {
		/*
		 * The more uniform the constant, the faster
		 * the state can become uniform and random-looking.
		 * At best it takes 2 iterations to recover from an
		 * all 0 state, though it only takes 1 iteration to
		 * affect x, which is what we output, so that's okay.
		 */
		iabsorb(0xb5c0fbcfec4d3b2fL);// from 5^(1/3)
		return prp64to64(x);
	}
	
	/**
	 * Bijective mapping from 64 bit to 64 bit, used
	 * by the squeeze method
	 * <br>
	 * This should not be used elsewhere, since it is designed
	 * specifically to work well with this hash
	 * 
	 * @param x 64-bit input value
	 * @return mapped value
	 */
	public static long prp64to64(long x){
		/*
		 * Multiply constant is derived from 3^(1/2):
		 * 0xbb67ae8584caa73b
		 * Adding 24 yields a prime:
		 * 0xbb67ae8584caa753
		 * Shift constant 60 is experimentally good, presumably because
		 * it puts some very high bits in the low bits
		 * 
		 * A multiply will take care of the high bits.
		 * Shift and xor will make sure the lowest bit has been affected.
		 * Another multiply takes care of the middle bits.
		 * 
		 * Actually, a single multiply would be good enough
		 * if intended for HashMap because of the extra hash step
		 * it uses, however, we're lazy and use hashes elsewhere
		 * so good mixing is helpful
		 */
		x*=0xbb67ae8584caa753L;
		return ((x>>>60)^x)*0xbb67ae8584caa753L;
	}

}
