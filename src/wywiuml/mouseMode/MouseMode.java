package wywiuml.mouseMode;

import java.awt.event.MouseAdapter;
import java.io.Serializable;

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
	
}
