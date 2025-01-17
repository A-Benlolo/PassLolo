package loloTable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JPasswordField;

import crypto.Password;

public class TableEntry {
	private static final char DEFAULT_ECHO_CHAR = new JPasswordField().getEchoChar();
	private static final String MASKED = new String(new char[] {DEFAULT_ECHO_CHAR, DEFAULT_ECHO_CHAR, DEFAULT_ECHO_CHAR, DEFAULT_ECHO_CHAR, DEFAULT_ECHO_CHAR, DEFAULT_ECHO_CHAR, DEFAULT_ECHO_CHAR, DEFAULT_ECHO_CHAR, DEFAULT_ECHO_CHAR, DEFAULT_ECHO_CHAR});
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mma");
	private String id;
	private String category;
	private String title;
	private String username;
	private String password;
	private String notes;
	private String createdTimestamp;
	private String modifiedTimestamp;
	
	/**
	* Creates a table entry full of empty values.
	*/
	public TableEntry() {
		title = "";
		username = "";
		password = "";
		createdTimestamp = TIME_FORMATTER.format(LocalDateTime.now());
		modifiedTimestamp = TIME_FORMATTER.format(LocalDateTime.now());
		id = generateID();
	}
	
	/**
	* Creates a table entry filled by the parameters. 
	*
	* @param category	The category of this entry.
	* @param title		The title of this entry.
	* @param username	The username for the entry.
	* @param password	The encrypted, encoded password for this entry.
	* @param notes		The notes for this entry.
	*/
	public TableEntry(String category, String title, String username, String password, String notes) {
		this.category = category;
		this.title = title;
		this.username = username;
		this.password = password;
		this.notes = notes;
		
		createdTimestamp = TIME_FORMATTER.format(LocalDateTime.now());
		modifiedTimestamp = TIME_FORMATTER.format(LocalDateTime.now());
		id = generateID();
	}

	/**
	 * Create a table entry that is a copy.
	 * 
	 * @param tableEntry The existing table entry
	 */
	public TableEntry(TableEntry tableEntry) {
		this.category = tableEntry.category;
		this.title = tableEntry.title;
		this.username = tableEntry.username;
		this.password = tableEntry.password;
		this.createdTimestamp = tableEntry.createdTimestamp;
		this.modifiedTimestamp = tableEntry.modifiedTimestamp;
		this.notes = tableEntry.notes;
		this.id = tableEntry.id;
	}
	
	/**
	 * Sets the category for this entry, and updates the timestamp.
	 * 
	 * @param title The category for this entry.
	 */
	public void setCategory(String category) {
		this.category = category;
		modifiedTimestamp = TIME_FORMATTER.format(LocalDateTime.now());
	}
	
	/**
	 * Sets the title for this entry, and updates the timestamp.
	 * 
	 * @param title The title for this entry.
	 */
	public void setTitle(String title) {
		this.title = title;
		modifiedTimestamp = TIME_FORMATTER.format(LocalDateTime.now());
	}
	
	/**
	 * Sets the username for this entry, and updates the timestamp.
	 * 
	 * @param username The username for this entry.
	 */
	public void setUsername(String username) {
		this.username = username;
		modifiedTimestamp = TIME_FORMATTER.format(LocalDateTime.now());
	}
	
	/**
	 * Sets the password for this entry, and updates the timestamp.
	 * 
	 * @param password The encrypted, encoded password for this entry.
	 */
	public void setPassword(String password) {
		this.password = password;
		modifiedTimestamp = TIME_FORMATTER.format(LocalDateTime.now());
	}
	
	/**
	 * Sets the notes for this entry, and updates the timestamp.
	 * 
	 * @param password The notes for this entry.
	 */
	public void setNotes(String notes) {
		this.notes = notes;
		modifiedTimestamp = TIME_FORMATTER.format(LocalDateTime.now());
	}
	
	/**
	 * Sets the created timestamp for this entry.
	 * <br>
	 * This should only be used when creating a copy!
	 * 
	 * @param timestamp The created timestamp for this entry.
	 */
	public void setCreatedTimestamp(String timestamp) {
		this.createdTimestamp = timestamp;
	}
	
