package gameObjects.action.message;

public class UserFileMessage extends UserMessage{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2520719693806673314L;
	private final String filename;
	private final byte data[];

	public UserFileMessage(int source, int sourcePlayer, int destinationPlayer, String filename, byte data[]) {
		super(source, sourcePlayer, destinationPlayer);
		this.filename = filename;
		this.data = data;
	}

	public String getFilename()
	{
		return filename;
	}

	public byte[] getData() {
		return data;
	}
}
