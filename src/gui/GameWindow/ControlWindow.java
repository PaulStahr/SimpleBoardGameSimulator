package gui.GameWindow;

import javax.swing.JFrame;

import gameObjects.instance.GameInstance;
import gui.Language.Language;
import gui.Language.LanguageChangeListener;
import gui.Language.LanguageHandler;
import gui.Language.Words;

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