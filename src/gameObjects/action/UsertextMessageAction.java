package gameObjects.action;

public class UsertextMessageAction extends GameAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8533629930145589362L;
	public final String message;
	public final int sourcePlayer;
	public final int destinationPlayer;
	public UsertextMessageAction(int source, int sourcePlayer, int destinationPlayer, String message) {
		super(source);
		this.message = message;
		this.sourcePlayer = sourcePlayer;
		this.destinationPlayer = destinationPlayer;
	}

}
