package window;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
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
import java.awt.font.TextAttribute;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicProgressBarUI;

import crypto.AES;
import crypto.Password;
import loloTable.TableEntry;

public class TableEntryEditor {
	private static final int WINDOW_WIDTH = 425;
	private static final int WINDOW_HEIGHT = 417;

	// The labels
	private static JLabel mainLabel;
	private static JLabel categoryLabel;
	private static JLabel titleLabel;
	private static JLabel usernameLabel;
	private static JLabel passwordLabel;
	private static JLabel confirmPasswordLabel;
	private static JLabel strengthLabel;
	private static JLabel notesLabel;

	// The fields
	private static JComboBox<String> categoryComboBox;
	private static JTextField titleField;
	private static JTextField usernameField;
	private static JPasswordField passwordField;
	private static JButton generateButton;
	private static JPasswordField confirmPasswordField;
	private static JButton revealButton;
	private static JProgressBar strengthBar;
	private static JButton dictionaryButton;
	private static JTextArea notesField;

	// Styling
	private static char defaultEchoChar;
	private static boolean userIsDone = false;
	private static boolean autoGeneratePasswordGlobal;
	private static boolean autoCheckDictionaryGlobal;
	
	/**
	 * An ease of use constant.
	 * <br>
	 * Use this option when the TableEntryEditor is adding a new entry.
	 */
	public static final String ADD = "Add";
	/**
	 * An ease of use constant.
	 * <br>
	 * Use this option when the TableEntryEditor is modifying an existing entry.
	 */
	public static final String EDIT = "Edit";
	
	// The thing to return
	private static TableEntry createdEntry;

