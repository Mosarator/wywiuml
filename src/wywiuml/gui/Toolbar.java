package wywiuml.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import wywiuml.mouseMode.CancelMode;
import wywiuml.mouseMode.CreateAssociationMode;
import wywiuml.mouseMode.CreateClassMode;
import wywiuml.mouseMode.CreateGeneralizationMode;
import wywiuml.mouseMode.MouseMode;
import wywiuml.mouseMode.SelectAndEditMode;

@SuppressWarnings("serial")
public class Toolbar extends JPanel {
	
	private ToolButton defaultButton;

	public Toolbar() {
		setLayout(new GridLayout(0, 1, 5, 5));
		setPreferredSize(new Dimension(110, 50));
		setBackground(Color.BLACK);
		
		ToolButton selectButton = new ToolButton(new ImageIcon("img/select.png"),SelectAndEditMode.getInstance());
		add(selectButton);
		
		ToolButton createClassButton = new ToolButton(new ImageIcon("img/class.png"),CreateClassMode.getInstance());
		add(createClassButton);
		
		ToolButton generalizationButton = new ToolButton(new ImageIcon("img/general.png"),new CreateGeneralizationMode());
		add(generalizationButton);
		//this.add(new ToolButton(new ImageIcon("img/select.png"),new SelectAndEditMode()));
		
		ToolButton associationButton = new ToolButton(new ImageIcon("img/associate.png"), new CreateAssociationMode());
		add(associationButton);
		
		defaultButton = selectButton;
		CancelMode.setToolbar(this);
	}
	
	public void resetMode() {
		defaultButton.doClick();
	}

	private static class ToolButton extends JButton {
		MouseMode mode = null;
		//TODO make buttons prettier
		private ToolButton(ImageIcon icon, MouseMode pMode) {
			mode = pMode;
			setIcon(icon);
			setBackground(Color.BLUE);
			setPreferredSize(new Dimension(20,20));
			Canvas canvas = Canvas.getInstance();
			
			
			addActionListener(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					canvas.setMouseMode(mode);
					canvas.cancelEditing();
					canvas.repaint();
				}
			});
		}
	}
}
