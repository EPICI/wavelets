package main;

import java.util.Arrays;

import javax.swing.JTextField;

//Wavelets generic utility class
public final class WaveUtils {
	
	public final static double twopi = Math.PI*2;
	public final static double epsilon = 1e-6;
	public static Curve testHarmonic;
	
	//Disallow invoking constructor
	private WaveUtils(){}
	
	public static void init(){
		testHarmonic = new Curve();
		testHarmonic.addPoint(0d, 1d);
		testHarmonic.addPoint(3d, -1d);
		testHarmonic.addPoint(6d, 0d);
	}
	
	//Frequency 1 sine
	public static double sinf(double x){
		return Math.sin(twopi*x);
	}
	
	//Frequency 1 square
	public static double squf(double x){
		if(x%1<0.5){
			return 1;
		}else{
			return -1;
		}
	}
	
	//Frequency 1 saw
	public static double sawf(double x){
		return 2*((x+0.5)%1)-1;
	}
	
	//Frequency 1 triangle
	public static double trif(double x){
		double phase = (x+0.25)%1;
		double base = sawf(2*(phase));
		if(phase<0.5){
			return base;
		}else{
			return -base;
		}
	}
	
	//Use predetermined curve
	public static double curf(double x, Curve curve){
		return curve.valueAtPos(x%1);
	}
	
	//Harmonics
	public static double harmonic(double baseFrequency,double increment,double correction,double autoFixBase,double autoFixExponent,double iterationCap,double frequencyCap,double time,Curve control){
		double result = 0;
		double currentFreq = baseFrequency;
		double count = 1;
		while(count<iterationCap&&currentFreq<frequencyCap){
			double corrected = count*(1+(increment-1)*correction);
			result+=sinf(time*currentFreq)*control.valueAtPos(corrected)/Math.pow(corrected, autoFixExponent)*Math.pow(autoFixBase, corrected);
			count++;
			currentFreq += increment*baseFrequency;
		}
		return result;
	}
	
	//Average absolute values in double array, recursion means high accuracy
	public static double absAvg(double[] inputArray,int divisions,int cap){
		double result = 0;
		int count = inputArray.length;
		if(count>cap){
			double[][] parts = new double[count][];
			for(int i=0;i<divisions;i++){
				int start = (count*i)/divisions;
				int end = (count*(i+1))/divisions;
				int length = end-start;
				parts[i]=new double[length];
				for(int j=0;j<length;j++){
					parts[i][j] = inputArray[j+start];
				}
				result += absAvg(parts[i],divisions,cap);
			}
			result /= divisions;
		}else{
			for(double i:inputArray){
				result += Math.abs(i);
			}
			result /= count;
		}
		//System.out.println(result);
		return result;
	}
	
	//Quick double[] to short[] with auto amplitude adjustment
	public static short[] quickShort(double[] doubleArray){
		return targetShort(doubleArray,0.25d);
	}
	
	//double[] to short[] with specified target
	public static short[] targetShort(double[] doubleArray,double target){
		double avgAmplitude = absAvg(doubleArray, 128, 65536);//Need more testing to determine if optimal
		double multiplier = 32768d*target/avgAmplitude;
		short[] shortArray = new short[doubleArray.length];
		for(int i=0;i<shortArray.length;i++){
			double value = doubleArray[i]*multiplier;
			if(!Double.isFinite(value)){
				value=0;
			}
			if(value>32767d){
				value=32767d;
			}else if(value<-32768d){
				value=-32768d;
			}
			shortArray[i] = (short) value;
		}
		return shortArray;
	}
	
	//double[] with target
	public static double[] targetDouble(double[] inputArray,double target){
		double avgAmplitude = absAvg(inputArray, 128, 65536);//Need more testing to determine if optimal
		double multiplier = target/avgAmplitude;
		double[] doubleArray = new double[inputArray.length];
		for(int i=0;i<doubleArray.length;i++){
			double value = inputArray[i]*multiplier;
			if(!Double.isFinite(value)){
				value=0;
			}
			if(value>1d){
				value=1d;
			}else if(value<-1d){
				value=-1d;
			}
			doubleArray[i] = value;
		}
		return doubleArray;
	}
	
	public static double[] testTone(double length,double sampleRate){
		int total = (int) (length*sampleRate);
		double[] result = new double[total];
		for(int i=0;i<total;i++){
			result[i] = harmonic(440d,1d,1d,1d,1d,7d,20000d,i/sampleRate,testHarmonic);
		}
		return result;
	}
	
	public static double arrayInterp(double[] inputArray,double index){
		int cap = inputArray.length-1;
		if(index<=0){
			return inputArray[0];
		}else if(index>=cap){
			return inputArray[cap];
		}else{
			int first = (int) Math.floor(index);
			double offset = index-first;
			return offset*inputArray[first+1]+(1-offset)*inputArray[first];
		}
	}
	
	//Place double as text in field
	public static void placeDoubleTextInField(JTextField inField,int length,double toPlace){
		char[] valArray = Double.toString(toPlace).toCharArray();
		inField.setText(new String(Arrays.copyOf(valArray,Math.min(valArray.length, length))));
	}
	
	//Read double from text in field
	public static double readDoubleFromField(JTextField inField,double defaultValue){
		try{
			return Double.valueOf(inField.getText());
		}catch(Exception e){
			return defaultValue;
		}
	}
	
	public static boolean isNear(double a, double b, double threshold){
		return Math.abs(a-b)<threshold;
	}
	
	public static boolean isNear(double a, double b){
		return Math.abs(a-b)<epsilon;
	}
	
	public static double correctRound(double n, double scale, double threshold){
		double scaled = n*scale;
		double rounded = Math.round(scaled);
		if(isNear(scaled,rounded,threshold)){
			return rounded/scale;
		}else{
			return n;
		}
	}
	
	public static double correctRound(double n, double scale){
		double scaled = n*scale;
		double rounded = Math.round(scaled);
		if(isNear(scaled,rounded)){
			return rounded/scale;
		}else{
			return n;
		}
	}
	
	public static double correctRound(double n){
		double rounded = Math.round(n);
		if(isNear(n,rounded)){
			return rounded;
		}else{
			return n;
		}
	}
	
	public static int quickHash(int multiplier,Object... objects){
		int result = multiplier;
		for(Object object:objects){
			result = multiplier*result+object.hashCode();
		}
		return result;
	}
	
	public static void trySleep(long duration){
		try{
			Thread.sleep(duration);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
	
	public static void trySleepNanos(int nanos){
		try{
			Thread.sleep(0L,nanos);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}

}
