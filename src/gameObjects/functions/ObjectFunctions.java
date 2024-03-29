package gameObjects.functions;

import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.action.player.PlayerEditAction;
import gameObjects.definition.GameObjectBook;
import gameObjects.definition.GameObjectBox;
import gameObjects.definition.GameObjectDice;
import gameObjects.definition.GameObjectToken;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.ObjectInstance.Relation;
import gameObjects.instance.ObjectState;
import geometry.Vector2;
import geometry.Vector2d;
import gui.game.GamePanel;
import gui.game.PrivateArea;
import main.Player;
import util.ArrayUtil;
import util.AwtGeometry;
import util.Pair;
import util.data.IntegerArrayList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.function.Predicate;

public class ObjectFunctions {
    public static final int SIDE_TO_FRONT = 1;
    public static final int SIDE_UNCHANGED = 0;
    public static final int SIDE_TO_BACK = -1;
    public static final int SIDE_FLIP = 2;


    //private static final Logger logger = LoggerFactory.getLogger(ObjectFunctions.class);

    /**
     * Get the top of the stack with with element objectInstance
     *
     * @param gameInstance   Instance of the game
     * @param objectInstance Instance of the stack
     * @return top Instance of the stack
     */
    public static ObjectInstance getStackTop(GameInstance gameInstance, ObjectInstance objectInstance) {
        if (objectInstance != null && objectInstance.go instanceof GameObjectToken) {
            ObjectInstance currentTop = objectInstance;
            int i = 0;
            while (currentTop.state.aboveInstanceId != -1) {
                currentTop = gameInstance.getObjectInstanceById(currentTop.state.aboveInstanceId);
                if (objectInstance == currentTop) {
                    throw new RuntimeException();
                }
                if (++i > gameInstance.getObjectInstanceCount()) {
                    throw new RuntimeException("Circle in Card Stack");
                }
            }
            return currentTop;
        } else {
            return null;
        }
    }

    /**
     * Get the bottom of the stack with with element objectInstance
     *
     * @param gameInstance   Instance of the game
     * @param objectInstance Instance of the stack
     * @return bottom Instance of the stack
     */
    public static ObjectInstance getStackBottom(GameInstance gameInstance, ObjectInstance objectInstance) {
        if (objectInstance != null && objectInstance.go instanceof GameObjectToken) {
            ObjectInstance currentBottom = objectInstance;
            int i = 0;
            while (currentBottom.state.belowInstanceId != -1) {
                currentBottom = gameInstance.getObjectInstanceById(currentBottom.state.belowInstanceId);
                if (objectInstance == currentBottom) {
                    throw new RuntimeException("Reached first card again " + currentBottom.id + " below " + objectInstance.state.belowInstanceId);
                }
                if (++i > gameInstance.getObjectInstanceCount()) {
                    throw new RuntimeException("Circle in Card Stack " + currentBottom.id);
                }
            }
            return currentBottom;
        } else {
            return null;
        }
    }


    /**
     * Checks if above Instance exists
     *
     * @param objectInstance object Instance
     * @return true if above Instance exists false otherwise
     */
    public static boolean hasAboveObject(ObjectInstance objectInstance) {
        if (objectInstance != null && objectInstance.go instanceof GameObjectToken)
            return objectInstance.state.aboveInstanceId != -1;
        else
            return false;
    }

    /**
     * gets above instance id
     *
     * @param objectInstance object Instance
     * @return id of above Instance or -1 if it does not exist
     */
    public static int getAboveObjectId(ObjectInstance objectInstance) {
        if (hasAboveObject(objectInstance)) {
            return objectInstance.state.aboveInstanceId;
        } else
            return -1;
    }

    /**
     * @param gameInstance   Instance of the game
     * @param objectInstance Instance of an object
     * @return Instance of above object or null if it does not exist
     */
    public static ObjectInstance getAboveObject(GameInstance gameInstance, ObjectInstance objectInstance) {
        if (hasAboveObject(objectInstance)) {
            return gameInstance.getObjectInstanceById(objectInstance.state.aboveInstanceId);
        } else
            return null;
    }


    /**
     * Checks if below Instance exists
     *
     * @param objectInstance Instance of object
     * @return true if above Instance exists false otherwise
     */
    public static boolean hasBelowObject(ObjectInstance objectInstance) {
        if (objectInstance != null && objectInstance.go instanceof GameObjectToken)
            return objectInstance.state.belowInstanceId != -1;
        else
            return false;
    }

    /**
     * gets below instance id
     *
     * @param objectInstance object Instance
     * @return id of below Instance or -1 if it does not exist
     */
    public static int getBelowObject(ObjectInstance objectInstance) {
        if (hasBelowObject(objectInstance)) {
            return objectInstance.state.belowInstanceId;
        } else
            return -1;
    }

    /**
     * @param gameInstance   Instance of the game
     * @param objectInstance Instance of an object
     * @return Instance of below object or null if it does not exist
     */
    public static ObjectInstance getBelowObject(GameInstance gameInstance, ObjectInstance objectInstance) {
        if (hasBelowObject(objectInstance)) {
            return gameInstance.getObjectInstanceById(objectInstance.state.belowInstanceId);
        } else
            return null;
    }

    /**
     * @param gameInstance   Instance of the game
     * @param objectInstance Instance of the object
     * @param included       if objectInstance should be included default is true
     * @return all ids of above Elements in the Stack starting with the bottom id
     */
    public static void getToOneEnd(GameInstance gameInstance, ObjectInstance objectInstance, boolean included, IntegerArrayList objectStack, Relation relation) {
        if (objectInstance != null && objectInstance.go instanceof GameObjectToken) {
            objectStack.clear();
            if (included) {
                objectStack.add(objectInstance.id);
            }
            ObjectInstance currentObjectInstance = objectInstance;
            int next;
            while ((next = currentObjectInstance.state.getRelatedId(relation)) != -1) {
                objectStack.add(next);
                currentObjectInstance = gameInstance.getObjectInstanceById(next);
                if (gameInstance.getObjectInstanceCount() < objectStack.size()) {
                    throw new RuntimeException("Circle in stack " + objectStack.toString() + " has more elements than the whole game " + gameInstance.getObjectInstanceCount());
                }
            }
        }
    }

    public static void getToOneEnd(GameInstance gameInstance, ObjectInstance objectInstance, boolean included, ArrayList<ObjectInstance> oiList, Relation relation) {
        if (objectInstance != null && objectInstance.go instanceof GameObjectToken) {
            oiList.clear();
            if (included) {
                oiList.add(objectInstance);
            }
            ObjectInstance currentObjectInstance = objectInstance;
            int next;
            while ((next = currentObjectInstance.state.getRelatedId(relation)) != -1) {
                currentObjectInstance = gameInstance.getObjectInstanceById(next);
                oiList.add(currentObjectInstance);
                if (gameInstance.getObjectInstanceCount() < oiList.size()) {
                    throw new RuntimeException("Circle in stack " + gameInstance.getObjectInstanceCount() + oiList.size());
                }
            }
        }
    }

    public static void getStackToOneEnd(GameInstance gameInstance, ObjectInstance objectInstance, IntegerArrayList objectStack, Relation relation) {
        getToOneEnd(gameInstance, objectInstance, true, objectStack, relation);
    }

    public static void getStackToOneEnd(GameInstance gameInstance, ObjectInstance objectInstance, ArrayList<ObjectInstance> oiList, Relation relation) {
        getToOneEnd(gameInstance, objectInstance, true, oiList,relation);
    }

    /**
     * @param gameInstance   Instance of the game
     * @param objectInstance Instance of the object
     * @return all ids of stack Elements in the Stack starting with the top id
     */
    public static void getStackFromTop(GameInstance gameInstance, ObjectInstance objectInstance, IntegerArrayList ial) {
        ial.clear();
        getStackToOneEnd(gameInstance, getStackTop(gameInstance, objectInstance), ial, Relation.BELOW);
    }

    /**
     * @param gameInstance   Instance of the game
     * @param objectInstance Instance of the object
     * @return all ids of stack Elements in the Stack starting with the bottom id
     */
    public static void getStackFromBottom(GameInstance gameInstance, ObjectInstance objectInstance, IntegerArrayList objectStack) {
        objectStack.clear();
        getStackToOneEnd(gameInstance, getStackBottom(gameInstance, objectInstance), objectStack, Relation.ABOVE);
    }

    public static void getStackFromBottom(GameInstance gameInstance, ObjectInstance objectInstance, ArrayList<ObjectInstance> oiList) {
        oiList.clear();
        getStackToOneEnd(gameInstance, getStackBottom(gameInstance, objectInstance), oiList, Relation.ABOVE);
    }

    /**
     * @param gameInstance   Instance of the game
     * @param objectInstance Instance of the object
     * @return all ids of stack Elements in the Stack starting with the top id
     */
    public static void getStack(GameInstance gameInstance, ObjectInstance objectInstance, IntegerArrayList objectStack) {
        getStackFromTop(gameInstance, objectInstance, objectStack);
    }

