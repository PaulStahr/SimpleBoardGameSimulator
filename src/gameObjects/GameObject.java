package gameObjects;
import java.awt.image.BufferedImage;

import main.ObjectState;

public abstract class GameObject {
	int size;
	public String id;
	
	public GameObject(String id)
	{
		this.id = id;
	}
	
	public abstract BufferedImage getLook(ObjectState state);

	public abstract ObjectState newObjectState();
}
