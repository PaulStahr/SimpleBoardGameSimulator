package io;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.definition.GameObject;
import gameObjects.definition.GameObjectDice;
import gameObjects.definition.GameObjectDice.DiceSide;
import gameObjects.definition.GameObjectDice.DiceState;
import gameObjects.definition.GameObjectFigure;
import gameObjects.definition.GameObjectFigure.FigureState;
import gameObjects.definition.GameObjectToken;
import gameObjects.definition.GameObjectToken.TokenState;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.ObjectState;
import main.Player;
import net.AsynchronousGameConnection;
import util.ArrayUtil;
import util.io.StreamUtil;

public class GameIO {

	private static Logger logger = LoggerFactory.getLogger(GameIO.class);

	/**
	 * Returns the Java version used by the system.
	 * @return the Java version used by the system
	 */
	private static int getVersion() {
		String version = System.getProperty("java.version");
		if(version.startsWith("1.")) {
			version = version.substring(2, 3);
		} else {
			int dot = version.indexOf('.');
			if(dot != -1) { version = version.substring(0, dot); }
		} return Integer.parseInt(version);
	}

	/**
	 * Write an ObjectState to an XML-Element. All fields of @param state
	 * will be added as Attributes to @param elem. If state is a TokenState
	 * or a FigureState, it will also write the side of standing attribute respectively.
	 * @param state the ObjectState
	 * @param elem the Element
	 */
	private static void writeStateToElement(ObjectState state, Element elem)
	{
		elem.setAttribute(IOString.X, 				Integer.toString(state.posX));
		elem.setAttribute(IOString.Y, 				Integer.toString(state.posY));
		elem.setAttribute(IOString.R, 				Integer.toString(state.rotation));
		elem.setAttribute(IOString.OWNER_ID, 		Integer.toString(state.owner_id));
		elem.setAttribute(IOString.DRAW_VALUE, 		Long.toString(state.drawValue));
		elem.setAttribute(IOString.ABOVE, 			Integer.toString(state.aboveInstanceId));
		elem.setAttribute(IOString.BELOW,			Integer.toString(state.belowInstanceId));
		elem.setAttribute(IOString.VALUE, 			Integer.toString(state.value));
		elem.setAttribute(IOString.ROTATION_STEP, 	Integer.toString(state.rotationStep));
		elem.setAttribute(IOString.IS_FIXED, 		Boolean.toString(state.isFixed));
		if (state instanceof TokenState)
    	{
			elem.setAttribute(IOString.SIDE, Boolean.toString(((TokenState)state).side));
    	}
		if (state instanceof GameObjectFigure.FigureState)
		{
			elem.setAttribute(IOString.STANDING, Boolean.toString(((GameObjectFigure.FigureState) state).standing));
		}
	}

	/**
	 * Edit a ObjectState from an XML Element. The Attributes "x", "y"
	 * and "r" are needed in @param elem. All others are optional.
	 * @param elem the Element with all needed information
	 * @param state the ObjectState that shall the updated
	 */
	private static void editStateFromElement(ObjectState state, Element elem)
	{
		state.posX = Integer.parseInt(elem.getAttributeValue(IOString.X));
		state.posY = Integer.parseInt(elem.getAttributeValue(IOString.Y));
		state.rotation = Integer.parseInt(elem.getAttributeValue(IOString.R));

		String v = elem.getAttributeValue(IOString.ABOVE);
		if (v != null)
		{
			state.aboveInstanceId = Integer.parseInt(v);
		}
		v = elem.getAttributeValue(IOString.BELOW);
		if (v != null)
		{
			state.belowInstanceId = Integer.parseInt(v);
		}
		Attribute ownerAttribute = elem.getAttribute(IOString.OWNER_ID);
		if (ownerAttribute != null)
		{
	        state.owner_id = Integer.parseInt(ownerAttribute.getValue());
		}
		Attribute drawValueAttribute = elem.getAttribute(IOString.DRAW_VALUE);
		if (drawValueAttribute != null)
		{
			state.drawValue = Long.parseLong(drawValueAttribute.getValue());
		}
		Attribute valueAttribute = elem.getAttribute(IOString.VALUE);
		if (valueAttribute != null)
		{
			state.value = Integer.parseInt(valueAttribute.getValue());
		}
		Attribute rotationStep = elem.getAttribute(IOString.ROTATION_STEP);
		if (rotationStep != null)
		{
			state.rotationStep = Integer.parseInt(rotationStep.getValue());
		}
		Attribute isFixed = elem.getAttribute(IOString.IS_FIXED);
		if (rotationStep != null)
		{
			state.isFixed = Boolean.parseBoolean(isFixed.getValue());
		}
		if (state instanceof TokenState && elem.getAttribute(IOString.SIDE) != null)
    	{
			((TokenState)state).side = Boolean.parseBoolean(elem.getAttributeValue(IOString.SIDE));
    	}
		if (state instanceof GameObjectFigure.FigureState && elem.getAttribute(IOString.STANDING) != null)
		{
			((GameObjectFigure.FigureState)state).standing = Boolean.parseBoolean(elem.getAttributeValue(IOString.STANDING));
		}
	}

