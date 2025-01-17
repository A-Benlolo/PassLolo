package crypto;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Scanner;

public class Password {
	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
	private static final String NUMERIC_SYMBOLS = "!@#$%^&*()";
	private static final String EXTRA_SYMBOLS = "`~-_=+[{]}\\|\"';:/?.>,<";

	/**
	 * Generate a password with numbers, letters, and symbols of specified length.
	 * 
	 * @param length The length of the password.
	 * 
	 * @return The generated password.
	 */
	public static String generate(int length) {
		String password = "";
		SecureRandom rand = new SecureRandom();
		int option = 0;

		for (int i = 0; i < length; i++) {
			// Choose a random option
			option = rand.nextInt(3);

			// Append the corresponding character
			switch (option) {
			case 0: // Number
				password += (rand.nextInt(10));
				break;
			case 1: // Letter
				// Lowercase
				if(rand.nextInt(2) == 0)
					password += (ALPHABET.toLowerCase().charAt(rand.nextInt(ALPHABET.length())));
				// Uppercase
				else
					password += (ALPHABET.toUpperCase().charAt(rand.nextInt(ALPHABET.length())));
				break;
			case 2: // Special
				password += (NUMERIC_SYMBOLS.charAt(rand.nextInt(NUMERIC_SYMBOLS.length())));
			}
		}

		return password;
	}
	
	/**
	 * Generate a password of specified criteria.
	 * 
	 * @param length The length of the password.
	 * @param lower If lowercase is wanted (a, b, c, ...).
	 * @param upper If uppercase is wanted (A, B, C, ...).
	 * @param digits If digits are wanted (1, 2, 3, ...).
	 * @param symbols If numeric symbols are wanted (!, @, #, ...).
	 * @param extraSymbols If extra symbols are wanted (~, +, ?, ...).
	 * 
	 * @return The generated password.
	 */
	public static String generate(int length, boolean lower, boolean upper, boolean digits, boolean symbols, boolean extraSymbols) {
		String password = "";
		SecureRandom rand = new SecureRandom();
		int option = 0;
		boolean isGoodOption;
		String symbolSet = "";
		
		// Determine the symbol set to use
		if(symbols && extraSymbols)
			symbolSet = NUMERIC_SYMBOLS+EXTRA_SYMBOLS;
		else if(symbols)
			symbolSet = NUMERIC_SYMBOLS;
		else if(extraSymbols)
			symbolSet = EXTRA_SYMBOLS;
		
		for (int i = 0; i < length; i++) {
			do {
				// Choose a random option
				option = rand.nextInt(3);
				isGoodOption = true;
				
				// Make sure the option fits requirements
				if(!digits && option == 0)
					isGoodOption = false;
				else if(!(lower || upper) && option == 1)
					isGoodOption = false;
				else if(!(symbols || extraSymbols) && option == 2)
					isGoodOption = false;
			} while(!isGoodOption);

			// Append the corresponding character
			switch (option) {
			case 0: // Number
				password += (rand.nextInt(10));
				break;
			case 1: // Letter
				// Lowercase or Uppercase
				if(lower && upper) {
					if(rand.nextInt(2) == 0)
						password += (ALPHABET.toLowerCase().charAt(rand.nextInt(ALPHABET.length())));
					else
						password += (ALPHABET.toUpperCase().charAt(rand.nextInt(ALPHABET.length())));
				}
				else if(lower)
					password += (ALPHABET.toLowerCase().charAt(rand.nextInt(ALPHABET.length())));
				else
					password += (ALPHABET.toUpperCase().charAt(rand.nextInt(ALPHABET.length())));
				break;
			case 2: // Special
				password += (symbolSet.charAt(rand.nextInt(symbolSet.length())));
				break;
			}
		}

		return password;
	}

