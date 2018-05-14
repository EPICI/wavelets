package ui;

import java.util.*;
import core.*;
import util.hash.*;
import util.math.Floats;
import util.text.Expressions;
import util.ui.Draw;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.Serializable;

import org.apache.pivot.util.*;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.skin.*;

/**
 * Normally an infinite slider (may be bounded or aliased),
 * but when clicked on, will switch to a single line of text
 * view which can be used to precisely input a value
 * 
 * @author EPICI
 * @version 1.0
 */
public class DoubleInput extends FillPane {
	
	/**
	 * The current session where this is being used
	 */
	public Session session;
	/**
	 * Determines the set of valid inputs, and how to go
	 * between them, handles simple behaviours like
	 * bounds and rounding, as well as other behaviours
	 * if the specific implementation has it
	 */
	public DoubleValidator validator;
	/**
	 * What 1 pixel drag corresponds to if passed as the
	 * <i>by</i> argument to the validator's <i>step</i> method
	 */
	public double stepScale;
	/**
	 * The value currently in this
	 * <br>
	 * During changes, this can differ from the values held by
	 * the inner components, in which case this should be treated
	 * as a working value and the components' values as the
	 * true current value
	 */
	public double value;
	/**
	 * The previously stored value, used by listeners
	 */
	public double lastValue;
	/**
	 * The value from the previous committed change, used by listeners
	 */
	public double lastValueCommit;
	/**
	 * The slider view
	 */
	public DoubleSlider slider;
	/**
	 * The text view
	 */
	public TextInput text;
	/**
	 * The text view's combined listener
	 */
	public TextInputListener textListener;
	/**
	 * The data change listeners
	 */
	public DataListenerList dataListeners;
	
	/**
	 * Default constructor
	 */
	public DoubleInput(){
		// Redirect with default values
		this(new DoubleValidator.HyperbolicStep(-Double.MAX_VALUE, Double.MAX_VALUE, 0, 2), 1, 0.002);
	}
	
	/**
	 * Full constructor
	 * 
	 * @param validator
	 * @param initialValue
	 * @param stepScale
	 */
	public DoubleInput(DoubleValidator validator,double initialValue,double stepScale){
		if(validator==null)throw new IllegalArgumentException("validator can't be null: validator ("+validator+")");
		this.validator = validator;
		this.stepScale = stepScale;
		this.value = initialValue;
		slider = new DoubleSlider();
		text = new TextInput();
		textListener = new TextInputListener(this);
		text.getComponentStateListeners().add(textListener);
		text.getComponentKeyListeners().add(textListener);
		dataListeners = new DataListenerList();
		slider.parent = this;
		slider.value = value;
		slider.init();
		add(slider);
		add(text);
		toSliderView(false);
	}
	
	/**
	 * Switch to the slider view
	 * 
	 * @param update whether to attempt to
	 * get the value from the text view
	 */
	public void toSliderView(boolean update){
		if(update){
			try{
				value = validator.nearest(parse(text.getText()));
				valueChanged(true);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		slider.setVisible(true);
		text.setVisible(false);
		slider.value = value;
		repaint();
	}
	
	/**
	 * Switch to the text view
	 */
	public void toTextView(){
		text.setVisible(true);
		slider.setVisible(false);
		text.setText(Double.toString(value));
		text.selectAll();
		repaint();
	}
	
	/**
	 * Notify the data listeners
	 * 
	 * @param commit whether the change is pending (false)
	 * or committed/permanent (true)
	 */
	public void valueChanged(boolean commit){
		valueChanged(commit,true);
	}
	
	/**
	 * Notify the data listeners
	 * 
	 * @param commit whether the change is pending (false)
	 * or committed/permanent (true)
	 * @param notify true to notify listeners, false to not notify listeners
	 */
	public void valueChanged(boolean commit,boolean notify){
		if(notify){
			for(DataListener listener:dataListeners){
				listener.updated(this, commit);
			}
		}
		lastValue = value;
		if(commit)lastValueCommit = value;
	}
	
	/**
	 * Convenience method to set the value to the nearest valid
	 * value without updating anything else
	 * 
	 * @param value
	 */
	public void setValueNearest(double value){
		this.value = validator.nearest(value);
	}
	
	/**
	 * Set the validator and update data accordingly
	 * 
	 * @param newValidator
	 */
	public void setValidator(DoubleValidator newValidator){
		if(newValidator==null)throw new IllegalArgumentException("validator can't be null: newValidator ("+newValidator+")");
		validator = newValidator;
		value = validator.nearest(value);
	}
	
	/**
	 * Attempt to parse an expression to a number
	 * <br>
	 * Using this API method means a different parsing
	 * method may be used in the future without need
	 * to change existing code
	 * 
	 * @param text
	 * @return
	 */
	public static double parse(String text){
		return Double.parseDouble(text);
	}
	
	/**
	 * A {@link ListenerList} of {@link DataListener}.
	 * When given an event, passes it on to each of its listeners.
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public static class DataListenerList extends WTKListenerList<DataListener> implements DataListener{

		@Override
		public void updated(DoubleInput component, boolean commit) {
			for(DataListener listener:this){
				listener.updated(component, commit);
			}
		}
		
	}
	
	/**
	 * Combined listener for the text view
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public static class TextInputListener implements ComponentStateListener, ComponentKeyListener{

		/**
		 * The parent, this instance listens to the parent's text view
		 */
		public DoubleInput parent;
		
		/**
		 * Standard constructor, sets fields
		 * 
		 * @param parent
		 */
		public TextInputListener(DoubleInput parent){
			this.parent = parent;
		}
		
		// Component key events
	    @Override
	    public boolean keyTyped(Component componentArgument, char character) {
	        return false;
	    }

	    @Override
	    public boolean keyPressed(Component componentArgument, int keyCode, Keyboard.KeyLocation keyLocation) {
	    	return false;
	    }
	    
	    @Override
	    public boolean keyReleased(Component componentArgument, int keyCode, Keyboard.KeyLocation keyLocation) {
	        switch(keyCode){
	        case KeyEvent.VK_ENTER:{
	        	// Enter -> switch to slider view, confirm changes
	        	parent.toSliderView(true);
	        	break;
	        }
	        case KeyEvent.VK_ESCAPE:{
	        	// Escape -> switch to slider view, discard changes
	        	parent.toSliderView(false);
	        	break;
	        }
	        }
	    	return false;
	    }

		// Component state events
	    @Override
	    public void enabledChanged(Component componentArgument) {
	    }

	    @Override
	    public void focusedChanged(Component componentArgument, Component obverseComponent) {
	        if(!componentArgument.isFocused()){
	        	// Lost focus -> switch to slider view, confirm changes
	        	parent.toSliderView(true);
	        }
	    }
		
	}
	
	/**
	 * Listener specific to {@link DoubleInput}, fires when
	 * the value is updated
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public static interface DataListener{
		
		/**
		 * Given the component alone, it is possible to get
		 * all other necessary information easily except for
		 * whether the change is pending or committed, so
		 * that is provided as the other argument
		 * <br>
		 * This can also fire if there is no change, but a previous
		 * pending change was committed
		 * 
		 * @param component component whose value changed
		 * @param commit whether the change is pending (false)
		 * or committed/permanent (true)
		 */
		public void updated(DoubleInput component,boolean commit);
		
	}
	
