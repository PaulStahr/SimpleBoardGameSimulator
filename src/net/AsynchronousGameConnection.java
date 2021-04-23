package net;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.DataHandler;
import data.Texture;
import gameObjects.action.AddObjectAction;
import gameObjects.action.AtomicAction;
import gameObjects.action.DestroyInstance;
import gameObjects.action.GameAction;
import gameObjects.action.GameObjectEditAction;
import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.action.message.UserFileMessage;
import gameObjects.action.message.UserSoundMessageAction;
import gameObjects.action.message.UsertextMessageAction;
import gameObjects.action.player.PlayerAddAction;
import gameObjects.action.player.PlayerCharacterPositionUpdate;
import gameObjects.action.player.PlayerEditAction;
import gameObjects.action.player.PlayerMousePositionUpdate;
import gameObjects.action.player.PlayerRemoveAction;
import gameObjects.action.structure.GameStructureEditAction;
import gameObjects.action.structure.GameTextureRemoveAction;
import gameObjects.instance.GameInstance;
import gameObjects.instance.GameInstance.GameChangeListener;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.ObjectState;
import gui.minigames.TetrisGameInstance.TetrisGameEvent;
import io.GameIO;
import io.ObjectStateIO;
import io.PlayerIO;
import main.Player;
import util.ArrayUtil;
import util.StringUtils;
import util.data.UniqueObjects;
import util.io.StreamUtil;
import util.stream.CappedInputStreamWrapper;

public class AsynchronousGameConnection implements Runnable, GameChangeListener{
    private static final Logger logger = LoggerFactory.getLogger(AsynchronousGameConnection.class);
    private final GameInstance gi;
    private Thread outputThread;
    private Thread inputThread;
    private InputStream input;
    private final OutputStream output;
    private final ArrayDeque<Object> queuedOutputs = new ArrayDeque<>();
    public final int id = (int)(Math.random() * Integer.MAX_VALUE);
    private ObjectInputStream objIn;
    private boolean stopOnError = true;
    private int outputEvents = 0;
    private int inputEvents = 0;
    private long otherToThisOffset = Long.MAX_VALUE / 10;
    private long otherTimingOffset = Long.MIN_VALUE;
    private boolean stop = false;
    public int blocksize = 0;
    private final Random random = new Random();
    private byte[] randBytes = UniqueObjects.EMPTY_BYTE_ARRAY;
    private Socket socket;//TODO close on exit
    private boolean destroyed = false;

    public int getInEvents(){return inputEvents;}
    public int getOutEvents(){return outputEvents;}
    public Socket getSocket(){return socket;}

    @Override
    public void changeUpdate(GameAction action) {
        if (Thread.currentThread() != inputThread && !stop) //TODO this is only a workaround
        {
            if (logger.isDebugEnabled()){logger.debug("Queue Action" + action);}
            queueOutput(action);
        }
        if (action instanceof DestroyInstance)
        {
            stop = true;
        }
    }

    private void queueOutput(Object output)
    {
        if (logger.isDebugEnabled()){logger.debug("Add queue " + id + " output");}
        synchronized(queuedOutputs)
        {
            queuedOutputs.add(output);
            queuedOutputs.notifyAll();
        }
    }

    public GameInstance getGameInstance(){return gi;}

    /**
     * Constructs an connection with the GameInstance and the two streams.
     * @param gi
     * @param input
     * @param output
     */
    public AsynchronousGameConnection(GameInstance gi, ObjectInputStream input, OutputStream output, Socket socket)
    {
        this.gi = gi;
        gi.addChangeListener(this);
        this.objIn = input;
        this.output = output;
    }
    
    /**
     * Constructs an connection with the GameInstance and the two streams.
     * @param gi
     * @param input
     * @param output
     * @param server 
     */
    public AsynchronousGameConnection(GameInstance gi, InputStream input, OutputStream output, Socket socket)
    {
        this.gi = gi;
        gi.addChangeListener(this);
        this.input = input;
        this.output = output;
        this.socket = socket;
    }

    public void syncPull()    {queueOutput(new CommandRead(NetworkString.GAME_INSTANCE));}
    public void syncPush()    {queueOutput(new CommandWrite(NetworkString.GAME_INSTANCE, -1));}

