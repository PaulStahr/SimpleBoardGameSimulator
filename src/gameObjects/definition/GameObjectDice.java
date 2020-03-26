package gameObjects.definition;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import gameObjects.GameObjectInstanceEditAction;
import gameObjects.instance.ObjectActionMenu;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.ObjectState;

public class GameObjectDice extends GameObject{
	ArrayList<BufferedImage> sides;
	public GameObjectDice(String uniqueName, String objectType, ArrayList<BufferedImage> sides) {
		super(uniqueName, objectType);
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