	/**
	 * Deals with some kind of set of doubles, can be infinite,
	 * specification doesn't require consistency but the user
	 * should not be surprised by the behaviour
	 * <br>
	 * The naming comes from treating doubles in the set as valid
	 * and other values as invalid
	 * <br>
	 * To isolate a specific method or group of methods, use a
	 * set which preserves values well, like the set of all
	 * finite doubles
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public static interface DoubleValidator extends BetterClone<DoubleValidator>, Serializable{
		
		/**
		 * Given a value, what valid value is closest to it?
		 * <br>
		 * Can be implemented as rounding or some other behaviour
		 * 
		 * @param value
		 * @return
		 */
		public double nearest(double value);
		
		/**
		 * Is this value valid or close enough?
		 * 
		 * @param value
		 * @return
		 */
		public boolean valid(double value);
		
		/**
		 * Assume we know nothing about this set;
		 * what value should we start with?
		 * 
		 * @return
		 */
		public double base();
		
		/**
		 * The lowest valid value
		 * 
		 * @return
		 */
		public double min();
		
		/**
		 * The highest valid value
		 * 
		 * @return
		 */
		public double max();
		
		/**
		 * Given a value, what is the lowest valid value which
		 * is greater than it?
		 * <br>
		 * Return same value if such value does not exist
		 * 
		 * @param value
		 * @return
		 */
		public double next(double value);
		
		/**
		 * Given a value, what is the highest valid value which
		 * is less than it?
		 * <br>
		 * Return same value if such value does not exist
		 * 
		 * @param value
		 * @return
		 */
		public double prev(double value);
		
		/**
		 * Increase value by some amount, return nearest
		 * valid value to that
		 * <br>
		 * Whether to use absolute, relative, or some other metric
		 * is up to the implementation, but it should be consistent
		 * 
		 * @param value
		 * @param by
		 * @return
		 */
		public double step(double value,double by);
		
		/**
		 * What is the granularity of this set?
		 * <br>
		 * If 0, means unrestricted, otherwise, values in this set
		 * are at least this much absolute difference or relative difference
		 * or some other metric apart
		 * 
		 * @return
		 */
		public default double rounding(){
			return 0;
		}
		
		/**
		 * Simple double validator which uses bounds and
		 * linear <i>step</i>, can be treated as an abstract class
		 * for more complex implementations
		 * 
		 * @author EPICI
		 * @version 1.0
		 */
		public static class BoundedDoubleValidator implements DoubleValidator{
			private static final long serialVersionUID = 1L;

			/**
			 * Hash key for <i>hashCode()</i>
			 */
			public static final long HK_HC = QuickKeyGen.next64();
			
			/**
			 * The minimum value
			 */
			public final double min;
			/**
			 * The maximum value
			 */
			public final double max;
			/**
			 * The base value
			 */
			public final double base;
			
			/**
			 * Shortcut constructor which uses all doubles
			 * as the range.
			 * 
			 * @param base see method <i>base()</i>
			 */
			public BoundedDoubleValidator(double base){
				this(-Double.MAX_VALUE, Double.MAX_VALUE, base);
			}
			
			/**
			 * Standard constructor providing both
			 * minimum and maximum, as well as base
			 * 
			 * @param min see method <i>min()</i>
			 * @param max see method <i>max()</i>
			 * @param base see method <i>base()</i>
			 */
			public BoundedDoubleValidator(double min,double max,double base){
				if(!(Double.isFinite(min)&&Double.isFinite(max)&&Double.isFinite(base)))throw new IllegalArgumentException("values must be finite: min ("+min+"), max ("+max+"), base ("+base+")");
				if(min>base||base>max)throw new IllegalArgumentException("inequality must hold: min ("+min+") <= base ("+base+") <= max ("+max+")");
				this.min=min;
				this.max=max;
				this.base=base;
			}

