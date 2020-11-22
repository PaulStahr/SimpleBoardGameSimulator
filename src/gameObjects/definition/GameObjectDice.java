package gameObjects.definition;

import java.awt.image.BufferedImage;
import java.util.Random;

import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectState;

public class GameObjectDice extends GameObject{
	public static class DiceSide
	{
		public final int value;
		public BufferedImage img;
		public final String imgStr;
	
		public DiceSide(int value, BufferedImage img, String imgStr)
		{
			this.value = value;
			this.img = img;
			this.imgStr = imgStr;
		}
	}
	public DiceSide dss[];
	
	public GameObjectDice(String uniqueName, String objectType, int widthInMM, int heightInMM, DiceSide sides[], int value, int rotationStep) {
		super(uniqueName, objectType, widthInMM, heightInMM, value, rotationStep, 0);
		this.dss = sides;
	}

	@Override
	public BufferedImage getLook(ObjectState state, int playerId) {
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
		return new DiceState();
	}

	public static class DiceState  extends ObjectState
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 2534848632779449228L;
		public int side = 0;
		
		public DiceState() {}
		
		public DiceState(DiceState diceState) {
			set(diceState);
		}


		@Override
		public int hashCode()
		{
			return super.hashCode() ^ side;
		}
		
		
		@Override
		public void set(ObjectState state)
		{
			super.set(state);
			side = ((DiceState)state).side;
		}


		@Override
		public ObjectState copy() {
			return new DiceState(this);
		}

		@Override
		public void reset() {
			super.reset();
			side = 0;
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

	@Override
	public void updateImages(GameInstance gi) {
		for (int i = 0; i < dss.length; ++i)
		{
			dss[i].img = gi.game.getImage(dss[i].imgStr);
		}
	}

}
