package gameObjects.definition;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectState;

public class GameObjectFigure extends GameObject{
	private transient BufferedImage standingLook;
	private transient BufferedImage lyingLook;
	private String standingLookStr;
	private String lyingLookStr;
	public GameObjectFigure(String uniqueName, String objectType, int widthInMM, int heightInMM, BufferedImage standingLook, int value, int rotationStep) {
		super(uniqueName, objectType, widthInMM, heightInMM, value, rotationStep);
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
	public void updateImages(GameInstance gi)
	{
		standingLook = gi.game.getImage(standingLookStr);
		lyingLook = gi.game.getImage(lyingLookStr);		
	}

	@Override
	public BufferedImage getLook(ObjectState state, int playerId) {
		return ((FigureState)state).standing ? standingLook : lyingLook;
	}
	
	@Override
	public ObjectState newObjectState()
	{
		return new FigureState();
	}

	public static class FigureState  extends ObjectState
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 2412895719581742132L;
		public boolean standing = true;
		
		public FigureState() {}
		
		public FigureState(FigureState figureState) {
			set(figureState);
		}

		@Override
		public int hashCode()
		{
			return super.hashCode() ^ (standing ? 0xF00BA : 0);
		}
		
		@Override
		public void set(ObjectState state)
		{
			super.set(state);
			standing = ((FigureState)state).standing;
		}

		@Override
		public ObjectState copy() {
			return new FigureState(this);
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
