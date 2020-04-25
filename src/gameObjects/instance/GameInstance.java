package gameObjects.instance;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.GameInstanceColumnType;
import gameObjects.action.GameAction;
import gameObjects.action.GamePlayerEditAction;
import main.Player;
import util.ArrayTools;
import util.jframe.table.TableColumnType;

public class GameInstance {
	public static final List<TableColumnType> TYPES = ArrayTools.unmodifiableList(new GameInstanceColumnType[]{GameInstanceColumnType.ID, GameInstanceColumnType.NAME, GameInstanceColumnType.CONNECT, GameInstanceColumnType.DELETE});
	public static final Logger logger = LoggerFactory.getLogger(GameInstance.class);
	public final Game game;
	public String password;
	public String name;
	public boolean hidden = false;
	public final ArrayList<ObjectInstance> objects = new ArrayList<>();
	public final ArrayList<Player> players = new ArrayList<>();
	public final ArrayList<GameAction> actions = new ArrayList<>();
	private final ArrayList<GameChangeListener> changeListener = new ArrayList<GameChangeListener>();
	
	public static interface GameChangeListener
	{
		public void changeUpdate(GameAction action);
	}
	
	public GameInstance(Game game, String name)
	{
		this.game = game;
		this.name = name;
	}
	
	public void clear()
	{
		objects.clear();
		players.clear();
		game.clear();
	}
	
	public Player addPlayer(Player player)
	{
		Player pl = getPlayer(player.id);
		if (pl != null)
		{
			pl.set(player);
		}else {
			players.add(player);
			pl = player;
		}
		update(new GamePlayerEditAction(0, pl, pl));
		return pl;		
	}
	
	public Player getPlayer(int id)
	{
		for (int i = 0; i < players.size(); ++i)
		{
			if (players.get(i).id == id)
			{
				return players.get(i);
			}
		}
		return null;
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

	public ObjectInstance addObjectInstance(ObjectInstance objectInstance)
	{
		ObjectInstance oi = getObjectInstance(objectInstance.id);
		if (oi != null)
		{
			oi.updateState(objectInstance.state);
			oi.scale = objectInstance.scale;
			return oi;
		}
		objects.add(objectInstance);
		return objectInstance;
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
			try
			{
				changeListener.get(i).changeUpdate(action);
			}catch(Exception e)
			{
				logger.error("Error in Change Listener", e);
			}
		}
	}

	public String[] getPlayerNames() {
		String names[] = new String[players.size()];
		for (int i = 0; i < players.size(); ++i)
		{
			names[i] = players.get(i).name;
		}
		return names;
	}

	public void remove(ObjectInstance objectInstance) {
		objects.remove(objectInstance);
		//TODO clean up references
	}

	public void addChangeListener(GameChangeListener listener) {
		changeListener.add(listener);
	}
}
