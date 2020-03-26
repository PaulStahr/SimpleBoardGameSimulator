package gameObjects.definition;

import java.awt.image.BufferedImage;

import gameObjects.instance.ObjectState;

public class GameObjectToken extends GameObject{
	BufferedImage upsideLook;
	BufferedImage downsideLook;
	public GameObjectToken(String uniqueName, String objectType, BufferedImage front, BufferedImage back) {
		super(uniqueName, objectType);
		this.upsideLook = front;
		downsideLook = null;
		this.downsideLook = back;
	}

	public GameObjectToken(String uniqueName, String objectType, BufferedImage front) {
		super(uniqueName, objectType);
		this.upsideLook = front;
		downsideLook = null;
	}

	@Override
	public BufferedImage getLook(ObjectState state) {
		return ((TokenState)state).side ? upsideLook : downsideLook;
	}
	
	@Override
	public ObjectState newObjectState()
	{
		return new TokenState();
	}

	public static class TokenState extends ObjectState
	{
		public boolean side = true;
		
		@Override
		public int hashCode()
		{
			return super.hashCode() ^ (side ? 0xF00BA : 0);
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
