package gameObjects.action.structure;

import gameObjects.action.GameAction;

public class GameTextureRemoveAction extends GameAction{
    /**
     * 
     */
    private static final long serialVersionUID = -7521606120756075674L;
    public final String textureName;

    public GameTextureRemoveAction(int source, String textureName) {
        super(source);
        this.textureName = textureName;
    }
}