	/**
	 * Edit a GameInstance from an XML Element.
	 * ATTENTION: The following fields are not edited: game, actions, changeListener, TYPES, logger
	 * @param root the root Element with all Elements that need updating as children
	 * @param gi the GameInstance that shall the updated
	 */
	private static void editGameInstanceFromElement(Element root, GameInstance gi) throws JDOMException, IOException {

		for (Element elem : root.getChildren())
		{
			String name = elem.getName();
			switch (name)
			{
				case IOString.PLAYER:
					Player player = createPlayerFromElement(elem);
					gi.addPlayer(null, player);
					break;
				case IOString.NAME:gi.name = elem.getValue();break;
				case IOString.SETTINGS:
					gi.name = elem.getAttributeValue(IOString.NAME);
					if (elem.getAttribute(IOString.PRIVATE_AREA) != null) {
						gi.private_area = Boolean.parseBoolean(elem.getAttributeValue(IOString.PRIVATE_AREA));
					}
					if (elem.getAttribute(IOString.TABLE) != null) {
						gi.table = Boolean.parseBoolean(elem.getAttributeValue(IOString.TABLE));
					}
					if (elem.getAttribute(IOString.TABLE_RADIUS) != null) {
						gi.tableRadius = Integer.parseInt(elem.getAttributeValue(IOString.TABLE_RADIUS));
					}
					break;
				case IOString.OBJECT:
					String uniqueName = elem.getAttributeValue(IOString.UNIQUE_NAME);
					ObjectInstance oi = new ObjectInstance(gi.game.getObject(uniqueName), Integer.parseInt(elem.getAttributeValue(IOString.ID)));
					editStateFromElement(oi.state, elem);
					gi.addObjectInstance(oi);
					break;
				case IOString.PASSWORD:gi.password = elem.getValue();break;
				case IOString.HIDDEN:gi.hidden = Boolean.parseBoolean(elem.getValue());break;
			}
		}
	}

	/**
	 * Creates a new GameObject. All needed information will be retrieved
	 * from the XML-element @param elem.
	 * @param elem the Element with all needed information
	 * @param images a HashMap of all images saved in the game
	 * @return the created GameInstance
	 */
	private static GameObject createGameObjectFromElement(Element elem, HashMap<String, BufferedImage> images)
	{
		String uniqueName = elem.getAttributeValue(IOString.UNIQUE_NAME);
		String type = elem.getAttributeValue(IOString.TYPE);
		GameObject result = null;
		int width = 66;
		int height = 88;
		int value = 0;
		int rotationStep = 90;
		int isFixed = 0;
		if(elem.getAttributeValue(IOString.WIDTH) != null) {
			width = Integer.parseInt(elem.getAttributeValue(IOString.WIDTH));
		}
		if(elem.getAttributeValue(IOString.HEIGHT) != null) {
			height = Integer.parseInt(elem.getAttributeValue(IOString.HEIGHT));
		}
		if(elem.getAttributeValue(IOString.VALUE) != null) {
			value = Integer.parseInt(elem.getAttributeValue(IOString.VALUE));
		}
		if(elem.getAttributeValue(IOString.ROTATION_STEP) != null) {
			rotationStep = Integer.parseInt(elem.getAttributeValue(IOString.ROTATION_STEP));
		}
		if(elem.getAttributeValue(IOString.IS_FIXED) != null) {
			isFixed = Integer.parseInt(elem.getAttributeValue(IOString.IS_FIXED));
		}
		switch(type)
		{
			case IOString.CARD:
			{
				result = new GameObjectToken(uniqueName, type, width, height, images.get(elem.getAttributeValue(IOString.FRONT)), images.get(elem.getAttributeValue(IOString.BACK)), value, rotationStep, isFixed);
				break;
			}
			case IOString.FIGURE:
			{
				result = new GameObjectFigure(uniqueName, type, width, height, images.get(elem.getAttributeValue(IOString.STANDING)), value, rotationStep, isFixed);
				break;
			}
			case IOString.DICE:
			{
				ArrayList<DiceSide> dss = new ArrayList<>();
				for (Element side : elem.getChildren())
				{
					if (side.getName().equals(IOString.SIDE))
					{
						BufferedImage img = images.get(side.getValue());
						if (img == null)
						{
							logger.warn("Image not found: ", side.getValue());
						}
						dss.add(new DiceSide(Integer.parseInt(side.getAttributeValue(IOString.VALUE)), img, side.getValue()));
					}
				}
				result = new GameObjectDice(uniqueName, type, width, height, dss.toArray(new DiceSide[dss.size()]), value, rotationStep);
				break;
			}
		}
		ArrayList<String> groups = new ArrayList<String>();
		for (Element child : elem.getChildren())
		{
			if (child.getName().equals(IOString.GROUP))
			{
				groups.add(child.getText());
			}
		}

		result.groups = groups.toArray(new String[groups.size()]);

		return result;
	}

