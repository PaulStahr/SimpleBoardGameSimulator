package gameObjects.instance;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import io.ObjectStateIO;
import util.data.IntegerArrayList;

public abstract class ObjectState implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6447814893037551696L;
	public int posX = 0;
	public int posY = 0;
	public int originalRotation = 0;
	public int rotation = 0;
	public int scale = 1;
	public int owner_id = -1;
	public int isSelected = -1;
	transient public boolean isActive = false;
	transient public long lastChange = System.nanoTime();
	public boolean inPrivateArea = false;

	/*stacking objects on top of each other*/
	public int aboveInstanceId = -1;
	public int belowInstanceId = -1;

	public int liesOnId = -1;
	public final IntegerArrayList aboveLyingObectIds = new IntegerArrayList();

	public int value = 0;
	public int sortValue = 0;
	public int drawValue = 0;
	public int rotationStep = 90;

	/*fix an object*/
	public boolean isFixed = false;



	@Override
	public int hashCode()
	{
		return posX ^ (posY << 16) ^ rotation ^ scale ^ owner_id;
	}

	public void set(ObjectState state) {
		this.posX = state.posX;
		this.posY = state.posY;
		this.originalRotation = state.originalRotation;
		this.rotation = state.rotation;
		this.scale = state.scale;
		this.owner_id = state.owner_id;
		this.isSelected = state.isSelected;
		this.inPrivateArea = state.inPrivateArea;
		this.aboveInstanceId = state.aboveInstanceId;
		this.belowInstanceId = state.belowInstanceId;
		this.liesOnId = state.liesOnId;
		this.aboveLyingObectIds.set(state.aboveLyingObectIds);
		this.value = state.value;
		this.sortValue = state.sortValue;
		this.rotationStep = state.rotationStep;
		this.isFixed = state.isFixed;
		this.lastChange = state.lastChange;
		this.isActive = state.isActive;
		this.drawValue = state.drawValue;
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
		rotation = originalRotation;
		scale = 1;
		owner_id = -1;
		isSelected = -1;
		isActive = false;
		inPrivateArea = false;
		aboveInstanceId = -1;
		belowInstanceId = -1;
		liesOnId = -1;
		aboveLyingObectIds.clear();
		drawValue = 1;
		rotationStep = 90;
		isFixed = false;
		lastChange = System.nanoTime();
	}
	
	@Override
	public String toString()
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectStateIO.writeObjectStateToStreamXml(this, bos);
		} catch (IOException e) {
			return e.toString();
		}
		return bos.toString();
	}
	
	@Override
    public boolean equals(Object other)
	{
	    if (other == this){return true;}
	    if (!(other instanceof ObjectState)){return false;}
	    ObjectState os = (ObjectState)other;
	    return this.posX == os.posX
	            && this.posY == os.posY
	            && this.rotation == os.rotation
	            && this.scale == os.scale
	            && this.owner_id == os.owner_id
	            && this.isSelected == os.isSelected
	            && this.inPrivateArea == os.inPrivateArea
	            && this.aboveInstanceId == os.aboveInstanceId
	            && this.belowInstanceId == os.belowInstanceId
				&& this.liesOnId == os.liesOnId
				&& this.aboveLyingObectIds.equals(os.aboveLyingObectIds)
	            && this.value == os.value
	            && this.drawValue == os.drawValue
	            && this.rotationStep == os.rotationStep
	            && this.isFixed == os.isFixed;
	}
}
