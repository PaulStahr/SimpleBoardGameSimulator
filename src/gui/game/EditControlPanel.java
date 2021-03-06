package gui.game;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import data.ControlCombination;
import gameObjects.action.GameAction;
import gameObjects.instance.GameInstance.GameChangeListener;
import gui.language.Language;
import gui.language.LanguageChangeListener;
import gui.language.LanguageHandler;
import gui.language.Words;

public class EditControlPanel extends JPanel implements ActionListener, GameChangeListener, Runnable, MouseListener, TableModelListener, LanguageChangeListener {


    /**
	 * 
	 */
	private static final long serialVersionUID = -1686844504581572726L;
	private static final FuncControl[] boardControls = {
			new FuncControl(Words.move_board, new ControlCombination(InputEvent.ALT_DOWN_MASK, 0,  -1,0), new ControlCombination(0, -1,  -1,16)),
			new FuncControl(Words.rotate, new ControlCombination(InputEvent.ALT_DOWN_MASK, 2,  -1, 0), new ControlCombination(0, -1,  KeyEvent.VK_UP, 0), new ControlCombination(0, -1,  KeyEvent.VK_DOWN, 0)),
			new FuncControl(Words.zoom, new ControlCombination(InputEvent.ALT_DOWN_MASK, -1,  -1, 4), new ControlCombination(0, -1,  KeyEvent.VK_PLUS, 0), new ControlCombination(0, -1,  KeyEvent.VK_MINUS, 0)),
			new FuncControl(Words.sit_down, new ControlCombination(0, 0,  -1, 32)),
			new FuncControl(Words.sit_to_own_seat, new ControlCombination(0, -1,  KeyEvent.VK_SPACE, 0)),
			new FuncControl(Words.hide_show_table, new ControlCombination(InputEvent.ALT_DOWN_MASK, -1,  KeyEvent.VK_T, 0)),
			new FuncControl(Words.hide_and_show_hand_area, new ControlCombination(0, -1, KeyEvent.VK_H,0)),
	};

