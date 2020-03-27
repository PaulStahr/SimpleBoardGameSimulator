package gameObjects.definition;

import gameObjects.instance.ObjectState;

import java.awt.image.BufferedImage;

public abstract class GameObject {
	public int widthInMM;
	public int heightInMM;
	public String uniqueName;
	public String objectType;

	public GameObject(String uniqueName, String objectType, int widthInMM, int heightInMM)
	{
		this.uniqueName = uniqueName;
		this.objectType = objectType;
		this.widthInMM = widthInMM;
		this.heightInMM = heightInMM;
	}
	
	public abstract BufferedImage getLook(ObjectState state);

	public int getWidth(ObjectState state) {
		BufferedImage img = getLook(state);
		return img == null ? 0 : (int) (img.getWidth()*0.2);
	}

	public int getHeight(ObjectState state) {
		BufferedImage img = getLook(state);
		return img == null ? 0 : (int) (img.getHeight()*0.2);
	}


	public abstract ObjectState newObjectState();

	public int hashCode()
	{
		return widthInMM ^ heightInMM ^ uniqueName.hashCode();
	}
}
