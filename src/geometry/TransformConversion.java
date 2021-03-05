package geometry;

import java.awt.geom.AffineTransform;

public class TransformConversion {
    public static void copy(AffineTransform in, Matrix3d out){out.set(in.getScaleX(), in.getShearY(), 0, in.getShearX(), in.getScaleY(), 0, in.getTranslateX(), in.getTranslateY(), 1);}
}
