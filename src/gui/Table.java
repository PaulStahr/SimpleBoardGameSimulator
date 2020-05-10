package gui;

import com.sun.corba.se.impl.orbutil.graph.Graph;
import javafx.scene.control.Tab;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;

public class Table {
    private int playerNumber;
    private int diameter;
    private Point2D boardOrigin;
    private Point2D screenOrigin;
    public Shape shape = null;

    public Table(int playerNumber, int diameter, Point2D boardOrigin){
        this.playerNumber = playerNumber;
        this.diameter = diameter;
        this.boardOrigin = boardOrigin;
        this.screenOrigin = boardOrigin;
        setTablePosition( (int) screenOrigin.getX(), (int) screenOrigin.getY(), this.diameter);
    }

    public void drawTable(GamePanel gamePanel, Graphics g){
        gamePanel.boardToScreenPos(this.boardOrigin, this.screenOrigin);
        setTablePosition((int) screenOrigin.getX(), (int) screenOrigin.getY(), this.diameter);

        Graphics2D graphics = (Graphics2D) g;
        AffineTransform tmp = graphics.getTransform();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // set background color
        Color tableBackground = new Color(139,69,19,255);
        graphics.setPaint(tableBackground);
        // set border color
        graphics.setColor(tableBackground);
        graphics.setStroke(new BasicStroke(2));
        graphics.fill(this.shape);
        graphics.setTransform(tmp);
    }

    public void updatePlayerNumber(int playerNumber){
        this.playerNumber = playerNumber;
    }

    public void updateDiameter(int diameter){
        this.diameter = diameter;
    }

    public void updateBoardOrigin(Point2D boardOrigin){
        this.boardOrigin = boardOrigin;
    }

    public void setTablePosition(int posX, int posY, int diameter) {
        if (this.shape instanceof Ellipse2D)
        {
            ((Ellipse2D)this.shape).setFrame(posX, posY, diameter, diameter);
        }
        else
        {
            Shape tableArea = new Ellipse2D.Double(posX, posY, diameter, diameter);
            this.shape = tableArea;
        }
    }
}
