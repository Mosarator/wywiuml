package wywiuml.shapes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import wywiuml.gui.Canvas;
import wywiuml.structures.ClassOrInterfaceUML;

public class ClassObject extends Shape {

	/**
	 * Default Values
	 */
	private static final Dimension DIMENSION = new Dimension(100, 150);
	private static final int linespacing = 5;
	private static final int topspacing = 5;
	private static final int leftspacing = 10;
	private static final int rightspacing = 5;
	private static final int bottomspacing = 5;
	private static final Color CLR_LINE = Color.BLACK;
	private static final Color CLR_BG = Color.WHITE;
	private static final Color CLR_TEXT = Color.BLACK;
	private static final Color CLR_ERRORTEXT = Color.RED;
	//private static final Color CLR_HIGHLIGHTED = Color.RED;

	private ClassOrInterfaceUML umlInfo;
	private boolean isInterface;
	private boolean isAbstract;
	private List<AnchorPoint> anchors = new ArrayList<AnchorPoint>();
	

	public static ClassObject fromUMLInfo(ClassOrInterfaceUML info) {
		// TODO: fromCode
		ClassObject obj = new ClassObject(0,0);
		obj.umlInfo = info;
		obj.setInterface(info.isInterface());
		obj.setAbstract(info.isAbstract());
		obj.recalculateSize(Canvas.getInstance().getFontMetrics(BASICFONT));
		return obj;
	}

	public ClassObject() {
		this(0, 0, DIMENSION);
	}

	public ClassObject(int posX, int posY) {
		this(posX, posY, DIMENSION);
	}

	public ClassObject(int posX, int posY, Dimension dim) {
		shapetype = ShapeType.CLASS;
		pos = new Point(posX, posY);
		this.dim = new Dimension(dim.width, dim.height);
		setDefaultUmlInfo();
	}

	public void addAnchor(AnchorPoint a) {

		anchors.add(a);

		// get closest border to connect to
		int diffL = Math.abs(a.getX() - pos.x);
		int diffR = Math.abs(a.getX() - (pos.x + dim.width));
		int diffT = Math.abs(a.getY() - pos.y);
		int diffB = Math.abs(a.getY() - (pos.y + dim.height));

		int min = Math.min(Math.min(diffL, diffR), Math.min(diffT, diffB));
		if (min == diffL) {
			a.setX(pos.x);
		} else if (min == diffR) {
			a.setX(pos.x + dim.width);
		} else if (min == diffT) {
			a.setY(pos.y);
		} else {
			a.setY(pos.y + dim.height);
		}

	}

	public void removeAnchor(AnchorPoint a) {
		anchors.remove(a);
	}
	
