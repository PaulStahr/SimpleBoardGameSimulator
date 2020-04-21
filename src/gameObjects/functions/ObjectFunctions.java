package gameObjects.functions;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Collections;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.GameObjectInstanceEditAction;
import gameObjects.GamePlayerEditAction;
import gameObjects.definition.GameObject;
import gameObjects.definition.GameObjectToken;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.ObjectState;
import geometry.Vector2;
import gui.GamePanel;
import main.Player;
import util.Pair;
import util.data.IntegerArrayList;

public class ObjectFunctions {
	private static final Logger logger = LoggerFactory.getLogger(ObjectFunctions.class);

    /** Get the top of the stack with with element objectInstance
     * @param gameInstance Instance of the game
     * @param objectInstance Instance of the stack
     * @return top Instance of the stack
     */
    public static ObjectInstance getStackTop(GameInstance gameInstance, ObjectInstance objectInstance) {
        if (objectInstance != null) {
            ObjectInstance currentTop = objectInstance;
            while (currentTop.state.aboveInstanceId != -1) {
                currentTop = gameInstance.objects.get(currentTop.state.aboveInstanceId);
                if (objectInstance == currentTop) {
                    throw new RuntimeException();
                }
            }
            return currentTop;
        } else {
            return null;
        }
    }

    /** Get the bottom of the stack with with element objectInstance
     * @param gameInstance Instance of the game
     * @param objectInstance Instance of the stack
     * @return bottom Instance of the stack
     */
    public static ObjectInstance getStackBottom(GameInstance gameInstance, ObjectInstance objectInstance) {
        if (objectInstance != null) {
            ObjectInstance currentBottom = objectInstance;
            while (currentBottom.state.belowInstanceId != -1) {
                currentBottom = gameInstance.objects.get(currentBottom.state.belowInstanceId);
                if (objectInstance == currentBottom) {
                    throw new RuntimeException();
                }
            }
            return currentBottom;
        } else {
            return null;
        }
    }


    /** Checks if above Instance exists
     * @param objectInstance object Instance
     * @return true if above Instance exists false otherwise
     */
    public static boolean hasAboveObject(ObjectInstance objectInstance){
        if (objectInstance != null)
            return objectInstance.state.aboveInstanceId != -1;
        else
            return false;
    }

    /** gets above instance id
     * @param objectInstance  object Instance
     * @return id of above Instance or -1 if it does not exist
     */
    public static int getAboveObjectId(ObjectInstance objectInstance)
    {
        if (hasAboveObject(objectInstance))
        {
            return objectInstance.state.aboveInstanceId;
        }
        else
            return -1;
    }

    /**
     * @param gameInstance Instance of the game
     * @param objectInstance Instance of an object
     * @return Instance of above object or null if it does not exist
     */
    public static ObjectInstance getAboveObject(GameInstance gameInstance, ObjectInstance objectInstance)
    {
        if (hasAboveObject(objectInstance))
        {
            return gameInstance.objects.get(objectInstance.state.aboveInstanceId);
        }
        else
            return null;
    }


    /** Checks if below Instance exists
     * @param objectInstance Instance of object
     * @return true if above Instance exists false otherwise
     */
    public static boolean hasBelowObject(ObjectInstance objectInstance){
        if (objectInstance != null)
            return objectInstance.state.belowInstanceId != -1;
        else
            return false;
    }

    /** gets below instance id
     * @param objectInstance  object Instance
     * @return id of below Instance or -1 if it does not exist
     */
    public static int getBelowObject(ObjectInstance objectInstance)
    {
        if (hasBelowObject(objectInstance))
        {
            return objectInstance.state.belowInstanceId;
        }
        else
            return -1;
    }

    /**
     * @param gameInstance Instance of the game
     * @param objectInstance Instance of an object
     * @return Instance of below object or null if it does not exist
     */
    public static ObjectInstance getBelowObject(GameInstance gameInstance, ObjectInstance objectInstance)
    {
        if (hasBelowObject(objectInstance))
        {
            return gameInstance.objects.get(objectInstance.state.belowInstanceId);
        }
        else
            return null;
    }

    /**
     * @param gameInstance Instance of the game
     * @param objectInstance Instance of the object
     * @param included if objectInstance should be included default is true
     * @return all ids of above Elements in the Stack starting with the bottom id
     */
    public static void getAboveStack(GameInstance gameInstance, ObjectInstance objectInstance, boolean included, IntegerArrayList objectStack) {
       if (objectInstance != null) {
            if (included) {
                objectStack.add(objectInstance.id);
            }
            ObjectInstance currentObjectInstance = objectInstance;
            while (currentObjectInstance.state.aboveInstanceId != -1) {
                objectStack.add(currentObjectInstance.state.aboveInstanceId);
                currentObjectInstance = gameInstance.objects.get(currentObjectInstance.state.aboveInstanceId);
            }
        }
    }
    public static void getAboveStack(GameInstance gameInstance, ObjectInstance objectInstance, IntegerArrayList objectStack) {
        getAboveStack(gameInstance, objectInstance, true, objectStack);
    }

    /**
     * @param gameInstance Instance of the game
     * @param objectInstance Instance of the object
     * @param included if objectInstance should be included default is true
     * @return all ids of below Elements in the Stack starting with the bottom id
     */   
    public static void getBelowStack(GameInstance gameInstance, ObjectInstance objectInstance, boolean included, IntegerArrayList objectStack) {
        if (objectInstance != null) {
            if (included) {
                objectStack.add(objectInstance.id);
            }
            ObjectInstance currentObjectInstance = objectInstance;
            while (currentObjectInstance.state.belowInstanceId != -1) {
                objectStack.add(currentObjectInstance.state.belowInstanceId);
                currentObjectInstance = gameInstance.objects.get(currentObjectInstance.state.belowInstanceId);
            }
        }
    }
    
