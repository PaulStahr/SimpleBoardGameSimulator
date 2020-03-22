package gui;
import java.awt.*;

import javax.swing.JFrame;

import gameObjects.instance.GameInstance;

public class GameWindow extends JFrame{
	GameInstance gi;
	public GamePanel gamePanel;

	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	
	public GameWindow(GameInstance gi)
	{
		this.gi = gi;
		gamePanel = new GamePanel(gi);
		add(gamePanel);
		setLayout(new GridLayout(1, 1));
		setSize((int) screenSize.getWidth(), (int) screenSize.getHeight());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	
}
