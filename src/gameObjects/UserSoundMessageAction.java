package gameObjects;

public class UserSoundMessageAction extends GameAction{
	public final String message;
	public UserSoundMessageAction(int source, String message) {
		super(source);
		this.message = message;
	}

}
