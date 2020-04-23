package gameObjects.instance;

import gameObjects.GameObjectInstanceEditAction;
import gameObjects.functions.ObjectFunctions;
import gui.GamePanel;
import main.Player;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

public class ObjectActionMenu {
    /*Popup menu for object actions*/
    public JPopupMenu popup = new JPopupMenu();

    public JMenuItem flipItem = new JMenuItem("Flip Card");
    public JMenuItem discardRecordItem = new JMenuItem("");
    public JMenuItem shuffleCardItem = new JMenuItem("Shuffle Stack");
    public JMenuItem flipStackItem = new JMenuItem("Flip Stack");

    public ObjectInstance gameObjectInstance;
    public GameInstance gameInstance;

    public ObjectActionMenu(ObjectInstance objectInstance, GameInstance gameInstance, Player player, GamePanel gamePanel)
    {
        this.gameObjectInstance = objectInstance;
        this.gameInstance = gameInstance;

        if (objectInstance.state.owner_id != -1 && objectInstance.state.owner_id == player.id)
        {
            discardRecordItem.setText("Discard Card");
        }
        else
        {
            discardRecordItem.setText("Record Card");
        }


        flipItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ObjectFunctions.flipTokenObject(gamePanel.id, gameInstance, player, objectInstance);
            }
        });

        discardRecordItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (objectInstance.state.owner_id != -1 && objectInstance.state.owner_id == player.id)
                {
                    objectInstance.state.owner_id = -1;
                }
                else
                {
                    objectInstance.state.owner_id = player.id;
                }

                gameInstance.update(new GameObjectInstanceEditAction(gamePanel.id, player, objectInstance));
            }
        });

        shuffleCardItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                /*Shuffle objects on a stack*/
                ObjectFunctions.shuffleStack(gamePanel.id, gameInstance, player, objectInstance);
            }
        });

        flipStackItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ObjectFunctions.flipTokenStack(gamePanel.id, gameInstance, player, objectInstance);
            }
        });

        popup.add(flipItem);
        popup.add(discardRecordItem);
        if(ObjectFunctions.countStack(gameInstance, objectInstance) > 1) {
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
