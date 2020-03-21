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
	public Player owner = null;
	public final ArrayList<String> groups = new ArrayList<>();
	
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

	public double getRotation() {
		return 0;
	}
	
	public int hashCode()
	{
		return go.hashCode() + state.hashCode() + id + (inHand == null ? 0 : inHand.hashCode()) + (owner == null ? 0 : owner.hashCode());
	}
	
	public ObjectActionMenu newObjectActionMenu(){
		return new CardActionMenu(this);
	}

	public static class CardActionMenu extends ObjectActionMenu
	{

		public CardActionMenu(ObjectInstance gameObject) {
			super(gameObject);
		}
	}
}
