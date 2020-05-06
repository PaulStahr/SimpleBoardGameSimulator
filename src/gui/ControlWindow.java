package gui;

import gameObjects.instance.GameInstance;

import javax.swing.*;

public class ControlWindow extends JFrame {
    /**
     *
     */
    private static final long serialVersionUID = -9190386402174391670L;
    GameInstance gi;
    public ControlWindow(GameInstance gi, LanguageHandler lh) {
        this.gi = gi;
        setContentPane(new EditControlPanel());
        setSize(500, 400);
    }

}