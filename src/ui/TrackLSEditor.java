package ui;

import core.*;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.Mouse.Button;
import org.apache.pivot.wtk.Mouse.ScrollType;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.*;
import org.apache.pivot.beans.*;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.content.ButtonData;
import org.apache.pivot.wtk.skin.*;
import org.apache.pivot.wtk.skin.terra.TerraTheme;

import util.Bits;
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
	 * The area fill for selected instances
	 */
	public static final Color SELECTED_FILL = new Color(255,255,255,64);

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
		 * Temporary unbounded anchorx
		 */
		public double uanchorx;
		/**
		 * Temporary unbounded anchory
		 */
		public double uanchory;
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
		 * <i>originMousex</i> mapped to world x when dragging began
		 */
		public double originWorldx;
		/**
		 * <i>originMousey</i> mapped to world y when dragging began
		 */
		public double originWorldy;
		/**
		 * The mouse x, where it was last seen
		 */
		public int lastMousex;
		/**
		 * The mouse y, where it was last seen
		 */
		public int lastMousey;
		/**
		 * Y-index (nonnegative) if a button is being pressed, otherwise -1
		 * <br>
		 * Ex. the third button from the top would be 2
		 */
		public int buttony;
		/**
		 * X-index (nonnegative) if a button is being pressed, otherwise -1
		 * <br>
		 * Ex. the fourth button from the left would be 3
		 */
		public int buttonx;
		
		/**
		 * Selection as offsets for each pattern
		 * <br>
		 * Please use the getter method for safety
		 * <br>
		 * Null if unset
		 */
		protected IdentityHashMap<Pattern,BitSet> selection;
		
		/**
		 * Get the selection, never returns null
		 * <br>
		 * Format: suppose a pair (k,v) exists, then let u be the
		 * index of a set bit in v, then an instance of pattern k
		 * at offset u measures exists in the selection
		 * <br>
		 * If you're still confused, look at {@link TrackLayerSimple}
		 * or the source here and how this data is handled
		 * 
		 * @param cleanup flag to iterate through and remove dead patterns
		 * @param clear flag to reset and return an empty selection
		 * @return the selection
		 */
		public IdentityHashMap<Pattern,BitSet> getSelection(boolean cleanup,boolean clear){
			if(clear||selection==null){
				return selection = new IdentityHashMap<>();
			}
			IdentityHashMap<Pattern,BitSet> r = selection;
			if(cleanup){
				Iterator<Pattern> iter = r.keySet().iterator();
				while(iter.hasNext()){
					Pattern p = iter.next();
					if(p.isDestroyed())iter.remove();
				}
			}
			return r;
		}
		
		public LinkedEditorPaneSkin(){
			anchorx=0;
			anchory=0;
			scalex=500;
			scaley=300;
			/*
			 * No need to add listeners, since Pivot takes care of that
			 * for us as long as we implement the methods here
			 */
		}
		
		/**
		 * It is possible to drag patterns across rows, which would
		 * change the types of patterns by turning all instances from
		 * the source pattern type to the destination pattern type,
		 * should we allow it?
		 * 
		 * @return
		 */
		public boolean allowPatternConvert(){
			LinkedEditorPane editor = (LinkedEditorPane)getComponent();
			return Preferences.getBooleanSafe(editor.parent.session,Preferences.INDEX_TLS_ALLOW_PATTERN_CONVERT);
		}
		
		/**
		 * It is possible to drag patterns on top of other patterns,
		 * and since we can't have overlaps, the duplicate is effectively
		 * destroyed and not recoverable, should we allow it?
		 * 
		 * @return
		 */
		public boolean allowPatternMerge(){
			LinkedEditorPane editor = (LinkedEditorPane)getComponent();
			return Preferences.getBooleanSafe(editor.parent.session,Preferences.INDEX_TLS_ALLOW_PATTERN_MERGE);
		}
		
		/**
		 * Try to move, return true if successful, false if failed (in which case
		 * nothing happens)
		 * <ul>
		 * <li>If dx and dy are both 0, rejects</li>
		 * <li>If dy is not 0 and pattern converting is disabled, rejects</li>
		 * <li>If patterns would be moved out of bounds, rejects</li>
		 * <li>If the moved selection overlaps unselected and merging is disabled, rejects</li>
		 * </ul>
		 * It's all in one function for convenience (not very efficient)
		 * 
		 * @param dx how many right
		 * @param dy how many down
		 * @param doMove whether to actually move if possible, or only test
		 * @return if it's allowed
		 */
		public boolean tryMove(int dx,int dy,boolean doMove){
			if(dx==0&&dy==0||dy!=0&&!allowPatternConvert())return false;
			LinkedEditorPane editor = (LinkedEditorPane)getComponent();
			TrackLayerSimple tls = editor.view;
			IdentityHashMap<Pattern,BitSet> selection = getSelection(true,false),
					patterns = tls.patterns;
			int n = patterns.size();
			Pattern[] pk = new Pattern[n];// Cache all pattern instances
			BitSet[] pv = new BitSet[n];
			BitSet[] ps = new BitSet[n];// Cache selection as well
			IdentityHashMap<Pattern,Integer> index = new IdentityHashMap<>();
			int i=0;
			for(Pattern p:patterns.keySet()){// Iteration order matters here
				pk[i]=p;
				BitSet s=pv[i]=(BitSet)patterns.get(p).clone();// Work with a copy
				index.put(p, i);
				BitSet b=ps[i]=selection.get(p);
				if(b!=null){
					int j=i+dy;
					if(j<0||j>=n||b.nextSetBit(0)+dx<0)return false;
					s.andNot(b);
				}
				i++;
			}
			boolean apm=allowPatternMerge();
			// Check first for no mutate requirement
			for(Pattern p:selection.keySet()){// Iteration order doesn't matter
				i=index.get(p);
				BitSet ips = Bits.shiftLeft(ps[i], dx);
				BitSet ipv = pv[i+dy];
				if(!apm&&ipv.intersects(ips))return false;
				ipv.or(ips);
			}
			if(doMove){
				for(Pattern p:selection.keySet()){
					i=index.get(p);
					patterns.put(pk[i+dy], pv[i+dy]);// It was copied before
				}
			}
			return true;
		}
		
		@Override
		public boolean mouseMove(Component component, int x, int y) {
			// Tracking data
			int lastMousex = this.lastMousex, lastMousey = this.lastMousey;
			this.lastMousex = x;
			this.lastMousey = y;
			mouseDragged = true;
			int mouseDown = this.mouseDown;
			// Skip if mouse not held
			if(mouseDown!=0){
				// --- Fetch some data ---
				LinkedEditorPane editor = (LinkedEditorPane) getComponent();
				//Session session = editor.parent.session;
				//TrackLayerSimple tls = editor.view;
				int width = getWidth(), height = getHeight();
				// Local variables are faster + inversion
				//IdentityHashMap<Pattern,BitSet> patterns = tls.patterns;
				double anchorx = this.anchorx, anchory = this.anchory,
						scalex = this.scalex, scaley = this.scaley,
						iscalex = 1d/scalex, iscaley = 1d/scaley;
				// Get bounds
				int swidth = editor.parent.sidebarWidth, ewidth = width-swidth;// Width remaining after sidebar
				int[] swidths = sidebarColumnWidths(swidth);
				int swidtha = swidths[0], swidthb = swidths[1];
				// Handle the event
				if(mouseDown==1){
					// Left mouse held -> drag to move
				}else if(mouseDown==2){
					// Right mouse held -> drag to select
				}else if(mouseDown==3){
					// Middle mouse held -> drag to scroll
					uanchorx += (x-lastMousex)/scalex;
					uanchory += (y-lastMousey)/scaley;
					anchorx = Math.max(0, uanchorx);
					anchory = Math.max(0, uanchory);
				}
				// TODO Auto-generated method stub
			}
			return true;// Consume the event
		}

		@Override
		public void mouseOut(Component component) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseOver(Component component) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public boolean mouseClick(Component component, Button button, int x, int y, int count) {
			// TODO Auto-generated method stub
			return true;// Consume the event
		}

		@Override
		public boolean mouseDown(Component component, Button button, int x, int y) {
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
			double iscalex = 1d/scalex, iscaley = 1d/scaley;
			uanchorx = anchorx;
			uanchory = anchory;
			originMousex = x;
			originMousey = y;
			originWorldx = x*iscalex+anchorx;
			originWorldy = y*iscaley+anchory;
			lastMousex = x;
			lastMousey = y;
			mouseDragged = false;
			// TODO Auto-generated method stub
			return true;// Consume the event
		}

		@Override
		public boolean mouseUp(Component component, Button button, int x, int y) {
			// --- Fetch some data ---
			LinkedEditorPane editor = (LinkedEditorPane) getComponent();
			Session session = editor.parent.session;
			TrackLayerSimple tls = editor.view;
			int width = getWidth(), height = getHeight();
			// Local variables are faster + inversion
			IdentityHashMap<Pattern,BitSet> patterns = tls.patterns;
			int n = patterns.size();
			Pattern[] pk = new Pattern[n];
			BitSet[] pv = new BitSet[n];
			int i=0;
			for(Pattern p:patterns.keySet()){
				pk[i]=p;
				pv[i]=patterns.get(p);
				i++;
			}
			double anchorx = this.anchorx, anchory = this.anchory,
					scalex = this.scalex, scaley = this.scaley,
					iscalex = 1d/scalex, iscaley = 1d/scaley;
			boolean mouseDragged = this.mouseDragged;
			// Get bounds
			int swidth = editor.parent.sidebarWidth, ewidth = width-swidth;// Width remaining after sidebar
			int[] swidths = sidebarColumnWidths(swidth);
			int swidtha = swidths[0], swidthb = swidths[1];
			double wx = x*iscalex+anchorx, wy = y*iscaley+anchory;
			int iwx = (int)wx, iwy = (int)wy;
			double owx = originWorldx, owy = originWorldy;
			int iowx = (int)owx, iowy = (int)owy;
			boolean yextend = iwy>=n;
			boolean la = x<swidth, lb = originMousex<swidth;
			// Handle the event
			if(mouseDown==1){
				// Left click/drag
				// Note: mouse not dragged implies la=lb
				if(mouseDragged){
					if(!(la|lb)){// Left drag -> move patterns
						double dx = wx-originWorldx,   dy = wy-originWorldy;
						int    idx=(int)Math.round(dx),idy=(int)Math.round(dy);
						tryMove(idx,idy,true);// Move if possible
					}// Leave nested to allow for easier addition of future behaviours
				}else{
					if(la){// Left click -> press button
						if(yextend){// Past last row, special
							if(x<swidtha){// Add new
								// TODO also pattern editor
							}else{// Move here
								if(session!=null){
									Object clip = session.clipBoard;
									if(clip!=null){
										if(clip instanceof Pattern){
											Pattern pclip = (Pattern)clip;
											if(!patterns.containsKey(pclip))patterns.put(pclip, new BitSet());
										}// Leave nested to allow for easier addition of future behaviours
									}
								}
							}
						}else{// In pattern row
							if(x<swidtha){// Delete
								patterns.remove(pk[iwy]);
							}else{// Edit
								// TODO pattern editor
							}
						}
					}else if(!yextend){
						// Quick toggle pattern
						pv[iwy].flip(iwx);
					}// Leave nested to allow for easier addition of future behaviours
				}
			}else if(mouseDown==2){
				// Right click/drag
				if(mouseDragged){// drag -> select
					if(!(la|lb)){
						// shift = invert, not shift = set
						IdentityHashMap<Pattern,BitSet> selection = getSelection(true,!shiftHeld);
						int xmin=iowx,xmax=iwx,ymin=iowy,ymax=iwy;
						if(xmin>xmax){
							int t=xmin;
							xmin=xmax;
							xmax=t;
						}
						if(ymin>ymax){
							int t=ymin;
							ymin=ymax;
							ymax=t;
						}
						ymax=Math.min(ymax, n-1);// Can't go past last row
						for(i=ymin;i<=ymax;i++){
							BitSet b = selection.get(pk[i]);
							if(b==null){
								b = new BitSet();
								selection.put(pk[i], b);
							}
							b.flip(xmin, xmax);// Invert
							b.and(pv[i]);// Can't select if it doesn't exist
						}
					}// Leave nested to allow for easier addition of future behaviours
				}else{// click -> open context menu
					
				}
			}else if(mouseDown==3){
				// Middle click/drag
				if(mouseDragged){// drag -> scroll
					
				}// Leave nested to allow for easier addition of future behaviours
			}
			// Signal the mouse not being pressed anymore
			mouseDown = 0;
			// May need repainting
			editor.repaint();
			return true;// Consume the event
		}
		
		@Override
		public boolean mouseWheel(Component component, ScrollType scrollType, int scrollAmount, int wheelRotation, int x, int y) {
			// TODO Auto-generated method stub
			return true;// Consume the event
		}
		
		@Override
		public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
			if(keyCode==KeyEvent.VK_SHIFT)shiftHeld = true;
			return true;// Consume the event
		}

		@Override
		public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
			if(keyCode==KeyEvent.VK_SHIFT)shiftHeld = false;
			return true;// Consume the event
		}

		@Override
		public boolean keyTyped(Component component, char character) {
			// TODO Auto-generated method stub
			return true;// Consume the event
		}

		@Override
		public void layout() {
			//There are no displayed subcomponents, so nothing needs to be done here
		}
		
		@Override
		public void paint(Graphics2D graphics){
			// --- Fetch some data ---
			LinkedEditorPane editor = (LinkedEditorPane) getComponent();
			Session session = editor.parent.session;
			TrackLayerSimple tls = editor.view;
			int width = getWidth(), height = getHeight();
			/*
			 * Determines resolution of gradient:
			 * Call this x, a range of 2^x pixels is a solid tone
			 * 0 is individual pixels (highest resolution)
			 * 30 makes everything the same tone (lowest resolution), 31 would cause overflow
			 */
			int gradientShift = (int)Preferences.getLongSafe(session, Preferences.INDEX_TLS_PATTERN_BAR_GRADIENT_SHIFT), gradientShiftInc = 1<<gradientShift, gradientShiftMask = gradientShiftInc-1, gradientShiftMaskInv = ~gradientShiftMask;
			double imageScale = 0.5d;
			// --- Pre-draw ---
			IdentityHashMap<Pattern,BitSet> patterns = tls.patterns;
			IdentityHashMap<Pattern,BitSet> selection = getSelection(false,false);
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
			BitSet[] selectv = new BitSet[patternCount];
			int i=0;
			for(Pattern k:patterns.keySet()){
				patternk[i]=k;
				patternv[i]=patterns.get(k);
				BitSet b = selection.get(k);
				if(b!=null)selectv[i]=b;
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
			int[] swidths = sidebarColumnWidths(swidth);
			int swidtha = swidths[0], swidthb = swidths[1];
			for(i=pfirst;i<plast;i++){
				int x1,y1,x2,y2;
				x1 = 0;
				y1 = (int)((i-anchory)*scaley);
				x2 = swidtha;
				y2 = (int)scaley;// TODO use buttonx and buttony instead to determine if pressed
				Draw.drawButton(graphics, x1, y1, x2, y2, "Delete", null, null, lineCol, derrorCol, mouseDown==1&&!mouseDragged&&lastMousex>x1&&lastMousex<x1+x2&&lastMousey>y1&&lastMousey<y1+y2, textCol, null, 0.5d, 0.5d, imageScale, 0);
				x1 = swidtha;
				x2 = swidthb;
				Draw.drawButton(graphics, x1, y1, x2, y2, "Edit", null, null, lineCol, buttonCol, mouseDown==1&&!mouseDragged&&lastMousex>x1&&lastMousex<x1+x2&&lastMousey>y1&&lastMousey<y1+y2, textCol, null, 0.5d, 0.5d, imageScale, 0);
			}
			if(pextend){
				int x1,y1,x2,y2;
				x1 = 0;
				y1 = (int)((plast-anchory)*scaley);
				x2 = swidtha;
				y2 = height-y1;
				Draw.drawButton(graphics, x1, y1, x2, y2, "Add new", null, null, lineCol, buttonCol, mouseDown==1&&!mouseDragged&&lastMousex>x1&&lastMousex<x1+x2&&lastMousey>y1&&lastMousey<y1+y2, textCol, null, 0.5d, 0.5d, imageScale, 0);
				x1 = swidtha;
				x2 = swidthb;
				Draw.drawButton(graphics, x1, y1, x2, y2, "Move here", null, null, lineCol, buttonCol, mouseDown==1&&!mouseDragged&&lastMousex>x1&&lastMousex<x1+x2&&lastMousey>y1&&lastMousey<y1+y2, textCol, null, 0.5d, 0.5d, imageScale, 0);
			}
			// Draw patterns
			graphics = (Graphics2D) graphics.create(swidth,0,ewidth,height);
			IdentityHashMap<Synthesizer,Color[]> synthSigs = new IdentityHashMap<>();
			double dxgradientInc = gradientShiftInc*iscalex;
			int xrefBlocks = (int)(anchorx/dxgradientInc);
			double dxref = xrefBlocks*dxgradientInc;
			int xref = -((int)((anchorx-dxref)*scalex));//First x for gradient, cannot be positive
			int xrange = ((ewidth-xref-1)>>gradientShift)+1;
			for(i=pfirst;i<plast;i++){
				// Graphics for region
				int iheight = (int)scaley;
				Graphics2D dgraphics = (Graphics2D) graphics.create(0, (int)((i-anchory)*scaley), ewidth, iheight);
				// Separator lines
				dgraphics.setColor(lineCol);
				dgraphics.fillRect(0,0,ewidth,1);
				for(int j=ifirst;j<ilast;j++){
					dgraphics.fillRect((int)((j-anchorx)*scalex), 0, 1, iheight);
				}
				// Fetch data
				Pattern pk = patternk[i];
				BitSet pv = patternv[i];
				BitSet sv = selectv[i];
				int pl = pk.length, pd = pk.divisions, ipf = ifirst-pl, pmin = 0, pmax = 0;
				ArrayList<int[]> pclips = pk.clips;
				Synthesizer synth = pk.getSynthesizer();
				Color[] sigs = synthSigs.get(synth);
				if(sigs==null){
					sigs = new Color[xrange];
					synthSigs.put(synth, sigs);
					for(int j=0;j<xrange;j++){
						double t=((j<<gradientShift)*iscalex+dxref)*speed;
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
					// If selected, fill
					boolean selected = sv!=null&&sv.get(delay);
					if(selected){
						graphics.setColor(SELECTED_FILL);
						int x1 = (int)Math.round(anchorx+scalex*delay);
						int x2 = (int)Math.round(anchorx+scalex*(delay+1));
						int y1 = (int)Math.round(anchory+scaley*i);
						int y2 = (int)Math.round(anchory+scaley*(i+1));
						graphics.fillRect(x1,y2,x2-x1,y2-y1);
					}
					// Draw one
					double xoffset = (delay-anchorx)*scalex;
					for(int[] clip:pclips){
						int from = (int)Math.round(xoffset+clip[0]*ixmult)-xref;
						int to = from + (int)Math.ceil(clip[1]*ixmult)-xref;
						int iy = ys[clip[2]-pmin];
						int nf = (from&gradientShiftMaskInv)+gradientShiftInc;
						int nt = (to-1)&gradientShiftMaskInv;
						Color col;
						if(nf>=nt){// Enough distance between endpoints
							col = sigs[from>>gradientShift];
							dgraphics.setColor(col);
							dgraphics.fillRect(from+xref, iy, nf-from, iclipHeight);
							for(int x=nf;x<nt;x+=gradientShiftInc){
								col = sigs[x>>gradientShift];
								dgraphics.setColor(col);
								dgraphics.fillRect(x+xref, iy, gradientShiftInc, iclipHeight);
							}
							col = sigs[nt>>gradientShift];
							dgraphics.setColor(col);
							dgraphics.fillRect(nt+xref, iy, to-nt, iclipHeight);
						}else{// Only one block
							col = sigs[from>>gradientShift];
							dgraphics.setColor(col);
							dgraphics.fillRect(from+xref, iy, to-from, iclipHeight);
						}
					}
					// Finally, fetch next
					delay = iter.nextInt();
				}
				Draw.drawTextImage(dgraphics, 0, 0, 0, 0, pk.getName(), null, null, ColorScheme.brighten(sigs[0], -0.3f), null, 0d, 0d, 0d, 0);
			}
		}
	}
	
	/**
	 * Widths of the columns for the sidebar buttons
	 * <br>
	 * Will sum to <i>swidth</i>
	 * <br>
	 * May be 0 if <i>swidth</i> is small, but never negative
	 * <br>
	 * Behaviour for negative <i>swidth</i> is unspecified
	 * (it should never happen anyway)
	 * 
	 * @param swidth total width of the sidebar
	 * @return widths of each column
	 */
	public static int[] sidebarColumnWidths(int swidth){
		int swidtha = swidth>>2;
		return new int[]{swidtha,swidth-swidtha};
	}
}
