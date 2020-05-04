package gameObjects.action;

import gameObjects.definition.GameObject;
import gameObjects.instance.GameInstance;

public class GameObjectEditAction extends GameAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1645061833108359226L;
	public final String objectId;
	private transient GameObject go;
	
	public GameObjectEditAction(int source, String objectId) {
		super(source);
		this.objectId = objectId;
	}

	public GameObjectEditAction(int source, GameObject go) {
		super(source);
		this.go = go;
		this.objectId = go.uniqueName;
	}

	public GameObject getObject(GameInstance gi) {
		if (go == null)
		{
			go = gi.game.getObject(objectId);
		}
		return go;
	}
}
