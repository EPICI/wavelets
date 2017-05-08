package core;

/**
 * A curve for which the x and y axes can be swapped
 * <br>
 * Some curves are capable of inverted views and other
 * unconventional approaches, so it is generalized to
 * {@link Curve} instead of forcing InvertibleCurve
 * 
 * @author EPICI
 * @version 1.0
 * @param <T> the return type for the transformation
 * @see TransformableCurve
 */
public interface InvertibleCurve<T extends Curve> extends Curve {
	
	/**
	 * Attempt to invert the curve
	 * <br>
	 * Behaviour, including accuracy, exceptions thrown for errors,
	 * and algorithms used depend on implementing classes
	 * 
	 * @return the inverted curve
	 */
	public T invert();
}
