package window;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

public class Settings {
	// Sizes
	private static final int WINDOW_WIDTH = 475;
	private static final int WINDOW_HEIGHT = 300;
	private static final Dimension SEPARATOR_DIMENSIONS = new Dimension(300, 1);
	
	// The editors
	private static JCheckBox clearClipboardCheckBox; 
	private static JSpinner clearClipboardDelaySpinner;
	private static JCheckBox autoGeneratePasswordCheckBox; 
	private static JCheckBox autoCheckDictionaryCheckBox; 
	private static JCheckBox rememberLastOpenedVaultCheckBox; 
	private static JCheckBox showEntriesOnTreeCheckBox; 
	private static JCheckBox onlySearchVisibleFieldsCheckBox; 
	
	// The labels
	private static JLabel clearClipboardLabel;
	private static JLabel clearClipboardDelayLabel;
	private static JLabel autoGeneratePasswordLabel;
	private static JLabel autoCheckDictionaryLabel;
	private static JLabel rememberLastOpenedVaultLabel;
	private static JLabel showEntriesOnTreeLabel;
	private static JLabel onlySearchVisibleFieldsLabel;
	
	// The sections
	private static JLabel clipboardSection;
	private static JLabel entryCreationSection;
	private static JLabel personalizationSection;
	private static JLabel categoryTreeSection;
	private static JLabel searchSection;
	
	// The trackers
	private static boolean useSettings = false;
	
	public static Object[] display(Object... settings) {
		// Reset the trackers
		useSettings = false;
		
		// The variables for readability
		int row = 0;

		// The panel that contains the message
		GridBagLayout layout = new GridBagLayout();
		JPanel panel = new JPanel(layout);
		panel.setBackground(Color.white);
		JScrollPane scrollPane = new JScrollPane(panel);

		// Add the section for clipboard
		clipboardSection = new JLabel();
		addSeparator(panel, clipboardSection, "Clipboard", row++);
		// Add the clear clipboard setting
		clearClipboardLabel = new JLabel();
		clearClipboardCheckBox = new JCheckBox();
		addSettingsLine(panel, row++, clearClipboardLabel, clearClipboardCheckBox, "Start timer to clear clipboard after copying (recommended)", settings[0]);
		// Add the delay for clipboard clearing settings
		clearClipboardDelayLabel = new JLabel();
		clearClipboardDelaySpinner = new JSpinner();
		addSettingsLine(panel, row++, clearClipboardDelayLabel, clearClipboardDelaySpinner, "Delay before clearing clipboard", settings[1]);
		
		// Add the section for entry creation
		entryCreationSection = new JLabel();
		addSeparator(panel, entryCreationSection, "Entry Creation", row++);
		// Add the auto generate password setting
		autoGeneratePasswordLabel = new JLabel();
		autoGeneratePasswordCheckBox = new JCheckBox();
		addSettingsLine(panel, row++, autoGeneratePasswordLabel, autoGeneratePasswordCheckBox, "Automatically generate password on changed settings", settings[2]);
		// Add the auto check dictionary setting
		autoCheckDictionaryLabel = new JLabel();
		autoCheckDictionaryCheckBox = new JCheckBox();
		addSettingsLine(panel, row++, autoCheckDictionaryLabel, autoCheckDictionaryCheckBox, "Check dictionary on submission", settings[3]);
		
		// Add the section for personalization
		personalizationSection = new JLabel();
		addSeparator(panel, personalizationSection, "Personalization", row++);
		// Add the remember last opened vault setting
		rememberLastOpenedVaultLabel = new JLabel();
		rememberLastOpenedVaultCheckBox = new JCheckBox();
		addSettingsLine(panel, row++, rememberLastOpenedVaultLabel, rememberLastOpenedVaultCheckBox, "Remember last opened vault", settings[4]);
		
		// Add the category tree section
		categoryTreeSection = new JLabel();
		addSeparator(panel, categoryTreeSection, "Category Tree", row++);
		// Add the show entries on tree setting
		showEntriesOnTreeLabel = new JLabel();
		showEntriesOnTreeCheckBox = new JCheckBox();
		addSettingsLine(panel, row++, showEntriesOnTreeLabel, showEntriesOnTreeCheckBox, "Show entries on category tree", settings[5]);
		
		// Add the search section
		searchSection = new JLabel();
		addSeparator(panel, searchSection, "Search", row++);
		// Add the search only visible fields setting
		onlySearchVisibleFieldsLabel = new JLabel();
		onlySearchVisibleFieldsCheckBox = new JCheckBox();
		addSettingsLine(panel, row++, onlySearchVisibleFieldsLabel, onlySearchVisibleFieldsCheckBox, "Include only visible fields in search", settings[6]);
		
		// Create the option pane and dialog
		JOptionPane pane = new JOptionPane(scrollPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {"Apply", "Cancel"}, null);
		JDialog dialog = new JDialog(new JFrame(), "Settings", true);
		dialog.setIconImage(new ImageIcon("..\\resources\\windowIcon.png").getImage());
		dialog.setContentPane(pane);
		dialog.setResizable(false);
		
		// Set the size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double screenWidth = screenSize.getWidth();
		double screenHeight = screenSize.getHeight();
		dialog.setBounds((int)screenWidth/2 - WINDOW_WIDTH/2, (int)screenHeight/2 - WINDOW_HEIGHT/2-50, WINDOW_WIDTH, WINDOW_HEIGHT);

		// Make the spinner commit changes on edit instead of lost focus
		JComponent comp1 = clearClipboardDelaySpinner.getEditor();
		JFormattedTextField field1 = (JFormattedTextField) comp1.getComponent(0);
		DefaultFormatter formatter1 = (DefaultFormatter) field1.getFormatter();
		formatter1.setCommitsOnValidEdit(true);
		
		// Add the listener to ensure a negative time is not used
		clearClipboardDelaySpinner.addChangeListener(dontAllowNegativeNumbers(clearClipboardDelaySpinner));
		
		// Add the listener to disable the spinner if the check box is unselected
		clearClipboardCheckBox.addChangeListener(disableComponent(clearClipboardCheckBox, clearClipboardDelayLabel, clearClipboardDelaySpinner));
		
		// Add the closing listeners
		pane.addPropertyChangeListener(buttonPressed(dialog));
		dialog.addWindowListener(windowClosing(dialog));
		
		// Add the listener to submit when enter is pressed in the correct field
		recursizeShortcutKeys(dialog, pane, dialog);
		
		// Display the dialog
		dialog.setVisible(true);
		
		if(useSettings) {
			return new Object[] {
					clearClipboardCheckBox.isSelected(),
					clearClipboardDelaySpinner.getValue(),
					autoGeneratePasswordCheckBox.isSelected(),
					autoCheckDictionaryCheckBox.isSelected(),
					rememberLastOpenedVaultCheckBox.isSelected(),
					showEntriesOnTreeCheckBox.isSelected(),
					onlySearchVisibleFieldsCheckBox.isSelected()
			};
		}
		else
			return null;
	}
	
