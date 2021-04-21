package gameObjects.action;

import java.io.Serializable;

import gameObjects.instance.GameInstance;
import main.Player;

public abstract class GameAction implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4972550031815213503L;
	public final int source;
	public long when = System.nanoTime();
	public GameAction(int source)
	{
		this.source = source;
	}

    public int sourcePlayerId() {return -1;}

    public Player getSourcePlayer(GameInstance gameInstance) {return null;}
}
