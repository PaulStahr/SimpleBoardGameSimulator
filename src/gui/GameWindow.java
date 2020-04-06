package gui;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.JFrame;

import gameObjects.instance.GameInstance;
import main.Player;

public class GameWindow extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1441104795154034811L;
	GameInstance gi;
	public GamePanel gamePanel;

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
		add(gamePanel);
		setLayout(new GridLayout(1, 1));
		setSize((int) screenSize.getWidth(), (int) screenSize.getHeight());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
}
