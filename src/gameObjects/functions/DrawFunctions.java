package gameObjects.functions;

import static gameObjects.functions.ObjectFunctions.getStack;
import static gameObjects.functions.ObjectFunctions.getStackBottom;
import static gameObjects.functions.ObjectFunctions.getStackTop;
import static gameObjects.functions.ObjectFunctions.haveSamePositions;
import static gameObjects.functions.ObjectFunctions.isInPrivateArea;
import static gameObjects.functions.ObjectFunctions.isStackCollected;
import static gameObjects.functions.ObjectFunctions.isStackInPrivateArea;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import gui.GamePanel;
import main.Player;
import util.data.IntegerArrayList;

public class DrawFunctions {
    private static final Logger logger = LoggerFactory.getLogger(ObjectFunctions.class);

    public static void drawBoard(GamePanel gamePanel, Graphics g, GameInstance gameInstance){
        g.clearRect(0, 0, gamePanel.getWidth(), gamePanel.getHeight());
        //TODO Florian:sometimes images are drawn twice (the active object?)
        g.drawString(String.valueOf(gamePanel.mouseWheelValue), gamePanel.mouseX, gamePanel.mouseY);
        g.drawImage(gameInstance.game.background, 0, 0, gamePanel.getWidth(), gamePanel.getHeight(), Color.BLACK, null);

        int privateAreaHeight = 700;
        int privateAreaWidth = 700;
        gamePanel.privateArea.setArea(gamePanel.getWidth()/2 - privateAreaWidth/2, gamePanel.getHeight()-privateAreaHeight/2, privateAreaWidth, privateAreaHeight, gamePanel.translateX, gamePanel.translateY, gamePanel.rotation, gamePanel.zooming);
        gamePanel.privateArea.draw(g);
    }

    public static void drawTokenObjects(GamePanel gamePanel, Graphics g, GameInstance gameInstance, ObjectInstance objectInstance, Player player){
        if (ObjectFunctions.isStackBottom(objectInstance)) {
            drawStack(g, ObjectFunctions.getAboveStack(gameInstance, objectInstance), gameInstance, player.id, 1);
            int playerId = ObjectFunctions.getStackOwner(gameInstance, getStack(gameInstance, objectInstance));
            if (playerId != -1) {
                Player p = gameInstance.getPlayer(playerId);
                drawStackBorder(gameInstance, g, p, objectInstance, 10, p.color, 1);
            }
            else{
                g.setColor(gamePanel.stackColor);
                drawStackBorder(gameInstance, g, player, objectInstance, 5, gamePanel.stackColor,1, true);
            }
        }

    }

    public static void drawPlayerMarkers(GamePanel gamePanel, Graphics g, GameInstance gameInstance, Player player, String infoText)
    {
        for(Player p: gameInstance.players) {
            g.setColor(p.color);
            Graphics2D g2 = (Graphics2D)g;
            g2.scale(1/gamePanel.zooming, 1/gamePanel.zooming);
            g.fillRect(p.mouseXPos - 5, p.mouseYPos - 5, 10, 10);
            g.drawString(p.name, p.mouseXPos + 15, p.mouseYPos + 5);

            if(isInPrivateArea(gamePanel, p.mouseXPos, p.mouseYPos))
                p.actionString = "Private Area";

            g.drawString(p.actionString, p.mouseXPos - 5, p.mouseYPos - 20);
            //g.drawString(p.name, p.mouseXPos, p.mouseYPos);
            drawBorder(g, p, ObjectFunctions.getNearestObjectByPosition(gameInstance, p, p.mouseXPos, p.mouseYPos, 1, null), 10, p.color, 1);
            g2.scale(gamePanel.zooming, gamePanel.zooming);
        }
        if (player != null)
        {
            g.setColor(player.color);
            g.drawString(infoText, player.mouseXPos - 25, player.mouseYPos + 5);
        }
    }

    public static void drawActiveObject(Graphics g, Player player, ObjectInstance activeObject){
        int playerId = player == null ? -1 : player.id;
        if(activeObject != null && activeObject.state.owner_id != playerId) {
            drawObject(g, activeObject, playerId, 1);
            if (player != null)
            {
                drawBorder(g, player, activeObject, 10, player.color, 1);
            }
        }
    }

