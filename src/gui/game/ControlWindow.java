package gui.game;

import javax.swing.JFrame;

import gameObjects.instance.GameInstance;
import gui.language.Language;
import gui.language.LanguageChangeListener;
import gui.language.LanguageHandler;
import gui.language.Words;

public class ControlWindow extends JFrame implements LanguageChangeListener {
    /**
     *
     */
    private static final long serialVersionUID = -9190386402174391670L;
    private final LanguageHandler lh;
    GameInstance gi;
    public ControlWindow(GameInstance gi, LanguageHandler lh) {
        this.gi = gi;
        this.lh = lh;
        lh.addLanguageChangeListener(this);
        languageChanged(lh.getCurrentLanguage());
        setContentPane(new EditControlPanel(lh));
        setSize(500, 400);
    }

    @Override
    public void languageChanged(Language language) {
        this.setTitle(language.getString(Words.control_window));
    }
}