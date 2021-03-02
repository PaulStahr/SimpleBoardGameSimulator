package gameObjects.action.player;

import main.Player;

public class PlayerMousePositionUpdate extends PlayerEditAction{
    public final int mouseX, mouseY;
    public PlayerMousePositionUpdate(int source, Player sourcePlayer, Player editedPlayer, int mouseX, int mouseY) {
        super(source, sourcePlayer, editedPlayer);
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public PlayerMousePositionUpdate(int source, int sourcePlayer, int editedPlayer, int mouseX, int mouseY) {
        super(source, sourcePlayer, editedPlayer);
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    /**
     * 
     */
    private static final long serialVersionUID = -6821973565404979690L;

}
