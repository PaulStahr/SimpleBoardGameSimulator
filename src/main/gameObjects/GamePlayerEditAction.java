package main.gameObjects;

public class GamePlayerEditAction extends GameAction{
	public final Player object;
	public final Player player;
	public GamePlayerEditAction(int source, Player player, Player activeObject) {
		super(source);
		if (player == null)
		{
			throw new NullPointerException();
		}
		this.player = player;
		this.object = activeObject;
	}

}