	public static TableEntry display(char mask, byte[] hashedKey, String action, Object[] categories, boolean autoGeneratePassword, boolean autoCheckDictionary, String... values) {
		// Assign some global variables
		defaultEchoChar = mask;
		autoGeneratePasswordGlobal = autoGeneratePassword;
		autoCheckDictionaryGlobal = autoCheckDictionary;
		String[] correctedValues = values;
		if(values.length < 5) {
			correctedValues = new String[5];
			for(int i = 0; i < values.length; i++)
				correctedValues[i] = values[i];
		}

		// The variables for readability
		int row, col;
		int width, height;
		double weightX=0, weightY=0;
		int anchor=GridBagConstraints.LINE_END, fill=GridBagConstraints.NONE;
		Insets insets = new Insets(5, 5, 5, -15);
		int ipadx = 0, ipady = 0;

		// The panel that contains the message
		GridBagLayout layout = new GridBagLayout();
		JPanel panel = new JPanel(layout);
		panel.setBorder(BorderFactory.createLineBorder(new Color(137, 140, 144), 1));
		panel.setBackground(new Color(250, 250, 250));

		// Add the main label
		row = 0; col = 0; width = 3; height = 1;
		anchor = GridBagConstraints.CENTER;
		insets = new Insets(-10, 0, 10, 0);
		GridBagConstraints mainLabelConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		mainLabel = new JLabel(action+" Entry");
		Map<TextAttribute, Integer> fontAttributes = new HashMap<TextAttribute, Integer>();
		fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		Font underline = mainLabel.getFont().deriveFont((float)30).deriveFont(fontAttributes);
		mainLabel.setFont(underline);
		panel.add(mainLabel, mainLabelConstraints);

		// Add the category label
		row = 1; col = 0; width = 1; height = 1;
		anchor=GridBagConstraints.LINE_END;
		insets = new Insets(5, 5, 5, -15);
		GridBagConstraints categoryLabelConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		categoryLabel = new JLabel("Category:");
		panel.add(categoryLabel, categoryLabelConstraints);

		// Add the title label
		row = 2; col = 0; width = 1; height = 1;
		anchor=GridBagConstraints.LINE_END;
		GridBagConstraints titleLabelConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		titleLabel = new JLabel("Title:");
		panel.add(titleLabel, titleLabelConstraints);

		// Add the username label
		row = 3; col = 0; width = 1; height = 1;
		anchor=GridBagConstraints.LINE_END;
		GridBagConstraints usernameLabelConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		usernameLabel = new JLabel("Username:");
		panel.add(usernameLabel, usernameLabelConstraints);

		// Add the password label
		row = 4; col = 0; width = 1; height = 1;
		anchor=GridBagConstraints.LINE_END;
		GridBagConstraints passwordLabelConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		passwordLabel = new JLabel("Password:");
		panel.add(passwordLabel, passwordLabelConstraints);

		// Add the confirm password label
		row = 5; col = 0; width = 1; height = 1;
		anchor=GridBagConstraints.LINE_END;
		GridBagConstraints comfirmPasswordLabelConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		confirmPasswordLabel = new JLabel("Confirm Password:");
		panel.add(confirmPasswordLabel, comfirmPasswordLabelConstraints);

		// Add the confirm password label
		row = 6; col = 0; width = 1; height = 1;
		anchor=GridBagConstraints.LINE_END;
		GridBagConstraints strengthLabelConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		strengthLabel = new JLabel("Password Strength:");
		panel.add(strengthLabel, strengthLabelConstraints);

		// Add the notes label
		row = 7; col = 0; width = 1; height = 1;
		anchor=GridBagConstraints.LINE_END;
		GridBagConstraints notesLabelConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		notesLabel = new JLabel("Notes:");
		panel.add(notesLabel, notesLabelConstraints);

		// Add the category combo box
		row = 1; col = 1; width = 2; height = 1;
		anchor = GridBagConstraints.LINE_START;
		insets = new Insets(3, 30, 3, 3);
		GridBagConstraints categoryComboBoxConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		categoryComboBox = new JComboBox<String>();
		categoryComboBox.setEditable(true);
		categoryComboBox.setPreferredSize(new Dimension(250, 20));
		for(int i = 0; i < categories.length; i++)
			categoryComboBox.addItem((String)categories[i]);
		categoryComboBox.setSelectedItem(correctedValues[0]);
		panel.add(categoryComboBox, categoryComboBoxConstraints);

		// Add the title field
		row = 2; col = 1; width = 2; height = 1;
		anchor = GridBagConstraints.LINE_START;
		GridBagConstraints titleFieldConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		titleField = new JTextField();
		titleField.setPreferredSize(new Dimension(250, 20));
		titleField.setText(correctedValues[1]);
		panel.add(titleField, titleFieldConstraints);

		// Add the username field
		row = 3; col = 1; width = 2; height = 1;
		anchor = GridBagConstraints.LINE_START;
		GridBagConstraints usernameFieldConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		usernameField = new JTextField();
		usernameField.setPreferredSize(new Dimension(250, 20));
		usernameField.setText(correctedValues[2]);
		panel.add(usernameField, usernameFieldConstraints);

		// Add the password field
		row = 4; col = 1; width = 2; height = 1;
		anchor = GridBagConstraints.LINE_START;
		GridBagConstraints passwordFieldConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		passwordField = new JPasswordField();
		passwordField.setPreferredSize(new Dimension(230, 20));
		passwordField.setEchoChar(defaultEchoChar);
		passwordField.setText(correctedValues[3]);
		panel.add(passwordField, passwordFieldConstraints);

		// Add the generate button
		row = 4; col = 2; width = 1; height = 1;
		anchor = GridBagConstraints.LINE_END;
		insets = new Insets(3, 0, 3, 3);
		GridBagConstraints generateButtonConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		generateButton = new JButton(new ImageIcon("..\\resources\\generate.png"));
		generateButton.setMargin(new Insets(0, 0, 0, 0));
		generateButton.setPreferredSize(new Dimension(22, 22));
		panel.add(generateButton, generateButtonConstraints);

		// Add the confirm password field
		row = 5; col = 1; width = 2; height = 1;
		insets = new Insets(3, 30, 3, 3);
		anchor = GridBagConstraints.LINE_START;
		GridBagConstraints confirmPasswordFieldConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		confirmPasswordField = new JPasswordField();
		confirmPasswordField.setPreferredSize(new Dimension(230, 20));
		confirmPasswordField.setEchoChar(defaultEchoChar);
		confirmPasswordField.setText(correctedValues[3]);
		panel.add(confirmPasswordField, confirmPasswordFieldConstraints);

		// Add the reveal button
		row = 5; col = 2; width = 1; height = 1;
		anchor = GridBagConstraints.LINE_END;
		insets = new Insets(3, 0, 3, 3);
		GridBagConstraints revealButtonConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		revealButton = new JButton(new ImageIcon("..\\resources\\reveal.png"));
		revealButton.setMargin(new Insets(0, 0, 0, 0));
		revealButton.setPreferredSize(new Dimension(22, 22));
		panel.add(revealButton, revealButtonConstraints);

		// Add the strength bar
		row = 6; col = 1; width = 1; height = 1;
		anchor = GridBagConstraints.LINE_START;
		insets = new Insets(3, 30, 3, 3);
		GridBagConstraints strengthBarConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
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
		row = 6; col = 2; width = 1; height = 1;
		anchor = GridBagConstraints.LINE_END;
		insets = new Insets(3, 3, 3, 3);
		GridBagConstraints dictionaryButtonConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		dictionaryButton = new JButton("Check Dictionary");
		dictionaryButton.setPreferredSize(new Dimension(120, 20));
		dictionaryButton.setEnabled(false);
		panel.add(dictionaryButton, dictionaryButtonConstraints);

		// Add the notes field
		row = 7; col = 1; width = 2; height = 1;
		insets = new Insets(3, 30, 3, 3);
		anchor = GridBagConstraints.LINE_START;
		GridBagConstraints notesFieldConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		notesField = new JTextArea();
		notesField.setBackground(Color.white);
		notesField.setEditable(true);
		notesField.setFont(usernameField.getFont());
		notesField.setLineWrap(true);
		notesField.setText(correctedValues[4]);
		JScrollPane notesPane = new JScrollPane(notesField);
		notesPane.setPreferredSize(new Dimension(250, 100));
		panel.add(notesPane, notesFieldConstraints);

		// Recursively unfocus everything on the panel
		recursiveUnfocus(panel);

		// Add the password generator listener
		generateButton.addMouseListener(generatePassword(passwordField, confirmPasswordField));

		// Add the reveal/mask password listener
		revealButton.addMouseListener(toggleEchoChar(defaultEchoChar, revealButton, passwordField, confirmPasswordField));

		// Add the check dictionary listener
		dictionaryButton.addMouseListener(checkDictionary(dictionaryButton, passwordField));
		passwordField.getDocument().addDocumentListener(toggleEnableComponent(dictionaryButton, passwordField, confirmPasswordField));
		confirmPasswordField.getDocument().addDocumentListener(toggleEnableComponent(dictionaryButton, passwordField, confirmPasswordField));

		// Add the strength bar listener
		passwordField.getDocument().addDocumentListener(determinePasswordStrength(passwordField, strengthBar));
		passwordField.setText(correctedValues[3]);

		// Create the option pane and dialog
		JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {"Accept", "Cancel"}, null);
		JDialog dialog = new JDialog(new JFrame(), "Entry Editor", true);
		dialog.setIconImage(new ImageIcon("..\\resources\\windowIcon.png").getImage());
		dialog.setContentPane(pane);
		dialog.setResizable(false);
		
