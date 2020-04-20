package main.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;

import main.net.GameServer;

public class ServerControlWindow extends JFrame implements ActionListener, Runnable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6664485369638777976L;
	private final JButton buttonStart = new JButton("Start");
	private final JButton buttonStop = new JButton("Stop");
	private Thread th;
	private GameServer gs = new GameServer(1234);
	public ServerControlWindow()
	{
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
			if (th != null)
			{
				th = new Thread(this);
				th.start();
			}
		}
		else if (source == buttonStop)
		{
			th = null;
		}
	}
	@Override
	public void run() {
		gs.start();
	}
}