	@Override
	public void draw(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(BASICLINE);
		g2d.setFont(BASICFONT);

		FontMetrics metrics = g.getFontMetrics();
		int textHeight = metrics.getHeight();
		recalculateSize(metrics);
		int lastHeight = 0;

		// draw Background
		g.setColor(CLR_BG);
		g.fillRect(pos.x, pos.y, dim.width, dim.height);

		// draw outline
		g.setColor(CLR_LINE);
		g.drawLine(pos.x, pos.y, pos.x + dim.width, pos.y);
		g.drawLine(pos.x, pos.y, pos.x, pos.y + dim.height);
		g.drawLine(pos.x + dim.width, pos.y, pos.x + dim.width, pos.y + dim.height);
		g.drawLine(pos.x, pos.y + dim.height, pos.x + dim.width, pos.y + dim.height);

		// draw all known Information

		if (umlInfo == null)
			return;

		/**
		 * if (isInterface && isAbstract) {
			g.setColor(CLR_ERRORTEXT);
		} else {
			g.setColor(CLR_TEXT);
		}
		Interface CAN be abstract, but it's useless
		**/
		
		g.setColor(CLR_TEXT);

		if (isInterface) {
			g.drawString("<<interface>>", pos.x + (dim.width - metrics.stringWidth("<<interface>>")) / 2,
					pos.y + topspacing + textHeight);
			lastHeight = topspacing + textHeight + linespacing;
		}

		String signature = "";
		if (umlInfo.uncompiledSignature.length() > 0) {
			g.setColor(CLR_ERRORTEXT);
			signature = umlInfo.uncompiledSignature;
		} else {
			signature = umlInfo.getSignature();
		}

		if (isAbstract) {
			g.setFont(ITALICFONT);
		}
		// Draw Signature in the Center
		g.drawString(signature, pos.x + (dim.width - metrics.stringWidth(signature)) / 2,
				pos.y + lastHeight + linespacing + textHeight);

		lastHeight += (linespacing + textHeight + bottomspacing);

		// Trennlinie 1
		g.setColor(CLR_LINE);
		g.drawLine(pos.x, pos.y + lastHeight, pos.x + dim.width, pos.y + lastHeight);

		g.setFont(BASICFONT);
		// Alle Attribute
		// Erst die Fehlerfreien
		g.setColor(CLR_TEXT);
		List<String> attributes = umlInfo.getAttributesInUML();
		for (int i = 0; i < attributes.size(); i++) {
			g.drawString(attributes.get(i), pos.x + leftspacing, pos.y + lastHeight + linespacing + textHeight);
			lastHeight += (linespacing + textHeight);
		}
		// Jetzt die mit Fehlern
		g.setColor(CLR_ERRORTEXT);
		for (String s : umlInfo.uncompiledAttributes) {
			g.drawString(s, pos.x + leftspacing, pos.y + lastHeight + linespacing + textHeight);
			lastHeight += (linespacing + textHeight);
		}
		lastHeight += bottomspacing;

		// Trennlinie 2
		g.setColor(CLR_LINE);
		g.drawLine(pos.x, pos.y + lastHeight, pos.x + dim.width, pos.y + lastHeight);

		// Alle Methoden
		g.setColor(CLR_TEXT);
		List<String> methods = umlInfo.getMethodsInUML();
		for (int i = 0; i < methods.size(); i++) {
			g.drawString(methods.get(i), pos.x + leftspacing, pos.y + lastHeight + linespacing + textHeight);
			lastHeight += (linespacing + textHeight);
		}
		// Die fehlerhaften
		g.setColor(CLR_ERRORTEXT);
		for (String s : umlInfo.uncompiledMethods) {
			g.drawString(s, pos.x + leftspacing, pos.y + lastHeight + linespacing + textHeight);
			lastHeight += (linespacing + textHeight);
		}

	}

	public JPanel getEditWindow() {
		return new EditWindow(this);
	}

	@Override
	public boolean isInside(Point p) {
		if (p.x >= pos.x && p.x <= pos.x + dim.width && p.y >= pos.y && p.y <= pos.y + dim.height)
			return true;
		return false;
	}

	@Override
	public void move(int moveX, int moveY) {
		pos = new Point(pos.x + moveX, pos.y + moveY);
		for (AnchorPoint anchor : anchors) {
			anchor.move(moveX, moveY);
		}
	}

	public ClassOrInterfaceUML getUMLInfo() {
		return umlInfo;
	}
	
	public void setUMLInfo(ClassOrInterfaceUML info) {
		umlInfo = info;
	}
	
 	private void setDefaultUmlInfo() {
		int number = 0;
		String className = null;
		do {
			ClassOrInterfaceUML temp = ClassOrInterfaceUML.getClassByName("Unbenannt_" + number);
			if (temp == null)
				className = "Unbenannt_" + number;
			number++;
		} while (className == null);

		this.umlInfo = ClassOrInterfaceUML.quickCreate("+" + className);
		//this.isInterface = false;
	}

