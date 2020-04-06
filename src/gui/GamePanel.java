package gui;

import java.awt.Color;
import java.awt.Graphics;
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
import gameObjects.functions.ObjectFunctions;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import main.Player;

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
	ControlPanel controlPanel = new ControlPanel();


	public GamePanel(GameInstance gameInstance)
	{
		this.gameInstance = gameInstance;
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		addMouseWheelListener(this);
		setFocusable(true);
		gameInstance.changeListener.add(this);

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
		p.add(new JLabel("Collect Objects: M"));
		p.add(new JLabel("Remove Stack: R"));
		p.add(new JLabel("Count Objects: C"));
		p.add(new JLabel("Count Values: Strg + C"));

		this.add(p);
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		//TODO Florian:sometimes images are drawn twice
		double zooming = Math.exp(-zoomFactor * 0.1);
		g.drawString(String.valueOf(mouseWheelValue), mouseX, mouseY);
		g.drawImage(gameInstance.game.background, 0, 0, getWidth(), getHeight(), Color.BLACK, null);
		for (int i = 0; i < gameInstance.objects.size(); ++i) {
			ObjectInstance oi = gameInstance.objects.get(i);
			if (ObjectFunctions.isStackBottom(oi)) {
				ObjectFunctions.drawStack(g, ObjectFunctions.getAboveStack(gameInstance, oi), gameInstance, player.id, zooming, logger);
			}
		}
		if(activeObject != null && ObjectFunctions.isStackTop(activeObject)) {
			ObjectFunctions.drawObject(g, activeObject, player.id, zooming, logger);
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
		activeObject = ObjectFunctions.setActiveObjectByMouseAndKey(gameInstance, arg0, loggedKeys, maxInaccuracy);
		if(activeObject != null) {
			objOrigPosX = activeObject.state.posX;
			objOrigPosY = activeObject.state.posY;
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if (activeObject != null && (SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isMiddleMouseButton(arg0))) {
			ObjectFunctions.releaseObjects(id, gameInstance, player, activeObject);
		}
		activeObject = null;
		mouseX = arg0.getX();
		mouseY = arg0.getY();
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		/* Drag only when left mouse down */
		if((SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isMiddleMouseButton(arg0)) && !isShiftDown && activeObject != null) {
			/*Drop objects if middle mouse button is not held*/
			if (!SwingUtilities.isMiddleMouseButton(arg0)) {
				ObjectFunctions.removeObject(id, gameInstance, player, activeObject);
			}
			ObjectFunctions.moveObjectTo(id, gameInstance, player, activeObject, objOrigPosX - pressedXPos + arg0.getX(), objOrigPosY - pressedYPos + arg0.getY());
			if (SwingUtilities.isMiddleMouseButton(arg0)) {
				if (ObjectFunctions.isStackCollected(gameInstance, activeObject))
					ObjectFunctions.moveBelowStackTo(id, gameInstance, player, activeObject, activeObject, false);
				else {
					ObjectFunctions.moveBelowStackTo(id, gameInstance, player, activeObject, activeObject, false);
					ObjectFunctions.moveAboveStackTo(id, gameInstance, player, activeObject, activeObject, false);
				}
			}
		}
		else if(SwingUtilities.isLeftMouseButton(arg0) && isShiftDown && activeObject != null) {
			/*Remove top card*/
			ObjectFunctions.removeObject(id, gameInstance, player, activeObject);
			ObjectFunctions.moveObjectTo(id, gameInstance, player, activeObject, objOrigPosX - pressedXPos + arg0.getX(), objOrigPosY - pressedYPos + arg0.getY());
			gameInstance.update(new GameObjectInstanceEditAction(id, player, activeObject));
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		mouseX = arg0.getX();
		mouseY = arg0.getY();
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
		if (action instanceof GameObjectInstanceEditAction)
		{
			repaint();
			//If direct repaint causes problems use this:
			//JFrameUtils.runByDispatcher(repaintRunnable);
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
			activeObject = ObjectFunctions.getNearestObjectByPosition(gameInstance, player, mouseX, mouseY, null);
			ObjectFunctions.flipObject(id, gameInstance, player, activeObject);
		}
		if(e.getKeyCode() == KeyEvent.VK_S)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getNearestObjectByPosition(gameInstance, player, mouseX, mouseY, null);
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
			activeObject = ObjectFunctions.getNearestObjectByPosition(gameInstance, player, mouseX, mouseY, null);
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

		if (e.getKeyCode() == KeyEvent.VK_M)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, mouseX, mouseY);
			ObjectFunctions.getAllObjectsOfType(id, gameInstance, player, activeObject);
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
		}
		getGraphics().drawString(String.valueOf(mouseWheelValue), mouseX, mouseY);
		repaint();
		if(mouseWheelValue < 0) {
			mouseWheelValue = 0;
		}
		getGraphics().drawString(String.valueOf(mouseWheelValue), mouseX, mouseY);
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {

	}
}
