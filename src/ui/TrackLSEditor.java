package ui;

import core.*;
import org.apache.pivot.wtk.*;
import java.awt.Color;
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
			// --- Pre-draw ---
			LinkedEditorPane editor = (LinkedEditorPane) getComponent();
			int width = getWidth(), height = getHeight();
			Color bgCol, textCol, lineCol, buttonTopCol, buttonBotCol, buttonPressCol;
			ColorScheme colors = editor.parent.session.getColors();
			if(colors==null){
				TerraTheme theme = (TerraTheme)Theme.getTheme();
			}else{
				bgCol = colors.background;
				textCol = colors.text;
				lineCol = colors.line;
				buttonTopCol = ColorScheme.brighten(colors.gradient,0.1f);
				buttonBotCol = colors.gradient;
				buttonPressCol = ColorScheme.brighten(colors.gradient, -0.1f);
			}
		}
	}
}