	/**
	 * Add a line to the settings for a setting
	 * 
	 * @param panel The panel to add to.
	 * @param row The row on the panel.
	 * @param label The label describing the settings.
	 * @param editor The components that changes the setting.
	 * @param text The text for the label.
	 * @param value The value for the editor.
	 */
	private static void addSettingsLine(JPanel panel, int row, JLabel label, Component editor, String text, Object value) {
		Insets insets = new Insets(5, 5, 5, -228);
		
		// Add the label
		GridBagConstraints labelConstraints = new GridBagConstraints(0, row, 2, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets, 0, 0);
		label = new JLabel(text);
		panel.add(label, labelConstraints);
		
		// Create the editor constraints
		insets = new Insets(5, -228, 5, 5);
		GridBagConstraints editorConstraints = new GridBagConstraints(2, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, insets, 0, 0);
		
		// Add the editor as a check box
		if(editor instanceof JCheckBox) {
			JCheckBox checkBox = (JCheckBox)editor;
			checkBox.setBackground(Color.white);
			checkBox.setSelected((Boolean)value);
			panel.add(checkBox, editorConstraints);
		}
		
		// Add the editor as a spinner
		if(editor instanceof JSpinner) {
			JSpinner spinner = (JSpinner)editor;
			spinner.setBackground(Color.white);
			spinner.setValue(value);
			panel.add(spinner, editorConstraints);
		}
	}
	
	/**
	 * Add a section header.
	 * 
	 * @param panel The panel to add the header too.
	 * @param section The label for the section.
	 * @param text The name of the section.
	 * @param row The row the section appears on.
	 */
	private static void addSeparator(JPanel panel, JLabel section, String text, int row) {
		Insets insets = new Insets(0, 5, 0, 5);
		
		// Add the section
		GridBagConstraints sectionConstraints = new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets, 0, 0);
		section = new JLabel(text);
		section.setForeground(new Color(64, 64, 64));	
		panel.add(section, sectionConstraints);
		
		// Add the clipboard separator
		GridBagConstraints separatorConstraints = new GridBagConstraints(1, row, 2, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets, 0, 0);
		JSeparator separator = new JSeparator();
		separator.setPreferredSize(SEPARATOR_DIMENSIONS);
		separator.getInsets(insets);
		panel.add(separator, separatorConstraints);
	}

	/**
	 * Apply shortcut keys to the dialog
	 * 
	 * @param root The root pane for all components.
	 * @param pane The option pane.
	 * @param dialog The window.
	 */
	private static void recursizeShortcutKeys(Component root, JOptionPane pane, JDialog dialog) {
		root.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				// Apply the accept action
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					pane.setValue(pane.getOptions()[0]);
				}
				// Apply the cancel action
				else if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
					dialog.setVisible(false);
				}
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
	 * Do not allow a spinner to contain a negative number.
	 * 
	 * @param spinner The spinner that is only allowed positive numbers.
	 * 
	 * @return A ChangeListener.
	 */
	private static ChangeListener dontAllowNegativeNumbers(JSpinner spinner) {
		return new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if((Integer)spinner.getValue() < 1)
					spinner.setValue(1);
			}
		};
	}
	
	/**
	 * Disable a list of components when a check box is not selected.
	 * 
	 * @param checkBox The check box that the components rely on.
	 * @param components The components that will be enabled or disabled.
	 * 
	 * @return A ChangeListener.
	 */
	private static ChangeListener disableComponent(JCheckBox checkBox, Component... components) {
		return new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				for(Component component : components)
					component.setEnabled(checkBox.isSelected());
			}
			
		};
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
				// The user clicked accept
				if(pce.getNewValue() != null && pce.getNewValue().equals("Apply"))
					useSettings = true;
				
				// The user wants to cancel
				else if(pce.getNewValue() != null && pce.getNewValue().equals("Cancel"))
					useSettings = false;
				
				dialog.setVisible(false);
			}
		};
	}

	/**
	 * Close the dialog.
	 * 
	 * @param dialog The dialog.
	 * 
	 * @return A WindowAdapter.
	 */
	private static WindowAdapter windowClosing(JDialog dialog) {
		return new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				useSettings = false;
				dialog.setVisible(false);
			}
		};
	}
}
