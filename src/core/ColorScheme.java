package core;

import java.awt.Color;
import java.io.Serializable;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.skin.terra.*;
import java.util.*;
import util.*;
import util.math.*;
import util.ds.*;
import util.hash.*;

/**
 * Represents an immutable color scheme matching Apache Pivot
 * <br>
 * Descriptions here assume a light color scheme
 * 
 * @author EPICI
 * @version 1.0
 */
public class ColorScheme implements Serializable{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Standard web color scheme, used if no others are available
	 */
	public static ColorScheme DEFAULT_COLORS = new ColorScheme(
			0x000000,
			0xFFFFFF,
			0x999999,
			0xDDDCD5,
			0x336699,
			0x336699,
			0xFFE480,
			0xEB0000
			);
	
	private static ColorScheme pivotColors;
	
	/**
	 * Caches brightening; it's not such an expensive operation
	 * but it's a little speed-up since most colors are derived
	 * from color scheme colors by brightening or darkening
	 */
	private static HashMap<Any.O2<Color,float[]>,Color> memoHsb = new LhmCache<>(1<<10,true);
	
	static{
		refreshDefaultPivotColors();
	}
	
	/**
	 * Seen in Apache Pivot in:
	 * <ul>
	 * <li>Text</li>
	 * </ul>
	 */
	public final Color text;
	/**
	 * Seen in Apache Pivot in:
	 * <ul>
	 * <li>Background fill</li>
	 * <li>Alternate table row background fill</li>
	 * </ul>
	 */
	public final Color background;
	/**
	 * Seen in Apache Pivot in:
	 * <ul>
	 * <li>Outlines</li>
	 * <li>Disabled items' text</li>
	 * <li>Disabled items' fill</li>
	 * </ul>
	 */
	public final Color line;
	/**
	 * Seen in Apache Pivot in:
	 * <ul>
	 * <li>Generic component fill/coloration with light/dark gradients/tones</li>
	 * <li>Alternate table row background fill</li>
	 * <li>Would-be selected background fill</li>
	 * <li>Form info</li>
	 * </ul>
	 */
	public final Color gradient;
	/**
	 * Seen in Apache Pivot in:
	 * <ul>
	 * <li>Selected items</li>
	 * <li>Form questions</li>
	 * <li>Titles</li>
	 * <li>Link button text</li>
	 * <li>Secondary color</li>
	 * </ul>
	 */
	public final Color highlight;
	/**
	 * Seen in Apache Pivot in:
	 * <ul>
	 * <li>Button selections</li>
	 * </ul>
	 */
	public final Color selected;
	/**
	 * Seen in Apache Pivot in:
	 * <ul>
	 * <li>Form warnings</li>
	 * <li>Tooltip fill</li>
	 * </ul>
	 */
	public final Color warning;
	/**
	 * Seen in Apache Pivot in:
	 * <ul>
	 * <li>Form errors</li>
	 * </ul>
	 */
	public final Color error;
	
	/**
	 * Hash key for <i>hashCode()</i>
	 */
	public static final long HK_HC = QuickKeyGen.next64();
	
	/**
	 * Copy from the given theme
	 * 
	 * @param theme the theme to copy the colors from
	 */
	public ColorScheme(TerraTheme theme){
		text = theme.getBaseColor(0);
		background = theme.getBaseColor(1);
		line = theme.getBaseColor(2);
		gradient = theme.getBaseColor(3);
		highlight = theme.getBaseColor(4);
		selected = theme.getBaseColor(5);
		warning = theme.getBaseColor(6);
		error = theme.getBaseColor(7);
	}
	
	/**
	 * Get the current theme, and attempt to remake the
	 * active color scheme based on it
	 */
	public static void refreshDefaultPivotColors() {
		Theme theme = Theme.getTheme();
		if(theme!=null && theme instanceof TerraTheme)
			pivotColors = new ColorScheme((TerraTheme) theme);
	}

	/**
	 * Pass all colors in order with RGB hexadecimal colours
	 * <br>
	 * Done with array/varargs for compatiblity and convenience
	 * 
	 * @param colors in order: text, background, line, gradient,
	 * highlight, selected, warning, error
	 */
	public ColorScheme(int... colors){
		text = new Color(colors[0]);
		background = new Color(colors[1]);
		line = new Color(colors[2]);
		gradient = new Color(colors[3]);
		highlight = new Color(colors[4]);
		selected = new Color(colors[5]);
		warning = new Color(colors[6]);
		error = new Color(colors[7]);
	}
	
	/**
	 * Pass all colors in order pre-made
	 * <br>
	 * Done with array/varargs for compatiblity and convenience,
	 * extra elements will be ignored
	 * 
	 * @param colors in order: text, background, line, gradient,
	 * highlight, selected, warning, error
	 */
	public ColorScheme(Color... colors){
		//Null check
		if(colors.length<8)throw new IllegalArgumentException("Must provide 8 colors");
		for(int i=0;i<8;i++)
			if(colors[i]==null)
				throw new NullPointerException("Color cannot be null");
		text = colors[0];
		background = colors[1];
		line = colors[2];
		gradient = colors[3];
		highlight = colors[4];
		selected = colors[5];
		warning = colors[6];
		error = colors[7];
	}
	
