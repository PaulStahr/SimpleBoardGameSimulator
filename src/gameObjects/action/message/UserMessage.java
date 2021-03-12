package gameObjects.action.message;

import gameObjects.action.GameAction;
import gameObjects.instance.GameInstance;
import main.Player;

public class UserMessage extends GameAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7825265253504323824L;

	public final int sourcePlayer;
	public final int destinationPlayer;

	private transient Player sourcePl;
	private transient Player destPl;

	@Override
    public Player getSourcePlayer(GameInstance gi)
	{
		if (sourcePl == null){sourcePl = gi.getPlayerById(sourcePlayer);}
		return sourcePl;
	}

	public Player getDestinationPlayer(GameInstance gi)
	{
		if (destPl == null){destPl = gi.getPlayerById(destinationPlayer);}
		return destPl;
	}

	public UserMessage(int source, int sourcePlayer, int destinationPlayer) {
		super(source);
		this.sourcePlayer = sourcePlayer;
		this.destinationPlayer = destinationPlayer;
	}

	public UserMessage(int source, Player sourcePlayer, Player destinationPlayer)
	{
	    this(source, sourcePlayer == null ? -1 : sourcePlayer.id, destinationPlayer == null ? -1 : destinationPlayer.id);
	    this.sourcePl = sourcePlayer;
	    this.destPl = destinationPlayer;
	}
}
