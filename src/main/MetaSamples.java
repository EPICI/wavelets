package main;

import java.util.*;

//Contains some extra useful data
public class MetaSamples extends Samples {
	private static final long serialVersionUID = 1L;
	
	public double speedMult = 1d;
	public double startPos = 0d;
	public double endPos = 1d;
	public double length = 1d;
	
	//Generic holder scripts can interact with
	public HashMap<String,Object> vars;
	
	public MetaSamples(int samplerate, double[] sampledata) {
		super(samplerate, sampledata);
	}
	public MetaSamples(Samples original){
		super(original.sampleRate,original.sampleData);
		spectrumReal = original.spectrumReal;
		spectrumImag = original.spectrumImag;
	}
	
	public void pushToNext(){
		startPos=endPos;
		endPos+=length;
	}
	
	//Also copies variables
	public void layerOnThisMeta(MetaSamples toLayer){
		super.layerOnThis(toLayer);
		for(String key:toLayer.vars.keySet()){
			vars.put(key, toLayer.vars.get(key));
		}
	}

	public static MetaSamples blankSamples(int samplerate,int count){
		return new MetaSamples(samplerate,blankArray(count));
	}
	public static MetaSamples blankSamplesFrom(MetaSamples original){
		MetaSamples result = new MetaSamples(original);
		result.speedMult=original.speedMult;
		result.startPos=original.startPos;
		result.endPos=original.endPos;
		result.length=original.length;
		result.vars=new HashMap<>(original.vars);
		return result;
	}
}
