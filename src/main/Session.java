package main;

import utils.*;

/**
 * 
 * @author EPICI
 * @version 1.0
 */
public class Session {
	
	/**
	 * Clipboard, for copypasta
	 */
	public BetterClone<?,?> clipBoard;
	
	protected int bufferSizeShift = 6;
	protected int bufferSize = 1<<bufferSizeShift;
	
	/**
	 * Default constructor, should try to find user preferences on its own
	 */
	public Session(){
		
	}
	
	/**
	 * Gets the buffer size in samples
	 * 
	 * @return the buffer size, a power of 2
	 */
	public int getBufferSize(){
		return bufferSize;
	}
	
	/**
	 * Sets the buffer size in samples
	 * 
	 * @param n the buffer size, a power of 2
	 */
	public void setBufferSize(int n){
		setBufferSizeLog(BitUtils.binLog(n));
	}
	
	/**
	 * Sets the buffer size in samples to 2^n
	 * 
	 * @param n will get set to 2^n
	 */
	public void setBufferSizeLog(int n){
		if(n>=4&&n<=12){
			bufferSizeShift = n;
			bufferSize = 1<<bufferSizeShift;
		}else{
			throw new IllegalArgumentException("n ("+n+") must be between 4 and 12 inclusive");
		}
	}
}
