package main;

//Class which provides basic waveforms and some other audio related functionality
public class Waveform {
	
	public final static double twopi = Math.PI*2;
	
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

	public static void main(String[] args) {
		//Leave empty
	}

}
