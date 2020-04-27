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

import gameObjects.instance.GameInstance;
import util.data.IntegerArrayList;

public class PrivateArea {
    public int width = 0;
    public int height = 0;
    public Shape shape = null;
    public int elementNumber = 0;
    public final IntegerArrayList privateObjects = new IntegerArrayList();
    public AffineTransform objectTransform;
    private final AffineTransform boardToScreenTransformation;
    private final AffineTransform screenToBoardTransformation;
    public Point2D.Double origin = new Point2D.Double();

    public PrivateArea(GamePanel gamePanel, GameInstance gameInstance, AffineTransform boardToScreenTransformation, AffineTransform screenToBoardTransformation) {
        this.boardToScreenTransformation = boardToScreenTransformation;
        this.screenToBoardTransformation = screenToBoardTransformation;
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
    	this.origin.setLocation(posX + width/2, posY + height/2);
    }

    public void draw(Graphics g) {
        Graphics2D graphics = (Graphics2D) g;
        AffineTransform tmp = graphics.getTransform();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // set background color
        Color privateAreaBackgound = new Color(255,153,153,127);
        graphics.setPaint(privateAreaBackgound);
        // set border color
        graphics.setColor(privateAreaBackgound);
        graphics.setStroke(new BasicStroke(2));
        graphics.fill(this.shape);
        graphics.setTransform(tmp);
    }

    public boolean containsBoardCoordinates(int posX, int posY) {
        Point2D transformedPoint = boardToScreenTransformation.transform(new Point2D.Double(posX, posY), null);
        return transformedPoint != null && shape.contains(transformedPoint);
    }
    public boolean containsScreenCoordinates(int posX, int posY) {
        return shape.contains(posX, posY);
    }

    public void setPrivateObjects(IntegerArrayList objectIds) {
        this.privateObjects.set(objectIds);
    }

    public double getAngle(int posX, int posY) {
        Point2D baseLine = new Point2D.Double(1, 0);
        Point2D point = new Point2D.Double(posX - origin.getX(), origin.getY() - posY);
        double angle = 180 - Math.toDegrees(Math.atan2(point.getY() - baseLine.getY(), point.getX() - baseLine.getX()));
        return angle;
    }

    public int getSectionByPosition(int posX, int posY){
        if (privateObjects.size() > 0) {
            double sectionSize = 180 / privateObjects.size();
            double angle = getAngle(posX, posY);
            return (int) ( angle/ sectionSize);
        } else {
            return -1;
        }
    }

    public int getObjectIdByPosition(int posX, int posY) {
        if (privateObjects.size() > 0) {
            int section = getSectionByPosition(posX, posY);
            if (section>privateObjects.size()-1){
                section = privateObjects.size()-1;
            }
            else if(section<0){
                section = 0;
            }
            return privateObjects.getI(section);
        } else {
            return -1;
        }

    }

    public int getInsertPosition(int posX, int posY) {
        if (privateObjects.size() > 0) {
            double sectionSize = 180 / (privateObjects.size()*2);
            double angle = getAngle(posX, posY);
            int sectionNum = (int) (angle / sectionSize);
            return (sectionNum + 1)/2;
        } else {
            return 0;
        }

    }

    public Point2D transformPoint(int posX, int posY) {
        Point2D transformedPoint = boardToScreenTransformation.transform(new Point2D.Double(posX, posY), null);
        return transformedPoint;
    }
}
