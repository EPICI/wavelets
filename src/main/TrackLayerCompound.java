package main;

import java.util.*;

//Contains tracks, which will combine to be layered on top of original samples
public class TrackLayerCompound implements Track {
	private static final long serialVersionUID = 1L;

	public ArrayList<Track> tracks;
	
	@Override
	public void applyTo(MetaSamples current) {
		if(tracks.size()>0){
			MetaSamples toAdd = MetaSamples.blankSamplesFrom(current);
			for(Track track:tracks){
				track.applyTo(toAdd);
			}
			current.layerOnThisLazy(toAdd);
		}
	}

}
