package gameObjects.definition;

import gameObjects.instance.ObjectState;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class GameObjectDice extends GameObject{
	ArrayList<BufferedImage> sides;
	public GameObjectDice(String uniqueName, String objectType, int widthInMM, int heightInMM, ArrayList<BufferedImage> sides) {
		super(uniqueName, objectType, widthInMM, heightInMM);
		this.sides = new ArrayList<BufferedImage>(sides);
	}

	@Override
	public BufferedImage getLook(ObjectState state) {
		return sides.get(((DiceState)state).side);
	}

	@Override
	public ObjectState newObjectState()
	{
		return new DiceState();
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
	
	// Outputs a random side of the dice and saves the new state
	public BufferedImage rollTheDice(DiceState state)
	{
		state.side = new Random().nextInt(sides.size());
		return sides.get(state.side);
	}

}
