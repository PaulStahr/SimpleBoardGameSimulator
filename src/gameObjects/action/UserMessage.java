package gameObjects.action;

public class UserMessage extends GameAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7825265253504323824L;

	public final int sourcePlayer;
	public final int destinationPlayer;

	public UserMessage(int source, int sourcePlayer, int destinationPlayer) {
		super(source);
		this.sourcePlayer = sourcePlayer;
		this.destinationPlayer = destinationPlayer;
	}

}
