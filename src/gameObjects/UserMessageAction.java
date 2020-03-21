package gameObjects;


public class UserMessageAction extends GameAction{
	public final String message;
	public UserMessageAction(Object source, String message) {
		super(source);
		this.message = message;
	}

}
