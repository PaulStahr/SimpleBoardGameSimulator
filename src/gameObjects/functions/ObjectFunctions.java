package gameObjects.functions;

import gameObjects.GameObjectInstanceEditAction;
import gameObjects.definition.GameObject;
import gameObjects.definition.GameObjectToken;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import main.Player;
import util.data.IntegerArrayList;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Collections;

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
        }
        else {
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
        }
        else {
            return null;
        }
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
        for (int i = 0; i < idList.size(); i++) {
            ObjectInstance currentObject = gameInstance.objects.get(idList.get(i));
            moveObjectTo(gamePanelId, gameInstance, player, currentObject, posX, posY);
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
        if (objectInstance != null) {
            IntegerArrayList objectStack = getStack(gameInstance, objectInstance);
            if (objectStack.size() > 1) {
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
                    gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, currentObject));
                }
            }
        }
    }
    public static void shuffleStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        shuffleStack(gamePanelId, gameInstance, player, objectInstance);
    }

    //Flip an object from one side to the other
    public static void flipObject(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        if (objectInstance != null) {
            ((GameObjectToken.TokenState) objectInstance.state).side = !((GameObjectToken.TokenState) objectInstance.state).side;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance));
        }
    }

    //Flip the stack which contains object instance, either including this or not, default true
    public static void flipStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, boolean including) {
        if (objectInstance != null) {
            IntegerArrayList objectStack = getStack(gameInstance, objectInstance);
            for (int id : objectStack) {
                if(id != objectInstance.id || including) {
                    ObjectInstance currentObject = gameInstance.objects.get(id);
                    int aboveId = currentObject.state.aboveInstanceId;
                    currentObject.state.aboveInstanceId = currentObject.state.belowInstanceId;
                    currentObject.state.belowInstanceId = aboveId;
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
        for(int id : stackIds)
        {
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
            int xDiff = xPos - (oi.state.posX + oi.getWidth() / 2), yDiff = yPos - (oi.state.posY + oi.getHeight() / 2);
            int dist = xDiff * xDiff + yDiff * yDiff;

            Boolean leftIn = (xPos > (oi.state.posX - maxInaccuracy));
            Boolean rightIn = (xPos < (oi.state.posX + oi.getWidth() + maxInaccuracy));
            Boolean topIn = (yPos < (oi.state.posY + oi.getHeight() + maxInaccuracy));
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


    public static ObjectInstance getActiveObjectByPosition(GameInstance gameInstance, int xPos, int yPos, int maxInaccuracy) {
        ObjectInstance activeObject = null;
        int distance = Integer.MAX_VALUE;
        Boolean insideObject = false;
        for (int i = 0; i < gameInstance.objects.size(); ++i) {
            ObjectInstance oi = gameInstance.objects.get(i);
            int xDiff = xPos - (oi.state.posX + oi.getWidth() / 2), yDiff = yPos - (oi.state.posY + oi.getHeight() / 2);
            int dist = xDiff * xDiff + yDiff * yDiff;

            Boolean leftIn = (xPos > (oi.state.posX - maxInaccuracy));
            Boolean rightIn = (xPos < (oi.state.posX + oi.getWidth() + maxInaccuracy));
            Boolean topIn = (yPos < (oi.state.posY + oi.getHeight() + maxInaccuracy));
            Boolean bottomIn = (yPos > (oi.state.posY - maxInaccuracy));

            if (dist < distance) {
                insideObject = leftIn && rightIn && topIn && bottomIn;
                if (insideObject) {
                    activeObject = oi;
                    distance = dist;
                }
            }
        }

        return activeObject;
    }

    public static ObjectInstance getActiveObjectByPosition(GameInstance gameInstance, int xPos, int yPos) {
        return getActiveObjectByPosition(gameInstance, xPos, yPos, 0);
    }




    public static void viewBelowCards(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, int cardMargin) {
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

    public static void viewBelowCards(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        viewBelowCards(gamePanelId, gameInstance, player, objectInstance, 20);
    }


    public static void collectStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        if (!haveSamePositions(getStackBottom(gameInstance, objectInstance), getStackTop(gameInstance, objectInstance))) {
            IntegerArrayList stack = getStackFromTop(gameInstance, objectInstance);
            moveStackTo(gamePanelId, gameInstance, player, stack, objectInstance);
        }
    }

    public static boolean isStackBottom(ObjectInstance objectInstance) {
        if (objectInstance.state.belowInstanceId == -1) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isStackTop(ObjectInstance objectInstance) {
        if (objectInstance.state.aboveInstanceId == -1) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean haveSamePositions(ObjectInstance objectInstanceA, ObjectInstance objectInstanceB) {
        return (objectInstanceA.state.posX == objectInstanceB.state.posX && objectInstanceA.state.posY == objectInstanceB.state.posY);
    }


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

    public static void takeObject(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        if (player != objectInstance.inHand) {
            objectInstance.inHand = player;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance));
        }
    }

    public static void dropObject(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance) {
        objectInstance.inHand = null;
        gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance));
    }


    public static ObjectInstance setActiveObjectByMouseAndKey(GameInstance gameInstance, MouseEvent arg0, boolean[] loggedKeys, int maxInaccuracy) {
        ObjectInstance activeObject = null;
        int pressedXPos = arg0.getX();
        int pressedYPos = arg0.getY();
        int distance = Integer.MAX_VALUE;
        Boolean insideObject = false;
        for (int i = 0; i < gameInstance.objects.size(); ++i) {
            ObjectInstance oi = gameInstance.objects.get(i);
            int xDiff = pressedXPos - (oi.state.posX + oi.getWidth() / 2), yDiff = pressedYPos - (oi.state.posY + oi.getHeight() / 2);
            int dist = xDiff * xDiff + yDiff * yDiff;

            Boolean leftIn = (pressedXPos > (oi.state.posX - maxInaccuracy));
            Boolean rightIn = (pressedXPos < (oi.state.posX + oi.getWidth() + maxInaccuracy));
            Boolean topIn = (pressedYPos < (oi.state.posY + oi.getHeight() + maxInaccuracy));
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

    public static void releaseObjects(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance activeObject, int maxInaccuracy) {
        for (int i = 0; i < gameInstance.objects.size(); ++i) {
            ObjectInstance oi = gameInstance.objects.get(i);
            int xDiff = activeObject.state.posX - oi.state.posX, yDiff = activeObject.state.posY - oi.state.posY;
            int dist = xDiff * xDiff + yDiff * yDiff;
            if (dist < maxInaccuracy * maxInaccuracy && oi != activeObject) {
                ObjectInstance topElement = ObjectFunctions.getStackTop(gameInstance, oi);
                if (topElement != activeObject) {
                    ObjectFunctions.mergeStacks(gamePanelId, gameInstance, player, activeObject, topElement);
                    break;
                }
            }
        }
    }


    public static void releaseObjects(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance activeObject) {
        releaseObjects(gamePanelId, gameInstance, player, activeObject, activeObject.getWidth()/3);
    }

    public static ObjectInstance findNeighbouredStackTop(GameInstance gameInstance, Player player, ObjectInstance activeObject, int maxInaccuracy){
        for (int i = 0; i < gameInstance.objects.size(); ++i) {
            ObjectInstance oi = gameInstance.objects.get(i);
            int xDiff = activeObject.state.posX - oi.state.posX, yDiff = activeObject.state.posY - oi.state.posY;
            int dist = xDiff * xDiff + yDiff * yDiff;
            if (dist < maxInaccuracy * maxInaccuracy && oi != activeObject) {
                ObjectInstance topElement = ObjectFunctions.getStackTop(gameInstance, oi);
                if (!checkIfInStack(gameInstance, activeObject, topElement)){
                    return topElement;
                }
            }
        }
        return null;
    }

    public static ObjectInstance findNeighbouredStackTop(GameInstance gameInstance, Player player, ObjectInstance activeObject){
        return findNeighbouredStackTop(gameInstance, player, activeObject, activeObject.getWidth()/3);
    }

    public static boolean checkIfInStack(GameInstance gameInstance,ObjectInstance stackInstance, ObjectInstance checkInstance)
    {
        IntegerArrayList stackList = getStackFromTop(gameInstance, stackInstance);
        for(int x : stackList){
            if (x == checkInstance.id) {
                return true;
            }
        }
        return  false;
    }

    public static void makeStack(int gamePanelId, GameInstance gameInstance, Player player, IntegerArrayList stackElements)
    {
        for(int i = 0; i < stackElements.size(); ++i)
        {
            if(i==0 && stackElements.size() > 1)
            {
                gameInstance.objects.get(stackElements.get(i)).state.belowInstanceId = -1;
                gameInstance.objects.get(stackElements.get(i)).state.aboveInstanceId = gameInstance.objects.get(stackElements.get(i+1)).id;
                gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, gameInstance.objects.get(stackElements.get(i))));
            }
            else if(i == stackElements.size()-1 && stackElements.size() > 1)
            {
                gameInstance.objects.get(stackElements.get(i)).state.aboveInstanceId = -1;
                gameInstance.objects.get(stackElements.get(i)).state.belowInstanceId = gameInstance.objects.get(stackElements.get(i-1)).id;
                setObjectPosition(gamePanelId, gameInstance, player, gameInstance.objects.get(stackElements.get(i)), gameInstance.objects.get(stackElements.get(i-1)));
            }
            else
            {
                gameInstance.objects.get(stackElements.get(i)).state.aboveInstanceId = gameInstance.objects.get(stackElements.get(i+1)).id;
                gameInstance.objects.get(stackElements.get(i)).state.belowInstanceId = gameInstance.objects.get(stackElements.get(i-1)).id;
                setObjectPosition(gamePanelId, gameInstance, player, gameInstance.objects.get(stackElements.get(i)), gameInstance.objects.get(stackElements.get(i-1)));
            }
        }
    }

    public static void setObjectPosition(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, int posX, int posY)
    {
        objectInstance.state.posX = posX;
        objectInstance.state.posY = posY;
        gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance));
    }


    public static void setObjectPosition(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, ObjectInstance targetObjectInstance)
    {
        setObjectPosition(gamePanelId, gameInstance, player, objectInstance, targetObjectInstance.state.posX, targetObjectInstance.state.posY);
    }

    public static void getAllObjectsOfType(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance)
    {
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

    public static IntegerArrayList getTopNObjects(GameInstance gameInstance, ObjectInstance objectInstance, int number)
    {
        IntegerArrayList objectList = new IntegerArrayList();
        ObjectInstance topObject = getStackTop(gameInstance, objectInstance);
        for (int i = 0; i < number; ++i)
        {
            int belowId = topObject.state.belowInstanceId;
            objectList.add(topObject.id);
            if (belowId != -1)
            {
                objectList.add(belowId);
                topObject = gameInstance.objects.get(topObject.state.belowInstanceId);
            }
            else
            {
                break;
            }
        }
        return objectList;
    }

    public static void splitStackAtN(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, int number){
        int splitObjectid = ObjectFunctions.getTopNObjects(gameInstance, objectInstance, number).last();
        if(gameInstance.objects.get(splitObjectid).state.belowInstanceId == -1) {
            gameInstance.objects.get(splitObjectid).state.belowInstanceId = -1;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, gameInstance.objects.get(splitObjectid)));
        }
        else
        {
            gameInstance.objects.get(gameInstance.objects.get(splitObjectid).state.belowInstanceId).state.aboveInstanceId = -1;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, gameInstance.objects.get(gameInstance.objects.get(splitObjectid).state.belowInstanceId)));
            gameInstance.objects.get(splitObjectid).state.belowInstanceId = -1;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, gameInstance.objects.get(splitObjectid)));
        }
    }

}
