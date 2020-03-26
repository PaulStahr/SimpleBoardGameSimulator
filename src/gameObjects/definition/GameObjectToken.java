package gameObjects.definition;

import gameObjects.instance.ObjectState;

import java.awt.image.BufferedImage;

public class GameObjectToken extends GameObject{
	BufferedImage upsideLook;
	BufferedImage downsideLook;
	int value;

	public GameObjectToken(String uniqueName, String objectType, int widthInMM, int heightInMM, BufferedImage front, BufferedImage back, int value) {
		super(uniqueName, objectType, widthInMM, heightInMM);
		this.upsideLook = front;
		this.downsideLook = back;
		this.value = value;
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
