package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;

public class PrivateArea {
    public int width = 0;
    public int height = 0;
    public Shape shape = null;
    public int elementNumber = 0;
    private final AffineTransform boardTransform;
    private final AffineTransform inverseBoardTransform;
       
    public PrivateArea(AffineTransform boardTransform, AffineTransform inverseBoardTransformation){
    	this.boardTransform = boardTransform;
    	this.inverseBoardTransform = inverseBoardTransformation;
    }

    public void setArea(double posX, double posY, double width,  double height, int translateX, int translateY, double rotation, double zooming) {
    	if (this.shape instanceof Arc2D.Double)
    	{
    		((Arc2D.Double)this.shape).setArc(posX, posY, width, height, 0, 180, Arc2D.OPEN);
    	}
    	else
    	{
	        Shape privateArea = new Arc2D.Double(posX, posY, width, height, 0, 180, Arc2D.OPEN);
	        this.shape = privateArea;
    	}
    }

    public void draw(Graphics g)
    {
        Graphics2D graphics = (Graphics2D) g;
        AffineTransform tmp = graphics.getTransform();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // set background color
        graphics.setPaint(Color.LIGHT_GRAY);
        // set border color
        graphics.setColor(Color.GRAY);
        graphics.setStroke(new BasicStroke(2));
        graphics.fill(this.shape);
        graphics.setTransform(tmp);
    }

    public boolean contains(int posX, int posY){
        Point2D transformedPoint = boardTransform.transform(new Point2D.Double(posX, posY), null);
        return shape.contains(transformedPoint);
    }
}
