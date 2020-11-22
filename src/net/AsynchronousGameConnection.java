package net;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.action.AddObjectAction;
import gameObjects.action.AddPlayerAction;
import gameObjects.action.GameAction;
import gameObjects.action.GameObjectEditAction;
import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.action.GamePlayerEditAction;
import gameObjects.action.GameStructureEditAction;
import gameObjects.action.message.UserFileMessage;
import gameObjects.action.message.UserSoundMessageAction;
import gameObjects.action.message.UsertextMessageAction;
import gameObjects.instance.GameInstance;
import gameObjects.instance.GameInstance.GameChangeListener;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.ObjectState;
import gui.minigames.TetrisGameInstance.TetrisGameEvent;
import io.GameIO;
import main.Player;
import util.StringUtils;
import util.data.UniqueObjects;
import util.io.StreamUtil;
import util.stream.CappedInputStreamWrapper;

public class AsynchronousGameConnection implements Runnable, GameChangeListener{
	public enum Network {
		ACTION_EDIT_STATE
	}

	private static final Logger logger = LoggerFactory.getLogger(AsynchronousGameConnection.class);
	private final GameInstance gi;
	Thread outputThread;
	Thread inputThread;
	InputStream input;
	OutputStream output;
	ArrayDeque<Object> queuedOutputs = new ArrayDeque<>();
	private final int connectionId = (int)(Math.random() * Integer.MAX_VALUE);
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
	
	public int getInEvents()
	{
		return inputEvents;
	}
	
	public int getOutEvents()
	{
		return outputEvents;
	}
	
	@Override
	public void changeUpdate(GameAction action) {
		if (Thread.currentThread() != inputThread)
		{
			if (logger.isDebugEnabled()){logger.debug("Queue Action != " + inputThread.getName());}
			queueOutput(action);
		}
	}
	
	private void queueOutput(Object output)
	{
		if (logger.isDebugEnabled()){logger.debug("Addd queue " + connectionId);}
		synchronized(queuedOutputs)
		{
			queuedOutputs.add(output);
			queuedOutputs.notifyAll();
		}
	}
	
	public GameInstance getGameInstance()
	{
		return gi;
	}
	
	/**
	 * Constructs an connection with the GameInstance and the two streams.
	 * @param gi
	 * @param input
	 * @param output
	 */
	public AsynchronousGameConnection(GameInstance gi, ObjectInputStream input, OutputStream output)
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
	 */
	public AsynchronousGameConnection(GameInstance gi, InputStream input, OutputStream output)
	{
		this.gi = gi;
		gi.addChangeListener(this);
		/*if (!(input instanceof ObjectInputStream))
		{
			throw new RuntimeException();
		}*/
		this.input = input;
		this.output = output;
	}
	
	public void syncPull()
	{
		queueOutput(new CommandRead(NetworkString.GAME_INSTANCE));
	}
	
	public void syncPush()
	{
		queueOutput(new CommandWrite(NetworkString.GAME_INSTANCE, -1));
	}
	
	public void start()
	{
		if (outputThread == null && inputThread == null)
		{
			outputThread = new Thread(this, "Output " + connectionId);
			outputThread.start();
			inputThread = new Thread(this, "Input " +connectionId);
			inputThread.start();
		}
		else
		{
			throw new RuntimeException("Already running");
		}
	}
	
	static class CommandObject
	{
	}
	
	static class StopConnection{}
	
	static class CommandRead
	{
		String type;
		public CommandRead(String type) {
			this.type = type;
		}
	}
	
	static class CommandWrite
	{
		String type;
		public int id;
		public CommandWrite(String type, int id) {
			this.type = type;
			this.id = id;
		}
	}
	
	static class CommandList
	{
		String type;
		public CommandList(String type) {
			this.type = type;
		}
	}
	
	static class CommandHash
	{
		String type;
		public CommandHash(String type) {
			this.type = type;
		}
	}
	
