package ui;

import core.*;
import org.apache.pivot.wtk.*;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.net.URL;
import java.util.*;
import org.apache.pivot.beans.*;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.content.ButtonData;
import org.apache.pivot.wtk.skin.*;
import org.apache.pivot.wtk.skin.terra.TerraTheme;

import util.ui.*;

/**
 * Editor UI for {@link TrackLayerSimple}
 * 
 * @author EPICI
 * @version 1.0
 */
public class TrackLSEditor extends Window implements Bindable {
	
	/**
	 * Extra space to leave above and below for a pattern, in semitones
	 */
	public static final int PITCH_MARGIN = 4;

	/**
	 * The current/linked session
	 */
	public Session session;
	/**
	 * The tab pane to switch between tracks
	 */
	public TabPane tabPane;
	/**
	 * Width of the floating sidebar in pixels
	 */
	public int sidebarWidth;
	/**
	 * Font size for text
	 */
	public int textSize;
	
	public TrackLSEditor(){
		super();
		sidebarWidth = 200;
	}
	
	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
		tabPane = (TabPane) namespace.get("tabPane");
	}

	/**
	 * The actual editor interface for the {@link TrackLayerSimple}.
	 * <br>
	 * Note that it is only a {@link Container} so it can hold components.
	 * None of the components are actually displayed, and would never need to
	 * be displayed, since no part of the UI is a separate component.
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public static class LinkedEditorPane extends Container implements Bindable{
		
		/**
		 * The parent {@link TrackLSEditor}
		 */
		public TrackLSEditor parent;
		/**
		 * The {@link TrackLayerSimple} for which this instance
		 * provides the UI
		 */
		public TrackLayerSimple view;
		/**
		 * The tab name
		 */
		public ButtonData tabName;
		
		@Override
		public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
			tabName = (ButtonData) namespace.get("tabName");
		}
		
		/**
		 * Initialize, called after setting fields
		 */
		public void init(){
			tabName.setText(view.getName());
		}
		
	}
	
	/**
	 * Skin for the {@link LinkedEditorPane}. By convention, it provides the UI
	 * and handles the rendering.
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public static class LinkedEditorPaneSkin extends ContainerSkin{
		
		/**
		 * The x position (world) which the top left corner
		 * of the pane corresponds to
		 */
		public double anchorx;
		/**
		 * The y position (world) which the top left corner
		 * of the pane corresponds to (+y is down not up)
		 */
		public double anchory;
		/**
		 * The x scale of the UI, maps measures (world) to pixel x
		 */
		public double scalex;
		/**
		 * The y scale of the UI, maps pattern rows (world) to pixel y
		 */
		public double scaley;
		/**
		 * Mouse button being held down (0 = not pressed, 1 = left, 2 = right, 3 = middle)
		 */
		public int mouseDown;
		/**
		 * Has the mouse moved since it was clicked down?
		 */
		public boolean mouseDragged;
		/**
		 * Is the shift key down?
		 */
		public boolean shiftHeld;
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
		/**
		 * Y index (nonnegative) if a button is being pressed, otherwise -1
		 */
		public int buttony;
		/**
		 * X-index of button being pressed, overridden by <i>buttony</i>
		 */
		public int buttonx;
		
		public LinkedEditorPaneSkin(){
			anchorx=0;
			anchory=0;
			scalex=500;
			scaley=300;
		}

		@Override
		public void layout() {
			//There are no displayed subcomponents, so nothing needs to be done here
		}
		
		@Override
		public void paint(Graphics2D graphics){
			// --- Get preferences ---
			/*
			 * TODO fetch from preferences when available
			 * Determines resolution of gradient:
			 * Call this x, a range of 2^x pixels is a solid tone
			 * 0 is individual pixels (highest resolution)
			 * 30 makes everything the same tone (lowest resolution), 31 would case overflow
			 */
			int gradientShift = 30, gradientShiftInc = 1<<gradientShift, gradientShiftMask = gradientShiftInc-1, gradientShiftMaskInv = ~gradientShiftMask;
			double imageScale = 0.5d;
			// --- Pre-draw ---
			LinkedEditorPane editor = (LinkedEditorPane) getComponent();
			TrackLayerSimple tls = editor.view;
			IdentityHashMap<Pattern,BitSet> patterns = tls.patterns;
			int width = getWidth(), height = getHeight();
			double anchorx = this.anchorx, anchory = this.anchory, scalex = this.scalex, scaley = this.scaley;// Cache
			Color bgCol, textCol, lineCol, buttonCol, errorCol, derrorCol;
			ColorScheme colors = editor.parent.session.getColors();
			if(colors==null){
				TerraTheme theme = (TerraTheme)Theme.getTheme();
				bgCol = theme.getBaseColor(1);
				textCol = theme.getBaseColor(0);
				lineCol = theme.getBaseColor(2);
				buttonCol = theme.getBaseColor(3);
				errorCol = theme.getBaseColor(7);
			}else{
				bgCol = colors.background;
				textCol = colors.text;
				lineCol = colors.line;
				buttonCol = colors.gradient;
				errorCol = colors.error;
			}
			derrorCol = ColorScheme.adjustHsb(errorCol, 0f, -0.5f, 0f);
			// Cache rows
			int patternCount = patterns.size();
			Pattern[] patternk = new Pattern[patternCount];
			BitSet[] patternv = new BitSet[patternCount];
			int i=0;
			for(Pattern k:patterns.keySet()){
				patternk[i]=k;
				patternv[i]=patterns.get(k);
				i++;
			}
			// Get bounds
			int swidth = editor.parent.sidebarWidth, ewidth = width-swidth;// Width remaining after sidebar
			double cornerx = anchorx+ewidth/scalex, cornery = anchory+height/scaley;
			int pfirst = (int)anchory, plast = 1+(int)cornery;//By convention it's first inclusive last exclusive
			boolean pextend = plast>patternCount;//Would it go past the last pattern?
			if(pextend)plast=patternCount;//Limit it
			int ifirst = (int)anchorx, ilast = 1+(int)cornerx;
			double iscalex = 1d/scalex, iscaley = 1d/scaley;
			Composition comp = editor.parent.session.composition;
			double speed = comp.baseSpeed;
			// --- Draw ---
			// Draw buttons
			int swidtha = swidth>>2, swidthb = swidth-swidtha;
			for(i=pfirst;i<plast;i++){
				int x1,y1,x2,y2;
				x1 = 0;
				y1 = (int)((i-anchory)*scaley);
				x2 = swidtha;
				y2 = (int)scaley;
				Draw.drawButton(graphics, x1, y1, x2, y2, "Delete", null, null, lineCol, derrorCol, mouseDown==1&&!mouseDragged&&lastMousex>x1&&lastMousex<x1+x2&&lastMousey>y1&&lastMousey<y1+y2, Color.BLACK, null, 0.5d, 0.5d, imageScale, 0);
				x1 = swidtha;
				x2 = swidthb;
				Draw.drawButton(graphics, x1, y1, x2, y2, "Edit", null, null, lineCol, buttonCol, mouseDown==1&&!mouseDragged&&lastMousex>x1&&lastMousex<x1+x2&&lastMousey>y1&&lastMousey<y1+y2, Color.BLACK, null, 0.5d, 0.5d, imageScale, 0);
			}
			if(pextend){
				int x1,y1,x2,y2;
				x1 = 0;
				y1 = (int)((plast-anchory)*scaley);
				x2 = swidtha;
				y2 = height-y1;
				Draw.drawButton(graphics, x1, y1, x2, y2, "Add new", null, null, lineCol, buttonCol, mouseDown==1&&!mouseDragged&&lastMousex>x1&&lastMousex<x1+x2&&lastMousey>y1&&lastMousey<y1+y2, Color.BLACK, null, 0.5d, 0.5d, imageScale, 0);
				x1 = swidtha;
				x2 = swidthb;
				Draw.drawButton(graphics, x1, y1, x2, y2, "Move here", null, null, lineCol, buttonCol, mouseDown==1&&!mouseDragged&&lastMousex>x1&&lastMousex<x1+x2&&lastMousey>y1&&lastMousey<y1+y2, Color.BLACK, null, 0.5d, 0.5d, imageScale, 0);
			}
			// Draw patterns
			graphics = (Graphics2D) graphics.create(swidth,0,ewidth,height);
			IdentityHashMap<Synthesizer,Color[]> synthSigs = new IdentityHashMap<>();
			int xrange = ((ewidth-1)>>gradientShift)+1;
			for(i=pfirst;i<plast;i++){
				Graphics2D dgraphics = (Graphics2D) graphics.create(0, (int)((i-anchory)*scaley), ewidth, (int)scaley);
				Pattern pk = patternk[i];
				BitSet pv = patternv[i];
				int pl = pk.length, pd = pk.divisions, ipf = ifirst-pl, pmin = 0, pmax = 0;
				ArrayList<int[]> pclips = pk.clips;
				Synthesizer synth = pk.getSynthesizer();
				Color[] sigs = synthSigs.get(synth);
				if(sigs==null){
					sigs = new Color[xrange];
					synthSigs.put(synth, sigs);
					for(int j=0;j<xrange;j++){
						double t=((j<<gradientShift)*iscalex+anchorx)*speed;
						sigs[j] = synth.getColorSignature(t);
					}
				}
				for(int[] clip:pclips){
					int pitch = clip[2];
					if(pitch<pmin)pmin=pitch;
					if(pitch>pmax)pmax=pitch;
				}
				pmin -= PITCH_MARGIN;
				pmax += PITCH_MARGIN+1;
				int prange = pmax-pmin;
				double clipHeight = height/(double)prange;
				int iclipHeight = (int)Math.ceil(clipHeight);
				// Precalculate y values (redundancy is okay because it's inexpensive)
				int[] ys = new int[prange];
				for(i=0;i<prange;i++){
					int iy=(int)Math.round(clipHeight*(prange-i-1));
					ys[i] = iy;
				}
				// Octave indicators (each doubling is one gradient repetition)
				for(int j=12*Math.floorDiv(pmax, 12);j>pmin;j-=12){
					int y1 = (int)Math.round(clipHeight*(pmax-j)), y2 = (int)Math.round(clipHeight*(pmax-j+12));// Recalculate because it can be out of bounds
					graphics.setPaint(new GradientPaint(0,y1,ColorScheme.brighten(buttonCol, 0.1f),0,y2, buttonCol));
					graphics.fillRect(0, y1, width, y2-y1);
				}
				double ixmult = scalex/pd;
				PrimitiveIterator.OfInt iter = pv.stream().iterator();
				int delay=Integer.MAX_VALUE;
				while(iter.hasNext()){
					// Skip until it's in the range
					delay = iter.nextInt();
					if(delay>ipf)break;
				}
				while(delay<ilast&&iter.hasNext()){
					// Draw one
					double xoffset = (delay-anchorx)*scalex;
					for(int[] clip:pclips){
						int from = (int)Math.round(xoffset+clip[0]*ixmult);
						int to = from + (int)Math.ceil(clip[1]*scalex);
						int iy = ys[clip[2]-pmin];
						int nf = (from&gradientShiftMaskInv)+gradientShiftInc;
						int nt = (to-1)&gradientShiftMaskInv;
						Color col;
						if(nf>=nt){// Enough distance between endpoints
							col = sigs[from>>gradientShift];
							dgraphics.setColor(col);
							dgraphics.fillRect(from, iy, nf-from, iclipHeight);
							for(int x=nf;x<nt;x+=gradientShiftInc){
								col = sigs[x>>gradientShift];
								dgraphics.setColor(col);
								dgraphics.fillRect(x, iy, gradientShiftInc, iclipHeight);
							}
							col = sigs[nt>>gradientShift];
							dgraphics.setColor(col);
							dgraphics.fillRect(nt, iy, to-nt, iclipHeight);
						}else{// Only one block
							col = sigs[from>>gradientShift];
							dgraphics.setColor(col);
							dgraphics.fillRect(from, iy, to-from, iclipHeight);
						}
					}
					// Finally, fetch next
					delay = iter.nextInt();
				}
			}
		}
	}
}
