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
	ArrayDeque<GameAction> ga = new ArrayDeque<>();
	ArrayDeque<String> queuedCommands = new ArrayDeque<>();
	boolean isUpdating = false;
	private final int id = (int)System.nanoTime();

	@Override
	public void changeUpdate(GameAction action) {
		if (action.source != id && !isUpdating)
		{
			synchronized(ga)
			{
				ga.add(action);
			}
		}
	}
	
	public AsynchronousGameConnection(GameInstance gi, InputStream input, OutputStream output)
	{
		this.gi = gi;
		gi.changeListener.add(this);
		this.input = input;
		this.output = output;
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
	
	public void run()
	{
		ArrayList<String> split = new ArrayList<>();
		if (Thread.currentThread() == outputThread)
		{
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
				String command;
				synchronized(queuedCommands)
				{
					command = queuedCommands.size() == 0 ? null : queuedCommands.pop();
				}
				try
				{
					if (command != null)
					{
						StringUtils.split(command, ' ', split);
						switch (split.get(1))
					    {
					    	case NetworkString.LIST:
					    	{
					    		switch (split.get(2))
					    		{
					    			case NetworkString.PLAYER:
					    			{
					    				objOut.writeObject("write list player");
					    				objOut.writeObject(gi.getPlayerNames());
					    			}
					    		}
					    	}
					    	case NetworkString.HASH:
					    	{
					    		objOut.writeObject("write hash");
					    		objOut.write(gi.hashCode());
				    			//writer.write("write hash ");
				    			//writer.write(gi.hashCode());
					    	}
					    	case "push":
					    	{
					    		switch (split.get(1))
					    		{
					    			case "gameinstance":
					    				break;
					    			case "gameobject":
					    				break;
					    			case "gameobjectinstance":
					    				break;
					    			case "player":
					    				break;
					    			
					    		}
					    	}
					    	case NetworkString.READ:
					    	{
					    		switch(split.get(1))
					    		{
					    			case NetworkString.GAME_INSTANCE:
					    			{
					    				objOut.writeObject("write zip");
					    				objOut.flush();
					    				try {
											GameIO.writeSnapshotToZip(gi, output);
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
					    				break;
					    			}
					    			case "gameobject":
					    			{
					    				objOut.writeObject("write zip");
					    				objOut.flush();
					    				GameObject go = gi.game.getObject(split.get(3));
					    				//GameIO.saveObjectInstance(go, output);
					    				break;
					    			}
					    			case "gameobjectinstance":
					    			{
					    				objOut.writeObject("write zip");
					    				objOut.flush();
					    				ObjectInstance oi = gi.getObjectInstance(Integer.parseInt(split.get(1)));
										try {
											GameIO.writeObjectInstanceToStream(oi, output);
										} catch (IOException e) {
											e.printStackTrace();
										}
										break;
					    			}
					    		}
					    	}
					    }
					}
					split.clear();
					
					GameAction action;
					synchronized (ga)
					{
						action = ga.size() == 0 ? null : ga.pop();
					}
					Socket server = null;
					
					if (action != null)
					{
					    try
					    {
						    if (action instanceof GameObjectInstanceEditAction)
					 		{
						    	System.out.println("out edit state");
						    	ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
						 		GameIO.writeObjectStateToStream(((GameObjectInstanceEditAction)action).object.state, byteStream);
						    	objOut.writeObject("action edit state " + id + " " + action.source + " " + ((GameObjectInstanceEditAction) action).player.id + " " + ((GameObjectInstanceEditAction) action).object.id + " " + byteStream.size());
						    	objOut.flush();
					 	    	/*writer.print("action edit state " );
						    	writer.print(id);
						    	writer.print(' ');
						    	writer.print(action.source);
						    	writer.print(' ');
						    	writer.print(((GameObjectInstanceEditAction) action).player.id);
						    	writer.print(' ');
						    	writer.print(((GameObjectInstanceEditAction) action).object.id);
						    	writer.print(' ');
						    	writer.print(byteStream.size());
						    	writer.print('\n');
						     	writer.flush();*/
						     	objOut.write(byteStream.toByteArray());
						     	objOut.flush();
						     	output.flush();
						   }
						    else if (action instanceof UsertextMessageAction)
						    {
						    	objOut.writeObject("action message " + action.source + ' ' + ((UsertextMessageAction) action).player + " " + ((UsertextMessageAction) action).message);
						    	/*writer.print("action message ");
						    	writer.print(action.source);
						    	writer.print(' ');
						    	writer.print(((UsertextMessageAction) action).player);
						    	writer.print(' ');
						    	writer.print(((UsertextMessageAction) action).message);*/
						    }
						    else if (action instanceof UserSoundMessageAction)
						    {
						    	objOut.writeObject("action message sound" + action.source + ' ' + ((UserSoundMessageAction) action).player + " " + ((UserSoundMessageAction) action).message);
						    	/*writer.print("action message ");
						    	writer.print(action.source);
						    	writer.print(' ');
						    	writer.print(((UserSoundMessageAction) action).player);
						    	writer.print(' ');
						    	writer.print(((UserSoundMessageAction) action).message);*/
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
		else if (Thread.currentThread() == inputThread)
		{
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
					Thread.sleep(100);
					
					//String line = in.nextLine();
					String line = (String)objIn.readObject();
					System.out.println("inline: " + line);
					if (line.startsWith("read"))
					{
						synchronized(queuedCommands)
						{
							queuedCommands.add(line);
						}	
					}
					else if (line.startsWith("write"))
					{
						StringUtils.split(line, ' ', split);
						String player = split.get(1);
						int id = Integer.parseInt(split.get(2));
						String type = split.get(3);	
					}
					else if (line.startsWith("action edit state"))
					{
						System.out.println("in edit state: " + line);
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
	}
}
