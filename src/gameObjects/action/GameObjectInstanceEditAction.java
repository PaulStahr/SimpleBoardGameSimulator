package gameObjects.action;


import gameObjects.instance.ObjectInstance;
import main.Player;
public class GameObjectInstanceEditAction extends GameAction{
	/**
	 * 
	 */
	public static final long serialVersionUID = 7138542826407548848L;
	public final int object;
	public final int player;
	public GameObjectInstanceEditAction(int source, int player, int activeObject) {
		super(source);
		this.player = player;
		this.object = activeObject;
	}

	public GameObjectInstanceEditAction(int source, Player player, ObjectInstance activeObject) {
		super(source);
		this.player = player.id;
		this.object = activeObject.id;
	}

}
