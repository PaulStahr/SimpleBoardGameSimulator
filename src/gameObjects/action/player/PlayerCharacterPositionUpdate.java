package gameObjects.action.player;

import java.awt.geom.AffineTransform;

import gui.game.Player;

public class PlayerCharacterPositionUpdate extends PlayerEditAction{
    /**
     * 
     */
    private static final long serialVersionUID = -4102027183308090434L;
    public final double scaleX, scaleY;
    public final double shearX, shearY;
    public final double translateX, translateY;
    public final int screenWidth, screenHeight;
    
    public PlayerCharacterPositionUpdate(int source, Player sourcePlayer, Player editedPlayer, AffineTransform at, int screenWidth, int screenHeight) {
        super(source, sourcePlayer, editedPlayer);
        this.scaleX = at.getScaleX();
        this.scaleY = at.getScaleY();
        this.shearX = at.getShearX();
        this.shearY = at.getShearY();
        this.translateX = at.getTranslateX();
        this.translateY = at.getTranslateY();
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public PlayerCharacterPositionUpdate(int source, int sourcePlayer, int editedPlayer, AffineTransform at, int screenWidth, int screenHeight) {
        super(source, sourcePlayer, editedPlayer);
        this.scaleX = at.getScaleX();
        this.scaleY = at.getScaleY();
        this.shearX = at.getShearX();
        this.shearY = at.getShearY();
        this.translateX = at.getTranslateX();
        this.translateY = at.getTranslateY();
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

}
