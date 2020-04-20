package main.gameObjects.functions;

import static main.gameObjects.functions.ObjectFunctions.getStack;
import static main.gameObjects.functions.ObjectFunctions.getStackBottom;
import static main.gameObjects.functions.ObjectFunctions.getStackTop;
import static main.gameObjects.functions.ObjectFunctions.haveSamePositions;
import static main.gameObjects.functions.ObjectFunctions.isStackCollected;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.gameObjects.instance.GameInstance;
import main.gameObjects.instance.ObjectInstance;
import main.gui.GamePanel;
import main.gameObjects.Player;
import main.util.data.IntegerArrayList;

public class DrawFunctions {
    private static final Logger logger = LoggerFactory.getLogger(ObjectFunctions.class);

    public static void drawBoard(GamePanel gamePanel, Graphics g, GameInstance gameInstance){
        g.clearRect(0, 0, gamePanel.getWidth(), gamePanel.getHeight());
        //TODO Florian:sometimes images are drawn twice (the active object?)
        g.drawString(String.valueOf(gamePanel.mouseWheelValue), gamePanel.mouseScreenX, gamePanel.mouseScreenY);
        g.drawImage(gameInstance.game.background, 0, 0, gamePanel.getWidth(), gamePanel.getHeight(), Color.BLACK, null);

        int privateAreaHeight = 700;
        int privateAreaWidth = 700;
        gamePanel.privateArea.setArea(gamePanel.getWidth()/2 - privateAreaWidth/2, gamePanel.getHeight()-privateAreaHeight/2, privateAreaWidth, privateAreaHeight, gamePanel.translateX, gamePanel.translateY, gamePanel.rotation, gamePanel.zooming);
        gamePanel.privateArea.draw(g);
    }

    public static void drawTokenObjects(GamePanel gamePanel, Graphics g, GameInstance gameInstance, ObjectInstance objectInstance, Player player, IntegerArrayList tmp){
        if (ObjectFunctions.isStackBottom(objectInstance)) {
            ObjectFunctions.getAboveStack(gameInstance, objectInstance, tmp);
            drawStack(gamePanel, g, tmp, gameInstance, player.id, 1);
            tmp.clear();
            getStack(gameInstance, objectInstance, tmp);
            int playerId = ObjectFunctions.getStackOwner(gameInstance, tmp);
            if (playerId != -1) {
                Player p = gameInstance.getPlayer(playerId);
                drawStackBorder(gameInstance, g, player, objectInstance, 10, p.color, 1, true, tmp);
            } else {
                if (tmp.size() > 1) {
                    g.setColor(gamePanel.stackColor);
                    drawStackBorder(gameInstance, g, player, objectInstance, 5, gamePanel.stackColor, 1, true, tmp);
                }
            }
        }
        tmp.clear();
    }

    public static void drawPlayerMarkers(GamePanel gamePanel, Graphics g, GameInstance gameInstance, Player player, String infoText)
    {
        Graphics2D g2 = (Graphics2D)g;
        AffineTransform tmp = g2.getTransform();
        for(Player p: gameInstance.players) {
            g.setColor(p.color);



            //if(isInPrivateArea(gamePanel, p.mouseXPos, p.mouseYPos))
                //TODO p.actionString = "Private Area";

            if(p.id == player.id)
            {
                g2.setTransform(new AffineTransform());
                g2.fillRect(gamePanel.mouseScreenX - 5, gamePanel.mouseScreenY - 5, 10, 10);
                g2.drawString(p.name, gamePanel.mouseScreenX + 15, gamePanel.mouseScreenY + 5);
                g2.drawString(p.actionString, gamePanel.mouseScreenX - 5, gamePanel.mouseScreenY - 20);
                g.drawString(infoText, gamePanel.mouseScreenX - 25, gamePanel.mouseScreenY + 5);
                g2.setTransform(tmp);
            }
            else {
                g2.scale(1/gamePanel.zooming, 1/gamePanel.zooming);
                g.fillRect(p.mouseXPos - 5, p.mouseYPos - 5, 10, 10);
                g.drawString(p.name, p.mouseXPos + 15, p.mouseYPos + 5);
                g.drawString(p.actionString, p.mouseXPos - 5, p.mouseYPos - 20);
            }
            //g.drawString(p.name, p.mouseXPos, p.mo    useYPos);
            //drawBorder(g, p, ObjectFunctions.getNearestObjectByPosition(gamePanel, gameInstance, p, p.mouseXPos, p.mouseYPos, 1, null), 10, p.color, 1);
            g2.scale(gamePanel.zooming, gamePanel.zooming);
        }
        g2.setTransform(tmp);
    }

    public static void drawActiveObject(GamePanel gamePanel, Graphics g, Player player, ObjectInstance activeObject) {
        int playerId = player == null ? -1 : player.id;
        if (activeObject != null && activeObject.state.owner_id != playerId) {
            drawObject(g, activeObject, playerId, 1);
            if (player != null) {
                drawBorder(g, player, activeObject, 10, player.color, 1);
            }
        }
        else if (activeObject != null && activeObject.state.inPrivateArea && activeObject.state.owner_id==playerId) {
            Graphics2D g2d = (Graphics2D)g;
            AffineTransform tmp = g2d.getTransform();
            AffineTransform transform = new AffineTransform();
            transform.translate(gamePanel.getWidth()/2, gamePanel.getHeight());
            transform.rotate(-Math.PI * 0.5 + Math.PI / (gamePanel.privateArea.privateObjects.size() * 2));
            transform.rotate(gamePanel.privateArea.privateObjects.indexOf(activeObject.id) * Math.PI / (gamePanel.privateArea.privateObjects.size()));
            transform.translate(-activeObject.getWidth(player.id) / 2, -activeObject.getHeight(player.id) / 2);
            transform.translate(0, -250);

            drawPrivateAreaBorder(g, player, activeObject, 10, player.color, transform);
            g2d.setTransform(tmp);
        }
    }

