package utilities;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Base64;
import java.util.LinkedList;

import crypto.AES;
import loloTable.TableEntry;

public class PrintableTable implements Printable {
	private String title;
	private TableEntry[] entries;
	private byte[] hashedKey;
	
	public PrintableTable(String title, TableEntry[] entries, byte[] hashedKey) {
		this.title = title;
		this.entries = new TableEntry[entries.length];
		for(int i = 0; i < entries.length; i++)
			this.entries[i] = new TableEntry(entries[i]);
		this.hashedKey = new byte[hashedKey.length];
		for(int i = 0; i < hashedKey.length; i++)
			this.hashedKey[i] = hashedKey[i];
	}
	
	/**
	 * Print the table.
	 * 
	 * @param graphics The graphics.
	 * @param pageFormat The format of the page.
	 * @param pageIndex The index of the page.
	 */
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        // Update the coordinates to be in the correct spot
        Graphics2D g2d = (Graphics2D)graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        // Get the original font of the font
        Font font = new Font("Courier New", Font.PLAIN, 12);
        graphics.setFont(font);
        //Font font = graphics.getFont();
        
        // Get the usable height of the page
		double pageHeight = pageFormat.getImageableHeight();
		
		// Get the heights of the title, header, and dynamic rows
		double[] heights = getRowHeights(font, 85);
		
		// Initialize rowsPerPage and howManyBreaks
		LinkedList<Integer> rowsPerPage = new LinkedList<Integer>();
		int howManyBreaks = 0;
		rowsPerPage.add(0);
		
		// Initialize the height of the Graphic on the page
		double currHeight = 0;
		
		// Determine the rowsPerPage and howManyBreaks
		for(int i = 0; i < heights.length; i++) {
			// If the next row will not fit, add a page break and add the row to the next page
			if(currHeight + heights[i] > pageHeight) {
				howManyBreaks++;
				currHeight = heights[i];
				rowsPerPage.add(1);
			}
			// Else just add the row
			else {
				currHeight += heights[i];
				rowsPerPage.add(rowsPerPage.removeLast()+1);
			}
		}
		
		// Determine the location of the page breaks
		int[] pageBreaks = new int[howManyBreaks];
		for(int i = 0; i < howManyBreaks; i++) {
			pageBreaks[i] = 0;
			for(int j = i; j < (i+rowsPerPage.get(i)); j++) {
				pageBreaks[i] += heights[j];
			}
		}
		
		// If the page index is out of bounds, stop
		if (pageIndex > pageBreaks.length)
		      return NO_SUCH_PAGE;
			
		// Draw the title and header on the first page
		int startingY = 5;
		if(pageIndex == 0) {
			// Update the startingY
			startingY = 85;
			
			// Save the default font
			Font defaultFont = graphics.getFont();

			// Draw the title
			String fullTitle = "PassLolo Vault: "+title;
			Font titleFont = defaultFont.deriveFont(32f);
			Rectangle rect = new Rectangle((int)pageFormat.getImageableX(), (int)pageFormat.getImageableY(), (int)pageFormat.getImageableWidth(), (int)pageFormat.getImageableHeight());

			// Get the FontMetrics
			FontMetrics metrics2 = graphics.getFontMetrics(titleFont);

			// Determine the X coordinate for the text
			int x = rect.x + (rect.width - metrics2.stringWidth(fullTitle)) / 2;

			// Set the font
			graphics.setFont(titleFont);

			// Draw the the title
			graphics.drawString(fullTitle, x, 50);

			drawHeader(graphics, defaultFont.deriveFont(Font.BOLD), 15, 50+10, "Title:", "Username:", "Password:", "Notes:");
		}
				
		// Determine the starting index
		int start = 0;
		for(int i = pageIndex - 1; i >= 0; i--)
			start += rowsPerPage.get(i);
		
		// Determine the ending index
		int end = rowsPerPage.get(pageIndex)+start;
		
		// Print the table
		for(int i = start ; i < end; i++)
			startingY += drawRow(graphics, font, 15, startingY, entries[i]);
		
