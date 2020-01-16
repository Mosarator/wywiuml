package wywiuml.shapes;

import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;


public abstract class Line extends Shape {

	protected List<LineSegment> segments = new ArrayList<LineSegment>();
	public AnchorPoint startPoint;
	public AnchorPoint endPoint;

	protected void drawLinePath(Graphics g) {
		if (segments == null)
			return;
		for (LineSegment s : segments) {
			g.drawLine(s.start.getX(), s.start.getY(), s.end.getX(), s.end.getY());
		}
	}


	public class LineSegment {
		private AnchorPoint start;
		private AnchorPoint end;

		public LineSegment(AnchorPoint s, AnchorPoint e) {
			start = s;
			end = e;
		}

		LineSegment[] split() {
			LineSegment[] split = new LineSegment[2];
			Point middle = new Point(((start.getX() + end.getX()) / 2), ((start.getY() + end.getY()) / 2));
			AnchorPoint middlePoint = new AnchorPoint(middle);
			split[0] = new LineSegment(start, middlePoint);
			split[1] = new LineSegment(middlePoint, end);
			return split;
		}
	}
	
	protected static class Vector2D {
		protected double x;
		protected double y;

		protected Vector2D(double xpos, double ypos) {
			x = xpos;
			y = ypos;
		}

		protected Vector2D add(Vector2D other) {
			if (other == null)
				return this;
			return new Vector2D(x + other.x, y + other.y);
		}

		protected Vector2D scale(double s) {
			return new Vector2D(x * s, y * s);
		}

		protected Vector2D normalized() {
			return new Vector2D(x / Math.sqrt(x * x + y * y), y / Math.sqrt(x * x + y * y));
		}

		public String toString() {
			return "(" + x + ", " + y + ")";
		}
	}


}
