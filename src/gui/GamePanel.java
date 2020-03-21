package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import main.GameInstance;
import main.ObjectInstance;

public class GamePanel extends JPanel implements MouseListener, MouseMotionListener{
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
	
	public GamePanel(GameInstance gameInstance)
	{
		this.gameInstance = gameInstance;
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		g.drawImage(gameInstance.game.background, 0, 0, getWidth(), getHeight(), Color.BLACK, null);
		for (int i = 0; i < gameInstance.objects.size(); ++i)
		{
			System.out.println(i);
			ObjectInstance oi = gameInstance.objects.get(i);
			double rotationRequired = Math.toRadians (oi.getRotation());
			BufferedImage img = oi.go.getLook(oi.state);
			double locationX = img.getWidth() / 2;
			double locationY = img.getHeight() / 2;
			AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
			g.drawImage(op.filter(img, null), oi.state.posX, oi.state.posY, null);
		}
	}



	@Override
	public void mouseClicked(MouseEvent arg0) {}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {
		pressedXPos = arg0.getX();
		pressedYPos = arg0.getY();
		int distance = Integer.MAX_VALUE;
		for (int i = 0;i<gameInstance.objects.size(); ++i)
		{
			ObjectInstance oi = gameInstance.objects.get(i);
			int xDiff = pressedXPos - oi.state.posX, yDiff = pressedYPos - oi.state.posY;
			int dist = xDiff * xDiff + yDiff * yDiff;
			if (dist < distance)
			{
				activeObject = oi;
				distance = dist;
			}
		}
		objOrigPosX = activeObject.state.posX;
		objOrigPosY = activeObject.state.posY;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		activeObject.state.posX = objOrigPosX - pressedXPos + arg0.getX();
		activeObject.state.posY = objOrigPosX - pressedXPos + arg0.getY();
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {}
}
