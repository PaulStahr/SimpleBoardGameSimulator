package gameObjects.action.player;

import gameObjects.action.GameAction;
import gameObjects.instance.GameInstance;
import gui.game.Player;

public class PlayerEditAction extends GameAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6547847191800890573L;
	public final int editedPlayer;
	public final int sourcePlayer;
	private transient Player sourcePlayerObject;
	private transient Player editedPlayerObject;
	public PlayerEditAction(int source, int sourcePlayer, int editedPlayer) {
		super(source);
		this.sourcePlayer = sourcePlayer;
		this.editedPlayer = editedPlayer;
	}

	public PlayerEditAction(int source, Player sourcePlayer, Player editedPlayer) {
		super(source);
		this.sourcePlayer = (sourcePlayerObject = sourcePlayer).id;
		this.editedPlayer = (editedPlayerObject = editedPlayer).id;
	}

	@Override
	public Player getSourcePlayer(GameInstance instance)
	{
		if (sourcePlayerObject == null)   {sourcePlayerObject = instance.getPlayerById(sourcePlayer);}
		return sourcePlayerObject;
	}
	
	public Player getEditedPlayer(GameInstance instance)
	{
		if (editedPlayerObject == null)   {editedPlayerObject = instance.getPlayerById(editedPlayer);}
		return editedPlayerObject;
	}
	
    @Override 
    public int sourcePlayerId(){return sourcePlayer;}
}
