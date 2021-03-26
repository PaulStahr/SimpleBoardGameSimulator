package gameObjects.definition;

import java.util.Random;

import data.Texture;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectState;

public class GameObjectDice extends GameObject{

	public static class DiceSide
	{
		public final int value;
		public Texture img;
		public final String imgStr;

	
		public DiceSide(int value, Texture img, String imgStr)
		{
			this.value = value;
			this.img = img;
			this.imgStr = imgStr;
		}

        public DiceSide(DiceSide other) {
            this.value = other.value;
            this.img = new Texture(other.img);
            this.imgStr = other.imgStr;
        }

        public DiceSide copy() {
            return new DiceSide(this);
        }
	}
	public DiceSide dss[];
	
	public GameObjectDice(String uniqueObjectName, String objectType, int widthInMM, int heightInMM, DiceSide sides[], int value, int sortValue, int rotationStep) {
		super(uniqueObjectName, objectType, widthInMM, heightInMM, value, sortValue, rotationStep, 0);
		this.dss = sides;
	}

	public GameObjectDice(GameObjectDice other) {
	    super(other);
	    dss = new DiceSide[other.dss.length];
	    for (int i = 0; i < dss.length; ++i)
	    {
	        dss[i] = other.dss[i].copy();
	    }
    }

    @Override
	public Texture getLook(ObjectState state, int playerId) {
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

	public static class DiceState extends ObjectState
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 2534848632779449228L;
		public int side = 0;
		public boolean unfold = false;
		
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
			unfold = ((DiceState)state).unfold;
		}


		@Override
		public ObjectState copy() {
			return new DiceState(this);
		}

		@Override
		public void reset() {
			super.reset();
			side = 0;
			unfold = false;
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

    @Override
    public GameObject copy() {return new GameObjectDice(this);}
}
