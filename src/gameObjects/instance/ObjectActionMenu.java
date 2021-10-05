package gameObjects.instance;

import data.controls.ControlTypes;
import gameObjects.definition.*;
import gui.game.GameObjectActions;
import gui.game.GamePanel;
import gui.game.UserControlString;
import gui.language.Language;
import gui.language.LanguageChangeListener;
import gui.language.LanguageHandler;
import gui.language.Words;
import main.Player;
import util.data.IntegerArrayList;

import javax.swing.*;
import java.awt.event.*;

public class ObjectActionMenu implements LanguageChangeListener {
    /*Popup menu for object actions*/
    public JPopupMenu popup = new JPopupMenu();
    UserControlString uc = new UserControlString();
    JTable objectControlsTable = new JTable(uc.objectControls.length, 2);



    public ObjectInstance gameObjectInstance;
    public GameInstance gameInstance;

    public ObjectActionMenu(ObjectInstance objectInstance, GameInstance gameInstance, Player player, GamePanel gamePanel)
    {
        this.gameObjectInstance = objectInstance;
        this.gameInstance = gameInstance;
        LanguageHandler lh = gamePanel.lh;
        languageChanged(lh.getCurrentLanguage());
        lh.addLanguageChangeListener(this);

        JLabel label = new JLabel(lh.getCurrentLanguage().getString(Words.object_controls));
        popup.add(label);
        popup.addSeparator();
        for (UserControlString.FuncControl control : uc.objectControls) {
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
                    }
                });
            }
        }
        
        if (this.gameObjectInstance.go instanceof GameObjectToken) {
            popup.addSeparator();
            label = new JLabel(lh.getCurrentLanguage().getString(Words.card_controls));
            popup.add(label);
            popup.addSeparator();
                    for (UserControlString.FuncControl control : uc.cardControls) {
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
                                }
                            });
                        }
                    }
        }
        else if (this.gameObjectInstance.go instanceof GameObjectFigure){
            popup.addSeparator();
            label = new JLabel(lh.getCurrentLanguage().getString(Words.figure_controls));
            popup.add(label);
            popup.addSeparator();
            for (UserControlString.FuncControl control : uc.figureControls) {
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
                        }
                    });
                }
            }
        }
        else if (this.gameObjectInstance.go instanceof GameObjectDice){
            popup.addSeparator();
            label = new JLabel(lh.getCurrentLanguage().getString(Words.dice_controls));
            popup.add(label);
            popup.addSeparator();
            for (UserControlString.FuncControl control : uc.diceControls) {
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
                        }
                    });
                }
            }
        }
        else if (this.gameObjectInstance.go instanceof GameObjectBox){
            popup.addSeparator();
            label = new JLabel(lh.getCurrentLanguage().getString(Words.box_controls));
            popup.add(label);
            popup.addSeparator();
            for (UserControlString.FuncControl control : uc.boxControls) {
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
                        }
                    });
                }
            }
        }
        else if (this.gameObjectInstance.go instanceof GameObjectBook){
            popup.addSeparator();
            label = new JLabel(lh.getCurrentLanguage().getString(Words.book_controls));
            popup.add(label);
            popup.addSeparator();
            for (UserControlString.FuncControl control : uc.bookControls) {
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
                        }
                    });
                }
            }
        }
    }

    @Override
    public void languageChanged(Language lang) {
        String[] columnNames = {lang.getString(Words.description), lang.getString(Words.key)};
        for (int col = 0; col < 2; ++col)
        {
            objectControlsTable.getColumnModel().getColumn(col).setHeaderValue(columnNames[col]);
        }
        int row = 0;
        for (UserControlString.FuncControl control : uc.objectControls)
        {
            objectControlsTable.getModel().setValueAt(lang.getString(control.word), row, 0);
            objectControlsTable.getModel().setValueAt(control.toString(lang), row, 1);
            ++row;
        }
    }


    public void showPopup(MouseEvent arg0) {
        if(SwingUtilities.isRightMouseButton(arg0)) {
            popup.show(arg0.getComponent(),
                    arg0.getX(), arg0.getY());
        }
    }

}
