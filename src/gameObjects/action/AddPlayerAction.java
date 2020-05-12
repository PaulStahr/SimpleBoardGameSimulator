package gameObjects.action;

import gameObjects.instance.GameInstance;
import main.Player;

public class AddPlayerAction extends AddObjectAction{
	public AddPlayerAction(int source, int objectId) {
		super(source, ADD_PLAYER, objectId);
	}

	public AddPlayerAction(int source, Player pl) {
		super(source, ADD_PLAYER, pl.id);
		this.player = pl;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4714754092903118426L;
	private transient Player player;
	
	public Player getPlayer(GameInstance gi) 
	{
		if (player == null)
		{
			player = gi.getPlayerById(objectId);
		}
		return player;
	}
}
