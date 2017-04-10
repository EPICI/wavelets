package components;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URL;
import org.apache.pivot.beans.*;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Button.DataRenderer;
import org.apache.pivot.wtk.ButtonGroup;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import java.util.HashMap;

import main.*;
import utils.*;

/**
 * Window manager UI, allows the user to manage the windows
 * 
 * @author EPICI
 * @version 1.0
 */
public class WindowManager extends Window implements Bindable {
	
	private static final String NAME_TRACKLCEDITOR = "Layered Track Editor";
	private static final String NAME_LIST = "list";
	private static final String NAME_SELECTOR = "selector";
	private static final String NAME_ADD = "add";
	
	/**
	 * The current/linked session
	 */
	public Session session;
	/**
	 * A {@link BoxPane} containing all window controls
	 */
	public BoxPane list;
	/**
	 * Maps all names to window controls
	 */
	public HashMap<String,LinkedTablePane> windows;
	/**
	 * Selector, used to select some core windows to open
	 */
	public ListButton addSelector;
	/**
	 * Click to open the selected window
	 */
	public PushButton addButton;

	/**
	 * Part of {@link Bindable}, allows for initialization by {@link BXMLSerializer}
	 */
	public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
		list = (BoxPane) namespace.get(NAME_LIST);
		windows = new HashMap<>();
		addSelector = (ListButton) namespace.get(NAME_SELECTOR);
		addButton = (PushButton) namespace.get(NAME_ADD);
		addButton.getButtonListeners().add(new ButtonListener(){

			@Override
			public void actionChanged(Button button, Action action) {
				Object selection = addSelector.getSelectedItem();
				if(selection instanceof String){//Safety
					String sel = (String) selection;
					switch(sel){
					case NAME_TRACKLCEDITOR:{
						if(!windows.containsKey(NAME_TRACKLCEDITOR)){
							TrackLCEditor trackLCEditor = PivotSwingUtils.loadBxml(TrackLCEditor.class, "trackLCEditor.bxml");
							JInternalFrame wrapped = PivotSwingUtils.wrapPivotWindow(trackLCEditor);
							addWindow(NAME_TRACKLCEDITOR,wrapped);
						}
						break;
					}
					}
				}
			}

			@Override
			public void buttonDataChanged(Button arg0, Object arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void buttonGroupChanged(Button arg0, ButtonGroup arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void dataRendererChanged(Button arg0, DataRenderer arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void toggleButtonChanged(Button arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void triStateChanged(Button arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	/**
	 * Add a new window
	 * 
	 * @param name the name to add under
	 * @param window the window to add
	 */
	public void addWindow(String name,JInternalFrame window){
		if(!windows.containsKey(name)){// If it already exists, don't re-add it
			try {
				LinkedTablePane linked = PivotSwingUtils.loadBxml(WindowManager.class,"windowManagerTable.bxml");
				linked.parent = this;
				linked.name = name;
				linked.frame = window;
				linked.label.setText(name);
				window.addInternalFrameListener(new InternalFrameListener(){

					@Override
					public void internalFrameActivated(InternalFrameEvent arg0) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void internalFrameClosed(InternalFrameEvent arg0) {
						removeWindow(name);
					}

					@Override
					public void internalFrameClosing(InternalFrameEvent arg0) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void internalFrameDeactivated(InternalFrameEvent arg0) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void internalFrameDeiconified(InternalFrameEvent arg0) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void internalFrameIconified(InternalFrameEvent arg0) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void internalFrameOpened(InternalFrameEvent arg0) {
						// TODO Auto-generated method stub
						
					}
					
				});
				list.add(linked);
				windows.put(name, linked);
			}catch(NullPointerException exception){//Load failed
				exception.printStackTrace();
			}
		}
	}
	
	/**
	 * Remove a window with the specified name
	 * 
	 * @param name the window's name
	 */
	public void removeWindow(String name){
		LinkedTablePane linked = windows.get(name);
		if(linked==null)return;//Safety
		JInternalFrame window = linked.frame;
		if(!window.isClosed()){
			try {
				window.setClosed(true);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}
			window.dispose();
		}
		list.remove(linked);
		windows.remove(name);
	}
	
	/**
	 * Used by {@link WindowManager} internally, accessible because API reasons
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public static class LinkedTablePane extends TablePane implements Bindable{
		/**
		 * The attached window
		 */
		public JInternalFrame frame;
		/**
		 * Click this button to close the window
		 */
		public PushButton remove;
		/**
		 * Click this button to open the window
		 */
		public PushButton open;
		/**
		 * Descriptor
		 */
		public Label label;
		/**
		 * Parent {@link WindowManager}
		 */
		public WindowManager parent;
		/**
		 * Name, used internally
		 */
		public String name;
		
		/**
		 * Part of {@link Bindable}, allows for initialization by {@link BXMLSerializer}
		 */
		public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
			remove = (PushButton) namespace.get("remove");
			open = (PushButton) namespace.get("open");
			label = (Label) namespace.get("label");
			final LinkedTablePane self = this;
			remove.getButtonListeners().add(new ButtonListener(){

				@Override
				public void actionChanged(Button button, Action action) {
					self.parent.removeWindow(self.name);
				}

				@Override
				public void buttonDataChanged(Button arg0, Object arg1) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void buttonGroupChanged(Button arg0, ButtonGroup arg1) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void dataRendererChanged(Button arg0, DataRenderer arg1) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void toggleButtonChanged(Button arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void triStateChanged(Button arg0) {
					// TODO Auto-generated method stub
					
				}
				
			});
			open.getButtonListeners().add(new ButtonListener(){

				@Override
				public void actionChanged(Button button, Action action) {
					JInternalFrame frame = self.frame;
					frame.setVisible(true);
					frame.toFront();
					frame.requestFocusInWindow();
				}

				@Override
				public void buttonDataChanged(Button arg0, Object arg1) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void buttonGroupChanged(Button arg0, ButtonGroup arg1) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void dataRendererChanged(Button arg0, DataRenderer arg1) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void toggleButtonChanged(Button arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void triStateChanged(Button arg0) {
					// TODO Auto-generated method stub
					
				}
				
			});
		}
	}

}