	/**
	 * Sets the modified timestamp for this entry.
	 * <br>
	 * This should only be used when creating a copy!
	 * 
	 * @param timestamp The modified timestamp for this entry.
	 */
	public void setModifiedTimestamp(String timestamp) {
		this.modifiedTimestamp = timestamp;
	}
	
	/**
	 * Sets the id for this entry
	 * <br>
	 * This should only be used when creating a copy!
	 * 
	 * @param timestamp The id for this entry.
	 */
	public void setID(String id) {
		this.id = id;
	}

	/**
	 * Get the category for this entry.
	 * 
	 * @return The category for this entry.
	 */
	public String getCategory() {
		return category;
	}
	
	/**
	 * Get the title for this entry.
	 * 
	 * @return The title for this entry.
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Get the username for this entry.
	 * 
	 * @return The username for this entry.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Get the password for this entry.
	 * 
	 * @param masked Whether you want the password to be masked or not.
	 * 
	 * @return The encrypted, encoded password for this entry.
	 */
	public String getPassword(boolean masked) {
		if(masked)
			return MASKED;
		return password;
	}

	/**
	 * Get the created timestamp for this entry.
	 * 
	 * @return The created timestamp for this entry.
	 */
	public String getCreatedTimestamp() {
		return createdTimestamp;
	}
	
	/**
	 * Get the modified timestamp for this entry.
	 * 
	 * @return The modified timestamp for this entry.
	 */
	public String getModifiedTimestamp() {
		return modifiedTimestamp;
	}
	
	/**
	 * Get the notes for this entry.
	 * 
	 * @return The notes for this entry.
	 */
	public String getNotes() {
		return notes;
	}
	
	/**
	 * Get the id for this entry.
	 * 
	 * @return The id for this entry.
	 */
	public String getID() {
		return id;
	}
	
	/**
	 * Determines if two table entries have the title, username, and category.
	 * <p>
	 * Every other field is ignored.
	 * 
	 * @param obj The Object entry in question.
	 * 
	 * @return A boolean value for if they are equal.
	 */
	public boolean equals(Object obj) {
		if(obj instanceof TableEntry) {
			TableEntry tableEntry = (TableEntry) obj;
			
			return tableEntry.title.equals(this.title) && tableEntry.username.equals(this.username) && tableEntry.category.equals(this.category); 
		}
		return false;
	}

	/**
	 * Determines if two table entries have the same id
	 * 
	 * @param tableEntry The table entry to compare to
	 * 
	 * @return A boolean for equality
	 */
	public boolean matchesID(TableEntry tableEntry) {
		return this.id.equals(tableEntry.id);
	}
	
	/**
	 * Gets the data of this table entry in an easy to read way.
	 * 
	 * @return The data for this table entry.
	 */
	public String toString() {
		return "[Category: "+category+", Title: "+title+", Username: "+username+", Password: "+password+", Creation Time: "+createdTimestamp+" Last Modification Time: "+modifiedTimestamp+"]";
	}

	/**
	 * Gets the private data of this entry in the form of an Object[].
	 * 
	 * @return An Object[] of all private data.
	 */
	public Object[] toFullObjectArray() {
		return new Object[] {id, category, title, username, password, notes, createdTimestamp, modifiedTimestamp};
	}
	
	/**
	 * Gets the necessary private data of this entry in the form of an Object[].
	 * 
	 * @return An Object[] of title, username, password, notes, and id.
	 */
	public Object[] toMinimalObjectArray() {
		return new Object[] {title, username, password, notes, id};
	}

	/**
	 * Compare two table entries for their relational value.
	 * 
	 * @param tableEntry The TableEntry to compare to.
	 * 
	 * @return -1, 0, 1 for <, ==, > respectively. 
	 */
	public int compareTo(TableEntry tableEntry) {
		int value = 0;
		
		// Compare the titles
		value = this.title.toLowerCase().compareTo(tableEntry.title.toLowerCase());
		if(value != 0)
			return value;
		
		// Compare the usernames
		value = this.username.toLowerCase().compareTo(tableEntry.username.toLowerCase());
		if(value != 0)
			return value;
		
		return 0;
	}
	
	//******************//
	//* Helper Methods *//
	//******************//
	private String generateID() {
		return Password.generate(20);
	}
}
