package gui;

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
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import gameObjects.definition.GameObjectDice;
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

import static gameObjects.functions.DrawFunctions.*;

//import gameObjects.GameObjectInstanceEditAction;

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


	ControlPanel controlPanel = new ControlPanel();
	private final AffineTransform boardTransformation = new AffineTransform();
	private final AffineTransform inverseBoardTransformation = new AffineTransform();
	public Color mouseColor = Color.black;
	public Color dragColor = Color.red;
	public Color stackColor = Color.green;
	public String infoText = "";



	boolean isSelectStarted = false;
	public IntegerArrayList selectedObjects = new IntegerArrayList();
	public int beginSelectPosX = 0;
	public int beginSelectPosY = 0;
	public int selectWidth = 0;
	public int selectHeight = 0;

	public String outText = "";

	public PrivateArea privateArea;

	public float cardOverlap = (float) (2/3.0);


	public GamePanel(GameInstance gameInstance)
	{
		this.gameInstance = gameInstance;
		this.privateArea = new PrivateArea(this, gameInstance, boardTransformation, inverseBoardTransformation);
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
		p.add(new JLabel("Take Objects to Hand: T"));
		p.add(new JLabel("Drop All Object from Hand: D"));
		p.add(new JLabel("Get Bottom Card: Shift + Grab"));
		p.add(new JLabel("Shuffle Stack: S"));
		p.add(new JLabel("Flip Card/Roll Dice: F"));
		p.add(new JLabel("Flip Card Stack: Strg + F"));
		p.add(new JLabel("View + Collect Stack: V"));
		p.add(new JLabel("Collect Selected Objects: M"));
		p.add(new JLabel("Collect All Objects: Strg + M"));
		p.add(new JLabel("Dissolve Stack: R"));
		p.add(new JLabel("Count Objects: C"));
		p.add(new JLabel("Count Values: Strg + C"));
		this.add(p);
		updateGameTransform();
		addComponentListener(this);
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		drawBoard(this, g, gameInstance);
		Graphics2D g2 = (Graphics2D)g;
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHints(rh);
        g2.setTransform(boardTransformation);
        try {
			inverseBoardTransformation.setTransform(boardTransformation.createInverse());
		} catch (NoninvertibleTransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        //Draw all objects not in some private area
		for (ObjectInstance oi : gameInstance.objects) {
			if (oi.state.owner_id != player.id || !oi.state.inPrivateArea) {
				try {
					drawTokenObjects(this, g, gameInstance, oi, player, ial);
					drawDiceObjects(this, g, gameInstance, oi, player, 1);
				}catch(Exception e)
				{
					logger.error("Error in drawing Tokens", e);
				}
			}
		}
		drawActiveObject(this, g, player, activeObject);
		drawPlayerMarkers(this, g, gameInstance, player, infoText);
		drawSelectedObjects(this, g, gameInstance, player, ial);
		drawTokensInPrivateArea(this, g, gameInstance);


		g.drawString(outText, 50, 50);
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		mouseScreenX = arg0.getX();
		mouseScreenY = arg0.getY();
		screenToBoardPos(mouseScreenX, mouseScreenY, mouseBoardPos);
		mouseInPrivateArea = ObjectFunctions.isInPrivateArea(this, player.mouseXPos, player.mouseYPos);
		if (mouseInPrivateArea)
		{
			//outText = String.valueOf(mouseScreenY);
			//outText = String.valueOf(this.privateArea.getAngle(mouseGamePos.getXI(), mouseGamePos.getYI()));
		}

		if (player != null) {
			player.setMousePos(mouseBoardPos.getXI(), mouseBoardPos.getYI());
			gameInstance.update(new GamePlayerEditAction(id, player, player));
			activeObject = ObjectFunctions.getNearestObjectByPosition(this, gameInstance, player, mouseBoardPos.getXI(), mouseBoardPos.getYI(), 1, null);
			if(activeObject != null) {
				//player.actionString = String.valueOf(activeObject.id);
				//outText = String.valueOf(activeObject.id);
			}
		}
		repaint();
	}


	@Override
	public void mouseClicked(MouseEvent arg0) {
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

	@Override
	public void mouseEntered(MouseEvent arg0) {
		requestFocus();
	}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {
		if (arg0.getClickCount() < 2) {

			if (SwingUtilities.isLeftMouseButton(arg0)) {
				isLeftMouseKeyHold = true;
			}
			if (SwingUtilities.isRightMouseButton(arg0)) {
				isRightMouseKeyHold = true;
			}
			mouseScreenX = arg0.getX();
			mouseScreenY = arg0.getY();
			screenToBoardPos(arg0.getX(), arg0.getY(), mousePressedGamePos);
			mouseBoardPos.set(mousePressedGamePos);
			if (!isSelectStarted && activeObject == null) {
					beginSelectPosX = arg0.getX();
					beginSelectPosY = arg0.getY();
					isSelectStarted = true;
			} else {
				selectedObjects.clear();
			}
			setActiveObjects();
			if (activeObject != null && this.privateArea.privateObjects.contains(activeObject.id)) {
				activeObject.state.posX = player.mouseXPos - activeObject.getWidth(player.id) / 2;
				activeObject.state.posY = player.mouseYPos - activeObject.getHeight(player.id) / 2;
			}
			if (activeObjects.size() > 0) {
				objOrigPosX.clear();
				objOrigPosY.clear();
				for (ObjectInstance oi:activeObjects) {
					objOrigPosX.add(oi.state.posX);
					objOrigPosY.add(oi.state.posY);
					oi.state.isActive = true;
				}

			}

			repaint();
		}
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		/*Translate the board if control is down*/
		if (arg0.isControlDown())
		{
			translateBoard(arg0);
		}
		else
		{
			if (player != null)
			{
				//int xDiff = objOrigPosX - mousePressedGamePos.getXI() + mouseBoardPos.getXI();
				//int yDiff = objOrigPosY - mousePressedGamePos.getYI() + mouseBoardPos.getYI();
				screenToBoardPos(mouseScreenX = arg0.getX(), mouseScreenY = arg0.getY(), mouseBoardPos);
				screenToBoardPos(mouseScreenX = arg0.getX(), mouseScreenY = arg0.getY(), mouseBoardPos);
				player.setMousePos(mousePressedGamePos.getXI(), mousePressedGamePos.getYI());
				player.setMousePos(mouseBoardPos.getXI(), mouseBoardPos.getYI());
				gameInstance.update(new GamePlayerEditAction(id, player, player));
				/*Handle all drags of Token Objects*/
				int counter = 0;
				for (ObjectInstance oi: activeObjects) {
					MoveFunctions.dragTokens(this, gameInstance, player, oi, arg0, objOrigPosX.get(counter)- mousePressedGamePos.getXI() + mouseBoardPos.getXI(), objOrigPosY.get(counter)- mousePressedGamePos.getYI() + mouseBoardPos.getYI(), mouseWheelValue);
					MoveFunctions.dragDices(this, gameInstance, player, oi, arg0,  objOrigPosX.get(counter)- mousePressedGamePos.getXI() + mouseBoardPos.getXI(), objOrigPosY.get(counter)- mousePressedGamePos.getYI() + mouseBoardPos.getYI(), mouseWheelValue);
					counter+=1;
				}

				if(activeObject == null && !SwingUtilities.isMiddleMouseButton(arg0) && !mouseInPrivateArea) {
					selectWidth = mouseScreenX - beginSelectPosX;
					selectHeight = mouseScreenY - beginSelectPosY;
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
	public void mouseReleased(MouseEvent arg0) {
		if (player == null)
		{
			return;
		}
		mouseScreenX = arg0.getX();
		mouseScreenY = arg0.getY();
		mouseInPrivateArea = ObjectFunctions.isInPrivateArea(this, player.mouseXPos, player.mouseYPos);
		if (activeObject != null && (SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isRightMouseButton(arg0) || SwingUtilities.isMiddleMouseButton(arg0))) {
			ObjectFunctions.releaseObjects(arg0, this, gameInstance, player, activeObject, mouseScreenX, mouseScreenY, 1);
			if (activeObject.go instanceof GameObjectDice && SwingUtilities.isMiddleMouseButton(arg0)){
				ObjectFunctions.rollTheDice(id,gameInstance,player,activeObject);
			}
		}
		if(isSelectStarted) {
			selectedObjects.clear();
			ObjectFunctions.getObjectsInsideBox(gameInstance, player,beginSelectPosX - translateX - getWidth() / 2, beginSelectPosY - translateY - getHeight() / 2, selectWidth, selectHeight, selectedObjects);
			selectHeight = 0;
			selectWidth = 0;
			beginSelectPosX = 0;
			beginSelectPosY = 0;
			isSelectStarted = false;
		}
		activeObject = null;
		gameInstance.update(new GamePlayerEditAction(id, player, player));
		mouseColor = player.color;
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
		if (logger.isDebugEnabled()) {logger.debug("change Update:" + action.source + " " + id);}
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
		boolean controlDown = e.isControlDown();
		setActiveObjects();
		if(e.getKeyCode() == KeyEvent.VK_C && !controlDown)
		{
			int count = 0;
			for (ObjectInstance oi:activeObjects) {
				count += ObjectFunctions.countStack(gameInstance, oi);
			}
			player.actionString = "Object Number: " + String.valueOf(count);
		}
		else if(e.getKeyCode() == KeyEvent.VK_F && !controlDown)
		{
			for (ObjectInstance oi:activeObjects) {
				ObjectFunctions.flipTokenObject(id, gameInstance, player, oi);
				ObjectFunctions.rollTheDice(id, gameInstance, player, oi);
			}
		}
		else if (controlDown && e.getKeyCode() == KeyEvent.VK_F)
		{
			for (ObjectInstance oi:activeObjects) {
				ObjectFunctions.flipTokenStack(id, gameInstance, player, ObjectFunctions.getStackTop(gameInstance, oi));
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_S) {
			for (ObjectInstance oi : ObjectFunctions.getStackRepresentatives(gameInstance, activeObjects)) {
				ObjectFunctions.shuffleStack(id, gameInstance, player, oi);
			}

		}
		else if (controlDown && e.getKeyCode() == KeyEvent.VK_C)
		{
			int count = 0;
			for (ObjectInstance oi:activeObjects) {
				count += ObjectFunctions.countStackValues(gameInstance, oi);
			}
			player.actionString = "Value: " + String.valueOf(count);
		}
		else if (e.getKeyCode() == KeyEvent.VK_V) {
			for (ObjectInstance oi:ObjectFunctions.getStackRepresentatives(gameInstance,activeObjects)) {
				if (ObjectFunctions.haveSamePositions(ObjectFunctions.getStackTop(gameInstance, oi), ObjectFunctions.getStackBottom(gameInstance, oi))) {
					ObjectFunctions.displayStack(this, gameInstance, player, oi, (int) (oi.getWidth(player.id) * cardOverlap));
				} else {
					if (activeObjects.size()==1){ObjectFunctions.collectStack(this, gameInstance, player, activeObjects.get(0));}else {
						ObjectFunctions.collectStack(this, gameInstance, player, oi);
					}
				}
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_R)
		{
			for (ObjectInstance oi:activeObjects) {
				ObjectFunctions.removeStackRelations(id, gameInstance, player, oi);
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_T)
		{
			for (ObjectInstance oi:activeObjects) {
				ObjectFunctions.takeObjects(this, gameInstance, player, oi);
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_D)
		{
			for (ObjectInstance oi:activeObjects) {
				ObjectFunctions.dropObjects(this, gameInstance, player, oi);
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_M && e.isControlDown())
		{
			for (ObjectInstance oi:activeObjects) {
				ObjectFunctions.getAllObjectsOfType(id, gameInstance, player, oi);
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_M && !e.isControlDown())
		{
			ObjectFunctions.makeStack(id, gameInstance, player, selectedObjects);
		}

	}


	@Override
	public void keyReleased(KeyEvent e) {
		if(!isLeftMouseKeyHold) {
			activeObject = null;
		}
		if(!isRightMouseKeyHold) {
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
		if (e.isControlDown() && !mouseInPrivateArea)
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

	public void updateGameTransform()
	{
		gameTransform.setIdentity();
		gameTransform.affineTranslate(-getWidth() / 2, -getHeight() / 2);
		gameTransform.scale(1 / zooming);
		gameTransform.rotateZ(rotation);
		gameTransform.affineTranslate(-translateX, -translateY);
		boardTransformation.setToIdentity();
		boardTransformation.translate(getWidth() / 2, getHeight() / 2);
		boardTransformation.scale(zooming, zooming);
		boardTransformation.rotate(rotation);
		boardTransformation.translate(translateX, translateY);
	}

	public void screenToBoardPos(int x, int y, Vector2d out)
	{
		gameTransform.transformAffine(x, y, out);
	}

	public void translateBoard(MouseEvent arg0)
	{
		if (SwingUtilities.isLeftMouseButton(arg0))
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

	public void setActiveObjects(){
		//Get active or selected objects
		activeObject = ObjectFunctions.getNearestObjectByPosition(this, gameInstance, player, mouseBoardPos.getXI(), mouseBoardPos.getYI(), 1, null);
		activeObjects.clear();
		if (selectedObjects.size() > 0){
			for (int id:selectedObjects){
				activeObjects.add(gameInstance.objects.get(id));
			}
		}
		else {
			if (activeObject != null) {
				activeObjects.add(activeObject);
			}
		}

	}

}
