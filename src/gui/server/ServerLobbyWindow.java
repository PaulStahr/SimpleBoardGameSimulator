package gui.server;

import static test.SimpleNetworkServertest.connectAndStartGame;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.BooleanSupplier;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.DataHandler;
import data.JFrameLookAndFeelUtil;
import data.Options;
import data.Options.OptionTreeNode;
import gameObjects.GameMetaInfo;
import gameObjects.action.player.PlayerAddAction;
import gameObjects.columnTypes.GameInstanceColumnType;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gui.create.CreateNewGameWindow;
import gui.game.GameWindow;
import gui.language.Language;
import gui.language.LanguageChangeListener;
import gui.language.LanguageHandler;
import gui.language.Words;
import io.GameIO;
import main.Player;
import net.AsynchronousGameConnection;
import net.GameServer;
import net.SynchronousGameClientLobbyConnection;
import test.SimpleNetworkServertest;
import util.JFrameUtils;
import util.ThreadPool.RunnableObject;
import util.jframe.JFileChooserRecentFiles;
import util.jframe.PasswordDialog;
import util.jframe.table.ButtonColumn;
import util.jframe.table.TableModel;

public class ServerLobbyWindow extends JFrame implements ActionListener, ListSelectionListener, TableModelListener, LanguageChangeListener {
	/**
	 * 
	 */
	private static final Logger logger = LoggerFactory.getLogger(ServerLobbyWindow.class);
	private static final long serialVersionUID = 6569919447688866509L;
	public final SynchronousGameClientLobbyConnection client;
	private final JProgressBar loadProgressBar = new JProgressBar(0, 100);
	private final JLabel progressBarTitle = new JLabel();

	private final JTextField textFieldName = new JTextField(Options.getString("last_connection.name"));
	//private final JTextField textFieldChat = new JTextField();
	//private final JTextArea textAreaChat = new JTextArea();
	/*TODOS here:
	show a list of lokally installed games
	*/
	private final DefaultTableModel tableModelOpenGames = new TableModel(GameInstance.TYPES);
	private final JTable tableOpenGames = new JTable(tableModelOpenGames);
	private final JScrollPane scrollPaneOpenGames = new JScrollPane(tableOpenGames);
    private final JButton buttonPoll = new JButton();
    private final JToggleButton buttonAutoPoll = new JToggleButton();
    private final JButton buttonLoadGame = new JButton();
    private final JButton buttonStartServer = new JButton();
	private final JButton buttonCreateGame = new JButton();

    private final JTextField textFieldId = new JTextField(String.valueOf(Options.getInteger("last_connection.id")));
    private final JLabel labelAddress = new JLabel();
    private final JLabel labelPort = new JLabel();
    private final JLabel labelName = new JLabel();
    private final JLabel labelId = new JLabel();
    private final JTextField textFieldAddress = new JTextField(Options.getString("last_connection.address"));
    private final JTextField textFieldPort = new JTextField(String.valueOf(Options.getInteger("last_connection.port")));
	private boolean isUpdating = false;
	private final LanguageHandler lh;
	private final ArrayList<GameMetaInfo> gmi = new ArrayList<>();
	
    private final AbstractAction tableAction = new AbstractAction() {
    	private static final long serialVersionUID = 3980835476835695337L;
			@Override
			public void actionPerformed(ActionEvent e)
 	    {
			ServerLobbyWindow.this.actionPerformed(e);
 	    }
    };
 	private final ButtonColumn connectColumn = new ButtonColumn(tableOpenGames,tableAction, GameInstance.TYPES.indexOf(GameInstanceColumnType.CONNECT));
	private final ButtonColumn visitColumn = new ButtonColumn(tableOpenGames,tableAction, GameInstance.TYPES.indexOf(GameInstanceColumnType.VISIT));
 	private final ButtonColumn deleteColumn = new ButtonColumn(tableOpenGames,tableAction, GameInstance.TYPES.indexOf(GameInstanceColumnType.DELETE));
    private GameServer gs;