    public void start()
    {
        if (outputThread == null && inputThread == null)
        {
            outputThread = new Thread(this, "Output " + id);
            outputThread.start();
            inputThread = new Thread(this, "Input " +id);
            inputThread.start();
        }
        else
        {
            throw new RuntimeException("Already running");
        }
    }

    static class CommandObject{}
    static class StopConnection{}

    static class CommandRead
    {
        final String type;
        public CommandRead(String type) {this.type = type;}
        
        @Override
        public String toString() {
            return CommandRead.class + " " + type;
        }
    }

    static class CommandWrite
    {
        final String type;
        public int id;
        public CommandWrite(String type, int id) {
            this.type = type;
            this.id = id;
        }
    }

    static class CommandPing implements Serializable
    {
        /**
         *
         */
        private static final long serialVersionUID = -1419752365189157656L;
        final int id;
        final int ttl;
        public CommandPing(int id, int ttl) {this.id = id;this.ttl = ttl;}
    }

    static class CommandPingForward extends CommandPing{
        private static int sid = (int)System.nanoTime();
        public CommandPingForward(int ttl) {super(sid++, ttl);}

        /**
         *
         */
        private static final long serialVersionUID = 158362809256485490L;}

    static class CommandPingBack extends CommandPing{

        public CommandPingBack(int id, int ttl) {super(id, ttl);}

        @Override
        public String toString() {
            return "Command Ping " + id + " " + ttl;
        }

        /**
         *
         */
        private static final long serialVersionUID = 8248889380911809147L;}

    public static interface PingCallback{
        public void run(PingInformation pi);
    }

    public static class PingInformation implements Runnable
    {
        public final int id;
        public final long timeout;
        public final PingCallback callback;
        boolean timeouted;
        boolean succesfull = false;

        @Override
        public void run()           {callback.run(this);}
        public boolean isTimeouted(){return timeouted;}

        public PingInformation(int id, long timeout, PingCallback callback, boolean timeouted)
        {
            this.id = id;
            this.timeout = timeout;
            this.callback = callback;
            this.timeouted = timeouted;
        }

        public boolean successfull() {return succesfull;}
    }

    private final ArrayList<PingInformation> pings = new ArrayList<>();

    public final PingInformation ping(final PingCallback callback, long timeout) {
        CommandPingForward cpf = new CommandPingForward(1);
        final PingInformation pc = new PingInformation(cpf.id, timeout, callback, false);
        DataHandler.hs.enqueue(new Runnable() {
            @Override
            public void run()
            {
                if (!pc.successfull()) {
                    pc.timeouted = true;
                    pc.run();
                }
                synchronized(pings){pings.remove(pc);}
            }
        }, timeout, true);
        synchronized(pings) {pings.add(pc);}
        queueOutput(cpf);
        return pc;
    }

    static class CommandList
    {
        final String type;
        public CommandList(String type) {this.type = type;}
    }

    static class CommandHash
    {
        final String type;
        public CommandHash(String type) {this.type = type;}
    }

    static class CommandScip implements Serializable
    {
        /**
         * 
         */
        private static final long serialVersionUID = 3856297382586773057L;
        final int bytes;
        public CommandScip(int bytes) {this.bytes = bytes;}
    }

    static class TimingOffsetChanged implements Serializable{
        
        /**
         * 
         */
        private static final long serialVersionUID = -8508545232652768659L;
        public final long offset;
        public TimingOffsetChanged(long offset){this.offset = offset;}
    }

