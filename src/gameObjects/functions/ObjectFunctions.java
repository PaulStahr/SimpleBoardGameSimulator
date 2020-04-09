package gameObjects.functions;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Collections;

import org.slf4j.Logger;

import gameObjects.GameObjectInstanceEditAction;
import gameObjects.definition.GameObject;
import gameObjects.definition.GameObjectToken;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import main.Player;
import util.Pair;
import util.data.IntegerArrayList;

public class ObjectFunctions {

    //Get the top of the stack with with element objectInstance
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

    //Get the bottom of the stack with with element objectInstance
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


    public static boolean isAboveObject(ObjectInstance objectInstance){
        if (objectInstance != null)
            return objectInstance.state.aboveInstanceId != -1;
        else
            return false;
    }

    public static int getAboveObjectId(ObjectInstance objectInstance)
    {
        if (isAboveObject(objectInstance))
        {
            return objectInstance.state.aboveInstanceId;
        }
        else
            return -1;
    }
    public static ObjectInstance getAboveObject(GameInstance gameInstance, ObjectInstance objectInstance)
    {
        if (isAboveObject(objectInstance))
        {
            return gameInstance.objects.get(objectInstance.state.aboveInstanceId);
        }
        else
            return null;
    }


    public static boolean isBelowObject(ObjectInstance objectInstance){
        if (objectInstance != null)
            return objectInstance.state.belowInstanceId != -1;
        else
            return false;
    }

    public static int getBelowObject(ObjectInstance objectInstance)
    {
        if (isBelowObject(objectInstance))
        {
            return objectInstance.state.belowInstanceId;
        }
        else
            return -1;
    }
    public static ObjectInstance getBelowObject(GameInstance gameInstance, ObjectInstance objectInstance)
    {
        if (isBelowObject(objectInstance))
        {
            return gameInstance.objects.get(objectInstance.state.belowInstanceId);
        }
        else
            return null;
    }

    //Get the id list of the elements above objectInstance in the stack starting with the bottom id with either objectInstance included or not, default is true
    public static IntegerArrayList getAboveStack(GameInstance gameInstance, ObjectInstance objectInstance, boolean included) {
        IntegerArrayList objectStack = new IntegerArrayList();
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
        return objectStack;
    }

    public static IntegerArrayList getAboveStack(GameInstance gameInstance, ObjectInstance objectInstance) {
        return getAboveStack(gameInstance, objectInstance, true);
    }

    //Get the id list of the elements below objectInstance in the stack starting with the top id with either objectInstance included or not, default is true
    public static IntegerArrayList getBelowStack(GameInstance gameInstance, ObjectInstance objectInstance, boolean included) {
        IntegerArrayList objectStack = new IntegerArrayList();
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
        return objectStack;
    }

    public static IntegerArrayList getBelowStack(GameInstance gameInstance, ObjectInstance objectInstance) {
        return getBelowStack(gameInstance, objectInstance, true);
    }


    /*Get the id list of the stack of element objectInstance starting with the top element*/
    public static IntegerArrayList getStackFromTop(GameInstance gameInstance, ObjectInstance objectInstance) {
        return getBelowStack(gameInstance, getStackTop(gameInstance, objectInstance));
    }

    //Get the id list of the stack of element objectInstance starting with the bottom element
    public static IntegerArrayList getStackFromBottom(GameInstance gameInstance, ObjectInstance objectInstance) {
        return getAboveStack(gameInstance, getStackBottom(gameInstance, objectInstance));
    }

    //The same as getStackFromBottom
    public static IntegerArrayList getStack(GameInstance gameInstance, ObjectInstance objectInstance) {
        return getAboveStack(gameInstance, getStackBottom(gameInstance, objectInstance));
    }

