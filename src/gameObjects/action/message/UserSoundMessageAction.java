package gameObjects.action.message;

public class UserSoundMessageAction extends UserMessage{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2599913200789247263L;
	public final byte message[];
	public UserSoundMessageAction(int source, int sourcePlayer, int destinationPlayer, byte message[]) {
		super(source, sourcePlayer, destinationPlayer);
		this.message = message;
	}

}
