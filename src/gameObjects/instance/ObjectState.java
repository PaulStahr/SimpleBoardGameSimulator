package gameObjects.instance;

import java.io.Serializable;

public abstract class ObjectState implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6447814893037551696L;
	public int posX;
	public int posY;
	public int rotation;
	public int owner_id = -1;
	public boolean inPrivateArea = false;

	/*stacking objects on top of each other*/
	public int aboveInstanceId = -1;
	public int belowInstanceId = -1;
	public int value;
	
	@Override
	public int hashCode()
	{
		return posX ^ (posY << 16) ^ rotation ^ owner_id;
	}

	public void set(ObjectState state) {
		this.posX = state.posX;
		this.posY = state.posY;
		this.rotation = state.rotation;
		this.owner_id = state.owner_id;
		this.inPrivateArea = state.inPrivateArea;
		this.aboveInstanceId = state.aboveInstanceId;
		this.belowInstanceId = state.belowInstanceId;
		this.value = state.value;
	}

	public abstract ObjectState copy(); 
}