	/**
	 * Creates a new Player. All needed information will be retrieved
	 * from the XML-element @param elem.
	 * @param elem the Element with all needed information
	 * @return the created GameInstance
	 */
	private static Player createPlayerFromElement(Element elem)
	{
		Player result = new Player(
				elem.getAttributeValue(IOString.NAME),
				Integer.parseInt(elem.getAttributeValue(IOString.ID)),
				new Color(Integer.parseInt(elem.getAttributeValue(IOString.COLOR))),
				Integer.parseInt(elem.getAttributeValue(IOString.MOUSE_X)),
				Integer.parseInt(elem.getAttributeValue(IOString.MOUSE_Y)));
		result.screenWidth = Integer.parseInt(elem.getAttributeValue(IOString.SCREEN_W));
		result.screenHeight = Integer.parseInt(elem.getAttributeValue(IOString.SCREEN_H));
		editAffineTransformFromElement(elem.getChild(IOString.AFFINE_TRANSFORM), result.screenToBoardTransformation);
		return result;
	}
	
	private static Player editPlayerFromElement(Element elem, Player player)
	{
		player.setName(elem.getAttributeValue(IOString.NAME));
		player.color = new Color(Integer.parseInt(elem.getAttributeValue(IOString.COLOR)));
		player.mouseXPos = Integer.parseInt(elem.getAttributeValue(IOString.MOUSE_X));
		player.mouseYPos = Integer.parseInt(elem.getAttributeValue(IOString.MOUSE_Y));
		player.screenWidth = Integer.parseInt(elem.getAttributeValue(IOString.SCREEN_W));
		player.screenHeight = Integer.parseInt(elem.getAttributeValue(IOString.SCREEN_H));
		editAffineTransformFromElement(elem.getChild(IOString.AFFINE_TRANSFORM), player.screenToBoardTransformation);
		return player;
	}

	/**
	 * Creates a new Element that represents the @param objectInstance.
	 * ATTENTION: Does not save the fields GameObject go, double scale and Player inHand.
	 * @param objectInstance the ObjectInstance that shall be encoded
	 * @return the created Element
	 */
	private static Element createElementFromObjectInstance(ObjectInstance objectInstance)
	{
		Element elem = new Element(IOString.OBJECT);
		elem.setAttribute(IOString.UNIQUE_NAME, objectInstance.go.uniqueName);
		elem.setAttribute(IOString.ID, Integer.toString(objectInstance.id));
		writeStateToElement(objectInstance.state, elem);
		return elem;
	}

	/**
	 * Creates a new Element that represents the @param gameObject. Additional field
	 * from different child classes (GameObjectToken, GameObjectFigure and GameObjectDice)
	 * are considered. (GameObjectFigure only saves the standing picture as the lying picture
	 * gets created from the standing one)
	 * @param gameObject the GameObject that shall be encoded
	 * @param game the game which contains the HashMap of all images in the game
	 * @return the created Element
	 */
	private static Element createElementFromGameObject(GameObject gameObject, Game game)
	{
		Element elem = new Element(IOString.OBJECT);
		elem.setAttribute(IOString.TYPE, gameObject.objectType);
		elem.setAttribute(IOString.UNIQUE_NAME, gameObject.uniqueName);
		elem.setAttribute(IOString.WIDTH, Integer.toString(gameObject.widthInMM));
		elem.setAttribute(IOString.HEIGHT, Integer.toString(gameObject.heightInMM));
		for (String group : gameObject.groups)
		{
			elem.addContent(new Element(IOString.GROUP).setText(group));
		}
		if (gameObject instanceof GameObjectToken)
		{
			GameObjectToken token = (GameObjectToken) gameObject;
			elem.setAttribute(IOString.VALUE, Integer.toString(token.value));
			elem.setAttribute(IOString.FRONT, game.getImageKey(token.getUpsideLook()));
			if (token.getDownsideLook() != null)
			{
				elem.setAttribute(IOString.BACK, game.getImageKey(token.getDownsideLook()));
			}

		}
		else if (gameObject instanceof GameObjectFigure)
		{
			GameObjectFigure figure = (GameObjectFigure) gameObject;
			elem.setAttribute(IOString.STANDING, game.getImageKey(figure.getStandingLook()));
		}
		else if (gameObject instanceof GameObjectDice)
		{
			GameObjectDice dice = (GameObjectDice) gameObject;
			for (DiceSide side : dice.dss)
			{
				Element sideElem = new Element(IOString.SIDE);
				sideElem.setAttribute(IOString.VALUE, Integer.toString(side.value));
				sideElem.setText(game.getImageKey(side.img));
				elem.addContent(sideElem);
			}
		}
		return elem;
	}
	
