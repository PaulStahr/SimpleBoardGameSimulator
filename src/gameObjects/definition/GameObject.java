package gameObjects.definition;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import gameObjects.instance.ObjectState;

public abstract class GameObject {
	int drawSize;
	public String uniqueName;
	public final ArrayList<String> groups = new ArrayList<>();

	public GameObject(String uniqueName)
	{
		this.uniqueName = uniqueName;
	}
	
	public abstract BufferedImage getLook(ObjectState state);

	public abstract ObjectState newObjectState();

	public int hashCode()
	{
		return drawSize ^ uniqueName.hashCode();
	}
}
