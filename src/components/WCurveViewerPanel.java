package components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Collections;
import javax.swing.JPanel;
import main.Curve;
import main.Wavelets;

//Curve viewer
public class WCurveViewerPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	public Curve trackCurve;
	
	//Constructor
	public WCurveViewerPanel(Curve curve){
		super();
		trackCurve = curve;
	}
	
	//Custom painting
	@Override
	public void paintComponent(Graphics g){
		//Get object boundaries
		//Rectangle clipBounds = g.getClipBounds();
		Dimension dimensions = getSize();
		Rectangle clipBounds = new Rectangle(0,0,dimensions.width,dimensions.height);
		//Get curve boundaries
		int size = trackCurve.getSize();
		double multiplier = 1.3;
		double leftt = 0.5-0.5*multiplier;
		double rightt = 0.5+0.5*multiplier;
		double margin = 0.01;
		double left;
		double right;
		double bottom;
		double top;
		if(size>0){
			double fl = trackCurve.getLocations().get(0)-margin;
			double fr = trackCurve.getLocations().get(size-1)+margin;
			double fb = Collections.min(trackCurve.getValues())-margin;
			double ft = Collections.max(trackCurve.getValues())+margin;
			left = Draw.lerp(fl,fr,leftt);
			right = Draw.lerp(fl,fr,rightt);
			bottom = Draw.lerp(fb,ft,leftt);
			top = Draw.lerp(fb,ft,rightt);
		}else{
			left=-1;
			right=1;
			top=-1;
			bottom=1;
		}
		//Create colour object from theme colours
		Color colOuter = Draw.colorFromArray(Wavelets.rgbaOuter);
		Color colInner = Draw.colorFromArray(Wavelets.rgbaInner);
		Color colGridLine = Draw.colorFromArray(Wavelets.rgbaGridLine);
		Color colHighlight = Draw.colorFromArray(Wavelets.rgbaHighlight);
		Color colSelect = Draw.colorFromArray(Wavelets.rgbaSelect);
		//Blank background
		g.setColor(colOuter);
		g.fillRect(0,0,clipBounds.width,clipBounds.height);
		g.setColor(colInner);
		g.fillRect(2,2,clipBounds.width-4,clipBounds.height-4);
		//Grid lines setup
		g.setColor(colGridLine);
		double gridPos;
		double gridInc;
		//Horizontal
		gridInc = Math.pow(10, Math.floor(Math.log10(top-bottom)-0.4));
		gridPos = Math.ceil(top-gridInc);
		while(gridPos>bottom){
			int mapping = (int) Math.floor((top-gridPos)/(top-bottom)*clipBounds.height);
			if(mapping>0 && mapping<clipBounds.height){
				g.fillRect(2, mapping, clipBounds.width-4, 1);
			}
			gridPos-=gridInc;
		}
		//Vertical
		gridInc = Math.pow(10, Math.floor(Math.log10(right-left)-0.4));
		gridPos = Math.floor(left+gridInc);
		while(gridPos<right){
			int mapping = (int) Math.floor((gridPos-left)/(right-left)*clipBounds.width);
			if(mapping>0 && mapping<clipBounds.width){
				g.fillRect(mapping, 2, 1, clipBounds.height-4);
			}
			gridPos+=gridInc;
		}
		if(size>0 && clipBounds.width>4){
			//Draw curve
			g.setColor(colHighlight);
			int[] points = new int[clipBounds.width-4];
			for(int i=2;i<clipBounds.width-2;i++){
				points[i-2] = (int) Math.round((trackCurve.valueAtPos(i*(right-left)/clipBounds.width+left)-top)/(bottom-top)*clipBounds.height);
			}
			for(int i=0;i<clipBounds.width-5;i++){
				g.drawLine(i+2, points[i], i+3, points[i+1]);
			}
			//Show points
			int bsize = Math.min(clipBounds.width,clipBounds.height)/(size+2);
			int lsize = Math.round(bsize*0.8f);
			int ssize = Math.round(bsize*0.4f);
			for(int i=0;i<size;i++){
				int csize;
				if(i==trackCurve.selected){
					g.setColor(colSelect);
					csize = lsize;
				}else{
					g.setColor(colHighlight);
					csize = ssize;
				}
				int cx = (int) Math.round((trackCurve.getLocations().get(i)-left)/(right-left)*(clipBounds.width-4)+2);
				int cy = (int) Math.round((trackCurve.getValues().get(i)-top)/(bottom-top)*(clipBounds.height-4)+2);
				g.drawLine(cx-csize, cy, cx+csize, cy);
				g.drawLine(cx, cy-csize, cx, cy+csize);
				g.drawOval(cx-csize, cy-csize, 2*csize+1, 2*csize+1);
			}
		}
	}
	
}
