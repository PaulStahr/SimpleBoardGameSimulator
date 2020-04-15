package gui;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import gameObjects.instance.GameInstance;
import main.Player;

public class GameWindow extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1441104795154034811L;
	GameInstance gi;
	JSplitPane slider;
	public GamePanel gamePanel;
	public IngameChatPanel chatPanel;

	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	
	public GameWindow(GameInstance gi)
	{
		this(gi, null);
	}
	
	public GameWindow(GameInstance gi, Player player)
	{
		this.gi = gi;

		gamePanel = new GamePanel(gi);
		gamePanel.player = player;
		chatPanel = new IngameChatPanel(gi, player);
		slider = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,gamePanel, chatPanel);
		slider.setOneTouchExpandable(true);
		slider.setResizeWeight(1); // the chat panel will not be resized when resizing the window
		this.add(slider);

		setLayout(new GridLayout(1, 1));
		setSize((int) screenSize.getWidth(), (int) screenSize.getHeight());
		slider.setDividerLocation(0.5);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
}
