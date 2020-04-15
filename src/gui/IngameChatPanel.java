package gui;


import java.awt.event.ActionListener;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.FlowView;
import javax.swing.text.View;
import javax.swing.BoxLayout;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import gameObjects.GameAction;
import gameObjects.UsertextMessageAction;
import gameObjects.instance.GameInstance;
import gameObjects.instance.GameInstance.GameChangeListener;
import main.Player;

final class UserInputListener implements ActionListener {
	JTextArea chatText;
	public UserInputListener(JTextArea chatText) {
		this.chatText = chatText;
	}
	public void actionPerformed(java.awt.event.ActionEvent event) {
		// add text from event? or from TextField to chatText
		
		//call send() on the IngameChatPanel
	}
}

// könnte von JPanel erben
public class IngameChatPanel extends JPanel implements GameChangeListener {
	private static final Logger logger = LoggerFactory.getLogger(GamePanel.class);

	public static final int preferredWidth = 60;
	public static final int preferredHeight = 70;

	int id = (int)System.nanoTime();
	private GameInstance game;
	protected Player player;
	private JTextArea chatText;
	protected JScrollPane chatScrollPane;
	protected JTextField messageInput;


	public IngameChatPanel(GameInstance game, Player player)
	{
		this.game = game;
		game.changeListener.add(this);
		this.player = player;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(new JLabel("Player Chat"));

		chatText = new JTextArea(preferredWidth, preferredHeight);
		chatText.setLineWrap(true);
		chatText.append("Chat from "+ java.time.ZonedDateTime.now() + "\n");

		chatText.setEditable(false);
		chatText.setMinimumSize(new Dimension(50,50));
		chatText.setPreferredSize(new Dimension(500000, 500000));
		chatText.setMaximumSize( new Dimension(1500000,1500000));
		chatScrollPane = new JScrollPane(chatText);
		chatScrollPane.setPreferredSize(new Dimension(500000, 500000));
		chatScrollPane.setMaximumSize( new Dimension(1500000,1500000));
		this.add(chatScrollPane);

		messageInput = new JTextField();
		messageInput.addActionListener(new InputListener(this));
		messageInput.setMinimumSize(new Dimension(100,100));
		this.add(messageInput);

		ActionListener userTextListener = new UserInputListener(chatText);
		messageInput.addActionListener(userTextListener);
	}
	
	protected void send(String message)
	{
		game.update(new UsertextMessageAction(id, player.id , message));
	}

	@Override
	public void changeUpdate(GameAction action) {
		if (action instanceof UsertextMessageAction)
		{
			UsertextMessageAction textAction = (UsertextMessageAction) action;
			chatText.append(textAction.message);
			chatText.setCaretPosition(chatText.getDocument().getLength());
		}
	}
	
	
}

class InputListener implements ActionListener {
	private IngameChatPanel chatPanel;
	InputListener(IngameChatPanel panel) {
		chatPanel = panel;
	}
	public void actionPerformed(java.awt.event.ActionEvent evt) {
		String inputText = chatPanel.messageInput.getText();
		if(inputText.length() > 0) {
			String message = chatPanel.player.name + ": "+ inputText +"\n";
			chatPanel.messageInput.setText("");
			chatPanel.send(message);
		}
	}

}