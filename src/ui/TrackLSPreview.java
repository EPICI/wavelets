package ui;

import java.awt.*;
import org.apache.pivot.wtk.*;
import core.*;
import util.*;
import util.ui.*;

/**
 * Row sized preview for a {@link TrackLayerSimple}
 * 
 * @author EPICI
 * @version 1.0
 */
public class TrackLSPreview extends Track.ViewComponent {

	/**
	 * {@link TrackLayerSimple} to preview
	 */
	public TrackLayerSimple target;
	/**
	 * Horizontal length allotted to one measure
	 * <br>
	 * Non-positive signals uninitialized
	 */
	public transient double pixelsPerMeasure = 0d;
	/**
	 * Height
	 * <br>
	 * Non-positive signals uninitialized
	 */
	public transient int height = 0;
	
	public TrackLSPreview(TrackLayerSimple track){
		target = track;
		installSkin(TrackLSPreview.class);
	}
	
	@Override
	public void setPixelsPerMeasure(double v) {
		pixelsPerMeasure = v;
	}

}
