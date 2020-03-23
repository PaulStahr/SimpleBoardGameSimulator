package net;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

import gameObjects.GameAction;
import gameObjects.GameObjectInstanceEditAction;
import gameObjects.UserSoundMessageAction;
import gameObjects.UsertextMessageAction;
import gameObjects.definition.GameObject;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.GameInstance.GameChangeListener;
import io.GameIO;
import util.StringUtils;

public class AsynchronousGameConnection implements Runnable, GameChangeListener{
	GameInstance gi;
	Thread outputThread;
	Thread inputThread;
	InputStream input;
	OutputStream output;
	ArrayDeque<Object> queuedOutputs = new ArrayDeque<>();
	boolean isUpdating = false;
	private final int id = (int)System.nanoTime();

	@Override
	public void changeUpdate(GameAction action) {
		if (action.source != id && !isUpdating)
		{
			queueOutput(action);
		}
	}
	
	private void queueOutput(Object output)
	{
		synchronized(queuedOutputs)
		{
			queuedOutputs.add(output);
			queuedOutputs.notifyAll();
		}
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
		gi.changeListener.add(this);
		this.input = input;
		this.output = output;
	}
	
	public void syncPull()
	{
		queueOutput(NetworkString.PULL);
	}
	
	public void syncPush()
	{
		queueOutput(NetworkString.PUSH);
	}
	
	public void start()
	{
		if (outputThread == null)
		{
			outputThread = new Thread(this);
			outputThread.start();
			inputThread = new Thread(this);
			inputThread.start();
		}
	}
	
