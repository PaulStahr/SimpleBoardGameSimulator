package gui.server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;

import gui.language.Language;
import gui.language.LanguageChangeListener;
import gui.language.LanguageHandler;
import net.GameServer;

public class ServerControlWindow extends JFrame implements ActionListener, LanguageChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6664485369638777976L;
	private final JButton buttonStart = new JButton("Start");
	private final JButton buttonStop = new JButton("Stop");
	private final GameServer gs;
	private final LanguageHandler lh;
	public ServerControlWindow(GameServer gs, LanguageHandler lh)
	{
		this.gs = gs;
		this.lh = lh;
		lh.addLanguageChangeListener(this);
		languageChanged(lh.getCurrentLanguage());
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		
		layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(buttonStart).addComponent(buttonStop));
		layout.setVerticalGroup(layout.createParallelGroup().addComponent(buttonStart).addComponent(buttonStop));
		
		buttonStart.addActionListener(this);
		buttonStop.addActionListener(this);
		setSize(200,100);
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object source = arg0.getSource();
		if (source == buttonStart)
		{
			gs.start();
		}
		else if (source == buttonStop)
		{
			gs.stop();
		}
	}

	@Override
	public void languageChanged(Language language) {

	}
}
