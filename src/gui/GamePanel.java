package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.GameAction;
import gameObjects.GameObjectInstanceEditAction;
import gameObjects.GamePlayerEditAction;
import gameObjects.functions.ObjectFunctions;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import main.Player;
import util.data.IntegerArrayList;

//import gameObjects.GameObjectInstanceEditAction;

public class GamePanel extends JPanel implements MouseListener, MouseMotionListener, GameInstance.GameChangeListener, KeyListener, KeyEventDispatcher, MouseWheelListener, ActionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3579141032474558913L;
	private static final Logger logger = LoggerFactory.getLogger(GamePanel.class);
	GameInstance gameInstance;
	ObjectInstance activeObject = null;
	int pressedXPos = -1;
	int pressedYPos = -1;
	int objOrigPosX = -1;
	int objOrigPosY = -1;
	public Player player;
	public final int id = (int)System.nanoTime();

	int maxInaccuracy = 20;

	boolean[] loggedKeys = new boolean[1024];

	Boolean isControlDown = false;
	Boolean isShiftDown = false;

	Boolean isLeftMouseKeyHold = false;

	int mouseX = -1;
	int mouseY = -1;

	int mouseWheelValue = 0;
	int zoomFactor = 0;
	int translateX = 0;
	int translateY = 0;
	double zooming = 1;
	double rotation = 0;


	ControlPanel controlPanel = new ControlPanel();

	Color mouseColor = Color.black;
	Color dragColor = Color.red;
	String infoText = "";

	boolean isSelectStarted = false;
	IntegerArrayList selectedObjects = new IntegerArrayList();
	int beginSelectPosX = 0;
	int beginSelectPosY = 0;
	int selectWidth = 0;
	int selectHeight = 0;
	private Color selectColor = Color.blue;


	public GamePanel(GameInstance gameInstance)
	{
		this.gameInstance = gameInstance;
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		addMouseWheelListener(this);
		setFocusable(true);
		gameInstance.changeListener.add(this);

		// This is the cheat sheet showing the user how he can interact with the game board
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(4, 4, 20, 0));
		p.add(new JLabel("Move Top Card: Left Click + Drag"));
		p.add(new JLabel("Move Stack: Middle Click + Drag"));
		p.add(new JLabel("Take Object: T"));
		p.add(new JLabel("Drop Object: D"));
		p.add(new JLabel("Get Bottom Card: Shift + Grab"));
		p.add(new JLabel("Shuffle Stack: S"));
		p.add(new JLabel("Flip Card: F"));
		p.add(new JLabel("Flip Stack: Strg + F"));
		p.add(new JLabel("View + Collect Stack: V"));
		p.add(new JLabel("Collect Selected Objects: M"));
		p.add(new JLabel("Collect All Objects: Strg + M"));
		p.add(new JLabel("Remove Stack: R"));
		p.add(new JLabel("Count Objects: C"));
		p.add(new JLabel("Count Values: Strg + C"));
		this.add(p);
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		//TODO Florian:sometimes images are drawn twice (the active object?)
		((Graphics2D)g).rotate(rotation);
		((Graphics2D)g).scale(zooming, zooming);
		((Graphics2D)g).translate(translateX, translateY);
		g.drawString(String.valueOf(mouseWheelValue), mouseX, mouseY);
		g.drawImage(gameInstance.game.background, 0, 0, getWidth(), getHeight(), Color.BLACK, null);
		int playerid = player == null ? -1 : player.id;
		for (int i = 0; i < gameInstance.objects.size(); ++i) {
			ObjectInstance oi = gameInstance.objects.get(i);
			if (ObjectFunctions.isStackBottom(oi)) {
				ObjectFunctions.drawStack(g, ObjectFunctions.getAboveStack(gameInstance, oi), gameInstance, playerid, 1, logger);
				int playerId = ObjectFunctions.getStackOwner(gameInstance, ObjectFunctions.getStack(gameInstance, oi));
				if (playerId != -1) {
					Player p = gameInstance.getPlayer(playerId);
					g.setColor(p.color);
					ObjectFunctions.drawStackBorder(gameInstance, g, p, oi, 10, 1);
				}
			}
		}
		if(activeObject != null) {
			ObjectFunctions.drawObject(g, activeObject, playerid, 1, logger);
			if (player != null)
			{
				g.setColor(player.color);
				ObjectFunctions.drawBorder(g, player, activeObject, 10,  1);
			}
		}
		else if (selectWidth > 0 && selectHeight > 0){
			g.setColor(player.color);
			g.drawRect(beginSelectPosX, beginSelectPosY, selectWidth, selectHeight);
		}

		g.setColor(mouseColor);
		for(Player p: gameInstance.players) {
			g.setColor(p.color);
			g.fillRect(p.mouseXPos - 5, p.mouseYPos - 5, 10, 10);
			g.drawString(p.name, p.mouseXPos + 15, p.mouseYPos + 5);
			g.drawString(p.actionString, p.mouseXPos - 5, p.mouseYPos - 20);
			//g.drawString(p.name, p.mouseXPos, p.mouseYPos);
			ObjectFunctions.drawBorder(g, p, ObjectFunctions.getNearestObjectByPosition(gameInstance, p, p.mouseXPos, p.mouseYPos, 1, null), 10, 1);
		}
		if (player != null)
		{
			g.setColor(player.color);
			g.drawString(infoText, player.mouseXPos - 25, player.mouseYPos + 5);
		}


		for(int id: selectedObjects)
		{
			ObjectInstance currentObject = gameInstance.objects.get(id);
			if (ObjectFunctions.isStackBottom(currentObject))
			{
				ObjectFunctions.drawStackBorder(gameInstance, g, player, currentObject, 5, (int) zooming);
			}
		}


	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		/* Right Mouse Click on Object */
		if(SwingUtilities.isRightMouseButton(arg0))
		{
			pressedXPos = arg0.getX();
			pressedYPos = arg0.getY();
			activeObject = ObjectFunctions.setActiveObjectByMouseAndKey(gameInstance, arg0, loggedKeys, maxInaccuracy);
			/*Show popup menu of active object*/
			if (activeObject!=null) {
				activeObject.newObjectActionMenu(gameInstance, player, this).showPopup(arg0);
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {
		isLeftMouseKeyHold = true;
		pressedXPos = arg0.getX();
		pressedYPos = arg0.getY();
		if(!isSelectStarted && activeObject == null)
		{
			beginSelectPosX = pressedXPos;
			beginSelectPosY = pressedYPos;
			isSelectStarted = true;
		}

		activeObject = ObjectFunctions.setActiveObjectByMouseAndKey(gameInstance, arg0, loggedKeys, maxInaccuracy);
		if(activeObject != null) {
			objOrigPosX = activeObject.state.posX;
			objOrigPosY = activeObject.state.posY;
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if (player == null)
		{
			return;
		}
		if (activeObject != null && (SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isMiddleMouseButton(arg0))) {
			ObjectFunctions.releaseObjects(id, gameInstance, player, activeObject, zooming);
		}
		activeObject = null;
		mouseX = arg0.getX();
		mouseY = arg0.getY();
		if (player != null) {
			gameInstance.update(new GamePlayerEditAction(id, player, player));
		}
		mouseColor = player.color;
		if(isSelectStarted) {
			selectedObjects = ObjectFunctions.getObjectsInsideBox(gameInstance, beginSelectPosX, beginSelectPosY, selectWidth, selectHeight);
			selectHeight = 0;
			selectWidth = 0;
			beginSelectPosX = 0;
			beginSelectPosY = 0;
			isSelectStarted = false;
		}
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		if (isControlDown)
		{
			translateX += zooming * (arg0.getX() - mouseX);
			translateY += zooming * (arg0.getY() - mouseY);
		}
		if (player == null)
		{
			return;
		}
		/* Drag only when left mouse down */
		if((SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isMiddleMouseButton(arg0)) && !isShiftDown && activeObject != null) {
			/*Drop objects if middle mouse button is not held*/
			if (!SwingUtilities.isMiddleMouseButton(arg0)) {
				ObjectFunctions.removeObject(id, gameInstance, player, activeObject);
			}
			if(mouseWheelValue > 0)
			{
				ObjectFunctions.splitStackAtN(id, gameInstance, player, activeObject, mouseWheelValue - 1);
			}
			ObjectFunctions.moveStackTo(id, gameInstance, player, activeObject, objOrigPosX - pressedXPos + arg0.getX(), objOrigPosY - pressedYPos + arg0.getY());
			if (!ObjectFunctions.isStackCollected(gameInstance, activeObject)) {
				ObjectFunctions.collectStack(id, gameInstance, player, activeObject);
				ObjectFunctions.moveStackTo(id, gameInstance, player, activeObject, objOrigPosX - pressedXPos + arg0.getX(), objOrigPosY - pressedYPos + arg0.getY());
				ObjectFunctions.viewBelowObjects(id, gameInstance, player, ObjectFunctions.getStackTop(gameInstance, activeObject), activeObject.getWidth(player.id)/2);
			}
		}
		else if(SwingUtilities.isLeftMouseButton(arg0) && isShiftDown && activeObject != null) {
			/*Remove top card*/
			ObjectFunctions.removeObject(id, gameInstance, player, activeObject);
			ObjectFunctions.moveObjectTo(id, gameInstance, player, activeObject, objOrigPosX - pressedXPos + arg0.getX(), objOrigPosY - pressedYPos + arg0.getY());
			gameInstance.update(new GameObjectInstanceEditAction(id, player, activeObject));
		}
		mouseX = arg0.getX();
		mouseY = arg0.getY();
		if(activeObject == null) {
			selectWidth = mouseX - beginSelectPosX;
			selectHeight = mouseY - beginSelectPosY;
		}
		if (player != null)
		{
			gameInstance.update(new GamePlayerEditAction(id, player, player));
			player.setMousePos(mouseX, mouseY);
		}
		mouseColor = dragColor;
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		mouseX = (int) (arg0.getX()*zooming);
		mouseY = (int) (arg0.getY()*zooming);
		if (player != null)
		{
			player.setMousePos((int) (mouseX/zooming), (int) (mouseY/zooming));
			gameInstance.update(new GamePlayerEditAction(id, player, player));
			activeObject = ObjectFunctions.getNearestObjectByPosition(gameInstance, player,mouseX, mouseY, zooming, null);
		}
		repaint();
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
		System.out.println(action.source + " " + id);
		if (action instanceof GameObjectInstanceEditAction)
		{
			repaint();
			//If direct repaint causes problems use this:
			//JFrameUtils.runByDispatcher(repaintRunnable);
		}
		else if (action instanceof GamePlayerEditAction)
		{
			repaint();
			System.out.println(((GamePlayerEditAction)action).object);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.isControlDown())
		{
			loggedKeys[e.getKeyCode()] = true;
			isControlDown = true;
		}
		else if(e.isShiftDown())
		{
			loggedKeys[e.getKeyCode()] = true;
			isShiftDown = true;
		}

		if(e.getKeyCode() == KeyEvent.VK_C && !loggedKeys[KeyEvent.VK_CONTROL])
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, mouseX, mouseY);
			int count = ObjectFunctions.countStack(gameInstance, activeObject);
			getGraphics().drawString("Object Number: " + String.valueOf(count), mouseX, mouseY);
		}
		if(e.getKeyCode() == KeyEvent.VK_F && !loggedKeys[KeyEvent.VK_CONTROL])
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getNearestObjectByPosition(gameInstance, player, mouseX, mouseY, zooming, null);
			ObjectFunctions.flipObject(id, gameInstance, player, activeObject);
		}
		if(e.getKeyCode() == KeyEvent.VK_S)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getNearestObjectByPosition(gameInstance, player, mouseX, mouseY, zooming, null);
			ObjectFunctions.shuffleStack(id, gameInstance, player, activeObject);
		}
		if (loggedKeys[KeyEvent.VK_CONTROL] && loggedKeys[KeyEvent.VK_F])
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, mouseX, mouseY);
			ObjectFunctions.flipStack(id, gameInstance, player, activeObject);
		}
		if (loggedKeys[KeyEvent.VK_CONTROL] && loggedKeys[KeyEvent.VK_C])
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, mouseX, mouseY);
			int count = ObjectFunctions.countStackValues(gameInstance, activeObject);
			getGraphics().drawString("Value: " + String.valueOf(count), mouseX, mouseY);
		}

		if (e.getKeyCode() == KeyEvent.VK_V)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getNearestObjectByPosition(gameInstance, player, mouseX, mouseY, zooming, null);
			if (activeObject!= null) {
				if (ObjectFunctions.haveSamePositions(ObjectFunctions.getStackTop(gameInstance, activeObject), ObjectFunctions.getStackBottom(gameInstance, activeObject))) {
					activeObject = ObjectFunctions.getStackTop(gameInstance, activeObject);
					ObjectFunctions.viewBelowObjects(id, gameInstance, player, activeObject, activeObject.getWidth(player.id) / 2);
				} else {
					ObjectFunctions.collectStack(id, gameInstance, player, activeObject);
				}
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_R)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, mouseX, mouseY);
			ObjectFunctions.removeStackRelations(id, gameInstance, player, activeObject);
		}
		if (e.getKeyCode() == KeyEvent.VK_T)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, mouseX, mouseY);
			ObjectFunctions.takeObjects(id, gameInstance, player, activeObject);
		}

		if (e.getKeyCode() == KeyEvent.VK_D)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, mouseX, mouseY);
			ObjectFunctions.dropObjects(id, gameInstance, player, activeObject);
		}

		if (e.getKeyCode() == KeyEvent.VK_P)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, mouseX, mouseY);
			getGraphics().drawString("Type: " + String.valueOf(activeObject.go.objectType) + " " + "Value: " + String.valueOf(activeObject.go.uniqueName), mouseX, mouseY);
		}

		if (e.getKeyCode() == KeyEvent.VK_M && isControlDown)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, mouseX, mouseY);
			ObjectFunctions.getAllObjectsOfType(id, gameInstance, player, activeObject);
		}

		if (e.getKeyCode() == KeyEvent.VK_M && !isControlDown)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, mouseX, mouseY);
			ObjectFunctions.makeStack(id, gameInstance, player, selectedObjects);
		}

	}


	@Override
	public void keyReleased(KeyEvent e) {
		//System.out.println("keyReleased: "+e);
		isControlDown = false;
		isShiftDown = false;
		loggedKeys[e.getKeyCode()] = false;
		if(!isLeftMouseKeyHold) {
			activeObject = null;
		}
		repaint();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		return false;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		mouseWheelValue += (int) e.getPreciseWheelRotation();
		if (isControlDown)
		{
			zoomFactor += (int) e.getPreciseWheelRotation();
			zooming = Math.exp(-zoomFactor * 0.1);
			//double zooming = Math.exp(-zoomFactor * 0.1);
			//ObjectFunctions.zoomObjects(gameInstance, zooming);
		}
		if(mouseWheelValue <= 0) {
			mouseWheelValue = 0;
			infoText = "";
		}
		else
			infoText = String.valueOf(mouseWheelValue);

		repaint();
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {

	}
}
