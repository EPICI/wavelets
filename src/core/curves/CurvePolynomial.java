package core.curves;

import java.util.*;
import core.Curve;
import util.hash.*;

/**
 * Represents a simple curve defined by a polynomial.
 * Coefficients are in little endian order so that the index
 * matches the exponent.
 * 
 * @author EPICI
 * @version 1.0
 */
public class CurvePolynomial implements Curve {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Hash key for <i>hashCode()</i>
	 */
	public static final long HK_HC = QuickKeyGen.next64();
	
	/**
	 * List of coefficients. The length of this list is the degree plus one.
	 * <br>
	 * Modifying this list directly is dangerous. Please use the appropriate methods
	 * instead.
	 */
	public final ArrayList<Double> coefficients;
	
	/**
	 * Constructs the polynomial representing the constant 0.
	 */
	public CurvePolynomial(){
		coefficients = new ArrayList<>();
	}
	
	/**
	 * Constructs the polynomial representing a constant.
	 * 
	 * @param constant degree 0 term
	 */
	public CurvePolynomial(double constant){
		this();
		if(!Double.isFinite(constant))throw new IllegalArgumentException("coefficient must be finite ("+constant+")");
		coefficients.add(constant);
	}
	
	/**
	 * Constructs the polynomial representing a line.
	 * 
	 * @param constant degree 0 term
	 * @param linear degree 1 term
	 */
	public CurvePolynomial(double constant,double linear){
		this();
		if(!(Double.isFinite(constant)&&Double.isFinite(linear)))throw new IllegalArgumentException("coefficient must be finite ("+constant+", "+linear+")");
		coefficients.add(constant);
		coefficients.add(linear);
	}
	
	/**
	 * Constructs the polynomial representing a line.
	 * 
	 * @param constant degree 0 term
	 * @param linear degree 1 term
	 * @param quadratic degree 2 term
	 */
	public CurvePolynomial(double constant,double linear,double quadratic){
		this();
		if(!(Double.isFinite(constant)&&Double.isFinite(linear)&&Double.isFinite(quadratic)))throw new IllegalArgumentException("coefficient must be finite ("+constant+", "+linear+", "+quadratic+")");
		coefficients.add(constant);
		coefficients.add(linear);
		coefficients.add(quadratic);
	}
	
	/**
	 * Constructor which gets the data from an array.
	 * 
	 * @param array coefficients following the rules of {@link #coefficients}
	 */
	public CurvePolynomial(double[] array){
		this();
		Objects.requireNonNull(array, "Source array cannot be null");
		for(double v:array){
			coefficients.add(v);
		}
	}
	
	/**
	 * Constructor which gets the data from a list.
	 * 
	 * @param list coefficients following the rules of {@link #coefficients}
	 */
	public CurvePolynomial(List<Double> list){
		this();
		Objects.requireNonNull(list, "Source list cannot be null");
		coefficients.addAll(list);
	}
	
	/**
	 * Copy constructor.
	 * 
	 * @param source instance to make a copy of
	 */
	public CurvePolynomial(CurvePolynomial source){
		this();
		copyFrom(source);
	}
	
	/**
	 * Copy the data of another instance
	 * 
	 * @param source
	 */
	public void copyFrom(CurvePolynomial source){
		Objects.requireNonNull(source, "Cannot copy from null");
		if(source!=this){
			coefficients.clear();
			coefficients.addAll(source.coefficients);
		}
	}
	
	/**
	 * Reverses the order of the coefficients list. Generally used
	 * to convert between big endian ordering (highest degree first, constant last)
	 * and little endian ordering (constant first, highest degree last).
	 * Trailing zeros may cause unexpected results.
	 */
	public void reverse(){
		Collections.reverse(coefficients);
	}
	
	/**
	 * Get the degree of the polynomial, which is one less than the length of
	 * the coefficient list. If the list is empty, returns -1.
	 * 
	 * @return
	 */
	public int degree(){
		return coefficients.size()-1;
	}
	
