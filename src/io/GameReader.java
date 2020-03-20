package io;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import gameObjects.GameObjectCard;
import main.Game;
import main.GameInstance;
import main.ObjectInstance;

public class GameReader {
	
	
	public static GameInstance readGame(InputStream in) throws IOException, JDOMException
	{
		ZipInputStream stream = new ZipInputStream(in);
		GameInstance result = readGame(stream);
		in.close();
		return result;
	}
	
	public static GameInstance readGame(ZipInputStream stream) throws IOException, JDOMException
	{
		HashMap<String, BufferedImage> images = new HashMap<>();
		ByteArrayOutputStream gameBuffer = new ByteArrayOutputStream();
		ByteArrayOutputStream gameInstanceBuffer = new ByteArrayOutputStream();
		try
	    {
			ZipEntry entry;
	        while((entry = stream.getNextEntry())!=null)
	        {
	        	String name = entry.getName();
	            if (name.endsWith(".png") || name.endsWith(".jpg"))
	            {
	            	BufferedImage img = ImageIO.read(stream);
	            	System.out.println("put " + name);
	            	images.put(name, img);
	            }
	            else if (name.equals("game.xml"))
	            {
            	    int nRead;
            	    byte[] data = new byte[1024];
            	    while ((nRead = stream.read(data, 0, data.length)) != -1) {
            	        gameBuffer.write(data, 0, nRead);
            	    }
            	    
	            }
	            else if (name.equals("game_instance.xml"))
	            {
            	    int nRead;
            	    byte[] data = new byte[1024];
            	    while ((nRead = stream.read(data, 0, data.length)) != -1) {
            	        gameInstanceBuffer.write(data, 0, nRead);
            	    }
            	    
	            }
	        }
	    }
	    finally
	    {
	        stream.close();
	    }
		Document doc = new SAXBuilder().build(new ByteArrayInputStream(gameBuffer.toByteArray()));
    	Element root = doc.getRootElement();
    	
    	Game game = new Game();

    	for (Element elem : root.getChildren())
    	{
    		String name = elem.getName();
    		if (name.equals("object"))
    		{
    			switch(elem.getAttributeValue("type"))
    			{
    				case "card":
    				{
    					game.objects.add(new GameObjectCard(elem.getAttributeValue("id"), images.get(elem.getAttributeValue("front")), images.get(elem.getAttributeValue("back"))));
    					break;
    				}
    			}
    		}
    		else if (name.equals("background"))
    		{
    			System.out.println(elem.getValue());
    			game.background = images.get(elem.getValue());
    		}
    		System.out.println(name);
	   	}
    	GameInstance result = new GameInstance(game);
    	readGameInstance(new ByteArrayInputStream(gameInstanceBuffer.toByteArray()), result);
    	return result;
	}
	
	public static void readGameInstance(InputStream is, GameInstance gi) throws JDOMException, IOException
	{
		Document doc = new SAXBuilder().build(is);
    	Element root = doc.getRootElement();
    	
    	for (Element elem : root.getChildren())
    	{
    		String name = elem.getName();
    		System.out.println("name" + name);
    		if (name.equals("object"))
    		{
    			String typeId = elem.getAttributeValue("type");
    			ObjectInstance oi = new ObjectInstance(gi.game.getObject(typeId), Integer.parseInt(elem.getAttributeValue("id")));
    			oi.state.posX = Integer.parseInt(elem.getAttributeValue("x"));
    			oi.state.posY = Integer.parseInt(elem.getAttributeValue("y"));
    			oi.state.rotation = Integer.parseInt(elem.getAttributeValue("r"));
    			gi.objects.add(oi);
    		}
	   	}
	}
}
