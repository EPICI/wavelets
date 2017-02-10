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
		
	}

	@Override
	public MetaComponent<? extends JComponent> getUI() {
		// TODO Auto-generated method stub
		return null;
	}

}
