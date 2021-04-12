package gameObjects.action.structure;

public class GameStructureObjectEditAction extends GameStructureEditAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7749723889366485300L;
	public final int objectId;

	public GameStructureObjectEditAction(int source, byte type, int objectId) {
		super(source, type);
		this.objectId = objectId;
	}

}
