package window;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.security.SecureRandom;
import java.util.Base64;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicProgressBarUI;

import crypto.AES;
import crypto.Password;
import utilities.Files;

public class VaultOpener {
	// The size of the window
	private static final int WINDOW_WIDTH = 460;
	private static final int WINDOW_HEIGHT = 239;

	// Trackers
	private static boolean userIsDone = false;
	private static boolean useInput = false;
	private static Object[] toReturn;
	
	// The labels
	static JLabel openVaultLabel = new JLabel("Vault File:");
	static JLabel openPasswordLabel = new JLabel("Master Password:");
	static JLabel newVaultLabel = new JLabel("Vault File:");
	static JLabel newPasswordLabel = new JLabel("Master Password:");
	static JLabel newConfirmPasswordLabel = new JLabel("Confirm Master Password:");
	static JLabel newStrengthLabel = new JLabel("Approximate Strength:");

	// The fields
	static JTextField openVaultField = new JTextField();
	static JPasswordField openPasswordField = new JPasswordField();
	static JTextField newVaultField = new JTextField();
	static JPasswordField newPasswordField = new JPasswordField();
	static JPasswordField newConfirmPasswordField = new JPasswordField();
	static JProgressBar newStrengthBar = new JProgressBar(0, 100);

	// The buttons
	static JButton openFileButton = new JButton(new ImageIcon("..\\resources\\open.png"));
	static JButton openRevealButton = new JButton(new ImageIcon("..\\resources\\reveal.png"));
	static JButton newFileButton = new JButton(new ImageIcon("..\\resources\\open.png"));
	static JButton newRevealButton = new JButton(new ImageIcon("..\\resources\\reveal.png"));
	static JButton newCheckDictionaryButton = new JButton("Check Dictionary");

	/**
	 * The ease of use constant for starting the window on the open tab
	 */
	public static final int OPEN = 0;
	/**
	 * The ease of use constant for starting the window on the new tab
	 */
	public static final int NEW = 1;
	