    public static void getBelowStack(GameInstance gameInstance, ObjectInstance objectInstance, IntegerArrayList ial) {
        getBelowStack(gameInstance, objectInstance, true, ial);
    }


    /**
     * @param gameInstance Instance of the game
     * @param objectInstance Instance of the object
     * @return all ids of stack Elements in the Stack starting with the top id
     */
    public static void getStackFromTop(GameInstance gameInstance, ObjectInstance objectInstance, IntegerArrayList ial) {
        getBelowStack(gameInstance, getStackTop(gameInstance, objectInstance), ial);
    }

    /**
     * @param gameInstance Instance of the game
     * @param objectInstance Instance of the object
     * @return all ids of stack Elements in the Stack starting with the bottom id
     */
    public static void getStackFromBottom(GameInstance gameInstance, ObjectInstance objectInstance, IntegerArrayList objectStack) {
        getAboveStack(gameInstance, getStackBottom(gameInstance, objectInstance), objectStack);
    }

    /**
     * @param gameInstance Instance of the game
     * @param objectInstance Instance of the object
     * @return all ids of stack Elements in the Stack starting with the bottom id
     */
    public static void getStack(GameInstance gameInstance, ObjectInstance objectInstance, IntegerArrayList objectStack) {
        getAboveStack(gameInstance, getStackBottom(gameInstance, objectInstance), objectStack);
    }

