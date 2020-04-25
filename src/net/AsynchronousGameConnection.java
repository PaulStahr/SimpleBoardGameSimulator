package net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.action.GameAction;
import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.action.GamePlayerEditAction;
import gameObjects.action.UserSoundMessageAction;
import gameObjects.action.UsertextMessageAction;
import gameObjects.instance.GameInstance;
import gameObjects.instance.GameInstance.GameChangeListener;
import gameObjects.instance.ObjectInstance;
import io.GameIO;
import main.Player;
import util.ArrayUtil;
import util.StringUtils;
import util.data.UniqueObjects;

public class AsynchronousGameConnection implements Runnable, GameChangeListener{
	private static final Logger logger = LoggerFactory.getLogger(AsynchronousGameConnection.class);
	GameInstance gi;
	Thread outputThread;
	Thread inputThread;
	InputStream input;
	OutputStream output;
	ArrayDeque<Object> queuedOutputs = new ArrayDeque<>();
	private final int id = (int)(Math.random() * Integer.MAX_VALUE);
	private ObjectInputStream objIn;
	private boolean stopOnError = true;

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
		if (logger.isDebugEnabled()){logger.debug("Addd queue " + id);}
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
	
	class CommandObject
	{
	}
	
	
	class CommandRead
	{
		String type;
		public CommandRead(String type) {
			this.type = type;
		}
	}
	
	class CommandWrite
	{
		String type;
		public int id;
		public CommandWrite(String type, int id) {
			this.type = type;
			this.id = id;
		}
	}
	
	class CommandList
	{
		String type;
		public CommandList(String type) {
			this.type = type;
		}
	}
	
	class CommandHash
	{
		String type;
		public CommandHash(String type) {
			this.type = type;
		}
	}
	
