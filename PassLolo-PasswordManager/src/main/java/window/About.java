package window;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class About {
	private static final int WINDOW_WIDTH = 300;
	private static final int WINDOW_HEIGHT = 175;
	
	public static void display() {
		// The variables for readability
		int row, col;
		int width, height;
		double weightX=0, weightY=0;
		int anchor=GridBagConstraints.LINE_END, fill=GridBagConstraints.NONE;
		Insets insets = new Insets(5, 5, 5, -15);
		int ipadx = 0, ipady = 0;
		
		// Create the panel
		GridBagLayout layout = new GridBagLayout();
		JPanel panel = new JPanel(layout);
		
		// Create the message at the top
		JLabel prompt = new JLabel();
		prompt.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(prompt);
		
		// Add the vault label for author1
		row = 0; col = 0; width = 1; height = 1;
		anchor=GridBagConstraints.LINE_END;
		insets = new Insets(5, 5, 5, 5);
		GridBagConstraints authorLabelConstraints1 = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		JLabel authorLabel1 = new JLabel("Author:");
		panel.add(authorLabel1, authorLabelConstraints1);
		
		// Add the vault label for software1
		row = 1; col = 0; width = 1; height = 1;
		anchor=GridBagConstraints.LINE_END;
		insets = new Insets(5, 5, 5, 5);
		GridBagConstraints softwareLabelConstraints1 = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		JLabel softwareLabel1 = new JLabel("Software:");
		panel.add(softwareLabel1, softwareLabelConstraints1);
		
		// Add the vault label for version1
		row = 2; col = 0; width = 1; height = 1;
		anchor=GridBagConstraints.LINE_END;
		insets = new Insets(5, 5, 5, 5);
		GridBagConstraints versionLabelConstraints1 = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		JLabel versionLabel1 = new JLabel("Version:");
		panel.add(versionLabel1, versionLabelConstraints1);
		
		// Add the vault label for author2
		row = 0; col = 1; width = 1; height = 1;
		anchor=GridBagConstraints.LINE_START;
		insets = new Insets(5, 15, 5, 5);
		GridBagConstraints authorLabelConstraints2 = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		JLabel authorLabel2 = new JLabel("Alex Benlolo");
		panel.add(authorLabel2, authorLabelConstraints2);

		// Add the vault label for software2
		row = 1; col = 1; width = 1; height = 1;
		anchor=GridBagConstraints.LINE_START;
		insets = new Insets(5, 15, 5, 5);
		GridBagConstraints softwareLabelConstraints2 = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		JLabel softwareLabel2 = new JLabel("PassLolo - Password Manager");
		panel.add(softwareLabel2, softwareLabelConstraints2);
		
		// Add the vault label for version2
		row = 2; col = 1; width = 1; height = 1;
		anchor=GridBagConstraints.LINE_START;
		insets = new Insets(5, 15, 5, 5);
		GridBagConstraints versionLabelConstraints2 = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		JLabel versionLabel2 = new JLabel("2.1.2.1");
		panel.add(versionLabel2, versionLabelConstraints2);
		
		// Create the dialog
		JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {"Okay"}, null);
		JDialog dialog = new JDialog(new JFrame(), "About PassLolo", true);
		dialog.setIconImage(new ImageIcon("..\\resources\\windowIcon.png").getImage());
		dialog.setContentPane(pane);
		dialog.setResizable(false);	
		
		// Set the size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double screenWidth = screenSize.getWidth();
		double screenHeight = screenSize.getHeight();
		dialog.setBounds((int)screenWidth/2 - WINDOW_WIDTH/2, (int)screenHeight/2 - WINDOW_HEIGHT/2-50, WINDOW_WIDTH, WINDOW_HEIGHT);

		
		pane.addPropertyChangeListener(buttonPressed(dialog));
		
		// Apply the keyboard shortcuts and style
		recursizeShortcutKeys(dialog, pane, dialog);
		
		// Show the dialog
		dialog.setVisible(true);
	}
	
	/**
	 * Apply shortcut keys to the dialog.
	 * 
	 * @param root The root pane for all components.
	 * @param pane The option pane.
	 * @param dialog The window.
	 */
	private static void recursizeShortcutKeys(Component root, JOptionPane pane, JDialog dialog) {
		root.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				// Apply the accept action
				if (e.getKeyChar() == KeyEvent.VK_ENTER)
					dialog.setVisible(false);
				// Apply the cancel action
				else if (e.getKeyChar() == KeyEvent.VK_ESCAPE)
					dialog.setVisible(false);
			}
		});
		
		// Do the recursion
		if (root instanceof Container) {
			Component[] children = ((Container) root).getComponents();
			for (Component child : children)
				recursizeShortcutKeys(child, pane, dialog);
		}
	}
	
	/**
	 * React to the buttons being pressed
	 * 
	 * @param dialog The dialog.
	 * 
	 * @return A PropertyChangeListener.
	 */
	private static PropertyChangeListener buttonPressed(JDialog dialog) {
		return new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent pce) {
				dialog.setVisible(false);
			}
		};
	}
}
