package core;

import java.util.*;
import javax.swing.*;
import org.python.core.*;
import util.jython.*;
import util.*;

/**
 * Contains patterns
 * 
 * @author EPICI
 * @version 1.0
 */
public class TrackLayerSimple implements Track, TransientContainer<TrackLayerCompound> {
	private static final long serialVersionUID = 1L;

	/**
	 * All the patterns and their respective delays
	 */
	public final IdentityHashMap<Pattern,HashSet<Integer>> patterns;
	/**
	 * List of active voices, used for playback
	 */
	protected transient ArrayList<Voice> voices;
	/**
	 * Parent {@link TrackLayerCompound}
	 */
	protected transient TrackLayerCompound parentTLC;
	
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
		//Setup
		if(!patterns.isEmpty()){
			//Add new voices if necessary
			for(Pattern pattern:patterns.keySet()){
				for(int delay:patterns.get(pattern)){
					double start = delay*current.speedMult;
					double end = (delay+pattern.length)*current.speedMult;
					if(start<=current.endPos&&end>=current.startPos){
						double multiplier = current.speedMult/pattern.divisions;
						ArrayList<double[]> toSendList = new ArrayList<double[]>();
						for(int[] clip:pattern.clips){
							double clipStart = clip[0]*multiplier+start;
							double clipLength = clip[1]*multiplier;
							double clipEnd = clipStart+clipLength;
							if(clipStart<=current.endPos&&clipEnd>=current.startPos){
								double voiceDelay = clipStart-current.startPos;
								toSendList.add(new double[]{voiceDelay,clipLength,clip[2]});
							}
						}
						int numToSend = toSendList.size();
						if(numToSend>0){
							double[][] toSend = toSendList.toArray(new double[numToSend][]);
							pattern.synthesizer.setGlobals(current.vars);
							pattern.synthesizer.spawnVoices(toSend, this, current.sampleRate);
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
		HashSet<Integer> delays = patterns.get(pattern);
		if(delays==null){
			delays = new HashSet<>();
			patterns.put(pattern, delays);
		}
		delays.add(delay);
	}
	
	/**
	 * Attempts to change a delay
	 * <br>
	 * Because sets don't allow duplicates, this can result in removing a pattern instance
	 * 
	 * @param pattern the keyed pattern
	 * @param oldDelay the original delay
	 * @param newDelay the new delay to set it to
	 */
	public void updateDelay(Pattern pattern,int oldDelay,int newDelay){
		HashSet<Integer> delays = patterns.get(pattern);
		if(delays!=null){
			delays.remove(oldDelay);
			delays.add(newDelay);
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
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for(Pattern pattern:patterns.keySet()){
			HashSet<Integer> delays = patterns.get(pattern);
			for(int start:delays){
				double end = pattern.length+start;
				if(start<min)min=start;
				if(end>max)max=end;
			}
		}
		double rate = 1d/parentComposition().baseSpeed;
		return new double[]{min*rate,max*rate};
	}

	@Override
	public MetaComponent<JInternalFrame> getUI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ViewComponent getViewComponent() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int hashCode(){
		return Hash.of(patterns);
	}

}
