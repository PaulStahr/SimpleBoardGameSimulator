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
import gui.minigames.TetrisGameInstance.TetrisGameEvent;
import gui.minigames.TetrisGameInstance.TetrisGameListener;

public class TetrisPanel extends JPanel implements Runnable, KeyListener, TetrisGameListener{
	/**
	 *
	 */
	private static final long serialVersionUID = 9138747418672277372L;
	private static final Logger logger = LoggerFactory.getLogger(TetrisPanel.class);
	private final TetrisGameInstance tgi;
	private static final Color colors[] = new Color[] {Color.BLACK, Color.RED, Color.GREEN, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.PINK, Color.GRAY};
	private Thread th;
	private boolean isRunning = false;
	private final Random rand = new Random();
	private boolean down;

	public TetrisGameInstance getGameInstance(){return tgi;}


	public TetrisPanel(TetrisGameInstance tetrisInstance)
	{
		this.tgi = tetrisInstance;
		setFocusable(true);
		addKeyListener(this);
		tetrisInstance.addGameListener(this);
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
					th.wait(down ? 50 : (int)(1000*Math.exp(-tgi.getFinishedRows() * 0.02)));
				}
			} catch (InterruptedException e) {
				logger.error("Unexpected interruption", e);
			}

			if (tgi.fallingObject.isEmpty())
			{
				byte successfull = tgi.add(new FallingObject((byte)rand.nextInt(18)), 4, 18);
				if (successfull > 0)
				{
					tgi.reset();
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
		int width = getWidth() - 50;
		int height = getHeight();
		int numRows = tgi.getRows();
		int numCols = tgi.getCols();
		super.paintComponent(g);
		for (int y = 0; y < numRows; ++y)
		{
            int yPos0 = height - ((y * 10 + 9) * height) / (numRows * 10);
            int yPos1 = height - ((y) * height) / numRows;
			for (int x = 0; x < numCols; ++x)
			{
				byte type = tgi.getPixel(x, y);
                int xPos0 = (x * width) / numCols;
                int xPos1 = ((x * 10 + 9) * width) / (numCols * 10);
				if (type != 0)
				{
					g.setColor(colors[Math.abs(type) - 1]);
					g.fillRect(xPos0, yPos0, xPos1 - xPos0, yPos1 - yPos0);
				}
			}
		}
		g.drawString("Points " + tgi.getPoints(), width, 50);
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


    @Override
    public void actionPerformed(TetrisGameEvent event) {
        repaint();
    }
}
