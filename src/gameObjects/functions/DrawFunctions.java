package gameObjects.functions;

import static gameObjects.functions.ObjectFunctions.isStackBottom;
import static gameObjects.functions.ObjectFunctions.isStackCollected;
import static gameObjects.functions.ObjectFunctions.isStackOwned;
import static gameObjects.functions.ObjectFunctions.isStackTop;
import static java.lang.Integer.min;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gameObjects.definition.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.Texture;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import geometry.Vector2d;
import gui.game.GamePanel;
import gui.game.Player;
import util.data.IntegerArrayList;

public class DrawFunctions {
    private static final Logger logger = LoggerFactory.getLogger(ObjectFunctions.class);

    public static void drawBackground(GamePanel gamePanel, Graphics g, GameInstance gameInstance){

        g.clearRect(0, 0, gamePanel.getWidth(), gamePanel.getHeight());
        //TODO Florian:sometimes images are drawn twice (the active object?)
        g.drawString(String.valueOf(gamePanel.mouseWheelValue), gamePanel.mouseScreenX, gamePanel.mouseScreenY);
        try {
            g.drawImage(gameInstance.game.background.getImage(), 0, 0, gamePanel.getWidth(), gamePanel.getHeight(), Color.BLACK, null);
        } catch (IOException e) {
            logger.error("Can't draw background", e);
        }

        if(gamePanel.table != null) {
            gamePanel.table.drawCompleteTable(gamePanel, gameInstance, g);
        }
    }

    public static void drawPrivateArea(GamePanel gamePanel, Graphics g){
        int privateAreaHeight = 750;
        int privateAreaWidth = 750;
        gamePanel.privateArea.setArea(gamePanel.getWidth()/2 - privateAreaWidth/2, gamePanel.getHeight()-privateAreaHeight/2, privateAreaWidth, privateAreaHeight, gamePanel.translateX, gamePanel.translateY, gamePanel.rotation, gamePanel.zooming);
        gamePanel.privateArea.draw(g, gamePanel.getWidth()/2, gamePanel.getHeight());
    }


    public static void drawObjectsFromList(GamePanel gamePanel, Graphics g, GameInstance gameInstance, Player player, IntegerArrayList ial){
        //Draw all objects
        IntegerArrayList tmp = new IntegerArrayList();
        drawObjectsFromList(gamePanel,g,gameInstance,player,ial,tmp);
    }

