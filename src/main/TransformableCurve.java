package main;

//Transformable curve
public interface TransformableCurve<T extends Curve> extends Curve {
	/*
	 * Transformations
	 * Return a new curve object which should mirror as
	 * closely as possible the curve after a transformation
	 * Accuracy depends on implementation, user should be aware
	 */
	
	//(x,y) -> (x+a,y+b)
	public T transformXYTranslate(double dx, double dy);
	
	//(x,y) -> (ax,by)
	public T transformXYScale(double xcoefficient, double ycoefficient);
	
	//(x,y) -> (c/x,y)
	public T transformXReciprocal(double dividend);
	
	//(x,y) -> (b^x,y)
	public T transformXExponent(double base);
	
	//(x,y) -> (logb(x),y)
	public T transformXLogarithm(double base);
	
	//(x,y) -> (x,c/y)
	public T transformYReciprocal(double dividend);
	
	//(x,y) -> (x,b^y)
	public T transformYExponent(double base);
	
	//(x,y) -> (x,logb(y))
	public T transformYLogarithm(double base);
	
	//(x,y) -> (x,y^b)
	public T transformYPower(double exponent);
	
	//(x,y) -> (x,f(y))
	public T transformYFunction(Curve function);
}