    //Move object Instance to xPos, yPos
    public static void moveObjectTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, int posX, int posY) {
        objectInstance.state.posX = posX;
        objectInstance.state.posY = posY;
        gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance));
    }

    public static void moveObjectTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, ObjectInstance targetObjectInstance) {
        moveObjectTo(gamePanelId, gameInstance, player, objectInstance, targetObjectInstance.state.posX, targetObjectInstance.state.posY);
    }


    //Move the stack given by an id list to posX, posY
    public static void moveStackTo(int gamePanelId, GameInstance gameInstance, Player player, IntegerArrayList idList, int posX, int posY) {
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
            moveObjectTo(gamePanelId, gameInstance, player, currentObject, posX + relativeX.get(i), posY + relativeY.get(i));
        }
    }

    //Move the stack given by an id list to objectInstance position
    public static void moveStackTo(int gamePanelId, GameInstance gameInstance, Player player, IntegerArrayList idList, ObjectInstance objectInstance) {
        if (objectInstance != null) {
            moveStackTo(gamePanelId, gameInstance, player, idList, objectInstance.state.posX, objectInstance.state.posY);
        }
    }

    //Move the stack given by an objectInstance to posX, posY
    public static void moveStackTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance stackObject, int posX, int posY) {
        moveStackTo(gamePanelId, gameInstance, player, getStackFromTop(gameInstance, stackObject), posX, posY);
    }

    //Move the stack given by an objectInstance to another objectInstance position
    public static void moveStackTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance stackObject, ObjectInstance baseObject) {
        moveStackTo(gamePanelId, gameInstance, player, getStackFromTop(gameInstance, stackObject), baseObject);
    }


    //Move the stack above objectInstance to posX, posY, with either stackObject is included or not, default is true
    public static void moveAboveStackTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance stackObject, int posX, int posY, boolean include) {
        moveStackTo(gamePanelId, gameInstance, player, getAboveStack(gameInstance, stackObject, include), posX, posY);
    }

    public static void moveAboveStackTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance stackObject, int posX, int posY) {
        moveAboveStackTo(gamePanelId, gameInstance, player, stackObject, posX, posY, true);
    }

    //Move the stack above objectInstance to another objectInstance position, with either stackObject is included or not, default is true
    public static void moveAboveStackTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance stackObject, ObjectInstance baseObject, boolean include) {
        moveStackTo(gamePanelId, gameInstance, player, getAboveStack(gameInstance, stackObject, include), baseObject);
    }

    public static void moveAboveStackTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance stackObject, ObjectInstance baseObject) {
        moveAboveStackTo(gamePanelId, gameInstance, player, stackObject, baseObject, true);
    }


    //Move the stack below objectInstance to posX, posY, with either stackObject is included or not, default is true
    public static void moveBelowStackTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance stackObject, int posX, int posY, boolean include) {
        moveStackTo(gamePanelId, gameInstance, player, getBelowStack(gameInstance, stackObject, include), posX, posY);
    }

    public static void moveBelowStackTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance stackObject, int posX, int posY) {
        moveBelowStackTo(gamePanelId, gameInstance, player, stackObject, posX, posY, true);
    }

    //Move the stack above objectInstance to another objectInstance position, with either stackObject is included or not, default is true
    public static void moveBelowStackTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance stackObject, ObjectInstance baseObject, boolean include) {
        moveStackTo(gamePanelId, gameInstance, player, getBelowStack(gameInstance, stackObject, include), baseObject);
    }

    public static void moveBelowStackTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance stackObject, ObjectInstance baseObject) {
        moveBelowStackTo(gamePanelId, gameInstance, player, stackObject, baseObject, true);
    }


    //Shuffle the stack containing objectInstance either with objectInstance or not, default is true
    public static void shuffleStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, boolean include) {
        if (objectInstance != null && (isStackInHand(gameInstance, player, getStack(gameInstance, objectInstance)) || isStackNotInHand(gameInstance, player, getStack(gameInstance, objectInstance)))) {
            IntegerArrayList objectStack = getStack(gameInstance, objectInstance);
            if (objectStack.size() > 1) {
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

    //Flip an object from one side to the other
    public static void flipObject(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        if (objectInstance != null && isObjectInHand(player, objectInstance)) {
            ((GameObjectToken.TokenState) objectInstance.state).side = !((GameObjectToken.TokenState) objectInstance.state).side;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance));
        }
    }

    //Flip the stack which contains object instance, either including this or not, default true
    public static void flipStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, boolean including) {
        if (objectInstance != null && isStackInHand(gameInstance, player, getStack(gameInstance, objectInstance))) {
            IntegerArrayList objectStack = getStack(gameInstance, objectInstance);
            int size = objectStack.size() - 1;
            for (int i = 0; i< objectStack.size(); ++i) {
                if (objectStack.get(i) != objectInstance.id || including) {
                    ObjectInstance currentObject = gameInstance.objects.get(objectStack.get(i));
                    int aboveId = currentObject.state.aboveInstanceId;
                    currentObject.state.aboveInstanceId = currentObject.state.belowInstanceId;
                    currentObject.state.belowInstanceId = aboveId;
                    if(i <= objectStack.size()/2)
                    {
                        int posX = currentObject.state.posX;
                        int posY = currentObject.state.posY;

                        currentObject.state.posX = gameInstance.objects.get(objectStack.get(size - i)).state.posX;
                        currentObject.state.posY = gameInstance.objects.get(objectStack.get(size - i)).state.posY;
                        gameInstance.objects.get(objectStack.get(size - i)).state.posX = posX;
                        gameInstance.objects.get(objectStack.get(size - i)).state.posY = posY;
                    }
                    flipObject(gamePanelId, gameInstance, player, currentObject);
                }
            }
        }
    }

    public static void flipStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        flipStack(gamePanelId, gameInstance, player, objectInstance, true);
    }

    //Count the number of elements in the stack containing objectInstance
    public static int countStack(GameInstance gameInstance, ObjectInstance objectInstance) {
        return getStack(gameInstance, objectInstance).size();
    }

    //Count the number of elements in the stack above objectInstance, either including objectInstance or not, default is true
    public static int countAboveStack(GameInstance gameInstance, ObjectInstance objectInstance, boolean including) {
        return getAboveStack(gameInstance, objectInstance, including).size();
    }

    public static int countAboveStack(GameInstance gameInstance, ObjectInstance objectInstance) {
        return countAboveStack(gameInstance, objectInstance, true);
    }

    //Count the number of elements in the stack below objectInstance, either including objectInstance or not, default is true
    public static int countBelowStack(GameInstance gameInstance, ObjectInstance objectInstance, boolean including) {
        return getBelowStack(gameInstance, objectInstance, including).size();
    }

    public static int countBelowStack(GameInstance gameInstance, ObjectInstance objectInstance) {
        return countBelowStack(gameInstance, objectInstance, true);
    }

    //Add up all the values of objects in stack containing objectInstance either including the value of it or not, default is true
    public static int countStackValues(GameInstance gameInstance, ObjectInstance objectInstance, boolean including) {
        IntegerArrayList stackIds = getBelowStack(gameInstance, objectInstance, including);
        int counter = 0;
        for (int id : stackIds) {
            GameObject currentObject = gameInstance.objects.get(id).go;
            if (currentObject instanceof GameObjectToken && (id != objectInstance.id || including)) {
                counter += ((GameObjectToken) currentObject).value;
            }
        }
        return counter;
    }

    public static int countStackValues(GameInstance gameInstance, ObjectInstance objectInstance) {
        return countStackValues(gameInstance, objectInstance, true);
    }

    //set above and below instance id to -1
    public static void removeAboveBelow(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        if (objectInstance != null) {
            objectInstance.state.aboveInstanceId = -1;
            objectInstance.state.belowInstanceId = -1;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance));
        }
    }


    //Remove objectInstance from the Stack
    public static ObjectInstance removeObject(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
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
                moveAboveStackTo(gamePanelId, gameInstance, player, objectInstance, objectInstance.state.posX, objectInstance.state.posY, false);
                gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, belowObject));
            } else {
                aboveObject.state.belowInstanceId = -1;
            }
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, aboveObject));
        } else {
            if (belowObject != null) {
                belowObject.state.aboveInstanceId = -1;
                gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, belowObject));
            }
        }
        removeAboveBelow(gamePanelId, gameInstance, player, objectInstance);
        return objectInstance;

    }


    //Get the top element stack around xPos, yPos with some inaccuracy
    public static ObjectInstance getTopActiveObjectByPosition(GameInstance gameInstance, int xPos, int yPos, int maxInaccuracy) {
        ObjectInstance activeObject = null;
        int distance = Integer.MAX_VALUE;
        Boolean insideObject = false;
        for (int i = 0; i < gameInstance.objects.size(); ++i) {
            ObjectInstance oi = gameInstance.objects.get(i);
            int xDiff = xPos - (oi.state.posX + oi.getWidth(-1) / 2), yDiff = yPos - (oi.state.posY + oi.getHeight(-1) / 2);
            int dist = xDiff * xDiff + yDiff * yDiff;

            Boolean leftIn = (xPos > (oi.state.posX - maxInaccuracy));
            Boolean rightIn = (xPos < (oi.state.posX + oi.getWidth(-1) + maxInaccuracy));
            Boolean topIn = (yPos < (oi.state.posY + oi.getHeight(-1) + maxInaccuracy));
            Boolean bottomIn = (yPos > (oi.state.posY - maxInaccuracy));

            if (dist < distance) {
                insideObject = leftIn && rightIn && topIn && bottomIn;
                if (insideObject) {
                    activeObject = getStackTop(gameInstance, oi);
                    distance = dist;
                }
            }
        }

        return activeObject;
    }

    public static ObjectInstance getTopActiveObjectByPosition(GameInstance gameInstance, int xPos, int yPos) {
        return getTopActiveObjectByPosition(gameInstance, xPos, yPos, 0);
    }

    //Get element nearest to xPos, yPos with some inaccuracy
    public static ObjectInstance getNearestObjectByPosition(GameInstance gameInstance, Player player, int xPos, int yPos, double zooming, int maxInaccuracy, ObjectInstance ignoredObject) {
        ObjectInstance activeObject = null;
        int distance = Integer.MAX_VALUE;
        Boolean insideObject = false;
        for (int i = 0; i < gameInstance.objects.size(); ++i) {
            ObjectInstance oi = gameInstance.objects.get(i);
            if (oi != ignoredObject) {
                int xDiff = (int) (xPos - (oi.state.posX*zooming + oi.getWidth(player.id) / 2)), yDiff = (int) (yPos - (oi.state.posY*zooming + oi.getHeight(player.id) / 2));
                int dist = xDiff * xDiff + yDiff * yDiff;

                Boolean leftIn = (xPos > (oi.state.posX*zooming - maxInaccuracy));
                Boolean rightIn = (xPos < (oi.state.posX*zooming + oi.getWidth(player.id) + maxInaccuracy));
                Boolean topIn = (yPos < (oi.state.posY*zooming + oi.getHeight(player.id) + maxInaccuracy));
                Boolean bottomIn = (yPos > (oi.state.posY*zooming - maxInaccuracy));

                if (dist < distance) {
                    insideObject = leftIn && rightIn && topIn && bottomIn;
                    if (insideObject) {
                        activeObject = oi;
                        distance = dist;
                    }
                }
            }
        }

        return activeObject;
    }

    public static ObjectInstance getNearestObjectByPosition(GameInstance gameInstance, Player player, int xPos, int yPos, double zooming, ObjectInstance ignoredObject) {
        ObjectInstance currentObject = getNearestObjectByPosition(gameInstance, player, xPos, yPos, zooming, 0, ignoredObject);
        if (haveSamePositions(getStackTop(gameInstance, currentObject), getStackBottom(gameInstance, currentObject)))
        {
            return getStackTop(gameInstance, currentObject);
        }
        else{
            return currentObject;
        }

    }


    //show all objects below element objectInstance
    public static void viewBelowObjects(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, int cardMargin) {
        if (objectInstance != null) {
            IntegerArrayList belowList = getBelowStack(gameInstance, objectInstance);
            if (belowList.size() > 1) {
                int posX = objectInstance.state.posX;
                int posY = objectInstance.state.posY;

                if (haveSamePositions(gameInstance.objects.get(belowList.get(0)), gameInstance.objects.get(belowList.last()))) {
                    for (int i = 0; i < belowList.size(); i++) {
                        moveObjectTo(gamePanelId, gameInstance, player, gameInstance.objects.get(belowList.get(i)), (int) (posX - (belowList.size() / 2.0 - i) * cardMargin), posY);
                        //removeFromStack(gamePanelId, gameInstance, player, gameInstance.objects.get(belowList.get(i)));
                    }
                }
            }

        }
    }

    public static void viewBelowObjects(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        viewBelowObjects(gamePanelId, gameInstance, player, objectInstance, 0);
    }

    //Move the whole stack to element object instance
    public static void collectStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        if (!haveSamePositions(getStackBottom(gameInstance, objectInstance), getStackTop(gameInstance, objectInstance))) {
            IntegerArrayList stack = getStackFromTop(gameInstance, objectInstance);
            for(int id: stack)
            {
                moveObjectTo(gamePanelId, gameInstance, player, gameInstance.objects.get(id), objectInstance);
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
        if (objectInstance != null && !haveSamePositions(getStackTop(gameInstance, objectInstance), getStackBottom(gameInstance, objectInstance))) {
            IntegerArrayList stackList = getStackFromTop(gameInstance, objectInstance);
            for (int x : stackList) {
                gameInstance.objects.get(x).state.aboveInstanceId = -1;
                gameInstance.objects.get(x).state.belowInstanceId = -1;
            }
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance));
        }
    }

    //take an object in the hand of player
    public static void takeObjects(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        IntegerArrayList stackIds = getStack(gameInstance, objectInstance);
        for(int id: stackIds)
        {
            ObjectInstance currentInstance = gameInstance.objects.get(id);
            if (player.id != currentInstance.state.owner_id && currentInstance.state.owner_id == -1) {
                currentInstance.state.owner_id = player.id;
                gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, currentInstance));
            }
        }
    }

    //drop an object from the hand of player
    public static void dropObjects(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        IntegerArrayList stackIds = getStack(gameInstance, objectInstance);
        for(int id: stackIds) {
            ObjectInstance currentInstance = gameInstance.objects.get(id);
            if (player != currentInstance.inHand) {
                currentInstance.state.owner_id = -1;
                flipObject(gamePanelId, gameInstance, player, currentInstance);
                gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, currentInstance));
            }
        }
    }


    public static ObjectInstance setActiveObjectByMouseAndKey(GameInstance gameInstance, MouseEvent arg0, boolean[] loggedKeys, int maxInaccuracy) {
        ObjectInstance activeObject = null;
        int pressedXPos = arg0.getX();
        int pressedYPos = arg0.getY();
        int distance = Integer.MAX_VALUE;
        Boolean insideObject = false;
        for (int i = 0; i < gameInstance.objects.size(); ++i) {
            ObjectInstance oi = gameInstance.objects.get(i);
            int xDiff = pressedXPos - (oi.state.posX + oi.getWidth(-1) / 2), yDiff = pressedYPos - (oi.state.posY + oi.getHeight(-1) / 2);
            int dist = xDiff * xDiff + yDiff * yDiff;

            Boolean leftIn = (pressedXPos > (oi.state.posX - maxInaccuracy));
            Boolean rightIn = (pressedXPos < (oi.state.posX + oi.getWidth(-1) + maxInaccuracy));
            Boolean topIn = (pressedYPos < (oi.state.posY + oi.getHeight(-1) + maxInaccuracy));
            Boolean bottomIn = (pressedYPos > (oi.state.posY - maxInaccuracy));

            if (dist < distance) {
                insideObject = leftIn && rightIn && topIn && bottomIn;
                if (insideObject) {
                    if (!loggedKeys[KeyEvent.VK_SHIFT] && ObjectFunctions.haveSamePositions(oi, ObjectFunctions.getStackTop(gameInstance, oi))) {
                        activeObject = ObjectFunctions.getStackTop(gameInstance, oi);
                    } else if (!ObjectFunctions.haveSamePositions(oi, ObjectFunctions.getStackTop(gameInstance, oi))) {
                        activeObject = oi;
                    } else {
                        activeObject = ObjectFunctions.getStackBottom(gameInstance, oi);
                    }
                    distance = dist;
                }
            }
        }
        if (!insideObject) {
            activeObject = null;
        }
        return activeObject;
    }


    public static void mergeStacks(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance topStackInstance, ObjectInstance bottomStackInstance) {
        moveStackTo(gamePanelId, gameInstance, player, topStackInstance, bottomStackInstance);
        ObjectInstance topElement = getStackTop(gameInstance, bottomStackInstance);
        ObjectInstance bottomElement = getStackBottom(gameInstance, topStackInstance);
        topElement.state.aboveInstanceId = bottomElement.id;
        bottomElement.state.belowInstanceId = topElement.id;
        gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, topElement));
        gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, bottomElement));

    }

    public static void releaseObjects(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance activeObject, double zooming, int maxInaccuracy) {
        if(activeObject != null) {
            ObjectInstance objectInstance = getNearestObjectByPosition(gameInstance, player, activeObject.state.posX, activeObject.state.posY, zooming, activeObject);
            if (isStackCollected(gameInstance, objectInstance)) {
                if (objectInstance != activeObject) {
                    ObjectFunctions.mergeStacks(gamePanelId, gameInstance, player, activeObject, objectInstance);
                }
            }
            else
            {
                Pair<ObjectInstance, ObjectInstance> insertObjects = getInsertObjects(gameInstance, player, activeObject.state.posX, activeObject.state.posY, zooming, activeObject);
                insertIntoStack(gamePanelId, gameInstance, player, activeObject, insertObjects.getKey(), insertObjects.getValue(), activeObject.getWidth(player.id)/2);
            }
        }
    }


    public static void releaseObjects(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance activeObject, double zooming) {
        releaseObjects(gamePanelId, gameInstance, player, activeObject, zooming, activeObject.getWidth(player.id) / 3);
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
        IntegerArrayList stackList = getStackFromTop(gameInstance, stackInstance);
        for (int x : stackList) {
            if (x == checkInstance.id) {
                return true;
            }
        }
        return false;
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

    public static Pair<ObjectInstance, ObjectInstance> getInsertObjects(GameInstance gameInstance, Player player, int posX, int posY, double zooming, ObjectInstance ignoredObject){
        ObjectInstance objectInstance = getNearestObjectByPosition(gameInstance, player, posX, posY, zooming, ignoredObject);
        if (isAboveObject(objectInstance) && isBelowObject(objectInstance))
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
        else if (isAboveObject(objectInstance))
        {
            return new Pair<>(getAboveObject(gameInstance, objectInstance), objectInstance);
        }
        else if(isBelowObject(objectInstance)){
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

    public static void insertIntoStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, ObjectInstance objectAbove, ObjectInstance objectBelow, int cardMargin) {
        if (!isStackCollected(gameInstance, objectBelow) || !isStackCollected(gameInstance, objectAbove)) {
            if (objectInstance != null) {
                if (objectBelow != null) {
                    objectBelow.state.aboveInstanceId = objectInstance.id;
                    objectInstance.state.belowInstanceId = objectBelow.id;
                    moveObjectTo(gamePanelId, gameInstance, player, objectInstance, objectBelow.state.posX + cardMargin, objectBelow.state.posY);
                    if (objectAbove != null) {
                        objectAbove.state.belowInstanceId = objectInstance.id;
                        objectInstance.state.aboveInstanceId = objectAbove.id;
                        moveObjectTo(gamePanelId, gameInstance, player, objectInstance, objectAbove);
                        moveAboveStackTo(gamePanelId, gameInstance, player,objectInstance, objectInstance.state.posX - objectInstance.getWidth(player.id)/2, objectInstance.state.posY, false);
                        //moveAboveStackTo(gameInstance, player, objectInstance);
                        //moveAboveStackTo(gamePanelId, gameInstance, player, objectInstance, objectInstance.state.posX + cardMargin, objectInstance.state.posY, false);
                    }
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
        ObjectInstance topObject = getStackTop(gameInstance, objectInstance);
        for (int i = 0; i < number; ++i) {
            int belowId = topObject.state.belowInstanceId;
            objectList.add(topObject.id);
            if (belowId != -1) {
                objectList.add(belowId);
                topObject = gameInstance.objects.get(topObject.state.belowInstanceId);
            } else {
                break;
            }
        }
        return objectList;
    }

    public static void splitStackAtN(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, int number) {
        int splitObjectid = ObjectFunctions.getTopNObjects(gameInstance, objectInstance, number).last();
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

    public static void drawStack(Graphics g, IntegerArrayList stackList, GameInstance gameInstance, int playerId, double zooming, Logger logger) {
        if (haveSamePositions(gameInstance.objects.get(stackList.get(0)), gameInstance.objects.get(stackList.last()))) {
            IntegerArrayList newStackList = new IntegerArrayList();
            newStackList.add(gameInstance.objects.get(stackList.last()).id);
            stackList = newStackList;
        }
        for (int id : stackList) {
            drawObject(g,gameInstance.objects.get(id), playerId,zooming,logger);
        }
    }

    public static void drawObject(Graphics g, ObjectInstance objectInstance, int playerId, double zooming, Logger logger) {
        BufferedImage img = objectInstance.go.getLook(objectInstance.state, playerId);
        if (objectInstance.getRotation() == 0) {
            if (objectInstance.state == null || img == null) {
                logger.error("Object state is null");
            } else {
                g.drawImage(img, (objectInstance.state.posX), (objectInstance.state.posY), (int) (objectInstance.scale * img.getWidth() * zooming), (int) (objectInstance.scale * img.getHeight() * zooming), null);
            }
        } else {
            double rotationRequired = Math.toRadians(objectInstance.getRotation());
            double locationX = img.getWidth() / 2;
            double locationY = img.getHeight() / 2;
            AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX * objectInstance.scale, locationY * objectInstance.scale);
            tx.scale(objectInstance.scale, objectInstance.scale);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
            g.drawImage(op.filter(img, null), objectInstance.state.posX, objectInstance.state.posY, null);
        }
    }

    public static void drawBorder(Graphics g, Player player, ObjectInstance objectInstance, int borderWidth, double zooming) {
        if (objectInstance != null) {
            g.setColor(player.color);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setStroke(new BasicStroke(borderWidth));
            g2d.drawRect(objectInstance.state.posX - borderWidth / 2, objectInstance.state.posY - borderWidth / 2, (int) (objectInstance.getWidth(player.id) * zooming) + borderWidth, (int) (objectInstance.getHeight(player.id) * zooming) + borderWidth);
        }
    }

    public static void drawStackBorder(GameInstance gameInstance, Graphics g, Player player, ObjectInstance objectInstance, int borderWidth, double zooming) {
        if (objectInstance != null) {
            if(isStackCollected(gameInstance, objectInstance))
            {
                drawBorder(g, player, getStackTop(gameInstance, objectInstance), borderWidth, zooming);
            }
            else
            {
                Graphics2D g2d = (Graphics2D) g.create();
                g.setColor(player.color);
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

    public static boolean isObjectInHand(Player player, ObjectInstance objectInstance){
        return objectInstance.state.owner_id == player.id;
    }


    public static int getStackOwner(GameInstance gameInstance, IntegerArrayList stackIds){
        int ownerId = -1;
        for(int id: stackIds)
        {
            if(ownerId == -1 || gameInstance.objects.get(id).state.owner_id == ownerId)
            {
                ownerId = gameInstance.objects.get(id).state.owner_id;
            }
            else
                return -1;
        }
        return ownerId;
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

}