        // The page was a success
        return PAGE_EXISTS;
	}
	
	/**
	 * Draw the header for a loloTable.
	 * 
	 * @param graphics The graphics.
	 * @param font The font.
	 * @param startX The X value.
	 * @param startY The Y value.
	 * @param headers The header text.
	 */
	private void drawHeader(Graphics graphics, Font font, int startX, int startY, String... headers) {
		graphics.setFont(font);
		
		// Draw the border
		graphics.drawRect(startX, startY, 100, 25);
		graphics.drawRect(startX+100, startY, 100, 25);
		graphics.drawRect(startX+100+100, startY, 150, 25);
		graphics.drawRect(startX+100+100+150, startY, 230, 25);
		
		graphics.drawString(headers[0], 3+startX, startY+(25/2));
		graphics.drawString(headers[1], 3+startX+100, startY+(25/2));
		graphics.drawString(headers[2], 3+startX+100+100, startY+(25/2));
		graphics.drawString(headers[3], 3+startX+100+100+150, startY+(25/2));
	}
	
	/**
	 * Draws a row using a loloTable entry.
	 * 
	 * @param graphics The graphics.
	 * @param font The font.
	 * @param startX The x value. 
	 * @param startY The y value.
	 * @param entry The entry with information.
	 * 
	 * @return The height of the row.
	 */
	private int drawRow(Graphics graphics, Font font, int startX, int startY, TableEntry entry) {
		// Decrypt
		byte[] decryptedBytes = new byte[0];
		String decrypted = "";
		try {
			decryptedBytes = AES.decrypt(Base64.getDecoder().decode(entry.getPassword(false)), hashedKey);
			decrypted = new String(decryptedBytes);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		
		// Draw the contents, and record the rectangles they occupy
		Rectangle[] rectangles = new Rectangle[4];
		rectangles[0] = TextRenderer.drawString(
				graphics, entry.getTitle(), font, Color.BLACK, 
				new Rectangle(3+startX, startY, 100, 25), TextAlignment.TOP_LEFT, TextFormat.FIRST_LINE_VISIBLE);
		
		rectangles[1] = TextRenderer.drawString(
				graphics, entry.getUsername(), font, Color.BLACK,
				new Rectangle(3+startX+100, startY, 100, 25), TextAlignment.TOP_LEFT, TextFormat.FIRST_LINE_VISIBLE);
		
		rectangles[2] = TextRenderer.drawString(
				graphics, decrypted, font, Color.BLACK,
				new Rectangle(3+startX+100+100, startY, 150, 25), TextAlignment.TOP_LEFT, TextFormat.FIRST_LINE_VISIBLE);
		
		rectangles[3] = TextRenderer.drawString(
				graphics, entry.getNotes(), font, Color.BLACK,
				new Rectangle(3+startX+100+100+150, startY, 230, 25), TextAlignment.TOP_LEFT, TextFormat.FIRST_LINE_VISIBLE);
		
		// Find the maximum height
		double maxHeight = rectangles[0].getHeight();
		for(int i = 0; i < rectangles.length; i++)
			if(rectangles[i].getHeight() > maxHeight)
				maxHeight = rectangles[i].getHeight();
		maxHeight = maxHeight+6;
		
		// Draw the rectangles at max height
		graphics.drawRect(startX, startY, 100, (int)maxHeight);
		graphics.drawRect(startX+100, startY, 100, (int)maxHeight);
		graphics.drawRect(startX+100+100, startY, 150, (int)maxHeight);
		graphics.drawRect(startX+100+100+150, startY, 230, (int)maxHeight);
		
		// Wipe the sensitive data
		decrypted = null;
		for(int i = 0; i < decryptedBytes.length; i++)
			decryptedBytes[i] = '\0';
		decryptedBytes = null;
		
		return (int)maxHeight;
	}
	
	/**
	 * Determine the height of each row of the table that will be created.
	 * 
	 * @param font The font used in the table.
	 * @param topBuffer The buffer above the first row.
	 * 
	 * @return A double[] of heights.
	 */
	private double[] getRowHeights(Font font, int topBuffer) {
		// Get the height of everything on the page
		double[] heights = new double[entries.length];
		double[] workingHeights = new double[4];
		
		// The height of the cells
		for(int i = 0; i < entries.length; i++) {
			// Decrypt
			byte[] decryptedBytes = new byte[0];
			String decrypted = "";
			try {
				decryptedBytes = AES.decrypt(Base64.getDecoder().decode(entries[i].getPassword(false)), hashedKey);
				decrypted = new String(decryptedBytes);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

			// Get all heights
			workingHeights[0] = TextRenderer.getRectangeFor(
					entries[i].getTitle(), font, Color.BLACK, 
					new Rectangle(0, 0, 100, 25), TextAlignment.TOP_LEFT, TextFormat.FIRST_LINE_VISIBLE).getHeight();
			workingHeights[1] = TextRenderer.getRectangeFor(
					entries[i].getUsername(), font, Color.BLACK, 
					new Rectangle(0, 0, 100, 25), TextAlignment.TOP_LEFT, TextFormat.FIRST_LINE_VISIBLE).getHeight();
			workingHeights[2] = TextRenderer.getRectangeFor(
					decrypted, font, Color.BLACK, 
					new Rectangle(0, 0, 150, 25), TextAlignment.TOP_LEFT, TextFormat.FIRST_LINE_VISIBLE).getHeight();
			workingHeights[3] = TextRenderer.getRectangeFor(
					entries[i].getNotes(), font, Color.BLACK, 
					new Rectangle(0, 0, 230, 25), TextAlignment.TOP_LEFT, TextFormat.FIRST_LINE_VISIBLE).getHeight();
			
			// Get max height
			heights[i] = workingHeights[0];
			for(int j = 1; j < workingHeights.length; j++)
				if(workingHeights[j] > heights[i])
					heights[i] = workingHeights[j];
						
			// Correct the height
			heights[i] += 6;
		}
		
		// Apply the top buffer
		heights[0] += topBuffer;
		
		return heights;
	}
}