    void send(GameAction action, ObjectOutputStream objOut, ByteArrayOutputStream byteStream)
    {
        try
        {
            objOut.writeUnshared(action);
            if (action instanceof GameObjectInstanceEditAction)
            {
                ObjectStateIO.writeStateToStreamObject(objOut, ((GameObjectInstanceEditAction)action).state);
            }
            else if (action instanceof PlayerEditAction)
            {
                if (!(action instanceof PlayerMousePositionUpdate || action instanceof PlayerCharacterPositionUpdate)) {
                    PlayerIO.writePlayerToStreamObject(objOut, ((PlayerEditAction)action).getEditedPlayer(gi));}
            }
            else if (action instanceof UsertextMessageAction
                    || action instanceof UserFileMessage
                    || action instanceof UserSoundMessageAction
                    || action instanceof TetrisGameEvent
                    || action instanceof GameTextureRemoveAction)
            {}
            else if (action instanceof GameObjectEditAction)
            {
                GameIO.writeObjectToStreamObject(objOut, ((GameObjectEditAction)action).getObject(gi));
            }
            else if (action instanceof GameStructureEditAction)
            {
                GameStructureEditAction gs = (GameStructureEditAction)action;
                if (gs instanceof PlayerAddAction)
                {
                    PlayerIO.writePlayerToStreamObject(objOut, ((PlayerAddAction)action).getPlayer(gi));
                }
                else
                {
                    switch(gs.type)
                    {
                        case GameStructureEditAction.EDIT_BACKGROUND:       objOut.writeUnshared(gi.game.getImageKey(gi.game.background));break;
                        case GameStructureEditAction.EDIT_TABLE_RADIUS:     objOut.writeInt(gi.tableRadius);break;
                        case GameStructureEditAction.EDIT_GAME_NAME:        objOut.writeUnshared(gi.game.name);break;
                        case GameStructureEditAction.EDIT_SESSION_NAME:     objOut.writeUnshared(gi.name);break;
                        case GameStructureEditAction.EDIT_SESSION_PASSWORD: objOut.writeUnshared(gi.password);break;
                        case AddObjectAction.ADD_IMAGE:
                        {
                            Map.Entry<String, Texture> entry = gi.game.getImage(((AddObjectAction)gs).objectId);
                            objOut.writeObject(entry.getKey());
                            GameIO.writeImageToStream(entry.getValue(), StringUtils.getFileType(entry.getKey()), byteStream);
                            objOut.writeInt(byteStream.size());
                            byteStream.writeTo(objOut);
                            byteStream.reset();
                            break;
                        }
                        case GameStructureEditAction.REMOVE_OBJECT:
                        case GameStructureEditAction.REMOVE_OBJECT_INSTANCE:
                        default:
                            logger.warn("Structure edit action " + gs.type + " is unknown");
                            break;
                    }
                }
            }
            else
            {
                logger.warn("Unknown actiontype " + action.getClass());
                return;
            }
            ++outputEvents;
        }
        catch ( Exception e ) {
            logger.error("Error at emmiting Game Action", e);
        }
    }

