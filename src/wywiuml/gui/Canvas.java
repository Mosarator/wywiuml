package wywiuml.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import wywiuml.mouseMode.CancelMode;
import wywiuml.mouseMode.MouseMode;
import wywiuml.mouseMode.SelectAndEditMode;
import wywiuml.shapes.Association;
import wywiuml.shapes.ClassObject;
import wywiuml.shapes.Generalization;
import wywiuml.shapes.Shape;
import wywiuml.shapes.Shape.SaveState;
import wywiuml.shapes.Shape.ShapeType;
import wywiuml.structures.ClassOrInterfaceUML;

@SuppressWarnings("serial")
public class Canvas extends JPanel {

	private final static Color BACKGROUND = Color.GRAY;
	private static Canvas instance;

	private List<Shape> shapes = new ArrayList<Shape>();
	private MouseMode currentMode;
	private MouseMode lastMode = SelectAndEditMode.getInstance();
	private boolean isEditing;

	public Shape selected;

	public static Canvas getInstance() {
		if (instance == null) {
			instance = new Canvas();
		}
		return instance;
	}

	public static void setInstance(Canvas newInstance) {
		if (newInstance != null)
			instance = newInstance;
		else
			getInstance();
	}

	public BufferedImage createImage() {
		BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		paint(img.createGraphics());
		return img;
	}

	private Canvas() {
		super();
		setLayout(null);
		setBackground(BACKGROUND);
		setMouseMode(lastMode);
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(BACKGROUND);
		g.fillRect(0, 0, getSize().width, getSize().height);

		// call the draw-method for every umlObject but in reverse order, so that
		// Anchors and Classes dont get overlapped by lines
		for (int i = shapes.size()-1; i >= 0; i--) {
			if (shapes.get(i).isHidden() == false)
				shapes.get(i).draw(g);
		}

		// Draw Panels
		for (Component comp : getComponents()) {
			comp.repaint();
		}
		// Highlight current Mode in upperleft corner
		highlightMode(g);
	}

	public Shape addShape(Shape obj) {
		shapes.add(obj);
		sortObjects();
		repaint();
		return obj;
	}

	public void removeShape(Shape obj) {
		shapes.remove(obj);
	}

	public void clean() {
		ClassOrInterfaceUML.removeAll();
		shapes.clear();
	}

	public boolean getIsEditing() {
		return isEditing;
	}

	public void setIsEditing(boolean b) {
		if (b) {
			lastMode = currentMode;
			setMouseMode(new CancelMode());
		}
		isEditing = b;
	}

	public void cancelEditing() {
		if (isEditing) {
			removeAll();
			isEditing = false;
			repaint();
			setMouseMode(lastMode);
		}
	}

	public Shape[] getShapes() {
		Shape[] result = new Shape[shapes.size()];
		for (int i = 0; i < shapes.size(); i++) {
			result[i] = shapes.get(i);
		}
		return result;
	}

	public Shape getShapeAt(Point p) {
		return getShapeAt(p, null);
	}

	public Shape getShapeAt(Point p, ShapeType filter) {
		if (filter == null) {
			for (Shape obj : shapes) {
				// hidden objects should not be clickable
				if(obj.isHidden())
					continue;
				if (obj.isInside(p))
					return obj;
			}
		} else {
			for (Shape obj : shapes) {
				// hidden objects should not be clickable
				if(obj.isHidden())
					continue;
				if ((obj.getShapeType() == filter) && (obj.isInside(p)))
					return obj;
			}
		}
		return null;
	}

	public Serializable getSaveState() {
		return new CanvasSaveState(this);
	}

	public void readSaveState(Serializable state) {
		CanvasSaveState cs;
		try {
			cs = (CanvasSaveState) state;
		} catch (Exception error) {
			JOptionPane.showMessageDialog(null, error.getMessage());
			return;
		}
		shapes.clear();
		SaveState s = null;
		for (int i = cs.shapesStates.length - 1; i >= 0; i--) {
			s = cs.shapesStates[i];
			switch (s.type) {
				case CLASS:
					ClassObject umlclass = new ClassObject();
					umlclass.readSaveState(s);
					addShape(umlclass);
					break;
				case GENERALIZATION:
				case REALIZATION:
					Generalization umlGeneralization = new Generalization();
					umlGeneralization.readSaveState(s);
					addShape(umlGeneralization);
					break;
				case AGGREGATION:
				case COMPOSITION:
				case ASSOCIATON:
					Association umlAssociation = new Association();
					umlAssociation.readSaveState(s);
					addShape(umlAssociation);
					break;
				case ANCHOR:
					// Do what?
					break;
				default:
					break;
			}
		}
	}

	public Canvas removeObject(Shape obj) {
		shapes.remove(obj);
		return this;
	}

	public MouseMode setMouseMode(MouseMode mode) {
		if (mode == currentMode)
			return currentMode;// earlyExit

		this.removeMouseListener(currentMode);
		this.removeMouseMotionListener(currentMode);
		currentMode = mode;
		this.addMouseListener(currentMode);
		this.addMouseMotionListener(currentMode);
		return currentMode;
	}

	private void sortObjects() {
		shapes.sort(new Comparator<Shape>() {
			@Override
			public int compare(Shape o1, Shape o2) {
				return o1.getShapeType().compareTo(o2.getShapeType());
			}
		});
	}

	private void highlightMode(Graphics g) {
		// Background- and Textcolor
		Color bgc = Color.BLACK;
		Color tc = Color.WHITE;

		int offset = 10;
		int padding = 5;
		String text = "Mausclick: " + currentMode.getDescription();

		// Get Fontmetrics
		g.setFont(Shape.BASICFONT);
		FontMetrics metrics = g.getFontMetrics();
		int width = metrics.stringWidth(text);
		int height = metrics.getHeight();

		g.setColor(bgc);
		g.fillRect(offset, offset, width + 2 * padding, height + 2 * padding);
		g.setColor(tc);
		g.drawString(text, offset + padding, offset + height + padding);

	}

	private static class CanvasSaveState implements Serializable {

		SaveState[] shapesStates;

		private CanvasSaveState(Canvas c) {
			shapesStates = new SaveState[c.shapes.size()];
			for (int i = 0; i < shapesStates.length; i++) {
				shapesStates[i] = c.shapes.get(i).getSaveState();
			}
		}

	}
}
