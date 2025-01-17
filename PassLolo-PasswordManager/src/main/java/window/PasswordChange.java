package window;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Base64;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicProgressBarUI;

import crypto.AES;
import crypto.Password;
import utilities.Files;

public class PasswordChange {
	// Styling
	private static final int WINDOW_WIDTH = 460;
	private static final int WINDOW_HEIGHT = 239;
	
	// Labels
	private static JLabel currentLabel;
	private static JLabel newLabel;
	private static JLabel confirmLabel;
	private static JLabel strengthLabel;
	
	// Editors
	private static JPasswordField currentField;
	private static JPasswordField newField;
	private static JPasswordField confirmField;
	private static JProgressBar strengthBar;
	
	// Buttons
	private static JButton currentReveal;
	private static JButton newReveal;
	private static JButton checkDictionary;
	
	// Trackers
	private static boolean userIsDone;
	private static boolean updatePassword;
	
	/**
	 * Display the window to change the master password.
	 * 
	 * @param path The path of the vault file.
	 * @param echoChar The echo character to use for the password fields.
	 * @param autoCheckDictionary The setting for automatically checking the dictionary
	 * 
	 * @return The new key, or null if the user cancels.
	 */
	public static byte[] display(String path, char echoChar, boolean autoCheckDictionary) {
		// Initialize the tracker
		userIsDone = false;
		updatePassword = false;
		
		// The panel that contains the message
		GridBagLayout layout = new GridBagLayout();
		JPanel panel = new JPanel(layout);
		panel.setBackground(new Color(250, 250, 250));
		panel.setBorder(BorderFactory.createLineBorder(new Color(137, 140, 144), 1));
		
		// Add the current password row
		currentLabel = new JLabel();
		currentField = new JPasswordField();
		currentField.setEchoChar(echoChar);
		currentReveal = new JButton();
		addRow(panel, 1, "Current Password:", currentLabel, currentField, currentReveal);
		
		// Add the new password row
		newLabel = new JLabel();
		newField = new JPasswordField();
		newField.setEchoChar(echoChar);
		newReveal = new JButton();
		addRow(panel, 2, "New Password:", newLabel, newField, newReveal);
		
		// Add the confirm password row
		confirmLabel = new JLabel();
		confirmField = new JPasswordField();
		confirmField.setEchoChar(echoChar);
		addRow(panel, 3, "Confirm New Password:", confirmLabel, confirmField, null);

		// Add the strength label
		GridBagConstraints strengthLabelConstraints = new GridBagConstraints(0, 4, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0);
		strengthLabel = new JLabel("Approximate Strength:");
		panel.add(strengthLabel, strengthLabelConstraints);
		
		// Add the strength bar
		GridBagConstraints strengthBarConstraints = new GridBagConstraints(1, 4, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 0, 0);
		strengthBar = new JProgressBar(0, 100);
		strengthBar.setMinimumSize(new Dimension(120, 20));
		strengthBar.setPreferredSize(new Dimension(120, 20));
		strengthBar.setStringPainted(true);
		strengthBar.setUI(new BasicProgressBarUI() {
			protected Color getSelectionBackground() { return Color.BLACK; }
			protected Color getSelectionForeground() { return Color.BLACK; }});
		strengthBar.setValue(0);
		panel.add(strengthBar, strengthBarConstraints);

		// Add the dictionary button
		GridBagConstraints dictionaryButtonConstraints = new GridBagConstraints(2, 4, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 0, 0);
		checkDictionary = new JButton("Check Dictionary");
		checkDictionary.setPreferredSize(new Dimension(120, 20));
		checkDictionary.setEnabled(false);
		panel.add(checkDictionary, dictionaryButtonConstraints);

		// Add the reveal listeners
		currentReveal.addActionListener(toggleEchoChar(echoChar, currentReveal, currentField));
		newReveal.addActionListener(toggleEchoChar(echoChar, newReveal, newField, confirmField));
		
		// Create the option pane and dialog
		JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {"Accept", "Cancel"}, null);
		JDialog dialog = new JDialog(new JFrame(), "Change Master Password", true);
		dialog.setIconImage(new ImageIcon("..\\resources\\windowIcon.png").getImage());
		dialog.setContentPane(pane);
		dialog.setResizable(false);

		// Set the size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double screenWidth = screenSize.getWidth();
		double screenHeight = screenSize.getHeight();
		dialog.setBounds((int)screenWidth/2 - WINDOW_WIDTH/2, (int)screenHeight/2 - WINDOW_HEIGHT/2-50, WINDOW_WIDTH, WINDOW_HEIGHT);
		
