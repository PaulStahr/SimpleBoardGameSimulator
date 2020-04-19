package util.jframe.table;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 *  The ButtonColumn class provides a renderer and an editor that looks like a
 *  JButton. The renderer and editor will then be used for a specified column
 *  in the table. The TableModel will contain the String to be displayed on
 *  the button.
 *
 *  The button can be invoked by a mouse click or by pressing the space bar
 *  when the cell has focus. Optionally a mnemonic can be set to invoke the
 *  button. When the button is invoked the provided Action is invoked. The
 *  source of the Action will be the table. The action command will contain
 *  the model row number of the button that was clicked.
 *
 */
public class ButtonColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener, MouseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1921455947364822841L;
	private Action action;
	private int mnemonic;
	private int column;
	private static final Border originalBorder = new JButton().getBorder();
	private static final Border focusBorder = new LineBorder(Color.BLUE);

	private JButton renderButton = new JButton();
	private JButton editButton = new JButton();
	private JTable table;
	private Object editorValue;
	private boolean isButtonColumnEditor;

	/**
	 *  Create the ButtonColumn to be used as a renderer and editor. The
	 *  renderer and editor will automatically be installed on the TableColumn
	 *  of the specified column.
	 *
	 *  @param table the table containing the button renderer/editor
	 *  @param action the Action to be invoked when the button is invoked
	 *  @param column the column to which the button renderer/editor is added
	 */
	public ButtonColumn(JTable table, Action action, int column)
	{
		this.column = column;
		this.action = action;

		editButton.setFocusPainted( false );
		editButton.addActionListener( this );
		setFocusBorder( focusBorder );

		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(column).setCellRenderer( this );
		columnModel.getColumn(column).setCellEditor( this );
		table.addMouseListener( this );
		this.table = table;
	}

	public ButtonColumn(Action action, int column)
	{
		this.column = column;
		this.action = action;

		editButton.setFocusPainted( false );
		editButton.addActionListener( this );
		setFocusBorder( focusBorder );
	}

	public void setTable(JTable table)
	{
		table.removeMouseListener(this);
		this.table =table;
		table.addMouseListener(this);
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(column).setCellRenderer( this );
		columnModel.getColumn(column).setCellEditor( this );		
	}

	/**
	 *  Get foreground color of the button when the cell has focus
	 *
	 *  @return the foreground color
	 */
	public Border getFocusBorder()
	{
		return focusBorder;
	}

	/**
	 *  The foreground color of the button when the cell has focus
	 *
	 *  @param focusBorder the foreground color
	 */
	public void setFocusBorder(Border focusBorder)
	{
		editButton.setBorder( focusBorder );
	}

	public int getMnemonic()
	{
		return mnemonic;
	}

	/**
	 *  The mnemonic to activate the button when the cell has focus
	 *
	 *  @param mnemonic the mnemonic
	 */
	public void setMnemonic(int mnemonic)
	{
		this.mnemonic = mnemonic;
		renderButton.setMnemonic(mnemonic);
		editButton.setMnemonic(mnemonic);
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		editButton.setIcon(value instanceof Icon ? (Icon)value : null);
		editButton.setText(value == null || value instanceof Icon ? "" :  value.toString());
		this.editorValue = value;
		return editButton;
	}

	@Override
	public Object getCellEditorValue()
	{
		return editorValue;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		if (isSelected)
		{
			renderButton.setForeground(table.getSelectionForeground());
	 		renderButton.setBackground(table.getSelectionBackground());
		}
		else
		{
			renderButton.setForeground(table.getForeground());
			renderButton.setBackground(UIManager.getColor("Button.background"));
		}

		renderButton.setBorder(hasFocus ? focusBorder : originalBorder);
		renderButton.setIcon(value instanceof Icon ? (Icon)value : null);
		renderButton.setText(value == null || value instanceof Icon ? "" :  value.toString());

		return renderButton;
	}

	public final static class TableButtonActionEvent extends ActionEvent
	{
		private final int col, row;
		private final ButtonColumn bc;
		public final int getCol()
		{
			return col;
		}
		
		public final int getRow()
		{
			return row;
		}
		
		public final ButtonColumn getButton()
		{
			return bc;
		}
		
		public TableButtonActionEvent(Object source, int id, String command, int row, int col, ButtonColumn bc) {
			super(source, id, command);
			this.row = row;
			this.col = col;
			this.bc = bc;
		}

		public TableButtonActionEvent(Object source, int id, String command, int modifiers, int row, int col, ButtonColumn bc) {
			super(source, id, command, modifiers);
			this.row = row;
			this.col = col;
			this.bc = bc;
		}
		
		public TableButtonActionEvent(Object source, int id, String command, long when, int modifiers, int row, int col, ButtonColumn bc) {
			super(source, id, command, when, modifiers);
			this.row = row;
			this.col = col;
			this.bc = bc;
		}

/**
		 * 
		 */
		private static final long serialVersionUID = 7650661768933708670L;
		
	}
	
//
//  Implement ActionListener interface
//
	/*
	 *	The button has been pressed. Stop editing and invoke the custom Action
	 */
	public void actionPerformed(ActionEvent e)
	{
		int row = table.convertRowIndexToModel( table.getEditingRow() );
		int col = table.convertColumnIndexToModel( table.getEditingColumn() );
		fireEditingStopped();
		action.actionPerformed(new TableButtonActionEvent(table.getModel(),ActionEvent.ACTION_PERFORMED,String.valueOf(row), row, col, this));
	}

//
//  Implement MouseListener interface
//
	/*
	 *  When the mouse is pressed the editor is invoked. If you then then drag
	 *  the mouse to another cell before releasing it, the editor is still
	 *  active. Make sure editing is stopped when the mouse is released.
	 */
    public void mousePressed(MouseEvent e)
    {
    	JTable table = (JTable)e.getSource();
    	if (table.isEditing() && table.getCellEditor() == this)
    	{
			isButtonColumnEditor = true;
    		
    	}
    	if (table.getColumnModel().getColumn(column).getCellEditor() != this)
		{
    		table.removeMouseListener(this);
		}
    }

    public void mouseReleased(MouseEvent e)
    {
    	JTable table = (JTable)e.getSource();
    	if (isButtonColumnEditor && table.isEditing())
    	{
    		table.getCellEditor().stopCellEditing();
    	}
		isButtonColumnEditor = false;
    	if (table.getColumnModel().getColumn(column).getCellEditor() != this)
		{
    		table.removeMouseListener(this);
		}
    }

    public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}