			@Override
			public double nearest(double value) {
				if(Double.isNaN(value))return base();
				if(value<min)return min;
				if(value>max)return max;
				return value;
			}

			@Override
			public boolean valid(double value) {
				return Double.isFinite(value)&&value>=min&&value<=max;
			}
			
			@Override
			public double base() {
				return base;
			}

			@Override
			public double min() {
				return min;
			}

			@Override
			public double max() {
				return max;
			}

			@Override
			public double next(double value) {
				if(Double.isNaN(value)||value>=max)return value;
				if(value<min)return min;
				return Math.nextUp(value);
			}

			@Override
			public double prev(double value) {
				if(Double.isNaN(value)||value<=min)return value;
				if(value>max)return max;
				return Math.nextDown(value);
			}

			@Override
			public double step(double value, double by) {
				return nearest(value+by);
			}
			
			public boolean equals(Object o){
				if(o==this)return true;
				if(o==null||!(o instanceof BoundedDoubleValidator))return false;
				BoundedDoubleValidator ov = (BoundedDoubleValidator) o;
				return rounding()==ov.rounding()&&min==ov.min&&max==ov.max;
			}
			
			public int hashCode(){
				HashTriArx hash = new HashTriArx(HK_HC);
				hash.absorb(min,max,rounding());
				return hash.squeezeInt();
			}
			
			public String toString(){
				double rounding = rounding();
				StringBuilder sb = new StringBuilder();
				if(rounding>0){
					/*
					 * There are a few alternatives to represent this, including
					 * a mod b = 0
					 * b|a meaning b divides a
					 * The vertical bar is clean and concise, so we use it
					 */
					sb.append(rounding);
					sb.append("|");
				}
				sb.append("[");
				sb.append(min);
				sb.append(",");
				sb.append(max);
				sb.append("]");
				return sb.toString();
			}
			
			private static final String BOUNDEDDOUBLEVALIDATOR_CLASS_NAME = BoundedDoubleValidator.class.getCanonicalName();
			@Override
			public DoubleValidator copy(int depth,Map<String,Object> options){
				double newMin = this.min, newMax = this.max, newBase = this.base;
				Map<String,Object> set = (Map<String,Object>)options.get("set");
				if(set!=null){
					Number val;
					val = (Number) set.get(BOUNDEDDOUBLEVALIDATOR_CLASS_NAME+".min");
					if(val!=null)newMin = val.doubleValue();
					val = (Number) set.get(BOUNDEDDOUBLEVALIDATOR_CLASS_NAME+".max");
					if(val!=null)newMax = val.doubleValue();
					val = (Number) set.get(BOUNDEDDOUBLEVALIDATOR_CLASS_NAME+".base");
					if(val!=null)newBase = val.doubleValue();
				}
				return new BoundedDoubleValidator(newMin,newMax,newBase);
			}
			
		}
		
		/**
		 * Similar to {@link BoundedDoubleValidator} except it is
		 * restricted to mathematical integers. Due to the way
		 * doubles work, this should be safe with 32 bit <i>int</i>s
		 * and work reasonably well with 64 bit <i>long</i>s since
		 * double uses 52 bit mantissa
		 * 
		 * @author EPICI
		 * @version 1.0
		 */
		public static class BoundedIntegerValidator extends BoundedDoubleValidator{
			private static final long serialVersionUID = 1L;
			
			/**
			 * Hash key for <i>hashCode()</i>
			 */
			public static final long HK_HC = QuickKeyGen.next64();
			
			/**
			 * Is it a mathematical integer?
			 * <br>
			 * Java primitive types <i>int</i> and <i>long</i>
			 * have their own restrictions, so while a value that
			 * passes this test is indeed a mathematical integer,
			 * it may cast to a different value
			 * 
			 * @param value
			 * @return
			 */
			public static boolean isInteger(double value){
				return Double.isFinite(value) && value==Math.rint(value);
			}

			/**
			 * Standard constructor providing both
			 * minimum and maximum, as well as base,
			 * all of which must be integers
			 * 
			 * @param min see method <i>min()</i>
			 * @param max see method <i>max()</i>
			 * @param base see method <i>base()</i>
			 */
			public BoundedIntegerValidator(double min, double max, double base) {
				super(min, max, base);
				if(!(isInteger(min)&&isInteger(max)&&isInteger(base)))throw new IllegalArgumentException("values must be integers: min ("+min+"), max ("+max+"), base ("+base+")");
			}
			
			@Override
			public double nearest(double value) {
				return Math.rint(super.nearest(value));
			}

			@Override
			public double next(double value) {
				if(Double.isNaN(value)||value>=max)return value;
				if(value<min)return min;
				value = Math.floor(value);
				return Math.max(Math.nextUp(value), value+1);
			}

			@Override
			public double prev(double value) {
				if(Double.isNaN(value)||value<=min)return value;
				if(value>max)return max;
				value = Math.ceil(value);
				return Math.min(Math.nextDown(value), value-1);
			}
			
			public double rounding(){
				return 1;
			}
			
