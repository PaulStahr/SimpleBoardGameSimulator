package gameObjects.instance;

import gameObjects.GameObjectInstanceEditAction;
import gameObjects.definition.GameObject;
import gameObjects.definition.GameObjectCard;
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
    public JMenuItem shuffleCardItem = new JMenuItem("Shuffle Cards");

    public ObjectInstance gameObjectInstance;
    public GameInstance gameInstance;

    public ObjectActionMenu(ObjectInstance gameObjectInstance, GameInstance gameInstance, Player player)
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
                gameInstance.update(new GameObjectInstanceEditAction(-1, player, gameObjectInstance));
            }
        });

        discardRecordItem.getAccessibleContext().setAccessibleDescription("Discard Record Card");
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

                gameInstance.update(new GameObjectInstanceEditAction(-1, player, gameObjectInstance));
            }
        });

        shuffleCardItem.getAccessibleContext().setAccessibleDescription("ShuffleCards");
        shuffleCardItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                /*Shuffle objects on a stack*/
                IntegerArrayList objectStack = new IntegerArrayList();
                objectStack.add(gameObjectInstance.id);
                ObjectInstance currentObjectInstance = gameObjectInstance;
                while (currentObjectInstance.state.belowInstanceId != -1)
                {
                    objectStack.add(currentObjectInstance.state.belowInstanceId);
                    currentObjectInstance =  gameInstance.objects.get(currentObjectInstance.state.belowInstanceId);
                }
                Collections.shuffle(objectStack);
                for (int i = 0; i < objectStack.size(); i++)
                {
                    ObjectInstance currentObject = gameInstance.objects.get(objectStack.get(i));
                    if (i==0 && i<objectStack.size()-1){
                        currentObject.state.belowInstanceId = -1;
                        currentObject.state.aboveInstanceId = gameInstance.objects.get(i+1).id;
                    }
                    else if(i==objectStack.size()-1){
                        currentObject.state.belowInstanceId = gameInstance.objects.get(i-1).id;
                        currentObject.state.aboveInstanceId = -1;
                    }
                    else{
                        currentObject.state.belowInstanceId = gameInstance.objects.get(i-1).id;
                        currentObject.state.aboveInstanceId = gameInstance.objects.get(i+1).id;
                    }
                    gameInstance.update(new GameObjectInstanceEditAction(-1, player, gameObjectInstance));
                }

            }
        });

        popup.add(flipItem);
        popup.add(discardRecordItem);
        popup.add(shuffleCardItem);
    }



    public void showPopup(MouseEvent arg0) {
        if(SwingUtilities.isRightMouseButton(arg0)) {
            popup.show(arg0.getComponent(),
                    arg0.getX(), arg0.getY());
        }
    }

}
