package utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;


public class Files {
	/**
	 * Creates a new file with the given path.
	 * 
	 * @param path The path of the file.
	 * 
	 * @return The File if it was created, or null if it already exists or an error occured.
	 */
	public static File create(String path) {
		File createdFile = null;

		try {
			createdFile = new File(path);
			if (createdFile.createNewFile())
				;
			else
				createdFile = null;	
		}
		catch (IOException e) {
			createdFile = null;
		}

		return createdFile;
	}

	/**
	 * Opens a file at the given path.
	 * 
	 * @param path The path of the file.
	 * 
	 * @return The opened File, or null if the file does not exist.
	 */
	public static File open(String path) {
		File file = null;

		file = new File(path);

		// Return the opened file, or null if it doesn't exist
		return (file.exists())? file : null;
	}

	/**
	 * Writes one line to the file at the given path.
	 * 
	 * @param filePath The path of the file.
	 * @param toWrite  What to write in the file.
	 * @param saveOld  If the old contents of the file should be saved.
	 */
	public static void write(String filePath, String toWrite, boolean saveOld) {
		FileWriter writer = null;
		Scanner fileIn = null;
		String copy = "";

		try {
			// Copy any existing data
			if(saveOld) {
				fileIn = new Scanner(new File(filePath));
				while (fileIn.hasNext())
					copy += fileIn.nextLine() + "\n";
				fileIn.close();
			}

			// Write to file
			writer = new FileWriter(filePath);
			if(saveOld)
				writer.write(copy);
			writer.append(toWrite);
			writer.close();
		} catch (Exception e) {
			System.err.println("Could not write to file.");
			e.printStackTrace();
		}
	}

	/**
	 * Writes several lines to a file at the given path.
	 * 
	 * @param filePath The path of the file.
	 * @param toWrite  What to write in the file.
	 * @param saveOld  If the old contents of the file should be saved.
	 */
	public static void write(String filePath, String[] toWrite, boolean saveOld) {
		FileWriter writer = null;
		Scanner fileIn = null;
		String copy = "";

		try {
			// Copy any existing data
			if(saveOld) {
				fileIn = new Scanner(new File(filePath));
				while (fileIn.hasNext())
					copy += fileIn.nextLine() + "\n";
				fileIn.close();
			}

			// Write to file
			writer = new FileWriter(filePath);
			if(saveOld)
				writer.write(copy);
			for(int i = 0; i < toWrite.length; i++)
				writer.append(toWrite[i]+ "\n");
			writer.close();
		} catch (Exception e) {
			System.err.println("Could not write to file.");
			e.printStackTrace();
		}
	}

	/**
	 * Reads all lines from a file at the given path.
	 * 
	 * @param filePath The path of the file.
	 * 
	 * @return A String[] of all contents.
	 */
	public static String[] read(String filePath) {
		LinkedList<String> toReturn = new LinkedList<String>();
		Scanner fileIn = null;

		try {
			fileIn = new Scanner(new File(filePath));
			while (fileIn.hasNextLine())
				toReturn.add(fileIn.nextLine());
		} catch (Exception e) {
			System.err.println("Could not read from file.");
			e.printStackTrace();
		}

		return toReturn.toArray(new String[toReturn.size()]);
	}

	/**
	 * Reads all lines from a file that are not on the excluded lines.
	 * 
	 * @param filePath The path of the file.
	 * @param excludeLines The lines to skip.
	 * 
	 * @return A String[] of all contents that were not skipped.
	 */
	public static String[] read(String filePath, LinkedList<Integer> excludeLines) {
		LinkedList<String> toReturn = new LinkedList<String>();
		Scanner fileIn = null;
		int currLine = 0;

		try {
			fileIn = new Scanner(new File(filePath));
			while (fileIn.hasNextLine()) {
				if(!excludeLines.contains(currLine))
					toReturn.add(fileIn.nextLine());
				else
					fileIn.nextLine();
				currLine++;
			}
		} catch (Exception e) {
			System.err.println("Could not read from file.");
			e.printStackTrace();
		}

		return toReturn.toArray(new String[toReturn.size()]);
	}

	/**
	 * Deletes a file at the given path.
	 * 
	 * @param filePath The path to the file.
	 * 
	 * @return The success of deletion. If file does not exist, true is returned.
	 */
	public static boolean delete(String filePath) {
		File toDelete = open(filePath);
		
		// If the file does not exist, return true
		if(toDelete == null)
			return true;
		
		// Return the attempt to delete the file
		return toDelete.delete();
	}
}
