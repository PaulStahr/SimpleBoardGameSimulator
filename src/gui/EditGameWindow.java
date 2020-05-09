package gui;

import javax.swing.JFrame;

import data.JFrameLookAndFeelUtil;
import gameObjects.instance.GameInstance;
import main.Player;

public class EditGameWindow extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9190386402174391670L;
	GameInstance gi;
	public EditGameWindow(GameInstance gi, LanguageHandler lh, Player player) {
		this.gi = gi;
		setContentPane(new EditGamePanel(gi, lh, player));
		JFrameLookAndFeelUtil.addToUpdateTree(this);
		setSize(500, 400);
	}

}
