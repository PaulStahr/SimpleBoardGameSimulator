package gui;

import gameObjects.GameAction;
import gameObjects.GameObjectInstanceEditAction;
import gameObjects.functions.ObjectFunctions;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import main.Player;
import util.data.IntegerArrayList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

//import gameObjects.GameObjectInstanceEditAction;

public class GamePanel extends JPanel implements MouseListener, MouseMotionListener, GameInstance.GameChangeListener, KeyListener, KeyEventDispatcher, MouseWheelListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3579141032474558913L;
	GameInstance gameInstance;
	ObjectInstance activeObject = null;
	int pressedXPos = -1;
	int pressedYPos = -1;
	int objOrigPosX = -1;
	int objOrigPosY = -1;
	public Player player;
	public final int id = (int)System.nanoTime();

	int maxInaccuracy = 20;

	boolean[] loggedKeys = new boolean[256];

	Boolean isControlDown = false;
	Boolean isShiftDown = false;

	Boolean isLeftMouseKeyHold = false;

	int mouseX = -1;
	int mouseY = -1;

	int mouseWheelValue = 0;

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
		g.drawImage(gameInstance.game.background, 0, 0, getWidth(), getHeight(), Color.BLACK, null);
		for (int i = 0; i < gameInstance.objects.size(); ++i) {
			ObjectInstance oi = gameInstance.objects.get(i);
			if (ObjectFunctions.isStackBottom(oi)) {
				IntegerArrayList aboveList = ObjectFunctions.getAboveStack(gameInstance, oi);
				for (int x : aboveList) {
					ObjectInstance currentInstance = gameInstance.objects.get(x);
					BufferedImage img = currentInstance.go.getLook(currentInstance.state);
					if (currentInstance.getRotation() == 0) {
						g.drawImage(img, currentInstance.state.posX, currentInstance.state.posY, (int) (currentInstance.scale * img.getWidth()), (int) (currentInstance.scale * img.getHeight()), null);
					} else {
						double rotationRequired = Math.toRadians(currentInstance.getRotation());
						double locationX = img.getWidth() / 2;
						double locationY = img.getHeight() / 2;
						AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX * currentInstance.scale, locationY * currentInstance.scale);
						tx.scale(currentInstance.scale, currentInstance.scale);
						AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
						g.drawImage(op.filter(img, null), currentInstance.state.posX, currentInstance.state.posY, null);
					}

				}
			}
		}

		if(activeObject != null) {
			for (int i = 0; i < gameInstance.objects.size(); ++i) {
				ObjectInstance oi = gameInstance.objects.get(i);
				if(oi == activeObject && oi.state.aboveInstanceId == -1)
				{
					double rotationRequired = Math.toRadians(oi.getRotation());
					BufferedImage img = oi.go.getLook(oi.state);
					double locationX = img.getWidth() / 2;
					double locationY = img.getHeight() / 2;
					AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX * oi.scale, locationY * oi.scale);
					tx.scale(oi.scale, oi.scale);
					AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
					g.drawImage(op.filter(img, null), oi.state.posX, oi.state.posY, null);
				}
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
				ObjectFunctions.moveStackTo(id, gameInstance, player, activeObject, activeObject);
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




	@Override
	public void changeUpdate(GameAction action) {
		if (action instanceof GameObjectInstanceEditAction)
		{
			repaint();
		}
	}

	public void keyTyped(KeyEvent e) {
	}
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
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, mouseX, mouseY);
			ObjectFunctions.flipObject(id, gameInstance, player, activeObject);
		}
		if(e.getKeyCode() == KeyEvent.VK_S)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, mouseX, mouseY);
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
			activeObject = ObjectFunctions.getActiveObjectByPosition(gameInstance, mouseX, mouseY);
			if (activeObject!= null) {
				if (ObjectFunctions.haveSamePositions(ObjectFunctions.getStackTop(gameInstance, activeObject), ObjectFunctions.getStackBottom(gameInstance, activeObject))) {
					activeObject = ObjectFunctions.getStackTop(gameInstance, activeObject);
					ObjectFunctions.viewBelowCards(id, gameInstance, player, activeObject, activeObject.getWidth() / 2);
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
			ObjectFunctions.takeObject(id, gameInstance, player, activeObject);
		}

		if (e.getKeyCode() == KeyEvent.VK_D)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, mouseX, mouseY);
			ObjectFunctions.dropObject(id, gameInstance, player, activeObject);
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
		getGraphics().drawString(String.valueOf(mouseWheelValue), mouseX, mouseY);
		repaint();
	}
}
