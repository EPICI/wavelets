package core;

import java.util.*;
import javax.swing.*;
import org.python.core.*;
import util.jython.*;
import util.ui.PivotSwingUtils;
import util.*;
import util.hash.*;
import ui.*;

/**
 * Contains patterns
 * 
 * @author EPICI
 * @version 1.0
 */
public class TrackLayerSimple implements Track, TransientContainer<TrackLayerCompound> {
	private static final long serialVersionUID = 1L;

	/**
	 * All the patterns and their respective delays,
	 * encoded as set bits in a bitset
	 */
	public final IdentityHashMap<Pattern,BitSet> patterns;
	/**
	 * List of active voices, used for playback
	 */
	protected transient ArrayList<Voice> voices;
	/**
	 * Parent {@link TrackLayerCompound}
	 */
	protected transient TrackLayerCompound parentTLC;
	/**
	 * The name of this track.
	 */
	protected String name;
	
	/**
	 * Hash key for <i>hashCode()</i>
	 */
	public static final long HK_HC = QuickKeyGen.next64();
	
	/**
	 * 
	 * @param parent the parent object
	 * @see TrackLayerCompound
	 */
	public TrackLayerSimple(TrackLayerCompound parent){
		patterns = new IdentityHashMap<>();
		initTransient(parent);
	}
	
	@Override
	public void applyTo(MetaSamples current) {
		Composition composition = current.composition;
		Session session = composition.currentSession;
		//Setup
		if(!patterns.isEmpty()){
			//Add new voices if necessary
			for(Pattern pattern:patterns.keySet()){
				PrimitiveIterator.OfInt iter = patterns.get(pattern).stream().iterator();
				while(iter.hasNext()){
					int delay = iter.nextInt();
					double start = composition.measuresToSeconds(delay);
					double end = composition.measuresToSeconds(delay+pattern.length);
					if(start<=current.endPos&&end>=current.startPos){
						double invDivisions = 1d/pattern.divisions;
						ArrayList<double[]> toSendList = new ArrayList<double[]>();
						for(int[] clip:pattern.clips){
							double clipStart = composition.measuresToSeconds(delay+clip[0]*invDivisions);
							double clipEnd = composition.measuresToSeconds(delay+(clip[0]+clip[1])*invDivisions);
							double clipLength = clipEnd-clipStart;
							if(clipStart<=current.endPos&&clipEnd>=current.startPos){
								double voiceDelay = clipStart-current.startPos;
								toSendList.add(new double[]{voiceDelay,clipLength,clip[2]});
							}
						}
						int numToSend = toSendList.size();
						if(numToSend>0){
							double[][] toSend = toSendList.toArray(new double[numToSend][]);
							pattern.synthesizer.setGlobals(current.vars);
							pattern.synthesizer.spawnVoices(toSend, this, session);
						}
					}
				}
			}
		}
		if(voices.size()>0){
			//Process existing voices
			int sampleCount = current.sampleData.length;
			Samples toLayer = Samples.blankSamples(current.sampleRate, sampleCount);
			for(Voice voice:voices){
				Samples toAdd = voice.nextSegment(sampleCount);
				toLayer.layerOnThisLazy(toAdd);
			}
			//Do layering
			current.layerOnThisLazy(toLayer);
			//Remove dead voices
			Iterator<Voice> viter = voices.iterator();
			while(viter.hasNext()){
				Voice voice = viter.next();
				if(!voice.isAlive()){
					viter.remove();
				}
			}
		}
	}

	@Override
	public void initTransient(TrackLayerCompound parent) {
		voices = new ArrayList<Voice>();
		parentTLC = parent;
	}
	
	public Composition parentComposition(){
		return parentTLC.parentComposition();
	}
	
	public boolean setParent(Object newParent){
		if(newParent instanceof TrackLayerCompound){
			parentTLC = (TrackLayerCompound)newParent;
			return true;
		}
		return false;
	}
	
