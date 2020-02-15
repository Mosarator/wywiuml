package wywiuml.shapes;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import wywiuml.gui.Canvas;

public class Association extends Line {

	private static final Color CLR_LINE = Color.BLACK;
	private static final Color CLR_ERROR = Color.RED;
	private static final int ARROWWIDTH = 10;
	private static final int ARROWHEIGHT = 10;
	private final static String firstCharacters = "[\\p{L}_\\$]";
	private final static String followingCharacters = "[\\w|" + firstCharacters + "]";
	private final static String visibilityCharacters = "[+#~-]";
	private final static String namingRegEx = ""+ firstCharacters + followingCharacters + "*";
	final static String nameRegex = "^(" + visibilityCharacters + "?)" + "\\s*(" + namingRegEx + ")\\s*$";
	final static String multiRegex = "^\\s*([\\*|\\d*])(?>(\\.{3})([\\*|\\d*]))?\\s*$";
	
	private static ShapeType currentDefault = ShapeType.ASSOCIATON;
	private boolean isCompleted;
	private boolean isCorrect;
	private String variable = "+ variable";
	// Kardinalitaeten
	private String multiFrom = "*";
	private String multiTo = "*";
	
	public Association() {
		this(false);
	}

	public Association(boolean completed) {
		this(completed, new Point(0, 0), new Point(0, 0));
	}

	public Association(boolean completed, Point start, Point end) {
		startP = start.getLocation();
		endP = end.getLocation();
		shapetype = currentDefault;
		if (completed) {
			complete(Canvas.getInstance().getShapeAt(start, ShapeType.CLASS),
					Canvas.getInstance().getShapeAt(end, ShapeType.CLASS));
		}
	}

	public void setMultiplicitys(String input1, String input2) {
		Pattern pattern = Pattern.compile(multiRegex);
		Matcher matcher1 = pattern.matcher(input1);
		Matcher matcher2 = pattern.matcher(input2);
		
		
		if(matcher1.find() && matcher2.find()) {
			isCorrect = true;
			multiFrom = input1.trim();
			multiTo = input2.trim();
		} else {
			isCorrect = false;
			multiFrom = input1;
			multiTo = input2;
		}
	}
	
 	public String getVariable() {
		return variable;
	}
	
	public void setVariable(String inputString) {
		Pattern pattern = Pattern.compile(nameRegex);
		Matcher matcher = pattern.matcher(inputString);

		StringBuilder str = new StringBuilder("");
		
		if(matcher.find()) {
			isCorrect = true;
			if(matcher.group(1).isEmpty())
				str.append("~");
			else
				str.append(matcher.group(1));
			str.append(" ");
			str.append(matcher.group(2));
		}
		else {
			isCorrect = false;
			str.append(inputString);
		}
		variable = str.toString().trim();
	}

	public boolean complete(Shape fromShape, Shape toShape) {
		if (super.complete(fromShape, toShape) == false) {
			return false;
		}
		isCorrect = true;
		isCompleted = true;
		return true;
	}

	public JPanel getEditWindow() {
		return new EditWindow(this);
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
			// Draw VariableText
			// Draw it on last segment
			LineSegment fs = segments.get(0);
			LineSegment ls = segments.get(segments.size() - 1);
			int xpos = (ls.getStartAnchor().getX() + ls.getEndAnchor().getX()) / 2;
			int ypos = (ls.getStartAnchor().getY() + ls.getEndAnchor().getY()) / 2;
			
			//FontMetrics metrics = g.getFontMetrics();
			//g.drawString(variable, xpos - metrics.stringWidth(variable) / 2, ypos - 5);
			// not centering the Text at the position leads to better results
			g.drawString(variable, xpos, ypos - 5);
			
			// cardiFrom
			xpos = (int)(fs.getStartAnchor().getX()*0.9f + fs.getEndAnchor().getX()*0.1f);
			ypos = (int)(fs.getStartAnchor().getY()*0.9f + fs.getEndAnchor().getY()*0.1f);
			g.drawString(multiFrom, xpos, ypos);
			
			// cardiTo
			xpos = (int)(ls.getStartAnchor().getX()*0.1f + ls.getEndAnchor().getX()*0.9f);
			ypos = (int)(ls.getStartAnchor().getY()*0.1f + ls.getEndAnchor().getY()*0.9f);
			g.drawString(multiTo, xpos, ypos);
			
		}
		
