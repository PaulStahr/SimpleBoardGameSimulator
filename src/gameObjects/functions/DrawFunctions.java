package gameObjects.functions;

import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import main.Player;
import org.slf4j.LoggerFactory;
import util.data.IntegerArrayList;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import org.slf4j.Logger;

import static gameObjects.functions.ObjectFunctions.*;

public class DrawFunctions {
    private static final Logger logger = LoggerFactory.getLogger(ObjectFunctions.class);
    public static void drawStack(Graphics g, IntegerArrayList stackList, GameInstance gameInstance, int playerId, double zooming) {
        if (haveSamePositions(gameInstance.objects.get(stackList.get(0)), gameInstance.objects.get(stackList.last()))) {
            IntegerArrayList newStackList = new IntegerArrayList();
            newStackList.add(gameInstance.objects.get(stackList.last()).id);
            stackList = newStackList;
        }
        for (int id : stackList) {
            drawObject(g,gameInstance.objects.get(id), playerId,zooming);
        }
    }

    public static void drawObject(Graphics g, ObjectInstance objectInstance, int playerId, double zooming) {
        BufferedImage img = objectInstance.go.getLook(objectInstance.state, playerId);
        if (objectInstance.getRotation() == 0) {//TODO: use Graphics rotation if possible
            if (objectInstance.state == null || img == null) {
                logger.error("Object state is null");
            } else {
                g.drawImage(img, (objectInstance.state.posX), (objectInstance.state.posY), (int) (objectInstance.scale * img.getWidth() * zooming), (int) (objectInstance.scale * img.getHeight() * zooming), null);
            }
        } else {//TODO add caching
            double rotationRequired = Math.toRadians(objectInstance.getRotation());
            double locationX = img.getWidth() / 2;
            double locationY = img.getHeight() / 2;
            AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX * objectInstance.scale, locationY * objectInstance.scale);
            tx.scale(objectInstance.scale, objectInstance.scale);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
            g.drawImage(op.filter(img, null), objectInstance.state.posX, objectInstance.state.posY, null);
        }
    }

    public static void drawBorder(Graphics g, Player player, ObjectInstance objectInstance, int borderWidth, Color color, double zooming) {
        if (objectInstance != null) {
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

}
