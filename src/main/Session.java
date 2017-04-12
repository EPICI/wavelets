package main;

import components.*;
import util.*;
import util.ui.PivotSwingUtils;

import javax.swing.*;
import org.apache.pivot.wtk.*;

/**
 * 
 * @author EPICI
 * @version 1.0
 */
public class Session {
	
	/**
	 * Clipboard, for copypasta
	 */
	public BetterClone<?,?> clipBoard;
	
	protected int bufferSizeShift = 6;
	protected int bufferSize = 1<<bufferSizeShift;
	
	public Composition composition;
	public String filename;
	
	public WindowManager windowManager;
	public JInternalFrame windowManagerFrame;
	
	public JDesktopPane desktopPane;
	public JFrame mainFrame;
	
	/**
	 * Default constructor, try to find preferences on its own
	 */
	public Session(){
		init(null);
	}
	
	/**
	 * Initialize (reset) with given location at which preferences should be found
	 * 
	 * @param prefSource filename/location, or null for default
	 */
	public void init(String prefSource){
		windowManager = PivotSwingUtils.loadBxml(WindowManager.class, "windowManager.bxml");
		windowManager.session = this;
		windowManagerFrame = PivotSwingUtils.wrapPivotWindow(windowManager);
		desktopPane = new JDesktopPane();
		mainFrame = new JFrame("Wavelets");
		mainFrame.setContentPane(desktopPane);
		desktopPane.add(windowManagerFrame);
		PivotSwingUtils.showFrameDefault(windowManagerFrame);
		desktopPane.setVisible(true);
		mainFrame.setSize(1280, 720);
		mainFrame.setVisible(true);
		//TODO
	}
	
	/**
	 * Delegated method for creating a new composition object,
	 * primary purpose is to allow customization
	 */
	public void newComposition(){
		composition = new Composition(this);
		filename = null;
	}
	
	/**
	 * Gets the buffer size in samples
	 * 
	 * @return the buffer size, a power of 2
	 */
	public int getBufferSize(){
		return bufferSize;
	}
	
	/**
	 * Sets the buffer size in samples
	 * 
	 * @param n the buffer size, a power of 2
	 */
	public void setBufferSize(int n){
		setBufferSizeLog(BitUtils.binLog(n));
	}
	
	/**
	 * Sets the buffer size in samples to 2^n
	 * 
	 * @param n will get set to 2^n
	 */
	public void setBufferSizeLog(int n){
		if(n>=4&&n<=12){
			bufferSizeShift = n;
			bufferSize = 1<<bufferSizeShift;
		}else{
			throw new IllegalArgumentException("n ("+n+") must be between 4 and 12 inclusive");
		}
	}
}
