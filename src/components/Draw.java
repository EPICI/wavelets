package components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

//Drawing class
public class Draw {
	//Max size
	public static final Dimension MAX_SIZE = new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE);
	
    //Copy of interpolation function
    public static double lerp(double a,double b,double t){
        return (1-t)*a+b*t;
    }
    
	//Colour from array
	public static Color colorFromArray(float[] values){
		return new Color(values[0],values[1],values[2],values[3]);
	}
	
	//Used in interpolation for colours
	public static int colInterp(int a, int b, double t){
		int result = (int) Math.round(a*(1-t)+b*t);
		if(result<0){
			return 0;
		}else if(result>255){
			return 255;
		}else{
			return result;
		}
	}
	
	//Interpolate
	public static Color interpColors(Color a, Color b, double t){
		return new Color(colInterp(a.getRed(),b.getRed(),t),
				colInterp(a.getGreen(),b.getGreen(),t),
				colInterp(a.getBlue(),b.getBlue(),t),
				colInterp(a.getAlpha(),b.getAlpha(),t));
	}
	
	//Rounded mapping, useful for drawing
	public static int mapToRound(double a, double b, double c, double d, double p){
		return (int) Math.round((p-a)/(b-a)*(d-c)+c);
	}
	
	//Centered string
	//From http://stackoverflow.com/questions/27706197/how-can-i-center-graphics-drawstring-in-java#27740330
	public static void centeredString(Graphics g, String s, int x, int y, int width, int height){
		//Get the FontMetrics
		FontMetrics metrics = g.getFontMetrics();
		//Determine the X coordinate for the text
		int textX = x + (width - metrics.stringWidth(s)) / 2;
		//Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
		int textY = y + ((height - metrics.getHeight()) / 2) + metrics.getAscent();
		g.drawString(s,textX,textY);
	}
	public static void centeredString(Graphics g, String s, int x, int y){
		centeredString(g,s,x,y,0,0);
	}
	public static void centeredString(Graphics g, String s, Rectangle r){
		//Use width and height
		centeredString(g,s,r.x,r.y,r.width,r.height);
	}
}
