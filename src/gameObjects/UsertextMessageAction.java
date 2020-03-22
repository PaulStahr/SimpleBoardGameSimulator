package gameObjects;


public class UsertextMessageAction extends GameAction{
	public final String message;
	public final int player;
	public UsertextMessageAction(int source, int player, String message) {
		super(source);
		this.message = message;
		this.player = player;
	}

}
