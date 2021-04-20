package gameObjects.instance;

import static java.lang.Math.max;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.action.DestroyInstance;
import gameObjects.action.GameAction;
import gameObjects.action.GameObjectEditAction;
import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.action.player.PlayerAddAction;
import gameObjects.action.player.PlayerCharacterPositionUpdate;
import gameObjects.action.player.PlayerEditAction;
import gameObjects.action.player.PlayerMousePositionUpdate;
import gameObjects.action.player.PlayerRemoveAction;
import gameObjects.action.structure.GameStructureEditAction;
import gameObjects.action.structure.GameStructureObjectEditAction;
import gameObjects.action.structure.GameTextureRemoveAction;
import gameObjects.columnTypes.GameInstanceColumnType;
import gameObjects.definition.GameObject;
import gameObjects.definition.GameObjectBook;
import gameObjects.definition.GameObjectDice;
import gameObjects.definition.GameObjectFigure;
import gameObjects.definition.GameObjectToken;
import gameObjects.functions.CheckingFunctions;
import gameObjects.functions.ObjectFunctions;
import main.Player;
import util.ArrayTools;
import util.ListTools;
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
	public boolean drawTable = true;
    public boolean put_down_area = true;
    public int seats = -1;
    public final ArrayList<Color> seatColors = new ArrayList<>();
	public String tableColor = "";
	public int admin = -1;
	public boolean debug_mode = false;
    public boolean initial_mode = true;
	public float cardOverlap = (float) (2/3.0);
	private long maxDrawValue = 0;
	public int tableRadius = 1200;
	private final StampedLock lock = new StampedLock();
	
    public final Comparator<Integer> idObjectInstanceDrawValueComparator = new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return GameInstance.this.getObjectInstanceById(o1).state.drawValue - GameInstance.this.getObjectInstanceById(o2).state.drawValue;
        }
    };


	public static interface GameChangeListener
	{
		public void changeUpdate(GameAction action);
	}
	
	public GameInstance(GameInstance other)
	{
	    this(new Game(other.game), other.name);
	    this.password = other.password;
	    this.name = other.name;
	    this.hidden =other.hidden;
	    for (ObjectInstance oi : other.objects){objects.add(oi.copy());}
	    for (Player pl : other.players)        {players.add(pl.copy());}
	}

	public GameInstance(Game game, String name)
	{
	    String colors[] =  {"#e81123", "#00188f", "#009e49", "#ff8c00", "#68217a", "#00bcf2", "#ec008c", "#fff100", "#00b294", "#bad80a"};
	    for (int i = 0; i < colors.length; ++i)
	    {
	        seatColors.add(Color.decode(colors[i]));
	    }
		this.game = game;
		this.name = name;
	}
	
	public void clear()
	{
		objects.clear();
		players.clear();
		game.clear();
	}

	public void begin_play() {
		//Stack all card objects with same position and group
		if (this.initial_mode == true) {
			ObjectFunctions.loadInitialState(this);
		}
		this.initial_mode = false;
	}


	public int getMaxDrawValue()
	{
		//return maxDrawValue;
        int maxDrawValue = 0;
        for (int idx = 0; idx<getObjectInstanceCount(); ++idx){
            maxDrawValue = max(maxDrawValue, getObjectInstanceByIndex(idx).state.drawValue);
        }
        return maxDrawValue;
	}
	
	
	public Player addPlayer(PlayerAddAction action, Player player)
	{
		Player pl = action == null ? getPlayerById(player.id) : action.getPlayer(this);
		if (pl != null)
		{
			pl.set(player);
			update(new PlayerEditAction(action == null ? 0 : action.source, pl, pl));
			return pl;
		}else {
			players.add(player);
			if (this.admin == -1){
				this.admin = player.id;
			}
			update(action == null ? new PlayerAddAction(0, player) : action);
			return player;
		}
	}
	
	public Player getPlayerById(int id)
	{
        int tries = 0;
        while (true)
        {
            long stamp = lock.tryOptimisticRead();
            lock:{
                try
                {
            		for (int i = 0; i < players.size(); ++i)
            		{
            			if (players.get(i).id == id)
            			{
                            if (!lock.validate(stamp)){break lock;}
               				return players.get(i);
            			}
            		}
                	if (lock.validate(stamp)){return null;}
                }catch (Exception e) {
                    if (tries < 5){logger.debug("Possible Read-Write Invalidation retry " + (tries++) +" of 5");}
                    else          {throw e;}
                }
            }
        }	
	}

	public Player getPlayerByIndex(int idx){return players.get(idx);}

	public int getPlayerCount(){return getPlayerCount(false);}

	public int getPlayerCount(boolean with_visitors)
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
	    int tries = 0;
	    while (true)
	    {
	        long stamp = lock.tryOptimisticRead();
	        lock:{
                try
                {
            		for (int i = 0; i < players.size(); ++i)
            		{
            		    Player pl = players.get(i);
            			if (name.equals(pl.getName()))
            			{
            			    if (lock.validate(stamp)){return pl;}
            			    break lock;
            			}
            		}
            		if (lock.validate(stamp)){return null;}
        		}catch (Exception e) {
        		    if (tries < 5){logger.debug("Possible Read-Write Invalidation retry " + (tries++) +" of 5");}
        		    else          {throw e;}
        		}
	        }
	    }
	}

	public ObjectInstance addObjectInstance(ObjectInstance objectInstance)
	{
		ObjectInstance res = getObjectInstanceById(objectInstance.id);
        if (res != null)
		{
	        res.updateState(objectInstance.state);
			res.scale = objectInstance.scale *= objectInstance.state.scale;
            return res;
		}
		objects.add(objectInstance);
		return objectInstance;
	}
	
	public ObjectInstance getObjectInstanceById(int id)
	{
        int tries = 0;
        while (true)
        {
            long stamp = lock.tryOptimisticRead();
            lock:{
                try
                {
            		for (int i = 0; i < objects.size(); ++i)
            		{
            		    ObjectInstance current = objects.get(i);
            			if (current.id == id)
            			{
                            if (!lock.validate(stamp)){break lock;}
            				return current;
            			}
            		}
                    if (lock.validate(stamp)){return null;}
                }catch (Exception e) {
                    if (tries < 5){logger.debug("Possible Read-Write Invalidation retry " + (tries++) +" of 5");}
                    else          {throw e;}
                }
            }
        }
	}

	public ObjectInstance getObjectInstanceByIndex(int index){return this.objects.get(index);}

	public int getObjectInstanceCount(){return this.objects.size();}
	
	public GameObject getObjectByIndex(int index){return this.game.getObjectByIndex(index);}
	
	@Override
    public int hashCode()
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
	    Player sourcePlayer = action.getSourcePlayer(this);
	    if (sourcePlayer != null)
        {
	        sourcePlayer.lastReceivedSignal = System.nanoTime();
        }
	    if (action instanceof PlayerMousePositionUpdate)
	    {
	        PlayerMousePositionUpdate pmpu = (PlayerMousePositionUpdate)action;
	        pmpu.getEditedPlayer(this).setMousePos(pmpu.mouseX, pmpu.mouseY);
	    }
	    else if (action instanceof PlayerCharacterPositionUpdate)
        {
	        PlayerCharacterPositionUpdate pcpu = (PlayerCharacterPositionUpdate)action;
	        Player edited = pcpu.getEditedPlayer(this);
	        edited.screenWidth = pcpu.screenWidth;
	        edited.screenHeight = pcpu.screenHeight;
	        edited.screenToBoardTransformation.setTransform(pcpu.scaleX, pcpu.shearY, pcpu.shearX, pcpu.scaleY, pcpu.translateX, pcpu.translateY);
        }
	    else if (action instanceof GameTextureRemoveAction)
        {
	        String textureName = ((GameTextureRemoveAction)action).textureName;
	        game.images.remove(textureName);
	        for (int i = 0; i < game.getGameObjectCount(); ++i)
	        {
	            GameObject go = game.getObjectByIndex(i);
	            if (go instanceof GameObjectToken) {
	                GameObjectToken token = (GameObjectToken)go;
                    if (textureName.equals(token.getUpsideLookId()))
                    {
                        token.setUpsideLook(null);
                    }
                    if (textureName.equals(token.getDownsideLookId()))
                    {
                        token.setDownsideLook(null);
                    }
	            }
	        }
        }
	    else if (action instanceof GameObjectEditAction)
		{
			GameObject obj = ((GameObjectEditAction) action).getObject(this);
			obj.updateImages(this);
		}
		else if (action instanceof GameObjectInstanceEditAction)
		{
			GameObjectInstanceEditAction editAction = (GameObjectInstanceEditAction) action;
			ObjectInstance oi = editAction.getObject(this);
			if (oi == null)
			{
			    logger.error("Can't find edited object " + editAction.object);
			}else {
			    if (oi.state.equals(editAction.state)) {return;}
                maxDrawValue = max(maxDrawValue , oi.state.drawValue);
		        oi.state.set(editAction.state);
			}
		}
		else if (action instanceof PlayerRemoveAction)
		{
			PlayerRemoveAction rpa = (PlayerRemoveAction)action;
			for (int i = 0; i < objects.size(); ++i)
			{    
			    ObjectInstance oi = objects.get(i);
			    ObjectState state = oi.state;
				if (state.owner_id == rpa.editedPlayer)
				{
					state.owner_id = -1;
					state.inPrivateArea = false;
				}
				if (state.isSelected == rpa.editedPlayer)
				{
					state.isSelected = -1;
					state.isActive = false;
				}
			}
			players.remove(rpa.getEditedPlayer(this));
		}
		else if (action instanceof GameStructureEditAction)
		{
			GameStructureEditAction structureAction = (GameStructureEditAction)action;
			if (structureAction instanceof GameStructureObjectEditAction)
			{
				GameStructureObjectEditAction gsoea = (GameStructureObjectEditAction)structureAction;
				switch (gsoea.type) {
					case GameStructureEditAction.REMOVE_OBJECT_INSTANCE:
					{
						ObjectInstance oi = getObjectInstanceById(gsoea.objectId);
						ObjectState state = oi.state;
						if (state.aboveInstanceId != -1)
						{
							getObjectInstanceById(state.aboveInstanceId).state.belowInstanceId = state.belowInstanceId;
							state.aboveInstanceId = -1;
						}
						if (oi.state.belowInstanceId != -1)
						{
							getObjectInstanceById(state.belowInstanceId).state.aboveInstanceId = state.aboveInstanceId;
							state.belowInstanceId = -1;
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
		if (action instanceof DestroyInstance)
        {
            changeListener.clear();
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

	public List<ObjectInstance> getTokenList() {
		List<ObjectInstance> oiList = new ArrayList<>();
		for (ObjectInstance oi : unmodifiableObjectInstanceList){
			if (oi.go instanceof GameObjectToken){
				oiList.add(oi);
			}
		}
		return Collections.unmodifiableList(oiList);
	}

	public List<ObjectInstance> getFigureList() {
		List<ObjectInstance> oiList = new ArrayList<>();
		for (ObjectInstance oi : unmodifiableObjectInstanceList){
			if (oi.go instanceof GameObjectFigure){
				oiList.add(oi);
			}
		}
		return Collections.unmodifiableList(oiList);
	}

	public List<ObjectInstance> getDiceList() {
		List<ObjectInstance> oiList = new ArrayList<>();
		for (ObjectInstance oi : unmodifiableObjectInstanceList){
			if (oi.go instanceof GameObjectDice){
				oiList.add(oi);
			}
		}
		return Collections.unmodifiableList(oiList);
	}

	public List<ObjectInstance> getBookList() {
		List<ObjectInstance> oiList = new ArrayList<>();
		for (ObjectInstance oi : unmodifiableObjectInstanceList){
			if (oi.go instanceof GameObjectBook){
				oiList.add(oi);
			}
		}
		return Collections.unmodifiableList(oiList);
	}

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
		game.removeObject(object);
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
		for (int read = 0; read < incoming.length;++read)
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
		for (int read = 0; read < incoming.length;++read)
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

	public void addChangeListener(GameChangeListener listener) {changeListener.add(listener);}
	public GameChangeListener getChangeListener(int index){return changeListener.get(index);}
	public int getChangeListenerCount() {return changeListener.size();}
	public void removeChangeListener(GameChangeListener listener) {changeListener.remove(listener);}
    public void getObjects(ArrayList<ObjectInstance> oiList) {oiList.addAll(objects);}

    public Player getPlayer(Predicate<Player> sameSeatPredicate) {return ListTools.get(sameSeatPredicate, players);}
}
