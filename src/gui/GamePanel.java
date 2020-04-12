package gui;

import static gameObjects.functions.DrawFunctions.drawActiveObject;
import static gameObjects.functions.DrawFunctions.drawBoard;
import static gameObjects.functions.DrawFunctions.drawPlayerMarkers;
import static gameObjects.functions.DrawFunctions.drawSelectedObjects;
import static gameObjects.functions.DrawFunctions.drawTokenObjects;
import static gameObjects.functions.DrawFunctions.drawTokensInPrivateArea;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.GameAction;
import gameObjects.GameObjectInstanceEditAction;
import gameObjects.GamePlayerEditAction;
import gameObjects.functions.MoveFunctions;
import gameObjects.functions.ObjectFunctions;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import geometry.Matrix3d;
import geometry.Vector2d;
import main.Player;
import util.data.IntegerArrayList;

//import gameObjects.GameObjectInstanceEditAction;

public class GamePanel extends JPanel implements MouseListener, MouseMotionListener, GameInstance.GameChangeListener, KeyListener, KeyEventDispatcher, MouseWheelListener, ActionListener, ComponentListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3579141032474558913L;
	private static final Logger logger = LoggerFactory.getLogger(GamePanel.class);
	GameInstance gameInstance;
	public ObjectInstance activeObject = null;
	int objOrigPosX = -1;
	int objOrigPosY = -1;
	public Player player;
	public final int id = (int)System.nanoTime();

	int maxInaccuracy = 20;

	boolean[] loggedKeys = new boolean[1024];

	boolean isControlDown = false;
	boolean isShiftDown = false;

	boolean isLeftMouseKeyHold = false;

	public int mouseX = -1;
	public int mouseY = -1;

	public int mouseWheelValue = 0;
	int zoomFactor = 0;
	public int translateX = 0;
	public int translateY = 0;
	public double zooming = 1;
	public double rotation = 0;


	ControlPanel controlPanel = new ControlPanel();
	private final AffineTransform boardTransformation = new AffineTransform();
	private final AffineTransform inverseBoardTransformation = new AffineTransform();
	public PrivateArea privateArea = new PrivateArea(boardTransformation, inverseBoardTransformation);
	public Color mouseColor = Color.black;
	public Color dragColor = Color.red;
	public Color stackColor = Color.green;
	public String infoText = "";

	private final Vector2d mouseGamePos = new Vector2d();
	private final Vector2d mousePressedGamePos = new Vector2d();
	private final Matrix3d gameTransform = new Matrix3d();

	boolean isSelectStarted = false;
	public IntegerArrayList selectedObjects = new IntegerArrayList();
	public int beginSelectPosX = 0;
	public int beginSelectPosY = 0;
	public int selectWidth = 0;
	public int selectHeight = 0;


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
		updateGameTransform();
		addComponentListener(this);
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
        Graphics2D g2 = (Graphics2D)g;
        g2.translate(getWidth() / 2, getHeight() / 2);
        g2.scale(zooming, zooming);
        g2.rotate(rotation);
        g2.translate(translateX, translateY);
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHints(rh);
        boardTransformation.setTransform(g2.getTransform());
        try {
			inverseBoardTransformation.setTransform(boardTransformation.createInverse());
		} catch (NoninvertibleTransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		drawBoard(this, g, gameInstance);
		for (ObjectInstance oi : gameInstance.objects) {
			drawTokenObjects(this, g, gameInstance, oi, player);
		}
		drawActiveObject(g, player, activeObject);
		drawPlayerMarkers(g, gameInstance, player, infoText);
		drawSelectedObjects(this, g, gameInstance, player);
		drawTokensInPrivateArea(this, g, gameInstance);

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		/* Right Mouse Click on Object */
		if(SwingUtilities.isRightMouseButton(arg0))
		{
			mouseToGamePos(arg0.getX(), arg0.getY(), mousePressedGamePos);
			mouseGamePos.set(mousePressedGamePos);
			activeObject = ObjectFunctions.setActiveObjectByMouseAndKey(gameInstance, mouseGamePos, loggedKeys, maxInaccuracy);
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
		mouseX  = arg0.getX();
		mouseY  = arg0.getY();
		mouseToGamePos(arg0.getX(), arg0.getY(), mousePressedGamePos);
		mouseGamePos.set(mousePressedGamePos);
		if(!isSelectStarted && activeObject == null)
		{
			beginSelectPosX = arg0.getX();
			beginSelectPosY = arg0.getY();
			isSelectStarted = true;
		}

		activeObject = ObjectFunctions.setActiveObjectByMouseAndKey(gameInstance, mouseGamePos, loggedKeys, maxInaccuracy);
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
			ObjectFunctions.releaseObjects(this, gameInstance, player, activeObject, 1);
		}
		activeObject = null;
		mouseX = arg0.getX();
		mouseY = arg0.getY();
		if (player != null) {
			gameInstance.update(new GamePlayerEditAction(id, player, player));
		}
		mouseColor = player.color;
		if(isSelectStarted) {
			selectedObjects = ObjectFunctions.getObjectsInsideBox(gameInstance, beginSelectPosX - translateX - getWidth() / 2, beginSelectPosY - translateY - getHeight() / 2, selectWidth, selectHeight);
			selectHeight = 0;
			selectWidth = 0;
			beginSelectPosX = 0;
			beginSelectPosY = 0;
			isSelectStarted = false;
		}
		repaint();
	}

	public void updateGameTransform()
	{
		gameTransform.setIdentity();
		gameTransform.affineTranslate(-getWidth() / 2, -getHeight() / 2);
		gameTransform.scale(1 / zooming);
		gameTransform.rotateZ(rotation);
		gameTransform.affineTranslate(-translateX, -translateY);
	}

	public void mouseToGamePos(int x, int y, Vector2d out)
	{
		gameTransform.transformAffine(x, y, out);
	}

	public void translateBoard(MouseEvent arg0)
	{
		if (SwingUtilities.isLeftMouseButton(arg0))
		{
			double tmpX = (arg0.getX() - mouseX) / zooming;
			double tmpY = (arg0.getY() - mouseY) / zooming;
			double sin = Math.sin(rotation);
			double cos = Math.cos(rotation);
			translateX += tmpX * cos + tmpY * sin;
			translateY += -tmpX * sin + tmpY * cos;
		}
		if (SwingUtilities.isRightMouseButton(arg0))
		{
			rotation += (arg0.getX() - mouseX) / 200.;
		}
		updateGameTransform();
		mouseToGamePos(mouseX = arg0.getX(), mouseY = arg0.getY(), mouseGamePos);
		player.setMousePos(mouseGamePos.getXI(), mouseGamePos.getYI());
		gameInstance.update(new GamePlayerEditAction(id, player, player));

	}
	
	@Override
	public void mouseDragged(MouseEvent arg0) {
		/*Translate the */
		if (arg0.isControlDown())
		{
			translateBoard(arg0);
		}
		else
		{
			if (player != null)
			{
				int xDiff = objOrigPosX - mousePressedGamePos.getXI() + mouseGamePos.getXI();
				int yDiff = objOrigPosY - mousePressedGamePos.getYI() + mouseGamePos.getYI();
				mouseToGamePos(mouseX = arg0.getX(), mouseY = arg0.getY(), mouseGamePos);
				player.setMousePos(mousePressedGamePos.getXI(), mousePressedGamePos.getYI());
				player.setMousePos(mouseGamePos.getXI(), mouseGamePos.getYI());
				gameInstance.update(new GamePlayerEditAction(id, player, player));

				/*Handle all drags of Token Objects*/
				MoveFunctions.dragTokens(this, gameInstance, player,activeObject, arg0, xDiff, yDiff, isShiftDown, mouseWheelValue, privateArea.shape);

				if(activeObject == null && !SwingUtilities.isMiddleMouseButton(arg0)) {
					selectWidth = mouseX - beginSelectPosX;
					selectHeight = mouseY - beginSelectPosY;
				}
			}
			else {
				return;
			}
		}
		mouseColor = dragColor;
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		mouseX = arg0.getX();
		mouseY = arg0.getY();;
		mouseToGamePos(mouseX, mouseY, mouseGamePos);
		if (player != null)
		{
			player.setMousePos(mouseGamePos.getXI(), mouseGamePos.getYI());
			gameInstance.update(new GamePlayerEditAction(id, player, player));
			activeObject = ObjectFunctions.getNearestObjectByPosition(gameInstance, player,mouseGamePos.getXI(), mouseGamePos.getYI(), 1, null);
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
		logger.debug("change Update:" + action.source + " " + id);
		if (action instanceof GameObjectInstanceEditAction)
		{
			repaint();
			//If direct repaint causes problems use this:
			//JFrameUtils.runByDispatcher(repaintRunnable);
		}
		else if (action instanceof GamePlayerEditAction)
		{
			repaint();
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
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, player, mouseGamePos.getXI(), mouseGamePos.getYI());
			int count = ObjectFunctions.countStack(gameInstance, activeObject);
			getGraphics().drawString("Object Number: " + String.valueOf(count), mouseX, mouseY);
		}
		if(e.getKeyCode() == KeyEvent.VK_F && !loggedKeys[KeyEvent.VK_CONTROL])
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getNearestObjectByPosition(gameInstance, player, mouseGamePos.getXI(), mouseGamePos.getYI(), 1, null);
			ObjectFunctions.flipObject(id, gameInstance, player, activeObject);
		}
		if(e.getKeyCode() == KeyEvent.VK_S)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getNearestObjectByPosition(gameInstance, player, mouseGamePos.getXI(), mouseGamePos.getYI(), 1, null);
			ObjectFunctions.shuffleStack(id, gameInstance, player, activeObject);
		}
		if (loggedKeys[KeyEvent.VK_CONTROL] && loggedKeys[KeyEvent.VK_F])
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance,player,mouseGamePos.getXI(), mouseGamePos.getYI());
			ObjectFunctions.flipStack(id, gameInstance, player, activeObject);
		}
		if (loggedKeys[KeyEvent.VK_CONTROL] && loggedKeys[KeyEvent.VK_C])
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance,player ,mouseGamePos.getXI(), mouseGamePos.getYI());
			int count = ObjectFunctions.countStackValues(gameInstance, activeObject);
			getGraphics().drawString("Value: " + String.valueOf(count), mouseX, mouseY);
		}

		if (e.getKeyCode() == KeyEvent.VK_V)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getNearestObjectByPosition(gameInstance, player,mouseGamePos.getXI(), mouseGamePos.getYI(), 1, null);
			if (activeObject!= null) {
				if (ObjectFunctions.haveSamePositions(ObjectFunctions.getStackTop(gameInstance, activeObject), ObjectFunctions.getStackBottom(gameInstance, activeObject))) {
					activeObject = ObjectFunctions.getStackTop(gameInstance, activeObject);
					ObjectFunctions.displayStack(this, gameInstance, player, activeObject, activeObject.getWidth(player.id) / 2);
				} else {
					ObjectFunctions.collectStack(this, gameInstance, player, activeObject);
				}
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_R)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, player,mouseGamePos.getXI(), mouseGamePos.getYI());
			ObjectFunctions.removeStackRelations(id, gameInstance, player, activeObject);
		}
		if (e.getKeyCode() == KeyEvent.VK_T)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance,player, mouseGamePos.getXI(), mouseGamePos.getYI());
			ObjectFunctions.takeObjects(id, gameInstance, player, activeObject);
		}

		if (e.getKeyCode() == KeyEvent.VK_D)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance,player, mouseGamePos.getXI(), mouseGamePos.getYI());
			ObjectFunctions.dropObjects(id, gameInstance, player, activeObject);
		}

		if (e.getKeyCode() == KeyEvent.VK_P)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, player, mouseGamePos.getXI(), mouseGamePos.getYI());
			getGraphics().drawString("Type: " + String.valueOf(activeObject.go.objectType) + " " + "Value: " + String.valueOf(activeObject.go.uniqueName), mouseGamePos.getXI(), mouseGamePos.getYI());
		}

		if (e.getKeyCode() == KeyEvent.VK_M && isControlDown)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, player, mouseGamePos.getXI(), mouseGamePos.getYI());
			ObjectFunctions.getAllObjectsOfType(id, gameInstance, player, activeObject);
		}

		if (e.getKeyCode() == KeyEvent.VK_M && !isControlDown)
		{
			loggedKeys[e.getKeyCode()] = true;
			activeObject = ObjectFunctions.getTopActiveObjectByPosition(gameInstance, player, mouseX, mouseY);
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

		if (isControlDown)
		{
			zoomFactor += (int) e.getPreciseWheelRotation();
			zooming = Math.exp(-zoomFactor * 0.1);
			updateGameTransform();
			//double zooming = Math.exp(-zoomFactor * 0.1);
			//ObjectFunctions.zoomObjects(gameInstance, zooming);
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

	@Override
	public void actionPerformed(ActionEvent actionEvent) {

	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		updateGameTransform();
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
		updateGameTransform();
	}
}
