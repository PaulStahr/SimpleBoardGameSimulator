package gameObjects;

import java.io.Serializable;

import gameObjects.instance.GameInstance;

public class GameMetaInfo implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7628362027154634902L;
	String id;
	public String name;
	public int connectedPlayerCount;
	public GameMetaInfo(String id)
	{
		this.id = id;
	}
	public GameMetaInfo(GameInstance gameInstance) {
		this.id = gameInstance.name;
		this.name = gameInstance.name;
		this.connectedPlayerCount = gameInstance.getPlayerNumber();
	}
	
}
