package util.ui;

import java.awt.*;
import java.awt.image.*;

import core.ColorScheme;

import java.awt.geom.*;

/**
 * Utility class containing methods to help with drawing
 * common objects
 * 
 * @author EPICI
 * @version 1.0
 */
public class Draw {
	
	/**
	 * Radius in pixels of the rounded corners in buttons
	 */
	public static final int BUTTON_CORNER_RADIUS = 4;

	//Disallow invoking constructor
	private Draw(){}
	
	/**
	 * Draw text and/or image together
	 * <br>
	 * Some use cases may intentionally make the bounding box too small,
	 * so we don't force a clip. If you want it to clip the edges, pass
	 * to this method a copy of the {@link Graphics2D} object with the
	 * desired clip rectangle.
	 * 
	 * @param graphics graphics object to draw with
	 * @param x top left coordinate x
	 * @param y top left coordinate y
	 * @param width total width
	 * @param height total height
	 * @param text text to draw, if any
	 * @param icon image to draw, if any
	 * @param font font override
	 * @param textFill the color of the text, if there is text
	 * @param imageTint optionally change the color of the image
	 * @param xalign x alignment for text/image, 0.0=left, 0.5=center, 1.0=right
	 * @param yalign y alignment for text/image, 0.0=top, 0.5=center, 1.0=bottom
	 * @param imageScale control of text/image scaling, 0.0=original size, 0.5=expand halfway (geometric mean), 1.0=fill space
	 * @param imageDirection bitmask: 1 bit will make the image draw to right of text instead of left, 2 bit will make the image draw below text instead of above
	 */
	public static void drawTextImage(Graphics2D graphics,int x,int y,int width,int height,String text,BufferedImage icon,Font font,Color textFill,Color imageTint,double xalign,double yalign,double imageScale,int imageDirection){
		boolean drawText = text!=null&&text.length()!=0;
		boolean drawImage = icon!=null;
		if(drawText){
			if(font==null)font=graphics.getFont();
			if(textFill==null)textFill=imageTint==null?Color.BLACK:imageTint;
			FontMetrics fontMetrics = graphics.getFontMetrics(font);
			String[] textParts = text.split("\\r\\n|\\n|\\r");
			int textCount = textParts.length, textWidth = 0, lineHeight = fontMetrics.getHeight(), textHeight = lineHeight*textCount;
			int[] textWidths = new int[textCount];
			for(int i=0;i<textCount;i++){
				String part = textParts[i];
				int partWidth = fontMetrics.stringWidth(part);
				textWidths[i] = partWidth;
				if(partWidth>textWidth)textWidth=partWidth;
			}
			if(drawImage){// Text and image
				int imageWidth = icon.getWidth(), imageHeight = icon.getHeight();
				/*
				 * We will draw the image either to the left of the text or above it
				 * If there is enough space, use horizontal
				 * If it can't fit horizontally but can fit vertically, use vertical
				 * If there is not enough space for either, pick based on width and height ratios
				 * If the image is taller than the text, it will be horizontal anyways
				 * If the image is wider than the text, it will be vertical anyways
				 * If the image is both wider and taller than the text, neither override takes effect
				 */
				int htotalWidth = imageWidth+textWidth, htotalHeight = Math.max(imageHeight,textHeight);
				int vtotalWidth = Math.max(imageWidth,textWidth), vtotalHeight = imageHeight+textHeight;
				boolean vertical = htotalWidth>width || vtotalHeight>height && htotalWidth*height > vtotalHeight*width;
				boolean wider = imageWidth>=textWidth, taller = imageHeight>=textHeight;
				if(wider^taller)vertical=wider;
				if(imageTint!=null&&!imageTint.equals(Color.WHITE)){
					LookupOp lut = lutMultiply(imageTint);
					icon = lut.filter(icon, null);
				}
				if(vertical){// Draw image above/below text
					double scale = Math.pow(Math.min((double)width/vtotalWidth,(double)height/vtotalHeight),imageScale);
					font=font.deriveFont((float)(scale*font.getSize()));
					double dimageWidth = imageWidth*scale;
					double dimageHeight = imageHeight*scale;
					double dlineHeight = lineHeight*scale;
					double dtotalHeight = vtotalHeight*scale;
					if((imageDirection&2)!=0){// Draw image above text
						double xoffset = x+xalign*(width-dimageWidth);
						double yoffset = y+yalign*(height-dtotalHeight);
						graphics.drawImage(icon, (int)xoffset, (int)yoffset, (int)(xoffset+dimageWidth), (int)(yoffset+=dimageHeight), 0, 0, imageWidth, imageHeight, null);
						yoffset += fontMetrics.getAscent()*scale;
						graphics.setColor(textFill);
						graphics.setFont(font);
						for(int i=0;i<textCount;i++){
							graphics.drawString(textParts[i], (int)(x+xalign*(width-textWidths[i]*scale)), (int)yoffset);
							yoffset+=dlineHeight;
						}
					}else{// Draw image below text
						// TODO
					}
				}else{// Draw image to left/right of text
					double scale = Math.pow(Math.min((double)width/htotalWidth,(double)height/htotalHeight),imageScale);
					font=font.deriveFont((float)(scale*font.getSize()));
					double dimageWidth = imageWidth*scale;
					double dimageHeight = imageHeight*scale;
					double dlineHeight = lineHeight*scale;
					double dtextWidth = textWidth*scale;
					double dtextHeight = textHeight*scale;
					double dtotalWidth = htotalWidth*scale;
					if((imageDirection&1)!=0){// Draw image to left of text
						double xoffset = x+xalign*(width-dtotalWidth);
						double yoffset = y+yalign*(height-dimageHeight);
						graphics.drawImage(icon, (int)xoffset, (int)yoffset, (int)(xoffset+dimageWidth), (int)(yoffset+dimageHeight), 0, 0, imageWidth, imageHeight, null);
						xoffset += dimageWidth;
						yoffset = fontMetrics.getAscent()*scale+dtextHeight*yalign;
						graphics.setColor(textFill);
						graphics.setFont(font);
						for(int i=0;i<textCount;i++){
							graphics.drawString(textParts[i], (int)(xoffset+xalign*(dtextWidth-textWidths[i]*scale)), (int)yoffset);
							yoffset+=dlineHeight;
						}
					}else{// Draw image to right of text
						// TODO
					}
				}
			}else{// Text only
				double scale = Math.pow(Math.min((double)width/textWidth,(double)height/textHeight),imageScale);
				font=font.deriveFont((float)(scale*font.getSize()));
				double dlineHeight = lineHeight*scale;
				double dtextHeight = textHeight*scale;
				double yoffset = y+fontMetrics.getAscent()*scale+(height-dtextHeight)*yalign;
				graphics.setColor(textFill);
				graphics.setFont(font);
				for(int i=0;i<textCount;i++){
					graphics.drawString(textParts[i], (int)(x+xalign*(width-textWidths[i]*scale)), (int)yoffset);
					yoffset+=dlineHeight;
				}
			}
		}else if(drawImage){// Image only
			int imageWidth = icon.getWidth(), imageHeight = icon.getHeight();
			double scale = Math.pow(Math.min((double)width/imageWidth, (double)height/imageHeight), imageScale);
			double dimageWidth = imageWidth*scale;
			double dimageHeight = imageHeight*scale;
			if(imageTint!=null&&!imageTint.equals(Color.WHITE)){
				LookupOp lut = lutMultiply(imageTint);
				icon = lut.filter(icon, null);
			}
			double xoffset = x+(width-dimageWidth)*xalign;
			double yoffset = y+(height-dimageHeight)*yalign;
			graphics.drawImage(icon, (int)xoffset, (int)yoffset, (int)(xoffset+dimageWidth), (int)(yoffset+dimageHeight), 0, 0, imageWidth, imageHeight, null);
		}
	}
	