    public static void drawObjectsFromList(GamePanel gamePanel, Graphics g, GameInstance gameInstance, Player player, IntegerArrayList oiList, IntegerArrayList ial) {
        for (int i = 0; i < oiList.size(); ++i){
            ObjectInstance oi = gameInstance.getObjectInstanceById(oiList.get(i));
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
                    else if (oi.go instanceof GameObjectBook){
                        drawBookObjects(gamePanel, g, gameInstance, oi, player, 1);
                    }
                    else if (oi.go instanceof GameObjectBox){
                        drawBoxObjects(gamePanel, g, gameInstance, oi, player, 1);
                    }
                }catch(Exception e)
                {
                    logger.error("Error in drawing Tokens", e);
                }
            }
        }
    }

    private static void drawBoxObjects(GamePanel gamePanel, Graphics g, GameInstance gameInstance, ObjectInstance oi, Player player, int zooming) {
        drawObject(gamePanel, g, gameInstance, gameInstance.getObjectInstanceById(oi.id), player, zooming, 5);
    }

    public static void drawBookObjects(GamePanel gamePanel, Graphics g, GameInstance gameInstance, ObjectInstance oi, Player player, int zooming) {
        drawObject(gamePanel, g, gameInstance, gameInstance.getObjectInstanceById(oi.id), player, zooming, 5);
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
        for(int pIdx = 0;pIdx < gameInstance.getPlayerCount(true);++pIdx) {
            Player p = gameInstance.getPlayerByIndex(pIdx);
            g.setColor(p.color);
            g2.setTransform(tmp);
			g2.transform(p.screenToBoardTransformation);
			double playerDeterminant = p.screenToBoardTransformation.getDeterminant();
	        g2.setStroke(wideStroke);
            //g2.drawLine(40, p.screenHeight, p.screenWidth, p.screenHeight);
            int imageNumber = pIdx % 10;

            BufferedImage img = gamePanel.playerImages[imageNumber];
            g2.translate(p.screenWidth/2, p.screenHeight - 20);
            double scale = 0.5 / Math.sqrt(playerDeterminant * determinant);
            g2.scale(scale, scale);
            g2.translate(-img.getWidth()/2, 0);
            g2.drawImage(img, null, 0, -10);
            g2.scale(5, 5);
            g2.drawString(p.getName(), 5, -10);
            g2.setTransform(tmp);
            //draw mouse position of other players
            g2.setStroke(basicStroke);
            g2.translate(p.mouseXPos, p.mouseYPos);

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
        if (gamePanel.hoveredObject != null && gamePanel.isDebug) {
            g2.fillRect(gamePanel.hoveredObject.state.posX-5, gamePanel.hoveredObject.state.posY-5, 10, 10);
            if (gamePanel.hoveredObject.state.inPrivateArea && gamePanel.hoveredObject.state.owner_id != -1) {
                Vector2d mouseBoardPos = new Vector2d();
                gamePanel.screenToBoardPos(gamePanel.mouseScreenX, gamePanel.mouseScreenY, mouseBoardPos);
                Point2D transformedPoint = new Point2D.Double(mouseBoardPos.getXI(), mouseBoardPos.getYI());
                gamePanel.privateArea.transformPoint(transformedPoint, transformedPoint);
                int index = gamePanel.privateArea.getPrivateObjectIndexByPosition((int) transformedPoint.getX(), (int) transformedPoint.getY());
                int oId = gamePanel.privateArea.getObjectIdByPosition((int) transformedPoint.getX(), (int) transformedPoint.getY());


                if (gamePanel.privateArea.privateObjectsPositions.size() > index && index != -1) {
                    AffineTransform temp = g2.getTransform();
                    AffineTransform affineTransform = new AffineTransform();
                    affineTransform.translate(gamePanel.getWidth()/2, gamePanel.getHeight()-2*gamePanel.privateArea.objects.size());
                    affineTransform.rotate(-Math.PI * 0.5 + Math.PI / (gamePanel.privateArea.objects.size() * 2));
                    affineTransform.rotate(gamePanel.privateArea.objects.indexOf(oId) * Math.PI / (gamePanel.privateArea.objects.size()));
                    affineTransform.scale(gamePanel.privateArea.zooming, gamePanel.privateArea.zooming);
                    affineTransform.translate(0, -250);

                    g2.setTransform(affineTransform);
                    g2.fillRect(- 5, - 5, 10, 10);
                    g2.setTransform(temp);
                }
                g2.drawString(String.valueOf(index), 0, 0);
            }
        }
    }

    public static void drawSelection(GamePanel gamePanel, Graphics g, Player player){
        Graphics2D g2 = (Graphics2D)g;
        g2.setTransform(new AffineTransform());
        if (gamePanel.hoveredObject == null  && !gamePanel.mouseInPrivateArea){
            g2.setColor(player.color);
            if(min(abs(gamePanel.selectWidth), abs(gamePanel.selectHeight)) > 0) {
                Stroke stroke = new BasicStroke(5.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 10.0f, new float[]{20.0f, 20.0f}, 0.0f);
                g2.setStroke(stroke);
                g2.drawRect(min(gamePanel.beginSelectPosScreenX, gamePanel.beginSelectPosScreenX + gamePanel.selectWidth), min(gamePanel.beginSelectPosScreenY, gamePanel.beginSelectPosScreenY + gamePanel.selectHeight), abs(gamePanel.selectWidth), abs(gamePanel.selectHeight));
            }
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
        int playerId = gamePanel.getPlayerId();
        g2.translate(gamePanel.getWidth() / 2, gamePanel.getHeight()-2*gamePanel.privateArea.objects.size());

        if (gamePanel.privateArea.objects.size() != 0) {
            int extraSpace; //Private Area needs extra space if object is dragged into it
            if (gamePanel.privateArea.currentDragPosition != -1 && gamePanel.hoveredObject != null && gamePanel.getNumberOfSelectedObjects() != 0 && !gamePanel.isSelectStarted) {
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
                        BufferedImage img = objectInstance.go.getLook(objectInstance.state, playerId).getImageNoExc();
                        g2.translate(0, -250);
                        Point2D ElementPosition = new Point2D.Double();
                        ElementPosition.setLocation(g2.getTransform().getTranslateX(), g2.getTransform().getTranslateY());
                        g2.drawImage(img, -(int) (objectInstance.scale * img.getWidth() * 0.5), -(int) (objectInstance.scale * img.getHeight() * 0.5), (int) (objectInstance.scale * img.getWidth()), (int) (objectInstance.scale * img.getHeight()), null);
                        g2.translate(0, 250);
                        g2.rotate(Math.PI / (gamePanel.privateArea.objects.size() + extraSpace));
                        if (gamePanel.privateArea.privateObjectsPositions.size() <= i){
                            gamePanel.privateArea.privateObjectsPositions.add(ElementPosition);
                        }
                        else{
                            gamePanel.privateArea.privateObjectsPositions.set(i, ElementPosition);
                        }
                    }
                } else {
                    //TODO out ouf bound errors
                    if (i == gamePanel.privateArea.currentDragPosition) {
                        g2.translate(0, -250);
                        g2.translate(0, 250);
                        g2.rotate(Math.PI / (gamePanel.privateArea.objects.size() + extraSpace));
                    } else if (i>=0 && i < gamePanel.privateArea.currentDragPosition) {
                        ObjectInstance objectInstance = gameInstance.getObjectInstanceById(gamePanel.privateArea.objects.getI(i));
                        if (!objectInstance.state.isActive) {
                            BufferedImage img = objectInstance.go.getLook(objectInstance.state, playerId).getImageNoExc();
                            g2.translate(0, -250);
                            g2.drawImage(img, -(int) (objectInstance.scale * img.getWidth() * 0.5), -(int) (objectInstance.scale * img.getHeight() * 0.5), (int) (objectInstance.scale * img.getWidth()), (int) (objectInstance.scale * img.getHeight()), null);
                            g2.translate(0, 250);
                            g2.rotate(Math.PI / (gamePanel.privateArea.objects.size() + extraSpace));
                        }
                    } else if (i>=0 && i > gamePanel.privateArea.currentDragPosition) {
                        ObjectInstance objectInstance = gameInstance.getObjectInstanceById(gamePanel.privateArea.objects.getI(i - 1));
                        if (!objectInstance.state.isActive) {
                            BufferedImage img = objectInstance.go.getLook(objectInstance.state, playerId).getImageNoExc();
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
            AffineTransform transform = new AffineTransform();

            transform.translate(gamePanel.getWidth()/2, gamePanel.getHeight()-2*gamePanel.privateArea.objects.size());
            transform.rotate(-Math.PI * 0.5 + Math.PI / (gamePanel.privateArea.objects.size() * 2));
            transform.rotate(gamePanel.privateArea.objects.indexOf(activeObject.id) * Math.PI / (gamePanel.privateArea.objects.size()));
            transform.scale(gamePanel.privateArea.zooming, gamePanel.privateArea.zooming);
            transform.translate(-activeObject.getWidth(player.id) / 2, -activeObject.getHeight(player.id) / 2);
            transform.translate(0, -250);
            drawPrivateAreaBorder(g, gameInstance, player, activeObject, 5, player.color, transform);
            g2.setTransform(tmp);
        }
    }

    public static void drawStack(GamePanel gamePanel, Graphics g, IntegerArrayList stackList, GameInstance gameInstance, Player player, double zooming) {
        if (stackList.size()>0) {
            if (isStackCollected(gameInstance,gameInstance.getObjectInstanceById(stackList.get(0))) || isStackOwned(gameInstance, stackList)){
                int oiId = gameInstance.getObjectInstanceById(stackList.getI(0)).id;
                stackList.clear();
                stackList.add(oiId);
            }
            for (int idx = stackList.size() - 1 ; idx >= 0; --idx) {
                drawObject(gamePanel, g, gameInstance, gameInstance.getObjectInstanceById(stackList.get(idx)), player, zooming, 5);
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
        if (objectInstance.go == null)      {throw new NullPointerException("GameObject is null");}
        if (player == null)                 {throw new NullPointerException("Player is null");}
        if (objectInstance.state == null)   {throw new NullPointerException("State is null");}
        Texture look = objectInstance.go.getLook(objectInstance.state, player.id);
        if (look == null)                   {throw new NullPointerException("Look is null " + objectInstance.go.getClass());}
        BufferedImage img = objectInstance.go.getLook(objectInstance.state, player.id).getImageNoExc();
        if (objectInstance.state == null || img == null) {
            logger.error("Object state is null");
        }
        else {
            Graphics2D g2 = (Graphics2D) g;
            AffineTransform tmp = g2.getTransform();
            g2.translate(objectInstance.state.posX, objectInstance.state.posY);
            //draw objects
            if (gamePanel.privateArea == null || !gamePanel.mouseInPrivateArea || !objectInstance.state.isActive) {
                g2.rotate(Math.toRadians(objectInstance.state.rotation));
                //Draw Dice Object
                if (objectInstance.go instanceof GameObjectDice) {
                    GameObjectDice.DiceState diceState = (GameObjectDice.DiceState) objectInstance.state;
                    if (diceState.unfold) {
                        List<BufferedImage> bufferedImages = new ArrayList<>();
                        DrawFunctions.unfoldDice(objectInstance, bufferedImages);
                        int side = diceState.side;
                        for (int i = 0; i < bufferedImages.size(); ++i) {
                            BufferedImage bufferedImage = bufferedImages.get(i);
                            int drawPos = i - side;
                            if (objectInstance.state.owner_id == -1) {
                                g2.translate(drawPos * -(int) (objectInstance.scale * img.getWidth() * zooming + 5.0f), 0);
                                g2.drawImage(bufferedImage, -(int) (objectInstance.scale * img.getWidth() * zooming * 0.5), -(int) (objectInstance.scale * img.getHeight() * zooming * 0.5), (int) (objectInstance.scale * img.getWidth() * zooming), (int) (objectInstance.scale * img.getHeight() * zooming), null);
                                g2.translate(drawPos * (int) (objectInstance.scale * img.getWidth() * zooming + 5.0f), 0);
                            }
                        }
                    }
                }
                //Draw Objects not in private area
                if (objectInstance.state.owner_id == -1 || ObjectFunctions.objectIsSelected(gameInstance, objectInstance.id)) {
                    g.drawImage(img, -(int) (objectInstance.scale * img.getWidth() * zooming * 0.5), -(int) (objectInstance.scale * img.getHeight() * zooming * 0.5), (int) (objectInstance.scale * img.getWidth() * zooming), (int) (objectInstance.scale * img.getHeight() * zooming), null);
                }
                //Draw Objects in private area
                else if (objectInstance.state.owner_id != -1){
                    //g.drawImage(img, -(int) (objectInstance.scale * img.getWidth() * zooming * 0.5), -(int) (objectInstance.scale * img.getHeight() * zooming * 0.5), (int) (objectInstance.scale * img.getWidth() * zooming), (int) (objectInstance.scale * img.getHeight() * zooming), null);
                    //Draw Object in front of player
                    //Player playerOwner = gameInstance.getPlayerById(objectInstance.state.owner_id);
                    /*
                    if (playerOwner != null) {
                        tmp = g2.getTransform();
                        g2.setTransform(new AffineTransform());
                        g2.setTransform(playerOwner.playerAtTableTransform);
                        g2.drawImage(img, -(int) (objectInstance.scale * img.getWidth() * zooming * 0.5), -(int) (objectInstance.scale * img.getHeight() * zooming * 0.5), (int) (objectInstance.scale * img.getWidth() * zooming), (int) (objectInstance.scale * img.getHeight() * zooming), null);
                        g2.setTransform(tmp);
                    }
                    */
                }
            }
            //draw object above private area
            else if (gamePanel.mouseInPrivateArea && objectInstance.state.isActive){
                int insertPosition = gamePanel.privateArea.getInsertPosition(gamePanel.mouseScreenX, gamePanel.mouseScreenY);
                //g2.rotate(player.screenToBoardTransformation.getDeterminant());
                //g2.rotate(Math.toRadians(objectInstance.state.originalRotation));
                g2.rotate(-Math.PI * 0.5 + Math.PI / ((gamePanel.privateArea.objects.size() + 1) * 2));
                g2.rotate(insertPosition * Math.PI / (gamePanel.privateArea.objects.size() + 1));
                g2.rotate(Math.toRadians(objectInstance.state.originalRotation));
                g2.rotate(-gamePanel.rotation);
                g2.scale(gamePanel.privateArea.zooming/(sqrt(g2.getTransform().getDeterminant())), gamePanel.privateArea.zooming/(sqrt(g2.getTransform().getDeterminant())));
                g2.drawImage(img, -(int) (objectInstance.scale * img.getWidth()  * 0.5), -(int) (objectInstance.scale * img.getHeight()  * 0.5), (int) (objectInstance.scale * img.getWidth() ), (int) (objectInstance.scale * img.getHeight() ), null);
            }

            //Define border strokes
            Stroke selectStroke = new BasicStroke(borderWidth,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            float strokePresentLength = Math.min((float)(objectInstance.scale * img.getHeight())/2.0f, (float)(objectInstance.scale * img.getWidth())/2.0f);
            float strokeAbsentLengthHeight = (float)(objectInstance.scale * img.getHeight() - strokePresentLength);
            float strokeAbsentLengthWidth = (float)(objectInstance.scale * img.getWidth() - strokePresentLength);
            float [] dash = new float[]{ strokePresentLength,  strokeAbsentLengthWidth, strokePresentLength, strokeAbsentLengthHeight, strokePresentLength, strokeAbsentLengthWidth, strokePresentLength, strokeAbsentLengthHeight };
            float dashPhase = strokePresentLength/2.0f;
            Stroke hoverStroke = new BasicStroke(borderWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, dashPhase);
            if (objectInstance.state.owner_id != -1) {
                    g2.setStroke(new BasicStroke(0));
                    Player playerOwner = gameInstance.getPlayerById(objectInstance.state.owner_id);
                    g2.setColor(playerOwner.color);
                    g2.drawString(playerOwner.getName() + " Hand Cards", -(int) (objectInstance.scale * img.getWidth() * zooming * 0.5), -(int) (objectInstance.scale * img.getHeight() * zooming * 0.5) - 20);
            }
            else {
                if (ObjectFunctions.isStackTop(objectInstance) && !ObjectFunctions.isStackBottom(objectInstance)) {
                    g2.setStroke(selectStroke);
                    g2.setColor(gamePanel.stackColor);
                }
                //draw selection border
                if (ObjectFunctions.objectIsSelectedByPlayer(gameInstance, player, objectInstance.id)) {
                    g2.setStroke(selectStroke);
                    g2.setColor(player.color);
                }
                //draw hover border
                else if (ObjectFunctions.isObjectHovered(objectInstance, gamePanel.hoveredObject)) {
                    g2.setStroke(hoverStroke);
                    g2.setColor(player.color);
                }

            }


            //Draw border around object
            int objectSelector = ObjectFunctions.getObjectSelector(gameInstance, objectInstance.id);
            if (objectSelector != -1)
            {
                g2.setStroke(selectStroke);
                g2.setColor(gameInstance.getPlayerById(objectSelector).color);
            }
            if (objectInstance.go instanceof GameObjectToken) {
                if (ObjectFunctions.isObjectHovered(objectInstance, gamePanel.hoveredObject) || ObjectFunctions.getObjectSelector(gameInstance, objectInstance.id) != -1 || objectInstance.state.owner_id != -1 || (ObjectFunctions.isStackTop(objectInstance) && !ObjectFunctions.isStackBottom(objectInstance))) {
                    if (isStackCollected(gameInstance, objectInstance)) {
                        g2.drawRect(-(int) (objectInstance.scale * img.getWidth() * zooming * 0.5) - borderWidth / 2, -(int) (objectInstance.scale * img.getHeight() * zooming * 0.5) - borderWidth / 2, (int) (objectInstance.scale * img.getWidth() * zooming) + borderWidth / 2, (int) (objectInstance.scale * img.getHeight() * zooming) + borderWidth / 2);
                    }
                    else {
                        if (isStackTop(objectInstance)) {
                            g2.drawLine(-(int) (objectInstance.scale * img.getWidth() * zooming * 0.5) - borderWidth / 2, -(int) (objectInstance.scale * img.getHeight() * zooming * 0.5) - borderWidth / 2, -(int) (objectInstance.scale * img.getWidth() * zooming * 0.5) - borderWidth / 2, (int) (objectInstance.scale * img.getHeight() * zooming * 0.5) + borderWidth / 2);
                        } else if (isStackBottom(objectInstance)) {
                            g2.drawLine((int) (objectInstance.scale * img.getWidth() * zooming * 0.5) + borderWidth / 2, -(int) (objectInstance.scale * img.getHeight() * zooming * 0.5) - borderWidth / 2, (int) (objectInstance.scale * img.getWidth() * zooming * 0.5) + borderWidth / 2, (int) (objectInstance.scale * img.getHeight() * zooming * 0.5) + borderWidth / 2);
                        }
                    }
                }
            }
            else{
                if (ObjectFunctions.isObjectHovered(objectInstance, gamePanel.hoveredObject) || ObjectFunctions.getObjectSelector(gameInstance, objectInstance.id) != -1) {
                    g2.drawRect(-(int) (objectInstance.scale * img.getWidth() * zooming * 0.5) - borderWidth/2, -(int) (objectInstance.scale * img.getHeight() * zooming * 0.5) - borderWidth/2, (int) (objectInstance.scale * img.getWidth() * zooming) + borderWidth/2, (int) (objectInstance.scale * img.getHeight() * zooming) + borderWidth / 2);
                }
            }
            if (logger.isDebugEnabled())
            {
	            g2.setColor(Color.GREEN);
	            g2.drawArc(-5, -5, 10, 10, 0, 360);
            }
            
            g2.setTransform(tmp);
            if (logger.isDebugEnabled())
            {
	            g2.setColor(Color.RED);
	            g2.drawArc(objectInstance.state.posX-5, objectInstance.state.posY-5, 10, 10, 0, 360);
            }
        }
    }

    public static void drawPrivateAreaBorder(Graphics g, GameInstance gameInstance, Player player, ObjectInstance objectInstance, int borderWidth, Color color, AffineTransform transform){
        if (objectInstance != null) {
            //Define border strokes
            BufferedImage img = objectInstance.go.getLook(objectInstance.state, player.id).getImageNoExc();
            //Stroke selectStroke = new BasicStroke(borderWidth,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            float strokePresentLength = Math.min((float)(objectInstance.scale * img.getHeight())/2.0f, (float)(objectInstance.scale * img.getWidth())/2.0f);
            float strokeAbsentLengthHeight = (float)(objectInstance.scale * img.getHeight() - strokePresentLength);
            float strokeAbsentLengthWidth = (float)(objectInstance.scale * img.getWidth() - strokePresentLength);
            float [] dash = new float[]{ strokePresentLength,  strokeAbsentLengthWidth, strokePresentLength, strokeAbsentLengthHeight, strokePresentLength, strokeAbsentLengthWidth, strokePresentLength, strokeAbsentLengthHeight };
            float dashPhase = strokePresentLength/2.0f;
            Stroke hoverStroke = new BasicStroke(borderWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, dashPhase);
            g.setColor(color);
            Graphics2D g2d = (Graphics2D) g.create();
            AffineTransform tmp = g2d.getTransform();
            if(transform != null)
                g2d.setTransform(transform);
            g2d.setStroke(hoverStroke);
            //g2d.rotate(Math.toRadians(objectInstance.state.originalRotation));
            g2d.drawRect(-borderWidth/2 ,-borderWidth/2, (objectInstance.getWidth(player.id)) + borderWidth, (objectInstance.getHeight(player.id)) + borderWidth);
            g2d.setTransform(tmp);
        }
    }

    public static void unfoldDice(ObjectInstance objectInstance, List<BufferedImage> bufferedImages) {
        bufferedImages.clear();
        if (objectInstance.go instanceof GameObjectDice)
        {
            GameObjectDice dice = (GameObjectDice)objectInstance.go;
            for(int i = 0; i<dice.dss.length; ++i) {
                GameObjectDice.DiceSide diceSide = dice.dss[i];
                bufferedImages.add(diceSide.img.getImageNoExc());
            }

        }
    }

    public static void drawDebugInfo(GamePanel gamePanel, Graphics2D g2, GameInstance gameInstance, Player player) {
        ObjectInstance hoveredObject = gamePanel.hoveredObject;
        IntegerArrayList selectedObjects = gamePanel.getSelectedObjects();
        IntegerArrayList ial = new IntegerArrayList();
        int hoverId = (hoveredObject== null) ? -1 : hoveredObject.id;
        int drawValue = (hoveredObject== null) ? -1 : hoveredObject.state.drawValue;

        StringBuilder stringSelectedObjects = new StringBuilder();
        StringBuilder stringHandCards = new StringBuilder();
        StringBuilder stringActiveObjects = new StringBuilder();
        StringBuilder stringHoveredStack = new StringBuilder();
        StringBuilder stringPrivateAreaCards = new StringBuilder();

        StringBuilder stringAboveIds = new StringBuilder();
        StringBuilder stringBelowId = new StringBuilder();
        StringBuilder stringBoxId = new StringBuilder();
        StringBuilder stringInBox = new StringBuilder();

        StringBuilder stringValue = new StringBuilder();

        StringBuilder stringSortValue = new StringBuilder();

        for (int i = 0; i < selectedObjects.size(); ++i)
        {
            if (i==0) {
                stringSelectedObjects.append(selectedObjects.get(i));
            }
            else{
                stringSelectedObjects.append("; ").append(selectedObjects.get(i));
            }
        }
        for (int i = 0; i < gameInstance.getObjectCount(); ++i)
        {
            ObjectInstance oi = gameInstance.getObjectInstanceByIndex(i);
            if (oi.state.owner_id == player.id) {
                if (stringHandCards.length() == 0) {
                    stringHandCards.append(oi.id);
                } else {
                    stringHandCards.append("; ").append(oi.id);
                }
            }
        }

        for (int i = 0; i < gameInstance.getObjectCount(); ++i)
        {
            ObjectInstance oi = gameInstance.getObjectInstanceByIndex(i);
            if (oi.state.isActive) {
                if (stringActiveObjects.length() == 0) {
                    stringActiveObjects.append(oi.id);
                } else {
                    stringActiveObjects.append("; ").append(oi.id);
                }
            }
        }

        ObjectFunctions.getStack(gameInstance, hoveredObject, ial);
        if (ial.size()>1) {
            for (int i : ial) {
                ObjectInstance oi = gameInstance.getObjectInstanceById(i);
                stringHoveredStack.append('|').append(oi.state.aboveInstanceId).append(' ').append(oi.id).append(' ').append(oi.state.belowInstanceId).append('|');
            }
        }

        for (int i = 0; i < gameInstance.getObjectCount(); ++i)
        {
            ObjectInstance oi = gameInstance.getObjectInstanceByIndex(i);
            if (oi.state.inPrivateArea) {
                if (stringPrivateAreaCards.length() == 0) {
                    stringPrivateAreaCards.append(oi.id);
                } else {
                    stringPrivateAreaCards.append("; ").append(oi.id);
                }
            }
        }

        if (hoverId != -1){
            ObjectInstance objectInstance = gameInstance.getObjectInstanceById(hoverId);
            for (int i= 0; i < objectInstance.state.aboveLyingObectIds.size(); ++i){
                if (i==0){
                    stringAboveIds.append(objectInstance.state.aboveLyingObectIds.get(i));
                }
                else{
                    stringAboveIds.append("; ").append(objectInstance.state.aboveLyingObectIds.get(i));
                }
            }
            stringBelowId.append(objectInstance.state.liesOnId);
            stringBoxId.append(objectInstance.state.boxId);
            stringInBox.append(objectInstance.state.inBox);
            stringValue.append(objectInstance.state.value);
            stringSortValue.append(objectInstance.state.sortValue);
        }

        Vector2d mouseBoardPos = new Vector2d();
        gamePanel.screenToBoardPos(gamePanel.mouseScreenX, gamePanel.mouseScreenY, mouseBoardPos);
        Point2D transformedPoint = new Point2D.Double(mouseBoardPos.getXI(), mouseBoardPos.getYI());
        gamePanel.privateArea.transformPoint(transformedPoint, transformedPoint);
        int sectionIndex = gamePanel.privateArea.getPrivateObjectIndexByPosition((int) transformedPoint.getX(), (int) transformedPoint.getY());


        int yPos = 20;
        int yStep = 20;
        g2.setColor(player.color);
        g2.drawString("Mouse: (" + Integer.toString(gamePanel.mouseScreenX) + ", " + Integer.toString(gamePanel.mouseScreenY) + ")" + " Wheel: " + Integer.toString(gamePanel.mouseWheelValue), 50, yPos);
        yPos+=yStep;
        Vector2d boardPosition = new Vector2d();
        gamePanel.screenToBoardPos(gamePanel.mouseScreenX, gamePanel.mouseScreenY, boardPosition);
        g2.drawString("Board: (" + Integer.toString(boardPosition.getXI()) + ", " + Integer.toString(boardPosition.getYI()) + ")" + " Wheel: " + Integer.toString(gamePanel.mouseWheelValue), 50, yPos);
        yPos+=yStep;
        g2.drawString("Player Pos: " + Boolean.toString(gamePanel.mouseInPrivateArea), 50, yPos);
        yPos+=yStep;
        g2.drawString("Private Area: " + Boolean.toString(gamePanel.mouseInPrivateArea), 50, yPos);
        yPos+=yStep;
        g2.drawString("Own Hand Cards: " + stringHandCards, 50, yPos);
        yPos+=yStep;
        g2.drawString("Cards in some private Area: " + stringPrivateAreaCards, 50, yPos);
        yPos+=yStep;
        g2.drawString("Hand Card Section Id: " + Integer.toString(sectionIndex), 50, yPos);
        yPos+=yStep;
        g2.drawString("Active Objects: " + stringActiveObjects, 50, yPos);
        yPos+=yStep;
        g2.drawString("Hovered Object: " + Integer.toString(hoverId), 50, yPos);
        yPos+=yStep;
        g2.drawString("Hovered Draw Value: " + Long.toString(drawValue), 50, yPos);
        yPos+=yStep;
        g2.drawString("Hovered Stack: " + stringHoveredStack, 50, yPos);
        yPos+=yStep;
        g2.drawString("Above  Lying: " + stringAboveIds, 50, yPos);
        yPos+=yStep;
        g2.drawString("Below Id: " + stringBelowId, 50, yPos);
        yPos+=yStep;
        g2.drawString("Value: " + stringValue, 50, yPos);
        yPos+=yStep;
        g2.drawString("Sort Value: " + stringSortValue, 50, yPos);
        yPos+=yStep;
        g2.drawString("Box Id: " + stringBoxId, 50, yPos);
        yPos+=yStep;
        g2.drawString("In Box: " + stringInBox, 50, yPos);
        yPos+=yStep;
        g2.drawString("Selected Objects: " + stringSelectedObjects, 50, yPos);
        yPos+=yStep;
        g2.drawString("Player Id: " + Integer.toString(player.id), 50, yPos);
        yPos+=yStep;
        g2.drawString("Admin Id: " + Integer.toString(gameInstance.admin), 50, yPos);
        yPos+=yStep;
        g2.drawString("Number of Pressed Keys: " + Integer.toString(gamePanel.downKeys.size()), 50, yPos);
        yPos+=yStep;
    }
}
