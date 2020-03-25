package gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.*;

import gameObjects.GameAction;
//import gameObjects.GameObjectInstanceEditAction;
import gameObjects.GameObjectInstanceEditAction;
import gameObjects.functions.ObjectFunctions;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import main.Player;
import util.data.IntegerArrayList;

import static java.lang.Math.abs;

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
		p.setLayout(new FlowLayout());
		p.add(new JLabel(" Move Top Card: Shift + Drag  | "));
		p.add(new JLabel(" Grab Stack: Strg + Drag  | "));
		p.add(new JLabel(" Take Object: T  | "));
		p.add(new JLabel(" Drop Object: D  | "));
		p.add(new JLabel(" Get Bottom Card: Shift + Grab  | "));
		p.add(new JLabel(" Shuffle Stack: S  | "));
		p.add(new JLabel(" Flip Card: F  | "));
		p.add(new JLabel(" Flip Stack: Strg + F  | "));
		p.add(new JLabel(" View Stack: V  | "));
		p.add(new JLabel(" Collect Stack: Strg + C  | "));
		p.add(new JLabel(" Remove Stack: R  | "));
		p.add(new JLabel(" Count Objects: C"));

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
					BufferedImage img = currentInstance.go.getLook(oi.state);
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
			getActiveObjectByMouseEvent(arg0);
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
		getActiveObjectByMouseEvent(arg0);
		if(activeObject != null) {
			objOrigPosX = activeObject.state.posX;
			objOrigPosY = activeObject.state.posY;
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if (activeObject != null && SwingUtilities.isLeftMouseButton(arg0)) {
			for (int i = 0; i < gameInstance.objects.size(); ++i) {
					ObjectInstance oi = gameInstance.objects.get(i);
					int xDiff = activeObject.state.posX - oi.state.posX, yDiff = activeObject.state.posY - oi.state.posY;
					int dist = xDiff * xDiff + yDiff * yDiff;
					if (dist < maxInaccuracy * maxInaccuracy && oi != activeObject) {
						ObjectInstance topElement = ObjectFunctions.getStackTop(gameInstance, oi);
						if (topElement != activeObject) {
							ObjectInstance stackBottom = ObjectFunctions.getStackBottom(gameInstance, activeObject);
							topElement.state.aboveInstanceId = stackBottom.id;
							stackBottom.state.belowInstanceId = topElement.id;
							stackBottom.state.posX = topElement.state.posX;
							stackBottom.state.posY = topElement.state.posY;
							gameInstance.update(new GameObjectInstanceEditAction(id, player, topElement));
							gameInstance.update(new GameObjectInstanceEditAction(id, player, stackBottom));
						}
						break;
				}
			}
		}
		activeObject = null;
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		/* Drag only when left mouse down */
		if(SwingUtilities.isLeftMouseButton(arg0) && !isShiftDown && activeObject != null) {
			/*Remove top card if not control pressed*/
			if (!isControlDown) {
				ObjectFunctions.removeObject(id, gameInstance, player, activeObject);
			}
			ObjectFunctions.moveObjectTo(id, gameInstance, player, activeObject, objOrigPosX - pressedXPos + arg0.getX(), objOrigPosY - pressedYPos + arg0.getY());
			if (isControlDown) {
				ObjectFunctions.moveBelowStackTo(id, gameInstance, player, activeObject);
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


	public void getActiveObjectByMouseEvent(MouseEvent arg0){
		pressedXPos = arg0.getX();
		pressedYPos = arg0.getY();
		int distance = Integer.MAX_VALUE;
		Boolean insideObject = false;
		for (int i = 0;i<gameInstance.objects.size(); ++i)
		{
			ObjectInstance oi = gameInstance.objects.get(i);
			int xDiff = pressedXPos - (oi.state.posX + oi.width/2), yDiff = pressedYPos - (oi.state.posY + oi.height/2);
			int dist = xDiff * xDiff + yDiff * yDiff;

			Boolean leftIn = (pressedXPos > (oi.state.posX - maxInaccuracy));
			Boolean rightIn = (pressedXPos < (oi.state.posX + oi.width + maxInaccuracy));
			Boolean topIn = (pressedYPos < (oi.state.posY + oi.height + maxInaccuracy));
			Boolean bottomIn = (pressedYPos > (oi.state.posY - maxInaccuracy));

			if (dist < distance) {
				insideObject = leftIn && rightIn && topIn && bottomIn;
				if (insideObject) {
					if(!isShiftDown && ObjectFunctions.haveSamePositions(oi, ObjectFunctions.getStackTop(gameInstance, oi))) {
						activeObject = ObjectFunctions.getStackTop(gameInstance, oi);
					}
					else if(!ObjectFunctions.haveSamePositions(oi, ObjectFunctions.getStackTop(gameInstance, oi))){
						activeObject = oi;
					}
					else {
						activeObject = ObjectFunctions.getStackBottom(gameInstance, oi);
					}
					distance = dist;
				}
			}
		}
		if(!insideObject)
		{
			activeObject = null;
		}
	}

	public void keyTyped(KeyEvent e) {
		//System.out.println("keyTyped: "+e);
	}
	public void keyPressed(KeyEvent e) {
		//System.out.println("keyPressed: "+e);
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

		if(e.getKeyCode() == KeyEvent.VK_C)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, mouseX, mouseY);
			int count = ObjectFunctions.countStack(gameInstance, activeObject);
			getGraphics().drawString("Object Number: " + String.valueOf(count), mouseX, mouseY);
		}
		if (loggedKeys[KeyEvent.VK_CONTROL] && loggedKeys[KeyEvent.VK_C])
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, mouseX, mouseY);
			ObjectFunctions.collectStack(id, gameInstance, player, activeObject);
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

		if (e.getKeyCode() == KeyEvent.VK_V)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, mouseX, mouseY);
			ObjectFunctions.viewBelowCards(id, gameInstance, player, activeObject, activeObject.width/2);
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
	}
	public void keyReleased(KeyEvent e) {
		//System.out.println("keyReleased: "+e);
		isControlDown = false;
		isShiftDown = false;
		loggedKeys[e.getKeyCode()] = false;
		activeObject = null;
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
