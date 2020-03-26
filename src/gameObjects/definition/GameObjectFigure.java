package gameObjects.definition;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import gameObjects.GameObjectInstanceEditAction;
import gameObjects.instance.ObjectActionMenu;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.ObjectState;

public class GameObjectFigure extends GameObject{
	BufferedImage standingLook;
	BufferedImage lyingLook;
	public GameObjectFigure(String uniqueName, String objectType, BufferedImage standingLook) {
		super(uniqueName, objectType);
		this.standingLook = standingLook;
		
		// calc a rotated figure from standing figure
		int width = standingLook.getWidth();
	    int height = standingLook.getHeight();

	    BufferedImage lyingFigure = new BufferedImage(height, width, standingLook.getType());

	    Graphics2D graphics2D = lyingFigure.createGraphics();
	    graphics2D.translate((height - width) / 2, (height - width) / 2);
	    graphics2D.rotate(Math.PI / 2, height / 2, width / 2);
	    graphics2D.drawRenderedImage(standingLook, null);
	    this.lyingLook = lyingFigure;
	}

	@Override
	public BufferedImage getLook(ObjectState state) {
		return ((FigureState)state).standing ? standingLook : lyingLook;
	}
	
	@Override
	public ObjectState newObjectState()
	{
		return new FigureState();
	}

	public static class FigureState  extends ObjectState
	{
		public boolean standing = true;
		
		@Override
		public int hashCode()
		{
			return super.hashCode() ^ (standing ? 0xF00BA : 0);
		}
	}
	
	public BufferedImage getStandingLook()
	{
		return standingLook;
	}
	
	public BufferedImage getlyingLook()
	{
		return lyingLook;
	}
}
