package gui.game.edit;

import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import gameObjects.definition.GameObject;
import gameObjects.definition.GameObjectToken;
import gameObjects.instance.GameInstance;
import gui.Language;
import gui.LanguageChangeListener;
import gui.LanguageHandler;
import gui.Words;
import util.JFrameUtils;

public class ObjectEditPanel extends JPanel implements DocumentListener, ItemListener, LanguageChangeListener{
    /**
     * 
     */
    private static final long serialVersionUID = -1555265079401333395L;
    private final JLabel labelName = new JLabel();
    private final JTextField textFieldName = new JTextField();
    private final JLabel labelWidth = new JLabel();
    private final JTextField textFieldWidth = new JTextField();
    private final JLabel labelHeight = new JLabel();
    private final JTextField textFieldHeight = new JTextField();
    private final GameObject go;
    private final GameInstance gi;
    private final ArrayList<JComboBox<String>> imageComboBoxes = new ArrayList<>();
    private final JComboBox<String> comboBoxFrontImage;
    private final JComboBox<String> comboBoxBackImage;
    
    private void updateImages()
    {
        String imageNames[] = gi.game.images.keySet().toArray(new String[gi.game.images.size()]);
        for (int i = 0; i < imageComboBoxes.size(); ++i)
        {
            JFrameUtils.updateComboBox(imageComboBoxes.get(i), imageNames);
        }
    }

    public static GameObject reduce(List<GameObject> go) {
        GameObject first = go.get(0).copy();
        for (int i = 1; i < go.size(); ++i)
        {
            GameObject current = go.get(i);
            if (!current.uniqueObjectName.equals(first.uniqueObjectName)){first.uniqueObjectName = null;}
            if (current.widthInMM != first.widthInMM)                    {first.widthInMM = Integer.MIN_VALUE;}             
            if (current.heightInMM != first.heightInMM)                  {first.heightInMM = Integer.MIN_VALUE;}             
        }
        return first;
    }

    public ObjectEditPanel(GameObject go, GameInstance gi, LanguageHandler lh)
    {
        this.gi = gi;
        assert (EventQueue.isDispatchThread());
        //GroupLayout layout = new GroupLayout(this);
        //setLayout(layout);YES_NO_OPTION
        setLayout(JFrameUtils.DOUBLE_COLUMN_LAUYOUT);
        this.go = go;
        textFieldName.setText(go.uniqueObjectName);
        textFieldWidth.setText(go.widthInMM == Integer.MIN_VALUE ? "" : Integer.toString(go.widthInMM));
        textFieldHeight.setText(go.heightInMM == Integer.MIN_VALUE ? "" : Integer.toString(go.heightInMM));
        textFieldWidth.getDocument().addDocumentListener(this);
        textFieldHeight.getDocument().addDocumentListener(this);
        
        add(labelName);
        add(textFieldName);
        add(labelWidth);
        add(textFieldWidth);
        add(labelHeight);
        add(textFieldHeight);
        
        /*layout.setHorizontalGroup(
                layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup().addComponent(labelName).addComponent(labelWidth).addComponent(labelHeight))
                .addGroup(layout.createParallelGroup().addComponent(textFieldName).addComponent(textFieldWidth).addComponent(textFieldHeight)));
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup().addComponent(labelName).addComponent(textFieldName))
                .addGroup(layout.createParallelGroup().addComponent(labelWidth).addComponent(textFieldWidth))
                .addGroup(layout.createParallelGroup().addComponent(labelHeight).addComponent(textFieldHeight)));*/
        
        if(go instanceof GameObjectToken)
        {
            JLabel labelFrontImage = new JLabel("Front Image");
            JLabel labelBackImage = new JLabel("Back Image");
            comboBoxFrontImage = new JComboBox<String>();
            comboBoxBackImage = new JComboBox<String>();
            imageComboBoxes.add(comboBoxFrontImage);
            imageComboBoxes.add(comboBoxBackImage);
            updateImages();
            GameObjectToken token = (GameObjectToken)go;
            comboBoxFrontImage.setSelectedItem(gi.game.getImageKey(token.getUpsideLook()));
            comboBoxBackImage.setSelectedItem(gi.game.getImageKey(token.getDownsideLook()));
            comboBoxFrontImage.addItemListener(this);
            add(labelFrontImage);
            add(comboBoxFrontImage);
            add(labelBackImage);
            add(comboBoxBackImage);
        }
        else
        {
            comboBoxFrontImage = null;
            comboBoxBackImage = null;
        }
        languageChanged(lh.getCurrentLanguage());
        lh.addLanguageChangeListener(this);
    }
    
    public void apply() {
        
    }
    
    @Override
    public void changedUpdate(DocumentEvent e) {
        Document source = e.getDocument();
        if       (source == textFieldWidth.getDocument())    {go.widthInMM = Integer.parseInt(textFieldWidth.getText());}
        else if  (source == textFieldHeight.getDocument())   {go.heightInMM = Integer.parseInt(textFieldHeight.getText());}
    }
    @Override
    public void insertUpdate(DocumentEvent e) {changedUpdate(e);}
    @Override
    public void removeUpdate(DocumentEvent e) {changedUpdate(e);}

    @Override
    public void itemStateChanged(ItemEvent event) {
        Object source = event.getSource();
        if (go instanceof GameObjectToken)
        {
            GameObjectToken got = (GameObjectToken)go;
            if (source == comboBoxFrontImage)   {got.setUpsideLook((String)comboBoxFrontImage.getSelectedItem());}
            if (source == comboBoxBackImage)    {got.setDownsideLook((String)comboBoxBackImage.getSelectedItem());}
        }
    }

    @Override
    public void languageChanged(Language language) {
        labelName.setText(language.getString(Words.name));
        labelWidth.setText(language.getString(Words.width));
        labelHeight.setText(language.getString(Words.height));
    }
}
