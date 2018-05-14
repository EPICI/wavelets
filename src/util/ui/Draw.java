package util.ui;

import java.awt.*;
import java.awt.image.*;

import core.ColorScheme;
import util.math.Floats;

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
	
	/**
	 * Utility class containing methods for approximating
	 * circles and arcs with cubic bezier curves (4 control points)
	 * <br>
	 * The 2 outer control points are the endpoints, and the 2
	 * inner control points are from a ray tangent to the circle
	 * coming from their respective parent endpoints
	 * <br>
	 * The distance along that ray is computed as
	 * <i>4/3 tan(angle/4)</i>
	 * using the angle of the arc, or
	 * <i>4/3 tan(pi/2n)</i>
	 * if the arc is 1/n of a circle
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public static class BezierArc{
		
		// Disallow invoking constructor
		private BezierArc(){}
		
		/**
		 * The magic number used in the formula, 4/3
		 */
		public static final double DISTANCE_CONSTANT = 4d/3;
		/**
		 * Distance for 1/2 of a circle
		 */
		public static final double HALF_DISTANCE = innerDistanceFraction(2);
		/**
		 * Distance for 1/4 of a circle
		 */
		public static final double QUARTER_DISTANCE = innerDistanceFraction(4);
		/**
		 * The longest a segment can be before it is automatically subdivided
		 */
		public static final double MAX_SEGMENT_ANGLE = Math.PI*0.5+Floats.D_EPSILON;
		/**
		 * Reciprocal of the segment angle limit before subdivision
		 */
		public static final double IMAX_SEGMENT_ANGLE = 1d/MAX_SEGMENT_ANGLE;
		
		/**
		 * Distance to the inner control points,
		 * given the angle of the arc
		 * 
		 * @param angle
		 * @return
		 */
		public static double innerDistanceAngle(double angle){
			return DISTANCE_CONSTANT*Math.tan(angle*0.25);
		}
		
		/**
		 * Distance to the inner control points,
		 * given that the arc is 1/n of a circle
		 * 
		 * @param n
		 * @return
		 */
		public static double innerDistanceFraction(double n){
			return DISTANCE_CONSTANT*Math.tan(Math.PI*0.5/n);
		}
		
		/**
		 * With a circle centered at
		 * <i>(cx,cy)</i>
		 * and with radius vector
		 * <i>(rx,ry)</i>
		 * add n quarter circles clockwise onto <i>path</i>.
		 * <br>
		 * If n is negative, goes -n counterclockwise instead
		 * <br>
		 * Does not automatically add the first point
		 * <br>
		 * Normally this would go counterclockwise, but in graphics
		 * +y is down instead of up, so it's clockwise instead
		 * 
		 * @param path path to append to
		 * @param cx x of center
		 * @param cy y of center
		 * @param rx x of radius
		 * @param ry y of radius
		 * @param n clockwise quarters
		 */
		public static void quarters(Path2D.Double path,double cx,double cy,double rx,double ry,int n){
			if(n>0){
				if(n>4)n=4;
				for(int i=0;i<n;i++){
					double nrx = -ry, nry = rx;
					path.curveTo(
							cx+rx-ry*QUARTER_DISTANCE,
							cy+ry+rx*QUARTER_DISTANCE,
							cx+nrx+nry*QUARTER_DISTANCE,
							cy+nry-nrx*QUARTER_DISTANCE,
							cx+nrx,
							cy+nry);
					rx = nrx;ry = nry;
				}
			}else if(n<0){
				n=-n;
				if(n>4)n=4;
				for(int i=0;i<n;i++){
					double nrx = ry, nry = -rx;
					path.curveTo(
							cx+rx-ry*QUARTER_DISTANCE,
							cy+ry+rx*QUARTER_DISTANCE,
							cx+nrx+nry*QUARTER_DISTANCE,
							cy+nry-nrx*QUARTER_DISTANCE,
							cx+nrx,
							cy+nry);
					rx = nrx;ry = nry;
				}
			}
		}
		
		/**
		 * With a circle centered at
		 * <i>(cx,cy)</i>
		 * and with radius
		 * <i>r</i>
		 * add an arc with clockwise angle
		 * <i>arcAngle</i>
		 * starting from clockwise angle
		 * <i>startAngle</i>
		 * using
		 * <i>segments</i>
		 * smaller bezier curves added onto <i>path</i>.
		 * <br>
		 * If 0 or negative <i>segments</i>, automatically calculates how many are needed
		 * for a decent approximation, and will use at least <i>-segments</i>
		 * <br>
		 * If <i>transform</i> is provided, will pass relative-to-center coordinates
		 * to it before adding the center and finally giving it to <i>path</i>
		 * <br>
		 * Does not automatically add the first point
		 * <br>
		 * Normally this would go counterclockwise, but in graphics
		 * +y is down instead of up, so it's clockwise instead
		 * 
		 * @param path
		 * @param transform
		 * @param cx
		 * @param cy
		 * @param r
		 * @param startAngle
		 * @param arcAngle
		 * @param segments
		 */
		public static void arc(Path2D.Double path,AffineTransform transform,double cx,double cy,double r,double startAngle,double arcAngle,int segments){
			// Null becomes identity
			if(transform==null)transform = new AffineTransform();
			// Limit to full circle
			arcAngle = Floats.median(-Math.PI*2, arcAngle, Math.PI*2);
			// Automatically calculate needed segments
			if(segments<=0){
				segments = Math.max(-segments, (int)Math.ceil(Math.abs(arcAngle)*IMAX_SEGMENT_ANGLE));
			}
			// Needed constants
			final double segAngle = arcAngle/segments;
			final double distance = innerDistanceAngle(segAngle);
			final double ca = Math.cos(segAngle), sa = Math.sin(segAngle);
			// Inner points + end, before and after transform
			final double[] pre = new double[6];
			final double[] post = new double[6];
			double nrx = pre[4] = r*Math.cos(startAngle);
			double nry = pre[5] = r*Math.sin(startAngle);
			// Start, before transform
			double rx,ry;
			for(int i=0;i<segments;i++){
				// The end becomes the start
				rx = nrx;
				ry = nry;
				// Rotation to get the next end
				pre[4] = nrx = rx*ca - ry*sa;
				pre[5] = nry = ry*ca + rx*sa;
				// Compute inner points
				pre[0] = rx-ry*distance;
				pre[1] = ry+rx*distance;
				pre[2] = nrx+nry*distance;
				pre[3] = nry-nrx*distance;
				// Transform
				transform.transform(pre, 0, post, 0, 3);
				// Add the segment
				path.curveTo(
						cx+post[0],
						cy+post[1],
						cx+post[2],
						cy+post[3],
						cx+post[4],
						cy+post[5]);
			}
		}
		
	}
	
}
