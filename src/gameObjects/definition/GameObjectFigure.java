package gameObjects.definition;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import data.Texture;
import gameObjects.columnTypes.GameObjectFiguresColumnType;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectState;
import util.ArrayTools;
import util.jframe.table.TableColumnType;

public class GameObjectFigure extends GameObject{
	private transient Texture standingLook;
	private transient Texture lyingLook;
	private String standingLookStr;
	private String lyingLookStr;

	public static final List<TableColumnType> FIGURE_ATTRIBUTES = ArrayTools.unmodifiableList(new TableColumnType[]{GameObjectFiguresColumnType.ID, GameObjectFiguresColumnType.NAME, GameObjectFiguresColumnType.POSX, GameObjectFiguresColumnType.POSY, GameObjectFiguresColumnType.RESET, GameObjectFiguresColumnType.DELETE});
	public GameObjectFigure(String uniqueObjectName, String objectType, int widthInMM, int heightInMM, Texture standingLook, int value, int sortValue, int rotationStep, int isFixed, boolean inBox, int boxId) {
		super(uniqueObjectName, objectType, widthInMM, heightInMM, value, sortValue, rotationStep,isFixed, inBox, boxId);
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
            this.lyingLook = new Texture(lyingFigure, standingLook.getId(), standingLook.suffix);
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
        public boolean equals(Object state) {
            if (!(state instanceof FigureState)) {return false;}
            FigureState fs = (FigureState)state;
            return fs.standing == this.standing && super.equals(state);
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