	static class CommandScip implements Serializable
	{
		int bytes;
		public CommandScip(int bytes) {
			this.bytes = bytes;
		}
	}
	
	static class TimingOffsetChanged implements Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -8508545232652768659L;
		public final long offset;
		
		public TimingOffsetChanged(long offset)
		{
			this.offset = offset;
		}
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
			Object outputObject = null;
			synchronized(queuedOutputs)
			{
				outputObject = queuedOutputs.size() == 0 ? null : queuedOutputs.pop();
			}
			if (outputObject == null)
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
							if (randBytes.length < blocksize)
							{
								randBytes = new byte[blocksize];
							}
							random.nextBytes(randBytes);
							for (int i = 0; i < blocksize; ++i){objOut.writeByte(randBytes[i]);}							
						}
					}
					objOut.flush();
					output.flush();
				} catch (IOException e1) {
					logger.error("Error during flushing output", e1);
				}
				synchronized(queuedOutputs)
				{
					if (queuedOutputs.size() == 0)
					{					
						try {
							queuedOutputs.wait();
						} catch (InterruptedException e) {
							logger.error("Unexpected interrupt", e);
						}
					}
					outputObject = queuedOutputs.size() == 0 ? null : queuedOutputs.pop();
				}
			}
			
			if (outputObject == null)
			{
				continue;
			}
			if (logger.isDebugEnabled()){logger.debug("Next queued object:" + outputObject.toString());}
			try
			{
				if (outputObject instanceof CommandWrite)
				{
					strB.append(NetworkString.WRITE).append(' ');
					int id = ((CommandWrite) outputObject).id;
		    		switch (((CommandWrite) outputObject).type)
		    		{
		    			case NetworkString.GAME_INSTANCE:
		    			{
		    				if (byteStream.size() != 0)
		    				{
		    					throw new RuntimeException();
		    				}
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
		    				GameIO.writePlayerToStream(gi.getPlayerById(id), byteStream);
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
				else if (outputObject instanceof String)
				{
				}
				else if (outputObject instanceof TimingOffsetChanged)
				{
					objOut.writeUnshared(outputObject);
				}
				else if (outputObject instanceof GameAction)
				{
					GameAction action = (GameAction)outputObject;
				    try
				    {
				    	if (action instanceof GameObjectInstanceEditAction)
				 		{
				    		objOut.writeUnshared(action);
				    		GameIO.writeStateToStreamObject(objOut, ((GameObjectInstanceEditAction)action).getObject(gi).state);
				    		++outputEvents;
				 		}
				    	else if (action instanceof GamePlayerEditAction)
				 		{
				    		objOut.writeUnshared(action);
				    		GameIO.writePlayerToStreamObject(objOut, ((GamePlayerEditAction)action).getEditedPlayer(gi));
				    		++outputEvents;
				 		}
				    	else if (action instanceof UsertextMessageAction)
				    	{
				    		objOut.writeUnshared(action);
				    		++outputEvents;
					   	}
				    	else if (action instanceof GameObjectEditAction)
				    	{
				    		++outputEvents;
				    		objOut.writeUnshared(action);
				    		GameIO.writeObjectToStreamObject(objOut, ((GameObjectEditAction)action).getObject(gi));
				    	}
				    	else if (action instanceof GameStructureEditAction)
				    	{ 
				    		GameStructureEditAction gs = (GameStructureEditAction)action;
				    		objOut.writeUnshared(gs);
				    		if (action instanceof AddPlayerAction)
			    			{
				    			GameIO.writePlayerToStreamObject(objOut, ((AddPlayerAction)action).getPlayer(gi));
			    			}
				    		else
				    		{
					    		switch(gs.type)
					    		{
					    			case GameStructureEditAction.EDIT_BACKGROUND: 		objOut.writeUnshared(gi.game.getImageKey(gi.game.background));break;
					    			case GameStructureEditAction.EDIT_TABLE_RADIUS: 	objOut.writeFloat(gi.tableRadius);break;
									case GameStructureEditAction.EDIT_GAME_NAME: 		objOut.writeUnshared(gi.game.name);break;
									case GameStructureEditAction.EDIT_SESSION_NAME: 	objOut.writeUnshared(gi.name);break;
									case GameStructureEditAction.EDIT_SESSION_PASSWORD:	objOut.writeUnshared(gi.password);break;
									case AddObjectAction.ADD_IMAGE:
									{
										Map.Entry<String, BufferedImage> entry = gi.game.getImage(((AddObjectAction)gs).objectId);
										objOut.writeObject(entry.getKey());
										GameIO.writeImageToStream(entry.getValue(), "png", byteStream);
										objOut.writeInt(byteStream.size());
										byteStream.writeTo(objOut);
										byteStream.reset();
										break;
									}
									case GameStructureEditAction.REMOVE_OBJECT:
									case GameStructureEditAction.REMOVE_OBJECT_INSTANCE:
									case GameStructureEditAction.REMOVE_PLAYER:
										break;
									default:
										logger.warn("Structure edit action " + gs.type + " is unknown");
										break;
					    		}
							}
				    		
				    		++outputEvents;
						}
					    else if (action instanceof UserSoundMessageAction)
					    {
				    		objOut.writeUnshared(action);
				    		++outputEvents;
					    }
					    else if (action instanceof TetrisGameEvent)
					    {
					    	objOut.writeUnshared(action);
					    	++outputEvents;
					    }
					}
				    catch ( Exception e ) {
				    	logger.error("Error at emmiting Game Action", e);
				    }
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

			if (objIn == null)
			{
				return;
			}
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
					if (stopOnError)
					{
						return;
					}
				}
				if (inputObject instanceof TimingOffsetChanged)
				{
					otherTimingOffset = ((TimingOffsetChanged) inputObject).offset;
					continue;
				}
				if (inputObject instanceof CommandScip)
				{
					for (int i = ((CommandScip)inputObject).bytes; i != 0; --i)
					{
						objIn.readByte();
					}
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
						ObjectState state = actionEdit.getObject(gi).state;
						if (state.lastChange > action.when)
						{
							GameIO.simulateStateFromStreamObject(objIn, state);
						}
						else
						{
							GameIO.editStateFromStreamObject(objIn, state);			
							state.lastChange = action.when;
						}
						gi.update(action);
						++inputEvents;
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
					if (action instanceof GamePlayerEditAction)
					{
						GamePlayerEditAction actionEdit = (GamePlayerEditAction)inputObject;
						Player editedPlayer = actionEdit.getEditedPlayer(gi);
						GameIO.editPlayerFromStreamObject(objIn, editedPlayer);
						gi.update(action);
						++inputEvents;
						continue;
					}
					if (action instanceof UsertextMessageAction)
					{
						gi.update(action);
						++inputEvents;
						continue;
					}
					if (action instanceof UserFileMessage)
					{
						gi.update(action);
						++inputEvents;
						continue;
					}
					if (action instanceof TetrisGameEvent)
					{
						gi.update(action);
						++inputEvents;
						continue;
					}
				}
				
				if (inputObject instanceof GameStructureEditAction)
				{
					GameStructureEditAction action = (GameStructureEditAction)inputObject;
					if (action instanceof AddObjectAction)
					{
						AddObjectAction addAction = (AddObjectAction)action;
						switch(action.type)
						{
							case AddObjectAction.ADD_IMAGE:
							{
								String name = (String)objIn.readObject();
								int cap = objIn.readInt();
								cappedIn.setCap(cap);
								gi.game.images.put(name, ImageIO.read(cappedIn));
								cappedIn.drain();
								gi.update(action);
								break;
							}
							case AddObjectAction.ADD_PLAYER:
							{
								Player player =((AddPlayerAction)action).getPlayer(gi);
								if (player == null)
								{
									player = new Player("", addAction.objectId);
								}
								GameIO.editPlayerFromStreamObject(objIn, player);
								gi.addPlayer((AddPlayerAction)action, player);
								break;
							}
							case AddObjectAction.ADD_GAME_OBJECT:
							{
								//TODO
								break;
							}
							case AddObjectAction.ADD_GAME_OBJECT_INSTANCE:
							{
								//todo
								break;
							}
							default: logger.error("Unknown type: " + action.type);
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
					if (inputObject instanceof String[])
					{
						logger.error("Input object has wrong type " + Arrays.toString((String[])inputObject));		
					}
					else
					{
						logger.error("Input object has wrong type " + inputObject.toString());
					}
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
						if (split.size() > 2)
						{
							id = Integer.parseInt(split.get(2));
						}
						queueOutput(new CommandWrite(split.get(1), id));
						break;
					}
					case NetworkString.WRITEBACK:
					{
						if (split.get(1).equals(NetworkString.PLAYER))
						{
							String players[] = (String[])objIn.readObject();
						}
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
										GameIO.editGameInstanceFromZip(cappedIn, gi, this);
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
							if (sourceId != connectionId)
							{
								int playerId = Integer.parseInt(split.get(5));
								int objectId = Integer.parseInt(split.get(6));
								Integer.parseInt(split.get(7)); //size
								
								ObjectInstance inst = gi.getObjectInstanceById(objectId);
								//ObjectState state = (ObjectState)objIn.readObject();
								//inst.state.set(state);
								GameIO.editStateFromStreamObject(objIn, inst.state);
								
								/*data = ArrayUtil.ensureLength(data, size);						
								objIn.readFully(data, 0, size);
								if (sourceId != id)
								{
									GameIO.editObjectStateFromStream(inst.state, new ByteArrayInputStream(data, 0, size));
								}*/
								Player pl = gi.getPlayerById(playerId);
								if (pl == null)
								{
									logger.error("Can't find player: " + playerId);
								}
								else
								{
									gi.update(new GameObjectInstanceEditAction(sourceId, pl, inst));
								}
								++inputEvents;
							}
						}
						else if (split.get(1).equals(NetworkString.EDIT) && split.get(2).equals(NetworkString.PLAYER))
						{
							int sourceConnectionId = Integer.parseInt(split.get(4));
							if (sourceConnectionId != connectionId)
							{
								int sourcePlayerId = Integer.parseInt(split.get(5));
								int editPlayerId = Integer.parseInt(split.get(6));
								int size = Integer.parseInt(split.get(7));
								//objIn.readFully(data, 0, size);
								Player object = gi.getPlayerById(editPlayerId);
								if (object != null)
								{
									GameIO.editPlayerFromStreamObject(objIn, object);
									//GameIO.editPlayerFromStreamZip(new ByteArrayInputStream(data, 0, size), object);
								}
								else
								{
									object = new Player("",  editPlayerId);
									GameIO.editPlayerFromStreamObject(objIn, object);
									//gi.addPlayer(GameIO.readPlayerFromStream(new ByteArrayInputStream(data, 0, size)));
								}
								Player sourcePlayer = gi.getPlayerById(sourcePlayerId);
								if (sourcePlayer == null)
								{
									logger.error("Can't find player: " + sourcePlayerId);
								}
								else
								{
									gi.update(new GamePlayerEditAction(sourceConnectionId, sourcePlayer, object));
								}
							}
						}
						else if (split.get(1).equals(NetworkString.TEXTMESSAGE))
						{
							int sourceConnection = Integer.parseInt(split.get(2));
							int sourcePlayer = Integer.parseInt(split.get(3));
							int destinationPlayer = Integer.parseInt(split.get(4));
							if (this.connectionId != sourceConnection)
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
	
	public void stop()
	{
		queueOutput(new StopConnection());
		stop = true;
	}
	
	@Override
	public void run()
	{
		if (Thread.currentThread() == outputThread)
		{
			outputLoop();
		}
		else if (Thread.currentThread() == inputThread)
		{
			inputLoop();
		}
	}
}
