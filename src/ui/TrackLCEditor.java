package ui;

import java.awt.event.*;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import org.apache.pivot.beans.*;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.content.ButtonData;
import core.*;
import util.*;
import util.ui.*;

/**
 * Editor UI for {@link TrackLayerCompound}
 * 
 * @author EPICI
 * @version 1.0
 */
public class TrackLCEditor extends Window implements Bindable {
	
	/**
	 * The current/linked session
	 */
	public Session session;
	/**
	 * The {@link TabPane} containing everything
	 */
	public TabPane tabPane;

	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
		tabPane = (TabPane) namespace.get("tabPane");
	}
	
	/**
	 * Call after fields are initialized
	 */
	public void init(){
		addTLC(session.composition.tracks);
	}
	
	/**
	 * Rebuilds all UIs
	 */
	public void remakeAll(){
		ArrayList<TrackLayerCompound> tracks = new ArrayList<>();
		TabPane.TabSequence tabs = tabPane.getTabs();
		// Reverse order for small speed boost
		for(int i=tabs.getLength()-1;i>=0;i--){
			Component component = tabs.get(i);
			if(component instanceof LinkedTablePane){
				LinkedTablePane ltp = (LinkedTablePane) component;
				tracks.add(ltp.view);
				tabs.remove(i,1);
			}
		}
		// Assuming all tracks got removed, this should add them back in forward order
		for(int i=tracks.size()-1;i>=0;i--){
			addTLC(tracks.get(i));
		}
	}
	
	/**
	 * Add UI for a {@link TrackLayerCompound} if it isn't already present
	 * 
	 * @param track the track to add the UI for
	 */
	public void addTLC(TrackLayerCompound track){
		TabPane.TabSequence tabs = tabPane.getTabs();
		for(Component component:tabs){
			if(component instanceof LinkedTablePane){
				LinkedTablePane ltp = (LinkedTablePane) component;
				if(ltp.view==track)return;
			}
		}
		addNewTLC(track);
	}
	
	private void addNewTLC(TrackLayerCompound track){
		try{
			TabPane.TabSequence tabs = tabPane.getTabs();
			LinkedTablePane linked = PivotSwingUtils.loadBxml(LinkedTablePane.class, "trackLCEditorTable.bxml");
			linked.parent = this;
			linked.view = track;
			linked.init();
			tabs.add(linked);
		}catch(NullPointerException exception){
			exception.printStackTrace();
		}
	}
	
	public static class LinkedTablePane extends TablePane implements Bindable{
		
		/**
		 * Rows which are not tracks
		 */
		public static final int EXTRA_ROWS = 1;
		
		/**
		 * The parent {@link TrackLCEditor}
		 */
		public TrackLCEditor parent;
		/**
		 * The {@link TrackLayerCompound} for which this instance
		 * provides the UI
		 */
		public TrackLayerCompound view;
		/**
		 * The tab name
		 */
		public ButtonData tabName;
		/**
		 * Button which moves all selected tracks here (in some order)
		 */
		public PushButton moveHere;
		/**
		 * Button which unpresses all selector buttons
		 */
		public PushButton deselectAll;
		/**
		 * Button which adds the selected type (if recognized)
		 */
		public PushButton addNew;
		/**
		 * List of known types to select from
		 */
		public ListButton typeSelector;
		/**
		 * Which keys are pressed for the move here button
		 */
		public BitSet moveHereKeys = new BitSet();
		
		@Override
		public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
			tabName = (ButtonData) namespace.get("tabName");
			moveHere = (PushButton) namespace.get("move");
			deselectAll = (PushButton) namespace.get("deselect");
			addNew = (PushButton) namespace.get("add");
			typeSelector = (ListButton) namespace.get("selector");
		}
		
		/**
		 * Initialize, called after setting fields
		 */
		public void init(){
			tabName.setText(view.getName());
			ArrayList<Track> tracks = view.tracks;
			RowSequence rows = getRows();
			if(tracks.size()!=0){
				BXMLSerializer serializer = new BXMLSerializer();
				for(Track track:view.tracks){
					LinkedTableRow row = PivotSwingUtils.loadBxml(TrackLCEditor.class, "trackLCEditorTableRow.bxml", serializer);
					row.parent = this;
					row.view = track;
					row.init();
					rows.add(row);
				}
			}
			moveHere.getButtonPressListeners().add(new ButtonPressListener(){

				@Override
				public void buttonPressed(org.apache.pivot.wtk.Button button) {
					ArrayList<Track> moved = new ArrayList<>();
					TabPane.TabSequence tabs = parent.tabPane.getTabs();
					for(int i=tabs.getLength()-1;i>=0;i--){
						Component component = tabs.get(i);
						if(component instanceof LinkedTablePane){
							LinkedTablePane ltp = (LinkedTablePane) component;
							TrackLayerCompound tlc = ltp.view;
							RowSequence rows = ltp.getRows();
							for(int j=rows.getLength()-1;j>=EXTRA_ROWS;j--){
								TablePane.Row row = rows.get(j);
								if(row instanceof LinkedTableRow){
									LinkedTableRow lrow = (LinkedTableRow) row;
									if(lrow.select.getState()==Button.State.SELECTED){
										moved.add(lrow.view);
										tlc.tracks.remove(j-EXTRA_ROWS);
									}
								}
							}
						}
					}
					if(moved.size()>0){// If there's nothing to move, don't bother
						ArrayList<Track> target = view.tracks;
						for(int i=moved.size()-1;i>=0;i--){
							Track itrack = moved.get(i);
							itrack.setParent(view);// Update the parent
							target.add(itrack);
						}
						parent.remakeAll();
					}
				}
				
			});
			deselectAll.getButtonPressListeners().add(new ButtonPressListener(){

				@Override
				public void buttonPressed(org.apache.pivot.wtk.Button button) {
					TabPane.TabSequence tabs = parent.tabPane.getTabs();
					for(int i=tabs.getLength()-1;i>=0;i--){
						Component component = tabs.get(i);
						if(component instanceof LinkedTablePane){
							LinkedTablePane ltp = (LinkedTablePane) component;
							RowSequence rows = ltp.getRows();
							for(int j=rows.getLength()-1;j>=EXTRA_ROWS;j--){
								TablePane.Row row = rows.get(j);
								if(row instanceof LinkedTableRow){
									LinkedTableRow lrow = (LinkedTableRow) row;
									lrow.select.setState(Button.State.UNSELECTED);
								}
							}
						}
					}
				}
				
			});
			addNew.getButtonPressListeners().add(new ButtonPressListener(){
				
				@Override
				public void buttonPressed(org.apache.pivot.wtk.Button button) {
					String typeSelected = Objects.toString(typeSelector.getSelectedItem());
					// Use toString() to force string so we can use switch
					switch(typeSelected){
					case "Layered Track":{
						TrackLayerCompound tlc = new TrackLayerCompound(view);
						tlc.initTransient(view);
						view.tracks.add(tlc);
						addTrack(tlc);
						break;
					}
					case "Pattern Track":{
						TrackLayerSimple tls = new TrackLayerSimple(view);
						tls.initTransient(view);
						view.tracks.add(tls);
						addTrack(tls);
						break;
					}
					}
				}
				
			});
			moveHere.getComponentMouseListeners().add(new ComponentMouseListener(){

				@Override
				public boolean mouseMove(Component component, int x, int y) {
					return false;
				}

				@Override
				public void mouseOut(Component component) {
					moveHereKeys.clear();// Forget which keys are pressed, for safety
				}

				@Override
				public void mouseOver(Component component) {
				}
				
			});
			moveHere.getComponentKeyListeners().add(new ComponentKeyListener(){

				@Override
				public boolean keyPressed(Component component, int keyCode, KeyLocation keyLocation) {
					moveHereKeys.set(keyCode);
					return false;
				}

				@Override
				public boolean keyReleased(Component component, int keyCode, KeyLocation keyLocation) {
					moveHereKeys.clear(keyCode);
					switch(keyCode){
					case KeyEvent.VK_V:{
						if(moveHereKeys.get(KeyEvent.VK_CONTROL)){
							Session session = parent.session;
							Object clipBoard = session.clipBoard;
							if(clipBoard instanceof Track){
								Track tclip = (Track)clipBoard;
								if(!view.tracks.contains(tclip)){
									view.tracks.add(tclip);
									addTrack(tclip);
								}
							}
						}
						break;
					}
					}
					return false;
				}

				@Override
				public boolean keyTyped(Component component, char character) {
					// TODO Auto-generated method stub
					return false;
				}
				
			});
			//TODO add existing track by pasting on move here button
		}
		
		/**
		 * Update with a single added track
		 * <br>
		 * Inefficient for bulk add operations, so use only for singles
		 */
		public void addTrack(Track track){
			BXMLSerializer serializer = new BXMLSerializer();
			LinkedTableRow row = PivotSwingUtils.loadBxml(TrackLCEditor.class, "trackLCEditorTableRow.bxml", serializer);
			row.parent = this;
			row.view = track;
			row.init();
			getRows().add(row);
		}
	}
	
	public static class LinkedTableRow extends TablePane.Row implements Bindable{
		
		/**
		 * The parent {@link LinkedTablePane}
		 */
		public LinkedTablePane parent;
		/**
		 * The {@link Track} for which this instance
		 * provides a preview and/or UI
		 */
		public Track view;
		/**
		 * Keep the track's view component around for easy access
		 */
		public Track.ViewComponent viewComponent;
		/**
		 * The host {@link FillPane}
		 */
		public FillPane fill;
		/**
		 * The button to select/deselect the track
		 * <br>
		 * This is a toggle button, so pressed=selected,
		 * as such the deselect button will unpress all select buttons
		 */
		public PushButton select;
		/**
		 * The button to remove the track
		 */
		public PushButton remove;
		/**
		 * The button to move the track up the list
		 */
		public PushButton moveUp;
		/**
		 * The button to move the track down the list
		 */
		public PushButton moveDown;
		
		public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
			fill = (FillPane) namespace.get("fill");
			select = (PushButton) namespace.get("select");
			remove = (PushButton) namespace.get("remove");
			moveUp = (PushButton) namespace.get("up");
			moveDown = (PushButton) namespace.get("down");
		}
		
		/**
		 * Initialize, called after setting fields
		 */
		public void init(){
			// java won't let us access outside class
			final LinkedTableRow self = this;
			viewComponent=view.getViewComponent();
			fill.add(viewComponent);
			viewComponent.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener(){

				@Override
				public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count){
					switch(button){
					case LEFT:{// Open the UI for that track
						parent.parent.session.openUI(view);
						break;
					}
					case RIGHT:{// Select or deselect it
						PivotSwingUtils.invertToggle(select);
						break;
					}
					}
					return true;
				}

				@Override
				public boolean mouseDown(Component component, Mouse.Button button, int x, int y){
					// TODO Auto-generated method stub
					return true;
				}

				@Override
				public boolean mouseUp(Component component, Mouse.Button button, int x, int y){
					// TODO Auto-generated method stub
					return true;
				}
				
			});
			remove.getButtonPressListeners().add(new ButtonPressListener(){
				
				@Override
				public void buttonPressed(org.apache.pivot.wtk.Button button) {
					// if it works, it counts
					parent.view.tracks.remove(view);
					parent.getRows().remove(self);
				}
				
			});
			moveUp.getButtonPressListeners().add(new ButtonPressListener(){
				
				@Override
				public void buttonPressed(org.apache.pivot.wtk.Button button) {
					TablePane.RowSequence rows = parent.getRows();
					int index = rows.indexOf(self);
					// Can't move past first row, so check
					if(index>LinkedTablePane.EXTRA_ROWS){
						PivotSwingUtils.swap(rows, index, index-1);
					}
				}
				
			});
			moveDown.getButtonPressListeners().add(new ButtonPressListener(){
				
				@Override
				public void buttonPressed(org.apache.pivot.wtk.Button button) {
					TablePane.RowSequence rows = parent.getRows();
					int total = rows.getLength();
					int index = rows.indexOf(self);
					// Can't move past last row, so check
					if(index>LinkedTablePane.EXTRA_ROWS&&index<total-1){
						PivotSwingUtils.swap(rows, index, index+1);
					}
				}
				
			});
		}
	}

}
