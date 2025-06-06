package org.xdef.sys;

/** Describes interface of source parsers.
 * @author Vaclav Trojan
 */
public interface SParser {

	/** This value is used as the special "end of source"
	 * or "no character" value. */
	public static final char NOCHAR = 0;

	/** Get end index of source buffer.
	 * @return end index of source buffer.
	 */
	public int endPos();

	/** Get the parsed string.
	 * @return parsed string.
	 */
	public String getSourceBuffer();

	/** Set parsed string.
	 * @param source parsed source string or null.
	 */
	public void setSourceBuffer(String source);

	/** Get value of parsed item as string.
	 * @return value of parsed item as string.
	 */
	public String getParsedString();

	/** Get part of the unparsed part of source and set EOS..
	 * @return unparsed part of source or empty string if EOS reached.
	 */
	public String getUnparsedBufferPart();

	/** Get parsed string from position.
	 * @param pos position from which parsed string will be extracted.
	 * @return parsed string from given position.
	 */
	public String getParsedBufferPartFrom(int pos);

	/** Get parsed string from positions.
	 * @param from start position from which parsed string will be extracted.
	 * @param to end position from which parsed string will be extracted.
	 * @return parsed string from given positions.
	 */
	public String getBufferPart(int from, int to);

	/** Get character on current position.
	 * @return character on current position or zero.
	 */
	public char getCurrentChar();

	/** Get character for the next current position.
	 * @return character for the next current position or zero.
	 */
	public char nextChar();

	/** Get buffer index of parsed object.
	 * @return current buffer index of parsed object.
	 */
	public int getIndex();

	/** Set current buffer position.
	 * @param pos source position of parsed object.
	 */
	public void setIndex(int pos);

	/** Check if the position is at the end of source.
	 * @return true if and only if the position of parsed object is at the end of source.
	 */
	public boolean eos();

	/** Set the source position to the end of source. */
	public void setEos();

	/** Parse white space.
	 * @return true if white space was present at actual position, otherwise return false.
	 */
	public boolean isSpace();

	/** Skip all white spaces an if at least one was found return true.
	 * @return true if and only if at least one white space was found.
	 */
	public boolean isSpaces();

	/** Check if character from argument is at the current source position and update current source position.
	 * @param ch required character.
	 * @return true if and only if the character is at current position.
	 */
	public boolean isChar(char ch);

	/** If character on actual position is not character specified by argument return the actual character and
	 * set position to next character. Otherwise return NOCHAR and actual position remains unchanged.
	 * @param ch Character to be checked.
	 * @return character on actual position or NOCHAR.
	 */
	public char notChar(char ch);

	/** Check if actual position points to a character in given interval. Set the actual position to the next
	 * character if given character was in the specified interval.
	 * @param minCh minimum of checked interval.
	 * @param maxCh maximum of checked interval.
	 * @return the actual character character from actual position, otherwise return <code>NOCHAR</code>.
	 */
	public char isInInterval(char minCh, char maxCh);

	/** Check if actual position points to a character out of given interval. Set the actual position to the
	 * next character if given character was recognized.
	 * @param minCh minimum of checked interval.
	 * @param maxCh maximum of checked interval.
	 * @return the actual character character from actual position, otherwise return <code>NOCHAR</code>.
	 */
	public char notInInterval(char minCh, char maxCh);

	/** If actual character is one of characters specified in given string return this character and set
	 * position to the next character. Otherwise return NOCHAR.
	 * @param chars String with characters to be checked.
	 * @return the actual character or NOCHAR.
	 */
	public char isOneOfChars(String chars);

	/** Check if at the current position is a digit.
	 * @return digital value of digit or -1;
	 */
	public int isDigit();

	/** Parse unsigned integer number.
	 * @return true if and only if an integer number was recognized.
	 */
	public boolean isInteger();

