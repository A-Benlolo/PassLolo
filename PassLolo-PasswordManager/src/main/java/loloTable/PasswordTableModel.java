package loloTable;

import javax.swing.table.DefaultTableModel;

public class PasswordTableModel extends DefaultTableModel {
	private static final long serialVersionUID = -5490939282473098979L;

	/**
	 * Create a password table model.
	 * 
	 * @param columns The title of the columns.
	 * @param rowCount The number of rows in the table.
	 */
	public PasswordTableModel(Object[] columns, int rowCount) {
		super(columns, rowCount);
	}

	/**
	 * Determine if the specified cell is editable.
	 * <p>
	 * This will always return false.
	 * 
	 * @param row The row that the cell is in.
	 * @param col The column the cell is in.
	 */
	public boolean isCellEditable(int row, int col){  
		return false;
	}

	/**
	 * Add a TableEntry to the table.
	 * 
	 * @param row The TableEntry to add.
	 */
	public void addRow(TableEntry row) {		
		super.addRow(row.toMinimalObjectArray());
	}
}
