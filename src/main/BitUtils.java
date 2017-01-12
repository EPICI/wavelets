package main;

//Read and write individual bits
public final class BitUtils {
	public static final int INT_LEFT_ONE = 1<<31;
	public static final int LONG_LEFT_ONE = 1<<63;
	
	private BitUtils(){}
	
	public static int write(int encoded,int index,boolean bit){
		int bits = INT_LEFT_ONE >>> index;
		if(bit){
			return encoded | bits;
		}else{
			return encoded & (~bits);
		}
	}
	
	public static boolean read(int encoded,int index){
		return (encoded << index) < 0;
	}
	
	public static long write(long encoded,int index,boolean bit){
		int bits = LONG_LEFT_ONE >>> index;
		if(bit){
			return encoded | bits;
		}else{
			return encoded & (~bits);
		}
	}
	
	public static boolean read(long encoded,int index){
		return (encoded << index) < 0;
	}
	
	public static int oneAtInt(int index){
		return INT_LEFT_ONE >>> index;
	}
	
	public static long oneAtLong(int index){
		return LONG_LEFT_ONE >>> index;
	}
}
