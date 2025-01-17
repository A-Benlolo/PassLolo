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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.text.DefaultFormatter;

import crypto.Password;

public class PasswordGenerator {
	private static final int WINDOW_WIDTH = 450;
	private static final int WINDOW_HEIGHT = 315;
	private static boolean userIsDone = false;
	private static boolean userWantsPassword = false;

	/**
	 * Display the window for password generation.
	 * 
	 * @param mask The character to mask the password with.
	 * @param returnPassword If a password will be returned. If not, then the settings will be returned.
	 * 
	 * @return The generated password, settings, or null if the user cancels.
	 */
	public static char[] display(char mask, boolean returnPassword, boolean autoGeneratePassword, boolean autoCheckDictionary) {
		// The variables for readability
		int row, col;
		int width, height;
		int weightX=0, weightY=0;
		int anchor=GridBagConstraints.CENTER, fill=GridBagConstraints.NONE;
		Insets insets = new Insets(3, 3, 3, 3);
		int ipadx = 0, ipady = 0;
		userWantsPassword = returnPassword;

		// Create the tabbed pane
		JTabbedPane tabbedPane = new JTabbedPane();

		// Create the panel for settings
		GridBagLayout settingsLayout = new GridBagLayout();
		JPanel settingsPanel = new JPanel(settingsLayout);

		JLabel strengthLabel = null;
		JProgressBar passwordStrength = null;
		JPasswordField password = null;
		JButton unmaskButton = null;
		JButton generateButton = null;
		JButton checkDictionary = null;
		if(returnPassword) {
			// Add the label prompting the strength
			row = 0; col = 0; width = 1; height = 1;
			anchor = GridBagConstraints.LINE_END;
			GridBagConstraints strengthLabelConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
			strengthLabel = new JLabel("Strength:");
			settingsPanel.add(strengthLabel, strengthLabelConstraints);

			// Add the bar to show the strength
			row = 0; col = 1; width = 1; height = 1;
			anchor = GridBagConstraints.LINE_START;
			GridBagConstraints strengthConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
			passwordStrength = new JProgressBar(0, 100);
			passwordStrength.setMinimumSize(new Dimension(75, 15));
			passwordStrength.setPreferredSize(new Dimension(75, 15));
			passwordStrength.setStringPainted(true);
			passwordStrength.setUI(new BasicProgressBarUI() {
				protected Color getSelectionBackground() { return Color.BLACK; }
				protected Color getSelectionForeground() { return Color.BLACK; }});
			passwordStrength.setValue(0);
			settingsPanel.add(passwordStrength, strengthConstraints);

			// Add the password field to the settings panel
			row = 1; col = 0; width = 2; height = 1;
			anchor = GridBagConstraints.LINE_END;
			GridBagConstraints passwordConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
			password = new JPasswordField();
			password.setEchoChar(mask);
			password.setPreferredSize(new Dimension(WINDOW_WIDTH-50, 20));
			password.setMinimumSize(new Dimension(350, 20));	
			settingsPanel.add(password, passwordConstraints);

			//Add the toggle password mask button to the settings panel
			row = 1; col = 2; width = 1; height = 1;
			anchor = GridBagConstraints.LINE_START;
			insets = new Insets(0, -5, 0, 0);
			GridBagConstraints unmaskButtonConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
			unmaskButton = new JButton(new ImageIcon("..\\resources\\reveal.png"));
			unmaskButton.setPreferredSize(new Dimension(22, 22));
			unmaskButton.setMaximumSize(new Dimension(22, 22));
			unmaskButton.setMinimumSize(new Dimension(22, 22));
			unmaskButton.setMargin(new Insets(0, 0, 0, 0));
			unmaskButton.setFocusable(false);
			settingsPanel.add(unmaskButton, unmaskButtonConstraints);

			// Add the Generate button to the settings panel
			row = 2; col = 0; width = 1; height = 1;
			anchor = GridBagConstraints.CENTER;
			insets = new Insets(3, 3, 3, 3);
			GridBagConstraints generateConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
			generateButton = new JButton("Generate");
			settingsPanel.add(generateButton, generateConstraints);

			// Add the Check Dictionary button to the settings panel
			row = 2; col = 1; width = 1; height = 1;
			insets = new Insets(3, 3, 3, 3);
			GridBagConstraints checkDictionaryConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
			checkDictionary = new JButton("Check Dictionary");
			settingsPanel.add(checkDictionary, checkDictionaryConstraints);
		}
		JLabel lengthLabel = null;
		JSpinner lengthSpinner = null;
		JLabel sizeLabel = null;
		JSpinner sizeSpinner = null;
		if(returnPassword) {
			// Add the label to prompt for length
			row = 3; col = 0; width = 1; height = 1;
			anchor = GridBagConstraints.LINE_START;
			insets = new Insets(3, 3, 3, 20);
			GridBagConstraints lengthLabelConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
			lengthLabel  = new JLabel("Length of password:");
			settingsPanel.add(lengthLabel, lengthLabelConstraints);

			// Add the length spinner
			row = 3; col = 1; width = 1; height = 1;
			GridBagConstraints lengthSpinnerConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
			lengthSpinner = new JSpinner();
			lengthSpinner.addChangeListener(dontAllowNegativeNumbers(lengthSpinner));
			lengthSpinner.setPreferredSize(new Dimension(40, 20));
			lengthSpinner.setMinimumSize(new Dimension(40, 20));
			lengthSpinner.setValue(30);
			settingsPanel.add(lengthSpinner, lengthSpinnerConstraints);
			// Make the spinner commit changes on edit instead of lost focus
			JComponent comp = lengthSpinner.getEditor();
			JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
			DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
			formatter.setCommitsOnValidEdit(true);
		}
		else {
			// Add the label to prompt for length
			row = 3; col = 0; width = 1; height = 1;
			anchor = GridBagConstraints.LINE_START;
			insets = new Insets(3, 3, 3, 20);
			GridBagConstraints lengthLabelConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
			lengthLabel  = new JLabel("Length of password:");
			settingsPanel.add(lengthLabel, lengthLabelConstraints);

			// Add the length spinner
			row = 3; col = 1; width = 1; height = 1;
			GridBagConstraints lengthSpinnerConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
			anchor = GridBagConstraints.LINE_START;
			lengthSpinner = new JSpinner();
			lengthSpinner.addChangeListener(dontAllowNegativeNumbers(lengthSpinner));
			lengthSpinner.setPreferredSize(new Dimension(40, 20));
			lengthSpinner.setMinimumSize(new Dimension(40, 20));
			lengthSpinner.setValue(30);
			settingsPanel.add(lengthSpinner, lengthSpinnerConstraints);
			// Make the spinner commit changes on edit instead of lost focus
			JComponent comp1 = lengthSpinner.getEditor();
			JFormattedTextField field1 = (JFormattedTextField) comp1.getComponent(0);
			DefaultFormatter formatter1 = (DefaultFormatter) field1.getFormatter();
			formatter1.setCommitsOnValidEdit(true);

			// Add the label to prompt for the number of passwords
			row = 3; col = 2; width = 1; height = 1;
			anchor = GridBagConstraints.LINE_START;
			insets = new Insets(3, 3, 3, 20);
			GridBagConstraints sizeLabelConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
			sizeLabel  = new JLabel("Number of passwords:");
			settingsPanel.add(sizeLabel, sizeLabelConstraints);

			// Add the length spinner
			row = 3; col = 3; width = 1; height = 1;
			GridBagConstraints sizeSpinnerConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
			anchor = GridBagConstraints.LINE_START;
			sizeSpinner = new JSpinner();
			sizeSpinner.addChangeListener(dontAllowNegativeNumbers(sizeSpinner));
			sizeSpinner.setPreferredSize(new Dimension(40, 20));
			sizeSpinner.setMinimumSize(new Dimension(40, 20));
			sizeSpinner.setValue(1);
			settingsPanel.add(sizeSpinner, sizeSpinnerConstraints);
			// Make the spinner commit changes on edit instead of lost focus
			JComponent comp2 = lengthSpinner.getEditor();
			JFormattedTextField field2 = (JFormattedTextField) comp2.getComponent(0);
			DefaultFormatter formatter2 = (DefaultFormatter) field2.getFormatter();
			formatter2.setCommitsOnValidEdit(true);
		}

		// Ensure the boxes are lined up correctly
		if(returnPassword)
			width = 1;
		else
			width = 2;

		// Add the lowercase check box
		row = 4; col = 0; height = 1;
		insets = new Insets(3, 0, 3, 20);
		GridBagConstraints lowercaseCheckBoxConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		JCheckBox lowercaseCheckBox = new JCheckBox("Lowercase (a, b, c, ...)");
		lowercaseCheckBox.setSelected(true);
		settingsPanel.add(lowercaseCheckBox, lowercaseCheckBoxConstraints);

		// Add the lowercase check box
		row = 5; col = 0; height = 1;
		GridBagConstraints uppercaseCheckBoxConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		JCheckBox uppercaseCheckBox = new JCheckBox("Uppercase (A, B, C, ...)");
		uppercaseCheckBox.setSelected(true);
		settingsPanel.add(uppercaseCheckBox, uppercaseCheckBoxConstraints);

		// Add the digits check box
		row = 6; col = 0; height = 1;
		GridBagConstraints digitsCheckBoxConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		JCheckBox digitsCheckBox = new JCheckBox("Digits (1, 2, 3, ...)");
		digitsCheckBox.setSelected(true);
		settingsPanel.add(digitsCheckBox, digitsCheckBoxConstraints);

		// Ensure the column is correct
		if(returnPassword)
			col = 1;
		else
			col = 2;

		// Add the numeric symbols check box
		row = 4; height = 1;
		GridBagConstraints numericSymbolsCheckBoxConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		JCheckBox numericSymbolsCheckBox = new JCheckBox("Numeric Symbols (!, @, #, ...)");
		numericSymbolsCheckBox.setSelected(true);
		settingsPanel.add(numericSymbolsCheckBox, numericSymbolsCheckBoxConstraints);

		// Add the extra symbols check box
		row = 5; height = 1;
		GridBagConstraints extraSymbolsCheckBoxConstraints = new GridBagConstraints(col, row, width, height, weightX, weightY, anchor, fill, insets, ipadx, ipady);
		JCheckBox extraSymbolsCheckBox = new JCheckBox("Extra Symbols (+, ?, ~, ...)");
		settingsPanel.add(extraSymbolsCheckBox, extraSymbolsCheckBoxConstraints);

		// Create the panel for preview
		JPanel previewPanel = new JPanel();

		// Create the label prompting that these are previews
		JLabel previewLabel = new JLabel("Here are some examples with the settings that you applied...");
		previewPanel.add(previewLabel);

		// Create the text area for the previews
		JTextArea preview = new JTextArea("Loading Previews...");
		preview.setPreferredSize(new Dimension(375, 500));
		preview.setEditable(false);

		// Create the scroll pane for the previews
		JScrollPane previewScrollPane = new JScrollPane(preview);
		if(returnPassword)
			previewScrollPane.setPreferredSize(new Dimension(400, 155));
		else
			previewScrollPane.setPreferredSize(new Dimension(400, 80));
		previewPanel.add(previewScrollPane);

		// Add the tabs
		tabbedPane.addTab("Settings", null, settingsPanel);
		tabbedPane.addTab("Preview", null, previewPanel);
		
		// Disable focus of everything
		recursiveUnfocus(tabbedPane);

		// Add the mask/unmask listener
		if(returnPassword) {
			unmaskButton.addMouseListener(toggleEchoChar(password, unmaskButton, mask));

			// Add the strength listener
			password.getDocument().addDocumentListener(determinePasswordStrength(password, passwordStrength));

			// Add the generation listener
			generateButton.addMouseListener(generatePassword(password, lengthSpinner, lowercaseCheckBox, uppercaseCheckBox, digitsCheckBox, numericSymbolsCheckBox, extraSymbolsCheckBox));
			
			
			// Add the auto generation listener
			if(autoGeneratePassword) {
				MouseEvent me = new MouseEvent(generateButton, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, generateButton.getX(), generateButton.getY(), 1, false);
				generateButton.dispatchEvent(me);
				lowercaseCheckBox.addActionListener(autoGeneratePasswordBoxes(generateButton, password, lengthSpinner, lowercaseCheckBox, uppercaseCheckBox, digitsCheckBox, numericSymbolsCheckBox, extraSymbolsCheckBox));
				uppercaseCheckBox.addActionListener(autoGeneratePasswordBoxes(generateButton, password, lengthSpinner, lowercaseCheckBox, uppercaseCheckBox, digitsCheckBox, numericSymbolsCheckBox, extraSymbolsCheckBox));
				digitsCheckBox.addActionListener(autoGeneratePasswordBoxes(generateButton, password, lengthSpinner, lowercaseCheckBox, uppercaseCheckBox, digitsCheckBox, numericSymbolsCheckBox, extraSymbolsCheckBox));
				numericSymbolsCheckBox.addActionListener(autoGeneratePasswordBoxes(generateButton, password, lengthSpinner, lowercaseCheckBox, uppercaseCheckBox, digitsCheckBox, numericSymbolsCheckBox, extraSymbolsCheckBox));
				extraSymbolsCheckBox.addActionListener(autoGeneratePasswordBoxes(generateButton, password, lengthSpinner, lowercaseCheckBox, uppercaseCheckBox, digitsCheckBox, numericSymbolsCheckBox, extraSymbolsCheckBox));
				lengthSpinner.addChangeListener(autoGeneratePasswordSpinner(generateButton, password, lengthSpinner, lowercaseCheckBox, uppercaseCheckBox, digitsCheckBox, numericSymbolsCheckBox, extraSymbolsCheckBox));
			}

			// Add the check dictionary listener
			checkDictionary.addMouseListener(checkDictionary(password));

			// Add the enable and disable listener for the check dictionary button
			password.getDocument().addDocumentListener(toggleEnableComponent(password, checkDictionary));
		}

		// Add the listener to generate preview passwords
		tabbedPane.addChangeListener(generatePreviewPasswords(preview, lengthSpinner, lowercaseCheckBox, uppercaseCheckBox, digitsCheckBox, numericSymbolsCheckBox, extraSymbolsCheckBox));

		JOptionPane pane = new JOptionPane(tabbedPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {"Accept", "Cancel"}, null);
		JDialog dialog = new JDialog(new JFrame(), "Password Generator", true);
		dialog.setIconImage(new ImageIcon("..\\resources\\windowIcon.png").getImage());
		dialog.setContentPane(pane);
		dialog.setResizable(false);
		
		// Set the size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double screenWidth = screenSize.getWidth();
		double screenHeight = screenSize.getHeight();
		dialog.setBounds((int)screenWidth/2 - WINDOW_WIDTH/2, (int)screenHeight/2 - WINDOW_HEIGHT/2-50, WINDOW_WIDTH, WINDOW_HEIGHT);

		// Add the listener for the buttons to function
		pane.addPropertyChangeListener(buttonPressed(dialog, password, passwordStrength, autoCheckDictionary, lowercaseCheckBox, uppercaseCheckBox, digitsCheckBox, numericSymbolsCheckBox, extraSymbolsCheckBox));
		dialog.addWindowListener(windowClosing(dialog, password));

		// Add the listener for key strokes to submit/exit
		recursizeRemoveSelectionByEscape(dialog, pane, dialog, "Accept", "Cancel");
		
		// Show the panel
		do {
			dialog.setVisible(true);

			if(!userIsDone)
				pane.setValue(null);

		} while(!userIsDone);

		// Return the password
		if(returnPassword) {
			char[] toReturn = password.getPassword();
			if(pane.getValue() != null && pane.getValue().equals("Accept") && toReturn.length != 0)
				return toReturn;
			else
				return null;
		}
		// Return the settings
		else {
			if(pane.getValue() != null && pane.getValue().equals("Accept")) {
				// Get the boolean values
				boolean[] selectedBoxes = new boolean[5];
				selectedBoxes[0] = lowercaseCheckBox.isSelected();
				selectedBoxes[1] = uppercaseCheckBox.isSelected();
				selectedBoxes[2] = digitsCheckBox.isSelected();
				selectedBoxes[3] = numericSymbolsCheckBox.isSelected();
				selectedBoxes[4] = extraSymbolsCheckBox.isSelected();

				// Convert boolean to character
				char[] toReturn = new char[7];
				for(int i = 0; i < selectedBoxes.length; i++)
					toReturn[i] = (selectedBoxes[i])? 'T' : 'F';
				int length = (int)lengthSpinner.getValue();
				int howMany = (int)sizeSpinner.getValue();

				// Get the length and count
				toReturn[5] = (char)length;
				toReturn[6] = (char) howMany;

				// Return the char[]
				return toReturn;
			}
			else
				return null;
		}
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
	 * @param field The password to reveal or mask.
	 * @param mask The character to mask with.
	 * 
	 * @return A MouseAdapter.
	 */
	private static MouseAdapter toggleEchoChar(JPasswordField field, JButton button, char mask) {
		return new MouseAdapter() {
			public void mouseReleased(MouseEvent me) {
				if(me.getButton() == MouseEvent.BUTTON1) {
					if(field.getEchoChar() == mask) {
						field.setEchoChar('\0');
						button.setIcon(new ImageIcon("..\\resources\\hide.png"));
					}
					else {
						field.setEchoChar(mask);
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
	 * Generate a password that meets criteria when a button is released.
	 * 
	 * @param field The password field.
	 * @param spinner The spinner that has the length of the password.
	 * @param checkBoxes The criteria.
	 * 
	 * @return A MouseAdapter.
	 */
	private static MouseAdapter generatePassword(JPasswordField field, JSpinner spinner, JCheckBox... checkBoxes) {
		return new MouseAdapter() {
			public void mouseReleased(MouseEvent me) {
				boolean hasSelection = false;
				for(int i = 0; i < checkBoxes.length; i++)
					if(checkBoxes[i].isSelected())
						hasSelection = true;
				if(hasSelection)
					field.setText(Password.generate((Integer)spinner.getValue(), checkBoxes[0].isSelected(), checkBoxes[1].isSelected(), checkBoxes[2].isSelected(), checkBoxes[3].isSelected(), checkBoxes[4].isSelected()));
				else
					field.setText(null);
			}
		};
	}

	/**
	 * Automatically generate a password that meets criteria when the criteria is changed.
	 * 
	 * @param generateButton The button that should be pressed to generate the password
	 * @param field The password field.
	 * @param spinner The spinner that has the length of the password.
	 * @param checkBoxes The criteria.
	 * 
	 * @return An ActionListener.
	 */
	private static ActionListener autoGeneratePasswordBoxes(JButton generateButton, JPasswordField field, JSpinner spinner, JCheckBox... checkBoxes) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MouseEvent me = new MouseEvent(generateButton, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, generateButton.getX(), generateButton.getY(), 1, false);
				generateButton.dispatchEvent(me);
				generatePassword(field, spinner, checkBoxes);
			}
		};
	}
	
	/**
	 * Automatically generate a password that meets criteria when the criteria is changed.
	 * 
	 * @param generateButton The button that should be pressed to generate the password
	 * @param field The password field.
	 * @param spinner The spinner that has the length of the password.
	 * @param checkBoxes The criteria.
	 * 
	 * @return An ChangeListener.
	 */
	private static ChangeListener autoGeneratePasswordSpinner(JButton generateButton, JPasswordField field, JSpinner spinner, JCheckBox... checkBoxes) {
		return new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				MouseEvent me = new MouseEvent(generateButton, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, generateButton.getX(), generateButton.getY(), 1, false);
				generateButton.dispatchEvent(me);
				generatePassword(field, spinner, checkBoxes);
			}
		};
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
	 * Check a dictionary for an entered password.
	 * 
	 * @param field The password field.
	 * 
	 * @return A MouseAdapter.
	 */
	private static MouseAdapter checkDictionary(JPasswordField field) {
		return new MouseAdapter() {
			public void mouseReleased(MouseEvent me) {
				if(me.getButton() == MouseEvent.BUTTON1) {
					// Get the password
					char[] passwordChars = field.getPassword();
					String password = new String(passwordChars);
					String result = "ERROR";

					if(passwordChars.length != 0) {
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
	 * Set a component to be disabled when a password field is blank.
	 * 
	 * @param field The password field.
	 * @param c The component
	 * @return A DocumentListener.
	 */
	private static DocumentListener toggleEnableComponent(JPasswordField field, Component c) {
		return new DocumentListener() {

			public void insertUpdate(DocumentEvent e) {
				c.setEnabled(true);
			}

			public void removeUpdate(DocumentEvent e) {
				char[] password = field.getPassword();
				if(password.length == 0)
					c.setEnabled(false);

				for(int i = 0; i < password.length; i++)
					password[i] = '\0';
			}

			public void changedUpdate(DocumentEvent e) { /*Not Applicable*/ }

		};
	}

	/**
	 * Generate a list of generated passwords using the applied settings.
	 * 
	 * @param textArea The area to write the passwords.
	 * @param spinner The length of the passwords.
	 * @param checkBoxes The password options.
	 * 
	 * @return A ChangeListener.
	 */
	private static ChangeListener generatePreviewPasswords(JTextArea textArea, JSpinner spinner, JCheckBox... checkBoxes) {
		return new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// Ensure something was selected
				boolean hasSelection = false;
				for(int i = 0; i < checkBoxes.length; i++)
					if(checkBoxes[i].isSelected())
						hasSelection = true;

				if(hasSelection) {
					textArea.setText("");
					String currPassword;
					// Populate the text area
					for(int i = 0; i < 27; i++) {
						currPassword = Password.generate((Integer)spinner.getValue(), checkBoxes[0].isSelected(), checkBoxes[1].isSelected(), checkBoxes[2].isSelected(), checkBoxes[3].isSelected(), checkBoxes[4].isSelected());
						textArea.append(currPassword+"\n");
					}
				}
				else
					textArea.setText("Invalid settings. Cannot preview passwords.");
				textArea.setCaretPosition(0);
			}
		};
	}
	
	/**
	 * Ensure the password the user entered is powerful. If it is not, display a warning messages.
	 * 
	 * @param dialog The dialog.
	 * @param field The password field.
	 * @param strength The strength bar of the password.
	 * 
	 * @return A PropertyChangeListener.
	 */
	private static PropertyChangeListener buttonPressed(JDialog dialog, JPasswordField field, JProgressBar strength, boolean autoCheckDictionary, JCheckBox... checkBoxes) {
		return new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent pce) {
				// The user clicked accept
				if(pce.getNewValue() != null && pce.getNewValue().equals("Accept")) {
					//The user wants a password
					if(userWantsPassword) {
						// Prepare the data
						char[] passwordChars;
						String password;
						String result;
						int confirm;
						
						// Check to see if the field is empty
						passwordChars = field.getPassword();
						if(passwordChars.length == 0) {
							JOptionPane.showMessageDialog(null, "Password field cannot be empty.", "Empty Password Field", JOptionPane.ERROR_MESSAGE);
							userIsDone = false;
							dialog.setVisible(false);
							return;
						}
						
						// Check to make sure the password is strong
						if(strength.getValue() != 100) {
							confirm = JOptionPane.showConfirmDialog(null, "Password strength is not at 100%. Are you sure you want to continue?", "Weak Password Warning", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, new ImageIcon("..\\resources\\searchResults.png"));
							if(confirm == JOptionPane.NO_OPTION) {
								dialog.setVisible(false);
								return;
							}
						}

						// Check to make sure the password is not in the dictionary
						if(autoCheckDictionary) {
							try {
								password = new String(passwordChars).toLowerCase();
								result = Password.checkDictionary(password);
								confirm = 0;
	
								// Warn the user.
								if(result != null) {
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
								
								// Wipe the sensitive data
								password = null;
								result = null;
								for(int i = 0; i < passwordChars.length; i++)
									passwordChars[i] = '\0';
	
							} catch(Exception e) {
								e.printStackTrace();
							}
						}
						userIsDone = true;
					}
					// The user wants the settings
					else {
						boolean areValidSettings = false;
						
						// Determine if at least one box is checked
						for(int i = 0; i < checkBoxes.length; i++)
							if(checkBoxes[i].isSelected())
								areValidSettings = true;
						
						if(!areValidSettings)
							JOptionPane.showMessageDialog(null, "Invalid password settings! At least one box needs to be ticked.", "Invalid Settings", JOptionPane.ERROR_MESSAGE);
						
						userIsDone = areValidSettings;
					}
				}
				// The user wants to cancel
				else if(pce.getNewValue() != null && pce.getNewValue().equals("Cancel"))
					userIsDone = true;
				dialog.setVisible(false);
			}
		};
	}

	/**
	 * Close the dialog and prepare to return null.
	 * 
	 * @param dialog The dialog.
	 * @param field The password field.
	 * 
	 * @return A WindowAdapter.
	 */
	public static WindowAdapter windowClosing(JDialog dialog, JPasswordField field) {
		return new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				userIsDone = true;
				if(userWantsPassword)
					field.setText(null);
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
	public static void recursizeRemoveSelectionByEscape(Component root, JOptionPane pane, JDialog dialog, String... options) {
		root.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				// Apply the accept action
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					dialog.setVisible(false);
					pane.setValue(options[0]);
					userIsDone = true;
				}
				// Apply the cancel action
				else if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
					dialog.setVisible(false);
					pane.setValue(options[1]);
					userIsDone = true;
				}
			}
		});
		
		// Do the recursion
		if (root instanceof Container) {
			Component[] children = ((Container) root).getComponents();
			for (Component child : children)
				recursizeRemoveSelectionByEscape(child, pane, dialog, options);
		}
	}
}