			private static final String BOUNDEDDOUBLEVALIDATOR_CLASS_NAME = BoundedDoubleValidator.class.getCanonicalName();
			@Override
			public DoubleValidator copy(int depth,Map<String,Object> options){
				double newMin = this.min, newMax = this.max, newBase = this.base;
				Map<String,Object> set = (Map<String,Object>)options.get("set");
				if(set!=null){
					Number val;
					val = (Number) set.get(BOUNDEDDOUBLEVALIDATOR_CLASS_NAME+".min");
					if(val!=null)newMin = val.doubleValue();
					val = (Number) set.get(BOUNDEDDOUBLEVALIDATOR_CLASS_NAME+".max");
					if(val!=null)newMax = val.doubleValue();
					val = (Number) set.get(BOUNDEDDOUBLEVALIDATOR_CLASS_NAME+".base");
					if(val!=null)newBase = val.doubleValue();
				}
				return new BoundedIntegerValidator(newMin,newMax,newBase);
			}
			
		}
		
		/**
		 * An implementation that separates <i>step()</i> from
		 * the double set functionality
		 * 
		 * @author EPICI
		 * @version 1.0
		 */
		public static class SplitDoubleValidator implements DoubleValidator{
			private static final long serialVersionUID = 1L;
			
			/**
			 * Hash key for <i>hashCode()</i>
			 */
			public static final long HK_HC = QuickKeyGen.next64();
			
			/**
			 * The validator used for the backing set
			 */
			public final DoubleValidator set;
			/**
			 * The validator used for <i>step()</i>
			 */
			public final DoubleValidator step;
			
			/**
			 * Standard constructor providing both
			 * 
			 * @param set determines what values are valid, including bounds, as well as base
			 * @param step determines stepping between values
			 */
			public SplitDoubleValidator(DoubleValidator set,DoubleValidator step){
				if(set==null||step==null)throw new IllegalArgumentException("backing validators cannot be null: set ("+set+"), step ("+step+")");
				while(set instanceof SplitDoubleValidator){// Remove redundant chaining
					set = ((SplitDoubleValidator)set).set;
				}
				this.set = set;
				this.step = step;
			}

			@Override
			public double nearest(double value) {
				return set.nearest(value);
			}

			@Override
			public boolean valid(double value) {
				return set.valid(value);
			}

			@Override
			public double base() {
				return set.base();
			}

			@Override
			public double min() {
				return set.min();
			}

			@Override
			public double max() {
				return set.max();
			}

			@Override
			public double next(double value) {
				return set.next(value);
			}

			@Override
			public double prev(double value) {
				return set.prev(value);
			}

			@Override
			public double step(double value, double by) {
				return set.nearest(step.step(value, by));
			}
			
			@Override
			public double rounding(){
				double rset = set.rounding(), rstep = step.rounding();
				return Math.max(rset, rstep);
			}
			
			public boolean equals(Object o){
				if(o==this)return true;
				if(o==null||!(o instanceof SplitDoubleValidator))return false;
				SplitDoubleValidator ov = (SplitDoubleValidator) o;
				return set.equals(ov.set)&&step.equals(ov.step);
			}
			
			public int hashCode(){
				HashTriArx hash = new HashTriArx(HK_HC);
				hash.absorbObj(set,step);
				return hash.squeezeInt();
			}
			
			public String toString(){
				StringBuilder sb = new StringBuilder();
				sb.append(Expressions.wrapBrackets(set.toString()));
				sb.append("[");
				sb.append(step);
				sb.append("]");
				return sb.toString();
			}
			
			private static final String SPLITDOUBLEVALIDATOR_CLASS_NAME = SplitDoubleValidator.class.getCanonicalName();
			@Override
			public DoubleValidator copy(int depth,Map<String,Object> options){
				if(options==null){
					options = new HashMap<String,Object>();
				}
				DoubleValidator newSet = this.set, newStep = this.step;
				Collection<String> blacklist = (Collection<String>)options.get("blacklist");
				Collection<String> whitelist = (Collection<String>)options.get("whitelist");
				Map<String,Object> set = (Map<String,Object>)options.get("set");
				DoubleValidator val;
				val = (DoubleValidator) set.get(SPLITDOUBLEVALIDATOR_CLASS_NAME+".set");
				if(val!=null){
					newSet = val;
				}else if(depth>0
						&&!BetterClone.fieldIncluded(blacklist, newSet.getClass(),
								SPLITDOUBLEVALIDATOR_CLASS_NAME+".set")
						|| BetterClone.fieldIncluded(whitelist, newSet.getClass(),
								SPLITDOUBLEVALIDATOR_CLASS_NAME+".set")){
					newSet = BetterClone.copy(newSet, depth-1, options);
				}
				val = (DoubleValidator) set.get(SPLITDOUBLEVALIDATOR_CLASS_NAME+".step");
				if(val!=null){
					newStep = val;
				}else if(depth>0
						&&!BetterClone.fieldIncluded(blacklist, newSet.getClass(),
								SPLITDOUBLEVALIDATOR_CLASS_NAME+".step")
						|| BetterClone.fieldIncluded(whitelist, newSet.getClass(),
								SPLITDOUBLEVALIDATOR_CLASS_NAME+".step")){
					newStep = BetterClone.copy(newStep, depth-1, options);
				}
				return new SplitDoubleValidator(newSet,newStep);
			}
			
		}
		
		/**
		 * Variant on {@link BoundedDoubleValidator} which uses a
		 * function based on hyperbolic functions for step rather
		 * than a linear step. <i>sinh</i> for example resembles
		 * linear around 0 but resembles exponential for larger
		 * values, both positive an negative.
		 * 
		 * @author EPICI
		 * @version 1.0
		 */
		public static class HyperbolicStep extends BoundedDoubleValidator{
			private static final long serialVersionUID = 1L;
			
