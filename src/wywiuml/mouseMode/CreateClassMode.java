package wywiuml.mouseMode;

import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import wywiuml.gui.Canvas;
import wywiuml.shapes.ClassObject;

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
		Canvas canvas = Canvas.getInstance();
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (canvas.getShapeAt(e.getPoint()) != null)
				return;
			ClassObject tmp = new ClassObject(e.getX(), e.getY());
			canvas.addShape(tmp);
			canvas.repaint();
			System.out.println("Clicked class!");
		}
	}

}
