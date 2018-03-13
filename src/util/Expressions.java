package util;

/**
 * Utility class for dealing with mathematical
 * and similar expressions in text form
 * 
 * @author EPICI
 * @version 1.0
 */
public final class Expressions {
	
	// Disallow invoking constructor
	private Expressions(){}
	
	private static final String[] DEFAULT_BRACKETS_LEFT  = {"(","[","{","<"};
	private static final String[] DEFAULT_BRACKETS_RIGHT = {")","]","}",">"};
	
	/**
	 * If it isn't already in parentheses, put
	 * parentheses around it
	 * 
	 * @param value
	 * @return
	 */
	public static String wrapBrackets(String value){
		return wrapBrackets(value,DEFAULT_BRACKETS_LEFT,DEFAULT_BRACKETS_RIGHT);
	}
	
	/**
	 * If it isn't already in the specified type of
	 * bracket, put those around it
	 * 
	 * @param value
	 * @param left left bracket
	 * @param right right bracket
	 * @return
	 */
	public static String wrapBrackets(String value,String left,String right){
		return wrapBrackets(value,new String[]{left},new String[]{right});
	}
	
	/**
	 * If it isn't already in any of a set of brackets,
	 * put the first around it
	 * <br>
	 * To be specific, if for some index <i>i</i>,
	 * <i>value</i> starts with <i>left[i]</i> and
	 * ends in <i>right[i]</i>, then it is considered to
	 * be bracketed already
	 * 
	 * @param value
	 * @param left
	 * @param right
	 * @return
	 */
	public static String wrapBrackets(String value,String[] left,String[] right){
		if(value==null)return null;
		int n = left.length;
		if(n!=right.length)throw new IllegalArgumentException("length of arrays must match: left ("+n+"), right ("+right.length+")");
		for(int i=0;i<n;i++){
			String ileft = left[i], iright = right[i];
			if(value.startsWith(ileft)&&value.endsWith(iright))return value;
		}
		return left[0]+value+right[0];
	}

}
