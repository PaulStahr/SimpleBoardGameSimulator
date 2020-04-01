package gameObjects.definition;

import java.awt.image.BufferedImage;
import java.util.Random;

import gameObjects.instance.ObjectState;

public class GameObjectDice extends GameObject{
	public static class DiceSide
	{
		public final int value;
		public final BufferedImage img;
	
		public DiceSide(int value, BufferedImage img)
		{
			this.value = value;
			this.img = img;
		}
	}
	public DiceSide dss[];
	
	public GameObjectDice(String uniqueName, String objectType, int widthInMM, int heightInMM, DiceSide sides[]) {
		super(uniqueName, objectType, widthInMM, heightInMM);
		this.dss = sides;
	}

	@Override
	public BufferedImage getLook(ObjectState state) {
		return dss[((DiceState)state).side].img;
	}

	public DiceSide getDiceSide(int value)
	{
		for (DiceSide dss : this.dss)
		{
			if (dss.value == value)
			{
				return dss;
			}
		}
		return null;
	}
	
	@Override
	public ObjectState newObjectState()
	{
		DiceState state = new DiceState();
		return state;
	}

	public static class DiceState  extends ObjectState
	{
		public int side = 0;
		
		@Override
		public int hashCode()
		{
			return super.hashCode() ^ side;
		}
	}
	
	/** Outputs a random side of the dice and saves the new state.
	 * @param state current DiceState; field "side" will be filled randomly
	 * @param rnd randomizer
	 *  */
	public DiceSide rollTheDice(DiceState state, Random rnd)
	{
		state.side = rnd.nextInt(dss.length);
		state.value = dss[state.side].value;
		return dss[state.side];
	}

}
