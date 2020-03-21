package gameObjects.instance;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import gameObjects.GameAction;
import gameObjects.GameObjectInstanceEditAction;
import gameObjects.definition.GameObject;
import main.Player;

public class GameInstance {
	public Game game;
	public String password;
	public String name;
	public boolean hidden = false;
	public final ArrayList<ObjectInstance> objects = new ArrayList<>();
	public final ArrayList<Player> players = new ArrayList<>();
	public final ArrayList<GameAction> actions = new ArrayList<>();
	public final ArrayList<GameChangeListener> changeListener = new ArrayList<GameChangeListener>();
	
	public static interface GameChangeListener
	{
		public void changeUpdate(GameAction action);
	}
	
	public GameInstance(Game game)
	{
		this.game = game;
	}
	
	public Player getPlayer(String name)
	{
		for (int i = 0; i < players.size(); ++i)
		{
			if (players.get(i).name.equals(name))
			{
				return players.get(i);
			}
		}
		return null;
	}
	
	public ObjectInstance getObjectInstance(int id)
	{
		for (int i = 0; i < objects.size(); ++i)
		{
			if (objects.get(i).id == id)
			{
				return objects.get(i);
			}
		}
		return null;
	}
	
	public int getHash()
	{
		int result = 0;
		for (int i = 0; i < objects.size(); ++i)
		{
			result ^= objects.get(i).hashCode();
		}
		for (int i = 0; i < players.size(); ++i)
		{
			result ^= players.get(i).hashCode();
		}
		result ^= name.hashCode();
		result ^= hidden ? 0xB : 0;
		result ^= game.hashCode();
		return result;
	}

	public void update(GameAction action) {
		for (int i = 0; i < changeListener.size(); ++i)
		{
			changeListener.get(i).changeUpdate(action);
		}
	}
}
