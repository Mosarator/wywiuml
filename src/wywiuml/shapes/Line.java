package wywiuml.shapes;

import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import wywiuml.gui.Canvas;
import wywiuml.shapes.Line.LineSegment;
import wywiuml.shapes.Shape.ShapeType;

public abstract class Line extends Shape {

	protected List<LineSegment> segments = new ArrayList<LineSegment>();
	public AnchorPoint startPoint;
	public AnchorPoint endPoint;
	protected Point startP, endP;

	public boolean complete(Shape fromShape, Shape toShape) {
		if (fromShape == null || toShape == null || fromShape.shapetype != ShapeType.CLASS
				|| toShape.shapetype != ShapeType.CLASS)
			return false;

		ClassObject from = (ClassObject) fromShape;
		ClassObject to = (ClassObject) toShape;

		startPoint = new AnchorPoint(startP);
		endPoint = new AnchorPoint(endP);

		startPoint.setConnectedShape(from);
		startPoint.setLine(this);
		endPoint.setConnectedShape(to);
		endPoint.setLine(this);

		segments = new ArrayList<Line.LineSegment>();
		segments.add(new LineSegment(startPoint, endPoint));
		from.addAnchor(startPoint);
		to.addAnchor(endPoint);
		return true;
	}

	@Override
	public boolean isInside(Point p) {
		return isInside(p, 5);
	}
	
	public boolean isInside(Point p, int tolerance) {
		if (getSegmentAt(p, tolerance) == null)
			return false;
		return true;
	}

	public LineSegment getSegmentAt(Point p, int tolerance) {
		for (LineSegment s : segments) {
			if (s.isInside(p, tolerance)) {
				return s;
			}
		}
		return null;
	}

	protected void drawLinePath(Graphics g) {
		if (segments == null)
			return;
		for (LineSegment s : segments) {
			g.drawLine(s.start.getX(), s.start.getY(), s.end.getX(), s.end.getY());
		}
	}

	@Override
	public void delete(Shape source) {
		System.out.println("delete line");
		if (source == null) {
			if (startPoint != null)
				startPoint.delete(this);
			if (endPoint != null)
				endPoint.delete(this);
			Canvas.getInstance().removeShape(this);
			return;
		}

		if (source.getShapeType() == ShapeType.ANCHOR) {
			Canvas.getInstance().removeShape(this);
			if (source != startPoint)
				startPoint.delete(this);
			if (source != endPoint)
				endPoint.delete(this);
			// TODO
			return;
		}
		super.delete(source);
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

		public boolean isInside(Point p, int tolerance) {
			// Distance funkction from stackoverflow post:
			// https://stackoverflow.com/questions/30559799/function-for-finding-the-distance-between-a-point-and-an-edge-in-java

			// getDistance
			float a = p.x - start.getX();
			float b = p.y - start.getY();
			float c = end.getX() - start.getX();
			float d = end.getY() - start.getY();
			float e = -d;
			float f = c;

			float dot = a * e + b * f;
			float len_sq = e * e + f * f;

			double dist = Math.abs(dot) / Math.sqrt(len_sq);

			if (dist <= tolerance)
				return true;
			else
				return false;
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
