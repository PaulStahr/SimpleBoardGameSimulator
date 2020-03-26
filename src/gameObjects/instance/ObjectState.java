package gameObjects.instance;

import main.Player;

public class ObjectState {
	public int posX;
	public int posY;
	public int rotation;
	public int owner_id = -1;

	/*stacking objects on top of each other*/
	public int aboveInstanceId = -1;
	public int belowInstanceId = -1;

	public int value = 0;
	
	public int hashCode()
	{
		return posX ^ (posY << 16) ^ rotation ^ owner_id;
	}
}
