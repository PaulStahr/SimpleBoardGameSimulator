package gui.game;

import javax.swing.JFrame;

import data.JFrameLookAndFeelUtil;
import gameObjects.instance.GameInstance;
import gui.language.Language;
import gui.language.LanguageChangeListener;
import gui.language.LanguageHandler;
import gui.language.Words;

import java.awt.event.WindowEvent;

public class EditGameWindow extends JFrame implements LanguageChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9190386402174391670L;
	GameInstance gi;
	public EditGameWindow(GameInstance gi, LanguageHandler lh, Player player) {
		this.gi = gi;
		lh.addLanguageChangeListener(this);
		languageChanged(lh.getCurrentLanguage());
		setContentPane(new EditGamePanel(gi, lh, player));
		JFrameLookAndFeelUtil.addToUpdateTree(this);
		setSize(500, 400);
		this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSED));
	}

	@Override
	public void languageChanged(Language language) {
		this.setTitle(language.getString(Words.edit_game));
	}


}
