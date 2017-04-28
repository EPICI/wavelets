package core;

import java.util.*;
import org.python.core.*;

/**
 * Same thing as {@link Samples} but with some other useful
 * metadata as well as variables scripts can use
 * 
 * @author EPICI
 * @version 1.0
 */
public class MetaSamples extends Samples {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Time to complete one measure in seconds
	 */
	public double speedMult = 1d;
	/**
	 * Current start position
	 */
	public double startPos = 0d;
	/**
	 * Current end position
	 */
	public double endPos = 1d;
	/**
	 * Current length
	 */
	public double length = 1d;
	
	/**
	 * Generic holder scripts can interact with, copied from
	 * parent composition
	 */
	public HashMap<String,Object> vars;
	/**
	 * Same thing but in dict form so Python users can
	 * access it
	 */
	public PyDictionary varDict;
	
	/**
	 * Clean constructor
	 * 
	 * @param samplerate sample rate in Hz
	 * @param sampledata sampled sound
	 */
	public MetaSamples(int samplerate, double[] sampledata) {
		super(samplerate, sampledata);
		vars=new HashMap<>();
		varDict=new PyDictionary();
		updatePyDict();
	}
	/**
	 * Copy constructor
	 * 
	 * @param original {@link Samples} to copy from
	 */
	public MetaSamples(Samples original){
		super(original.sampleRate,original.sampleData);
		spectrumReal = original.spectrumReal;
		spectrumImag = original.spectrumImag;
		vars=new HashMap<>();
		varDict=new PyDictionary();
		updatePyDict();
	}
	
	/**
	 * Incrrement time
	 */
	public void pushToNext(){
		startPos=endPos;
		endPos+=length;
	}
	
	/**
	 * Augmented layer method
	 * <br>
	 * Also copies variables
	 * 
	 * @param toLayer the {@link MetaSamples} object whose sample data will be
	 * layerd on this
	 */
	public void layerOnThisMeta(MetaSamples toLayer){
		super.layerOnThis(toLayer);
		for(String key:toLayer.vars.keySet()){
			vars.put(key, toLayer.vars.get(key));
		}
		updatePyDict();
	}
	
	/**
	 * One way copy to give Python users easy access
	 * <br>
	 * Java objects are wrapped to be usable by Python,
	 * Python objects are copied directly
	 */
	public void updatePyDict(){
		for(String key:vars.keySet()){
			Object value = vars.get(key);
			PyObject toSet;
			if(value instanceof PyObject){
				toSet = (PyObject) value;
			}else{
				toSet = Py.java2py(value);
			}
			varDict.__setitem__(key, toSet);
		}
	}

	/**
	 * Static factory method to create a new blank {@link MetaSamples} object
	 * 
	 * @param samplerate sample rate in Hz
	 * @param count number of samples aka length
	 * @return a new {@link MetaSamples} object
	 */
	public static MetaSamples blankSamples(int samplerate,int count){
		return new MetaSamples(samplerate,new double[count]);
	}
	/**
	 * Static factory method to create a new {@link MetaSamples} object,
	 * copies what it can from the original, except for sample data which
	 * is blank
	 * 
	 * @param original the original object to copy from
	 * @return a new {@link MetaSamples} object
	 */
	public static MetaSamples blankSamplesFrom(MetaSamples original){
		MetaSamples result = new MetaSamples(original);
		result.speedMult=original.speedMult;
		result.startPos=original.startPos;
		result.endPos=original.endPos;
		result.length=original.length;
		result.vars=new HashMap<>(original.vars);
		result.varDict=new PyDictionary();
		result.updatePyDict();
		return result;
	}
}
