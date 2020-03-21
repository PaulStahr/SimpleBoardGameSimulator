package gameObjects.instance;

import gameObjects.definition.GameObject;
import gameObjects.definition.GameObjectCard;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class ObjectActionMenu {
    /*Popup menu for object actions*/
    public JPopupMenu popup = new JPopupMenu();

    public JMenuItem menuItem = new JMenuItem("Flip Card");

    public ObjectInstance gameObject;

    public ObjectActionMenu(ObjectInstance gameObject)
    {
        this.gameObject = gameObject;

        menuItem.setMnemonic(KeyEvent.VK_P);
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Flip Card");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	((GameObjectCard.CardState)gameObject.state).side = !((GameObjectCard.CardState)gameObject.state).side;
            }
        });
        popup.add(menuItem);
    }



    public void showPopup(MouseEvent arg0) {
        if(SwingUtilities.isRightMouseButton(arg0)) {
            popup.show(arg0.getComponent(),
                    arg0.getX(), arg0.getY());
        }
    }

}
