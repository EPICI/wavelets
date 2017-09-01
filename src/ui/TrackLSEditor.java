package ui;

import core.*;
import org.apache.pivot.wtk.*;
import java.net.URL;
import java.util.*;
import org.apache.pivot.beans.*;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.content.ButtonData;
import util.ui.*;

/**
 * Editor UI for {@link TrackLayerSimple}
 * 
 * @author EPICI
 * @version 1.0
 */
public class TrackLSEditor extends Window implements Bindable {

	/**
	 * The current/linked session
	 */
	public Session session;
	
	public TrackLSEditor(){
		super();
	}
	
	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
		// TODO Auto-generated method stub
		
	}

	
}
