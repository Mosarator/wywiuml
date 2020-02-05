package wywiuml.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import wywiuml.shapes.ClassObject;


@SuppressWarnings("serial")
public class Editor extends JFrame{

	public Editor() {
		super();
		this.setLayout(new BorderLayout());
		
		this.add(new Menubar(), BorderLayout.NORTH);
		this.add(new Toolbar(), BorderLayout.WEST);
		this.add(Canvas.getInstance(), BorderLayout.CENTER);
		
		//Create example
		ClassObject obj = new ClassObject(150, 80);
		Canvas.getInstance().addShape(obj);
	}
	
	
	
	public static void main(String[] args) {
		Editor editor = new Editor();
		editor.setTitle("UML-Editor");
		editor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		editor.setSize(1200, 700);
		editor.setLocationRelativeTo(null);
		editor.setVisible(true);
	}

}