	public static Object[] display(char defaultEchoChar, int tab, boolean autoCheckDictionary, String... population) {
		// The variables for readability
		int row, col;
		int width, height;
		double weightX=0, weightY=0;
		int anchor=GridBagConstraints.LINE_END, fill=GridBagConstraints.NONE;
		Insets insets = new Insets(5, 5, 5, -15);
		int ipadx = 0, ipady = 0;

		// Create the tabbed pane
		JTabbedPane tabbedPane = new JTabbedPane();

		// The panel that contains the message for opening a vault
		GridBagLayout openLayout = new GridBagLayout();
		JPanel openPanel = new JPanel(openLayout);
		tabbedPane.addTab("Open", null, openPanel);

		// The panel that contains the message for making a new vault
		GridBagLayout newLayout = new GridBagLayout();
		JPanel newPanel = new JPanel(newLayout);
		tabbedPane.addTab("New", null, newPanel);

		// Set the tab to what the user requested
		tabbedPane.setSelectedIndex(tab);

		// Add the vault label to the open tab
		row = 0; col = 0; width = 1; height = 1;
		anchor=GridBagConstraints.LINE_END;
		insets = new Insets(5, 5, 5, 5);
		GridBagConstraints vaultLabelConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		openVaultLabel = new JLabel("Vault File:");
		openPanel.add(openVaultLabel, vaultLabelConstraints);

		// Add the password label to the open tab
		row = 1; col = 0; width = 1; height = 1;
		anchor=GridBagConstraints.LINE_END;
		insets = new Insets(5, 5, 5, 5);
		GridBagConstraints passwordLabelConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		openPasswordLabel = new JLabel("Master Password:");
		openPanel.add(openPasswordLabel, passwordLabelConstraints);

		// Add the vault field to the open tab
		row = 0; col = 1; width = 1; height = 1;
		anchor=GridBagConstraints.LINE_END;
		insets = new Insets(5, 5, 5, 5);
		GridBagConstraints vaultFieldConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		openVaultField = new JTextField();
		openVaultField.setPreferredSize(new Dimension(230, 20));
		if(population != null && population.length != 0)
			openVaultField.setText(population[0]);
		openVaultField.setFocusable(false);
		openPanel.add(openVaultField, vaultFieldConstraints);

		// Add the password field to the open tab
		row = 1; col = 1; width = 2; height = 1;
		anchor = GridBagConstraints.LINE_START;
		GridBagConstraints passwordFieldConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		openPasswordField = new JPasswordField();
		openPasswordField.setPreferredSize(new Dimension(230, 20));
		openPasswordField.setEchoChar(defaultEchoChar);
		openPanel.add(openPasswordField, passwordFieldConstraints);

		// Add the file button to the open tab
		row = 0; col = 2; width = 1; height = 1;
		anchor = GridBagConstraints.LINE_END;
		insets = new Insets(3, -7, 3, 3);
		GridBagConstraints fileButtonConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		openFileButton = new JButton(new ImageIcon("..\\resources\\open.png"));
		openFileButton.setMargin(new Insets(0, 0, 0, 0));
		openFileButton.setPreferredSize(new Dimension(22, 22));
		openPanel.add(openFileButton, fileButtonConstraints);

		// Add the reveal button to the open tab
		row = 1; col = 2; width = 1; height = 1;
		anchor = GridBagConstraints.LINE_END;
		insets = new Insets(3, -7, 3, 3);
		GridBagConstraints revealButtonConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		openRevealButton = new JButton(new ImageIcon("..\\resources\\reveal.png"));
		openRevealButton.setMargin(new Insets(0, 0, 0, 0));
		openRevealButton.setPreferredSize(new Dimension(22, 22));
		openPanel.add(openRevealButton, revealButtonConstraints);

		// Add the vault label to the new tab
		row = 0; col = 0; width = 1; height = 1;
		anchor=GridBagConstraints.LINE_END;
		insets = new Insets(5, 5, 5, 5);
		GridBagConstraints newVaultLabelConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		newVaultLabel = new JLabel("Vault File:");
		newPanel.add(newVaultLabel, newVaultLabelConstraints);

		// Add the password label to the new tab
		row = 1; col = 0; width = 1; height = 1;
		anchor=GridBagConstraints.LINE_END;
		insets = new Insets(5, 5, 5, 5);
		GridBagConstraints newPasswordLabelConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		newPasswordLabel = new JLabel("Master Password:");
		newPanel.add(newPasswordLabel, newPasswordLabelConstraints);

		// Add the confirm password label to the new tab
		row = 2; col = 0; width = 1; height = 1;
		anchor=GridBagConstraints.LINE_END;
		insets = new Insets(5, 5, 5, 5);
		GridBagConstraints newConfirmPasswordLabelConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		newConfirmPasswordLabel = new JLabel("Confirm Master Password:");
		newPanel.add(newConfirmPasswordLabel, newConfirmPasswordLabelConstraints);

		// Add the strength label to the new tab
		row = 3; col = 0; width = 1; height = 1;
		anchor=GridBagConstraints.LINE_END;
		insets = new Insets(5, 5, 5, 5);
		GridBagConstraints newStrengthLabelConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		newStrengthLabel = new JLabel("Approximate Strength:");
		newPanel.add(newStrengthLabel, newStrengthLabelConstraints);

		// Add the vault field to the open tab
		row = 0; col = 1; width = 2; height = 1;
		anchor=GridBagConstraints.LINE_START;
		insets = new Insets(5, 5, 5, 5);
		GridBagConstraints newVaultFieldConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		newVaultField = new JTextField();
		newVaultField.setPreferredSize(new Dimension(224, 20));
		newVaultField.setFocusable(false);
		newPanel.add(newVaultField, newVaultFieldConstraints);
		
		// Add the file button to the open tab
		row = 0; col = 2; width = 1; height = 1;
		anchor = GridBagConstraints.LINE_END;
		insets = new Insets(3, -7, 3, 3);
		GridBagConstraints newFileButtonConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		newFileButton = new JButton(new ImageIcon("..\\resources\\open.png"));
		newFileButton.setMargin(new Insets(0, 0, 0, 0));
		newFileButton.setPreferredSize(new Dimension(22, 22));
		newPanel.add(newFileButton, newFileButtonConstraints);
		
		// Add the password field to the new tab
		row = 1; col = 1; width = 2; height = 1;
		anchor = GridBagConstraints.LINE_START;
		insets = new Insets(5, 5, 5, 5);
		GridBagConstraints newPasswordFieldConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		newPasswordField = new JPasswordField();
		newPasswordField.setPreferredSize(new Dimension(224, 20));
		newPasswordField.setEchoChar(defaultEchoChar);
		newPanel.add(newPasswordField, newPasswordFieldConstraints);

		// Add the reveal button to the new tab
		row = 1; col = 2; width = 1; height = 1;
		anchor = GridBagConstraints.LINE_END;
		insets = new Insets(3, 3, 3, 3);
		GridBagConstraints newRevealButtonConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		newRevealButton = new JButton(new ImageIcon("..\\resources\\reveal.png"));
		newRevealButton.setMargin(new Insets(0, 0, 0, 0));
		newRevealButton.setPreferredSize(new Dimension(22, 22));
		newPanel.add(newRevealButton, newRevealButtonConstraints);

		// Add the password field to the new tab
		row = 2; col = 1; width = 3; height = 1;
		anchor = GridBagConstraints.LINE_START;
		insets = new Insets(5, 5, 5, 5);
		GridBagConstraints newConfirmPasswordFieldConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		newConfirmPasswordField = new JPasswordField();
		newConfirmPasswordField.setPreferredSize(new Dimension(243, 20));
		newConfirmPasswordField.setEchoChar(defaultEchoChar);
		newPanel.add(newConfirmPasswordField, newConfirmPasswordFieldConstraints);

		// Add the strength bar to the new tab
		row = 3; col = 1; width = 1; height = 1;
		anchor = GridBagConstraints.LINE_START;
		insets = new Insets(3, 3, 3, 3);
		GridBagConstraints strengthBarConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		newStrengthBar = new JProgressBar(0, 100);
		newStrengthBar.setMinimumSize(new Dimension(120, 20));
		newStrengthBar.setPreferredSize(new Dimension(120, 20));
		newStrengthBar.setStringPainted(true);
		newStrengthBar.setUI(new BasicProgressBarUI() {
			protected Color getSelectionBackground() { return Color.BLACK; }
			protected Color getSelectionForeground() { return Color.BLACK; }});
		newStrengthBar.setValue(0);
		newPanel.add(newStrengthBar, strengthBarConstraints);

		// Add the dictionary button
		row = 3; col = 2; width = 1; height = 1;
		anchor = GridBagConstraints.LINE_END;
		insets = new Insets(3, 3, 3, 3);
		GridBagConstraints dictionaryButtonConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		newCheckDictionaryButton = new JButton("Check Dictionary");
		newCheckDictionaryButton.setPreferredSize(new Dimension(120, 20));
		newCheckDictionaryButton.setEnabled(false);
		newPanel.add(newCheckDictionaryButton, dictionaryButtonConstraints);

		Object[] options;
		if(tab == 0)
			 options = new Object[] {"Enter", "Cancel"};
		else
			options = new Object[] {"Create", "Cancel"};
		JOptionPane pane = new JOptionPane(tabbedPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options, null);
		JDialog dialog = new JDialog((Dialog)null, "Vault Opener", true);
		dialog.setIconImage(new ImageIcon("..\\resources\\windowIcon.png").getImage());
		dialog.setContentPane(pane);
		dialog.setResizable(false);
		
		// Set the size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double screenWidth = screenSize.getWidth();
		double screenHeight = screenSize.getHeight();
		dialog.setBounds((int)screenWidth/2 - WINDOW_WIDTH/2, (int)screenHeight/2 - WINDOW_HEIGHT/2-50, WINDOW_WIDTH, WINDOW_HEIGHT);

		// Recursively unfocus everything on the panel
		recursiveUnfocus(tabbedPane);

		// Add the listener that opens the file opener
		JFrame parent = new JFrame();
		parent.setIconImage(new ImageIcon("..\\resources\\windowIcon.png").getImage());
		openFileButton.addMouseListener(showFileOpener(parent, openVaultField));
		newFileButton.addMouseListener(showFileOpener(parent, newVaultField));

		// Add the listener to toggle the button for changing the echo character
		openRevealButton.addMouseListener(toggleEchoChar(defaultEchoChar, openRevealButton, openPasswordField));
		newRevealButton.addMouseListener(toggleEchoChar(defaultEchoChar, newRevealButton, newPasswordField, newConfirmPasswordField));
		
		// Add the listener for the dictionary button
		newCheckDictionaryButton.addMouseListener(checkDictionary(newCheckDictionaryButton, newPasswordField));
		newPasswordField.getDocument().addDocumentListener(toggleEnableComponent(newCheckDictionaryButton, newPasswordField, newConfirmPasswordField));
		newConfirmPasswordField.getDocument().addDocumentListener(toggleEnableComponent(newCheckDictionaryButton, newPasswordField, newConfirmPasswordField));
		
		// Add the listener for the strength bar
		newPasswordField.getDocument().addDocumentListener(determinePasswordStrength(newPasswordField, newStrengthBar));
		
		// Add the listener to toggle the options
		tabbedPane.addChangeListener(toggleButtonOptions(pane, 0, "Enter", "Create"));
		
		// Add the listener to submit when enter is pressed in the correct field
		recursizeHotkeys(dialog, pane, dialog);
		
		// Apply the closing listeners
		pane.addPropertyChangeListener(buttonPressed(dialog, autoCheckDictionary));
		dialog.addWindowListener(windowClosing(dialog));
		
		// Focus on the password field by default
		openPasswordField.requestFocus();
		
		// Show the panel
		do {
			dialog.setVisible(true);
						
			// Reset the value of pane, so the user can click the same button twice
			pane.setValue(null);
			
		} while(!userIsDone);
		
		// Reset userIsDone for if this method is called again
		userIsDone = false;
				
		// Return the appropriate value
		if(useInput)
			return toReturn;
		else
			return null;
	}

