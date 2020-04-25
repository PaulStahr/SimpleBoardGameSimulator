package gui;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.action.GameAction;
import gameObjects.action.GamePlayerEditAction;
import gameObjects.action.UsertextMessageAction;
import gameObjects.instance.GameInstance;
import gameObjects.instance.GameInstance.GameChangeListener;
import main.Player;


public class IngameChatPanel extends JPanel implements GameChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8779142095973760951L;
	private static final Logger logger = LoggerFactory.getLogger(IngameChatPanel.class);
	private static final Dimension chatTextMinDimension =  new Dimension(200,50);
	private static final Dimension chatTextPrefDimension = new Dimension(500,10000);
	//private static final Dimension chatTextMaxDimension =  new Dimension(10000,10000);

	int id = (int)System.nanoTime();
	private GameInstance game;
	protected Player player;

	protected JTabbedPane chatPanes;
	protected final SimpleAttributeSet textStyle;
	protected JTextField messageInput;
	protected String receiverPlayerName = "all";
	private final JComboBox<String> sendTo = new JComboBox<String>();

	void updatePlayerList()
	{
		String[] playerNames = game.getPlayerNames();
		String[] sendToNames = new String[playerNames.length];
		// The first option in the sendTo combobox is to send the message to everybody "all"
		sendToNames[0] = "all";

		int targetIndex = 1;
		for(int playerIndex=0; playerIndex<game.getPlayerNumber(); playerIndex++) {
			// copy all player names to the combobox. 
			// Omit the own name, since you don't want to send messages to yourself.
			if (!playerNames[playerIndex].matches(player.name)) {
				sendToNames[targetIndex] = playerNames[playerIndex];
				targetIndex++;
			}

		}
		Object selected = sendTo.getSelectedItem();
		sendTo.setModel(new DefaultComboBoxModel<String>(sendToNames));
		sendTo.setSelectedItem(selected);
	}

	public IngameChatPanel(GameInstance game, Player player)
	{
		this.game = game;
		game.addChangeListener(this);
		this.player = player;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(new JLabel("Player Chat"));

		textStyle = new SimpleAttributeSet();

		chatPanes = new JTabbedPane(SwingConstants.BOTTOM);
		this.add(chatPanes);
		createChatPane("all");

		updatePlayerList();
		chatPanes.addChangeListener(new TabListener(chatPanes,sendTo));
		sendTo.addActionListener(new SendToListener(this));
		JPanel sendToPanel = new JPanel();
		sendToPanel.setLayout(new BoxLayout(sendToPanel, BoxLayout.X_AXIS));
		sendToPanel.add(new JLabel("Send to: "));
		sendToPanel.add(sendTo);
		sendToPanel.add(Box.createHorizontalGlue());
		this.add(sendToPanel);

		messageInput = new JTextField();
		messageInput.addActionListener(new InputListener(this));

		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));
		messagePanel.add(new JLabel("Message: "));
		messagePanel.add(messageInput);
	
		this.add(messagePanel);

	}

	private void createChatPane(String tabName) {
		JTextPane chatTextPane = new JTextPane();
		appendColorMessage(chatTextPane, "Chat from "+ java.time.ZonedDateTime.now() + "\n", Color.black);
		chatTextPane.setEditable(false);
		//chatTextPane.setMinimumSize(chatTextMinDimension);

		JScrollPane chatScrollPane;
		chatScrollPane = new JScrollPane(chatTextPane);
		chatScrollPane.setMinimumSize(chatTextMinDimension);
		chatScrollPane.setPreferredSize(chatTextPrefDimension);
		//chatScrollPane.setMaximumSize( new Dimension(1500000,1500000));

		chatPanes.addTab(tabName, chatScrollPane);
	}
	
	// add a message to the chat area in the color of the sending player
	private void appendColorMessage(JTextPane chatTextPane, String message, Color color) {
		StyleConstants.setForeground(textStyle, color);
		try {
			StyledDocument chatText = chatTextPane.getStyledDocument();
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
		if (action instanceof GamePlayerEditAction)
		{
			updatePlayerList();
		}
		else if (action instanceof UsertextMessageAction)
		{
			UsertextMessageAction textAction = (UsertextMessageAction) action;
			Player pl = game.getPlayerById(textAction.player);
			String rawMessage = textAction.message;
			Color color = pl == null ? Color.BLACK : pl.color;
			String recipient = rawMessage.substring(0, rawMessage.indexOf(":"));
			// chop off the recipient part from the message
			rawMessage = rawMessage.substring(rawMessage.indexOf(":")+1);
			String sender = rawMessage.substring(0, rawMessage.indexOf(":"));
			// chop off the sender part from the message:			
			String message = rawMessage.substring(rawMessage.indexOf(":")+1);
			String tabName;
			if ( sender.matches(player.name) | recipient.matches(player.name)  | recipient.matches("all")) {
				// The message was sent by me or it was sent to me or it was sent to all (which means also to me)

				// Find the tab to add the message to
				int chatIndex = -1;
				if (recipient.matches("all")) {
					tabName = "all";
				} else if (sender.matches(player.name)) {
					// I sent the message. Look for a tab named with the reciver
					tabName = recipient;
				} else {
					// it is a private message sent to me
					// look for a tab with the sender name
					tabName = sender;
				}
				chatIndex = chatPanes.indexOfTab(tabName);
				if (chatIndex == -1) {
					// The one-on-one chat does not exist yet.
					// Create a new tab.
					createChatPane(tabName);
					chatIndex = chatPanes.getTabCount() -1;
					chatPanes.setSelectedIndex(chatIndex);
				}

				JScrollPane scrollPane =(JScrollPane) chatPanes.getComponentAt(chatIndex);
				JViewport viewPort = scrollPane.getViewport();
				JTextPane chatPane = (JTextPane) viewPort.getView();
			
				appendColorMessage(chatPane, sender + ": "+ message, color);
			}
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
			String message = chatPanel.receiverPlayerName +":"+ chatPanel.player.name + ":"+ inputText +"\n";
			chatPanel.messageInput.setText("");
			chatPanel.send(message);
		}
	}

}

/*
* When a different recipient is selected in the combo box,
* switch to the corresponding dialog in the tabbed pane.
*/
class SendToListener implements ActionListener {
	private IngameChatPanel chatPanel;

	SendToListener(IngameChatPanel panel) {
		chatPanel = panel;
	}

	@Override
	public void actionPerformed(java.awt.event.ActionEvent evt) {
		JComboBox<?> sendTo = (JComboBox<?>) evt.getSource();
		chatPanel.receiverPlayerName = (String) sendTo.getSelectedItem();

		int receiverIndex = chatPanel.chatPanes.indexOfTab(chatPanel.receiverPlayerName);
		if (receiverIndex >= 0) {
			// When a player sends a private message for the first time, the chat tab does not yet exist.
			chatPanel.chatPanes.setSelectedIndex(receiverIndex);
		}
	}


}

/*
 * When a tab changes, update the combobox 
 * such that the next send message will go to the dialog selected in the tabbed pane.
 */
class TabListener implements ChangeListener {
	private JTabbedPane tabPane;
	private JComboBox<String> sendToBox;

	TabListener(JTabbedPane tabs, JComboBox<String> box) {
		tabPane = tabs;
		sendToBox = box;
	}

	@Override
	public void stateChanged(javax.swing.event.ChangeEvent evt) {
		String toWhom = tabPane.getTitleAt(tabPane.getSelectedIndex());
		sendToBox.setSelectedItem(toWhom);
	}
}

