package wywiuml.shapes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import wywiuml.gui.Canvas;
import wywiuml.shapes.Line.LineSegment;
import wywiuml.shapes.Line.Vector2D;
import wywiuml.shapes.Shape.ShapeType;

public class Association extends Line {
	
	private static final Color CLR_LINE = Color.BLACK;
	private static final Color CLR_ERROR = Color.RED;
	private static final int ARROWWIDTH = 10;
	private static final int ARROWHEIGHT = 10;
	private boolean isCompleted;
	private boolean isCorrect;
	private String variable;
	
	public Association() {
		this(false);
	}

	public Association(boolean completed) {
		this(completed, new Point(0, 0), new Point(0, 0));
	}

	public Association(boolean completed, Point start, Point end) {
		startP = start.getLocation();
		endP = end.getLocation();
		isCompleted = completed;
		if (completed) {
			boolean success = complete(Canvas.getInstance().getShapeAt(start, ShapeType.CLASS), Canvas.getInstance().getShapeAt(end, ShapeType.CLASS));
			if(success == false)
				shapetype = ShapeType.ASSOCIATON;
		} else {
			// Default behavior;
			shapetype = ShapeType.ASSOCIATON;
		}
	}

	public void setVariable(String umlinfo) {
		// TODO check for correctness
		isCorrect = false;
		variable = umlinfo;
	}
	
	public boolean complete(Shape fromShape, Shape toShape) {
		if (super.complete(fromShape, toShape) == false) {
			return false;
		}
		isCorrect = true;
		ClassObject from = (ClassObject) fromShape;
		ClassObject to = (ClassObject) toShape;

		/*if (from.isInterface() == to.isInterface()) {
			shapetype = ShapeType.GENERALIZATION;
		} else {
			if (to.isInterface()) {
				shapetype = ShapeType.REALIZATION;
			} else {
				shapetype = ShapeType.GENERALIZATION;
				isCorrect = false;
			}
		}*/
		//TODO
		shapetype = ShapeType.ASSOCIATON;
		return true;
	}

	@Override
	public JPopupMenu getPopupMenu() {
		return new Popup(this);
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
		case AGGREGATION:
		case COMPOSITION:
		case ASSOCIATON:
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
		Vector2D endpoint2 = endpoint.add(direction.scale(-2 * h));
		Vector2D leftPoint = endpoint.add(direction.scale(-1 * h)).add(perpendicular.scale(-1 * w));
		Vector2D rightPoint = endpoint.add(direction.scale(-1 * h)).add(perpendicular.scale(1 * w));

		Polygon poly = new Polygon();
		poly.xpoints = new int[]{(int) endpoint.x, (int) leftPoint.x, (int) endpoint2.x , (int) rightPoint.x};
		poly.ypoints = new int[]{(int) endpoint.y, (int) leftPoint.y, (int) endpoint2.y , (int) rightPoint.y};
		poly.npoints = 4;
		switch(shapetype) {
		case AGGREGATION:
			Color old = g.getColor();
			g.setColor(Color.WHITE);
			g.fillPolygon(poly);
			g.setColor(old);
			g.drawPolygon(poly);
			break;
		case COMPOSITION:
			g.fillPolygon(poly);
			break;
		case ASSOCIATON:
		default:
			g.drawLine((int) endpoint.x, (int) endpoint.y, (int) leftPoint.x, (int) leftPoint.y);
			g.drawLine((int) endpoint.x, (int) endpoint.y, (int) rightPoint.x, (int) rightPoint.y);
			break;
		}
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
	
	@SuppressWarnings("serial")
	private static class Popup extends JPopupMenu{
		
		private Popup(Association line) {
			Canvas canvas = Canvas.getInstance();
			add(new JMenuItem(new AbstractAction("Make Association") {
				public void actionPerformed(ActionEvent e) {
					line.shapetype = ShapeType.ASSOCIATON;
					canvas.repaint();
				}
			}) {
			});
			
			add(new JMenuItem(new AbstractAction("Make Aggregation") {
				public void actionPerformed(ActionEvent e) {
					line.shapetype = ShapeType.AGGREGATION;
					canvas.repaint();
				}
			}) {
			});
			
			add(new JMenuItem(new AbstractAction("Make Composition") {
				public void actionPerformed(ActionEvent e) {
					line.shapetype = ShapeType.COMPOSITION;
					canvas.repaint();
				}
			}) {
			});
			
			add(new JMenuItem(new AbstractAction("L�schen") {
				public void actionPerformed(ActionEvent e) {
					line.delete(null);
					canvas.repaint();
				}
			}) {
			});
		}
	}
}
