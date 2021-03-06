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
import javax.swing.JInternalFrame;
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
public class TrackLSEditor extends DataEditor.Tabbed<TrackLayerSimple> {
	
	/**
	 * Extra space to leave above and below for a pattern, in semitones
	 */
	public static final int PITCH_MARGIN = 4;
	/**
	 * Smallest possible value for x or y scale
	 */
	public static final double SCALE_MIN = 4;
	/**
	 * Largest possible value for x or y scale
	 */
	public static final double SCALE_MAX = 1<<13;
	/**
	 * Width of select/highlight border
	 */
	public static final int SHL_BORDER_WIDTH = 2;
	/**
	 * Minimum width and height to show that border
	 */
	public static final int SHL_BORDER_THRESHOLD = SHL_BORDER_WIDTH*5;

	/**
	 * Width of the floating sidebar in pixels
	 */
	public int sidebarWidth;
	
	/**
	 * Convenience method to create a new one from outside
	 * 
	 * @return
	 */
	public static TrackLSEditor createNew(){
		return PivotSwingUtils.loadBxml(TrackLSEditor.class, "trackLSEditor.bxml");
	}
	
	public TrackLSEditor(){
		super();
		sidebarWidth = 200;
	}
	
	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
		super.initialize(namespace, location, resources);
	}
	
	@Override
	public void init(){
		super.init();
	}
	
	@Override
	protected void addNewEditorData(TrackLayerSimple track){
		try{
			TabPane.TabSequence tabs = tabPane.getTabs();
			LinkedEditorPane linked = LinkedEditorPane.createNew();
			linked.parent = this;
			linked.view = track;
			linked.init();
			tabs.add(linked);
		}catch(NullPointerException exception){
			exception.printStackTrace();
		}
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
	public static class LinkedEditorPane extends Container implements Bindable, DataEditor.Instance<TrackLayerSimple>{
		
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
			installSkin(LinkedEditorPane.class);
		}
		
		/**
		 * Initialize, called after setting fields
		 */
		public void init(){
			tabName.setText(view.getName());
		}
		
		/**
		 * Convenience method to create a new one from outside
		 * 
		 * @return
		 */
		public static LinkedEditorPane createNew(){
			return PivotSwingUtils.loadBxml(LinkedEditorPane.class, "trackLSEditorPane.bxml");
		}
		
		@Override
		public TrackLayerSimple getEditorData(){
			return view;
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
		 * Selection as offsets for each pattern
		 * <br>
		 * Please use the getter method for safety
		 * <br>
		 * Null if unset
		 */
		protected IdentityHashMap<Pattern,BitSet> selection;
		
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
				r.keySet().removeIf((Pattern p)->p.isDestroyed());
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
			return Preferences.getBooleanSafe(editor.parent.session,Preferences.INDEX_BOOLEAN_TLS_ALLOW_PATTERN_CONVERT);
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
			return Preferences.getBooleanSafe(editor.parent.session,Preferences.INDEX_BOOLEAN_TLS_ALLOW_PATTERN_MERGE);
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
		
		/**
		 * Get 2-tuple of x index, y index of button being pressed
		 * <br>
		 * So the 4th from the top and 7th from the left would be (6,3)
		 * <br>
		 * If no button is pressed, returns null
		 * 
		 * @param ignore if true, will ignore the current click/drag mouse state
		 * @return
		 */
		public int[] buttonxy(boolean ignore){
			if(!ignore&&(mouseDown!=1||mouseDragged))return null;
			LinkedEditorPane editor = (LinkedEditorPane) getComponent();
			TrackLayerSimple tls = editor.view;
			int mousex = lastMousex, mousey = lastMousey;
			if(mousex<0||mousey<0)return null;// Out of bounds signals mouse not over editor
			double anchorx = this.anchorx, anchory = this.anchory,
					scalex = this.scalex, scaley = this.scaley,
					iscalex = 1d/scalex, iscaley = 1d/scaley;
			int swidth = editor.parent.sidebarWidth;// Width remaining after sidebar
			int[] swidths = sidebarColumnWidths(swidth);
			int swidthn = swidths.length;
			int ry=Math.min((int)(mousey*iscaley+anchory),tls.patterns.size());
			int rx=0;
			while(rx<swidthn&&swidths[rx]<mousex){
				mousex-=swidths[rx];
				rx++;
			}
			if(rx==swidthn)return null;
			return new int[]{rx,ry};
		}
		
		@Override
		public boolean mouseMove(Component component, int x, int y) {
			// Tracking data
			int lastMousex = this.lastMousex, lastMousey = this.lastMousey;
			this.lastMousex = x;
			this.lastMousey = y;
			mouseDragged = true;
			int mouseDown = this.mouseDown;
			LinkedEditorPane editor = (LinkedEditorPane) getComponent();
			int width = getWidth(), height = getHeight();
			int swidth = editor.parent.sidebarWidth, ewidth = width-swidth;// Width remaining after sidebar
			// Skip if mouse not held
			if(mouseDown!=0){
				// --- Fetch some data ---
				double anchorx = this.anchorx, anchory = this.anchory,
						scalex = this.scalex, scaley = this.scaley,
						iscalex = 1d/scalex, iscaley = 1d/scaley;
				// Get bounds
				int[] swidths = sidebarColumnWidths(swidth);
				int swidtha = swidths[0], swidthb = swidths[1];
				// Handle the event
				if(mouseDown==1){
					// Left mouse held -> drag to move
					// TODO for a later date: preview selection
				}else if(mouseDown==2){
					// Right mouse held -> drag to select
					// TODO for a later date: context menu
				}else if(mouseDown==3){
					// Middle mouse held -> drag to scroll
					uanchorx += (x-lastMousex)/scalex;
					uanchory += (y-lastMousey)/scaley;
					anchorx = Math.max(0, uanchorx);
					anchory = Math.max(0, uanchory);
				}
				// may need repainting
				editor.repaint();
			}else if(x<swidth||lastMousex<swidth){
				// Buttons may need redrawing
				editor.repaint();
			}
			return true;// Consume the event
		}

		@Override
		public void mouseOut(Component component) {
			LinkedEditorPane editor = (LinkedEditorPane) getComponent();
			keys.clear();// Forget which keys are pressed, for safety
			lastMousex = -1;
			lastMousey = -1;
			editor.repaint();// May need repainting
		}

		@Override
		public void mouseOver(Component component) {
		}
		
		@Override
		public boolean mouseClick(Component component, Button button, int x, int y, int count) {
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
			LinkedEditorPane editor = (LinkedEditorPane) getComponent();
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
			editor.repaint();// May need repainting
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
								Pattern pattern = Pattern.makeDefaultPattern(session);
								editor.view.patterns.put(pattern, new BitSet());
								MetaComponent<JInternalFrame> meta = session.windowManager.getWindow("Pattern Editor");
								PatternEditor peditor;
								if(meta==null||meta.component.isClosed()){
									peditor = PatternEditor.createNew();
									peditor.session = session;
									peditor.init();
									JInternalFrame wrapped = PivotSwingUtils.wrapPivotWindow(peditor);
									meta = new MetaComponent<>("Default Pattern Editor","Pattern Editor",wrapped,null);
									meta.metaData.put("window", peditor);
								}
								peditor = (PatternEditor) meta.metaData.get("window");
								peditor.addEditorData(pattern);
								session.windowManager.addWindow(meta,true);
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
							Pattern pattern = pk[iwy];
							if(x<swidtha){// Delete
								patterns.remove(pattern);
							}else{// Edit
								MetaComponent<JInternalFrame> meta = session.windowManager.getWindow("Pattern Editor");
								PatternEditor peditor;
								if(meta==null||meta.component.isClosed()){
									peditor = PatternEditor.createNew();
									peditor.session = session;
									peditor.init();
									JInternalFrame wrapped = PivotSwingUtils.wrapPivotWindow(peditor);
									meta = new MetaComponent<>("Default Pattern Editor","Pattern Editor",wrapped,null);
									meta.metaData.put("window", peditor);
								}
								peditor = (PatternEditor) meta.metaData.get("window");
								peditor.addEditorData(pattern);
								session.windowManager.addWindow(meta,true);
								session.windowManager.addWindow(meta,true);
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
						IdentityHashMap<Pattern,BitSet> selection = getSelection(true,!shiftHeld());
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
				// scroll is already handled in mouseMove so we don't need to do anything here
			}
			// Signal the mouse not being pressed anymore
			mouseDown = 0;
			// May need repainting
			editor.repaint();
			return true;// Consume the event
		}
		
		@Override
		public boolean mouseWheel(Component component, ScrollType scrollType, int scrollAmount, int wheelRotation, int x, int y) {
			return true;// Consume the event
		}
		
		@Override
		public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
			keys.set(keyCode);
			return true;// Consume the event
		}

		@Override
		public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
			keys.clear(keyCode);
			boolean modified = false;
			LinkedEditorPane editor = (LinkedEditorPane) getComponent();
			switch(keyCode){
			case KeyEvent.VK_C:{
				if(controlHeld()){// Ctrl + C -> copy
					// TODO see what's selected and copy it
				}
				break;
			}
			case KeyEvent.VK_V:{
				if(controlHeld()){// Ctrl + V -> paste
					Session session = editor.parent.session;
					TrackLayerSimple tls = editor.view;
					Object clipBoard = session.clipBoard;
					if(clipBoard instanceof Pattern){
						Pattern pclip = (Pattern)clipBoard;
						if(!tls.patterns.containsKey(pclip)){
							tls.patterns.put(pclip, new BitSet());
							modified = true;
						}
					}
				}
				break;
			}
			}
			if(modified){// This flag tracks whether any changes were made
				editor.repaint();
			}
			return true;// Consume the event
		}

		@Override
		public boolean keyTyped(Component component, char character) {
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
			int gradientShift = (int)Preferences.getIntSafe(session, Preferences.INDEX_INT_TLS_PATTERN_BAR_GRADIENT_SHIFT), gradientShiftInc = 1<<gradientShift, gradientShiftMask = gradientShiftInc-1, gradientShiftMaskInv = ~gradientShiftMask;
			double imageScale = 0.5d;
			// --- Pre-draw ---
			IdentityHashMap<Pattern,BitSet> patterns = tls.patterns;
			IdentityHashMap<Pattern,BitSet> selection = getSelection(false,false);
			double anchorx = this.anchorx, anchory = this.anchory, scalex = this.scalex, scaley = this.scaley;// Cache
			Color bgCol, textCol, lineCol, buttonCol, buttonColLight, errorCol, derrorCol, selectCol, aselectCol;
			ColorScheme colors = Session.getColors(editor.parent.session);
			bgCol = colors.background;
			textCol = colors.text;
			lineCol = colors.line;
			buttonCol = colors.gradient;
			selectCol = colors.selected;
			errorCol = colors.error;
			derrorCol = ColorScheme.adjustHsb(errorCol, 0f, -0.5f, 0f);
			aselectCol = ColorScheme.setAlpha(selectCol, 30);
			buttonColLight = ColorScheme.brighten(buttonCol, 0.1f);
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
				selectv[i]=b==null?new BitSet():b;// Null safety
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
			boolean useShlBorder = scalex>=SHL_BORDER_THRESHOLD && scaley>=SHL_BORDER_THRESHOLD;
			// --- Draw ---
			// Draw buttons
			int[] swidths = sidebarColumnWidths(swidth);
			int swidtha = swidths[0], swidthb = swidths[1];
			int[] buttonxy = buttonxy(false);
			int buttonx = buttonxy==null?-1:buttonxy[0];
			int buttony = buttonxy==null?-1:buttonxy[1];
			for(i=pfirst;i<plast;i++){
				int x1,y1,x2,y2;
				x1 = 0;
				y1 = (int)((i-anchory)*scaley);
				x2 = swidtha;
				y2 = (int)scaley;
				Draw.drawButton(graphics, x1, y1, x2, y2, "Delete", null, null, lineCol, derrorCol, buttony==i&&buttonx==0, textCol, null, 0.5d, 0.5d, imageScale, 0);
				x1 = swidtha;
				x2 = swidthb;
				Draw.drawButton(graphics, x1, y1, x2, y2, "Edit", null, null, lineCol, buttonCol, buttony==i&&buttonx==1, textCol, null, 0.5d, 0.5d, imageScale, 0);
			}
			if(pextend){
				int x1,y1,x2,y2;
				x1 = 0;
				y1 = (int)((plast-anchory)*scaley);
				x2 = swidtha;
				y2 = height-y1;
				Draw.drawButton(graphics, x1, y1, x2, y2, "Add new", null, null, lineCol, buttonCol, buttony==patternCount&&buttonx==0, textCol, null, 0.5d, 0.5d, imageScale, 0);
				x1 = swidtha;
				x2 = swidthb;
				Draw.drawButton(graphics, x1, y1, x2, y2, "Move here", null, null, lineCol, buttonCol, buttony==patternCount&&buttonx==1, textCol, null, 0.5d, 0.5d, imageScale, 0);
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
				ArrayList<Clip> pclips = pk.clips;
				Synthesizer synth = pk.getSynthesizer();
				Color[] sigs = synthSigs.get(synth);
				if(sigs==null){
					sigs = new Color[xrange];
					synthSigs.put(synth, sigs);
					for(int j=0;j<xrange;j++){
						double t=comp.measuresToSeconds((j<<gradientShift)*iscalex+dxref);
						sigs[j] = synth.getColorSignature(t);
					}
				}
				for(Clip clip:pclips){
					int pitch = clip.pitch;
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
				for(int j=12*Math.floorDiv(pmin, 12);j<pmax;j+=12){
					int y1 = (int)Math.round(clipHeight*(pmax-j)), y2 = (int)Math.round(clipHeight*(pmax-j-12));// Recalculate because it can be out of bounds
					graphics.setPaint(new GradientPaint(0,y1,buttonColLight,0,y2, buttonCol));
					graphics.fillRect(0, y2, width, y1-y2);
				}
				double ixmult = scalex/pd;
				for(int delay=ipf+1;delay>=0&&delay<ilast;delay = pv.nextSetBit(delay+1)){
					// Draw one
					double xoffset = (delay-anchorx)*scalex;
					for(Clip clip:pclips){
						int from = (int)Math.round(xoffset+clip.delay*ixmult)-xref;
						int to = from + (int)Math.ceil(clip.length*ixmult)-xref;
						int iy = ys[clip.pitch-pmin];
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
				}
				// Selection overlay must be on top
				for(int delay=ipf+1;delay>=0&&delay<ilast;delay = pv.nextSetBit(delay+1)){
					// If selected, fill
					boolean selected = sv.get(delay);
					if(selected){
						graphics.setColor(aselectCol);
						int x1 = (int)Math.round(anchorx+scalex*delay);
						int x2 = (int)Math.round(anchorx+scalex*(delay+1));
						int y1 = (int)Math.round(anchory+scaley*i);
						int y2 = (int)Math.round(anchory+scaley*(i+1));
						int dx = x2-x1;
						int dy = y2-y1;
						graphics.fillRect(x1,y2,dx,dy);
						if(useShlBorder){// Also draw border
							graphics.setColor(selectCol);
							graphics.setStroke(new BasicStroke(SHL_BORDER_WIDTH));
							if(i==0||!selectv[i-1].get(delay)){// Top border
								graphics.drawLine(x1, y1, x2, y1);
							}
							if(i==patternCount-1||!selectv[i+1].get(delay)){// Bottom border
								graphics.drawLine(x1, y2, x2, y2);
							}
							if(delay==0||!sv.get(delay-1)){// Left border
								graphics.drawLine(x1, y1, x1, y2);
							}
							if(!pv.get(delay+1)){// Right border
								graphics.drawLine(x2, y1, x2, y2);
							}
						}
					}
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
