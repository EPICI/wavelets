package components;

import java.net.URL;
import java.util.ArrayList;

import org.apache.pivot.beans.*;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.content.ButtonData;

import main.*;
import utils.PivotSwingUtils;

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
		
		@Override
		public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
			tabName = (ButtonData) namespace.get("tabName");
		}
		
		/**
		 * Initialize, called after setting fields
		 */
		public void init(){
			tabName.setText(view.getName());
			ArrayList<Track> tracks = view.tracks;
			if(tracks.size()!=0){
				BXMLSerializer serializer = new BXMLSerializer();
				for(Track track:view.tracks){
					LinkedTableRow row = PivotSwingUtils.loadBxml(TrackLCEditor.class, "trackLCEditorTableRow.bxml", serializer);
					row.parent = this;
					row.init();
					getRows().add(row);
				}
			}
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
		 * The host {@link FillPane}
		 */
		public FillPane fill;
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
			remove = (PushButton) namespace.get("remove");
			moveUp = (PushButton) namespace.get("up");
			moveDown = (PushButton) namespace.get("down");
		}
		
		/**
		 * Initialize, called after setting fields
		 */
		public void init(){
			//TODO
		}
	}

}
