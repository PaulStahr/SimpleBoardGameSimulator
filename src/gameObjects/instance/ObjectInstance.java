package gameObjects.instance;
import java.awt.Image;
import java.util.Comparator;
import java.util.List;

import gameObjects.GameObjectInstanceColumnType;
import gameObjects.definition.GameObject;
import gui.GamePanel;
import main.Player;
import util.ArrayTools;
import util.ArrayTools.ObjectToIntTransform;
import util.jframe.table.TableColumnType;

public class ObjectInstance {
	public static final List<TableColumnType> TYPES = ArrayTools.unmodifiableList(new TableColumnType[]{GameObjectInstanceColumnType.ID, GameObjectInstanceColumnType.NAME, GameObjectInstanceColumnType.OWNER, GameObjectInstanceColumnType.ABOVE, GameObjectInstanceColumnType.BELOW, GameObjectInstanceColumnType.RESET, GameObjectInstanceColumnType.DELETE});
	public static final Comparator<? super ObjectInstance> ID_COMPARATOR = new Comparator<ObjectInstance>() {
		@Override
		public int compare(ObjectInstance o1, ObjectInstance o2) {
			return o1.id - o2.id;
		}
	};
	public static final ObjectToIntTransform<ObjectInstance> OBJECT_TO_ID = new ObjectToIntTransform<ObjectInstance>() {

		@Override
		public int toInt(ObjectInstance o) {
			return o.id;
		}
	};
	public final ObjectState state;
	public final GameObject go;
	public final int id;
	public double scale = 0.2;
	public double tmpScale = 0.2;

	public ObjectInstance(GameObject go, int id)
	{
		this.go = go;
		this.state = go.newObjectState();
		this.id = id;
		this.state.drawValue = id;
	}
	
	public Image getLook(int playerId)
	{
		return go.getLook(state, playerId);
	}

	public int getWidth(int playerId)
	{
		return go.getWidth(state, playerId);
	}

	public int getHeight(int playerId)
	{
		return go.getHeight(state, playerId);
	}

	public void updateState(ObjectState objectState)
	{
		state.set(objectState);
	}
	
	public int owner_id()
	{
		return state.owner_id;
	}

	public int getRotation() {
		return state.rotation;
	}
	
	@Override
	public int hashCode()
	{
		return go.hashCode() + state.hashCode() + id;
	}
	
	public ObjectActionMenu newObjectActionMenu(GameInstance gameInstance, Player player, GamePanel gamePanel){
		return new CardActionMenu(this, gameInstance, player, gamePanel);
	}

	public static class CardActionMenu extends ObjectActionMenu
	{
		public CardActionMenu(ObjectInstance gameObject, GameInstance gameInstance, Player player, GamePanel gamePanel) {
			super(gameObject, gameInstance, player, gamePanel);
		}
	}
}
