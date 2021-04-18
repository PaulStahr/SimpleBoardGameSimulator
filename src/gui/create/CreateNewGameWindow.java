package gui.create;

import gui.designs.ObjectAddForm;
import gui.language.Language;
import gui.language.LanguageChangeListener;
import gui.language.LanguageHandler;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.TitledBorder;

public class CreateNewGameWindow extends JFrame implements LanguageChangeListener, ActionListener {
    private static final long serialVersionUID = 7299671775874755731L;
    private final LanguageHandler lh;
    private final JPanel panel5 = new JPanel();
    private final JTextField gameName = new JTextField();
    private final JLabel gameNameLabel = new JLabel();
    private final JLabel label1 = new JLabel();
    private final JPanel table = new JPanel();
    private final JLabel label2 = new JLabel();
    private final JTextField tableSize = new JTextField();
    private final JLabel label3 = new JLabel();
    private final JTextField tableColor = new JTextField();
    private final JLabel label4 = new JLabel();
    private final JCheckBox stackerVisible = new JCheckBox();
    private final JLabel label5 = new JLabel();
    private final JSpinner chairNumber = new JSpinner();
    private final JLabel label6 = new JLabel();
    private final JCheckBox tableVisible = new JCheckBox();
    private final JPanel panel2 = new JPanel();
    private final JPanel panel3 = new JPanel();
    private final JLabel label7 = new JLabel();
    private final JCheckBox handCardAreaVisible = new JCheckBox();
    private final JPanel panel4 = new JPanel();
    private final JScrollPane scrollPane1 = new JScrollPane();
    private final JList objectList = new JList();
    private final JButton addObjectButton = new JButton();
    private final JButton createGameButton = new JButton();


    public CreateNewGameWindow(LanguageHandler lh) {
        this.lh = lh;
        lh.addLanguageChangeListener(this);
        languageChanged(lh.getCurrentLanguage());
        languageChanged(lh.getCurrentLanguage());
        InputLayout();
        Dimension ScreenDimension = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(ScreenDimension.width / 3, ScreenDimension.height / 2);
        setLayout(new BorderLayout());
        add(panel5, BorderLayout.CENTER);
    }

    public void InputLayout(){
        panel5.setLayout(new GridBagLayout());
        gameName.setText("NewGame");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(gameName, gbc);
        gameNameLabel.setText("GameName");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel5.add(gameNameLabel, gbc);
        final JLabel label8 = new JLabel();
        label8.setText("Background");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel5.add(label8, gbc);
        table.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(table, gbc);
        table.setBorder(BorderFactory.createTitledBorder(null, "Table Options", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label9 = new JLabel();
        label9.setText("Size");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        table.add(label9, gbc);
        tableSize.setText("700");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        table.add(tableSize, gbc);
        final JLabel label10 = new JLabel();
        label10.setText("Color");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        table.add(label10, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        table.add(tableColor, gbc);
        final JLabel label11 = new JLabel();
        label11.setText("Ablagebereich");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        table.add(label11, gbc);
        stackerVisible.setSelected(true);
        stackerVisible.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        table.add(stackerVisible, gbc);
        final JLabel label12 = new JLabel();
        label12.setText("Chairs");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        table.add(label12, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        table.add(chairNumber, gbc);
        final JLabel label13 = new JLabel();
        label13.setText("Visible");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        table.add(label13, gbc);
        tableVisible.setSelected(true);
        tableVisible.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        table.add(tableVisible, gbc);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(panel6, gbc);
        panel6.setBorder(BorderFactory.createTitledBorder(null, "Drop Background Image", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(panel7, gbc);
        panel7.setBorder(BorderFactory.createTitledBorder(null, "Hand Card Area", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label14 = new JLabel();
        label14.setText("Visible");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel7.add(label14, gbc);
        handCardAreaVisible.setSelected(true);
        handCardAreaVisible.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel7.add(handCardAreaVisible, gbc);
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel5.add(panel8, gbc);
        panel8.setBorder(BorderFactory.createTitledBorder(null, "Game Objects", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JScrollPane scrollPane2 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel8.add(scrollPane2, gbc);
        scrollPane2.setViewportView(objectList);
        addObjectButton.setText("Add Object");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        panel8.add(addObjectButton, gbc);
        createGameButton.setText("Create Game");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        panel8.add(createGameButton, gbc);
    }

    @Override
    public void languageChanged(Language language) {
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == "") {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                }
            });
        }
        else if (source == addObjectButton){
            ObjectAddForm ow = new ObjectAddForm(lh);
            ow.setVisible(true);
        }
    }
}

class ObjectTransferHandler extends TransferHandler{

    public ObjectTransferHandler(){

    }
}