	private void outputLoop()
	{
		ArrayList<String> split = new ArrayList<>();
		StringBuilder strB = new StringBuilder();
		//PrintWriter writer = new PrintWriter(output, true);
		ObjectOutputStream objOut = null;
		try {
			objOut = new ObjectOutputStream(output);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	while (true)
		{
			Object outputObject = null;
			synchronized(queuedOutputs)
			{
				if (queuedOutputs.size() == 0)
				{
					try {
						objOut.flush();
						output.flush();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						queuedOutputs.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				outputObject = queuedOutputs.size() == 0 ? null : queuedOutputs.pop();
			}
			try
			{
				if (outputObject instanceof String)
				{
					String command = (String)outputObject;
					StringUtils.split(command, ' ', split);
					switch (split.get(1))
				    {
				    	case NetworkString.LIST:
				    	{
				    		switch (split.get(2))
				    		{
				    			case NetworkString.PLAYER:
				    			{
				    				strB.append(NetworkString.READBACK).append(' ').append(NetworkString.PLAYER);
				    				objOut.writeObject(strB.toString());
				    				objOut.writeObject(gi.getPlayerNames());
				    				strB.setLength(0);
				    			}
				    		}
				    	}
				    	case NetworkString.HASH:
				    	{
				    		strB.append(NetworkString.READBACK).append(' ').append(NetworkString.HASH).append(' ');
				    		objOut.writeObject(strB.toString());
		    				objOut.write(gi.hashCode());
		    				strB.setLength(0);
				    	}
				    	case NetworkString.PULL:
				    	case NetworkString.READ:
				    	{
				    		strB.append(split.get(1).equals(NetworkString.PULL) ? NetworkString.PUSH : NetworkString.READBACK).append(' ');
				    		switch (split.get(1))
				    		{
				    			case NetworkString.GAME_INSTANCE:
				    			{
				    				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				    				GameIO.writeSnapshotToZip(gi, byteStream);
				    				strB.append(NetworkString.ZIP).append(' ').append(NetworkString.GAME_INSTANCE).append(' ').append(byteStream.size());
				    				objOut.writeObject(strB.toString());
				    				objOut.write(byteStream.toByteArray());
				    				strB.setLength(0);
				    				break;
				    			}
				    			case NetworkString.GAME:
				    			{
				    				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				    				GameIO.writeGameToZip(gi.game, byteStream);
				    				strB.append(NetworkString.ZIP).append(' ').append(NetworkString.GAME).append(' ').append(byteStream.size());
				    				objOut.writeObject(strB.toString());
				    				objOut.write(byteStream.toByteArray());
				    				strB.setLength(0);
				    				break;
				    			}
				    			case NetworkString.GAME_OBJECT:
				    			{
				    				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				    				GameIO.writeObjectToZip(gi.game, byteStream);
				    				strB.append(NetworkString.ZIP).append(' ').append(NetworkString.GAME_OBJECT).append(' ').append(byteStream.size());
				    				objOut.writeObject(strB.toString());
				    				objOut.write(byteStream.toByteArray());
				    				strB.setLength(0);
				    				break;
				    			}
				    			case NetworkString.GAME_OBJECT_INSTANCE:
				    			{
				    				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				    				GameIO.writeObjectInstanceToZip(gi.game, byteStream);
				    				strB.append(NetworkString.ZIP).append(' ').append(NetworkString.GAME_OBJECT_INSTANCE).append(' ').append(byteStream.size());
				    				objOut.write(byteStream.toByteArray());
				    				break;
				    			}
				    			case NetworkString.PLAYER:
				    			{
				    				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				    				GameIO.writePlayerToZip(gi.getPlayer(Integer.parseInt(split.get(2))), byteStream);
				    				strB.append(NetworkString.ZIP).append(' ').append(NetworkString.PLAYER).append(' ').append(byteStream.size());
				    				objOut.write(byteStream.toByteArray());
				    				break;
				    			}
				    		}
				    	}
				    }
				}
				split.clear();
				
				
				Socket server = null;
				
				if (outputObject instanceof GameAction)
				{
					GameAction action = (GameAction)outputObject;
				    try
				    {
					    if (action instanceof GameObjectInstanceEditAction)
				 		{
					    	ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
					 		GameIO.writeObjectStateToStream(((GameObjectInstanceEditAction)action).object.state, byteStream);
					 		strB.append(NetworkString.ACTION).append(' ')
					 			.append(NetworkString.EDIT).append(' ')
					 			.append(NetworkString.STATE).append(' ')
					 			.append(id).append(' ')
					 			.append(action.source).append(' ')
					 			.append(((GameObjectInstanceEditAction) action).player.id).append(' ')
					 			.append(((GameObjectInstanceEditAction) action).object.id).append(' ')
					 			.append( byteStream.size());
					 		objOut.writeObject(strB.toString());
					    	objOut.write(byteStream.toByteArray());
					     	strB.setLength(0);
					   }
					   else if (action instanceof UsertextMessageAction)
					   {
					    	strB.append(NetworkString.ACTION).append(' ')
					 			.append(NetworkString.TEXTMESSAGE).append(' ')
					 			.append(((UsertextMessageAction) action).player).append(' ')
					 			.append(id).append(' ')
					 			.append(action.source).append(' ')
					 			.append(((GameObjectInstanceEditAction) action).player.id).append(' ')
					 			.append(((GameObjectInstanceEditAction) action).object.id).append(' ');
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
				    	e.printStackTrace();
				    }
				    finally {
				    	if ( server != null )
				    	{
				    		try { server.close(); } catch ( IOException e ) { }
				    	}
				    }
					//server = new Socket( address, port);
					
					//Scanner in  = new Scanner( server.getInputStream() );
					//PrintWriter out = new PrintWriter( server.getOutputStream(), true );
				
			    }
			}catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void inputLoop()
	{
		ArrayList<String> split = new ArrayList<>();
		//Scanner in = new Scanner( input);
		ObjectInputStream objIn = null;
		try {
			objIn = new ObjectInputStream(input);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (true)
		{
			try {
				//String line = in.nextLine();
				String line = (String)objIn.readObject();
				System.out.println("inline: " + line);
				if (line.startsWith(NetworkString.READ))
				{
					synchronized(queuedOutputs)
					{
						queuedOutputs.add(line);
						queuedOutputs.notifyAll();
					}	
				}
				else if (line.startsWith(NetworkString.WRITE))
				{
					StringUtils.split(line, ' ', split);
					String player = split.get(1);
					int id = Integer.parseInt(split.get(2));
					String type = split.get(3);	
				}
				else if (line.startsWith(NetworkString.ACTION + " " + NetworkString.EDIT + " " + NetworkString.STATE))
				{
					split.clear();
					StringUtils.split(line, ' ', split);
					int sourceId = Integer.parseInt(split.get(4));
					if (sourceId != id)
					{
						int playerId = Integer.parseInt(split.get(5));
						int objectId = Integer.parseInt(split.get(6));
						int size = Integer.parseInt(split.get(7));
						byte data[] = new byte[size];
						
						objIn.read(data, 0, size);
						//byte data[] = objIn.readObject();
						if (sourceId != id)
						{
							GameIO.editObjectStateFromStream(gi.getObjectInstance(objectId).state, new ByteArrayInputStream(data));
						}
						isUpdating = true;
						gi.update(new GameObjectInstanceEditAction(sourceId, gi.getPlayer(playerId), gi.getObjectInstance(objectId)));
						isUpdating = false;
					}
				}
				else if (line.startsWith("action message"))
				{
					StringUtils.split(line, ' ', split);
					int sourceId = Integer.parseInt(split.get(2));
					if (sourceId != id)
					{
						int playerId = Integer.parseInt(split.get(3));
						gi.update(new UsertextMessageAction(sourceId, playerId, split.get(4)));
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			split.clear();
		}
	}
	
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
