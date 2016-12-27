package components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JPanel;
import main.*;

//Show all layers
public class WLayersViewerPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	//Constant
	public static final double SEMITONE = Math.pow(2d, 1d/12d);
	public static final double LOG_SEMITONE = Math.log(SEMITONE);
	
	//In order: all other layers, current layer, selected clip
	public static float[] alphas = {0.1f,0.2f,0.3f};
	//Cached boundaries
	public static double[] cacheBounds = {0,1,-2,2};
	public static int cacheHash = 1;
	
	public WLayersViewerPanel(){
		super();
	}
	
	@Override
	public void paintComponent(Graphics g){
		//Get object boundaries
		//Rectangle clipBounds = g.getClipBounds();
		Dimension dimensions = getSize();
		Rectangle clipBounds = new Rectangle(0,0,dimensions.width,dimensions.height);
		int xcap = clipBounds.width-4;//Efficiency
		int ycap = clipBounds.height-4;//Same here
		//Create colour object from theme colours
		Color colOuter = Draw.colorFromArray(Wavelets.rgbaOuter);
		Color colInner = Draw.colorFromArray(Wavelets.rgbaInner);
		Color colGridLine = Draw.colorFromArray(Wavelets.rgbaGridLine);
		Color colHighlight = Draw.colorFromArray(Wavelets.rgbaHighlight,alphas[0]);
		Color colActive = Draw.colorFromArray(Wavelets.rgbaActive,alphas[1]);
		Color colSelect = Draw.colorFromArray(Wavelets.rgbaSelect,alphas[2]);
		//Boundaries
		double left = 0;
		double right = 1;
		double bottom = 0;
		double top = 0;
		int newHash = Wavelets.composition.layers.hashCode();
		if(newHash==cacheHash){
			left = cacheBounds[0];
			right = cacheBounds[1];
			bottom = cacheBounds[2];
			top = cacheBounds[3];
		}else{
			for(Layer layer:Wavelets.composition.layers.values()){
				double[] timeBounds = layer.getTimeBounds();
				double[] freqBounds = layer.getFreqBounds();
				double[] pitchBounds = new double[]{Math.log(freqBounds[0]/440d)/LOG_SEMITONE,Math.log(freqBounds[1]/440d)/LOG_SEMITONE};
				if(timeBounds[0]<left){
					left=timeBounds[0];
				}
				if(timeBounds[1]>right){
					right=timeBounds[1];
				}
				if(pitchBounds[0]<bottom){
					bottom=pitchBounds[0];
				}
				if(pitchBounds[1]>top){
					top=pitchBounds[1];
				}
			}
			bottom-=2;
			top+=2;
			cacheHash = newHash;
			cacheBounds = new double[]{left,right,bottom,top};
		}
		int samples = Wavelets.composition.samplesPerSecond;
		double lsamples = left*samples;
		double rsamples = right*samples;
		//Blank background
		g.setColor(colOuter);
		g.fillRect(0,0,clipBounds.width,clipBounds.height);
		g.setColor(colInner);
		g.fillRect(2,2,xcap,ycap);
		//Grid lines setup
		g.setColor(colGridLine);
		double gridPos;
		double gridInc;
		//Horizontal
		gridInc = 1d;
		gridPos = Math.ceil(top-gridInc);
		while(gridPos>bottom){
			int mapping = (int) Math.floor((top-gridPos)/(top-bottom)*clipBounds.height);
			if(mapping>0 && mapping<clipBounds.height){
				g.fillRect(2, mapping, xcap, 1);
			}
			gridPos-=gridInc;
		}
		//Vertical
		gridInc = Math.pow(10, Math.floor(Math.log10(right-left)-0.6));
		gridPos = Math.floor(left+gridInc);
		while(gridPos<right){
			int mapping = (int) Math.floor((gridPos-left)/(right-left)*clipBounds.width);
			if(mapping>0 && mapping<clipBounds.width){
				g.fillRect(mapping, 2, 1, ycap);
			}
			gridPos+=gridInc;
		}
		//Draw
		for(String layerName:Wavelets.composition.layers.keySet()){
			boolean isSelectedLayer = layerName.equals(Wavelets.composition.layerSelection);
			if(!isSelectedLayer){
				g.setColor(colHighlight);
			}
			Layer layer = Wavelets.composition.layers.get(layerName);
			for(int i=0;i<layer.clipCount;i++){
				if(isSelectedLayer){
					if(i==layer.selectedClip){
						g.setColor(colSelect);
					}else{
						g.setColor(colActive);
					}
				}
				Clip clip = layer.clips.get(i);
				if(clip.length>0){
					double[] freqs = clip.getFreq();
					int position = Draw.mapToRound(left, right, 2, xcap, clip.startTime);
					int mapIndex = Draw.mapToRound(2, xcap, lsamples, rsamples, position);
					int offset = mapIndex;
					mapIndex = 0;
					double freq = freqs[mapIndex];
					double pitch = Math.log(freq/440d)/LOG_SEMITONE;
					int pos1 = Draw.mapToRound(top,bottom,2,ycap,pitch-0.5d);
					int pos2 = Draw.mapToRound(top,bottom,2,ycap,pitch+0.5d);
					g.drawLine(position, pos1-1, position, pos2+1);
					while(mapIndex<freqs.length){
						freq = freqs[mapIndex];
						pitch = Math.log(freq/440d)/LOG_SEMITONE;
						pos1 = Draw.mapToRound(top,bottom,2,ycap,pitch-0.5d);
						pos2 = Draw.mapToRound(top,bottom,2,ycap,pitch+0.5d);
						g.drawLine(position, pos1, position, pos2);
						g.fillRect(position, pos1, 1, 1);
						g.fillRect(position, pos2, 1, 1);
						position++;
						mapIndex = Draw.mapToRound(2, xcap, lsamples, rsamples, position)-offset;
					}
					g.drawLine(position, pos1-1, position, pos2+1);
				}
			}
		}
	}
}
