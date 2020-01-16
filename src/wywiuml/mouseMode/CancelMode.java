package wywiuml.mouseMode;

import java.awt.event.MouseEvent;

import wywiuml.gui.Canvas;
import wywiuml.gui.Toolbar;

public class CancelMode extends MouseMode{
	private static Toolbar toolbar;
	//private static CancelMode instance;
	
	
	@Override
	public String getModeName() {
		return "CancelMode";
	}
	
	@Override
	public String getDescription() {
		return "Abbruch";
	}
	
	public static void setToolbar(Toolbar tb) {
		toolbar = tb;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		Canvas canvas = Canvas.getInstance();
		canvas.cancelEditing();
		toolbar.resetMode();
		canvas.repaint();
	}

}
