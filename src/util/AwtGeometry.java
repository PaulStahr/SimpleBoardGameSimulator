package util;

import java.awt.geom.Point2D;

public class AwtGeometry {
    public static final Point2D add(Point2D lhs, Point2D rhs, Point2D out) {out.setLocation(lhs.getX() + rhs.getX(), lhs.getY() + rhs.getY()); return out;}

    public static Point2D addTo(Point2D in, Point2D out) {return add(in, out, out);}
}
