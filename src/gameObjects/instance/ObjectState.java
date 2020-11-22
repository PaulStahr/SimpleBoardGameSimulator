package gameObjects.instance;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import io.GameIO;

public abstract class ObjectState implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6447814893037551696L;
	public int posX;
	public int posY;
	public int rotation;
	public int owner_id = -1;
	transient public boolean isActive = false;
	transient public long lastChange = System.nanoTime();
	public boolean inPrivateArea = false;

	/*stacking objects on top of each other*/
	public int aboveInstanceId = -1;
	public int belowInstanceId = -1;
	public int value;
	public long drawValue = 0;
	public int rotationStep = 90;

	/*fix an object*/
	public boolean isFixed = false;
	
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
		this.rotationStep = state.rotationStep;
		this.isFixed = state.isFixed;
		this.lastChange = state.lastChange;
	}

	public abstract ObjectState copy();

	public double getRadiansRotation()
	{
		return Math.toRadians(rotation);
	}

	public void reset()
	{
		posX = 0;
		posY = 0;
		rotation = 0;
		owner_id = -1;
		isActive = false;
		inPrivateArea = false;
		aboveInstanceId = -1;
		belowInstanceId = -1;
		value = 0;
		drawValue = 0;
		rotationStep = 90;
		isFixed = false;
		lastChange = System.nanoTime();
	}
	
	@Override
	public String toString()
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			GameIO.writeObjectStateToStreamXml(this, bos);
		} catch (IOException e) {
			return e.toString();
		}
		return bos.toString();
	}
}
