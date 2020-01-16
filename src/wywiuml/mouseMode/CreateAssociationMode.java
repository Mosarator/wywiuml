package wywiuml.mouseMode;

import java.awt.Point;
import java.awt.event.MouseEvent;

import wywiuml.gui.Canvas;
import wywiuml.shapes.Association;
import wywiuml.shapes.Generalization;
import wywiuml.shapes.Shape;
import wywiuml.shapes.Shape.ShapeType;


public class CreateAssociationMode extends MouseMode {

	private Point startP;
	private Point lastPos;
	private Canvas canvas;
	private Association currentLine;
	
	
	public CreateAssociationMode() {
		canvas = Canvas.getInstance();
		modeName = "CreateAssociationMode";
		description = "Assoziation";
	}
	
	
	@Override
	public void mousePressed(MouseEvent e) {
		System.out.println("pressed");
		startP = e.getPoint();
		Shape obj = canvas.getShapeAt(e.getPoint());
		if (obj == null)
			return;
		if(obj.getShapeType()!=ShapeType.CLASS)
			return;
		lastPos = startP;
		currentLine = new Association(false, startP, startP);
		canvas.addShape(currentLine);
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if (currentLine == null)
			return;
		int diffX = e.getPoint().x - lastPos.x;
		int diffY = e.getPoint().y - lastPos.y;
		currentLine.move(diffX, diffY);
		canvas.repaint();
		lastPos = e.getPoint();
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		Shape from = canvas.getShapeAt(startP);
		Shape to = canvas.getShapeAt(e.getPoint());
		if (from == null || to == null || from == to) {
			// no valid connection
			canvas.removeObject(currentLine);
		} else {
			currentLine.complete(from, to);
		}
		currentLine = null;
		canvas.repaint();
	}
	
}