	private void recalculateSize(FontMetrics metrics) {
		int newWidth = DIMENSION.width;
		int newHeight = DIMENSION.height;
		List<String> attributes = new ArrayList<String>();
		attributes.addAll(umlInfo.getAttributesInUML());
		attributes.addAll(umlInfo.uncompiledAttributes);
		List<String> methods = new ArrayList<String>();
		methods.addAll(umlInfo.getMethodsInUML());
		methods.addAll(umlInfo.uncompiledMethods);
		int headlines = 1;
		if (isInterface)
			headlines = 2;
		int calcHeight = (headlines + methods.size() + attributes.size()) * (linespacing + metrics.getHeight())
				+ topspacing + bottomspacing * 3;
		newHeight = Math.max(newHeight, calcHeight);
		newWidth = DIMENSION.width - leftspacing - rightspacing;
		if (methods.size() > 0)
			for (int i = 0; i < methods.size(); i++) {
				newWidth = Math.max(newWidth, metrics.stringWidth(methods.get(i)));
			}
		if (attributes.size() > 0)
			for (int i = 0; i < attributes.size(); i++) {
				newWidth = Math.max(newWidth, metrics.stringWidth(attributes.get(i)));
			}
		String signature = "";
		if (umlInfo.uncompiledSignature.length() > 0) {
			signature = umlInfo.uncompiledSignature;
		} else {
			signature = umlInfo.getSignature();
		}
		newWidth = Math.max(newWidth, metrics.stringWidth(signature));
		newWidth += leftspacing + rightspacing;
		dim.width = newWidth;
		dim.height = newHeight;
	}

	public boolean isInterface() {
		return isInterface;
	}

	public void setInterface(boolean isInterface) {
		this.isInterface = isInterface;
		umlInfo.setIsInterface(isInterface);
	}

	@Override
	public JPopupMenu getPopupMenu() {
		return new PopupMenu(this);
	}

	@Override
	public void update() {
		update(false);
	}

	@Override
	public void update(boolean recursive) {
		recalculateSize(Canvas.getInstance().getFontMetrics(BASICFONT));

		// TODO recursive
		if (recursive) {

		} else {
			for (AnchorPoint a : anchors) {
				a.move(0, 0, true);
				a.getLine().update();
			}
		}
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
		umlInfo.setIsAbstract(isAbstract);
	}

	@Override
	public SaveState getSaveState() {
		return new ClassSaveState(this);
	}

	@Override
	public boolean readSaveState(Serializable saveState) {
		
		ClassSaveState ss = (ClassSaveState) saveState;
		
		pos.x = ss.posx;
		pos.y = ss.posy;
		dim.height = ss.height;
		dim.width = ss.width;
		setAbstract(ss.isAbstract);
		setInterface(ss.isInterface);
		//isAbstract = ss.isAbstract;
		//isInterface = ss.isInterface;
		ClassOrInterfaceUML newInfo;
		
		try {
			newInfo = ClassOrInterfaceUML.quickCreate(ss.signature);
			newInfo.uncompiledSignature = "";
		} catch (Exception error) {
			newInfo = ClassOrInterfaceUML.quickCreate("+ ErrorClass");
			newInfo.uncompiledSignature = ss.signature;
		}

		newInfo.uncompiledAttributes.clear();
		for (String att : ss.attributes) {
			try {
				newInfo.addAttributeFromUMLString(att);
			} catch (Exception error) {
				if (!att.trim().isEmpty())
					newInfo.uncompiledAttributes.add(att);
			}
		}
		newInfo.uncompiledAttributes.clear();
		for (String meth : ss.methods) {
			try {
				newInfo.addMethodFromUMLString(meth);
			} catch (Exception error) {
				System.out.println("ERROR: " + error.getMessage());
				if (!meth.trim().isEmpty())
					newInfo.uncompiledMethods.add(meth);
			}
		}
		ClassOrInterfaceUML.removeFromList(umlInfo);
		umlInfo = newInfo;
		return false;
	}

