package gameObjects;

import gameObjects.instance.ObjectInstance;
import main.Player;

public class GameObjectInstanceEditAction extends GameAction{
	public final ObjectInstance object;
	public final Player player;
	public GameObjectInstanceEditAction(Object source, Player player, ObjectInstance activeObject) {
		super(source);
		this.player = player;
		this.object = activeObject;
	}

}