    public static void drawSelectedObjects(GamePanel gamePanel, Graphics g,GameInstance gameInstance, Player player, IntegerArrayList tmp){
        for(int id: gamePanel.selectedObjects)
        {
            ObjectInstance currentObject = gameInstance.objects.get(id);
            if (ObjectFunctions.isStackBottom(currentObject)&& currentObject.state.owner_id != player.id)
            {
                drawStackBorder(gameInstance, g, player, currentObject, 5, player.color, 1, tmp);
                tmp.clear();
            }
        }
        Graphics2D g2 = (Graphics2D)g;
        g2.setTransform(new AffineTransform());
        if (gamePanel.activeObject == null && gamePanel.selectWidth > 0 && gamePanel.selectHeight > 0 && !gamePanel.mouseInPrivateArea){
            g.setColor(player.color);
            g.drawRect(gamePanel.beginSelectPosX, gamePanel.beginSelectPosY, gamePanel.selectWidth, gamePanel.selectHeight);
        }
    }

    public static void drawTokensInPrivateArea(GamePanel gamePanel, Graphics g, GameInstance gameInstance){
        Graphics2D g2 = (Graphics2D)g;
        AffineTransform tmp = g2.getTransform();
        g2.setTransform(new AffineTransform());
        int playerId = gamePanel.player == null ? -1 : gamePanel.player.id;
        g2.translate(gamePanel.getWidth() / 2, gamePanel.getHeight());
        if (gamePanel.privateArea.privateObjects.size() != 0)
        {
            g2.rotate(-Math.PI * 0.5 + Math.PI / (gamePanel.privateArea.privateObjects.size() * 2));
            for(int id : gamePanel.privateArea.privateObjects)
            {
                ObjectInstance objectInstance = gameInstance.objects.get(id);
                BufferedImage img = objectInstance.go.getLook(objectInstance.state, playerId);
                g2.translate(0, -250);
                g2.drawImage(img, -(int) (objectInstance.scale * img.getWidth() *0.5),-(int) (objectInstance.scale * img.getHeight() * 0.5), (int) (objectInstance.scale * img.getWidth()), (int) (objectInstance.scale * img.getHeight()), null);
                g2.translate(0, 250);
                g2.rotate(Math.PI / (gamePanel.privateArea.privateObjects.size()));
            }
        }
        g2.setTransform(tmp);
    }

    public static void drawStack(GamePanel gamePanel, Graphics g, IntegerArrayList stackList, GameInstance gameInstance, int playerId, double zooming) {
        /*if (isStackInPrivateArea(gamePanel, gameInstance, stackList))
        {
        }*/
        if(true){
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

    public static void drawPrivateAreaBorder(Graphics g, Player player, ObjectInstance objectInstance, int borderWidth, Color color, AffineTransform transform){
        if (objectInstance != null) {
            g.setColor(color);
            Graphics2D g2d = (Graphics2D) g.create();
            AffineTransform tmp = g2d.getTransform();
            if(transform != null)
                g2d.setTransform(transform);
            g2d.setStroke(new BasicStroke(borderWidth));
            g2d.drawRect(-borderWidth/2 ,-borderWidth/2, (objectInstance.getWidth(player.id)) + borderWidth, (objectInstance.getHeight(player.id)) + borderWidth);
            g2d.setTransform(tmp);
        }
    }

    public static void drawBorder(Graphics g, Player player, ObjectInstance objectInstance, int borderWidth, Color color, double zooming, AffineTransform transform) {
        if (objectInstance != null) {
            g.setColor(color);
            Graphics2D g2d = (Graphics2D) g.create();
            AffineTransform tmp = g2d.getTransform();
            if(transform != null)
                g2d.setTransform(transform);
            g2d.setStroke(new BasicStroke(borderWidth));
            g2d.drawRect(objectInstance.state.posX - borderWidth / 2, objectInstance.state.posY - borderWidth / 2, (int) (objectInstance.getWidth(player.id) * zooming) + borderWidth, (int) (objectInstance.getHeight(player.id) * zooming) + borderWidth);
            g2d.setTransform(tmp);
        }
    }
    public static void drawBorder(Graphics g, Player player, ObjectInstance objectInstance, int borderWidth, Color color, double zooming) {
        drawBorder(g, player, objectInstance, borderWidth, color, zooming, null);
    }

    public static void drawStackBorder(GameInstance gameInstance, Graphics g, Player player, ObjectInstance objectInstance, int borderWidth, Color color, double zooming, boolean drawProperStack, IntegerArrayList tmp) {
        if (objectInstance != null && player != null) {
            if(isStackCollected(gameInstance, objectInstance))
            {
            	getStack(gameInstance, objectInstance, tmp);
                if((!drawProperStack || tmp.size() > 1))
                    drawBorder(g, player, getStackTop(gameInstance, objectInstance), borderWidth, color, zooming);
            	tmp.clear();
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

    public static void drawStackBorder(GameInstance gameInstance, Graphics g, Player player, ObjectInstance objectInstance, int borderWidth, Color color, int zooming, IntegerArrayList tmp) {
        drawStackBorder(gameInstance, g, player, objectInstance, borderWidth, color, zooming, false, tmp);
    }
}
