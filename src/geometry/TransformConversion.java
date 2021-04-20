package geometry;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class TransformConversion {
    public static void copy(AffineTransform in, Matrix3d out){out.set(in.getScaleX(), in.getShearX(), in.getTranslateX(), in.getShearY(), in.getScaleY(), in.getTranslateY(), 0, 0, 1);}

    public static void copy(Matrix3d in, AffineTransform out) {out.setTransform(in.m00, in.m10, in.m01, in.m11, in.m02, in.m12);}

    public static double distQ(AffineTransform at, Matrix3d mat3d) {
        double scx = at.getScaleX()     - mat3d.m00;
        double shx = at.getShearX()     - mat3d.m01;
        double tx  = at.getTranslateX() - mat3d.m02;
        double scy = at.getScaleY()     - mat3d.m11;
        double shy = at.getShearY()     - mat3d.m10;
        double ty  = at.getTranslateY() - mat3d.m12;
        return scx * scx + scy * scy + shx * shx + shy * shy + tx * tx + ty * ty;
    }

    public static void copy(Point2D p, Vector2d v) {v.set(p.getX(), p.getY());}

    public static void copy(Vector2d v, Point2D p) {p.setLocation(v.x, v.y);}

    public static double distQ(AffineTransform at0, AffineTransform at1) {
        double scx = at0.getScaleX()     - at1.getScaleX();
        double shx = at0.getShearX()     - at1.getShearX();
        double tx  = at0.getTranslateX() - at1.getTranslateX();
        double scy = at0.getScaleY()     - at1.getScaleY();
        double shy = at0.getShearY()     - at1.getShearY();
        double ty  = at0.getTranslateY() - at1.getTranslateY();
        return scx * scx + scy * scy + shx * shx + shy * shy + tx * tx + ty * ty;
    }
}
