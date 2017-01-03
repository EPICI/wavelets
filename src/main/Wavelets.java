package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import components.*;

//The main program
public class Wavelets{
	
	//The main frame
	public static JFrame mainFrame;
	//Secondary frames
	public static JFrame popupFrame;
	public static JPanel popupPanel;
	//The top menu bar
	public static JMenuBar menuBar = new JMenuBar();
	//Menus
	public static ArrayList<JMenu> menus = new ArrayList<JMenu>();
	//Menu items
	public static ArrayList<ArrayList<JMenuItem>> menuItems = new ArrayList<ArrayList<JMenuItem>>();
	public static ArrayList<ArrayList<JMenuItem>> submenuItems = new ArrayList<ArrayList<JMenuItem>>();
	//Composer panes
	public static JScrollPane composerTopScrollPane;//Top panel is toolbar
	public static JPanel composerTopInPanel;//Inside panel
	public static JPanel composerBottomPanel;//Bottom panel is filters manager
	public static JScrollPane composerLeftScrollPane;//Left panel is layer manager
	public static JPanel composerLeftInPanel;//Inside panel
	public static JScrollPane composerRightScrollPane;//Right panel is shared property manager
	public static JPanel composerRightInPanel;//Inside panel
	public static JPanel composerCenterPanel;//Middle is clip viewer
	public static ArrayList<JComponent> composerPanels = new ArrayList<JComponent>();
	//Child objects
	public static ArrayList<JPanel> composerTopPanelSubpanels;//Top panel subpanels
	public static ArrayList<ArrayList<JComponent>> composerTopPanelComponents;//Top panel objects
	public static JPanel composerLeftPanelSubpanel;//Toolbar subpanel
	public static JButton composerLeftPanelAddButton;//Add new layer
	//Curve editor panes
	public static JScrollPane curveEditorScrollPane;//Parent scroll pane
	public static JPanel curveEditorInPanel;//Inside panel
	public static JPanel curveEditorToolbar;//Toolbar parent
	//Child objects
	public static ArrayList<JPanel> curveEditorToolbars;//Individual toolbars
	public static JComboBox<String> curveEditorSelector;//Select a Curve object
	public static JComboBox<String> curveEditorInterpSelector;//Interpolation mode selector
	public static JComboBox<String> curveEditorPreviewSelector;//Preview mode selector
	public static JLabel curveEditorInterpLabel;//Interpolation mode label
	public static JLabel curveEditorPreviewLabel;//Preview label
	public static JTextField curveEditorName;//Rename
	public static JButton curveEditorRename;//Rename selected curve
	public static JButton curveEditorDelete;//Delete selected curve
	public static JButton curveEditorCreate;//Create new curve with name
	public static JButton curveEditorDupli;//Duplicate selected curve
	public static JButton curveEditorPreviewPlay;//Play preview
	//Protective bit
	public static boolean curveEditorSelectorChanging = false;
	//Curves cache used for comparison
	public static String[] curvesCache;
	//Node editor panes
	public static JScrollPane nodeEditorScrollPane;//Parent scroll pane
	public static JPanel nodeEditorInPanel;//Inside panel
	public static JPanel nodeEditorToolbar;//Toolbar parent
	//Child objects
	public static ArrayList<JPanel> nodeEditorToolbars;//Individual toolbars
	public static JComboBox<String> nodeEditorSelector;//Select a Nodes object
	public static JTextField nodeEditorName;//Rename
	public static JButton nodeEditorRename;//Rename selected nodes
	public static JButton nodeEditorDelete;//Delete selected nodes
	public static JButton nodeEditorCreate;//Create new nodes with name
	public static JButton nodeEditorDupli;//Duplicate selected nodes
	//Protective bit
	public static boolean nodeEditorSelectorChanging = false;
	//Nodes cache used for comparison
	public static String[] nodesCache;
	//Set to true while saving, exporting, etc.
	public static boolean working;
	//Current window
	public static String window;
	
	//Current composition
	public static Composition composition;
	
	//Main thread
	public static Thread mainThread = Thread.currentThread();
	//Main player
	public static Player mainPlayer = new Player();
	
	//Save file
	public static String fileName = "";
	public static boolean fileNamed = false;
	
	//Window scale
	public static double windowX = 1000;
	public static double windowY = 600;
		
	//Theme colours, RGBA
	public static float[] rgbaOuter = {0.8f,0.8f,0.8f,1.0f};
	public static float[] rgbaInner = {1.0f,1.0f,1.0f,1.0f};
	public static float[] rgbaText = {0.0f,0.0f,0.0f,1.0f};
	public static float[] rgbaGridLine = {0.6f,0.6f,0.6f,1.0f};
	public static float[] rgbaHighlight = {0.2f,0.8f,0.2f,1.0f};
	public static float[] rgbaSelect = {0.8f,0.2f,0.2f,1.0f};
	public static float[] rgbaActive = {0.2f,0.2f,0.8f,1.0f};
	public static float[] rgbaNodeInput = {0.7f,0.7f,0.3f,1.0f};
	public static float[] rgbaNodeInter = {0.3f,0.7f,0.7f,1.0f};//Intermediate
	public static float[] rgbaNodeOutput = {0.7f,0.3f,0.7f,1.0f};
	
	//String finders
	public static HashMap<String,StringFinder> stringFinders = new HashMap<String,StringFinder>();
	public static String[] stringFindersKeysArray;
	
	//Clip comparators
	public static HashMap<String,Comparator<Clip>> clipComparators = new HashMap<String,Comparator<Clip>>();
	public static String[] clipComparatorsKeysArray;
	
	//Layer quick draw clip behaviours
	public static HashMap<String,Integer> layerQdBehaviours = new HashMap<String,Integer>();
	public static String[] layerQdBehavioursKeysArray;
	
	public interface StringFinder{
		//Check if a string matches in some way
		public boolean match(String body,String search);
	}
	
