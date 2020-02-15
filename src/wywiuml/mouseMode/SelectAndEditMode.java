package wywiuml.mouseMode;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import wywiuml.gui.Canvas;
import wywiuml.shapes.AnchorPoint;
import wywiuml.shapes.Association;
import wywiuml.shapes.ClassObject;
import wywiuml.shapes.Shape;
import wywiuml.shapes.Shape.ShapeType;

public class SelectAndEditMode extends MouseMode {
	private static SelectAndEditMode instance;
	private Shape selectedShape;
	private Point startP;
	private Point lastP;

	public static SelectAndEditMode getInstance() {
		if (instance == null)
			instance = new SelectAndEditMode();
		return instance;
	}

	private SelectAndEditMode() {
		super();
		this.modeName = "SelectAndEditMode";
		this.description = "Bearbeiten und Verschieben";
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			startP = e.getPoint();
			lastP = e.getPoint();
			selectedShape = Canvas.getInstance().getShapeAt(e.getPoint());
			Canvas.getInstance().selected = selectedShape;

		} else if (SwingUtilities.isRightMouseButton(e)) {

		}

		Canvas.getInstance().repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			int diffx = e.getPoint().x - lastP.x;
			int diffy = e.getPoint().y - lastP.y;
			if (selectedShape != null) {
				switch (selectedShape.getShapeType()) {
				case CLASS:
					((ClassObject) selectedShape).move(diffx, diffy);
					break;
				case ANCHOR:
					((AnchorPoint) selectedShape).move(diffx, diffy, true);
					break;
				default:
					break;
				}
				lastP = e.getPoint();
			}
		}
		Canvas.getInstance().repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
		Canvas canvas = Canvas.getInstance();
		if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
			System.out.println("Double Click!");
			Shape obj = canvas.getShapeAt(e.getPoint());
			if(obj == null)
				return;
			JPanel editWindow= null;
			if (obj.getShapeType() == ShapeType.CLASS) {
				editWindow = ((ClassObject) obj).getEditWindow();
			}
			if(obj.getShapeType() == ShapeType.ASSOCIATON ||
					obj.getShapeType() == ShapeType.AGGREGATION ||
					obj.getShapeType() == ShapeType.COMPOSITION
					) {
				editWindow = ((Association) obj).getEditWindow();
			}
			if(editWindow == null)
				return;
			editWindow.setVisible(false);
			canvas.add(editWindow);
			editWindow.setVisible(true);
			editWindow.requestFocusInWindow();
			canvas.setIsEditing(true);
			
		}
		Canvas.getInstance().repaint();
	}

}
