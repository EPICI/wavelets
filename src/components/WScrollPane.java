package components;

import java.awt.Graphics;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import javax.swing.*;

public class WScrollPane extends JScrollPane{
	//Constructor
	public WScrollPane(JComponent component){
		super(component);
		//Update on scroll
		horizontalScrollBar.addAdjustmentListener(new AdjustmentListener(){
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				repaint();
			}
		});
		verticalScrollBar.addAdjustmentListener(new AdjustmentListener(){
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				repaint();
			}
		});
	}
	
	//Custom painting
	@Override
	public void paintComponent(Graphics g){
		//Paint inner JPanel
		super.paintComponent(g);
	}
	
	public static void main(String[] args){
		//Leave empty
	}
}
