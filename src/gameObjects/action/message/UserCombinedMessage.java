package gameObjects.action.message;

public class UserCombinedMessage extends UserMessage{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5604523435929061525L;
	public final String filenames[];
	public final byte data[][];
	public final String message;
	public final byte soundmessages[][];

	public UserCombinedMessage(int source, int sourcePlayer, int destinationPlayer, String message, String filenames[], byte data[][], byte soundmessages[][]) {
		super(source, sourcePlayer, destinationPlayer);
		this.message = message;
		this.filenames = filenames;
		this.data = data;
		this.soundmessages = soundmessages;
	}

}
