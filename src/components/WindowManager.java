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

/**
 * Window manager, allows the user to manage the windows
 * 
 * @author EPICI
 * @version 1.0
 */
public class WindowManager extends Window implements Bindable {
	
	/**
	 * A {@link BoxPane} containing all window controls
	 */
	public static BoxPane list;
	/**
	 * Maps all names to window controls
	 */
	public static HashMap<String,LinkedTablePane> windows;
	/**
	 * Selector, used to select some core windows to open
	 */
	public static ListButton addSelector;
	/**
	 * Click to open the selected window
	 */
	public static PushButton addButton;

	/**
	 * Part of {@link Bindable}, allows for initialization by {@link BXMLSerializer}
	 */
	public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
		list = (BoxPane) namespace.get("list");
		windows = new HashMap<>();
		addSelector = (ListButton) namespace.get("selector");
		addButton = (PushButton) namespace.get("add");
		addButton.getButtonListeners().add(new ButtonListener(){

			@Override
			public void actionChanged(Button button, Action action) {
				Object selection = addSelector.getSelectedItem();
				if(selection instanceof String){//Safety
					String sel = (String) selection;
					switch(sel){
					//TODO
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
		if(windows.containsKey(name)){
			// It already exists, don't re-add it
			return;
		}
		LinkedTablePane linked;
		try {
			linked = (LinkedTablePane) new BXMLSerializer().readObject(WindowManager.class,"windowManager.bxml");
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
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SerializationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Remove a window with the specified name
	 * 
	 * @param name the window's name
	 */
	public void removeWindow(String name){
		LinkedTablePane linked = windows.get(name);
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
