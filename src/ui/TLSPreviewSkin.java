package ui;

import java.util.*;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.GradientPaint;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.skin.*;
import org.apache.pivot.wtk.skin.terra.*;
import javax.swing.JInternalFrame;
import core.*;
import util.*;

/**
 * A skin for {@link TLSPreview}
 * <br>
 * Necessary because rendering is delegated to skin
 * 
 * @author EPICI
 * @version 1.0
 */
public class TLSPreviewSkin extends ComponentSkin {
	
	/**
	 * Extra space to leave above and below, in semitones
	 */
	public static final int PITCH_MARGIN = 4;
	/**
	 * Radius for rounding for pattern backdrop
	 */
	public static final int PATTERN_BACKDROP_ROUND_RADIUS = 16;

	/**
	 * Active color scheme. If null, defaults to theme colors.
	 */
	public ColorScheme colors;
	
	public transient Preferences preferences;
	protected transient TrackLayerSimple tls;
	protected transient Composition comp;
	protected transient int width, height;
	protected transient double[] gbounds, lbounds;
	protected transient boolean highlighted = false;
	
	public TLSPreviewSkin(){
		
	}

	@Override
	public void layout() {
		
	}
	
	@Override
	public void mouseOver(Component component){
		highlighted = true;
		component.repaint();
	}
	
	@Override
	public void mouseOut(Component component){
		highlighted = false;
		component.repaint();
	}
	
	@Override
	public boolean mouseClick(Component component,Mouse.Button button,int x,int y,int count){
		TLSPreview target = (TLSPreview) component;
		tls = target.target;
		MetaComponent<JInternalFrame> meta = tls.getUI();
		comp = tls.parentComposition();
		Session session = comp.currentSession;
		session.windowManager.addWindow(meta);
		return true;
	}
	
	/**
	 * Calculate the correct width, set the transient field, and return it
	 * 
	 * @return the correct width
	 */
	public int updateWidth(){
		TLSPreview target = (TLSPreview) getComponent();
		tls = target.target;
		lbounds = tls.getTimeBounds();
		comp = tls.parentComposition();
		gbounds = comp.tracks.getTimeBounds();
		double diffs = gbounds[1]-gbounds[0];
		return width = (int) (diffs/comp.baseSpeed*target.pixelsPerMeasure);
	}
	
	/**
	 * Calculate the correct height, set the transient field, and return it
	 * 
	 * @return the correct height
	 */
	public int updateHeight(){
		TLSPreview target = (TLSPreview) getComponent();
		return height = target.height;
	}

	@Override
	public int getPreferredHeight(int width) {
		return updateHeight();
	}

	@Override
	public int getPreferredWidth(int height) {
		return updateWidth();
	}

	@Override
	public void paint(Graphics2D graphics) {
		//TODO
		long style = 0;//TODO fetch from preferences when available
	}

}
