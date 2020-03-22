package net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Scanner;

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
	private final int id = (int)System.nanoTime();

	@Override
	public void changeUpdate(GameAction action) {
		if (action.source != id)
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
		PrintWriter writer = new PrintWriter(output, true);
		if (Thread.currentThread() == outputThread)
		{
			while (true)
			{
				String command;
				synchronized(queuedCommands)
				{
					command = queuedCommands.size() == 0 ? null : queuedCommands.pop();
				}
				if (command != null)
				{
					StringUtils.split(command, ' ', split);
					switch (split.get(1))
				    {
				    	case "list":
				    	{
				    		switch (split.get(2))
				    		{
				    			case "player":
				    			{
				    				writer.write("write list player");
				    				for (int i = 0; i < gi.players.size(); ++i)
				    				{
				    					writer.write(gi.players.get(i).name);
				    				}
				    				writer.flush();
				    			}
				    		}
				    	}
				    	case "hash":
				    	{
			    			writer.write("write hash ");
			    			writer.write(gi.hashCode());
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
				    	case "read":
				    	{
				    		switch(split.get(1))
				    		{
				    			case "gameinstance":
				    			{
				    				writer.print("write zip");
				    				writer.flush();
				    				try {
										GameIO.saveSnapshotToZip(gi, output);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
				    				break;
				    			}
				    			case "gameobject":
				    			{
				    				writer.print("write zip");
				    				writer.flush();
				    				GameObject go = gi.game.getObject(split.get(3));
				    				//GameIO.saveObjectInstance(go, output);
				    				break;
				    			}
				    			case "gameobjectinstance":
				    			{
				    				writer.print("write zip");
				    				writer.flush();
				    				ObjectInstance oi = gi.getObjectInstance(Integer.parseInt(split.get(1)));
				    				GameIO.writeObjectInstanceToStream(oi, output);
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
					    	writer.print("action edit " );
					    	writer.print(id);
					    	writer.print(' ');
					    	writer.print(action.source);
					    	writer.print(' ');
					    	writer.print(((GameObjectInstanceEditAction) action).player.id);
					    	writer.print(' ');
					    	writer.print(((GameObjectInstanceEditAction) action).object.id);
					    	writer.flush();
					 		GameIO.writeObjectInstanceToStream(((GameObjectInstanceEditAction)action).object, output);
				 		}
					    else if (action instanceof UsertextMessageAction)
					    {
					    	writer.print("action message ");
					    	writer.print(action.source);
					    	writer.print(' ');
					    	writer.print(((UsertextMessageAction) action).player);
					    	writer.print(' ');
					    	writer.print(((UsertextMessageAction) action).message);
					    }
					    else if (action instanceof UserSoundMessageAction)
					    {
					    	writer.print("action message ");
					    	writer.print(action.source);
					    	writer.print(' ');
					    	writer.print(((UsertextMessageAction) action).player);
					    	writer.print(' ');
					    	writer.print(((UsertextMessageAction) action).message);
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
			}
		}
		else if (Thread.currentThread() == inputThread)
		{
			Scanner in = new Scanner( input);
			while (true)
			{
				try {
					Thread.sleep(100);
					String line = in.nextLine();
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
					else if (line.startsWith("action edit"))
					{
						StringUtils.split(line, ' ', split);
						int sourceId = Integer.parseInt(split.get(2));
						if (sourceId != id)
						{
							int playerId = Integer.parseInt(split.get(3));
							int objectId = Integer.parseInt(split.get(4));
							if (sourceId != id)
							{
								GameIO.editObjectInstanceFromStream(gi.getObjectInstance(objectId), input);
							}
							gi.update(new GameObjectInstanceEditAction(sourceId, gi.getPlayer(playerId), gi.getObjectInstance(objectId)));
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
					split.clear();
					
				    line = in.nextLine();
				    StringUtils.split(line, ' ', split);
				}catch(Exception e) {
					e.printStackTrace();
				}
		    }
		}
	}
}
