package gui;
import java.awt.GridLayout;

import javax.swing.JFrame;

import gameObjects.instance.GameInstance;

public class GameWindow extends JFrame{
	GameInstance gi;
	GamePanel gamePanel;
	
	public GameWindow(GameInstance gi)
	{
		this.gi = gi;
		gamePanel = new GamePanel(gi);
		add(gamePanel);
		setLayout(new GridLayout(1, 1));
		setSize(500,500);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	
}
