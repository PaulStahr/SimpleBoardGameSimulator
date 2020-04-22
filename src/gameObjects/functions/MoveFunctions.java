package gameObjects.functions;

import gameObjects.GameObjectInstanceEditAction;
import gameObjects.definition.GameObjectToken;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import gui.GamePanel;
import main.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class MoveFunctions {

    public static void dragTokens(GamePanel gamePanel, GameInstance gameInstance, Player player, ObjectInstance activeObject, MouseEvent arg0, int xDiff, int yDiff, int mouseWheelValue){
        /* Drag when left mouse down or middle mouse button is down and shift is not held*/
         if((SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isRightMouseButton(arg0) || SwingUtilities.isMiddleMouseButton(arg0)) && !arg0.isShiftDown() && activeObject != null && activeObject.go instanceof GameObjectToken) {
            /*Remove dragged object from stack if middle mouse button is not held*/
            if (!SwingUtilities.isMiddleMouseButton(arg0)) {
                ObjectFunctions.removeObject(gamePanel, gameInstance, player, activeObject);
            }
            /*Remove top N dragged objects from stack if middle mouse button is held*/
            else if(mouseWheelValue > 0 && SwingUtilities.isMiddleMouseButton(arg0))
            {
                ObjectFunctions.splitStackAtN(gamePanel.id, gameInstance, player, activeObject, mouseWheelValue);
            }
            /*Move the dragged objects to the position*/
             player.actionString = "Took " + ObjectFunctions.countStack(gameInstance, activeObject) + " objects ";
            ObjectFunctions.moveStackTo(gamePanel, gameInstance, player, activeObject, xDiff, yDiff);
            /*Display uncollected stack*/
            if (!ObjectFunctions.isStackCollected(gameInstance, activeObject)) {
                ObjectFunctions.displayStack(gamePanel, gameInstance, player, ObjectFunctions.getStackTop(gameInstance, activeObject), (int) (activeObject.getWidth(player.id)*gamePanel.cardOverlap));
            }
        }
        else if((SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isRightMouseButton(arg0)) && arg0.isShiftDown() && activeObject != null && activeObject.state.owner_id != player.id) {
            /*Remove top card*/
            ObjectFunctions.removeObject(gamePanel, gameInstance, player, activeObject);
            ObjectFunctions.moveObjectTo(gamePanel, gameInstance, player, activeObject, xDiff, yDiff);
            gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player, activeObject));
        }
    }
}
