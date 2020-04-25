package gameObjects.action;

import gameObjects.instance.ObjectInstance;
import main.Player;

public class GameObjectInstanceEditAction extends GameAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7138542826407548848L;
	public final ObjectInstance object;
	public final int player;
	public GameObjectInstanceEditAction(int source, Player player, ObjectInstance activeObject) {
		super(source);
		if (player == null)
		{
			throw new NullPointerException();
		}
		this.player = player.id;
		this.object = activeObject;
	}

}
