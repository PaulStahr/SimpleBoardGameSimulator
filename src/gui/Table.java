package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import gameObjects.functions.PlayerFunctions;
import gameObjects.instance.GameInstance;
import main.Player;

public class Table {
    private int diameter;
    private int playerDiameter;
    private int stackerWidth;
    private final Point2D tableOrigin = new Point2D.Double();
    private final Point2D tableScreenOrigin = new Point2D.Double();
    public Color tableColor = Color.decode("#ce8b54");
    public Shape tableShape = null;
    public Shape stackerShape = null;
    public ArrayList<Shape> playerShapes = new ArrayList<>();

    public Table(GameInstance gameInstance, int diameter, Point2D tableOrigin){
        this.diameter = diameter;
        this.playerDiameter = diameter/5;
        this.stackerWidth = diameter/4;
        this.tableOrigin.setLocation(tableOrigin.getX(), tableOrigin.getY());
        this.tableScreenOrigin.setLocation(tableOrigin.getX(), tableOrigin.getY());
        setTableParameters((int) tableScreenOrigin.getX(), (int) tableScreenOrigin.getY(), this.diameter);
        if (!gameInstance.tableColor.equals(""))
        {
            tableColor = Color.decode(gameInstance.tableColor);
        }

        Rectangle boundingRect = new Rectangle((int)tableScreenOrigin.getX() + this.diameter, (int)tableScreenOrigin.getX() + this.diameter);
        Shape playerShape = new Arc2D.Double(boundingRect, 0, 180, Arc2D.OPEN);

        if (gameInstance.seats <= 0) {
            for (int i = 0; i < gameInstance.getPlayerList().size(); ++i) {
                playerShapes.add(playerShape);
            }
        }
        else{
            for (int i = 0; i < gameInstance.seats; ++i){
                //Shape playerShape = new Ellipse2D.Double(tableScreenOrigin.getX() + this.diameter, tableScreenOrigin.getY() + this.diameter, playerDiameter, playerDiameter);
                playerShapes.add(playerShape);
            }
        }
    }

    public void drawCompleteTable(GamePanel gamePanel, GameInstance gameInstance, Graphics g){
        setTableParameters((int) tableScreenOrigin.getX(), (int) tableScreenOrigin.getY(), diameter);

        this.tableShape = gamePanel.getBoardToScreenTransform().createTransformedShape(this.tableShape);

        if (gamePanel.isPutDownAreaVisible) {
            this.stackerShape = gamePanel.getBoardToScreenTransform().createTransformedShape(this.stackerShape);
        }

        Point2D originPlayerCenter = new Point2D.Double(tableScreenOrigin.getX() + diameter/2, tableScreenOrigin.getY() + diameter + playerDiameter/2);
        Point2D originPlayerBottom = new Point2D.Double(tableScreenOrigin.getX() + diameter/2, tableScreenOrigin.getY() + diameter + playerDiameter + 50);
        Point2D tableCenter = new Point2D.Double(tableScreenOrigin.getX() + diameter/2, tableScreenOrigin.getY() + diameter/2);

        Graphics2D graphics2D = (Graphics2D) g;
        AffineTransform tmp = graphics2D.getTransform();
        Point2D rotatedPoint = new Point2D.Double();
        Rectangle rectangle = new Rectangle(  -diameter/10, diameter/20, diameter/5, diameter/5);

        for (int i = 0; i< playerShapes.size(); ++i){
            double angle = 360./playerShapes.size()*i;
            AffineTransform rotateAroundCenterTransform = AffineTransform.getRotateInstance(Math.toRadians(angle), tableCenter.getX(), tableCenter.getY());
            rotateAroundCenterTransform.transform(originPlayerCenter, rotatedPoint);
            setPlayerParameter(i, (int) rotatedPoint.getX() - playerDiameter/2, (int) rotatedPoint.getY() - playerDiameter/2, playerDiameter, (int) angle);

            Player player = PlayerFunctions.GetPlayerFromTablePlace(gameInstance, i);

            player.playerAtTableTransform.setToIdentity();
            player.playerAtTableTransform.rotate(Math.toRadians(angle));
            player.playerAtTableTransform.translate(originPlayerBottom.getX(), originPlayerBottom.getY()-2*playerDiameter);
          
            //Set screen transform
            this.playerShapes.set(i, gamePanel.getBoardToScreenTransform().createTransformedShape(this.playerShapes.get(i)));

            graphics2D.setTransform(gamePanel.getBoardToScreenTransform());
            graphics2D.transform(player.playerAtTableTransform);
            graphics2D.setColor(player.color);

            if (gameInstance.seatColors.size() > i)
            {
                graphics2D.setColor(gameInstance.seatColors.get(i % 10));
            }

            Stroke stroke = new BasicStroke(20.0f);
            graphics2D.setStroke(stroke);
            graphics2D.draw(rectangle);
        }
        graphics2D.setTransform(tmp);

        drawTable(g, gamePanel);
        drawPlayers(gameInstance, g);
    }

