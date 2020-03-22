package gameObjects.instance;

import main.Player;

public class ObjectState {
	public int posX;
	public int posY;
	public int rotation;
	public int owner_id = -1;

	/*stacking objects on top of each other*/
	public ObjectInstance aboveInstance = null;
	public ObjectInstance belowInstance = null;
	
	public int hashCode()
	{
		return posX ^ (posY << 16) ^ rotation ^ owner_id;
	}
}