    public static void makeStack(int gamePanelId, GameInstance gameInstance, Player player, IntegerArrayList objectStack, IntegerArrayList oldPos){
        for (int i = 0; i < objectStack.size(); i++) {
            ObjectInstance currentObject = gameInstance.getObjectInstanceById(objectStack.getI(i));
            ObjectState state = currentObject.state.copy();
            if (i == 0) {
                state.belowInstanceId = -1;
                state.aboveInstanceId = objectStack.getI(i + 1);
            } else if (i == objectStack.size() - 1) {
                state.belowInstanceId = objectStack.getI(i - 1);
                state.aboveInstanceId = -1;
            } else {
                state.belowInstanceId = objectStack.getI(i - 1);
                state.aboveInstanceId = objectStack.getI(i + 1);
            }
            state.posX = oldPos.getI(i*2);
            state.posY = oldPos.getI(i*2+1);
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, currentObject, state));
        }
    }

    private static IntegerArrayList getStackPositions(GameInstance gameInstance, IntegerArrayList objectStack, IntegerArrayList positions)
    {
        for (int id : objectStack) {
            ObjectInstance inst = gameInstance.getObjectInstanceById(id);
            positions.add(inst.state.posX);
            positions.add(inst.state.posY);
        }
        return positions;
    }

    /**
     * Shuffles the given stack
     *  @param gamePanel    id of game panel
     * @param gameInstance   Instance of Game
     * @param player         Current player
     * @param objectInstance Instance of Object
     * @param include        if object should be included in shuffling operation, default is true
     */
    public static void shuffleStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, boolean include) {
        if (objectInstance != null && objectInstance.go instanceof GameObjectToken) {
            IntegerArrayList objectStack = new IntegerArrayList();
            getStackFromBottom(gameInstance, objectInstance, objectStack);
            removeStackRelations(gamePanelId, gameInstance, player, objectInstance);
            if (objectStack.size() > 1) {
                IntegerArrayList oldPos = new IntegerArrayList();
                getStackPositions(gameInstance, objectStack, oldPos);
                Collections.shuffle(objectStack);
                player.actionString = objectStack.size() + " Objects Shuffled";
                makeStack(gamePanelId, gameInstance, player, objectStack, oldPos);
            }
        }
    }

    public static void shuffleStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        shuffleStack(gamePanelId, gameInstance, player, objectInstance, true);
    }

    /**
     * Flips the given object
     *
     * @param gamePanelId    id of game panel
     * @param gameInstance   Instance of Game
     * @param player         Current player
     * @param objectInstance Instance of Object
     */
    public static void flipTokenObject(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        if (objectInstance != null && objectInstance.go instanceof GameObjectToken) {
            player.actionString = "Object Flipped";
            GameObjectToken.TokenState state = ((GameObjectToken.TokenState) objectInstance.state.copy());
            state.side = !state.side;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance, state));
        }
    }

    public static void flipTokenToSide(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, boolean front) {
        if (objectInstance != null && objectInstance.go instanceof GameObjectToken) {
            if (player != null) {
                player.actionString = "Object Flipped to Side";
            }
            GameObjectToken.TokenState state = ((GameObjectToken.TokenState) objectInstance.state.copy());
            state.side = !front;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance, state));
        }
    }

    public static void rollTheDice(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance activeObject) {
        if (activeObject != null && activeObject.go instanceof GameObjectDice) {
            GameObjectDice diceObject = (GameObjectDice) activeObject.go;
            Random rnd = new Random();
            GameObjectDice.DiceState state = (GameObjectDice.DiceState) activeObject.state;
            diceObject.rollTheDice(state, rnd);
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, activeObject, state));
            player.actionString = "Rolled Dice";
        }
    }

    public static void nextBookPage(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance){
        if (objectInstance != null && objectInstance.go instanceof GameObjectBook){
            GameObjectBook gameObjectBook = (GameObjectBook) objectInstance.go;
            GameObjectBook.BookState state = (GameObjectBook.BookState) objectInstance.state;
            gameObjectBook.nextPage(state);
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance, state));
            player.actionString = "Next Page";
        }
    }

    public static void previousBookPage(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance){
        if (objectInstance != null && objectInstance.go instanceof GameObjectBook){
            GameObjectBook gameObjectBook = (GameObjectBook) objectInstance.go;
            GameObjectBook.BookState state = (GameObjectBook.BookState) objectInstance.state;
            gameObjectBook.previousPage(state);
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance, state));
            player.actionString = "Previous Page";
        }
    }

    /**
     * Flips the given object
     *
     * @param gamePanelId    game panel
     * @param gameInstance   Instance of Game
     * @param player         Current player
     * @param objectInstance Instance of Object
     * @param include        if object should be included in flipping, default is true
     */
    public static void flipTokenStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, ObjectInstance hoveredObject, boolean include) {
        if (objectInstance != null && objectInstance.go instanceof GameObjectToken) {
            IntegerArrayList objectStack = new IntegerArrayList();
            getStack(gameInstance, objectInstance, objectStack);
            int size = objectStack.size() - 1;
            for (int i = 0; i < objectStack.size(); ++i) {
                if (objectStack.get(i) != objectInstance.id || include) {
                    ObjectInstance currentObject = gameInstance.getObjectInstanceById(objectStack.get(i));
                    if (!objectInstance.state.inPrivateArea) {
                        int aboveId = currentObject.state.aboveInstanceId;
                        ObjectState state = currentObject.state.copy();
                        state.aboveInstanceId = currentObject.state.belowInstanceId;
                        state.belowInstanceId = aboveId;
                        gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, currentObject, state));
                        if (false && i <= objectStack.size() / 2) {
                            int posX = currentObject.state.posX;
                            int posY = currentObject.state.posY;
                            int posXNew = gameInstance.getObjectInstanceById(objectStack.get(size - i)).state.posX;
                            int posYNew = gameInstance.getObjectInstanceById(objectStack.get(size - i)).state.posY;
                            MoveFunctions.moveObjectTo(gamePanelId,gameInstance,player,currentObject, posXNew, posYNew);
                            MoveFunctions.moveObjectTo(gamePanelId,gameInstance,player,gameInstance.getObjectInstanceById(objectStack.get(size - i)), posX, posY);
                        }
                    }
                    flipTokenObject(gamePanelId, gameInstance, player, currentObject);
                }
            }
            player.actionString = "Stack Flipped";
            selectStack(gamePanelId, gameInstance, player, objectStack.getI(0), hoveredObject);
        }
    }

    public static void flipTokenStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, ObjectInstance hoveredObject) {
        flipTokenStack(gamePanelId, gameInstance, player, objectInstance, hoveredObject, true);
    }

    /**
     * Counts number of elements in stack
     *
     * @param gameInstance Instance of game
     * @param stackObject  Instance of object in stack
     * @return number of elements in stack
     */
    public static int countStack(GameInstance gameInstance, ObjectInstance stackObject) {
        if (stackObject != null && stackObject.go instanceof GameObjectToken) {//TODO this is slow try to not allocate ram in a counting method
            IntegerArrayList stack = new IntegerArrayList();
            getStack(gameInstance, stackObject, stack);
            return stack.size();
        }
        return 0;
    }

    /**
     * Counts number of elements above stackObject in stack
     *
     * @param gameInstance Instance of game
     * @param stackObject  Instance of object in stack
     * @param include      if stackObject should be also counted, default true
     * @return number of elements in stack above stackObject
     */
    public static int countAboveStack(GameInstance gameInstance, ObjectInstance stackObject, boolean include) {
        if (stackObject != null && stackObject.go instanceof GameObjectToken) {
            IntegerArrayList stack = new IntegerArrayList();
            getToOneEnd(gameInstance, stackObject, include, stack, Relation.ABOVE);
            return stack.size();
        }
        return 0;
    }

    public static int countAboveStack(GameInstance gameInstance, ObjectInstance objectInstance) {
        return countAboveStack(gameInstance, objectInstance, true);
    }

    /**
     * Counts number of elements below stackObject in stack
     *
     * @param gameInstance Instance of game
     * @param stackObject  Instance of object in stack
     * @param include      if stackObject should be also counted, default true
     * @return number of elements in stack below stackObject
     */
    public static int countBelowStack(GameInstance gameInstance, ObjectInstance stackObject, boolean include) {
        if (stackObject != null && stackObject.go instanceof GameObjectToken) {
            IntegerArrayList ial = new IntegerArrayList();
            getToOneEnd(gameInstance, stackObject, include, ial, Relation.BELOW);
            return ial.size();
        }
        return 0;
    }

    public static int countBelowStack(GameInstance gameInstance, ObjectInstance objectInstance) {
        return countBelowStack(gameInstance, objectInstance, true);
    }

    /**
     * Counts values of elements in stack
     *
     * @param gameInstance Instance of game
     * @param stackObject  Instance of object in stack
     * @param include      if stackObject should be also counted, default true
     * @return sum of values of elements in stack
     */
    public static int countStackValues(GameInstance gameInstance, ObjectInstance stackObject, boolean include) {
        if (stackObject != null && stackObject.go instanceof GameObjectToken) {
            IntegerArrayList stackIds = new IntegerArrayList();
            getToOneEnd(gameInstance, stackObject, include, stackIds, Relation.BELOW);
            int counter = 0;
            for (int id : stackIds) {
                ObjectInstance currentObject = gameInstance.getObjectInstanceById(id);
                if (currentObject.go instanceof GameObjectToken && (id != stackObject.id || include)) {
                    counter += currentObject.state.value;
                }
            }
            return counter;
        }
        return 0;
    }

    public static int countStackValues(GameInstance gameInstance, ObjectInstance objectInstance) {
        return countStackValues(gameInstance, objectInstance, true);
    }

    /**
     * Removes stack relations of object Instance, i.e., set above and below instance id to -1
     *
     * @param gamePanelId    id of game panel
     * @param gameInstance   Instance of game
     * @param player         Instance of player
     * @param objectInstance Instance of object
     */
    public static void removeAboveBelow(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        if (objectInstance != null && objectInstance.go instanceof GameObjectToken) {
        	ObjectState state = objectInstance.state.copy();
            state.aboveInstanceId = -1;
            state.belowInstanceId = -1;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance, state));
        }
    }


    /**
     * Removes  object from stack
     *
     * @param gamePanelId      game panel
     * @param gameInstance   Instance of game
     * @param player         Instance of player
     * @param objectInstance Instance of object
     * @return removed object
     */
    public static ObjectInstance removeObject(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        if (objectInstance != null && objectInstance.go instanceof GameObjectToken) {
            ObjectInstance aboveObject = null;
            ObjectInstance belowObject = null;
            if (objectInstance.state.aboveInstanceId != -1) {
                aboveObject = gameInstance.getObjectInstanceById(objectInstance.state.aboveInstanceId);
            }
            if (objectInstance.state.belowInstanceId != -1) {
                belowObject = gameInstance.getObjectInstanceById(objectInstance.state.belowInstanceId);
            }
            if (aboveObject != null) {
            	ObjectState aboveState = aboveObject.state.copy();
                if (belowObject != null) {
                	ObjectState belowState = belowObject.state.copy();
                    aboveState.belowInstanceId = belowObject.id;
                    belowState.aboveInstanceId = aboveObject.id;
                    if(!isStackCollected(gameInstance,aboveObject)) {
                        MoveFunctions.moveAboveStackTo(gamePanelId, gameInstance, player, objectInstance, objectInstance.state.posX, objectInstance.state.posY, false);
                    }
                    gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, belowObject, belowState));
                } else {
                    aboveState.belowInstanceId = -1;
                }
                gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, aboveObject, aboveState));
            } else {
                if (belowObject != null) {
                   	ObjectState belowState = belowObject.state.copy();
                   	belowState.aboveInstanceId = -1;
                    gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, belowObject, belowState));
                }
            }
            removeAboveBelow(gamePanelId, gameInstance, player, objectInstance);
        }
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
        for (int idx = 0; idx < gameInstance.getObjectInstanceCount();++idx) {
            ObjectInstance oi = gameInstance.getObjectInstanceByIndex(idx);
            int dist = getObjectDistanceTo(oi, xPos, yPos);
            if (dist < distance && isOnObject(xPos, yPos, oi, player.id, maxInaccuracy)) {
                activeObject = getStackTop(gameInstance, oi);
                distance = dist;
            }
        }
        return activeObject;
    }

    public static ObjectInstance getTopActiveObjectByPosition(GameInstance gameInstance, Player player, int xPos, int yPos) {
        return getTopActiveObjectByPosition(gameInstance, player, xPos, yPos, 0);
    }

    private static void getRelativeObjectBorderPoints(int playerId, ObjectInstance oi, ArrayList<Point2D> BorderPoints){
        int oiw = oi.getWidth(playerId), oih = oi.getHeight(playerId);
        //TODO Handle objects of different shape, i.e. cirlces, ellipse
        BorderPoints.clear();
        BorderPoints.add(new Point2D.Double(oiw/2, oih/2));
        BorderPoints.add(new Point2D.Double(oiw/2, -oih/2));
        BorderPoints.add(new Point2D.Double(-oiw/2, oih/2));
        BorderPoints.add(new Point2D.Double(-oiw/2, -oih/2));
    }
    private static void getAbsoluteObjectBorderPoints(int playerId, ObjectInstance oi, ArrayList<Point2D> BorderPoints){
        ArrayList<Point2D> RelativeLocations = new ArrayList<>();
        getRelativeObjectBorderPoints(playerId, oi, RelativeLocations);
        BorderPoints.clear();
        for (Point2D Point : RelativeLocations) {
            int xCenter = oi.state.posX;
            int yCenter = oi.state.posY;
            double radians = oi.state.rotation * 2 * Math.PI / 360;
            double sin = Math.sin(radians), cos = Math.cos(radians);
            double transformedX = Point.getX() * cos - Point.getY() * sin + xCenter;
            double transformedY = Point.getY() * sin + Point.getY() * cos + yCenter;
            BorderPoints.add(new Point2D.Double(transformedX, transformedY));
        }
    }

    private static boolean liesOnObject(ObjectInstance baseObject, ObjectInstance oi, int playerId, int maxInaccuracy) {
        ArrayList<Point2D> BorderPoints = new ArrayList<>();
        getAbsoluteObjectBorderPoints(playerId, oi, BorderPoints);
        for (Point2D Point : BorderPoints){
            if (!isOnObject((int) Point.getX(), (int) Point.getY(), baseObject, playerId, maxInaccuracy)){
                return false;
            }
        }
        return true;
    }

    private static boolean isValidLieOnObject(Player player, ObjectInstance oi){
        return !oi.state.isFixed && !oi.state.inBox && !oi.state.inPrivateArea && (oi.state.owner_id == -1 || oi.state.owner_id == player.id);
    }



    private static ObjectInstance getGroundObject(GameInstance gameInstance, Player player, ObjectInstance oi, int playerId, int maxInaccuracy) {
        for (int idx = 0; idx < gameInstance.getObjectInstanceCount();++idx) {
            ObjectInstance currentObject = gameInstance.getObjectInstanceByIndex(idx);
            if (oi.id != currentObject.id && isValidLieOnObject(player, currentObject) && liesOnObject(currentObject, oi, player.id, maxInaccuracy)){
                int Counter = 0;
                while (currentObject.state.aboveLyingObectIds.size() > 0 && Counter < gameInstance.getObjectInstanceCount()){
                    for (int id : currentObject.state.aboveLyingObectIds){
                        if (liesOnObject(gameInstance.getObjectInstanceById(id), oi, playerId, maxInaccuracy)){
                            currentObject = gameInstance.getObjectInstanceByIndex(id);
                        }
                    }
                    ++Counter;
                }
                return currentObject;
            }
        }
        return null;
    }

    public static void lieObjectOnObject(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance groundObject, ObjectInstance activeObject) {
        ObjectState groundState = groundObject.state.copy();
        ObjectState activeState = activeObject.state.copy();
        if (groundObject.go instanceof GameObjectBox){
            GameObjectBox.BoxState boxState = (GameObjectBox.BoxState) groundObject.state;
            activeState.owner_select_reset();
            activeState.boxId = boxState.boxId;
            activeState.inBox = true;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, activeObject, activeState));
        }
        else {
            activeState.liesOnId = groundObject.id;
            if (!groundState.aboveLyingObectIds.contains(activeObject.id)) {
                groundState.aboveLyingObectIds.add(activeObject.id);
                gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, groundObject, groundState));
                gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, activeObject, activeState));
            } else {
                //TODO if the programm is here something has gone wrong
            }
        }
    }

    public static void removeLieOnRelation(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance){
        if (objectInstance.state.liesOnId != -1){
            ObjectInstance LieOnObject = gameInstance.getObjectInstanceById(objectInstance.state.liesOnId);
            ObjectState state = LieOnObject.state.copy();
            int idx = state.aboveLyingObectIds.indexOf(objectInstance.id);
            if (idx != -1){
                state.aboveLyingObectIds.remove(idx);
            }
            ObjectState aboveState = objectInstance.state.copy();
            aboveState.liesOnId = -1;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance, aboveState));
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, LieOnObject, state));
        }
    }

    public static void getAllAboveLyingObjects(GameInstance gameInstance, Player player, ObjectInstance objectInstance, IntegerArrayList ial){
        for (int id : objectInstance.state.aboveLyingObectIds){
            ial.add(id);
            getAllAboveLyingObjects(gameInstance, player, gameInstance.getObjectInstanceById(id), ial);
        }
    }

    public static boolean isOnObject(int xPos, int yPos, ObjectInstance oi, int playerId, int maxInaccuracy) {
    	int oiw = oi.getWidth(playerId), oih = oi.getHeight(playerId);
        int xCenter = oi.state.posX;
        int yCenter = oi.state.posY;
        int xDiff = xPos - xCenter, yDiff = yPos - yCenter;

        double radians = oi.state.rotation*2*Math.PI/360;
        double sin = Math.sin(radians), cos = Math.cos(radians);
        double transformedX = -xDiff * cos + yDiff * sin + xCenter;
        double transformedY = -xDiff * sin - yDiff * cos + yCenter;
        boolean leftIn 	= (transformedX > (oi.state.posX - maxInaccuracy - oiw/2));
        boolean rightIn = (transformedX < (oi.state.posX + maxInaccuracy + oiw/2));
        boolean topIn 	= (transformedY < (oi.state.posY + maxInaccuracy + oih/2));
        boolean bottomIn= (transformedY > (oi.state.posY - maxInaccuracy - oih/2));
        return leftIn && rightIn && topIn && bottomIn;
    }

    //Get element nearest to xPos, yPos with some inaccuracy
    public static ObjectInstance getNearestObjectByPosition(GamePanel gamePanel, GameInstance gameInstance, Player player, int xPos, int yPos, double zooming, int maxInaccuracy, IntegerArrayList ignoredObjects) {
        ObjectInstance activeObject = null;
        if (isInPrivateArea(gamePanel.privateArea, xPos, yPos)) {
            Point2D transformedPoint = new Point2D.Double(xPos, yPos);
            gamePanel.privateArea.transformPoint(transformedPoint, transformedPoint);
        	int id = gamePanel.privateArea.getObjectIdByPosition((int) transformedPoint.getX(), (int) transformedPoint.getY());
            activeObject = gameInstance.getObjectInstanceById(id);
        }
        else {
            for (int idx = 0; idx < gameInstance.getObjectInstanceCount();++idx) {
                ObjectInstance oi = gameInstance.getObjectInstanceByIndex(idx);
                if (CheckFunctions.isValidNearestObject(oi, player, ignoredObjects)) {
                    if (isOnObject(xPos, yPos, oi, player.id, maxInaccuracy) && (activeObject == null || (oi.state.drawValue > activeObject.state.drawValue))) {
                        activeObject = oi;
                    }
                }
            }
        }
        return activeObject;
    }

    public static ObjectInstance getNearestObjectByPosition(GamePanel gamePanel, GameInstance gameInstance, Player player, int xPos, int yPos, double zooming, IntegerArrayList ignoredObjects) {
        ObjectInstance currentObject = getNearestObjectByPosition(gamePanel, gameInstance, player, xPos, yPos, zooming, 0, ignoredObjects);
        if (haveSamePositions(getStackTop(gameInstance, currentObject), getStackBottom(gameInstance, currentObject)) && !currentObject.state.inPrivateArea) {
            return getStackTop(gameInstance, currentObject);
        } else {
            return currentObject;
        }
    }

    public static void displayStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, int posX, int posY, int cardMargin) {
        if (objectInstance != null && objectInstance.go instanceof GameObjectToken) {
            ObjectInstance stackTopInstance = getStackTop(gameInstance,objectInstance);
            IntegerArrayList belowList = new IntegerArrayList();
            getStackToOneEnd(gameInstance, stackTopInstance, belowList,Relation.BELOW);
            if (belowList.size() > 1) {
                if (isStackCollected(gameInstance, stackTopInstance)) {
                    for (int i = 0; i < belowList.size(); i++) {
                        MoveFunctions.moveObjectTo(gamePanelId, gameInstance, player, gameInstance.getObjectInstanceById(belowList.get(i)), (int) (posX - (belowList.size() / 2.0 - i) * cardMargin), posY);
                        //removeFromStack(gamePanelId, gameInstance, player, gameInstance.getObjectInstanceById(belowList.get(i)));
                    }
                }
            }

        }
    }

    //show all objects below element objectInstance
    public static void displayStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, int cardMargin) {
        if (objectInstance != null && objectInstance.go instanceof GameObjectToken) {
            ObjectInstance stackTopInstance = getStackTop(gameInstance,objectInstance);
            IntegerArrayList belowList = new IntegerArrayList();
            getStackToOneEnd(gameInstance, stackTopInstance, belowList, Relation.BELOW);
            if (belowList.size() > 1) {
                int posX = stackTopInstance.state.posX;
                int posY = stackTopInstance.state.posY;

                if (isStackCollected(gameInstance, stackTopInstance)) {
                    for (int i = 0; i < belowList.size(); i++) {
                        MoveFunctions.moveObjectTo(gamePanelId, gameInstance, player, gameInstance.getObjectInstanceById(belowList.get(i)), (int) (posX - (belowList.size() / 2.0 - i) * cardMargin), posY);
                        //removeFromStack(gamePanelId, gameInstance, player, gameInstance.getObjectInstanceById(belowList.get(i)));
                    }
                }
            }

        }
    }



        //Move the whole stack to element object instance
    public static void collectStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        IntegerArrayList stack = new IntegerArrayList();
        getStackFromTop(gameInstance, objectInstance, stack);
        if (!stackIsCollected(gameInstance, player, stack) && objectInstance.go instanceof GameObjectToken) {
            for (int id : stack) {
                MoveFunctions.moveObjectTo(gamePanelId, gameInstance, player, gameInstance.getObjectInstanceById(id), objectInstance);
            }
        }
    }

    public static boolean stackIsCollected(GameInstance gameInstance, Player player, IntegerArrayList ial){
        ObjectInstance first = gameInstance.getObjectInstanceById(ial.getI(0));
        for (int i=1;i<ial.size();++i){
            if (!haveSamePositions(first, gameInstance.getObjectInstanceById(ial.get(i)))){
                return false;
            }
        }
        return true;
    }

    //check if objectInstance is at the bottom of a stack
    public static boolean isStackBottom(ObjectInstance objectInstance) {
        return objectInstance != null && objectInstance.go instanceof GameObjectToken && objectInstance.state.belowInstanceId == -1;
    }

    //Check if object Instance is at the top of a stack
    public static boolean isStackTop(ObjectInstance objectInstance) {
        return objectInstance != null && objectInstance.go instanceof GameObjectToken && objectInstance.state.aboveInstanceId == -1;
    }

    //check if two objects have the same position
    public static boolean haveSamePositions(ObjectInstance objectInstanceA, ObjectInstance objectInstanceB) {
        return objectInstanceA != null && objectInstanceB != null && objectInstanceA.go instanceof GameObjectToken && objectInstanceB.go instanceof GameObjectToken && objectInstanceA.state.posX == objectInstanceB.state.posX && objectInstanceA.state.posY == objectInstanceB.state.posY;
    }

    //check if two objects have the same position
    public static boolean isStackCollected(GameInstance gameInstance, ObjectInstance objectInstance) {
        return objectInstance != null && objectInstance.go instanceof GameObjectToken && haveSamePositions(getStackTop(gameInstance, objectInstance), getStackBottom(gameInstance, objectInstance));
    }

    //remove all relations in an object stack
    public static void removeStackRelations(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        if (objectInstance != null && objectInstance.go instanceof GameObjectToken) {
            IntegerArrayList stackList = new IntegerArrayList();
            getStackFromTop(gameInstance, objectInstance, stackList);
            for (int x : stackList) {
                ObjectState state = gameInstance.getObjectInstanceById(x).state.copy();
                state.aboveInstanceId = -1;
                state.belowInstanceId = -1;
                gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, gameInstance.getObjectInstanceById(x), state));
            }
        }
    }

    //take an object in the hand of player
    public static void takeObjects(int source, GameInstance gameInstance, Player player, Point2D point, double offset, ObjectInstance objectInstance, ObjectInstance hoveredObject) {
        if (objectInstance != null && objectInstance.go instanceof GameObjectToken) {
            IntegerArrayList stackIds = new IntegerArrayList();
            getStack(gameInstance, objectInstance, stackIds);
            removeStackRelations(source, gameInstance,player,objectInstance);
            for (int id : stackIds) {
                ObjectInstance currentInstance = gameInstance.getObjectInstanceById(id);
                if (player.id != currentInstance.state.owner_id && currentInstance.state.owner_id == -1) {
                    insertIntoOwnStack(source, gameInstance, player, point, offset, currentInstance, hoveredObject, 0, (int) (currentInstance.getWidth(player.id) * gameInstance.cardOverlap));
                }
            }
            //ObjectFunctions.displayStack(gamePanel, gameInstance, player, objectInstance, (int) (objectInstance.getWidth(player.id) * gamePanel.cardOverlap));
        }
    }

    //drop all objects from the hand of player
    public static void dropObjects(int source, GameInstance gameInstance, Player player, ObjectInstance hoveredObject) {
        ArrayList<ObjectInstance> oiList = new ArrayList<>();
        getOwnedStack(gameInstance, player, oiList);
        ObjectFunctions.removeFromOwnStack(source, gameInstance, player, oiList);
        ObjectFunctions.setNewDrawValue(source, gameInstance, player, oiList);
        //stack all dropped objects
        makeStack(source, gameInstance, player, oiList, hoveredObject, SIDE_UNCHANGED);
    }

    public static void dropObject(int source, GameInstance gameInstance, Player player, ObjectInstance objectInstance){
        if (objectInstance != null && objectInstance.go instanceof GameObjectToken) {
            removeFromOwnStack(source, gameInstance, player, objectInstance);
            ObjectFunctions.setNewDrawValue(source, gameInstance, player, objectInstance);
        }
    }

    public static void playObject(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance objectInstance, ObjectInstance hoveredObject){
        playObject(gamePanel, gameInstance, player, objectInstance, hoveredObject, true);
    }

    public static void playObject(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance objectInstance, ObjectInstance hoveredObject, boolean front){
        if (objectInstance != null && objectInstance.go instanceof GameObjectToken) {
            removeFromOwnStack(gamePanel.id, gameInstance, player, objectInstance);
            ObjectFunctions.setNewDrawValue(gamePanel.id, gameInstance, player, objectInstance);
        }
        double angle = PlayerFunctions.GetCurrentPlayerRotation(gameInstance, player);
        ObjectState objectState = objectInstance.state.copy();
        objectState.rotation = (int) angle;
        gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player, objectInstance, objectState));
        Point2D PlayerShift = new Point2D.Double(-Math.sin(Math.toRadians(angle))*gamePanel.table.getStackerWidth()/3, Math.cos(Math.toRadians(angle))*gamePanel.table.getStackerWidth()/3);
        Point2D tableCenter = AwtGeometry.addTo(PlayerShift, gamePanel.table.getTableCenter(new Point2D.Double()));
        MoveFunctions.moveObjectTo(gamePanel.id, gameInstance,player, objectInstance, (int) (tableCenter.getX()), (int) (tableCenter.getY()));
        flipTokenToSide(gamePanel.id, gameInstance, player, objectInstance, front);
        deselectObject(gamePanel.id, gameInstance, player, objectInstance.id, hoveredObject);
    }

    public static boolean IsObjectOnShape(GameInstance gameInstance, Shape shape, AffineTransform boardToScreenTransform, ObjectInstance objectInstance){
        Point2D.Double p = new Point2D.Double(objectInstance.state.posX, objectInstance.state.posY);
        boardToScreenTransform.transform(p, p);
        return shape != null && shape.contains(p);
    }

    public static void getObjectsOnShape(int gamePanelId, GameInstance gameInstance, Player player, Shape shape, AffineTransform boardToScreenTransform, ObjectInstance objectInstance, IntegerArrayList ial){
        ial.clear();
        if (shape == null){return;}
        getAllObjectsOfGroup(gamePanelId, gameInstance, player, objectInstance, ial, false);
        final Point2D p = new Point2D.Double();
        ial.removeIf(new Predicate<Integer>() {
            @Override
            public boolean test(Integer id) {
                ObjectState state = gameInstance.getObjectInstanceById(id).state;
                p.setLocation(state.posX, state.posY);
                boardToScreenTransform.transform(p, p);
                return !shape.contains(p);
            }
        });
    }

    public static void stackObjectsOnShapeAndMoveToPoint(int gamePanelId, GameInstance gameInstance, Player player, Shape shape, Point2D centerPoint, AffineTransform boardToScreenTransform, IntegerArrayList ial, ObjectInstance hoveredObject, int side){
        getObjectsOnShape(gamePanelId, gameInstance, player, shape, boardToScreenTransform, hoveredObject, ial);
        makeStack(gamePanelId, gameInstance, player, ial, hoveredObject, side);
        MoveFunctions.moveStackTo(gamePanelId, gameInstance, player, ial, (int) centerPoint.getX(), (int) centerPoint.getY());
    }

    public static ObjectInstance setActiveObjectByMouseAndKey(GamePanel gamePanel, GameInstance gameInstance, Player player, MouseEvent arg0, Vector2 mouse, int maxInaccuracy) {
        ObjectInstance activeObject = null;
        int pressedXPos = mouse.getXI();
        int pressedYPos = mouse.getYI();
        activeObject = getNearestObjectByPosition(gamePanel, gameInstance, player, pressedXPos, pressedYPos, 1, null);
        if (arg0.isShiftDown() && activeObject.go instanceof GameObjectToken) {
            activeObject = getStackBottom(gameInstance, activeObject);
        }
        if (activeObject != null && activeObject.go instanceof GameObjectToken && activeObject.state.owner_id != -1 && activeObject.state.owner_id != player.id) {
            return null;
        }
        return activeObject;
    }

    public static void mergeStacks(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance topStackInstance, ObjectInstance bottomStackInstance) {
        if (topStackInstance != null && topStackInstance.go instanceof GameObjectToken && bottomStackInstance != null && bottomStackInstance.go instanceof GameObjectToken) {
            MoveFunctions.moveStackTo(gamePanelId, gameInstance, player, topStackInstance, bottomStackInstance);
            ObjectInstance topElement = getStackTop(gameInstance, bottomStackInstance);
            ObjectInstance bottomElement = getStackBottom(gameInstance, topStackInstance);
            ObjectState topState = topElement.state.copy();
            ObjectState bottomState = bottomElement.state.copy();
            topState.aboveInstanceId = bottomElement.id;
            bottomState.belowInstanceId = topElement.id;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, topElement, topState));
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, bottomElement, bottomState));
        }
    }

    //TODO can only handle one stack
    public static void getOwnedStack(GameInstance gameInstance, Player player, IntegerArrayList idList, boolean ignoreActiveObject) {
        idList.clear();
        for (int idx = 0; idx < gameInstance.getObjectInstanceCount();++idx) {
            ObjectInstance oi = gameInstance.getObjectInstanceByIndex(idx);
            if (oi.state.owner_id == player.id && (!ignoreActiveObject || !oi.state.isActive)) {
                getStackFromBottom(gameInstance, oi, idList);
                return;
            }
        }
    }
    public static void getOwnedStack(GameInstance gameInstance, Player player, ArrayList<ObjectInstance> oiList, boolean ignoreActiveObject) {
        oiList.clear();
        for (int idx = 0; idx < gameInstance.getObjectInstanceCount();++idx) {
            ObjectInstance oi = gameInstance.getObjectInstanceByIndex(idx);
            if (oi.state.owner_id == player.id && (!ignoreActiveObject || !oi.state.isActive)) {
                getStackFromBottom(gameInstance, oi, oiList);
                return;
            }
        }
    }

    public static void getOwnedStack(GameInstance gameInstance, Player player, IntegerArrayList idList) {
        getOwnedStack(gameInstance,player,idList,true);
    }

    public static void getOwnedStack(GameInstance gameInstance, Player player, ArrayList<ObjectInstance> oiList) {
        getOwnedStack(gameInstance,player,oiList,true);
    }

    public static void deselectObject(int gamePanelId, GameInstance gameInstance, Player player, int objectId, ObjectInstance hoveredObject){
        ObjectInstance oi = gameInstance.getObjectInstanceById(objectId);
        if(oi != null) {
            ObjectState state = oi.state.copy();
            if (hoveredObject != null && hoveredObject == oi)
            {
                hoveredObject = null;
            }
            if(player == null || ObjectFunctions.objectIsSelectedByPlayer(gameInstance, player, objectId))
            {
                state.isActive = false;
                state.isSelected = -1;
                if (oi.go instanceof GameObjectDice)
                {
                    GameObjectDice.DiceState diceState = (GameObjectDice.DiceState) state;
                    diceState.unfold = false;
                }
                gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, oi, state));
            }
        }
    }

    public static void deselectObjects(int gamePanelId, GameInstance gameInstance, Player player, IntegerArrayList idList, ObjectInstance hoveredObject){
        for (int id: idList){
            deselectObject(gamePanelId, gameInstance, player, id, hoveredObject);
        }
    }

    public static void deselectAllSelected(int gamePanelId, GameInstance gameInstance, Player player, IntegerArrayList ial, ObjectInstance hoveredObject)
    {
        ial.clear();
        getSelectedObjects(gameInstance, player, ial);
        deselectObjects(gamePanelId, gameInstance, player, ial, hoveredObject);
    }


    public static boolean isObjectHovered(ObjectInstance objectInstance, ObjectInstance hoveredObject)
    {
        return hoveredObject != null && hoveredObject.id == objectInstance.id;
    }

    public static void selectObject(int gamePanelId, GameInstance gameInstance, Player player, int objectId){
        ObjectInstance oi = gameInstance.getObjectInstanceById(objectId);
        if (getObjectSelector(gameInstance, oi) == -1)
        {
            ObjectState state = oi.state.copy();
            state.isActive = true;
            state.isSelected = player.id;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, oi, state));
        }

    }

    public static void selectStack(int gamePanelId, GameInstance gameInstance, Player player, int objectId, ObjectInstance hoveredObject){
        IntegerArrayList ial = new IntegerArrayList();
        getStackFromTop(gameInstance, gameInstance.getObjectInstanceById(objectId), ial);
        deselectObjects(gamePanelId, gameInstance, player, ial, hoveredObject);
        selectObject(gamePanelId, gameInstance, player, ial.getI(0));
    }

    public static void selectObjects(int gamePanelId, GameInstance gameInstance, Player player, IntegerArrayList objectInstances){
        objectInstances.sort(gameInstance.idObjectInstanceDrawValueComparator);
        for (int id: objectInstances)
        {
            ObjectInstance oi = gameInstance.getObjectInstanceById(id);
            if (oi.go instanceof GameObjectToken){
                if(isStackTop(oi)){
                    selectObject(gamePanelId, gameInstance, player, id);
                }
            }
            else {
                selectObject(gamePanelId, gameInstance, player, id);
            }
        }
    }

    public static void deactivateObject(GameInstance gameInstance, int oId){
        gameInstance.getObjectInstanceById(oId).state.isActive = false;
    }

    public static void deactivateObjects(GameInstance gameInstance, IntegerArrayList objectInstances){
        for (int id: objectInstances)
        {
            deactivateObject(gameInstance, id);
        }
    }

    public static void deactivateAllObjects(GameInstance gameInstance){
        for (int i = 0; i < gameInstance.getObjectInstanceCount(); ++i)
        {
            deactivateObject(gameInstance, gameInstance.getObjectInstanceByIndex(i).id);
        }
    }

    public static boolean objectIsSelectedByPlayer(GameInstance gameInstance, Player player, int objectId){
        return gameInstance.getObjectInstanceById(objectId).state.isSelected == player.id;
    }

    public static boolean objectIsSelectedByPlayer(GameInstance gameInstance, Player player, ObjectInstance oi){
        return oi.state.isSelected == player.id;
    }

    private static boolean objectIsSelectedByOtherPlayer(GameInstance gameInstance, Player player, ObjectInstance oi) {
        return !objectIsSelectedByPlayer(gameInstance,player,oi) && objectIsSelected(gameInstance,oi);
    }

    public static int getObjectSelector(GameInstance gameInstance, ObjectInstance oi){
        return oi.state.isSelected;
    }

    public static void getObjectSelector(GameInstance gameInstance, ObjectInstance objectId, Player player){//This method doesn't make any sense
        player = gameInstance.getPlayerById(getObjectSelector(gameInstance, objectId));
    }

    public static boolean objectIsSelected(GameInstance gameInstance, ObjectInstance oi){
        return getObjectSelector(gameInstance, oi) != -1;
    }

    public static void getSelectedObjects(GameInstance gameInstance, Player player, IntegerArrayList ial)
    {
        ial.clear();
        for(ObjectInstance oi : gameInstance.getObjectInstanceList())
        {
            if (ObjectFunctions.objectIsSelectedByPlayer(gameInstance, player, oi))
            {
                ial.add(oi.id);
            }
        }
    }

    public static void getSelectedObjects(GameInstance gameInstance, IntegerArrayList ial)
    {
        ial.clear();
        for(ObjectInstance oi : gameInstance.getObjectInstanceList())
        {
            if (ObjectFunctions.objectIsSelected(gameInstance, oi))
            {
                ial.add(oi.id);
            }
        }
    }

    static public void sortObjectsByDrawValue(GameInstance gameInstance, Player player, IntegerArrayList ial){
        ial.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return gameInstance.getObjectInstanceById(o1).state.drawValue - gameInstance.getObjectInstanceById(o2).state.drawValue;
            }
        });
    }


        static public void sortSelectedObjectsByDrawValue(GameInstance gameInstance, Player player, IntegerArrayList ial, IntegerArrayList objOrigPosX, IntegerArrayList objOrigPosY){
        IntegerArrayList selectedObjects = new IntegerArrayList();
        ObjectFunctions.getSelectedObjects(gameInstance, player, selectedObjects);
        IntegerArrayList sortedObjPosX = new IntegerArrayList();
        IntegerArrayList sortedObjPosY = new IntegerArrayList();
        if (objOrigPosX.size() > 0) {
            for (int i = 0; i < selectedObjects.size();++i) {
                sortedObjPosX.add(0);
                sortedObjPosY.add(0);
            }
        }
        ial.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return gameInstance.getObjectInstanceById(o2).state.drawValue - gameInstance.getObjectInstanceById(o1).state.drawValue;
            }
        });
        if (objOrigPosX.size() > 0) {
            for (int i = 0; i < ial.size(); ++i) {
                int idx = selectedObjects.indexOf(ial.getI(i));
                sortedObjPosX.set(i, objOrigPosX.getI(idx));
                sortedObjPosY.set(i, objOrigPosY.getI(idx));
            }
        }
        if (sortedObjPosX.size() > 0) {
            objOrigPosX.set(sortedObjPosX);
            objOrigPosY.set(sortedObjPosY);
        }
    }

    public static void releaseObjects(MouseEvent arg0, GamePanel gamePanel, GameInstance gameInstance, Player player, Point2D point, double offset, ObjectInstance activeObject, ObjectInstance hoveredObject, int posX, int posY, double zooming, int maxInaccuracy) {
        if (activeObject != null) {
            // Game Token Objects
            if (activeObject.go instanceof GameObjectToken) {
                // Release Object over private Area
                if (gamePanel.privateArea != null && activeObject.state.owner_id != player.id && gamePanel.privateArea.containsScreenCoordinates(posX, posY)) {
                    IntegerArrayList stackIds = new IntegerArrayList();
                    ObjectFunctions.getStack(gameInstance, activeObject, stackIds);
                    removeStackRelations(gamePanel.id, gameInstance, player, activeObject);
                    for (int id : stackIds) {
                        ObjectInstance currentObject = gameInstance.getObjectInstanceById(id);
                        int index = gamePanel.privateArea.getInsertPosition(posX, posY);
                        if (SwingUtilities.isRightMouseButton(arg0)){
                            flipTokenObject(gamePanel.id,gameInstance,player,currentObject);
                        }
                        insertIntoOwnStack(gamePanel.id, gameInstance, player, point, offset, currentObject, hoveredObject, index, 0); //(int) (activeObject.getWidth(player.id)*gamePanel.cardOverlap)
                    }
                    stackIds.clear();
                    getAllAboveLyingObjects(gameInstance, player, activeObject, stackIds);
                    for (int id : stackIds){
                        ObjectInstance currentObject = gameInstance.getObjectInstanceById(id);
                        removeLieOnRelation(gamePanel.id, gameInstance, player, currentObject);
                        if (currentObject.go instanceof GameObjectToken) {
                            insertIntoOwnStack(gamePanel.id, gameInstance, player, point, offset, currentObject, hoveredObject, 0, 0);
                        }
                    }
                }
                //Release Object over private Area which was owned before
                else if (gamePanel.privateArea != null && activeObject.state.owner_id == player.id && activeObject.state.isActive && gamePanel.privateArea.containsScreenCoordinates(posX, posY)) {
                    removeFromOwnStack(gamePanel.id, gameInstance, player, activeObject);
                    int index = gamePanel.privateArea.getInsertPosition(posX, posY);
                    insertIntoOwnStack(gamePanel.id, gameInstance, player, point, offset, activeObject, hoveredObject, index, 0); //(int) (activeObject.getWidth(player.id)*gamePanel.cardOverlap)
                }
                //Release previously owned card
                else if (activeObject.state.owner_id == player.id && !gamePanel.privateArea.containsScreenCoordinates(posX, posY)) {
                    removeFromOwnStack(gamePanel.id, gameInstance, player, activeObject);
                    if (SwingUtilities.isLeftMouseButton(arg0)) {
                        ObjectFunctions.flipTokenObject(gamePanel.id, gameInstance, player, activeObject);
                        if(gamePanel.table.stackerShape.contains(posX, posY)){
                            playObject(gamePanel, gameInstance, player, activeObject, hoveredObject);
                        }
                    }
                }
                else {
                    IntegerArrayList activeOIds = new IntegerArrayList();
                    getStack(gameInstance, activeObject, activeOIds);
                    ObjectInstance nearestObject = getNearestObjectByPosition(gamePanel, gameInstance, player, activeObject.state.posX, activeObject.state.posY, zooming, activeOIds);
                    if (nearestObject != null && nearestObject.state.owner_id == -1) {
                        //if (isStackCollected(gameInstance, nearestObject)) {
                            IntegerArrayList oiIds = new IntegerArrayList();
                            getStack(gameInstance, nearestObject, oiIds);
                            //If release token on token stack then stack all together

                            if (nearestObject != activeObject && (activeOIds.size() > 1 || oiIds.size() > 1) && sameGroups(nearestObject, activeObject)) {
                                ObjectFunctions.mergeStacks(gamePanel.id, gameInstance, player, activeObject, nearestObject);
                                activeOIds.clear();
                                getStack(gameInstance, nearestObject, activeOIds);
                            }
                            // Else define lie on definition
                            else{
                                //Lie object on other object
                                ObjectInstance groundObject = getGroundObject(gameInstance, player, activeObject, player.id, maxInaccuracy);
                                if (groundObject != null){
                                    lieObjectOnObject(gamePanel.id, gameInstance, player, groundObject, activeObject);
                                }
                                else{
                                    removeLieOnRelation(gamePanel.id, gameInstance, player, activeObject);
                                }
                            }
//                        }
//                        else{
//                            activeOIds.clear();
//                            getStack(gameInstance, activeObject, activeOIds);
//                            Pair<ObjectInstance, ObjectInstance> insertObjects = getInsertObjects(gamePanel, gameInstance, player, activeObject.state.posX, activeObject.state.posY, zooming, activeOIds);
//                            insertIntoStack(gamePanel, gameInstance, player, activeObject, insertObjects.getKey(), insertObjects.getValue(), (int) (activeObject.getWidth(player.id) * gamePanel.cardOverlap));
//                        }
                    }
                    else {
                        removeLieOnRelation(gamePanel.id, gameInstance, player, activeObject);
                    }
                }

            }
            // Non Token Objects
            else{
                //Lie object on other object
                ObjectInstance groundObject = getGroundObject(gameInstance, player, activeObject, player.id, maxInaccuracy);
                if (groundObject != null){
                    lieObjectOnObject(gamePanel.id, gameInstance, player, groundObject, activeObject);
                }
                else{
                    removeLieOnRelation(gamePanel.id, gameInstance, player, activeObject);
                }
            }
            ObjectState state = activeObject.state.copy();
            state.isActive = false;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player, activeObject, state));
            gamePanel.privateArea.currentDragPosition = -1;
        }
    }

    private static boolean sameGroups(ObjectInstance nearestObject, ObjectInstance activeObject) {
        for (String group : nearestObject.go.groups){
            if (ArrayUtil.linearSearchEqual(activeObject.go.groups, group) >= 0){
                return true;
            }
        }
        return false;
    }

    private static boolean haveSameSize(Player player, ObjectInstance nearestObject, ObjectInstance activeObject) {
        return nearestObject.getWidth(player.id) == activeObject.getWidth(player.id) && nearestObject.getHeight(player.id) == activeObject.getHeight(player.id);
    }

    public static void moveOwnStackToBoardPosition(int gamePanelId, GameInstance gameInstance, Player player, Point2D point, double offset, IntegerArrayList ial){
        //move own stack to private bottom
        if (player != null) {
            ObjectFunctions.getOwnedStack(gameInstance,player,ial);
            if (ial.size() >0) {
                ObjectInstance oi = gameInstance.getObjectInstanceById(ial.getI(0));
                if (oi.state.owner_id == player.id) {
                    double angle = PlayerFunctions.GetCurrentPlayerRotation(gameInstance, player);
                    ObjectState objectState = oi.state.copy();
                    objectState.rotation = (int) angle;
                    gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, oi, objectState));

                    Point2D PlayerShift = new Point2D.Double(-Math.sin(Math.toRadians(angle))*offset, Math.cos(Math.toRadians(angle))*offset);
                    collectStack(gamePanelId, gameInstance, player, oi);
                    Point2D ownStackPosition = AwtGeometry.addTo(PlayerShift, point);
                    MoveFunctions.moveStackTo(gamePanelId, gameInstance, player, ial, (int) (ownStackPosition.getX()), (int) (ownStackPosition.getY()));
                    rotateStack(gameInstance, ial, (int) angle);
                }
            }
        }
    }

    public static void releaseObjects(MouseEvent arg0, GamePanel gamePanel, GameInstance gameInstance, Player player, Point2D point, double offset, ObjectInstance activeObject, ObjectInstance hoveredObject, int posX, int posY, double zooming) {
        releaseObjects(arg0, gamePanel, gameInstance, player, point, offset, activeObject, hoveredObject, posX, posY, zooming, 0);
    }

    public static ObjectInstance findNeighbouredStackTop(GameInstance gameInstance, Player player, ObjectInstance activeObject, int maxInaccuracy) {
        for (int i = 0; i < gameInstance.getObjectInstanceCount(); ++i) {
            ObjectInstance oi = gameInstance.getObjectInstanceByIndex(i);
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

    public static void makeStack(int gamePanelId, GameInstance gameInstance, Player player, ArrayList<ObjectInstance> oiList, ObjectInstance hoveredObject, int side) {
        if (oiList.size() > 1) {
            for (int i = 0; i < oiList.size(); ++i) {
                ObjectInstance currentObject = oiList.get(i);
                if (currentObject.go instanceof GameObjectToken && currentObject.state.owner_id == -1 && !ObjectFunctions.objectIsSelectedByOtherPlayer(gameInstance, player, currentObject)) {
                    deselectObject(gamePanelId, gameInstance, player, currentObject.id, hoveredObject);
                    removeStackRelations(gamePanelId, gameInstance, player, currentObject);
                    if (side != SIDE_UNCHANGED){flipTokenToSide(gamePanelId, gameInstance, player, currentObject, side == SIDE_TO_FRONT);}
                    ObjectState state = currentObject.state.copy();
                    state.rotation = currentObject.state.originalRotation;
                    if (i == 0 && oiList.size() > 1) {
                        state.belowInstanceId = -1;
                        state.aboveInstanceId = oiList.get(i + 1).id;
                        gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, currentObject, state));
                    } else if (i == oiList.size() - 1) {
                        state.aboveInstanceId = -1;
                        state.belowInstanceId = oiList.get(i - 1).id;
                        gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, currentObject, state));
                        MoveFunctions.moveObjectTo(gamePanelId, gameInstance, player, currentObject, oiList.get(i - 1));
                    } else {
                        state.aboveInstanceId = oiList.get(i + 1).id;
                        state.belowInstanceId = oiList.get(i - 1).id;
                        gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, currentObject, state));
                        MoveFunctions.moveObjectTo(gamePanelId, gameInstance, player, currentObject, gameInstance.getObjectInstanceById(oiList.get(i - 1).id));
                    }
                }
            }
        }
    }


    public static void makeStack(int gamePanelId, GameInstance gameInstance, Player player, IntegerArrayList stackElements, ObjectInstance hoveredObject, int side) {
        if (stackElements.size() > 1) {
            for (int i = 0; i < stackElements.size(); ++i) {
                ObjectInstance currentObject = gameInstance.getObjectInstanceById(stackElements.getI(i));
                if (currentObject.go instanceof GameObjectToken && currentObject.state.owner_id == -1 && !ObjectFunctions.objectIsSelectedByOtherPlayer(gameInstance, player, currentObject)) {
                    deselectObject(gamePanelId, gameInstance, player, currentObject.id, hoveredObject);
                    removeStackRelations(gamePanelId, gameInstance, player, currentObject);
                    if (side != SIDE_UNCHANGED){flipTokenToSide(gamePanelId, gameInstance, player, currentObject, side == SIDE_TO_FRONT);}
                    ObjectState state = currentObject.state.copy();
                    state.rotation = currentObject.state.originalRotation;
                    if (i == 0 && stackElements.size() > 1) {
                        state.belowInstanceId = -1;
                        state.aboveInstanceId = stackElements.getI(i + 1);
                        gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, currentObject, state));
                    } else if (i == stackElements.size() - 1) {
                        state.aboveInstanceId = -1;
                        state.belowInstanceId = stackElements.getI(i - 1);
                        gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, currentObject, state));
                        MoveFunctions.moveObjectTo(gamePanelId, gameInstance, player, currentObject, gameInstance.getObjectInstanceById(stackElements.getI(i - 1)));
                    } else {
                        state.aboveInstanceId = stackElements.getI(i + 1);
                        state.belowInstanceId = stackElements.getI(i - 1);
                        gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, currentObject, state));
                        MoveFunctions.moveObjectTo(gamePanelId, gameInstance, player, currentObject, gameInstance.getObjectInstanceById(stackElements.getI(i - 1)));
                    }
                }
            }
        }
    }


    //TODO here are some errors
    public static Pair<ObjectInstance, ObjectInstance> getInsertObjects(GamePanel gamePanel, GameInstance gameInstance, Player player, int posX, int posY, double zooming, IntegerArrayList ignoredObjects) {
        ObjectInstance nearestObject = getNearestObjectByPosition(gamePanel, gameInstance, player, posX, posY, zooming, ignoredObjects);
        if (hasAboveObject(nearestObject) && hasBelowObject(nearestObject)) {
            if (getObjectDistanceTo(getAboveObject(gameInstance, nearestObject), posX, posY) < getObjectDistanceTo(getBelowObject(gameInstance, nearestObject), posX, posY)) {
                return new Pair<>(getAboveObject(gameInstance, nearestObject), nearestObject);
            } else {
                return new Pair<>(nearestObject, getBelowObject(gameInstance, nearestObject));
            }
        } else if (hasAboveObject(nearestObject)) {
            if (posX > nearestObject.state.posX + nearestObject.getWidth(player.id) * gameInstance.cardOverlap) {
                return new Pair<>(nearestObject, null);
            }
            return new Pair<>(getAboveObject(gameInstance, nearestObject), nearestObject);
        } else if (hasBelowObject(nearestObject)) {
            if (posX < nearestObject.state.posX + nearestObject.getWidth(player.id) * gameInstance.cardOverlap) {
                return new Pair<>(null, nearestObject);
            }
            return new Pair<>(nearestObject, getBelowObject(gameInstance, nearestObject));
        } else
            return new Pair<>(null, null);
    }

    public static int getObjectDistanceTo(ObjectInstance objectInstance, int posX, int posY) {
        int diffX = posX - objectInstance.state.posX;
        int diffY = posY - objectInstance.state.posY;
        return diffX * diffX + diffY * diffY;
    }

    public static void insertIntoStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, ObjectInstance objectAbove, ObjectInstance objectBelow, int cardMargin) {
        if (objectInstance != null) {//TODO Florian check
            if (objectBelow != null) {
                ObjectState stateBelow = objectBelow.state.copy();
                ObjectState state = objectInstance.state.copy();
                stateBelow.aboveInstanceId = objectInstance.id;
                state.belowInstanceId = objectBelow.id;
                MoveFunctions.moveObjectTo(gamePanelId, gameInstance, player, objectInstance, objectBelow.state.posX - cardMargin, objectBelow.state.posY);
                if (objectAbove != null) {
                    ObjectState stateAbove = objectAbove.state.copy();
                    stateAbove.belowInstanceId = objectInstance.id;
                    state.aboveInstanceId = objectAbove.id;
                    gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectAbove, stateAbove));
                    gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance, state));
                    MoveFunctions.moveObjectTo(gamePanelId, gameInstance, player, objectInstance, objectAbove);
                    if (!isStackCollected(gameInstance, objectInstance)) {
                        MoveFunctions.moveAboveStackTo(gamePanelId, gameInstance, player, objectInstance, objectInstance.state.posX - cardMargin, objectInstance.state.posY, false);
                        //moveBelowStackTo(gamePanel, gameInstance, player, objectInstance, objectInstance.state.posX+cardMargin/2, objectInstance.state.posY, false);
                    }
                }
                gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance, state));
                gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectBelow, stateBelow));
            } else if (objectAbove != null) {
                ObjectState state = objectInstance.state.copy();
                ObjectState stateAbove = objectAbove.state.copy();
                stateAbove.belowInstanceId = objectInstance.id;
                state.aboveInstanceId = objectAbove.id;
                gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance, state));
                gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectAbove, stateAbove));
                MoveFunctions.moveObjectTo(gamePanelId, gameInstance, player, objectInstance, objectAbove);
                if (!isStackCollected(gameInstance, objectInstance)) {
                    MoveFunctions.moveAboveStackTo(gamePanelId, gameInstance, player, objectInstance, (int) (objectInstance.state.posX - objectInstance.getWidth(player.id) * gameInstance.cardOverlap), objectInstance.state.posY, false);
                }
            }
        }
    }

    public static void insertIntoStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, IntegerArrayList stackIds, int insertId, int cardMargin) {
        ObjectInstance aboveInstance = null;
        ObjectInstance belowInstance = null;
        insertId = Math.max(insertId, 0);

        if (insertId < stackIds.size()) {
            aboveInstance = gameInstance.getObjectInstanceById(stackIds.getI(insertId));
        } else {
            insertId = stackIds.size();
        }

        if (insertId > 0 && stackIds.size() > 0) {
            belowInstance = gameInstance.getObjectInstanceById(stackIds.getI(insertId - 1));
        }
        insertIntoStack(gamePanelId, gameInstance, player, objectInstance, aboveInstance, belowInstance, cardMargin);
    }

    public static void insertIntoOwnStack(int id, GameInstance gameInstance, Player player, Point2D point, double offset, ObjectInstance objectInstance, ObjectInstance hoveredObject, int insertId, int cardMargin) {
        IntegerArrayList idList = new IntegerArrayList();
        getOwnedStack(gameInstance, player, idList);
        ObjectState state = objectInstance.state.copy();
        state.owner_id = player.id;
        state.inPrivateArea = true;
        state.isActive = false;
        gameInstance.update(new GameObjectInstanceEditAction(id, player, objectInstance, state));
        insertIntoStack(id, gameInstance, player, objectInstance, idList, insertId, cardMargin);
        moveOwnStackToBoardPosition(id, gameInstance, player, point, offset, idList);
        deselectObject(id, gameInstance, player, objectInstance.id, hoveredObject);
    }

    public static void removeFromOwnStack(int source, GameInstance gameInstance, Player player, ArrayList<ObjectInstance> oiList) {
        for (ObjectInstance oi : oiList){
            removeFromOwnStack(source, gameInstance, player, oi);
        }
    }

    public static void removeFromOwnStack(int source, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        removeObject(source, gameInstance, player, objectInstance);
        ObjectState objectState = objectInstance.state.copy();
        objectState.owner_id = -1;
        objectState.inPrivateArea = false;
        gameInstance.update(new GameObjectInstanceEditAction(source, player, objectInstance, objectState));
    }

    public static void getObjectOwner(GameInstance gameInstance, ObjectInstance objectInstance, Player player){
        player = gameInstance.getPlayerById(objectInstance.state.owner_id);
    }

    public static void getAllObjectsOfGroup(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, IntegerArrayList ial, boolean allCards) {
        ial.clear();
        ial.add(objectInstance.id);
        String objectGroup = objectInstance.go.objectType;
        if (objectInstance.go.groups.length > 0) {
            objectGroup = objectInstance.go.groups[0];
        }
        for (int idx = 0; idx < gameInstance.getObjectInstanceCount();++idx) {
            ObjectInstance oi = gameInstance.getObjectInstanceByIndex(idx);
            String oiGroup = oi.go.objectType;
            if (oi.go.groups.length > 0) {
                oiGroup = oi.go.groups[0];
            }
            if (oiGroup.equals(objectGroup) && oi.id != objectInstance.id) {
                if (allCards) {
                    deselectObject(gamePanelId, gameInstance, null, oi.id, null);
                    if (oi.state.owner_id != -1){
                        ObjectState objectState = oi.state.copy();
                        objectState.owner_select_reset();
                        gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, oi, objectState));
                    }
                    ial.add(oi.id);
                }
                else
                    if (oi.state.owner_id == -1 && (oi.state.isSelected == player.id || oi.state.isSelected == -1)) {
                        ial.add(oi.id);
                    }
            }
        }

    }

    public static void stackAllObjectsOfGroup(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, ObjectInstance hoveredObject, boolean withHandCards) {
        IntegerArrayList ial = new IntegerArrayList();
        getAllObjectsOfGroup(gamePanelId, gameInstance, player, objectInstance, ial, withHandCards);
        makeStack(gamePanelId, gameInstance, player, ial, hoveredObject, SIDE_TO_BACK);
    }

    public static IntegerArrayList getTopNObjects(GameInstance gameInstance, ObjectInstance objectInstance, int number) {
        IntegerArrayList objectList = new IntegerArrayList();
        ObjectInstance currentObject = getStackTop(gameInstance, objectInstance);
        objectList.add(currentObject.id);
        for (int i = 0; i < number - 1; ++i) {
            int belowId = currentObject.state.belowInstanceId;
            if (belowId != -1) {
                objectList.add(belowId);
                currentObject = gameInstance.getObjectInstanceById(currentObject.state.belowInstanceId);
            } else {
                break;
            }
        }
        return objectList;
    }

    public static void splitStackAtN(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, int number) {
        IntegerArrayList topNObjects = ObjectFunctions.getTopNObjects(gameInstance, objectInstance, number);
        int splitObjectid = topNObjects.last();
        if (gameInstance.getObjectInstanceById(splitObjectid).state.belowInstanceId == -1) {
        	ObjectState state = gameInstance.getObjectInstanceById(splitObjectid).state.copy();
            gameInstance.getObjectInstanceById(splitObjectid).state.belowInstanceId = -1;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player.id, splitObjectid, state));
        } else {
        	ObjectInstance splitObject = gameInstance.getObjectInstanceById(splitObjectid);
            ObjectState splitState = splitObject.state.copy();
        	ObjectInstance belowObject = gameInstance.getObjectInstanceById(splitState.belowInstanceId);
            ObjectState belowState = belowObject.state.copy();
        	belowState.aboveInstanceId = -1;
            splitState.belowInstanceId = -1;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, belowObject, belowState));
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, splitObject, splitState));
        }
    }


    public static boolean isObjectInHand(Player player, ObjectInstance objectInstance) {
        return objectInstance.state.owner_id == player.id;
    }


    public static int getStackOwner(GameInstance gameInstance, IntegerArrayList stackIds) {
        /*
        int ownerId = -1;
        for(int id: stackIds)  //TODO: I don't think that this method is doing what it should do
        {
            if(ownerId == -1)
            {
                ownerId = gameInstance.getObjectInstanceById(id).state.owner_id;
            }
            else if(gameInstance.getObjectInstanceById(id).state.owner_id != ownerId)
                return -1;
        }*/
        return gameInstance.getObjectInstanceById(stackIds.getI(0)).state.owner_id;
    }

    public static boolean isStackOwned(GameInstance gameInstance, IntegerArrayList stackIds) {
        for (int id : stackIds) {
            if (gameInstance.getObjectInstanceById(id).state.owner_id != -1) {
                return true;
            }
        }
        return false;
    }

    public static boolean isStackInHand(GameInstance gameInstance, Player player, IntegerArrayList stackIds) {
        for (int id : stackIds) {
            if (!isObjectInHand(player, gameInstance.getObjectInstanceById(id))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isStackNotInHand(GameInstance gameInstance, Player player, IntegerArrayList stackIds) {//TODO method name is not really fitting to method(equivalent to isStackOwned)
        for (int id : stackIds) {
            if (gameInstance.getObjectInstanceById(id).state.owner_id != -1) {
                return false;
            }
        }
        return true;
    }

    public static void zoomObjects(GameInstance gameInstance, double zooming) {
        for (int idx = 0; idx < gameInstance.getObjectInstanceCount();++idx) {
            ObjectInstance objectInstance = gameInstance.getObjectInstanceByIndex(idx);
            objectInstance.state.posX = (int) (objectInstance.state.posX * zooming);
            objectInstance.state.posY = (int) (objectInstance.state.posY * zooming);
        }
    }


    public static void getObjectsInsideBox(GameInstance gameInstance, Player player, int posX, int posY, int width, int height, IntegerArrayList idList, AffineTransform boardToScreenTransformation) {
        idList.clear();
        Point2D point = new Point2D.Double();
        int lowX = Math.min(posX, posX + width);
        int lowY = Math.min(posY, posY + height);
        int highX = Math.max(posX, posX + width);
        int highY = Math.max(posY, posY + height);
        for (ObjectInstance objectInstance : gameInstance.getObjectInstanceList()) {
            if (CheckFunctions.objectOnTableSelectable(objectInstance)) {
                point.setLocation(objectInstance.state.posX, objectInstance.state.posY);
                boardToScreenTransformation.transform(point, point);
                boolean leftIn = point.getX() > lowX;
                boolean rightIn = point.getX() < highX;
                boolean topIn = point.getY() < highY;
                boolean bottomIn = point.getY() > lowY;

                if (leftIn && rightIn && topIn && bottomIn && objectInstance.state.owner_id == -1 && !objectInstance.state.isFixed) {
                    idList.add(objectInstance.id);
                }
            }
        }
    }

    public static boolean isInPrivateArea(PrivateArea privateArea, int posX, int posY) {
        return privateArea != null && privateArea.containsBoardCoordinates(posX, posY);
    }


    public static boolean isStackInPrivateArea(PrivateArea privateArea, GameInstance gameInstance, IntegerArrayList stackIds) {
        if (stackIds.size() > 0) {
            for (int id : stackIds) {
                if (!privateArea.contains(id))
                    return false;
            }
            return true;
        }
        return false;
    }

    public static IntegerArrayList getObjectRepresentatives(GameInstance gameInstance, IntegerArrayList objectIds) {
        return getObjectRepresentatives(gameInstance, objectIds, false);
    }

    public static IntegerArrayList getObjectRepresentatives(GameInstance gameInstance, IntegerArrayList objectIds, boolean sorted) {//TODO sorted is not doing anything
        IntegerArrayList ial = new IntegerArrayList();
        for (int id : objectIds) {
            if (gameInstance.getObjectInstanceById(id).go instanceof GameObjectToken) {
                int topId = getStackTop(gameInstance, gameInstance.getObjectInstanceById(id)).id;
                ial.addUnique(topId);
            }
            else{
                ial.add(id);
            }
        }
        ial.sort(new Comparator<Integer>() {//TODO this is probably very slow
            @Override
            public int compare(Integer o1, Integer o2) {
                return countStack(gameInstance, gameInstance.getObjectInstanceById(o1)) - countStack(gameInstance, gameInstance.getObjectInstanceById(o2));
            }
        });
        return ial;
    }



    public static ArrayList<ObjectInstance> getObjectRepresentatives(GameInstance gameInstance, ArrayList<ObjectInstance> objectInstances) {
        IntegerArrayList ial = new IntegerArrayList();
        for (ObjectInstance oi : objectInstances) {
            if (oi.go instanceof GameObjectToken) {
                ial.add(oi.id);
            }
        }
        ArrayList<ObjectInstance> objectInstances1 = new ArrayList<>();
        addIdListToObjectList(gameInstance, ial, objectInstances1);
        return objectInstances1;
    }

    public static void getDrawOrder(GameInstance gameInstance, IntegerArrayList ial){
        ArrayList<ObjectInstance> drawValues = new ArrayList<>();
        addIdListToObjectList(gameInstance, ial, drawValues);
        drawValues.sort(objectInstanceDrawValueComparator);
        ial.clear();
        addObjectListToIdList(drawValues, ial);
    }

    public static void addIdListToObjectList(GameInstance gameInstance, IntegerArrayList ial, ArrayList<ObjectInstance> oiList){
        for (int id : ial){oiList.add(gameInstance.getObjectInstanceById(id));}
    }

    public static void addObjectListToIdList(ArrayList<ObjectInstance> oiList, IntegerArrayList ial){
        for (ObjectInstance oi : oiList){ial.add(oi.id);}
    }

    private static final Comparator<ObjectInstance> sortValueComparator = new Comparator<ObjectInstance>() {
        @Override
        public int compare(ObjectInstance o1, ObjectInstance o2) {
            return o2.state.sortValue - o1.state.sortValue;
        }
    };

    public static final Comparator<ObjectInstance> objectInstanceDrawValueComparator = new Comparator<ObjectInstance>() {
        @Override
        public int compare(ObjectInstance o1, ObjectInstance o2) {
            return o1.state.drawValue - o2.state.drawValue;
        }
    };

    public static void sortByValue(GameInstance gameInstance, ArrayList<ObjectInstance> oiList, boolean reverse) {
        oiList.sort(sortValueComparator);
        if (reverse) {Collections.reverse(oiList);}
    }

    public static void sortHandCardsByValue(GamePanel gamePanel, GameInstance gameInstance, Player player, Point2D point, double offset, IntegerArrayList ial, ArrayList<ObjectInstance> oiList, ObjectInstance hoveredObject, boolean reverse){
        getOwnedStack(gameInstance, player, ial);
        oiList.clear();
        addIdListToObjectList(gameInstance, ial, oiList);
        sortByValue(gameInstance, oiList, reverse);
        for (int i = 0; i < oiList.size(); ++i){
            removeFromOwnStack(gamePanel.id, gameInstance, player, oiList.get(i));
            insertIntoOwnStack(gamePanel.id, gameInstance, player, point, offset, oiList.get(i), hoveredObject, i, 0);
        }
    }

    public static void sortByDrawValue(GameInstance gameInstance, IntegerArrayList ial){
        ial.sort(gameInstance.idObjectInstanceDrawValueComparator);
    }

    public static void sortByDrawValue(GameInstance gameInstance, ArrayList<ObjectInstance> oiList){
        oiList.sort(objectInstanceDrawValueComparator);
    }

    public static void setNewDrawValue(int gamePanelId, GameInstance gameInstance, Player player, ArrayList<ObjectInstance> oiList){
        oiList.sort(objectInstanceDrawValueComparator);
        for (ObjectInstance oi : oiList){
            setNewDrawValue(gamePanelId, gameInstance, player, oi);
        }
    }

    public static void setNewDrawValue(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance){
        if (objectInstance != null) {
        	ObjectState state = objectInstance.state.copy();
            state.drawValue = gameInstance.getMaxDrawValue() + 1;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance, state));
            IntegerArrayList ial = new IntegerArrayList();
            getAllAboveLyingObjects(gameInstance, player, objectInstance, ial);
            for (int id : ial){
                ObjectInstance aboveObject = gameInstance.getObjectInstanceById(id);
                ObjectState aboveState = aboveObject.state.copy();
                aboveState.drawValue = gameInstance.getMaxDrawValue() + 1;
                gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, aboveObject, aboveState));
            }
        }
    }

    public static int getStackIdOfObject(GameInstance gameInstance, ObjectInstance objectInstance, IntegerArrayList ial){
        ObjectFunctions.getStack(gameInstance, objectInstance, ial);
        return ial.indexOf(objectInstance.id);
    }

    public static void rotateStack(GameInstance gameInstance, IntegerArrayList ial, int rotation){
        for (int id : ial){
            ObjectInstance oi = gameInstance.getObjectInstanceById(id);
            oi.state.rotation = rotation;
        }
    }

    public static void rotateStep(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, IntegerArrayList ial){
        ial.clear();
        getStack(gameInstance,objectInstance,ial);
        for (int id:ial){
            ObjectInstance oi = gameInstance.getObjectInstanceById(id);
            ObjectState state = oi.state.copy();
            if (oi.state.rotationStep != -1) {
                state.rotation = oi.getRotation() + oi.state.rotationStep;
            }
            else{
                state.rotation = oi.getRotation() + 360/gameInstance.getPlayerCount(false);
            }
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, oi, state));
        }
    }

    public static void fixObject(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance oi) {
    	ObjectState state = oi.state.copy();
        state.isFixed = true;
        gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, oi, state));
    }

    public static void giveObjects(GamePanel gamePanel, GameInstance gameInstance, Point2D point, double offset, IntegerArrayList ial, ArrayList<ObjectInstance> oiList, ObjectInstance hoveredObject) {
        Collections.shuffle(ial);
        int playerNum = gameInstance.getPlayerCount();
        int numElements = ial.size()/playerNum;

        int counter = 0;
        int playerCounter = 0;
        IntegerArrayList integerList = new IntegerArrayList();
        for(Player player : gameInstance.getPlayerList())
        {
            //Set trick num to zero
            player.trickNum = 0;
            gameInstance.update(new PlayerEditAction(gamePanel.id, player, player));
            for (int currentElementIndex = 0; currentElementIndex < numElements; ++currentElementIndex){
                int Pos = numElements*playerCounter + currentElementIndex;
                takeObjects(gamePanel.id,gameInstance,player, point, offset, gameInstance.getObjectInstanceById(ial.getI(Pos)), hoveredObject);
            }
            sortHandCardsByValue(gamePanel, gameInstance, player, point, offset, integerList, oiList, hoveredObject, false);
            integerList.clear();
            ++playerCounter;
        }
    }

    public static boolean isOnShape(int gamePanelId, GameInstance gameInstance, Shape stackerShape, AffineTransform boardToScreenTransform, int xi, int yi) {
        Point2D transformedPoint = new Point2D.Double(xi, yi);
        transformedPoint = boardToScreenTransform.transform(transformedPoint, transformedPoint);
        return stackerShape != null && stackerShape.contains(transformedPoint);
    }

    public static void takeTrick(int gamePanelId, GameInstance gameInstance, Player player, Shape shape, Point2D point, double offset, AffineTransform boardToScreenTransform, ObjectInstance objectInstance, IntegerArrayList ial, ObjectInstance hoveredObject) {
        stackObjectsOnShapeAndMoveToPoint(gamePanelId, gameInstance, player, shape, point, boardToScreenTransform, ial, hoveredObject, SIDE_TO_BACK);
        double angle = PlayerFunctions.GetCurrentPlayerRotation(gameInstance, player);
        //TODO change the trick angle according to the number of tricks
        double trickAngle = angle - (5*player.trickNum - 15);
        ObjectState objectState = objectInstance.state.copy();
        objectState.rotation = (int) trickAngle;
        gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance, objectState));
        Point2D PlayerShift = new Point2D.Double(-Math.sin(Math.toRadians(trickAngle))*offset, Math.cos(Math.toRadians(trickAngle))*offset);
        Point2D trickPoint = AwtGeometry.addTo(PlayerShift, point);
        MoveFunctions.moveStackTo(gamePanelId, gameInstance, player, ial, (int) (trickPoint.getX()), (int) (trickPoint.getY()));
        rotateStack(gameInstance, ial, (int) angle);
        ++player.trickNum;
    }

    /**
     * @param gamePanel
     * @param gameInstance
     * @param objectPosition
     */
    public static void getPrivateAreaHandCardPositionFromHoveredObject(GamePanel gamePanel, GameInstance gameInstance, Vector2d objectPosition) {
        objectPosition.set(0, 0);
        if (gamePanel.hoveredObject.state.inPrivateArea && gamePanel.hoveredObject.state.owner_id != -1) {
            AffineTransform affineTransform = new AffineTransform();
            Vector2d mouseBoardPos = new Vector2d();
            gamePanel.screenToBoardPos(gamePanel.mouseScreenX, gamePanel.mouseScreenY, mouseBoardPos);
            Point2D transformedPoint = new Point2D.Double(mouseBoardPos.getXI(), mouseBoardPos.getYI());
            gamePanel.privateArea.transformPoint(transformedPoint, transformedPoint);
            int index = gamePanel.privateArea.getPrivateObjectIndexByPosition((int) transformedPoint.getX(), (int) transformedPoint.getY());
            int oId = gamePanel.privateArea.getObjectIdByPosition((int) transformedPoint.getX(), (int) transformedPoint.getY());

            if (gamePanel.privateArea.privateObjectsPositions.size() > index && index != -1) {
                affineTransform.translate(gamePanel.getWidth() / 2, gamePanel.getHeight() - 2 * gamePanel.privateArea.objects.size());
                affineTransform.rotate(-Math.PI * 0.5 + Math.PI / (gamePanel.privateArea.objects.size() * 2));
                affineTransform.rotate(gamePanel.privateArea.objects.indexOf(oId) * Math.PI / (gamePanel.privateArea.objects.size()));
                affineTransform.scale(gamePanel.privateArea.zooming, gamePanel.privateArea.zooming);
                affineTransform.translate(0, -250);
                double x = affineTransform.getTranslateX();
                double y = affineTransform.getTranslateY();
                gamePanel.screenToBoardPos((int) x, (int) y, objectPosition);
            }

        }
    }

    public static void unpackBox(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        if (objectInstance != null && objectInstance.go instanceof GameObjectBox){
            ArrayList<Pair<String, ObjectInstance>> groupPositions = new ArrayList<>();
            for (ObjectInstance oi : gameInstance.getObjectInstanceList()){
                if (oi.state.inBox && oi.state.boxId == objectInstance.state.boxId && oi.id != objectInstance.id){
                    ObjectState objectState = oi.state.copy();
                    objectState.inBox = false;
                    objectState.posX = objectState.originalX;
                    objectState.posY = objectState.originalY;
                    objectState.rotation = objectState.originalRotation;
                    gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, oi, objectState));
                    if (oi.go instanceof GameObjectToken) {
                        if (oi.go.groups.length > 0) {
                            String objectGroup = oi.go.groups[0];
                            int Counter = 0;
                            while (true){
                                if (Counter < groupPositions.size()){
                                    ObjectInstance currentObject = groupPositions.get(Counter).getValue();
                                    String CurrentObjectGroup = currentObject.go.groups[0];
                                    if (CurrentObjectGroup.equals(objectGroup) && currentObject.state.posX == oi.state.posX && currentObject.state.posY == oi.state.posY){
                                        ObjectState oiState = oi.state.copy();
                                        ObjectState TopState = currentObject.state.copy();
                                        oiState.belowInstanceId = currentObject.id;
                                        TopState.aboveInstanceId = oi.id;
                                        gameInstance.update(new GameObjectInstanceEditAction(-1, null, oi, oiState));
                                        gameInstance.update(new GameObjectInstanceEditAction(-1, null, currentObject, TopState));
                                        flipTokenToSide(-1, gameInstance, null, oi, true);
                                        groupPositions.set(Counter, new Pair<>(objectGroup, oi));
                                        break;
                                    }
                                }
                                else{
                                    groupPositions.add(new Pair<>(objectGroup, oi));
                                    flipTokenToSide(-1, gameInstance, null, oi, true);
                                    break;
                                }
                                ++Counter;
                            }
                        }
                    }
                }
            }
        }
    }


    public static void packBox(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        if (objectInstance != null && objectInstance.go instanceof GameObjectBox){
            for (ObjectInstance oi : gameInstance.getObjectInstanceList()){
                if (oi.state.boxId == objectInstance.state.boxId && oi.id != objectInstance.id){
                    ObjectState objectState = oi.state.copy();
                    objectState.box_reset();
                    objectState.inBox = true;
                    gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, oi, objectState));
                }
            }
        }
    }

    public static void loadInitialState(GameInstance gameInstance) {
        ArrayList<Pair<String, ObjectInstance>> groupPositions = new ArrayList<>();
        for (ObjectInstance oi : gameInstance.getObjectInstanceList()) {
            if (oi.go instanceof GameObjectToken) {
                if (oi.go.groups.length > 0) {
                    String objectGroup = oi.go.groups[0];
                    int Counter = 0;
                    while (true){
                        if (Counter < groupPositions.size()){
                            ObjectInstance currentObject = groupPositions.get(Counter).getValue();
                            String CurrentObjectGroup = currentObject.go.groups[0];
                            if (CurrentObjectGroup.equals(objectGroup) && currentObject.state.posX == oi.state.posX && currentObject.state.posY == oi.state.posY){
                                ObjectState oiState = oi.state.copy();
                                ObjectState TopState = currentObject.state.copy();
                                oiState.belowInstanceId = currentObject.id;
                                TopState.aboveInstanceId = oi.id;
                                gameInstance.update(new GameObjectInstanceEditAction(-1, null, oi, oiState));
                                gameInstance.update(new GameObjectInstanceEditAction(-1, null, currentObject, TopState));
                                flipTokenToSide(-1, gameInstance, null, oi, true);
                                groupPositions.set(Counter, new Pair<>(objectGroup, oi));
                                break;
                            }
                        }
                        else{
                            groupPositions.add(new Pair<>(objectGroup, oi));
                            flipTokenToSide(-1, gameInstance, null, oi, true);
                            break;
                        }
                        ++Counter;
                    }
                }
            }
        }

    }

    public static void deselectAll(int id, GameInstance gameInstance) {
        for (ObjectInstance oi : gameInstance.getObjectInstanceList()){
            ObjectFunctions.deselectObject(id, gameInstance, null, oi.id, null);
        }
    }
}