	@Override
	public void actionPerformed(ActionEvent e)
    {
		Object source = e.getSource();
		if (source == buttonPoll || source == buttonAutoPoll)
		{
			updateCurrentGames();
		}
		else if (source == buttonLoadGame)
		{
			JFileChooser fileChooser = new JFileChooserRecentFiles();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			{
				Options.set("last_connection.name", textFieldName.getText());
				try
				{
					File file = fileChooser.getSelectedFile();
					GameInstance gi = new GameInstance(new Game("foo"), null);
					if (file.isDirectory())
					{
						progressBarTitle.setVisible(true);
						loadProgressBar.setVisible(true);
						repaint();
						GameIO.readSnapshotFromFolder(file, gi);
						//showProgressBar(false);
					}
					else if(file.getName().endsWith(".zip"))
					{
						progressBarTitle.setVisible(true);
						loadProgressBar.setVisible(true);
						repaint();
						GameIO.readSnapshotFromZip(new FileInputStream(file), gi);
						//showProgressBar(false);
					}
					else
					{//TODO
						JFrameUtils.logErrorAndShow("Unknown filetype", new IOException(), logger);
					}
					if (gi.name == null)
					{
						gi.name = "Unnamed";
					}
					//Player player = new Player(textFieldName.getText(), Integer.parseInt(textFieldId.getText()));
					client.pushGameSession(gi);
			    	try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						logger.error("Unnexpected interrupt", e1);
					}
			    	//client.addPlayerToGameSession(player, gi.name, gi.password);
			    	AsynchronousGameConnection connection = client.connectToGameSession(gi, gi.password);
			    	connection.start();
			    	gi.begin_play();
			    	//gi.addPlayer(null, player);
			    	//GameWindow gw = new GameWindow(gi, player, lh);
			    	//gw.setVisible(true);
				}catch(IOException | JDOMException ex)
				{
					JFrameUtils.logErrorAndShow("Can't connect to Server", ex, logger);
				}
			}
			updateCurrentGames();
		}
		else if (source == buttonStartServer)
		{
			if (gs == null) {
				gs = new GameServer(Integer.parseUnsignedInt(textFieldPort.getText()));
				JFrameUtils.showInfoMessage(lh.getCurrentLanguage().getString(Words.server_start_info), logger);
				buttonStartServer.setText(lh.getCurrentLanguage().getString(Words.stop_server));
				DataHandler.addKeepProgramPredicate(new BooleanSupplier() {
	                @Override
	                public boolean getAsBoolean() {
	                    return gs.isRunning();
	                }
	            });
				gs.start();
			}
			else{
				buttonStartServer.setText(lh.getCurrentLanguage().getString(Words.start_server));
				JFrameUtils.showInfoMessage(lh.getCurrentLanguage().getString(Words.server_stop_info), logger);
				gs.stop();
				gs = null;
			}
		}
		else if (source == buttonCreateGame){
			String address = "127.0.0.1";
			int port = 8000 + (int)(Math.random() * 100);
			GameServer gs = SimpleNetworkServertest.startNewServer(port);
			Player player = new Player("NewGame", 1);
			FileInputStream fis = null;
			try {
				String game_string = DataHandler.getResourceFolder() + "StartGame.zip";
				fis = new FileInputStream("src/resources/StartGame.zip");
			} catch (IOException fileNotFoundException) {
				logger.error("File not Found", fileNotFoundException);
			}
			GameInstance gi = new GameInstance(new Game("foo"), null);
			try {
				GameIO.readSnapshotFromZip(fis, gi);
			} catch (IOException ioException) {
				ioException.printStackTrace();
			} catch (JDOMException jdomException) {
				jdomException.printStackTrace();
			}
			gi.name = "Create Game";
			player = gi.addPlayer(new PlayerAddAction(-1, player));
			try {
				fis.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
			try {
				connectAndStartGame(address, port, player, gi, lh);
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
			CreateNewGameWindow ow = new CreateNewGameWindow(lh);
			ow.setVisible(true);
		}
		else if (e instanceof ButtonColumn.TableButtonActionEvent)
		{
			ButtonColumn.TableButtonActionEvent event = (ButtonColumn.TableButtonActionEvent)e;
			Object tableSource = event.getSource();
			int col = event.getCol();
			int row = event.getRow();
			if (tableSource == tableModelOpenGames)
			{
				if (GameInstance.TYPES.get(col) == GameInstanceColumnType.CONNECT)
				{
					try
					{
						int playerId = Integer.parseInt(textFieldId.getText());
						OptionTreeNode lc = Options.getNode("last_connection");
						Options.set(lc, "address", textFieldAddress.getText());
						Options.set(lc, "port", Integer.valueOf(textFieldPort.getText()));
						Options.set(lc, "name", textFieldName.getText());
						Options.set(lc, "id", playerId);
						Player player = new Player(textFieldName.getText(), playerId, false);
						String password = null;
						if (gmi.get(row).passwordRequired)
						{
							JPasswordField pf = new JPasswordField(10);
							int option = PasswordDialog.showOptionDialog("Passwort eingeben", "Ok", "Cancel", pf);
							if (option == JOptionPane.OK_OPTION)
							{
								password = new String(pf.getPassword());
							}
						}
						GameInstance gi = null;
                        try {
                            gi = client.getGameInstance((String)tableModelOpenGames.getValueAt(row, GameInstance.TYPES.indexOf(GameInstanceColumnType.ID)));
                        } catch (JDOMException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
				    	//GameInstance gi = new GameInstance(new Game(), (String)tableModelOpenGames.getValueAt(row, GameInstance.TYPES.indexOf(GameInstanceColumnType.ID)));
						if (password != null)
				    	{
				    		gi.password = password;
				    	}
				    	AsynchronousGameConnection connection = client.connectToGameSession(gi, gi.password);
				    	player = gi.addPlayer(new PlayerAddAction(-1, player));
				    	connection.syncPull();
				    	connection.start();
				    	GameWindow gw = new GameWindow(gi, player, lh);
				    	gw.setVisible(true);
				    	gw.client = client;
					} catch (IOException e1) {
						JFrameUtils.logErrorAndShow("Can't connect to server", e1, logger);
					}
					updateCurrentGames();
		 	    }
				else if (GameInstance.TYPES.get(col) == GameInstanceColumnType.VISIT)
				{
					try
					{
						int playerId = Integer.parseInt(textFieldId.getText());
						OptionTreeNode last_connection = Options.getNode("last_connection");
						Options.set(last_connection, "address", textFieldAddress.getText());
						Options.set(last_connection, "port", Integer.valueOf(textFieldPort.getText()));
						Options.set(last_connection, "name", textFieldName.getText());
						Options.set(last_connection, "id", playerId);
						Player player = new Player(textFieldName.getText(), playerId, true);
						String password = null;
						if (gmi.get(row).passwordRequired)
						{
							JPasswordField pf = new JPasswordField(10);
							int option = PasswordDialog.showOptionDialog("Passwort eingeben", "Ok", "Cancel", pf);
							if (option == JOptionPane.OK_OPTION)
							{
								password = new String(pf.getPassword());
							}
						}
						GameInstance gi = client.getGameInstance((String)tableModelOpenGames.getValueAt(row, GameInstance.TYPES.indexOf(GameInstanceColumnType.ID)));
						if (password != null)
						{
							gi.password = password;
						}

						//client.addPlayerToGameSession(player, gi.name, gi.password);
						AsynchronousGameConnection connection = client.connectToGameSession(gi, gi.password);
						player = gi.addPlayer(new PlayerAddAction(-1, player));
						connection.syncPull();
						connection.start();
						GameWindow gw = new GameWindow(gi, player, lh);
						gw.setVisible(true);
					} catch (IOException | JDOMException e1) {
						JFrameUtils.logErrorAndShow("Can't connect to server", e1, logger);
					}
					updateCurrentGames();
				}

				else if (GameInstance.TYPES.get(col) == GameInstanceColumnType.DELETE)
				{
					GameInstance gi;
					try {
						int playerId = Integer.parseInt(textFieldId.getText());
						gi = client.getGameInstance((String)tableModelOpenGames.getValueAt(row, GameInstance.TYPES.indexOf(GameInstanceColumnType.ID)));
						if (playerId == gi.admin) {
							client.deleteGame(gi.name, gi.password);
							updateCurrentGames();
						}
						else{
							JFrameUtils.showInfoMessage("Can't delete game. You have no admin rights!", logger);
						}
					} catch (IOException | JDOMException e2) {
						JFrameUtils.logErrorAndShow("Can't delete game", e2, logger);
					}
				}
			}
		}
    }

	private RunnableObject updateGamesRunnable = new RunnableObject("Update Games", null) {
	    @Override
        public void run() {
	        buttonPoll.setEnabled(false);
            try {
                client.setAdress(textFieldAddress.getText());
                client.setPort(Integer.parseInt(textFieldPort.getText()));
                gmi.clear();
                client.getGameInstanceMeta(gmi);
                JFrameUtils.runByDispatcher(new Runnable() {
                    @Override
                    public void run() {
                        JFrameUtils.updateTable(tableOpenGames, scrollPaneOpenGames, gmi, GameInstance.TYPES, tableModelOpenGames, null, connectColumn, visitColumn, deleteColumn);                                                 
                    }
                });
            } catch (IOException | ClassNotFoundException e1) {
                if (!buttonAutoPoll.isSelected())
                {
                    JFrameUtils.runByDispatcher(new Runnable() {
                        @Override
                        public void run() {
                            JFrameUtils.logErrorAndShow("Can't update information", e1, logger);
                        }
                    });
                }
            }
            buttonPoll.setEnabled(true);
            if (buttonAutoPoll.isSelected()) {
                DataHandler.hs.enqueue(updateCurrentGamesRunnable, System.nanoTime() + 500000000, false);
            }
        }
	};
	
	private final Runnable updateCurrentGamesRunnable = new Runnable() {
        @Override
        public void run() {
            updateCurrentGames();
        }
    };

	private void updateCurrentGames() {DataHandler.tp.run(updateGamesRunnable, false);}

	public ServerLobbyWindow(SynchronousGameClientLobbyConnection client, LanguageHandler lh)
	{
		this.lh = lh;
		lh.addLanguageChangeListener(this);
		languageChanged(lh.getCurrentLanguage());
		Container content = getContentPane();
		GroupLayout layout = new GroupLayout(content);
		loadProgressBar.setStringPainted(true);
		loadProgressBar.setVisible(false);
		progressBarTitle.setVisible(false);
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
						.addComponent(labelAddress)
						.addComponent(textFieldAddress)
						.addComponent(labelPort)
						.addComponent(textFieldPort)
						.addComponent(labelName)
						.addComponent(textFieldName)
						.addComponent(labelId)
						.addComponent(textFieldId))
				.addComponent(scrollPaneOpenGames)
				.addComponent(progressBarTitle)
				.addComponent(loadProgressBar)
				.addGroup(layout.createSequentialGroup()
						.addComponent(buttonPoll)
						.addComponent(buttonAutoPoll)
						.addComponent(buttonLoadGame)
						.addComponent(buttonStartServer)
						.addComponent(buttonCreateGame)));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout
						.createParallelGroup()
						.addComponent(labelAddress)
						.addComponent(textFieldAddress)
						.addComponent(labelPort)
						.addComponent(textFieldPort)
						.addComponent(labelName)
						.addComponent(textFieldName)
						.addComponent(labelId)
						.addComponent(textFieldId))
				.addComponent(scrollPaneOpenGames)
				.addComponent(progressBarTitle)
				.addComponent(loadProgressBar)
				.addGroup(layout
						.createParallelGroup()
						.addComponent(buttonPoll)
						.addComponent(buttonAutoPoll)
						.addComponent(buttonLoadGame)
						.addComponent(buttonStartServer)
						.addComponent(buttonCreateGame)));
		setLayout(layout);
		buttonPoll.addActionListener(this);
		buttonAutoPoll.addActionListener(this);
		tableOpenGames.getSelectionModel().addListSelectionListener(this);
		tableOpenGames.getModel().addTableModelListener(this);
		this.client = client;
		buttonLoadGame.addActionListener(this);
		buttonStartServer.addActionListener(this);
		buttonCreateGame.addActionListener(this);
		JFrameLookAndFeelUtil.addToUpdateTree(this);
		setMinimumSize(getPreferredSize());
		updateCurrentGames();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener(DataHandler.closeProgramWindowListener);
	}
	
    @Override
	public void tableChanged(TableModelEvent e) {
    	if (!EventQueue.isDispatchThread()){throw new RuntimeException("Table Changes only allowed by dispatchment thread");}
       	if (isUpdating ){return;}
   		int rowBegin = e.getFirstRow();
    	if (rowBegin == TableModelEvent.HEADER_ROW){return;}
    	isUpdating = true;
		

		isUpdating = false;
    }
    

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void languageChanged(Language language) {
		buttonPoll.setText(language.getString(Words.refresh));
		buttonAutoPoll.setText(language.getString(Words.autorefresh));
		buttonLoadGame.setText(language.getString(Words.load_game));
		buttonStartServer.setText(language.getString(Words.start_server));
		buttonCreateGame.setText(language.getString(Words.create_game));
		labelAddress.setText(language.getString(Words.server_address));
		labelPort.setText(language.getString(Words.port));
		labelName.setText(language.getString(Words.player_name));
		labelId.setText(language.getString(Words.player_id));
		progressBarTitle.setText(language.getString(Words.load_game));
		this.setTitle(language.getString(Words.game_list));
	}

	public void showProgressBar(boolean show){

		SwingUtilities.updateComponentTreeUI(progressBarTitle);
		SwingUtilities.updateComponentTreeUI(loadProgressBar);
	}
}
