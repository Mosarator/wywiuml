package wywiuml.mouseMode;

import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import wywiuml.gui.Canvas;
import wywiuml.shapes.ClassObject;
import wywiuml.shapes.Shape;
import wywiuml.shapes.Shape.ShapeType;

public class CreateClassMode extends MouseMode {
	private static CreateClassMode instance;

	public static MouseMode getInstance() {
		if (instance == null)
			instance = new CreateClassMode();
		return instance;
	}

	private CreateClassMode() {
		super();
		this.modeName = "CreateClassMode";
		this.description = "Klasse Kreieren";

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
		Canvas canvas = Canvas.getInstance();
		if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
			Shape obj = canvas.getShapeAt(e.getPoint(),ShapeType.CLASS);
			if(obj == null)
				return;
			JPanel editWindow= ((ClassObject) obj).getEditWindow();
			if(editWindow == null)
				return;
			editWindow.setVisible(false);
			canvas.add(editWindow);
			editWindow.setVisible(true);
			editWindow.requestFocusInWindow();
			canvas.setIsEditing(true);
		}else if (SwingUtilities.isLeftMouseButton(e)) {
			if (canvas.getShapeAt(e.getPoint()) != null)
				return;
			ClassObject tmp = new ClassObject(e.getX(), e.getY());
			canvas.addShape(tmp);
			canvas.repaint();
		}
	}

}
