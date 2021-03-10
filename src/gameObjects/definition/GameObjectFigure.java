package gameObjects.definition;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import data.Texture;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectState;

public class GameObjectFigure extends GameObject{
	private transient Texture standingLook;
	private transient Texture lyingLook;
	private String standingLookStr;
	private String lyingLookStr;
	public GameObjectFigure(String uniqueObjectName, String objectType, int widthInMM, int heightInMM, Texture standingLook, int value, int rotationStep, int isFixed) {
		super(uniqueObjectName, objectType, widthInMM, heightInMM, value, rotationStep,isFixed);
		this.standingLook = standingLook;
		// calc a rotated figure from standing figure
		try {
    		int width = standingLook.getImage().getWidth();
    	    int height = standingLook.getImage().getHeight();
    
    	    BufferedImage lyingFigure = new BufferedImage(height, width, standingLook.getImage().getType());
    
    	    Graphics2D graphics2D = lyingFigure.createGraphics();
    	    graphics2D.translate((height - width) / 2, (height - width) / 2);
    	    graphics2D.rotate(Math.PI / 2, height / 2, width / 2);
    	    graphics2D.drawRenderedImage(standingLook.getImage(), null);
            this.lyingLook = new Texture(lyingFigure, standingLook.suffix);
		}
		catch(IOException e)
		{
		    e.printStackTrace();
		}
	}
	
	public GameObjectFigure(GameObjectFigure other) {
	    super(other);
        this.standingLook = other.standingLook.copy();
        this.lyingLook = other.lyingLook.copy();
        this.standingLookStr = other.standingLookStr;
        this.lyingLookStr = other.lyingLookStr;
    }

    @Override
	public void updateImages(GameInstance gi)
	{
		standingLook = gi.game.getImage(standingLookStr);
		lyingLook = gi.game.getImage(lyingLookStr);		
	}

	@Override
	public Texture getLook(ObjectState state, int playerId) {
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

		@Override
		public void reset() {
			super.reset();
			standing = true;
		}
	}
	
	public Texture getStandingLook()
	{
		return standingLook;
	}
	
	public Texture getlyingLook()
	{
		return lyingLook;
	}

    @Override
    public GameObject copy() {return new GameObjectFigure(this);}
}
