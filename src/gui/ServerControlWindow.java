package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;

import net.GameServer;

public class ServerControlWindow extends JFrame implements ActionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6664485369638777976L;
	private final JButton buttonStart = new JButton("Start");
	private final JButton buttonStop = new JButton("Stop");
	private final GameServer gs;
	public ServerControlWindow(GameServer gs)
	{
		this.gs = gs;
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
}
