package gameObjects;

import gameObjects.instance.ObjectInstance;

public class GameObjectInstanceEditAction extends GameAction{
	public final ObjectInstance object;
	public GameObjectInstanceEditAction(Object source, ObjectInstance activeObject) {
		super(source);
		this.object = activeObject;
	}

}