	/**
	 * Adds a voice
	 * 
	 * @param pattern the source pattern
	 * @param args Python args + keyword args
	 * @param keywords Python keywords
	 */
	public void addVoice(Pattern pattern,PyObject[] args,String[] keywords){
		Factory<Voice> voiceFactory = pattern.getVoiceFactory();
		addVoice(voiceFactory.create(args, keywords));
	}
	
	/**
	 * Adds a single voice
	 * 
	 * @param voice the voice to add
	 */
	public void addVoice(Voice voice){
		voices.add(voice);
	}
	
	/**
	 * Agressively removes all voices
	 */
	public void removeAllVoices(){
		for(Voice voice:voices){
			voice.destroy();
		}
		voices.clear();
	}
	
	/**
	 * Adds a pattern
	 * 
	 * @param pattern the pattern to add
	 * @param delay the delay to add with
	 */
	public void addPattern(Pattern pattern,int delay){
		BitSet delays = patterns.get(pattern);
		if(delays==null){
			delays = new BitSet();
			patterns.put(pattern, delays);
		}
		delays.set(delay);
	}
	
	/**
	 * Removes a pattern
	 * 
	 * @param pattern the target pattern
	 * @param delay the delay to remove
	 */
	public void removePattern(Pattern pattern,int delay){
		BitSet delays = patterns.get(pattern);
		if(delays!=null){
			delays.clear(delay);
		}
	}
	
	/**
	 * Attempts to change a delay
	 * <br>
	 * Force flag:
	 * <ul>
	 * <li>If true, functionally equivalent to <i>removePattern</i> followed by <i>addPattern</i></li>
	 * <li>If false, will do nothing if there is no pattern at <i>oldDelay</i> or there is already a pattern at <i>newDelay</i></li>
	 * </ul>
	 * 
	 * @param pattern the keyed pattern
	 * @param oldDelay the original delay
	 * @param newDelay the new delay to set it to
	 * @param force explained in doc
	 * @return true if successful or forced
	 */
	public boolean updateDelay(Pattern pattern,int oldDelay,int newDelay,boolean force){
		BitSet delays = patterns.get(pattern);
		if(force){
			if(delays==null){
				delays=new BitSet();
				patterns.put(pattern, delays);
			}
			delays.clear(oldDelay);//No branch=faster
			delays.set(newDelay);
			return true;
		}else{
			boolean success = delays!=null&&delays.get(oldDelay)&&!delays.get(newDelay);
			if(success&&oldDelay!=newDelay){
				delays.clear(oldDelay);
				delays.set(newDelay);
			}
			return success;
		}
		
	}
	
	/**
	 * Get the current number of patterns
	 * 
	 * @return the current pattern count
	 */
	public int getPatternCount(){
		return patterns.size();
	}
	
	public double[] getTimeBounds(){
		Composition composition = parentComposition();
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for(Pattern pattern:patterns.keySet()){
			BitSet delays = patterns.get(pattern);
			int first = delays.nextSetBit(0);
			int last = delays.length()-1+pattern.length;
			if(first<min)min=first;
			if(last>max)max=last;
		}
		return new double[]{composition.measuresToSeconds(min),composition.measuresToSeconds(max)};
	}

	public static TrackLSEditor editor;
	public static MetaComponent<JInternalFrame> meta;
	@Override
	public MetaComponent<JInternalFrame> getUI() {
		if(meta==null||meta.component.isClosed()){
			Session session = parentComposition().currentSession;
			editor = TrackLSEditor.createNew();
			editor.session = session;
			JInternalFrame wrapped = PivotSwingUtils.wrapPivotWindow(editor);
			meta = new MetaComponent<>("Default Pattern Track Editor","Pattern Track Editor",wrapped);
		}
		editor.addTLS(this);
		return meta;
	}

	@Override
	public ViewComponent getViewComponent() {
		return new TrackLSPreview(this);
	}
	
	public int hashCode(){
		HashTriArx hash = new HashTriArx(HK_HC);
		hash.absorbObj(patterns);
		return hash.squeezeInt();
	}
	
	public String getName(){
		if(name==null || name.length()==0)return Track.defaultName("Pattern Track", this);
		return name;
	}
	
	public void setName(String newName){
		name=newName;
	}

}