	/**
	 * Draw a Pivot-styled button
	 * 
	 * @param graphics graphics object to draw with
	 * @param x top left coordinate x
	 * @param y top left coordinate y
	 * @param width total width
	 * @param height total height
	 * @param text text to draw, if any
	 * @param icon image to draw, if any
	 * @param font font override
	 * @param outline color of outline
	 * @param innerFillBase base color for inside
	 * @param innerFillSolid flag to not use gradient for fill, used for pressed/disabled buttons
	 * @param textFill the color of the text, if there is text
	 * @param imageTint optionally change the color of the image
	 * @param xalign x alignment for text/image, 0.0=left, 0.5=center, 1.0=right
	 * @param yalign y alignment for text/image, 0.0=top, 0.5=center, 1.0=bottom
	 * @param imageScale control of text/image scaling, 0.0=original size, 0.5=expand halfway (geometric mean), 1.0=fill space
	 * @param imageDirection bitmask: 1 bit will make the image draw to right of text instead of left, 2 bit will make the image draw below text instead of above
	 */
	public static void drawButton(Graphics2D graphics,int x,int y,int width,int height,String text,BufferedImage icon,Font font,Color outline,Color innerFillBase,boolean innerFillSolid,Color textFill,Color imageTint,double xalign,double yalign,double imageScale,int imageDirection){
		// Get new translation and clip
		graphics = (Graphics2D) graphics.create(x, y, width, height);
		// Get button shape
		RoundRectangle2D.Double boundShape = new RoundRectangle2D.Double(0.5,0.5,width-1,height-1,BUTTON_CORNER_RADIUS,BUTTON_CORNER_RADIUS);
		// Inner fill
		graphics.setPaint(gradientVertical(innerFillBase,innerFillSolid?innerFillBase:ColorScheme.brighten(innerFillBase, 0.1f),height,0));
		graphics.fill(boundShape);
		// Text and image
		int awidth = width-2*BUTTON_CORNER_RADIUS, aheight = height-2*BUTTON_CORNER_RADIUS;
		drawTextImage(graphics,BUTTON_CORNER_RADIUS,BUTTON_CORNER_RADIUS,awidth,aheight,text,icon,font,textFill,imageTint,xalign,yalign,imageScale,imageDirection);
		// Outline
		graphics.setPaint(outline);
		graphics.setStroke(new BasicStroke(1));
		graphics.draw(boundShape);
	}
	
