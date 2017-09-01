package core;

import java.util.*;
import javax.swing.*;

import util.Hash;

/**
 * Contains tracks
 * <br>
 * A new blank {@link MetaSamples} object is created, all tracks process that,
 * and that object is finally layered onto the given one
 * 
 * @author EPICI
 * @version 1.0
 */
public class TrackLayerCompound implements Track, TransientContainer<TLCParent>, TLCParent {
	private static final long serialVersionUID = 1L;
	
	/**
	 * All contained tracks
	 */
	public ArrayList<Track> tracks;
	/**
	 * If this is the root {@link TrackLayerCompound}, the parent composition
	 */
	protected transient Composition parentComposition;
	/**
	 * If this is not the root {@link TrackLayerCompound}, the parent {@link TrackLayerCompound}
	 */
	protected transient TrackLayerCompound parentTLC;
	/**
	 * If it is the root {@link TrackLayerCompound}, and therefore
	 * its parent is the composition
	 */
	protected transient boolean parentIsComposition;
	/**
	 * The name, if it is named
	 */
	protected String name;
	
	public TrackLayerCompound(TLCParent parent){
		initTransient(parent);
	}
	
	@Override
	public void applyTo(MetaSamples current) {
		if(tracks.size()>0){
			MetaSamples toAdd = MetaSamples.blankSamplesFrom(current);
			for(Track track:tracks){
				double[] trackTimeBounds = track.getTimeBounds();
				if(trackTimeBounds[0]!=Double.MAX_VALUE&&trackTimeBounds[1]!=Double.MIN_VALUE&&trackTimeBounds[0]<current.endPos&&trackTimeBounds[1]>current.startPos){
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
		parentIsComposition=parent instanceof Composition;
		if(parentIsComposition){
			parentComposition = (Composition) parent;
		}else{
			parentTLC = (TrackLayerCompound) parent;
		}
	}
	
	public Composition parentComposition(){
		if(parentIsComposition){
			return parentComposition;
		}
		return parentTLC.parentComposition();
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
		return Hash.of(tracks);
	}
	
	public String getName(){
		if(name==null || name.length()==0)return parentIsComposition?"Root Track":Track.defaultName("Layered Track", this);
		return name;
	}
	
	public void setName(String newName){
		name=newName;
	}

}
