package gameObjects.functions;

import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.definition.GameObjectBook;
import gameObjects.definition.GameObjectBox;
import gameObjects.definition.GameObjectDice;
import gameObjects.definition.GameObjectFigure;
import gameObjects.definition.GameObjectToken;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.ObjectInstance.Relation;
import gameObjects.instance.ObjectState;
import geometry.Vector2d;
import gui.game.GamePanel;
import gui.game.Player;
import util.data.IntegerArrayList;

public class MoveFunctions {
    /**
     * Moves object to posX, posY
     *
     * @param gamePanel      Game Panel object
     * @param gameInstance   Instance of Game
     * @param player         Current player
     * @param objectInstance Instance of object
     * @param posX           target x position of object
     * @param posY           target y position of object
     */
    public static void moveObjectTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, int posX, int posY) {
        ObjectState state = objectInstance.state.copy();
        IntegerArrayList ial = new IntegerArrayList();
        ObjectFunctions.getAllAboveLyingObjects(gameInstance, player, objectInstance, ial);
        for (int id : ial){
            ObjectInstance oi = gameInstance.getObjectInstanceById(id);
            int relativeX = oi.state.posX - state.posX;
            int relativeY = oi.state.posY - state.posY;
            ObjectState aboveLyingState = oi.state.copy();
            aboveLyingState.posX = posX + relativeX;
            aboveLyingState.posY = posY + relativeY;
            aboveLyingState.rotation = (int) PlayerFunctions.GetCurrentPlayerRotation(gameInstance, player);
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, oi, aboveLyingState));
        }
        state.posX = posX;
        state.posY = posY;
        state.rotation = (int) PlayerFunctions.GetCurrentPlayerRotation(gameInstance, player);
        gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance, state));
    }

    /**
     * Moves objectInstance to posX, posY of targetObjectInstance
     *
     * @param gamePanel            Game Panel object
     * @param gameInstance         Instance of Game
     * @param player               Current player
     * @param objectInstance       Instance of object
     * @param targetObjectInstance Target Instance
     */
    public static void moveObjectTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance, ObjectInstance targetObjectInstance) {
        moveObjectTo(gamePanelId, gameInstance, player, objectInstance, targetObjectInstance.state.posX, targetObjectInstance.state.posY);
    }


    /**
     * Moves stack to x, y position, remaining the relative positions of cards inside the stack
     *
     * @param gamePanel    Game Panel object
     * @param gameInstance Instance of Game
     * @param player       Current player
     * @param idList       All ids of stack elements
     * @param posX         Target x position
     * @param posY         Target y position
     */
    public static void moveStackTo(int gamePanelId, GameInstance gameInstance, Player player, IntegerArrayList idList, int posX, int posY) {
        IntegerArrayList relativeX = new IntegerArrayList(idList.size());
        IntegerArrayList relativeY = new IntegerArrayList(idList.size());
        relativeX.add(0);
        relativeY.add(0);
        int firstPosX = gameInstance.getObjectInstanceById(idList.getI(0)).state.posX;
        int firstPosY = gameInstance.getObjectInstanceById(idList.getI(0)).state.posY;
        for (int i = 1; i < idList.size(); i++) {//TODO Why is this done in two loops?
            ObjectInstance currentObject = gameInstance.getObjectInstanceById(idList.getI(i));            
            relativeX.add(currentObject.state.posX - firstPosX);
            relativeY.add(currentObject.state.posY - firstPosY);
        }
        for (int i = 0; i < idList.size(); i++) {
            ObjectInstance currentObject = gameInstance.getObjectInstanceById(idList.getI(i));
            moveObjectTo(gamePanelId, gameInstance, player, currentObject, posX + relativeX.getI(i), posY + relativeY.getI(i));
        }
    }

    /**
     * Moves stack to x, y position of targetObjectInstance
     *
     * @param gamePanel            Game Panel object
     * @param gameInstance         Instance of Game
     * @param player               Current player
     * @param idList               All ids of stack elements
     * @param targetObjectInstance Target Instance
     */
    public static void moveStackTo(int gamePanelId, GameInstance gameInstance, Player player, IntegerArrayList idList, ObjectInstance targetObjectInstance) {
        if (targetObjectInstance != null && targetObjectInstance.go instanceof GameObjectToken) {
            moveStackTo(gamePanelId, gameInstance, player, idList, targetObjectInstance.state.posX, targetObjectInstance.state.posY);
        }
    }

    /**
     * Moves stack to x, y position of stack object
     *
     * @param gamePanelId    Game Panel object
     * @param gameInstance Instance of Game
     * @param player       Current player
     * @param stackObject  Instance of stack
     * @param posX         Target x position
     * @param posY         Target y position
     */
    public static void moveStackTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance stackObject, int posX, int posY) {
        if (stackObject != null && stackObject.go instanceof GameObjectToken) {
            IntegerArrayList ial = new IntegerArrayList();
            ObjectFunctions.getStackFromTop(gameInstance, stackObject, ial);
            moveStackTo(gamePanelId, gameInstance, player, ial, posX, posY);
        }
    }

    /**
     * Moves stack to x, y position of targetObjectInstance
     *
     * @param gamePanelId            Game Panel object
     * @param gameInstance         Instance of Game
     * @param player               Current player
     * @param stackObject          Instance of stack
     * @param targetObjectInstance Target Instance
     */
    public static void moveStackTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance stackObject, ObjectInstance targetObjectInstance) {
        if (stackObject != null && stackObject.go instanceof GameObjectToken) {
            IntegerArrayList ial = new IntegerArrayList();
            ObjectFunctions.getStackFromTop(gameInstance, stackObject, ial);
            moveStackTo(gamePanelId, gameInstance, player, ial, targetObjectInstance);
        }
    }


    /**
     * Moves stack above stackObject to posX, posY
     *
     * @param gamePanelId    Game Panel object
     * @param gameInstance Instance of Game
     * @param player       Current player
     * @param stackObject  Instance of stack
     * @param posX         Target x position
     * @param posY         Target y position
     * @param include      if stackObject should be inluded default is true
     */
    public static void moveAboveStackTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance stackObject, int posX, int posY, boolean include) {
        if (stackObject != null && stackObject.go instanceof GameObjectToken) {
            IntegerArrayList tmp = new IntegerArrayList();
            ObjectFunctions.getToOneEnd(gameInstance, stackObject, include, tmp, Relation.ABOVE);
            moveStackTo(gamePanelId, gameInstance, player, tmp, posX, posY);
        }
    }

    public static void moveAboveStackTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance stackObject, int posX, int posY) {
        moveAboveStackTo(gamePanelId, gameInstance, player, stackObject, posX, posY, true);
    }

    /**
     * Moves stack above stackObject to posX, posY of targetObjectInstance
     *
     * @param gamePanelId            Game Panel object
     * @param gameInstance         Instance of Game
     * @param player               Current player
     * @param stackObject          Instance of stack
     * @param targetObjectInstance Target object
     * @param include              if stackObject should be inluded default is true
     */
    public static void moveAboveStackTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance stackObject, ObjectInstance targetObjectInstance, boolean include) {
        if (stackObject != null && stackObject.go instanceof GameObjectToken) {
            IntegerArrayList tmp = new IntegerArrayList();
            ObjectFunctions.getToOneEnd(gameInstance, stackObject, include, tmp, Relation.ABOVE);
            moveStackTo(gamePanelId, gameInstance, player, tmp, targetObjectInstance);
        }
    }

    public static void moveAboveStackTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance stackObject, ObjectInstance baseObject) {
        moveAboveStackTo(gamePanelId, gameInstance, player, stackObject, baseObject, true);
    }


    /**
     * Moves stack below stackObject to posX, posY
     *
     * @param gamePanelId    Game Panel object
     * @param gameInstance Instance of Game
     * @param player       Current player
     * @param stackObject  Instance of stack
     * @param posX         Target x position
     * @param posY         Target y position
     * @param include      if stackObject should be included default is true
     */
    public static void moveBelowStackTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance stackObject, int posX, int posY, boolean include) {
        if (stackObject != null && stackObject.go instanceof GameObjectToken) {
            IntegerArrayList ial = new IntegerArrayList();
            ObjectFunctions.getToOneEnd(gameInstance, stackObject, include, ial, Relation.BELOW);
            moveStackTo(gamePanelId, gameInstance, player, ial, posX, posY);
        }
    }

    public static void moveBelowStackTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance stackObject, int posX, int posY) {
        moveBelowStackTo(gamePanelId, gameInstance, player, stackObject, posX, posY, true);
    }

    /**
     * Moves stack above stackObject to posX, posY of targetObjectInstance
     *
     * @param gamePanelId            Game Panel object
     * @param gameInstance         Instance of Game
     * @param player               Current player
     * @param stackObject          Instance of stack
     * @param targetObjectInstance Target object
     * @param include              if stackObject should be included default is true
     */
    public static void moveBelowStackTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance stackObject, ObjectInstance targetObjectInstance, boolean include) {
        if (stackObject != null && stackObject.go instanceof GameObjectToken) {
            IntegerArrayList ial = new IntegerArrayList();
            ObjectFunctions.getToOneEnd(gameInstance, stackObject, include, ial, Relation.BELOW);
            moveStackTo(gamePanelId, gameInstance, player, ial, targetObjectInstance);
        }
    }

    public static void moveBelowStackTo(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance stackObject, ObjectInstance baseObject) {
        moveBelowStackTo(gamePanelId, gameInstance, player, stackObject, baseObject, true);
    }

    public static void dragTokens(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance activeObject, MouseEvent arg0, int xDiff, int yDiff, int mouseWheelValue, boolean selectedDrag){
        /* Drag when left mouse down or middle mouse button is down*/
         if((SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isRightMouseButton(arg0) || SwingUtilities.isMiddleMouseButton(arg0)) && activeObject != null && activeObject.go instanceof GameObjectToken) {
             /*Remove dragged object from stack if middle mouse button is not held*/
             if (!SwingUtilities.isMiddleMouseButton(arg0) && !(SwingUtilities.isLeftMouseButton(arg0) && arg0.isShiftDown()) && !selectedDrag) {
                 ObjectFunctions.removeObject(gamePanelId, gameInstance, player, activeObject);
             }
             /*Remove top N dragged objects from stack if middle mouse button is held*/
             else if (mouseWheelValue > 0 && (SwingUtilities.isMiddleMouseButton(arg0) || SwingUtilities.isLeftMouseButton(arg0) && arg0.isShiftDown()) && !selectedDrag) {
                 ObjectFunctions.splitStackAtN(gamePanelId, gameInstance, player, activeObject, mouseWheelValue);
             }
             /*Move the dragged objects to the position*/
             int count = ObjectFunctions.countStack(gameInstance, activeObject);
             player.actionString = "Took " + count + (count > 1 ? " objects " : " object");
             moveStackTo(gamePanelId, gameInstance, player, activeObject, xDiff, yDiff);
             /*Display uncollected stack*/
             if (!ObjectFunctions.isStackCollected(gameInstance, activeObject)) {
                 ObjectFunctions.displayStack(gamePanelId, gameInstance, player, ObjectFunctions.getStackTop(gameInstance, activeObject), (int) (activeObject.getWidth(player.id) * gameInstance.cardOverlap));
             }
         }
         /*Get bottom card with shift TODO*/
        /*else if((SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isRightMouseButton(arg0)) && arg0.isShiftDown() && activeObject != null && activeObject.state.owner_id == -1 && activeObject.go instanceof GameObjectToken) {
            activeObject = ObjectFunctions.getStackBottom(gameInstance,activeObject);
            ObjectFunctions.removeObject(gamePanel.id, gameInstance, player, activeObject);
            ObjectFunctions.moveObjectTo(gamePanel.id, gameInstance, player, activeObject, xDiff, yDiff);
            gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player.id, activeObject.id));
        }
         */
    }

    public static void dragDices(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance activeObject, MouseEvent arg0, int xDiff, int yDiff, int mouseWheelValue) {
        if (activeObject != null && (activeObject.go instanceof GameObjectDice) && (SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isMiddleMouseButton(arg0))) {
            moveObjectTo(gamePanelId, gameInstance, player, activeObject, xDiff, yDiff);
        }
    }
    public static void dragBooks(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance activeObject, MouseEvent arg0, int xDiff, int yDiff, int mouseWheelValue) {
        if (activeObject != null && (activeObject.go instanceof GameObjectBook) && (SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isMiddleMouseButton(arg0))) {
            moveObjectTo(gamePanelId, gameInstance, player, activeObject, xDiff, yDiff);
        }
    }

    public static void dragFigures(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance activeObject, MouseEvent arg0, int xDiff, int yDiff, int mouseWheelValue) {
        if (activeObject != null && (activeObject.go instanceof GameObjectFigure) && (SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isMiddleMouseButton(arg0))) {
            moveObjectTo(gamePanelId, gameInstance, player, activeObject, xDiff, yDiff);
        }
    }

    public static void dragBoxes(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance activeObject, MouseEvent arg0, int xDiff, int yDiff, int mouseWheelValue) {
        if (activeObject != null && (activeObject.go instanceof GameObjectBox) && (SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isMiddleMouseButton(arg0))) {
            moveObjectTo(gamePanelId, gameInstance, player, activeObject, xDiff, yDiff);
        }
    }

    public static void dragObjects(GamePanel gamePanel, GameInstance gameInstance, Player player, MouseEvent arg0, IntegerArrayList selectedObjectIds, IntegerArrayList objOrigPosX, IntegerArrayList objOrigPosY, Vector2d mousePressedGamePos, Vector2d mouseBoardPos, int mouseWheelValue){
        int counter = 0;
        for (int id : selectedObjectIds) {
            boolean selectedDrag = selectedObjectIds.size() > 1;
            ObjectInstance oi = gameInstance.getObjectInstanceById(id);
            ObjectFunctions.removeLieOnRelation(gamePanel.id, gameInstance, player, oi);
            MoveFunctions.dragTokens(gamePanel.id, gameInstance, player, oi, arg0, objOrigPosX.getI(counter) - mousePressedGamePos.getXI() + mouseBoardPos.getXI(), objOrigPosY.get(counter) - mousePressedGamePos.getYI() + mouseBoardPos.getYI(), mouseWheelValue, selectedDrag);
            MoveFunctions.dragDices(gamePanel.id, gameInstance, player, oi, arg0, objOrigPosX.getI(counter) - mousePressedGamePos.getXI() + mouseBoardPos.getXI(), objOrigPosY.get(counter) - mousePressedGamePos.getYI() + mouseBoardPos.getYI(), mouseWheelValue);
            MoveFunctions.dragFigures(gamePanel.id, gameInstance, player, oi, arg0, objOrigPosX.getI(counter) - mousePressedGamePos.getXI() + mouseBoardPos.getXI(), objOrigPosY.get(counter) - mousePressedGamePos.getYI() + mouseBoardPos.getYI(), mouseWheelValue);
            MoveFunctions.dragBooks(gamePanel.id, gameInstance, player, oi, arg0, objOrigPosX.getI(counter) - mousePressedGamePos.getXI() + mouseBoardPos.getXI(), objOrigPosY.get(counter) - mousePressedGamePos.getYI() + mouseBoardPos.getYI(), mouseWheelValue);
            MoveFunctions.dragBoxes(gamePanel.id, gameInstance, player, oi, arg0, objOrigPosX.getI(counter) - mousePressedGamePos.getXI() + mouseBoardPos.getXI(), objOrigPosY.get(counter) - mousePressedGamePos.getYI() + mouseBoardPos.getYI(), mouseWheelValue);
            counter += 1;
        }
        gamePanel.repaint();
    }
}
