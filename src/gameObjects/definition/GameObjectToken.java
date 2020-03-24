package gameObjects.definition;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import gameObjects.GameObjectInstanceEditAction;
import gameObjects.instance.ObjectActionMenu;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.ObjectState;

public class GameObjectToken extends GameObject{
	BufferedImage upsideLook;
	BufferedImage downsideLook = null;
	public GameObjectToken(String uniqueName, BufferedImage front, BufferedImage back) {
		super(uniqueName);
		this.upsideLook = front;
		this.downsideLook = back;
	}
	
	public GameObjectToken(String uniqueName, BufferedImage front) {
		super(uniqueName);
		this.upsideLook = front;
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

	public static class TokenState  extends ObjectState
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
