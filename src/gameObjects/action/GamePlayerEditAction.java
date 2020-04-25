package gameObjects.action;

import main.Player;

public class GamePlayerEditAction extends GameAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6547847191800890573L;
	public final Player editedPlayer;
	public final int sourcePlayer;
	public GamePlayerEditAction(int source, Player player, Player activeObject) {
		super(source);
		if (player == null)
		{
			throw new NullPointerException();
		}
		this.sourcePlayer = player.id;
		this.editedPlayer = activeObject;
	}

}