	/**
	 * Disable all children of root from being focusable.
	 * <p>
	 * Does not apply to components such as JTextField, which need focus to function.
	 * 
	 * @param root The starting component.
	 */
	private static void recursiveUnfocus(Component root) {
		if(root instanceof JCheckBox)
			((JCheckBox) root).setFocusPainted(false);
		else if(!(root instanceof JTextArea || root instanceof JComboBox || root instanceof JTextField))
			root.setFocusable(false);

		if(root instanceof Container) {
			Component[] children = ((Container) root).getComponents();
			for (Component child : children) {
				recursiveUnfocus(child);
			}
		}
	} 

	/**
	 * Get a sub-string after the last occurrence of a specified character.
	 * 
	 * @param str The string to search.
	 * @param c The character to search for.
	 * 
	 * @return The sub-string after the last occurrence of c in str.
	 */
	private static String getStringAfterLastOccurrence(String str, char c) {
		int charPosition = -1;
		String toReturn = "";
		
	    // Get the position of the last period
		for(int i = 0; i < str.length(); i++)
			if(str.charAt(i) == c)
				charPosition = i;
		
		if(charPosition != -1)
			for(int i = charPosition+1; i < str.length(); i++)
				toReturn += str.charAt(i);
		
		return toReturn;
	}
	