		// don't bother drawing the arrowhead if line is
		// not completed and very short
		if(!isCompleted && startP.distance(endP)<10) {
			return;
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
		poly.xpoints = new int[] { (int) endpoint.x, (int) leftPoint.x, (int) endpoint2.x, (int) rightPoint.x };
		poly.ypoints = new int[] { (int) endpoint.y, (int) leftPoint.y, (int) endpoint2.y, (int) rightPoint.y };
		poly.npoints = 4;

		switch (shapetype) {
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
	public boolean isInside(Point p) {

		// Check if inside variable text.
		Rectangle rect = new Rectangle();
		LineSegment lastSegment = segments.get(segments.size() - 1);
		lastSegment.getStartAnchor();
		int xpos = (lastSegment.getStartAnchor().getX() + lastSegment.getEndAnchor().getX()) / 2;
		int ypos = (lastSegment.getStartAnchor().getY() + lastSegment.getEndAnchor().getY()) / 2;
		FontMetrics metrics = Canvas.getInstance().getFontMetrics(BASICFONT);

		rect.x = xpos;
		rect.y = ypos - metrics.getHeight() - 5;
		rect.width = metrics.stringWidth(variable);
		rect.height = metrics.getHeight();

		return (super.isInside(p) || rect.contains(p));
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
		return new AssociationSaveState(this);
	}

	@Override
	public boolean readSaveState(Serializable saveState) {
		AssociationSaveState state = null;
		try {
			state = (AssociationSaveState) saveState;
		} catch (Exception error) {
			System.out.println(error.getMessage());
			return false;
		}

		startP = new Point(state.startX, state.startY);
		endP = new Point(state.endX, state.endY);
		complete(Canvas.getInstance().getShapeAt(startP, ShapeType.CLASS),
				Canvas.getInstance().getShapeAt(endP, ShapeType.CLASS));
		shapetype = state.type;
		setVariable(state.variable);
		setMultiplicitys(state.multiFrom, state.multiTo);
		return true;
	}

	@SuppressWarnings("serial")
	private static class AssociationSaveState extends SaveState {
		int startX;
		int startY;
		int endX;
		int endY;
		String variable;
		String multiFrom;
		String multiTo;

		private AssociationSaveState(Association obj) {
			type = obj.shapetype;
			variable = obj.getVariable();
			multiFrom = obj.multiFrom;
			multiTo = obj.multiTo;
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
	private static class EditWindow extends JPanel {
		private JTextField attField;
		private JTextField fromField;
		private JTextField toField;
		private JButton acceptButt;
		private JButton cancelButt;
		private JPanel buttPanel;

		private EditWindow(Association line) {
			super();
			Canvas canvas = Canvas.getInstance();
			setLayout(new GridLayout(2, 1));
			attField = new JTextField(line.variable);
			fromField = new JTextField(line.multiFrom);
			toField = new JTextField(line.multiTo);
			
			acceptButt = new JButton("OK");
			acceptButt.addActionListener(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					line.setVariable(attField.getText());
					line.setMultiplicitys(fromField.getText(), toField.getText());
					Canvas.getInstance().cancelEditing();
					Canvas.getInstance().repaint();
				}
			});

			cancelButt = new JButton("Cancel");
			cancelButt.addActionListener(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Canvas.getInstance().cancelEditing();
					;
					Canvas.getInstance().repaint();
				}
			});

			buttPanel = new JPanel();
			buttPanel.setLayout(new GridLayout(1, 2));

			buttPanel.add(acceptButt);
			buttPanel.add(cancelButt);

			add(attField);
			add(buttPanel);

			Rectangle rect = new Rectangle();
			LineSegment fs = line.segments.get(0);
			LineSegment ls = line.segments.get(line.segments.size() - 1);
			int xpos = (ls.getStartAnchor().getX() + ls.getEndAnchor().getX()) / 2;
			int ypos = (ls.getStartAnchor().getY() + ls.getEndAnchor().getY()) / 2;
			FontMetrics metrics = canvas.getFontMetrics(BASICFONT);

			rect.x = xpos;
			rect.y = ypos;
			rect.width = metrics.stringWidth(line.variable);
			rect.height = metrics.getHeight();

			setBounds(rect.x, rect.y, Math.max(rect.width, buttPanel.getPreferredSize().width),
					rect.height + buttPanel.getPreferredSize().height);
			
			// Make the Multiplicitys free floating
			canvas.add(fromField);
			xpos = (int)(fs.getStartAnchor().getX()*0.9f + fs.getEndAnchor().getX()*0.1f);
			ypos = (int)(fs.getStartAnchor().getY()*0.9f + fs.getEndAnchor().getY()*0.1f);
			fromField.setBounds(xpos, ypos, 50, metrics.getHeight());
			
			
			// cardiTo
			canvas.add(toField);
			xpos = (int)(ls.getStartAnchor().getX()*0.1f + ls.getEndAnchor().getX()*0.9f);
			ypos = (int)(ls.getStartAnchor().getY()*0.1f + ls.getEndAnchor().getY()*0.9f);
			toField.setBounds(xpos, ypos, 50, metrics.getHeight());
			
			setVisible(true);
		}
	}

	@SuppressWarnings("serial")
	private static class Popup extends JPopupMenu {

		private Popup(Association line) {
			Canvas canvas = Canvas.getInstance();

			add(new JMenuItem(new AbstractAction("Make Association") {
				public void actionPerformed(ActionEvent e) {
					line.shapetype = ShapeType.ASSOCIATON;
					currentDefault = line.shapetype;
					canvas.repaint();
				}
			}) {
			});

			add(new JMenuItem(new AbstractAction("Make Aggregation") {
				public void actionPerformed(ActionEvent e) {
					line.shapetype = ShapeType.AGGREGATION;
					currentDefault = line.shapetype;
					canvas.repaint();
				}
			}) {
			});

			add(new JMenuItem(new AbstractAction("Make Composition") {
				public void actionPerformed(ActionEvent e) {
					line.shapetype = ShapeType.COMPOSITION;
					currentDefault = line.shapetype;
					canvas.repaint();
				}
			}) {
			});

			add(new JMenuItem(new AbstractAction("L\u00f6schen") {
				public void actionPerformed(ActionEvent e) {
					line.delete(null);
					canvas.repaint();
				}
			}) {
			});
		}
	}
}
