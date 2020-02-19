package wywiuml.gui;

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import wywiuml.parsing.Parser;
import wywiuml.shapes.Association;
import wywiuml.shapes.ClassObject;
import wywiuml.shapes.Generalization;
import wywiuml.shapes.Shape;
import wywiuml.shapes.Shape.ShapeType;
import wywiuml.structures.ClassOrInterfaceUML;
import wywiuml.structures.ClassOrInterfaceUML.DuplicateException;

@SuppressWarnings("serial")
public class Menubar extends JMenuBar {

	private String lastPath = "";

	public Menubar() {
		JMenu menu = new JMenu("Datei");
		add(menu);

		JMenuItem importMenu = new JMenu("Importieren...");
		menu.add(importMenu);

		JMenuItem importFromCode = new JMenuItem(new AbstractAction("... aus Java Klasse") {
			@Override
			public void actionPerformed(ActionEvent e) {
				Canvas canvas = Canvas.getInstance();

				String filepath;
				if (lastPath.isEmpty()) {
					filepath = System.getProperty("user.home");
				} else {
					filepath = lastPath;
				}
				JFileChooser chooser = new JFileChooser(filepath);
				int choice = chooser.showOpenDialog(null);
				if (choice != JFileChooser.APPROVE_OPTION) {
					// early exit
					return;
				}
				File file = chooser.getSelectedFile();
				if(file.getName().endsWith(".java") == false) {
					JOptionPane.showMessageDialog(null, "Keine Java Datei");
					return;
				}
				List<ClassOrInterfaceDeclaration> cids = Parser.parseClassesFromFile(file);
				for(ClassOrInterfaceDeclaration cid : cids) {
					canvas.addShape(ClassObject.fromUMLInfo(ClassOrInterfaceUML.fromDeclaration(cid)));
				}
				
			}
		});
		importMenu.add(importFromCode);

		JMenuItem importFromPath = new JMenuItem(new AbstractAction("... aus Projektpfad") {
			@Override
			public void actionPerformed(ActionEvent e) {

				Canvas canvas = Canvas.getInstance();

				String filepath;
				if (lastPath.isEmpty()) {
					filepath = System.getProperty("user.home");
				} else {
					filepath = lastPath;
				}
				JFileChooser chooser = new JFileChooser(filepath);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int choice = chooser.showOpenDialog(null);
				if (choice != JFileChooser.APPROVE_OPTION) {
					// early exit
					return;
				}
				canvas.clean();
				String path = chooser.getSelectedFile().getAbsolutePath();
				lastPath = path;
				// TODO get Path first
				List<ClassOrInterfaceDeclaration> cids = Parser.parseClassesFromProjectPath(path);
				int classAmount = cids.size();

				if (classAmount == 0) {
					JOptionPane.showMessageDialog(null, "No Java Files found!");
					return;
				}

				ClassOrInterfaceUML[] cius = new ClassOrInterfaceUML[classAmount];
				for (int i = 0; i < cius.length; i++) {
					cius[i] = ClassOrInterfaceUML.fromDeclaration(cids.get(i));
				}
				ClassObject[] objs = new ClassObject[classAmount];
				for (int i = 0; i < cius.length; i++) {
					objs[i] = ClassObject.fromUMLInfo(cius[i]);
				}
				ClassObject last = null;
				int nextLeftIndent = 0;
				for (ClassObject obj : objs) {
					if (last == null) {
						obj.setPosition(5, 5);
					} else if (last.getPosition().y + last.getDimension().height + obj.getDimension().height < canvas
							.getHeight()) {
						obj.setPosition(last.getPosition().x, last.getPosition().y + last.getDimension().height + 5);
					} else {
						obj.setPosition(nextLeftIndent, 5);
					}
					last = obj;
					nextLeftIndent = Math.max(nextLeftIndent, last.getPosition().x + last.getDimension().width + 5);
					canvas.addShape(obj);
				}

				for (int i = 0; i < classAmount; i++) {
					// Check for Generalization
					for (ClassOrInterfaceUML ext : cius[i].getExtendedClasses()) {
						// find index of the extended class
						int found = -1;
						for (int k = 0; k < classAmount; k++) {
							if (cius[k] == ext)
								found = k;
						}
						if (found >= 0) {
							// draw a line
							Generalization line = new Generalization(true, objs[i].getPosition(),
									objs[found].getPosition());
							canvas.addShape(line);
						}
					}
					// Now implementations
					for (ClassOrInterfaceUML imp : cius[i].getImplementedClasses()) {
						// find index of the extended class
						int found = -1;
						for (int k = 0; k < classAmount; k++) {
							if (cius[k] == imp)
								found = k;
						}
						if (found >= 0) {
							// draw a line
							Generalization line = new Generalization(true, objs[i].getPosition(),
									objs[found].getPosition());
							canvas.addShape(line);
						}
					}
				}

				// TODO "uses" lines
				canvas.repaint();
			}
		});
		importMenu.add(importFromPath);

		JMenuItem export = new JMenu("Exportieren...");
		menu.add(export);

		JMenuItem exportImg = new JMenuItem(new AbstractAction("...als JPG") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String filepath;
				if (lastPath.isEmpty()) {
					filepath = System.getProperty("user.home");
				} else {
					filepath = lastPath;
				}
				JFileChooser chooser = new JFileChooser(filepath);
				int choice = chooser.showSaveDialog(null);
				switch (choice) {
					case JFileChooser.APPROVE_OPTION:
						try {
							lastPath = chooser.getSelectedFile().getParent();
							File outputfile = new File(chooser.getSelectedFile() + ".png");
							ImageIO.write(Canvas.getInstance().createImage(), "png", outputfile);
						} catch (Exception error) {
						}
						break;
					default:
						// do nothing
						break;
				}
			}
		});
		export.add(exportImg);

		JMenuItem exportProject = new JMenuItem(new AbstractAction("...als Java Project") {
			@Override
			public void actionPerformed(ActionEvent e) {
				Canvas canvas = Canvas.getInstance();

				// Fetch all ClassObjects
				List<ClassObject> classobjects = new ArrayList<ClassObject>();
				for (Shape s : canvas.getShapes()) {
					if (s.getShapeType() == ShapeType.CLASS) {
						classobjects.add((ClassObject) s);
					}
				}

				// Create copies for later
				List<ClassOrInterfaceUML> cloneInfos = new ArrayList<ClassOrInterfaceUML>();
				for (ClassObject obj : classobjects) {
					cloneInfos.add(obj.getUMLInfo().clone());
				}

				// implement Generalizations and associations
				for (Shape s : canvas.getShapes()) {

					ClassObject from;
					ClassObject to;
					switch (s.getShapeType()) {
						case GENERALIZATION:
						case REALIZATION:
							Generalization g = (Generalization) s;
							from = (ClassObject) (g.startPoint.getConnectedShape());
							to = (ClassObject) (g.endPoint.getConnectedShape());
							if (g.getShapeType() == ShapeType.GENERALIZATION) {
								from.getUMLInfo().addExtendedClass(to.getUMLInfo().getName());
							} else {
								from.getUMLInfo().addImplementedClass(to.getUMLInfo().getName());
							}
							break;
						case ASSOCIATON:
						case AGGREGATION:
						case COMPOSITION:
							Association a = (Association) s;
							from = (ClassObject) (a.startPoint.getConnectedShape());
							to = (ClassObject) (a.endPoint.getConnectedShape());
							try{from.getUMLInfo()
									.addAttributeFromUMLString(a.getVariable() + ":" + to.getUMLInfo().getName());
							}catch(DuplicateException ex) {
								JOptionPane.showMessageDialog(null, a.getVariable() + " bereits vergeben. Duplikat beim Speichern verworfen!");
							}
							break;
						default:
							break;
					}
				}

				String filepath;
				if (lastPath.isEmpty()) {
					filepath = System.getProperty("user.home");
				} else {
					filepath = lastPath;
				}

				JFileChooser chooser = new JFileChooser(filepath);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int choice = chooser.showSaveDialog(null);
				switch (choice) {
					case JFileChooser.APPROVE_OPTION:
						lastPath = filepath;
						for (ClassObject obj : classobjects) {
							try {
								filepath = chooser.getSelectedFile().getAbsolutePath() + "/"
										+ obj.getUMLInfo().getName() + ".java";
								File outputfile = new File(filepath);
								BufferedWriter writer = new BufferedWriter(new FileWriter(outputfile));
								writer.write(obj.getUMLInfo().toCode());
								writer.close();
							} catch (Exception error) {
								JOptionPane.showMessageDialog(null,
										"Problem while writing in File: " + obj.getUMLInfo().getName() + ".java");
							}
						}
					default:
						// do nothing
						break;
				}

				// Reset the ClassInformations
				for (int i = 0; i < classobjects.size(); i++) {
					classobjects.get(i).setUMLInfo(cloneInfos.get(i));
				}
			}
		});
		export.add(exportProject);

		JMenuItem saveAsUML = new JMenuItem(new AbstractAction("Speichern als UML") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
				int choice = chooser.showSaveDialog(null);
				switch (choice) {
					case JFileChooser.APPROVE_OPTION:
						try {
							File file = chooser.getSelectedFile();
							if (file.getAbsolutePath().endsWith(".uml") == false) {
								file = new File(chooser.getSelectedFile() + ".uml");
							}
							FileOutputStream outFile = new FileOutputStream(file);
							ObjectOutputStream out = new ObjectOutputStream(outFile);
							out.writeObject(Canvas.getInstance().getSaveState());
							out.close();
							outFile.close();

						} catch (Exception error) {
							JOptionPane.showMessageDialog(null, error.getMessage());
						}
						break;
					default:
						// do nothing
						break;
				}
			}
		});
		menu.add(saveAsUML);

		JMenuItem loadUML = new JMenuItem(new AbstractAction("Laden von UML") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
				int choice = chooser.showOpenDialog(null);
				switch (choice) {
					case JFileChooser.APPROVE_OPTION:
						try {
							FileInputStream inFile = new FileInputStream(chooser.getSelectedFile());
							ObjectInputStream in = new ObjectInputStream(inFile);
							Canvas.getInstance().readSaveState((Serializable) in.readObject());
							in.close();
							inFile.close();
							Canvas.getInstance().repaint();
						} catch (Exception error) {
							JOptionPane.showMessageDialog(null, error.getMessage());
						}
						break;
					default:
						// do nothing
						break;
				}
			}
		});
		menu.add(loadUML);

	}

}
