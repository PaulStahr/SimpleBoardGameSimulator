package gameObjects.definition;

import java.awt.image.BufferedImage;
import java.util.List;

import gameObjects.GameObjectColumnType;
import gameObjects.instance.ObjectState;
import util.ArrayTools;
import util.data.UniqueObjects;
import util.jframe.table.TableColumnType;

public abstract class GameObject {
	public int widthInMM;
	public int heightInMM;
	public String uniqueName;
	public String objectType;
	public String groups[] = UniqueObjects.EMPTY_STRING_ARRAY;
	public static final List<TableColumnType> TYPES = ArrayTools.unmodifiableList(new TableColumnType[]{GameObjectColumnType.ID, GameObjectColumnType.NAME, GameObjectColumnType.DELETE});

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
}
