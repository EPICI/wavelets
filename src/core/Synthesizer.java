package core;

import org.python.core.PyObject;
import util.jython.*;
import java.awt.Color;
import java.io.Serializable;
import java.util.HashMap;
import javax.swing.*;
import util.*;

/**
 * Generic synthesizer interface, shared with Java and Python
 * <br>
 * Used in tandem with patterns to generate voices which may
 * rely on the synthesizer
 * 
 * @author EPICI
 * @version 1.0
 */
public interface Synthesizer extends Named, Destructable, TransientContainer<Composition> {
	/**
	 * Use array of double arrays to indicate which clips need to be created
	 * <br>
	 * Inner double arrays are [start,end,pitch,volume,other parameters]
	 * <br>
	 * The synthesizer should have the rest of the necessary data already
	 * <br>
	 * Should add voices to the target
	 * 
	 * @param clips a list of clips in [start,end,pitch,other parameters] format
	 * @param target the target {@link TrackLayerSimple} to add voices to
	 * @param session current session, from which other data can be derived
	 */
	public void spawnVoices(double[][] clips,TrackLayerSimple target,Session session);
	
	/**
	 * Create a single live voice with the specified pitch
	 * <br>
	 * Used for real time previews and stuff
	 * <br>
	 * Since only one is needed, direct return is fine
	 * 
	 * @param params the pitch, then any other parameters
	 * @param session current session, from which other data can be derived
	 * @return the newly created live voice
	 */
	public Voice spawnLiveVoice(int[] params,Session session);
	
	/**
	 * Request UI with metadata
	 * 
	 * @return frame with UI, which will be opened and tracked
	 * by the window manager along with the others
	 */
	public MetaComponent<? extends JInternalFrame> getUI();
	
	/**
	 * Sets the variables variable so it can be manipulated or read
	 * <br>
	 * Should be called before spawning voices
	 * 
	 * @param vars the variables map
	 */
	public void setGlobals(HashMap<String,Object> vars);
	
	/**
	 * @return true if this is a Python synthesizer
	 */
	public boolean isPython();
	
	/**
	 * Get voice factory object directly
	 * <br>
	 * Should only be used for Java synths, since otherwise
	 * it would actually be slower or more complicated
	 * 
	 * @return factory object for this synthesizer
	 */
	public Factory<Voice> getVoiceFactory();
	
	/**
	 * Get a list, tuple, or dict with information needed to make a PyVoiceFactory
	 * <br>
	 * Should only be used for Python synths, since otherwise
	 * it would actually be slower and more complicated
	 * 
	 * @return list, tuple or dict with the information
	 */
	public PyObject getPvfInfo();
	
	/**
	 * Get a colour which represents this synthesizer
	 * <br>
	 * It should be somewhat representative in that similar synths
	 * should have similar colours (though different synths may
	 * not necessarily have different colours, after all, it isn't a form
	 * of hashing)
	 * 
	 * @param time time at which to get the signature
	 * @return a colour "signature"
	 * @see VarDouble
	 */
	public Color getColorSignature(double time);
	
	/**
	 * Synthesizers are saved with an instance of this rather than
	 * the actual synthesizer, that way it's not necessary to store
	 * all the data with the project files, but only the data relevant
	 * to the instance. Common data like scripts and preferences can go
	 * elsewhere. Since the {@link Session} is needed to resolve such
	 * information, it is used in the interface here.
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public static interface Specification extends Named, Serializable{
		/**
		 * Create or fetch the synthesizer object corresponding to this
		 * specification, using the session.
		 * 
		 * @param session current session
		 * @return specified synthesizer object
		 */
		public Synthesizer resolve(Session session);
		
		/**
		 * Specification which stores the synthesizer directly.
		 * 
		 * @author EPICI
		 * @version 1.0
		 */
		public static class DirectSpecification implements Specification{
			private static final long serialVersionUID = 1L;
			
			/**
			 * The synthesizer instance
			 */
			public Synthesizer synthesizer;
			
			/**
			 * Standard constructor, used to wrap a synthesizer.
			 * 
			 * @param synthesizer
			 */
			public DirectSpecification(Synthesizer synthesizer){
				this.synthesizer = synthesizer;
			}
			
			public Synthesizer resolve(Session session){
				return synthesizer;
			}
			
			@Override
			public String getName(){
				return synthesizer.getName();
			}
			
			@Override
			public boolean setName(String newName){
				return synthesizer.setName(newName);
			}
		}
	}
	
	/**
	 * Use default methods to get a specification for a synthesizer
	 * 
	 * @param synth
	 * @return
	 */
	public static Specification specWrap(Synthesizer synth){
		return new Specification.DirectSpecification(synth);
	}
}
