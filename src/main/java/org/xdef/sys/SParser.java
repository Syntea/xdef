package org.xdef.sys;

/** Describes interface of source parsers.
 * @author Vaclav Trojan
 */
public interface SParser {

	/** This value is used as the special "end of source"
	 * or "no character" value. */
	public static final char NOCHAR = 0;

	/** Get the parsed string.
	 * @return parsed string.
	 */
	public String getSourceBuffer();

	/** Set parsed string.
	 * @param source parsed source string or <tt>null</tt>.
	 */
	public void setSourceBuffer(String source);

	/** Get value of parsed item as string.
	 * @return value of parsed item as string.
	 */
	public String getParsedString();

	/** Get part of the unparsed part of source and set EOS..
	 * @return unparsed part of source or <tt>null</tt> if EOS reached.
	 */
	public String getUnparsedBufferPart();

	/** Get parsed string from position.
	 * @param pos position from which parsed string will be extracted.
	 * @return parsed string from given position.
	 */
	public String getParsedBufferPartFrom(int pos);

	/** Get parsed string from positions.
	 * @param from start position from which parsed string will be
	 *  extracted.
	 * @param to end position from which parsed string will be extracted.
	 * @return parsed string from given positions.
	 */
	public String getBufferPart(int from, int to);

	/** Get character on current position.
	 * @return character on current position or <tt>zero</tt>.
	 */
	public char getCurrentChar();

	/** Get character for the next current position.
	 * @return character for the next current position or <tt>zero</tt>.
	 */
	public char nextChar();

	/** Get buffer index of parsed object.
	 * @return current buffer index of parsed object.
	 */
	public int getIndex();

	/** Set current buffer position.
	 * @param pos source position of parsed object.
	 */
	public void setBufIndex(int pos);

	/** Check if the position is at the end of source.
	 * @return <tt>true</tt> if and only if the position of parsed object
	 * is at the end of source.
	 */
	public boolean eos();

	/** Set the source position to the end of source. */
	public void setEos();

	/** Parse white space.
	 * @return <tt>true</tt> if white space was present at actual position,
	 * otherwise return <tt>false</tt>.
	 */
	public boolean isSpace();

	/** Skip all white spaces an if at least one was found return
	 * <tt>true</tt>.
	 * @return <tt>true</tt> if and only if at least one white space was
	 * found.
	 */
	public boolean isSpaces();

	/** Check if the character from argument is at the current source
	 * position and update current source position.
	 * @param ch required character.
	 * @return <tt>true</tt> if and only if the character is at current
	 * position.
	 */
	public boolean isChar(char ch);

	/** If character on actual position is not character specified by
	 * argument the method returns the actual character and sets
	 * position to next character. Otherwise it returns NOCHAR and actual
	 * position remains unchanged.
	 * @param ch Character to be checked.
	 * @return character on actual position or NOCHAR.
	 */
	public char notChar(char ch);

	/** Check if actual position points to a character in given interval.
	 * Set the actual position to the next character if given character
	 * was in the specified interval.
	 * @param minCh minimum of checked interval.
	 * @param maxCh maximum of checked interval.
	 * @return the actual character character from actual position,
	 * otherwise return <tt>NOCHAR</tt>.
	 */
	public char isInInterval(char minCh, char maxCh);

	/** Check if actual position points to a character out of given interval.
	 * Set the actual position to the next character if given character was
	 * recognized.
	 * @param minCh minimum of checked interval.
	 * @param maxCh maximum of checked interval.
	 * @return the actual character character from actual position,
	 * otherwise return <tt>NOCHAR</tt>.
	 */
	public char notInInterval(char minCh, char maxCh);

	/** If actual character is one of characters specified in given string the
	 * method returns this character and sets position to the next
	 * character. Otherwise it returns NOCHAR.
	 * @param chars String with characters to be checked.
	 * @return the actual character or NOCHAR.
	 */
	public char isOneOfChars(String chars);

	/** Check if at the current source position is digit.
	 * @return digital value of digit or -1;
	 */
	public int isDigit();

	/** Parse unsigned integer number.
	 * @return <tt>true</tt> if and only if an integer number was
	 * recognized.
	 */
	public boolean isInteger();

	/** Check if actual position points to signed integer number. Set the
	 * actual position to the next character after the number if number
	 * was recognized.
	 * @return <tt>true</tt> if signed integer was parsed, otherwise
	 * return <tt>false</tt>.
	 */
	public boolean isSignedInteger();

	/** Parse unsigned float number.
	 * @return <i>true</i> if and only if an float number was recognized.
	 */
	public boolean isFloat();

