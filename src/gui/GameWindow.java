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
import gameObjects.functions.CheckingFunctions;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import gui.minigames.TetrisGameInstance;
import gui.minigames.TetrisGameInstance.TetrisGameEvent;
import gui.minigames.TetrisGameInstance.TetrisGameListener;
import gui.minigames.TetrisWindow;
import io.GameIO;
import main.Player;
import util.JFrameUtils;
import util.TimedUpdateHandler;

public class GameWindow extends JFrame implements ActionListener, LanguageChangeListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1441104795154034811L;
	private final GameInstance gi;
	private final JSplitPane sliderRight;
	public GamePanel gamePanel;
	public IngameChatPanel chatPanel;
	public JToolBar toolBar;
	private final JMenuItem menuItemExit = new JMenuItem();
	private final JMenuItem menuItemEditGame = new JMenuItem();
	private final JMenuItem menuItemSaveGame = new JMenuItem();
	private final JMenuItem menuItemSettings = new JMenuItem();
	private final JMenuItem menuItemAbout = new JMenuItem();
	private final JMenuItem menuItemControls = new JMenuItem();
	private final JMenuItem menuItemTetris = new JMenuItem();
	private final JMenu menuStatus = new JMenu("Status");
	private final JMenuItem menuItemStatusPlayerConsistency = new JMenuItem("Correct Card-Consistency");
	private final JMenuItem menuItemStatusGaiaConsistency = new JMenuItem("Correct Free-Object-Consistency");
	private final JMenu menuFile = new JMenu();
	private final JMenu menuExtras = new JMenu();
	private final JMenu menuControls = new JMenu();

	private final LanguageHandler lh;
	
	private final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private class GameWindowUpdater implements TimedUpdateHandler
	{
		
		private final ArrayList<ObjectInstance> tmp = new ArrayList<>();

		@Override
		public int getUpdateInterval() {
			return 1000;
		}

		@Override
		public synchronized void update() {
			boolean playerConsistent = CheckingFunctions.checkPlayerConsistency(gamePanel.player.id, tmp, gi); 
			menuItemStatusPlayerConsistency.setEnabled(!playerConsistent);
			boolean gaiaConsistent = CheckingFunctions.checkPlayerConsistency(-1, tmp, gi);
			menuItemStatusGaiaConsistency.setEnabled(!gaiaConsistent);
			menuStatus.setForeground(playerConsistent && gaiaConsistent ? Color.BLACK : Color.RED);
		}
	}
	private final GameWindowUpdater gww = new GameWindowUpdater();
	
	private static final Logger logger = LoggerFactory.getLogger(GameWindow.class);
	
	public GameWindow(GameInstance gi, LanguageHandler lh)
	{
		this(gi, null, lh);
	}
	
	
	public GameWindow(GameInstance gi, Player player, LanguageHandler lh)
	{
		this.gi = gi;
		this.lh = lh;
		if (gi.admin == player.id) {
			this.setTitle(gi.name + " (Admin Mode)");
		}
		else {
			this.setTitle(gi.name);
		}
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
		menuItemStatusPlayerConsistency.addActionListener(this);
		menuItemStatusGaiaConsistency.addActionListener(this);
		gamePanel = new GamePanel(gi, lh);
		gamePanel.player = player;
		chatPanel = new IngameChatPanel(gi, player);

		sliderRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,chatPanel, gamePanel);
		sliderRight.setOneTouchExpandable(true);
		sliderRight.setResizeWeight(0); // the chat panel will not be resized when resizing the window
		this.add(sliderRight);

		setLayout(new GridLayout(1, 1));
		setSize((int) screenSize.getWidth(), (int) screenSize.getHeight());
		sliderRight.setDividerLocation(0.0d);
		JFrameLookAndFeelUtil.addToUpdateTree(this);
		languageChanged(lh.getCurrentLanguage());
		lh.addLanguageChangeListener(this);
		DataHandler.timedUpdater.add(gww);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object source = arg0.getSource();
		if(source == menuItemExit)
		{
			this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}
		else if (source == menuItemEditGame )
		{
			new EditGameWindow(gi, lh, gamePanel.player).setVisible(true);
		}
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
					if (event.source == tgi.source)
					{
						gamePanel.gameInstance.update(event);
					}
				}
			});
			gamePanel.gameInstance.addChangeListener(tgi);
			tw.setVisible(true);
			tw.setAlwaysOnTop(true);
		}
		else if (source == menuItemStatusPlayerConsistency)
		{
			gi.repairPlayerConsistency(gamePanel.player.id, gamePanel.player, new ArrayList<>());
			gww.update();
		}
		else if (source == menuItemStatusGaiaConsistency)
		{
			gi.repairPlayerConsistency(-1, gamePanel.player, new ArrayList<>());
			gww.update();
		}
	}

	@Override
	public void languageChanged(Language language) {
		menuItemExit.setText(		language.getString(Words.exit));
		menuItemEditGame.setText(	language.getString(Words.edit));
		menuItemSaveGame.setText(	language.getString(Words.save));
		menuItemSettings.setText(	language.getString(Words.settings));
		menuItemAbout.setText(		language.getString(Words.about));
		menuItemControls.setText(	language.getString(Words.controls));
		menuItemTetris.setText(		language.getString(Words.tetris));
		menuFile.setText(			language.getString(Words.files));
		menuExtras.setText(			language.getString(Words.extras));
		menuControls.setText(		language.getString(Words.controls));
	}
	
}
