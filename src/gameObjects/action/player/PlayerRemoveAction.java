package gameObjects.action.player;

import gui.game.Player;

public class PlayerRemoveAction extends PlayerEditAction{
	public PlayerRemoveAction(int source, Player sourcePlayer, Player editedPlayer) {
		super(source, sourcePlayer, editedPlayer);
	}

	public PlayerRemoveAction(int source, int sourcePlayer, int editedPlayer) {
		super(source, sourcePlayer, editedPlayer);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2634449824178185251L;
}