	/**
	 * Get the actual degree of the polynomial, ignoring trailing zero terms.
	 * If the list is empty, returns -1.
	 * 
	 * @return
	 */
	public int trueDegree(){
		int length = coefficients.size();
		length--;
		while(length>=0 && coefficients.get(length)==0)length--;
		return length;
	}
	
	/**
	 * Get a coefficient. Will always be 0 if higher than that of the list.
	 * 
	 * @param degree aka index
	 * @return
	 */
	public double getTerm(int degree){
		int length = coefficients.size();
		return degree<length?coefficients.get(degree):0;
	}
	
	/**
	 * Set a coefficient. Will expand the list if necessary.
	 * 
	 * @param degree aka index
	 * @param coefficient
	 * @return
	 */
	public void setTerm(int degree,double coefficient){
		if(!Double.isFinite(coefficient))throw new IllegalArgumentException("coefficient must be finite ("+coefficient+")");
		int length = coefficients.size();
		while(length<=degree){
			coefficients.add(0d);
			length++;
		}
		coefficients.set(degree, coefficient);
	}
	
	/**
	 * Trims the coefficient list by removing trailing zeros.
	 * 
	 * @return number of zeros removed
	 */
	public int trimList(){
		int length = coefficients.size();
		int stop = length-1;
		while(stop>=0 && coefficients.get(stop)==0)coefficients.remove(stop--);
		return length-stop-1;
	}

	@Override
	public double valueAtPosition(double position) {
		double result = 0, grow = 1;
		for(double coefficient:coefficients){
			result += coefficient*grow;
			grow *= position;
		}
		return result;
	}
	
	@Override
	public boolean equals(Object o){
		if(o==this)return true;
		if(o==null||!(o instanceof CurvePolynomial))return false;
		CurvePolynomial ov = (CurvePolynomial) o;
		return coefficients.equals(ov.coefficients);
	}
	
	@Override
	public int hashCode(){
		HashTriArx hash = new HashTriArx(HK_HC);
		hash.absorbObj(coefficients);
		return hash.squeezeInt();
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("P(");
		int length = coefficients.size();
		if(length>0){
			sb.append(coefficients.get(0));
			for(int i=1;i<length;i++){
				sb.append(", ");
				sb.append(coefficients.get(i));
			}
		}
		sb.append(")");
		return sb.toString();
	}
	
	public static void main(String[] args){
		final String sep = "------------------------------";
		CurvePolynomial poly = new CurvePolynomial();
		System.out.println(sep);
		System.out.println((poly.degree()<0)?"Pass":"Fail");
		System.out.println((poly.getTerm(1000)==0)?"Pass":"Fail");
		System.out.println(sep);
		poly.coefficients.add(2d);
		poly.coefficients.add(1d);
		poly.coefficients.add(0d);
		System.out.println((poly.degree()==2)?"Pass":"Fail");
		System.out.println((poly.trueDegree()==1)?"Pass":"Fail");
		System.out.println((poly.getTerm(1000)==0)?"Pass":"Fail");
		System.out.println((poly.trimList()==1)?"Pass":"Fail");
		System.out.println(sep);
		poly.setTerm(1000, 3d);
		System.out.println((poly.degree()==1000)?"Pass":"Fail");
		System.out.println((poly.trueDegree()==1000)?"Pass":"Fail");
		System.out.println((poly.trimList()==0)?"Pass":"Fail");
		poly.coefficients.add(0d);
		System.out.println((poly.degree()==1001)?"Pass":"Fail");
		System.out.println((poly.trueDegree()==1000)?"Pass":"Fail");
		System.out.println((poly.getTerm(1000)==3)?"Pass":"Fail");
		System.out.println((poly.trimList()==1)?"Pass":"Fail");
		System.out.println(sep);
	}

}
