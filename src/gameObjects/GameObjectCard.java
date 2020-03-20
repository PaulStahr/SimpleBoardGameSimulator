package gameObjects;

import java.awt.image.BufferedImage;

import main.ObjectState;

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
	
	class CardState  extends ObjectState
	{
		boolean side;
	}
}
