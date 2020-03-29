package io;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import gameObjects.definition.GameObject;
import gameObjects.definition.GameObjectDice;
import gameObjects.definition.GameObjectDice.DiceSideState;
import gameObjects.definition.GameObjectFigure;
import gameObjects.definition.GameObjectToken;
import gameObjects.definition.GameObjectToken.TokenState;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.ObjectState;
import main.Player;
import net.AsynchronousGameConnection;

public class GameIO {

	private static int getVersion() {
		String version = System.getProperty("java.version");
		if(version.startsWith("1.")) {
			version = version.substring(2, 3);
		} else {
			int dot = version.indexOf(".");
			if(dot != -1) { version = version.substring(0, dot); }
		} return Integer.parseInt(version);
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
	
	private static void editStateFromElement(ObjectState state, Element elem)
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

	private static void editGameInstanceFromElement(Element root, GameInstance gi) throws JDOMException, IOException {

		for (Element elem : root.getChildren())
		{
			String name = elem.getName();
			if (name.equals("player"))
			{
				Player player = createPlayerFromElement(elem);
				System.out.println(player);
				gi.addPlayer(player);
			}
			else if (name.equals("name"))
			{
				gi.name = elem.getValue();
			}
			else if (name.equals("object"))
			{
				String uniqueName = elem.getAttributeValue("unique_name");
				ObjectInstance oi = new ObjectInstance(gi.game.getObject(uniqueName), Integer.parseInt(elem.getAttributeValue("id")));
				editStateFromElement(oi.state, elem);
				gi.addObjectInstance(oi);
			}
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
				ArrayList<DiceSideState> dss = new ArrayList<>();
				for (Element side : elem.getChildren())
				{
					dss.add(new DiceSideState(Integer.parseInt(side.getAttributeValue("value")), images.get(side.getValue())));
				}
				return new GameObjectDice(elem.getAttributeValue("unique_name"), elem.getAttributeValue("type"), width, height, dss.toArray(new DiceSideState[dss.size()]));
			}
		}
		return null;
	}

	private static Element createElementFromGameObjectInstance(ObjectInstance gameObjectInstance)
	{
		Element elem = new Element("object");
		elem.setAttribute("unique_name", gameObjectInstance.go.uniqueName);
		elem.setAttribute("id", Integer.toString(gameObjectInstance.id));
		writeStateToElement(gameObjectInstance.state, elem);
		return elem;
	}

	private static Element createElementFromGameObject(GameObject gameObject, HashMap<String, BufferedImage> images)
	{
		Element elem = new Element("object");
		elem.setAttribute("type", gameObject.objectType);
		elem.setAttribute("unique_name", gameObject.uniqueName);
		elem.setAttribute("width", Integer.toString(gameObject.widthInMM));
		elem.setAttribute("height", Integer.toString(gameObject.heightInMM));
		if (gameObject instanceof GameObjectToken)
		{
			GameObjectToken token = (GameObjectToken) gameObject;
			elem.setAttribute("value", Integer.toString(token.value));
			for (String key : images.keySet())
			{
				if(images.get(key).equals(token.getUpsideLook()))//TODO slow
				{
					elem.setAttribute("front", key);
					break;
				}
			}
			if (token.getDownsideLook() != null)
			{
				for (String key : images.keySet())
				{
					if(images.get(key).equals(token.getDownsideLook()))
					{
						elem.setAttribute("back", key);
						break;
					}
				}
			}

		}
		else if (gameObject instanceof GameObjectFigure)
		{
			GameObjectFigure figure = (GameObjectFigure) gameObject;
			for (String key : images.keySet())
			{
				if(images.get(key).equals(figure.getStandingLook()))
				{
					elem.setAttribute("standing", key);
					break;
				}
			}
		}
		else if (gameObject instanceof GameObjectDice)
		{
			GameObjectDice dice = (GameObjectDice) gameObject;
			for (DiceSideState sideState : dice.dss)
			{
				Element side = new Element("side");
				side.setAttribute("value", Integer.toString(sideState.value));
				for (Map.Entry<String, BufferedImage> mapEntry : images.entrySet())
				{
					if(mapEntry.getValue().equals(sideState.img)) //TODO is this possible without comparing images?
					{
						side.setText(mapEntry.getKey());
						break;
					}
				}
				elem.addContent(side);
			}
		}
		return elem;
	}

	private static Element createElementFromPlayer(Player player) {
		Element elem = new Element("player");
		elem.setAttribute("name", player.name);
		elem.setAttribute("id", Integer.toString(player.id));
		return elem;
	}

