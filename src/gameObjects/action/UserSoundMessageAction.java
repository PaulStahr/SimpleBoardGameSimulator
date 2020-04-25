package gameObjects.action;

public class UserSoundMessageAction extends GameAction{
	public final String message;
	public final int player;
	public UserSoundMessageAction(int source, int player, String message) {
		super(source);
		this.player = player;
		this.message = message;
	}

}