			/**
			 * Hash key for <i>hashCode()</i>
			 */
			public static final long HK_HC = QuickKeyGen.next64();
			
			/**
			 * Logarithm of <i>pbase</i>, used because <i>exp()</i>
			 * is faster than <i>pow()</i>
			 */
			public final double lpbase;
			/**
			 * Hyperbolic functions normally use e as a base,
			 * we allow usage of a different base
			 */
			public final double pbase;

			/**
			 * Shortcut constructor which uses all doubles
			 * as the range and 0 as the default value.
			 * 
			 * @param pbase base for power to use instead of e
			 */
			public HyperbolicStep(double pbase){
				this(-Double.MAX_VALUE, Double.MAX_VALUE, 0, pbase);
			}
			
			/**
			 * Standard constructor, see {@link BoundedDoubleValidator} for usage
			 * 
			 * @param min
			 * @param max
			 * @param base
			 * @param pbase base for power to use instead of e
			 */
			public HyperbolicStep(double min, double max, double base, double pbase) {
				super(min, max, base);
				this.pbase = pbase;
				this.lpbase = Math.log(pbase);
				if(!(Double.isFinite(lpbase)&&lpbase!=0))throw new IllegalArgumentException("base for powers must produce valid logarithm: pbase ("+pbase+") -> lpbase ("+lpbase+")");
			}
			
			/**
			 * Computes <i>sinh(asinh(value)+by*lpbase)</i>
			 */
			public double step(double value,double by){
				// Compute pbase^by, pbase^-by
				double ub = Math.exp(by*lpbase), vb = 1/ub;
				// Compute the final result
				return (Math.sqrt(value*value+1)*(ub-vb)+value*(ub+vb))*0.5;
			}
			
			public int hashCode(){
				HashTriArx hash = new HashTriArx(HK_HC);
				hash.absorb(min,max,pbase,rounding());
				return hash.squeezeInt();
			}
			
			public String toString(){
				StringBuilder sb = new StringBuilder();
				sb.append(Expressions.wrapBrackets(super.toString()));
				sb.append("[H");
				sb.append(pbase);
				sb.append("]");
				return sb.toString();
			}
			
			private static final String HYPERBOLICSTEP_CLASS_NAME = HyperbolicStep.class.getCanonicalName();
			private static final String BOUNDEDDOUBLEVALIDATOR_CLASS_NAME = BoundedDoubleValidator.class.getCanonicalName();
			@Override
			public DoubleValidator copy(int depth,Map<String,Object> options){
				double newMin = this.min, newMax = this.max, newBase = this.base, newPbase = this.pbase;
				Map<String,Object> set = (Map<String,Object>)options.get("set");
				if(set!=null){
					Number val;
					val = (Number) set.get(BOUNDEDDOUBLEVALIDATOR_CLASS_NAME+".min");
					if(val!=null)newMin = val.doubleValue();
					val = (Number) set.get(BOUNDEDDOUBLEVALIDATOR_CLASS_NAME+".max");
					if(val!=null)newMax = val.doubleValue();
					val = (Number) set.get(BOUNDEDDOUBLEVALIDATOR_CLASS_NAME+".base");
					if(val!=null)newBase = val.doubleValue();
					val = (Number) set.get(HYPERBOLICSTEP_CLASS_NAME+".pbase");
					if(val!=null)newPbase = val.doubleValue();
				}
				return new HyperbolicStep(newMin,newMax,newBase,newPbase);
			}
			
		}
		
		/**
		 * Applies a linear function (x -&gt; <i>mul</i>&#00b7;x+<i>add</i>)
		 * over another {@link DoubleValidator}.
		 * 
		 * @author EPICI
		 * @version 1.0
		 */
		public static class LinearMap implements DoubleValidator{
			private static final long serialVersionUID = 1L;
			
			/**
			 * Hash key for <i>hashCode()</i>
			 */
			public static final long HK_HC = QuickKeyGen.next64();
			
			/**
			 * The original/backing instance
			 */
			public final DoubleValidator view;
			/**
			 * Coefficient of linear term
			 */
			public final double mul;
			/**
			 * Constant term
			 */
			public final double add;
			
			/**
			 * Shortcut constructor to use a {@link BoundedDoubleValidator}
			 * representing all valid values.
			 * 
			 * @param mul coefficient of linear term
			 * @param add constant term
			 */
			public LinearMap(double mul,double add){
				this(new BoundedDoubleValidator(0),mul,add);
			}
			
			/**
			 * Standard constructor providing all values.
			 * 
			 * @param view the instance to map
			 * @param mul coefficient of linear term
			 * @param add constant term
			 */
			public LinearMap(DoubleValidator view,double mul,double add){
				if(view==null)throw new IllegalArgumentException("backing validator cannot be null: view ("+view+")");
				if(!(Double.isFinite(mul)&&Double.isFinite(add)))throw new IllegalArgumentException("values must be finite: mul ("+mul+"), add ("+add+")");
				this.view=view;
				this.mul=mul;
				this.add=add;
			}
			
			/**
			 * Apply the mapping to a value.
			 * Result is bound to finite doubles.
			 * 
			 * @param value
			 * @return
			 */
			public double map(double value){
				double result = mul*value+add;
				return Floats.median(-Double.MAX_VALUE, result, Double.MAX_VALUE);
			}
			