		// Set the size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double screenWidth = screenSize.getWidth();
		double screenHeight = screenSize.getHeight();
		dialog.setBounds((int)screenWidth/2 - WINDOW_WIDTH/2, (int)screenHeight/2 - WINDOW_HEIGHT/2-50, WINDOW_WIDTH, WINDOW_HEIGHT);

		// Add the closing listeners
		pane.addPropertyChangeListener(buttonPressed(dialog, hashedKey));
		dialog.addWindowListener(windowClosing(dialog));
		
		// Add the listener to submit when enter is pressed in the correct field
		recursizeRemoveSelectionByEscape(dialog, pane, dialog);

		// Show the panel
		do {
			dialog.setVisible(true);
			
			// Reset the value of pane, so the user can click the same button twice
			pane.setValue(null);
			
		} while(!userIsDone);

		return createdEntry;
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
	 * Open the password generator, and assign the result to password fields.
	 * 
	 * @param fields The password fields to assign the result to.
	 * 
	 * @return A MouseAdapter.
	 */
	private static MouseAdapter generatePassword(JPasswordField... fields) {
		return new MouseAdapter() {
			public void mouseReleased(MouseEvent me) {
				if(me.getButton() == MouseEvent.BUTTON1) {
					// Generate the password
					char[] generatedPasswordChars = PasswordGenerator.display(defaultEchoChar, true, autoGeneratePasswordGlobal, autoCheckDictionaryGlobal);
					String generatedPassword;

					// Assign the generated password to the fields
					if(generatedPasswordChars != null) {
						generatedPassword = new String(generatedPasswordChars);
						for(int i = 0; i < fields.length; i++)
							fields[i].setText(generatedPassword);

						// Wipe the sensitive data
						generatedPassword = null;
						for(int i = 0; i < generatedPasswordChars.length; i++)
							generatedPasswordChars[i] = '\0';
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
	 * Create a table entry with the entered information.
	 * 
	 * @param dialog The dialog.
	 * @param hashedKey The key used to encrypt the password.
	 * 
	 * @return A PropertyChangeListener.
	 */
	private static PropertyChangeListener buttonPressed(JDialog dialog, byte[] hashedKey) {
		return new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent pce) {
				// The user clicked accept
				if(pce.getNewValue() != null && pce.getNewValue().equals("Accept")) {
					// Ensure the password fields match
					char[] password1 = passwordField.getPassword();
					char[] password2 = confirmPasswordField.getPassword();
					boolean equal = password1.length == password2.length;
					int confirm = 0;
					
					// Make sure the lengths are equal and the characters match
					if(equal)
						for(int i = 0; i < password1.length; i++)
							if(password1[i] != password2[i])
								equal = false;
					
					// If something didn't match, tell the user
					if(!equal) {
						JOptionPane.showMessageDialog(null, "The password fields do not match.", "Entry Creation Error", JOptionPane.ERROR_MESSAGE);
						dialog.setVisible(false);
						userIsDone = false;
						return;
					}
					
					// Make sure the password field is not empty
					if(password1.length == 0) {
						JOptionPane.showMessageDialog(null, "Password field cannot be empty.", "Empty Password Field", JOptionPane.ERROR_MESSAGE);
						userIsDone = false;
						dialog.setVisible(false);
						return;
					}
					
					// Check to make sure the password is strong
					if(strengthBar.getValue() != 100 && password1.length != 0) {
						confirm = JOptionPane.showConfirmDialog(null, "Password strength is not at 100%. Are you sure you want to continue?", "Weak Password Warning", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, new ImageIcon("..\\resources\\searchResults.png"));
						if(confirm == JOptionPane.NO_OPTION) {
							dialog.setVisible(false);
							return;
						}
					}
					
					// Get all of the fields
					String category = (String)categoryComboBox.getSelectedItem();
					String title = titleField.getText();
					String username = usernameField.getText();
					String notes = notesField.getText();
					char[] passwordChars = passwordField.getPassword();
					String password = new String(passwordChars);
					
					// Change category to an empty string if null
					category = (category == null)? "" : category;

					// Check the dictionary for the password
					String result = "";
					if(autoCheckDictionaryGlobal) {
						result = Password.checkDictionary(password);
						// Warn the user.
						if(result != null && passwordChars.length != 0) {
							// Of an exact match
							if(result.toLowerCase().equals(password))
								confirm = JOptionPane.showConfirmDialog(null, "An exact match of this password was found in a dictionary. Are you sure you want to continue?", "Weak Password Warning", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, new ImageIcon("..\\resources\\searchResults.png"));
							// Of a similar match
							else
								confirm = JOptionPane.showConfirmDialog(null, "A similar match of this password was found in a dictionary: \""+result+".\" Are you sure you want to continue?", "Weak Password Warning", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, new ImageIcon("..\\resources\\searchResults.png"));
	
							if(confirm == JOptionPane.NO_OPTION) {
								dialog.setVisible(false);
								return;
							}
						}
					}
					
					// Encrypt and encode the password, then create the entry
					try {
						password = new String(Base64.getEncoder().encode(AES.encrypt(password.getBytes(), hashedKey)));
						createdEntry = new TableEntry(category, title, username, password, notes);
					} catch (Exception e) {
						createdEntry = null;
						e.printStackTrace();
					}
					
					// Wipe the sensitive data
					result = null;
					password = null;
					for(int i = 0; i < passwordChars.length; i++)
						passwordChars[i] = '\0';
					for(int i = 0; i < password1.length; i++)
						password1[i] = '\0';
					for(int i = 0; i < password2.length; i++)
						password2[i] = '\0';
					
					// Set the user as done
					userIsDone = true;
				}
				// The user wants to cancel
				else if(pce.getNewValue() != null && pce.getNewValue().equals("Cancel"))
					userIsDone = true;
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
	public static WindowAdapter windowClosing(JDialog dialog) {
		return new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				userIsDone = true;
				createdEntry = null;
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
	private static void recursizeRemoveSelectionByEscape(Component root, JOptionPane pane, JDialog dialog) {
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
				}
			}
		});
		
		// Do the recursion
		if (root instanceof Container) {
			Component[] children = ((Container) root).getComponents();
			for (Component child : children)
				recursizeRemoveSelectionByEscape(child, pane, dialog);
		}
	}
}
