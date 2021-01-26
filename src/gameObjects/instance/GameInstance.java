package gameObjects.instance;

import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import gameObjects.functions.CheckingFunctions;
import main.Player;
import util.ArrayTools;
import util.jframe.table.TableColumnType;

public class GameInstance {
	public static final List<TableColumnType> TYPES = ArrayTools.unmodifiableList(new GameInstanceColumnType[]{GameInstanceColumnType.ID, GameInstanceColumnType.NAME,   GameInstanceColumnType.NUM_PLAYERS, GameInstanceColumnType.NUM_VISITORS, GameInstanceColumnType.CONNECT, GameInstanceColumnType.VISIT, GameInstanceColumnType.DELETE});
	public static final Logger logger = LoggerFactory.getLogger(GameInstance.class);
	public final Game game;
	public String password;
	public String name;
	public boolean hidden = false;
	private final ArrayList<ObjectInstance> objects = new ArrayList<>();
	private final List<ObjectInstance> unmodifiableObjectInstanceList = Collections.unmodifiableList(objects);
	private final ArrayList<Player> players = new ArrayList<>();
	private final List<Player> unmodifiablePlayer = Collections.unmodifiableList(players);
	private final ArrayList<GameAction> actions = new ArrayList<>();
	private final ArrayList<GameChangeListener> changeListener = new ArrayList<GameChangeListener>();
    public boolean private_area = true;
	public boolean table = true;
    public boolean put_down_area = true;
    public int seats = -1;
	public List<String> seatColors = new ArrayList<>(Arrays.asList("#e81123", "#00188f", "#009e49", "#ff8c00", "#68217a", "#00bcf2", "#ec008c", "#fff100", "#00b294", "#bad80a"));
	public String tableColor = "";
	public int admin = -1;
	public boolean debug_mode = false;
	private long maxDrawValue = 0;
	public int tableRadius = 1200;
	
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
	
