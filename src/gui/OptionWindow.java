package gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager.LookAndFeelInfo;

import data.JFrameLookAndFeelUtil;
import data.Options;
import gui.Language.LanguageSummary;
import util.JFrameUtils;

public class OptionWindow extends JFrame implements LanguageChangeListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7299671775874755731L;
	private final JButton buttonOk = new JButton();
	private final JButton buttonCancel = new JButton();
	private final JButton buttonAccept = new JButton();
	private final JPanel panelSettings = new JPanel();
	private final JLabel labelInvertRotation = new JLabel();
	private final JCheckBox checkBoxInvertRotation = new JCheckBox();
	private final JLabel labelLayoutmanager = new JLabel();
	private final JComboBox<String> comboBoxLayoutManager = new JComboBox<>();
    private final JLabel labelLanguage = new JLabel();
	private final JComboBox<LanguageSummary> comboBoxLanguages = new JComboBox<>();
	private final JLabel labelShowPing = new JLabel();
	private final JCheckBox checkBoxShowPing = new JCheckBox();
	private final JLabel labelDebug = new JLabel();
	private final JCheckBox checkBoxDebug = new JCheckBox();
	private final LanguageHandler lh;
	
	public OptionWindow(LanguageHandler lh)
	{
		comboBoxLanguages.setModel(new DefaultComboBoxModel<LanguageSummary>(lh.getLanguages()));
		comboBoxLanguages.setSelectedItem(lh.getCurrentSummary());
		buttonOk.addActionListener(this);
		buttonCancel.addActionListener(this);
		buttonAccept.addActionListener(this);
		this.lh = lh;
		lh.addLanguageChangeListener(this);
		languageChanged(lh.getCurrentLanguage());
		
		checkBoxShowPing.setSelected(Options.getBoolean("gui.show_ping", false));
		checkBoxDebug.setSelected(Options.getBoolean("debug", false));
		
		panelSettings.setLayout(JFrameUtils.DOUBLE_COLUMN_LAUYOUT);
		panelSettings.add(labelLanguage);
		panelSettings.add(comboBoxLanguages);
		panelSettings.add(labelInvertRotation);
		panelSettings.add(checkBoxInvertRotation);
		panelSettings.add(labelLayoutmanager);
		panelSettings.add(comboBoxLayoutManager);
		panelSettings.add(labelShowPing);
		panelSettings.add(checkBoxShowPing);
		panelSettings.add(labelDebug);
		panelSettings.add(checkBoxDebug);
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(panelSettings).addGroup(layout.createSequentialGroup().addComponent(buttonOk).addComponent(buttonAccept).addComponent(buttonCancel)));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(panelSettings).addGroup(layout.createParallelGroup().addComponent(buttonOk).addComponent(buttonAccept).addComponent(buttonCancel)));
		languageChanged(lh.getCurrentLanguage());
		for (LookAndFeelInfo lookAndFeelInfo : JFrameLookAndFeelUtil.installedLookAndFeels)
			comboBoxLayoutManager.addItem(lookAndFeelInfo.getName());
		JFrameLookAndFeelUtil.addToUpdateTree(this);
		setSize(400,400);
	}
	
	@Override
	public void languageChanged(Language language) {
		buttonOk.setText(language.getString(Words.ok));
		buttonAccept.setText(language.getString(Words.accept));
		buttonCancel.setText(language.getString(Words.cancel));
		labelLanguage.setText(language.getString(Words.language));
		labelInvertRotation.setText(language.getString(Words.invert_rotation));
		labelShowPing.setText(language.getString(Words.show_ping));
		labelDebug.setText(language.getString(Words.debug));
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == buttonAccept || source == buttonOk)
		{
			lh.setCurrentLanguage((LanguageSummary)comboBoxLanguages.getSelectedItem());
        	EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
		        	JFrameLookAndFeelUtil.setLookAndFeel(JFrameLookAndFeelUtil.installedLookAndFeels.get(comboBoxLayoutManager.getSelectedIndex()));
				}
			});
            Options.set("layout_manager", JFrameLookAndFeelUtil.installedLookAndFeels.get(comboBoxLayoutManager.getSelectedIndex()).getClassName());
            Options.set("invert_rotation", checkBoxInvertRotation.isSelected());
            Options.set("gui.show_ping", checkBoxShowPing.isSelected());
            Options.set("debug", checkBoxDebug.isSelected());
            Options.triggerUpdates();
		}
		if (source == buttonOk || source == buttonCancel)
		{
			dispose();
		}
	}
}
