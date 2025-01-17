package window;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import loloTable.PaddedCellRenderer;
import loloTable.PasswordTableModel;
import loloTable.TableEntry;

public class TableEntryRemover {
	private static final int WINDOW_WIDTH = 425;
	private static final int WINDOW_HEIGHT = 250;

	public static boolean display(TableEntry... entries) {
		// Create the panel
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		// Create the message at the top
		JLabel prompt = new JLabel();
		prompt.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(prompt);
		
		// Create the table
		JTable table = new JTable(new PasswordTableModel(new Object[] { "Title", "Username"}, 0));
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		scrollPane.setPreferredSize(new Dimension(WINDOW_WIDTH-50, WINDOW_HEIGHT-125));
		
		// Sort the table by the first column
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
		List<RowSorter.SortKey> sortKeys = new ArrayList<>();
		table.setRowSorter(sorter);
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);
		sorter.sort();
		
		// Customize the table
		table.setFocusable(false);
		table.getTableHeader().setReorderingAllowed(false);
		table.setRowHeight(25);
		table.setSelectionBackground(new Color(175, 212, 242));
		table.setSelectionForeground(new Color(0, 0, 0));
		table.setShowHorizontalLines(false);
		table.setGridColor(new Color(0, 0, 0, 35));
		scrollPane.getViewport().setBackground(Color.white);
		
		// Apply the padded cells to the table
		PaddedCellRenderer paddedCellRenderer = new PaddedCellRenderer();
		table.setDefaultRenderer(Object.class, paddedCellRenderer);
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(false);
		panel.add(scrollPane);
		
		// Add the entries to the table
		Object[] tableEntry = new Object[2];
		String title, username;
		for(int i = 0; i < entries.length; i++) {
			title = entries[i].getTitle();
			username = entries[i].getUsername();
			tableEntry[0] = (title.isEmpty())? "[No Title]" : title;
			tableEntry[1] = (username.isEmpty())? "[No Username]" : username;
			((PasswordTableModel)table.getModel()).addRow(tableEntry);
		}
		
		// Determine what to set the prompt
		boolean allInRecyclingBin = true;
		for(TableEntry entry : entries) {
			if(!entry.getCategory().equals("[Recycling Bin]"))
				allInRecyclingBin = false;
		}
		if(allInRecyclingBin) {
			if(entries.length == 1)
				prompt.setText("Are you sure you want to PERMANENTLY DELETE the following entry?");
			else
				prompt.setText("Are you sure you want to PERMANENTLY DELETE the following entries?");
		}
		else {
			if(entries.length == 1)
				prompt.setText("Are you sure you want to move the following entry to the recycling bin?");
			else
				prompt.setText("Are you sure you want to move the following entries to the recycling bin?");
		}

		// Display the window
		int option = JOptionPane.showConfirmDialog(null, panel, "Entry Remover", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
		if(option == JOptionPane.YES_OPTION) {
			JOptionPane.showInternalOptionDialog(null, "Press Okay to begin. This may take a while", "Removal Process", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new Object[] {"Okay"}, null);
			return true;
		}
		return false;
	}
}
