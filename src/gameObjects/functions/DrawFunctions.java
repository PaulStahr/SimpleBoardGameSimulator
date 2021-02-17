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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import gameObjects.definition.GameObjectBook;
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

    public static void drawBackground(GamePanel gamePanel, Graphics g, GameInstance gameInstance){

        g.clearRect(0, 0, gamePanel.getWidth(), gamePanel.getHeight());
        //TODO Florian:sometimes images are drawn twice (the active object?)
        g.drawString(String.valueOf(gamePanel.mouseWheelValue), gamePanel.mouseScreenX, gamePanel.mouseScreenY);
        g.drawImage(gameInstance.game.background, 0, 0, gamePanel.getWidth(), gamePanel.getHeight(), Color.BLACK, null);

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
        IntegerArrayList oiList = new IntegerArrayList();
        IntegerArrayList fixedObjects = new IntegerArrayList();
        for (int idx : ial){
            if (!gameInstance.getObjectInstanceByIndex(idx).state.isFixed)
            {
                oiList.add(gameInstance.getObjectInstanceByIndex(idx).id);
            }
            else
            {
                fixedObjects.add(gameInstance.getObjectInstanceByIndex(idx).id);
            }
        }
        //Draw the fixed objects first
        drawObjectsFromList(gamePanel,g,gameInstance,player,fixedObjects,ial);

        drawObjectsFromList(gamePanel,g,gameInstance,player,oiList,ial);

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
                }catch(Exception e)
                {
                    logger.error("Error in drawing Tokens", e);
                }
            }
        }
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
        for(int pIdx = 0;pIdx < gameInstance.getPlayerNumber(true);++pIdx) {
            Player p = gameInstance.getPlayerByIndex(pIdx);
            g.setColor(p.color);
            g2.setTransform(tmp);
			g2.transform(p.screenToBoardTransformation);
			double playerDeterminant = p.screenToBoardTransformation.getDeterminant();
	        g2.setStroke(wideStroke);
            //g2.drawLine(40, p.screenHeight, p.screenWidth, p.screenHeight);
            int imageNumber = pIdx % 10;

            BufferedImage img = gamePanel.playerImages[imageNumber];
            g2.translate((p.screenWidth)/2, p.screenHeight - 20);
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
            if (gamePanel.privateArea.currentDragPosition != -1 && gamePanel.hoveredObject != null && gamePanel.selectedObjects.size() != 0 && !gamePanel.isSelectStarted) {
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
                    //TODO out ouf bound errors
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

            transform.translate(gamePanel.getWidth()/2, gamePanel.getHeight()-2*gamePanel.privateArea.objects.size());
            transform.rotate(-Math.PI * 0.5 + Math.PI / (gamePanel.privateArea.objects.size() * 2));
            transform.rotate(gamePanel.privateArea.objects.indexOf(activeObject.id) * Math.PI / (gamePanel.privateArea.objects.size()));
            transform.scale(gamePanel.privateArea.zooming, gamePanel.privateArea.zooming);
            transform.translate(-activeObject.getWidth(player.id) / 2, -activeObject.getHeight(player.id) / 2);
            transform.translate(0, -250);
            drawPrivateAreaBorder(g, gameInstance, player, activeObject, 5, player.color, transform);
            g2d.setTransform(tmp);
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
        BufferedImage img = objectInstance.go.getLook(objectInstance.state, player.id);
        if (objectInstance.state == null || img == null) {
            logger.error("Object state is null");
        }
        else {
            Graphics2D g2 = (Graphics2D) g;
            AffineTransform tmp = g2.getTransform();
            g2.translate(objectInstance.state.posX, objectInstance.state.posY);
            //draw object not in private area
            if (gamePanel.privateArea == null || !gamePanel.mouseInPrivateArea || !objectInstance.state.isActive) {
                g2.rotate(Math.toRadians(objectInstance.state.rotation));
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
                if (objectInstance.state.owner_id == -1 || ObjectFunctions.objectIsSelected(gameInstance, objectInstance.id)) {
                    g.drawImage(img, -(int) (objectInstance.scale * img.getWidth() * zooming * 0.5), -(int) (objectInstance.scale * img.getHeight() * zooming * 0.5), (int) (objectInstance.scale * img.getWidth() * zooming), (int) (objectInstance.scale * img.getHeight() * zooming), null);
                }
                else if (objectInstance.state.owner_id != -1){
                    boolean x = ObjectFunctions.objectIsSelected(gameInstance, objectInstance.id);
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
                int insertPosition = gamePanel.privateArea.getInsertPosition(gamePanel.mouseScreenX, gamePanel.mouseScreenY, gamePanel.getWidth()/2, gamePanel.getHeight());
                //g2.rotate(player.screenToBoardTransformation.getDeterminant());
                //g2.rotate(Math.toRadians(objectInstance.state.originalRotation));
                g2.rotate(-Math.PI * 0.5 + Math.PI / ((gamePanel.privateArea.objects.size() + 1) * 2));
                g2.rotate(insertPosition * Math.PI / (gamePanel.privateArea.objects.size() + 1));
                g2.rotate(Math.toRadians(objectInstance.state.originalRotation));
                g2.rotate(-gamePanel.rotation);
                g2.scale(gamePanel.privateArea.zooming/(sqrt(g2.getTransform().getDeterminant())), gamePanel.privateArea.zooming/(sqrt(g2.getTransform().getDeterminant())));
                g2.drawImage(img, -(int) (objectInstance.scale * img.getWidth()  * 0.5), -(int) (objectInstance.scale * img.getHeight()  * 0.5), (int) (objectInstance.scale * img.getWidth() ), (int) (objectInstance.scale * img.getHeight() ), null);
            }
            else{
                boolean x = true;
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
                else if (ObjectFunctions.isObjecthovered(gamePanel, objectInstance)) {
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
                if (ObjectFunctions.isObjecthovered(gamePanel, objectInstance) || ObjectFunctions.getObjectSelector(gameInstance, objectInstance.id) != -1 || objectInstance.state.owner_id != -1 || (ObjectFunctions.isStackTop(objectInstance) && !ObjectFunctions.isStackBottom(objectInstance))) {
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
                if (ObjectFunctions.isObjecthovered(gamePanel, objectInstance) || ObjectFunctions.getObjectSelector(gameInstance, objectInstance.id) != -1) {
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
            Graphics2D g2 = (Graphics2D) g;
            //Define border strokes
            BufferedImage img = objectInstance.go.getLook(objectInstance.state, player.id);
            Stroke selectStroke = new BasicStroke(borderWidth,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
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
                bufferedImages.add(diceSide.img);
            }

        }
    }

    public static void drawDebugInfo(GamePanel gamePanel, Graphics2D g2, GameInstance gameInstance, Player player) {
        ObjectInstance hoveredObject = gamePanel.hoveredObject;
        IntegerArrayList selectedObjects = gamePanel.selectedObjects;
        IntegerArrayList ial = new IntegerArrayList();
        int hoverId = (hoveredObject== null) ? -1 : hoveredObject.id;
        int drawValue = (hoveredObject== null) ? -1 : hoveredObject.state.drawValue;
        String stringSelectedObjects = "";
        String stringHandCards = "";
        String stringActiveObjects = "";
        String stringHoveredStack = "";
        String stringPrivateAreaCards = "";


        for (int i = 0; i < selectedObjects.size(); ++i)
        {
            if (i==0) {
                stringSelectedObjects += Integer.toString(selectedObjects.get(i));
            }
            else{
                stringSelectedObjects += "; " + Integer.toString(selectedObjects.get(i));
            }
        }
        for (int i = 0; i < gameInstance.getObjectNumber(); ++i)
        {
            ObjectInstance oi = gameInstance.getObjectInstanceByIndex(i);
            if (oi.state.owner_id == player.id) {
                if (stringHandCards.equals("")) {
                    stringHandCards += Integer.toString(oi.id);
                } else {
                    stringHandCards += "; " + Integer.toString(oi.id);
                }
            }
        }

        for (int i = 0; i < gameInstance.getObjectNumber(); ++i)
        {
            ObjectInstance oi = gameInstance.getObjectInstanceByIndex(i);
            if (oi.state.isActive) {
                if (stringActiveObjects.equals("")) {
                    stringActiveObjects += Integer.toString(oi.id);
                } else {
                    stringActiveObjects += "; " + Integer.toString(oi.id);
                }
            }
        }

        ObjectFunctions.getStack(gameInstance, hoveredObject, ial);
        if (ial.size()>1) {
            for (int i : ial) {
                ObjectInstance oi = gameInstance.getObjectInstanceById(i);
                stringHoveredStack += "|" + oi.state.aboveInstanceId + " " + oi.id + " " + oi.state.belowInstanceId + "|";
            }
        }

        for (int i = 0; i < gameInstance.getObjectNumber(); ++i)
        {
            ObjectInstance oi = gameInstance.getObjectInstanceByIndex(i);
            if (oi.state.inPrivateArea) {
                if (stringPrivateAreaCards.equals("")) {
                    stringPrivateAreaCards += Integer.toString(oi.id);
                } else {
                    stringPrivateAreaCards += "; " + Integer.toString(oi.id);
                }
            }
        }

        int yPos = 20;
        int yStep = 20;
        g2.setColor(player.color);
        g2.drawString("Mouse: (" + Integer.toString(gamePanel.mouseScreenX) + ", " + Integer.toString(gamePanel.mouseScreenY) + ")" + " Wheel: " + Integer.toString(gamePanel.mouseWheelValue), 50, yPos);
        yPos+=yStep;
        g2.drawString("Player Pos: " + Boolean.toString(gamePanel.mouseInPrivateArea), 50, yPos);
        yPos+=yStep;
        g2.drawString("Private Area: " + Boolean.toString(gamePanel.mouseInPrivateArea), 50, yPos);
        yPos+=yStep;
        g2.drawString("Own Hand Cards: " + stringHandCards, 50, yPos);
        yPos+=yStep;
        g2.drawString("Cards in some private Area: " + stringPrivateAreaCards, 50, yPos);
        yPos+=yStep;
        g2.drawString("Active Objects: " + stringActiveObjects, 50, yPos);
        yPos+=yStep;
        g2.drawString("Hovered Object: " + Integer.toString(hoverId), 50, yPos);
        yPos+=yStep;
        g2.drawString("Hovered Draw Value: " + Long.toString(drawValue), 50, yPos);
        yPos+=yStep;
        g2.drawString("Hovered Stack: " + stringHoveredStack, 50, yPos);
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