    private void drawPlayers(GameInstance gameInstance, Graphics g) {//TODO this method seems to do nothing
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform tmp = graphics2D.getTransform();
        int counter = 0;
        for (int i = 0; i < playerShapes.size(); ++i){
            // set background color
            int place = 0;
            if (counter < gameInstance.getPlayerNumber())
            {
                for (int j = 0; j < gameInstance.getPlayerNumber(); ++j)
                {
                    if (gameInstance.getPlayerByIndex(j).id < gameInstance.getPlayerByIndex(counter).id) {
                        ++place;
                    }
                }
            }
            Player player = gameInstance.getPlayerByIndex(place);
            graphics2D.setPaint(player.color);
            //TODO correct shape rotation
            //graphics2D.draw(shape);
            ++counter;
            graphics2D.setTransform(tmp);
        }
        graphics2D.setTransform(tmp);
    }

    public void drawTable(Graphics g, GamePanel gamePanel){
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform tmp = graphics2D.getTransform();
        // set background color
        graphics2D.setPaint(this.tableColor);
        // set border color
        graphics2D.fill(this.tableShape);

        if (gamePanel.isPutDownAreaVisible) {
            graphics2D.setPaint(Color.gray);
            graphics2D.fill(this.stackerShape);
        }

        graphics2D.setTransform(tmp);
    }

    public void updateDiameter(int diameter){
        this.diameter = diameter;
    }

    public void updateBoardOrigin(Point2D boardOrigin){
        this.tableOrigin.setLocation(boardOrigin.getX(), boardOrigin.getY());
    }

    public void setTableParameters(int posX, int posY, int diameter) {
        if (this.tableShape instanceof Ellipse2D)
        {
            ((Ellipse2D)this.tableShape).setFrame(posX, posY, diameter, diameter);
            ((Ellipse2D)this.stackerShape).setFrame(posX + diameter/2-stackerWidth/2, posY + diameter/2-stackerWidth/2, stackerWidth, stackerWidth);
        }
        else
        {
            Shape tableArea = new Ellipse2D.Double(posX, posY, diameter, diameter);
            Shape stackerShape = new Ellipse2D.Double(posX + diameter/2-stackerWidth/2, posY+diameter/2-stackerWidth/2, stackerWidth, stackerWidth);
            this.stackerShape = stackerShape;
            this.tableShape = tableArea;
        }
    }

    public void setPlayerParameter(int pos, int posX, int posY, int diameter, int angle) {
        if (this.playerShapes.get(pos) instanceof Arc2D)
        {
            ((Arc2D)this.playerShapes.get(pos)).setFrame(posX, posY, diameter, diameter);
        }
        else
        {
            Shape playerShape = new Arc2D.Double(posX, posY, diameter, diameter, angle, angle + 180,Arc2D.OPEN);
            this.playerShapes.set(pos, playerShape);
        }
    }

    public void updatePlayers(GameInstance gameInstance){
        for (int i =this.playerShapes.size(); i<gameInstance.getPlayerList().size(); ++i){
            Shape playerShape = new Arc2D.Double(tableScreenOrigin.getX() + this.diameter, tableScreenOrigin.getY() + this.diameter, playerDiameter, playerDiameter, 0, 180,Arc2D.OPEN);

            //Shape playerShape = new Ellipse2D.Double(tableScreenOrigin.getX() + this.diameter, tableScreenOrigin.getY() + this.diameter, playerDiameter, playerDiameter);
            playerShapes.add(playerShape);
        }
    }

    public Point2D getTableOrigin(){
        return this.tableOrigin;
    }

    public Point2D getTableCenter(Point2D tableCenter){
        tableCenter.setLocation(this.tableOrigin.getX() + diameter/2, this.tableOrigin.getY() + diameter/2);
        return tableCenter;
    }

    public Point2D getTableScreenOrigin(){
        return this.tableScreenOrigin;
    }

    public int getDiameter(){
        return diameter;
    }
    public int getStackerWidth(){return this.stackerWidth;}
}
