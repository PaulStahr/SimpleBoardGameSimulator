package gameObjects.definition;
import java.awt.image.BufferedImage;

import gameObjects.instance.ObjectState;

public abstract class GameObject {
	int size;
	public String id;
	
	public GameObject(String id)
	{
		this.id = id;
	}
	
	public abstract BufferedImage getLook(ObjectState state);

	public abstract ObjectState newObjectState();
	
	public int hashCode()
	{
		return size + id.hashCode();
	}
}