	/**
	 * Toggle a password field between for being masked.
	 * 
	 * @param mask The character to mask with.
	 * @param field The password fields to reveal or mask.
	 * 
	 * @return A MouseAdapter.
	 */
	private static MouseAdapter toggleEchoChar(char mask, JButton button, JPasswordField... fields) {
		return new MouseAdapter() {
			public void mouseReleased(MouseEvent me) {
				if(me.getButton() == MouseEvent.BUTTON1) {
					for(int i = 0; i < fields.length; i++) {
						if(fields[i].getEchoChar() == mask) {
							fields[i].setEchoChar('\0');
							button.setIcon(new ImageIcon("..\\resources\\hide.png"));
						}
						else {
							fields[i].setEchoChar(mask);
							button.setIcon(new ImageIcon("..\\resources\\reveal.png"));
						}
					}
				}
			}
		};
	}

	/**
	 * Show a JFileChooser in open mode that only accepts .vlt files
	 * 
	 * @return A MouseAdapter.
	 */
	private static MouseAdapter showFileOpener(JFrame parent, JTextField field) {
		return new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				if(me.getButton() == MouseEvent.BUTTON1) {
					// Display a file chooser
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setCurrentDirectory(new File("..\\"));
					fileChooser.setFileFilter(new FileNameExtensionFilter("Vault Files (.vlt)", "vlt"));
					fileChooser.setDialogTitle("Select Vault");
					int option = fileChooser.showOpenDialog(parent);

					if(option == JFileChooser.APPROVE_OPTION) {
						String filePath = fileChooser.getSelectedFile().getAbsolutePath();
						filePath = (getStringAfterLastOccurrence(filePath, '.').equals("vlt"))? filePath : filePath+".vlt";
						field.setText(filePath);
					}

				}
			}
		};
	}

	/**
	 * Check a dictionary for an entered password.
	 * 
	 * @param field The password field.
	 * 
	 * @return A MouseAdapter.
	 */
	private static MouseAdapter checkDictionary(JButton button, JPasswordField field) {
		return new MouseAdapter() {
			public void mouseReleased(MouseEvent me) {
				if(me.getButton() == MouseEvent.BUTTON1) {
					// Get the password
					char[] passwordChars = field.getPassword();
					String password = new String(passwordChars);
					String result = "ERROR";

					if(button.isEnabled()) {
						// Check the dictionary
						try {
							result = Password.checkDictionary(password);
						} catch(Exception e) {
							e.printStackTrace();
						}

						// Display a dialog
						if(result == null)
							JOptionPane.showOptionDialog(null, "No matches found!", "Search Results", JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, new ImageIcon("..\\resources\\searchResults.png"), new Object[] {"Okay"}, null);
						else if(result.toLowerCase().equals(password.toLowerCase()))
							JOptionPane.showOptionDialog(null, "Exact match found! It is highly recommended to not use this password.", "Search Results", JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, new ImageIcon("..\\resources\\searchResults.png"), new Object[] {"Okay"}, null);
						else 
							JOptionPane.showOptionDialog(null, "Similar match found: \""+result+".\" It is not recommended to use this password.", "Search Results", JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, new ImageIcon("..\\resources\\searchResults.png"), new Object[] {"Okay"}, null);
					}
					// Wipe the sensitive data
					password = null;
					for(int i = 0; i < passwordChars.length; i++)
						passwordChars[i] = '\0';
				}
			}
		};
	}

	/**
	 * Set a component to be disabled when two password fields do not match or are blank
	 * 
	 * @param field The password field.
	 * @param c The component
	 * @return A DocumentListener.
	 */
	private static DocumentListener toggleEnableComponent(Component c, JPasswordField... fields) {
		return new DocumentListener() {

			public void insertUpdate(DocumentEvent e) { toggleEnable(); }

			public void removeUpdate(DocumentEvent e) { toggleEnable(); }

			public void changedUpdate(DocumentEvent e) { /*Not Applicable*/ }

			public void toggleEnable() {
				char[] password1 = fields[0].getPassword();
				char[] password2 = fields[1].getPassword();
				boolean equal = password1.length == password2.length;

				// If either field is blank, disable the component
				if(password1.length == 0 || password2.length == 0)
					c.setEnabled(false);

				// Else, make sure the lengths are equal and the characters match
				else if(equal) {
					for(int i = 0; i < password1.length; i++)
						if(password1[i] != password2[i])
							equal = false;
					c.setEnabled(equal);
				}
				else
					c.setEnabled(false);

				// Wipe the sensitive data
				for(int i = 0; i < password1.length; i++)
					password1[i] = '\0';
				for(int i = 0; i < password2.length; i++)
					password2[i] = '\0';				
			}
		};
	}

	/**
	 * Set a progress bar to display the strength of an entered password.
	 * 
	 * @param field The password field.
	 * @param strength The progress bar.
	 * 
	 * @return A DocumentListener.
	 */
	private static DocumentListener determinePasswordStrength(JPasswordField field, JProgressBar strength) {
		return new DocumentListener() {

			public void insertUpdate(DocumentEvent e) { determineStrength(); }

			public void removeUpdate(DocumentEvent e) { determineStrength(); }

			public void changedUpdate(DocumentEvent e) {/* Not Applicable */}

			private void determineStrength() {
				// Get the data
				char[] passwordChars = field.getPassword();
				String password = new String(passwordChars);
				int score = Password.strengthOf(password);

				// Compute the score
				strength.setValue(score);
				strength.setString(score+"%");

				// Determine the bar color
				int red = (int)(255-(score/100.0)*180);
				int green = (int)((score/100.0)*200);
				int blue = 0;
				strength.setForeground(new Color(red, green, blue));

				// Wipe the sensitive data
				password = null;
				for(int i = 0; i < passwordChars.length; i++)
					passwordChars[i] = '\0';
			}
		};
	}

	/**
	 * Change the options for the buttons when a tab is changed.
	 * 
	 * @return A ChangeListener.
	 */
	private static ChangeListener toggleButtonOptions(JOptionPane pane, int index, Object option1, Object option2) {
		return new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Object[] options = pane.getOptions();
								
				if(options[index].equals(option1))
					options[index] = option2;
				else
					options[index] = option1;
				
				pane.setOptions(options);					
			}
		};
	}
	
	/**
	 * Process the input based on the button pressed for the dialog.
	 * 
	 * @param dialog The dialog to analyze.
	 * 
	 * @return A PropertyChangeListener.
	 */
	private static PropertyChangeListener buttonPressed(JDialog dialog, boolean autoCheckDictionary) {
		return new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent pce) {
				// The user clicked enter
				if(pce.getNewValue() != null && pce.getNewValue().equals("Enter")) {	
					String filePath = openVaultField.getText();
					File file = Files.open(filePath);
					
					// The vault file field is empty
					if(filePath.isEmpty()) {
						JOptionPane.showMessageDialog(null, "Vault file field is empty.", "Empty Field", JOptionPane.ERROR_MESSAGE);
						userIsDone = false;
						dialog.setVisible(false);
						return;
					}
					
					// The file does not exist
					else if(file == null) {
						JOptionPane.showMessageDialog(null, "Vault file does not exist.", "File Not Found", JOptionPane.ERROR_MESSAGE);
						userIsDone = false;
						dialog.setVisible(false);
						return;
					}
					
					// Prepare the key
					char[] passwordChars = openPasswordField.getPassword();
					String password = new String(passwordChars);
					
					// Read all lines of the file
					String allInfo[] = Files.read(filePath);
					byte[] hashedKey = new byte[0];
					String encryptedPath = "";
					
					// Attempt to decrypt the second line
					try {
						hashedKey = AES.hashKey(password);
						AES.decrypt(Base64.getDecoder().decode(allInfo[0]), hashedKey);
						encryptedPath = Base64.getEncoder().encodeToString(AES.encrypt(filePath.getBytes(), hashedKey));
					} catch(Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, "Invalid master password.", "Password Error", JOptionPane.ERROR_MESSAGE);
						userIsDone = false;
						dialog.setVisible(false);
						return;
					}
					
					// Wipe the sensitive data
					password = null;
					for(int i = 0; i < passwordChars.length; i++)
						passwordChars[i] = '\0';
					
					// Convert all info to an Object[]
					toReturn = new Object[allInfo.length];
					for(int i = 0; i < allInfo.length; i++)
						toReturn[i] = (Object)allInfo[i];
					
					// Replace the first line with hashedKey
					toReturn[0] = hashedKey;
					toReturn[1] = encryptedPath;
					
					userIsDone = true;
					useInput = true;
				}
				
				// The user wants to create
				else if(pce.getNewValue() != null && pce.getNewValue().equals("Create")) {
					// Attempt to create the file
					String filePath = newVaultField.getText();
					File file = Files.create(filePath);
					
					// The vault file field is empty
					if(filePath.isEmpty()) {
						JOptionPane.showMessageDialog(null, "Vault file field is empty.", "Empty Field", JOptionPane.ERROR_MESSAGE);
						userIsDone = false;
						dialog.setVisible(false);
						return;
					}
					
					// The file already existed
					else if(file == null) {
						JOptionPane.showMessageDialog(null, "Vault file name already in use.", "File In Use", JOptionPane.ERROR_MESSAGE);
						userIsDone = false;
						dialog.setVisible(false);
						return;
					}
					
					// Prepare the key
					char[] passwordChars = newPasswordField.getPassword();
					char[] confirmPasswordChars = newConfirmPasswordField.getPassword();
					String password = new String(passwordChars);
					String confirmPassword = new String(confirmPasswordChars);
					
					// The password field is empty
					if(passwordChars.length == 0) {
						JOptionPane.showMessageDialog(null, "Password field cannot be empty.", "Password Error", JOptionPane.ERROR_MESSAGE);
						userIsDone = false;
						dialog.setVisible(false);
						Files.delete(filePath);
						return;
					}
					
					// The passwords do not match
					if(!password.equals(confirmPassword)) {
						JOptionPane.showMessageDialog(null, "Password fields do not match.", "Password Error", JOptionPane.ERROR_MESSAGE);
						userIsDone = false;
						dialog.setVisible(false);
						Files.delete(filePath);
						return;
					}
					
					// Check to make sure the password is strong
					int confirm = 0;
					String result = "";
					if(newStrengthBar.getValue() != 100) {
						confirm = JOptionPane.showConfirmDialog(null, "Password strength is not at 100%. Are you sure you want to continue?", "Weak Password Warning", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, new ImageIcon("..\\resources\\searchResults.png"));
						if(confirm == JOptionPane.NO_OPTION) {
							userIsDone = false;
							dialog.setVisible(false);
							Files.delete(filePath);
							return;
						}
					}
					
					if(autoCheckDictionary) {
						result = Password.checkDictionary(password);
						// Warn the user if the dictionary came back with a result
						if(result != null) {
							// Of an exact match
							if(result.toLowerCase().equals(password))
								confirm = JOptionPane.showConfirmDialog(null, "An exact match of this password was found in a dictionary. Are you sure you want to continue?", "Weak Password Warning", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, new ImageIcon("..\\resources\\searchResults.png"));
							// Of a similar match
							else
								confirm = JOptionPane.showConfirmDialog(null, "A similar match of this password was found in a dictionary: \""+result+".\" Are you sure you want to continue?", "Weak Password Warning", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, new ImageIcon("..\\resources\\searchResults.png"));
	
							if(confirm == JOptionPane.NO_OPTION) {
								userIsDone = false;
								dialog.setVisible(false);
								Files.delete(filePath);
								return;
							}
						}
					}
					
					// Prepare the data 
					toReturn = new Object[2];
					byte[] hashedKey = new byte[0];
					String encryptedPath = "";
					
					// Hash the key, encrypt a random byte[], assigning it to the first line
					try {
						hashedKey = AES.hashKey(password);
						encryptedPath = Base64.getEncoder().encodeToString(AES.encrypt(filePath.getBytes(), hashedKey));
						
						// Write the data to the file
						byte[] firstLine = new byte[32];
						new SecureRandom().nextBytes(firstLine);
						Files.write(filePath, new String[] {Base64.getEncoder().encodeToString(AES.encrypt(firstLine, hashedKey)), encryptedPath}, false);
					} catch (Exception e) {
						e.printStackTrace();
					}
					toReturn[0] = hashedKey;
					toReturn[1] = encryptedPath;
					
					// Wipe the sensitive data
					for(int i = 0; i < passwordChars.length; i++)
						passwordChars[i] = '\0';
					for(int i = 0; i < confirmPasswordChars.length; i++)
						confirmPasswordChars[i] = '\0';
					passwordChars = null;
					confirmPasswordChars = null;
					password = null;
					confirmPassword = null;
					
					// Update the trackers
					userIsDone = true;
					useInput = true;
				}
				// The user wants to cancel
				else if(pce.getNewValue() != null && pce.getNewValue().equals("Cancel")) {
					userIsDone = true;
					useInput = false;
				}
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
				userIsDone = true;
				useInput = false;
				dialog.setVisible(false);
			}
		};
	}
	
	/**
	 * Apply shortcut keys to the dialog
	 * 
	 * @param root The root pane for all components.
	 * @param pane The option pane.
	 * @param dialog The window.
	 */
	private static void recursizeHotkeys(Component root, JOptionPane pane, JDialog dialog) {
		root.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				// Apply the accept action
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					pane.setValue(pane.getOptions()[0]);
				}
				// Apply the cancel action
				else if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
					dialog.setVisible(false);
					userIsDone = true;
					useInput = false;
				}
			}
		});
		
		// Do the recursion
		if (root instanceof Container) {
			Component[] children = ((Container) root).getComponents();
			for (Component child : children)
				recursizeHotkeys(child, pane, dialog);
		}
	}
}
