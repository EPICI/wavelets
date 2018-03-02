package ui;

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
import core.*;
import org.apache.pivot.wtk.ButtonGroup;
import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.util.HashMap;
import util.*;
import util.ui.PivotSwingUtils;

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
		addButton.getButtonPressListeners().add(new ButtonPressListener(){

			@Override
			public void buttonPressed(Button button) {
				Object selection = addSelector.getSelectedItem();
				if(selection instanceof String){//Safety
					String sel = (String) selection;
					switch(sel){
					case NAME_TRACKLCEDITOR:{
						if(!windows.containsKey(NAME_TRACKLCEDITOR)){
							TrackLCEditor trackLCEditor = PivotSwingUtils.loadBxml(TrackLCEditor.class, "trackLCEditor.bxml");
							trackLCEditor.session=session;
							trackLCEditor.init();
							JInternalFrame wrapped = PivotSwingUtils.wrapPivotWindow(trackLCEditor);
							addWindow(NAME_TRACKLCEDITOR,wrapped);
							openWindow(NAME_TRACKLCEDITOR);
						}
						break;
					}
					}
				}
			}
			
		});
	}
	
	/**
	 * Shortcut for adding a new window
	 * <br>
	 * Adds by <i>group</i>
	 * 
	 * @param meta contains reference to frame and additional metadata
	 */
	public void addWindow(MetaComponent<? extends JInternalFrame> meta){
		addWindow(meta.group,meta.component);
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
				session.desktopPane.add(window);
			}catch(NullPointerException exception){//Load failed
				exception.printStackTrace();
			}
		}
	}
	
	/**
	 * Open a window by name
	 * 
	 * @param name the name of the window to open
	 * @return true if successful
	 */
	public boolean openWindow(String name){
		LinkedTablePane linked = windows.get(name);
		if(linked==null)return false;
		JInternalFrame frame = linked.frame;
		return PivotSwingUtils.showFrame(frame,nextFrameLocation(),nextFrameDimensions());
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
	 * Where should the next frame be opened at?
	 * 
	 * @return
	 */
	public int[] nextFrameLocation(){
		java.awt.Point topLeft = session.windowManagerFrame.getLocation();
		for(LinkedTablePane ltp:windows.values()){
			JInternalFrame frame = ltp.frame;
			if(!frame.isVisible())continue;
			topLeft = frame.getLocation();
			break;
		}
		return nextFrameLocationOffset(topLeft.x,topLeft.y);
	}
	
	/**
	 * Given the location of this frame, offset it to get a
	 * potential next frame location
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	protected int[] nextFrameLocationOffset(int x,int y){
		final double MULTIPLIER = 0.25;
		final double XFRACTION = 0.01;
		final double YFRACTION = 0.05;
		java.awt.Dimension dim = session.desktopPane.getSize();
		int width = dim.width;
		int height = dim.height;
		return new int[]{(int)((x+width*XFRACTION)%(width*MULTIPLIER)),
				(int)((y+height*YFRACTION)%(height*MULTIPLIER))};
	}
	
	/**
	 * What size should the next frame be?
	 * 
	 * @return
	 */
	public int[] nextFrameDimensions(){
		final double MULTIPLIER = 0.75;
		java.awt.Dimension dim = session.desktopPane.getSize();
		return new int[]{(int)(dim.width*MULTIPLIER),(int)(dim.height*MULTIPLIER)};
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
			remove.getButtonPressListeners().add(new ButtonPressListener(){

				@Override
				public void buttonPressed(Button button) {
					self.parent.removeWindow(self.name);
				}
				
			});
			open.getButtonPressListeners().add(new ButtonPressListener(){

				@Override
				public void buttonPressed(Button button) {
					parent.openWindow(self.name);
				}
				
			});
		}
	}

}
