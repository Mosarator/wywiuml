package wywiuml.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import wywiuml.shapes.ClassObject;
import wywiuml.shapes.Generalization;
import wywiuml.structures.ClassOrInterfaceUML;

@SuppressWarnings("serial")
public class Menubar extends JMenuBar {

	public Menubar() {
		JMenu menu = new JMenu("Datei");
		add(menu);

		JMenuItem importMenu = new JMenu("Importieren...");
		menu.add(importMenu);

		JMenuItem importFromCode = new JMenuItem(new AbstractAction("... aus Projektpfad") {

			@Override
			public void actionPerformed(ActionEvent e) {

				Canvas canvas = Canvas.getInstance();
				
				JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int choice = chooser.showOpenDialog(null);
				if(choice == ABORT) {
					//early exit
					return;
				}
				canvas.clean();
				String path = chooser.getSelectedFile().getAbsolutePath();

				// TODO get Path first
				List<ClassOrInterfaceDeclaration> cids = Parser.parseClassesFromProjectPath(path);
				int classAmount = cids.size();
				
				if(classAmount == 0) {
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
						//find index of the extended class
						int found=-1;
						for(int k=0;k<classAmount;k++) {
							if(cius[k]==ext)
								found=k;
						}
						if(found>=0) {
							//draw a line
							Generalization line = new Generalization(true, objs[i].getPosition(), objs[found].getPosition());
							canvas.addShape(line);
						}
					}
					// Now implementations
					for (ClassOrInterfaceUML imp : cius[i].getImplementedClasses()) {
						//find index of the extended class
						int found=-1;
						for(int k=0;k<classAmount;k++) {
							if(cius[k]==imp)
								found=k;
						}
						if(found>=0) {
							//draw a line
							Generalization line = new Generalization(true, objs[i].getPosition(), objs[found].getPosition());
							canvas.addShape(line);
						}
					}
				}

				// TODO "uses" lines
				canvas.repaint();
			}
		});
		importMenu.add(importFromCode);

		JMenuItem export = new JMenu("Exportieren...");
		menu.add(export);

		JMenuItem exportImg = new JMenuItem(new AbstractAction("...als JPG") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
				int choice = chooser.showSaveDialog(null);
				switch (choice) {
					case JFileChooser.APPROVE_OPTION:
						try {
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

		JMenuItem saveAsUML = new JMenuItem(new AbstractAction("Speichern als UML") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
				int choice = chooser.showSaveDialog(null);
				switch (choice) {
					case JFileChooser.APPROVE_OPTION:
						try {
							FileOutputStream outFile = new FileOutputStream(chooser.getSelectedFile() + ".uml");
							ObjectOutputStream out = new ObjectOutputStream(outFile);
							out.writeObject(Canvas.getInstance().getSaveState());
							System.out.println("speichern erfolgreich");
							out.close();
							outFile.close();

						} catch (Exception error) {
							System.out.println(error.getMessage());
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
							System.out.println("Laden initialisieren");
							Canvas.getInstance().readSaveState((Serializable) in.readObject());
							System.out.println("Laden erfolgreich");
							in.close();
							inFile.close();
							Canvas.getInstance().repaint();
						} catch (Exception error) {
							System.out.println(error.getMessage());
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
