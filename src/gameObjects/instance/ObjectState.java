package gameObjects.instance;

import main.Player;

public class ObjectState {
	public int posX;
	public int posY;
	public int rotation;
	public Player owner;
	
	public int hashCode()
	{
		return posX ^ (posY << 16) ^ rotation ^ (owner == null ? 0: owner.hashCode());
	}
}
