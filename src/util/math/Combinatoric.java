package util.math;

public final class Combinatoric {

	private Combinatoric(){}
	

	/**
	 * Calculates n choose k in O(n) time
	 * <br>
	 * Breaks for very large n, under 29 is guaranteed safe
	 * 
	 * @param n n
	 * @param k k
	 * @return n choose k
	 */
	public static int chooseInt(int n,int k){
		int n1 = n+1;
		int product = 1;
		for(int i=1;i<=k;i++){
			product = product * (n1 - i) / i;
		}
		return product;
	}
	
	/**
	 * Calculates n choose p for 0 &#x2264; p &#x2264; k
	 * <br>
	 * Breaks for very large n, under 29 is guaranteed safe
	 * 
	 * @param n n
	 * @param k k
	 * @return values for that range
	 */
	public static int[] chooseIntRange(int n,int k){
		int n1 = n+1;
		int product = 1;
		int[] result = new int[k+1];
		result[0] = 1;
		for(int i=1;i<=k;i++){
			product = product*(n1-i)/i;
			result[i] = product;
		}
		return result;
	}
	
	/**
	 * Calculates n choose k in O(n) time
	 * <br>
	 * Breaks for very large n, under 60 is guaranteed safe
	 * 
	 * @param n n
	 * @param k k
	 * @return n choose k
	 */
	public static long chooseLong(int n,int k){
		int n1 = n+1;
		long product = 1;
		for(int i=1;i<=k;i++){
			product = product * (n1 - i) / i;
		}
		return product;
	}
	
	/**
	 * Calculates n choose p for 0 &#x2264; p &#x2264; k
	 * <br>
	 * Breaks for very large n, under 60 is guaranteed safe
	 * 
	 * @param n n
	 * @param k k
	 * @return values for that range
	 */
	public static long[] chooseLongRange(int n,int k){
		int n1 = n+1;
		int product = 1;
		long[] result = new long[k+1];
		result[0] = 1;
		for(int i=1;i<=k;i++){
			product = product*(n1-i)/i;
			result[i] = product;
		}
		return result;
	}
	
	/**
	 * Calculates n choose k in O(n) time
	 * <br>
	 * Always safe, but not necessarily accurate
	 * 
	 * @param n n
	 * @param k k
	 * @return n choose k
	 */
	public static double chooseDouble(int n,int k){
		int n1 = n+1;
		double product = 1d;
		for(int i=1;i<=k;i++){
			product = (product * (n1-i)) / i;
		}
		return product;
	}
	
	/**
	 * Calculates n choose p for 0 &#x2264; p &#x2264; k
	 * <br>
	 * Always safe, but not necessarily accurate
	 * 
	 * @param n n
	 * @param k k
	 * @return values for that range
	 */
	public static double[] chooseDoubleRange(int n,int k){
		int n1 = n+1;
		double product = 1d;
		double[] result = new double[k+1];
		result[0] = 1;
		for(int i=1;i<=k;i++){
			product = (product * (n1-i)) / i;
			result[i] = product;
		}
		return result;
	}
	
	/**
	 * Calculates n choose p for 0 &#x2264; p &#x2264; k
	 * <br>
	 * Adaptive
	 * 
	 * @param n n
	 * @param k k
	 * @return values for that range
	 */
	public static double[] adaptiveChooseDoubleRange(int n,int k){
		if(n>=60){
			return chooseDoubleRange(n,k);
		}else if(n>=29){
			long[] pre = chooseLongRange(n,k);
			double[] result = new double[pre.length];
			for(int i=0;i<pre.length;i++){
				result[i]=pre[i];
			}
			return result;
		}else if(n>0){
			int[] pre = chooseIntRange(n,k);
			double[] result = new double[pre.length];
			for(int i=0;i<pre.length;i++){
				result[i]=pre[i];
			}
			return result;
		}else{
			throw new IllegalArgumentException("n ("+n+") must be 1 or greater");
		}
	}
}
