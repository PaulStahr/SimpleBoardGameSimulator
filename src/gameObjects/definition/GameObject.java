package gameObjects.definition;

import java.awt.image.BufferedImage;

import gameObjects.GameInstanceColumnType;
import gameObjects.instance.ObjectState;

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
	
	public abstract BufferedImage getLook(ObjectState state, int playerId);

	public int getWidth(ObjectState state, int playerId) {
		BufferedImage img = getLook(state, playerId);
		return img == null ? 0 : (int) (img.getWidth()*0.2);
	}

	public int getHeight(ObjectState state, int playerId) {
		BufferedImage img = getLook(state, playerId);
		return img == null ? 0 : (int) (img.getHeight()*0.2);
	}


	public abstract ObjectState newObjectState();

	@Override
	public int hashCode()
	{
		return widthInMM ^ heightInMM ^ uniqueName.hashCode();
	}

	public Object getValue(GameInstanceColumnType visibleCol) {
		return "";
	}
}
