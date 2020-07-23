package loloTable;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


public class PaddedCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 6001897431907305255L;
	
	// The rendering component
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		// Add padding to the left
		setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		
		// Add the alternating colors
		if(!isSelected) {
			if (row % 2 == 1) 
				c.setBackground(new Color(245, 240, 240));
			else
				c.setBackground(Color.white);
		}
		
		return this;
	}
}