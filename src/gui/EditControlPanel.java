package gui;


import java.awt.EventQueue;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Document;

import gameObjects.GameObjectColumnType;
import gameObjects.GameObjectInstanceColumnType;
import gameObjects.ImageColumnType;
import gameObjects.PlayerColumnType;
import gameObjects.action.AddObjectAction;
import gameObjects.action.GameAction;
import gameObjects.action.GameObjectEditAction;
import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.action.GamePlayerEditAction;
import gameObjects.action.GameStructureEditAction;
import gameObjects.definition.GameObject;
import gameObjects.definition.GameObjectToken;
import gameObjects.instance.GameInstance;
import gameObjects.instance.GameInstance.GameChangeListener;
import gameObjects.instance.ObjectInstance;
import main.Player;
import util.ArrayTools;
import util.JFrameUtils;
import util.jframe.table.ButtonColumn;
import util.jframe.table.TableColumnType;
import util.jframe.table.TableModel;

public class EditControlPanel extends JPanel implements ActionListener, GameChangeListener, Runnable, MouseListener, TableModelListener, LanguageChangeListener{


    private String[] columnNames = {"Description", "Key"};

    private Object[][] boardControls = {{"Move Board", "Strg + Left Mouse Button"},
            {"Rotate Board", "Strg + Right Mouse Button"},
            {"Zoom", "Strg + Mouse Wheel"},
    };
    JLabel boardControlsLabel = new JLabel("Board Controls");
    JTable boardControlsTable = new JTable(boardControls, columnNames);
    JScrollPane boardControlPane = new JScrollPane(boardControlsTable);

    private Object[][] objectControls = {{"Move Object", "Click + Drag"},
            {"Move Stack", "Click Mouse Wheel + Drag"},
            {"Get Top n Card", "Rotate Mouse Wheel + Click Mouse Wheel + Drag"},
            {"Get Bottom Card", "Shift + Click + Drag"},
            {"Rotate object", "R"},
            {"Select objects", "Click  + Drag"},
            {"Shuffle Stack", "S"},
            {"Merge objects", "M"},
            {"Collect all objects of a group", "Strg + M"},
            {"Flip objects/Roll dice", "F"},
    };
    JLabel objectControlsLabel = new JLabel("Object Controls");
    JTable objectControlsTable = new JTable(objectControls, columnNames);
    JScrollPane objectControlsPane = new JScrollPane(objectControlsTable);

    private Object[][] privateAreaControls = {{"Take Objects to Hand Cards", "T, Left Mouse + Drag"},
            {"Take Objects to Hand Cards face down", "Right Mouse + Drag"},
            {"Play card face up", "Left Mouse + Drop Outside Hand Cards"},
            {"Play card face down", "Right Mouse + Drop Outside Hand Cards"},
            {"Drop All Hand Cards", "D"},
    };
    JLabel privateAreaControlsLabel = new JLabel("Control Hand Cards");
    JTable privateAreaControlsTable = new JTable(privateAreaControls, columnNames);
    JScrollPane privateAreaControlsPane = new JScrollPane(privateAreaControlsTable);


    private Object[][] countControls = {{"Count Card Number", "C"},
            {"Count Card Value", "Strg + C"},
    };
    JLabel countControlsLabel = new JLabel("Control Hand Cards");
    JTable countControlsTable = new JTable(countControls, columnNames);
    JScrollPane countControlsPane = new JScrollPane(countControlsTable);
    //keyAssignmentTable.setFillsViewportHeight(true);

    public EditControlPanel(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(boardControlsLabel);
        this.add(boardControlPane);
        this.add(objectControlsLabel);
        this.add(objectControlsPane);
        this.add(privateAreaControlsLabel);
        this.add(privateAreaControlsPane);
        this.add(countControlsLabel);
        this.add(countControlsPane);
    }
    @Override
    public void changeUpdate(GameAction action) {

    }

    @Override
    public void languageChanged(Language language) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void run() {

    }

    @Override
    public void tableChanged(TableModelEvent e) {

    }
}

