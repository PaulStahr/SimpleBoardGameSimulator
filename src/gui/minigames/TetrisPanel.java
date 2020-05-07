package gui.minigames;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gui.minigames.TetrisGameInstance.FallingObject;
import gui.minigames.TetrisGameInstance.TetrisGameResetEvent;

public class TetrisPanel extends JPanel implements Runnable, KeyListener{
	private static final Logger logger = LoggerFactory.getLogger(TetrisPanel.class);
	private final TetrisGameInstance tgi;
	private static final Color colors[] = new Color[] {Color.BLACK, Color.RED, Color.GREEN, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.PINK, Color.GRAY};
	private Thread th;
	private boolean isRunning = false;
	private final Random rand = new Random();
	private boolean down;
	public TetrisPanel(TetrisGameInstance tetrisInstance)
	{
		this.tgi = tetrisInstance;
		setFocusable(true);
		addKeyListener(this);
	}
	
	public void start()
	{
		if (th == null)
		{
			th = new Thread(this);
			isRunning = true;
			th.start();
		}
	}
	
	public void stop()
	{
		isRunning = false;
	}
	
	@Override
	public void run()
	{
		while (isRunning)
		{
			try {
				synchronized(th)
				{
					if (down)
					{
						th.wait(50);
					}
					else
					{
						th.wait((int)(1000*Math.exp(-tgi.placedObjectCount() * 0.02)));
					}
				}
			} catch (InterruptedException e) {
				logger.error("Unexpected interruption", e);
			}
			
			if (tgi.fallingObject.size() == 0)
			{
				byte successfull = tgi.add(new FallingObject((byte)rand.nextInt(18), 4, 18));
				if (successfull > 0)
				{
					tgi.actionPerformed(new TetrisGameResetEvent());
				}
			}
			tgi.logic_step();
			repaint();
		}
		th = null;
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		int width = getWidth();
		int height = getHeight();
		int numRows = tgi.getRows();
		int numCols = tgi.getCols();
		super.paintComponent(g);
		for (int y = 0; y < numRows; ++y)
		{
			for (int x = 0; x < numCols; ++x)
			{
				byte type = tgi.getPixel(x, y);
				if (type != 0)
				{
					g.setColor(colors[Math.abs(type) - 1]);
					g.fillRect((x * width) / numCols, height - ((y + 1) * height) / numRows, width / numCols, height / numRows);
				}
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent event) {
		switch(event.getKeyCode())
		{
			case KeyEvent.VK_RIGHT: tgi.moveRight(0);repaint();break;
			case KeyEvent.VK_LEFT: tgi.moveLeft(0);repaint();break;
			case KeyEvent.VK_UP: tgi.rotate(0);repaint();break;
			case KeyEvent.VK_DOWN: down = true;synchronized(th) {
				th.notifyAll();
			}
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent event) {
		switch(event.getKeyCode())
		{
			case KeyEvent.VK_DOWN: down= false;break;
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {}
}