	/**
	 * Horizontal linear gradient, <i>first</i> color at <i>first</i> x coordinate,
	 * <i>second</i> color at <i>second</i> x coordinate
	 * 
	 * @param colFirst
	 * @param colSecond
	 * @param xfirst
	 * @param xsecond
	 * @return
	 */
	public static GradientPaint gradientHorizontal(Color colFirst,Color colSecond,float xfirst,float xsecond){
		return new GradientPaint(xfirst,0,colFirst,xsecond,0,colSecond);
	}
	
	/**
	 * Vertical linear gradient, <i>first</i> color at <i>first</i> y coordinate,
	 * <i>second</i> color at <i>second</i> y coordinate
	 * 
	 * @param colFirst
	 * @param colSecond
	 * @param yfirst
	 * @param ysecond
	 * @return
	 */
	public static GradientPaint gradientVertical(Color colFirst,Color colSecond,float yfirst,float ysecond){
		return new GradientPaint(0,yfirst,colFirst,0,ysecond,colSecond);
	}
	
	/**
	 * Lookup table for "multiply"
	 * 
	 * @param color color to multiply with
	 * @return LookupOp
	 */
	public static LookupOp lutMultiply(Color color){
		int tintrgb = color.getRGB(), tintr = tintrgb>>16&0xff, tintg = tintrgb>>8&0xff, tintb = tintrgb&0xff;
		byte[] subr = new byte[256], subg = new byte[256], subb = new byte[256];
		for(int i=0;i<256;i++){
			subr[i]=(byte)(tintr*i/255);
			subg[i]=(byte)(tintg*i/255);
			subb[i]=(byte)(tintb*i/255);
		}
		return new LookupOp(new ByteLookupTable(0,new byte[][]{subr,subg,subb}),null);
	}
	
}