	/**
	 * Determine the strength of the password.
	 * 
	 * @param password The password in question.
	 * @return A numeric percentage, 0 - 100.
	 */
	public static int strengthOf(String password) {
		int score = 0;
		int upperLetterCounter = 0;
		int lowerLetterCounter = 0;
		int numberCounter = 0;
		int symbolCounter = 0;
		int middleCounter = 0;
		int consecutiveCounter = 0;
		int sequenceCounter = 0, currSequence = 0;
		char currChar;
		boolean onlyLetters = true, onlyNumbers = true;
		boolean consUpper = false, consLower = false, consNumber = false;
		boolean hasUpper = false, hasLower = false, hasDigit = false, hasSymbol = false;
		
		// Number of characters
		score += (password.length()*4);
		
		// If the string is empty, return 0
		if(password.isEmpty())
			return 0;

		// Check the first character
		if(Character.isUpperCase(password.charAt(0))) {
			onlyNumbers = false;
			upperLetterCounter++;
		}
		else if(Character.isLowerCase(password.charAt(0))) {
			onlyNumbers = false;
			lowerLetterCounter++;
		}
		else if(Character.isDigit(password.charAt(0))) {
			onlyLetters = false;
			numberCounter++;
		}
		else {
			onlyLetters = false;
			onlyNumbers = false;
			symbolCounter++;
		}

		// Check the middle characters, if length>2
		if(password.length() > 2) {
			for(int i = 1; i< password.length()-1; i++) {
				if(Character.isUpperCase(password.charAt(i))) {
					onlyNumbers = false;
					upperLetterCounter++;
				}
				else if(Character.isLowerCase(password.charAt(i))) {
					onlyNumbers = false;
					lowerLetterCounter++;
				}
				else if(Character.isDigit(password.charAt(i))) {
					onlyLetters = false;
					numberCounter++;
					middleCounter++;
				}
				else {
					onlyLetters = false;
					onlyNumbers = false;
					symbolCounter++;
					middleCounter++;
				}
			}
		}

		// Check the last character
		if(Character.isUpperCase(password.charAt(password.length()-1))) {
			onlyNumbers = false;
			upperLetterCounter++;
		}
		else if(Character.isLowerCase(password.charAt(password.length()-1))) {
			onlyNumbers = false;
			lowerLetterCounter++;
		}
		else if(Character.isDigit(password.charAt(password.length()-1))) {
			onlyLetters = false;
			numberCounter++;
		}
		else {
			onlyLetters = false;
			onlyNumbers = false;
			symbolCounter++;
		}
		
		// Check for consecutive patterns
		for(int i = 0; i < password.length(); i++) {
			if(Character.isLowerCase(password.charAt(i))) {
				if(consLower)
					consecutiveCounter++;
				consLower = true;
				consUpper = false;
				consNumber = false;
			}
			else if(Character.isUpperCase(password.charAt(i))) {
				if(consUpper)
					consecutiveCounter++;
				consLower = false;
				consUpper = true;
				consNumber = false;
			}
			else if(Character.isDigit(password.charAt(i))) {
				if(consNumber)
					consecutiveCounter++;
				consLower = false;
				consUpper = false;
				consNumber = true;
			}
			else {
				consLower = false;
				consUpper = false;
				consNumber = false;
			}
		}
		
		// Check for sequences
		for(int i = 0; i < password.length(); i++) {
			currChar = password.charAt(i);
			for(int j = i+1; j < password.length(); j++) {
				if(isSequential(currChar, password.charAt(j))) {
					currChar = password.charAt(j);
					currSequence++;
				}
				else
					break;
			}
			if(currSequence > 2)
				sequenceCounter += currSequence;
			currSequence = 0;
		}
		
		// Check if password contains one of every character types
		String curr = "";
		for(int i = 0; i < password.length(); i++) {
			curr += password.charAt(i);
			if(ALPHABET.toUpperCase().contains(curr))
				hasUpper = true;
			else if(ALPHABET.toLowerCase().contains(curr))
				hasLower = true;
			else if(NUMERIC_SYMBOLS.contains(curr) || EXTRA_SYMBOLS.contains(curr))
				hasSymbol = true;
			else
				hasDigit = true;
			curr = "";
		}

		// Apply additions
		if(lowerLetterCounter != 0)
			score += ((password.length()-upperLetterCounter)*2);
		if(upperLetterCounter != 0)
			score += ((password.length()-lowerLetterCounter)*2);
		if(!onlyNumbers)
			score += (numberCounter*4);
		score += (symbolCounter*6);
		score += (middleCounter*2);
		
		// Apply deductions
		if(onlyLetters || onlyNumbers)
			score -= password.length();
		score -= (consecutiveCounter*2);
		score -= (sequenceCounter*3);
		if(!hasUpper)
			score -= 10;
		if(!hasLower)
			score -= 10;
		if(!hasDigit)
			score -= 10;
		if(!hasSymbol)
			score -=10;
		
		// Put score in range 0-100
		score = (score > 100)? 100 : score;
		score = (score < 0)? 0 : score;
		
		return score;
	}

	/**
	 * Determine if a password is in a dictionary of known passwords.
	 * 
	 * @param password The password in question.
	 * 
	 * @return The closest match found in the dictionary, or null if nothing found.
	 */
	public static String checkDictionary(String password) {
		String contained = null;
		// Determine if the password contains anything from the dictionary
		try {
			Scanner dictionaryScanner = new Scanner(new File("..\\resources\\passwordDictionary.txt"));
			String search;
			while(dictionaryScanner.hasNext()) {
				search = dictionaryScanner.next();
				if(contained == null && (password.toLowerCase().contains(search.toLowerCase()) || search.toLowerCase().contains(password.toLowerCase())) && (Math.abs(search.length()-password.length()) <= 5))
					contained=search;
				else if(password.toLowerCase().equals(search.toLowerCase()))
					return search;
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		return contained;
	}
	
	/**
	 * Determine if two characters are sequential.
	 * 
	 * @param a The first character.
	 * @param b The second character.
	 * @return A boolean value.
	 */
	private static boolean isSequential(char a, char b) {
		if(Character.isDigit(a) && Character.isDigit(b))
			return Character.getNumericValue(a)+1 == Character.getNumericValue(b);
			
		else if (Character.isAlphabetic(a) && Character.isAlphabetic(b))
			return Character.toUpperCase(a)+1 == Character.toUpperCase(b);

		return false;
	}
}
