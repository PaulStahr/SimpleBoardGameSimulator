package gameObjects.action;

public class GameStructureEditAction extends GameAction {

	public static final byte EDIT_BACKGROUND = 0;
	public static final byte EDIT_GAME_NAME = 1;
	public static final byte EDIT_SESSION_PASSWORD = 2;
	public static final byte EDIT_SESSION_NAME = 3;
	public static final byte EDIT_TABLE_RADIUS = 12;
	public static final byte REMOVE_IMAGE = 8;
	public static final byte REMOVE_OBJECT = 10;
	public static final byte REMOVE_OBJECT_INSTANCE = 11;
	
	public final byte type;
	
	public GameStructureEditAction(int source, byte type) {
		super(source);
		this.type = type;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -9012274246521512695L;

}
