package gameObjects.action;

public class AddObjectAction extends GameStructureEditAction{
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
