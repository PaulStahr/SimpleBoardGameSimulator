package gui;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.JFrameLookAndFeelUtil;
import data.Options;
import gameObjects.GameInstanceColumnType;
import gameObjects.GameMetaInfo;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import io.GameIO;
import main.Player;
import net.AsynchronousGameConnection;
import net.GameServer;
import net.SynchronousGameClientLobbyConnection;
import util.JFrameUtils;
import util.jframe.JFileChooserRecentFiles;
import util.jframe.PasswordDialog;
import util.jframe.table.ButtonColumn;
import util.jframe.table.TableModel;

public class ServerLobbyWindow extends JFrame implements ActionListener, ListSelectionListener, TableModelListener, LanguageChangeListener{
	/**
	 * 
	 */
	private static final Logger logger = LoggerFactory.getLogger(ServerLobbyWindow.class);
	private static final long serialVersionUID = 6569919447688866509L;
	public final SynchronousGameClientLobbyConnection client;
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
    private final JButton buttonCreateGame = new JButton();
    private final JButton buttonStartServer = new JButton();
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
 	private final ButtonColumn deleteColumn = new ButtonColumn(tableOpenGames,tableAction, GameInstance.TYPES.indexOf(GameInstanceColumnType.DELETE));
 	
	@Override
	public void actionPerformed(ActionEvent e)
    {
		Object source = e.getSource();
		if (source == buttonPoll)
		{
			try {
				client.setAdress(textFieldAddress.getText());
				client.setPort(Integer.parseInt(textFieldPort.getText()));
				gmi.clear();
				client.getGameInstanceMeta(gmi);
				JFrameUtils.updateTable(tableOpenGames, scrollPaneOpenGames, gmi, GameInstance.TYPES, tableModelOpenGames, connectColumn, deleteColumn);
			} catch (IOException | ClassNotFoundException e1) {
				JFrameUtils.logErrorAndShow("Can't update information", e1, logger);
			}
			
			
		}
		else if (source == buttonCreateGame)
		{
			JFileChooser fileChooser = new JFileChooserRecentFiles();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			{
				Options.set("last_connection.name", textFieldName.getText());
				try
				{
					File file = fileChooser.getSelectedFile();
					GameInstance gi = new GameInstance(new Game(), null);
					if (file.isDirectory())
					{
						GameIO.readSnapshotFromFolder(file, gi);
					}
					else if(file.getName().endsWith(".zip"))
					{
						GameIO.readSnapshotFromZip(new FileInputStream(file), gi);
					}
					else
					{
						JFrameUtils.logErrorAndShow("Can't interpret Game", new IOException(), logger);
					}
					if (gi.name == null)
					{
						gi.name = "Unnamed";
					}
					Player player = new Player(textFieldName.getText(), Integer.parseInt(textFieldId.getText()));
					GameWindow gw = new GameWindow(gi, player, lh);
			    	client.pushGameSession(gi);
			    	try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						logger.error("Unnexpected interrupt", e1);
					}
			    	client.addPlayerToGameSession(player, gi.name, gi.password);
			    	AsynchronousGameConnection connection = client.connectToGameSession(gi, gi.password);
			    	connection.start();
			    	gi.addPlayer(player);
			    	gw.setVisible(true);
				}catch(IOException | JDOMException ex)
				{
					JFrameUtils.logErrorAndShow("Can't connect to Server", ex, logger);
				}
			}
		}
		else if (source == buttonStartServer)
		{
			GameServer gs = new GameServer(Integer.parseUnsignedInt(textFieldPort.getText()));
			gs.start();
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
						Options.set("last_connection.address", textFieldAddress.getText());
						Options.set("last_connection.port", Integer.valueOf(textFieldPort.getText()));
						Options.set("last_connection.name", textFieldName.getText());
						Options.set("last_connection.id", playerId);
						Player player = new Player(textFieldName.getText(), playerId);
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
						
						client.addPlayerToGameSession(player, gi.name, gi.password);
				    	GameWindow gw = new GameWindow(gi, player, lh);
				    	AsynchronousGameConnection connection = client.connectToGameSession(gi, gi.password);
				    	connection.syncPull();
				    	connection.start();
				    	gw.setVisible(true);
					} catch (IOException | JDOMException e1) {
						JFrameUtils.logErrorAndShow("Can't connect to server", e1, logger);
					}
		 	    }
				else if (GameInstance.TYPES.get(col) == GameInstanceColumnType.DELETE)
				{
					GameInstance gi;
					try {
						gi = client.getGameInstance((String)tableModelOpenGames.getValueAt(row, GameInstance.TYPES.indexOf(GameInstanceColumnType.ID)));
						client.deleteGame(gi.name, gi.password);
					} catch (IOException | JDOMException e2) {
						JFrameUtils.logErrorAndShow("Can't delete game", e2, logger);
					}
				}
			}
		}
    }
	
	public ServerLobbyWindow(SynchronousGameClientLobbyConnection client, LanguageHandler lh)
	{
		this.lh = lh;
		lh.addLanguageChangeListener(this);
		languageChanged(lh.getCurrentLanguage());
		Container content = getContentPane();
		GroupLayout layout = new GroupLayout(content);
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
				.addGroup(layout.createSequentialGroup()
						.addComponent(buttonPoll)
						.addComponent(buttonCreateGame)
						.addComponent(buttonStartServer)));
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
				.addGroup(layout
						.createParallelGroup()
						.addComponent(buttonPoll)
						.addComponent(buttonCreateGame)
						.addComponent(buttonStartServer)));
		setLayout(layout);
		buttonPoll.addActionListener(this);
		tableOpenGames.getSelectionModel().addListSelectionListener(this);
		tableOpenGames.getModel().addTableModelListener(this);
		this.client = client;
		buttonCreateGame.addActionListener(this);
		buttonStartServer.addActionListener(this);
		textFieldAddress.setText(client.getAddress());
		textFieldPort.setText(Integer.toString(client.getPort()));
		JFrameLookAndFeelUtil.addToUpdateTree(this);
		setMinimumSize(getPreferredSize());
	}
	
    @Override
	public void tableChanged(TableModelEvent e) {
    	if (!EventQueue.isDispatchThread())
    	{
    		throw new RuntimeException("Table Changes only allowed by dispatchment thread");
    	}
       	if (!isUpdating )
		{
       		int rowBegin = e.getFirstRow();
        	if (rowBegin == TableModelEvent.HEADER_ROW)
        	{
        		return;
        	}
        	isUpdating = true;
 			Object source = e.getSource();
    		int colBegin = e.getColumn() == TableModelEvent.ALL_COLUMNS ? 0 : e.getColumn();
        	int rowEnd = e.getLastRow() + 1;
			if (source == tableModelOpenGames)
			{
				
				//tablechanged(e, tableOperGames, scene.surfaceObjectList, colBegin, rowBegin, rowEnd, tableModelOpenGames, GameInstance.TYPES);
			}

			isUpdating = false;
		}
    }
    

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void languageChanged(Language language) {
		buttonPoll.setText(language.getString(Words.refresh));
		buttonCreateGame.setText(language.getString(Words.new_game));
		buttonStartServer.setText(language.getString(Words.start_server));
		labelAddress.setText(language.getString(Words.server_address));
		labelPort.setText(language.getString(Words.port));
		labelName.setText(language.getString(Words.name));
		labelId.setText(language.getString(Words.id));
	}
}