			/**
			 * Apply the inverse mapping to a value.
			 * Result is bound to finite doubles.
			 * 
			 * @param value
			 * @return
			 */
			public double invmap(double value){
				double result = (value-add)/mul;
				return Floats.median(-Double.MAX_VALUE, result, Double.MAX_VALUE);
			}
			
			@Override
			public double nearest(double value) {
				return map(view.nearest(invmap(value)));
			}

			@Override
			public boolean valid(double value) {
				return view.valid(invmap(value));
			}

			@Override
			public double base() {
				return map(view.base());
			}

			@Override
			public double min() {
				if(mul==0){
					return add;
				}else if(mul>0){
					return map(view.min());
				}else{
					return map(view.max());
				}
			}

			@Override
			public double max() {
				if(mul==0){
					return add;
				}else if(mul>0){
					return map(view.max());
				}else{
					return map(view.min());
				}
			}

			@Override
			public double next(double value) {
				return map(view.next(invmap(value)));
			}

			@Override
			public double prev(double value) {
				return map(view.prev(invmap(value)));
			}

			@Override
			public double step(double value, double by) {
				return map(view.step(invmap(value),by));
			}
			
			@Override
			public double rounding(){
				return mul*view.rounding();
			}
			
			public boolean equals(Object o){
				if(o==this)return true;
				if(o==null||!(o instanceof LinearMap))return false;
				LinearMap ov = (LinearMap) o;
				boolean eqmul = mul==ov.mul;
				boolean eqadd = add==ov.add;
				if(eqmul&&mul==0)return eqadd;
				/*
				 * We can't know the behaviour of the backing instance.
				 * Even if they would be equal after the transform,
				 * we can't be sure.
				 */
				return (eqmul&eqadd)&&view.equals(ov.view);
			}
			
			public int hashCode(){
				HashTriArx hash = new HashTriArx(HK_HC);
				hash.absorb(mul,add);
				hash.absorbObj(view);
				return hash.squeezeInt();
			}
			
			public String toString(){
				StringBuilder sb = new StringBuilder();
				sb.append(Expressions.wrapBrackets(view.toString()));
				/*
				 * Polynomial is preferred to be represented in little endian
				 * order, so that index of coefficient = exponent of that term
				 */
				sb.append("[P(");
				sb.append(add);
				sb.append(",");
				sb.append(mul);
				sb.append(")");
				return sb.toString();
			}
			