	private static Element createElementFromAffineTransform(AffineTransform at)
	{
		Element elem = new Element(IOString.AFFINE_TRANSFORM);
		elem.setAttribute(IOString.SCALE_X, Double.toString(at.getScaleX()));
		elem.setAttribute(IOString.SCALE_Y, Double.toString(at.getScaleY()));
		elem.setAttribute(IOString.SHEAR_X, Double.toString(at.getShearX()));
		elem.setAttribute(IOString.SHEAR_Y, Double.toString(at.getShearY()));
		elem.setAttribute(IOString.TRANSLATE_X, Double.toString(at.getTranslateX()));
		elem.setAttribute(IOString.TRANSLATE_Y, Double.toString(at.getTranslateY()));
		return elem;
	}
	
	private static Element editAffineTransformFromElement(Element elem, AffineTransform at)
	{
		at.setTransform(
				Double.parseDouble(elem.getAttributeValue(IOString.SCALE_X)), Double.parseDouble(elem.getAttributeValue(IOString.SHEAR_Y)),
				Double.parseDouble(elem.getAttributeValue(IOString.SCALE_Y)), Double.parseDouble(elem.getAttributeValue(IOString.SHEAR_X)),
				Double.parseDouble(elem.getAttributeValue(IOString.TRANSLATE_X)), Double.parseDouble(elem.getAttributeValue(IOString.TRANSLATE_Y)));
		return elem;
	}

	/**
	 * Creates a new Element that represents the @param player.
	 * @param player the Player that shall be encoded
	 * @return the created Element
	 */
	private static Element createElementFromPlayer(Player player) {
		Element elem = new Element(IOString.PLAYER);
		elem.setAttribute(IOString.NAME, player.getName());
		elem.setAttribute(IOString.ID, Integer.toString(player.id));
		elem.setAttribute(IOString.COLOR, Integer.toString(player.color.getRGB()));
		elem.setAttribute(IOString.MOUSE_X, Integer.toString(player.mouseXPos));
		elem.setAttribute(IOString.MOUSE_Y, Integer.toString(player.mouseYPos));
		elem.setAttribute(IOString.SCREEN_W, Integer.toString(player.screenWidth));
		elem.setAttribute(IOString.SCREEN_H, Integer.toString(player.screenHeight));
		elem.addContent(createElementFromAffineTransform(player.screenToBoardTransformation));
		return elem;
	}

