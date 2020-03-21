package gameObjects.definition;

import java.awt.image.BufferedImage;

import gameObjects.instance.ObjectState;

public class GameObjectCard extends GameObject{
	BufferedImage upsideLook;
	BufferedImage downsideLook;
	public GameObjectCard(String id, BufferedImage front, BufferedImage back) {
		super(id);
		this.upsideLook = front;
		this.downsideLook = back;
	}

	@Override
	public BufferedImage getLook(ObjectState state) {
		return ((CardState)state).side ? upsideLook : downsideLook;
	}
	
	@Override
	public ObjectState newObjectState()
	{
		return new CardState();
	}
	
	public static class CardState  extends ObjectState
	{
		public boolean side;
		
		@Override
		public int hashCode()
		{
			return super.hashCode() ^ (side ? 0xF00BA : 0);
		}
	}
}
