package gameObjects.action;

public class UsertextMessageAction extends GameAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8533629930145589362L;
	public final String message;
	public final int player;
	public UsertextMessageAction(int source, int player, String message) {
		super(source);
		this.message = message;
		this.player = player;
	}

}
