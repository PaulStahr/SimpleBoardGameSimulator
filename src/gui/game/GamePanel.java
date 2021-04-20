package gui.game;

import static gameObjects.functions.DrawFunctions.drawBackground;
import static gameObjects.functions.DrawFunctions.drawObjectsFromList;
import static gameObjects.functions.DrawFunctions.drawPlayerPositions;
import static gameObjects.functions.DrawFunctions.drawPrivateArea;
import static gameObjects.functions.DrawFunctions.drawSelection;
import static gameObjects.functions.DrawFunctions.drawTokensInPrivateArea;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.ControlCombination;
import data.DataHandler;
import data.Options;
import data.SystemFileUtil;
import gameObjects.action.GameAction;
import gameObjects.action.GameObjectEditAction;
import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.action.player.PlayerAddAction;
import gameObjects.action.player.PlayerCharacterPositionUpdate;
import gameObjects.action.player.PlayerEditAction;
import gameObjects.action.player.PlayerMousePositionUpdate;
import gameObjects.action.player.PlayerRemoveAction;
import gameObjects.action.structure.GameStructureEditAction;
import gameObjects.definition.GameObjectBook;
import gameObjects.definition.GameObjectBox;
import gameObjects.definition.GameObjectDice;
import gameObjects.definition.GameObjectFigure;
import gameObjects.definition.GameObjectToken;
import gameObjects.functions.CheckFunctions;
import gameObjects.functions.DrawFunctions;
import gameObjects.functions.MoveFunctions;
import gameObjects.functions.ObjectFunctions;
import gameObjects.functions.PlayerFunctions;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.ObjectState;
import geometry.Matrix3d;
import geometry.TransformConversion;
import geometry.Vector2d;
import gui.language.Language;
import gui.language.LanguageChangeListener;
import gui.language.LanguageHandler;
import gui.language.Words;
import io.GameIO;
import main.Player;
import util.StringUtils;
import util.TimedUpdateHandler;
import util.data.DoubleArrayList;
import util.data.IntegerArrayList;
import util.io.StreamUtil;


