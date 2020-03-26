package gameObjects.instance;
import gameObjects.definition.GameObject;
import gui.GamePanel;
import main.Player;

import java.awt.*;

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
	
	public Image getLook()
	{
		return go.getLook(state);
	}

	public int getWidth()
	{
		return go.getWidth(state);
	}

	public int getHeight()
	{
		return go.getHeight(state);
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
