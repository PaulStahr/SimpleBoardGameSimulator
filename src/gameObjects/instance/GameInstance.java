package gameObjects.instance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.GameInstanceColumnType;
import gameObjects.action.GameAction;
import gameObjects.action.GameObjectEditAction;
import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.action.GamePlayerEditAction;
import gameObjects.definition.GameObject;
import main.Player;
import util.ArrayTools;
import util.jframe.table.TableColumnType;

public class GameInstance {
	public static final List<TableColumnType> TYPES = ArrayTools.unmodifiableList(new GameInstanceColumnType[]{GameInstanceColumnType.ID, GameInstanceColumnType.NAME, GameInstanceColumnType.NUM_PLAYERS, GameInstanceColumnType.CONNECT, GameInstanceColumnType.DELETE});
	public static final Logger logger = LoggerFactory.getLogger(GameInstance.class);
	public final Game game;
	public String password;
	public String name;
	public boolean hidden = false;
	private final ArrayList<ObjectInstance> objects = new ArrayList<>();
	private final ArrayList<Player> players = new ArrayList<>();
	private final ArrayList<GameAction> actions = new ArrayList<>();
	private final ArrayList<GameChangeListener> changeListener = new ArrayList<GameChangeListener>();
	private long maxDrawValue = 0;
	
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
	
	public long getMaxDrawValue()
	{
		return maxDrawValue;
        /*long maxDrawValue = 0;
        for (int idx = 0; idx<getObjectNumber(); ++idx){
            maxDrawValue = max(maxDrawValue, getObjectInstanceByIndex(idx).state.drawValue);
        }
        return maxDrawValue;*/
	}
	
	
	public Player addPlayer(Player player)
	{
		Player pl = getPlayerById(player.id);
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
	
	public Player getPlayerById(int id)
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
	public Player getPlayerByIndex(int idx)
	{
		return players.get(idx);
	}
	public int getPlayerNumber()
	{
		return players.size();
	}
	
	public Player getPlayerByName(String name)
	{
		for (int i = 0; i < players.size(); ++i)
		{
			if (players.get(i).getName().equals(name))
			{
				return players.get(i);
			}
		}
		return null;
	}

	public ObjectInstance addObjectInstance(ObjectInstance objectInstance)
	{
		ObjectInstance oi = getObjectInstanceById(objectInstance.id);
		if (oi != null)
		{
			oi.updateState(objectInstance.state);
			oi.scale = objectInstance.scale;
			return oi;
		}
		objects.add(objectInstance);
		return objectInstance;
	}
	
	public ObjectInstance getObjectInstanceById(int id)
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

	public ObjectInstance getObjectInstanceByIndex(int index)
	{
		return this.objects.get(index);
	}

	public int getObjectNumber(){
		return this.objects.size();
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
		if (action instanceof GameObjectEditAction)
		{
			GameObject obj = ((GameObjectEditAction) action).getObject(this);
			obj.updateImages(this);
		}
		else if (action instanceof GameObjectInstanceEditAction)
		{
			ObjectInstance oi = ((GameObjectInstanceEditAction) action).getObject(this);
			maxDrawValue = Math.max(maxDrawValue , oi.state.drawValue);
		}
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
			names[i] = players.get(i).getName();
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

	public List<ObjectInstance> getObjectInstanceList() {
		return Collections.unmodifiableList(objects);
	}

	public List<Player> getPlayerList() {
		return Collections.unmodifiableList(players);
	}

	public void remove(GameObject object) {
		game.objects.remove(object);
	}

	public void remove(int source, Player player) {
		for (int i = 0; i < objects.size(); ++i)
		{
			if (objects.get(i).state.owner_id == player.id)
			{
				objects.get(i).state.owner_id = -1;
				objects.get(i).state.inPrivateArea = false;
				update(new GameObjectInstanceEditAction(source, player, objects.get(i)));
			}
		}
		players.remove(player);
	}
}
