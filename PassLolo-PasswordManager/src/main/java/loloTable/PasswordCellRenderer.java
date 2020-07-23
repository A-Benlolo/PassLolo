package loloTable;

import java.awt.Component;
import java.util.Base64;

import javax.swing.JPasswordField;
import javax.swing.JTable;

import crypto.AES;

public class PasswordCellRenderer extends PaddedCellRenderer {
	private static final long serialVersionUID = 6001897431907305255L;
	private static final char DEFAULT_ECHO_CHAR = new JPasswordField().getEchoChar();
	private byte[] hashedKey;

	// The rendering component
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		// The working data
		byte[] decryptedBytes = new byte[0];
		String decryptedString = "";
		
		// Reveal the cell if it is selected
		try {
			decryptedBytes = AES.decrypt(Base64.getDecoder().decode(((String)table.getValueAt(row, 2))), hashedKey);
			decryptedString = new String(decryptedBytes);
			if (isSelected)
				setText(decryptedString);
			else
				setText(masked(decryptedString.length(), DEFAULT_ECHO_CHAR));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// Wipe the sensitive data
		decryptedString = null;
		for(int i = 0; i < decryptedBytes.length; i++)
			decryptedBytes[i] = '\0';
		decryptedBytes = null;

		return this;
	}

	/**
	 * Sets the key to use when decrypting the column.
	 * 
	 * @param hashedkey What the key will be set to.
	 */
	public void setHashedPassword(byte[] hashedkey) {
		this.hashedKey = new byte[hashedkey.length];
		for(int i = 0; i < hashedkey.length; i++)
			this.hashedKey[i] = hashedkey[i];
	}

	/**
	 * Generates a mask of a specified length, composed of a specified character.
	 * 
	 * @param length The length of the mask.
	 * @param mask The character used to mask
	 * @return
	 */
	private String masked(int length, char mask) {
		String toReturn = "";
		for(int i = 0; i < 10; i++)
			toReturn += mask;
		return toReturn;

	}
}