package gui;

import static gameObjects.functions.DrawFunctions.drawBoard;
import static gameObjects.functions.DrawFunctions.drawObjectsFromList;
import static gameObjects.functions.DrawFunctions.drawPlayerPositions;
import static gameObjects.functions.DrawFunctions.drawPrivateArea;
import static gameObjects.functions.DrawFunctions.drawSelection;
import static gameObjects.functions.DrawFunctions.drawTokensInPrivateArea;

import java.awt.*;
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
import javax.swing.*;

import gameObjects.action.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.ControlCombination;
import data.DataHandler;
import data.SystemFileUtil;
import gameObjects.definition.GameObjectDice;
import gameObjects.functions.MoveFunctions;
import gameObjects.functions.ObjectFunctions;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
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
	GameInstance gameInstance;
	public ObjectInstance activeObject = null;
	public ArrayList<ObjectInstance> activeObjects = new ArrayList<>();
	IntegerArrayList objOrigPosX = new IntegerArrayList();
	IntegerArrayList objOrigPosY = new IntegerArrayList();
	public Player player;
	public final int id = (int)System.nanoTime();
	private final IntegerArrayList ial = new IntegerArrayList();

	int maxInaccuracy = 20;

	boolean isLeftMouseKeyHold = false;
	boolean isRightMouseKeyHold = false;
	boolean isMiddleMouseKeyHold = false;

	//-1 no key, 0 left, 1 middle, 2 right
	int firstMouseClick = -1;

	public int mouseScreenX = -1;
	public int mouseScreenY = -1;
	private final Vector2d mouseScreenPos = new Vector2d();
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
	public final IntegerArrayList selectedObjects = new IntegerArrayList();
	public final IntegerArrayList scaledObjects = new IntegerArrayList();
	public final ArrayList<Double> savedScalingFactors = new ArrayList<>();
	private double originalScalingFactor = 3;
	private double scalingFactor = originalScalingFactor;
	public int beginSelectPosScreenX = 0;
	public int beginSelectPosScreenY = 0;
	public int selectWidth = 0;
	public int selectHeight = 0;

	public String outText = "";

	public PrivateArea privateArea;
	public Table table = null;
	public int tableDiameter = 1500;
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
		this.privateArea = new PrivateArea(this, gameInstance, boardToScreenTransformation, screenToBoardTransformation);
		this.table = new Table(gameInstance, tableDiameter,new Point2D.Double(-tableDiameter/2,-tableDiameter/2));
		this.gameInstance = gameInstance;

		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		addMouseWheelListener(this);
		setFocusable(true);
		gameInstance.addChangeListener(this);
	
		// This is the cheat sheet showing the user how he can interact with the game board
		SheetPanel shP = new SheetPanel(new BorderLayout());
		lh.addLanguageChangeListener(shP);
		shP.languageChanged(lh.getCurrentLanguage());
		add(shP);


		
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
		drawBoard(this, g, gameInstance);
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
		//Redraw active objects not in some private area
		drawObjectsFromList(this, g, gameInstance, player, activeObjects, ial);

		//Draw all player related information
		drawPlayerPositions(this, g, gameInstance, player, infoText);

		g2.setTransform(tmp);
		//Draw objects in private area
		drawTokensInPrivateArea(this, g, gameInstance, player, activeObject);
		g.drawString(outText, 50, 50);
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
		mouseInPrivateArea = ObjectFunctions.isInPrivateArea(this, mouseBoardPos.getXI(), mouseBoardPos.getYI());

		if (player != null) {
			//set the mouse position of the player to send to other players
			player.setMousePos(mouseBoardPos.getXI(), mouseBoardPos.getYI());
			gameInstance.update(new GamePlayerEditAction(id, player, player));
			//get nearest object concerning the mouse position
			ObjectInstance nearestObject = ObjectFunctions.getNearestObjectByPosition(this, gameInstance, player, mouseBoardPos.getXI(), mouseBoardPos.getYI(), 1, null);
			//do actions if mouse is in the private area or not and there is some nearest object
			if (mouseInPrivateArea && nearestObject != null && !arg0.isControlDown())
			{
				if (activeObject != null){
					activeObject.state.isActive = false;
				}
				nearestObject.state.isActive = false;
				activeObject = nearestObject;
				outText = String.valueOf(mouseScreenY);
				outText = "OId: " + String.valueOf(activeObject.id) + " StackPos: " + String.valueOf(ObjectFunctions.getStackIdOfObject(gameInstance,activeObject, ial));
			}else if (!mouseInPrivateArea){
				if (activeObject != null) {
					activeObject.state.isActive = false;
					outText = "OId: " + String.valueOf(activeObject.id);
				}
				if (nearestObject != null) {
					activeObject = nearestObject;
					activeObject.state.isActive = true;
				} else {
					if (activeObject != null) {
						activeObject.state.isActive = false;
						activeObject = null;
					}
				}
			}
		}
		repaint();
	}


	@Override
	public void mouseClicked(MouseEvent arg0) {
		if (arg0.getClickCount() == 2) {
			int posPlayer = gameInstance.getPlayerList().indexOf(player);
			for (int i = 0; i < this.table.playerShapes.size();++i) {
				if(this.table.playerShapes.get(i).contains(mouseScreenX, mouseScreenY) && i == posPlayer) {
					sitDown(i);
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

	private void sitDown(int pos){
		translateX = 0;
		translateY = 0;
		zooming = getHeight()/ ((float) this.table.getDiameter() + 200);
		rotation = 0;
		updateGameTransform();
		rotation = Math.toRadians(- 360/this.table.playerShapes.size() * pos);
		updateGameTransform();
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

			mouseScreenX = arg0.getX();
			mouseScreenY = arg0.getY();
			screenToBoardPos(arg0.getX(), arg0.getY(), mousePressedGamePos);
			mouseBoardPos.set(mousePressedGamePos);
			mouseInPrivateArea = ObjectFunctions.isInPrivateArea(this, mouseBoardPos.getXI(), mouseBoardPos.getYI());
			if (!isSelectStarted && activeObject == null) {
				beginSelectPosScreenX = arg0.getX();
				beginSelectPosScreenY = arg0.getY();
				isSelectStarted = true;
			} else {
				for (int i : selectedObjects) {
					gameInstance.getObjectInstanceById(i).state.isActive = false;
				}
				selectedObjects.clear();
			}
			setActiveObjects();
			if (activeObject != null && this.privateArea.contains(activeObject.id)) {
				ObjectFunctions.setNewDrawValue(this.id, gameInstance, player, activeObject);
				gameInstance.update(new GameObjectInstanceEditAction(id, player, activeObject));

				activeObject.state.posX = player.mouseXPos - activeObject.getWidth(player.id) / 2;
				activeObject.state.posY = player.mouseYPos - activeObject.getHeight(player.id) / 2;
			}
			if (activeObjects.size() > 0) {
				objOrigPosX.clear();
				objOrigPosY.clear();
				for (ObjectInstance oi : activeObjects) {
					objOrigPosX.add(oi.state.posX);
					objOrigPosY.add(oi.state.posY);
					oi.state.isActive = true;
				}
			}
			//Handle all drags of Objects
			MoveFunctions.dragObjects(this, gameInstance, player, arg0, activeObjects, objOrigPosX, objOrigPosY, mousePressedGamePos, mouseBoardPos, mouseWheelValue);
			if (this.privateArea.containsScreenCoordinates(mouseScreenX, mouseScreenY)) {
				this.privateArea.currentDragPosition = this.privateArea.getInsertPosition(mouseScreenX, mouseScreenY);
				outText = "Position:" + String.valueOf(this.privateArea.currentDragPosition);
			}
			else{
				this.privateArea.currentDragPosition = -1;
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		/*Translate the board if control is down*/
		if ((arg0.isControlDown() || (SwingUtilities.isMiddleMouseButton(arg0) && activeObjects.size() == 0)) && !mouseInPrivateArea)
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


				MoveFunctions.dragObjects(this,gameInstance,player,arg0,activeObjects,objOrigPosX,objOrigPosY,mousePressedGamePos,mouseBoardPos,mouseWheelValue);
				/*Handle all drags of Token Objects*/
				if (this.privateArea.containsScreenCoordinates(mouseScreenX, mouseScreenY)){
					this.privateArea.currentDragPosition = this.privateArea.getInsertPosition(mouseScreenX, mouseScreenY);
					outText = "Position:" + String.valueOf(this.privateArea.currentDragPosition);
				}
				else{
					this.privateArea.currentDragPosition = -1;
				}
				if (activeObject == null && selectedObjects.size() == 0 && !SwingUtilities.isMiddleMouseButton(arg0) && !mouseInPrivateArea) {
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
			if (activeObjects.size() > 0 && (SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isRightMouseButton(arg0) || SwingUtilities.isMiddleMouseButton(arg0))) {
				for (ObjectInstance oi : ObjectFunctions.getStackRepresentatives(gameInstance, activeObjects)) {
					ObjectFunctions.setNewDrawValue(this.id, gameInstance, player, oi);
					ObjectFunctions.releaseObjects(arg0, this, gameInstance, player, oi, mouseScreenX, mouseScreenY, 1);
					if (oi.go instanceof GameObjectDice && SwingUtilities.isMiddleMouseButton(arg0)) {
						ObjectFunctions.rollTheDice(id, gameInstance, player, oi);
					}
				}
			}
			if (isSelectStarted) {
				selectedObjects.clear();
				ObjectFunctions.getObjectsInsideBox(gameInstance, player, beginSelectPosScreenX, beginSelectPosScreenY, selectWidth, selectHeight, selectedObjects, boardToScreenTransformation);
				selectHeight = 0;
				selectWidth = 0;
				beginSelectPosScreenX = 0;
				beginSelectPosScreenY = 0;
				isSelectStarted = false;
			}
			if (activeObject != null){
				activeObject.state.isActive = false;
			}
			activeObject = null;
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
		boolean controlDown = e.isControlDown();
		boolean shiftDown = e.isShiftDown();

		translateBoard(e);

		if (!mouseInPrivateArea) {
			setActiveObjects();
		}
		else {
			activeObjects.clear();
			if (activeObject != null) {
				activeObjects.add(activeObject);
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_C && !shiftDown) {
			int count = 0;
			for (ObjectInstance oi : activeObjects) {
				count += ObjectFunctions.countStack(gameInstance, oi);
			}
			player.actionString = "Object Number: " + String.valueOf(count);
		} else if (e.getKeyCode() == KeyEvent.VK_F && !shiftDown) {
			for (ObjectInstance oi : activeObjects) {
				ObjectFunctions.flipTokenObject(id, gameInstance, player, oi);
				ObjectFunctions.rollTheDice(id, gameInstance, player, oi);
			}
		} else if (shiftDown && e.getKeyCode() == KeyEvent.VK_F) {
			for (ObjectInstance oi : ObjectFunctions.getStackRepresentatives(gameInstance, activeObjects)) {
				ObjectFunctions.flipTokenStack(id, gameInstance, player, ObjectFunctions.getStackTop(gameInstance, oi));
			}
		} else if (e.getKeyCode() == KeyEvent.VK_S) {
			for (ObjectInstance oi : ObjectFunctions.getStackRepresentatives(gameInstance, activeObjects)) {
				ObjectFunctions.shuffleStack(id, gameInstance, player, oi);
			}

		} else if (shiftDown && e.getKeyCode() == KeyEvent.VK_C) {
			int count = 0;
			for (ObjectInstance oi : activeObjects) {
				count += ObjectFunctions.countStackValues(gameInstance, oi);
			}
			player.actionString = "Value: " + String.valueOf(count);
		} else if (e.getKeyCode() == KeyEvent.VK_V) {
			if (!mouseInPrivateArea) {
				for (ObjectInstance oi : ObjectFunctions.getStackRepresentatives(gameInstance, activeObjects)) {
					if (ObjectFunctions.haveSamePositions(ObjectFunctions.getStackTop(gameInstance, oi), ObjectFunctions.getStackBottom(gameInstance, oi))) {
						ObjectFunctions.displayStack(id, gameInstance, player, oi, (int) (oi.getWidth(player.id) * cardOverlap));
					} else {
						if (activeObjects.size() == 1) {
							ObjectFunctions.collectStack(id, gameInstance, player, activeObjects.get(0));
						} else {
							ObjectFunctions.collectStack(id, gameInstance, player, oi);
						}
					}
				}
			} else {
				ObjectFunctions.getOwnedStack(gameInstance, player, ial);
				if (ial.size() > 0) {
					ObjectInstance oi = gameInstance.getObjectInstanceById(ial.getI(0));
					if (ObjectFunctions.haveSamePositions(ObjectFunctions.getStackTop(gameInstance, oi), ObjectFunctions.getStackBottom(gameInstance, oi))) {
						ObjectFunctions.displayStack(id, gameInstance, player, oi, (int) (oi.getWidth(player.id) * cardOverlap));
					} else {
						if (activeObjects.size() == 1) {
							ObjectFunctions.collectStack(id, gameInstance, player, activeObjects.get(0));
						} else {
							ObjectFunctions.collectStack(id, gameInstance, player, oi);
						}
					}
				}
			}
		} else if (e.getKeyCode() == KeyEvent.VK_D) {
			for (ObjectInstance oi : activeObjects) {
				ObjectFunctions.dropObjects(this, gameInstance, player, oi);
			}

		}else if (controlDown && !boardTranslation){
			for (ObjectInstance oi : ObjectFunctions.getStackRepresentatives(gameInstance, activeObjects)){
				if(!scaledObjects.contains(oi.id)){
					scaledObjects.add(oi.id);
					savedScalingFactors.add(oi.scale);
					oi.scale *= scalingFactor;
				}
			}
		}else if (e.getKeyCode() == KeyEvent.VK_ENTER){
			int posPlayer = gameInstance.getPlayerList().indexOf(player);
			if (0<=posPlayer && this.table != null && posPlayer<this.table.playerShapes.size()){
				sitDown(posPlayer);
			}
		}


		if(!mouseInPrivateArea) {
			if (e.getKeyCode() == KeyEvent.VK_R) {
				for (ObjectInstance oi : ObjectFunctions.getStackRepresentatives(gameInstance, activeObjects)) {
					ObjectFunctions.rotateStep(id, gameInstance, player, oi, ial);
				}
				repaint();
			} else if (e.getKeyCode() == KeyEvent.VK_R && controlDown) {
				for (ObjectInstance oi : activeObjects) {
					ObjectFunctions.removeStackRelations(id, gameInstance, player, oi);
				}
			} else if (e.getKeyCode() == KeyEvent.VK_T) {
				for (ObjectInstance oi : activeObjects) {
					ObjectFunctions.takeObjects(this, gameInstance, player, oi);
					oi.state.isActive = false;
				}
				selectedObjects.clear();
				activeObjects.clear();
			} else if (e.getKeyCode() == KeyEvent.VK_Y && controlDown) {
				for (ObjectInstance oi : activeObjects) {
					ObjectFunctions.getAllObjectsOfGroup(id, gameInstance, player, oi);
				}
			} else if (e.getKeyCode() == KeyEvent.VK_Y && !controlDown) {
				ObjectFunctions.makeStack(id, gameInstance, player, selectedObjects);
			}
		}
	}


	@Override
	public void keyReleased(KeyEvent e) {
		if (!mouseInPrivateArea) {
			if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
				for (int i = 0; i < scaledObjects.size(); i++) {
					gameInstance.getObjectInstanceById(scaledObjects.get(i)).scale = savedScalingFactors.get(i);
				}
				scaledObjects.clear();
				savedScalingFactors.clear();
				scalingFactor = originalScalingFactor;
			} else {
				if (!isLeftMouseKeyHold) {
					if (activeObject != null) {
						activeObject.state.isActive = false;
					}
					activeObject = null;
				}
				if (!isRightMouseKeyHold) {
					if (activeObject != null) {
						activeObject.state.isActive = false;
					}
					activeObject = null;
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
		if (e.isControlDown() && !mouseInPrivateArea && activeObject != null){
			outText = String.valueOf(e.getPreciseWheelRotation());
			double scale = 1.1;
			if((int) e.getPreciseWheelRotation() < 0) {
				scale = 0.9;
			}
			for (int id : scaledObjects){
				gameInstance.getObjectInstanceById(id).scale *= scale;
			}
		}
		else if (e.isControlDown() && !mouseInPrivateArea)
		{
			zoomFactor += (int) e.getPreciseWheelRotation();
			zooming = Math.exp(-zoomFactor * 0.1);
			updateGameTransform();
		}
		else{
			mouseWheelValue += (int) e.getPreciseWheelRotation();
		}
		if(mouseWheelValue <= 0) {
			mouseWheelValue = 0;
			infoText = "";
		}
		else
			infoText = String.valueOf(mouseWheelValue);

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
		//move own stack to private bottom
		if (player != null) {
			ObjectFunctions.getOwnedStack(gameInstance,player,ial);
			if (ial.size() >0) {
				ObjectInstance oi = gameInstance.getObjectInstanceById(ial.get(0));
				if (oi.state.owner_id == player.id) {
					Point2D targetPoint = new Point2D.Double(0, 0);
					player.playerAtTableTransform.transform(targetPoint,targetPoint);
					ObjectFunctions.rotateStack(gameInstance, ial, player.playerAtTableRotation);
					ObjectFunctions.moveStackTo(id, gameInstance, player, ial, (int) targetPoint.getX(), (int) targetPoint.getY());
				}
			}
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
			rotation += (arg0.getX() - mouseScreenX) / 200.;
		}
		updateGameTransform();
		screenToBoardPos(mouseScreenX = arg0.getX(), mouseScreenY = arg0.getY(), mouseBoardPos);
		player.setMousePos(mouseBoardPos.getXI(), mouseBoardPos.getYI());
		gameInstance.update(new GamePlayerEditAction(id, player, player));
	}

	/** Get active or selected objects
	 *
	 */
	public void setActiveObjects(){
		if (activeObject != null){
			activeObject.state.isActive = false;
		}
		activeObject = ObjectFunctions.getNearestObjectByPosition(this, gameInstance, player, mouseBoardPos.getXI(), mouseBoardPos.getYI(), 1, null);
		activeObjects.clear();
		if (selectedObjects.size() > 0){
			for (int id:selectedObjects){
				activeObjects.add(gameInstance.getObjectInstanceById(id));
			}
		}
		else {
			if (activeObject != null) {
				activeObjects.add(activeObject);
			}
		}

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
			add(new JLabel(lang.getString(Words.sit_down) 				+ ": " + new ControlCombination(0, -1, 'E', 0).toString(lang)));
			add(new JLabel(lang.getString(Words.move_top_card) 				+ ": " + new ControlCombination(0, 0, '\0', 1).toString(lang)));
			add(new JLabel(lang.getString(Words.move_stack) 				+ ": " + new ControlCombination(0, 1, '\0', 1).toString(lang)));
			add(new JLabel(lang.getString(Words.take_objects_to_hand)	 	+ ": " + new ControlCombination(0, -1, 'T', 0).toString(lang)));
			add(new JLabel(lang.getString(Words.drop_all_objects_from_hand) + ": " + new ControlCombination(0, -1, 'D', 0).toString(lang)));
			add(new JLabel(lang.getString(Words.get_bottom_card) 			+ ": " + new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1, '\0', 2).toString(lang)));
			add(new JLabel(lang.getString(Words.shuffle_stack) 				+ ": " + new ControlCombination(0, -1, 'S', 0).toString(lang)));
			add(new JLabel(lang.getString(Words.flip_card)					+ ": " + new ControlCombination(0, -1, 'F', 0).toString(lang)));
			add(new JLabel(lang.getString(Words.flip_stack)					+ ": " + new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1, 'F', 0).toString(lang)));
			add(new JLabel(lang.getString(Words.view_collect_stack)	 		+ ": " + new ControlCombination(0, -1, 'V', 0).toString(lang)));
			add(new JLabel(lang.getString(Words.collect_selected_objects)	+ ": " + new ControlCombination(0, -1, 'Y', 0).toString(lang)));
			add(new JLabel(lang.getString(Words.collect_all_objects)  		+ ": " + new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1, 'Y', 0).toString(lang)));
			add(new JLabel(lang.getString(Words.rotate_object)  			+ ": " + new ControlCombination(0, -1, 'R', 0).toString(lang)));
			add(new JLabel(lang.getString(Words.dissolve_stack)				+ ": " + new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1, 'R', 0).toString(lang)));
			add(new JLabel(lang.getString(Words.count_objects)				+ ": " + new ControlCombination(0, -1, 'C', 0).toString(lang)));
			add(new JLabel(lang.getString(Words.count_values)				+ ": " + new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1, 'C', 0).toString(lang)));
			revalidate();
		}
	}
	
}
