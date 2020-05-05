package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import gui.Language.LanguageSummary;
import util.JFrameUtils;

public class OptionWindow extends JFrame implements LanguageChangeListener, ActionListener {
	private final JButton buttonOk = new JButton();
	private final JButton buttonCancel = new JButton();
	private final JButton buttonAccept = new JButton();
	private final JLabel labelLanguage = new JLabel();
	private final JPanel panelSettings = new JPanel();
	private final JLabel labelLayoutmanager = new JLabel();
	private final JComboBox<String> comboBoxLayoutManager = new JComboBox<>();
	private final JComboBox<LanguageSummary> comboBoxLanguages = new JComboBox<>();
	private final LanguageHandler lh;
	
	public OptionWindow(LanguageHandler lh)
	{
		comboBoxLanguages.setModel(new DefaultComboBoxModel(lh.getLanguages()));
		buttonOk.addActionListener(this);
		buttonCancel.addActionListener(this);
		buttonAccept.addActionListener(this);
		this.lh = lh;
		panelSettings.setLayout(JFrameUtils.DOUBLE_COLUMN_LAUYOUT);
		panelSettings.add(labelLanguage);
		panelSettings.add(comboBoxLanguages);
		panelSettings.add(labelLayoutmanager);
		panelSettings.add(comboBoxLayoutManager);
		comboBoxLanguages.setSelectedItem(lh.getCurrentSummary());
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		
		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(panelSettings).addGroup(layout.createSequentialGroup().addComponent(buttonOk).addComponent(buttonAccept).addComponent(buttonCancel)));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(panelSettings).addGroup(layout.createParallelGroup().addComponent(buttonOk).addComponent(buttonAccept).addComponent(buttonCancel)));
		languageChanged(lh.getCurrentLanguage());
		setSize(400,400);
	}
	
	@Override
	public void languageChanged(Language language) {
		buttonOk.setText(language.getString(Words.ok));
		buttonAccept.setText(language.getString(Words.accept));
		buttonCancel.setText(language.getString(Words.cancel));
		labelLanguage.setText(language.getString(Words.language));
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == buttonAccept || source == buttonOk)
		{
			lh.setCurrentLanguage((LanguageSummary)comboBoxLanguages.getSelectedItem());
		}
		if (source == buttonOk || source == buttonCancel)
		{
			dispose();
		}
	}
}