    private Object getQueuedObject(ObjectOutputStream objOut) {
        boolean flush = true;
        while(!stop)
        {
            synchronized(queuedOutputs)
            {
                if (!queuedOutputs.isEmpty())
                {
                    return queuedOutputs.pop();
                }
                else if (!flush)
                {
                    if (stop){return null;}
                    try {
                        queuedOutputs.wait();
                    } catch (InterruptedException e) {
                        logger.error("Unexpected interrupt", e);
                    }
                }
            }
            if (flush)
            {
                try {
                    if (blocksize  != 0)
                    {
                        objOut.writeUnshared(new CommandScip(blocksize));
                        if (random == null)//TODO write only as much as needed
                        {
                            for (int i = 0; i < blocksize; ++i){objOut.writeByte(0);}
                        }
                        else
                        {
                            randBytes = ArrayUtil.ensureLength(randBytes, blocksize);
                            random.nextBytes(randBytes);
                            objOut.write(randBytes, 0, blocksize);//TODO test
                        }
                    }
                    objOut.flush();
                    output.flush();
                    flush = false;
                } catch (IOException e1) {
                    logger.error("Error during flushing output", e1);
                    if (e1 instanceof SocketException)
                    {
                        stop = true;
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private void outputLoop()
    {
        ArrayList<String> split = new ArrayList<>();
        StringBuilder strB = new StringBuilder();
        ObjectOutputStream objOut = null;
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            objOut = StreamUtil.toObjectStream(output);
        } catch (IOException e1) {
            logger.error("Can't initialize ObjectStream", e1);
        }
        while (!stop)
        {
            Object outputObject = getQueuedObject(objOut);
            if (stop)
            {
                return;
            }
            if (logger.isDebugEnabled()){logger.debug("Next queued object:" + outputObject.toString());}
            try
            {
                if (outputObject instanceof CommandWrite)
                {
                    if (byteStream.size() != 0){throw new RuntimeException("Byte-Stream was not cleared");}
                    strB.append(NetworkString.WRITE).append(' ');
                    int id = ((CommandWrite) outputObject).id;
                    switch (((CommandWrite) outputObject).type)
                    {
                        case NetworkString.GAME_INSTANCE:
                        {
                            GameIO.writeSnapshotToZip(gi, byteStream);
                            if (logger.isDebugEnabled()){logger.debug("Write game instance to stream " + byteStream.size());}
                            strB.append(NetworkString.ZIP).append(' ').append(NetworkString.GAME_INSTANCE).append(' ').append(byteStream.size());
                            objOut.writeUnshared(strB.toString());
                            byteStream.writeTo(objOut);
                            byteStream.reset();
                            strB.setLength(0);
                            break;
                        }
                        case NetworkString.GAME:
                        {
                            GameIO.writeGameToZip(gi.game, byteStream);
                            strB.append(NetworkString.ZIP).append(' ').append(NetworkString.GAME).append(' ').append(byteStream.size());
                            objOut.writeObject(strB.toString());
                            byteStream.writeTo(objOut);
                            byteStream.reset();
                            strB.setLength(0);
                            break;
                        }
                        case NetworkString.GAME_OBJECT:
                        {
                            GameIO.writeObjectToZip(gi.game.getObject(Integer.toString(id)), byteStream);
                            strB.append(NetworkString.ZIP).append(' ').append(NetworkString.GAME_OBJECT).append(' ').append(byteStream.size());
                            objOut.writeObject(strB.toString());
                            byteStream.writeTo(objOut);
                            byteStream.reset();
                            strB.setLength(0);
                            break;
                        }
                        case NetworkString.GAME_OBJECT_INSTANCE:
                        {
                            GameIO.writeObjectInstanceToZip(gi.getObjectInstanceById(id), byteStream);
                            strB.append(NetworkString.ZIP).append(' ').append(NetworkString.GAME_OBJECT_INSTANCE).append(' ').append(byteStream.size());
                            objOut.writeObject(strB.toString());
                            byteStream.writeTo(objOut);
                            strB.setLength(0);
                            byteStream.reset();
                            break;
                        }
                        case NetworkString.PLAYER:
                        {
                            PlayerIO.writePlayerToStream(gi.getPlayerById(id), byteStream);
                            strB.append(NetworkString.ZIP).append(' ').append(NetworkString.PLAYER).append(' ').append(byteStream.size());
                            byteStream.writeTo(objOut);
                            strB.setLength(0);
                            byteStream.reset();
                            break;
                        }
                    }    
                }
                else if (outputObject instanceof CommandRead)
                {
                    strB.append(NetworkString.READ).append(' ').append(((CommandRead) outputObject).type);
                    objOut.writeObject(strB.toString());
                    strB.setLength(0);
                }
                else if (outputObject instanceof CommandList)
                {
                    switch (split.get(2))
                    {
                        case NetworkString.PLAYER:
                        {
                            strB.append(NetworkString.WRITEBACK).append(' ').append(NetworkString.PLAYER);
                            objOut.writeObject(strB.toString());
                            objOut.writeObject(gi.getPlayerNames());
                            strB.setLength(0);
                            break;
                        }
                    }
                }
                else if (outputObject instanceof CommandHash)
                {
                    strB.append(NetworkString.WRITEBACK).append(' ').append(NetworkString.HASH).append(' ');
                    objOut.writeObject(strB.toString());
                    objOut.write(gi.hashCode());
                    strB.setLength(0);
                }
                else if (outputObject instanceof String){}
                else if (outputObject instanceof TimingOffsetChanged || outputObject instanceof CommandPing)
                {
                    objOut.writeUnshared(outputObject);
                }
                else if (outputObject instanceof GameAction)
                {
                    GameAction action = (GameAction)outputObject;
                    send(action, objOut, byteStream);
                }
            }catch(IOException e)
            {
                logger.error("Error at output Loop", e);
            }
        }
    }
    
    public void inputLoop()
    {
        ArrayList<String> split = new ArrayList<>();
        if (objIn == null)
        {
            try {
                objIn = StreamUtil.toObjectStream(input);
            } catch (IOException e1) {
                logger.error("Can't open Object-Input-Stream", e1);
            }

            if (objIn == null){return;}
        }
        CappedInputStreamWrapper cappedIn = new CappedInputStreamWrapper(objIn, 0);
        while (!stop)
        {
            try {
                Object inputObject = null;
                try
                {
                    inputObject = objIn.readObject();
                }catch(OptionalDataException e)
                {
                    logger.error("Can't extract object", e);
                    if (stopOnError){return;}
                }
                if (logger.isDebugEnabled()){logger.debug("Next input object:" + inputObject.toString());}
                if (inputObject instanceof TimingOffsetChanged)
                {
                    otherTimingOffset = ((TimingOffsetChanged) inputObject).offset;
                    continue;
                }
                if (inputObject instanceof CommandScip)
                {
                    long bytes = ((CommandScip)inputObject).bytes;
                    long scipped = StreamUtil.skip(objIn, bytes);
                    if (bytes != scipped)
                    {
                        logger.warn("Needed to scip " + bytes + " bytes but got " + scipped);
                    }
                    continue;
                }
                if (inputObject instanceof CommandPingForward)
                {
                    CommandPingForward ping = ((CommandPingForward)inputObject);
                    queueOutput(new CommandPingBack(ping.id, ping.ttl));
                    continue;
                }
                if (inputObject instanceof CommandPingBack)
                {
                    CommandPingBack cpb = (CommandPingBack)inputObject;
                    synchronized (pings) {
                        for (int read = 0; read < pings.size(); ++read)
                        {
                            PingInformation current = pings.get(read);
                            if (current.id == cpb.id){DataHandler.tp.run(current, "Ping Callback");}
                        }                        
                    }
                    continue;
                }
                if (inputObject instanceof GameAction)
                {
                    long nanoTime = System.nanoTime();
                    GameAction action = ((GameAction)inputObject);
                    if (action.when + otherToThisOffset >= nanoTime)
                    {
                        otherToThisOffset = nanoTime - action.when;
                        queueOutput(new TimingOffsetChanged(otherToThisOffset));
                    }
                    action.when += otherToThisOffset;
                    if (action instanceof GameObjectInstanceEditAction)
                    {
                        GameObjectInstanceEditAction actionEdit = (GameObjectInstanceEditAction)inputObject;
                        ObjectInstance oi = actionEdit.getObject(gi);
                        if (oi == null) {logger.error("Couldn't find object " + actionEdit.object);}
                        ObjectState state = oi.state.copy();
                        actionEdit.state = state;
                        if (state.lastChange > action.when)
                        {
                            logger.debug("Scipping action");
                            ObjectStateIO.simulateStateFromStreamObject(objIn, state);
                        }
                        else
                        {
                            ObjectStateIO.editStateFromStreamObject(objIn, state);
                            state.lastChange = action.when;
                        }
                        gi.update(action);
                        ++inputEvents;
                        continue;
                    }
                    if (action instanceof AtomicAction)
                    {
                        AtomicAction atomic = (AtomicAction)action;
                        GameAction actions[] = new GameAction[objIn.readInt()];
                        for (int i = 0; i < actions.length; ++i)
                        {
                            actions[i] = (AtomicAction)objIn.readObject();
                        }
                        atomic.set(actions);
                        gi.update(atomic);
                        continue;
                    }
                    if (action instanceof GameObjectEditAction)
                    {
                        GameObjectEditAction actionEdit = (GameObjectEditAction)inputObject;
                        GameIO.editGameObjectFromStreamObject(objIn, actionEdit.getObject(gi));
                        gi.update(action);
                        ++inputEvents;
                        continue;
                    }
                    if (action instanceof PlayerEditAction)
                    {
                        if (action instanceof PlayerMousePositionUpdate || action instanceof PlayerCharacterPositionUpdate)
                        {
                            gi.update(action);
                            ++inputEvents;
                            continue;
                        }
                        PlayerEditAction actionEdit = (PlayerEditAction)inputObject;
                        Player editedPlayer = actionEdit.getEditedPlayer(gi);
                        PlayerIO.editPlayerFromStreamObject(objIn, editedPlayer);
                        gi.update(action);
                        ++inputEvents;
                        continue;
                    }
                    if (action instanceof UsertextMessageAction 
                        || action instanceof UserSoundMessageAction
                        || action instanceof UserFileMessage
                        || action instanceof TetrisGameEvent
                        || action instanceof PlayerRemoveAction
                        || action instanceof GameTextureRemoveAction)
                    {
                        gi.update(action);
                        ++inputEvents;
                        continue;
                    }
                    logger.warn("Unknown actiontype class " + action.getClass());
                }
                if (inputObject instanceof GameStructureEditAction)
                {
                    GameStructureEditAction action = (GameStructureEditAction)inputObject;
                    if (action instanceof AddObjectAction)
                    {
                        if (action instanceof PlayerAddAction)
                        {
                            PlayerAddAction addAction = ((PlayerAddAction)action);
                            Player player =((PlayerAddAction)action).getPlayer(gi);
                            if (player == null){
                                player = new Player("", addAction.objectId);
                                addAction = new PlayerAddAction(id, player);
                            }
                            PlayerIO.editPlayerFromStreamObject(objIn, player);
                            gi.addPlayer(addAction);
                        }
                        else
                        {
                            switch(action.type)
                            {
                                case AddObjectAction.ADD_IMAGE:
                                {
                                    String name = (String)objIn.readObject();
                                    int cap = objIn.readInt();
                                    gi.game.images.put(name, new Texture(StreamUtil.toByteArray(objIn, cap), StringUtils.getFileType(name)));
                                    gi.update(action);
                                    break;
                                }
                                case AddObjectAction.ADD_PLAYER:{break;}
                                case AddObjectAction.ADD_GAME_OBJECT:{break;}
                                case AddObjectAction.ADD_GAME_OBJECT_INSTANCE:{break;}
                                default: logger.error("Unknown type: " + action.type);
                            }
                        }
                    }
                    else
                    {
                        switch(action.type)
                        {
                            case GameStructureEditAction.EDIT_TABLE_RADIUS: gi.tableRadius = objIn.readInt();break;
                            case GameStructureEditAction.EDIT_BACKGROUND:gi.game.background = gi.game.images.get(objIn.readObject());break;
                            case GameStructureEditAction.EDIT_GAME_NAME:gi.game.name = (String)objIn.readObject();break;
                            case GameStructureEditAction.EDIT_SESSION_NAME:gi.name = (String)objIn.readObject();break;
                            case GameStructureEditAction.EDIT_SESSION_PASSWORD:gi.password = (String)objIn.readObject();break;
                            default: logger.error("Unknown type: " + action.type);                        
                        }
                        gi.update(action);
                    }
                    ++inputEvents;
                    continue;
                }
                if (!(inputObject instanceof String))
                {
                    if (inputObject instanceof String[])    {logger.error("Input object has wrong type " + Arrays.toString((String[])inputObject));        }
                    else                                    {logger.error("Input object has wrong type " + inputObject.toString());}
                }
                else
                {
                    if (logger.isDebugEnabled()){logger.debug("Got input:" + inputObject.toString());}
                }
                String line = (String)inputObject;
                StringUtils.split(line, ' ', split);

                switch(split.get(0))
                {
                    case NetworkString.READ:
                    {
                        int id = -1;
                        if (split.size() > 2){id = Integer.parseInt(split.get(2));}
                        queueOutput(new CommandWrite(split.get(1), id));
                        break;
                    }
                    case NetworkString.WRITEBACK:
                    {
                        if (split.get(1).equals(NetworkString.PLAYER)){String players[] = (String[])objIn.readObject();}
                        break;
                    }
                    case NetworkString.WRITE:
                    {
                        switch(split.get(1))
                        {
                            case NetworkString.ZIP:
                            {
                                switch(split.get(2))
                                {
                                    case NetworkString.GAME_INSTANCE:
                                    {
                                        if (logger.isDebugEnabled()){logger.debug("Do local instance write " + split.get(3));}
                                        int size = Integer.parseInt(split.get(3));
                                        //byte data[] = (byte[])objIn.readObject();
                                        cappedIn.setCap(size);
                                        GameIO.readSnapshotFromZip(cappedIn, gi);
                                        cappedIn.drain();
                                        break;
                                    }
                                }
                                break;
                            }
                            case NetworkString.PLAIN:
                            {
                                String player = split.get(2);
                                int id = Integer.parseInt(split.get(3));
                                String type = split.get(4);
                                break;
                            }
                        }
                        break;
                    }
                    case NetworkString.ACTION:
                    {
                        if (split.get(1).equals(NetworkString.EDIT) && split.get(2).equals(NetworkString.STATE))
                        {
                            int sourceId = Integer.parseInt(split.get(4));
                            if (sourceId != id)
                            {
                                int playerId = Integer.parseInt(split.get(5));
                                int objectId = Integer.parseInt(split.get(6));
                                Integer.parseInt(split.get(7)); //size
                                
                                ObjectInstance inst = gi.getObjectInstanceById(objectId);
                                ObjectState state = inst.state.copy();
                                ObjectStateIO.editStateFromStreamObject(objIn, state);
                                Player pl = gi.getPlayerById(playerId);
                                if (pl == null)    {logger.error("Can't find player: " + playerId);}
                                else            {gi.update(new GameObjectInstanceEditAction(sourceId, pl, inst, state));}
                                ++inputEvents;
                            }
                        }
                        else if (split.get(1).equals(NetworkString.EDIT) && split.get(2).equals(NetworkString.PLAYER))
                        {
                            int sourceConnectionId = Integer.parseInt(split.get(4));
                            if (sourceConnectionId != id)
                            {
                                int sourcePlayerId = Integer.parseInt(split.get(5));
                                int editPlayerId = Integer.parseInt(split.get(6));
                                int size = Integer.parseInt(split.get(7));
                                //objIn.readFully(data, 0, size);
                                Player object = gi.getPlayerById(editPlayerId);
                                if (object != null)
                                {
                                    PlayerIO.editPlayerFromStreamObject(objIn, object);
                                    //GameIO.editPlayerFromStreamZip(new ByteArrayInputStream(data, 0, size), object);
                                }
                                else
                                {
                                    object = new Player("",  editPlayerId);
                                    PlayerIO.editPlayerFromStreamObject(objIn, object);
                                    //gi.addPlayer(GameIO.readPlayerFromStream(new ByteArrayInputStream(data, 0, size)));
                                }
                                Player sourcePlayer = gi.getPlayerById(sourcePlayerId);
                                if (sourcePlayer == null)    {logger.error("Can't find player: " + sourcePlayerId);}
                                else                        {gi.update(new PlayerEditAction(sourceConnectionId, sourcePlayer, object));}
                            }
                        }
                        else if (split.get(1).equals(NetworkString.TEXTMESSAGE))
                        {
                            int sourceConnection = Integer.parseInt(split.get(2));
                            int sourcePlayer = Integer.parseInt(split.get(3));
                            int destinationPlayer = Integer.parseInt(split.get(4));
                            if (this.id != sourceConnection)
                            {
                                int playerId = Integer.parseInt(split.get(4));
                                gi.update(new UsertextMessageAction(sourcePlayer, playerId, destinationPlayer, (String)objIn.readObject()));
                            }
                        }
                    }
                    break;
                }
            }catch(Exception e) {
                logger.error("Exception in input loop", e);
                if (e instanceof EOFException || (e instanceof SocketException && e.getMessage().equals("Connection reset")))
                {
                    return;
                }
            }
            split.clear();
        }
    }
    
    public void destroy()
    {
        stop();
        if (gi != null){gi.removeChangeListener(this);}
        destroyed  = true;
    }
    
    public void stop()
    {
        queueOutput(new StopConnection());
        stop = true;
    }
    
    @Override
    public void run()
    {
        if (Thread.currentThread() == outputThread)     {outputLoop();}
        else if (Thread.currentThread() == inputThread) {inputLoop();}
        else                                            {throw new IllegalStateException();}
    }
}
