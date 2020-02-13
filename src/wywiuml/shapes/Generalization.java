package wywiuml.shapes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import wywiuml.gui.Canvas;

public class Generalization extends Line {
	private boolean isCompleted;
	private boolean isCorrect;
	private static final Color CLR_LINE = Color.BLACK;
	private static final Color CLR_ERROR = Color.RED;

	private static final int ARROWWIDTH = 10;
	private static final int ARROWHEIGHT = 10;
	
	public Generalization() {
		this(false);
	}

	public Generalization(boolean completed) {
		this(completed, new Point(0, 0), new Point(0, 0));
	}

	public Generalization(boolean completed, Point start, Point end) {
		startP = start.getLocation();
		endP = end.getLocation();
		isCompleted = completed;
		if (completed) {
			boolean success = complete(Canvas.getInstance().getShapeAt(start, ShapeType.CLASS), Canvas.getInstance().getShapeAt(end, ShapeType.CLASS));
			if(success == false)
				// Default behaviour;
				shapetype = ShapeType.GENERALIZATION;
		} else {
			// Default behaviour;
			shapetype = ShapeType.GENERALIZATION;
		}
	}

	public boolean complete(Shape fromShape, Shape toShape) {
		if(super.complete(fromShape, toShape)== false) {
			return false;
		}
		isCorrect = true;
		ClassObject from = (ClassObject) fromShape;
		ClassObject to = (ClassObject) toShape;
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
	public void move(int moveX, int moveY) {
		endP = new Point(endP.x + moveX, endP.y + moveY);
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

		int[] xPoints = { (int) endpoint.x, (int) leftPoint.x, (int) rightPoint.x };
		int[] yPoints = { (int) endpoint.y, (int) leftPoint.y, (int) rightPoint.y };

		g.fillPolygon(xPoints, yPoints, 3);
	}

	@Override
	public void update() {
		update(false);
	}

	@Override
	public JPopupMenu getPopupMenu() {
		return new PopupMenu(this);
	}

	@Override
	public void update(boolean recursive) {
		// TODO implement recursive order
		ClassObject from = (ClassObject) startPoint.getConnectedShape();
		ClassObject to = (ClassObject) endPoint.getConnectedShape();
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
	}

	@Override
	public SaveState getSaveState() {
		return new GeneralizationSaveState(this);
	}

	@Override
	public boolean readSaveState(Serializable saveState) {
		GeneralizationSaveState state = null;
		try {
			state = (GeneralizationSaveState) saveState;
		} catch (Exception error) {
			System.out.println(error.getMessage());
			return false;
		}

		startP = new Point(state.startX,state.startY);
		endP = new Point(state.endX, state.endY);
		complete(Canvas.getInstance().getShapeAt(startP, ShapeType.CLASS), Canvas.getInstance().getShapeAt(endP, ShapeType.CLASS));
		return true;
	}

	@SuppressWarnings("serial")
	private static class GeneralizationSaveState extends SaveState {
		int startX;
		int startY;
		int endX;
		int endY;

		private GeneralizationSaveState(Generalization obj) {
			type = obj.shapetype;
			if (obj.startPoint != null && obj.endPoint != null) {
				startX = obj.startPoint.getX();
				startY = obj.startPoint.getY();
				endX = obj.endPoint.getX();
				endY = obj.endPoint.getY();
			} else {
				startX = obj.startP.x;
				startY = obj.startP.y;
				endX = obj.endP.x;
				endY = obj.endP.y;
			}
		}

	}

	@SuppressWarnings("serial")
	private static class PopupMenu extends JPopupMenu{
		private PopupMenu(Generalization line){
			
			add(new JMenuItem(new AbstractAction("L\u00f6schen") {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					line.delete(null);
					Canvas.getInstance().repaint();
				}
			}));
			
			
			
		}
		
	}
	
}
