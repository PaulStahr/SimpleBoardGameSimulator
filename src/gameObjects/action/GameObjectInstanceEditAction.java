package gameObjects.action;


import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.ObjectState;
import main.Player;
public class GameObjectInstanceEditAction extends GameAction{
	/**
	 * 
	 */
	public static final long serialVersionUID = 7138542826407548848L;
	public final int object;
	public final int player;
	private transient Player playerObject;
	private transient ObjectInstance activeObject;
	public transient ObjectState state;
	public GameObjectInstanceEditAction(int source, int player, int activeObject, ObjectState state) {
		super(source);
		this.player = player;
		this.object = activeObject;
		this.state = state;
	}

	public GameObjectInstanceEditAction(int source, Player player, ObjectInstance activeObject, ObjectState state) {
		super(source);
		this.player = player == null ? -1 : player.id;
		this.playerObject = player;
		this.object = activeObject.id;
		this.activeObject = activeObject;
		this.state = state;
	}

	public Player getPlayer(GameInstance instance)
	{
		if (playerObject == null)
		{
			return playerObject = instance.getPlayerById(player);
		}
		return playerObject;
	}
	
	public ObjectInstance getObject(GameInstance instance)
	{
		if (playerObject == null)
		{
			return activeObject = instance.getObjectInstanceById(object);
		}
		return activeObject;
	}
}
