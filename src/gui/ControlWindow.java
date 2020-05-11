package gui;

import javax.swing.JFrame;

import gameObjects.instance.GameInstance;

public class ControlWindow extends JFrame {
    /**
     *
     */
    private static final long serialVersionUID = -9190386402174391670L;
    GameInstance gi;
    public ControlWindow(GameInstance gi, LanguageHandler lh) {
        this.gi = gi;
        setContentPane(new EditControlPanel(lh));
        setSize(500, 400);
    }

}