	/**
	 * Pass all colors in order with RGB vectors
	 * <br>
	 * Done with array/varargs for compatiblity and convenience
	 * 
	 * @param colors in order: text, background, line, gradient,
	 * highlight, selected, warning, error
	 */
	public ColorScheme(float[]... colors){
		text = make3(colors[0]);
		background = make3(colors[1]);
		line = make3(colors[2]);
		gradient = make3(colors[3]);
		highlight = make3(colors[4]);
		selected = make3(colors[5]);
		warning = make3(colors[6]);
		error = make3(colors[7]);
	}
	
	/**
	 * Pass all colors in order with RGB vectors
	 * <br>
	 * Done with array/varargs for compatiblity and convenience
	 * 
	 * @param colors in order: text, background, line, gradient,
	 * highlight, selected, warning, error
	 */
	public ColorScheme(double[]... colors){
		text = make3(colors[0]);
		background = make3(colors[1]);
		line = make3(colors[2]);
		gradient = make3(colors[3]);
		highlight = make3(colors[4]);
		selected = make3(colors[5]);
		warning = make3(colors[6]);
		error = make3(colors[7]);
	}
	
	/**
	 * Push this color scheme to a theme, modifying it in place, and return it
	 * 
	 * @param theme the theme to modify
	 * @return the same theme after changing
	 */
	public TerraTheme writeTo(TerraTheme theme){
		theme.setBaseColor(0, text);
		theme.setBaseColor(1, background);
		theme.setBaseColor(2, line);
		theme.setBaseColor(3, gradient);
		theme.setBaseColor(4, highlight);
		theme.setBaseColor(5, selected);
		theme.setBaseColor(6, warning);
		theme.setBaseColor(7, error);
		return theme;
	}
	
	public int hashCode(){
		HashTriArx hash = new HashTriArx(HK_HC);
		hash.absorbObj(text,background,line,gradient,highlight,selected,warning,error);
		return hash.squeezeInt();
	}
	
	public boolean equals(Object obj) {
		if (this == obj)return true;
		if (obj == null)return false;
		if (!(obj instanceof ColorScheme))return false;
		ColorScheme other = (ColorScheme) obj;
		return
				text.equals(other.text) &&
				background.equals(other.background) &&
				line.equals(other.line) &&
				gradient.equals(other.gradient) &&
				highlight.equals(other.highlight) &&
				selected.equals(other.selected) &&
				warning.equals(other.warning) &&
				error.equals(other.error);
	}
	
	public static ColorScheme getPivotColors(){
		return pivotColors==null?DEFAULT_COLORS:pivotColors;
	}

	private static Color make3(float[] vec){
		return new Color(vec[0],vec[1],vec[2]);
	}
	
	private static Color make3(double[] vec){
		return new Color((float)vec[0],(float)vec[1],(float)vec[2]);
	}
	
	/**
	 * Taken from {@link TerraTheme}. Brightens or darkens a color <i>linearly</i>.
	 * <br>
	 * Note that {@link TerraTheme} uses <i>+0.1</i> for the next lighter color
	 * and <i>-0.1</i> for the next darker color, so to be consistent with
	 * the rest of pivot, those increments should be used.
	 * 
	 * @param color original color
	 * @param adjustment value between -1 and 1, 0 has no effect
	 * @return brightness-adjusted color
	 */
	public static Color brighten(Color color, float adjustment) {
		return adjustHsb(color,0f,0f,adjustment);
	}
	
	/**
	 * Taken from {@link TerraTheme}. Adjusts hue, saturation and brightness <i>linearly</i>
	 * and independently of the others.
	 * 
	 * @param color original color
	 * @param dhue adjustment value for hue (1=360 degrees)
	 * @param hsat adjustment value for saturation (-1 to greyscale, 1 to color)
	 * @param dbright adjustment value brightness (-1 to black, 1 to white)
	 * @return adjusted color
	 */
	public static Color adjustHsb(Color color, float dhue, float dsat, float dbright){
		float[] fkey = {dhue,dsat,dbright};
		Any.O2<Color,float[]> key = new Any.O2<Color,float[]>(color,fkey);
		Color result = memoHsb.get(key);
		if(result!=null)return result;
		int argb = color.getRGB();
		float[] hsb = Color.RGBtoHSB((argb>>16)&0xff, (argb>>8)&0xff, argb&0xff, null);
		int rgb = Color.HSBtoRGB(hsb[0]+dhue, Floats.median(0, 1, hsb[1]+dsat), Floats.median(0, 1, hsb[2]+dbright));
		result = new Color((argb&0xff000000) | (rgb & 0xffffff), true);
		memoHsb.put(key, result);
		return result;
	}
	
	/**
	 * Convenience method to copy RGB of a color and change
	 * just the alpha value. Even though it's not a lot of code,
	 * it's less confusing to use than the methods or constructors
	 * provided by {@link Color}. So this is provided.
	 * 
	 * @param color original color, RGB will be used
	 * @param alpha alpha of new color
	 * @return color using old RGB and new alpha
	 */
	public static Color setAlpha(Color color,int alpha){
		return new Color(color.getRGB()|(alpha<<24),true);
	}
	
}