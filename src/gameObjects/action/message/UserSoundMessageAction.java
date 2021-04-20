package gameObjects.action.message;

import gui.game.Player;

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
	public UserSoundMessageAction(int source, Player sourcePlayer, Player destinationPlayer, byte[] message) {
        super(source, sourcePlayer, destinationPlayer);
        this.message = message;
    }

    public byte[] getData() {
		return message;
	}
}
