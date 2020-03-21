package gameObjects.instance;

import gameObjects.definition.GameObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class ObjectActionMenu {
    /*Popup menu for object actions*/
    public JPopupMenu popup = new JPopupMenu();

    public JMenuItem menuItem = new JMenuItem("Flip Card");

    public GameObject gameObject;

    public ObjectActionMenu(GameObject gameObject)
    {
        this.gameObject = gameObject;

        menuItem.setMnemonic(KeyEvent.VK_P);
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Flip Card");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gameObject.getLook(gameObject.newObjectState());
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
