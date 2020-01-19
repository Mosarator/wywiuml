package wywiuml.shapes;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Stroke;

import java.io.Serializable;

import javax.swing.JPopupMenu;


public abstract class Shape {
	
	public enum ShapeType{
		ASSOCIATON, GENERALIZATION, REALIZATION, DEPENDENCY, AGGREGATION, COMPOSITION, CLASS, COMMENT, ANCHOR 
	}

	private final static float[] dash = {5.0f};
	public final static Stroke DASHEDLINE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,0.0f,dash,0.0f);
	public final static Stroke BASICLINE = new BasicStroke();
	public final static Font BASICFONT 	= new Font("Sans_Serif", Font.PLAIN, 12);
	public final static Font ITALICFONT = new Font("Sans_Serif", Font.ITALIC, 12);
	
	
	protected ShapeType shapetype;
	protected Dimension dim;
	protected Point pos;
	private boolean isHidden;
	
 	public ShapeType getShapeType() {
		return shapetype;
	}
	
	public abstract boolean isInside(Point p);
	
	public abstract JPopupMenu getPopupMenu();
	
	public abstract void draw(Graphics g);
	
	public abstract void move(int moveX, int moveY);
	
	public abstract void update();
	
	public abstract void update(boolean recursive);
	
	public abstract SaveState getSaveState();
	
	public abstract boolean readSaveState(Serializable saveState);
	
	public Dimension getDimension() {
		return dim;
	}
	
	public Point getPosition() {
		return pos;
	}
	
	public void setPosition(int posX, int posY) {
		pos = new Point(posX,posY); 
	}
	
	
	public boolean isHidden() {
		return isHidden;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}


	@SuppressWarnings("serial")
	public static class SaveState implements Serializable{
		public ShapeType type;
	}
}
