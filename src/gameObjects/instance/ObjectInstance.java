package gameObjects.instance;
import java.util.Comparator;
import java.util.List;

import data.Texture;
import gameObjects.columnTypes.GameObjectInstanceColumnType;
import gameObjects.definition.GameObject;
import gui.game.GamePanel;
import gui.game.Player;
import util.ArrayTools;
import util.ArrayTools.ObjectToIntTransform;
import util.jframe.table.TableColumnType;

public class ObjectInstance {
	public static final List<TableColumnType> TYPES = ArrayTools.unmodifiableList(new TableColumnType[]{GameObjectInstanceColumnType.ID, GameObjectInstanceColumnType.NAME, GameObjectInstanceColumnType.POSX, GameObjectInstanceColumnType.POSY, GameObjectInstanceColumnType.OWNER, GameObjectInstanceColumnType.ABOVE, GameObjectInstanceColumnType.BELOW, GameObjectInstanceColumnType.RESET, GameObjectInstanceColumnType.DELETE});
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
    public static enum Relation{ABOVE, BELOW};
	public final ObjectState state;
	public final GameObject go;
	public final int id;
	public double scale = 0.2;
	public double tmpScale = 1;

	public ObjectInstance(GameObject go, int id)
	{
		this.go = go;
		this.state = go.newObjectState();
		this.id = id;
	}
	
	public ObjectInstance(ObjectInstance other) {
        this.state = other.state.copy();
        this.go = other.go.copy();
        this.id = other.id;
        this.scale = other.scale;
        this.tmpScale = other.tmpScale;
    }

    public Texture getLook(int playerId){return go.getLook(state, playerId);}

	public int getWidth(int playerId){return go.getWidth(state, playerId) * this.state.scale;}

	public int getHeight(int playerId)
	{
		return go.getHeight(state, playerId) * this.state.scale;
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

	public ObjectState getState() {return state;}

    public ObjectState copyState() {return state.copy();}

    public ObjectInstance copy() {return new ObjectInstance(this);}
}
