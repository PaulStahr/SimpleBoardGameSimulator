package gui;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
// imports for the colored chat area
import javax.swing.text.StyledDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.GameAction;
import gameObjects.UsertextMessageAction;
import gameObjects.instance.GameInstance;
import gameObjects.instance.GameInstance.GameChangeListener;
import main.Player;


public class IngameChatPanel extends JPanel implements GameChangeListener {
	private static final Logger logger = LoggerFactory.getLogger(GamePanel.class);

	public static final int preferredWidth = 60;
	public static final int preferredHeight = 70;
	private static final Dimension chatTextMinDimension = new Dimension(400,100);

	int id = (int)System.nanoTime();
	private GameInstance game;
	protected Player player;
	private JTextPane chatTextPane;
	private StyledDocument chatText;
	protected final SimpleAttributeSet textStyle;
	protected JScrollPane chatScrollPane;
	protected JTextField messageInput;


	public IngameChatPanel(GameInstance game, Player player)
	{
		this.game = game;
		game.changeListener.add(this);
		this.player = player;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(new JLabel("Player Chat"));

		chatTextPane = new JTextPane();
		textStyle = new SimpleAttributeSet();
		chatText = chatTextPane.getStyledDocument();
		appendColorMessage("Chat from "+ java.time.ZonedDateTime.now() + "\n", Color.black);

		chatTextPane.setEditable(false);
		chatTextPane.setMinimumSize(chatTextMinDimension);
		chatTextPane.setPreferredSize(new Dimension(500000, 500000));
		chatTextPane.setMaximumSize( new Dimension(1500000,1500000));
		chatScrollPane = new JScrollPane(chatTextPane);
		chatScrollPane.setMinimumSize(chatTextMinDimension);
		chatScrollPane.setPreferredSize(new Dimension(500000, 500000));
		chatScrollPane.setMaximumSize( new Dimension(1500000,1500000));
		this.add(chatScrollPane);

		messageInput = new JTextField();
		messageInput.addActionListener(new InputListener(this));
		this.add(messageInput);

	}
	
	// add a message to the chat area in the color of the sending player
	private void appendColorMessage(String message, Color color) {
		StyleConstants.setForeground(textStyle, color);
		try {
			chatText.insertString(chatText.getLength(), message, textStyle);
			chatTextPane.setCaretPosition(chatText.getLength());
		} catch (BadLocationException e) {
			logger.error("Failed to append text in chat area.");
		}
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
			String message = textAction.message;
			Color color = Color.black;
			for (Player player : game.players) {
				if (textAction.player == player.id) {
					color = player.color;
				}
			}
			appendColorMessage(message, color);
		}
	}
	
	
}

class InputListener implements ActionListener {
	private IngameChatPanel chatPanel;
	InputListener(IngameChatPanel panel) {
		chatPanel = panel;
	}
	@Override
	public void actionPerformed(java.awt.event.ActionEvent evt) {
		String inputText = chatPanel.messageInput.getText();
		if(inputText.length() > 0) {
			String message = chatPanel.player.name + ": "+ inputText +"\n";
			chatPanel.messageInput.setText("");
			chatPanel.send(message);
		}
	}

}