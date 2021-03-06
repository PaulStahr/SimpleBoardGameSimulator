package geometry;

import java.awt.geom.AffineTransform;

public class TransformConversion {
    public static void copy(AffineTransform in, Matrix3d out){out.set(in.getScaleX(), in.getShearX(), in.getTranslateX(), in.getShearY(), in.getScaleY(), in.getTranslateY(), 0, 0, 1);}

    public static double qdist(AffineTransform at, Matrix3d mat3d) {
        double scx = at.getScaleX()     - mat3d.m00;
        double shx = at.getShearX()     - mat3d.m01;
        double tx  = at.getTranslateX() - mat3d.m02;
        double scy = at.getScaleY()     - mat3d.m11;
        double shy = at.getShearY()     - mat3d.m10;
        double ty  = at.getTranslateY() - mat3d.m12;
        return scx * scx + scy * scy + shx * shx + shy * shy + tx * tx + ty * ty;
    }
}
