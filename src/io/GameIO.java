package io;

import static java.lang.Integer.max;

import java.awt.Color;
import java.awt.geom.AffineTransform;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.Texture;
import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.definition.GameObject;
import gameObjects.definition.GameObjectBook;
import gameObjects.definition.GameObjectBook.BookSide;
import gameObjects.definition.GameObjectBox;
import gameObjects.definition.GameObjectDice;
import gameObjects.definition.GameObjectDice.DiceSide;
import gameObjects.definition.GameObjectFigure;
import gameObjects.definition.GameObjectToken;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import main.Player;
import util.ArrayUtil;
import util.StringUtils;
import util.data.IntegerArrayList;
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

	public static final int readAttribute(Element elem, String key, int def)
	{
		Attribute attribute = elem.getAttribute(key);
		return attribute != null ? Integer.parseInt(attribute.getValue()) : def;
	}

	public static final long readAttribute(Element elem, String key, long def)
	{
		Attribute attribute = elem.getAttribute(key);
		return attribute != null ? Long.parseLong(attribute.getValue()) : def;
	}

	public static final boolean readAttribute(Element elem, String key, boolean def)
	{
		Attribute attribute = elem.getAttribute(key);
		return attribute != null ? Boolean.parseBoolean(attribute.getValue()) : def;
	}

	public static final String readAttribute(Element elem, String key, String def) {
		Attribute attribute = elem.getAttribute(key);
		return attribute != null ? attribute.getValue() : def;
	}

	public static void readAttribute(Element elem, String key, IntegerArrayList def) {
		Attribute attribute = elem.getAttribute(key);
		if (attribute != null) {
			IntegerArrayList ial = new IntegerArrayList(attribute.getValue());
			def.clear();
			for (int id : ial){
				def.add(id);
			}
		}
	}



	/**
	 * Edit a GameInstance from an XML Element.
	 * ATTENTION: The following fields are not edited: game, actions, changeListener, TYPES, logger
	 * @param root the root Element with all Elements that need updating as children
	 * @param gi the GameInstance that shall the updated
	 */
	private static void editGameInstanceFromElement(Element root, GameInstance gi) throws JDOMException, IOException {
        //Control the xml version, downward compatible current version 2.0
		//String xmlVersion = root.getAttributeValue(StringIO.VERSION);
		int uniqueId = 0;
		for (Element elem : root.getChildren()) {
			String name = elem.getName();
			switch (name) {
				case StringIO.PLAYER:
					Player player = PlayerIO.createPlayerFromElement(elem);
					gi.addPlayer(null, player);
					break;
				case StringIO.NAME:
					gi.name = elem.getValue();
					break;
				case StringIO.SETTINGS:
					for (Element elemSettings : elem.getChildren()) {
						String settingsName = elemSettings.getName();
						switch (settingsName) {
							case StringIO.NAME:
								gi.name = elemSettings.getValue();
								break;
							case StringIO.PRIVATE_AREA:
								gi.private_area = Boolean.parseBoolean(elemSettings.getValue());
								break;
							case StringIO.TABLE:
								gi.drawTable = Boolean.parseBoolean(elemSettings.getValue());
								gi.put_down_area = readAttribute(elemSettings, StringIO.PUT_DOWN_AREA, gi.put_down_area);
								gi.tableRadius = readAttribute(elemSettings, StringIO.TABLE_RADIUS, gi.tableRadius);
								gi.tableColor = readAttribute(elemSettings, StringIO.COLOR, gi.tableColor);
								break;
							case StringIO.SEATS:
								gi.seatColors.clear();
								gi.seats = -1;
								for (Element seatElement : elemSettings.getChildren()) {
									String seatElementName = seatElement.getName();
									if (seatElementName.equals(StringIO.SEAT)) {
										++gi.seats;
										if (seatElement.getAttribute(StringIO.COLOR) != null) {
											gi.seatColors.add(Color.decode(seatElement.getAttributeValue(StringIO.COLOR)));
										}
									}
								}
								gi.seats = max(-1, gi.seats + 1);
								break;
							case StringIO.ADMIN:
								if (!elemSettings.getValue().equals("") && Integer.parseInt(elemSettings.getValue()) > -1) {
									gi.admin = Integer.parseInt(elemSettings.getValue());
								}
								else {
									gi.admin = -1;
								}
								break;
							case StringIO.DEBUG_MODE:
								gi.debug_mode = Boolean.parseBoolean(elemSettings.getValue());
								break;
							case StringIO.INITIAL_MODE:
								gi.initial_mode = Boolean.parseBoolean(elemSettings.getValue());
						}
					}
					break;
				case StringIO.OBJECT:
					String uniqueName = elem.getAttributeValue(StringIO.UNIQUE_NAME);
					if (uniqueName == null) {
						throw new IOException("Object must have a unique name");
					}
					int objectCount = readAttribute(elem, StringIO.NUMBER, 1);
					for (int i = 0; i < objectCount; ++i) {
						if (gi.game.getObject(uniqueName) == null) {
							throw new IOException("Unique name" + uniqueName + " not defined");
						}
						ObjectInstance oi = new ObjectInstance(gi.game.getObject(uniqueName), readAttribute(elem, StringIO.ID, uniqueId));
                        if (oi.state.isFixed) {
                            oi.state.drawValue = 0;
                        } else {
                            oi.state.drawValue = max(max(oi.state.drawValue, uniqueId), 1);
                        }
                        ObjectStateIO.editStateFromElement(oi.state, elem);
                        oi.state.originalX = readAttribute(elem, StringIO.ORIGINAL_X, oi.state.posX);
                        oi.state.originalY = readAttribute(elem, StringIO.ORIGINAL_Y, oi.state.posY);
                        oi.state.originalRotation = readAttribute(elem, StringIO.ORIGINAL_ROTATION, oi.state.rotation);
						gi.addObjectInstance(oi);
						++uniqueId;
					}
					break;
				case StringIO.PASSWORD:
					gi.password = elem.getValue();
					break;
				case StringIO.HIDDEN:
					gi.hidden = Boolean.parseBoolean(elem.getValue());
					break;
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
	public static GameObject createGameObjectFromElement(Element elem, HashMap<String, Texture> images)
	{
		String objectName = elem.getAttributeValue(StringIO.UNIQUE_NAME);
		String type = elem.getAttributeValue(StringIO.TYPE);
		GameObject result = null;
		int width = readAttribute(elem, StringIO.WIDTH, 66);
		int height = readAttribute(elem, StringIO.HEIGHT, 88);
		int value = readAttribute(elem, StringIO.VALUE, 0);
		int sortValue = readAttribute(elem, StringIO.SORT_VALUE, 0);
		int rotationStep = readAttribute(elem, StringIO.ROTATION_STEP, 90);
		int boxId = readAttribute(elem, StringIO.BOX_ID, -1);
		boolean inBox = readAttribute(elem, StringIO.IN_BOX, false);
		int isFixed = readAttribute(elem, StringIO.IS_FIXED, 0);
		switch(type)
		{
			case StringIO.CARD:
			{
				result = new GameObjectToken(objectName, type, width, height, images.get(elem.getAttributeValue(StringIO.FRONT)), images.get(elem.getAttributeValue(StringIO.BACK)), value, sortValue, rotationStep, isFixed, inBox, boxId);
				break;
			}
			case StringIO.FIGURE:
			{
				result = new GameObjectFigure(objectName, type, width, height, images.get(elem.getAttributeValue(StringIO.STANDING)), value, sortValue, rotationStep, isFixed, inBox, boxId);
				break;
			}
			case StringIO.DICE:
			{
				ArrayList<DiceSide> dss = new ArrayList<>();
				for (Element side : elem.getChildren())
				{
					if (side.getName().equals(StringIO.SIDE))
					{
						Texture img = images.get(side.getValue());
						if (img == null){logger.warn("Image not found: ", side.getValue());}
						dss.add(new DiceSide(Integer.parseInt(side.getAttributeValue(StringIO.VALUE)), img, side.getValue()));
					}
				}
				result = new GameObjectDice(objectName, type, width, height, dss.toArray(new DiceSide[dss.size()]), value, sortValue, rotationStep, inBox, boxId);
				break;
			}
			case StringIO.BOOK:
			{
				ArrayList<BookSide> bss = new ArrayList<>();
				for (Element side : elem.getChildren())
				{
					if (side.getName().equals(StringIO.SIDE))
					{
						Texture img = images.get(side.getValue());
						if (img == null){logger.warn("Image not found: ", side.getValue());}
						bss.add(new BookSide(Integer.parseInt(side.getAttributeValue(StringIO.VALUE)), img, side.getValue()));
					}
				}
				result = new GameObjectBook(objectName, type, width, height, bss.toArray(new BookSide[bss.size()]), value, sortValue, rotationStep, inBox, boxId);
				break;
			}
			case StringIO.BOX:
			{
				result = new GameObjectBox(objectName, type, width, height, images.get(elem.getAttributeValue(StringIO.FRONT)), images.get(elem.getAttributeValue(StringIO.BACK)), rotationStep, inBox, boxId);
				break;
			}
		}
		ArrayList<String> groups = new ArrayList<String>();
		for (Element child : elem.getChildren())
		{
			if (child.getName().equals(StringIO.GROUP))
			{
				groups.add(child.getText());
			}
		}
		result.groups = groups.toArray(new String[groups.size()]);
		return result;
	}

	/**
	 * Creates a new Element that represents the @param objectInstance.
	 * ATTENTION: Does not save the fields GameObject go, double scale and Player inHand.
	 * @param objectInstance the ObjectInstance that shall be encoded
	 * @return the created Element
	 */
	private static Element createElementFromObjectInstance(ObjectInstance objectInstance)
	{
		Element elem = new Element(StringIO.OBJECT);
		elem.setAttribute(StringIO.UNIQUE_NAME, objectInstance.go.uniqueObjectName);
		elem.setAttribute(StringIO.ID, Integer.toString(objectInstance.id));
		ObjectStateIO.writeStateToElement(objectInstance.state, elem);
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
	public static Element createElementFromGameObject(GameObject gameObject, Game game)
	{
		Element elem = new Element(StringIO.OBJECT);
		elem.setAttribute(StringIO.TYPE, gameObject.objectType);
		elem.setAttribute(StringIO.UNIQUE_NAME, gameObject.uniqueObjectName);
		elem.setAttribute(StringIO.WIDTH, Integer.toString(gameObject.widthInMM));
		elem.setAttribute(StringIO.HEIGHT, Integer.toString(gameObject.heightInMM));
		elem.setAttribute(StringIO.VALUE, Integer.toString(gameObject.value));
		elem.setAttribute(StringIO.SORT_VALUE, Integer.toString(gameObject.sortValue));
		elem.setAttribute(StringIO.ROTATION_STEP, Integer.toString(gameObject.rotationStep));
		elem.setAttribute(StringIO.BOX_ID, Integer.toString(gameObject.boxId));
		elem.setAttribute(StringIO.IN_BOX, Boolean.toString(gameObject.inBox));
		elem.setAttribute(StringIO.IS_FIXED, Integer.toString(gameObject.isFixed));
		for (String group : gameObject.groups)
		{
			elem.addContent(new Element(StringIO.GROUP).setText(group));
		}
		if (gameObject instanceof GameObjectToken)
		{
			GameObjectToken token = (GameObjectToken) gameObject;
			elem.setAttribute(StringIO.FRONT, game.getImageKey(token.getUpsideLook()));
			Texture downsideLook = token.getDownsideLook();
			if (downsideLook != null) {elem.setAttribute(StringIO.BACK, game.getImageKey(downsideLook));}
		}
		else if (gameObject instanceof GameObjectFigure)
		{
			GameObjectFigure figure = (GameObjectFigure) gameObject;
			elem.setAttribute(StringIO.STANDING, game.getImageKey(figure.getStandingLook()));
		}
		else if (gameObject instanceof GameObjectDice)
		{
			GameObjectDice dice = (GameObjectDice) gameObject;
			for (DiceSide side : dice.dss)
			{
				Element sideElem = new Element(StringIO.SIDE);
				sideElem.setAttribute(StringIO.VALUE, Integer.toString(side.value));
				sideElem.setText(game.getImageKey(side.img));
				elem.addContent(sideElem);
			}
		}
		else if (gameObject instanceof  GameObjectBook){
			GameObjectBook book = (GameObjectBook) gameObject;
			for (BookSide side : book.bss)
			{
				Element sideElem = new Element(StringIO.SIDE);
				sideElem.setAttribute(StringIO.VALUE, Integer.toString(side.value));
				sideElem.setText(game.getImageKey(side.img));
				elem.addContent(sideElem);
			}
		}
		else if (gameObject instanceof GameObjectBox){
			GameObjectBox box = (GameObjectBox) gameObject;
			elem.setAttribute(StringIO.FRONT, game.getImageKey(box.getUpsideLook()));
			if (box.getDownsideLook() != null)
			{
				elem.setAttribute(StringIO.BACK, game.getImageKey(box.getDownsideLook()));
			}
		}
		return elem;
	}
	
	static Element createElementFromAffineTransform(AffineTransform at)
	{
		Element elem = new Element(StringIO.AFFINE_TRANSFORM);
		elem.setAttribute(StringIO.SCALE_X, Double.toString(at.getScaleX()));
		elem.setAttribute(StringIO.SCALE_Y, Double.toString(at.getScaleY()));
		elem.setAttribute(StringIO.SHEAR_X, Double.toString(at.getShearX()));
		elem.setAttribute(StringIO.SHEAR_Y, Double.toString(at.getShearY()));
		elem.setAttribute(StringIO.TRANSLATE_X, Double.toString(at.getTranslateX()));
		elem.setAttribute(StringIO.TRANSLATE_Y, Double.toString(at.getTranslateY()));
		return elem;
	}
	
	static Element editAffineTransformFromElement(Element elem, AffineTransform at)
	{
		at.setTransform(
				Double.parseDouble(elem.getAttributeValue(StringIO.SCALE_X)), Double.parseDouble(elem.getAttributeValue(StringIO.SHEAR_Y)),
				Double.parseDouble(elem.getAttributeValue(StringIO.SHEAR_X)), Double.parseDouble(elem.getAttributeValue(StringIO.SCALE_Y)),
				Double.parseDouble(elem.getAttributeValue(StringIO.TRANSLATE_X)), Double.parseDouble(elem.getAttributeValue(StringIO.TRANSLATE_Y)));
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
	    	Element root_inst = new Element(StringIO.XML);

			for (int idx = 0; idx < gi.getObjectInstanceCount(); idx++) {
	        	ObjectInstance ObjectInstance = gi.getObjectInstanceByIndex(idx);
        		root_inst.addContent(createElementFromObjectInstance(ObjectInstance));
        	}
			for (int idx = 0; idx < gi.getPlayerCount(); idx++) {
				Player player = gi.getPlayerByIndex(idx);
				root_inst.addContent(PlayerIO.createElementFromPlayer(player));
			}

	        Element sessionName = new Element(StringIO.NAME);
	        sessionName.setText(gi.name);
	        root_inst.addContent(sessionName);

			Element settings = new Element(StringIO.SETTINGS);

			Element settingsName = new Element(StringIO.NAME);
			settingsName.setText(gi.name);
			Element table = new Element(StringIO.TABLE);
			table.setText(Boolean.toString(gi.drawTable));
			table.setAttribute(StringIO.PUT_DOWN_AREA, Boolean.toString(gi.put_down_area));
			table.setAttribute(StringIO.TABLE_RADIUS, Integer.toString(gi.tableRadius));
			table.setAttribute(StringIO.COLOR, gi.tableColor);

			Element privateArea = new Element(StringIO.PRIVATE_AREA);
			privateArea.setText(Boolean.toString(gi.private_area));

			Element admin = new Element(StringIO.ADMIN);
			admin.setText(Integer.toString(gi.admin));

			Element debugMode = new Element(StringIO.DEBUG_MODE);
			debugMode.setText(Boolean.toString(gi.debug_mode));

			Element initialMode = new Element(StringIO.INITIAL_MODE);
			initialMode.setText(Boolean.toString(gi.initial_mode));

			Element seats = new Element(StringIO.SEATS);
			for (int i=0; i<gi.seats;++i)
			{
				Element seat = new Element(StringIO.SEAT);
				if (gi.seatColors.size() > i)
				{
				    String buf = Integer.toHexString(gi.seatColors.get(i % 10).getRGB());
				    String hex = "#"+buf.substring(buf.length()-6);
					seat.setAttribute(StringIO.COLOR, hex);
				}
				seats.addContent(seat);
			}

			settings.addContent(settingsName);
			settings.addContent(table);
			settings.addContent(privateArea);
			if (gi.seats > 0) {
				settings.addContent(seats);
			}
			settings.addContent(admin);
			settings.addContent(debugMode);
			settings.addContent(initialMode);
			settings.setAttribute(StringIO.HIDDEN,Boolean.toString(gi.hidden));
			root_inst.addContent(settings);

			Element password = new Element(StringIO.PASSWORD);
			if (gi.password != null)
			{
				password.setText(String.valueOf(gi.password));
			}
			root_inst.addContent(password);
	    	
	        doc_inst.addContent(root_inst);
	    	ZipEntry xmlZipOutput = new ZipEntry(StringIO.GAME_INSTANCE_XML);
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
	
	public static final void writeImageToStream(Texture img, String suffix, OutputStream out) throws IOException
	{
	    if (suffix.equals(img.suffix))
	    {
	        img.writeTo(out);
	    }
	    else
	    {
    		if (getVersion() < 9)
        	{
        		MemoryCacheImageOutputStream tmp = new MemoryCacheImageOutputStream(out);
    	    	ImageIO.write(img.getImage(), suffix, tmp);
        		tmp.close();
        	}
    	    else
    	    {
    	    	ImageIO.write(img.getImage(), suffix, out);
        	}
	    }
    }
	
	public static void writeGameToZip(Game game, ZipOutputStream zipOutputStream) throws IOException
	{	
		// Save all images
	    for (HashMap.Entry<String, Texture> pair : game.images.entrySet()) {
	    	String key = pair.getKey();
		    ZipEntry imageZipOutput = new ZipEntry(key);
		    zipOutputStream.putNextEntry(imageZipOutput);
		    String filetype = StringUtils.getFileType(key);
		    if (filetype != null && ArrayUtil.firstEqualIndex(ImageIO.getWriterFileSuffixes(), filetype) != -1)
		    {
	    		writeImageToStream(pair.getValue(), filetype, zipOutputStream);
		    }
		    zipOutputStream.closeEntry();
	    }
	    
		Document doc_game = new Document();
    	Element root_game = new Element(StringIO.XML);
    	doc_game.addContent(root_game);

        for (int idx = 0; idx < game.getGameObjectCount(); idx++) {
        	GameObject entry = game.getGameObjectByIndex(idx);
        	root_game.addContent(createElementFromGameObject(entry, game));
        }
        
        Element elem_back = new Element(StringIO.BACKGROUND);
        elem_back.setText(game.getImageKey(game.background));
        root_game.addContent(elem_back);

        ZipEntry gameZipOutput = new ZipEntry(StringIO.GAME_XML);
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
	 * Besides, two files StringIO.GAME_XML and StringIO.GAME_INSTANCE_XML are expected, that encode
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



	private static class GameSnapshotreader
	{
		private final ByteArrayOutputStream gameBuffer = new ByteArrayOutputStream();
		private final ByteArrayOutputStream gameInstanceBuffer = new ByteArrayOutputStream();
		private final GameInstance result;
		private final HashMap<String, Texture> images = new HashMap<>();
		
		public GameSnapshotreader(GameInstance result)
		{
			this.result = result;
		}

		void put (String name, InputStream content) throws IOException
		{
			if (name.equals(StringIO.GAME_XML))
			{
				StreamUtil.copy(content, gameBuffer);
			}
			else if (name.equals(StringIO.GAME_INSTANCE_XML))
			{
			    StreamUtil.copy(content, gameInstanceBuffer);
			}
			else
			{
			    String filetype = StringUtils.getFileType(name);
			    if (filetype != null && ArrayUtil.firstEqualIndex(ImageIO.getReaderFileSuffixes(), filetype) != -1)
			    {
		    		Texture img = new Texture(content, filetype);
					images.put(name, img);
			    }
			}
		}
		
		private static <K, V> Map<K, V>copyMap(Map<K, V> original, Map<K, V> second_Map)
	    {
	        for (Map.Entry<K, V> entry : original.entrySet()) {
	            second_Map.put(entry.getKey(),entry.getValue());
	        }
	        return second_Map;
	    }
		
		void run() throws JDOMException, IOException
		{
			Document doc = new SAXBuilder().build(new ByteArrayInputStream(gameBuffer.toByteArray()));
			Element root = doc.getRootElement();

			//Control the xml version, downward compatible current version 2.0
			result.game.images.clear();
			copyMap(images, result.game.images);
			for (Element elem : root.getChildren()) {
				final String name = elem.getName();
				if (name.equals(StringIO.OBJECT)) {
					if (elem.getAttribute(StringIO.NUMBER) != null)
					{
					    int count = Integer.parseInt(elem.getAttributeValue(StringIO.NUMBER));
						for (int i=0; i<count;++i) {
							result.game.addObject(createGameObjectFromElement(elem, result.game.images));
						}
					}
					else {
						result.game.addObject(createGameObjectFromElement(elem, result.game.images));
					}
				} else if (name.equals(StringIO.BACKGROUND)) {
					result.game.background = result.game.images.get(elem.getValue());
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
	 * Besides, two files StringIO.GAME_XML and StringIO.GAME_INSTANCE_XML are expected, that encode
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
     * @param inputStream the InputStream with all update information
     * @param source Paul sagt wir brauchen irgendwann die AsynchronousGameConnection^^ Bisher brauchen wir sie nicht.
     */
    public static void editGameInstanceFromZip(InputStream inputStream, GameInstance gi) throws JDOMException, IOException {
        ZipInputStream stream = new ZipInputStream(inputStream);
        ZipEntry entry;
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        while ((entry = stream.getNextEntry()) != null)
        {
            StreamUtil.copy(stream, byteStream);
            if (entry.getName().equals(StringIO.GAME_INSTANCE_XML))
            {
                editGameInstanceFromStream(new ByteArrayInputStream(byteStream.toByteArray()), gi);
            }
            byteStream.reset();
        }
        stream.close();
    }

	public static void writeObjectToStreamObject(ObjectOutputStream objOut, GameObject editedObject) throws IOException {
		objOut.writeInt(editedObject.widthInMM);
		objOut.writeInt(editedObject.heightInMM);
		objOut.writeInt(editedObject.rotationStep);
		objOut.writeInt(editedObject.value);
		objOut.writeInt(editedObject.sortValue);
		objOut.writeObject(editedObject.objectType);
		objOut.writeInt(editedObject.boxId);
		objOut.writeBoolean(editedObject.inBox);
		objOut.writeInt(editedObject.isFixed);
		if (editedObject instanceof GameObjectToken)
		{
			GameObjectToken token = (GameObjectToken)editedObject;
			objOut.writeObject(token.getDownsideLookId());
			objOut.writeObject(token.getUpsideLookId());
		}
		else if (editedObject instanceof GameObjectBox)
		{
			GameObjectBox box = (GameObjectBox) editedObject;
			objOut.writeObject(box.getDownsideLookId());
			objOut.writeObject(box.getUpsideLookId());
		}
	}
	
	public static void editGameObjectFromStreamObject(ObjectInputStream objIn, GameObject object) throws IOException, ClassNotFoundException {
		object.widthInMM = objIn.readInt();
		object.heightInMM = objIn.readInt();
		object.rotationStep = objIn.readInt();
		object.value = objIn.readInt();
		object.sortValue = objIn.readInt();
		object.objectType = (String)objIn.readObject();
		object.boxId = objIn.readInt();
		object.inBox = objIn.readBoolean();
		object.isFixed = objIn.readInt();
		if (object instanceof GameObjectToken)
		{
			GameObjectToken token = (GameObjectToken)object;
			token.setDownsideLook((String)objIn.readObject());
			token.setUpsideLook((String)objIn.readObject());
		}
		else if (object instanceof GameObjectBox)
		{
			GameObjectBox box = (GameObjectBox)object;
			box.setDownsideLook((String)objIn.readObject());
			box.setUpsideLook((String)objIn.readObject());
		}	}
	
	public static void writeGameObjectInstanceEditActionToStreamObject(ObjectOutputStream out, GameObjectInstanceEditAction action) throws IOException
	{
		out.writeInt(action.source);
		out.writeInt(action.player);
		out.writeInt(action.object);
	}
}