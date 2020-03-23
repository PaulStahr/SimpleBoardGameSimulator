package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.*;

import gameObjects.GameAction;
//import gameObjects.GameObjectInstanceEditAction;
import gameObjects.GameObjectInstanceEditAction;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import main.Player;

public class GamePanel extends JPanel implements MouseListener, MouseMotionListener, GameInstance.GameChangeListener{
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


	public GamePanel(GameInstance gameInstance)
	{
		this.gameInstance = gameInstance;
		addMouseListener(this);
		addMouseMotionListener(this);
		gameInstance.changeListener.add(this);
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		g.drawImage(gameInstance.game.background, 0, 0, getWidth(), getHeight(), Color.BLACK, null);
		for (int i = 0; i < gameInstance.objects.size(); ++i)
		{
			System.out.println(i);
			ObjectInstance oi = gameInstance.objects.get(i);
			if(oi.state.aboveInstanceId == -1 && oi != activeObject) {
				BufferedImage img = oi.go.getLook(oi.state);
				if (oi.getRotation() == 0)
				{
					g.drawImage(img, oi.state.posX, oi.state.posY , (int)(oi.scale * img.getWidth()), (int)(oi.scale * img.getHeight()), null);		
				}
				else
				{
					double rotationRequired = Math.toRadians(oi.getRotation());
					double locationX = img.getWidth() / 2;
					double locationY = img.getHeight() / 2;
					
					AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
					tx.scale(oi.scale, oi.scale);
					AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
					g.drawImage(op.filter(img, null), oi.state.posX, oi.state.posY, null);	
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
					AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
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
			activeObject.newObjectActionMenu(gameInstance, player, this).showPopup(arg0);
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
			Boolean hasReleased = false;
			for (int i = 0; i < gameInstance.objects.size(); ++i) {
				if(!hasReleased) {
					ObjectInstance oi = gameInstance.objects.get(i);

					int xDiff = activeObject.state.posX - oi.state.posX, yDiff = activeObject.state.posY - oi.state.posY;
					int dist = xDiff * xDiff + yDiff * yDiff;
					if (dist < maxInaccuracy * maxInaccuracy && oi != activeObject) {
						ObjectInstance topElement = getTopElement(oi);
						if (topElement != activeObject) {
							topElement.state.aboveInstanceId = activeObject.id;
							activeObject.state.belowInstanceId = topElement.id;
							activeObject.state.posX = topElement.state.posX;
							activeObject.state.posY = topElement.state.posY;
							gameInstance.update(new GameObjectInstanceEditAction(id, player, topElement));
							gameInstance.update(new GameObjectInstanceEditAction(id, player, activeObject));
						}
						hasReleased = true;
					}
				}
			}
			activeObject = null;
		}
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		/* Drag only when left mouse down */
		if(SwingUtilities.isLeftMouseButton(arg0)) {
			if(activeObject != null) {

				/*Remove top card*/
				if(activeObject.state.belowInstanceId != -1) {
					gameInstance.objects.get(activeObject.state.belowInstanceId).state.aboveInstanceId = -1;
					gameInstance.update(new GameObjectInstanceEditAction(id, player, gameInstance.objects.get(activeObject.state.belowInstanceId)));
				}
				activeObject.state.belowInstanceId = -1;

				activeObject.state.posX = objOrigPosX - pressedXPos + arg0.getX();
				activeObject.state.posY = objOrigPosX - pressedXPos + arg0.getY();
				gameInstance.update(new GameObjectInstanceEditAction(id, player, activeObject));
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {}




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
		Boolean topObjectFound = false;
		for (int i = 0;i<gameInstance.objects.size(); ++i)
		{
			ObjectInstance oi = gameInstance.objects.get(i);
			if(!topObjectFound && oi != activeObject) {
				int xDiff = pressedXPos - oi.state.posX, yDiff = pressedYPos - oi.state.posY;
				int dist = xDiff * xDiff + yDiff * yDiff;

				Boolean leftIn = (pressedXPos > (oi.state.posX - maxInaccuracy));
				Boolean rightIn = (pressedXPos < (oi.state.posX + oi.width + maxInaccuracy));
				Boolean topIn = (pressedYPos < (oi.state.posY + oi.height + maxInaccuracy));
				Boolean bottomIn = (pressedYPos > (oi.state.posY - maxInaccuracy));

				if (dist < distance) {
					insideObject = leftIn && rightIn && topIn && bottomIn;
					if (insideObject) {
						activeObject = getTopElement(oi);
						distance = dist;
						topObjectFound = true;
					}
				}
			}
		}


		if(insideObject)
		{
			activeObject = getTopElement(activeObject);
		}
		else {
			activeObject = null;
		}
	}


	public ObjectInstance getTopElement(ObjectInstance objectInstance){
		ObjectInstance startElement = objectInstance;
		ObjectInstance currentTop = objectInstance;
		while (currentTop.state.aboveInstanceId != -1){
			currentTop = gameInstance.objects.get(currentTop.state.aboveInstanceId);
			if (startElement == currentTop)
			{
				throw new RuntimeException();
			}
		}
		return currentTop;
	}

	public ObjectInstance getBottomElement(ObjectInstance objectInstance){
		ObjectInstance currentBottom = objectInstance;
		while (currentBottom.state.belowInstanceId != -1){
			currentBottom = gameInstance.objects.get(currentBottom.state.aboveInstanceId);
		}
		return currentBottom;
	}

}