	/** Parse signed float number. Set actual position to the next character
	 * after the number if number was recognized.
	 * @return <i>true</i> if and only if float number was parsed.
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

	/** Get current character from source and set position to the next
	 * character. If the current position is at the end of source return
	 * zero.
	 * @return current character or zero.
	 */
	public char peekChar();

	/** Check if actual position points to upper case letter. Set the
	 * source position to the next character if letter was recognized and
	 * return the character otherwise return NOCHAR and source position
	 * remains unchanged.
	 * @return character or NOCHAR.
	 */
	public char isUpperCaseLetter();

	/** Check if actual position points to lower case letter. Set the source
	 * position to the next character if letter was recognized and return
	 * the character otherwise return NOCHAR and source position remains
	 * unchanged.
	 * @return character or NOCHAR.
	 */
	public char isLowerCaseLetter();

	/** Check if actual position points to given token (case insensitive).
	 * Set the actual position to the next character after the token if
	 * given token was recognized.
	 * @param token The token to be checked case insensitive.
	 * @return <i>true</i> if token was present at actual position.
	 */
	public boolean isTokenIgnoreCase(String token);

	/** Check if a token from argument is at the current source position and
	 * update actual source position.
	 * @param s required token.
	 * @return <tt>true</tt> if the token is at current position.
	 */
	public boolean isToken(String s);

	/** If on source position is one of tokens specified in the argument the
	 * method returns index to this item and sets position to the next
	 * position after a token. Otherwise it returns -1.
	 * @param tokens Array of tokens be checked.
	 * @return Index of found token or -1.
	 */
	public int isOneOfTokens(final String... tokens);

	/** Find token. If the token was found returns <tt>true</tt> and sets
	 * position <b>to</b> the position of token. Otherwise returns
	 * <tt>false</tt> and sets position to the end of source.
	 * @param token Token to be found.
	 * @return true if the token was found.
	 */
	public boolean findToken(final String token);

	/** Skip to specified character. The position is set <b>to</b> the found
	 * character. Returns <tt>true</tt> and sets position <b>to</b>
	 * the position of character. Otherwise returns <tt>false</tt> and sets
	 * position to the end of source.
	 * @param ch Character to be searched for.
	 * @return <i>true</i> if the character was found.
	 */
	public boolean findChar(char ch);

	/** Skip to first occurrence of one of specified character set.
	 * The position is set <b>to</b> the found character.Return a character
	 * from the list of characters if the character was found, otherwise
	 * return NOCHAR and set the position to the end of source.
	 * @param chars String with set of characters.
	 * @return found character or NOCHAR.
	 */
	public char findOneOfChars(final String chars);

