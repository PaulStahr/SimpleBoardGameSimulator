package main.gameObjects.instance;
import java.awt.Image;

import main.gameObjects.GameObjectInstanceColumnType;
import main.gameObjects.Player;
import main.gameObjects.definition.GameObject;
import main.gui.GamePanel;
import main.util.jframe.table.ColumnTypes;
import main.util.jframe.table.TableColumnType;

public class ObjectInstance {
	public static final ColumnTypes TYPES = new ColumnTypes(new TableColumnType[]{GameObjectInstanceColumnType.ID, GameObjectInstanceColumnType.NAME, GameObjectInstanceColumnType.DELETE}, new TableColumnType[]{GameObjectInstanceColumnType.ID, GameObjectInstanceColumnType.NAME, GameObjectInstanceColumnType.DELETE});
	public final ObjectState state;
	public final GameObject go;
	public final int id;
	public double scale = 0.2;


	public ObjectInstance(GameObject go, int id)
	{
		this.go = go;
		this.state = go.newObjectState();
		this.id = id;
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

	public Object getValue(TableColumnType col) {
		GameObjectInstanceColumnType tmp = (GameObjectInstanceColumnType)col;
		switch(tmp)
		{
		case DELETE:
			return "delete";
		case ID:
			return id;
		case NAME:
			return go.uniqueName;
		default:
			return "";
		}
	}
}
