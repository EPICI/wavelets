package main;

/**
 * A curve that can be transformed
 * <br>
 * For each, return a new curve object which should mirror as
 * closely as possible the curve after a transformation
 * <br>
 * Accuracy depends on implementation, user should be aware
 * and know what objects they are dealing with
 * 
 * @author EPICI
 * @param <T> the return type for all methods
 */
public interface TransformableCurve<T extends Curve> extends Curve {
	
	/**
	 * (x, y) &#x2192; (x+a, y+b)
	 * 
	 * @param dx a
	 * @param dy b
	 * @return a new T object reflecting the transformation
	 */
	public T transformXYTranslate(double dx, double dy);
	
	/**
	 * (x, y) &#x2192; (ax, by)
	 * 
	 * @param xcoefficient a
	 * @param ycoefficient b
	 * @return a new T object reflecting the transformation
	 */
	public T transformXYScale(double xcoefficient, double ycoefficient);
	
	/**
	 * (x, y) &#x2192; (c/x, y)
	 * 
	 * @param dividend c
	 * @return a new T object reflecting the transformation
	 */
	public T transformXReciprocal(double dividend);
	
	/**
	 * (x, y) &#x2192; (b^x, y)
	 * 
	 * @param base b
	 * @return a new T object reflecting the transformation
	 */
	public T transformXExponent(double base);
	
	/**
	 * (x, y) &#x2192; (log_b(x), y)
	 * 
	 * @param base b
	 * @return a new T object reflecting the transformation
	 */
	public T transformXLogarithm(double base);
	
	/**
	 * (x, y) &#x2192; (x, c/y)
	 * 
	 * @param dividend c
	 * @return a new T object reflecting the transformation
	 */
	public T transformYReciprocal(double dividend);
	
	/**
	 * (x, y) &#x2192; (x, b^y)
	 * 
	 * @param base b
	 * @return a new T object reflecting the transformation
	 */
	public T transformYExponent(double base);
	
	/**
	 * (x, y) &#x2192; (x, log_b(y))
	 * 
	 * @param base b
	 * @return a new T object reflecting the transformation
	 */
	public T transformYLogarithm(double base);
	
	/**
	 * (x, y) &#x2192; (x, y^c)
	 * 
	 * @param exponent c
	 * @return a new T object reflecting the transformation
	 */
	public T transformYPower(double exponent);
	
	/**
	 * (x, y) &#x2192; (x, f(y))
	 * 
	 * @param function f
	 * @return a new T object reflecting the transformation
	 */
	public T transformYFunction(Curve function);
}