	private void outputLoop()
	{
		ArrayList<String> split = new ArrayList<>();
		StringBuilder strB = new StringBuilder();
		//PrintWriter writer = new PrintWriter(output, true);
		ObjectOutputStream objOut = null;
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		if (output instanceof ObjectOutputStream)
		{
			objOut = ((ObjectOutputStream)output);
		}
		else
		{
			try {
				objOut = new ObjectOutputStream(output);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
    	while (true)
		{
			Object outputObject = null;
			synchronized(queuedOutputs)
			{
				outputObject = queuedOutputs.size() == 0 ? null : queuedOutputs.pop();
			}
			if (outputObject == null)
			{
				try {
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
		    				//byte data[] = byteStream.toByteArray();
		    				if (logger.isDebugEnabled()){logger.debug("Write game instance to stream " + byteStream.size());}
		    				strB.append(NetworkString.ZIP).append(' ').append(NetworkString.GAME_INSTANCE).append(' ').append(byteStream.size());
		    				objOut.writeObject(strB.toString());
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
		    				//objOut.writeObject(byteStream.toByteArray());
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
				else if (outputObject instanceof GameAction)
				{
					GameAction action = (GameAction)outputObject;
				    try
				    {
				    	if (action instanceof GameObjectInstanceEditAction)
				 		{
					    	//GameIO.writeObjectStateToStreamXml(((GameObjectInstanceEditAction)action).object.state, byteStream);
					 		strB.append(NetworkString.ACTION).append(' ')
					 			.append(NetworkString.EDIT).append(' ')
					 			.append(NetworkString.STATE).append(' ')
					 			.append(id).append(' ')
					 			.append(action.source).append(' ')
					 			.append(((GameObjectInstanceEditAction) action).player.id).append(' ')
					 			.append(((GameObjectInstanceEditAction) action).object.id).append(' ')
					 			.append( byteStream.size());
					 		objOut.writeObject(strB.toString());
					 		GameIO.writeStateToStreamObject(objOut, ((GameObjectInstanceEditAction)action).object.state);
					 		//objOut.writeObject(((GameObjectInstanceEditAction)action).object.state.copy());
					 		//byteStream.writeTo(objOut);
					    	byteStream.reset();
					     	strB.setLength(0);
					   }
				    	else if (action instanceof GamePlayerEditAction)
				 		{
					    	//GameIO.writePlayerToStream(((GamePlayerEditAction)action).object, byteStream);
					 		strB.append(NetworkString.ACTION).append(' ')
					 			.append(NetworkString.EDIT).append(' ')
					 			.append(NetworkString.PLAYER).append(' ')
					 			.append(id).append(' ')
					 			.append(action.source).append(' ')
					 			.append(((GamePlayerEditAction) action).player.id).append(' ')
					 			.append(((GamePlayerEditAction) action).object.id).append(' ')
					 			.append( byteStream.size());
					 		objOut.writeObject(strB.toString());
					 		GameIO.writePlayerToStreamObject(objOut, ((GamePlayerEditAction)action).object);
					 		//byteStream.writeTo(objOut);
					    	//byteStream.reset();
					     	strB.setLength(0);
					   }
					   else if (action instanceof UsertextMessageAction)
					   {
					    	strB.append(NetworkString.ACTION).append(' ')
					 			.append(NetworkString.TEXTMESSAGE).append(' ')
					 			.append(id).append(' ')
					 			.append(action.source).append(' ')
					 			.append(((UsertextMessageAction) action).player);
					 		objOut.writeObject(strB.toString());
					 		objOut.writeObject(((UsertextMessageAction) action).message);
					     	strB.setLength(0);
					    }
					    else if (action instanceof UserSoundMessageAction)
					    {
					    	strB.append(NetworkString.ACTION).append(' ')
					 			.append(NetworkString.SOUNDMESSAGE).append(' ')
					 			.append(((UsertextMessageAction) action).player).append(' ')
					 			.append(id).append(' ')
					 			.append(action.source).append(' ')
					 			.append(((GameObjectInstanceEditAction) action).player.id).append(' ')
					 			.append(((GameObjectInstanceEditAction) action).object.id).append(' ');
					 		objOut.writeObject(strB.toString());
					 		objOut.writeObject(((UsertextMessageAction) action).message);
					     	strB.setLength(0);
					    }
					}
				    catch ( Exception e ) {
				    	logger.error("Error at emmiting Game Action", e);
				    }
			    }
			}catch(IOException e)
			{
		    	logger.error("Error at input Loop", e);
			}
		}
	}
	
	public void inputLoop()
	{
		ArrayList<String> split = new ArrayList<>();
		byte data[] = UniqueObjects.EMPTY_BYTE_ARRAY;
		//Scanner in = new Scanner( input);
		if (objIn == null)
		{
			if (input instanceof ObjectInputStream)
			{
				objIn = (ObjectInputStream)input;
			}
			else
			{
				try {
					objIn = new ObjectInputStream(input);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		if (objIn == null)
		{
			return;
		}
		while (true)
		{
			try {
				//String line = in.nextLine();
				Object inputObject = null;
				try
				{
					inputObject = objIn.readObject();
				}catch(OptionalDataException e)
				{
					logger.error("Can't extract object", e);
					if (stopOnError )
					{
						return;
					}
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
										data = ArrayUtil.ensureLength(data, size);
										objIn.readFully(data, 0, size);
										GameIO.editGameInstanceFromZip(new ByteArrayInputStream(data, 0, size), gi, this);
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
								int size = Integer.parseInt(split.get(7));
								
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
							}
						}
						else if (split.get(1).equals(NetworkString.EDIT) && split.get(2).equals(NetworkString.PLAYER))
						{
							int sourceId = Integer.parseInt(split.get(4));
							if (sourceId != id)
							{
								int sourcePlayerId = Integer.parseInt(split.get(5));
								int playerId = Integer.parseInt(split.get(6));
								int size = Integer.parseInt(split.get(7));
								data = ArrayUtil.ensureLength(data, size);
								
								//objIn.readFully(data, 0, size);
								Player object = gi.getPlayerById(playerId);
								if (object != null)
								{
									GameIO.editPlayerFromStreamObject(objIn, object);
									//GameIO.editPlayerFromStreamZip(new ByteArrayInputStream(data, 0, size), object);
								}
								else
								{
									object = new Player("",  playerId);
									GameIO.editPlayerFromStreamObject(objIn, object);
									gi.addPlayer(object);
									//gi.addPlayer(GameIO.readPlayerFromStream(new ByteArrayInputStream(data, 0, size)));
								}
								Player sourcePlayer = gi.getPlayerById(sourcePlayerId);
								if (sourcePlayer == null)
								{
									logger.error("Can't find player: " + sourcePlayerId);
								}
								else
								{
									gi.update(new GamePlayerEditAction(sourceId, sourcePlayer, object));
								}
							}
						}
						else if (split.get(1).equals(NetworkString.TEXTMESSAGE))
						{
							int id = Integer.parseInt(split.get(2));
							int sourceId = Integer.parseInt(split.get(3));
							if (sourceId != id)
							{
								int playerId = Integer.parseInt(split.get(4));
								gi.update(new UsertextMessageAction(sourceId, playerId, (String)objIn.readObject()));
							}
						}
					}
					break;
				}
			}catch(Exception e) {
				logger.error("Exception in input loop", e);
				if (e instanceof EOFException)
				{
					return;
				}
			}
			split.clear();
		}
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