    public static void drawSelectedObjects(GamePanel gamePanel, Graphics g,GameInstance gameInstance, Player player){
        for(int id: gamePanel.selectedObjects)
        {
            ObjectInstance currentObject = gameInstance.objects.get(id);
            if (ObjectFunctions.isStackBottom(currentObject)&& currentObject.state.owner_id != player.id)
            {
                drawStackBorder(gameInstance, g, player, currentObject, 5, player.color, 1);
            }
        }
        Graphics2D g2 = (Graphics2D)g;
        g2.setTransform(new AffineTransform());
        if (gamePanel.activeObject == null && gamePanel.selectWidth > 0 && gamePanel.selectHeight > 0){
            g.setColor(player.color);
            g.drawRect(gamePanel.beginSelectPosX, gamePanel.beginSelectPosY, gamePanel.selectWidth, gamePanel.selectHeight);
        }

    }

    public static void drawTokensInPrivateArea(GamePanel gamePanel, Graphics g, GameInstance gameInstance){
        Graphics2D g2 = (Graphics2D)g;
        g2.setTransform(new AffineTransform());

        ArrayList<ObjectInstance> inHand = new ArrayList<>();
        int playerId = gamePanel.player == null ? -1 : gamePanel.player.id;
        for (int i = 0;i < gameInstance.objects.size(); ++i)
        {
            ObjectInstance oi = gameInstance.objects.get(i);
            if (oi.state.owner_id == playerId)
            {
                inHand.add(oi);
            }
        }
        g2.translate(gamePanel.getWidth() / 2, gamePanel.getHeight());
        if (inHand.size() != 0)
        {
            g2.rotate(-Math.PI * 0.5 + Math.PI / (inHand.size() * 2));
            for (int i = 0; i < inHand.size(); ++i)
            {
                ObjectInstance objectInstance = inHand.get(i);
                BufferedImage img = objectInstance.go.getLook(objectInstance.state, playerId);
                g2.translate(0, -250);
                g.drawImage(img, -(int) (objectInstance.scale * img.getWidth() *0.5),-(int) (objectInstance.scale * img.getHeight() * 0.5), (int) (objectInstance.scale * img.getWidth()), (int) (objectInstance.scale * img.getHeight()), null);
                g2.translate(0, 250);
                g2.rotate(Math.PI / (inHand.size()));
            }
        }
        g2.setTransform(new AffineTransform());

    }

    public static void drawStack(Graphics g, IntegerArrayList stackList, GameInstance gameInstance, int playerId, double zooming) {
        if (isStackInPrivateArea(gameInstance, stackList))
        {

        }
        else {
            if (haveSamePositions(gameInstance.objects.get(stackList.get(0)), gameInstance.objects.get(stackList.last()))) {
                IntegerArrayList newStackList = new IntegerArrayList();
                newStackList.add(gameInstance.objects.get(stackList.last()).id);
                stackList = newStackList;
            }
            for (int id : stackList) {
                drawObject(g, gameInstance.objects.get(id), playerId, zooming);
            }
        }
    }

    public static void drawObject(Graphics g, ObjectInstance objectInstance, int playerId, double zooming) {
        BufferedImage img = objectInstance.go.getLook(objectInstance.state, playerId);
        if (objectInstance.getRotation() == 0) {//TODO: use Graphics rotation if possible
            if (objectInstance.state == null || img == null) {
                logger.error("Object state is null");
            } else {
            	Graphics2D g2 = (Graphics2D)g;
            	AffineTransform tmp = g2.getTransform();
            	g2.translate(objectInstance.state.posX + objectInstance.scale * img.getWidth() * zooming*0.5, objectInstance.state.posY + objectInstance.scale * img.getHeight() * zooming*0.5);
            	g2.rotate(Math.toRadians(objectInstance.state.rotation));
                g.drawImage(img, -(int) (objectInstance.scale * img.getWidth() * zooming*0.5),-(int) (objectInstance.scale * img.getHeight() * zooming * 0.5), (int) (objectInstance.scale * img.getWidth() * zooming), (int) (objectInstance.scale * img.getHeight() * zooming), null);
                g2.setTransform(tmp);
            }
        } else {//TODO add caching
            //double rotationRequired = Math.toRadians(objectInstance.getRotation());
            //double locationX = img.getWidth() / 2;
           // double locationY = img.getHeight() / 2;
            //AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX * objectInstance.scale, locationY * objectInstance.scale);
            //tx.scale(objectInstance.scale, objectInstance.scale);
            //AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
            //g.drawImage(op.filter(img, null), objectInstance.state.posX, objectInstance.state.posY, null);
        	Graphics2D g2 = (Graphics2D)g;
            AffineTransform tmp = g2.getTransform();
        	g2.translate(objectInstance.state.posX + objectInstance.scale * img.getWidth() * zooming*0.5, objectInstance.state.posY + objectInstance.scale * img.getHeight() * zooming*0.5);
        	g2.rotate(Math.toRadians(objectInstance.state.rotation));
            g.drawImage(img, -(int) (objectInstance.scale * img.getWidth() * zooming*0.5),-(int) (objectInstance.scale * img.getHeight() * zooming * 0.5), (int) (objectInstance.scale * img.getWidth() * zooming), (int) (objectInstance.scale * img.getHeight() * zooming), null);
            g2.setTransform(tmp);
        }
    }

