package gameObjects.action;

import java.io.Serializable;

public abstract class GameAction  implements Serializable{
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
	
}
