package wywiuml.shapes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.Serializable;

import javax.swing.JPopupMenu;

import wywiuml.gui.Canvas;

public class AnchorPoint extends Shape {
	
	private static final Color CLR = Color.BLACK;
	
	private final int size = 6;
	private final int tolerance = 10;
	private Shape connectedShape;
	private Line line;
	
	public AnchorPoint() {
		this(new Point(0,0));
	}
	
	public AnchorPoint(Point p) {
		pos = p;
		shapetype = ShapeType.ANCHOR;
		Canvas.getInstance().addShape(this);
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(CLR);
		g.fillRect(pos.x - size / 2, pos.y - size / 2, size, size);
	}

	@Override
	public void delete(Shape source) {
		if(source == null) {
			//TODO
			return;
		}
		if(source.getShapeType() == ShapeType.CLASS) {
			connectedShape = null;
			line.delete(this);
			Canvas.getInstance().removeShape(this);
			return;
		}
		// Default: Source is Line (check for it?)
		if(connectedShape.getShapeType() == ShapeType.CLASS) {
			((ClassObject)connectedShape).removeAnchor(this);
		}
		connectedShape.delete(this);
		Canvas.getInstance().removeShape(this);
	}
	
	@Override
	public boolean isInside(Point p) {
		if (p.x > pos.x - size / 2 - tolerance && p.x < pos.x + size / 2 + tolerance
				&& p.y > pos.y - size / 2 - tolerance && p.y < pos.y + size / 2 + tolerance)
			return true;
		return false;
	}

	@Override
	public void move(int moveX, int moveY) {
		move(moveX, moveY, false);
	}

	public void move(int moveX, int moveY, boolean sticky) {
		pos.x += moveX;
		pos.y += moveY;
		if (hasConnectedShape() && sticky) {
			
			//Clamp the pos to the shape
			pos.x = Math.max(connectedShape.pos.x, Math.min(connectedShape.pos.x + connectedShape.dim.width, pos.x));
			pos.y = Math.max(connectedShape.pos.y, Math.min(connectedShape.pos.y + connectedShape.dim.height, pos.y));
			
			//find closest Border
			int diffL = Math.abs(getX() - connectedShape.pos.x);
			int diffR = Math.abs(getX() - (connectedShape.pos.x + connectedShape.dim.width));
			int diffT = Math.abs(getY() - connectedShape.pos.y);
			int diffB = Math.abs(getY() - (connectedShape.pos.y + connectedShape.dim.height));
			
			//Stick to the border
			int min = Math.min(Math.min(diffL, diffR), Math.min(diffT, diffB));
			if (min == diffL) {
				System.out.println("Links");
				setX(connectedShape.pos.x);
			} else if (min == diffR) {
				System.out.println("Rechts");
				setX(connectedShape.pos.x + connectedShape.dim.width);
			} else if (min == diffT) {
				System.out.println("Oben");
				setY(connectedShape.pos.y);
			} else {
				System.out.println("Unten");
				setY(connectedShape.pos.y + connectedShape.dim.height);
			}
		}

		Canvas.getInstance().repaint();
	}

	public int getX() {
		return pos.x;
	}

	public int getY() {
		return pos.y;
	}

	public void setX(int x) {
		pos.x = x;
	}

	public void setY(int y) {
		pos.y = y;
	}

	public boolean hasConnectedShape() {
		return (connectedShape != null);
	}

	public Shape getConnectedShape() {
		return connectedShape;
	}

	public void setConnectedShape(Shape shape) {
		connectedShape = shape;
	}

	@Override
	public JPopupMenu getPopupMenu() {
		// TODO Auto-generated method stub
		return null;
	}

	public Line getLine() {
		return line;
	}

	public void setLine(Line line) {
		this.line = line;
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
		//TODO do smthing else?
		SaveState state = new SaveState();
		state.type = ShapeType.ANCHOR;
		return state;
	}

	@Override
	public boolean readSaveState(Serializable saveState) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
}
