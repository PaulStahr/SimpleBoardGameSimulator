package gameObjects.instance;

import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.GameInstanceColumnType;
import gameObjects.action.AddPlayerAction;
import gameObjects.action.GameAction;
import gameObjects.action.GameObjectEditAction;
import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.action.GamePlayerEditAction;
import gameObjects.action.GameStructureEditAction;
import gameObjects.action.GameStructureObjectEditAction;
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
		//return maxDrawValue;
        long maxDrawValue = 0;
        for (int idx = 0; idx<getObjectNumber(); ++idx){
            maxDrawValue = max(maxDrawValue, getObjectInstanceByIndex(idx).state.drawValue);
        }
        return maxDrawValue;
	}
	
	
	public Player addPlayer(AddPlayerAction action, Player player)
	{
		Player pl = action == null ? getPlayerById(player.id) : action.getPlayer(this);
		if (pl != null)
		{
			pl.set(player);
			update(new GamePlayerEditAction(action == null ? 0 : action.source, pl, pl));
			return pl;
		}else {
			players.add(player);
			update(action == null ? new AddPlayerAction(0, player) : action);
			return player;
		}
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
			maxDrawValue = max(maxDrawValue , oi.state.drawValue);
		}
		else if (action instanceof GameStructureEditAction)
		{
			GameStructureEditAction structureAction = (GameStructureEditAction)action;
			if (structureAction instanceof GameStructureObjectEditAction)
			{
				GameStructureObjectEditAction gsoea = (GameStructureObjectEditAction)structureAction;
				if (gsoea.type == GameStructureEditAction.REMOVE_PLAYER)
				{
					
					for (int i = 0; i < objects.size(); ++i)
					{
						if (objects.get(i).state.owner_id == gsoea.objectId)
						{
							objects.get(i).state.owner_id = -1;
							objects.get(i).state.inPrivateArea = false;
						}
					}
					players.remove(getPlayerById(gsoea.objectId));
				}
			}
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

	public void remove(int source, ObjectInstance objectInstance) {
		objects.remove(objectInstance);
		update(new GameStructureObjectEditAction(source, GameStructureEditAction.REMOVE_OBJECT_INSTANCE, objectInstance.id));
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


	public void remove(int source, GameObject object) {
		game.objects.remove(object);
		update(new GameStructureObjectEditAction(source, GameStructureEditAction.REMOVE_OBJECT, object.uniqueName.hashCode()));
	}
	
	public void getOwnedObjects(int player_id, ArrayList<ObjectInstance> result)
	{
		for (int i = 0; i < objects.size(); ++i)
		{
			if (objects.get(i).owner_id() == player_id)
			{
				result.add(objects.get(i));
			}
		}
	}
	
	public boolean checkPlayerConsistency(int player_id, ArrayList<ObjectInstance> tmp)
	{
		tmp.clear();
		getOwnedObjects(player_id, tmp);
		ObjectInstance bottom = null;
		ObjectInstance top = null;
		for (int i = 0; i < tmp.size(); ++i)
		{
			ObjectInstance oi = tmp.get(i);
			if (oi.state.belowInstanceId == -1)
			{
				if (bottom != null)
				{
					return false;
				}
				bottom = oi;
			}
			if (oi.state.aboveInstanceId == -1)
			{
				if (top != null)
				{
					return false;
				}
				top = oi;
			}
		}
		ObjectInstance current = bottom;
		for (int i = 1; i < tmp.size(); ++i)
		{
			ObjectInstance next = getObjectInstanceById(current.state.aboveInstanceId);
			if (next == null || next.state.belowInstanceId != current.id)
			{
				return false;
			}
			current = next;
		}
		return current == top;
	}
	
	private static final Comparator<ObjectInstance> aboveCompare = new Comparator<ObjectInstance>() {
		@Override
		public int compare(ObjectInstance a, ObjectInstance b) {
			if (a.state.aboveInstanceId == b.id)
			{
				return 1;
			}
			if (b.state.aboveInstanceId == a.id)
			{
				return -1;
			}
			return 0;
		}
	};
	
	private void makeStack(ArrayList<ObjectInstance> tmp)
	{
		if (tmp.size() != 0)
		{
			ObjectInstance last = tmp.get(0);
			last.state.belowInstanceId = -1;
			for (int i = 1; i < tmp.size(); ++i)
			{
				ObjectInstance current = tmp.get(i);
				last.state.aboveInstanceId = current.id;
				current.state.belowInstanceId = last.id;
				last = current;
			}
			last.state.aboveInstanceId = -1;
		}
	}
	
	/**
	 * Collects all cards of a given player and makes a new stack out of it. Only the aboveId is considered. If there are more than one stack, then these will be merged, keeping the parial order. Circles will be put in at random order
	 * @param player_id
	 * @param player
	 * @param tmp
	 */
	public void repairPlayerConsistency(int player_id, Player player, ArrayList<ObjectInstance> tmp)
	{
		tmp.clear();
		getOwnedObjects(player_id, tmp);
		
		tmp.sort(ObjectInstance.ID_COMPARATOR);
		int incoming[] = new int[tmp.size()];
		for (int i = 0; i < tmp.size(); ++i)
		{
			ObjectInstance current = tmp.get(i);
			if (current.state.aboveInstanceId != -1)
			{
				int aboveIdx = ArrayTools.binarySearch(tmp, current.state.aboveInstanceId, ObjectInstance.OBJECT_TO_ID);
				if (aboveIdx >= 0)
				{
					++incoming[aboveIdx];
				}
			}
		}
		for (int read = 0, write = 0; read < incoming.length; ++read)
		{
			read = Math.max(write, read);
			if (incoming[read] == 0)
			{
				ObjectInstance current = tmp.get(read);
				int curIdx = read;
				while(true){
					incoming[curIdx] = incoming[write];
					incoming[write] = 0;
					tmp.set(curIdx, tmp.get(write));
					tmp.set(write, current);
					++write;
					curIdx = ArrayTools.binarySearch(tmp, current.state.aboveInstanceId, ObjectInstance.OBJECT_TO_ID);
					if (curIdx < 0)
					{
						break;
					}
					current = tmp.get(curIdx);
					--incoming[curIdx];
					if (incoming[curIdx] != 0)
					{
						break;
					}
				}
			}
		}
		makeStack(tmp);
		for (int i = 0; i < tmp.size(); ++i)
		{
			update(new GameObjectInstanceEditAction(-1, player, tmp.get(i)));
		}
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
		update(new GameStructureObjectEditAction(source, GameStructureEditAction.REMOVE_PLAYER, player.id));
	}
}
