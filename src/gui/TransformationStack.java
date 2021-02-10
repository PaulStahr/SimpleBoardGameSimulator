package gui;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Arrays;

public class TransformationStack {
	private AffineTransform transforms[] = new AffineTransform[] {new AffineTransform()};
	private final AffineTransform inverse = new AffineTransform();
	private int index = 0;
	
	public void push()
	{
		if (transforms.length < index)
		{
			int oldLength = transforms.length;
			transforms = Arrays.copyOf(transforms, transforms.length * 2);
			for (int i = oldLength; i < transforms.length; ++i)
			{
				transforms[i] = new AffineTransform();
			}
		}
		++index;
		transforms[index].setTransform(transforms[index-1]);
	}
	
	public final void pop()
	{
		if (index == 0){throw new ArrayIndexOutOfBoundsException(index);}
		--index;
	}
	
	public final AffineTransform get(){return transforms[index];}
	
	public final AffineTransform inverse() throws NoninvertibleTransformException
	{
		inverse.setTransform(transforms[index]);
		inverse.invert();
		return inverse;
	}
	
	public final void load()
	{
		if (index == 0)	{transforms[index].setToIdentity();}
		else			{transforms[index].setTransform(transforms[index - 1]);}
	}
}
