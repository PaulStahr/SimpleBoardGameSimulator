package gameObjects.definition;

import java.awt.image.BufferedImage;

import gameObjects.instance.ObjectState;

public class GameObjectToken extends GameObject{
	BufferedImage upsideLook;
	BufferedImage downsideLook;
	public int value;

	public GameObjectToken(String uniqueName, String objectType, int widthInMM, int heightInMM, BufferedImage front, BufferedImage back, int value) {
		super(uniqueName, objectType, widthInMM, heightInMM);
		this.upsideLook = front;
		this.downsideLook = back;
		this.value = value;
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
	}
	
	public BufferedImage getUpsideLook()
	{
		return upsideLook;
	}
	
	public BufferedImage getDownsideLook()
	{
		return downsideLook;
	}
}
