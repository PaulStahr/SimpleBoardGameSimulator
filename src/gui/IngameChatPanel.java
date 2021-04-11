package gui;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTML;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.DataHandler;
import gameObjects.action.GameAction;
import gameObjects.action.message.UserCombinedMessage;
import gameObjects.action.message.UserFileMessage;
import gameObjects.action.message.UserMessage;
import gameObjects.action.message.UserSoundMessageAction;
import gameObjects.action.message.UsertextMessageAction;
import gameObjects.action.player.PlayerEditAction;
import gameObjects.instance.GameInstance;
import gameObjects.instance.GameInstance.GameChangeListener;
import main.Player;
import util.JFrameUtils;
import util.io.StreamUtil;


public class IngameChatPanel extends JPanel implements GameChangeListener, KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8779142095973760951L;
	private static final Logger logger = LoggerFactory.getLogger(IngameChatPanel.class);
	private static final Dimension chatTextMinDimension =  new Dimension(200,50);
	private static final Dimension chatTextPrefDimension = new Dimension(500,10000);
	private final ArrayList<UserFileMessage> fileMessages = new ArrayList<>();
	//private static final Dimension chatTextMaxDimension =  new Dimension(10000,10000);

	final int id = (int)System.nanoTime();
	private GameInstance game;
	protected Player player;

	protected final JTabbedPane chatPanes = new JTabbedPane(SwingConstants.BOTTOM);
	protected final SimpleAttributeSet textStyle = new SimpleAttributeSet();
	protected final JTextField messageInput = new JTextField();
	protected String receiverPlayerName = "all";
	private final JComboBox<String> sendTo = new JComboBox<String>();
	private	 int playerModCount = 0;
	private Player receiverPlayer;
	
	void updatePlayerList()
	{
		int modCount = game.getPlayerCount();
		for (int i = 0; i < game.getPlayerCount(); ++i){modCount += game.getPlayerByIndex(i).getNameModCount();}
		if (modCount == playerModCount){return;}
		playerModCount = modCount;
		String[] sendToNames = new String[game.getPlayerCount()];
		// The first option in the sendTo combobox is to send the message to everybody "all"
		sendToNames[0] = "all";

		for(int playerIndex=0, writeIndex = 0; playerIndex<game.getPlayerCount(); playerIndex++) {
			Player current = game.getPlayerByIndex(playerIndex);
			// copy all player names to the combobox. 
			// Omit the own name, since you don't want to send messages to yourself.
			if (current != player) {
				sendToNames[++writeIndex] = current.getName();
			}
		}
		JFrameUtils.updateComboBox(sendTo, sendToNames);
		sendTo.setSelectedIndex(0);
	}

	public IngameChatPanel(GameInstance game, Player player)
	{
		this.game = game;
		game.addChangeListener(this);
		this.player = player;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(new JLabel("Player Chat"));

		this.add(chatPanes);
		createChatPane("all");

		updatePlayerList();
		chatPanes.addChangeListener(new TabListener(chatPanes,sendTo));
		sendTo.addActionListener(new SendToListener());
		JPanel sendToPanel = new JPanel();
		sendToPanel.setLayout(new BoxLayout(sendToPanel, BoxLayout.X_AXIS));
		sendToPanel.add(new JLabel("Send to: "));
		sendToPanel.add(sendTo);
		sendToPanel.add(Box.createHorizontalGlue());
		this.add(sendToPanel);

		messageInput.addKeyListener(this);
		messageInput.addActionListener(new InputListener());

		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));
		messagePanel.add(new JLabel("Message: "));
		messagePanel.add(messageInput);
	
		this.add(messagePanel);
		messageInput.setDropTarget(new DropTarget() {
		    /**
			 * 
			 */
			private static final long serialVersionUID = 8601119174600655506L;

			@SuppressWarnings("unchecked")
			@Override
			public synchronized void drop(DropTargetDropEvent evt) {
		        try {
		            evt.acceptDrop(DnDConstants.ACTION_COPY);
		            List<? extends File> droppedFiles = (List<? extends File>)
		                evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
	                for (File file : droppedFiles) {
		            	game.update(new UserFileMessage(id, player, receiverPlayer, file.getName(), Files.readAllBytes(file.toPath())));
		            }
		        } catch (Exception ex) {
		            JFrameUtils.logErrorAndShow("Couldn't drop element", ex, logger);
		        }
		    }
		});

	}

	private void createChatPane(String tabName) {
		JTextPane chatTextPane = new JTextPane();
		chatTextPane.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {}
			
			@Override
			public void mousePressed(MouseEvent e) {}
			
			@Override
			public void mouseExited(MouseEvent e) {}
			
			@Override
			public void mouseEntered(MouseEvent e) {}
			
			@Override
			public void mouseClicked(MouseEvent e) {
		        int     pos  = chatTextPane.viewToModel( e.getPoint() );
		        Element elem = ((StyledDocument) chatTextPane.getDocument()).getCharacterElement(pos);
		        if (elem != null)
		        {
		        	AttributeSet set = elem.getAttributes();
		        	Object attribute = set.getAttribute(HTML.Attribute.HREF);
		        	if (attribute != null)
		        	{
		        		UserFileMessage action = fileMessages.get((Integer)attribute);
		        		JFileChooser fileChooser = new JFileChooser();
		        		fileChooser.setSelectedFile(new File(action.getFilename()));
		    			if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
		    			{
		    				try {
		    					File file = fileChooser.getSelectedFile();
		    					FileOutputStream out = new FileOutputStream(file);
		    					out.write(action.getData());
		    					out.close();
		    				} catch (IOException ex) {
		    					JFrameUtils.logErrorAndShow("Couldn't save file", ex, logger );
		    				}
		    			}
		        		logger.info("attribute " + attribute.toString());
		        	}
		        }
			}
		});
		textStyle.addAttribute(StyleConstants.Foreground, Color.BLACK);
		appendMessage(chatTextPane, "Chat from "+ java.time.ZonedDateTime.now() + "\n", textStyle);
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
	private void appendMessage(JTextPane chatTextPane, String message, SimpleAttributeSet style) {
		try {
			StyledDocument chatText = chatTextPane.getStyledDocument();
			chatText.insertString(chatText.getLength(), message, style);
			chatTextPane.setCaretPosition(chatText.getLength());
		} catch (BadLocationException e) {
			logger.error("Failed to append text in chat area.");
		}
	}
	
	private final Runnable updatePlayerListRunnable = new Runnable() {
		
		@Override
		public void run() {
			updatePlayerList();
		}
	};
	
	@Override
	public void changeUpdate(GameAction action) {
		if (action instanceof PlayerEditAction)
		{
			JFrameUtils.runByDispatcher(updatePlayerListRunnable);
		}
		else if (action instanceof UserMessage)
		{
			UserMessage userMessage = (UserMessage)action;
			int sourceId = userMessage.sourcePlayer;
			int destinationId = userMessage.destinationPlayer;
			if ( sourceId == player.id || destinationId == player.id || destinationId == -1) {
				Player sourcePlayer = userMessage.getSourcePlayer(game);
				textStyle.addAttribute(StyleConstants.Foreground, sourcePlayer == null ? Color.BLACK : sourcePlayer.color);
				String tabName;
				// The message was sent by me or it was sent to me or it was sent to all (which means also to me)
				// Find the tab to add the message to
				if (destinationId == -1) {
					tabName = "all";
				} else if (sourcePlayer == player) {
					// I sent the message. Look for a tab named with the reciver
					tabName = userMessage.getDestinationPlayer(game).getName();
				} else {
					// it is a private message sent to me
					// look for a tab with the sender name
					tabName = sourcePlayer.getName();
				}
				int chatIndex = chatPanes.indexOfTab(tabName);
				if (chatIndex == -1) {
					// The one-on-one chat does not exist yet.
					// Create a new tab.
					chatIndex = chatPanes.getTabCount();
					createChatPane(tabName);
					chatPanes.setSelectedIndex(chatIndex);
				}
				JScrollPane scrollPane =(JScrollPane) chatPanes.getComponentAt(chatIndex);
				JTextPane chatPane = (JTextPane)scrollPane.getViewport().getView();
				if (action instanceof UserFileMessage)
				{
					textStyle.addAttribute(HTML.Attribute.HREF, fileMessages.size());
					fileMessages.add((UserFileMessage)action);
					appendMessage(chatPane, ((UserFileMessage)action).getFilename(), textStyle);
					textStyle.removeAttribute(HTML.Attribute.HREF);
				}
				else if (action instanceof UserCombinedMessage)
				{
					//TODO
				}
				else if (action instanceof UserSoundMessageAction)
				{
			        try {
			        	DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, getAudioFormat());
			        	SourceDataLine sourceLine = (SourceDataLine)AudioSystem.getLine(dataLineInfo);
			        	sourceLine.open(getAudioFormat());
				        sourceLine.start();
				        sourceLine.write(((UserSoundMessageAction)action).getData(), 0, ((UserSoundMessageAction)action).getData().length);
				        sourceLine.drain();
				        sourceLine.close();
			        } catch (LineUnavailableException e) {
			        	logger.error("Can't play audio sample", e);
			        } catch (Exception e){
			        	logger.error("Can't play audio sample", e);
			        }
				}
				else if (action instanceof UsertextMessageAction)
				{
					UsertextMessageAction textAction = (UsertextMessageAction) action;
					appendMessage(chatPane, sourcePlayer.getName() + ": "+ textAction.message + "\n", textStyle);
				}
			}
		}
	}
	
	/*
	* When a different recipient is selected in the combo box,
	* switch to the corresponding dialog in the tabbed pane.
	*/
	class SendToListener implements ActionListener {
			SendToListener() {}

		@Override
		public void actionPerformed(java.awt.event.ActionEvent evt) {
			receiverPlayerName = (String) sendTo.getSelectedItem();
			if (receiverPlayerName == "all")
			{
				receiverPlayer = null;
			}
			else if ((receiverPlayer = game.getPlayerByName(receiverPlayerName)) == null)
			{
				logger.error("Player " + receiverPlayerName + " not found");
			}
			int receiverIndex = chatPanes.indexOfTab(receiverPlayerName);
			if (receiverIndex >= 0) {
				// When a player sends a private message for the first time, the chat tab does not yet exist.
				chatPanes.setSelectedIndex(receiverIndex);
			}
		}
	}

	class InputListener implements ActionListener {
		@Override
		public void actionPerformed(java.awt.event.ActionEvent evt) {
			String inputText = messageInput.getText();
			if(inputText.length() > 0) {
				messageInput.setText("");
				game.update(new UsertextMessageAction(id, player, receiverPlayer, inputText));
			}
		}
	}

    AudioFormat getAudioFormat() {
        float sampleRate = 44100;
        int sampleSizeInBits = 16;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                                             channels, signed, bigEndian);
        return format;
    }
	
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    
    public void keyChanged(KeyEvent arg0)
    {
    	if (arg0.isControlDown() == (line != null))
			return;
		if(arg0.isControlDown())
		{
			try {
	            AudioFormat format = getAudioFormat();
	            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
	 
	            // checks if system supports the data line
	            if (!AudioSystem.isLineSupported(info)) {logger.error("Line not supported");}
	            line = (TargetDataLine) AudioSystem.getLine(info);
	            line.open(format);
	            line.start();
	            final AudioInputStream ais = new AudioInputStream(line);
	            DataHandler.tp.run(new Runnable() {
	            	@Override
					public void run()
	            	{
	    				try {
							StreamUtil.copy(ais, bos);
						} catch (IOException e) {
							logger.error("Can't copy sound", e);
						}
	    				game.update(new UserSoundMessageAction(id, player, receiverPlayer, bos.toByteArray()));
	    	        	bos.reset();
	    	        	line.close();
	    		        line = null;
	            	}
	            }, "Capture sound");
	        } catch (LineUnavailableException ex) {
	            ex.printStackTrace();
	        }
		}
		else
		{
			line.stop();
			line.drain();
		}
    }
    
    TargetDataLine line = null;
	@Override
	public void keyPressed(KeyEvent arg0) {keyChanged(arg0);}

	@Override
	public void keyReleased(KeyEvent arg0) {keyChanged(arg0);}

	@Override
	public void keyTyped(KeyEvent arg0) {}
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

