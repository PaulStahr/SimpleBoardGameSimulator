package gameObjects.action.player;

import gameObjects.action.AddObjectAction;
import gameObjects.instance.GameInstance;
import main.Player;

public class PlayerAddAction extends AddObjectAction{
	public PlayerAddAction(int source, int objectId) {
		super(source, ADD_PLAYER, objectId);
	}

	public PlayerAddAction(int source, Player pl) {
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
