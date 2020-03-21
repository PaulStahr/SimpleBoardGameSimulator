package main;
import java.awt.Image;

import gameObjects.GameObject;

public class ObjectInstance {
	public final ObjectState state;
	public final GameObject go;
	public final int id;
	public Player inHand = null;
	public Player owner = null;
	
	public ObjectInstance(GameObject go, int id)
	{
		this.go = go;
		this.state = go.newObjectState();
		this.id = id;
	}
	
	public Image getLook()
	{
		return go.getLook(state);
	}

	public double getRotation() {
		return 0;
	}
	
}
