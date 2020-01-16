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

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;


@SuppressWarnings("serial")
public class Menubar extends JMenuBar {

	public Menubar() {
		JMenu menu = new JMenu("Datei");
		add(menu);
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
							Canvas.getInstance().readSaveState((Serializable)in.readObject());
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
