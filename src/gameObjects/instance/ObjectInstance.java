package gameObjects.instance;
import java.awt.Image;

import gameObjects.definition.GameObject;
import gui.GamePanel;
import main.Player;

public class ObjectInstance {
	public final ObjectState state;
	public final GameObject go;
	public final int id;
	public double scale = 0.2;
	public Player inHand = null;

	
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
		state.posX = objectState.posX;
		state.posY = objectState.posY;
		state.rotation = objectState.rotation;
		state.owner_id = objectState.owner_id;
		state.aboveInstanceId = objectState.aboveInstanceId;
		state.belowInstanceId = objectState.belowInstanceId;
		state.value = objectState.value;
		state.owner_id = objectState.owner_id;
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
		return go.hashCode() + state.hashCode() + id + (inHand == null ? 0 : inHand.hashCode()) + state.owner_id;
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
