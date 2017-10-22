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
 * A skin for {@link TrackLSPreview}
 * <br>
 * Necessary because rendering is delegated to skin
 * 
 * @author EPICI
 * @version 1.0
 */
public class TrackLSPreviewSkin extends ComponentSkin {
	
	/**
	 * Extra space to leave above and below, in semitones
	 */
	public static final int PITCH_MARGIN = 4;

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
	
	public TrackLSPreviewSkin(){
		
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
		TrackLSPreview target = (TrackLSPreview) component;
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
		TrackLSPreview target = (TrackLSPreview) getComponent();
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
		TrackLSPreview target = (TrackLSPreview) getComponent();
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
	public void paint(Graphics2D graphics) {//TODO replace boolean gradient setting with variable resolution (1 per pixel max, 1 for whole screen min)
		/*
		 * TLS style bits
		 * 0 = toggle synth signature colour gradient or solid
		 */
		long style = 0;//TODO fetch from preferences when available
		boolean gradientSynthSig = (style&1)!=0;
		TrackLSPreview target = (TrackLSPreview) getComponent();
		TrackLayerSimple tls = target.target;
		updateWidth();updateHeight();
		int width = target.getWidth(), height = target.getHeight();
		double baseSpeed = comp.baseSpeed;
		double xadd = -gbounds[0]/baseSpeed, xmult = width*baseSpeed/(gbounds[1]-gbounds[0]);
		Color bg;
		if(colors==null){
			TerraTheme theme = (TerraTheme) Theme.getTheme();
			bg = theme.getBaseColor(1);
		}else{
			bg = colors.background;
		}
		Color bgdark = TerraTheme.darken(bg);
		IdentityHashMap<Pattern,BitSet> patterns = tls.patterns;
		int minPitch = 0, maxPitch = 0;
		if(gradientSynthSig){
			// Precalculate more values
			double invxmult = 1d/xmult;
			// Get x positions where rendering is needed
			IdentityHashMap<Synthesizer,BitSet> synthPos = new IdentityHashMap<>();
			for(Pattern pattern:patterns.keySet()){
				Synthesizer synth = pattern.getSynthesizer();
				BitSet xs = synthPos.get(synth);
				if(xs==null){
					xs = new BitSet(width);
					synthPos.put(synth, xs);
				}
				double ixmult = xmult/pattern.divisions;
				PrimitiveIterator.OfInt iter = patterns.get(pattern).stream().iterator();
				while(iter.hasNext()){
					int delay = iter.nextInt();
					double xoffset = (delay+xadd)*xmult;
					for(int[] clip:pattern.clips){
						int from = (int)Math.round(xoffset+clip[0]*ixmult);
						int to = from+(int)Math.ceil(clip[1]*xmult);
						xs.set(from,to);
					}
				}
				int lminPitch = Integer.MAX_VALUE, lmaxPitch = Integer.MIN_VALUE;
				for(int[] clip:pattern.clips){
					int pitch = clip[2];
					if(pitch<lminPitch)lminPitch=pitch;
					if(pitch>lmaxPitch)lmaxPitch=pitch;
				}
				if(lminPitch<minPitch)minPitch=lminPitch;
				if(lmaxPitch>maxPitch)maxPitch=lmaxPitch;
			}
			// Fetch synth colors
			IdentityHashMap<Synthesizer,Color[]> synthSigs = new IdentityHashMap<>();
			for(Synthesizer synth:synthPos.keySet()){
				Color[] sigs = new Color[width];
				PrimitiveIterator.OfInt iter = synthPos.get(synth).stream().iterator();
				while(iter.hasNext()){
					int x = iter.nextInt();
					double t = x*invxmult-xadd;
					sigs[x] = synth.getColorSignature(t);
				}
				synthSigs.put(synth, sigs);
			}
			minPitch -= PITCH_MARGIN;maxPitch += PITCH_MARGIN + 1;// Because they are bars, not lines, and take up a space
			int pitchRange = maxPitch - minPitch;
			double clipHeight = height/(double)pitchRange;
			int iclipHeight = (int)Math.ceil(clipHeight);
			// Precalculate y values (redundancy is okay because it's inexpensive)
			int[] ys = new int[pitchRange];
			for(int i=0;i<pitchRange;i++){
				int iy=(int)Math.round(clipHeight*(pitchRange-i-1));
				ys[i] = iy;
			}
			// Octave indicators (each doubling is one gradient repetition)
			for(int i=12*Math.floorDiv(maxPitch, 12);i>minPitch;i-=12){
				int y1 = (int)Math.round(clipHeight*(maxPitch-i)), y2 = (int)Math.round(clipHeight*(maxPitch-i+12));// Recalculate because it can be out of bounds
				graphics.setPaint(new GradientPaint(0,y1,bg,0,y2,bgdark));
				graphics.fillRect(0, y1, width, y2-y1);
			}
			// Octave indicators (each doubling is one gradient repetition)
			for(int i=12*Math.floorDiv(maxPitch, 12);i>minPitch;i-=12){
				int y1 = (int)Math.round(clipHeight*(maxPitch-i)), y2 = (int)Math.round(clipHeight*(maxPitch-i+12));// Recalculate because it can be out of bounds
				graphics.setPaint(new GradientPaint(0,y1,bg,0,y2,bgdark));
				graphics.fillRect(0, y1, width, y2-y1);
			}
			// Draw bars
			for(Pattern pattern:patterns.keySet()){
				Synthesizer synth = pattern.getSynthesizer();
				Color[] sigs = synthSigs.get(synth);
				double ixmult = xmult/pattern.divisions;
				PrimitiveIterator.OfInt iter = patterns.get(pattern).stream().iterator();
				while(iter.hasNext()){
					int delay = iter.nextInt();
					double xoffset = (delay+xadd)*xmult;
					for(int[] clip:pattern.clips){
						int from = (int)Math.round(xoffset+clip[0]*ixmult);
						int to = from + (int)Math.ceil(clip[1]*xmult);
						int iy = ys[clip[2]-minPitch];
						for(int x=from;x<to;x++){
							Color col = sigs[x];
							graphics.setColor(col);
							graphics.fillRect(x, iy, 1, iclipHeight);
						}
					}
				}
			}
		}else{
			// Fetch synth colors at t=0 and pitch bounds at the same time
			IdentityHashMap<Synthesizer,Color> synthSigs = new IdentityHashMap<>();
			for(Pattern pattern:patterns.keySet()){
				Synthesizer synth = pattern.getSynthesizer();
				Color sig = synthSigs.get(synth);
				if(sig==null){
					sig = synth.getColorSignature(0d);
					synthSigs.put(synth, sig);
				}
				int lminPitch = Integer.MAX_VALUE, lmaxPitch = Integer.MIN_VALUE;
				for(int[] clip:pattern.clips){
					int pitch = clip[2];
					if(pitch<lminPitch)lminPitch=pitch;
					if(pitch>lmaxPitch)lmaxPitch=pitch;
				}
				if(lminPitch<minPitch)minPitch=lminPitch;
				if(lmaxPitch>maxPitch)maxPitch=lmaxPitch;
			}
			minPitch -= PITCH_MARGIN;maxPitch += PITCH_MARGIN + 1;// Because they are bars, not lines, and take up a space
			int pitchRange = maxPitch - minPitch;
			double clipHeight = height/(double)pitchRange;
			int iclipHeight = (int)Math.ceil(clipHeight);
			// Precalculate y values (redundancy is okay because it's inexpensive)
			int[] ys = new int[pitchRange];
			for(int i=0;i<pitchRange;i++){
				int iy=(int)Math.round(clipHeight*(pitchRange-i-1));
				ys[i] = iy;
			}
			// Octave indicators (each doubling is one gradient repetition)
			for(int i=12*Math.floorDiv(maxPitch, 12);i>minPitch;i-=12){
				int y1 = (int)Math.round(clipHeight*(maxPitch-i)), y2 = (int)Math.round(clipHeight*(maxPitch-i+12));// Recalculate because it can be out of bounds
				graphics.setPaint(new GradientPaint(0,y1,bg,0,y2,bgdark));
				graphics.fillRect(0, y1, width, y2-y1);
			}
			// Draw bars
			for(Pattern pattern:patterns.keySet()){
				Synthesizer synth = pattern.getSynthesizer();
				Color sig = synthSigs.get(synth);
				graphics.setColor(sig);
				double ixmult = xmult/pattern.divisions;
				PrimitiveIterator.OfInt iter = patterns.get(pattern).stream().iterator();
				while(iter.hasNext()){
					int delay = iter.nextInt();
					double xoffset = (delay+xadd)*xmult;
					for(int[] clip:pattern.clips){
						graphics.fillRect((int)Math.round(xoffset+clip[0]*ixmult), ys[clip[2]-minPitch], (int)Math.ceil(clip[1]*xmult), iclipHeight);
					}
				}
			}
		}
		if(highlighted){
			graphics.setColor(new Color(0x22ffffff));
			graphics.fillRect(0, 0, width, height);
		}
	}

}
