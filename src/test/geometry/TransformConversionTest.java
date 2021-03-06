package test.geometry;

import static org.junit.Assert.assertTrue;

import java.awt.geom.AffineTransform;

import org.junit.Test;

import geometry.Matrix3d;
import geometry.TransformConversion;

public class TransformConversionTest {    
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
             //at.rotate(2);
        //mat3d.rotateZ(2);
        try{
            assertTrue(TransformConversion.qdist(at, mat3d) < 0.1);
        }catch(AssertionError e){
            throw new AssertionError(at + "!=" + mat3d, e);
       }
    }
}