	/**
	 * Creates a snapshot of a current GameInstance @param gi incl. the
	 * GameObject gi.game itself. This snapshot can be read by
	 * {@link #readSnapshotFromZip(InputStream)} to resume to
	 * the instance later.
	 * ATTENTION 1: Even though the @param os is an abstract OutputStream, it will
	 * used to create a ZipOutputStream via ZipOutputStream(os).
	 * ATTENTION 2: The following fields are not written into the
	 * ZipOutputStream: actions, changeListener, TYPES, logger
	 * @param gi the GameInstance that shall be encoded
	 * @param os the OutputStream the GameInstance will be written to
	 */
	public static void writeSnapshotToZip(GameInstance gi, OutputStream os) throws IOException
	{
		ZipOutputStream zipOutputStream = null;
		try
		{
			zipOutputStream = new ZipOutputStream(os);
			zipOutputStream.setLevel(9);
			writeGameToZip(gi.game, zipOutputStream);
		    // save game_instance.xml
	    	Document doc_inst = new Document();
	    	Element root_inst = new Element(IOString.XML);

			for (int idx = 0; idx < gi.getObjectNumber(); idx++) {
	        	ObjectInstance ObjectInstance = gi.getObjectInstanceByIndex(idx);
        		root_inst.addContent(createElementFromObjectInstance(ObjectInstance));
        	}
			for (int idx = 0; idx < gi.getPlayerNumber(); idx++) {
				Player player = gi.getPlayerByIndex(idx);
				root_inst.addContent(createElementFromPlayer(player));
			}
	        Element sessionName = new Element(IOString.NAME);
	        sessionName.setText(gi.name);
	        root_inst.addContent(sessionName);

	        Element tableActivated = new Element(IOString.TABLE);
	        tableActivated.setText(Boolean.toString(gi.table));
	        root_inst.addContent(tableActivated);

	        Element privateAreaActivated = new Element(IOString.TABLE);
	        privateAreaActivated.setText(Boolean.toString(gi.private_area));
	        root_inst.addContent(privateAreaActivated);

	        Element tableRadius = new Element(IOString.TABLE_RADIUS);
	        tableRadius.setText(Float.toString(gi.tableRadius));
	        root_inst.addContent(tableRadius);

			Element hidden = new Element(IOString.HIDDEN);
			hidden.setText(String.valueOf(gi.hidden));
			root_inst.addContent(hidden);

			Element password = new Element(IOString.PASSWORD);
			hidden.setText(String.valueOf(gi.password));
			root_inst.addContent(password);
	    	
	        doc_inst.addContent(root_inst);
	    	ZipEntry xmlZipOutput = new ZipEntry(IOString.GAME_INSTANCE_XML);
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
	
	public static final void writeImageToStream(BufferedImage img, String suffix, OutputStream out) throws IOException
	{
		if (getVersion() < 9)
    	{
    		MemoryCacheImageOutputStream tmp = new MemoryCacheImageOutputStream(out);
	    	ImageIO.write(img, suffix, tmp);
    		tmp.close();
    	}
	    else
	    {
	    	ImageIO.write(img, suffix, out);
    	}
    }
	
	public static void writeGameToZip(Game game, ZipOutputStream zipOutputStream) throws IOException
	{	
		// Save all images
	    for (HashMap.Entry<String, BufferedImage> pair : game.images.entrySet()) {
	    	String key = pair.getKey();
		    ZipEntry imageZipOutput = new ZipEntry(key);
		    zipOutputStream.putNextEntry(imageZipOutput);
		    int idx = key.lastIndexOf('.');
		    if (idx != -1)
		    {
		    	String suffix = key.substring(idx + 1);
		    	if (ArrayUtil.firstEqualIndex(ImageIO.getWriterFileSuffixes(), suffix) != -1)
		    	{
		    		writeImageToStream(pair.getValue(), suffix, zipOutputStream);
		    	}
		    }
		    zipOutputStream.closeEntry();
	    }
	    
		Document doc_game = new Document();
    	Element root_game = new Element(IOString.XML);
    	doc_game.addContent(root_game);

        for (int idx = 0; idx < game.objects.size(); idx++) {
        	GameObject entry = game.objects.get(idx);
        	root_game.addContent(createElementFromGameObject(entry, game));
        }
        
        Element elem_back = new Element(IOString.BACKGROUND);
        elem_back.setText(game.getImageKey(game.background));
        root_game.addContent(elem_back);



        ZipEntry gameZipOutput = new ZipEntry(IOString.GAME_XML);
    	zipOutputStream.putNextEntry(gameZipOutput);
    	new XMLOutputter(Format.getPrettyFormat()).output(doc_game, zipOutputStream);
    	zipOutputStream.closeEntry();
	}

	/**
	 * Encodes all fields of @param game to an XML document and puts
	 * it into @param os.
	 * ATTENTION: Even though the @param os is an abstract OutputStream, it will
	 * used to create a ZipOutputStream via ZipOutputStream(os).
	 * @param game the Game that shall be encoded
	 * @param os the OutputStream the Game will be written to
	 */
	public static void writeGameToZip(Game game, OutputStream os) throws IOException
	{	
		ZipOutputStream zipOutputStream = null;
		try
		{
			zipOutputStream = new ZipOutputStream(os);
			writeGameToZip(game, zipOutputStream);
		}
		finally
		{
			if (zipOutputStream != null)
			{
				zipOutputStream.close();
			}
		}
	}

	/**
	 * Encodes all fields of @param object to an XML document and puts
	 * it into @param os.
	 * @param object the ObjectState that shall be encoded
	 * @param output the OutputStream the Game will be written to
	 */
	public static void writeObjectStateToStreamXml(ObjectState object, OutputStream output) throws IOException
	{
		Document doc = new Document();
    	Element elem = new Element(IOString.OBJECT_STATE);
		writeStateToElement(object, elem);
		doc.addContent(elem);
    	new XMLOutputter(Format.getPrettyFormat()).output(doc, output);
	}

	public static void writePlayerToStream(Player player, OutputStream output) throws IOException
	{
		Document doc = new Document();
		Element elem = createElementFromPlayer(player);
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

	/**
	 * Reads a snapshot of a GameInstance encoded into @param in incl. the
	 * GameObject gi.game itself.
	 * ATTENTION 1: Even though the @param in is an abstract InputStream, it will
	 * used to create a ZipInputStream via ZipInputStream(in).
	 * ATTENTION 2: It is expected, that all images used in the game are in the stream.
	 * Possible image formats are .png and .jpg.
	 * Besides, two files IOString.GAME_XML and IOString.GAME_INSTANCE_XML are expected, that encode
	 * the game itself and the specific instance respectively.
	 * @param in the InputStream that encodes the snapshot
	 * @return the GameInstance encodes in @param stream
	 */
	public static GameInstance readSnapshotFromZip(InputStream in, GameInstance gi) throws IOException, JDOMException
	{
		ZipInputStream stream = new ZipInputStream(in);
		GameInstance result = readSnapshotFromZip(stream, gi);
		in.close();
		return result;
	}

	public static long copy(InputStream from, OutputStream to)
		      throws IOException {
		byte[] buf = new byte[0x1000];
	    long total = 0;
	    while (true) {
	      int r = from.read(buf);
	      if (r == -1) {
	        break;
	      }
	      to.write(buf, 0, r);
	      total += r;
	    }
	    return total;
	}	
	
	private static class GameSnapshotreader
	{
		ByteArrayOutputStream gameBuffer = new ByteArrayOutputStream();
		ByteArrayOutputStream gameInstanceBuffer = new ByteArrayOutputStream();
		final GameInstance result;
		
		public GameSnapshotreader(GameInstance result)
		{
			this.result = result;
		}
		
		void put (String name, InputStream content) throws IOException
		{
			if (name.equals(IOString.GAME_XML))
			{
				copy(content, gameBuffer);
			}
			else if (name.equals(IOString.GAME_INSTANCE_XML))
			{
				copy(content, gameInstanceBuffer);
			}
			else
			{
			    int idx = name.lastIndexOf('.');
			    if (idx != -1)
			    {
			    	String suffix = name.substring(idx + 1);
			    	if (ArrayUtil.firstEqualIndex(ImageIO.getReaderFileSuffixes(), suffix) != -1)
			    	{
			    		BufferedImage img = ImageIO.read(content);
						result.game.images.put(name, img);
			    	}
			    }
			}
		}
		
		void run() throws JDOMException, IOException
		{
			Document doc = new SAXBuilder().build(new ByteArrayInputStream(gameBuffer.toByteArray()));
			Element root = doc.getRootElement();

			for (Element elem : root.getChildren())
			{
				final String name = elem.getName();
				if (name.equals(IOString.OBJECT))
				{
					result.game.objects.add(createGameObjectFromElement(elem, result.game.images));
				}
				else if (name.equals(IOString.BACKGROUND))
				{
					result.game.background = result.game.images.get(elem.getValue());
				}
				else if (name.equals(IOString.NAME))
				{
					result.name = elem.getValue();
				}
				else if (name.equals(IOString.SETTINGS))
				{
					if (elem.getAttribute(IOString.NAME) != null)
					{
						result.name = elem.getAttributeValue(IOString.NAME);
					}
					if (elem.getAttribute(IOString.PRIVATE_AREA) != null)
					{
						result.private_area = Boolean.parseBoolean(elem.getAttributeValue(IOString.PRIVATE_AREA));
					}
					if (elem.getAttribute(IOString.TABLE) != null)
					{
						result.table = Boolean.parseBoolean(elem.getAttributeValue(IOString.TABLE));
					}
					if (elem.getAttribute(IOString.TABLE_RADIUS) != null) {
						result.tableRadius = Integer.parseInt(elem.getAttributeValue(IOString.TABLE_RADIUS));
					}
				}
			}
			if (result.name == null)
			{
				logger.warn("Name not set");
				result.name = String.valueOf(Math.random());
			}
			editGameInstanceFromStream(new ByteArrayInputStream(gameInstanceBuffer.toByteArray()), result);
			
		}
	}
	
	public static GameInstance readSnapshotFromFolder(File folder, GameInstance result) throws IOException, JDOMException
	{
		GameSnapshotreader gsr = new GameSnapshotreader(result);
		for (File subfile : folder.listFiles())
		{
			FileInputStream input = new FileInputStream(subfile);
			gsr.put(subfile.getName(), input);
			input.close();
		}
		gsr.run();
		return gsr.result;
	}
	
	/**
	 * Reads a snapshot of a GameInstance encoded into @param stream incl. the
	 * GameObject gi.game itself.
	 * ATTENTION: It is expected, that all images used in the game are in the stream.
	 * Possible image formats are .png and .jpg.
	 * Besides, two files IOString.GAME_XML and IOString.GAME_INSTANCE_XML are expected, that encode
	 * the game itself and the specific instance respectively.
	 * @param stream the ZipInputStream that encodes the snapshot
	 * @return the GameInstance encodes in @param stream
	 */
	public static GameInstance readSnapshotFromZip(ZipInputStream stream, GameInstance gi) throws IOException, JDOMException
	{
		GameSnapshotreader gsr = new GameSnapshotreader(gi);
		try
		{
			ZipEntry entry;
			while((entry = stream.getNextEntry())!=null)
			{
				String name = entry.getName();
				gsr.put(name, stream);
			}
		}
		finally
		{
			stream.close();
		}
		gsr.run();
		return gsr.result;
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

	/**
	 * Edit the ObjectState @param objectState from information encoded in @param input.
	 * ATTENTION 1: @param input is expected to contain an XML document
	 * that can be used to build a Document via the SAXBuilder.
	 * ATTENTION 2: Only the root element will be considered and it has to have the
	 * attributes "x", "y" and "r". All other attributes are optional.
	 * @param objectState the ObjectState to be edited
	 * @param input the InputStream with all update information
	 */
	public static void editObjectStateFromStream(ObjectState objectState, InputStream input) throws IOException, JDOMException
	{
		Document doc = new SAXBuilder().build(input);
    	Element elem = doc.getRootElement();

    	editStateFromElement(objectState, elem);
	}

	/**
	 * Edit the GameInstance @param gi from information encoded in @param input.
	 * ATTENTION 1: @param input is expected to contain an XML document
	 * that can be used to build a Document via the SAXBuilder.
	 * ATTENTION 2: Only the fields players, name and objects get updated.
	 * @param gi the GameInstance to be edited
	 * @param is the InputStream with all update information
	 */
	public static void editGameInstanceFromStream(InputStream is, GameInstance gi) throws JDOMException, IOException
	{
		Document doc = new SAXBuilder().build(is);
    	Element root = doc.getRootElement();

		editGameInstanceFromElement(root, gi);
	}

	/**
	 * Edit the GameInstance @param gi from information encoded in @param input.
	 * ATTENTION 1: @param input is expected to contain an XML document
	 * that can be used to build a Document via the SAXBuilder.
	 * ATTENTION 2: Only the fields players, name and objects get updated.
	 * @param gi the GameInstance to be edited
	 * @param is the InputStream with all update information
	 */
	public static void editGameInstanceFromZip(InputStream is, GameInstance gi) throws JDOMException, IOException
	{
		ZipInputStream stream = new ZipInputStream(is);
		editGameInstanceFromStream(stream, gi);
		stream.close();
	}

	/**
	 * Edit the GameInstance @param gi from information encoded in @param input.
	 * ATTENTION 1: @param input is expected to contain an XML document
	 * that can be used to build a Document via the SAXBuilder.
	 * ATTENTION 2: Only the fields players, name and objects get updated.
	 * @param gi the GameInstance to be edited
	 * @param inputStream the InputStream with all update information
	 * @param source Paul sagt wir brauchen irgendwann die AsynchronousGameConnection^^ Bisher brauchen wir sie nicht.
	 */
	public static void editGameInstanceFromZip(InputStream inputStream, GameInstance gi,
			AsynchronousGameConnection source) throws JDOMException, IOException {
		ZipInputStream stream = new ZipInputStream(inputStream);
		ZipEntry entry;
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		while ((entry = stream.getNextEntry()) != null)
		{
			copy(stream, byteStream);
			if (entry.getName().equals(IOString.GAME_INSTANCE_XML))
			{
				editGameInstanceFromStream(new ByteArrayInputStream(byteStream.toByteArray()), gi);
			}
			byteStream.reset();
		}
		stream.close();
	}
	
	/**
	 * Edit the GameInstance @param gi from information encoded in @param input.
	 * ATTENTION 1: @param input is expected to contain an XML document
	 * that can be used to build a Document via the SAXBuilder.
	 * ATTENTION 2: Only the fields players, name and objects get updated.
	 * @param gi the GameInstance to be edited
	 * @param inputStream the InputStream with all update information
	 * @param source Paul sagt wir brauchen irgendwann die AsynchronousGameConnection^^ Bisher brauchen wir sie nicht.
	 */
	public static void editGameInstanceFromStream(InputStream inputStream, GameInstance gi,
			AsynchronousGameConnection source) throws JDOMException, IOException {
		editGameInstanceFromStream(inputStream, gi);
	}

	public static void editPlayerFromStreamZip(InputStream is, Player gi) throws JDOMException, IOException
	{
		Document doc = new SAXBuilder().build(is);
    	Element root = doc.getRootElement();
		editPlayerFromElement(root, gi);
	}

	public static void writeObjectToStreamObject(ObjectOutputStream objOut, GameObject editedObject) throws IOException {
		objOut.writeInt(editedObject.widthInMM);
		objOut.writeInt(editedObject.heightInMM);
		objOut.writeInt(editedObject.rotationStep);
		objOut.writeInt(editedObject.value);
		objOut.writeObject(editedObject.objectType);
		objOut.writeObject(editedObject.isFixed);
		if (editedObject instanceof GameObjectToken)
		{
			GameObjectToken token = (GameObjectToken)editedObject;
			objOut.writeObject(token.getDownsideLookId());
			objOut.writeObject(token.getUpsideLookId());
		}
	}
	
	public static void editGameObjectFromStreamObject(ObjectInputStream objIn, GameObject object) throws IOException, ClassNotFoundException {
		object.widthInMM = objIn.readInt();
		object.heightInMM = objIn.readInt();
		object.rotationStep = objIn.readInt();
		object.value = objIn.readInt();
		object.objectType = (String)objIn.readObject();
		object.isFixed = objIn.readInt();
		if (object instanceof GameObjectToken)
		{
			GameObjectToken token = (GameObjectToken)object;
			token.setDownsideLook((String)objIn.readObject());
			token.setUpsideLook((String)objIn.readObject());
		}
	}
	
	public static void editPlayerFromStreamObject(ObjectInputStream is, Player player) throws ClassNotFoundException, IOException
	{
		if (player == null)
		{
			is.readObject();
			StreamUtil.skip(is, 68);
			throw new NullPointerException();
		}
		player.setName((String)is.readObject());
		//read player color
		player.color = new Color(is.readInt());
		//read player mouse position
		player.mouseXPos = is.readInt();
		player.mouseYPos = is.readInt();
		//read player window position
		player.screenToBoardTransformation.setTransform(is.readDouble(), is.readDouble(),is.readDouble(), is.readDouble(),is.readDouble(), is.readDouble());
		player.screenWidth = is.readInt();
		player.screenHeight = is.readInt();
	}
	
	public static void writePlayerToStreamObject(ObjectOutputStream out, Player player) throws IOException
	{
		out.writeObject(player.getName());
		//write player color
		out.writeInt(player.color.getRGB());
		//write player mouse position
		out.writeInt(player.mouseXPos);
		out.writeInt(player.mouseYPos);
		//write player window position
		out.writeDouble(player.screenToBoardTransformation.getScaleX());
		out.writeDouble(player.screenToBoardTransformation.getShearY());
		out.writeDouble(player.screenToBoardTransformation.getShearX());
		out.writeDouble(player.screenToBoardTransformation.getScaleY());
		out.writeDouble(player.screenToBoardTransformation.getTranslateX());
		out.writeDouble(player.screenToBoardTransformation.getTranslateY());
		out.writeInt(player.screenWidth);
		out.writeInt(player.screenHeight);
	}
	
	public static void simulateStateFromStreamObject(ObjectInputStream is, ObjectState state) throws IOException
	{
		StreamUtil.skip(is, 41);
		if (state instanceof TokenState)
		{
			StreamUtil.skip(is, 1);
		}
		else if (state instanceof DiceState)
		{
			StreamUtil.skip(is, 4);
		}
		else if (state instanceof FigureState)
		{
			StreamUtil.skip(is, 1);
		}
	}
	
	public static void editStateFromStreamObject(ObjectInputStream is, ObjectState state) throws IOException
	{
		state.aboveInstanceId = is.readInt();
		state.belowInstanceId = is.readInt();
		state.inPrivateArea = is.readBoolean();
		state.owner_id = is.readInt();
		state.drawValue = is.readLong();
		state.posX = is.readInt();
		state.posY = is.readInt();
		state.rotation = is.readInt();
		state.value = is.readInt();
		state.rotationStep = is.readInt();
		state.isFixed = is.readBoolean();
		if (state instanceof TokenState)
		{
			((TokenState)state).side = is.readBoolean();
		}
		else if (state instanceof DiceState)
		{
			((DiceState)state).side = is.readInt();			
		}
		else if (state instanceof FigureState)
		{
			((FigureState) state).standing = is.readBoolean();
		}
	}
	
	public static void writeGameObjectInstanceEditActionToStreamObject(ObjectOutputStream out, GameObjectInstanceEditAction action) throws IOException
	{
		out.writeInt(action.source);
		out.writeInt(action.player);
		out.writeInt(action.object);
	}
	
	public static void writeStateToStreamObject(ObjectOutputStream out, ObjectState state) throws IOException
	{
		out.writeInt(state.aboveInstanceId);
		out.writeInt(state.belowInstanceId);
		out.writeBoolean(state.inPrivateArea);
		out.writeInt(state.owner_id);
		out.writeLong(state.drawValue);
		out.writeInt(state.posX);
		out.writeInt(state.posY);
		out.writeInt(state.rotation);
		out.writeInt(state.value);
		out.writeInt(state.rotationStep);
		out.writeBoolean(state.isFixed);
		if (state instanceof TokenState)
		{
			out.writeBoolean(((TokenState)state).side);
		}
		else if (state instanceof DiceState)
		{
			out.writeInt(((DiceState)state).side);			
		}
		else if (state instanceof FigureState)
		{
			out.writeBoolean(((FigureState) state).standing);
		}
	}

	public static Player readPlayerFromStream(InputStream is) throws JDOMException, IOException
	{
		Document doc = new SAXBuilder().build(is);
    	Element root = doc.getRootElement();

		return createPlayerFromElement(root);
	}


	public static void editPlayerFromZip(InputStream inputStream, Player player) throws IOException, JDOMException {
		ZipInputStream stream = new ZipInputStream(inputStream);
		ZipEntry entry;
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		while ((entry = stream.getNextEntry()) != null)
		{
			copy(stream, byteStream);
			if (entry.getName().equals(IOString.PLAYER_XML))
			{
				editPlayerFromStreamZip(new ByteArrayInputStream(byteStream.toByteArray()), player);
			}
			byteStream.reset();
		}
		stream.close();
	}
	
	public static Player readPlayerFromZip(InputStream inputStream) throws IOException, JDOMException {
		ZipInputStream stream = new ZipInputStream(inputStream);
		ZipEntry entry;
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		Player pl = null;
		while ((entry = stream.getNextEntry()) != null)
		{
			copy(stream, byteStream);
			if (entry.getName().equals(IOString.PLAYER_XML))
			{
				pl = readPlayerFromStream(new ByteArrayInputStream(byteStream.toByteArray()));
			}
			byteStream.reset();
		}
		stream.close();
		return pl;
	}

	
}