    /** Moves object to posX, posY
     * @param gamePanel Game Panel object
     * @param gameInstance Instance of Game
     * @param player Current player
     * @param objectInstance Instance of object
     * @param posX target x position of object
     * @param posY target y position of object
     */
    public static void moveObjectTo(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance objectInstance, int posX, int posY) {
        objectInstance.state.posX = posX;
        objectInstance.state.posY = posY;
        gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player, objectInstance));
    }

    /** Moves objectInstance to posX, posY of targetObjectInstance
     * @param gamePanel Game Panel object
     * @param gameInstance Instance of Game
     * @param player Current player
     * @param objectInstance Instance of object
     * @param targetObjectInstance Target Instance
     */
    public static void moveObjectTo(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance objectInstance, ObjectInstance targetObjectInstance) {
        moveObjectTo(gamePanel, gameInstance, player, objectInstance, targetObjectInstance.state.posX, targetObjectInstance.state.posY);
    }


    /** Moves stack to x, y position
     * @param gamePanel Game Panel object
     * @param gameInstance Instance of Game
     * @param player Current player
     * @param idList All ids of stack elements
     * @param posX Target x position
     * @param posY Target y position
     */
    public static void moveStackTo(GamePanel gamePanel, GameInstance gameInstance, Player player, IntegerArrayList idList, int posX, int posY) {
        IntegerArrayList relativeX = new IntegerArrayList();
        IntegerArrayList relativeY = new IntegerArrayList();
        relativeX.add(0);
        relativeY.add(0);
        for (int i = 0; i < idList.size(); i++) {
            if (i != 0) {
                relativeX.add(gameInstance.objects.get(idList.get(i)).state.posX - gameInstance.objects.get(idList.get(0)).state.posX);
                relativeY.add(gameInstance.objects.get(idList.get(i)).state.posY - gameInstance.objects.get(idList.get(0)).state.posY);
            }
        }
        for (int i = 0; i < idList.size(); i++) {
            ObjectInstance currentObject = gameInstance.objects.get(idList.get(i));
            moveObjectTo(gamePanel, gameInstance, player, currentObject, posX + relativeX.get(i), posY + relativeY.get(i));
        }
    }

    /** Moves stack to x, y position of targetObjectInstance
     * @param gamePanel Game Panel object
     * @param gameInstance Instance of Game
     * @param player Current player
     * @param idList All ids of stack elements
     * @param targetObjectInstance Target Instance
     */
    public static void moveStackTo(GamePanel gamePanel, GameInstance gameInstance, Player player, IntegerArrayList idList, ObjectInstance targetObjectInstance) {
        if (targetObjectInstance != null) {
            moveStackTo(gamePanel, gameInstance, player, idList, targetObjectInstance.state.posX, targetObjectInstance.state.posY);
        }
    }

    /** Moves stack to x, y position of stack object
     * @param gamePanel Game Panel object
     * @param gameInstance Instance of Game
     * @param player Current player
     * @param stackObject Instance of stack
     * @param posX Target x position
     * @param posY Target y position
     */
    public static void moveStackTo(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance stackObject, int posX, int posY) {
    	IntegerArrayList ial = new IntegerArrayList();
    	getStackFromTop(gameInstance, stackObject, ial);
        moveStackTo(gamePanel, gameInstance, player, ial, posX, posY);
    }

    /** Moves stack to x, y position of targetObjectInstance
     * @param gamePanel Game Panel object
     * @param gameInstance Instance of Game
     * @param player Current player
     * @param stackObject Instance of stack
     * @param targetObjectInstance Target Instance
     */
    public static void moveStackTo(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance stackObject, ObjectInstance targetObjectInstance) {
       	IntegerArrayList ial = new IntegerArrayList();
    	getStackFromTop(gameInstance, stackObject, ial);
        moveStackTo(gamePanel, gameInstance, player, ial, targetObjectInstance);
    }


    /** Moves stack above stackObject to posX, posY
     * @param gamePanel Game Panel object
     * @param gameInstance Instance of Game
     * @param player Current player
     * @param stackObject Instance of stack
     * @param posX Target x position
     * @param posY Target y position
     * @param include if stackObject should be inluded default is true
     */
    public static void moveAboveStackTo(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance stackObject, int posX, int posY, boolean include) {
    	IntegerArrayList tmp = new IntegerArrayList();
    	getAboveStack(gameInstance, stackObject, include, tmp);
        moveStackTo(gamePanel, gameInstance, player, tmp, posX, posY);
    }
    public static void moveAboveStackTo(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance stackObject, int posX, int posY) {
        moveAboveStackTo(gamePanel, gameInstance, player, stackObject, posX, posY, true);
    }

    /** Moves stack above stackObject to posX, posY of targetObjectInstance
     * @param gamePanel Game Panel object
     * @param gameInstance Instance of Game
     * @param player Current player
     * @param stackObject Instance of stack
     * @param targetObjectInstance Target object
     * @param include if stackObject should be inluded default is true
     */
    public static void moveAboveStackTo(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance stackObject, ObjectInstance targetObjectInstance, boolean include) {
    	IntegerArrayList tmp = new IntegerArrayList();
    	getAboveStack(gameInstance, stackObject, include, tmp);
        moveStackTo(gamePanel, gameInstance, player, tmp, targetObjectInstance);
    }
    public static void moveAboveStackTo(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance stackObject, ObjectInstance baseObject) {
        moveAboveStackTo(gamePanel, gameInstance, player, stackObject, baseObject, true);
    }


    /** Moves stack below stackObject to posX, posY
     * @param gamePanel Game Panel object
     * @param gameInstance Instance of Game
     * @param player Current player
     * @param stackObject Instance of stack
     * @param posX Target x position
     * @param posY Target y position
     * @param include if stackObject should be included default is true
     */
    public static void moveBelowStackTo(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance stackObject, int posX, int posY, boolean include) {
    	IntegerArrayList ial = new IntegerArrayList();
    	getBelowStack(gameInstance, stackObject, include, ial);
        moveStackTo(gamePanel, gameInstance, player, ial, posX, posY);
    }
    public static void moveBelowStackTo(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance stackObject, int posX, int posY) {
        moveBelowStackTo(gamePanel, gameInstance, player, stackObject, posX, posY, true);
    }

    /** Moves stack above stackObject to posX, posY of targetObjectInstance
     * @param gamePanel Game Panel object
     * @param gameInstance Instance of Game
     * @param player Current player
     * @param stackObject Instance of stack
     * @param targetObjectInstance Target object
     * @param include if stackObject should be included default is true
     */
    public static void moveBelowStackTo(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance stackObject, ObjectInstance targetObjectInstance, boolean include) {
    	IntegerArrayList ial = new IntegerArrayList();
    	getBelowStack(gameInstance, stackObject, include, ial);
        moveStackTo(gamePanel, gameInstance, player, ial, targetObjectInstance);
    }
    public static void moveBelowStackTo(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance stackObject, ObjectInstance baseObject) {
        moveBelowStackTo(gamePanel, gameInstance, player, stackObject, baseObject, true);
    }


    /** Shuffles the given stack
     * @param gamePanelId id of game panel
     * @param gameInstance Instance of Game
     * @param player Current player
     * @param objectInstance Instance of Object
     * @param include if object should be included in shuffling operation, default is true
     */
    public static void shuffleStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, boolean include) {
    	if (objectInstance == null)
    	{
    		return;
    	}
        IntegerArrayList objectStack = new IntegerArrayList();
        getStack(gameInstance, objectInstance, objectStack);
        if (objectInstance != null && (isStackInHand(gameInstance, player, objectStack) || isStackNotInHand(gameInstance, player, objectStack))) {
            if (objectStack.size() > 1) {
                player.actionString = "Shuffled Objects";
                gameInstance.update(new GamePlayerEditAction(gamePanelId, player, player));
                IntegerArrayList oldX = new IntegerArrayList();
                IntegerArrayList oldY = new IntegerArrayList();
                for (int id : objectStack) {
                    oldX.add(gameInstance.objects.get(id).state.posX);
                    oldY.add(gameInstance.objects.get(id).state.posY);
                }
                Collections.shuffle(objectStack);
                for (int i = 0; i < objectStack.size(); i++) {
                    ObjectInstance currentObject = gameInstance.objects.get(objectStack.get(i));
                    if (i == 0 && i < objectStack.size() - 1) {
                        currentObject.state.belowInstanceId = -1;
                        currentObject.state.aboveInstanceId = gameInstance.objects.get(objectStack.get(i + 1)).id;
                    } else if (i == objectStack.size() - 1) {
                        currentObject.state.belowInstanceId = gameInstance.objects.get(objectStack.get(i - 1)).id;
                        currentObject.state.aboveInstanceId = -1;
                    } else {
                        currentObject.state.belowInstanceId = gameInstance.objects.get(objectStack.get(i - 1)).id;
                        currentObject.state.aboveInstanceId = gameInstance.objects.get(objectStack.get(i + 1)).id;
                    }
                    currentObject.state.posX = oldX.get(i);
                    currentObject.state.posY = oldY.get(i);
                    gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, currentObject));
                }
            }
        }
    }
    public static void shuffleStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        shuffleStack(gamePanelId, gameInstance, player, objectInstance, true);
    }

    /** Flips the given object
     * @param gamePanelId id of game panel
     * @param gameInstance Instance of Game
     * @param player Current player
     * @param objectInstance Instance of Object
     */
    public static void flipObject(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        if (objectInstance != null ) {
            player.actionString = "Flipped Object";
            gameInstance.update(new GamePlayerEditAction(gamePanelId, player, player));
            ((GameObjectToken.TokenState) objectInstance.state).side = !((GameObjectToken.TokenState) objectInstance.state).side;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance));

        }
    }

    /** Flips the given object
     * @param gamePanelId id of game panel
     * @param gameInstance Instance of Game
     * @param player Current player
     * @param objectInstance Instance of Object
     * @param include if object should be included in flipping, default is true
     */
    public static void flipStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, boolean include) {
        if (objectInstance != null) {//&& isStackInHand(gameInstance, player, getStack(gameInstance, objectInstance))
            IntegerArrayList objectStack = new IntegerArrayList();
            getStack(gameInstance, objectInstance, objectStack);
            int size = objectStack.size() - 1;
            for (int i = 0; i< objectStack.size(); ++i) {
                if (objectStack.get(i) != objectInstance.id || include) {
                    ObjectInstance currentObject = gameInstance.objects.get(objectStack.get(i));
                    if (!objectInstance.state.inPrivateArea) {
                        int aboveId = currentObject.state.aboveInstanceId;
                        currentObject.state.aboveInstanceId = currentObject.state.belowInstanceId;
                        currentObject.state.belowInstanceId = aboveId;
                        if (i <= objectStack.size() / 2) {
                            int posX = currentObject.state.posX;
                            int posY = currentObject.state.posY;

                            currentObject.state.posX = gameInstance.objects.get(objectStack.get(size - i)).state.posX;
                            currentObject.state.posY = gameInstance.objects.get(objectStack.get(size - i)).state.posY;
                            gameInstance.objects.get(objectStack.get(size - i)).state.posX = posX;
                            gameInstance.objects.get(objectStack.get(size - i)).state.posY = posY;
                        }
                    }
                    flipObject(gamePanelId, gameInstance, player, currentObject);
                }
            }
            player.actionString = "Flipped Stack";
            gameInstance.update(new GamePlayerEditAction(gamePanelId, player, player));
        }
    }
    public static void flipStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        flipStack(gamePanelId, gameInstance, player, objectInstance, true);
    }

    /** Counts number of elements in stack
     * @param gameInstance Instance of game
     * @param stackObject Instance of object in stack
     * @return number of elements in stack
     */
    public static int countStack(GameInstance gameInstance, ObjectInstance stackObject) {
    	IntegerArrayList stack = new IntegerArrayList();
        getStack(gameInstance, stackObject, stack);
        return stack.size();
    }

    /** Counts number of elements above stackObject in stack
     * @param gameInstance Instance of game
     * @param stackObject Instance of object in stack
     * @param include if stackObject should be also counted, default true
     * @return number of elements in stack above stackObject
     */
    public static int countAboveStack(GameInstance gameInstance, ObjectInstance stackObject, boolean include) {
    	IntegerArrayList stack = new IntegerArrayList();
        getAboveStack(gameInstance, stackObject, include, stack);
        return stack.size();
    }
    public static int countAboveStack(GameInstance gameInstance, ObjectInstance objectInstance) {
        return countAboveStack(gameInstance, objectInstance, true);
    }

    /** Counts number of elements below stackObject in stack
     * @param gameInstance Instance of game
     * @param stackObject Instance of object in stack
     * @param include if stackObject should be also counted, default true
     * @return number of elements in stack below stackObject
     */
    public static int countBelowStack(GameInstance gameInstance, ObjectInstance stackObject, boolean include) {
    	IntegerArrayList ial = new IntegerArrayList();
    	getBelowStack(gameInstance, stackObject, include, ial);
        return ial.size();
    }
    public static int countBelowStack(GameInstance gameInstance, ObjectInstance objectInstance) {
        return countBelowStack(gameInstance, objectInstance, true);
    }

    /** Counts values of elements in stack
     * @param gameInstance Instance of game
     * @param stackObject Instance of object in stack
     * @param include if stackObject should be also counted, default true
     * @return sum of values of elements in stack
     */
    public static int countStackValues(GameInstance gameInstance, ObjectInstance stackObject, boolean include) {
    	IntegerArrayList stackIds = new IntegerArrayList();
        getBelowStack(gameInstance, stackObject, include, stackIds);
        int counter = 0;
        for (int id : stackIds) {
            GameObject currentObject = gameInstance.objects.get(id).go;
            if (currentObject instanceof GameObjectToken && (id != stackObject.id || include)) {
                counter += ((GameObjectToken) currentObject).value;
            }
        }
        return counter;
    }
    public static int countStackValues(GameInstance gameInstance, ObjectInstance objectInstance) {
        return countStackValues(gameInstance, objectInstance, true);
    }

    /** Removes stack relations of object Instance, i.e., set above and below instance id to -1
     * @param gamePanelId id of game panel
     * @param gameInstance Instance of game
     * @param player Instance of player
     * @param objectInstance Instance of object
     */
    public static void removeAboveBelow(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        if (objectInstance != null) {
            objectInstance.state.aboveInstanceId = -1;
            objectInstance.state.belowInstanceId = -1;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance));
        }
    }


    /** Removes  object from stack
     * @param gamePanel game panel
     * @param gameInstance Instance of game
     * @param player Instance of player
     * @param objectInstance Instance of object
     * @return removed object
     */
    public static ObjectInstance removeObject(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        ObjectInstance aboveObject = null;
        ObjectInstance belowObject = null;
        if (objectInstance.state.aboveInstanceId != -1) {
            aboveObject = gameInstance.objects.get(objectInstance.state.aboveInstanceId);
        }
        if (objectInstance.state.belowInstanceId != -1) {
            belowObject = gameInstance.objects.get(objectInstance.state.belowInstanceId);
        }
        if (aboveObject != null) {
            if (belowObject != null) {
                aboveObject.state.belowInstanceId = belowObject.id;
                belowObject.state.aboveInstanceId = aboveObject.id;
                moveAboveStackTo(gamePanel, gameInstance, player, objectInstance, objectInstance.state.posX, objectInstance.state.posY, false);
                gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player, belowObject));
            } else {
                aboveObject.state.belowInstanceId = -1;
            }
            gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player, aboveObject));
        } else {
            if (belowObject != null) {
                belowObject.state.aboveInstanceId = -1;
                gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player, belowObject));
            }
        }
        removeAboveBelow(gamePanel.id, gameInstance, player, objectInstance);
        return objectInstance;

    }


    /**
     * @param gameInstance 
     * @param player
     * @param xPos
     * @param yPos
     * @param maxInaccuracy
     * @return
     */
    //Get the top element stack around xPos, yPos with some inaccuracy
    public static ObjectInstance getTopActiveObjectByPosition(GameInstance gameInstance, Player player, int xPos, int yPos, int maxInaccuracy) {
        ObjectInstance activeObject = null;
        int distance = Integer.MAX_VALUE;
        for (ObjectInstance oi: gameInstance.objects) {
            int dist = isOnObject(xPos, yPos, oi, player.id, maxInaccuracy);
                   
            if (dist < distance) {
                activeObject = getStackTop(gameInstance, oi);
                distance = dist;
            }
        }

        return activeObject;
    }

    public static ObjectInstance getTopActiveObjectByPosition(GameInstance gameInstance, Player player, int xPos, int yPos) {
        return getTopActiveObjectByPosition(gameInstance,player, xPos, yPos, 0);
    }

    private static int isOnObject(int xPos, int yPos, ObjectInstance oi, int playerid, int maxInaccuracy)
    {
    	int xDiff = xPos - (oi.state.posX + oi.getWidth(playerid) / 2), yDiff = yPos - (oi.state.posY + oi.getHeight(playerid) / 2);
        int dist = xDiff * xDiff + yDiff * yDiff;

        double sin = Math.sin(oi.state.rotation), cos = Math.cos(oi.state.rotation);
        double transformedX = xPos * cos + yPos * sin;
        double transformedY =-xPos * sin + yPos * cos;
        boolean leftIn = (transformedX > (oi.state.posX - maxInaccuracy));
        boolean rightIn = (transformedX < (oi.state.posX + oi.getWidth(playerid) + maxInaccuracy));
        boolean topIn = (transformedY < (oi.state.posY + oi.getHeight(playerid) + maxInaccuracy));
        boolean bottomIn = (transformedY > (oi.state.posY - maxInaccuracy));
        return leftIn && rightIn && topIn && bottomIn ? dist : Integer.MAX_VALUE;
    }
    
    //Get element nearest to xPos, yPos with some inaccuracy
    public static ObjectInstance getNearestObjectByPosition(GamePanel gamePanel, GameInstance gameInstance, Player player, int xPos, int yPos, double zooming, int maxInaccuracy, IntegerArrayList ignoredObjects) {
        ObjectInstance activeObject = null;
        int distance = Integer.MAX_VALUE;
        if(isInPrivateArea(gamePanel, xPos, yPos)) {
            Point2D transformedPoint = gamePanel.privateArea.transformPoint(xPos, yPos);
            if (gamePanel.privateArea.privateObjects.size() > 0) {
                activeObject = gameInstance.objects.get(gamePanel.privateArea.getObjectIdByPosition((int) transformedPoint.getX(), (int) transformedPoint.getY()));
            }
        }
        else {
            for (ObjectInstance oi : gameInstance.objects) {
                if ((ignoredObjects == null || !ignoredObjects.contains(oi.id)) && !oi.state.inPrivateArea) {
                    int dist = isOnObject(xPos, yPos, oi, player.id, maxInaccuracy);
                    if (dist < distance) {
                        activeObject = oi;
                        distance = dist;
                    }
                }
            }
        }
        return activeObject;
    }

    public static ObjectInstance getNearestObjectByPosition(GamePanel gamePanel, GameInstance gameInstance, Player player, int xPos, int yPos, double zooming, IntegerArrayList ignoredObjects) {
        ObjectInstance currentObject = getNearestObjectByPosition(gamePanel, gameInstance, player, xPos, yPos, zooming, 0, ignoredObjects);
        if (haveSamePositions(getStackTop(gameInstance, currentObject), getStackBottom(gameInstance, currentObject)) && currentObject.state.inPrivateArea==false)
        {
            return getStackTop(gameInstance, currentObject);
        }
        else{
            return currentObject;
        }

    }

    public static ObjectInstance getNearestObjectInPrivateArea(GamePanel gamePanel, GameInstance gameInstance){
        ObjectInstance activeObject = null;
        return activeObject;
    }


    //show all objects below element objectInstance
    public static void displayStack(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance objectInstance, int cardMargin) {
        if (objectInstance != null) {
            IntegerArrayList belowList = new IntegerArrayList();
            getBelowStack(gameInstance, objectInstance, belowList);
            if (belowList.size() > 1) {
                int posX = objectInstance.state.posX;
                int posY = objectInstance.state.posY;

                if (haveSamePositions(gameInstance.objects.get(belowList.get(0)), gameInstance.objects.get(belowList.last()))) {
                    for (int i = 0; i < belowList.size(); i++) {
                        moveObjectTo(gamePanel, gameInstance, player, gameInstance.objects.get(belowList.get(i)), (int) (posX - (belowList.size() / 2.0 - i) * cardMargin), posY);
                        //removeFromStack(gamePanelId, gameInstance, player, gameInstance.objects.get(belowList.get(i)));
                    }
                }
            }

        }
    }

    public static void displayStack(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        displayStack(gamePanel, gameInstance, player, objectInstance, 0);
    }

    //Move the whole stack to element object instance
    public static void collectStack(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        if (!haveSamePositions(getStackBottom(gameInstance, objectInstance), getStackTop(gameInstance, objectInstance))) {
            IntegerArrayList stack = new IntegerArrayList();
            getStackFromTop(gameInstance, objectInstance, stack);
            for(int id: stack)
            {
                moveObjectTo(gamePanel, gameInstance, player, gameInstance.objects.get(id), objectInstance);
            }
        }
    }

    //check if objectInstance is at the bottom of a stack
    public static boolean isStackBottom(ObjectInstance objectInstance) {
        if (objectInstance.state.belowInstanceId == -1) {
            return true;
        } else {
            return false;
        }
    }

    //Check if object Instance is at the top of a stack
    public static boolean isStackTop(ObjectInstance objectInstance) {
        if (objectInstance.state.aboveInstanceId == -1) {
            return true;
        } else {
            return false;
        }
    }

    //check if two objects have the same position
    public static boolean haveSamePositions(ObjectInstance objectInstanceA, ObjectInstance objectInstanceB) {
        if(objectInstanceA!= null && objectInstanceB!= null)
            return (objectInstanceA.state.posX == objectInstanceB.state.posX && objectInstanceA.state.posY == objectInstanceB.state.posY);
        else
            return false;
    }

    //check if two objects have the same position
    public static boolean isStackCollected(GameInstance gameInstance, ObjectInstance objectInstance) {
        return haveSamePositions(getStackTop(gameInstance, objectInstance), getStackBottom(gameInstance,objectInstance));
    }

    //remove all relations in an object stack
    public static void removeStackRelations(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        if (objectInstance != null) {
            IntegerArrayList stackList = new IntegerArrayList();
            getStackFromTop(gameInstance, objectInstance, stackList);
            for (int x : stackList) {
            	ObjectState state = gameInstance.objects.get(x).state;
                state.aboveInstanceId = -1;
                state.belowInstanceId = -1;
            }
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance));
        }
    }

    //take an object in the hand of player
    public static void takeObjects(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        IntegerArrayList stackIds = new IntegerArrayList();
        getStack(gameInstance, objectInstance, stackIds);
        for(int id: stackIds)
        {
            ObjectInstance currentInstance = gameInstance.objects.get(id);
            if (player.id != currentInstance.state.owner_id && currentInstance.state.owner_id == -1) {
                currentInstance.state.owner_id = player.id;
                currentInstance.state.inPrivateArea = true;
                gamePanel.privateArea.insertObject(currentInstance.id, 0);
                gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player, currentInstance));
            }
        }
    }

    //drop an object from the hand of player
    public static void dropObjects(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        IntegerArrayList stackIds = new IntegerArrayList();
        getStack(gameInstance, objectInstance, stackIds);
        for(int id: stackIds) {
            ObjectInstance currentInstance = gameInstance.objects.get(id);
            if (player.id == currentInstance.state.owner_id) {
                currentInstance.state.owner_id = -1;
                flipObject(gamePanelId, gameInstance, player, currentInstance);
                gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, currentInstance));
            }
        }
    }


    public static ObjectInstance setActiveObjectByMouseAndKey(GamePanel gamePanel, GameInstance gameInstance, Player player, Vector2 mouse, boolean isShiftDown, int maxInaccuracy) {
        ObjectInstance activeObject = null;
        int pressedXPos = mouse.getXI();
        int pressedYPos = mouse.getYI();
        activeObject = getNearestObjectByPosition(gamePanel,gameInstance, player,pressedXPos,pressedYPos,1,null);
        if (isShiftDown){
            activeObject = getStackBottom(gameInstance, activeObject);
        }
        if(activeObject != null && activeObject.state.owner_id != -1 && activeObject.state.owner_id!=player.id){
            return null;
        }
        return activeObject;
    }


    public static void mergeStacks(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance topStackInstance, ObjectInstance bottomStackInstance) {
        moveStackTo(gamePanel, gameInstance, player, topStackInstance, bottomStackInstance);
        ObjectInstance topElement = getStackTop(gameInstance, bottomStackInstance);
        ObjectInstance bottomElement = getStackBottom(gameInstance, topStackInstance);
        topElement.state.aboveInstanceId = bottomElement.id;
        bottomElement.state.belowInstanceId = topElement.id;
        gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player, topElement));
        gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player, bottomElement));

    }

    public static void releaseObjects(MouseEvent arg0, GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance activeObject, int posX, int posY, double zooming, int maxInaccuracy) {
        if(activeObject != null) {
            IntegerArrayList stackIds = new IntegerArrayList();
            if (gamePanel.privateArea != null && !gamePanel.privateArea.privateObjects.contains(activeObject.id) &&  gamePanel.privateArea.containsScreenCoordinates(posX, posY)) {
                ObjectFunctions.getStack(gameInstance, activeObject, stackIds);
                removeStackRelations(gamePanel.id, gameInstance, player, activeObject);
                for(int id: stackIds)
                {
                    ObjectInstance currentObject = gameInstance.objects.get(id);
                    gamePanel.privateArea.insertObject(currentObject.id, posX, posY);
                    currentObject.state.owner_id = player.id;
                    currentObject.state.inPrivateArea = true;
                    gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player, currentObject));
                }

            } else if(activeObject.state.owner_id == player.id && !gamePanel.privateArea.containsScreenCoordinates(posX, posY)) {
                activeObject.state.owner_id = -1;
                activeObject.state.inPrivateArea = false;
                if (SwingUtilities.isLeftMouseButton(arg0)) {
                    ObjectFunctions.flipObject(gamePanel.id, gameInstance, player, activeObject);
                }
            }
            gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player, activeObject));
        }

        if(activeObject != null && activeObject.state.owner_id != player.id) {
            IntegerArrayList stackIds = new IntegerArrayList();
            getStack(gameInstance, activeObject, stackIds);
            ObjectInstance objectInstance = getNearestObjectByPosition(gamePanel, gameInstance, player, activeObject.state.posX + activeObject.getWidth(player.id)/2, activeObject.state.posY + activeObject.getHeight(player.id)/2, zooming, stackIds);
            if (isStackCollected(gameInstance, objectInstance)) {
                if (objectInstance != activeObject) {
                    ObjectFunctions.mergeStacks(gamePanel, gameInstance, player, activeObject, objectInstance);
                }
            }
            else if(objectInstance != null)
            {
                Pair<ObjectInstance, ObjectInstance> insertObjects = getInsertObjects(gamePanel, gameInstance, player, activeObject.state.posX + activeObject.getWidth(player.id)/2, activeObject.state.posY + activeObject.getHeight(player.id)/2, zooming, new IntegerArrayList(activeObject.id));
                insertIntoStack(gamePanel, gameInstance, player, activeObject, insertObjects.getKey(), insertObjects.getValue(), activeObject.getWidth(player.id)/2);
            }
        }
    }


    public static void releaseObjects(MouseEvent arg0, GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance activeObject, int posX, int posY, double zooming) {
        releaseObjects(arg0, gamePanel, gameInstance, player, activeObject, posX, posY, zooming, activeObject.getWidth(player.id) / 3);
    }

    public static ObjectInstance findNeighbouredStackTop(GameInstance gameInstance, Player player, ObjectInstance activeObject, int maxInaccuracy) {
        for (int i = 0; i < gameInstance.objects.size(); ++i) {
            ObjectInstance oi = gameInstance.objects.get(i);
            int xDiff = activeObject.state.posX - oi.state.posX, yDiff = activeObject.state.posY - oi.state.posY;
            int dist = xDiff * xDiff + yDiff * yDiff;
            if (dist < maxInaccuracy * maxInaccuracy && oi != activeObject) {
                ObjectInstance topElement = ObjectFunctions.getStackTop(gameInstance, oi);
                if (!checkIfInStack(gameInstance, activeObject, topElement)) {
                    return topElement;
                }
            }
        }
        return null;
    }

    public static ObjectInstance findNeighbouredStackTop(GameInstance gameInstance, Player player, ObjectInstance activeObject) {
        return findNeighbouredStackTop(gameInstance, player, activeObject, activeObject.getWidth(-1) / 3);
    }

    public static boolean checkIfInStack(GameInstance gameInstance, ObjectInstance stackInstance, ObjectInstance checkInstance) {
        IntegerArrayList stackList = new IntegerArrayList();
        getStackFromTop(gameInstance, stackInstance, stackList);
        return stackList.contains(checkInstance.id);
    }

    public static void makeStack(int gamePanelId, GameInstance gameInstance, Player player, IntegerArrayList stackElements) {
        for (int i = 0; i < stackElements.size(); ++i) {
            if (i == 0 && stackElements.size() > 1) {
                gameInstance.objects.get(stackElements.get(i)).state.belowInstanceId = -1;
                gameInstance.objects.get(stackElements.get(i)).state.aboveInstanceId = gameInstance.objects.get(stackElements.get(i + 1)).id;
                gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, gameInstance.objects.get(stackElements.get(i))));
            } else if (i == stackElements.size() - 1 && stackElements.size() > 1) {
                gameInstance.objects.get(stackElements.get(i)).state.aboveInstanceId = -1;
                gameInstance.objects.get(stackElements.get(i)).state.belowInstanceId = gameInstance.objects.get(stackElements.get(i - 1)).id;
                setObjectPosition(gamePanelId, gameInstance, player, gameInstance.objects.get(stackElements.get(i)), gameInstance.objects.get(stackElements.get(i - 1)));
            } else {
                gameInstance.objects.get(stackElements.get(i)).state.aboveInstanceId = gameInstance.objects.get(stackElements.get(i + 1)).id;
                gameInstance.objects.get(stackElements.get(i)).state.belowInstanceId = gameInstance.objects.get(stackElements.get(i - 1)).id;
                setObjectPosition(gamePanelId, gameInstance, player, gameInstance.objects.get(stackElements.get(i)), gameInstance.objects.get(stackElements.get(i - 1)));
            }
        }
    }

    public static Pair<ObjectInstance, ObjectInstance> getInsertObjects(GamePanel gamePanel, GameInstance gameInstance, Player player, int posX, int posY, double zooming, IntegerArrayList ignoredObjects){
        ObjectInstance objectInstance = getNearestObjectByPosition(gamePanel, gameInstance, player, posX, posY, zooming, ignoredObjects);
        if (hasAboveObject(objectInstance) && hasBelowObject(objectInstance))
        {
            if(getDistanceToObjectCenter(getAboveObject(gameInstance, objectInstance), posX, posY, player.id) < getDistanceToObjectCenter(getBelowObject(gameInstance, objectInstance), posX, posY, player.id))
            {
                return new Pair<>(getAboveObject(gameInstance,objectInstance), objectInstance);
            }
            else
            {
                return new Pair<>(objectInstance, getBelowObject(gameInstance,objectInstance));
            }
        }
        else if (hasAboveObject(objectInstance))
        {
            return new Pair<>(getAboveObject(gameInstance, objectInstance), objectInstance);
        }
        else if(hasBelowObject(objectInstance)){
            return new Pair<>(objectInstance, getBelowObject(gameInstance, objectInstance));
        }
        else
            return new Pair<>(null, null);
    }

    public static int getDistanceToObjectCenter(ObjectInstance objectInstance, int posX, int posY, int playerId){
        int diffX = (posX - (objectInstance.state.posX + objectInstance.getWidth(playerId)/2));
        int diffY = (posY - (objectInstance.state.posY + objectInstance.getHeight(playerId)/2));
        return diffX*diffX + diffY*diffY;
    }

    public static void insertIntoStack(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance objectInstance, ObjectInstance objectAbove, ObjectInstance objectBelow, int cardMargin) {
            if (objectInstance != null) {
                if (objectBelow != null) {
                    objectBelow.state.aboveInstanceId = objectInstance.id;
                    objectInstance.state.belowInstanceId = objectBelow.id;
                    moveObjectTo(gamePanel, gameInstance, player, objectInstance, objectBelow.state.posX + cardMargin, objectBelow.state.posY);
                    if (objectAbove != null) {
                        objectAbove.state.belowInstanceId = objectInstance.id;
                        objectInstance.state.aboveInstanceId = objectAbove.id;
                        gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player, objectAbove));
                        moveObjectTo(gamePanel, gameInstance, player, objectInstance, objectAbove);
                        if (!isStackCollected(gameInstance, objectInstance)) {
                            moveAboveStackTo(gamePanel, gameInstance, player, objectInstance, objectInstance.state.posX - objectInstance.getWidth(player.id) / 2, objectInstance.state.posY, false);
                        }
                    }
                    gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player, objectInstance));
                    gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player, objectBelow));
                }
                else if(objectAbove != null) {
                    objectAbove.state.belowInstanceId = objectInstance.id;
                    objectInstance.state.aboveInstanceId = objectAbove.id;
                    gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player, objectInstance));
                    gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player, objectAbove));
                    moveObjectTo(gamePanel, gameInstance, player, objectInstance, objectAbove);
                    if (!isStackCollected(gameInstance, objectInstance)) {
                        moveAboveStackTo(gamePanel, gameInstance, player, objectInstance, objectInstance.state.posX - objectInstance.getWidth(player.id) / 2, objectInstance.state.posY, false);
                    }
                }
            }
    }

    public static void setObjectPosition(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, int posX, int posY) {
        objectInstance.state.posX = posX;
        objectInstance.state.posY = posY;
        gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance));
    }


    public static void setObjectPosition(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, ObjectInstance targetObjectInstance) {
        setObjectPosition(gamePanelId, gameInstance, player, objectInstance, targetObjectInstance.state.posX, targetObjectInstance.state.posY);
    }

    public static void getAllObjectsOfType(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        String objectType = objectInstance.go.objectType;
        IntegerArrayList objectTypeList = new IntegerArrayList();
        objectTypeList.add(objectInstance.id);
        for (int i = 0; i < gameInstance.objects.size(); ++i) {
            if (gameInstance.objects.get(i).go.objectType.equals(objectType) && i != objectInstance.id) {
                objectTypeList.add(i);
            }
        }
        makeStack(gamePanelId, gameInstance, player, objectTypeList);
    }

    public static IntegerArrayList getTopNObjects(GameInstance gameInstance, ObjectInstance objectInstance, int number) {
        IntegerArrayList objectList = new IntegerArrayList();
        ObjectInstance currentObject = getStackTop(gameInstance, objectInstance);
        objectList.add(currentObject.id);
        for (int i = 0; i < number - 1; ++i) {
            int belowId = currentObject.state.belowInstanceId;
            if (belowId != -1) {
                objectList.add(belowId);
                currentObject = gameInstance.objects.get(currentObject.state.belowInstanceId);
            } else {
                break;
            }
        }
        return objectList;
    }

    public static void splitStackAtN(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, int number) {
        IntegerArrayList topNObjects = ObjectFunctions.getTopNObjects(gameInstance, objectInstance, number);
        int splitObjectid = topNObjects.last();
        if (gameInstance.objects.get(splitObjectid).state.belowInstanceId == -1) {
            gameInstance.objects.get(splitObjectid).state.belowInstanceId = -1;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, gameInstance.objects.get(splitObjectid)));
        } else {
            gameInstance.objects.get(gameInstance.objects.get(splitObjectid).state.belowInstanceId).state.aboveInstanceId = -1;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, gameInstance.objects.get(gameInstance.objects.get(splitObjectid).state.belowInstanceId)));
            gameInstance.objects.get(splitObjectid).state.belowInstanceId = -1;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, gameInstance.objects.get(splitObjectid)));
        }
    }


    public static boolean isObjectInHand(Player player, ObjectInstance objectInstance){
        return objectInstance.state.owner_id == player.id;
    }


    public static int getStackOwner(GameInstance gameInstance, IntegerArrayList stackIds){
        /*
        int ownerId = -1;
        for(int id: stackIds)  //TODO: I don't think that this method is doing what it should do
        {
            if(ownerId == -1)
            {
                ownerId = gameInstance.objects.get(id).state.owner_id;
            }
            else if(gameInstance.objects.get(id).state.owner_id != ownerId)
                return -1;
        }*/
        return gameInstance.objects.get(stackIds.getI(0)).state.owner_id;
    }

    public static boolean isStackInHand(GameInstance gameInstance, Player player, IntegerArrayList stackIds){
        for(int id: stackIds)
        {
            if(!isObjectInHand(player, gameInstance.objects.get(id)))
            {
                return false;
            }
        }
        return true;
    }

    public static boolean isStackNotInHand(GameInstance gameInstance, Player player, IntegerArrayList stackIds) {
        for(int id: stackIds)
        {
            if(gameInstance.objects.get(id).state.owner_id != -1)
            {
                return false;
            }
        }
        return true;
    }

    public static void zoomObjects(GameInstance gameInstance, double zooming){
        for(ObjectInstance objectInstance: gameInstance.objects){
            objectInstance.state.posX = (int) (objectInstance.state.posX * zooming);
            objectInstance.state.posY = (int) (objectInstance.state.posY * zooming);
        }
    }


    public static void getObjectsInsideBox(GameInstance gameInstance, int posX, int posY, int width, int height, IntegerArrayList idList) {
        for (ObjectInstance objectInstance : gameInstance.objects) {
            boolean leftIn = (objectInstance.state.posX > posX);
            boolean rightIn = (objectInstance.state.posX < (posX + width));
            boolean topIn = (objectInstance.state.posY < (posY + height));
            boolean bottomIn = (objectInstance.state.posY > (posY));

            if (leftIn && rightIn && topIn && bottomIn) {
                idList.add(objectInstance.id);
            }
        }
    }

    public static boolean isInPrivateArea(GamePanel gamePanel,int posX, int posY)
    {
        return gamePanel.privateArea.containsBoardCoordinates(posX, posY);
    }


    public static boolean isStackInPrivateArea(GamePanel gamePanel, GameInstance gameInstance, IntegerArrayList stackIds)
    {
        if (stackIds.size()>0) {
            for (int id : stackIds) {
                if (!gamePanel.privateArea.privateObjects.contains(id))
                    return false;
            }
            return true;
        }
        return false;
    }

}
