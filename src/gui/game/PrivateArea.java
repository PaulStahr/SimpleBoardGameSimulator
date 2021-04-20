package gui.game;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import gameObjects.functions.ObjectFunctions;
import gameObjects.instance.GameInstance;
import util.Calculate;
import util.data.IntegerArrayList;
import util.data.IntegerArrayList.ReadOnlyIntegerArrayList;

public class PrivateArea {
    public int width = 0;
    public int height = 0;
    public Shape shape = null;
    public int elementNumber = 0;
    public double zooming = 1;
    public double zoomingFactor = 1;

    private final IntegerArrayList privateObjects = new IntegerArrayList();
    public final List<Point2D> privateObjectsPositions = new ArrayList<>();
    public final ReadOnlyIntegerArrayList objects = privateObjects.readOnly();
    public AffineTransform objectTransform;
    private final AffineTransform boardToScreenTransformation;
    private final AffineTransform screenToBoardTransformation;
    public Point2D.Double origin = new Point2D.Double();
    public Point2D.Double basePoint = new Point2D.Double();

    public int currentDragPosition = -1;
    public double savedZooming;

    public PrivateArea(AffineTransform boardToScreenTransformation, AffineTransform screenToBoardTransformation) {
        this.boardToScreenTransformation = boardToScreenTransformation;
        this.screenToBoardTransformation = screenToBoardTransformation;
    }

    public PrivateArea(GamePanel gamePanel, GameInstance gameInstance, AffineTransform boardToScreenTransformation, AffineTransform screenToBoardTransformation) {
        this.boardToScreenTransformation = boardToScreenTransformation;
        this.screenToBoardTransformation = screenToBoardTransformation;
    }

    public void setArea(double posX, double posY, double width,  double height, int translateX, int translateY, double rotation, double zooming) {
    	if (this.shape instanceof Arc2D.Double)
    	{
    		((Arc2D.Double)this.shape).setArc(posX, posY, width*zooming, height*zooming, 0, 180, Arc2D.OPEN);
    	}
    	else
    	{
	        this.shape = new Arc2D.Double(posX, posY, width*zooming, height*zooming, 0, 180, Arc2D.OPEN);;
	        this.width = (int) width;//TODO shouldn't this be executed in both cases?
	        this.height = (int) height;
            this.origin.setLocation(posX + width/2, posY + height/2);
    	}

    }

    public void draw(Graphics g, int originX, int originY) {
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
        setArea(originX-(width/2.*zooming), originY-(height/2.*zooming), width, height, 0, 0, 0, zooming);
        graphics.fill(this.shape);
        graphics.setTransform(tmp);
    }

    public boolean containsBoardCoordinates(int posX, int posY) {
        if (shape == null){return false;}
        Point2D transformedPoint = new Point2D.Double(posX, posY);
        boardToScreenTransformation.transform(transformedPoint, transformedPoint);
        return shape.contains(transformedPoint);
    }

    public boolean containsScreenCoordinates(int posX, int posY) {
        return shape.contains(posX, posY);
    }

    public void updatePrivateObjects(GameInstance gameInstance, Player player) {
    	privateObjects.clear();
    	ObjectFunctions.getOwnedStack(gameInstance, player, privateObjects);
    }

    public double getAngle(int posX, int posY) {
        Point2D baseLine = new Point2D.Double(1, 0);
        Point2D point = new Point2D.Double(posX - (int) this.basePoint.getX(), (int) this.basePoint.getY() - posY);
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

    public int getPrivateObjectIndexByPosition(int posX, int posY){
        if (privateObjects.size() > 0) {
            int section = getSectionByPosition(posX, posY);
            return Calculate.clip(section, 0, privateObjects.size() - 1);
        } else {
            return -1;
        }
    }

    public int getObjectIdByPosition(int posX, int posY) {
        if (privateObjects.size() > 0) {
            int section = getSectionByPosition(posX, posY);
            return privateObjects.getI(Calculate.clip(section, 0, privateObjects.size() - 1));
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
    
    public Point2D transformPoint(Point2D in, Point2D out) {
        return boardToScreenTransformation.transform(in, out);
    }

	public boolean contains(int id) {
		return privateObjects.contains(id);
	}
}
