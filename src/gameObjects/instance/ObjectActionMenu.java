package gameObjects.instance;

import gameObjects.GameObjectInstanceEditAction;
import gameObjects.definition.GameObject;
import gameObjects.definition.GameObjectCard;
import main.Player;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class ObjectActionMenu {
    /*Popup menu for object actions*/
    public JPopupMenu popup = new JPopupMenu();

    public JMenuItem flipItem = new JMenuItem("Flip Card");
    public JMenuItem discardRecordItem = new JMenuItem("");

    public ObjectInstance gameObjectInstance;
    public GameInstance gameInstance;

    public ObjectActionMenu(ObjectInstance gameObject, GameInstance gameInstance, Player player)
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

        flipItem.getAccessibleContext().setAccessibleDescription("Flip Card");
        flipItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((GameObjectCard.CardState)gameObjectInstance.state).side = !((GameObjectCard.CardState)gameObjectInstance.state).side;
                gameInstance.update(new GameObjectInstanceEditAction(0, null, gameObjectInstance));
            }
        });

        discardRecordItem.getAccessibleContext().setAccessibleDescription("Discard Record Card");
        discardRecordItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (gameObjectInstance.inHand == player)
                {
                    gameObjectInstance.inHand = null;
                }
                else
                {
                    gameObjectInstance.inHand = player;
                }

                gameInstance.update(new GameObjectInstanceEditAction(0, null, gameObjectInstance));
            }
        });

        popup.add(flipItem);
        popup.add(discardRecordItem);
    }



    public void showPopup(MouseEvent arg0) {
        if(SwingUtilities.isRightMouseButton(arg0)) {
            popup.show(arg0.getComponent(),
                    arg0.getX(), arg0.getY());
        }
    }

}