    public static void drawBorder(Graphics g, Player player, ObjectInstance objectInstance, int borderWidth, Color color, double zooming) {
        if (objectInstance != null && objectInstance.state.owner_id != player.id) {
            g.setColor(color);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setStroke(new BasicStroke(borderWidth));
            g2d.drawRect(objectInstance.state.posX - borderWidth / 2, objectInstance.state.posY - borderWidth / 2, (int) (objectInstance.getWidth(player.id) * zooming) + borderWidth, (int) (objectInstance.getHeight(player.id) * zooming) + borderWidth);
        }
    }

    public static void drawStackBorder(GameInstance gameInstance, Graphics g, Player player, ObjectInstance objectInstance, int borderWidth, Color color, double zooming, boolean drawProperStack) {
        if (objectInstance != null) {
            if(isStackCollected(gameInstance, objectInstance))
            {
                if((!drawProperStack || getStack(gameInstance, objectInstance).size() > 1))
                    drawBorder(g, player, getStackTop(gameInstance, objectInstance), borderWidth, color, zooming);
            }
            else
            {
                Graphics2D g2d = (Graphics2D) g.create();
                g.setColor(color);
                g2d.setStroke(new BasicStroke(borderWidth));
                ObjectInstance stackTop = getStackTop(gameInstance, objectInstance);
                ObjectInstance stackBottom = getStackBottom(gameInstance, objectInstance);
                g2d.drawLine(stackTop.state.posX, stackTop.state.posY, stackTop.state.posX, stackTop.state.posY + (int)(stackTop.getHeight(player.id)*zooming));
                g2d.drawLine(stackBottom.state.posX + (int)(stackBottom.getWidth(player.id) * zooming), stackBottom.state.posY, stackBottom.state.posX+(int)(stackBottom.getWidth(player.id)*zooming), stackBottom.state.posY + (int)(stackBottom.getHeight(player.id)*zooming));
                g2d.drawLine(stackTop.state.posX, stackTop.state.posY, stackBottom.state.posX +(int)(stackBottom.getWidth(player.id)*zooming), stackBottom.state.posY);
                g2d.drawLine(stackTop.state.posX, stackTop.state.posY + (int)(stackTop.getHeight(player.id)*zooming), stackBottom.state.posX +(int)(stackBottom.getWidth(player.id)*zooming), stackBottom.state.posY + (int)(stackBottom.getHeight(player.id)*zooming));
            }
        }
    }

    public static void drawStackBorder(GameInstance gameInstance, Graphics g, Player player, ObjectInstance objectInstance, int borderWidth, Color color, int zooming) {
        drawStackBorder(gameInstance, g, player, objectInstance, borderWidth, color, zooming, false);
    }

    public static Shape setPrivateArea(Graphics g, double posX, double posY, double width, double height, double rotation, double zooming){
        AffineTransform tx = new AffineTransform();
        tx.scale(zooming, zooming);
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // set background color
        graphics.setPaint(Color.LIGHT_GRAY);
        Shape privateArea = new Arc2D.Double(posX, posY, width, height, 0, 180, Arc2D.OPEN);

        // set border color
        graphics.setColor(Color.GRAY);
        graphics.setStroke(new BasicStroke(2));
        graphics.fill(privateArea);

        return tx.createTransformedShape(privateArea);
    }

}
