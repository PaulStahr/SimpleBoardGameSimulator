package main.gui;

import javax.swing.JFrame;

import main.gameObjects.instance.GameInstance;

public class EditGameWindow extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9190386402174391670L;
	GameInstance gi;
	public EditGameWindow(GameInstance gi) {
		this.gi = gi;
		setContentPane(new EditGamePanel(gi));
	}

}
