package main.gameObjects.instance;

import javax.swing.*;
import java.awt.event.MouseEvent;

public class ObjectScrollMenu {

    /*Popup menu for object actions*/
    public JPopupMenu popup = new JPopupMenu();

    public ObjectScrollMenu(int number)
    {}

    public void showPopup(MouseEvent arg0) {

            popup.show(arg0.getComponent(),
                    arg0.getX(), arg0.getY());
    }


}
