package main.gameObjects;

import main.util.jframe.table.TableColumnType;

public class GameMetaInfo
{
	String id;
	public String name;
	public int connectedPlayerCount;
	public GameMetaInfo(String id)
	{
		this.id = id;
	}
	
	public Object getValue(TableColumnType tableColumnType) {
		GameInstanceColumnType gigt = (GameInstanceColumnType) tableColumnType;
		switch(gigt)
		{
			case DELETE:
				return "delete";
			case CONNECT:
				return "Connect";
			case ID:
				return id;
			case NAME:
				return name;
			default:
				throw new IllegalArgumentException();
		
		}
	}
	
}
