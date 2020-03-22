package gameObjects.definition;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import gameObjects.GameObjectInstanceEditAction;
import gameObjects.instance.ObjectActionMenu;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.ObjectState;

public class GameObjectCard extends GameObject{
	BufferedImage upsideLook;
	BufferedImage downsideLook;
	public GameObjectCard(String uniqueName, BufferedImage front, BufferedImage back) {
		super(uniqueName);
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
	
	public BufferedImage getUpsideLook()
	{
		return upsideLook;
	}
	
	public BufferedImage getDownsideLook()
	{
		return downsideLook;
	}
}
