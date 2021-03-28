package util.jframe;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class PasswordDialog extends JOptionPane{
	
	/**
     * 
     */
    private static final long serialVersionUID = -3884312520153866856L;

    public static int showOptionDialog(String message, String yes, String no, JPasswordField pass)
	{
		JPanel panel = new JPanel();
		JLabel label = new JLabel(message);
		panel.add(label);
		panel.add(pass);
		String[] options = new String[]{yes, no};
		return JOptionPane.showOptionDialog(null, panel, "The title",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[1]);
	}

}