	private static Player createPlayerFromElement(Element elem)
	{
		return new Player(elem.getAttributeValue("name"), Integer.parseInt(elem.getAttributeValue("id")));
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
		    for (HashMap.Entry<String, BufferedImage> pair : game.images.entrySet()) {
		    	String key = pair.getKey();
			    ZipEntry imageZipOutput = new ZipEntry(key);
			    zipOutputStream.putNextEntry(imageZipOutput);

			    if (getVersion() < 9)
		    	{
		    		MemoryCacheImageOutputStream tmp = new MemoryCacheImageOutputStream(zipOutputStream);
		    		ImageIO.write(pair.getValue(), key.substring(key.length() - 3), tmp);
		    		tmp.close();
		    	}
			    else
			    {
			    	ImageIO.write(pair.getValue(), key.substring(key.length() - 3), zipOutputStream);
		    	}
			    zipOutputStream.closeEntry();
		    }
		    
		    // save game.xml
		    Document doc_game = new Document();
	    	Element root_game = new Element("xml");
	    	doc_game.addContent(root_game);

			for (int idx = 0; idx < game.objects.size(); idx++)  {
	        	GameObject entry = game.objects.get(idx);
	        	root_game.addContent(createElementFromGameObject(entry, game.images));
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
	        	ObjectInstance gameObjectInstance = gi.objects.get(idx);
        		root_inst.addContent(createElementFromGameObjectInstance(gameObjectInstance));
        	}
			for (int idx = 0; idx < gi.players.size(); idx++) {
				Player player = gi.players.get(idx);
				root_inst.addContent(createElementFromPlayer(player));
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

	public static void writeGameToZip(Game game, OutputStream os) throws IOException
	{	
		ZipOutputStream zipOutputStream = null;
		try
		{
			zipOutputStream = new ZipOutputStream(os);
			
			// Save all images
		    for (String key : game.images.keySet()) {

			    ZipEntry imageZipOutput = new ZipEntry(key);
			    zipOutputStream.putNextEntry(imageZipOutput);

			    if (key.endsWith(".jpg"))
			    {
			    	ImageIO.write(game.images.get(key), "jpg", zipOutputStream);
			    }
			    else if (key.endsWith(".png"))
			    {
			    	ImageIO.write(game.images.get(key), "png", zipOutputStream);
			    }
			    zipOutputStream.closeEntry();
		    }
		    
			Document doc_game = new Document();
	    	Element root_game = new Element("xml");
	    	doc_game.addContent(root_game);

	        for (int idx = 0; idx < game.objects.size(); idx++) {
	        	GameObject entry = game.objects.get(idx);
	        	root_game.addContent(createElementFromGameObject(entry, game.images));
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

	public static void writeObjectStateToZip(ObjectState object, OutputStream output) throws IOException
	{
		Document doc = new Document();
    	Element elem = new Element("object_state");
		writeStateToElement(object, elem);
		doc.addContent(elem);
    	new XMLOutputter(Format.getPrettyFormat()).output(doc, output);
	}

	// TODO fertig machen
	public static void writeObjectInstanceToZip(ObjectInstance game, ByteArrayOutputStream byteStream) {
		// TODO Auto-generated method stub

	}

	// TODO Fragen -> What Object?
	public static void writeObjectToZip(GameObject game, ByteArrayOutputStream byteStream) {
		// TODO Auto-generated method stub

	}

	// TODO fertig machen
	public static void writePlayerToZip(Player player, ByteArrayOutputStream byteStream) {
		// TODO Auto-generated method stub

	}

	public static GameInstance readSnapshotFromZip(InputStream in) throws IOException, JDOMException
	{
		ZipInputStream stream = new ZipInputStream(in);
		GameInstance result = readSnapshotFromZip(stream);
		in.close();
		return result;
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
		editGameInstanceFromZip(new ByteArrayInputStream(gameInstanceBuffer.toByteArray()), result);
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

	public static void editObjectStateFromZip(ObjectState objectState, InputStream input) throws IOException, JDOMException
	{
		Document doc = new SAXBuilder().build(input);
    	Element elem = doc.getRootElement();

    	editStateFromElement(objectState, elem);
	}

	public static void editGameInstanceFromZip(InputStream is, GameInstance gi) throws JDOMException, IOException
	{
		Document doc = new SAXBuilder().build(is);
    	Element root = doc.getRootElement();

		editGameInstanceFromElement(root, gi);
	}

	// Paul sagt wir brauchen irgendwann die AsynchronousGameConnection^^ Bisher brauchen wir sie nicht.
	public static void editGameInstanceFromZip(InputStream InputStream, GameInstance gi,
			AsynchronousGameConnection source) throws JDOMException, IOException {
		editGameInstanceFromZip(InputStream, gi);
	}
	
}
