package ui;

import java.net.URL;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;

import core.Named;
import core.Session;

/**
 * Interface implemented by editors of some data.
 * 
 * @author EPICI
 * @version 1.0
 *
 * @param <T> the type of data this edits.
 */
public interface DataEditor<T> {

	/**
	 * Add the interface for this instance if it isn't already present.
	 * Returns true if it was added, in which case it wasn't already present.
	 * 
	 * @param data
	 * @return
	 */
	public boolean addEditorData(T data);
	
	/**
	 * Base class for data editor implementations which
	 * assign to each tab an instance to edit,
	 * and have its editor in that tab.
	 * 
	 * @author EPICI
	 * @version 1.0
	 *
	 * @param <T> see documentation for {@link DataEditor}
	 */
	public static abstract class Tabbed<T> extends Window implements DataEditor<T>, Bindable {
		
		/**
		 * The current/linked session
		 */
		public Session session;
		/**
		 * The tab pane to switch between tracks
		 */
		public TabPane tabPane;
		
		public boolean addEditorData(T data){
			TabPane.TabSequence tabs = tabPane.getTabs();
			for(Component component:tabs){
				if(component instanceof Instance<?>){
					Instance<?> ieditor = (Instance<?>) component;
					Object other = ieditor.getEditorData();
					if(
							// different name may have same data but must be treated as separate
							(data instanceof Named)
							&& (other instanceof Named)
							? ((Named)data).getName().equals(((Named)other).getName())
							// finally, do standard equality test
							: data.equals(other)
							)return false;
				}
			}
			addNewEditorData(data);
			return true;
		}
		
		/**
	     * Called to initialize the class after it has been completely
	     * processed and bound by the serializer.
	     * <br>
	     * Notably for the purposes of {@link DataEditor.Tabbed},
	     * it sets {@link #tabPane}.
	     *
	     * @param namespace
	     * The serializer's namespace. The bindable object can use this to extract named
	     * values defined in the BXML file. Alternatively, the {@link BXML} annotation
	     * can be used by trusted code to automatically map namespace values to member
	     * variables.
	     *
	     * @param location
	     * The location of the BXML source. May be <tt>null</tt> if the location of the
	     * source is not known.
	     *
	     * @param resources
	     * The resources that were used to localize the deserialized content. May be
	     * <tt>null</tt> if no resources were specified.
	     */
		@Override
		public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
			tabPane = (TabPane) namespace.get("tabPane");
		}
		
		/**
		 * For internal use only: add the editor for a new instance regardless of whether
		 * it is already in {@link #tabPane}.
		 * 
		 * @param data
		 */
		protected abstract void addNewEditorData(T data);
		
		/**
		 * Call after fields are initialized.
		 * {@link #session} should have been set externally.
		 * {@link #tabPane} should be set by
		 * {@link #initialize(org.apache.pivot.collections.Map, java.net.URL, org.apache.pivot.util.Resources)}.
		 */
		public void init(){
			
		}
		
	}
	
	/**
	 * Interface implemented by editors of a single instance.
	 * 
	 * @author EPICI
	 * @version 1.0
	 *
	 * @param <T> see documentation for {@link DataEditor}.
	 */
	public static interface Instance<T>{
		
		/**
		 * Get the instance which is being edited by this editor.
		 * 
		 * @return
		 */
		public T getEditorData();
		
	}

}
