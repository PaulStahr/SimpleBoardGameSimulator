package gameObjects.definition;

import gameObjects.instance.ObjectState;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Random;

public class GameObjectDice extends GameObject{
	public HashMap<Integer, BufferedImage> sides;
	public GameObjectDice(String uniqueName, String objectType, int widthInMM, int heightInMM, HashMap<Integer, BufferedImage> sides) {
		super(uniqueName, objectType, widthInMM, heightInMM);
		this.sides = new HashMap<>(sides);
	}

	@Override
	public BufferedImage getLook(ObjectState state) {
		return sides.get(((DiceState)state).side);
	}

	@Override
	public ObjectState newObjectState()
	{
		DiceState state = new DiceState();
		Object[] keys = sides.keySet().toArray();
		Object key = keys[new Random().nextInt(keys.length)];
		state.side = (int) key;
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
	public BufferedImage rollTheDice(DiceState state)
	{
		Object[] keys = sides.keySet().toArray();
		Object key = keys[new Random().nextInt(keys.length)];
		state.side = (int) key;
		state.value = state.side;
		return sides.get(key);
	}

}
