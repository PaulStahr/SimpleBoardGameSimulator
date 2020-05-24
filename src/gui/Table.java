package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import gameObjects.instance.GameInstance;
import main.Player;

public class Table {
    private int diameter;
    private int playerDiameter;
    private final Point2D boardOrigin = new Point2D.Double();
    private final Point2D screenOrigin = new Point2D.Double();
    public Shape tableShape = null;
    public ArrayList<Shape> playerShapes = new ArrayList<>();

    public Table(GameInstance gameInstance, int diameter, Point2D boardOrigin){
        this.diameter = diameter;
        this.playerDiameter = diameter/10;
        this.boardOrigin.setLocation(boardOrigin.getX(), boardOrigin.getY());
        this.screenOrigin.setLocation(boardOrigin.getX(), boardOrigin.getY());
        setTableParameters((int) screenOrigin.getX(), (int) screenOrigin.getY(), this.diameter);
        for (int i = 0; i < gameInstance.getPlayerList().size(); ++i){
            Shape playerShape = new Ellipse2D.Double(screenOrigin.getX() + this.diameter, screenOrigin.getY() + this.diameter, playerDiameter, playerDiameter);
            playerShapes.add(playerShape);
        }
    }

    public void drawCompleteTable(GamePanel gamePanel, GameInstance gameInstance, Graphics g){
        setTableParameters((int) screenOrigin.getX(), (int) screenOrigin.getY(), diameter);

        this.tableShape = gamePanel.getBoardToScreenTransform().createTransformedShape(this.tableShape);

        Point2D originPlayerMiddle = new Point2D.Double(screenOrigin.getX() + diameter/2, screenOrigin.getY() + diameter + playerDiameter/2);
        Point2D originPlayerBottom = new Point2D.Double(screenOrigin.getX() + diameter/2, screenOrigin.getY() + diameter + playerDiameter + 50);
        Point2D tableMiddle = new Point2D.Double(screenOrigin.getX() + diameter/2, screenOrigin.getY() + diameter/2);
        final AffineTransform identity = new AffineTransform();
        
        Graphics2D graphics2D = (Graphics2D) g;
        AffineTransform tmp = graphics2D.getTransform();
        Point2D rotatedPoint = new Point2D.Double();
        for (int i = 0; i< playerShapes.size(); ++i){
            double angle = 360/playerShapes.size()*i;
            rotatedPoint.setLocation(0, 0);
            AffineTransform.getRotateInstance(Math.toRadians(angle), tableMiddle.getX(), tableMiddle.getY())
                    .transform(originPlayerMiddle, rotatedPoint);
            setPlayerParameter(i, (int) rotatedPoint.getX() - playerDiameter/2, (int) rotatedPoint.getY() - playerDiameter/2, playerDiameter);
            AffineTransform.getRotateInstance(Math.toRadians(angle), tableMiddle.getX(), tableMiddle.getY())
                    .transform(originPlayerBottom, rotatedPoint);

            graphics2D.setTransform(identity);
            //graphics2D.translate((int) rotatedPoint.getX(), (int) rotatedPoint.getY());

            int place = 0;
            for (int j = 0; j < gameInstance.getPlayerNumber(); ++j)
            {
                if (gameInstance.getPlayerByIndex(j).id < gameInstance.getPlayerByIndex(i).id)
                {
                    ++place;
                }
            }

            Player player = gameInstance.getPlayerByIndex(place);

            player.playerAtTableTransform.setTransform(identity);
            player.playerAtTableTransform.translate(rotatedPoint.getX(), rotatedPoint.getY());
            player.playerAtTableTransform.rotate(Math.toRadians(angle));
            player.playerAtTableTransform.translate(-100, -2*playerDiameter);
            player.playerAtTableTransform.preConcatenate(graphics2D.getTransform());
            player.playerAtTableRotation = (int) angle;
            //gameInstance.getPlayerByIndex(i).playerAtTableTransform.preConcatenate(gamePanel.getBoardToScreenTransform());


            graphics2D.setTransform(tmp);
            //Set screen transform
            this.playerShapes.set(i, gamePanel.getBoardToScreenTransform().createTransformedShape(this.playerShapes.get(i)));
            Rectangle rectangle = new Rectangle(  0, 0, 200, 200);

            AffineTransform testTransform = new AffineTransform(player.playerAtTableTransform);
            testTransform.preConcatenate(gamePanel.getBoardToScreenTransform());

            graphics2D.setTransform(testTransform);
            graphics2D.setColor(player.color);
            graphics2D.fill(rectangle);
            graphics2D.drawString("Test",0,0);
            //graphics2D.setTransform(testTransform);

            graphics2D.setTransform(tmp);

        }

        drawTable(g);
        drawPlayers(gameInstance, g);
    }

    private void drawPlayers(GameInstance gameInstance, Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform tmp = graphics2D.getTransform();
        int counter = 0;
        for (Shape shape : playerShapes){
            // set background color
            int place = 0;
            for (int j = 0; j < gameInstance.getPlayerNumber(); ++j)
            {
                if (gameInstance.getPlayerByIndex(j).id < gameInstance.getPlayerByIndex(counter).id)
                {
                    ++place;
                }
            }
            Player player = gameInstance.getPlayerByIndex(place);
            graphics2D.setPaint(player.color);
            graphics2D.fill(shape);
            ++counter;
            graphics2D.setTransform(tmp);
        }
        graphics2D.setTransform(tmp);
    }

    public void drawTable(Graphics g){
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform tmp = graphics2D.getTransform();
        // set background color
        Color tableBackground = new Color(139,69,19,255);
        graphics2D.setPaint(tableBackground);
        // set border color
        graphics2D.fill(this.tableShape);
        graphics2D.setTransform(tmp);
    }

    public void updateDiameter(int diameter){
        this.diameter = diameter;
    }

    public void updateBoardOrigin(Point2D boardOrigin){
        this.boardOrigin.setLocation(boardOrigin.getX(), boardOrigin.getY());
    }

    public void setTableParameters(int posX, int posY, int diameter) {
        if (this.tableShape instanceof Ellipse2D)
        {
            ((Ellipse2D)this.tableShape).setFrame(posX, posY, diameter, diameter);
        }
        else
        {
            Shape tableArea = new Ellipse2D.Double(posX, posY, diameter, diameter);
            this.tableShape = tableArea;
        }
    }

    public void setPlayerParameter(int pos, int posX, int posY, int diameter) {
        if (this.playerShapes.get(pos) instanceof Ellipse2D)
        {
            ((Ellipse2D)this.playerShapes.get(pos)).setFrame(posX, posY, diameter, diameter);
        }
        else
        {
            Shape playerShape = new Ellipse2D.Double(posX, posY, diameter, diameter);
            this.playerShapes.set(pos, playerShape);
        }
    }

    public void updatePlayers(GameInstance gameInstance){
        for (int i = 0; i<gameInstance.getPlayerList().size(); ++i){
            if (i >= this.playerShapes.size()){
                Shape playerShape = new Ellipse2D.Double(screenOrigin.getX() + this.diameter, screenOrigin.getY() + this.diameter, playerDiameter, playerDiameter);
                playerShapes.add(playerShape);
            }
        }
    }

    public Point2D getBoardOrigin(){
        return this.boardOrigin;
    }

    public int getDiameter(){
        return diameter;
    }
}
