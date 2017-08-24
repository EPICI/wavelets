package core;

import java.io.*;

import util.*;
import util.ui.PivotSwingUtils;
import javax.swing.*;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.skin.terra.TerraTheme;

import ui.*;

/**
 * Represents an active session
 * 
 * @author EPICI
 * @version 1.0
 */
public class Session {
	
	/**
	 * Clipboard, for copypasta
	 */
	public BetterClone<?,?> clipBoard;
	
	/**
	 * Log2 of buffer size
	 */
	protected int bufferSizeShift = 7;
	/**
	 * Samples in buffer
	 */
	protected int bufferSize = 1<<bufferSizeShift;
	/**
	 * Samples per second
	 */
	protected int sampleRate = 44100;
	
	/**
	 * The composition being edited
	 */
	public Composition composition;
	/**
	 * The filename to load from and save to
	 */
	public String filename;
	
	/**
	 * The window manager object
	 */
	public WindowManager windowManager;
	/**
	 * The window for the window manager
	 */
	public JInternalFrame windowManagerFrame;
	
	/**
	 * The desktop pane that contains everything in the UI
	 */
	public JDesktopPane desktopPane;
	/**
	 * The JFrame that holds the JDesktopPane
	 */
	public JFrame mainFrame;
	
	/**
	 * Color scheme, from TerraTheme
	 */
	protected ColorScheme colors;
	
	/**
	 * Pointer to the console or whatever was originally stdout
	 */
	protected PrintStream console;
	
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
		// Initialize Apache Pivot skins
		Theme theme = Theme.getTheme();
		colors = ColorScheme.getPivotColors();
		theme.set(CircularSlider.class, CircularSliderSkin.class);
		theme.set(TrackLSPreview.class, TrackLSPreviewSkin.class);
		
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
	 * Utility method, will open the UI of some track
	 * <br>
	 * JInternalFrame instances get opened as expected,
	 * otherwise it's added to a shared editor window
	 * 
	 * @param track the track to open the UI for
	 */
	public void openUI(Track track){
		MetaComponent<? extends JComponent> meta = track.getUI();
		JComponent component = meta.component;
		if(component instanceof JInternalFrame){
			windowManager.addWindow(meta.group,(JInternalFrame)component);
		}else{
			//TODO
		}
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
	 * Get the sample rate in Hz
	 * 
	 * @return the sample rate
	 */
	public int getSampleRate(){
		return sampleRate;
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
		setBufferSizeLog(Bits.binLog(n));
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
	
	/**
	 * Get the current color scheme
	 * 
	 * @return the current color scheme
	 */
	public ColorScheme getColors(){
		return colors;
	}
	
	/**
	 * Get the active {@link PrintStream} which should be used
	 * everywhere else in place of System.out and System.err
	 * 
	 * @return the target output
	 */
	public PrintStream getConsole(){
		return console;
	}
}
