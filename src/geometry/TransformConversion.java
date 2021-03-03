package geometry;

import java.awt.geom.AffineTransform;

public class TransformConversion {
    public static void copy(AffineTransform in, Matrix3d out){out.set(in.getScaleX(), in.getShearX(), 0, in.getShearY(), in.getScaleY(), 0, in.getTranslateX(), in.getTranslateY(), 1);}
}
