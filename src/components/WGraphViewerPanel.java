package components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Arrays;
import javax.swing.JPanel;
import main.Wavelets;

//Graph viewer
public class WGraphViewerPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	//Graph data
	public double[] graphData = new double[0];
	public double increment = 1;
	public double margin = 0.02;
	
	//Constructor
	public WGraphViewerPanel(){
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
		Color colHighlight = Draw.colorFromArray(Wavelets.rgbaHighlight);
		//Blank background
		g.setColor(colOuter);
		g.fillRect(0,0,clipBounds.width,clipBounds.height);
		g.setColor(colInner);
		g.fillRect(2,2,xcap,ycap);
		//Safety checks
		boolean isEmpty = graphData.length==0;
		//Setup for drawing
		double lowerBound;
		double upperBound;
		if(isEmpty){
			lowerBound = -1;
			upperBound = 1;
		}else{
			lowerBound = Arrays.stream(graphData).min().getAsDouble()-margin;
			upperBound = Arrays.stream(graphData).max().getAsDouble()+margin;
		}
		//Grid lines setup
		g.setColor(colGridLine);
		double left = 0;
		double right = ycap*increment;
		double gridPos;
		double gridInc;
		//Horizontal
		gridInc = Math.pow(10, Math.floor(Math.log10(upperBound-lowerBound)-0.4));
		gridPos = Math.ceil(upperBound-gridInc);
		while(gridPos>lowerBound){
			int mapping = (int) Math.floor((upperBound-gridPos)/(upperBound-lowerBound)*clipBounds.height);
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
		if(!isEmpty){
			//Draw graph
			double position = 0;
			int intPosition = 0;
			int total = Math.min(xcap, graphData.length);
			int[] points = new int[total];
			for(int i=0;i<total;i++){
				points[i] = Draw.mapToRound(lowerBound,upperBound,2,ycap,graphData[intPosition]);
				position += increment;
				intPosition = (int) Math.round(position);
			}
			g.setColor(colHighlight);
			for(int i=1;i<total;i++){
				g.drawLine(i+1, points[i-1], i+2, points[i]);
			}
		}
	}
}