	@Override
	public void delete(Shape source) {
		// Deletion starts from here
		if(source == null) {
			Canvas.getInstance().removeShape(this);
			ClassOrInterfaceUML.removeFromList(umlInfo);
			for(AnchorPoint a : anchors) {
				a.delete(this);
			}
		}
		super.delete(source);
	}
	
	@SuppressWarnings("serial")
	private static class PopupMenu extends JPopupMenu {
		// Constructor
		public PopupMenu(ClassObject umlclass) {
			Canvas canvas = Canvas.getInstance();

			add(new JMenuItem(new AbstractAction("Toggle Abstract") {
				public void actionPerformed(ActionEvent e) {
					umlclass.setAbstract(!umlclass.isAbstract());
					umlclass.update();
					canvas.repaint();
				}
			}) {
			});

			add(new JMenuItem(new AbstractAction("Toggle Interface") {
				public void actionPerformed(ActionEvent e) {
					umlclass.setInterface(!umlclass.isInterface());
					umlclass.update();
					canvas.repaint();
				}
			}) {
			});

			add(new JMenuItem(new AbstractAction("Verstecken") {
				public void actionPerformed(ActionEvent e) {
					umlclass.setHidden(true);
					canvas.repaint();
				}
			}) {
			});
			
			add(new JMenuItem(new AbstractAction("L\u00f6schen") {
				public void actionPerformed(ActionEvent e) {
					umlclass.delete(null);
					canvas.repaint();
				}
			}) {
			});

			add(new JMenuItem(new AbstractAction("Print Code to Console") {
				public void actionPerformed(ActionEvent e) {
					System.out.println(umlclass.umlInfo.toCode());
				}
			}) {
			});
			
			
		}

	}

	@SuppressWarnings("serial")
	private static class EditWindow extends JPanel {
		private JTextField nameField;
		private JTextArea attArea;
		private JTextArea methArea;
		private JButton acceptButt;
		private JButton cancelButt;
		private JPanel infoPanel;
		private JPanel buttPanel;
		private int padding = 0;

		private EditWindow(ClassObject obj) {
			super();
			// "Konstanten"
			Color bg = Color.BLACK;
			List<String> attributes = new ArrayList<String>();
			attributes.addAll(obj.umlInfo.getAttributesInUML());
			attributes.addAll(obj.umlInfo.uncompiledAttributes);
			List<String> methods = new ArrayList<String>();
			methods.addAll(obj.umlInfo.getMethodsInUML());
			methods.addAll(obj.umlInfo.uncompiledMethods);
			JPanel outerPanel = this;
			StringBuilder str = new StringBuilder("");

			setBackground(bg);
			setLayout(new BorderLayout());
			if (obj.umlInfo.uncompiledSignature.length() > 0) {
				nameField = new JTextField(obj.umlInfo.uncompiledSignature);
			} else {
				nameField = new JTextField(obj.umlInfo.getSignature());
			}
			infoPanel = new JPanel();
			infoPanel.setLayout(new GridLayout(2, 1, padding, padding));
			buttPanel = new JPanel();
			buttPanel.setLayout(new GridLayout(1, 2));

			add(nameField, BorderLayout.NORTH);
			add(infoPanel, BorderLayout.CENTER);
			add(buttPanel, BorderLayout.SOUTH);

			attArea = new JTextArea();
			for (String att : attributes) {
				str.append(att);
				str.append("\r\n");
			}
			attArea.setText(str.toString());
			JScrollPane attPane = new JScrollPane(attArea);

			str.setLength(0);// reset Buffer
			methArea = new JTextArea();
			for (String meth : methods) {
				str.append(meth);
				str.append("\r\n");
			}
			methArea.setText(str.toString());
			JScrollPane methPane = new JScrollPane(methArea);

			infoPanel.add(attPane);
			infoPanel.add(methPane);

			acceptButt = new JButton("OK");
			acceptButt.addActionListener(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ClassOrInterfaceUML newInfo = null;
					try {
						//obj.uncompiledSignature = "";
						newInfo = ClassOrInterfaceUML.quickCreate(nameField.getText());
						newInfo.uncompiledSignature = "";
					} catch (Exception error) {
						newInfo = ClassOrInterfaceUML.quickCreate("+ ErrorClass");
						newInfo.uncompiledSignature = nameField.getText();
					}
					newInfo.setIsAbstract(obj.isAbstract);
					newInfo.setIsInterface(obj.isInterface);
					//List<String> uncompiledAttributes = new ArrayList<String>();
					String[] lines = attArea.getText().split("\r?\n");
					for (String line : lines) {
						try {
							newInfo.addAttributeFromUMLString(line);
						} catch (Exception error) {
							if (!line.trim().isEmpty())
								newInfo.uncompiledAttributes.add(line);
						}
					}

					//List<String> uncompiledMethods = new ArrayList<String>();
					lines = methArea.getText().split("\r?\n");
					for (String line : lines) {
						try {
							newInfo.addMethodFromUMLString(line);
						} catch (Exception error) {
							System.out.println("ERROR: " + error.getMessage());
							if (!line.trim().isEmpty())
								newInfo.uncompiledMethods.add(line);
						}
					}
					ClassOrInterfaceUML.removeFromList(obj.umlInfo);
					obj.umlInfo = newInfo;
					//obj.uncompiledAttributes = uncompiledAttributes;
					//obj.uncompiledMethods = uncompiledMethods;
					//Canvas.getInstance().remove(outerPanel);
					Canvas.getInstance().cancelEditing();
					obj.update();
					Canvas.getInstance().repaint();
				}
			});

