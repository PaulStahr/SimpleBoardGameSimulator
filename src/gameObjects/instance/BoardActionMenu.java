package gameObjects.instance;

import data.controls.ControlTypes;
import geometry.Vector2d;
import gui.game.GameObjectActions;
import gui.game.GamePanel;
import gui.game.UserControlString;
import gui.language.Language;
import gui.language.LanguageChangeListener;
import gui.language.LanguageHandler;
import gui.language.Words;
import util.data.IntegerArrayList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;


public class BoardActionMenu  implements LanguageChangeListener {

    /*Popup menu for object actions*/
    public JPopupMenu popup = new JPopupMenu();
    UserControlString uc = new UserControlString();

    public ObjectInstance gameObjectInstance;
    public GameInstance gameInstance;
    public Vector2d menuPosition = new Vector2d();
    private Component invoker;

    public BoardActionMenu(GamePanel gamePanel){
        LanguageHandler lh = gamePanel.lh;
        languageChanged(lh.getCurrentLanguage());
        lh.addLanguageChangeListener(this);

        JLabel label = new JLabel(lh.getCurrentLanguage().getString(Words.board_controls));
        popup.add(label);
        popup.addSeparator();
        for (UserControlString.FuncControl control : uc.boardControls) {
            JMenuItem item = new JMenuItem(lh.getCurrentLanguage().getString(control.word));
            item.setEnabled(control.hasAction);
            KeyStroke keyStroke = null;
            if (control.cc.length > 0) {
                keyStroke = control.cc[0].toKeyStroke();
            }
            if (keyStroke != null) {
                //item.setAccelerator(keyStroke);
            }
            item.setToolTipText(control.toString(lh.getCurrentLanguage()));
            popup.add(item);
            ControlTypes controlTypes = control.controlTypes;
            if (controlTypes != null && control.hasAction) {
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        IntegerArrayList ial = new IntegerArrayList();
                        GameObjectActions.RunAction(controlTypes, gamePanel, ial);
                        popup.show(invoker, menuPosition.getXI(), menuPosition.getYI());
                    }
                });
            }
        }
        popup.addSeparator();
        label = new JLabel(lh.getCurrentLanguage().getString(Words.hand_card_area_controls));
        popup.add(label);
        popup.addSeparator();
        for (UserControlString.FuncControl control : uc.privateAreaControls) {
            JMenuItem item = new JMenuItem(lh.getCurrentLanguage().getString(control.word));
            item.setEnabled(control.hasAction);
            KeyStroke keyStroke = null;
            if (control.cc.length > 0) {
                keyStroke = control.cc[0].toKeyStroke();
            }
            if (keyStroke != null) {
                //item.setAccelerator(keyStroke);
            }
            item.setToolTipText(control.toString(lh.getCurrentLanguage()));
            popup.add(item);
            ControlTypes controlTypes = control.controlTypes;
            if (controlTypes != null && control.hasAction) {
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        IntegerArrayList ial = new IntegerArrayList();
                        GameObjectActions.RunAction(controlTypes, gamePanel, ial);
                        popup.show(invoker, menuPosition.getXI(), menuPosition.getYI());
                    }
                });
            }
        }
        
    }
    
    @Override
    public void languageChanged(Language language) {
        
    }

    public void showPopup(MouseEvent arg0) {
        if(SwingUtilities.isRightMouseButton(arg0)) {
            menuPosition.set(arg0.getX(), arg0.getY());
            invoker = arg0.getComponent();
            popup.show(invoker, menuPosition.getXI(), menuPosition.getYI());
        }
    }


}
