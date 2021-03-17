package gui;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.DataHandler;
import data.JFrameLookAndFeelUtil;
import data.Options;
import gameObjects.action.GameAction;
import gameObjects.action.player.PlayerRemoveAction;
import gameObjects.functions.CheckingFunctions;
import gameObjects.functions.CheckingFunctions.GameInconsistency;
import gameObjects.instance.GameInstance;
import gameObjects.instance.GameInstance.GameChangeListener;
import gameObjects.instance.ObjectInstance;
import gui.minigames.TetrisGameInstance;
import gui.minigames.TetrisGameInstance.TetrisGameEvent;
import gui.minigames.TetrisGameInstance.TetrisGameListener;
import gui.minigames.TetrisWindow;
import io.GameIO;
import main.Player;
import net.AsynchronousGameConnection;
import net.AsynchronousGameConnection.PingCallback;
import net.SynchronousGameClientLobbyConnection;
import util.JFrameUtils;
import util.TimedUpdateHandler;

public class GameWindow extends JFrame implements ActionListener, LanguageChangeListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1441104795154034811L;
	public SynchronousGameClientLobbyConnection client;
	private final GameInstance gi;
	private final JSplitPane sliderRight;
	public GamePanel gamePanel;
	public IngameChatPanel chatPanel;
	public JToolBar toolBar;

	private final JMenu menuStatus = new JMenu();
	private final JMenu menuFile = new JMenu();
	private final JMenu menuExtras = new JMenu();
	private final JMenu menuControls = new JMenu();

	private final JMenuItem menuItemExit = new JMenuItem();
	private final JMenuItem menuItemEditGame = new JMenuItem();
	private final JMenuItem menuItemSaveGame = new JMenuItem();
	private final JMenuItem menuItemSettings = new JMenuItem();
	private final JMenuItem menuItemAbout = new JMenuItem();
	private final JMenuItem menuItemControls = new JMenuItem();
	private final JMenuItem menuItemTetris = new JMenuItem();

	private final JMenuItem menuItemStatusPlayerConsistency = new JMenuItem("Correct Card-Consistency");
	private final JMenuItem menuItemStatusGaiaConsistency = new JMenuItem("Correct Free-Object-Consistency");
	private final JMenuItem menuItemSyncPull = new JMenuItem();
	private final JMenuItem menuItemReconnect = new JMenuItem();


	private final LanguageHandler lh;
	private final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	private class GameWindowUpdater implements TimedUpdateHandler, Runnable
	{
		
		private final ArrayList<ObjectInstance> tmp = new ArrayList<>();
		private final ArrayList<ObjectInstance> tmp2 = new ArrayList<>();

		@Override
		public int getUpdateInterval() {
			return 1000;
		}

		@Override
		public synchronized void update() {
			JFrameUtils.runByDispatcher(this);
		}

		@Override
		public void run() {
		    GameInconsistency gic = CheckingFunctions.checkPlayerConsistency(gamePanel.getPlayerId(), tmp, tmp2, gi);
            boolean playerConsistent = gic == null;
            menuItemStatusPlayerConsistency.setEnabled(!playerConsistent);
            menuItemStatusPlayerConsistency.setText("Correct Card-Consistency" + (gic == null ? "" : (" (" + gic.toString() + ")")));
            gic = CheckingFunctions.checkPlayerConsistency(-1, tmp, tmp2, gi);
            boolean gaiaConsistent = gic == null;
            menuItemStatusGaiaConsistency.setEnabled(!gaiaConsistent);
            menuItemStatusGaiaConsistency.setText("Correct Free-Object-Consistency" + (gic == null ? "" : (" (" + gic.toString() + ")")));
            menuStatus.setForeground(playerConsistent && gaiaConsistent ? Color.BLACK : Color.RED);
            for (int i = 0; i < gi.getChangeListenerCount(); ++i)
            {
                GameChangeListener gcl = gi.getChangeListener(i);
                if (gcl instanceof AsynchronousGameConnection)
                {
                    AsynchronousGameConnection agc = (AsynchronousGameConnection)gcl;
                    agc.ping(pingCallback, System.nanoTime() + 1000000000);
                }
            }
        }
	}
	
    private PingCallback pingCallback = new PingCallback() {
        @Override
        public void run(AsynchronousGameConnection.PingInformation pi) {
            Color col = pi.isTimeouted() ? Color.RED : Color.BLACK;
            if (menuItemReconnect.getForeground() != col) {menuItemReconnect.setForeground(col);}
        }
    };

	private final GameWindowUpdater gww = new GameWindowUpdater();

	private static final Logger logger = LoggerFactory.getLogger(GameWindow.class);

	public GameWindow(GameInstance gi, LanguageHandler lh){this(gi, null, lh);}

	public GameWindow(GameInstance gi, Player player, LanguageHandler lh)
	{
		this.gi = gi;
		this.lh = lh;
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		JMenuBar menuBar = new JMenuBar();
		JToolBar toolBar = new JToolBar();
		toolBar.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(new JLabel("TableSize"));

		Integer[] tableSizes = new Integer[10];
		for (int i = 0; i < 10; ++i){
			tableSizes[i] = i;
		}
		toolBar.add(new JComboBox<Integer>(tableSizes));

		//this.add(toolBar, BorderLayout.WEST);
		menuBar.add(menuFile);
		menuBar.add(menuExtras);
		menuBar.add(menuControls);
		menuBar.add(menuStatus);
		setJMenuBar(menuBar);
		menuItemExit.addActionListener(this);
		menuItemEditGame.addActionListener(this);
		menuItemSaveGame.addActionListener(this);
		menuItemAbout.addActionListener(Credits.getOpenWindowListener());
		menuItemSettings.addActionListener(this);
		menuItemControls.addActionListener(this);
		menuItemTetris.addActionListener(this);
		menuFile.add(menuItemExit);
		menuFile.add(menuItemEditGame);
		menuFile.add(menuItemSaveGame);
		menuExtras.add(menuItemSettings);
		menuExtras.add(menuItemTetris);
		menuExtras.add(menuItemAbout);
		menuControls.add(menuItemControls);
		menuStatus.add(menuItemStatusPlayerConsistency);
		menuStatus.add(menuItemStatusGaiaConsistency);
		menuStatus.add(menuItemSyncPull);
		menuStatus.add(menuItemReconnect);
		menuItemStatusPlayerConsistency.addActionListener(this);
		menuItemStatusGaiaConsistency.addActionListener(this);
		menuItemSyncPull.addActionListener(this);
		menuItemReconnect.addActionListener(this);
		gamePanel = new GamePanel(gi, lh);
		gamePanel.setPlayer(player);
		chatPanel = new IngameChatPanel(gi, player);

		sliderRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,chatPanel, gamePanel);
		sliderRight.setOneTouchExpandable(true);

		sliderRight.setResizeWeight(0); // the chat panel will not be resized when resizing the window
		sliderRight.getLeftComponent().setMinimumSize(new Dimension());
		sliderRight.setDividerLocation(0.0d);
		this.add(sliderRight);

		setLayout(new GridLayout(1, 1));
		setSize((int) screenSize.getWidth(), (int) screenSize.getHeight());
		JFrameLookAndFeelUtil.addToUpdateTree(this);
		languageChanged(lh.getCurrentLanguage());
		lh.addLanguageChangeListener(this);
		DataHandler.timedUpdater.add(gww);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		gi.addChangeListener(new GameChangeListener() {
			
			@Override
			public void changeUpdate(GameAction action) {
				if (action instanceof PlayerRemoveAction)
				{	
					if (((PlayerRemoveAction)action).editedPlayer == player.id)
					{
						JFrameUtils.logErrorAndShow("You were removed from the game", null, logger);
						dispose();
					}
				}
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object source = arg0.getSource();
		if(source == menuItemExit)			{this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));}
		else if (source == menuItemEditGame){new EditGameWindow(gamePanel, gi, lh, gamePanel.getPlayer()).setVisible(true);}
		else if (source == menuItemSaveGame)
		{
			JFileChooser fileChooser = new JFileChooser();
			if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
			{
				try {
					File file = fileChooser.getSelectedFile();
					if (!file.getName().endsWith(".zip"))
					{
						file = new File(file.getAbsolutePath() + ".zip");
					}
					FileOutputStream out = new FileOutputStream(file);
					GameIO.writeSnapshotToZip(gi, out);
					out.close();
				} catch (IOException e) {
					JFrameUtils.logErrorAndShow("Couldn't save game", e, logger );
				}
			}
		}
		else if (source == menuItemSettings)
		{
			OptionWindow ow = new OptionWindow(lh);
			ow.setVisible(true);
			ow.setAlwaysOnTop(true);
		}
		else if (source == menuItemControls){
			ControlWindow cw = new ControlWindow(gi, lh);
			cw.setVisible(true);
			cw.setAlwaysOnTop(true);
		}
		else if (source == menuItemTetris)
		{
			TetrisWindow tw = new TetrisWindow();
			TetrisGameInstance tgi = tw.getGameInstance();
			tgi.addGameListener(new TetrisGameListener() {
				
				@Override
				public void actionPerformed(TetrisGameEvent event) {
					if (event.source == tgi.source){gamePanel.gameInstance.update(event);}
				}
			});
			gamePanel.gameInstance.addChangeListener(tgi);
			tw.setVisible(true);
			tw.setAlwaysOnTop(true);
		}
		else if (source == menuItemStatusPlayerConsistency)
		{
			gi.repairPlayerConsistency(gamePanel.getPlayerId(), gamePanel.getPlayer(), new ArrayList<>());
			JFrameUtils.runByDispatcher(gww);
		}
		else if (source == menuItemStatusGaiaConsistency)
		{
			gi.repairPlayerConsistency(-1, gamePanel.getPlayer(), new ArrayList<>());
			JFrameUtils.runByDispatcher(gww);
		}
		else if (source == menuItemSyncPull)
		{
		    for (int i = 0; i < gi.getChangeListenerCount(); ++i)
		    {
                GameChangeListener gcl = gi.getChangeListener(i);
                if (gcl instanceof AsynchronousGameConnection)
                {
                    ((AsynchronousGameConnection) gcl).syncPull();
                }
		    }
		}
		else if (source == menuItemReconnect)
		{
			try {
				SynchronousGameClientLobbyConnection client = null;
				for (int i = 0; i < gi.getChangeListenerCount(); ++i)
				{
					GameChangeListener gcl = gi.getChangeListener(i);
					if (gcl instanceof AsynchronousGameConnection)
					{
						Socket socket = ((AsynchronousGameConnection)gcl).getSocket();
						client = new SynchronousGameClientLobbyConnection(socket.getInetAddress().getCanonicalHostName(), socket.getPort());
						break;
					}
				}
				Player player = this.gamePanel.getPlayer();
				GameInstance gi = client.getGameInstance(this.gi.name);
				gi.password = this.gi.password;
				AsynchronousGameConnection connection = client.connectToGameSession(gi, gi.password);
				player = gi.addPlayer(null, player);
				connection.syncPull();
				connection.start();
				GameWindow gw = new GameWindow(gi, player, lh);
				gw.setVisible(true);
				setVisible(false);
				dispose();
			}catch(Exception e){
				JFrameUtils.logErrorAndShow("Reconnect failed", e, logger);
			}
		}
	}

	@Override
	public void languageChanged(Language language) {
		menuItemExit.setText(         language.getString(Words.exit));
		menuItemEditGame.setText(     language.getString(Words.edit));
		menuItemSaveGame.setText(     language.getString(Words.save));
		menuItemSettings.setText(     language.getString(Words.settings));
		menuItemAbout.setText(        language.getString(Words.about));
		menuItemControls.setText(     language.getString(Words.controls));
		menuItemTetris.setText(       language.getString(Words.tetris));
		menuFile.setText(             language.getString(Words.files));
		menuExtras.setText(           language.getString(Words.extras));
		menuControls.setText(         language.getString(Words.controls));
		menuItemReconnect.setText(    language.getString(Words.reconnect));
		menuItemSyncPull.setText(     language.getString(Words.sync_pull));
		menuStatus.setText(           language.getString(Words.status));
		//Set Title of the window
		StringBuilder strB = new StringBuilder();
		strB.append(language.getString(Words.game) + ": " + gi.name);
		
        Player pl = gamePanel.getPlayer();
		if (gi.admin == gamePanel.getPlayerId()) {strB.append(" (Admin Mode)");}
		strB.append(", ");
		if (pl.visitor){strB.append(" (Visitor Mode), ");}
		strB.append(pl.getName()).append(" (Id: ").append(gamePanel.getPlayerId()).append("), ").append(lh.getCurrentLanguage().getString(Words.server)).append(": ").append(Options.getString("last_connection.address"));
		this.setTitle(strB.toString());
	}
}
