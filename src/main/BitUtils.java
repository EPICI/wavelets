package main;

//Utility class
public final class BitUtils {
	//Disallow invoking constructor
	private BitUtils(){}
	
	/*
	 * Fast log base 2 for integers
	 * Taken from http://stackoverflow.com/questions/3305059/how-do-you-calculate-log-base-2-in-java-for-integers#3305710
	 * Returns 0 for 0
	 */
	public static int binLog(int bits){
		int log = 0;
		if( ( bits & 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
		if( bits >= 256 ) { bits >>>= 8; log += 8; }
		if( bits >= 16  ) { bits >>>= 4; log += 4; }
		if( bits >= 4   ) { bits >>>= 2; log += 2; }
		return log + ( bits >>> 1 );
	}
	
	/*
	 * Fast power of 2 test
	 */
	public static boolean isPo2(int bits){
		return (bits&(bits-1))==0;
	}
	
	public static void main(String[] args){
		//Log base 2 sanity tests
		System.out.println("log2(1024)="+binLog(1024)+" (expected 10)");
		System.out.println("log2(3000)="+binLog(3000)+" (expected 11)");
		System.out.println("log2(14)="+binLog(14)+" (expected 3)");
		System.out.println("log2(73902)="+binLog(73902)+" (expected 16)");
		//Power of 2 sanity tests
		System.out.println("ispo2(65535)="+isPo2(65535)+" (expected false)");
		System.out.println("ispo2(65536)="+isPo2(65536)+" (expected true)");
		System.out.println("ispo2(65537)="+isPo2(65537)+" (expected false)");
		System.out.println("ispo2(3)="+isPo2(3)+" (expected false)");
		System.out.println("ispo2(4)="+isPo2(4)+" (expected true)");
	}
}
