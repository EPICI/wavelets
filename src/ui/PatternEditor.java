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
		 * Reserved rows at the top of the clip table
		 */
		public static final int CLIP_TABLE_EXTRA_ROWS_TOP = 5;
		/**
		 * Reserved rows at the bottom of the clip table
		 */
		public static final int CLIP_TABLE_EXTRA_ROWS_BOTTOM = 1;
		/**
		 * Index of the row for the divisions input
		 */
		public static final int INDEX_DIVISIONS_INPUT = 4;
		/**
		 * Index of the row for the clip start input
		 */
		public static final int INDEX_CLIP_START_INPUT = 3;
		/**
		 * Index of the row for the clip duration input
		 */
		public static final int INDEX_CLIP_DURATION_INPUT = 5;
		/**
		 * Index of the row for the template selector
		 */
		public static final int INDEX_TEMPLATE_SELECTOR = 2;
		
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
		 * Button which displays the name of the synth used
		 * <br>
		 * Click to open the synth manager
		 * <br>
		 * Paste synth here to change to that synth
		 */
		public PushButton synthButton;
		/**
		 * Input field for pattern divisions
		 */
		public DoubleInput divisionsInput;
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
		/**
		 * Input field for start of clip
		 */
		public DoubleInput clipStartInput;
		/**
		 * Input field for duration of clip
		 */
		public DoubleInput clipDurationInput;
		/**
		 * List to select a template to use
		 */
		public ListButton templateSelector;
		/**
		 * Field to set the name of the current template,
		 * created when the button is pressed
		 */
		public TextInput templateRenameInput;
		/**
		 * Button to temporarily replace the selector with
		 * a text input which is used to rename the template,
		 * or if the text input is active, attempt to commit
		 * the new name
		 */
		public PushButton templateRename;
		/**
		 * Button to switch to a copy of the currently selected
		 * template, or if none are selected, a new template
		 */
		public PushButton templateCopy;
		/**
		 * Button to switch to a new blank template
		 */
		public PushButton templateNew;
		/**
		 * Button to add a new parameter to the bottom
		 */
		public PushButton templateParamNew;
		
		// TODO clip selection
		
		@Override
		public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
			TablePane.Row tr;
			tabName = (ButtonData) namespace.get("tabName");
			editorInnerPane = (LinkedEditorInnerPane) namespace.get("editorInnerPane");
			outerTablePane = (TablePane) namespace.get("outerTablePane");
			menuBar = (MenuBar) namespace.get("menuBar");
			innerTablePane = (TablePane) namespace.get("innerTablePane");
			patternScrollPane = (ScrollPane) namespace.get("patternScrollPane");
			patternTablePane = (TablePane) namespace.get("patternTablePane");
			synthButton = (PushButton) namespace.get("synthButton");
			clipScrollPane = (ScrollPane) namespace.get("clipScrollPane");
			clipTablePane = (TablePane) namespace.get("clipTablePane");
			templateSelector = (ListButton) namespace.get("templateSelector");
			templateRename = (PushButton) namespace.get("templateRename");
			templateCopy = (PushButton) namespace.get("templateCopy");
			templateNew = (PushButton) namespace.get("templateNew");
			templateParamNew = (PushButton) namespace.get("templateParamNew");
			
			divisionsInput = new DoubleInput(
					new DoubleInput.DoubleValidator.SplitDoubleValidator(
							new DoubleInput.DoubleValidator.BoundedIntegerValidator(1, Integer.MAX_VALUE, 4),
							new DoubleInput.DoubleValidator.HyperStep(-Double.MAX_VALUE, Double.MAX_VALUE, 0, 2)),
					4, 0.05);
			divisionsInput.dataListeners.add(new DivisionsInputListener(this));
			tr = patternTablePane.getRows().get(INDEX_DIVISIONS_INPUT);
			tr.update(0, divisionsInput);
			
			clipStartInput = new DoubleInput(
					new DoubleInput.DoubleValidator.BoundedIntegerValidator(0,Integer.MAX_VALUE,0),
					0, 0.1);
			// TODO listener for this
			tr = clipTablePane.getRows().get(INDEX_CLIP_START_INPUT);
			tr.update(0, clipStartInput);
			// TODO integer sliders for start, duration and update the rows
			
			// TODO template listeners
		}
		
		/**
		 * Initialize, called after setting fields
		 */
		public void init(){
			tabName.setText(view.getName());
			divisionsInput.value = view.divisions;
		}
		
		/**
		 * Listeners for changes to the divisions input field
		 * and updates the pattern accordingly
		 * 
		 * @author EPICI
		 * @version 1.0
		 */
		public static class DivisionsInputListener implements DoubleInput.DataListener{
			
			/**
			 * Remember the parent, other data can be derived from here
			 */
			public LinkedEditorPane parent;
			
			/**
			 * Standard constructor
			 * 
			 * @param parent
			 */
			public DivisionsInputListener(LinkedEditorPane parent){
				this.parent = parent;
			}

			@Override
			public void updated(DoubleInput component, boolean commit) {
				// Don't preview for this operation
				if(commit){
					Pattern pattern = parent.view;
					pattern.setDivisions((int)Math.round(component.value));
				}
			}
			
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
