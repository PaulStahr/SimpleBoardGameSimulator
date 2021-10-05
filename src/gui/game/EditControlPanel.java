package gui.game;


import gui.language.Language;
import gui.language.LanguageChangeListener;
import gui.language.LanguageHandler;
import gui.language.Words;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class EditControlPanel extends JPanel implements ActionListener, Runnable, MouseListener, TableModelListener, LanguageChangeListener {

    private static final UserControlString uc = new UserControlString();
    /**
	 *
	 */
	private static final long serialVersionUID = -1686844504581572726L;



	JLabel boardControlsLabel = new JLabel("Board Controls");
    JTable boardControlsTable = new JTable(uc.boardControls.length, 2);
    JScrollPane boardControlPane = new JScrollPane(boardControlsTable);

    JLabel objectControlsLabel = new JLabel("Object Controls");
    JTable objectControlsTable = new JTable(uc.objectControls.length, 2);
    JScrollPane objectControlsPane = new JScrollPane(objectControlsTable);

    JLabel cardControlsLabel = new JLabel("Card Controls");
    JTable cardControlsTable = new JTable(uc.cardControls.length, 2);
    JScrollPane cardControlsPane = new JScrollPane(cardControlsTable);

    JLabel diceControlsLabel = new JLabel("Dice Controls");
    JTable diceControlsTable = new JTable(uc.diceControls.length, 2);
    JScrollPane diceControlsPane = new JScrollPane(diceControlsTable);
    
    JLabel boxControlsLabel = new JLabel("Box Controls");
    JTable boxControlsTable = new JTable(uc.boxControls.length, 2);
    JScrollPane boxControlsPane = new JScrollPane(boxControlsTable);

    JLabel bookControlsLabel = new JLabel("Box Controls");
    JTable bookControlsTable = new JTable(uc.bookControls.length, 2);
    JScrollPane bookControlsPane = new JScrollPane(bookControlsTable);

    JLabel privateAreaControlsLabel = new JLabel("Hand Card Area");
    JTable privateAreaControlsTable = new JTable(uc.privateAreaControls.length, 2);
    JScrollPane privateAreaControlsPane = new JScrollPane(privateAreaControlsTable);


    JLabel countControlsLabel = new JLabel("Control Hand Cards");
    JTable countControlsTable = new JTable(uc.countControls.length, 2);
    JScrollPane countControlsPane = new JScrollPane(countControlsTable);
    //keyAssignmentTable.setFillsViewportHeight(true);

    public EditControlPanel(LanguageHandler lh){
        languageChanged(lh.getCurrentLanguage());
        lh.addLanguageChangeListener(this);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(boardControlsLabel);
        this.add(boardControlPane);
        this.add(privateAreaControlsLabel);
        this.add(privateAreaControlsPane);
        this.add(objectControlsLabel);
        this.add(objectControlsPane);
        this.add(cardControlsLabel);
        this.add(cardControlsPane);
        this.add(diceControlsLabel);
        this.add(diceControlsPane);
        this.add(boxControlsLabel);
        this.add(boxControlsPane);
        this.add(bookControlsLabel);
        this.add(bookControlsPane);
        this.add(countControlsLabel);
        this.add(countControlsPane);
    }



    @Override
    public void languageChanged(Language lang) {
		String[] columnNames = {lang.getString(Words.description), lang.getString(Words.key)};
		for (int col = 0; col < 2; ++col)
		{
		 	boardControlsTable.getColumnModel().getColumn(col).setHeaderValue(columnNames[col]);
            privateAreaControlsTable.getColumnModel().getColumn(col).setHeaderValue(columnNames[col]);
            objectControlsTable.getColumnModel().getColumn(col).setHeaderValue(columnNames[col]);
            cardControlsTable.getColumnModel().getColumn(col).setHeaderValue(columnNames[col]);
            diceControlsTable.getColumnModel().getColumn(col).setHeaderValue(columnNames[col]);
            boxControlsTable.getColumnModel().getColumn(col).setHeaderValue(columnNames[col]);
            bookControlsTable.getColumnModel().getColumn(col).setHeaderValue(columnNames[col]);
		 	countControlsTable.getColumnModel().getColumn(col).setHeaderValue(columnNames[col]);
		}
        for (int row = 0; row < uc.boxControls.length; ++row)
 		{
            boardControlsTable.getModel().setValueAt(lang.getString(uc.boardControls[row].word), row, 0);
            boardControlsTable.getModel().setValueAt(uc.boardControls[row].toString(lang), row, 1);
 		}
        for (int row = 0; row < uc.privateAreaControls.length; ++row)
        {
            privateAreaControlsTable.getModel().setValueAt(lang.getString(uc.privateAreaControls[row].word), row, 0);
            privateAreaControlsTable.getModel().setValueAt(uc.privateAreaControls[row].toString(lang), row, 1);
        }
 		for (int row = 0; row < uc.objectControls.length; ++row)
 		{
 			objectControlsTable.getModel().setValueAt(lang.getString(uc.objectControls[row].word), row, 0);
            objectControlsTable.getModel().setValueAt(uc.objectControls[row].toString(lang), row, 1);
 		}
        for (int row = 0; row < uc.cardControls.length; ++row)
        {
            cardControlsTable.getModel().setValueAt(lang.getString(uc.cardControls[row].word), row, 0);
            cardControlsTable.getModel().setValueAt(uc.cardControls[row].toString(lang), row, 1);
        }
        for (int row = 0; row < uc.diceControls.length; ++row)
        {
            diceControlsTable.getModel().setValueAt(lang.getString(uc.diceControls[row].word), row, 0);
            diceControlsTable.getModel().setValueAt(uc.diceControls[row].toString(lang), row, 1);
        }
        for (int row = 0; row < uc.boxControls.length; ++row)
        {
            boxControlsTable.getModel().setValueAt(lang.getString(uc.boxControls[row].word), row, 0);
            boxControlsTable.getModel().setValueAt(uc.boxControls[row].toString(lang), row, 1);
        }
        for (int row = 0; row < uc.bookControls.length; ++row)
        {
            bookControlsTable.getModel().setValueAt(lang.getString(uc.bookControls[row].word), row, 0);
            bookControlsTable.getModel().setValueAt(uc.bookControls[row].toString(lang), row, 1);
        }
 		for (int row = 0; row < uc.countControls.length; ++row)
 		{
 			countControlsTable.getModel().setValueAt(lang.getString(uc.countControls[row].word), row, 0);
            countControlsTable.getModel().setValueAt(uc.countControls[row].toString(lang), row, 1);
 		}
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void run() {

    }

    @Override
    public void tableChanged(TableModelEvent e) {

    }
}

