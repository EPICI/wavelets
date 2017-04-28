package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.*;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.skin.terra.*;

import core.ColorScheme;
import util.math.Angles;
import util.math.Floats;

/**
 * A skin for {@link CircularSlider} with the Terra theme.
 * 
 * @author EPICI
 * @version 1.0
 */
public class CircularSliderSkin extends TerraSliderSkin {
	
	/**
	 * A list of all ticks.
	 * Ordering here determines what order they draw in.
	 * <br>
	 * Encoding here is:
	 * <ol>
	 * <li>Angle in radians, so 0 is right and pi/2 is up</li>
	 * <li>Extension length as multiplier for radius, so 1.2
	 * is 20% outwards and 0.7 is 30% inwards</li>
	 * <li>Optional width override, so 4 is 4 pixels, negative signals ignore</li>
	 * <li>Optional color override hue, used modulo 1.0</li>
	 * <li>Optional color override saturation, in range 0-1</li>
	 * <li>Optional color override brightness, in range 0-1, negative signals ignore</li>
	 * </ol>
	 * Double arrays are used because they're convenient primitive wrappers.
	 * If the array is shorter than 2 it will be ignored.
	 * All colors must be provided if any are.
	 */
	public final ArrayList<double[]> ticks;
	
	/**
	 * The active color scheme. If null, uses the current theme or superclass
	 * colors.
	 */
	public ColorScheme colors;
	/**
	 * Color override for left of the thumb.
	 */
	public Color trackLeft;
	/**
	 * Color override for right of the thumb.
	 */
	public Color trackRight;
	
	protected transient boolean mouseDown = false;
	protected transient double lastAngle = 0d;

	/**
	 * Standard constructor. Initializes fields to defaults.
	 */
	public CircularSliderSkin() {
		super();
		ticks = new ArrayList<>();
	}
	
	@Override
	public boolean mouseMove(Component component,int x,int y){
		if(mouseDown){
			CircularSlider slider = (CircularSlider) getComponent();
			double cx = slider.getX()-0.5d*slider.getWidth(), cy = slider.getY()-0.5d*slider.getHeight();
			double dx = x-cx, dy = y-cy;
			double newAngle = Math.atan2(dx, -dy);
			double diff = Angles.limit(newAngle-lastAngle);
			slider.setCurAngle(slider.getCurAngle()+diff);
			lastAngle=newAngle;
		}
		return true;
	}
	
	@Override
	public boolean mouseDown(Component component,Mouse.Button button,int x,int y){
		switch(button){
		case LEFT:{
			CircularSlider slider = (CircularSlider) getComponent();
			double cx = slider.getX()-0.5d*slider.getWidth(), cy = slider.getY()-0.5d*slider.getHeight();
			mouseDown = true;
			double dx = x-cx, dy = y-cy;
			lastAngle = Math.atan2(dx, -dy);
			break;
		}
		case RIGHT:
		case MIDDLE:
		}
		return true;
	}
	
	@Override
	public boolean mouseUp(Component component,Mouse.Button button,int x,int y){
		switch(button){
		case LEFT:{
			mouseDown = false;
			break;
		}
		case RIGHT:
		case MIDDLE:
		}
		return true;
	}
	
	@Override
	public boolean mouseWheel(Component component,Mouse.ScrollType scrollType,int scrollAmount,int wheelRotation,int x,int y){
		CircularSlider slider = (CircularSlider) getComponent();
		double min = slider.getStart(), max = slider.getEnd(), val = slider.getValueExact()+scrollAmount;
		if(val<min)val = min;
		if(val>max)val = max;
		slider.setValueExact(val);
		return true;
	}
	
	@Override
	public void paint(Graphics2D graphics){
		// --- Pre-draw ---
		CircularSlider slider = (CircularSlider) getComponent();
		int width = getWidth(), height = getHeight();
		int cx = width>>1, cy = height>>1;
		int r = (Math.min(cx, cy)*3)>>2, thumbd = getThumbWidth();
		double minrad=slider.getMinAngle(), maxrad = slider.getMaxAngle(), currad = slider.getCurAngle(), diffrad = Angles.limitp(maxrad-minrad);
		int mindeg = (int)Math.round(Math.toDegrees(minrad)), maxdeg = (int)Math.round(Math.toDegrees(maxrad)), diffdeg = (int)Math.round(Math.toDegrees(diffrad));
		double curcos = Math.cos(currad), cursin = -Math.sin(currad), thumbr = thumbd*0.5d;//Sine is inverted because y is inverted
		double lmag = r-thumbr, hmag = r+thumbr, dmx = curcos*r, dmy = cursin*r;
		double lmx = curcos*lmag, lmy = cursin*lmag, hmx = curcos*hmag, hmy = cursin*hmag;
		int trackWidth = getTrackWidth();
		boolean enabled = slider.isEnabled();
		Color trackCol, buttonCol, tickCol, sectCol;
		if(colors==null){
			TerraTheme theme = (TerraTheme)Theme.getTheme();
			trackCol = getTrackColor();
			buttonCol = getButtonBackgroundColor();
			tickCol = enabled?theme.getBaseColor(4):theme.getBaseColor(2);
			sectCol = mouseDown?theme.getColor(17):theme.getColor(8);
		}else{
			trackCol = colors.line;
			buttonCol = colors.gradient;
			tickCol = enabled?colors.highlight:colors.line;
			sectCol = TerraTheme.brighten(mouseDown?colors.selected:colors.line);
		}
		
		// --- Drawing ---
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		// Inner sector fill
		graphics.setColor(sectCol);
		graphics.fillArc(cx-r, cy-r, r<<1, r<<1, maxdeg, 360-diffdeg);
		// Ticks
		for(double[] tick:ticks){
			int tlen = tick.length;
			if(tlen<2)continue;
			if(tlen>=3 && tick[2]>=0d){// Width override
				graphics.setStroke(new BasicStroke((float)tick[2],BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
			}else{
				graphics.setStroke(new BasicStroke(trackWidth,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
			}
			if(tlen>=6 && tick[5]>=0d){// Color override
				graphics.setColor(Color.getHSBColor((float)tick[3],enabled?(float)tick[4]:0f,(float)tick[5]));
			}else{
				graphics.setColor(tickCol);
			}
			double tickAngle = Angles.limit(tick[0]), tickExtend = r*tick[1];
			double tickCos = Math.cos(tickAngle), tickSin = -Math.sin(tickAngle);//Sine is inverted because y is inverted
			graphics.drawLine((int)Math.round(tickCos*r), (int)Math.round(tickSin*r), (int)Math.round(tickCos*tickExtend), (int)Math.round(tickSin*tickExtend));
		}
		// Outer sector outline
		graphics.setColor(trackCol);
		graphics.setStroke(new BasicStroke(trackWidth,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
		graphics.drawArc(cx-r, cy-r, r<<1, r<<1, mindeg, diffdeg);
		// Thumb
		graphics.setPaint((enabled&&!mouseDown)?new GradientPaint((float)lmx,(float)lmy,buttonCol,(float)hmx,(float)hmy,TerraTheme.brighten(buttonCol)):TerraTheme.darken(buttonCol));
		graphics.fillOval((int)Math.round(cx+dmx-thumbr), (int)Math.round(cy+dmy-thumbr), thumbd, thumbd);
	}

}
