package ui;

import core.*;
import core.curves.*;
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
		public static final int CLIP_TABLE_EXTRA_ROWS_TOP = 7;
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
		public static final int INDEX_CLIP_START_INPUT = 2;
		/**
		 * Index of the row for the clip duration input
		 */
		public static final int INDEX_CLIP_DURATION_INPUT = 4;
		/**
		 * Index of the row for the clip volume input
		 */
		public static final int INDEX_CLIP_VOLUME_INPUT = 6;
		/**
		 * Index of the row for the template selector
		 */
		public static final int INDEX_TEMPLATE_SELECTOR = 2;
		
		/**
		 * Name of the step mode using
		 * {@link DoubleInput.DoubleValidator.BoundedDoubleValidator}
		 */
		public static final String STEP_MODE_NAME_LINEAR = "Add";
		/**
		 * Name of the step mode using
		 * {@link DoubleInput.DoubleValidator.HyperbolicStep}
		 */
		public static final String STEP_MODE_NAME_HYPERBOLIC = "Multiply";
		
		/**
		 * Name of the update mode which
		 * sets the value of the property for all clips
		 * to the new value of the input.
		 */
		public static final String UPDATE_MODE_NAME_SET = "Overwrite";
		/**
		 * Name of the update mode which
		 * additively shifts the value of the property for all clips
		 * so the old value of the input becomes the new value of the input.
		 */
		public static final String UPDATE_MODE_NAME_DIFFERENCE_ADD = "Add";
		/**
		 * Name of the update mode which
		 * multiplicatively shifts the value of the property for all clips
		 * so the old value of the input becomes the new value of the input.
		 */
		public static final String UPDATE_MODE_NAME_DIFFERENCE_MULTIPLY = "Multiply";
		
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
		 * Input field for volume of a clip
		 */
		public DoubleInput clipVolumeInput;
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
		
		/**
		 * Selection with the original (same object) as keys
		 * and a copy (different object) as values.
		 * <br>
		 * The original (key) is changed by operations.
		 * <br>
		 * When a change is confirmed, value data should be copied from
		 * keys. Similary, when a change is cancelled, key data should be
		 * copied from values.
		 */
		public IdentityHashMap<Clip,Clip> selection;
		
		// TODO method to get current template object
		
		/**
		 * Get the selection, never returns null
		 * <br>
		 * Format: suppose a pair (k,v) exists, k is a reference to the same
		 * object as in the {@link Pattern}, and v is a copy of that object.
		 * If k does not match v, then an operation is pending. After the operation
		 * is confirmed or cancelled, k will match v.
		 * 
		 * @param cleanup flag to iterate through and remove dead clips
		 * @param clear flag to reset and return an empty selection
		 * @return the selection
		 */
		public IdentityHashMap<Clip,Clip> getSelection(boolean cleanup,boolean clear){
			if(clear||selection==null){
				boolean update = selection!=null&&selection.size()>0;
				selection = new IdentityHashMap<>();
				if(update)selectionChanged();
				return selection;
			}
			IdentityHashMap<Clip,Clip> r = selection;
			if(cleanup){
				// Identity hash set
				Set<Clip> patternClips = Collections.newSetFromMap(new IdentityHashMap<>());
				patternClips.addAll(view.clips);
				r.keySet().retainAll(patternClips);
			}
			return r;
		}
		
		/**
		 * Either confirm or cancel pending changes. If confirm,
		 * copy will copy from original. If cancel, original will
		 * copy from copy. In both cases, original and copy will
		 * match after this call.
		 * 
		 * @param confirm true to confirm, false to cancel
		 */
		public void finalizeSelection(boolean confirm){
			IdentityHashMap<Clip,Clip> selection = getSelection(true,false);
			for(java.util.Map.Entry<Clip, Clip> entry:selection.entrySet()){
				Clip original = entry.getKey();
				Clip copy = entry.getValue();
				if(confirm){
					copy.copyFrom(original);
				}else{
					original.copyFrom(copy);
				}
			}
		}
		
		/**
		 * Call this when something is selected or deselected,
		 * will update data or interface as necessary.
		 */
		public void selectionChanged(){
			// Avoid calling getSelection()
			IdentityHashMap<Clip,Clip> selection = this.selection;
			int nselected = selection.size();
			boolean setenabled = nselected>0;
			if(nselected==0){// Empty selection
				clipStartInput.setValueNearest(0);
				clipDurationInput.setValueNearest(1);
				clipVolumeInput.setValueNearest(0);
			}else if(nselected==1){// Single selection
				Clip single = selection.keySet().iterator().next();
				clipStartInput.setValueNearest(single.delay);
				clipDurationInput.setValueNearest(single.length);
				clipVolumeInput.setValueNearest(single.volume);
			}else{// Multiple selection
				int maxDelay = 0;
				for(Clip clip:selection.keySet()){
					maxDelay = Math.max(maxDelay, clip.delay);
				}
				clipStartInput.setValueNearest(maxDelay);
			}
			clipStartInput.valueChanged(true,false);
			clipStartInput.toSliderView(false);
			clipDurationInput.valueChanged(true,false);
			clipDurationInput.toSliderView(false);
			clipVolumeInput.valueChanged(true,false);
			clipVolumeInput.toSliderView(false);
			for(TablePane.Row tr:clipTablePane.getRows()){
				// Only look at first column, which is clip
				Component comp = tr.get(0);
				// Inputs should be enabled/disabled
				if(comp instanceof DoubleInput){
					comp.setEnabled(setenabled);
				}
			}
		}
		
		@Override
		public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
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
		}
		
		/**
		 * Initialize, called after setting fields
		 */
		public void init(){
			TablePane.Row tr;
			
			divisionsInput = new DoubleInput(
					new DoubleInput.DoubleValidator.SplitDoubleValidator(
							new DoubleInput.DoubleValidator.BoundedIntegerValidator(1, 1e6, 4),
							new DoubleInput.DoubleValidator.HyperbolicStep(2)),
					view.divisions, 0.05);
			divisionsInput.dataListeners.add(new DivisionsInputListener(this));
			tr = patternTablePane.getRows().get(INDEX_DIVISIONS_INPUT);
			tr.update(0, divisionsInput);
			
			clipStartInput = new DoubleInput(
					new DoubleInput.DoubleValidator.BoundedIntegerValidator(-1e6,1e6,0),
					0, 0.1);
			clipStartInput.dataListeners.add(new ClipStartInputListener(this));
			tr = clipTablePane.getRows().get(INDEX_CLIP_START_INPUT);
			tr.update(0, clipStartInput);
			
			clipDurationInput = new DoubleInput(
					new DoubleInput.DoubleValidator.HyperbolicStep(1e-6, 1e6, 1, 2),
					1, 0.01);
			clipDurationInput.dataListeners.add(new ClipDurationInputListener(this));
			tr = clipTablePane.getRows().get(INDEX_CLIP_DURATION_INPUT);
			tr.update(0, clipDurationInput);
			
			clipVolumeInput = new DoubleInput(
					new DoubleInput.DoubleValidator.BoundedDoubleValidator(-10, 10, 0),
					1, 0.01);
			clipVolumeInput.dataListeners.add(new ClipVolumeInputListener(this));
			tr = clipTablePane.getRows().get(INDEX_CLIP_VOLUME_INPUT);
			tr.update(0, clipVolumeInput);
			
			// TODO template listeners
			
			tabName.setText(view.getName());
		}
		
		/**
		 * Listens for changes to the divisions input field
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
	
	/**
	 * Listens for changes to the clip start input field
	 * and updates the selected clips accordingly
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public static class ClipStartInputListener implements DoubleInput.DataListener{
		
		/**
		 * Remember the parent, other data can be derived from here
		 */
		public LinkedEditorPane parent;
		
		/**
		 * Standard constructor
		 * 
		 * @param parent
		 */
		public ClipStartInputListener(LinkedEditorPane parent){
			this.parent = parent;
		}
		
		@Override
		public void updated(DoubleInput component, boolean commit) {
			final double value = component.value,
					lastValue = component.lastValue,
					lastValueCommit = component.lastValueCommit;
			int add = (int)(value-lastValueCommit);
			IdentityHashMap<Clip,Clip> selection = parent.getSelection(false,false);
			for(java.util.Map.Entry<Clip, Clip> entry:selection.entrySet()){
				Clip original = entry.getKey();
				Clip copy = entry.getValue();
				original.delay = Math.max(0, copy.delay+add);
			}
			if(commit){
				if(value==lastValueCommit){// Cancel
					parent.finalizeSelection(false);
				}else{// Confirm
					parent.finalizeSelection(true);
				}
			}
		}
		
	}
	
	/**
	 * Listens for changes to the clip duration input field
	 * and updates the selected clips accordingly
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public static class ClipDurationInputListener implements DoubleInput.DataListener{
		
		/**
		 * Remember the parent, other data can be derived from here
		 */
		public LinkedEditorPane parent;
		
		/**
		 * Standard constructor
		 * 
		 * @param parent
		 */
		public ClipDurationInputListener(LinkedEditorPane parent){
			this.parent = parent;
		}
		
		@Override
		public void updated(DoubleInput component, boolean commit) {
			final double value = component.value,
					lastValue = component.lastValue,
					lastValueCommit = component.lastValueCommit;
			double mult = value/lastValueCommit;
			IdentityHashMap<Clip,Clip> selection = parent.getSelection(false,false);
			for(java.util.Map.Entry<Clip, Clip> entry:selection.entrySet()){
				Clip original = entry.getKey();
				Clip copy = entry.getValue();
				original.length = Math.max(1, (int)Math.round(copy.length*mult));
			}
			if(commit){
				if(value==lastValueCommit){// Cancel
					parent.finalizeSelection(false);
				}else{// Confirm
					parent.finalizeSelection(true);
				}
			}
		}
		
	}
	
	/**
	 * Listens for changes to the clip volume input field
	 * and updates the selected clips accordingly
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public static class ClipVolumeInputListener implements DoubleInput.DataListener{
		
		/**
		 * Remember the parent, other data can be derived from here
		 */
		public LinkedEditorPane parent;
		
		/**
		 * Standard constructor
		 * 
		 * @param parent
		 */
		public ClipVolumeInputListener(LinkedEditorPane parent){
			this.parent = parent;
		}
		
		@Override
		public void updated(DoubleInput component, boolean commit) {
			final double value = component.value,
					lastValue = component.lastValue,
					lastValueCommit = component.lastValueCommit;
			double mult = value/lastValueCommit;
			IdentityHashMap<Clip,Clip> selection = parent.getSelection(false,false);
			for(java.util.Map.Entry<Clip, Clip> entry:selection.entrySet()){
				Clip original = entry.getKey();
				Clip copy = entry.getValue();
				original.volume = copy.volume*mult;
			}
			if(commit){
				if(value==lastValueCommit){// Cancel
					parent.finalizeSelection(false);
				}else{// Confirm
					parent.finalizeSelection(true);
				}
			}
		}
		
	}
	
	public static class LinkedClipTableRow extends TablePane.Row implements Bindable{
		
		/**
		 * Index of the row for the clip property input
		 */
		public static final int INDEX_PROPERTY_INPUT = 1;
		/**
		 * Index of the row for the clip property bounds (min/max/default) inputs
		 */
		public static final int INDEX_BOUNDS_INPUT = 1;
		
		/**
		 * The pane that contains this
		 */
		public LinkedEditorPane parent;
		/**
		 * The property that this edits
		 */
		public Clip.Template.Property view;
		/**
		 * The label on the left side that shows the current name of the property
		 */
		public Label nameLabel;
		/**
		 * Selector for what method the input should use to step values
		 */
		public ListButton stepModeSelector;
		/**
		 * Selector for what method to use for updating the clips' value
		 * based on change in input value
		 */
		public ListButton updateModeSelector;
		/**
		 * Button to swap this property with the one below (after) it
		 */
		public PushButton moveDown;
		/**
		 * Button to swap this property with the one above (before) it
		 */
		public PushButton moveUp;
		/**
		 * Button to remove this property from the template
		 */
		public PushButton remove;
		/**
		 * Table pane containing the min, max, and base inputs
		 */
		public TablePane boundsTablePane;
		/**
		 * Left side table pane, for editing the property for clips
		 */
		public TablePane leftTablePane;
		/**
		 * Table pane containing the buttons for moving and removing
		 */
		public TablePane moveTablePane;
		/**
		 * Right side table pane, for editing the property in the template
		 */
		public TablePane rightTablePane;
		/**
		 * Text field used to edit the name of the property
		 */
		public TextInput nameInput;
		/**
		 * The input for the clip's value for this property
		 */
		public DoubleInput propertyInput;
		/**
		 * The input for the minimum value of the property
		 */
		public DoubleInput minInput;
		/**
		 * The input for the default value of the property
		 */
		public DoubleInput baseInput;
		/**
		 * The input for the maximum value of the property
		 */
		public DoubleInput maxInput;
		
		private static final String SPLITDOUBLEVALIDATOR_CLASS_NAME = DoubleInput.DoubleValidator.SplitDoubleValidator.class.getCanonicalName();
		private static final String BOUNDEDDOUBLEVALIDATOR_CLASS_NAME = DoubleInput.DoubleValidator.BoundedDoubleValidator.class.getCanonicalName();
		private static final String DOUBLEVALIDATOR_CLASS_NAME = DoubleInput.DoubleValidator.class.getCanonicalName();
		
		@Override
		public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
			nameLabel = (Label) namespace.get("nameLabel");
			stepModeSelector = (ListButton) namespace.get("stepModeSelector");
			updateModeSelector = (ListButton) namespace.get("updateModeSelector");
			moveDown = (PushButton) namespace.get("moveDown");
			moveUp = (PushButton) namespace.get("moveUp");
			remove = (PushButton) namespace.get("remove");
			boundsTablePane = (TablePane) namespace.get("boundsTablePane");
			leftTablePane = (TablePane) namespace.get("leftTablePane");
			moveTablePane = (TablePane) namespace.get("moveTablePane");
			rightTablePane = (TablePane) namespace.get("rightTablePane");
			nameInput = (TextInput) namespace.get("nameInput");
		}
		
		/**
		 * Initialize, called after setting fields
		 */
		public void init(){
			TablePane.Row tr;
			
			propertyInput = new DoubleInput(
					new DoubleInput.DoubleValidator.SplitDoubleValidator(
							new DoubleInput.DoubleValidator.BoundedDoubleValidator(view.base),
							new DoubleInput.DoubleValidator.BoundedDoubleValidator(0)),
					view.base, 0.05
					);
			// TODO add listener for this
			tr = leftTablePane.getRows().get(INDEX_PROPERTY_INPUT);
			tr.update(0, propertyInput);
			
			minInput = new DoubleInput(
					new DoubleInput.DoubleValidator.SplitDoubleValidator(
							new DoubleInput.DoubleValidator.BoundedDoubleValidator(0),
							new DoubleInput.DoubleValidator.HyperbolicStep(2)),
					0, 0.01
					);
			minInput.dataListeners.add(new TemplateMinInputListener(this));
			tr = boundsTablePane.getRows().get(INDEX_BOUNDS_INPUT);
			tr.update(0, minInput);
			
			maxInput = new DoubleInput(
					new DoubleInput.DoubleValidator.SplitDoubleValidator(
							new DoubleInput.DoubleValidator.BoundedDoubleValidator(1),
							new DoubleInput.DoubleValidator.HyperbolicStep(2)),
					1, 0.01
					);
			maxInput.dataListeners.add(new TemplateMaxInputListener(this));
			tr = boundsTablePane.getRows().get(INDEX_BOUNDS_INPUT);
			tr.update(2, maxInput);
			
			// TODO property, min, base, max inputs
			
			// TODO name updating listener
			
			// TODO remove listener
			
			// TODO move up, down listener
			
			// TODO update mode listener
			
			// TODO step mode listener
			
			// fix the mismatches
			updateView(true);
			updatePropertyInput(false,true,true,true,true);
		}
		
		/**
		 * Given the name of an update mode, return the corresponding
		 * curve instance which maps each value to a new value.
		 * If not recognized, returns null.
		 * 
		 * @param name
		 * @param oldValue old value of the property input
		 * @param newValue new value of the property input
		 * @return
		 */
		public Curve getUpdateInstance(String name,double oldValue,double newValue){
			switch(name){
			case PatternEditor.LinkedEditorPane.UPDATE_MODE_NAME_SET:{
				return new CurvePolynomial(newValue);
			}
			case PatternEditor.LinkedEditorPane.UPDATE_MODE_NAME_DIFFERENCE_ADD:{
				return new CurvePolynomial(newValue-oldValue,1);
			}
			case PatternEditor.LinkedEditorPane.UPDATE_MODE_NAME_DIFFERENCE_MULTIPLY:{
				return new CurvePolynomial(0,newValue/oldValue);
			}
			}
			return null;
		}
		
		/**
		 * Given the name of a step mode, return the corresponding
		 * validator instance. If not recognized, returns null.
		 * 
		 * @param name
		 * @return
		 */
		public DoubleInput.DoubleValidator getStepInstance(String name){
			switch(name){
			case PatternEditor.LinkedEditorPane.STEP_MODE_NAME_LINEAR:{
				return new DoubleInput.DoubleValidator.BoundedDoubleValidator(0);
			}
			case PatternEditor.LinkedEditorPane.STEP_MODE_NAME_HYPERBOLIC:{
				return new DoubleInput.DoubleValidator.HyperbolicStep(2);
			}
			}
			return null;
		}
		
		/**
		 * Given a stepper instance, return the canonical name of the
		 * corresponding step mode. If not recognized, returns null.
		 * 
		 * @param stepper
		 * @return
		 */
		public String getStepName(DoubleInput.DoubleValidator stepper){
			if(stepper instanceof DoubleInput.DoubleValidator.HyperbolicStep){
				return PatternEditor.LinkedEditorPane.STEP_MODE_NAME_HYPERBOLIC;
			}else if(stepper instanceof DoubleInput.DoubleValidator.BoundedDoubleValidator){
				return PatternEditor.LinkedEditorPane.STEP_MODE_NAME_LINEAR;
			}
			return null;
		}
		
		/**
		 * Update the view using all properties based on the property input.
		 * Doesn't handle updating between the property input and the other settings.
		 * <br>
		 * Update mode is handled by the list button, not the property input,
		 * and follows the same rules for update direction.
		 * 
		 * @param reverse true to update from the view to the property input instead
		 */
		public void updateView(boolean reverse){
			if(reverse){
				Clip.Template.Property view = this.view;
				DoubleInput.DoubleValidator validator = propertyInput.validator;
				java.util.Map<String,Object> copyOptions, copySet;
				Collection<String> copyWhitelist;
				copyOptions = BetterClone.fixOptions(null);
				copySet = (java.util.Map<String,Object>)copyOptions.get("set");
				copySet.put(BOUNDEDDOUBLEVALIDATOR_CLASS_NAME+".min", view.min);
				copySet.put(BOUNDEDDOUBLEVALIDATOR_CLASS_NAME+".max", view.max);
				copySet.put(BOUNDEDDOUBLEVALIDATOR_CLASS_NAME+".base", view.base);
				copySet.put(SPLITDOUBLEVALIDATOR_CLASS_NAME+".step", getStepInstance(view.step));
				copyWhitelist = (Collection<String>)copyOptions.get("whitelist");
				copyWhitelist.add("*"+DOUBLEVALIDATOR_CLASS_NAME);
				propertyInput.setValidator(
						BetterClone.copy(propertyInput.validator, 0, copyOptions));
				// TODO update
			}else{
				Clip.Template.Property view = this.view;
				DoubleInput.DoubleValidator.SplitDoubleValidator validator =
						(DoubleInput.DoubleValidator.SplitDoubleValidator) propertyInput.validator;
				view.min = validator.min();
				view.max = validator.max();
				view.base = validator.base();
				view.step = Objects.requireNonNull(getStepName(validator.step), "step is not a recognized type");
				// TODO update
			}
		}
		
		/**
		 * Update to or from the property input. For all items which are to be updated,
		 * the property input and setting input will match after.
		 * 
		 * @param reverse false to update from other settings to the property input,
		 * true to update from property input to other settings
		 * @param min true to update the minimum
		 * @param max true to update the maximum
		 * @param base true to update the default value
		 * @param step true to update the stepping
		 */
		public void updatePropertyInput(boolean reverse,boolean min,boolean max,boolean base,boolean step){
			if(min){
				// Get the new value
				double value = minInput.value;
				// TODO reverse branch
				java.util.Map<String,Object> copyOptions, copySet;
				Collection<String> copyWhitelist;
				// Set property input minimum
				copyOptions = BetterClone.fixOptions(null);
				copySet = (java.util.Map<String,Object>)copyOptions.get("set");
				copySet.put(BOUNDEDDOUBLEVALIDATOR_CLASS_NAME+".min", value);
				copyWhitelist = (Collection<String>)copyOptions.get("whitelist");
				copyWhitelist.add("*"+DOUBLEVALIDATOR_CLASS_NAME);
				propertyInput.setValidator(
						BetterClone.copy(propertyInput.validator, 0, copyOptions));
				// Set max input minimum, because max can't be below min
				copyOptions = BetterClone.fixOptions(null);
				copySet = (java.util.Map<String,Object>)copyOptions.get("set");
				copySet.put(BOUNDEDDOUBLEVALIDATOR_CLASS_NAME+".min", value);
				copyWhitelist = (Collection<String>)copyOptions.get("whitelist");
				copyWhitelist.add("*"+DOUBLEVALIDATOR_CLASS_NAME);
				maxInput.setValidator(
						BetterClone.copy(maxInput.validator, 0, copyOptions));
				// Set default input minimum, because default can't be below min
				copyOptions = BetterClone.fixOptions(null);
				copySet = (java.util.Map<String,Object>)copyOptions.get("set");
				copySet.put(BOUNDEDDOUBLEVALIDATOR_CLASS_NAME+".min", value);
				copyWhitelist = (Collection<String>)copyOptions.get("whitelist");
				copyWhitelist.add("*"+DOUBLEVALIDATOR_CLASS_NAME);
				baseInput.setValidator(
						BetterClone.copy(baseInput.validator, 0, copyOptions));
			}
			if(max){
				// TODO update max
			}
			if(base){
				// TODO update base
			}
			if(step){
				// TODO update step
			}
		}
		
		/**
		 * Listens for changes to the template property minimum value input field
		 * and updates the clip property input accordingly
		 * 
		 * @author EPICI
		 * @version 1.0
		 */
		public static class TemplateMinInputListener implements DoubleInput.DataListener{
			
			/**
			 * Remember the parent, other data can be derived from here
			 */
			public LinkedClipTableRow parent;
			
			/**
			 * Standard constructor
			 * 
			 * @param parent
			 */
			public TemplateMinInputListener(LinkedClipTableRow parent){
				this.parent = parent;
			}
			
			@Override
			public void updated(DoubleInput component, boolean commit) {
				final double value = component.value,
						lastValue = component.lastValue,
						lastValueCommit = component.lastValueCommit;
				if(commit && value!=lastValueCommit){// Confirm
					parent.updatePropertyInput(false, true, false, false, false);
					// it's okay that we don't update the clips
				}
			}
			
		}
		
		/**
		 * Listens for changes to the template property maximum value input field
		 * and updates the clip property input accordingly
		 * 
		 * @author EPICI
		 * @version 1.0
		 */
		public static class TemplateMaxInputListener implements DoubleInput.DataListener{
			
			/**
			 * Remember the parent, other data can be derived from here
			 */
			public LinkedClipTableRow parent;
			
			/**
			 * Standard constructor
			 * 
			 * @param parent
			 */
			public TemplateMaxInputListener(LinkedClipTableRow parent){
				this.parent = parent;
			}
			
			@Override
			public void updated(DoubleInput component, boolean commit) {
				final double value = component.value,
						lastValue = component.lastValue,
						lastValueCommit = component.lastValueCommit;
				if(commit && value!=lastValueCommit){// Confirm
					parent.updatePropertyInput(false, false, true, false, false);
					// it's okay that we don't update the clips
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
