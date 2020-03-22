package gameObjects.instance;
import java.awt.Image;
import java.util.ArrayList;

import gameObjects.definition.GameObject;
import main.Player;

public class ObjectInstance {
	public final ObjectState state;
	public final GameObject go;
	public final int id;
	public double scale = 0.2;
	public Player inHand = null;

	/* size of the objects */
	public int width;
	public int height;


	
	public ObjectInstance(GameObject go, int id)
	{
		this.go = go;
		this.state = go.newObjectState();
		this.id = id;

		this.width = getLook().getWidth(null);
		this.height = getLook().getHeight(null);
	}
	
	public Image getLook()
	{
		return go.getLook(state);
	}
	
	public int owner_id()
	{
		return state.owner_id;
	}

	public int getRotation() {
		return state.rotation;
	}
	
	public int hashCode()
	{
		return go.hashCode() + state.hashCode() + id + (inHand == null ? 0 : inHand.hashCode()) + state.owner_id;
	}
	
	public ObjectActionMenu newObjectActionMenu(GameInstance gameInstance, Player player){
		return new CardActionMenu(this, gameInstance, player);
	}

	public static class CardActionMenu extends ObjectActionMenu
	{

		public CardActionMenu(ObjectInstance gameObject, GameInstance gameInstance, Player player) {
			super(gameObject, gameInstance, player);
		}
	}
}