	/** Check if actual position points to signed integer number. Set the actual position to the next
	 * character after the number if number was recognized.
	 * @return true if signed integer was parsed, otherwise return false.
	 */
	public boolean isSignedInteger();

	/** Parse unsigned float number.
	 * @return true if and only if an float number was recognized.
	 */
	public boolean isFloat();

	/** Parse signed float number. Set actual position to the next character after the number if number was
	 * recognized.
	 * @return true if and only if float number was parsed.
	 */
	public boolean isSignedFloat();

	/** Check if at the current source position is letter.
	 * @return letter or 0;
	 */
	public char isLetter();

	/** Check if at the current source position is letter or digit.
	 * @return letter or digit or 0;
	 */
	public char isLetterOrDigit();

	/** Check if at the current position is Emoji character.
	 * @return digital value of digit or -1;
	 */
	public char isEmoji();

	/** Get current character from source and set position to the next
	 * character. If the current position is at the end of source return zero.
	 * @return current character or zero.
	 */
	public char peekChar();

	/** Check if actual position points to upper case letter. Set the source position to the next character
	 * if letter was recognized and return the character otherwise return NOCHAR don't change position.
	 * @return character or NOCHAR.
	 */
	public char isUpperCaseLetter();

	/** Check if actual position points to lower case letter. Set the source position to the next character
	 * if letter was recognized and return the character otherwise return NOCHAR and don't change position.
	 * @return character or NOCHAR.
	 */
	public char isLowerCaseLetter();

	/** Check if actual position points to given token (case insensitive). Set the actual position to the next
	 * character after the token if given token was recognized.
	 * @param token The token to be checked case insensitive.
	 * @return true if token was present at actual position.
	 */
	public boolean isTokenIgnoreCase(String token);

	/** Check if a token from argument is at the current source position and update actual source position.
	 * @param s required token.
	 * @return true if the token is at current position.
	 */
	public boolean isToken(String s);

	/** If on source position is one of tokens specified in the argument the method returns index to this item
	 * and set position to the next position after a token. Otherwise it return -1.
	 * @param tokens Array of tokens be checked.
	 * @return Index of found token or -1.
	 */
	public int isOneOfTokens(final String... tokens);

	/** If on source position is one of tokens (ignored case) specified in the argument the method return
	 * index to this item and set position to the next position after a token. Otherwise return -1.
	 * @param tokens Array of tokens be checked.
	 * @return Index of found token or -1.
	 */
	public int isOneOfTokensIgnoreCase(final String... tokens);

	/** Find token. If the token was found returns true and sets position to the position of token. Otherwise
	 * return false and set position to the end of source.
	 * @param token Token to be found.
	 * @return true if the token was found.
	 */
	public boolean findToken(final String token);

	/** Skip to specified character. The position is set to the found character. Return true and set position
	 * to the position of character. Otherwise return false and set position to the end of source.
	 * @param ch Character to be searched for.
	 * @return true if the character was found.
	 */
	public boolean findChar(char ch);

	/** Skip to first occurrence of one of specified character set. The position is set to the found
	 * character.Return a character from the list of characters if the character was found, otherwise
	 * return NOCHAR and set the position to the end of source.
	 * @param chars String with set of characters.
	 * @return found character or NOCHAR.
	 */
	public char findOneOfChars(final String chars);

	/** Check if an error was reported (result is same as !matches()).
	 * @return true if and only if data were parsed with an error.
	 */
	public boolean errors();

	/** Add error.
	 * @param id identifier of error (may be null).
	 */
	public void error(String id);

	/** Add error.
	 * @param id identifier of error (may be null).
	 * @param msg text of error.
	 * @param mod Message modification parameters.
	 */
	public void error(String id, String msg, Object... mod);

	/** Add error.
	 * @param registeredID id of registered report.
	 * @param mod Message modification parameters.
	 */
	public void error(long registeredID, Object... mod);

	/** Put report at position.
	 * @param report The report.
	 */
	public void putReport(final Report report);
}