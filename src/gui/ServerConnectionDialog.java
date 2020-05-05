package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.SynchronousGameClientLobbyConnection;

public class ServerConnectionDialog extends JFrame implements ActionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3954762140525349396L;
	JLabel labelAddress = new JLabel("Server");
	JTextField textFieldAddress = new JTextField();
	JLabel labelName = new JLabel("Name");
	JTextField textFieldName = new JTextField();
	JButton buttonConnect = new JButton();
	JButton buttonCancel = new JButton();
	JButton buttonStartServer = new JButton();
	private final LanguageHandler lh;
	public ServerConnectionDialog(LanguageHandler lh)
	{
		this("", -1, lh);
	}

	public ServerConnectionDialog(String string, int i, LanguageHandler lh) {
		this.lh = lh;
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		
		layout.setHorizontalGroup(
		layout.createParallelGroup().addGroup(
		layout.createSequentialGroup().addComponent(labelAddress).addComponent(textFieldAddress)).addGroup(
		layout.createSequentialGroup().addComponent(labelName).addComponent(textFieldName)).addGroup(
		layout.createSequentialGroup().addComponent(buttonConnect).addComponent(buttonCancel).addComponent(buttonStartServer)));
		
		layout.setVerticalGroup(
		layout.createSequentialGroup().addGroup(
		layout.createParallelGroup().addComponent(labelAddress).addComponent(textFieldAddress)).addGroup(
		layout.createParallelGroup().addComponent(labelName).addComponent(textFieldName)).addGroup(
		layout.createParallelGroup().addComponent(buttonConnect).addComponent(buttonCancel).addComponent(buttonStartServer)));
		setSize(1000, 500);
		buttonConnect.addActionListener(this);
		buttonStartServer.addActionListener(this);
		textFieldAddress.setText(string);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object source = arg0.getSource();
		if (source == buttonConnect)
		{
			ServerLobbyWindow slw = new ServerLobbyWindow(new SynchronousGameClientLobbyConnection(textFieldAddress.getText(), 1234), lh);
		}
		else if (source == buttonStartServer)
		{
			ServerControlWindow scw = new ServerControlWindow();
			scw.setVisible(true);
		}
	}
	

}
