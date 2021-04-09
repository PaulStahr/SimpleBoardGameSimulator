package gameObjects.definition;

import java.util.List;

import data.Texture;
import gameObjects.columnTypes.GameObjectTokenColumnType;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectState;
import util.ArrayTools;
import util.jframe.table.TableColumnType;

public class GameObjectToken extends GameObject{
	private String downsideLookId;
	private String upsideLookId;
	private Texture downsideLook;
    private Texture upsideLook;

	public static final List<TableColumnType> TOKEN_ATTRIBUTES = ArrayTools.unmodifiableList(new TableColumnType[]{GameObjectTokenColumnType.ID, GameObjectTokenColumnType.NAME, GameObjectTokenColumnType.POSX, GameObjectTokenColumnType.POSY, GameObjectTokenColumnType.OWNER, GameObjectTokenColumnType.ABOVE, GameObjectTokenColumnType.BELOW, GameObjectTokenColumnType.RESET, GameObjectTokenColumnType.DELETE});


	public String getDownsideLookId(){return downsideLookId;}
	
	public String getUpsideLookId(){return upsideLookId;}
	
	public void setUpsideLook(String upsideLookId)
	{
		this.upsideLookId = upsideLookId;
		this.upsideLook = null;
	}
	
	public void setDownsideLook(String downsideLookId)
	{
		this.downsideLookId = downsideLookId;
		this.downsideLook = null;
	}

	public GameObjectToken(String uniqueObjectName, String objectType, int widthInMM, int heightInMM, Texture front, Texture back, int value, int sortValue, int rotationStep, int isFixed, boolean inBox, int boxId) {
		super(uniqueObjectName, objectType, widthInMM, heightInMM, value, sortValue, rotationStep, isFixed, inBox, boxId);
		this.upsideLook = front;
		this.downsideLook = back;
	}

	public GameObjectToken(GameObjectToken other) {
	    super(other);
        this.downsideLookId = other.downsideLookId;
        this.upsideLookId = other.upsideLookId;
        this.downsideLook = other.downsideLook;
        this.upsideLook = other.upsideLook;
    }

    @Override
	public Texture getLook(ObjectState state, int playerId) {
		return ((TokenState)state).side != (state.owner_id != playerId)? upsideLook : downsideLook;
	}
	
	@Override
	public ObjectState newObjectState()
	{
		TokenState state = new TokenState();
		state.value = this.value;
		state.sortValue = this.sortValue;
		state.rotationStep = this.rotationStep;
		state.isFixed = (this.isFixed != 0);
	    return state;
	}

	public static class TokenState extends ObjectState
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -5833198843575301636L;
		public boolean side = true;
		
		public TokenState(TokenState tokenState) {set(tokenState);}

		public TokenState() {}

		@Override
		public int hashCode(){return super.hashCode() ^ (side ? 0xF00BA : 0);}
		
		@Override
		public void set(ObjectState state)
		{
			super.set(state);
			side = ((TokenState)state).side;
		}

		@Override
		public ObjectState copy() {return new TokenState(this);}

		@Override
        public boolean equals(Object state) {
		    if (!(state instanceof TokenState)) {return false;}
		    TokenState ts = (TokenState)state;
		    return ts.side == this.side && super.equals(state);
		}
		
		@Override
		public void reset() {
			super.reset();
			side = true;
		}
	}
	
	public Texture getUpsideLook(){return upsideLook;}
	
	public Texture getDownsideLook(){return downsideLook;}

	@Override
	public void updateImages(GameInstance gi) {
		downsideLook = gi.game.getImage(downsideLookId);
		upsideLook = gi.game.getImage(upsideLookId);
	}

    @Override
    public GameObject copy() {return new GameObjectToken(this);}
}
