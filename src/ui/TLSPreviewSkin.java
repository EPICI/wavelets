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
		TLSPreview target = (TLSPreview) getComponent();
		updateWidth();updateHeight();
		int width = target.getWidth(), height = target.getHeight();
		double invSpeedMult = 1d/comp.baseSpeed;
		double xadd = -gbounds[0]*invSpeedMult, xmult = width*invSpeedMult/(gbounds[1]-gbounds[0]);
		Color bg;
		if(colors==null){
			TerraTheme theme = (TerraTheme) Theme.getTheme();
			bg = theme.getBaseColor(1);
		}else{
			bg = colors.background;
		}
		Any.O2<ArrayList<Pattern>, ArrayList<Integer>> patterns = target.target.getPatterns();
		ArrayList<Pattern> patternl = patterns.a;
		ArrayList<Integer> delayl = patterns.b;
		int patterncount = patternl.size();
		IdentityHashMap<Pattern,int[]> ppitchBounds = new IdentityHashMap<>();
		IdentityHashMap<Synthesizer,Color[]> synthSigs = new IdentityHashMap<>();
		int minPitch = 0, maxPitch = 0;
		int alphaMask = (8+192/patterncount)<<24;
		for(Pattern pattern:patternl){
			int[] old = ppitchBounds.put(pattern, null);
			if(old==null){
				Synthesizer synth = pattern.getSynthesizer();
				if(synthSigs.get(synth)==null){
					Color sig = synth.getColorSignature(0d);
					synthSigs.put(synth, new Color[]{
							sig,// The base colour
							TerraTheme.darken(sig),// Darker, used for gradient
							new Color((TerraTheme.brighten(sig).getRGB()&0x00ffffff)|alphaMask)// Alpha, used for backdrop
							});
				}
				int lminPitch = Integer.MAX_VALUE, lmaxPitch = Integer.MIN_VALUE;
				for(int[] clip:pattern.clips){
					int pitch = clip[2];
					if(pitch<lminPitch)lminPitch=pitch;
					if(pitch>lmaxPitch)lmaxPitch=pitch;
				}
				if(lminPitch<minPitch)minPitch=lminPitch;
				if(lmaxPitch>maxPitch)maxPitch=lmaxPitch;
				ppitchBounds.put(pattern, new int[]{lminPitch,lmaxPitch});
			}
		}
		minPitch -= PITCH_MARGIN;maxPitch += PITCH_MARGIN;
		for(int[] lpitchBounds:ppitchBounds.values()){
			if(lpitchBounds[0]==Integer.MAX_VALUE && lpitchBounds[1]==Integer.MIN_VALUE){
				lpitchBounds[0] = minPitch;
				lpitchBounds[1] = maxPitch;
			}
		}
		int pitchRange = maxPitch - minPitch;
		double clipHeight = height/(double)pitchRange;
		for(int i=12*Math.floorDiv(maxPitch, 12);i>minPitch;i-=12){
			int y1 = (int)Math.round(clipHeight*(maxPitch-i)), y2 = (int)Math.round(clipHeight*(maxPitch-i+12));
			graphics.setPaint(new GradientPaint(0,y1,bg,0,y2,TerraTheme.darken(bg)));
			graphics.fillRect(0, y1, width, y2-y1);
		}
		graphics.setPaint(new GradientPaint(0,0,bg,0,height,TerraTheme.darken(bg)));
		graphics.fillRect(0, 0, width, height);
		// Split loop to ensure clips are always visible
		for(int i=0;i<patterncount;i++){
			Pattern pattern = patternl.get(i);
			Color[] sigCol = synthSigs.get(pattern.getSynthesizer());
			int[] lpitchBounds = ppitchBounds.get(pattern);
			int delay = delayl.get(i), lminPitch = lpitchBounds[0], lmaxPitch = lpitchBounds[1];
			double xoffset = (delay+xadd)*xmult;
			graphics.setColor(sigCol[2]);
			graphics.fillRoundRect((int)Math.round(xoffset), (int)Math.round(clipHeight*(maxPitch-lmaxPitch)), (int)Math.round(pattern.length*xmult), (int)Math.round(clipHeight*(lmaxPitch-lminPitch)), PATTERN_BACKDROP_ROUND_RADIUS, PATTERN_BACKDROP_ROUND_RADIUS);
		}
		for(int i=0;i<patterncount;i++){
			Pattern pattern = patternl.get(i);
			Color[] sigCol = synthSigs.get(pattern.getSynthesizer());
			int delay = delayl.get(i);
			double xoffset = (delay+xadd)*xmult;
			graphics.setPaint(new GradientPaint(0,0,sigCol[0],0,height,sigCol[1]));
			for(int[] clip:pattern.clips){
				graphics.fillRect((int)Math.round(xoffset+clip[0]*xmult), (int)Math.round(clipHeight*(maxPitch-clip[2])), (int)Math.round(clip[1]*xmult), (int)Math.round(clipHeight));
			}
		}
		if(highlighted){
			graphics.setColor(new Color(0x22ffffff));
			graphics.fillRect(0, 0, width, height);
		}
	}

}
