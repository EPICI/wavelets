package components;

import javax.swing.*;
import main.*;
import java.awt.*;
import java.awt.event.*;

public class WLayerQuickDrawPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	//Constant
	public static final double SEMITONE = Math.pow(2d, 1d/12d);
	public static final double LOG_SEMITONE = Math.log(SEMITONE);
	//In order: layer, selected clip, light grid line, dark grid line
	public static float[] alphas = {0.3f,0.6f,0.4f,0.8f};
	
	public Layer targetLayer;
	public double left;
	public double right;
	public double bottom;
	public double top;
	public double targetLeft;
	public double targetRight;
	public double mapMultiplier;
	public Clip activeClip;
	public Clip copyClip;
	public Layer activeLayer;
	public ClipBehaviour clipBehaviour;
	public boolean isDrawing;
	public boolean mouseDrawing;
	public boolean movingEnd;
	public double startx;
	public double starty;
	public double endx;
	public double endy;
	public double currentx;
	public double currenty;
	public long lastPlayed;
	public long msDelay;
	
	public static interface ClipBehaviour{
		//Two coordinates define initial and current relative mouse positions
		public void updateClip(Clip toUpdate,double x1,double y1,double x2,double y2,double mapMult,double mapOffset);
	}
	
	public static class CbFlatTone implements ClipBehaviour{
		
		String control1;

		public CbFlatTone(String input1){
			control1 = input1;
		}
		
		@Override
		public void updateClip(Clip toUpdate, double x1, double y1, double x2, double y2, double mapMult, double mapOffset) {
			toUpdate.startTime = x1*mapMult+mapOffset;
			toUpdate.endTime = x2*mapMult+mapOffset;
			toUpdate.updateLength();
			toUpdate.inputs.put(control1, y2);
			toUpdate.inputsRegistered = true;
			toUpdate.refreshInputs();
		}
		
	}
	
	public static class CbLinearTone implements ClipBehaviour{
		
		String control1;
		String control2;

		public CbLinearTone(String input1,String input2){
			control1 = input1;
			control2 = input2;
		}
		
		@Override
		public void updateClip(Clip toUpdate, double x1, double y1, double x2, double y2, double mapMult, double mapOffset) {
			toUpdate.startTime = x1*mapMult+mapOffset;
			toUpdate.endTime = x2*mapMult+mapOffset;
			toUpdate.updateLength();
			toUpdate.inputs.put(control1, y1);
			toUpdate.inputs.put(control2, y2);
			toUpdate.inputsRegistered = true;
			toUpdate.refreshInputs();
		}
		
	}
	
	public WLayerQuickDrawPanel(Layer target,double newRight,double newBottom,double newTop,double targetStart,double targetEnd,ClipBehaviour behaviour,Clip copyFrom){
		super();
		targetLayer = target;
		left = 0d;
		right = newRight;
		bottom = newBottom;
		top = newTop;
		clipBehaviour = behaviour;
		copyClip = copyFrom;
		targetLeft = targetStart;
		targetRight = targetEnd;
		mapMultiplier = (targetEnd-targetStart)/right;
		activeLayer = new Layer();
		activeLayer.parentComposition = target.parentComposition;
		activeLayer.initTransient();
		isDrawing = false;
		lastPlayed = System.currentTimeMillis();
		msDelay = (long) ((targetRight-targetLeft)*1000d);
		addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if(!isDrawing){
					//Get object boundaries
					//Rectangle clipBounds = g.getClipBounds();
					Dimension dimensions = getSize();
					Rectangle clipBounds = new Rectangle(0,0,dimensions.width,dimensions.height);
					currentx = Draw.mapToRound(2, clipBounds.width-2, left, right, e.getX());
					currenty = Draw.mapToRound(2, clipBounds.height-2, top, bottom, e.getY());
					startDraw();
					mouseDrawing = true;
					movingEnd = true;
				}
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				endDraw();
				mouseDrawing = false;
			}
			
		});
		addMouseMotionListener(new MouseMotionListener(){

			@Override
			public void mouseDragged(MouseEvent e) {
				if(isDrawing){
					//Get object boundaries
					//Rectangle clipBounds = g.getClipBounds();
					Dimension dimensions = getSize();
					Rectangle clipBounds = new Rectangle(0,0,dimensions.width,dimensions.height);
					currentx = Draw.mapToRound(2, clipBounds.width-2, left, right, e.getX());
					currenty = Draw.mapToRound(2, clipBounds.height-2, top, bottom, e.getY());
					if(movingEnd){
						replaceEnd();
					}else{
						replaceStart();
					}
				}
				restartPlayer();
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				//Get object boundaries
				//Rectangle clipBounds = g.getClipBounds();
				Dimension dimensions = getSize();
				Rectangle clipBounds = new Rectangle(0,0,dimensions.width,dimensions.height);
				currentx = Draw.mapToRound(2, clipBounds.width-2, left, right, e.getX());
				currenty = Draw.mapToRound(2, clipBounds.height-2, top, bottom, e.getY());
				restartPlayer();
			}
			
		});
		addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				switch(keyCode){
				case KeyEvent.VK_A:{
					startDraw();
					mouseDrawing = false;
					break;
				}case KeyEvent.VK_S:{
					if(isDrawing){
						if(mouseDrawing){
							movingEnd = !movingEnd;
						}else{
							replaceStart();
						}
					}
					break;
				}case KeyEvent.VK_D:{
					if(isDrawing && !mouseDrawing){
						replaceEnd();
					}
					break;
				}case KeyEvent.VK_F:{
					if(isDrawing && !mouseDrawing){
						endDraw();
					}
					break;
				}
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	public void startDraw(){
		isDrawing = true;
		startx = currentx;
		starty = currenty;
		endx = currentx;
		endy = currenty;
		activeClip = new Clip();
		activeClip.parentLayer = activeLayer;
		activeClip.nodesName = copyClip.nodesName;
		activeClip.initTransient();
		activeClip.copyFrom(copyClip);
		clipBehaviour.updateClip(activeClip, startx, starty, endx, endy, mapMultiplier, targetLeft);
		activeLayer.addClip(activeClip);
		repaint();
	}
	
	public void endDraw(){
		isDrawing = false;
	}
	
	public void replaceStart(){
		if(currentx!=startx || currenty!=starty){
			startx = currentx;
			starty = currenty;
			clipBehaviour.updateClip(activeClip, startx, starty, endx, endy, mapMultiplier, targetLeft);
			repaint();
		}
	}
	
	public void replaceEnd(){
		if(currentx!=endx || currenty!=endy){
			endx = currentx;
			endy = currenty;
			clipBehaviour.updateClip(activeClip, startx, starty, endx, endy, mapMultiplier, targetLeft);
			repaint();
		}
	}
	
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
		Color colGridLine = Draw.colorFromArray(Wavelets.rgbaGridLine,alphas[2]);
		Color colGridLineDark = Draw.colorFromArray(Wavelets.rgbaGridLine,alphas[3]);
		Color colHighlight = Draw.colorFromArray(Wavelets.rgbaHighlight,alphas[0]);
		Color colActive = Draw.colorFromArray(Wavelets.rgbaActive,alphas[1]);
		int samples = activeLayer.parentComposition.samplesPerSecond;
		double lsamples = targetLeft*samples;
		double rsamples = targetRight*samples;
		//Blank background
		g.setColor(colOuter);
		g.fillRect(0,0,clipBounds.width,clipBounds.height);
		g.setColor(colInner);
		g.fillRect(2,2,xcap,ycap);
		//Grid lines
		drawGrid(g,clipBounds,colGridLine,1d,1d);
		drawGrid(g,clipBounds,colGridLineDark,4d,12d);
		//Draw
		for(int i=0;i<activeLayer.clipCount;i++){
			if(i==activeLayer.selectedClip){
				g.setColor(colActive);
			}else{
				g.setColor(colHighlight);
			}
			Clip clip = activeLayer.clips.get(i);
			if(clip.length>0&&clip.inputsRegistered){
				double[] freqs = clip.getFreq();
				int position = Draw.mapToRound(targetLeft, targetRight, 2, xcap, clip.startTime);
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
	
	public void merge(){
		for(Clip current:activeLayer.clips){
			targetLayer.addClip(current);
		}
		activeLayer.destroyLayer();
		activeLayer = null;
	}
	
	public void cancel(){
		activeLayer.destroy();
		activeLayer = null;
	}
	
	public void restartPlayer(){
		double[] timeBounds = activeLayer.getTimeBounds();
		double difference = timeBounds[1]-timeBounds[0];
		if(difference>0){
			msDelay = (long) ((difference)*1000d)+50l;
			long currentTime = System.currentTimeMillis();
			if(currentTime-lastPlayed>msDelay){
				lastPlayed = currentTime;
				double[] doubleArray = activeLayer.getAudio();
				short[] shortArray = WaveUtils.quickShort(doubleArray);
				Wavelets.mainPlayer.playSound(shortArray);
			}
		}
	}
	
	public void drawGrid(Graphics g,Rectangle clipBounds,Color color,double horizInc, double vertInc){
		int xcap = clipBounds.width-4;//Efficiency
		int ycap = clipBounds.height-4;//Same here
		//Setup
		g.setColor(color);
		double gridPos;
		//Horizontal
		gridPos = top;
		while(gridPos>bottom){
			if(WaveUtils.isNear(gridPos%vertInc, 0)){
				int mapping = (int) Math.floor((top-gridPos)/(top-bottom)*ycap+2);
				if(mapping>2 && mapping<clipBounds.height-2){
					g.fillRect(2, mapping, xcap, 1);
				}
			}
			gridPos-=1d;
		}
		//Vertical
		gridPos = left;
		while(gridPos<right){
			if(WaveUtils.isNear(gridPos%horizInc, 0)){
				int mapping = (int) Math.floor((gridPos-left)/(right-left)*xcap+2);
				if(mapping>2 && mapping<clipBounds.width-2){
					g.fillRect(mapping, 2, 1, ycap);
				}
			}
			gridPos+=1d;
		}
	}
	
	public static void initPopup(Layer target,Clip copyFrom,ClipBehaviour behaviour,double[] inputs){
		Wavelets.popupPanel.removeAll();
		WLayerQuickDrawPanel drawPanel = new WLayerQuickDrawPanel(target, inputs[0], inputs[1], inputs[2], inputs[3], inputs[4], behaviour, copyFrom);
		JButton confirmButton = new JButton("Apply");
		JButton closeButton = new JButton("Close");
		confirmButton.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent arg0) {
				drawPanel.merge();
				Wavelets.popupFrame.dispose();
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		closeButton.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent arg0) {
				drawPanel.cancel();
				Wavelets.popupFrame.dispose();
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		GridBagConstraints constraint = new GridBagConstraints();
		constraint.gridx=0;
		constraint.gridy=0;
		constraint.gridwidth=2;
		constraint.weightx=1;
		constraint.weighty=1;
		constraint.fill = GridBagConstraints.BOTH;
		Wavelets.popupPanel.add(drawPanel, constraint);
		constraint.weighty=0.1;
		constraint.gridwidth=1;
		constraint.gridy=1;
		constraint.fill = GridBagConstraints.NONE;
		Wavelets.popupPanel.add(confirmButton, constraint);
		constraint.gridx=1;
		Wavelets.popupPanel.add(closeButton, constraint);
		Wavelets.updateDisplay(Wavelets.popupFrame);
		drawPanel.requestFocusInWindow();
	}
}
