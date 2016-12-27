package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import components.*;

//The main program
public class Wavelets{
	
	//The main frame
	public static JFrame mainFrame;
	//The top menu bar
	public static JMenuBar menuBar = new JMenuBar();
	//Menus
	public static ArrayList<JMenu> menus = new ArrayList<JMenu>();
	//Menu items
	public static ArrayList<ArrayList<JButton>> menuItems = new ArrayList<ArrayList<JButton>>();
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
	
	//Place double as text in field
	public static void placeDoubleTextInField(JTextField inField,int length,double toPlace){
		char[] valArray = Double.toString(toPlace).toCharArray();
		inField.setText(new String(Arrays.copyOf(valArray,Math.min(valArray.length, length))));
	}
	
	//Update the display
	public static void updateDisplay(){
		//Update components
		SwingUtilities.updateComponentTreeUI(mainFrame);
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
		JPanel firstLayerPanel = composition.addLayer();
		firstLayerPanel.setPreferredSize(new Dimension(280,60));//TODO Temporary
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
			//TODO load file
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
			addNewLayer();
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
		composerTopScrollPane.setPreferredSize(new Dimension(1800,60));//TODO Temporary
		composerBottomPanel = new JPanel(new BorderLayout());
		composerLeftInPanel = new JPanel();
		composerLeftInPanel.setLayout(new BoxLayout(composerLeftInPanel, BoxLayout.PAGE_AXIS));
		composerLeftScrollPane = new JScrollPane(composerLeftInPanel);
		composerLeftScrollPane.setPreferredSize(new Dimension(300,800));//TODO Temporary
		composerRightScrollPane = new JScrollPane(composerRightInPanel);
		composerCenterPanel = new JPanel(new BorderLayout());
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
		composerTopPanelComponents.get(1).add(new JButton("Cache samples"));
		composerTopPanelComponents.get(1).add(new JButton("Clear cache"));
		composerTopPanelComponents.add(new ArrayList<JComponent>());
		composerTopPanelComponents.get(2).add(new JLabel("Layer"));
		composerTopPanelComponents.get(2).add(new JButton("Play"));
		composerTopPanelComponents.get(2).add(new JButton("Cache samples"));
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
		composerLeftPanelAddButton = new JButton("+");
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
		//TODO set 100 range as "as envelope"
		Curve.previewModes.put("As frequency-amplitude graph: 220Hz filtered saw (increment 1, base 1, exponent 1)", 200);
		Curve.previewModes.put("As frequency-amplitude graph: 220Hz filtered square (increment 2, base 1, exponent 1)", 201);
		Curve.previewModes.put("As frequency-amplitude graph: 220Hz filtered triangle (increment 2, base -1, exponent 2)", 202);
		//Curve.previewModes.put("As frequency-amplitude graph: 220Hz filtered inharmonic (increment 50/49, base 1, exponent 1)", 203);
		//Curve.previewModes.put("As frequency-amplitude graph: 220Hz filtered inharmonic (increment 22/21, base 1, exponent 1)", 204);
		//Curve.previewModes.put("As frequency-amplitude graph: 220Hz filtered inharmonic (increment 18/17, base 1, exponent 1)", 205);
		//Curve.previewModes.put("As frequency-amplitude graph: 220Hz filtered inharmonic (increment 14/13, base 1, exponent 1)", 206);
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
						short[] shortArray = Waveform.quickShort(doubleArray);
						mainPlayer.playSound(shortArray);
						break;
					}case 1:{
						double rate = 440d/composition.samplesPerSecond;
						double[] doubleArray = new double[32768];
						for(int i=0;i<32768;i++){
							doubleArray[i] = selectedCurve.valueAtPos((rate*i)%1);
						}
						short[] shortArray = Waveform.quickShort(doubleArray);
						mainPlayer.playSound(shortArray);
						break;
					}case 2:{
						double rate = 880d/composition.samplesPerSecond;
						double[] doubleArray = new double[32768];
						for(int i=0;i<32768;i++){
							doubleArray[i] = selectedCurve.valueAtPos((rate*i)%1);
						}
						short[] shortArray = Waveform.quickShort(doubleArray);
						mainPlayer.playSound(shortArray);
						break;
					}case 200:{
						double[] doubleArray = new double[32768];
						for(int i=0;i<32768;i++){
							doubleArray[i] = Waveform.harmonic(220d, 1d, 1d, 1d, 1d, 100d, 20000d, i/44100d, selectedCurve);
						}
						short[] shortArray = Waveform.quickShort(doubleArray);
						mainPlayer.playSound(shortArray);
						break;
					}case 201:{
						double[] doubleArray = new double[32768];
						for(int i=0;i<32768;i++){
							doubleArray[i] = Waveform.harmonic(220d, 2d, 1d, 1d, 1d, 100d, 20000d, i/44100d, selectedCurve);
						}
						short[] shortArray = Waveform.quickShort(doubleArray);
						mainPlayer.playSound(shortArray);
						break;
					}case 202:{
						double[] doubleArray = new double[32768];
						for(int i=0;i<32768;i++){
							doubleArray[i] = Waveform.harmonic(220d, 2d, 1d, -1d, 2d, 100d, 20000d, i/44100d, selectedCurve);
						}
						short[] shortArray = Waveform.quickShort(doubleArray);
						mainPlayer.playSound(shortArray);
						break;
					}/*case 203:{
						short[] soundArray = new short[32768];
						for(int i=0;i<32768;i++){
							soundArray[i] = (short) (16384*Waveform.harmonic(220d, 50d/49d, 1d, 1d, 1d, 100d, 20000d, i/44100d, selectedCurve));
						}
						mainPlayer.playSound(soundArray);
						break;
					}case 204:{
						short[] soundArray = new short[32768];
						for(int i=0;i<32768;i++){
							soundArray[i] = (short) (16384*Waveform.harmonic(220d, 22d/21d, 1d, 1d, 1d, 100d, 20000d, i/44100d, selectedCurve));
						}
						mainPlayer.playSound(soundArray);
						break;
					}case 205:{
						short[] soundArray = new short[32768];
						for(int i=0;i<32768;i++){
							soundArray[i] = (short) (16384*Waveform.harmonic(220d, 18d/17d, 1d, 1d, 1d, 100d, 20000d, i/44100d, selectedCurve));
						}
						mainPlayer.playSound(soundArray);
						break;
					}case 206:{
						short[] soundArray = new short[32768];
						for(int i=0;i<32768;i++){
							soundArray[i] = (short) (16384*Waveform.harmonic(220d, 14d/13d, 1d, 1d, 1d, 100d, 20000d, i/44100d, selectedCurve));
						}
						mainPlayer.playSound(soundArray);
						break;
					}*/
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
		//Add menu bar
		menus.add(new JMenu("File"));
		menuItems.add(new ArrayList<JButton>());
		menuItems.get(0).add(new JButton("Save"));
		menuItems.get(0).add(new JButton("Save As"));
		menuItems.get(0).add(new JButton("Open"));
		menuItems.get(0).add(new JButton("Export"));
		addMenu(menus.get(0),menuItems.get(0));
		menus.add(new JMenu("Window"));
		menuItems.add(new ArrayList<JButton>());
		menuItems.get(1).add(new JButton("Composer"));
		menuItems.get(1).add(new JButton("Curve Editor"));
		menuItems.get(1).add(new JButton("Node Editor"));
		addMenu(menus.get(1),menuItems.get(1));
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
		menuItems.get(0).get(0).addMouseListener(new MouseListener() {

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
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		menuItems.get(0).get(1).addMouseListener(new MouseListener() {

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
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		menuItems.get(0).get(2).addMouseListener(new MouseListener() {

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
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		menuItems.get(0).get(3).addMouseListener(new MouseListener() {

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
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		menuItems.get(1).get(0).addMouseListener(new MouseListener() {//Composer

			@Override
			public void mouseClicked(MouseEvent arg0) {
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
		menuItems.get(1).get(1).addMouseListener(new MouseListener() {//Curve Editor

			@Override
			public void mouseClicked(MouseEvent arg0) {
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
		menuItems.get(1).get(2).addMouseListener(new MouseListener() {//Node Editor

			@Override
			public void mouseClicked(MouseEvent arg0) {
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
	
	//Adds a menu
	public static void addMenu(JMenu currentMenu,ArrayList<JButton> currentMenuItems){
		for(JButton currentMenuItem : currentMenuItems){
			currentMenu.add(currentMenuItem);
		}
		menuBar.add(currentMenu);
	}
	
	//Update combo box for node selector
	public static void updateNodeEditorSelector(){
		nodeEditorSelectorChanging = true;
		nodeEditorSelector.removeAllItems();
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
			currentNodes.graphPanel.setPreferredSize(new Dimension(mainFrame.getWidth(),200));
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
	
	public static void main(String[] args){
		working = true;
		mainThread.setName("Wavelets - Main");
		initPlayer();
		//Run through arguments
		//String skinFileName = "DefaultSkin.xml";
		for(String cString : args){
			String[] parts = cString.split(":");
			switch(parts[0]){
			case "scale":{
				try{
					windowX=Double.valueOf(parts[1]);
					windowY=Double.valueOf(parts[2]);
				}catch(Exception e){
					
				}
			}case "load":{
				fileNamed = true;
				fileName = parts[1];
			}/*case "skin":{
				skinFileName = parts[1];
			}*/
			}
		}
		//Set up the window
		mainFrame = new JFrame("Wavelets");
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
		//Load
		initComposition();
		//Continue setup
		initMenus();
		initPanels();
		enableComposer();
		//Set Synth UI
		//initUISkin(skinFileName);
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