			private static final String LINEARMAP_CLASS_NAME = LinearMap.class.getCanonicalName();
			@Override
			public DoubleValidator copy(int depth,Map<String,Object> options){
				DoubleValidator newView = this.view;
				double newMul = this.mul, newAdd = this.add;
				Collection<String> blacklist = (Collection<String>)options.get("blacklist");
				Collection<String> whitelist = (Collection<String>)options.get("whitelist");
				Map<String,Object> set = (Map<String,Object>)options.get("set");
				DoubleValidator dval;
				dval = (DoubleValidator) set.get(LINEARMAP_CLASS_NAME+".view");
				if(dval!=null){
					newView = dval;
				}else if(depth>0
						&&!BetterClone.fieldIncluded(blacklist, newView.getClass(),
								LINEARMAP_CLASS_NAME+".view")
						|| BetterClone.fieldIncluded(whitelist, newView.getClass(),
								LINEARMAP_CLASS_NAME+".view")){
					newView = BetterClone.copy(newView, depth-1, options);
				}
				if(set!=null){
					Number val;
					val = (Number) set.get(LINEARMAP_CLASS_NAME+".mul");
					if(val!=null)newMul = val.doubleValue();
					val = (Number) set.get(LINEARMAP_CLASS_NAME+".add");
					if(val!=null)newAdd = val.doubleValue();
				}
				return new LinearMap(newView,newMul,newAdd);
			}
			
		}
		
	}
	
	/**
	 * Validated slider for double values
	 * <br>
	 * May have resemblance to slider, but is different enough
	 * that it is fully re-implemented
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public static class DoubleSlider extends Component{
		
		/**
		 * The one that contains this instance
		 */
		public DoubleInput parent;
		/**
		 * Mirrors the parent
		 */
		public double value;
		
		/**
		 * Default constructor
		 */
		public DoubleSlider(){
			installSkin(DoubleSlider.class);
		}
		
		/**
		 * Call after fields are set
		 */
		public void init(){
			updateFromParent();
		}
		
		/**
		 * Update this instance to match the parent's data
		 */
		public void updateFromParent(){
			value = parent.value;
		}
		
		/**
		 * Update the parent to match this instance's data
		 */
		public void updateToParent(){
			parent.value = value;
		}
		
	}
	
	/**
	 * Skin for {@link DoubleSlider}, which by convention handles
	 * input and rendering
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public static class DoubleSliderSkin extends ComponentSkin{
		
		/**
		 * What the preferred height is initialized to
		 */
		public static final int DEFAULT_HEIGHT = 40;
		/**
		 * What the preferred width is initialized to
		 */
		public static final int DEFAULT_WIDTH = 160;
		/**
		 * The test string used to determine needed bounds
		 */
		public static final String TEST_STRING = "0.123456789";
		/**
		 * How wide the arrow is compared to the minor radius
		 */
		public static final double ARROW_FAC = 0.3;
		
		/**
		 * Cache the font so other stuff can use it
		 */
		public Font font;
		/**
		 * Keep track of the preferred width
		 */
		public int preferredWidth;
		/**
		 * Keep track of the preferred height
		 */
		public int preferredHeight;
		/**
		 * If true, don't automatically update the preferred
		 * width and height
		 */
		public boolean preferredLock;
		
		/**
		 * Mouse button being held down (0 = not pressed, 1 = left, 2 = right, 3 = middle)
		 */
		public int mouseDown;
		/**
		 * Has the mouse moved since it was clicked down?
		 */
		public boolean mouseDragged;
		/**
		 * Which keys are held down?
		 */
		public BitSet keys = new BitSet();
		/**
		 * The mouse x where dragging began
		 */
		public int originMousex;
		/**
		 * The mouse y where dragging began
		 */
		public int originMousey;
		/**
		 * The mouse x, where it was last seen
		 */
		public int lastMousex;
		/**
		 * The mouse y, where it was last seen
		 */
		public int lastMousey;
		
		public DoubleSliderSkin(){
			preferredWidth = DEFAULT_WIDTH;
			preferredHeight = DEFAULT_HEIGHT;
			preferredLock = false;
		}
		
		/**
		 * Is the shift key held down?
		 * 
		 * @return
		 */
		public boolean shiftHeld(){
			return keys.get(KeyEvent.VK_SHIFT);
		}
		
		/**
		 * Is the control key held down?
		 * 
		 * @return
		 */
		public boolean controlHeld(){
			return keys.get(KeyEvent.VK_CONTROL);
		}
		
		/**
		 * If there is currently a pending change, how much is it
		 * before scaling, otherwise 0
		 * 
		 * @return
		 */
		public double getShift(){
			if(mouseDown!=1)return 0;
			// Choose x or y based on whichever is larger
			double dx = lastMousex-originMousex;
			double dy = originMousey-lastMousey;// up needs to be positive
			return Math.abs(dx)>Math.abs(dy)?dx:dy;
		}
		
		@Override
	    public boolean mouseDown(Component componentArgument, Mouse.Button button, int x, int y) {
			DoubleSlider slider = (DoubleSlider) getComponent();
	        if(slider.isBlocked())return false;// if disabled, then ignore
			switch(button){
			case LEFT:
				mouseDown = 1;
				break;
			case RIGHT:
				mouseDown = 2;
				break;
			case MIDDLE:
				mouseDown = 3;
				break;
			default:
				mouseDown = 4;
				break;
			}
	        originMousex =  x;
	        originMousey =  y;
	        lastMousex = x;
	        lastMousey = y;
	        mouseDragged = false;
	        slider.repaint();
			return false;
	    }

	    @Override
	    public boolean mouseUp(Component componentArgument, Mouse.Button button, int x, int y) {
	    	DoubleSlider slider = (DoubleSlider) getComponent();
    		DoubleInput sliderParent = slider.parent;
	    	if(mouseDown==1){
	    		if(mouseDragged){// Dragged -> change value
	    			// copy back value
	    			slider.value = sliderParent.value;
	    			// need to notify listeners
	    			sliderParent.valueChanged(true);
	    	    	slider.repaint();
	    		}else{// Clicked -> switch view or fine adjustment
	    			int width = getWidth(), height = getHeight();
	    			int lowerDist,upperDist;
	    			boolean useUp;
	    			if(width<height){
	    				lowerDist = x;
	    				upperDist = width-x;
	    			}else{
	    				lowerDist = y;
	    				upperDist = height-y;
	    			}
    				useUp = upperDist<lowerDist;
	    			boolean useIncrement = Math.min(lowerDist, upperDist)<0;
	    			if(useIncrement){// Clicked near arrow -> increment
	    				if(useUp){
	    					sliderParent.value = sliderParent.validator.next(slider.value);
	    				}else{
	    					sliderParent.value = sliderParent.validator.prev(slider.value);
	    				}
	    				slider.value = sliderParent.value;
	    				sliderParent.valueChanged(true);
	    				slider.repaint();
	    			}else{// Clicked in middle -> switch view
	    				// no data to change
		    			sliderParent.toTextView();
	    			}
	    		}
	    	}
	    	mouseDown = 0;
	    	return false;
	    }
	    
	    @Override
	    public boolean mouseMove(Component componentArgument, int x, int y) {
	    	if(mouseDown==1){
	    		DoubleSlider slider = (DoubleSlider) getComponent();
	    		DoubleInput sliderParent = slider.parent;
		    	mouseDragged = true;
		    	double shift = getShift();
		    	lastMousex = x;
		    	lastMousey = y;
		    	shift = getShift()-shift;
		    	shift *= sliderParent.stepScale;
		    	double newValue = sliderParent.validator.step(sliderParent.value, shift);
		    	sliderParent.value = newValue;
		    	sliderParent.valueChanged(false);
		    	slider.repaint();
	    	}
	        return false;
	    }
	    
	    @Override
		public void mouseOut(Component component) {
			DoubleSlider slider = (DoubleSlider) getComponent();
			keys.clear();// Forget which keys are pressed, for safety
			lastMousex = -1;
			lastMousey = -1;
			slider.repaint();// May need repainting
		}
	    
	    @Override
		public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
			keys.set(keyCode);
			return false;
		}
	    
	    @Override
	    public boolean keyReleased(Component componentArgument, int keyCode, Keyboard.KeyLocation keyLocation) {
	        keys.clear(keyCode);
    		DoubleSlider slider = (DoubleSlider) getComponent();
    		DoubleInput sliderParent = slider.parent;
	        switch(keyCode){
	        case KeyEvent.VK_ESCAPE:{
	        	// Cancel current changes
	        	mouseDown = 0;
	        	sliderParent.value = slider.value;
	        	sliderParent.valueChanged(true);
    	    	slider.repaint();
	        	break;
	        }
	        }
	    	return false;
	    }

		@Override
		public void layout() {
		}

		@Override
		public int getPreferredHeight(int width) {
			return preferredHeight;
		}

		@Override
		public int getPreferredWidth(int height) {
			return preferredWidth;
		}
		
		@Override
	    public void preferredSizeChanged(Component componentArgument,
	        int previousPreferredWidth, int previousPreferredHeight) {
			preferredLock = true;
		}

		@Override
		public void paint(Graphics2D g) {
			// --- Fetch some data ---
			int width = getWidth(), height = getHeight();
			AffineTransform at = g.getTransform();
			// Ensure width > height
			if(width<height){// Vertical slider
				at.quadrantRotate(-1);
				at.translate(0, height);
				g.setTransform(at);
				int tmp = width;width = height;height = tmp;
			}
			DoubleSlider ds = (DoubleSlider) getComponent();
			DoubleInput di = ds.parent;
			Session session = di.session;
			ColorScheme colors = Session.getColors(session);
			Font font = this.font = g.getFont();
			FontMetrics fm = g.getFontMetrics();
			final double ushift = getShift();
			boolean enabled = !ds.isBlocked();
			// Update cached bounds if possible
			if(!preferredLock){
				Rectangle2D bounds = fm.getStringBounds(TEST_STRING, g);
				preferredHeight = (int)(bounds.getHeight()*1.9);
				preferredWidth = (int)(bounds.getWidth()*1.1);
			}
			// --- Pre-draw ---
			// Get colors
			Color bgColUpper, bgColLower, textCol, lineCol, arrowCol, changeColUpper, changeColLower;
			bgColLower = colors.gradient;
			bgColUpper = ColorScheme.brighten(bgColLower, 0.1f);
			textCol = colors.text;
			lineCol = colors.line;
			arrowCol = colors.line;
			changeColLower = colors.highlight;
			changeColUpper = ColorScheme.brighten(changeColLower, 0.1f);
			// Calculate extra coordinates and dimensions
			final double centerx = width*0.5, centery = height*0.5;
			final int mindiameter = height, maxdiameter = width;
			final double minradius = 0.5*mindiameter, maxradius = 0.5*maxdiameter;
			// --- Draw ---
			// The base is a rectangle with half circle caps
			RoundRectangle2D.Double shapeOuter = new RoundRectangle2D.Double(0, 0, width, height, minradius, minradius);
			g.setPaint(new GradientPaint(0,0,bgColUpper,0,height,bgColLower));
			g.fill(shapeOuter);
			// If any pending changes, show it
			if(ushift!=0){
				// Map it to a nicer visual using logistic
				double barlength = (2/(1+Math.exp(-ushift/maxradius*Floats.LOG2))-1)*maxradius;
				// Split into rectangular section and cap
				double caplength = Math.copySign(Math.min(Math.abs(barlength), minradius), barlength);
				barlength -= caplength;
				// Create the path
				double topy = centery-minradius, boty = centery+minradius;
				double endx = centerx+barlength;
				Path2D.Double path = new Path2D.Double();
				path.moveTo(endx, topy);
				Draw.BezierArc.arc(path, AffineTransform.getScaleInstance(caplength, minradius), endx, centery, 1, -Math.PI*0.5, Math.PI, 2);
				path.lineTo(centerx, boty);
				path.lineTo(centerx, topy);
				path.closePath();
				// Render it
				g.setPaint(new GradientPaint(0,0,changeColUpper,0,height,changeColLower));
				g.fill(path);
			}
			// Outline comes after so it is always above
			g.setColor(lineCol);
			g.draw(shapeOuter);
			// Draw text
			double maxTextLength = width-minradius*4;
			String text = Double.toString(di.value);
			double textLength = fm.stringWidth(text);
			/*
			 * What we should do is directly produce an exact correct length
			 * string which most closely represents the value, however,
			 * I'm lazy so for now it's implemented as a simple truncate
			 * which will often lose the exponent
			 */
			while(textLength>maxTextLength){
				text = text.substring(0, text.length()-1);
				textLength = fm.stringWidth(text);
			}
			Draw.drawTextImage(g, 0, 0, width, height, text, null, font, textCol, null, 0.5, 0.5, 0.0, 0);
			// No arrows if disabled, this indicates they cannot change it
			if(enabled){
				// Draw arrows
				double arrowWidth = minradius*ARROW_FAC;
				g.setColor(arrowCol);
				Path2D.Double arrowPath = new Path2D.Double();
				arrowPath.moveTo(width-minradius+arrowWidth, centery);
				arrowPath.moveTo(width-minradius, centery+arrowWidth);
				arrowPath.moveTo(width-minradius, centery-arrowWidth);
				arrowPath.closePath();
				g.fill(arrowPath);
				arrowPath = new Path2D.Double();
				arrowPath.moveTo(minradius-arrowWidth, centery);
				arrowPath.moveTo(minradius, centery+arrowWidth);
				arrowPath.moveTo(minradius, centery-arrowWidth);
				arrowPath.closePath();
				g.fill(arrowPath);
			}
		}
		
	}
	
}
