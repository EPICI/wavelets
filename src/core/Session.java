package core;

import java.awt.event.WindowEvent;
import java.io.*;
import java.util.*;
import util.*;
import util.ui.PivotSwingUtils;
import javax.swing.*;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.skin.terra.TerraTheme;
import core.synth.*;
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
	 * <br>
	 * Used to force usage of the {@link BetterClone} interface,
	 * but actually the "clipboard" is not always used to copy objects,
	 * and in many instances a valid way to transfer or move objects
	 */
	public Object clipBoard;
	
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
	 * Preferences/settings
	 */
	protected Preferences preferences;
	
	/**
	 * Pointer to the console or whatever was originally stdout
	 */
	protected PrintStream console;
	
	/**
	 * Audio player
	 */
	protected PlayerDoubleBuffer player;
	
	/**
	 * A special marker used as the time for when no audio
	 * is playing, and the point from which audio starts playing
	 * <br>
	 * Always update this together with <i>timeCursorEnd</i>
	 */
	public double timeCursor;
	/**
	 * If there is a time range selection, this is the other end of it
	 * <br>
	 * If this is equal to <i>timeCursor</i> it is assumed that no range
	 * is selected
	 */
	public double timeCursorEnd;
	
	/**
	 * Common names of classes which the user can access.
	 * Removing existing values may break code which assumes it,
	 * so only add names for your own classes.
	 */
	public Map<Class<?>,String> commonNames;
	
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
		theme.set(TrackLSEditor.LinkedEditorPane.class, TrackLSEditor.LinkedEditorPaneSkin.class);
		theme.set(PatternEditor.LinkedEditorInnerPane.class, PatternEditor.LinkedEditorInnerPaneSkin.class);
		theme.set(DoubleInput.DoubleSlider.class, DoubleInput.DoubleSliderSkin.class);
		// create core UI
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
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new java.awt.event.WindowListener(){

			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				// When you click the close button
				// TODO ask to save changes
				mainFrame.dispose();
				// exit
				System.exit(0);
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}
			
		});
		mainFrame.setResizable(true);
		mainFrame.setVisible(true);
		// make the audio player
		player = new PlayerDoubleBuffer(bufferSize);
		player.session = this;
		// make the common names map
		commonNames = new IdentityHashMap<>();
		commonNames.put(Composition.class, "Project");
		commonNames.put(Track.class, "Track");
		commonNames.put(TrackLayerSimple.class, "Pattern Track");
		commonNames.put(TrackLayerCompound.class, "Layered Track");
		commonNames.put(Clip.Template.class, "Clip Template");
		commonNames.put(Curve.class, "1D Curve");
		commonNames.put(Pattern.class, "Pattern");
		commonNames.put(Synthesizer.class, "Synthesizer");
		commonNames.put(SynthNOsc.class, "Nx Osc Synth");
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
		MetaComponent<? extends JComponent> ometa = track.getUI();
		JComponent component = ometa.component;
		if(component instanceof JInternalFrame){
			MetaComponent<JInternalFrame> meta = new MetaComponent<>(ometa);
			windowManager.addWindow(meta,true);
		}else{
			// not supported yet
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
	 * Method to allow outside users to get the player
	 * in order to play audio or do other things with it
	 * 
	 * @return
	 */
	public Player getPlayer(){
		return player;
	}
	
	/**
	 * If the composition or a track is currently
	 * being previewed, this gets the current time
	 * in the composition, otherwise it returns
	 * the location of the time cursor
	 * 
	 * @return
	 */
	public double getCurrentTime(){
		if(player.isPlaying()){
			return player.currentTime();
		}else{
			return timeCursor;
		}
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
	 * Get the current color scheme used in a session
	 * <br>
	 * If the session or its colors are null, return default colors,
	 * so this will never fail or return null
	 * 
	 * @param session
	 * @return
	 */
	public static ColorScheme getColors(Session session){
		ColorScheme result;
		if(session==null||(result=session.getColors())==null)return ColorScheme.getPivotColors();
		return result;
	}
	
	/**
	 * Get the current preferences
	 * 
	 * @return the current preferences
	 */
	public Preferences getPreferences(){
		return preferences;
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
	
	private static final String SESSION_CLASS_NAME = Session.class.getCanonicalName();
	private static final String PREFERENCES_CLASS_NAME = Preferences.class.getCanonicalName();
	private static final String COMPOSITION_CLASS_NAME = Composition.class.getCanonicalName();
	private static final String PLAYER_CLASS_NAME = Player.class.getCanonicalName();
	/**
	 * For an options map for {@link BetterClone#copy(int, Map)},
	 * add some common options, including the current session.
	 * 
	 * @param options
	 */
	public void setCopyOptions(Map<String,Object> options){
		// add current session
		options.put("session", this);
		// blacklist big classes
		Collection<String> copyBlacklist = (Collection<String>) options.get("blacklist");
		copyBlacklist.add("*"+SESSION_CLASS_NAME);
		copyBlacklist.add("*"+PREFERENCES_CLASS_NAME);
		copyBlacklist.add("*"+COMPOSITION_CLASS_NAME);
		copyBlacklist.add("*"+PLAYER_CLASS_NAME);
	}
	
	/**
	 * Get the human-readable common name for a class.
	 * Currently does not use localization.
	 * 
	 * @param cls
	 * @return
	 */
	public String getCommonName(Class<?> cls){
		// if it's in the map, return that
		String name = commonNames.get(cls);
		if(name!=null)return name;
		// give up
		return cls.getSimpleName();
	}
	
}
