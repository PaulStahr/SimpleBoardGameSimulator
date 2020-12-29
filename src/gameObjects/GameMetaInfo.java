package gameObjects;

import java.io.Serializable;

import gameObjects.instance.GameInstance;

public class GameMetaInfo implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7628362027154634902L;
	public final String id;
	public final String name;
	public final int connectedPlayerCount;
	public final boolean passwordRequired;
	
	public GameMetaInfo(GameInstance gameInstance) {
		this.id = gameInstance.name;
		this.name = gameInstance.name;
		this.connectedPlayerCount = gameInstance.getPlayerNumber();
		this.passwordRequired = !"".equals(gameInstance.password);
	}
	
}
