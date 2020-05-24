package gameObjects.functions;

import static gameObjects.functions.ObjectFunctions.isStackCollected;
import static java.lang.Integer.min;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.definition.GameObjectDice;
import gameObjects.definition.GameObjectFigure;
import gameObjects.definition.GameObjectToken;
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
        g.drawString(String.valueOf(gamePanel.mouseWheelValue), gamePanel.mouseScreenX, gamePanel.mouseScreenY);
        g.drawImage(gameInstance.game.background, 0, 0, gamePanel.getWidth(), gamePanel.getHeight(), Color.BLACK, null);

        if(gamePanel.table != null) {
            gamePanel.table.drawCompleteTable(gamePanel, gameInstance, g);
        }
    }

    public static void drawPrivateArea(GamePanel gamePanel, Graphics g){
        int privateAreaHeight = 700;
        int privateAreaWidth = 700;
        gamePanel.privateArea.setArea(gamePanel.getWidth()/2 - privateAreaWidth/2, gamePanel.getHeight()-privateAreaHeight/2, privateAreaWidth, privateAreaHeight, gamePanel.translateX, gamePanel.translateY, gamePanel.rotation, gamePanel.zooming);
        gamePanel.privateArea.draw(g, gamePanel.getWidth()/2, gamePanel.getHeight());
    }


    public static void drawObjectsFromList(GamePanel gamePanel, Graphics g, GameInstance gameInstance, Player player, IntegerArrayList ial){
        //Draw all objects not in some private area
        ArrayList<ObjectInstance> oiList = new ArrayList<>();
        for (int idx : ial){
            oiList.add(gameInstance.getObjectInstanceByIndex(idx));
        }
        drawObjectsFromList(gamePanel,g,gameInstance,player,oiList,ial);
    }

    public static void drawObjectsFromList(GamePanel gamePanel, Graphics g, GameInstance gameInstance, Player player, ArrayList<ObjectInstance> oiList, IntegerArrayList ial) {
        for (int i = 0; i < oiList.size(); ++i){
            ObjectInstance oi = oiList.get(i);
            if (oi.state.owner_id != player.id || !oi.state.inPrivateArea || oi.state.isActive) {
                try {
                    if (oi.go instanceof GameObjectToken) {
                        drawTokenObjects(gamePanel, g, gameInstance, oi, player, ial);
                    }
                    else if (oi.go instanceof GameObjectDice) {
                        drawDiceObjects(gamePanel, g, gameInstance, oi, player, 1);
                    }
                    else if (oi.go instanceof GameObjectFigure) {
                        drawFigureObjects(gamePanel, g, gameInstance, oi, player, 1);
                    }
                }catch(Exception e)
                {
                    logger.error("Error in drawing Tokens", e);
                }
            }
        }
    }

        public static void drawTokenObjects(GamePanel gamePanel, Graphics g, GameInstance gameInstance, ObjectInstance objectInstance, Player player, IntegerArrayList tmp){
        //draw tokens in the order of its draw value
        if (ObjectFunctions.isStackTop(objectInstance)) {
            tmp.clear();
            ObjectFunctions.getBelowStack(gameInstance, objectInstance, tmp);
            drawStack(gamePanel, g, tmp, gameInstance, player, 1);
        }
        tmp.clear();
    }

    public static void drawDiceObjects(GamePanel gamePanel, Graphics g, GameInstance gameInstance, ObjectInstance oi, Player player, int zooming){
        drawObject(gamePanel, g, gameInstance, gameInstance.getObjectInstanceById(oi.id), player, zooming, 5);
    }

    public static void drawFigureObjects(GamePanel gamePanel, Graphics g, GameInstance gameInstance, ObjectInstance oi, Player player, int zooming){
        drawObject(gamePanel, g, gameInstance, gameInstance.getObjectInstanceById(oi.id), player, zooming, 5);
    }

    public static void drawPlayerPositions(GamePanel gamePanel, Graphics g, GameInstance gameInstance, Player player, String infoText) {
        Graphics2D g2 = (Graphics2D)g;
        AffineTransform tmp = g2.getTransform();
        BasicStroke wideStroke = new BasicStroke(4);
        BasicStroke basicStroke = new BasicStroke();
        double determinant = tmp.getDeterminant();
        for(int pIdx = 0;pIdx < gameInstance.getPlayerNumber();++pIdx) {
            Player p = gameInstance.getPlayerByIndex(pIdx);
            g.setColor(p.color);
            g2.setTransform(tmp);
			g2.transform(p.screenToBoardTransformation);
			double playerDeterminant = p.screenToBoardTransformation.getDeterminant();
	        g2.setStroke(wideStroke);
            //g2.drawLine(40, p.screenHeight, p.screenWidth, p.screenHeight);
            int imageNumber = p.id % 10;
            if (p.id != player.id)
			{
                BufferedImage img = gamePanel.playerImages[imageNumber];
                g2.translate((p.screenWidth)/2, p.screenHeight - 20);
                double scale = 0.5 / Math.sqrt(playerDeterminant * determinant);
                g2.scale(scale, scale);
                g2.translate(-img.getWidth()/2, 0);
                g2.drawImage(img, null, 0, -10);
                g2.scale(5, 5);
                g2.drawString(p.getName(), 15, 60);
			}
            g2.setTransform(tmp);

            //draw mouse position of other players
            g2.setStroke(basicStroke);
            g2.translate(p.mouseXPos-5, p.mouseYPos-5);

            AffineTransform newTmp = g2.getTransform();
            AffineTransform newTransform = new AffineTransform();
            newTransform.translate(newTmp.getTranslateX(), newTmp.getTranslateY());



            g2.setTransform(newTransform);

            g2.fillRect(0, 0, 10, 10);
            g2.drawString(p.getName(),  15,  5);

            if(p.id == player.id) {
                g2.drawString(player.actionString, 0, -15);
                g2.drawString(infoText, -20, 10);
            }
        }



        g2.setTransform(tmp);
    }

    public static void drawSelection(GamePanel gamePanel, Graphics g, Player player){
        Graphics2D g2 = (Graphics2D)g;
        g2.setTransform(new AffineTransform());
        if (gamePanel.activeObject == null  && !gamePanel.mouseInPrivateArea){
            g.setColor(player.color);
            g.drawRect(min(gamePanel.beginSelectPosScreenX, gamePanel.beginSelectPosScreenX+gamePanel.selectWidth),min(gamePanel.beginSelectPosScreenY, gamePanel.beginSelectPosScreenY+gamePanel.selectHeight), abs(gamePanel.selectWidth), abs(gamePanel.selectHeight));
        }
    }

    /** Draws all objects which are in the private area of player
     * @param gamePanel
     * @param g
     * @param gameInstance
     * @param player
     * @param activeObject
     */
    public static void drawTokensInPrivateArea(GamePanel gamePanel, Graphics g, GameInstance gameInstance, Player player, ObjectInstance activeObject){
        Graphics2D g2 = (Graphics2D)g;
        AffineTransform tmp = g2.getTransform();
        g2.setTransform(new AffineTransform());
        int playerId = gamePanel.player == null ? -1 : gamePanel.player.id;
        g2.translate(gamePanel.getWidth() / 2, gamePanel.getHeight());

        if (gamePanel.privateArea.objects.size() != 0) {
            int extraSpace; //Private Area needs extra space if object is dragged into it
            if (gamePanel.privateArea.currentDragPosition != -1 && gamePanel.activeObject != null && gamePanel.activeObjects.size() != 0 && !gamePanel.isSelectStarted) {
                extraSpace = 1;
            } else {
                extraSpace = 0;
            }

            g2.rotate(-Math.PI * 0.5 + Math.PI / ((gamePanel.privateArea.objects.size() + extraSpace) * 2));
            g2.scale(gamePanel.privateArea.zooming, gamePanel.privateArea.zooming);
            for (int i = 0; i < gamePanel.privateArea.objects.size() + extraSpace; ++i) {
                if (extraSpace == 0) {
                    ObjectInstance objectInstance = gameInstance.getObjectInstanceById(gamePanel.privateArea.objects.getI(i));
                    if (!objectInstance.state.isActive) {
                        BufferedImage img = objectInstance.go.getLook(objectInstance.state, playerId);
                        g2.translate(0, -250);

                        g2.drawImage(img, -(int) (objectInstance.scale * img.getWidth() * 0.5), -(int) (objectInstance.scale * img.getHeight() * 0.5), (int) (objectInstance.scale * img.getWidth()), (int) (objectInstance.scale * img.getHeight()), null);
                        g2.translate(0, 250);
                        g2.rotate(Math.PI / (gamePanel.privateArea.objects.size() + extraSpace));
                    }
                } else {
                    if (i == gamePanel.privateArea.currentDragPosition) {
                        g2.translate(0, -250);
                        g2.translate(0, 250);
                        g2.rotate(Math.PI / (gamePanel.privateArea.objects.size() + extraSpace));
                    } else if (i < gamePanel.privateArea.currentDragPosition) {
                        ObjectInstance objectInstance = gameInstance.getObjectInstanceById(gamePanel.privateArea.objects.getI(i));
                        if (!objectInstance.state.isActive) {
                            BufferedImage img = objectInstance.go.getLook(objectInstance.state, playerId);
                            g2.translate(0, -250);
                            g2.drawImage(img, -(int) (objectInstance.scale * img.getWidth() * 0.5), -(int) (objectInstance.scale * img.getHeight() * 0.5), (int) (objectInstance.scale * img.getWidth()), (int) (objectInstance.scale * img.getHeight()), null);
                            g2.translate(0, 250);
                            g2.rotate(Math.PI / (gamePanel.privateArea.objects.size() + extraSpace));
                        }
                    } else if (i > gamePanel.privateArea.currentDragPosition) {
                        ObjectInstance objectInstance = gameInstance.getObjectInstanceById(gamePanel.privateArea.objects.getI(i - 1));
                        if (!objectInstance.state.isActive) {
                            BufferedImage img = objectInstance.go.getLook(objectInstance.state, playerId);
                            g2.translate(0, -250);
                            g2.drawImage(img, -(int) (objectInstance.scale * img.getWidth() * 0.5), -(int) (objectInstance.scale * img.getHeight() * 0.5), (int) (objectInstance.scale * img.getWidth()), (int) (objectInstance.scale * img.getHeight()), null);
                            g2.translate(0, 250);
                            g2.rotate(Math.PI / (gamePanel.privateArea.objects.size() + extraSpace));
                        }
                    }

                }
            }
        }

        g2.setTransform(tmp);
        if (activeObject != null && !activeObject.state.isActive && activeObject.state.owner_id == playerId) {
            Graphics2D g2d = (Graphics2D)g;
            tmp = g2d.getTransform();
            AffineTransform transform = new AffineTransform();

            transform.translate(gamePanel.getWidth()/2, gamePanel.getHeight());
            transform.rotate(-Math.PI * 0.5 + Math.PI / (gamePanel.privateArea.objects.size() * 2));
            transform.rotate(gamePanel.privateArea.objects.indexOf(activeObject.id) * Math.PI / (gamePanel.privateArea.objects.size()));
            transform.scale(gamePanel.privateArea.zooming, gamePanel.privateArea.zooming);
            transform.translate(-activeObject.getWidth(player.id) / 2, -activeObject.getHeight(player.id) / 2);
            transform.translate(0, -250);
            drawPrivateAreaBorder(g, player, activeObject, 5, player.color, transform);
            g2d.setTransform(tmp);
        }
    }

    public static void drawStack(GamePanel gamePanel, Graphics g, IntegerArrayList stackList, GameInstance gameInstance, Player player, double zooming) {
        if (stackList.size()>0) {
            if (isStackCollected(gameInstance,gameInstance.getObjectInstanceById(stackList.get(0)))){
                int oiId = gameInstance.getObjectInstanceById(stackList.getI(0)).id;
                stackList.clear();
                stackList.add(oiId);
            }
            for (int id : stackList) {
                drawObject(gamePanel, g, gameInstance, gameInstance.getObjectInstanceById(id), player, zooming, 5);
            }
        }
    }

    /** Draw the objects in the game
     * @param gamePanel
     * @param g
     * @param gameInstance
     * @param objectInstance
     * @param player
     * @param zooming
     * @param borderWidth
     */
    public static void drawObject(GamePanel gamePanel, Graphics g, GameInstance gameInstance, ObjectInstance objectInstance, Player player, double zooming, int borderWidth) {
        BufferedImage img = objectInstance.go.getLook(objectInstance.state, player.id);
        if (objectInstance.state == null || img == null) {
            logger.error("Object state is null");
        } else {
            Graphics2D g2 = (Graphics2D) g;
            AffineTransform tmp = g2.getTransform();
            g2.translate(objectInstance.state.posX + objectInstance.scale * img.getWidth() * zooming * 0.5, objectInstance.state.posY + objectInstance.scale * img.getHeight() * zooming * 0.5);
            //draw object not in private area
            if (gamePanel.privateArea == null || !gamePanel.privateArea.containsBoardCoordinates(objectInstance.state.posX, objectInstance.state.posY) || !objectInstance.state.isActive){
                g2.rotate(Math.toRadians(objectInstance.state.rotation));
                g.drawImage(img, -(int) (objectInstance.scale * img.getWidth() * zooming * 0.5), -(int) (objectInstance.scale * img.getHeight() * zooming * 0.5), (int) (objectInstance.scale * img.getWidth() * zooming), (int) (objectInstance.scale * img.getHeight() * zooming), null);
            }
            //draw object above private area
            else if (objectInstance.state.isActive){
                int insertPosition = gamePanel.privateArea.getInsertPosition(gamePanel.mouseScreenX, gamePanel.mouseScreenY, gamePanel.getWidth()/2, gamePanel.getHeight());
                //g2.rotate(player.screenToBoardTransformation.getDeterminant());
                g2.rotate(-Math.PI * 0.5 + Math.PI / ((gamePanel.privateArea.objects.size() + 1) * 2));
                g2.rotate(insertPosition * Math.PI / (gamePanel.privateArea.objects.size() + 1));
                g2.rotate(-gamePanel.rotation);
                g2.scale(gamePanel.privateArea.zooming/(sqrt(g2.getTransform().getDeterminant())), gamePanel.privateArea.zooming/(sqrt(g2.getTransform().getDeterminant())));
                g2.drawImage(img, -(int) (objectInstance.scale * img.getWidth()  * 0.5), -(int) (objectInstance.scale * img.getHeight()  * 0.5), (int) (objectInstance.scale * img.getWidth() ), (int) (objectInstance.scale * img.getHeight() ), null);
            }

            //Draw Border around objects
            g2.setStroke(new BasicStroke(borderWidth));

            if (objectInstance.state.owner_id != -1) {
                    Player playerOwner = gameInstance.getPlayerById(objectInstance.state.owner_id);
                    g2.setColor(playerOwner.color);
                    g2.drawString(playerOwner.getName() + " Hand Cards", -(int) (objectInstance.scale * img.getWidth() * zooming * 0.5), -(int) (objectInstance.scale * img.getHeight() * zooming * 0.5) - 20);
            }
            else if(objectInstance.state.isActive || gamePanel.selectedObjects.contains(objectInstance.id)){
                g2.setColor(player.color);
            }
            else if(ObjectFunctions.isStackTop(objectInstance) && !ObjectFunctions.isStackBottom(objectInstance)){
                g2.setStroke(new BasicStroke(borderWidth));
                g2.setColor(gamePanel.stackColor);
            }

            if (objectInstance.go instanceof GameObjectToken) {
                if (objectInstance.state.isActive || gamePanel.selectedObjects.contains(objectInstance.id) || objectInstance.state.owner_id != -1 || (ObjectFunctions.isStackTop(objectInstance) && !ObjectFunctions.isStackBottom(objectInstance))) {
                    g2.drawRect(-(int) (objectInstance.scale * img.getWidth() * zooming * 0.5) - borderWidth/2, -(int) (objectInstance.scale * img.getHeight() * zooming * 0.5) - borderWidth/2, (int) (objectInstance.scale * img.getWidth() * zooming) + borderWidth/2, (int) (objectInstance.scale * img.getHeight() * zooming) + borderWidth / 2);
                }
            }
            else{
                if (objectInstance.state.isActive || gamePanel.selectedObjects.contains(objectInstance.id)) {
                    g2.drawRect(-(int) (objectInstance.scale * img.getWidth() * zooming * 0.5) - borderWidth/2, -(int) (objectInstance.scale * img.getHeight() * zooming * 0.5) - borderWidth/2, (int) (objectInstance.scale * img.getWidth() * zooming) + borderWidth/2, (int) (objectInstance.scale * img.getHeight() * zooming) + borderWidth / 2);
                }
            }
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
}
