package main;

import java.util.*;

import javax.swing.JComponent;

//Contains tracks, which will combine to be layered on top of original samples
public class TrackLayerCompound implements Track, TransientContainer<TLCParent>, TLCParent {
	private static final long serialVersionUID = 1L;

	/*
	 * Don't modify this, ever
	 * Empty time bounds
	 */
	private static final double[] emptyBounds = new double[]{Double.MAX_VALUE,Double.MIN_VALUE};
	
	public ArrayList<Track> tracks;
	public transient Composition parentComposition;
	public transient TrackLayerCompound parentTLC;
	public transient boolean parentIsComposition;
	
	@Override
	public void applyTo(MetaSamples current) {
		if(tracks.size()>0){
			MetaSamples toAdd = MetaSamples.blankSamplesFrom(current);
			for(Track track:tracks){
				double[] trackTimeBounds = track.getTimeBounds();
				if(trackTimeBounds!=emptyBounds&&trackTimeBounds[0]<current.endPos&&trackTimeBounds[1]>current.startPos){
					track.applyTo(toAdd);
				}
			}
			current.layerOnThisMeta(toAdd);
		}
	}
	
	public double[] getTimeBounds(){
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for(Track track:tracks){
			double[] trackTimeBounds = track.getTimeBounds();
			double start = trackTimeBounds[0];
			double end = trackTimeBounds[1];
			if(start<min){
				min=start;
			}
			if(end>max){
				max=end;
			}
		}
		return new double[]{min,max};
	}

	@Override
	public void initTransient(TLCParent parent) {
		// TODO Auto-generated method stub
		if(parent instanceof Composition){
			parentComposition = (Composition) parent;
			parentIsComposition = true;
		}else{
			parentTLC = (TrackLayerCompound) parent;
			parentIsComposition = false;
		}
	}
	
	public Composition parentComposition(){
		if(parentIsComposition){
			return parentComposition;
		}
		return parentTLC.parentComposition();
	}

	@Override
	public MetaComponent<? extends JComponent> getUI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MetaComponent<? extends JComponent> getViewComponent() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int hashCode(){
		int result = 4441;
		for(Track track:tracks){
			result = 262147*result+track.hashCode();
		}
		return result;
	}

}
