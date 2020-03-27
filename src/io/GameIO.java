package io;

import gameObjects.definition.GameObject;
import gameObjects.definition.GameObjectDice;
import gameObjects.definition.GameObjectFigure;
import gameObjects.definition.GameObjectToken;
import gameObjects.definition.GameObjectToken.TokenState;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.ObjectState;
import main.Player;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class GameIO {
	public Player readPlayer(ZipInputStream stream)
	{
		return null;
	}
	
	private static void writeStateToElement(ObjectState state, Element elem)
	{
		elem.setAttribute("x", Integer.toString(state.posX));
		elem.setAttribute("y", Integer.toString(state.posY));
		elem.setAttribute("r", Integer.toString(state.rotation));
		elem.setAttribute("owner", Integer.toString(state.owner_id));
		elem.setAttribute("above", Integer.toString(state.aboveInstanceId));
		elem.setAttribute("below", Integer.toString(state.belowInstanceId));
		elem.setAttribute("value", Integer.toString(state.value));
		if (state instanceof TokenState)
    	{
			elem.setAttribute("side", Boolean.toString(((TokenState)state).side));
    	}
		if (state instanceof GameObjectFigure.FigureState)
		{
			elem.setAttribute("standing", Boolean.toString(((GameObjectFigure.FigureState) state).standing));
		}
	}
	
	private static void readStateFromElement(ObjectState state, Element elem)
	{
		state.posX = Integer.parseInt(elem.getAttributeValue("x"));
		state.posY = Integer.parseInt(elem.getAttributeValue("y"));
		String v = elem.getAttributeValue("above");
		if (v != null)
		{
			state.aboveInstanceId = Integer.parseInt(v);
		}
		v = elem.getAttributeValue("below");
		if (v != null)
		{
			state.belowInstanceId = Integer.parseInt(v);
		}
		state.rotation = Integer.parseInt(elem.getAttributeValue("r"));
		Attribute ownerAttribute = elem.getAttribute("owner_id");
		if (ownerAttribute != null)
		{
	        state.owner_id = Integer.parseInt(ownerAttribute.getValue());
		}
		Attribute valueAttribute = elem.getAttribute("value");
		if (valueAttribute != null)
		{
			state.value = Integer.parseInt(valueAttribute.getValue());
		}
		if (state instanceof TokenState && elem.getAttribute("side") != null)
    	{
			((TokenState)state).side = Boolean.parseBoolean(elem.getAttributeValue("side"));
    	}
		if (state instanceof GameObjectFigure.FigureState && elem.getAttribute("standing") != null)
		{
			((GameObjectFigure.FigureState)state).standing = Boolean.parseBoolean(elem.getAttributeValue("standing"));
		}
	}

	private static GameObject createGameObjectFromElement(Element elem, HashMap<String, BufferedImage> images)
	{
		switch(elem.getAttributeValue("type"))
		{
			case "card":
			{
				int width = 66;
				int height = 88;
				int value = 0;
				if(elem.getAttributeValue("width") != null) {
					width = Integer.parseInt(elem.getAttributeValue("width"));
				}
				if(elem.getAttributeValue("height") != null) {
					height = Integer.parseInt(elem.getAttributeValue("height"));
				}
				if(elem.getAttributeValue("value") != null) {
					value = Integer.parseInt(elem.getAttributeValue("value"));
				}
				return new GameObjectToken(elem.getAttributeValue("unique_name"), elem.getAttributeValue("type"), width, height, images.get(elem.getAttributeValue("front")), images.get(elem.getAttributeValue("back")), value);
			}
			case "figure":
			{
				int width = 20;
				int height = 40;
				if(elem.getAttributeValue("width") != null) {
					width = Integer.parseInt(elem.getAttributeValue("width"));
				}
				if(elem.getAttributeValue("height") != null) {
					height = Integer.parseInt(elem.getAttributeValue("height"));
				}
				return new GameObjectFigure(elem.getAttributeValue("unique_name"), elem.getAttributeValue("type"), width, height, images.get(elem.getAttributeValue("standing")));
			}
			case "dice":
			{
				int width = 20;
				int height = 20;
				if(elem.getAttributeValue("width") != null) {
					width = Integer.parseInt(elem.getAttributeValue("width"));
				}
				if(elem.getAttributeValue("height") != null) {
					height = Integer.parseInt(elem.getAttributeValue("height"));
				}
				HashMap<Integer, BufferedImage> sides = new HashMap<>();
				for (Element side : elem.getChildren())
				{
					sides.put(Integer.parseInt(elem.getAttributeValue("value")), images.get(side.getValue()));
				}
				return new GameObjectDice(elem.getAttributeValue("unique_name"), elem.getAttributeValue("type"), width, height, sides);
			}
		}
		return null;
	}

	/* Not written: password, actions, changeListener, TYPES, logger*/
	public static void writeSnapshotToZip(GameInstance gi, OutputStream os) throws IOException
	{
		ZipOutputStream zipOutputStream = null;
		try
		{
			zipOutputStream = new ZipOutputStream(os);
			Game game = gi.game;
			// Save all images
		    Iterator<Entry<String, BufferedImage>> it = game.images.entrySet().iterator();
		    while (it.hasNext()) {
		    	HashMap.Entry<String, BufferedImage> pair = it.next();
		    	String key = pair.getKey();
			    ZipEntry imageZipOutput = new ZipEntry(key);
			    zipOutputStream.putNextEntry(imageZipOutput);

			    if (key.endsWith(".jpg"))
			    {
			    	ImageIO.write(pair.getValue(), "jpg", zipOutputStream);
			    }
			    else if (key.endsWith(".png"))
			    {
			    	ImageIO.write(pair.getValue(), "png", zipOutputStream);
			    }
			    zipOutputStream.closeEntry();
		    }
		    
		    // save game.xml
		    Document doc_game = new Document();
	    	Element root_game = new Element("xml");
	    	doc_game.addContent(root_game);

			for (int idx = 0; idx < game.objects.size(); idx++)  {
	        	GameObject entry = game.objects.get(idx);
	        	Element elem = new Element("object");
				elem.setAttribute("type", entry.objectType);
				elem.setAttribute("unique_name", entry.uniqueName);
				elem.setAttribute("width", Integer.toString(entry.widthInMM));
				elem.setAttribute("height", Integer.toString(entry.heightInMM));
	        	if (entry instanceof GameObjectToken)
	        	{
	        		GameObjectToken token = (GameObjectToken) entry;
					elem.setAttribute("value", Integer.toString(token.value));
	        		for (String key : game.images.keySet())
	        		{
	        			if(game.images.get(key).equals(token.getUpsideLook()))
	        			{
	        				elem.setAttribute("front", key);
	        				break;
	        	        }
	        		}
	        		if (token.getDownsideLook() != null)
	        		{
						for (String key : game.images.keySet())
						{
							if(game.images.get(key).equals(token.getDownsideLook()))
							{
								elem.setAttribute("back", key);
								break;
							}
						}
					}

	        	}
	        	else if (entry instanceof GameObjectFigure)
	        	{
					GameObjectFigure figure = (GameObjectFigure) entry;
					for (String key : game.images.keySet())
					{
						if(game.images.get(key).equals(figure.getStandingLook()))
						{
							elem.setAttribute("standing", key);
							break;
						}
					}
				}
				else if (entry instanceof GameObjectDice)
				{
					GameObjectDice dice = (GameObjectDice) entry;
					for (Integer side_idx : dice.sides.keySet())
					{
						Element side = new Element("side");
						side.setAttribute("value", Integer.toString(side_idx));
						for (String key : game.images.keySet())
						{
							if(game.images.get(key).equals(dice.sides.get(side_idx)))
							{
								side.setText(key);
								break;
							}
						}
						elem.addContent(side);
					}
				}
	        	root_game.addContent(elem);
	        }
	        
	        Element elem_back = new Element("background");
	        for (String key : game.images.keySet())
    		{
    			if(game.images.get(key).equals(game.background)) 
    			{
    				elem_back.setText(key);
    				break;
    	        }
    		}
	        root_game.addContent(elem_back);
	    	
	    	ZipEntry gameZipOutput = new ZipEntry("game.xml");
	    	zipOutputStream.putNextEntry(gameZipOutput);
	    	new XMLOutputter(Format.getPrettyFormat()).output(doc_game, zipOutputStream);
	    	zipOutputStream.closeEntry();

		    // save game_instance.xml
	    	Document doc_inst = new Document();
	    	Element root_inst = new Element("xml");

			for (int idx = 0; idx < gi.objects.size(); idx++) {
	        	ObjectInstance gameObject = gi.objects.get(idx);
	        	Element elem = new Element("object");
        		elem.setAttribute("unique_name", gameObject.go.uniqueName);
        		elem.setAttribute("id", Integer.toString(gameObject.id));
        		writeStateToElement(gameObject.state, elem);
        		root_inst.addContent(elem);
        	}
			for (int idx = 0; idx < gi.players.size(); idx++) {
				Player player = gi.players.get(idx);
				Element elem = new Element("player");
				writePlayerToElement(player, elem);
				root_inst.addContent(elem);
			}
	        Element sessionName = new Element("name");
	        sessionName.setText(gi.name);
	        root_inst.addContent(sessionName);

			Element hidden = new Element("hidden");
			hidden.setText(String.valueOf(gi.hidden));
			root_inst.addContent(hidden);
	    	
	        doc_inst.addContent(root_inst);
	    	ZipEntry xmlZipOutput = new ZipEntry("game_instance.xml");
	    	zipOutputStream.putNextEntry(xmlZipOutput);
	    	new XMLOutputter(Format.getPrettyFormat()).output(doc_inst, zipOutputStream);
	    	zipOutputStream.closeEntry();
		}
		finally
		{
			if (zipOutputStream != null)
			{
				zipOutputStream.close();
			}
		}
	}

	private static void writePlayerToElement(Player player, Element elem) {
		elem.setAttribute("name", player.name);
		elem.setAttribute("id", Integer.toString(player.id));
	}

	public static void writeGameToZip(Game game, OutputStream os) throws IOException
	{	
		ZipOutputStream zipOutputStream = null;
		try
		{
			zipOutputStream = new ZipOutputStream(os);
			
			// Save all images
		    Iterator<Entry<String, BufferedImage>> it = game.images.entrySet().iterator();
		    while (it.hasNext()) {
		    	HashMap.Entry<String, BufferedImage> pair = it.next();
		        //System.out.println(pair.getKey() + " = " + pair.getValue());
		    
			    ZipEntry imageZipOutput = new ZipEntry(pair.getKey());
			    zipOutputStream.putNextEntry(imageZipOutput);

			    if (pair.getKey().endsWith(".jpg"))
			    {
			    	ImageIO.write(pair.getValue(), "jpg", zipOutputStream);
			    }
			    else if (pair.getKey().endsWith(".png"))
			    {
			    	ImageIO.write(pair.getValue(), "png", zipOutputStream);
			    }
			    zipOutputStream.closeEntry();
		    }
		    
			Document doc_game = new Document();
	    	Element root_game = new Element("xml");
	    	doc_game.addContent(root_game);
	    	
	    	Iterator<GameObject> gameIt = game.objects.iterator();
	        while (gameIt.hasNext()) {
	        	GameObject entry = gameIt.next();
	        	Element elem = new Element("object");
	        	if (entry instanceof GameObjectToken)
	        	{
	        		GameObjectToken card = (GameObjectToken) entry;
	        		elem.setAttribute("type", "card");
	        		elem.setAttribute("unique_name", card.uniqueName);
	        		for (String key : game.images.keySet())
	        		{
	        			if(game.images.get(key).equals(card.getUpsideLook())) 
	        			{
	        				elem.setAttribute("front", key);
	        				break;
	        	        }
	        		}
	        		
	        		for (String key : game.images.keySet())
	        		{
	        			if(game.images.get(key).equals(card.getDownsideLook())) 
	        			{
	        				elem.setAttribute("back", key);
	        				break;
	        	        }
	        		}
	        	}
	        	root_game.addContent(elem);
	        }
	        
	        Element elem_back = new Element("background");
	        for (String key : game.images.keySet())
			{
				if(game.images.get(key).equals(game.background)) 
				{
					elem_back.setText(key);
					break;
		        }
			}
	        root_game.addContent(elem_back);
	    	
	        ZipEntry gameZipOutput = new ZipEntry("game.xml");
	    	zipOutputStream.putNextEntry(gameZipOutput);
	    	new XMLOutputter(Format.getPrettyFormat()).output(doc_game, zipOutputStream);
	    	zipOutputStream.closeEntry();
		}
		finally
		{
			if (zipOutputStream != null)
			{
				zipOutputStream.close();
			}
		}
	}

	public static void writeObjectStateToStream(ObjectState object, OutputStream output) throws IOException
	{
		Document doc = new Document();
    	
    	Element elem = new Element("object_state");
		writeStateToElement(object, elem);

		doc.addContent(elem);
    	new XMLOutputter(Format.getPrettyFormat()).output(doc, output);

	}

	// TODO Fragen
	public static GameInstance readSnapshotFromZip(InputStream in) throws IOException, JDOMException
	{
		ZipInputStream stream = new ZipInputStream(in);
		GameInstance result = readSnapshotFromZip(stream);
		in.close();
		return result;
	}

	// TODO Fragen
	public static void editGameInstanceFromZip(ZipInputStream stream, GameInstance game, Object source)
	{
		//Editiere nur das was in dem Stream steht
		//rufe dabei die update funktion des games auf, um ﾃｼber die ﾃ､nderungen mitzuteilen
		//Rufe dabei auch die update Methode auf 
	}

	// TODO Fragen
	public static void editObjectInstanceFromZip(ObjectInstance objectInstance, InputStream input) throws IOException {
		ZipInputStream zipStream = new ZipInputStream(input);
		editObjectInstanceFromZip(objectInstance, zipStream);
		zipStream.close();
	}

	// TODO Fragen
	public static void editObjectInstanceFromZip(ObjectInstance objectInstance, ZipInputStream in) {
		
	}

	public static void editObjectStateFromStream(ObjectState objectState, InputStream input) throws IOException, JDOMException
	{
		Document doc = new SAXBuilder().build(input);
    	Element elem = doc.getRootElement();

    	readStateFromElement(objectState, elem);
	}

	public static GameInstance readSnapshotFromZip(ZipInputStream stream) throws IOException, JDOMException
	{
		Game game = new Game();
		HashMap<String, BufferedImage> images = game.images;
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
    	
    
    	for (Element elem : root.getChildren())
    	{
    		String name = elem.getName();
    		if (name.equals("object"))
    		{
				game.objects.add(createGameObjectFromElement(elem, game.images));
    		}
    		else if (name.equals("background"))
    		{
    			game.background = images.get(elem.getValue());
    		}
	   	}
    	GameInstance result = new GameInstance(game);
    	readGameInstanceFromStream(new ByteArrayInputStream(gameInstanceBuffer.toByteArray()), result);
    	return result;
	}
	

	public static void readGameInstanceFromStream(InputStream is, GameInstance gi) throws JDOMException, IOException
	{
		Document doc = new SAXBuilder().build(is);
    	Element root = doc.getRootElement();
    	
    	for (Element elem : root.getChildren())
    	{
    		String name = elem.getName();
    		if (name.equals("name"))
    		{
	    		gi.name = elem.getValue();	
    		}
    		else if (name.equals("object"))
    		{
    			String uniqueName = elem.getAttributeValue("unique_name");
    			ObjectInstance oi = new ObjectInstance(gi.game.getObject(uniqueName), Integer.parseInt(elem.getAttributeValue("id")));
    			readStateFromElement(oi.state, elem);
    			gi.objects.add(oi);
    		}
	   	}
	}

	public static void writeObjectInstanceToZip(Game game, ByteArrayOutputStream byteStream) {
		// TODO Auto-generated method stub
		
	}

	public static void writeObjectToZip(Game game, ByteArrayOutputStream byteStream) {
		// TODO Auto-generated method stub
		
	}

	public static void writePlayerToZip(Player player, ByteArrayOutputStream byteStream) {
		// TODO Auto-generated method stub
		
	}
	
}
