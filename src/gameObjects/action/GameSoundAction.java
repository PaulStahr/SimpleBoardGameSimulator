package gameObjects.action;

import gui.game.Player;

public class GameSoundAction extends GameAction {
    public final int player;
    private transient Player playerObject;
    public GameSoundAction(int source, int player) {
        super(source);
        this.player = player;
    }
}
