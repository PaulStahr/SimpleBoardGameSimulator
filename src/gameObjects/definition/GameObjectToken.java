package gameObjects.definition;

import java.awt.image.BufferedImage;

import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectState;

public class GameObjectToken extends GameObject{
	private String downsideLookId;
	private String upsideLookId;
	private BufferedImage upsideLook;
	private BufferedImage downsideLook;
	
	public String getDownsideLookId()
	{
		return downsideLookId;
	}
	
	public String getUpsideLookId()
	{
		return upsideLookId;
	}
	
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

	public GameObjectToken(String uniqueName, String objectType, int widthInMM, int heightInMM, BufferedImage front, BufferedImage back, int value, int rotationStep) {
		super(uniqueName, objectType, widthInMM, heightInMM, value, rotationStep);
		this.upsideLook = front;
		this.downsideLook = back;
	}

	@Override
	public BufferedImage getLook(ObjectState state, int playerId) {
		return ((TokenState)state).side != (state.owner_id != playerId)? upsideLook : downsideLook;
	}
	
	@Override
	public ObjectState newObjectState()
	{
		TokenState state = new TokenState();
		state.value = this.value;
		state.rotationStep = this.rotationStep;
	    return state;
	}

	public static class TokenState extends ObjectState
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -5833198843575301636L;
		public boolean side = true;
		
		public TokenState(TokenState tokenState) {
			set(tokenState);
		}

		public TokenState() {}

		@Override
		public int hashCode()
		{
			return super.hashCode() ^ (side ? 0xF00BA : 0);
		}
		
		@Override
		public void set(ObjectState state)
		{
			super.set(state);
			side = ((TokenState)state).side;
		}

		@Override
		public ObjectState copy() {
			return new TokenState(this);
		}

		@Override
		public void reset() {
			super.reset();
			side = true;
		}
	}
	
	public BufferedImage getUpsideLook()
	{
		return upsideLook;
	}
	
	public BufferedImage getDownsideLook()
	{
		return downsideLook;
	}

	@Override
	public void updateImages(GameInstance gi) {
		downsideLook = gi.game.getImage(downsideLookId);
		upsideLook = gi.game.getImage(upsideLookId);
	}
}