public class GamePanel extends JPanel implements MouseListener, MouseMotionListener, GameInstance.GameChangeListener, KeyListener, KeyEventDispatcher, MouseWheelListener, ActionListener, ComponentListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3579141032474558913L;
	private static final Logger logger = LoggerFactory.getLogger(GamePanel.class);
	
	static boolean invert_rotation = false;
	static boolean show_ping = false;
	public static boolean isDebug = false;
	static {
		Options.addModificationListener(new Runnable() {

			@Override
			public void run() {
				invert_rotation = Options.getBoolean("invert_rotation", false);
				show_ping = Options.getBoolean("gui.show_ping", false);
				isDebug = Options.getBoolean("debug", false);
			}
		});		
	}
	
	final GameInstance gameInstance;
	public ObjectInstance hoveredObject = null;
	private final IntegerArrayList objOrigPosX = new IntegerArrayList();
	private final IntegerArrayList objOrigPosY = new IntegerArrayList();
	private Player player;
	public final int id = (int)System.nanoTime();
	private final IntegerArrayList ial = new IntegerArrayList();
	private final IntegerArrayList ial2 = new IntegerArrayList();
	private final ArrayList<ObjectInstance> objectInstanceList = new ArrayList<>();

	int maxInaccuracy = 20;

	boolean isLeftMouseKeyHold = false;
	boolean isRightMouseKeyHold = false;
	boolean isMiddleMouseKeyHold = false;

	//Table settings
	boolean isTableVisible = true;
	boolean isPutDownAreaVisible = true;

	int keyPressed = 0;

	//-1 no key, 0 left, 1 middle, 2 right
	int firstMouseClick = -1;

	public int mouseScreenX = -1;
	public int mouseScreenY = -1;
	private final Vector2d mouseBoardPos = new Vector2d();
	private final Vector2d mousePressedGamePos = new Vector2d();
	private final Matrix3d gameTransform = new Matrix3d();


	public int mouseWheelValue = 0;
	int zoomFactor = 0;
	public int translateX = 0;
	public int translateY = 0;
	public double zooming = 1;
	public double rotation = 0;
	public boolean mouseInPrivateArea = false;
	public boolean mouseInStacker = false;
    public boolean boardTranslation = false;


	//private final ControlPanel controlPanel = new ControlPanel();
	private final AffineTransform boardToScreenTransformation = new AffineTransform();
	private final AffineTransform screenToBoardTransformation = new AffineTransform();
	public Color mouseColor = Color.black;
	public Color dragColor = Color.red;
	public Color stackColor = Color.green;
	public String infoText = "";
	private final ArrayList<ObjectInstance> oiList = new ArrayList<>();



	public boolean isSelectStarted = false;

	public final IntegerArrayList scaledObjects = new IntegerArrayList();
	public final DoubleArrayList savedScalingFactors = new DoubleArrayList();
	private double originalScalingFactor = 3;
	private double scalingFactor = originalScalingFactor;
	public int beginSelectPosScreenX = 0;
	public int beginSelectPosScreenY = 0;
	public int selectWidth = 0;
	public int selectHeight = 0;
	private IntegerArrayList selectedObjects = new IntegerArrayList();

	//Show text only if in debug mode
	public String outText = "";

	public PrivateArea privateArea;
	public Table table = null;
	public int activePlayer = -1;


	public BufferedImage[] playerImages = new BufferedImage[10];

	private final Map<AudioClip, Clip> audioClips = new HashMap<>();

	public Set<Integer> downKeys = new HashSet<>();
	
	private final TimedUpdateHandler autosave = new TimedUpdateHandler() {
		@Override
		public void update() {
			if (isVisible())
			{
				try {
					File tmpFile = new File(SystemFileUtil.defaultProgramDirectory() + "/autosave-tmp.zip");
					FileOutputStream fOut = new FileOutputStream(tmpFile);
					GameIO.writeSnapshotToZip(gameInstance, fOut);
					fOut.close();
					Files.move(tmpFile.toPath(), new File(SystemFileUtil.defaultProgramDirectory() + "/autosave.zip").toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					logger.error("Can't create autosave", e);
				}
			}
		}
		
		@Override
		public int getUpdateInterval() {
			return 60000;
		}
	};

	public static enum AudioClip{drop, open, select, click, shuffle};
	
	public GamePanel(GameInstance gameInstance, LanguageHandler lh)
	{
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.gameInstance = gameInstance;
		this.privateArea = new PrivateArea(this, gameInstance, boardToScreenTransformation, screenToBoardTransformation);
		int tableRadius = gameInstance.tableRadius;
		this.table = new Table(this, gameInstance, 2*tableRadius,new Point2D.Double(-tableRadius,-tableRadius));

		if (!this.gameInstance.private_area) {
			this.privateArea.zooming = 0;
		}
		this.isTableVisible = gameInstance.drawTable;
		this.isPutDownAreaVisible = gameInstance.put_down_area;

		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		addMouseWheelListener(this);
		setFocusable(true);
		gameInstance.addChangeListener(this);
	
		// This is the cheat sheet showing the user how he can interact with the game board
		//SheetPanel shP = new SheetPanel(new BorderLayout());
		//lh.addLanguageChangeListener(shP);
		//shP.languageChanged(lh.getCurrentLanguage());
		//add(shP);


		
		updateGameTransform();
		addComponentListener(this);

		for (int i = 0; i < 10; ++i) {
			try {
				playerImages[i] = ImageIO.read(DataHandler.getResourceAsStream("images/kenney-animalpack/PNG/Round/id" + String.valueOf(i) + ".png"));
			} catch (IOException e) {
				logger.error("Can't read PlayerImage", e);
			}
		}
		//Read the audio clips
		for(AudioClip clipString : AudioClip.values()) {
			try {
				ByteArrayInputStream bis = new ByteArrayInputStream(StreamUtil.toByteArray(DataHandler.getResourceAsStream("audio/kenney-audio/" + clipString + ".wav")));
				AudioInputStream stream = AudioSystem.getAudioInputStream(bis);
				AudioFormat format = stream.getFormat();
				DataLine.Info info = new DataLine.Info(Clip.class, format);
				Clip clip = (Clip) AudioSystem.getLine(info);
				clip.open(stream);
				audioClips.put(clipString, clip);
			} catch (UnsupportedAudioFileException e) {
				logger.error("Unsupported Audio File", e);
			} catch (IOException e) {
				logger.error("IO Error loading sounds", e);
			} catch (LineUnavailableException | IllegalArgumentException e) {
				logger.error("No sound availible", e);
			}
		}
		playAudio(AudioClip.open);
		DataHandler.timedUpdater.add(autosave);
	}

    public void playAudio(AudioClip clip) {
        Clip cl = audioClips.get(clip);
        if (cl == null) {
            logger.warn("Couldn't find sound " + clip);
        }else{
            cl.setFramePosition(0);
            cl.start();
        }
    }

    public void setPlayer(Player pl)
    {
    	this.player = pl;
    	try {
    	    privateArea.updatePrivateObjects(gameInstance, player);
    	}catch(Exception e) {
    	    logger.error("Couldn't update private area");
    	}
    }

	/** Drawing of all the game objects, draws the board, object instances and the players
	 * @param g game graphic object
	 */
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		//Draw the board and the table
		if (isTableVisible) {
			drawBackground(this, g, gameInstance);
		}
		Graphics2D g2 = (Graphics2D)g;
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHints(rh);
        g2.setTransform(boardToScreenTransformation);

        //Draw all objects not in some private area
		ObjectFunctions.getDrawOrder(gameInstance, ial);
		oiList.clear();
		gameInstance.getObjects(oiList);
		ArrayList<ObjectInstance> drawableObjects = new ArrayList<>();
		CheckFunctions.drawableObjectsOnTable(oiList, drawableObjects);
		drawableObjects.sort(ObjectFunctions.objectInstanceDrawValueComparator);
		ObjectFunctions.objectListToIntegerArrayList(ial, drawableObjects);
		drawObjectsFromList(this,g,gameInstance,player, ial);

		//Draw selection rectangle
		drawSelection(this, g, player);

		//Draw Private Area
		if (!player.visitor) {
			drawPrivateArea(this, g);
		}


		AffineTransform tmp = g2.getTransform();
		g2.setTransform(boardToScreenTransformation);
		//Redraw selected objects not in some private area
		ial.clear();
		for (int id : selectedObjects){
		    if (!ial.contains(id)) {ial.add(id);}
			ObjectFunctions.getAllAboveLyingObjects(gameInstance, player, gameInstance.getObjectInstanceById(id), ial2);
			for(int id2 : ial2){
				if (!ial.contains(id2)){
					ial.add(id2);
				}
			}
		}
		ObjectFunctions.sortByDrawValue(gameInstance, ial);
		//ArrayUtil.unifySorted(ial);
		drawObjectsFromList(this, g, gameInstance, player, ial, ial2);


		//Draw objects in private area
		if (!player.visitor && privateArea.zooming != 0) {
			drawTokensInPrivateArea(this, g, gameInstance, player, hoveredObject);
		}

		//Draw all player related information
		drawPlayerPositions(this, g, gameInstance, player, infoText);
		g2.setTransform(tmp);

		//Draw debug informations
		if (isDebug) {
			DrawFunctions.drawDebugInfo(this, g2, gameInstance, player);
		}
		if (show_ping) {
            g2.drawString("Last Signal", getWidth() - 100, 80);		    
		    for (int i = 0; i < gameInstance.getPlayerCount(); ++i)
		    {
		        Player pl = gameInstance.getPlayerByIndex(i);
		        g2.drawString(pl.getName() + " " + ((System.nanoTime() - pl.lastReceivedSignal) / 100000000) / 10f, getWidth() - 100, 100 + 20 * i);
		    }
		}
	}

	/** Get mouse position and active objects while moving the mouse around the board
	 * @param arg0 the current mouse event
	 */
	@Override
	public void mouseMoved(MouseEvent arg0) {
		//Get mouse screen positions
		mouseScreenX = arg0.getX();
		mouseScreenY = arg0.getY();
		//Convert screen to board position
		screenToBoardPos(mouseScreenX, mouseScreenY, mouseBoardPos);
		//Check if mouse is in the private area
		mouseInPrivateArea = ObjectFunctions.isInPrivateArea(privateArea, mouseBoardPos.getXI(), mouseBoardPos.getYI());
		mouseInStacker = ObjectFunctions.isOnShape(id, gameInstance, table.stackerShape, boardToScreenTransformation, mouseBoardPos.getXI(), mouseBoardPos.getYI());

		//Check if some key is pressed
		if (player != null) {
			//set the mouse position of the player to send to other players
		    gameInstance.update(new PlayerMousePositionUpdate(id, player, player, mouseBoardPos.getXI(), mouseBoardPos.getYI()));

			if (!player.visitor) {
				//Disable action string if mouse moves and no key is pressed
				if (downKeys.size() == 0) {
					player.actionString = "";
	         	}
				//get nearest object concerning the mouse position
				ObjectInstance nearestObject = ObjectFunctions.getNearestObjectByPosition(this, gameInstance, player, mouseBoardPos.getXI(), mouseBoardPos.getYI(), 1, null);
				//do actions if mouse is in the private area or not and there is some nearest object
				if (mouseInPrivateArea && nearestObject != null && !arg0.isControlDown() && !arg0.isShiftDown() && !arg0.isAltDown()) {
					if (hoveredObject != null)
				    {
				        hoveredObject.state.isActive = false;
				    }
					nearestObject.state.isActive = false;
					setHoveredObject(gameInstance, player, nearestObject);
				} else if (!mouseInPrivateArea) {
					if (nearestObject != null && !nearestObject.state.inPrivateArea) {
						if (hoveredObject == null) {
							setHoveredObject(gameInstance, player, nearestObject);
						} else if (nearestObject.id != hoveredObject.id) {
							setHoveredObject(gameInstance, player, nearestObject);
						}
						if (isDebug && hoveredObject != null) {
							outText = "Hover Object: " + String.valueOf(hoveredObject.id);
							outText += "\n Select: " + String.valueOf(hoveredObject.state.isSelected);
						}
					} else {
						unhoverObject(gameInstance, player);
						if (isDebug) {
							outText = " ";
						}
					}
				}
			}
		}
		//TODO do repaint if something happens and not if mouse moved?
		repaint();
	}


	@Override
	public void mouseClicked(MouseEvent arg0) {
	    playAudio(AudioClip.click);
		if (!player.visitor) {
			if (arg0.getClickCount() == 2) {
				//Play object with double click
				if (hoveredObject != null) {
					if (!ObjectFunctions.IsObjectOnShape(gameInstance, table.stackerShape, getBoardToScreenTransform(), hoveredObject) && hoveredObject.go instanceof GameObjectToken) {
						ObjectFunctions.playObject(this, gameInstance, player, hoveredObject, hoveredObject, selectedObjects);
						playAudio(AudioClip.drop);
					} else if (ObjectFunctions.IsObjectOnShape(gameInstance, table.stackerShape, getBoardToScreenTransform(), hoveredObject) && hoveredObject.go instanceof GameObjectToken) {
						double offset = 0;
						if (table != null){
							offset = table.getTableOffset(player, hoveredObject);
						}
						Point2D tableCenter = new Point2D.Double();
						table.getTableCenter(tableCenter);
						ObjectFunctions.takeTrick(id, gameInstance, player, table.stackerShape, tableCenter, offset, getBoardToScreenTransform(), hoveredObject, ial, hoveredObject, selectedObjects);
					} else if (hoveredObject.go instanceof GameObjectBox){
						if (!arg0.isShiftDown()) {
							ObjectFunctions.unpackBox(id, gameInstance, player, hoveredObject);
						}
						else{
							ObjectFunctions.packBox(id, gameInstance, player, hoveredObject);
						}
					}
				}
				else{
					//Sit down on double click on seat
					for (int i = 0; i < this.table.playerShapes.size(); ++i) {
						if (this.table.playerShapes.get(i).contains(mouseScreenX, mouseScreenY)) {
							sitDown(player, i);
							break;
						}
					}
				}
			}
			if (hoveredObject != null && hoveredObject.go instanceof GameObjectBook){
				if (SwingUtilities.isLeftMouseButton(arg0)){
					ObjectFunctions.nextBookPage(id, gameInstance, player, hoveredObject);
				}
				else if (SwingUtilities.isRightMouseButton(arg0)){
					ObjectFunctions.previousBookPage(id, gameInstance, player, hoveredObject);
				}
			}
		}
		//setClickedMouseKey(arg0);
		/* Right Mouse Click on Object
		if(SwingUtilities.isRightMouseButton(arg0))
		{
			screenToBoardPos(arg0.getX(), arg0.getY(), mousePressedGamePos);
			mouseBoardPos.set(mousePressedGamePos);
			activeObject = ObjectFunctions.setActiveObjectByMouseAndKey(this, gameInstance,player, mouseBoardPos, loggedKeys, maxInaccuracy);
			//Show popup menu of active object
			if (activeObject!=null) {
				activeObject.newObjectActionMenu(gameInstance, player, this).showPopup(arg0);
			}
		}*/
	}

	public void beginPlay(){
		player.beginPlay(this, gameInstance);
		repaint();
	}

	public void sitDown(Player sitDownPlayer, int pos, boolean swap) {
		if (sitDownPlayer != null && sitDownPlayer == player && pos >= 0 && pos < this.table.playerShapes.size()) {
			translateX = 0;
			translateY = 0;
			zooming = getHeight() / ((float) this.table.getDiameter() + 200);
			zoomFactor = (int) (-10 * Math.log(zooming));
			rotation = 0;
			updateGameTransform();
			rotation = Math.toRadians(-360 / Math.max(1, this.table.playerShapes.size()) * pos);
			updateGameTransform();

			int currentPosition = sitDownPlayer.seatNum;
			if (currentPosition != pos && swap) {
			    Player player1 = gameInstance.getPlayer(sitDownPlayer.sameSeatPredicate);
				if (player1 != null) {
					player1.seatNum = currentPosition;

					sitDownPlayer.seatNum = pos;
					sitDownPlayer.setPlayerColor(gameInstance.seatColors.get(pos));

					gameInstance.update(new PlayerEditAction(id, player, player1));
					gameInstance.update(new PlayerEditAction(id, player, sitDownPlayer));
					if (table != null && ial.size() > 0){
						double offset = table.getTableOffset(sitDownPlayer, gameInstance.getObjectInstanceById(ial.get(0)));
						ObjectFunctions.moveOwnStackToBoardPosition(id, gameInstance, sitDownPlayer, table.getTableCenter(new Point2D.Double()), offset, ial);
					}
				}
			}
			else {
				sitDownPlayer.seatNum = pos;
				if (gameInstance.seatColors.size() > pos) {
					sitDownPlayer.setPlayerColor(gameInstance.seatColors.get(pos));
				}
				gameInstance.update(new PlayerEditAction(id, player, sitDownPlayer));
				if (table != null && ial.size() > 0){
					double offset = table.getTableOffset(sitDownPlayer, gameInstance.getObjectInstanceById(ial.get(0)));
					ObjectFunctions.moveOwnStackToBoardPosition(id, gameInstance, sitDownPlayer, table.getTableCenter(new Point2D.Double()), offset, ial);
				}
			}
		}
	}


	public void sitDown(Player player, int pos){
		sitDown(player, pos, false);
	}

	private boolean checkFirstMouseClick(MouseEvent arg0) {
		if ((firstMouseClick == -1) && SwingUtilities.isLeftMouseButton(arg0)){
			firstMouseClick = 0;
			return true;
		}
		else if ((firstMouseClick == -1) && SwingUtilities.isMiddleMouseButton(arg0)){
			firstMouseClick = 1;
			return true;
		}
		else if ((firstMouseClick == -1) && SwingUtilities.isRightMouseButton(arg0)){
			firstMouseClick = 2;
			return true;
		}
		return false;
	}

	public boolean allMouseKeysReleased(MouseEvent arg0){
		int counter = 0;
		if (SwingUtilities.isLeftMouseButton(arg0)){
			++counter;
			isLeftMouseKeyHold = false;
		}
		if (SwingUtilities.isMiddleMouseButton(arg0)){
			++counter;
			isMiddleMouseKeyHold = false;
		}
		if (SwingUtilities.isRightMouseButton(arg0)){
			++counter;
			isRightMouseKeyHold = false;
		}
		return counter <= 1;
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		requestFocus();
	}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {
		if (!player.visitor) {
			if (checkFirstMouseClick(arg0)) {
				isLeftMouseKeyHold |= SwingUtilities.isLeftMouseButton(arg0);
				isRightMouseKeyHold |= SwingUtilities.isRightMouseButton(arg0);
				isMiddleMouseKeyHold |= SwingUtilities.isMiddleMouseButton(arg0);
				//get nearest object concerning the mouse position
				ObjectInstance nearestObject = ObjectFunctions.getNearestObjectByPosition(this, gameInstance, player, mouseBoardPos.getXI(), mouseBoardPos.getYI(), 1, null);
				mouseScreenX = arg0.getX();
				mouseScreenY = arg0.getY();
				screenToBoardPos(arg0.getX(), arg0.getY(), mousePressedGamePos);
				mouseBoardPos.set(mousePressedGamePos);
				mouseInPrivateArea = ObjectFunctions.isInPrivateArea(privateArea, mouseBoardPos.getXI(), mouseBoardPos.getYI());
				ObjectFunctions.updateSelectedObjects(gameInstance, player, selectedObjects);
				if (!isSelectStarted && hoveredObject == null) {
					if (!arg0.isControlDown()) {
						ObjectFunctions.deselectAllSelected(id, gameInstance, player, ial, hoveredObject, selectedObjects);
					}
					beginSelectPosScreenX = arg0.getX();
					beginSelectPosScreenY = arg0.getY();
					isSelectStarted = true;
				}
				//Select objects on click
				else if (!mouseInPrivateArea && nearestObject != null) {
					//Select unselected objects on click and deselect selected objects on Control+click
					if (!ObjectFunctions.objectIsSelectedByPlayer(gameInstance, player, nearestObject.id) || arg0.isControlDown()) {
						if (!arg0.isControlDown()) {
							ObjectFunctions.deselectAllSelected(id, gameInstance, player, ial, hoveredObject, selectedObjects);
							ObjectFunctions.selectObject(id, gameInstance, player, nearestObject.id, selectedObjects);
                            playAudio(AudioClip.select);
						} else if (ObjectFunctions.objectIsSelectedByPlayer(gameInstance, player, nearestObject.id)) {
							ObjectFunctions.deselectObject(id, gameInstance, player, nearestObject.id, hoveredObject, selectedObjects);
						} else {
							ObjectFunctions.selectObject(id, gameInstance, player, nearestObject.id, selectedObjects);
							playAudio(AudioClip.select);
						}
					}
				} else if (mouseInPrivateArea && hoveredObject != null && this.privateArea.contains(hoveredObject.id)) {
					Vector2d objectPosition = new Vector2d();
					ObjectFunctions.getPrivateAreaHandCardPositionFromHoveredObject(this, gameInstance, objectPosition);
					ObjectState state = hoveredObject.state.copy();
					state.posX = objectPosition.getXI();
					state.posY = objectPosition.getYI();
					gameInstance.update(new GameObjectInstanceEditAction(id, player, hoveredObject, state));

					ObjectFunctions.deselectAllSelected(id, gameInstance, player, ial, hoveredObject, selectedObjects);
					ObjectFunctions.selectObject(id, gameInstance, player, hoveredObject.id, selectedObjects);
                    playAudio(AudioClip.select);
					ObjectFunctions.setNewDrawValue(id, gameInstance, player, hoveredObject);

				} else {
					ObjectFunctions.deselectAllSelected(id, gameInstance, player, ial, hoveredObject, selectedObjects);
				}
				if (!isSelectStarted) {
					if (selectedObjects.size() > 0) {
						objOrigPosX.clear();
						objOrigPosY.clear();
						for (int id : selectedObjects) {
							ObjectInstance oi = gameInstance.getObjectInstanceById(id);
							objOrigPosX.add(oi.state.posX);
							objOrigPosY.add(oi.state.posY);
							oi.state.isActive = true;
						}
					}
					//Handle all drags of Objects
					sortSelectedObjects();
					MoveFunctions.dragObjects(this, gameInstance, player, arg0, selectedObjects, objOrigPosX, objOrigPosY, mousePressedGamePos, mouseBoardPos, mouseWheelValue);
				}
				if (this.privateArea.containsScreenCoordinates(mouseScreenX, mouseScreenY)) {
					this.privateArea.currentDragPosition = this.privateArea.getInsertPosition(mouseScreenX, mouseScreenY);
					outText = "Position:" + String.valueOf(this.privateArea.currentDragPosition);
				} else {
					this.privateArea.currentDragPosition = -1;
				}
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {

			/*Translate the board if alt is down*/
			if ((arg0.isAltDown() && SwingUtilities.isLeftMouseButton(arg0)) || (arg0.isAltDown() && SwingUtilities.isRightMouseButton(arg0)) ) {
				//updatePlayerMousePos(arg0);
				translateBoard(arg0);
				boardTranslation = true;
			} else {

				if (player != null) {
					updatePlayerMousePos(arg0);
					if (!player.visitor) {
						ObjectFunctions.getOwnedStack(gameInstance, player, ial);
						StringBuilder strB = new StringBuilder();
						StringUtils.toString(ial, ' ', strB).append(' ');
						outText = strB.toString();


						if (!isSelectStarted) {
							sortSelectedObjects();
							MoveFunctions.dragObjects(this, gameInstance, player, arg0, selectedObjects, objOrigPosX, objOrigPosY, mousePressedGamePos, mouseBoardPos, mouseWheelValue);
							/*Handle all drags of Token Objects*/
							if (this.privateArea.containsScreenCoordinates(mouseScreenX, mouseScreenY)) {
								this.privateArea.currentDragPosition = this.privateArea.getInsertPosition(mouseScreenX, mouseScreenY);
								outText = "Position:" + String.valueOf(this.privateArea.currentDragPosition);
							} else {
								this.privateArea.currentDragPosition = -1;
							}
						} else {
							if (hoveredObject == null && !SwingUtilities.isMiddleMouseButton(arg0) && !(SwingUtilities.isLeftMouseButton(arg0) && arg0.isShiftDown()) && !mouseInPrivateArea) {
								selectWidth = mouseScreenX - beginSelectPosScreenX;
								selectHeight = mouseScreenY - beginSelectPosScreenY;
							}
						}

					} else {
						return;
					}
				}
			}
			repaint();
	}


	@Override
	public void mouseReleased(MouseEvent arg0) {
		if (allMouseKeysReleased(arg0)) {
            boardTranslation = false;
			firstMouseClick = -1;
			if (player == null || player.visitor) {
				return;
			}
			if (!isSelectStarted && selectedObjects.size() > 0 && (SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isRightMouseButton(arg0) || SwingUtilities.isMiddleMouseButton(arg0))) {
				for (int id : ObjectFunctions.getObjectRepresentatives(gameInstance, selectedObjects)) {
					ObjectInstance oi = gameInstance.getObjectInstanceById(id);
					ObjectFunctions.setNewDrawValue(id, gameInstance, player, oi);
					ObjectFunctions.releaseObjects(arg0, this, gameInstance, player, table.getTableCenter(new Point2D.Double()), table.getTableOffset(player, gameInstance.getObjectInstanceById(id)), oi, hoveredObject, selectedObjects, mouseScreenX, mouseScreenY, 1);
					if (oi.go instanceof GameObjectDice && SwingUtilities.isMiddleMouseButton(arg0)) {
						ObjectFunctions.rollTheDice(id, gameInstance, player, oi);
					}
				}
			}
			if (isSelectStarted) {
				ObjectFunctions.getObjectsInsideBox(gameInstance, player, beginSelectPosScreenX, beginSelectPosScreenY, selectWidth, selectHeight, ial, boardToScreenTransformation);
				if (!arg0.isControlDown()){
					ObjectFunctions.deselectAllSelected(id, gameInstance, player, ial2, hoveredObject, selectedObjects);
					ObjectFunctions.selectObjects(id, gameInstance, player, ial, selectedObjects);
				}
				else{
					for (int id : ial) {
						if(ObjectFunctions.objectIsSelectedByPlayer(gameInstance,player,id)) {
							ObjectFunctions.deselectObject(this.id, gameInstance, player, id, hoveredObject, selectedObjects);
						}
						else{
							ObjectFunctions.selectObject(this.id,gameInstance,player,id, selectedObjects);
						}
					}
				}
				selectHeight = 0;
				selectWidth = 0;
				beginSelectPosScreenX = 0;
				beginSelectPosScreenY = 0;
				isSelectStarted = false;
			}
			ObjectFunctions.deactivateAllObjects(gameInstance);
			ObjectInstance nearestObject = ObjectFunctions.getNearestObjectByPosition(this, gameInstance, player, mouseBoardPos.getXI(), mouseBoardPos.getYI(), 1, null);
			setHoveredObject(gameInstance, player, nearestObject);
			//gameInstance.update(new GamePlayerEditAction(id, player, player));
			mouseColor = player.color;
			repaint();
		}
	}




	@SuppressWarnings("unused")
	private final Runnable repaintRunnable = new Runnable() {
		@Override
		public void run()
		{
			repaint();
		}
	};


	@Override
	public void changeUpdate(GameAction action) {
		if (logger.isDebugEnabled()) {logger.debug("change Update:" + action.source + " " + id);}
		if (action instanceof GameObjectInstanceEditAction)
		{
			privateArea.updatePrivateObjects(gameInstance, player);
			repaint();
			//If direct repaint causes problems use this:
			//JFrameUtils.runByDispatcher(repaintRunnable);
		}
		else if (action instanceof PlayerEditAction || action instanceof GameStructureEditAction || action instanceof GameObjectEditAction)
		{
			repaint();
		}

		if ((action instanceof PlayerAddAction || action instanceof PlayerRemoveAction) && this.table != null){
			this.table.updatePlayers(gameInstance);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		downKeys.add(e.getKeyCode());
		e.consume();
		boolean controlDown = e.isControlDown();
		boolean shiftDown = e.isShiftDown();
		boolean altDown = e.isAltDown();
		translateBoard(e);

		if (!player.visitor) {
			//Mouse move independent keys
			if (e.getKeyCode() == KeyEvent.VK_H) {
				if (privateArea.zooming == 0.1) {
					privateArea.zooming = privateArea.savedZooming;
					mouseInPrivateArea = ObjectFunctions.isInPrivateArea(privateArea, mouseBoardPos.getXI(), mouseBoardPos.getYI());
				} else {
					privateArea.savedZooming = privateArea.zooming;
					privateArea.zooming = 0.1;
					mouseInPrivateArea = false;
				}
				updateGameTransform();
				repaint();

			} else if (e.getKeyCode() == KeyEvent.VK_T && altDown) {
				isTableVisible = !isTableVisible;
			}else if (shiftDown && e.getKeyCode() == KeyEvent.VK_C) {
				int count = 0;
				for (int oId : selectedObjects) {
					ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
					count += ObjectFunctions.countStackValues(gameInstance, oi);
				}
				player.actionString = "Value: " + String.valueOf(count);
			}
			else if (e.getKeyCode() == KeyEvent.VK_C && !shiftDown) {
				int count = 0;
                ial.set(selectedObjects);
				for (int oId : ial) {
					ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
					if (oi.go instanceof GameObjectToken) {
						count += ObjectFunctions.countStack(gameInstance, oi);
					} else if (oi.go instanceof GameObjectDice) {
						count += 1;
					} else if (oi.go instanceof GameObjectFigure) {
						count += 1;
					}
				}
				player.actionString = "Object Number: " + String.valueOf(count);
			}

			//Only allowed if mouse is in stacker in the middle of the table
			if (mouseInStacker){
				if(hoveredObject != null && e.getKeyCode() == KeyEvent.VK_M && !shiftDown)
				{
					Point2D tableCenter = table.getTableCenter(new Point2D.Double());
					ObjectFunctions.stackObjectsOnShapeAndMoveToPoint(id, gameInstance, player, table.stackerShape, tableCenter, getBoardToScreenTransform(), ial, hoveredObject, selectedObjects, ObjectFunctions.SIDE_TO_BACK);
				}
			}

			//Only allowed if mouse is not clicked
			if (!(isLeftMouseKeyHold || isRightMouseKeyHold || isMiddleMouseKeyHold)) {
				if (e.getKeyCode() == KeyEvent.VK_F && !shiftDown && !altDown) {
                    ial.set(selectedObjects);
					for (int oId : ial) {
						ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
						ObjectFunctions.flipTokenObject(id, gameInstance, player, oi);
						ObjectFunctions.rollTheDice(id, gameInstance, player, oi);
					}
					if (hoveredObject != null && ObjectFunctions.getObjectSelector(gameInstance, hoveredObject.id) != player.id) {
						ObjectFunctions.flipTokenObject(id, gameInstance, player, hoveredObject);
						ObjectFunctions.rollTheDice(id, gameInstance, player, hoveredObject);
					}
				} else if (e.getKeyCode() == KeyEvent.VK_F && shiftDown) {
                    ial.set(selectedObjects);
					for (int oId : ObjectFunctions.getObjectRepresentatives(gameInstance, ial)) {
						ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
						ObjectFunctions.flipTokenStack(id, gameInstance, player, ObjectFunctions.getStackTop(gameInstance, oi), hoveredObject, selectedObjects);
					}
				} else if (e.getKeyCode() == KeyEvent.VK_S) {
                    ial.set(selectedObjects);
					for (int oId : ObjectFunctions.getObjectRepresentatives(gameInstance, ial)) {
						ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
						ObjectFunctions.shuffleStack(id, gameInstance, player, oi);
                        playAudio(AudioClip.shuffle);
						ObjectFunctions.deselectObject(id, gameInstance, player, oi.id, hoveredObject, selectedObjects);
						ObjectFunctions.selectObject(id, gameInstance, player, ObjectFunctions.getStackTop(gameInstance, oi).id, selectedObjects);
					}

				}
				else if (e.getKeyCode() == KeyEvent.VK_V) {
					if (!mouseInPrivateArea) {
						for (int oId : ObjectFunctions.getObjectRepresentatives(gameInstance, selectedObjects)) {
							ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
							if (ObjectFunctions.haveSamePositions(ObjectFunctions.getStackTop(gameInstance, oi), ObjectFunctions.getStackBottom(gameInstance, oi))) {
								ObjectFunctions.displayStack(id, gameInstance, player, oi, (int) (oi.getWidth(player.id) * gameInstance.cardOverlap));
							} else {
								if (selectedObjects.size() == 1) {
									ObjectInstance selectedObject = gameInstance.getObjectInstanceById(selectedObjects.get(0));
									ObjectFunctions.collectStack(id, gameInstance, player, selectedObject);
								} else {
									ObjectFunctions.collectStack(id, gameInstance, player, oi);
								}
							}
						}
						// unfold dice
						for (int oId : selectedObjects) {
							ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
							if (oi != null && oi.go instanceof GameObjectDice) {
								GameObjectDice.DiceState state = (GameObjectDice.DiceState) oi.state;
								state.unfold = !state.unfold;
							}
						}
						if (hoveredObject != null && player.id != ObjectFunctions.getObjectSelector(gameInstance, hoveredObject.id) && hoveredObject.go instanceof GameObjectDice) {
							GameObjectDice.DiceState state = (GameObjectDice.DiceState) hoveredObject.state;
							state.unfold = !state.unfold;
						}

					} else {
						ObjectFunctions.getOwnedStack(gameInstance, player, ial);
						if (ial.size() > 0) {
							ObjectInstance oi = gameInstance.getObjectInstanceById(ial.getI(0));
							if (ObjectFunctions.haveSamePositions(ObjectFunctions.getStackTop(gameInstance, oi), ObjectFunctions.getStackBottom(gameInstance, oi))) {
								ObjectFunctions.displayStack(id, gameInstance, player, oi, (int) (oi.getWidth(player.id) * gameInstance.cardOverlap));
							} else {
								if (selectedObjects.size() == 1) {
									ObjectInstance selectedObject = gameInstance.getObjectInstanceById(selectedObjects.get(0));
									ObjectFunctions.collectStack(id, gameInstance, player, selectedObject);
								} else {
									ObjectFunctions.collectStack(id, gameInstance, player, oi);
								}
							}
						}
					}
				} else if (e.getKeyCode() == KeyEvent.VK_D && shiftDown) {
					ObjectFunctions.dropObjects(this, gameInstance, player, hoveredObject, selectedObjects);
				} else if (e.getKeyCode() == KeyEvent.VK_D && !shiftDown) {
					ObjectFunctions.dropObject(this, gameInstance, player, hoveredObject);
				} else if (e.getKeyCode() == KeyEvent.VK_P && !shiftDown) {
					if (hoveredObject != null) {
						ObjectFunctions.playObject(this, gameInstance, player, hoveredObject, hoveredObject, selectedObjects);
                        playAudio(AudioClip.drop);
					}
				} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					sitDown(player, PlayerFunctions.GetTablePlayerPosition(player));
				}


				//Only allowed if mouse is not in private area
				if (!mouseInPrivateArea) {
					if (e.getKeyCode() == KeyEvent.VK_R && !shiftDown) {
						for (int oId : ObjectFunctions.getObjectRepresentatives(gameInstance, selectedObjects)) {
							ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
							ObjectFunctions.rotateStep(id, gameInstance, player, oi, ial);
						}
						repaint();
					} else if (e.getKeyCode() == KeyEvent.VK_R && shiftDown) {
                        ial.set(selectedObjects);
						for (int oId : ial) {
							ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
							ObjectFunctions.removeStackRelations(id, gameInstance, player, oi);
						}
					} else if (e.getKeyCode() == KeyEvent.VK_T && !altDown) {
						for (int oId : ObjectFunctions.getObjectRepresentatives(gameInstance, selectedObjects)) {
							ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
							double offset = 0;
							if (table != null){
								offset = table.getTableOffset(player, oi);
							}
							ObjectFunctions.takeObjects(this, gameInstance, player, table.getTableCenter(new Point2D.Double()), offset,  oi, hoveredObject, selectedObjects);
						}
						ObjectFunctions.deselectAllSelected(id, gameInstance, player, ial, hoveredObject, selectedObjects);
					} else if (e.getKeyCode() == KeyEvent.VK_M && shiftDown && !altDown) {
                        ial.set(selectedObjects);
						for (int oId : ial) {
							ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
							ObjectFunctions.stackAllObjectsOfGroup(id, gameInstance, player, oi, hoveredObject, selectedObjects, false);
						}

					} else if (e.getKeyCode() == KeyEvent.VK_M && shiftDown && altDown) {
                        ial.set(selectedObjects);
						for (int oId : ial) {
							ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
							ObjectFunctions.stackAllObjectsOfGroup(id, gameInstance, player, oi, hoveredObject, selectedObjects, true);
						}
					}else if (e.getKeyCode() == KeyEvent.VK_M && !shiftDown) {
						ial.set(selectedObjects);
						ObjectFunctions.makeStack(id, gameInstance, player, ial, hoveredObject, selectedObjects, ObjectFunctions.SIDE_UNCHANGED);
					} else if (altDown && !boardTranslation && scaledObjects.size() > 0) {
						for (int oId : ObjectFunctions.getObjectRepresentatives(gameInstance, selectedObjects)) {
							ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
							if (!scaledObjects.contains(oi.id)) {
								scaledObjects.add(oi.id);
								savedScalingFactors.add(oi.scale);
								oi.scale *= scalingFactor;
							}
						}
						repaint();
					} else if (e.getKeyCode() == KeyEvent.VK_F && altDown) {
						ial.set(selectedObjects);
						for (int oId : ial) {
							ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
							ObjectFunctions.fixObject(id, gameInstance, player, oi);
						}
					}
					else if (e.getKeyCode() == KeyEvent.VK_G){
						ial.clear();
						for (int oId : ObjectFunctions.getObjectRepresentatives(gameInstance, selectedObjects)) {
							IntegerArrayList stackList = new IntegerArrayList();
							ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
							ObjectFunctions.getStack(gameInstance, oi, stackList);
							ObjectFunctions.removeStackRelations(id, gameInstance, player, oi);
							ial.add(stackList);
						}
						ObjectFunctions.giveObjects(this, gameInstance, table.getTableCenter(new Point2D.Double()), table.getTableOffset(player, gameInstance.getObjectInstanceById(ial.get(0))), ial, objectInstanceList, hoveredObject, selectedObjects);
					}
					else if (e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_ADD) {
						//Scale hovered Object
						double scale = 1.1;
						if (hoveredObject != null && (hoveredObject.scale > 0.1 || scale > 1) && (hoveredObject.scale < 2 || scale < 1)) {
							hoveredObject.tmpScale /= scale;
							hoveredObject.scale *= scale;
						}
					}
					else if (e.getKeyCode() == KeyEvent.VK_MINUS || e.getKeyCode() == KeyEvent.VK_SUBTRACT) {
						//Scale hovered Object
						double scale = 0.9;
						if (hoveredObject != null &&(hoveredObject.scale > 0.1 || scale > 1) && (hoveredObject.scale < 2 || scale < 1)) {
							hoveredObject.tmpScale /= scale;
							hoveredObject.scale *= scale;
						}
					}
				}

				//Only allowed if mouse in private area
				if (mouseInPrivateArea){
					if (e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_ADD) {
						int value = 1;
						privateArea.zoomingFactor += value;
						if ((privateArea.zooming >= 0.2) && (privateArea.zooming < 2 || value>0)) {
							privateArea.zooming = Math.exp(-privateArea.zoomingFactor * 0.1);
						} else {
							privateArea.zoomingFactor -= value;
						}
						updateGameTransform();
					}
					else if (e.getKeyCode() == KeyEvent.VK_MINUS || e.getKeyCode() == KeyEvent.VK_SUBTRACT) {
						int value = -1;
						privateArea.zoomingFactor += value;
						if ((privateArea.zooming >= 0.2 || value < 0) && (privateArea.zooming < 2)) {
							privateArea.zooming = Math.exp(-privateArea.zoomingFactor * 0.1);
						} else {
							privateArea.zoomingFactor -= value;
						}
						updateGameTransform();
					}
					else if (e.getKeyCode() == KeyEvent.VK_S){
						if (hoveredObject != null){
							ObjectFunctions.sortHandCardsByValue(this, gameInstance, player, table.getTableCenter(new Point2D.Double()), table.getTableOffset(player, hoveredObject), ial, objectInstanceList, hoveredObject, selectedObjects, false);
						}
					}
				}
			}
		}
	}


	@Override
	public void keyReleased(KeyEvent e) {
		downKeys.remove(e.getKeyCode());
		if (!mouseInPrivateArea) {
			if (e.getKeyCode() == KeyEvent.VK_ALT && scaledObjects.size() > 0) {
				for (int i = 0; i < scaledObjects.size(); i++) {
					gameInstance.getObjectInstanceById(scaledObjects.get(i)).scale = savedScalingFactors.getD(i);
				}
				scaledObjects.clear();
				savedScalingFactors.clear();
				scalingFactor = originalScalingFactor;
			}
			repaint();
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		return false;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		//Scale hovered Object
		if (!mouseInPrivateArea && hoveredObject != null){
			outText = String.valueOf(e.getPreciseWheelRotation());
			double scale = 0.95;
			if((int) e.getPreciseWheelRotation() < 0) {
				scale = 1.05;
			}

			if ((hoveredObject.scale>0.1 || scale>1) && (hoveredObject.scale<2 || scale<1)) {
				hoveredObject.tmpScale /= scale;
				hoveredObject.scale *= scale;
			}
		}
		else if (e.isAltDown() && !mouseInPrivateArea)
		{
			zoomFactor += (int) e.getPreciseWheelRotation();
			if ((zooming >= 0.2 || e.getPreciseWheelRotation() < 0) && (zooming < 5 || e.getPreciseWheelRotation() > 0)) {
				zooming = Math.exp(-zoomFactor * 0.1);
			}else{
				zoomFactor -= (int) e.getPreciseWheelRotation();
			}
			updateGameTransform();
		}else if (mouseInPrivateArea)
		{
			privateArea.zoomingFactor += (int) e.getPreciseWheelRotation();
			if ((privateArea.zooming >= 0.2 || e.getPreciseWheelRotation() < 0) && (privateArea.zooming < 2 || e.getPreciseWheelRotation() > 0)) {
				privateArea.zooming = Math.exp(-privateArea.zoomingFactor * 0.1);
			}else{
				privateArea.zoomingFactor -= (int) e.getPreciseWheelRotation();
			}
			updateGameTransform();
		}

		else{
			mouseWheelValue += (int) e.getPreciseWheelRotation();
		}
		if(mouseWheelValue <= 0) {
			mouseWheelValue = 0;
			infoText = "";
		}
		else {
			infoText = String.valueOf(mouseWheelValue);
		}

		repaint();
	}

	public void updatePlayerMousePos(MouseEvent arg0){
		screenToBoardPos(mouseScreenX = arg0.getX(), mouseScreenY = arg0.getY(), mouseBoardPos);
		player.setMousePos(mousePressedGamePos.getXI(), mousePressedGamePos.getYI());
		player.setMousePos(mouseBoardPos.getXI(), mouseBoardPos.getYI());
		mouseInPrivateArea = ObjectFunctions.isInPrivateArea(privateArea, mouseBoardPos.getXI(), mouseBoardPos.getYI());
		gameInstance.update(new PlayerEditAction(id, player, player));
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {}

	@Override
	public void componentHidden(ComponentEvent arg0) {}

	@Override
	public void componentMoved(ComponentEvent arg0) {}

	@Override
	public void componentResized(ComponentEvent arg0) {updateGameTransform();}

	@Override
	public void componentShown(ComponentEvent arg0) {updateGameTransform();}

	public void updateGameTransform()
	{
		boardToScreenTransformation.setToIdentity();
		boardToScreenTransformation.translate(getWidth() / 2, getHeight() / 2);
		boardToScreenTransformation.scale(zooming, zooming);
		boardToScreenTransformation.rotate(rotation);
		boardToScreenTransformation.translate(translateX, translateY);
		try {
			screenToBoardTransformation.setTransform(boardToScreenTransformation);
			screenToBoardTransformation.invert();
		} catch (NoninvertibleTransformException e) {
			logger.error("Transformation not invertible");
		}
		TransformConversion.copy(screenToBoardTransformation, gameTransform);
		if (player != null)
		{
			gameInstance.update(new PlayerCharacterPositionUpdate(id, player, player, screenToBoardTransformation, getWidth(), getHeight()));
		}
		if (privateArea!=null){
			int privateAreaWidth = 750;
			int privateAreaHeight = 750;
			privateArea.basePoint.setLocation(getWidth()/2, getHeight());
		}
	}

	public AffineTransform getBoardToScreenTransform(){return boardToScreenTransformation;}

	public void screenToBoardPos(int x, int y, Vector2d out){gameTransform.rdotAffine(x, y, out);}//TODO check

	public void boardToScreenPos(Point2D boardCoordinates, Point2D screenCooardinates){boardToScreenTransformation.transform(boardCoordinates, screenCooardinates);}

	public void translateBoard(KeyEvent keyEvent){
		double tmpX = 0;
		double tmpY = 0;
		boolean isTranslation = false;
		if (keyEvent.getKeyCode() == KeyEvent.VK_LEFT){
			tmpX = 50./zooming;
			isTranslation = true;
		}else if (keyEvent.getKeyCode() == KeyEvent.VK_RIGHT){
			tmpX = -50./zooming;
			isTranslation = true;
		}
		if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN){
			tmpY = -50./zooming;
			isTranslation = true;
		}else if (keyEvent.getKeyCode() == KeyEvent.VK_UP){
			tmpY = 50./zooming;
			isTranslation = true;
		}

		if (!mouseInPrivateArea && hoveredObject==null && (keyEvent.getKeyCode() == KeyEvent.VK_PLUS || keyEvent.getKeyCode() == KeyEvent.VK_ADD)){
			zoomFactor -= 1;
			zooming = Math.exp(-zoomFactor * 0.1);
			updateGameTransform();
		}else if (!mouseInPrivateArea && hoveredObject==null && (keyEvent.getKeyCode() == KeyEvent.VK_MINUS || keyEvent.getKeyCode() == KeyEvent.VK_SUBTRACT)) {
			zoomFactor += 1;
			zooming = Math.exp(-zoomFactor * 0.1);
			updateGameTransform();
		}

		if (isTranslation) {
			double sin = Math.sin(rotation);
			double cos = Math.cos(rotation);
			translateX += tmpX * cos + tmpY * sin;
			translateY += -tmpX * sin + tmpY * cos;
		}

		if (keyEvent.getKeyCode() == KeyEvent.VK_PAGE_UP)
		{
			rotation += 10 / 200.;
			isTranslation = true;
		}else if (keyEvent.getKeyCode() == KeyEvent.VK_PAGE_DOWN){
			rotation -= 10 / 200.;
			isTranslation = true;
		}
		if (isTranslation) {
			updateGameTransform();
			gameInstance.update(new PlayerMousePositionUpdate(id, player, player, mouseBoardPos.getXI(), mouseBoardPos.getYI()));
		}
	}


	public void translateBoard(MouseEvent arg0)
	{
		if (SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isMiddleMouseButton(arg0))
		{
			double tmpX = (arg0.getX() - mouseScreenX) / zooming;
			double tmpY = (arg0.getY() - mouseScreenY) / zooming;
			double sin = Math.sin(rotation);
			double cos = Math.cos(rotation);
			translateX += tmpX * cos + tmpY * sin;
			translateY += -tmpX * sin + tmpY * cos;
		}
		if (SwingUtilities.isRightMouseButton(arg0))
		{
			rotation += (arg0.getX() - mouseScreenX) / (invert_rotation ? 200. : -200.);
		}
		updateGameTransform();
		screenToBoardPos(mouseScreenX = arg0.getX(), mouseScreenY = arg0.getY(), mouseBoardPos);
		player.setMousePos(mouseBoardPos.getXI(), mouseBoardPos.getYI());
		gameInstance.update(new PlayerEditAction(id, player, player));
	}



	public int getNumberOfSelectedObjects(){
		return this.selectedObjects.size();
	}

	public IntegerArrayList getSelectedObjects(){
		return this.selectedObjects;
	}

	public void setHoveredObject(GameInstance gameInstance, Player player, ObjectInstance objectInstance){
		//Can only hover if not selected
		if (objectInstance != null && (!ObjectFunctions.objectIsSelected(gameInstance, objectInstance.id) || ObjectFunctions.objectIsSelectedByPlayer(gameInstance,player, objectInstance.id))) {
			this.hoveredObject = objectInstance;
		}
	}

	public void unhoverObject(GameInstance gameInstance, Player player)
	{
		if (this.hoveredObject != null)
		{
			this.hoveredObject.scale *= this.hoveredObject.tmpScale;
			this.hoveredObject.tmpScale = 1;
			if (this.hoveredObject.go instanceof GameObjectDice)
			{
				if (!ObjectFunctions.objectIsSelectedByPlayer(gameInstance, player, this.hoveredObject.id)) {
					GameObjectDice.DiceState diceState = (GameObjectDice.DiceState) this.hoveredObject.state;
					diceState.unfold = false;
				}
			}
		}
		this.hoveredObject = null;
	}

	private static void sortObjectsByDrawValue(
	        IntegerArrayList selectedObjects,
	        IntegerArrayList ial,
	        IntegerArrayList objOrigPosX,
	        IntegerArrayList objOrigPosY,
	        GameInstance gameInstance)
	{
	    ial.set(selectedObjects);
        IntegerArrayList sortedObjPosX = new IntegerArrayList();
        IntegerArrayList sortedObjPosY = new IntegerArrayList();
        if (objOrigPosX.size() > 0) {
            for (int i = 0; i < selectedObjects.size();++i) {
                sortedObjPosX.add(0);
                sortedObjPosY.add(0);
            }
        }
        ial.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return gameInstance.getObjectInstanceById(o1).state.drawValue - gameInstance.getObjectInstanceById(o2).state.drawValue;
            }
        });
        if (objOrigPosX.size() > 0) {
            for (int i = 0; i < ial.size(); ++i) {
                int idx = selectedObjects.indexOf(ial.getI(i));
                sortedObjPosX.set(i, objOrigPosX.getI(idx));
                sortedObjPosY.set(i, objOrigPosY.getI(idx));
            }
        }
        selectedObjects.set(ial);
        if (sortedObjPosX.size() > 0) {
            objOrigPosX.set(sortedObjPosX);
            objOrigPosY.set(sortedObjPosY);
        }
	}
	
	public void sortSelectedObjects(){
		sortObjectsByDrawValue(selectedObjects, ial, objOrigPosX, objOrigPosY, gameInstance);
	}

	class SheetPanel extends JPanel implements LanguageChangeListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = -9066866830480740588L;

		public SheetPanel(BorderLayout borderLayout) {
			this.setBorder( BorderFactory.createLineBorder( Color.black ) );
		}

		@Override
		public void languageChanged(Language lang) {
			removeAll();
			setLayout(new GridLayout(16, 1, 20, 0));
			add(new JLabel(lang.getString(Words.sit_down)                + ": " + new ControlCombination(0, -1,  KeyEvent.VK_ENTER, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.move_top_card) 	         + ": " + new ControlCombination(0, 0,  -1, 1).toString(lang)));
			add(new JLabel(lang.getString(Words.move_stack)              + ": " + new ControlCombination(0, 1, -1, 1).toString(lang)));
			add(new JLabel(lang.getString(Words.take_objects_to_hand)    + ": " + new ControlCombination(0, -1,  KeyEvent.VK_T, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.drop_all_hand_cards)     + ": " + new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1,  KeyEvent.VK_D, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.get_bottom_card)         + ": " + new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1,  -1, 2).toString(lang)));
			add(new JLabel(lang.getString(Words.shuffle_stack)           + ": " + new ControlCombination(0, -1,  KeyEvent.VK_S, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.flip_card)		         + ": " + new ControlCombination(0, -1,  KeyEvent.VK_F, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.flip_stack)	             + ": " + new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1,  KeyEvent.VK_F, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.view_collect_stack)	     + ": " + new ControlCombination(0, -1,  KeyEvent.VK_V, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.collect_selected_objects)+ ": " + new ControlCombination(0, -1,  KeyEvent.VK_M, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.collect_all_objects)     + ": " + new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1,  KeyEvent.VK_M, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.rotate_object)           + ": " + new ControlCombination(0, -1,  KeyEvent.VK_R, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.dissolve_stack)	         + ": " + new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1,  KeyEvent.VK_R, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.count_objects)           + ": " + new ControlCombination(0, -1,  KeyEvent.VK_C, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.count_values)            + ": " + new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1,  KeyEvent.VK_C, 0).toString(lang)));
			revalidate();
		}
	}

	public int getPlayerId() {
		Player pl = player;
		return pl == null ? -1 : pl.id;
	}

	public Player getPlayer() {return player;}
}