	//Common window listeners
	public static WindowListener wlHide = new WindowListener() {

		@Override
		public void windowActivated(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowClosed(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowClosing(WindowEvent e) {
			((JFrame) e.getSource()).dispose();
		}

		@Override
		public void windowDeactivated(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowDeiconified(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowIconified(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowOpened(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	//Set default font
	//Source: http://stackoverflow.com/questions/7434845/setting-the-default-font-of-swing-program
	public static void setUIFont (javax.swing.plaf.FontUIResource f){
		Enumeration<Object> keys = UIManager.getDefaults().keys();//TODO figure out what type it is
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value != null && value instanceof javax.swing.plaf.FontUIResource)
			UIManager.put (key, f);
		}
	}
	
	//Update the display
	public static void updateDisplay(){
		//Update components
		SwingUtilities.updateComponentTreeUI(mainFrame);
		//TODO redraw all
	}
	
	//Update the display
	public static void updateDisplay(JFrame target){
		//Update components
		SwingUtilities.updateComponentTreeUI(target);
		//TODO redraw all
	}
		
	//Re-add all components, useful for loading or refreshing
	public static void reAddAll(){
		switch(window){
		case "Composer":{
			composerLeftInPanel.removeAll();
			initComposerLeftPanelSubpanel();
			for(JPanel currentPanel : composition.layerPanels){
				composerLeftInPanel.add(currentPanel);
			}
			break;
		}
		}
		
	}
	
	//Add a new layer
	public static void addNewLayer(){
		addLayerScroll(composition.addLayer());
	}
	public static void addLayerScroll(JScrollPane firstLayerPanel){
		composerLeftInPanel.add(firstLayerPanel);
	}
	
	//Initialize player thread
	public static void initPlayer(){
		mainPlayer.setName("Wavelets - Audio player");
		mainPlayer.setDaemon(true);
		mainPlayer.start();
	}
	
	//Initialize composition
	public static void initComposition(){
		if(fileNamed){
			String origFileName = fileName;
			String[] dotParts = fileName.split(".");
			String extension = dotParts[dotParts.length-1].toLowerCase();
			switch(extension){
			case "txt":
			case "json":{
				//Interpret as JSON
				fileNamed = false;//Avoid resaving to it
				fileName = "";
				composition = new Composition();
				composition.initTransient();
				//From https://www.thepolyglotdeveloper.com/2015/03/parse-json-file-java/
				try{
					BufferedReader br;
					br = new BufferedReader(new FileReader(origFileName));
					StringBuilder sb = new StringBuilder();
					String line = br.readLine();
					while (line != null) {
						sb.append(line);
						line = br.readLine();
					}
					br.close();
					composition.importDataFromJson(sb.toString(), true, true);
				}catch(FileNotFoundException e){
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch(IOException e){
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}case "wlc1":{
				//Interpret as serialized composition v1
				//Add something like this when updating:
				//fileName.replaceAll("wlc1", "wlc2");
				try{
					FileInputStream fis = new FileInputStream(origFileName);
					ObjectInputStream ois = new ObjectInputStream(fis);
					Object first = ois.readObject();
					if(first instanceof Composition){
						composition = (Composition) first;
					}
					fis.close();
				}catch(FileNotFoundException e){
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch(IOException e){
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch(ClassNotFoundException e){
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			}
		}else{
			composition = new Composition();
			composition.initTransient();
		}
	}
	
	//Complete composition initialization
	public static void initCompositionSecond(){
		if(fileNamed){
			//Refresh the display
			reAddAll();
		}else{
			
		}
	}
	
	//Initialize panels
	public static void initPanels(){
		//Composer layout
		initComposer();
		//Curve editor layout
		initCurveEditor();
		//Node editor layout
		initNodeEditor();
	}
	
	//Create composer layout and enable buttons
	public static void enableComposer(){
		mainFrame.add(composerTopScrollPane,BorderLayout.PAGE_START);
		mainFrame.add(composerBottomPanel,BorderLayout.PAGE_END);
		mainFrame.add(composerLeftScrollPane,BorderLayout.LINE_START);
		mainFrame.add(composerRightScrollPane,BorderLayout.LINE_END);
		mainFrame.add(composerCenterPanel,BorderLayout.CENTER);
		//Disable composer button
		menuItems.get(1).get(0).setEnabled(false);
		window = "Composer";
	}
	
	//Remove composer layout and disable buttons
	public static void disableComposer(){
		mainFrame.remove(composerTopScrollPane);
		mainFrame.remove(composerBottomPanel);
		mainFrame.remove(composerLeftScrollPane);
		mainFrame.remove(composerRightScrollPane);
		mainFrame.remove(composerCenterPanel);
		//Enable composer button
		menuItems.get(1).get(0).setEnabled(true);
	}
	
	//Create curve editor layout and enable buttons
	public static void enableCurveEditor(){
		mainFrame.add(curveEditorScrollPane);
		updateCurveSelection();
		menuItems.get(1).get(1).setEnabled(false);
		window = "Curve Editor";
	}
	
	//Remove curve editor layout and disable buttons
	public static void disableCurveEditor(){
		mainFrame.remove(curveEditorScrollPane);
		menuItems.get(1).get(1).setEnabled(true);
	}
	
	//Create node editor layout and enable buttons
	public static void enableNodeEditor(){
		mainFrame.add(nodeEditorScrollPane);
		updateNodeSelection();
		menuItems.get(1).get(2).setEnabled(false);
		window = "Node Editor";
	}
	
	//Remove node editor layout and disable buttons
	public static void disableNodeEditor(){
		mainFrame.remove(nodeEditorScrollPane);
		menuItems.get(1).get(2).setEnabled(true);
	}
	
	public static void initComposer(){
		//Composer layout
		composerTopInPanel = new JPanel(new FlowLayout());
		composerTopScrollPane = new JScrollPane(composerTopInPanel);
		composerBottomPanel = new JPanel(new BorderLayout());
		composerLeftInPanel = new JPanel();
		composerLeftInPanel.setLayout(new BoxLayout(composerLeftInPanel, BoxLayout.PAGE_AXIS));
		composerLeftScrollPane = new JScrollPane(composerLeftInPanel);
		composerLeftScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		composerRightScrollPane = new JScrollPane(composerRightInPanel);
		composerCenterPanel = new WLayersViewerPanel();
		composerPanels.add(composerTopScrollPane);
		composerPanels.add(composerBottomPanel);
		composerPanels.add(composerLeftScrollPane);
		composerPanels.add(composerRightScrollPane);
		composerPanels.add(composerCenterPanel);
		//Top panel
		initComposerTopPanelSubpanel();
		//Left panel
		initComposerLeftPanelSubpanel();
		//Right panel
		initComposerRightPanelSubpanel();
	}
	
	public static void initComposerTopPanelSubpanel(){
		//Create objects
		composerTopPanelSubpanels = new ArrayList<JPanel>();
		composerTopPanelComponents = new ArrayList<ArrayList<JComponent>>();
		composerTopPanelComponents.add(new ArrayList<JComponent>());
		composerTopPanelComponents.get(0).add(new JButton("Stop player"));
		composerTopPanelComponents.add(new ArrayList<JComponent>());
		composerTopPanelComponents.get(1).add(new JLabel("Composition"));
		composerTopPanelComponents.get(1).add(new JButton("Play"));
		composerTopPanelComponents.get(1).add(new JButton("Clear cache"));
		composerTopPanelComponents.add(new ArrayList<JComponent>());
		composerTopPanelComponents.get(2).add(new JLabel("Layer"));
		composerTopPanelComponents.get(2).add(new JButton("Play"));
		composerTopPanelComponents.get(2).add(new JButton("Clear cache"));
		composerTopPanelComponents.add(new ArrayList<JComponent>());
		composerTopPanelComponents.get(3).add(new JLabel("No clip selected"));//This should have the clip number
		composerTopPanelComponents.get(3).add(new JButton("\u25C4"));//Left arrow
		composerTopPanelComponents.get(3).add(new JButton("\u25BA"));//Right arrow
		//Add objects
		for(ArrayList<JComponent> currentcomponentlist : composerTopPanelComponents){
			JPanel currentpanel = new JPanel();
			currentpanel.setLayout(new FlowLayout());
			composerTopInPanel.add(currentpanel);
			composerTopPanelSubpanels.add(currentpanel);
			for(JComponent currentcomponent : currentcomponentlist){
				currentpanel.add(currentcomponent);
			}
		}
		//Listeners
		composerTopPanelComponents.get(0).get(0).addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				mainPlayer.stopSound();
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
		composerTopPanelComponents.get(2).get(1).addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(composition.layers.containsKey(composition.layerSelection)){
					Layer current = composition.layers.get(composition.layerSelection);
					if(current.clipCount>0){
						double[] soundDouble = current.getAudio();
						short[] soundShort = WaveUtils.quickShort(soundDouble);
						mainPlayer.playSound(soundShort);
					}
				}
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
		composerTopPanelComponents.get(1).get(2).addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				composition.clearCache();
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
		composerTopPanelComponents.get(1).get(1).addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				double[] soundDouble = composition.getAudio();
				short[] soundShort = WaveUtils.quickShort(soundDouble);
				mainPlayer.playSound(soundShort);
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
		composerTopPanelComponents.get(2).get(2).addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(composition.layers.containsKey(composition.layerSelection)){
					Layer current = composition.layers.get(composition.layerSelection);
					current.clearCache();
				}
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
		composerTopPanelComponents.get(3).get(1).addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(composition.layers.containsKey(composition.layerSelection)){
					Layer current = composition.layers.get(composition.layerSelection);
					if(current.clipCount>0){
						current.selectedClip--;
						current.updateClipSelection();
					}else{
						current.addClip();
					}
				}
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
		composerTopPanelComponents.get(3).get(2).addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(composition.layers.containsKey(composition.layerSelection)){
					Layer current = composition.layers.get(composition.layerSelection);
					if(current.clipCount>0){
						current.selectedClip++;
						current.updateClipSelection();
					}else{
						current.addClip();
					}
				}
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
	}
		
	public static void initComposerLeftPanelSubpanel(){
		//Left panel
		composerLeftPanelSubpanel = new JPanel(new BorderLayout());
		composerLeftPanelSubpanel.setPreferredSize(new Dimension(280,60));//TODO temporary
		composerLeftPanelAddButton = new JButton("Add new layer");
		//composerLeftPanelAddButton.setToolTipText("Creates a new blank layer.");
		composerLeftPanelSubpanel.add(composerLeftPanelAddButton,BorderLayout.CENTER);
		composerLeftPanelAddButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				addNewLayer();
				updateDisplay();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			}

			@Override
			public void mouseExited(MouseEvent e) {
				//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			}
		});
		composerLeftInPanel.add(composerLeftPanelSubpanel);
	}
	
	public static void initComposerRightPanelSubpanel(){
		//Right panel
		composerRightInPanel = new JPanel();
		composerRightScrollPane = new JScrollPane(composerRightInPanel);
	}
	
	//Initialize curve editor layout
	public static void initCurveEditor(){
		//Initialize Curve class
		Curve.modeNames.put("Linear Interpolation", 0);
		Curve.modeNames.put("Square Interpolation", 1);
		Curve.modeNames.put("Sine Interpolation", 2);
		Curve.modeNames.put("Auto Cubic Bezier", 3);
		Curve.previewModes.put("As waveform: 220Hz flat tone", 0);
		Curve.previewModes.put("As waveform: 440Hz flat tone", 1);
		Curve.previewModes.put("As waveform: 880Hz flat tone", 2);
		Curve.previewModes.put("As envelope: 440Hz default harmonic", 100);
		Curve.previewModes.put("As envelope: 440Hz default harmonic, 1/4 time scale", 101);
		Curve.previewModes.put("As envelope: 440Hz default harmonic, 1/16 time scale", 102);
		Curve.previewModes.put("As frequency-amplitude graph: 440Hz filtered saw (increment 1, base 1, exponent 1)", 200);
		Curve.previewModes.put("As frequency-amplitude graph: 440Hz filtered square (increment 2, base 1, exponent 1)", 201);
		Curve.previewModes.put("As frequency-amplitude graph: 440Hz filtered triangle (increment 2, base -1, exponent 2)", 202);
		Curve.updateModeNames();
		composition.updateCurves();
		//Initialize layout
		curveEditorRename = new JButton("Rename");
		//curveEditorRename.setToolTipText("Renames the curve. Will not work if the name is already taken.");
		curveEditorDelete = new JButton("Delete");
		//curveEditorDelete.setToolTipText("Deletes the curve.");
		curveEditorCreate = new JButton("Create");
		//curveEditorCreate.setToolTipText("Duplicates the current curve. The new curve will have the name in the text field. Will not work if the name is already taken.");
		curveEditorDupli = new JButton("Duplicate");
		curveEditorDupli.setEnabled(false);
		curveEditorName = new JTextField(40);
		curveEditorSelector = new JComboBox<String>(composition.curvesKeysArray);
		if(composition.curvesKeysArray.length>0){
			curveEditorSelector.setSelectedIndex(0);
		}
		//curveEditorSelector.setToolTipText("Selects a curve.");
		curveEditorPreviewLabel = new JLabel("Preview mode");
		curveEditorPreviewSelector = new JComboBox<String>(Curve.previewModesKeys);
		curveEditorPreviewPlay = new JButton("Play");
		curveEditorToolbar = new JPanel(new BorderLayout());
		curveEditorToolbars = new ArrayList<JPanel>();
		curveEditorToolbars.add(new JPanel(new GridBagLayout()));
		curveEditorToolbars.get(0).add(curveEditorSelector);
		curveEditorToolbars.get(0).add(curveEditorName);
		curveEditorToolbars.get(0).add(curveEditorRename);
		curveEditorToolbars.get(0).add(curveEditorDelete);
		curveEditorToolbars.get(0).add(curveEditorCreate);
		curveEditorToolbars.get(0).add(curveEditorDupli);
		curveEditorToolbars.add(new JPanel(new BorderLayout()));
		curveEditorInterpSelector = new JComboBox<String>(Curve.modeNamesKeys);
		curveEditorInterpSelector.setSelectedIndex(0);//Default to simple bezier
		//curveEditorInterpSelector.setToolTipText("Selects an interpolation mode for the selected curve.");
		curveEditorInterpLabel = new JLabel("Interpolation Mode");
		curveEditorToolbars.get(1).add(curveEditorInterpLabel, BorderLayout.LINE_START);
		curveEditorToolbars.get(1).add(curveEditorInterpSelector, BorderLayout.LINE_END);
		//curveEditorToolbars.add(new JPanel(new BorderLayout()));
		curveEditorToolbars.add(new JPanel(new BorderLayout()));
		curveEditorToolbars.get(2).add(curveEditorPreviewLabel,BorderLayout.LINE_START);
		curveEditorToolbars.get(2).add(curveEditorPreviewSelector,BorderLayout.CENTER);
		curveEditorToolbars.get(2).add(curveEditorPreviewPlay,BorderLayout.LINE_END);
		curveEditorToolbar.add(curveEditorToolbars.get(0),BorderLayout.PAGE_START);
		curveEditorToolbar.add(curveEditorToolbars.get(1),BorderLayout.CENTER);
		curveEditorToolbar.add(curveEditorToolbars.get(2),BorderLayout.PAGE_END);
		curveEditorInPanel = new JPanel(new BorderLayout());
		curveEditorInPanel.add(curveEditorToolbar,BorderLayout.PAGE_START);
		curveEditorScrollPane = new JScrollPane(curveEditorInPanel);
		//Listeners
		curveEditorSelector.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent event) {
				if(!curveEditorSelectorChanging){
					if(event.getStateChange()==ItemEvent.SELECTED){
						composition.curveSelection = (String) event.getItem();
						updateCurveSelection();
					}
				}
			}
			
		});
		curveEditorRename.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(composition.curves.containsKey(composition.curveSelection)){
					String newName = curveEditorName.getText();
					if(!(newName.equals(composition.curveSelection)||composition.curves.containsKey(newName))){
						composition.renameCurve(composition.curveSelection, curveEditorName.getText());
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		curveEditorDelete.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if(composition.curves.containsKey(composition.curveSelection)){
					composition.removeCurve(composition.curveSelection);
					composition.updateCurves();
					updateCurveSelection();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		curveEditorCreate.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				String newName = curveEditorName.getText();
				if(!composition.curves.containsKey(newName)){
					composition.addCurve(newName);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		curveEditorDupli.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				String newName = curveEditorName.getText();
				if(!composition.curves.containsKey(newName)){
					composition.dupliCurve(newName,composition.curveSelection);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		curveEditorInterpSelector.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent event) {
				if(event.getStateChange()==ItemEvent.SELECTED){
					if(composition.curves.containsKey(composition.curveSelection)){
						composition.curves.get(composition.curveSelection).setMode(Curve.modeNames.get(event.getItem()));
						updateDisplay();
					}
				}
			}
			
		});
		curveEditorPreviewSelector.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent event) {
				if(event.getStateChange()==ItemEvent.SELECTED){
					Curve.selectedPreviewMode = Curve.previewModes.get(event.getItem());
				}
			}
			
		});
		curveEditorPreviewPlay.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(composition.curves.containsKey(composition.curveSelection)){
					Curve selectedCurve = composition.curves.get(composition.curveSelection);
					switch(Curve.selectedPreviewMode){
					case 0:{
						double rate = 220d/composition.samplesPerSecond;
						double[] doubleArray = new double[32768];
						for(int i=0;i<32768;i++){
							doubleArray[i] = selectedCurve.valueAtPos((rate*i)%1);
						}
						short[] shortArray = WaveUtils.quickShort(doubleArray);
						mainPlayer.playSound(shortArray);
						break;
					}case 1:{
						double rate = 440d/composition.samplesPerSecond;
						double[] doubleArray = new double[32768];
						for(int i=0;i<32768;i++){
							doubleArray[i] = selectedCurve.valueAtPos((rate*i)%1);
						}
						short[] shortArray = WaveUtils.quickShort(doubleArray);
						mainPlayer.playSound(shortArray);
						break;
					}case 2:{
						double rate = 880d/composition.samplesPerSecond;
						double[] doubleArray = new double[32768];
						for(int i=0;i<32768;i++){
							doubleArray[i] = selectedCurve.valueAtPos((rate*i)%1);
						}
						short[] shortArray = WaveUtils.quickShort(doubleArray);
						mainPlayer.playSound(shortArray);
						break;
					}case 100:{
						double sampleRate = composition.samplesPerSecond;
						double[] doubleArray = WaveUtils.testTone(selectedCurve.getLocations().get(selectedCurve.listSize-1), sampleRate);
						for(int i=0;i<doubleArray.length;i++){
							doubleArray[i] *= selectedCurve.valueAtPos(i/sampleRate);
						}
						short[] shortArray = WaveUtils.quickShort(doubleArray);
						mainPlayer.playSound(shortArray);
						break;
					}case 101:{
						double sampleRate = composition.samplesPerSecond;
						double[] doubleArray = WaveUtils.testTone(selectedCurve.getLocations().get(selectedCurve.listSize-1)/4d, sampleRate);
						sampleRate/=4d;//This value will not be used again anyways
						for(int i=0;i<doubleArray.length;i++){
							doubleArray[i] *= selectedCurve.valueAtPos(i/sampleRate);
						}
						short[] shortArray = WaveUtils.quickShort(doubleArray);
						mainPlayer.playSound(shortArray);
						break;
					}case 102:{
						double sampleRate = composition.samplesPerSecond;
						double[] doubleArray = WaveUtils.testTone(selectedCurve.getLocations().get(selectedCurve.listSize-1)/16d, sampleRate);
						sampleRate/=16d;//This value will not be used again anyways
						for(int i=0;i<doubleArray.length;i++){
							doubleArray[i] *= selectedCurve.valueAtPos(i/sampleRate);
						}
						short[] shortArray = WaveUtils.quickShort(doubleArray);
						mainPlayer.playSound(shortArray);
						break;
					}case 200:{
						double[] doubleArray = new double[32768];
						for(int i=0;i<32768;i++){
							doubleArray[i] = WaveUtils.harmonic(440d, 1d, 1d, 1d, 1d, 100d, 20000d, i/44100d, selectedCurve);
						}
						short[] shortArray = WaveUtils.quickShort(doubleArray);
						mainPlayer.playSound(shortArray);
						break;
					}case 201:{
						double[] doubleArray = new double[32768];
						for(int i=0;i<32768;i++){
							doubleArray[i] = WaveUtils.harmonic(440d, 2d, 1d, 1d, 1d, 100d, 20000d, i/44100d, selectedCurve);
						}
						short[] shortArray = WaveUtils.quickShort(doubleArray);
						mainPlayer.playSound(shortArray);
						break;
					}case 202:{
						double[] doubleArray = new double[32768];
						for(int i=0;i<32768;i++){
							doubleArray[i] = WaveUtils.harmonic(440d, 2d, 1d, -1d, 2d, 100d, 20000d, i/44100d, selectedCurve);
						}
						short[] shortArray = WaveUtils.quickShort(doubleArray);
						mainPlayer.playSound(shortArray);
						break;
					}
					}
				}
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
	}
	
	public static void initNodeEditor(){
		composition.updateNodes();
		//Initialize layout
		nodeEditorRename = new JButton("Rename");
		nodeEditorDelete = new JButton("Delete");
		nodeEditorCreate = new JButton("Create");
		nodeEditorDupli = new JButton("Duplicate");
		nodeEditorDupli.setEnabled(false);
		nodeEditorName = new JTextField(40);
		nodeEditorSelector = new JComboBox<String>(composition.nodesKeysArray);
		if(composition.nodesKeysArray.length>0){
			nodeEditorSelector.setSelectedIndex(0);
		}
		nodeEditorToolbar = new JPanel(new BorderLayout());
		nodeEditorToolbars = new ArrayList<JPanel>();
		nodeEditorToolbars.add(new JPanel(new GridBagLayout()));
		nodeEditorToolbars.get(0).add(nodeEditorSelector);
		nodeEditorToolbars.get(0).add(nodeEditorName);
		nodeEditorToolbars.get(0).add(nodeEditorRename);
		nodeEditorToolbars.get(0).add(nodeEditorDelete);
		nodeEditorToolbars.get(0).add(nodeEditorCreate);
		nodeEditorToolbars.get(0).add(nodeEditorDupli);
		nodeEditorToolbar.add(nodeEditorToolbars.get(0),BorderLayout.PAGE_START);
		nodeEditorInPanel = new JPanel(new BorderLayout());
		nodeEditorInPanel.add(nodeEditorToolbar,BorderLayout.PAGE_START);
		nodeEditorScrollPane = new JScrollPane(nodeEditorInPanel);
		//Add all listeners
		nodeEditorSelector.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent event) {
				if(!nodeEditorSelectorChanging){
					if(event.getStateChange()==ItemEvent.SELECTED){
						composition.nodesSelection = (String) event.getItem();
						updateNodeSelection();
					}
				}
			}
			
		});
		nodeEditorRename.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(composition.nodes.containsKey(composition.nodesSelection)){
					String newName = nodeEditorName.getText();
					if(!(newName.equals(composition.nodesSelection)||composition.nodes.containsKey(newName))){
						composition.renameNodes(composition.nodesSelection, nodeEditorName.getText());
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		nodeEditorDelete.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if(composition.nodes.containsKey(composition.nodesSelection)){
					composition.removeNodes(composition.nodesSelection);
					composition.updateNodes();
					updateNodeSelection();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		nodeEditorCreate.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				String newName = nodeEditorName.getText();
				if(!composition.nodes.containsKey(newName)){
					composition.addNodes(newName);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		nodeEditorDupli.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				String newName = nodeEditorName.getText();
				if(!composition.nodes.containsKey(newName)){
					composition.dupliNodes(newName,composition.nodesSelection);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	//Initialize menus
	public static void initMenus(){
		stringFinders.put("Contains", new StringFinder(){
			public boolean match(String base, String search){
				return base.contains(search);
			}
		});
		stringFinders.put("Exactly matches", new StringFinder(){
			public boolean match(String base, String search){
				return base.equals(search);
			}
		});
		stringFinders.put("Contained by", new StringFinder(){
			public boolean match(String base, String search){
				return search.contains(base);
			}
		});
		stringFinders.put("Matches keywords", new StringFinder(){
			public boolean match(String base, String search){
				boolean result = true;
				ArrayList<String> origWords = new ArrayList<String>(Arrays.asList(base.split(" ")));
				String[] searches = search.split(" ");
				for(String current:searches){
					char searchType = current.charAt(0);
					String searchTerm = current.substring(1);
					switch(searchType){
					case '+':{
						result &= origWords.contains(searchTerm);
						break;
					}case '-':{
						result &= !origWords.contains(searchTerm);
						break;
					}
					}
				}
				return result;
			}
		});
		stringFindersKeysArray = stringFinders.keySet().toArray(new String[0]);
		clipComparators.put("Start time", new Comparator<Clip>(){
			@Override
			public int compare(Clip a, Clip b) {
				if(b.startTime>a.startTime){
					return 1;
				}else if(a.startTime>b.startTime){
					return -1;
				}else{
					return 0;
				}
			}
		});
		clipComparators.put("End time", new Comparator<Clip>(){
			@Override
			public int compare(Clip a, Clip b) {
				if(b.endTime>a.endTime){
					return 1;
				}else if(a.endTime>b.endTime){
					return -1;
				}else{
					return 0;
				}
			}
		});
		clipComparatorsKeysArray = clipComparators.keySet().toArray(new String[0]);
		layerQdBehaviours.put("1 control (constant pitch)", 0);
		layerQdBehaviours.put("2 controls (linear pitch)", 1);
		layerQdBehavioursKeysArray = layerQdBehaviours.keySet().toArray(new String[0]);
		//Add menu bar
		menus.add(new JMenu("File"));
		menuItems.add(new ArrayList<JMenuItem>());
		menuItems.get(0).add(new JMenuItem("Save"));
		menuItems.get(0).add(new JMenuItem("Save As"));
		menuItems.get(0).add(new JMenuItem("Open"));
		menuItems.get(0).add(new JMenu("Import"));
		menuItems.get(0).add(new JMenu("Export"));
		addMenu(menus.get(0),menuItems.get(0));
		menuBar.add(menus.get(0));
		menus.add(new JMenu("Window"));
		menuItems.add(new ArrayList<JMenuItem>());
		menuItems.get(1).add(new JMenuItem("Composer"));
		menuItems.get(1).add(new JMenuItem("Curve Editor"));
		menuItems.get(1).add(new JMenuItem("Node Editor"));
		addMenu(menus.get(1),menuItems.get(1));
		menuBar.add(menus.get(1));
		menus.add(new JMenu("Tools"));
		menuItems.add(new ArrayList<JMenuItem>());
		menuItems.get(2).add(new JMenu("Layer"));
		menuItems.get(2).add(new JMenu("Curve"));
		menuItems.get(2).add(new JMenu("Nodes"));
		addMenu(menus.get(2),menuItems.get(2));
		menuBar.add(menus.get(2));
		submenuItems.add(new ArrayList<JMenuItem>());//File > Import
		submenuItems.get(0).add(new JMenuItem("Data as JSON from file"));
		submenuItems.get(0).add(new JMenuItem("Data as JSON from text"));
		addMenu((JMenu) menuItems.get(0).get(3),submenuItems.get(0));
		submenuItems.add(new ArrayList<JMenuItem>());//File > Export
		submenuItems.get(1).add(new JMenuItem("Data as JSON to file"));
		submenuItems.get(1).add(new JMenuItem("Data as JSON to text"));
		addMenu((JMenu) menuItems.get(0).get(4),submenuItems.get(1));
		submenuItems.add(new ArrayList<JMenuItem>());//Tools > Layer
		submenuItems.get(2).add(new JMenuItem("Quick draw"));
		submenuItems.get(2).add(new JMenuItem("Time mapping"));
		submenuItems.get(2).add(new JMenuItem("Input mapping"));
		submenuItems.get(2).add(new JMenuItem("Merge"));
		submenuItems.get(2).add(new JMenuItem("Sort"));
		submenuItems.get(2).add(new JMenuItem("Time shift"));
		submenuItems.get(2).add(new JMenuItem("Duplicate"));
		submenuItems.get(2).add(new JMenuItem("Round"));
		addMenu((JMenu) menuItems.get(2).get(0),submenuItems.get(2));
		mainFrame.setJMenuBar(menuBar);
		//Set up events
		initMenuEvents();
		//Disable save if not named
		if(!fileNamed){
			menuItems.get(0).get(0).setEnabled(false);
		}
	}
	
	//Initialize menu events
	public static void initMenuEvents(){
		menuItems.get(1).get(0).addActionListener(new ActionListener() {//Composer
			@Override
			public void actionPerformed(ActionEvent arg0) {
				switch(window){//Must contain all other layouts
				case("Curve Editor"):{
					disableCurveEditor();
				}case("Node Editor"):{
					disableNodeEditor();
				}
				}
				enableComposer();
				updateDisplay();
			}
		});
		menuItems.get(1).get(1).addActionListener(new ActionListener() {//Composer
			@Override
			public void actionPerformed(ActionEvent arg0) {
				switch(window){//Must contain all other layouts
				case("Composer"):{
					disableComposer();
				}case("Node Editor"):{
					disableNodeEditor();
				}
				}
				enableCurveEditor();
				updateDisplay();
			}
		});
		menuItems.get(1).get(2).addActionListener(new ActionListener() {//Composer
			@Override
			public void actionPerformed(ActionEvent arg0) {
				switch(window){//Must contain all other layouts
				case("Composer"):{
					disableComposer();
				}case("Curve Editor"):{
					disableCurveEditor();
				}
				}
				enableNodeEditor();
				updateDisplay();
			}
		});
		submenuItems.get(0).get(1).addActionListener(new ActionListener() {//Composer
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clearPopupWindowListeners();
				popupFrame.setTitle("Wavelets [JSON Import]");
				popupFrame.addWindowListener(wlHide);
				JTextArea inputArea = new JTextArea(20,20);//Size is temporary solution
				JScrollPane inputScroll = new JScrollPane(inputArea);
				JButton closeButton = new JButton("Close");
				JButton importButton = new JButton("Import");
				JCheckBox replaceCheck = new JCheckBox("Replace");
				JCheckBox propertyCheck = new JCheckBox("Copy properties");
				closeButton.addMouseListener(new MouseListener() {

					@Override
					public void mouseClicked(MouseEvent arg0) {
						popupFrame.dispose();
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
				importButton.addMouseListener(new MouseListener(){

					@Override
					public void mouseClicked(MouseEvent arg0) {
						composition.importDataFromJson(inputArea.getText(), replaceCheck.isSelected(), propertyCheck.isSelected());
					}

					@Override
					public void mouseEntered(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void mouseExited(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void mousePressed(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void mouseReleased(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}
					
				});
				popupPanel.removeAll();
				GridBagConstraints constraint = new GridBagConstraints();
				constraint.gridx=0;
				constraint.gridy=0;
				constraint.gridwidth=2;
				popupPanel.add(inputScroll,constraint);
				constraint.gridwidth=1;
				constraint.gridy=1;
				popupPanel.add(replaceCheck,constraint);
				constraint.gridx=1;
				popupPanel.add(propertyCheck,constraint);
				constraint.gridy=2;
				constraint.gridx=0;
				popupPanel.add(importButton,constraint);
				constraint.gridx=1;
				popupPanel.add(closeButton,constraint);
				popupFrame.pack();
				popupFrame.setVisible(true);
			}
		});
		submenuItems.get(1).get(1).addActionListener(new ActionListener() {//Export to text
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clearPopupWindowListeners();
				popupFrame.setTitle("Wavelets [JSON Export]");
				popupFrame.addWindowListener(wlHide);
				String exported = composition.exportJson().toString(2);
				JTextArea outputArea = new JTextArea(exported,20,20);//Size is temporary solution
				JScrollPane outputScroll = new JScrollPane(outputArea);
				outputArea.setText(exported);
				JButton closeButton = new JButton("Close");
				closeButton.addMouseListener(new MouseListener() {

					@Override
					public void mouseClicked(MouseEvent arg0) {
						popupFrame.dispose();
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
				popupPanel.removeAll();
				GridBagConstraints constraint = new GridBagConstraints();
				constraint.gridx=0;
				constraint.gridy=0;
				popupPanel.add(outputScroll,constraint);
				constraint.gridy=1;
				popupPanel.add(closeButton,constraint);
				popupFrame.pack();
				popupFrame.setVisible(true);
			}
		});
		submenuItems.get(2).get(0).addActionListener(new ActionListener() {//Quick draw
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(composition.layers.containsKey(composition.layerSelection)){
					Layer currentLayer = composition.layers.get(composition.layerSelection);
					clearPopupWindowListeners();
					popupFrame.setTitle("Wavelets [Layer quick draw]");
					popupFrame.addWindowListener(wlHide);
					Clip copyClip = new Clip();
					copyClip.parentLayer = composition.nodeLayer;
					copyClip.nodesName = composition.nodesSelection;
					copyClip.initTransient();
					copyClip.infoNodeSelector.setSelectedItem(composition.nodesSelection);
					JLabel topLabel = new JLabel("Setup");
					JTextField timeField = new JTextField(30);
					JLabel timeLabel = new JLabel("Time divisions");
					JLabel timeMapLabel = new JLabel("Map time range to");
					JTextField timeMapLowerField = new JTextField(30);
					JLabel timeMapRangeLabel = new JLabel("to");
					JTextField timeMapUpperField = new JTextField(30);
					JLabel pitchLabel = new JLabel("Pitch range");
					JTextField pitchRangeLowerField = new JTextField(30);
					JLabel pitchRangeLabel = new JLabel("to");
					JTextField pitchRangeUpperField = new JTextField(30);
					JLabel templateLabel = new JLabel("Clip template");
					JLabel drawModeLabel = new JLabel("Drawing behaviour:");
					JComboBox<String> drawModeSelector = new JComboBox<String>(layerQdBehavioursKeysArray);
					JButton confirmButton = new JButton("Continue");
					JButton closeButton = new JButton("Close");
					confirmButton.addMouseListener(new MouseListener() {

						@Override
						public void mouseClicked(MouseEvent arg0) {
							if(copyClip.inputsRegistered){
								final double right = WaveUtils.readDoubleFromField(timeField, 32d);
								final double bottom = WaveUtils.readDoubleFromField(pitchRangeLowerField, -12d);
								final double top = WaveUtils.readDoubleFromField(pitchRangeUpperField, bottom + 24d);
								final double mapLeft = WaveUtils.readDoubleFromField(timeMapLowerField, 0d);
								final double mapRight = WaveUtils.readDoubleFromField(timeMapUpperField, mapLeft + 8d);
								final String behaviourName = (String) drawModeSelector.getSelectedItem();
								final int behaviourId = layerQdBehaviours.get(behaviourName);
								final Nodes nodesUsed = copyClip.nodeNetwork;
								nodesUsed.refreshInputs();
								final ArrayList<String> nodeInputs = nodesUsed.inputRequests;
								final String[] nodeInputsArray = nodeInputs.toArray(new String[0]);
								switch(behaviourId){
								case 0:{
									popupPanel.removeAll();
									JLabel topLabel = new JLabel("Setup");
									JLabel inputLabel1 = new JLabel("Pitch control input:");
									JComboBox<String> inputSelector1 = new JComboBox<String>(nodeInputsArray);
									JButton confirmButton = new JButton("Continue");
									JButton closeButton = new JButton("Close");
									confirmButton.addMouseListener(new MouseListener(){

										@Override
										public void mouseClicked(MouseEvent arg0) {
											final String input1 = (String) inputSelector1.getSelectedItem();
											final WLayerQuickDrawPanel.ClipBehaviour behaviour = new WLayerQuickDrawPanel.CbFlatTone(input1);
											WLayerQuickDrawPanel.initPopup(currentLayer, copyClip, behaviour, new double[]{right,bottom,top,mapLeft,mapRight});
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
											copyClip.destroy();
											popupFrame.dispose();
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
									constraint.weightx=1;
									constraint.weighty=1;
									constraint.gridx=0;
									constraint.gridy=0;
									constraint.gridwidth=2;
									popupPanel.add(topLabel,constraint);
									constraint.gridwidth=1;
									constraint.gridy=1;
									popupPanel.add(inputLabel1,constraint);
									constraint.gridx=1;
									popupPanel.add(inputSelector1, constraint);
									constraint.gridy=2;
									constraint.gridx=0;
									popupPanel.add(confirmButton,constraint);
									constraint.gridx=1;
									popupPanel.add(closeButton, constraint);
									updateDisplay(popupFrame);
									break;
								}case 1:{
									popupPanel.removeAll();
									JLabel topLabel = new JLabel("Setup");
									JLabel inputLabel1 = new JLabel("Starting pitch control input:");
									JComboBox<String> inputSelector1 = new JComboBox<String>(nodeInputsArray);
									JLabel inputLabel2 = new JLabel("Ending pitch control input:");
									JComboBox<String> inputSelector2 = new JComboBox<String>(nodeInputsArray);
									JButton confirmButton = new JButton("Continue");
									JButton closeButton = new JButton("Close");
									confirmButton.addMouseListener(new MouseListener(){

										@Override
										public void mouseClicked(MouseEvent arg0) {
											final String input1 = (String) inputSelector1.getSelectedItem();
											final String input2 = (String) inputSelector2.getSelectedItem();
											final WLayerQuickDrawPanel.ClipBehaviour behaviour = new WLayerQuickDrawPanel.CbLinearTone(input1,input2);
											WLayerQuickDrawPanel.initPopup(currentLayer, copyClip, behaviour, new double[]{right,bottom,top,mapLeft,mapRight});
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
											copyClip.destroy();
											popupFrame.dispose();
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
									constraint.weightx=1;
									constraint.weighty=1;
									constraint.gridx=0;
									constraint.gridy=0;
									constraint.gridwidth=2;
									popupPanel.add(topLabel,constraint);
									constraint.gridwidth=1;
									constraint.gridy=1;
									popupPanel.add(inputLabel1,constraint);
									constraint.gridx=1;
									popupPanel.add(inputSelector1, constraint);
									constraint.gridy=2;
									constraint.gridx=0;
									popupPanel.add(inputLabel2,constraint);
									constraint.gridx=1;
									popupPanel.add(inputSelector2, constraint);
									constraint.gridy=3;
									constraint.gridx=0;
									popupPanel.add(confirmButton,constraint);
									constraint.gridx=1;
									popupPanel.add(closeButton, constraint);
									updateDisplay(popupFrame);
									break;
								}
								}
								updateDisplay(popupFrame);
							}
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
					closeButton.addMouseListener(new MouseListener() {

						@Override
						public void mouseClicked(MouseEvent arg0) {
							copyClip.destroy();
							popupFrame.dispose();
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
					popupPanel.removeAll();
					GridBagConstraints constraint = new GridBagConstraints();
					constraint.weightx=1;
					constraint.weighty=1;
					constraint.gridx=0;
					constraint.gridy=0;
					constraint.gridwidth=4;
					popupPanel.add(topLabel,constraint);
					constraint.gridy=1;
					constraint.gridwidth=2;
					popupPanel.add(timeField,constraint);
					constraint.gridx=2;
					constraint.gridwidth=1;
					popupPanel.add(timeLabel,constraint);
					constraint.gridx=0;
					constraint.gridy=2;
					constraint.gridwidth=3;
					popupPanel.add(timeMapLabel,constraint);
					constraint.gridwidth=1;
					constraint.gridy=3;
					popupPanel.add(timeMapLowerField,constraint);
					constraint.gridx=1;
					popupPanel.add(timeMapRangeLabel,constraint);
					constraint.gridx=2;
					popupPanel.add(timeMapUpperField,constraint);
					constraint.gridx=0;
					constraint.gridy=4;
					constraint.gridwidth=3;
					popupPanel.add(pitchLabel,constraint);
					constraint.gridy=5;
					constraint.gridwidth=1;
					popupPanel.add(pitchRangeLowerField,constraint);
					constraint.gridx=1;
					popupPanel.add(pitchRangeLabel,constraint);
					constraint.gridx=2;
					popupPanel.add(pitchRangeUpperField,constraint);
					constraint.gridy=6;
					constraint.gridx=0;
					popupPanel.add(drawModeLabel,constraint);
					constraint.gridx=1;
					constraint.gridwidth=2;
					popupPanel.add(drawModeSelector,constraint);
					constraint.gridx=0;
					constraint.gridy=7;
					popupPanel.add(confirmButton,constraint);
					constraint.gridx=2;
					popupPanel.add(closeButton,constraint);
					constraint.gridx=3;
					constraint.gridy=1;
					constraint.gridwidth=1;
					popupPanel.add(templateLabel,constraint);
					constraint.gridy=2;
					constraint.gridheight=5;
					popupPanel.add(copyClip.parentPanel,constraint);
					popupFrame.pack();
					popupFrame.setVisible(true);
				}
			}
		});
		submenuItems.get(2).get(1).addActionListener(new ActionListener() {//Time mapping
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(composition.layers.containsKey(composition.layerSelection)){
					Layer currentLayer = composition.layers.get(composition.layerSelection);
					if(currentLayer.clipCount>0){
						double[] timeBounds = currentLayer.getTimeBounds();
						clearPopupWindowListeners();
						popupFrame.setTitle("Wavelets [Layer time mapping]");
						popupFrame.addWindowListener(wlHide);
						JLabel upperLabel = new JLabel("Map range");
						JTextField oldStart = new JTextField(30);
						oldStart.setText(Double.toString(timeBounds[0]));
						JLabel oldLabel = new JLabel("to");
						JTextField oldEnd = new JTextField(30);
						oldEnd.setText(Double.toString(timeBounds[1]));
						JLabel lowerLabel = new JLabel("to range");
						JTextField newStart = new JTextField(30);
						newStart.setText(Double.toString(timeBounds[1]));
						JLabel newLabel = new JLabel("to");
						JTextField newEnd = new JTextField(30);
						newEnd.setText(Double.toString(2*timeBounds[1]-timeBounds[0]));
						JLabel layerLabel = new JLabel("in layer \""+currentLayer.name+"\"");
						JButton confirmButton = new JButton("Apply");
						JButton closeButton = new JButton("Close");
						confirmButton.addMouseListener(new MouseListener() {

							@Override
							public void mouseClicked(MouseEvent arg0) {
								double a = WaveUtils.readDoubleFromField(oldStart, timeBounds[0]);
								double b = WaveUtils.readDoubleFromField(oldEnd, timeBounds[1]);
								double c = WaveUtils.readDoubleFromField(newStart, a);
								double d = WaveUtils.readDoubleFromField(newEnd, c+b-a);
								double rate = (d-c)/(b-a);
								for(Clip current:currentLayer.clips){
									current.startTime = (current.startTime-a)*rate+c;
									current.endTime = (current.endTime-a)*rate+c;
									current.refreshInputs();
								}
								currentLayer.clearCache();
								popupFrame.dispose();
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
						closeButton.addMouseListener(new MouseListener() {

							@Override
							public void mouseClicked(MouseEvent arg0) {
								popupFrame.dispose();
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
						popupPanel.removeAll();
						GridBagConstraints constraint = new GridBagConstraints();
						constraint.weightx=1;
						constraint.weighty=1;
						constraint.gridx=0;
						constraint.gridy=0;
						constraint.gridwidth=3;
						popupPanel.add(upperLabel,constraint);
						constraint.gridy=2;
						popupPanel.add(lowerLabel,constraint);
						constraint.gridx=0;
						constraint.gridy=4;
						constraint.gridwidth=1;
						popupPanel.add(layerLabel,constraint);
						constraint.gridx=1;
						popupPanel.add(confirmButton,constraint);
						constraint.gridx=2;
						popupPanel.add(closeButton,constraint);
						constraint.gridx=0;
						constraint.gridy=1;
						popupPanel.add(oldStart,constraint);
						constraint.gridx=1;
						popupPanel.add(oldLabel,constraint);
						constraint.gridx=2;
						popupPanel.add(oldEnd,constraint);
						constraint.gridx=0;
						constraint.gridy=3;
						popupPanel.add(newStart,constraint);
						constraint.gridx=1;
						popupPanel.add(newLabel,constraint);
						constraint.gridx=2;
						popupPanel.add(newEnd,constraint);
						popupFrame.pack();
						popupFrame.setVisible(true);
					}
				}
			}
		});
		submenuItems.get(2).get(2).addActionListener(new ActionListener() {//Input mapping
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(composition.layers.containsKey(composition.layerSelection)){
					Layer currentLayer = composition.layers.get(composition.layerSelection);
					if(currentLayer.clipCount>0){
						clearPopupWindowListeners();
						popupFrame.setTitle("Wavelets [Layer input mapping]");
						popupFrame.addWindowListener(wlHide);
						JLabel inputLabel = new JLabel("If input");
						JComboBox<String> finderSelector = new JComboBox<String>(stringFindersKeysArray);
						JTextField searchField = new JTextField(60);
						JLabel upperLabel = new JLabel("Map range");
						JTextField oldStart = new JTextField(30);
						oldStart.setText("0.0");
						JLabel oldLabel = new JLabel("to");
						JTextField oldEnd = new JTextField(30);
						oldEnd.setText("1.0");
						JLabel lowerLabel = new JLabel("to range");
						JTextField newStart = new JTextField(30);
						newStart.setText("0.0");
						JLabel newLabel = new JLabel("to");
						JTextField newEnd = new JTextField(30);
						newEnd.setText("1.0");
						JLabel layerLabel = new JLabel("in layer \""+currentLayer.name+"\"");
						JButton confirmButton = new JButton("Apply");
						JButton closeButton = new JButton("Close");
						confirmButton.addMouseListener(new MouseListener() {

							@Override
							public void mouseClicked(MouseEvent arg0) {
								StringFinder finder = stringFinders.get(finderSelector.getSelectedItem());
								String search = searchField.getText();
								double a = WaveUtils.readDoubleFromField(oldStart, 0d);
								double b = WaveUtils.readDoubleFromField(oldEnd, 1d);
								double c = WaveUtils.readDoubleFromField(newStart, a);
								double d = WaveUtils.readDoubleFromField(newEnd, c+b-a);
								double rate = (d-c)/(b-a);
								for(Clip current:currentLayer.clips){
									for(String inputName:current.inputs.keySet()){
										if(finder.match(inputName,search)){
											current.inputs.put(inputName, (current.inputs.get(inputName)-a)*rate+c);
										}
									}
									current.refreshInputs();
								}
								currentLayer.clearCache();
								popupFrame.dispose();
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
						closeButton.addMouseListener(new MouseListener() {

							@Override
							public void mouseClicked(MouseEvent arg0) {
								popupFrame.dispose();
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
						popupPanel.removeAll();
						GridBagConstraints constraint = new GridBagConstraints();
						constraint.weightx=1;
						constraint.weighty=1;
						constraint.gridx=0;
						constraint.gridy=0;
						constraint.gridwidth=3;
						popupPanel.add(inputLabel,constraint);
						constraint.gridy=2;
						popupPanel.add(upperLabel,constraint);
						constraint.gridy=4;
						popupPanel.add(lowerLabel,constraint);
						constraint.gridx=0;
						constraint.gridy=6;
						constraint.gridwidth=1;
						popupPanel.add(layerLabel,constraint);
						constraint.gridx=1;
						popupPanel.add(confirmButton,constraint);
						constraint.gridx=2;
						popupPanel.add(closeButton,constraint);
						constraint.gridx=0;
						constraint.gridy=3;
						popupPanel.add(oldStart,constraint);
						constraint.gridx=1;
						popupPanel.add(oldLabel,constraint);
						constraint.gridx=2;
						popupPanel.add(oldEnd,constraint);
						constraint.gridx=0;
						constraint.gridy=5;
						popupPanel.add(newStart,constraint);
						constraint.gridx=1;
						popupPanel.add(newLabel,constraint);
						constraint.gridx=2;
						popupPanel.add(newEnd,constraint);
						constraint.gridx=0;
						constraint.gridy=1;
						popupPanel.add(finderSelector,constraint);
						constraint.gridx=1;
						constraint.gridwidth=2;
						popupPanel.add(searchField,constraint);
						popupFrame.pack();
						popupFrame.setVisible(true);
					}
				}
			}
		});
		submenuItems.get(2).get(3).addActionListener(new ActionListener() {//Merge
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String currentName = composition.layerSelection;
				if(composition.layers.containsKey(currentName)){
					Layer currentLayer = composition.layers.get(currentName);
					if(currentLayer.clipCount>0){
						clearPopupWindowListeners();
						popupFrame.setTitle("Wavelets [Layer merge]");
						popupFrame.addWindowListener(wlHide);
						JLabel infoLabel = new JLabel("Move clips from layer \""+currentLayer.name+"\" to");
						JComboBox<String> targetSelector = new JComboBox<String>(composition.layersKeysArray);
						targetSelector.removeItem(currentLayer.name);
						JButton confirmButton = new JButton("Apply");
						JButton closeButton = new JButton("Close");
						confirmButton.addMouseListener(new MouseListener() {

							@Override
							public void mouseClicked(MouseEvent arg0) {
								Layer targetLayer = composition.layers.get(targetSelector.getSelectedItem());
								for(Clip original:currentLayer.clips){
									targetLayer.addClip(original);
								}
								composition.removeLayerOnly(currentName);
								targetLayer.clearCache();
								popupFrame.dispose();
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
						closeButton.addMouseListener(new MouseListener() {

							@Override
							public void mouseClicked(MouseEvent arg0) {
								popupFrame.dispose();
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
						popupPanel.removeAll();
						GridBagConstraints constraint = new GridBagConstraints();
						constraint.weightx=1;
						constraint.weighty=1;
						constraint.gridx=0;
						constraint.gridy=0;
						popupPanel.add(infoLabel,constraint);
						constraint.gridx=1;
						popupPanel.add(targetSelector,constraint);
						constraint.gridy=1;
						popupPanel.add(closeButton,constraint);
						constraint.gridx=0;
						popupPanel.add(confirmButton,constraint);
						popupFrame.pack();
						popupFrame.setVisible(true);
					}
				}
			}
		});
		submenuItems.get(2).get(4).addActionListener(new ActionListener() {//Sort
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String currentName = composition.layerSelection;
				if(composition.layers.containsKey(currentName)){
					Layer currentLayer = composition.layers.get(currentName);
					if(currentLayer.clipCount>0){
						clearPopupWindowListeners();
						popupFrame.setTitle("Wavelets [Layer sort]");
						popupFrame.addWindowListener(wlHide);
						JLabel infoLabel = new JLabel("Sort clips in layer \""+currentLayer.name+"\" by");
						JComboBox<String> compSelector = new JComboBox<String>(clipComparatorsKeysArray);
						JCheckBox invertCheck = new JCheckBox("Reverse order");
						JButton confirmButton = new JButton("Apply");
						JButton closeButton = new JButton("Close");
						confirmButton.addMouseListener(new MouseListener() {

							@Override
							public void mouseClicked(MouseEvent arg0) {
								Comparator<Clip> clipComparator = clipComparators.get(compSelector.getSelectedItem());
								if(invertCheck.isSelected()){
									clipComparator = Collections.reverseOrder(clipComparator);
								}
								Collections.sort(currentLayer.clips,clipComparator);
								currentLayer.updateClipSelection();
								popupFrame.dispose();
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
						closeButton.addMouseListener(new MouseListener() {

							@Override
							public void mouseClicked(MouseEvent arg0) {
								popupFrame.dispose();
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
						popupPanel.removeAll();
						GridBagConstraints constraint = new GridBagConstraints();
						constraint.weightx=1;
						constraint.weighty=1;
						constraint.gridx=0;
						constraint.gridy=0;
						constraint.gridwidth=2;
						popupPanel.add(infoLabel,constraint);
						constraint.gridwidth=1;
						constraint.gridx=2;
						popupPanel.add(compSelector,constraint);
						constraint.gridy=1;
						popupPanel.add(closeButton,constraint);
						constraint.gridx=1;
						popupPanel.add(confirmButton,constraint);
						constraint.gridx=0;
						popupPanel.add(invertCheck,constraint);
						popupFrame.pack();
						popupFrame.setVisible(true);
					}
				}
			}
		});
		submenuItems.get(2).get(5).addActionListener(new ActionListener() {//Time shift
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String currentName = composition.layerSelection;
				if(composition.layers.containsKey(currentName)){
					Layer currentLayer = composition.layers.get(currentName);
					if(currentLayer.clipCount>0){
						clearPopupWindowListeners();
						popupFrame.setTitle("Wavelets [Layer time shift]");
						popupFrame.addWindowListener(wlHide);
						JLabel infoLabel = new JLabel("Shift time of clips in layer \""+currentLayer.name+"\" using method:");
						JComboBox<String> compSelector = new JComboBox<String>(Layer.timeManipulatorsKeysArray);
						JTextField inputField = new JTextField(30);
						JButton confirmButton = new JButton("Apply");
						JButton closeButton = new JButton("Close");
						confirmButton.addMouseListener(new MouseListener() {

							@Override
							public void mouseClicked(MouseEvent arg0) {
								double setting = WaveUtils.readDoubleFromField(inputField, 0d);
								Layer.TimeManipulator tm = Layer.timeManipulators.get(compSelector.getSelectedItem());
								tm.applyTo(currentLayer, setting);
								currentLayer.clearCache();
								popupFrame.dispose();
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
						closeButton.addMouseListener(new MouseListener() {

							@Override
							public void mouseClicked(MouseEvent arg0) {
								popupFrame.dispose();
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
						popupPanel.removeAll();
						GridBagConstraints constraint = new GridBagConstraints();
						constraint.weightx=1;
						constraint.weighty=1;
						constraint.gridx=0;
						constraint.gridy=0;
						constraint.gridwidth=2;
						popupPanel.add(infoLabel,constraint);
						constraint.gridy=1;
						constraint.gridwidth=1;
						popupPanel.add(compSelector,constraint);
						constraint.gridx=1;
						popupPanel.add(inputField,constraint);
						constraint.gridx=0;
						constraint.gridy=2;
						popupPanel.add(confirmButton,constraint);
						constraint.gridx=1;
						popupPanel.add(closeButton,constraint);
						popupFrame.pack();
						popupFrame.setVisible(true);
					}
				}
			}
		});
		submenuItems.get(2).get(6).addActionListener(new ActionListener() {//Duplicate
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String currentName = composition.layerSelection;
				if(composition.layers.containsKey(currentName)){
					Layer currentLayer = composition.layers.get(currentName);
					if(currentLayer.clipCount>0){
						clearPopupWindowListeners();
						popupFrame.setTitle("Wavelets [Layer duplicate]");
						popupFrame.addWindowListener(wlHide);
						JLabel infoLabel = new JLabel("Create duplicate of layer \""+currentLayer.name+"\" with name");
						JTextField inputField = new JTextField(30);
						JButton confirmButton = new JButton("Apply");
						JButton closeButton = new JButton("Close");
						confirmButton.addMouseListener(new MouseListener() {

							@Override
							public void mouseClicked(MouseEvent arg0) {
								String newName = inputField.getText();
								if(!composition.layers.containsKey(newName)){
									Layer toAdd = new Layer();
									toAdd.setName(newName);
									toAdd.parentComposition = composition;
									for(Clip current:currentLayer.clips){
										toAdd.dupliClip(current);
									}
									//TODO copy filters
									composition.addLayer(toAdd);
								}
								popupFrame.dispose();
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
						closeButton.addMouseListener(new MouseListener() {

							@Override
							public void mouseClicked(MouseEvent arg0) {
								popupFrame.dispose();
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
						popupPanel.removeAll();
						GridBagConstraints constraint = new GridBagConstraints();
						constraint.weightx=1;
						constraint.weighty=1;
						constraint.gridx=0;
						constraint.gridy=0;
						popupPanel.add(infoLabel,constraint);
						constraint.gridx=1;
						popupPanel.add(inputField,constraint);
						constraint.gridy=1;
						popupPanel.add(closeButton,constraint);
						constraint.gridx=0;
						popupPanel.add(confirmButton,constraint);
						popupFrame.pack();
						popupFrame.setVisible(true);
					}
				}
			}
		});
		submenuItems.get(2).get(7).addActionListener(new ActionListener() {//Round
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String currentName = composition.layerSelection;
				if(composition.layers.containsKey(currentName)){
					Layer currentLayer = composition.layers.get(currentName);
					if(currentLayer.clipCount>0){
						clearPopupWindowListeners();
						popupFrame.setTitle("Wavelets [Layer round]");
						popupFrame.addWindowListener(wlHide);
						JLabel infoLabel = new JLabel("Round values of clips in layer \""+currentLayer.name+"\" with scale");
						JTextField inputField = new JTextField(30);
						inputField.setText("60");
						JCheckBox inputCheck = new JCheckBox("Round inputs");
						JButton confirmButton = new JButton("Apply");
						JButton closeButton = new JButton("Close");
						confirmButton.addMouseListener(new MouseListener() {

							@Override
							public void mouseClicked(MouseEvent arg0) {
								double scale = WaveUtils.correctRound(WaveUtils.readDoubleFromField(inputField, 21600d));
								boolean roundInputs = inputCheck.isSelected();
								for(Clip current:currentLayer.clips){
									current.startTime = WaveUtils.correctRound(current.startTime,scale);
									current.endTime = WaveUtils.correctRound(current.endTime,scale);
									if(roundInputs){
										for(String key:current.inputs.keySet()){
											current.inputs.put(key, WaveUtils.correctRound(current.inputs.get(key)));
										}
									}
								}
								popupFrame.dispose();
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
						closeButton.addMouseListener(new MouseListener() {

							@Override
							public void mouseClicked(MouseEvent arg0) {
								popupFrame.dispose();
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
						popupPanel.removeAll();
						GridBagConstraints constraint = new GridBagConstraints();
						constraint.weightx=1;
						constraint.weighty=1;
						constraint.gridx=0;
						constraint.gridy=0;
						popupPanel.add(infoLabel,constraint);
						constraint.gridx=1;
						constraint.gridwidth=2;
						popupPanel.add(inputField,constraint);
						constraint.gridx=0;
						constraint.gridy=1;
						constraint.gridwidth=1;
						popupPanel.add(inputCheck,constraint);
						constraint.gridx=1;
						popupPanel.add(confirmButton,constraint);
						constraint.gridx=2;
						popupPanel.add(closeButton,constraint);
						popupFrame.pack();
						popupFrame.setVisible(true);
					}
				}
			}
		});
	}
	
	//Adds a menu
	public static void addMenu(JMenu currentMenu,ArrayList<JMenuItem> currentMenuItems){
		for(JMenuItem currentMenuItem : currentMenuItems){
			currentMenu.add(currentMenuItem);
		}
		//menuBar.add(currentMenu);
	}
	
	//Update combo box for node selector
	public static void updateNodeEditorSelector(){
		nodeEditorSelectorChanging = true;
		nodeEditorSelector.removeAllItems();
		composition.updateNodes();
		for(String name:composition.nodesKeysArray){
			nodeEditorSelector.addItem(name);
		}
		nodeEditorSelectorChanging = false;
	}
	
	//Update node selection
	public static void updateNodeSelection(){
		while(nodeEditorInPanel.getComponentCount()>1){//Get rid of the other panels
			nodeEditorInPanel.remove(1);
		}
		if(composition.nodes.size()==1){
			composition.nodesSelection = composition.nodes.keySet().iterator().next();
		}
		if(composition.nodes.containsKey(composition.nodesSelection)){
			Nodes currentNodes = composition.nodes.get(composition.nodesSelection);
			currentNodes.scriptArea.setText(currentNodes.source);
			currentNodes.refreshTestClip();
			nodeEditorInPanel.add(currentNodes.scriptPanel,BorderLayout.LINE_START);
			nodeEditorInPanel.add(currentNodes.viewPanel,BorderLayout.CENTER);
			nodeEditorInPanel.add(currentNodes.testPanel,BorderLayout.LINE_END);
			nodeEditorInPanel.add(currentNodes.graphPanel,BorderLayout.PAGE_END);
			nodeEditorName.setText(composition.nodesSelection);
			nodeEditorDupli.setEnabled(true);
			//Temporary solution
			currentNodes.graphPanel.setPreferredSize(new Dimension(mainFrame.getWidth()-50,200));
		}else{
			nodeEditorName.setText("None selected");
			nodeEditorDupli.setEnabled(false);
		}
		//Update the combo box
		if(composition.nodesKeysArray!=nodesCache){
			updateNodeEditorSelector();
			nodesCache = composition.nodesKeysArray;
		}
		//Update the display
		updateDisplay();
	}
	
	//Update combo box for curve selector
	public static void updateCurveEditorSelector(){
		curveEditorSelectorChanging = true;
		curveEditorSelector.removeAllItems();
		composition.updateCurves();
		for(String name:composition.curvesKeysArray){
			curveEditorSelector.addItem(name);
		}
		curveEditorSelectorChanging = false;
	}
	
	//Update curve selection
	public static void updateCurveSelection(){
		while(curveEditorInPanel.getComponentCount()>1){//Get rid of the other panels
			curveEditorInPanel.remove(1);
		}
		if(composition.curves.size()==1){
			composition.curveSelection = composition.curves.keySet().iterator().next();
		}
		if(composition.curves.containsKey(composition.curveSelection)){
			Curve currentCurve = composition.curves.get(composition.curveSelection);
			curveEditorInPanel.add(currentCurve.viewPanel,BorderLayout.CENTER);
			curveEditorInPanel.add(currentCurve.editPanel, BorderLayout.PAGE_END);
			curveEditorName.setText(composition.curveSelection);
			if(composition.curves.get(composition.curveSelection).getSize()>0){
				curveEditorPreviewPlay.setEnabled(true);
			}else{
				curveEditorPreviewPlay.setEnabled(false);
			}
			curveEditorDupli.setEnabled(true);
		}else{
			curveEditorName.setText("None selected");
			curveEditorPreviewPlay.setEnabled(false);
			curveEditorDupli.setEnabled(false);
		}
		//Update the combo box
		if(composition.curvesKeysArray!=curvesCache){
			updateCurveEditorSelector();
			curvesCache = composition.curvesKeysArray;
		}
		//Update the display
		updateDisplay();
	}
	
	//Clear window listeners on popup window
	public static void clearPopupWindowListeners(){
		WindowListener[] listeners = popupFrame.getWindowListeners();
		for(WindowListener current:listeners){
			popupFrame.removeWindowListener(current);
		}
	}
	
	//Attempt to parse command line argument
	public static void parseCmdArg(String arg){
		String[] parts = arg.split(";");
		switch(parts[0]){
		case "-scale":{
			try{
				windowX=Double.valueOf(parts[1]);
				windowY=Double.valueOf(parts[2]);
			}catch(Exception e){
				e.printStackTrace();
			}
			break;
		}
		}
	}
	
	public static void main(String[] args){
		working = true;
		mainThread.setName("Wavelets - Main");
		initPlayer();
		//Run through arguments
		if(args.length>0){
			//Expected file is first argument
			String firstArg = args[0];
			if(firstArg.startsWith("-")){
				parseCmdArg(firstArg);
			}else{
				fileName = firstArg;
				fileNamed = true;
			}
			for(int i=1;i<args.length;i++){
				String arg = args[i];
				parseCmdArg(arg);
			}
		}
		//Set up the window
		mainFrame = new JFrame("Wavelets (\""+Arrays.toString(args)+"\")");
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowListener() {

			@Override
			public void windowActivated(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				if(!working){
					System.exit(0);
				}
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowIconified(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowOpened(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		popupFrame = new JFrame("Wavelets [Secondary]");
		popupPanel = new JPanel(new GridBagLayout());
		popupFrame.add(popupPanel);
		popupFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		//Load
		initComposition();
		//Initialize data classes
		Layer.init(composition);
		//Continue setup
		initMenus();
		initPanels();
		enableComposer();
		//Initialize utility static class
		WaveUtils.init();
		//Set default font
		setUIFont(new javax.swing.plaf.FontUIResource("Arial Unicode MS",Font.PLAIN,14));
		//Finish composition setup
		initCompositionSecond();
		//Finish setting up the window
		mainFrame.pack();
		mainFrame.setVisible(true);
		//Have this at the end
		working = false;
	}
}
