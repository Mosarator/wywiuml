package wywiuml.shapes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JPopupMenu;

import wywiuml.gui.Canvas;
import wywiuml.shapes.Line.LineSegment;
import wywiuml.shapes.Line.Vector2D;
import wywiuml.shapes.Shape.ShapeType;

public class Association extends Line{
	private boolean isCompleted;
	private boolean isCorrect;
	private static final Color CLR_LINE = Color.BLACK;
	private static final Color CLR_ERROR = Color.RED;

	private static final int ARROWWIDTH = 10;
	private static final int ARROWHEIGHT = 10;
	private Point startP, endP;
	private AnchorPoint start, end;
	
	public Association() {
		this(false);
	}
	
	public Association(boolean completed) {
		this(completed,new Point(0,0),new Point(0,0));
	}
	
	public Association(boolean completed, Point startPoint, Point endPoint) {
		startP = startPoint.getLocation();
		endP = endPoint.getLocation();
		isCompleted = completed;
		if (completed) {
			complete(Canvas.getInstance().getShapeAt(startPoint), Canvas.getInstance().getShapeAt(endPoint));
		} else {
			// Default berhaviour;
			shapetype = ShapeType.GENERALIZATION;
		}
	}
	
	public boolean complete(Shape fromShape, Shape toShape) {
		if (fromShape == null || toShape == null || fromShape.shapetype != ShapeType.CLASS
				|| toShape.shapetype != ShapeType.CLASS)
			return false; 

		ClassObject from = (ClassObject) fromShape;
		ClassObject to = (ClassObject) toShape;

		isCompleted = true;
		start = new AnchorPoint(startP);
		end = new AnchorPoint(endP);

		start.setConnectedShape(from);
		start.setLine(this);
		end.setConnectedShape(to);
		end.setLine(this);

		segments = new ArrayList<Line.LineSegment>();
		segments.add(new LineSegment(start, end));
		from.addAnchor(start);
		to.addAnchor(end);
		isCorrect = true;
		if (from.isInterface() == to.isInterface()) {
			shapetype = ShapeType.GENERALIZATION;
		} else {
			if (to.isInterface()) {
				shapetype = ShapeType.REALIZATION;
			} else {
				shapetype = ShapeType.GENERALIZATION;
				isCorrect = false;
			}
		}
		return true;
	}

	@Override
	public boolean isInside(Point p) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public JPopupMenu getPopupMenu() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void draw(Graphics g) {

		if (isCorrect) {
			g.setColor(CLR_LINE);
		} else {
			g.setColor(CLR_ERROR);
		}

		Graphics2D g2d = (Graphics2D) g;
		switch (shapetype) {
		case GENERALIZATION:
			g2d.setStroke(BASICLINE);
			break;
		case REALIZATION:
			g2d.setStroke(DASHEDLINE);
			break;
		default:
			g2d.setStroke(BASICLINE);
			break;
		}

		if (!isCompleted) {
			g.drawLine(startP.x, startP.y, endP.x, endP.y);
		} else {
			drawLinePath(g);
		}
		// Draw ArrowHead
		// Senkrechte bestimmen:
		int dx = endP.x - startP.x;
		int dy = endP.y - startP.y;
		double m1 = ((double) dy) / dx; // m entspricht auch dem tangens
		double m2 = -1 * (1 / m1);
		int h = ARROWHEIGHT;
		int w = ARROWWIDTH;

		Vector2D direction = new Vector2D(dx, dy).normalized();
		Vector2D perpendicular = new Vector2D(1, m2).normalized();
		if (direction.y == 0) {
			perpendicular = new Vector2D(0, direction.x * -1);
		}
		Vector2D endpoint = new Vector2D(endP.x, endP.y);
		Vector2D leftPoint = endpoint.add(direction.scale(-1 * h)).add(perpendicular.scale(-1 * w));
		Vector2D rightPoint = endpoint.add(direction.scale(-1 * h)).add(perpendicular.scale(1 * w));

		g.drawLine((int) endpoint.x, (int) endpoint.y, (int) leftPoint.x, (int) leftPoint.y);
		g.drawLine((int) endpoint.x, (int) endpoint.y, (int) rightPoint.x, (int) rightPoint.y);

	}

	@Override
	public void move(int moveX, int moveY) {
		endP = new Point(endP.x + moveX, endP.y + moveY);
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(boolean recursive) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SaveState getSaveState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean readSaveState(Serializable saveState) {
		// TODO Auto-generated method stub
		return false;
	}

}
