package gameObjects.action;

public class AddObjectAction extends GameStructureEditAction{
	public static final byte ADD_IMAGE = 4;
	public static final byte ADD_PLAYER = 5;
	public static final byte ADD_GAME_OBJECT = 6;
	public static final byte ADD_GAME_OBJECT_INSTANCE = 7;
	public final int objectId;

	public AddObjectAction(int source, byte type, int objectId) {
		super(source, type);
		this.objectId = objectId;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -9121803638386978230L;

}