	public int getMaxDrawValue()
	{
		//return maxDrawValue;
        int maxDrawValue = 0;
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
			player.setPlayerColor(this);
			update(new GamePlayerEditAction(action == null ? 0 : action.source, pl, pl));
			return pl;
		}else {
			players.add(player);
			if (this.admin == -1){
				this.admin = player.id;
			}
			player.setPlayerColor(this);
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

	public Player getPlayerByIndex(int idx){return players.get(idx);}

	public int getPlayerNumber(){return getPlayerNumber(false);}

	public int getPlayerNumber(boolean with_visitors)
	{
		if (with_visitors)
		{
			return players.size();
		}
		else{
			int non_visitors = 0;
			for (Player player : players){
				if (!player.visitor){
					++non_visitors;
				}
			}
			return non_visitors;
		}
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
			oi.scale = objectInstance.scale *= objectInstance.state.scale;
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

	public ObjectInstance getObjectInstanceByIndex(int index){return this.objects.get(index);}

	public int getObjectNumber(){return this.objects.size();}
	
	public GameObject getObjectByIndex(int index){return this.game.objects.get(index);}
	
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
			GameObjectInstanceEditAction editAction = (GameObjectInstanceEditAction) action;
			ObjectInstance oi = editAction.getObject(this);
			maxDrawValue = max(maxDrawValue , oi.state.drawValue);
			oi.state.set(editAction.state);
		}
		else if (action instanceof GameStructureEditAction)
		{
			GameStructureEditAction structureAction = (GameStructureEditAction)action;
			if (structureAction instanceof GameStructureObjectEditAction)
			{
				GameStructureObjectEditAction gsoea = (GameStructureObjectEditAction)structureAction;
				switch (gsoea.type) {
					case GameStructureEditAction.REMOVE_PLAYER:
					{
						for (int i = 0; i < objects.size(); ++i)
						{
							if (objects.get(i).state.owner_id == gsoea.objectId)
							{
								objects.get(i).state.owner_id = -1;
								objects.get(i).state.inPrivateArea = false;
							}
							if (objects.get(i).state.isSelected == gsoea.objectId)
							{
								objects.get(i).state.isSelected = -1;
								objects.get(i).state.isActive = false;
							}
						}
						players.remove(getPlayerById(gsoea.objectId));
						break;
					}
					case GameStructureEditAction.REMOVE_OBJECT_INSTANCE:
					{
						ObjectInstance oi = getObjectInstanceById(gsoea.objectId);
						if (oi.state.aboveInstanceId != -1)
						{
							getObjectInstanceById(oi.state.aboveInstanceId).state.belowInstanceId = oi.state.belowInstanceId;
							oi.state.aboveInstanceId = -1;
						}
						if (oi.state.belowInstanceId != -1)
						{
							getObjectInstanceById(oi.state.belowInstanceId).state.aboveInstanceId = oi.state.aboveInstanceId;
							oi.state.belowInstanceId = -1;
						}
						objects.remove(gsoea.objectId);
						break;
					}
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
		update(new GameStructureObjectEditAction(source, GameStructureEditAction.REMOVE_OBJECT_INSTANCE, objectInstance.id));
	}

	public List<ObjectInstance> getObjectInstanceList() {return unmodifiableObjectInstanceList;}

	public List<Player> getPlayerList() {
		List<Player> playerList = new ArrayList<>();
		for (Player player : players){
			if (!player.visitor){
				playerList.add(player);
			}
		}
		return Collections.unmodifiableList(playerList);
	}

	public List<Player> getPlayerList(boolean with_visitors){return with_visitors ? unmodifiablePlayer : getPlayerList();}

	public void remove(int source, GameObject object) {
		game.objects.remove(object);
		update(new GameStructureObjectEditAction(source, GameStructureEditAction.REMOVE_OBJECT, object.uniqueObjectName.hashCode()));
	}
	
	public void getOwnedPrivateObjects(int player_id, boolean inPrivateArea, ArrayList<ObjectInstance> result)
	{
		for (int i = 0; i < objects.size(); ++i)
		{
			ObjectInstance oi = objects.get(i);
			if (oi.owner_id() == player_id && oi.state.inPrivateArea == inPrivateArea)
			{
				result.add(objects.get(i));
			}
		}
	}
	
	private static void makeStack(ArrayList<ObjectInstance> tmp, int begin, int end)
	{
		if (begin != end)
		{
			ObjectInstance last = tmp.get(begin);
			last.state.belowInstanceId = -1;
			for (int i = begin + 1; i < end; ++i)
			{
				ObjectInstance current = tmp.get(i);
				last.state.aboveInstanceId = current.id;
				current.state.belowInstanceId = last.id;
				last = current;
			}
			last.state.aboveInstanceId = -1;
		}
	}
	
	private void update(ArrayList<ObjectInstance> list, int begin, int end, Player player)
	{
		for (int i = begin; i < end; ++i)
		{
			update(new GameObjectInstanceEditAction(-1, player, list.get(i), list.get(i).state.copy()));
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
		getOwnedPrivateObjects(player_id, true, tmp);
		ArrayList<ObjectInstance> output = new ArrayList<>();
		tmp.sort(ObjectInstance.ID_COMPARATOR);
		int incoming[] = new int[tmp.size()];
		CheckingFunctions.countIncoming(tmp, incoming);
		for (int read = 0; read < incoming.length;)
		{
			if (incoming[read] == 0){CheckingFunctions.packBelongingObjects(incoming, read, tmp, output);}
		}
		makeStack(output, 0, output.size());
		update(output, 0, output.size(), player);
		output.clear();
		tmp.clear();
		getOwnedPrivateObjects(player_id, false, tmp);
		tmp.sort(ObjectInstance.ID_COMPARATOR);
		incoming = new int[tmp.size()];
		CheckingFunctions.countIncoming(tmp, incoming);
		for (int read = 0; read < incoming.length;)
		{
			if (incoming[read] == 0)
			{
				CheckingFunctions.packBelongingObjects(incoming, read, tmp, output);
				makeStack(output, 0, output.size());
				update(output, 0, output.size(), player);
				output.clear();
			}
		}
	}

	public void remove(int source, Player player) {
		players.remove(player);
		update(new GameStructureObjectEditAction(source, GameStructureEditAction.REMOVE_PLAYER, player.id));
	}

	public void addChangeListener(GameChangeListener listener) {changeListener.add(listener);}
	public GameChangeListener getChangeListener(int index){return changeListener.get(index);}
	public int getChangeListenerCount() {return changeListener.size();}
}
