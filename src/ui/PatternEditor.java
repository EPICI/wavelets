package ui;

import core.*;
import ui.TrackLSEditor.LinkedEditorPane;

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
 * Editor UI for {@link Pattern}
 * 
 * @author EPICI
 * @version 1.0
 */
public class PatternEditor extends Window implements Bindable {
	
	/**
	 * The current/linked session
	 */
	public Session session;
	/**
	 * The tab pane to switch between patterns
	 */
	public TabPane tabPane;
	
	public PatternEditor(){
		super();
	}
	
	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
		tabPane = (TabPane) namespace.get("tabPane");
	}
	
	/**
	 * The actual editor interface for the {@link Pattern}.
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
		 * The parent {@link PatternEditor}
		 */
		public PatternEditor parent;
		/**
		 * The {@link Pattern} for which this instance
		 * provides the UI
		 */
		public Pattern view;
		/**
		 * The main part of the pattern editor,
		 * a custom type
		 */
		public LinkedEditorInnerPane editorInnerPane;
		/**
		 * The tab name
		 */
		public ButtonData tabName;
		/**
		 * The table pane that wraps the whole thing
		 */
		public TablePane outerTablePane;
		/**
		 * The top menu
		 */
		public MenuBar menuBar;
		/**
		 * The table pane that wraps the whole thing except for the menu
		 */
		public TablePane innerTablePane;
		/**
		 * Pattern properties scroll pane wrapping a table pane
		 */
		public ScrollPane patternScrollPane;
		/**
		 * Pattern properties table pane, each row is an editable property
		 */
		public TablePane patternTablePane;
		/**
		 * Clip properties scroll pane wrapping a table pane
		 */
		public ScrollPane clipScrollPane;
		/**
		 * Clip properties table pane, each row is an editable property
		 * <br>
		 * It uses 2 columns: left corresponds to editing the specific
		 * selected clip(s), right corresponds to editing the template
		 * that defines the left's behaviour
		 */
		public TablePane clipTablePane;
		
		@Override
		public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
			tabName = (ButtonData) namespace.get("tabName");
			editorInnerPane = (LinkedEditorInnerPane) namespace.get("editorInnerPane");
			outerTablePane = (TablePane) namespace.get("outerTablePane");
			menuBar = (MenuBar) namespace.get("menuBar");
			innerTablePane = (TablePane) namespace.get("innerTablePane");
			patternScrollPane = (ScrollPane) namespace.get("patternScrollPane");
			patternTablePane = (TablePane) namespace.get("patternTablePane");
			clipScrollPane = (ScrollPane) namespace.get("clipScrollPane");
			clipTablePane = (TablePane) namespace.get("clipTablePane");
		}
		
		/**
		 * Initialize, called after setting fields
		 */
		public void init(){
			tabName.setText(view.getName());
		}
		
	}
	
	public static class LinkedEditorInnerPane extends Component implements Bindable{
		
		/**
		 * The pane that contains this
		 */
		public LinkedEditorPane parent;
		
		@Override
		public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
		}
		
		/**
		 * Initialize, called after setting fields
		 */
		public void init(){
		}
		
	}
	
	/**
	 * Skin for the {@link LinkedEditorInnerPane}. By convention, it provides the UI
	 * and handles the rendering.
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public static class LinkedEditorInnerPaneSkin extends ContainerSkin{
		
		@Override
		public void layout() {
			//There are no displayed subcomponents, so nothing needs to be done here
		}
		
		@Override
		public void paint(Graphics2D graphics){
			
		}
		
	}

}