	private static final FuncControl[] objectControls= {
			new FuncControl(Words.select_objects, 	new ControlCombination(0, 0,  -1,0)),
			new FuncControl(Words.select_multiple_objects, 	new ControlCombination(InputEvent.CTRL_DOWN_MASK, 0,  -1,0)),
			new FuncControl(Words.move_object, 		new ControlCombination(0, 0, -1, 1)),
			new FuncControl(Words.move_stack,	 	new ControlCombination(InputEvent.SHIFT_DOWN_MASK, 0, -1, 1),new ControlCombination(0, 1, -1, 1)),
			new FuncControl(Words.get_top_n_card, 	new ControlCombination(0, 1, -1, 5)),
			new FuncControl(Words.get_bottom_card, 	new ControlCombination(InputEvent.SHIFT_DOWN_MASK, 2,  -1, 1)),
			new FuncControl(Words.rotate_object, 	new ControlCombination(0, -1,  KeyEvent.VK_R, 0)),
			new FuncControl(Words.shuffle_stack, 	new ControlCombination(0, -1, KeyEvent.VK_S, 0)),
			new FuncControl(Words.merge_objects, 	new ControlCombination(0, -1, KeyEvent.VK_M, 0)),
			new FuncControl(Words.play_card_face_up, new ControlCombination(0, 0, -1, 32)),
			new FuncControl(Words.collect_all_objects_of_a_group, new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1,  KeyEvent.VK_M, 0)),
			new FuncControl(Words.flip_objects_roll_dice, new ControlCombination(0, -1,  KeyEvent.VK_F, 0)),
			new FuncControl(Words.unfold_dice, new ControlCombination(0, -1,  KeyEvent.VK_V, 0)),
			new FuncControl(Words.zoom, new ControlCombination(InputEvent.ALT_DOWN_MASK, -1,  -1, 4), new ControlCombination(0, -1,  KeyEvent.VK_PLUS, 0), new ControlCombination(0, -1,  KeyEvent.VK_MINUS, 0)),
			new FuncControl(Words.fix_object, new ControlCombination(InputEvent.ALT_DOWN_MASK, -1,  KeyEvent.VK_F, 0)),

	};

	private static final FuncControl[] privateAreaControls= {
			new FuncControl(Words.hide_and_show_hand_area, new ControlCombination(0, -1, KeyEvent.VK_H,0)),
			new FuncControl(Words.distribute_cards_to_players, new ControlCombination(0, -1,  KeyEvent.VK_G, 0)),
			new FuncControl(Words.take_object_to_hand, new ControlCombination(0, -1,  KeyEvent.VK_T, 0), new ControlCombination(0, 0,  -1, 1)),
			new FuncControl(Words.take_object_to_hand_face_down, new ControlCombination(0, 2,  -1,1)),
			new FuncControl(Words.play_card_face_up, new ControlCombination(0, 1,  -1, 8)),
			new FuncControl(Words.play_card_face_down, new ControlCombination(0, 2,  -1, 8)),
			new FuncControl(Words.play_active_hand_card, new ControlCombination(0, -1, KeyEvent.VK_P, 0), new ControlCombination(0, 0, -1, 32)),
			new FuncControl(Words.drop_all_hand_cards, new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1, KeyEvent.VK_D, 0)),
			new FuncControl(Words.drop_active_hand_card, new ControlCombination(0, -1, KeyEvent.VK_D, 0)),
			new FuncControl(Words.zoom, new ControlCombination(0, -1,  -1, 4), new ControlCombination(0, -1,  KeyEvent.VK_PLUS, 0), new ControlCombination(0, -1,  KeyEvent.VK_MINUS, 0)),
	};

	private static final FuncControl[] countControls = {
			new FuncControl(Words.count_card_number, new ControlCombination(0, -1,  KeyEvent.VK_C, 0)),
			new FuncControl(Words.count_card_value, new ControlCombination(InputEvent.CTRL_DOWN_MASK, -1, KeyEvent.VK_C, 0)),
	};


	JLabel boardControlsLabel = new JLabel("Board Controls");
    JTable boardControlsTable = new JTable(boardControls.length, 2);
    JScrollPane boardControlPane = new JScrollPane(boardControlsTable);

    JLabel objectControlsLabel = new JLabel("Object Controls");
    JTable objectControlsTable = new JTable(objectControls.length, 2);
    JScrollPane objectControlsPane = new JScrollPane(objectControlsTable);

    JLabel privateAreaControlsLabel = new JLabel("Hand Card Area");
    JTable privateAreaControlsTable = new JTable(privateAreaControls.length, 2);
    JScrollPane privateAreaControlsPane = new JScrollPane(privateAreaControlsTable);


    JLabel countControlsLabel = new JLabel("Control Hand Cards");
    JTable countControlsTable = new JTable(countControls.length, 2);
    JScrollPane countControlsPane = new JScrollPane(countControlsTable);
    //keyAssignmentTable.setFillsViewportHeight(true);

    public EditControlPanel(LanguageHandler lh){
        languageChanged(lh.getCurrentLanguage());
        lh.addLanguageChangeListener(this);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(boardControlsLabel);
        this.add(boardControlPane);
        this.add(objectControlsLabel);
        this.add(objectControlsPane);
        this.add(privateAreaControlsLabel);
        this.add(privateAreaControlsPane);
        this.add(countControlsLabel);
        this.add(countControlsPane);
    }
    @Override
    public void changeUpdate(GameAction action) {

    }
    
	
	private static class FuncControl
	{
		Words word;
		ControlCombination cc[];
		
		public FuncControl(Words word, ControlCombination... cc){
			this.word = word;
			this.cc = cc;
		}

		public String toString(Language lang) {
			StringBuilder strB = new StringBuilder();
			for (int i = 0; i < cc.length; ++i)
			{
				StringBuilder strC = new StringBuilder();
				if (i != 0)
				{
					strB.append(',');
				}
				cc[i].toString(strC, lang);
				strB.append(strC);
			}
			return strB.toString();
		}
	}


    @Override
    public void languageChanged(Language lang) {
		String[] columnNames = {lang.getString(Words.description), lang.getString(Words.key)};
		for (int col = 0; col < 2; ++col)
		{
		 	boardControlsTable.getColumnModel().getColumn(col).setHeaderValue(columnNames[col]);
		 	privateAreaControlsTable.getColumnModel().getColumn(col).setHeaderValue(columnNames[col]);
     		objectControlsTable.getColumnModel().getColumn(col).setHeaderValue(columnNames[col]);
		 	countControlsTable.getColumnModel().getColumn(col).setHeaderValue(columnNames[col]);
		}
        for (int row = 0; row < boardControls.length; ++row)
 		{
            boardControlsTable.getModel().setValueAt(lang.getString(boardControls[row].word), row, 0);
            boardControlsTable.getModel().setValueAt(boardControls[row].toString(lang), row, 1);
 		}
 		for (int row = 0; row < objectControls.length; ++row)
 		{
 			objectControlsTable.getModel().setValueAt(lang.getString(objectControls[row].word), row, 0);
            objectControlsTable.getModel().setValueAt(objectControls[row].toString(lang), row, 1);
 		}
 		for (int row = 0; row < privateAreaControls.length; ++row)
 		{
 			privateAreaControlsTable.getModel().setValueAt(lang.getString(privateAreaControls[row].word), row, 0);
            privateAreaControlsTable.getModel().setValueAt(privateAreaControls[row].toString(lang), row, 1);
 		}
 		for (int row = 0; row < countControls.length; ++row)
 		{
 			countControlsTable.getModel().setValueAt(lang.getString(countControls[row].word), row, 0);
            countControlsTable.getModel().setValueAt(countControls[row].toString(lang), row, 1);
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

