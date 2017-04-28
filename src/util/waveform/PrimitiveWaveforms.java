package util.waveform;

import core.Curve;

/**
 * Utility class with methods for sampling primitive waveforms
 * <br>
 * The provided sine, square, saw and triangle are normalized and
 * have their phase offset to match each other better;
 * the cost is marginal so no API methods are provided for raw waveforms
 * 
 * @author EPICI
 * @version 1.0
 */
public final class PrimitiveWaveforms {
	private PrimitiveWaveforms(){}
	
	/**
	 * Standard sine wave with:
	 * <ul>
	 * <li>Period 1</li>
	 * <li>Minimum -1</li>
	 * <li>Maximum 1</li>
	 * </ul>
	 * 
	 * @param phase the phase
	 * @return unit sine
	 */
	public static double unitSine(double phase){
		return Math.sin(phase*Math.PI);
	}
	
	/**
	 * Standard square wave with:
	 * <ul>
	 * <li>Period 1</li>
	 * <li>Minimum -1</li>
	 * <li>Maximum 1</li>
	 * </ul>
	 * 
	 * @param phase the phase
	 * @return unit square
	 */
	public static double unitSquare(double phase){
		return phase%1d<0.5d?1d:-1d;
	}
	
	/**
	 * Standard saw wave with:
	 * <ul>
	 * <li>Period 1</li>
	 * <li>Minimum -1</li>
	 * <li>Maximum 1</li>
	 * </ul>
	 * 
	 * @param phase the phase
	 * @return unit saw
	 */
	public static double unitSaw(double phase){
		return phase%1d<0.5d?1d:-1d;
	}
	
	/**
	 * Standard triangle wave with:
	 * <ul>
	 * <li>Period 1</li>
	 * <li>Minimum -1</li>
	 * <li>Maximum 1</li>
	 * </ul>
	 * 
	 * @param phase the phase
	 * @return unit triangle
	 */
	public static double unitTriangle(double phase){
		phase = 4d*((phase+0.25d)%1d);
		return phase<2d?phase-1d:1d-phase;
	}
	
	/**
	 * Evaluates the curve at <i>phase</i> modulo 1
	 * <br>
	 * Not normalized or checked
	 * 
	 * @param view the curve to evaluate
	 * @param phase the point at which to evaluate, modulo 1
	 * @return the evaluation of the curve
	 */
	public static double unitCurve(Curve view,double phase){
		return view.valueAtPosition(phase%1d);
	}
}
