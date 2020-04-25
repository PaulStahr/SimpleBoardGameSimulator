package gameObjects.action;

public class UserSoundMessageAction extends GameAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2599913200789247263L;
	public final String message;
	public final int player;
	public UserSoundMessageAction(int source, int player, String message) {
		super(source);
		this.player = player;
		this.message = message;
	}

}
