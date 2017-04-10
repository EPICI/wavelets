package main;

import java.util.*;
import javax.swing.*;
import org.python.core.*;
import utils.*;

/**
 * Contains patterns
 * 
 * @author EPICI
 * @version 1.0
 */
public class TrackLayerSimple implements Track, TransientContainer<TrackLayerCompound> {
	private static final long serialVersionUID = 1L;

	/**
	 * All the patterns
	 */
	protected ArrayList<Pattern> patterns;
	/**
	 * For each pattern, the delay in measures
	 */
	protected ArrayList<Integer> delays;
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
		patterns = new ArrayList<Pattern>();
		delays = new ArrayList<Integer>();
		initTransient(parent);
	}
	
	@Override
	public void applyTo(MetaSamples current) {
		//Setup
		int numPatterns = patterns.size();
		if(numPatterns>0){
			//Add new voices if necessary
			for(int i=0;i<numPatterns;i++){
				Pattern pattern = patterns.get(i);
				int delay = delays.get(i);
				double start = delay*current.speedMult;
				double end = (delay+pattern.length)*current.speedMult;
				if(start<=current.endPos&&end>=current.startPos){
					double multiplier = current.speedMult/pattern.divisions;
					ArrayList<double[]> toSendList = new ArrayList<double[]>();
					for(int[] clip:pattern.clips){
						double clipStart = clip[0]*multiplier+start;
						double clipOffset = clip[1]*multiplier+start;
						double clipEnd = clipStart+clipOffset;
						if(clipStart<=current.endPos&&clipEnd>=current.startPos){
							double voiceDelay = clipStart-current.startPos;
							toSendList.add(new double[]{voiceDelay,clipOffset,clip[2]});
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
		VoiceFactory voiceFactory = pattern.getVoiceFactory();
		Voice toAdd = voiceFactory.create(args, keywords);
		voices.add(toAdd);
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
		patterns.add(pattern);
		delays.add(delay);
	}
	
	/**
	 * Replaces a single pattern
	 * 
	 * @param index the index of the pattern to replace
	 * @param pattern the new pattern to replace with
	 */
	public void replacePattern(int index,Pattern pattern){
		patterns.set(index, pattern);
	}
	
	/**
	 * Updates a single delay value
	 * 
	 * @param index the index of the pattern to change the delay of
	 * @param delay the new delay
	 */
	public void updateDelay(int index,int delay){
		delays.set(index, delay);
	}
	
	/**
	 * Get a pattern with its delay
	 * 
	 * @param index the index to fetch
	 * @return the pattern with delay
	 */
	public Any.O2<Pattern, Integer> getPattern(int index){
		return new Any.O2<>(patterns.get(index), delays.get(index));
	}
	
	/**
	 * <b>Very dangerous, it's easy to mess up the lists, the method
	 * is only provided for the few algorithms that may make good
	 * use of it</b>
	 * 
	 * @return a pair containing the patterns list and delays list
	 */
	public Any.O2<ArrayList<Pattern>, ArrayList<Integer>> getPatterns(){
		return new Any.O2<>(patterns,delays);
	}
	
	public double[] getTimeBounds(){
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		int total = patterns.size();
		for(int i=0;i<total;i++){
			double start = delays.get(i);
			double end = patterns.get(i).length+start;
			if(start<min){
				min=start;
			}
			if(end>max){
				max=end;
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
	public org.apache.pivot.wtk.Component getViewComponent() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int hashCode(){
		return 133121*patterns.hashCode()+delays.hashCode();
	}

}
