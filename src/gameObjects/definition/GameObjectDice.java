package gameObjects.definition;

import gameObjects.instance.ObjectState;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Random;

public class GameObjectDice extends GameObject{
	public static class DiceSideState
	{
		public final int value;
		public final BufferedImage img;
	
		public DiceSideState(int value, BufferedImage img)
		{
			this.value = value;
			this.img = img;
		}
	}
	public DiceSideState dss[];
	
	public GameObjectDice(String uniqueName, String objectType, int widthInMM, int heightInMM, DiceSideState sides[]) {
		super(uniqueName, objectType, widthInMM, heightInMM);
		this.dss = sides;
	}

	@Override
	public BufferedImage getLook(ObjectState state) {
		return dss[((DiceState)state).side].img;
	}

	public DiceSideState getDiceState(int value)
	{
		for (DiceSideState dss : this.dss)
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
		DiceState state = new DiceState();//This implementation copys the state (Name suggests just creating object)
		state.side = 0;
		state.value = state.side;
		return state;
	}

	public static class DiceState  extends ObjectState
	{
		public int side;
		
		@Override
		public int hashCode()
		{
			return super.hashCode() ^ side;
		}
	}
	
	// Outputs a random side of the dice and saves the new state
	public DiceSideState rollTheDice(DiceState state, Random rnd)
	{
		state.side = rnd.nextInt(dss.length);
		state.value = state.side;
		return dss[state.value];
	}

}
