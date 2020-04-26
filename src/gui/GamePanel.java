package gui;

import static gameObjects.functions.DrawFunctions.drawActiveObject;
import static gameObjects.functions.DrawFunctions.drawBoard;
import static gameObjects.functions.DrawFunctions.drawDiceObjects;
import static gameObjects.functions.DrawFunctions.drawFigureObjects;
import static gameObjects.functions.DrawFunctions.drawPlayerPositions;
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
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.action.GameAction;
import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.action.GamePlayerEditAction;
import gameObjects.definition.GameObjectDice;
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
	private final AffineTransform boardToScreenTransformation = new AffineTransform();
	private final AffineTransform screenToBoardTransformation = new AffineTransform();
	public Color mouseColor = Color.black;
	public Color dragColor = Color.red;
	public Color stackColor = Color.green;
	public String infoText = "";



	boolean isSelectStarted = false;
	public IntegerArrayList selectedObjects = new IntegerArrayList();
	public int beginSelectPosScreenX = 0;
	public int beginSelectPosScreenY = 0;
	public int selectWidth = 0;
	public int selectHeight = 0;

	public String outText = "";

	public PrivateArea privateArea;

	public float cardOverlap = (float) (2/3.0);


	public GamePanel(GameInstance gameInstance)
	{
		this.gameInstance = gameInstance;
		this.privateArea = new PrivateArea(this, gameInstance, boardToScreenTransformation, screenToBoardTransformation);
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		addMouseWheelListener(this);
		setFocusable(true);
		gameInstance.addChangeListener(this);

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
        g2.setTransform(boardToScreenTransformation);
        try {
			screenToBoardTransformation.setTransform(boardToScreenTransformation.createInverse());
		} catch (NoninvertibleTransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        //Draw all objects not in some private area
		for (int idx = 0; idx < gameInstance.getObjectNumber(); ++ idx) {
			ObjectInstance oi = gameInstance.getObjectInstanceByIndex(idx);
			if (oi.state.owner_id != player.id || !oi.state.inPrivateArea) {
				try {
					drawTokenObjects(this, g, gameInstance, oi, player, ial);
					drawDiceObjects(g, gameInstance, oi, player, 1);
					drawFigureObjects(g, gameInstance, oi, player, 1);
				}catch(Exception e)
				{
					logger.error("Error in drawing Tokens", e);
				}
			}
		}
		drawActiveObject(this, gameInstance, g, player, activeObject);
		drawPlayerPositions(this, g, gameInstance, player, infoText);
		drawSelectedObjects(this, g, gameInstance, player, ial);
		drawTokensInPrivateArea(this, g, gameInstance);


		g.drawString(outText, 50, 50);
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		mouseScreenX = arg0.getX();
		mouseScreenY = arg0.getY();
		screenToBoardPos(mouseScreenX, mouseScreenY, mouseBoardPos);
		mouseInPrivateArea = ObjectFunctions.isInPrivateArea(this, mouseBoardPos.getXI(), mouseBoardPos.getYI());


		if (player != null) {
			player.setMousePos(mouseBoardPos.getXI(), mouseBoardPos.getYI());
			gameInstance.update(new GamePlayerEditAction(id, player, player));
			ObjectInstance nearestObject = ObjectFunctions.getNearestObjectByPosition(this, gameInstance, player, mouseBoardPos.getXI(), mouseBoardPos.getYI(), 1, null);
			if (mouseInPrivateArea && nearestObject != null)
			{
				if (activeObject != null){
					activeObject.state.isActive = false;
				}
				nearestObject.state.isActive = false;
				activeObject = nearestObject;
				outText = String.valueOf(mouseScreenY);
			}else if (!mouseInPrivateArea){
				if (activeObject != null) {
					activeObject.state.isActive = false;
					outText = String.valueOf(activeObject.id);
				}
				if (nearestObject != null) {
					activeObject = nearestObject;
					activeObject.state.isActive = true;
				} else {
					if (activeObject != null) {
						activeObject.state.isActive = false;
						activeObject = null;
					}
				}
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
					beginSelectPosScreenX = arg0.getX();
					beginSelectPosScreenY = arg0.getY();
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
					boolean selectedDrag = false;
					if (activeObjects.size()>1){
						selectedDrag = true;
					}
					MoveFunctions.dragTokens(this, gameInstance, player, oi, arg0, objOrigPosX.get(counter)- mousePressedGamePos.getXI() + mouseBoardPos.getXI(), objOrigPosY.get(counter)- mousePressedGamePos.getYI() + mouseBoardPos.getYI(), mouseWheelValue, selectedDrag);
					MoveFunctions.dragDices(this, gameInstance, player, oi, arg0,  objOrigPosX.get(counter)- mousePressedGamePos.getXI() + mouseBoardPos.getXI(), objOrigPosY.get(counter)- mousePressedGamePos.getYI() + mouseBoardPos.getYI(), mouseWheelValue);
					MoveFunctions.dragFigures(this, gameInstance, player, oi, arg0,  objOrigPosX.get(counter)- mousePressedGamePos.getXI() + mouseBoardPos.getXI(), objOrigPosY.get(counter)- mousePressedGamePos.getYI() + mouseBoardPos.getYI(), mouseWheelValue);
					counter+=1;
				}

				if(activeObject == null && selectedObjects.size()==0 && !SwingUtilities.isMiddleMouseButton(arg0) && !mouseInPrivateArea) {
					selectWidth = mouseScreenX - beginSelectPosScreenX;
					selectHeight = mouseScreenY - beginSelectPosScreenY;
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
		if (activeObjects.size() > 0 && (SwingUtilities.isLeftMouseButton(arg0) || SwingUtilities.isRightMouseButton(arg0) || SwingUtilities.isMiddleMouseButton(arg0))) {
			for (ObjectInstance oi:activeObjects) {
				ObjectFunctions.releaseObjects(arg0, this, gameInstance, player, oi, mouseScreenX, mouseScreenY, 1);
				if (oi.go instanceof GameObjectDice && SwingUtilities.isMiddleMouseButton(arg0)) {
					ObjectFunctions.rollTheDice(id, gameInstance, player, oi);
				}
			}
		}
		if(isSelectStarted) {
			selectedObjects.clear();
			ObjectFunctions.getObjectsInsideBox(gameInstance, player,beginSelectPosScreenX, beginSelectPosScreenY, selectWidth, selectHeight, selectedObjects, boardToScreenTransformation);
			selectHeight = 0;
			selectWidth = 0;
			beginSelectPosScreenX = 0;
			beginSelectPosScreenY = 0;
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
				oi.state.isActive = false;
			}
			selectedObjects.clear();
			activeObjects.clear();
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
				ObjectFunctions.getAllObjectsOfGroup(id, gameInstance, player, oi);
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
		boardToScreenTransformation.setToIdentity();
		boardToScreenTransformation.translate(getWidth() / 2, getHeight() / 2);
		boardToScreenTransformation.scale(zooming, zooming);
		boardToScreenTransformation.rotate(rotation);
		boardToScreenTransformation.translate(translateX, translateY);
		if (player != null){
			Point2D point = new Point2D.Double();
			
			point.setLocation(0, getHeight());
			screenToBoardTransformation.transform(point, point);
			player.setBoardPos(0, point);
			point.setLocation(getWidth(), getHeight());
			screenToBoardTransformation.transform(point, point);
			player.setBoardPos(1, point);
			point.setLocation(getWidth(), 0);
			screenToBoardTransformation.transform(point, point);
			player.setBoardPos(2, point);
			point.setLocation(0, 0);
			screenToBoardTransformation.transform(point, point);
			player.setBoardPos(3, point);
			gameInstance.update(new GamePlayerEditAction(id, player, player));
		}

		//move own stack to private bottom
		/*
		if (player != null) {
			IntegerArrayList ial = new IntegerArrayList();
			ObjectFunctions.getOwnedStack(gameInstance,player,ial);
			if (ial.size() >0) {
				if (gameInstance.getObjectInstanceById(ial.get(0)).state.owner_id == player.id) {
					Point2D origin = new Point2D.Double(getWidth() / 2, getHeight());
					screenToBoardTransformation.transform(origin, null);
					ObjectFunctions.moveStackTo(this, gameInstance, player, ial, (int) origin.getX(), (int) origin.getY());
				}
			}
		}
		*/

	}

	public void screenToBoardPos(int x, int y, Vector2d out)
	{
		gameTransform.transformAffine(x, y, out);
	}
	public void boardToScreenPos(Point2D in, Point2D out)
	{
		out.setLocation(in.getX(), in.getY());
		boardToScreenTransformation.transform(out, null);
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
				activeObjects.add(gameInstance.getObjectInstanceById(id));
			}
		}
		else {
			if (activeObject != null) {
				activeObjects.add(activeObject);
			}
		}

	}

}