		// Recursively unfocus everything in the dialog
		recursiveUnfocus(dialog);
		recursizeHotkeys(dialog, pane, dialog);
		
		// Apply the strength listeners
		newField.getDocument().addDocumentListener(determinePasswordStrength(newField, strengthBar));
		
		// Apply the dictionary listener
		checkDictionary.addActionListener(checkDictionary(newField));
		newField.getDocument().addDocumentListener(toggleEnableComponent(checkDictionary, newField, confirmField));
		confirmField.getDocument().addDocumentListener(toggleEnableComponent(checkDictionary, newField, confirmField));
		
		// Add the buttons pressed listener
		pane.addPropertyChangeListener(buttonPressed(dialog, path, autoCheckDictionary));
		dialog.addWindowListener(windowClosing(dialog));
		
		do {
			// Display the dialog
			dialog.setVisible(true);
			
			// Reset the value of the pane
			pane.setValue(null);
			
		} while(!userIsDone);
		
		if(updatePassword) {
			byte[] hashedKey = new byte[0];
			char[] chars = new char[0];
			String str = "";
			try {
				chars = newField.getPassword();
				str = new String(chars);
				hashedKey = AES.hashKey(str);
				return hashedKey;
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				for(int i = 0; i < chars.length; i++)
					chars[i] = '\0';
				str = null;
			}
		}
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
	 * Add a row to a panel.
	 * 
	 * @param panel The panel to add the row to.
	 * @param row The row that this will be on the panel.
	 * @param text The label text for this row.
	 * @param label The label for this row.
	 * @param field The field for this row.
	 * @param button The button for this row. Not added if null.
	 */
	private static void addRow(JPanel panel, int row, String text, JLabel label, JPasswordField field, JButton button) {
		Insets insets = new Insets(5, 5, 5, 5);
		
		// Add the label
		GridBagConstraints labelContraints = new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, insets, 0, 0);
		label.setText(text);
		panel.add(label, labelContraints);
		
		// Add the password field
		GridBagConstraints fieldConstraints = new GridBagConstraints(1, row, (button == null)? 3 : 2, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets, 0, 0);
		field.setPreferredSize((button == null)? new Dimension(250, 20) : new Dimension(230, 20));
		field.setMinimumSize((button == null)? new Dimension(250, 20) : new Dimension(230, 20));
		panel.add(field, fieldConstraints);
		
