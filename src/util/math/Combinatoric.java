package util.math;

/**
 * Some combinatorics stuff
 * <br>
 * This is covered by Apache Commons Math, but this provides
 * implementations for primitives
 * 
 * @author EPICI
 * @version 1.0
 */
public final class Combinatoric {

	private Combinatoric(){}

	/**
	 * Calculates n choose k in O(n) time
	 * <br>
	 * Breaks for very large n, under 30 is guaranteed safe
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
	 * Breaks for very large n, under 30 is guaranteed safe
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
	 * Breaks for very large n, under 62 is guaranteed safe
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
	 * Breaks for very large n, under 62 is guaranteed safe
	 * 
	 * @param n n
	 * @param k k
	 * @return values for that range
	 */
	public static long[] chooseLongRange(int n,int k){
		int n1 = n+1;
		long product = 1;
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
	 * Not necessarily accurate
	 * <br>
	 * Breaks for very large n, under 1021 is guaranteed safe
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
	 * Not necessarily accurate
	 * <br>
	 * Breaks for very large n, under 1021 is guaranteed safe
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
	 * Adaptive, uses highest precision available without sacrificing speed
	 * <br>
	 * Breaks for very large n, under 1021 is guaranteed safe
	 * 
	 * @param n n
	 * @param k k
	 * @return values for that range
	 */
	public static double[] adaptiveChooseDoubleRange(int n,int k){
		if(n>=62){
			return chooseDoubleRange(n,k);
		}else if(n>=30){
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
	
	public static void main(String[] args){
		int i=1;
		while(chooseIntRange(i,i)[i]==1)i++;
		System.out.println("chooseIntRange breaks at "+i);
		while(chooseLongRange(i,i)[i]==1)i++;
		System.out.println("chooseLongRange breaks at "+i);
		while(Double.isFinite(chooseDoubleRange(i,i)[i]))i++;
		System.out.println("chooseDoubleRange breaks at "+i);
	}
}
