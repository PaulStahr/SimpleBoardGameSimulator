package test.geometry;

import static org.junit.Assert.assertTrue;

import java.awt.geom.AffineTransform;

import org.junit.Test;

import geometry.Matrix3d;
import geometry.TransformConversion;

public class TransformConversionTest {    
    @Test
    public void testMatrixConversion() {
        AffineTransform at0 = new AffineTransform(1,2,3,4,5,6);
        AffineTransform at1 = new AffineTransform(0,0,0,0,0,0);
        Matrix3d mat3d = new Matrix3d();
        TransformConversion.copy(at0, mat3d);
        TransformConversion.copy(mat3d,at1);
        assertTrue(at0 + "!=" + at1, TransformConversion.distQ(at0, at1) < 0.1);        
    }
    
    @Test
    public void testCompatibility()
    {
        AffineTransform at = new AffineTransform(1,2,3,4,5,6);
        Matrix3d mat3d = new Matrix3d();
        TransformConversion.copy(at, mat3d);
        at.translate(3.5,4.5);
        mat3d.invert(mat3d);
        mat3d.postTranslate(-3.5, -4.5);
        mat3d.invert(mat3d);
        assertTrue(at + "!=" + mat3d, TransformConversion.distQ(at, mat3d) < 0.1);
    }
}