			cancelButt = new JButton("Cancel");
			cancelButt.addActionListener(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Canvas.getInstance().cancelEditing();
					Canvas.getInstance().repaint();
				}
			});

			buttPanel.add(acceptButt);
			buttPanel.add(cancelButt);

			setBounds(obj.pos.x - padding, obj.pos.y - padding,
					Math.max(obj.dim.width, buttPanel.getPreferredSize().width),
					obj.dim.height + buttPanel.getPreferredSize().height);

			setVisible(true);
		}

	}

	@SuppressWarnings("serial")
	private static class ClassSaveState extends SaveState {
		boolean isInterface;
		boolean isAbstract;
		String signature;
		String[] attributes;
		String[] methods;
		int posx;
		int posy;
		int width;
		int height;

		private ClassSaveState(ClassObject obj) {
			type = ShapeType.CLASS;
			isInterface = obj.isInterface;
			isAbstract = obj.isAbstract;
			if (obj.umlInfo.uncompiledSignature.length() > 0)
				signature = obj.umlInfo.uncompiledSignature;
			else
				signature = obj.umlInfo.getSignature();

			List<String> umlAtts = obj.umlInfo.getAttributesInUML();
			List<String> umlwrongAtts = obj.umlInfo.uncompiledAttributes;
			attributes = new String[umlAtts.size() + umlwrongAtts.size()];

			List<String> umlMeths = obj.umlInfo.getMethodsInUML();
			List<String> umlwrongMeths = obj.umlInfo.uncompiledMethods;
			methods = new String[umlMeths.size() + umlwrongMeths.size()];

			for (int i = 0; i < umlAtts.size(); i++) {
				attributes[i] = umlAtts.get(i);
			}
			for (int i = 0; i < umlwrongAtts.size(); i++) {
				attributes[i + umlAtts.size()] = umlwrongAtts.get(i);
			}

			for (int i = 0; i < umlMeths.size(); i++) {
				methods[i] = umlMeths.get(i);
			}
			for (int i = 0; i < umlwrongMeths.size(); i++) {
				methods[i + umlMeths.size()] = umlwrongMeths.get(i);
			}
			posx = obj.pos.x;
			posy = obj.pos.y;
			width = obj.dim.width;
			height = obj.dim.height;
		}
	}

}
