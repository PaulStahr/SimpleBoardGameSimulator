package gameObjects.functions;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.definition.GameObjectDice;
import gameObjects.definition.GameObjectFigure;
import gameObjects.definition.GameObjectToken;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import geometry.Vector2d;
import gui.GamePanel;
import main.Player;
import util.data.IntegerArrayList;

public class MoveFunctions {

    public static void dragTokens(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance activeObject, MouseEvent arg0, int xDiff, int yDiff, int mouseWheelValue, boolean selectedDrag){
        /* Drag when left mouse down or middle mouse button is down and shift is not held*/
         if((SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isRightMouseButton(arg0) || SwingUtilities.isMiddleMouseButton(arg0)) && !arg0.isShiftDown() && activeObject != null && activeObject.go instanceof GameObjectToken) {
            /*Remove dragged object from stack if middle mouse button is not held*/
            if (!SwingUtilities.isMiddleMouseButton(arg0) && !selectedDrag) {
                ObjectFunctions.removeObject(gamePanel, gameInstance, player, activeObject);
            }
            /*Remove top N dragged objects from stack if middle mouse button is held*/
            else if(mouseWheelValue > 0 && SwingUtilities.isMiddleMouseButton(arg0) && !selectedDrag)
            {
                ObjectFunctions.splitStackAtN(gamePanel.id, gameInstance, player, activeObject, mouseWheelValue);
            }
            /*Move the dragged objects to the position*/
             player.actionString = "Took " + ObjectFunctions.countStack(gameInstance, activeObject) + (ObjectFunctions.countStack(gameInstance, activeObject) > 1 ?" objects " : " object");
            ObjectFunctions.moveStackTo(gamePanel, gameInstance, player, activeObject, xDiff, yDiff);
            /*Display uncollected stack*/
            if (!ObjectFunctions.isStackCollected(gameInstance, activeObject)) {
                ObjectFunctions.displayStack(gamePanel, gameInstance, player, ObjectFunctions.getStackTop(gameInstance, activeObject), (int) (activeObject.getWidth(player.id)*gamePanel.cardOverlap));
            }
        }
         /*Get bottom card with shift*/
        else if((SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isRightMouseButton(arg0)) && arg0.isShiftDown() && activeObject != null && activeObject.state.owner_id == -1 && activeObject.go instanceof GameObjectToken) {
            activeObject = ObjectFunctions.getStackBottom(gameInstance,activeObject);
            ObjectFunctions.removeObject(gamePanel, gameInstance, player, activeObject);
            ObjectFunctions.moveObjectTo(gamePanel, gameInstance, player, activeObject, xDiff, yDiff);
            gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player.id, activeObject.id));
        }
    }

    public static void dragDices(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance activeObject, MouseEvent arg0, int xDiff, int yDiff, int mouseWheelValue) {
        if (activeObject != null && (activeObject.go instanceof GameObjectDice) && (SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isMiddleMouseButton(arg0))) {
            ObjectFunctions.moveObjectTo(gamePanel, gameInstance, player, activeObject, xDiff, yDiff);
            gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player.id, activeObject.id));
        }
    }

    public static void dragFigures(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance activeObject, MouseEvent arg0, int xDiff, int yDiff, int mouseWheelValue) {
        if (activeObject != null && (activeObject.go instanceof GameObjectFigure) && (SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isMiddleMouseButton(arg0))) {
            ObjectFunctions.moveObjectTo(gamePanel, gameInstance, player, activeObject, xDiff, yDiff);
            gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player.id, activeObject.id));
        }
    }

    public static void dragObjects(GamePanel gamePanel, GameInstance gameInstance, Player player, MouseEvent arg0, ArrayList<ObjectInstance> activeObjects, IntegerArrayList objOrigPosX, IntegerArrayList objOrigPosY, Vector2d mousePressedGamePos, Vector2d mouseBoardPos, int mouseWheelValue){
        int counter = 0;
        for (ObjectInstance oi : activeObjects) {
            boolean selectedDrag = false;
            if (activeObjects.size() > 1) {
                selectedDrag = true;
            }
            MoveFunctions.dragTokens(gamePanel, gameInstance, player, oi, arg0, objOrigPosX.get(counter) - mousePressedGamePos.getXI() + mouseBoardPos.getXI(), objOrigPosY.get(counter) - mousePressedGamePos.getYI() + mouseBoardPos.getYI(), mouseWheelValue, selectedDrag);
            MoveFunctions.dragDices(gamePanel, gameInstance, player, oi, arg0, objOrigPosX.get(counter) - mousePressedGamePos.getXI() + mouseBoardPos.getXI(), objOrigPosY.get(counter) - mousePressedGamePos.getYI() + mouseBoardPos.getYI(), mouseWheelValue);
            MoveFunctions.dragFigures(gamePanel, gameInstance, player, oi, arg0, objOrigPosX.get(counter) - mousePressedGamePos.getXI() + mouseBoardPos.getXI(), objOrigPosY.get(counter) - mousePressedGamePos.getYI() + mouseBoardPos.getYI(), mouseWheelValue);
            counter += 1;
        }
        gamePanel.repaint();
    }
}
