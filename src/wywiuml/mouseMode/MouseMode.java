package wywiuml.mouseMode;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;

import javax.swing.SwingUtilities;

import wywiuml.gui.Canvas;
import wywiuml.shapes.Shape;
import wywiuml.shapes.Shape.ShapeType;

@SuppressWarnings("serial")
public abstract class MouseMode extends MouseAdapter {

	protected String modeName;
	protected String description;
	
	public String getModeName() {
		return modeName;
	}
	
	public String getDescription() {
		return description;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// Default behaviour
		// Rightclick should always open PopupMenu
		// If not overwrite this Method instead of calling super
		Canvas canvas = Canvas.getInstance();
		if (SwingUtilities.isRightMouseButton(e)) {
			Shape obj = canvas.getShapeAt(e.getPoint());
			if (obj != null && obj.getPopupMenu() != null)
				obj.getPopupMenu().show(canvas, e.getPoint().x, e.getPoint().y);
		}
	}
	
}
