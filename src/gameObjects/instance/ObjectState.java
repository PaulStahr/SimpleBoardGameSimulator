package gameObjects.instance;

public class ObjectState {
	public int posX;
	public int posY;
	public int rotation;
	public int owner_id = -1;

	/*stacking objects on top of each other*/
	public int aboveInstanceId = -1;
	public int belowInstanceId = -1;
	public int value;
	
	@Override
	public int hashCode()
	{
		return posX ^ (posY << 16) ^ rotation ^ owner_id;
	}
}
