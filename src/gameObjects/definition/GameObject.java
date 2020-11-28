package gameObjects.definition;

import java.awt.image.BufferedImage;
import java.util.List;

import gameObjects.GameObjectColumnType;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectState;
import util.ArrayTools;
import util.data.UniqueObjects;
import util.jframe.table.TableColumnType;

public abstract class GameObject {
	public int widthInMM;
	public int heightInMM;
	public String uniqueObjectName;
	public String objectType;
	public int value;
	public int rotationStep;
	public int isFixed;
	public String groups[] = UniqueObjects.EMPTY_STRING_ARRAY;

	public static final List<TableColumnType> TYPES = ArrayTools.unmodifiableList(new TableColumnType[]{GameObjectColumnType.ID, GameObjectColumnType.NAME, GameObjectColumnType.DELETE});

	public GameObject(String uniqueObjectName, String objectType, int widthInMM, int heightInMM, int value, int rotationStep, int isFixed)
	{
		this.uniqueObjectName = uniqueObjectName;
		this.objectType = objectType;
		this.widthInMM = widthInMM;
		this.heightInMM = heightInMM;
		this.value = value;
		this.rotationStep = rotationStep;
		this.isFixed = isFixed;
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
		return widthInMM ^ heightInMM ^ uniqueObjectName.hashCode();
	}

	public abstract void updateImages(GameInstance gi);
}
