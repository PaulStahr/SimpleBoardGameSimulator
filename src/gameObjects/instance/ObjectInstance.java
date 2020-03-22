package gameObjects.instance;
import java.awt.Image;
import java.util.ArrayList;

import gameObjects.definition.GameObject;
import main.Player;

public class ObjectInstance {
	public final ObjectState state;
	public final GameObject go;
	public final int id;
	public Player inHand = null;
	
	public ObjectInstance(GameObject go, int id)
	{
		this.go = go;
		this.state = go.newObjectState();
		this.id = id;
	}
	
	public Image getLook()
	{
		return go.getLook(state);
	}
	
	public Player owner()
	{
		return state.owner;
	}

	public int getRotation() {
		return state.rotation;
	}
	
	public int hashCode()
	{
		return go.hashCode() + state.hashCode() + id + (inHand == null ? 0 : inHand.hashCode()) + (state.owner == null ? 0 : state.owner.hashCode());
	}
	
	public ObjectActionMenu newObjectActionMenu(GameInstance gameInstance){
		return new CardActionMenu(this, gameInstance, owner());
	}

	public static class CardActionMenu extends ObjectActionMenu
	{

		public CardActionMenu(ObjectInstance gameObject, GameInstance gameInstance, Player player) {
			super(gameObject, gameInstance, null);
		}
	}
}
