package ui;

import java.net.URL;
import java.util.ArrayList;

import javax.swing.JInternalFrame;

import org.apache.pivot.beans.*;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.content.ButtonData;

import core.*;
import util.ui.PivotSwingUtils;

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
		for(Component component:tabPane){
			if(component instanceof LinkedTablePane){
				LinkedTablePane ltp = (LinkedTablePane) component;
				if(ltp.view==track)return;
			}
		}
		addNewTLC(track);
	}
	
	private void addNewTLC(TrackLayerCompound track){
		try{
			LinkedTablePane linked = PivotSwingUtils.loadBxml(TrackLCEditor.class, "trackLCEditorTable.bxml");
			linked.parent = this;
			linked.view = track;
			linked.init();
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
					ArrayList<Track> target = view.tracks;
					for(int i=moved.size()-1;i>=0;i--){
						target.add(moved.get(i));
					}
					parent.remakeAll();
				}
				
			});
			//TODO listeners + add new track + add existing track
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
			viewComponent=view.getViewComponent();
			fill.add(viewComponent);
			viewComponent.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener(){

				@Override
				public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count){
					parent.parent.session.openUI(view);
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
			//TODO listeners for buttons + track view click listener
		}
	}

}
