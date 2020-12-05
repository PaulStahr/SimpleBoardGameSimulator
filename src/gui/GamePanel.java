package gui;

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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import javax.imageio.ImageIO;
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
import gameObjects.action.AddPlayerAction;
import gameObjects.action.GameAction;
import gameObjects.action.GameObjectEditAction;
import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.action.GamePlayerEditAction;
import gameObjects.action.GameStructureEditAction;
import gameObjects.definition.GameObjectDice;
import gameObjects.functions.MoveFunctions;
import gameObjects.functions.ObjectFunctions;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.ObjectState;
import geometry.Matrix3d;
import geometry.Vector2d;
import io.GameIO;
import main.Player;
import util.TimedUpdateHandler;
import util.data.IntegerArrayList;


public class GamePanel extends JPanel implements MouseListener, MouseMotionListener, GameInstance.GameChangeListener, KeyListener, KeyEventDispatcher, MouseWheelListener, ActionListener, ComponentListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3579141032474558913L;
	private static final Logger logger = LoggerFactory.getLogger(GamePanel.class);
	
	static boolean invert_rotation = false;
	static {
		Options.addModificationListener(new Runnable() {

			@Override
			public void run() {
				invert_rotation = Options.getBoolean("invert_rotation", false);
			}
		});		
	}
	
	final GameInstance gameInstance;
	public ObjectInstance hoveredObject = null;
	IntegerArrayList objOrigPosX = new IntegerArrayList();
	IntegerArrayList objOrigPosY = new IntegerArrayList();
	public Player player;
	public final int id = (int)System.nanoTime();
	private final IntegerArrayList ial = new IntegerArrayList();

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
    public boolean boardTranslation = false;


	//private final ControlPanel controlPanel = new ControlPanel();
	private final AffineTransform boardToScreenTransformation = new AffineTransform();
	private final AffineTransform screenToBoardTransformation = new AffineTransform();
	public Color mouseColor = Color.black;
	public Color dragColor = Color.red;
	public Color stackColor = Color.green;
	public String infoText = "";




	public boolean isSelectStarted = false;

	public final IntegerArrayList scaledObjects = new IntegerArrayList();
	public final ArrayList<Double> savedScalingFactors = new ArrayList<>();
	private double originalScalingFactor = 3;
	private double scalingFactor = originalScalingFactor;
	public int beginSelectPosScreenX = 0;
	public int beginSelectPosScreenY = 0;
	public int selectWidth = 0;
	public int selectHeight = 0;
	public IntegerArrayList selectedObjects = new IntegerArrayList();

	//Show text only if in debug mode
	public String outText = "";
	public boolean isDebug = true;

	public PrivateArea privateArea;
	public Table table = null;
	public int activePlayer = -1;

	public float cardOverlap = (float) (2/3.0);

	public BufferedImage[] playerImages = new BufferedImage[10];
	
	TimedUpdateHandler autosave = new TimedUpdateHandler() {
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


    public GamePanel(GameInstance gameInstance, LanguageHandler lh)
	{
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		int tableRadius = gameInstance.tableRadius;
		this.table = new Table(gameInstance, 2*tableRadius,new Point2D.Double(-tableRadius,-tableRadius));
		this.gameInstance = gameInstance;
		this.privateArea = new PrivateArea(this, gameInstance, boardToScreenTransformation, screenToBoardTransformation);
		this.isDebug = gameInstance.debug_mode;

		if (!this.gameInstance.private_area) {
			this.privateArea.zooming = 0;
		}
		this.isTableVisible = gameInstance.table;
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
				e.printStackTrace();
			}
		}
		DataHandler.timedUpdater.add(autosave);
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
		drawObjectsFromList(this,g,gameInstance,player,ObjectFunctions.getDrawOrder(gameInstance));

		//Draw selection rectangle
		drawSelection(this, g, player);

		//Draw Private Area
		drawPrivateArea(this, g);


		AffineTransform tmp = g2.getTransform();
		g2.setTransform(boardToScreenTransformation);
		//Redraw selected objects not in some private area
		drawObjectsFromList(this, g, gameInstance, player, selectedObjects, ial);

		//Draw all player related information
		drawPlayerPositions(this, g, gameInstance, player, infoText);
		g2.setTransform(tmp);
		//Draw objects in private area
		drawTokensInPrivateArea(this, g, gameInstance, player, hoveredObject);

		//Draw debug informations
		if (isDebug) {
			int hoverId = (hoveredObject== null) ? -1 : hoveredObject.id;
			String stringSelectedObjects = "";
			for (int i = 0; i < selectedObjects.size(); ++i)
			{
				if (i==0) {
					stringSelectedObjects += Integer.toString(selectedObjects.get(i));
				}
				else{
					stringSelectedObjects += "; " + Integer.toString(selectedObjects.get(i));
				}
			}
			g.drawString("Hovered Object: " + Integer.toString(hoverId), 50, 20);
			g.drawString("Selected Objects: " + stringSelectedObjects, 50, 40);
			g.drawString("Player Id: " + Integer.toString(player.id), 50, 60);
			g.drawString("Admin Id: " + Integer.toString(gameInstance.admin), 50, 80);
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
		//Check if mouse is in the private area if private area is not hidden
		mouseInPrivateArea = ObjectFunctions.isInPrivateArea(this, mouseBoardPos.getXI(), mouseBoardPos.getYI());

		//Check if some key is pressed
		if (player != null) {
			//set the mouse position of the player to send to other players
			player.setMousePos(mouseBoardPos.getXI(), mouseBoardPos.getYI());
			//Disable action string if mouse moves and no key is pressed
			if (keyPressed == 0)
			{
				player.actionString = "";
			}
			gameInstance.update(new GamePlayerEditAction(id, player, player));
			//get nearest object concerning the mouse position
			ObjectInstance nearestObject = ObjectFunctions.getNearestObjectByPosition(this, gameInstance, player, mouseBoardPos.getXI(), mouseBoardPos.getYI(), 1, null);
			//do actions if mouse is in the private area or not and there is some nearest object
			if (mouseInPrivateArea && nearestObject != null && !arg0.isControlDown() && !arg0.isShiftDown() && !arg0.isAltDown())
			{
				if (hoveredObject != null){
					hoveredObject.state.isActive = false;
				}
				nearestObject.state.isActive = false;
				hoveredObject = nearestObject;
				outText = String.valueOf(mouseScreenY);
				outText = "OId: " + String.valueOf(hoveredObject.id) + " StackPos: " + String.valueOf(ObjectFunctions.getStackIdOfObject(gameInstance, hoveredObject, ial));
			}else if (!mouseInPrivateArea){
				if (nearestObject != null && !nearestObject.state.inPrivateArea) {
					hoveredObject = nearestObject;
					if (isDebug) {
						outText = "Hover Object: " + String.valueOf(hoveredObject.id);
						outText += "\n Select: " + String.valueOf(hoveredObject.state.isSelected);
					}
				} else {
					if (hoveredObject != null) {
						hoveredObject = null;
						if (isDebug) {
							outText = " ";
						}
					}
				}
			}
		}
		repaint();
	}


	@Override
	public void mouseClicked(MouseEvent arg0) {

		if (arg0.getClickCount() == 2) {
			//Sit down on double click on seat
			for (int i = 0; i < this.table.playerShapes.size();++i) {
				if(this.table.playerShapes.get(i).contains(mouseScreenX, mouseScreenY)) {
					sitDown(player, i);
					break;
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

	private void sitDown(Player player, int pos){
		translateX = 0;
		translateY = 0;
		zooming = getHeight()/ ((float) this.table.getDiameter() + 200);
		zoomFactor = (int)(-10*Math.log(zooming));
		rotation = 0;
		updateGameTransform();
		rotation = Math.toRadians(- 360/Math.max(1,this.table.playerShapes.size()) * pos);
		updateGameTransform();
		if (player != null && gameInstance.seatColors.size() > pos)
		{
			player.color = Color.decode(gameInstance.seatColors.get(pos));
		}
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
		if (checkFirstMouseClick(arg0)) {
			if (SwingUtilities.isLeftMouseButton(arg0)) {
				isLeftMouseKeyHold = true;
			}
			if (SwingUtilities.isRightMouseButton(arg0)) {
				isRightMouseKeyHold = true;
			}
			if (SwingUtilities.isMiddleMouseButton(arg0)) {
				isMiddleMouseKeyHold = true;
			}
			//get nearest object concerning the mouse position
			ObjectInstance nearestObject = ObjectFunctions.getNearestObjectByPosition(this, gameInstance, player, mouseBoardPos.getXI(), mouseBoardPos.getYI(), 1, null);
			mouseScreenX = arg0.getX();
			mouseScreenY = arg0.getY();
			screenToBoardPos(arg0.getX(), arg0.getY(), mousePressedGamePos);
			mouseBoardPos.set(mousePressedGamePos);
			mouseInPrivateArea = ObjectFunctions.isInPrivateArea(this, mouseBoardPos.getXI(), mouseBoardPos.getYI());
			ObjectFunctions.updateSelectedObjects(this, gameInstance, player);
			if (!isSelectStarted && hoveredObject == null) {
				ObjectFunctions.deselectAllSelected(this, gameInstance, player, ial);
				beginSelectPosScreenX = arg0.getX();
				beginSelectPosScreenY = arg0.getY();
				isSelectStarted = true;
			}
			//Select objects on click
			else if (!mouseInPrivateArea) {
				//Select unselected objects on click and deselect selected objects on Control+click
				if(!ObjectFunctions.objectIsSelectedByPlayer(gameInstance, player, nearestObject.id) || arg0.isControlDown()) {
					if (!arg0.isControlDown()) {
						ObjectFunctions.deselectAllSelected(this, gameInstance, player, ial);
						ObjectFunctions.selectObject(this, gameInstance, player, nearestObject.id);
					}
					else if (ObjectFunctions.objectIsSelectedByPlayer(gameInstance, player, nearestObject.id)) {
						ObjectFunctions.deselectObject(this, gameInstance, player, nearestObject.id);
					}
					else {
						ObjectFunctions.selectObject(this, gameInstance, player, nearestObject.id);
					}
				}
			}
			else {
				ObjectFunctions.deselectAllSelected(this, gameInstance, player, ial);
			}
			if (hoveredObject != null && this.privateArea.contains(hoveredObject.id)) {
				ObjectFunctions.setNewDrawValue(this.id, gameInstance, player, hoveredObject);
				ObjectState state = hoveredObject.state.copy();
				state.posX = player.mouseXPos;
				state.posY = player.mouseYPos;
				gameInstance.update(new GameObjectInstanceEditAction(id, player, hoveredObject, state));
			}
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
			MoveFunctions.dragObjects(this, gameInstance, player, arg0, selectedObjects, objOrigPosX, objOrigPosY, mousePressedGamePos, mouseBoardPos, mouseWheelValue);
			if (this.privateArea.containsScreenCoordinates(mouseScreenX, mouseScreenY)) {
				this.privateArea.currentDragPosition = this.privateArea.getInsertPosition(mouseScreenX, mouseScreenY, this.getWidth()/2, this.getHeight());
				outText = "Position:" + String.valueOf(this.privateArea.currentDragPosition);
			}
			else{
				this.privateArea.currentDragPosition = -1;
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		/*Translate the board if alt is down*/
		if (arg0.isAltDown() && (SwingUtilities.isLeftMouseButton(arg0)))
		{
			//updatePlayerMousePos(arg0);
			translateBoard(arg0);
			boardTranslation = true;
		}
		else
		{
			if (player != null) {
				ObjectFunctions.getOwnedStack(gameInstance, player, ial);
				StringBuilder strB = new StringBuilder();
				for (int id : ial) {
					strB.append(id).append(' ');
				}
				outText = strB.toString();

				updatePlayerMousePos(arg0);
				MoveFunctions.dragObjects(this,gameInstance,player,arg0,selectedObjects,objOrigPosX,objOrigPosY,mousePressedGamePos,mouseBoardPos,mouseWheelValue);
				/*Handle all drags of Token Objects*/
				if (this.privateArea.containsScreenCoordinates(mouseScreenX, mouseScreenY)){
					this.privateArea.currentDragPosition = this.privateArea.getInsertPosition(mouseScreenX, mouseScreenY, this.getWidth()/2, this.getHeight());
					outText = "Position:" + String.valueOf(this.privateArea.currentDragPosition);
				}
				else{
					this.privateArea.currentDragPosition = -1;
				}
				if (hoveredObject == null && selectedObjects.size() == 0 && !SwingUtilities.isMiddleMouseButton(arg0) && !(SwingUtilities.isLeftMouseButton(arg0) && arg0.isShiftDown()) && !mouseInPrivateArea) {
					selectWidth = mouseScreenX - beginSelectPosScreenX;
					selectHeight = mouseScreenY - beginSelectPosScreenY;
				}

			}
			else {
				return;
			}
		}
		repaint();
	}


	@Override
	public void mouseReleased(MouseEvent arg0) {
		if (allMouseKeysReleased(arg0)) {
            boardTranslation = false;
			firstMouseClick = -1;
			if (player == null) {
				return;
			}
			if (selectedObjects.size() > 0 && (SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isRightMouseButton(arg0) || SwingUtilities.isMiddleMouseButton(arg0))) {
				for (int id : ObjectFunctions.getStackRepresentatives(gameInstance, selectedObjects)) {
					ObjectInstance oi = gameInstance.getObjectInstanceById(id);
					ObjectFunctions.setNewDrawValue(this.id, gameInstance, player, oi);
					ObjectFunctions.releaseObjects(arg0, this, gameInstance, player, oi, mouseScreenX, mouseScreenY, 1);
					if (oi.go instanceof GameObjectDice && SwingUtilities.isMiddleMouseButton(arg0)) {
						ObjectFunctions.rollTheDice(id, gameInstance, player, oi);
					}
				}
			}
			if (isSelectStarted) {
				ObjectFunctions.deselectAllSelected(this, gameInstance, player, selectedObjects);
				ObjectFunctions.getObjectsInsideBox(gameInstance, player, beginSelectPosScreenX, beginSelectPosScreenY, selectWidth, selectHeight, ial, boardToScreenTransformation);
				ObjectFunctions.selectObjects(this, gameInstance, player, ial);
				selectHeight = 0;
				selectWidth = 0;
				beginSelectPosScreenX = 0;
				beginSelectPosScreenY = 0;
				isSelectStarted = false;
			}
			if (hoveredObject != null){
				hoveredObject.state.isActive = false;
			}
			hoveredObject = ObjectFunctions.getNearestObjectByPosition(this, gameInstance, player, mouseBoardPos.getXI(), mouseBoardPos.getYI(), 1, null);
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
		else if (action instanceof GamePlayerEditAction || action instanceof GameStructureEditAction || action instanceof GameObjectEditAction)
		{
			repaint();
		}

		if (action instanceof AddPlayerAction){
			if (table != null) {
				this.table.updatePlayers(gameInstance);
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		e.consume();
		++keyPressed;
		boolean controlDown = e.isControlDown();
		boolean shiftDown = e.isShiftDown();
		boolean altDown = e.isAltDown();

		translateBoard(e);

		if (!(isLeftMouseKeyHold || isRightMouseKeyHold || isMiddleMouseKeyHold)) {
			if (!mouseInPrivateArea) {
			}
			else {
				ObjectFunctions.deselectAllSelected(this, gameInstance, player, ial);
				if (hoveredObject != null) {
					ObjectFunctions.selectObject(this, gameInstance, player, hoveredObject.id);
				}
			}
			if (e.getKeyCode() == KeyEvent.VK_C && !shiftDown) {
				int count = 0;
				for (int oId : selectedObjects) {
					ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
					count += ObjectFunctions.countStack(gameInstance, oi);
				}
				player.actionString = "Object Number: " + String.valueOf(count);
			} else if (e.getKeyCode() == KeyEvent.VK_F && !shiftDown) {
				for (int oId : selectedObjects) {
					ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
					ObjectFunctions.flipTokenObject(id, gameInstance, player, oi);
					ObjectFunctions.rollTheDice(id, gameInstance, player, oi);
				}
			} else if (shiftDown && e.getKeyCode() == KeyEvent.VK_F) {
				for (int oId : ObjectFunctions.getStackRepresentatives(gameInstance, selectedObjects)) {
					ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
					ObjectFunctions.flipTokenStack(id, gameInstance, player, ObjectFunctions.getStackTop(gameInstance, oi));
				}
			} else if (e.getKeyCode() == KeyEvent.VK_S) {
				for (int oId : ObjectFunctions.getStackRepresentatives(gameInstance, selectedObjects)) {
					ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
					ObjectFunctions.shuffleStack(id, gameInstance, player, oi);
				}

			} else if (shiftDown && e.getKeyCode() == KeyEvent.VK_C) {
				int count = 0;
				for (int oId : selectedObjects) {
					ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
					count += ObjectFunctions.countStackValues(gameInstance, oi);
				}
				player.actionString = "Value: " + String.valueOf(count);
			} else if (e.getKeyCode() == KeyEvent.VK_V) {
				if (!mouseInPrivateArea) {
					for (int oId : ObjectFunctions.getStackRepresentatives(gameInstance, selectedObjects)) {
						ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
						if (ObjectFunctions.haveSamePositions(ObjectFunctions.getStackTop(gameInstance, oi), ObjectFunctions.getStackBottom(gameInstance, oi))) {
							ObjectFunctions.displayStack(id, gameInstance, player, oi, (int) (oi.getWidth(player.id) * cardOverlap));
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
					for(int oId : selectedObjects) {
						ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
						if (oi != null && oi.go instanceof GameObjectDice) {
							GameObjectDice.DiceState state = (GameObjectDice.DiceState) oi.state;
							state.unfold = !state.unfold;
						}
					}

				} else {
					ObjectFunctions.getOwnedStack(gameInstance, player, ial);
					if (ial.size() > 0) {
						ObjectInstance oi = gameInstance.getObjectInstanceById(ial.getI(0));
						if (ObjectFunctions.haveSamePositions(ObjectFunctions.getStackTop(gameInstance, oi), ObjectFunctions.getStackBottom(gameInstance, oi))) {
							ObjectFunctions.displayStack(id, gameInstance, player, oi, (int) (oi.getWidth(player.id) * cardOverlap));
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
				for (int oId : selectedObjects) {
					ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
					ObjectFunctions.dropObjects(this, gameInstance, player, oi);
				}
			} else if (e.getKeyCode() == KeyEvent.VK_D && !shiftDown) {
				ObjectFunctions.dropObject(this, gameInstance, player, hoveredObject);
			} else if (e.getKeyCode() == KeyEvent.VK_P && !shiftDown) {
				if (hoveredObject.state.inPrivateArea) {
					ObjectFunctions.playObject(this, gameInstance, player, hoveredObject);
				}
			} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {

				int place = 0;
				for (int i = 0; i < gameInstance.getPlayerNumber(); ++i) {
					if (gameInstance.getPlayerByIndex(i).id < player.id) {
						++place;
					}
				}
				sitDown(player, place);
			}
			else if (e.getKeyCode() == KeyEvent.VK_H && altDown){
				if (privateArea.zooming == 0) {
					privateArea.zooming = 1;
				}
				else{
					privateArea.zooming = 0;
					updateGameTransform();
				}
			}
			else if (e.getKeyCode() == KeyEvent.VK_T && altDown){
				isTableVisible = !isTableVisible;
			}


			if (!mouseInPrivateArea) {
				if (e.getKeyCode() == KeyEvent.VK_R && !shiftDown) {
					for (int oId : ObjectFunctions.getStackRepresentatives(gameInstance, selectedObjects)) {
						ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
						ObjectFunctions.rotateStep(id, gameInstance, player, oi, ial);
					}
					repaint();
				}
				else if (e.getKeyCode() == KeyEvent.VK_R && shiftDown) {
					for (int oId : selectedObjects) {
						ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
						ObjectFunctions.removeStackRelations(id, gameInstance, player, oi);
					}
				}
				else if (e.getKeyCode() == KeyEvent.VK_T && !altDown) {
					for (int oId : ObjectFunctions.getStackRepresentatives(gameInstance, selectedObjects)) {
						ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
						ObjectFunctions.takeObjects(this, gameInstance, player, oi);
					}
					ObjectFunctions.deselectAllSelected(this, gameInstance, player, ial);
				}
				else if (e.getKeyCode() == KeyEvent.VK_M && shiftDown) {
					for (int oId : selectedObjects) {
						ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
						ObjectFunctions.getAllObjectsOfGroup(id, gameInstance, player, oi);
					}
				}
				else if (e.getKeyCode() == KeyEvent.VK_M && !shiftDown) {
					ObjectFunctions.makeStack(id, gameInstance, player, selectedObjects);
				}
				else if (altDown && !boardTranslation && scaledObjects.size() > 0) {
					for (int oId : ObjectFunctions.getStackRepresentatives(gameInstance, selectedObjects)) {
						ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
						if (!scaledObjects.contains(oi.id)) {
							scaledObjects.add(oi.id);
							savedScalingFactors.add(oi.scale);
							oi.scale *= scalingFactor;
						}
					}
					repaint();
				}
				else if (e.getKeyCode() == KeyEvent.VK_F && altDown)
				{
					for (int oId : selectedObjects) {
						ObjectInstance oi = gameInstance.getObjectInstanceById(oId);
						ObjectFunctions.fixObject(id, gameInstance,player,oi);
					}
				}
			}
		}
	}


	@Override
	public void keyReleased(KeyEvent e) {
		--keyPressed;

		if (!mouseInPrivateArea) {
			if (e.getKeyCode() == KeyEvent.VK_ALT && scaledObjects.size() > 0) {
				for (int i = 0; i < scaledObjects.size(); i++) {
					gameInstance.getObjectInstanceById(scaledObjects.get(i)).scale = savedScalingFactors.get(i);
				}
				scaledObjects.clear();
				savedScalingFactors.clear();
				scalingFactor = originalScalingFactor;
			}
			else {
				if (!(isLeftMouseKeyHold || isRightMouseKeyHold || isMiddleMouseKeyHold) && hoveredObject == null) {
					if (hoveredObject != null) {
						hoveredObject.state.isActive = false;
					}
					hoveredObject = null;
				}
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
		if (e.isAltDown() && !mouseInPrivateArea && hoveredObject != null){
			outText = String.valueOf(e.getPreciseWheelRotation());
			double scale = 1.1;
			if((int) e.getPreciseWheelRotation() < 0) {
				scale = 0.9;
			}
			for (int id : scaledObjects){
				gameInstance.getObjectInstanceById(id).scale *= scale;
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
		}else if (e.isAltDown() && mouseInPrivateArea)
		{
			privateArea.zoomingFactor += (int) e.getPreciseWheelRotation();
			if ((privateArea.zooming >= 0.5 || e.getPreciseWheelRotation() < 0) && (privateArea.zooming < 2 || e.getPreciseWheelRotation() > 0)) {
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
		mouseInPrivateArea = ObjectFunctions.isInPrivateArea(this, mouseBoardPos.getXI(), mouseBoardPos.getYI());
		gameInstance.update(new GamePlayerEditAction(id, player, player));
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {}

	@Override
	public void componentHidden(ComponentEvent arg0) {}

	@Override
	public void componentMoved(ComponentEvent arg0) {}

	@Override
	public void componentResized(ComponentEvent arg0) {
		updateGameTransform();
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
		updateGameTransform();
	}

	public void updateGameTransform()
	{
		gameTransform.setIdentity();
		gameTransform.affineTranslate(-getWidth() / 2, -getHeight() / 2);
		gameTransform.scale(1 / zooming);
		gameTransform.rotateZ(rotation);
		gameTransform.affineTranslate(-translateX, -translateY);
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
		if (player != null)
		{
			player.screenToBoardTransformation.setTransform(screenToBoardTransformation);
			player.screenWidth = getWidth();
			player.screenHeight = getHeight();
			gameInstance.update(new GamePlayerEditAction(id, player, player));
		}
	}

	public AffineTransform getBoardToScreenTransform(){
		return boardToScreenTransformation;
	}

	public void screenToBoardPos(int x, int y, Vector2d out)
	{
		gameTransform.transformAffine(x, y, out);
	}
	public void boardToScreenPos(Point2D boardCoordinates, Point2D screenCooardinates)
	{
		boardToScreenTransformation.transform(boardCoordinates, screenCooardinates);
	}

	public void translateBoard(KeyEvent keyEvent){
		double tmpX = 0;
		double tmpY = 0;
		boolean isTranslation = false;
		if (keyEvent.getKeyCode() == KeyEvent.VK_LEFT){
			tmpX = -50./zooming;
			isTranslation = true;
		}else if (keyEvent.getKeyCode() == KeyEvent.VK_RIGHT){
			tmpX = 50./zooming;
			isTranslation = true;
		}
		if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN){
			tmpY = 50./zooming;
			isTranslation = true;
		}else if (keyEvent.getKeyCode() == KeyEvent.VK_UP){
			tmpY = -50./zooming;
			isTranslation = true;
		}

		if (keyEvent.getKeyCode() == KeyEvent.VK_PLUS || keyEvent.getKeyCode() == KeyEvent.VK_ADD){
			zoomFactor -= 1;
			zooming = Math.exp(-zoomFactor * 0.1);
			updateGameTransform();
		}else if (keyEvent.getKeyCode() == KeyEvent.VK_MINUS || keyEvent.getKeyCode() == KeyEvent.VK_SUBTRACT){
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
			player.setMousePos(mouseBoardPos.getXI(), mouseBoardPos.getYI());
			gameInstance.update(new GamePlayerEditAction(id, player, player));
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
		gameInstance.update(new GamePlayerEditAction(id, player, player));
	}

	class SheetPanel extends JPanel implements LanguageChangeListener{
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
			add(new JLabel(lang.getString(Words.sit_down) 					+ ": " + new ControlCombination(0, -1,  KeyEvent.VK_ENTER, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.move_top_card) 				+ ": " + new ControlCombination(0, 0,  -1, 1).toString(lang)));
			add(new JLabel(lang.getString(Words.move_stack) 				+ ": " + new ControlCombination(0, 1, -1, 1).toString(lang)));
			add(new JLabel(lang.getString(Words.take_objects_to_hand)	 	+ ": " + new ControlCombination(0, -1,  KeyEvent.VK_T, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.drop_all_hand_cards) + ": " + new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1,  KeyEvent.VK_D, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.get_bottom_card) 			+ ": " + new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1,  -1, 2).toString(lang)));
			add(new JLabel(lang.getString(Words.shuffle_stack) 				+ ": " + new ControlCombination(0, -1,  KeyEvent.VK_S, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.flip_card)					+ ": " + new ControlCombination(0, -1,  KeyEvent.VK_F, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.flip_stack)					+ ": " + new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1,  KeyEvent.VK_F, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.view_collect_stack)	 		+ ": " + new ControlCombination(0, -1,  KeyEvent.VK_V, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.collect_selected_objects)	+ ": " + new ControlCombination(0, -1,  KeyEvent.VK_M, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.collect_all_objects)  		+ ": " + new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1,  KeyEvent.VK_M, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.rotate_object)  			+ ": " + new ControlCombination(0, -1,  KeyEvent.VK_R, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.dissolve_stack)				+ ": " + new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1,  KeyEvent.VK_R, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.count_objects)				+ ": " + new ControlCombination(0, -1,  KeyEvent.VK_C, 0).toString(lang)));
			add(new JLabel(lang.getString(Words.count_values)				+ ": " + new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1,  KeyEvent.VK_C, 0).toString(lang)));
			revalidate();
		}
	}
	
}
