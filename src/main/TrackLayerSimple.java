package main;

import java.util.*;
import org.python.core.*;

//Contains patterns, will layer directly on top of original samples
public class TrackLayerSimple implements Track, TransientContainer<TrackLayerCompound> {
	private static final long serialVersionUID = 1L;

	public ArrayList<Pattern> patterns;
	public ArrayList<Integer> delays;
	public transient ArrayList<Voice> voices;
	
	public TrackLayerSimple(TrackLayerCompound parent){
		patterns = new ArrayList<Pattern>();
		initTransient(parent);
	}
	
	@Override
	public void applyTo(MetaSamples current) {
		//Setup
		int numPatterns = patterns.size();
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
					pattern.synthesizer.spawnVoices(toSend, this, current.sampleRate);
				}
			}
		}
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

	@Override
	public void initTransient(TrackLayerCompound parent) {
		voices = new ArrayList<Voice>();
	}
	
	public void addVoice(Pattern pattern,PyObject[] args,String[] keywords){
		PyVoiceFactory pvf = pattern.getPvf();
		Voice toAdd = pvf.create(args, keywords);
		voices.add(toAdd);
	}
	
	public void addPattern(Pattern pattern,int delay){
		patterns.add(pattern);
		delays.add(delay);
	}
	
	

}
