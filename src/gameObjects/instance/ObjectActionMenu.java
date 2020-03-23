package gameObjects.instance;

import gameObjects.GameObjectInstanceEditAction;
import gameObjects.definition.GameObject;
import gameObjects.definition.GameObjectCard;
import gameObjects.functions.CardStackFunctions;
import gui.GamePanel;
import main.Player;
import util.data.IntegerArrayList;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

public class ObjectActionMenu {
    /*Popup menu for object actions*/
    public JPopupMenu popup = new JPopupMenu();

    public JMenuItem flipItem = new JMenuItem("Flip Card");
    public JMenuItem discardRecordItem = new JMenuItem("");
    public JMenuItem shuffleCardItem = new JMenuItem("Shuffle Stack");
    public JMenuItem flipStackItem = new JMenuItem("Flip Stack");

    public ObjectInstance gameObjectInstance;
    public GameInstance gameInstance;

    public ObjectActionMenu(ObjectInstance gameObjectInstance, GameInstance gameInstance, Player player, GamePanel gamePanel)
    {
        this.gameObjectInstance = gameObjectInstance;
        this.gameInstance = gameInstance;

        if (gameObjectInstance.inHand != null && gameObjectInstance.inHand == player)
        {
            discardRecordItem.setText("Discard Card");
        }
        else
        {
            discardRecordItem.setText("Record Card");
        }


        flipItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CardStackFunctions.flipObject(gamePanel.id, gameInstance, player, gameObjectInstance);
            }
        });

        discardRecordItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (gameObjectInstance.inHand != null && gameObjectInstance.inHand == player)
                {
                    gameObjectInstance.inHand = null;
                }
                else
                {
                    gameObjectInstance.inHand = player;
                }

                gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player, gameObjectInstance));
            }
        });

        shuffleCardItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                /*Shuffle objects on a stack*/
                CardStackFunctions.shuffleStack(gamePanel.id, gameInstance, player, gameObjectInstance);
            }
        });

        flipStackItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CardStackFunctions.flipStack(gamePanel.id, gameInstance, player, gameObjectInstance);
            }
        });

        popup.add(flipItem);
        popup.add(discardRecordItem);
        if(CardStackFunctions.countStack(gameInstance, gameObjectInstance) > 1) {
            popup.add(shuffleCardItem);
            popup.add(flipStackItem);
        }
    }



    public void showPopup(MouseEvent arg0) {
        if(SwingUtilities.isRightMouseButton(arg0)) {
            popup.show(arg0.getComponent(),
                    arg0.getX(), arg0.getY());
        }
    }

}