	/** Check if an error was reported (result is same as !matches()).
	 * @return <tt>true</tt> if and only if data were parsed with an error.
	 */
	public boolean errors();

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
//
//	/** Parse ISO8601 date or date and time format (see
//	 * <a href = "http://www.w3.org/TR/NOTE-datetime">
//	 * www.w3.org/TR/NOTE-datetime</a>).
//	 * @return <tt>true</tt> if date on current position suits to ISO8601
//	 * date and time format.
//	 */
//	public boolean isISO8601Datetime();
//
//	/** Parse ISO8601 date and time format (see
//	 * <a href = "http://www.w3.org/TR/NOTE-datetime">
//	 * www.w3.org/TR/NOTE-datetime</a>).
//	 * @return <tt>true</tt> if date on current position suits to ISO8601
//	 * date format.
//	 */
//	public boolean isISO8601DateAndTime();
//
//	/** Parse date in ISO8601 format (see
//	 * <a href = "http://www.w3.org/TR/NOTE-datetime">
//	 * ISO 8601/W3C specification</a>).
//	 * @return <tt>true</tt> if date on current position suits to ISO8601
//	 * date format.
//	 */
//	public boolean isISO8601Date();
//
//	/** Parse time in ISO8601 format (see
//	 * <a href = "http://www.w3.org/TR/NOTE-datetime">
//	 * ISO 8601/W3C specification</a>).
//	 * @return <tt>true</tt> if date on current position suits to ISO8601
//	 * time format.
//	 */
//	public boolean isISO8601Time();
//
//	/** Parse date and/or time. Argument is string with format mask where
//	 * characters are interpreted as follows:
//	 * <ul>
//	 * <li><b>a</b> AM/PM marker</li>
//	 * <li><b>D</b> day in year</li>
//	 * <li><b>d</b> day of month (1 through 31)</li>
//	 * <li><b>E</b> day of week (text)
//	 *  <p> E, EE, EEE - abbreviated day name (Mon, Tue, .. Sun</p>
//	 *  <p> EEEE (and more)- full month name (Monday, Tuesday, .. Sunday</p>
//	 * </li>
//	 * <li><b>e</b> day of week (number 1=Monday, 7=Sunday)</li>
//	 * <li><b>F</b> day of week in month</li>
//	 * <li><b>G</b> era (0=BC, 1=AD)</li>
//	 * <li><b>H</b> hour (0 through 23)</li>
//	 * <li><b>h</b> hour (1..12 with am/pm)</li>
//	 * <li><b>K</b> hour 0..11 with am/pm)</li>
//	 * <li><b>k</b> hour 1..23</li>
//	 * <li><b>M</b> month in year (1=January .. 12=December).
//	 * <p> M - number without leading zer</p>
//	 * <p> MM - number with leading zero</p>
//	 * <p> MMM - abbreviated month name (Jan, Feb, .. Dec</p>
//	 * <p> MMMM (and more)- full month name (January, February, .. December</p>
//	 * </li>
//	 * <li><b>m</b> minute (0 through 59)</li>
//	 * <li><b>s</b> second (0 through 59, may be 60)</li>
//	 * <li><b>S</b> digits representing a decimal fraction of a second</li>
//	 * <li><b>y</b> year</li>
//	 * <li><b>Y</b> year (ISO variant, including negative numbers and leading
//	 * zeroes)</li>
//	 * <li><b>YY</b> year (two digits specification, century is added from
//	 * the actual year)</li>
//	 * <li><b>RR</b> year - two digits specification, century is added
//	 * according to ORACLE specification:<p>
//	 * Let c is century of actual date and y is number representing the year in
//	 * actual century. Let r be the specified two-digit year; then</p>
//	 * <p>if 00 &lt;= y &lt;= 49 and 00 &lt;= r &lt;= 49 then
//	 * result = c * 100 + r</p>
//	 * <p>if 00 &lt;= y &lt;= 49 and 50 &lt;= r &lt;= 99 then
//	 * result = (c + 1) * 100 + r</p>
//	 * <p>if 50 &lt;= y &lt;= 99 and 00 &lt;= r &lt;= 49 then
//	 * result = (c - 1) * 100 + r</p>
//	 * <p>if 50 &lt;= y &lt;= 99 and 50 &lt;= r &lt;= 99 then
//	 * result = c * 100 + r</p>
//	 * </li>
//	 * <li><b>W</b> week in month</li>
//	 * <li><b>w</b> week in year</li>
//	 * <li><b>Z</b> time zone designator (Z or +hh:mm or -hh:mm)
//	 * <p> ZZ time zone designator (Z or +h:m or -h:m)</p>
//	 * <p> ZZZZZ time zone designator (Z or +hhmm or -hhmm)</p>
//	 * <p> ZZZZZZ time zone designator (Z or +hh:mm or -hh:mm)</p>
//	 * </li>
//	 * <li><b>z</b> zone name
//	 * <p>z, zz, zzz abbreviated zone name (CET)</p>
//	 * <p>zzzz and more full zone name (Central European Time)</p>
//	 * </li>
//	 * </ul>
//	 * <p>One occurrence of above characters represent number without leading
//	 * zeroes. Repeated characters M,d,H,h,m and s represents n-digit number
//	 * with leading zeroes. However, the only year specifications 'y', 'yy'
//	 * and 'yyyy' are permitted, otherwise  the exception SYS059 is thrown.</p>
//	 * <p>Other characters are interpreted as strings which must match. If the
//	 * mask should describe one of above characters it must be quoted in
//	 * apostrophes. The character apostrophe itself must be doubled.</p>
//	 * <p>Parts of mask in square brackets are optional (e.g.</p>
//	 * <p>"HH:mm[:ss[.SSS]][z]"</p>
//	 * <p>describes time specification where fields with seconds, milliseconds
//	 * or zone are optional).</p>
//	 * <p>Variants of mask are separated by bar character '|' (e.g.</p>
//	 * <p>"yyyyMMdd|EEE, d MMM y H:m:s"</p>
//	 * <p>allows to specify date in format of number or in "unix" format).
//	 * Specification of default (predefined) value is in {} brackets and must
//	 * preceed each variant specification (e.g.</p>
//	 * <p>"{H8m30}[HH:mm]ss|{H8m30}yyyyMMdd[HH:mm]"</p>
//	 * - if hours and minutes are not specified the value is set to 08:30).
//	 * @param format String with date format.
//	 * @return <tt>true</tt> if the date source on current position suits
//	 * to given format.
//	 * @throws SRuntimeException if mask format is incorrect:
//	 * <ul>
//	 * <li>SYS059 incorrect year specification</li>
//	 * <li>SYS049 unclosed quoted literal</li>
//	 * </ul>
//	 */
//	public boolean isDatetime(final String format) throws SRuntimeException;
//
//	/** Parse duration in format format ISO 8061.
//	 * @return true if correct format of duration was parsed.
//	 */
//	public boolean isDuration();
//
//	/** Get value of parsed date and/or time. Returns instance of Calendar
//	 * with parsed values. Values which were not parsed are set to zero.
//	 * @return Calendar with parsed values.
//	 * @throws SRuntimeException SYS072 Data error
//	 */
//	public Calendar getParsedCalendar() throws SRuntimeException;
//
//	/** Get SDatetime object with value of parsed date.
//	 * @return SDatetime object with parsed values or <tt>null</tt>.
//	 */
//	public SDatetime getParsedSDatetime();
//
//	/** Get SDuration object with value of parsed duration.
//	 * @return SDuration object with parsed values or <tt>null</tt>.
//	 */
//	public SDuration getParsedSDuration();
//
//////////////////////////////////////////////////////////////////////////////////
//// Reporter methods
//////////////////////////////////////////////////////////////////////////////////
//
//	/** Get report writer.
//	 * @return Report writer associated with this reporter or <tt>null</tt>.
//	 */
//	public ReportWriter getReportWriter();
//
//	/** Check if fatals, errors, light errors or warnings were generated.
//	 * @return true if errors or warnings occurred.
//	 */
//	public boolean errorWarnings();
//
//	/** Check error reports in the reporter. Return normally if no error was
//	 * reported, otherwise throws the exception with the list of error
//	 * messages (max. MAX_REPORTS messages).
//	 * @throws SRuntimeException if an error was reported.
//	 */
//	public void checkAndThrowErrors() throws SRuntimeException;
//
//////////////////////////////////////////////////////////////////////////////////
//// XML parsing methods
//////////////////////////////////////////////////////////////////////////////////
//
//	/** Parse XML whitespace character (see
//	 * <a href="http://www.w3.org/TR/REC-xml/">W3C XML specification</a>).
//	 * @return parsed character or ENDCHAR.
//	 */
//	public char isXMLWhitespaceChar();
//
//	/** Parse XML name start character (see
//	 * <a href="http://www.w3.org/TR/REC-xml/">W3C XML specification</a>).
//	 * @param xmlVersion false .. "1.0", true .. "1.1"
//	 * @return parsed character or ENDCHAR.
//	 */
//	public char isXMLNamestartChar(boolean xmlVersion);
//
//	/** Parse XML name extension character (see
//	 * <a href="http://www.w3.org/TR/REC-xml/">W3C XML specification</a>).
//	 * @param xmlVersion false .. "1.0", true .. "1.1"
//	 * @return parsed character or ENDCHAR.
//	 */
//	public char isXMLNameExtensionChar(boolean xmlVersion);
//
//	/** Parse XML nmtoken and save result to parsed string field (see
//	 * <a href="http://www.w3.org/TR/REC-xml/">W3C XML specification</a>).
//	 * @param xmlVersion false .. "1.0", true .. "1.1"
//	 * @return true if rule passed.
//	 *
//	 */
//	public boolean isNMToken(boolean xmlVersion);
//
//	/** Parse XML NCName and save result to parsed string field (see
//	 * <a href="http://www.w3.org/TR/REC-xml/">W3C XML specification</a>).
//	 * @param xmlVersion false .. "1.0", true .. "1.1"
//	 * @return true if NCNname was recognized.
//	 */
//	public boolean isNCName(boolean xmlVersion);
//
//	/** Parse XML name and save result to parsed string field (see
//	 * <a href="http://www.w3.org/TR/REC-xml/">W3C XML specification</a>).
//	 * @param xmlVersion false .. "1.0", true .. "1.1"
//	 * @return true if actual input is XML name.
//	 */
//	public boolean isXMLName(boolean xmlVersion);
//
//	/** Parse Java identifier and save result to _parsedString.
//	 * @return true if actual input is Java identifier.
//	 */
//	public boolean isJavaName();
//
//	/** Parse Java fully qualified identifier and save result to
//	 * parsedString.
//	 * @return true if actual input is Java fully qualified identifier.
//	 */
//	public boolean isJavaQName();
//
//	/** Parse valid XML character and save result to parsed string field
//	 * (see <a href="http://www.w3.org/TR/REC-xml/">
//	 * W3C XML specification</a>).
//	 * @param xmlVersion false .. "1.0", true .. "1.1"
//	 * @return parsed character or ENDCHAR.
//	 */
//	public char isXMLChar(boolean xmlVersion);
//
}