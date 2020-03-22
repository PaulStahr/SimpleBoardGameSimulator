package gameObjects;

import gameObjects.instance.ObjectInstance;
import main.Player;

public class GameObjectInstanceEditAction extends GameAction{
	public final ObjectInstance object;
	public final Player player;
	public GameObjectInstanceEditAction(int source, Player player, ObjectInstance activeObject) {
		super(source);
		if (player == null)
		{
			throw new RuntimeException();
		}
		this.player = player;
		this.object = activeObject;
	}

}
