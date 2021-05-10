package gui.minigames;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import data.JFrameLookAndFeelUtil;
import util.JFrameUtils;

public class TetrisWindow extends JFrame implements WindowListener{
	/**
	 *
	 */
	private static final long serialVersionUID = -6147856397016097419L;
	private final TetrisPanel tp = new TetrisPanel(new TetrisGameInstance());

	public TetrisGameInstance getGameInstance()
	{
		return tp.getGameInstance();
	}

	public TetrisWindow()
	{
		setLayout(JFrameUtils.SINGLE_ROW_LAYOUT);
		add(tp);
		JFrameLookAndFeelUtil.addToUpdateTree(this);
		setSize(300, 600);
		tp.start();
		addWindowListener(this);
	}

	@Override
	public void windowActivated(WindowEvent arg0) {}

	@Override
	public void windowClosed(WindowEvent arg0) {

	}

	@Override
	public void windowClosing(WindowEvent arg0) {}

	@Override
	public void windowDeactivated(WindowEvent arg0) {}

	@Override
	public void windowDeiconified(WindowEvent arg0) {}

	@Override
	public void windowIconified(WindowEvent arg0) {}

	@Override
	public void windowOpened(WindowEvent arg0) {}
}