		// Add the button, if applicable
		if(button != null) {
			insets = new Insets(0, -19, 0, 0);
			GridBagConstraints buttonConstraints = new GridBagConstraints(3, row, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, insets, 0, 0);
			button.setIcon(new ImageIcon("..\\resources\\reveal.png"));
			button.setMargin(new Insets(0, 0, 0, 0));
			button.setPreferredSize(new Dimension(22, 22));
			panel.add(button, buttonConstraints);
		}
	}

	/**
	 * Toggle a password field between for being masked.
	 * 
	 * @param mask The character to mask with.
	 * @param field The password fields to reveal or mask.
	 * 
	 * @return A MouseAdapter.
	 */
	private static ActionListener toggleEchoChar(char mask, JButton button, JPasswordField... fields) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
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
	 * Check a dictionary for an entered password.
	 * 
	 * @param field The password field.
	 * 
	 * @return A MouseAdapter.
	 */
	private static ActionListener checkDictionary(JPasswordField field) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent me) {
				// Get the password
				char[] passwordChars = field.getPassword();
				String password = new String(passwordChars);
				String result = "ERROR";

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
				// Wipe the sensitive data
				password = null;
				for(int i = 0; i < passwordChars.length; i++)
					passwordChars[i] = '\0';
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
	 * Process the input based on the button pressed for the dialog.
	 * 
	 * @param dialog The dialog to analyze.
	 * 
	 * @return A PropertyChangeListener.
	 */
	private static PropertyChangeListener buttonPressed(JDialog dialog, String filePath, boolean autoCheckDictionary) {
		return new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent pce) {
				// The user clicked accept
				if(pce.getNewValue() != null && pce.getNewValue().equals("Accept")) {
					
					// Generate the hash of the entered current password, and check for correctness
					char[] enteredPasswordChars = currentField.getPassword();
					String enteredPassword = new String(enteredPasswordChars);
					byte[] enteredHashBytes = new byte[0];
					try {
						enteredHashBytes = AES.hashKey(enteredPassword);
						AES.decrypt(Base64.getDecoder().decode(Files.read(filePath)[0]), enteredHashBytes);
					} catch(Exception e) {
						JOptionPane.showMessageDialog(null, "Current password incorrect", "Incorrect Credentials", JOptionPane.ERROR_MESSAGE);
						userIsDone = false;
						dialog.setVisible(false);
						return;
					} finally {
						for(int i = 0; i < enteredPasswordChars.length; i++) 
							enteredPasswordChars[i] = '\0';
						for(int i = 0; i < enteredHashBytes.length; i++)
							enteredHashBytes[i] = '\0';
						enteredPassword = null;
					}
					
					// Ensure all the new password fields are occupied and matching
					char[] newPasswordChars = newField.getPassword();
					char[] confirmPasswordChars = confirmField.getPassword();
					if(newPasswordChars.length == 0) {
						JOptionPane.showMessageDialog(null, "New password cannot be blank", "Incorrect Credentials", JOptionPane.ERROR_MESSAGE);
						userIsDone = false;
						dialog.setVisible(false);
						for(int i = 0; i < newPasswordChars.length; i++)
							newPasswordChars[i] = '\0';
						for(int i = 0; i < confirmPasswordChars.length; i++)
							confirmPasswordChars[i] = '\0';
						return;
					}
					
					if(newPasswordChars.length != confirmPasswordChars.length) {
						JOptionPane.showMessageDialog(null, "New password fields do not match", "Incorrect Credentials", JOptionPane.ERROR_MESSAGE);
						userIsDone = false;
						dialog.setVisible(false);
						for(int i = 0; i < newPasswordChars.length; i++)
							newPasswordChars[i] = '\0';
						for(int i = 0; i < confirmPasswordChars.length; i++)
							confirmPasswordChars[i] = '\0';
						return;
					}
					for(int i = 0; i < newPasswordChars.length; i++) {
						if(newPasswordChars[i] != confirmPasswordChars[i]) {
							JOptionPane.showMessageDialog(null, "New password fields do not match", "Incorrect Credentials", JOptionPane.ERROR_MESSAGE);
							userIsDone = false;
							dialog.setVisible(false);
							for(int j = 0; j < newPasswordChars.length; j++)
								newPasswordChars[j] = '\0';
							for(int j = 0; j < confirmPasswordChars.length; j++)
								confirmPasswordChars[j] = '\0';
							return;
						}
					}
					
					// Check to make sure the password is strong
					int confirm = 0;
					String newPassword = new String(newPasswordChars);
					String result = "";
					if(strengthBar.getValue() != 100) {
						confirm = JOptionPane.showConfirmDialog(null, "Password strength is not at 100%. Are you sure you want to continue?", "Weak Password Warning", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, new ImageIcon("..\\resources\\searchResults.png"));
						if(confirm == JOptionPane.NO_OPTION) {
							userIsDone = false;
							dialog.setVisible(false);
							for(int i = 0; i < newPasswordChars.length; i++)
								newPasswordChars[i] = '\0';
							for(int i = 0; i < confirmPasswordChars.length; i++)
								confirmPasswordChars[i] = '\0';
							newPassword = null;
							result  = null;
							return;
						}
					}
					
					if(autoCheckDictionary) {
						result = Password.checkDictionary(newPassword);
						// Warn the user if the dictionary came back with a result
						if(result != null) {
							// Of an exact match
							if(result.toLowerCase().equals(newPassword))
								confirm = JOptionPane.showConfirmDialog(null, "An exact match of this password was found in a dictionary. Are you sure you want to continue?", "Weak Password Warning", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, new ImageIcon("..\\resources\\searchResults.png"));
							// Of a similar match
							else
								confirm = JOptionPane.showConfirmDialog(null, "A similar match of this password was found in a dictionary: \""+result+".\" Are you sure you want to continue?", "Weak Password Warning", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, new ImageIcon("..\\resources\\searchResults.png"));
	
							if(confirm == JOptionPane.NO_OPTION) {
								userIsDone = false;
								dialog.setVisible(false);
								for(int i = 0; i < newPasswordChars.length; i++)
									newPasswordChars[i] = '\0';
								for(int i = 0; i < confirmPasswordChars.length; i++)
									confirmPasswordChars[i] = '\0';
								newPassword = null;
								result  = null;
								return;
							}
						}
					}
					
					// Wipe the sensitive data
					for(int i = 0; i < newPasswordChars.length; i++)
						newPasswordChars[i] = '\0';
					for(int i = 0; i < confirmPasswordChars.length; i++)
						confirmPasswordChars[i] = '\0';
					newPassword = null;
					result  = null;
					
					userIsDone = true;
					updatePassword = true;
				}
				
				// The user wants to cancel
				else if(pce.getNewValue() != null && pce.getNewValue().equals("Cancel")) {
					userIsDone = true;
					updatePassword = false;
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
				updatePassword = false;
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
					updatePassword = false;
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
