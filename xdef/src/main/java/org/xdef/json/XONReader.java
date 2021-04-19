package org.xdef.json;

import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import org.xdef.msg.JSON;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.Price;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SException;
import org.xdef.sys.SParser;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;

/** Parser of JSON/XON source.
 * @author Vaclav Trojan
 */
public class XONReader extends StringParser implements XONParsers {

	/** Flag to accept comments (default false; true=accept comments). */
	boolean _acceptComments;
	/** Flag to generate SPositions (default false; true=generate position). */
	boolean _genJObjects;
	/** Flag if parse JSON or XON (default false; false=JSON, true=XON). */
	boolean _xonMode;
	/** Flag if the parsed data are in X-definition (default false). */
	private boolean _jdef;
	/** Parser of XON source. */
	private final JParser _jp;

	/** Create instance of parser.
	 * @param jp parser of XON source.
	 */
	XONReader(JParser jp) {_jp = jp;}

	/** Create instance of parser.
	 * @param jp parser of XON source.
	 * @param source String with source data.
	 */
	public XONReader(final String source, JParser jp) {
		super(source);
		_jp = jp;
	}
	/** Create instance of parser.
	 * @param jp parser of XON source.
	 * @param source Reader with source data.
	 */
	public XONReader(final Reader source, JParser jp) {
		super(source, new ArrayReporter());
		_jp = jp;
	}
	/** Create instance of parser.
	 * @param jp parser of XON source.
	 * @param source URL with source data.
	 */
	public XONReader(final URL source, JParser jp) {
		super(source, new ArrayReporter(), 0);
		_jp = jp;
	}

	/** Set mode that JSON is parsed in X-definition compiler. */
	public final void setXdefMode() {
		_genJObjects = _acceptComments = _jdef = true;
		_xonMode = false;
	}

	/** Set mode that JSON is parsed in X-definition compiler. */
	public final void setJObjectsMode() {
		_genJObjects = true;
	}

	/** Set mode that XON is parsed. */
	public final void setXonMode() {
		_genJObjects = _jdef = false;
		_acceptComments = _xonMode = true;
	}

	/** Set mode for strict JSON parsing (JSON, no comments). */
	public final void setJsonMode() {
		_acceptComments = _xonMode = _genJObjects = _jdef = false;
	}

	/** Set mode for JSON parsing (with comments). */
	public final void setCommentsMode() {
		_acceptComments = true;
	}

	/** Create modification string with source position.
	 * @return modification string with source position.
	 */
	public final String genPosMod() {
		return "&{line}" + getLineNumber()
			+ "&{column}" + getColumnNumber()
			+ "&{sysId}" + getSysId();
	}

	/** Skip white space separators (and comments if accepted).
	 * @return true if a space or comment was found.
	 */
	public final boolean isSpacesOrComments() {
		boolean result = isSpaces();
		boolean wasLineComment;
		while((wasLineComment = isChar('#')) || isToken("/*") ) {
			result = true;
			if (!_acceptComments) { // omments not allowed
				warning(JSON.JSON019);  //Comments are not allowed here
			}
			if (wasLineComment) {
				while(!eos() && !isNewLine()) {
					nextChar();
				}
			} else if (!findTokenAndSkip("*/")) {
				error(JSON.JSON015); //Unclosed comment
				setEos();
				return result;
			}
			isSpaces();
		}
		return result;
	}

	/** Read JSON/XON map.
	 * 0@throws SRuntimeException is an error occurs.
	 */
	private void readMap() throws SRuntimeException {
		_jp.mapStart(getPosition());
		isSpacesOrComments();
		SPosition spos = getPosition();
		if (isChar('}')) { // empty map
			_jp.mapEnd(spos);
			return;
		}
		boolean wasScript = false;
		int i;
		while(!eos()) {
			if (_jdef && !wasScript
				&& (i = isOneOfTokens(JsonParser.SCRIPT_NAME,
					JsonParser.ONEOF_NAME)) >= 0) {
				SBuffer name = new SBuffer(getParsedString(), spos);
				wasScript = true;
				isSpacesOrComments();
				SBuffer value = null;
				if (i == 1) {
					if (isChar(':')) {
						isSpacesOrComments();
						spos = getPosition();
						Object o = readSimpleValue();
						if (o instanceof String) {
							value = new SBuffer((String) o, spos);
						} else {
							//Value of $script must be string with X-script
							_jp.error(this, JSON.JSON018);
						}
					}
					_jp.xdScript(name, value);
				} else {
					if (!isChar(':') && i != 1) {
						//"&{0}"&{1}{ or "}{"} expected
						_jp.error(this, JSON.JSON002, ":");
					}
					spos = getPosition();
					Object o = readSimpleValue();
					if (o != null && o instanceof String) {
						_jp.xdScript(name, new SBuffer((String) o, spos));
					} else {
						//Value of $script must be string with X-script
						_jp.error(this, JSON.JSON018);
					}
				}
			} else {
				SBuffer name;
				spos = getPosition();
				if (isChar('"')) {
					name = new SBuffer(JsonTools.readJSONString(this), spos);
				} else if (_xonMode && isXMLName(StringParser.XMLVER1_0)) {
					String s = getParsedString();
					int ndx = s.indexOf(':');
					if (ndx == 0) {
						_jp.fatal(this, JSON.JSON004); //Name of item expected
						_jp.mapEnd(this);
						return;
					}
					if (ndx > 0) {
						setIndex(getIndex() - (s.length() - ndx));
						s = s.substring(0, ndx);
					}
					name = new SBuffer(s, spos);
				} else {
					_jp.fatal(this, JSON.JSON004); //Name of item expected
					_jp.mapEnd(this);
					return;
				}
				isSpacesOrComments();
				if (!isChar(':')) {
					//"&{0}"&{1}{ or "}{"} expected
					_jp.error(this, JSON.JSON002, ":");
				}
				_jp.namedValue(name);
				isSpacesOrComments();
				readItem();
			}
			isSpacesOrComments();
			if (isChar('}')) {
				isSpacesOrComments();
				_jp.mapEnd(this);
				return;
			}
			if (isChar(',') || _xonMode) {
				spos = getPosition();
				isSpacesOrComments();
				if (isChar('}')) {
					if (!_xonMode) {
						SPosition spos1 = getPosition();
						setPosition(spos);
						_jp.error(this, JSON.JSON020); //redundant comma
						setPosition(spos1);
					}
					_jp.mapEnd(this);
					return;
				}
			} else {
				if (eos()) {
					break;
				}
				//"&{0}"&{1}{ or "}{"} expected
				_jp.error(this, JSON.JSON002, ",", "}");
				if (getCurrentChar() != '"') {
					break;
				}
			}
		}
		//"&{0}"&{1}{ or "}{"} expected&{#SYS000}
		_jp.fatal(this, JSON.JSON002, "}");
		if (findOneOfChars("[]{}") == NOCHAR) {// skip to next item
			setEos();
		}
		_jp.mapEnd(this);
	}

	/** Read JSON/XON array.
	 * 0@throws SRuntimeException is an error occurs.
	 */
	private void readArray() throws SRuntimeException {
		isSpacesOrComments();
		_jp.arrayStart(getPosition());
		if (isChar(']')) { // empty array
			_jp.arrayEnd(getPosition());
			return;
		}
		boolean wasScript = false;
		boolean wasErrorReported = false;
		while(!eos()) {
			int i;
			SPosition spos = getPosition();
			if (!wasScript &&_jdef
				&& (i = isOneOfTokens(JsonParser.SCRIPT_NAME,
					JsonParser.ONEOF_NAME))>=0) {
				SBuffer name = new SBuffer(getParsedString(), spos);
				wasScript = true;
				SBuffer value = null;
				if (isChar(':')) {
					isSpacesOrComments();
					JsonParser.JValue jv = readSimpleValue();
					if (jv.getValue() instanceof String) {
						if (i == 1) {
							value = new SBuffer((String) jv.getValue(),
								jv.getPosition());
						}
					} else {
						//Value of $script must be string with X-script
						_jp.error(this, JSON.JSON018);
					}
				} else {
					if (i == 0) {
						//"&{0}"&{1}{ or "}{"} expected
					   _jp.error(this, JSON.JSON002, ":");
					}
				}
				_jp.xdScript(name, value);
			} else {
				readItem();
			}
			isSpacesOrComments();
			if (isChar(']')) {
				_jp.arrayEnd(this);
				return;
			}
			if (isChar(',')) {
				spos = getPosition();
				isSpacesOrComments();
				if (isChar(']')) {
					if (!_xonMode) {
						SPosition spos1 = getPosition();
						setPosition(spos);
						_jp.error(this, JSON.JSON020); //redundant comma
						setPosition(spos1);
					}
					_jp.arrayEnd(this);
					return;
				}
			} else {
				if (wasErrorReported) {
					break;
				}
				 //"&{0}"&{1}{ or "}{"} expected
				_jp.error(this, JSON.JSON002,",","]");
				if (eos()) {
					break;
				}
				wasErrorReported = true;
			}
		}
		_jp.error(this, JSON.JSON002, "]"); //"&{0}"&{1}{ or "}{"} expected
		if (findOneOfChars("[]{}") == NOCHAR) {// skip to next item
			setEos();
		}
		_jp.arrayEnd(this);
	}

	/** Returns parsed simpleValue.
	 * @param x parsed simpleValue to be returned.
	 * @return parsed simpleValue. If the switch _genJObjects is true, then
	 * the parsed simpleValue contains source position.
	 */
	private JsonParser.JValue returnValue(SPosition spos, final Object x) {
		return new JsonParser.JValue(spos, x);
	}

	/** Returns error and parsed simpleValue.
	 * @param x parsed simpleValue to be returned.
	 * @param code currencyCode of error message.
	 * @param skipChars string with characters to which source will be skipped.
	 * @param params list od error message parameters (may be empty.)
	 * @return parsed simpleValue. If the switch _genJObjects is true, then
	 * the parsed simpleValue contains source position.
	 */
	private JsonParser.JValue returnError(SPosition spos,
		final Object x,
		final long code,
		final String skipChars,
		final Object... params) {
		_jp.error(this, code, params);
		if (findOneOfChars(skipChars) == NOCHAR) {// skip to next item
			setEos();
		}
		return returnValue(spos, x);
	}

	/** Read name of GPS position. */
	private String readGPSName() {
		StringBuilder sb = new StringBuilder();
		char ch;
		if (isChar('"')) { //quoted
			for (;;) {
				if (isToken("\\\"")) { // escaped quote
					sb.append('"');
				} else if (isChar('"')) { // end of name
					break;
				} else if (eos()) { //error (missing quote)
					sb.setLength(0);
					break;
				} else {
					sb.append(peekChar());
				}
			}
		} else if ((ch = isLetter()) != SParser.NOCHAR) { //not quoted
			sb.append(ch);
			while ((ch = getCurrentChar()) != SParser.NOCHAR
				&& (Character.isLetter(ch) || ch == ' ')) {
				sb.append(peekChar());
			}
		}
		String result = sb.toString().trim();
		if (result.isEmpty()) {
			// Incorrect GPosition &{0}{: }
			throw new SRuntimeException(XDEF.XDEF222, "name: " + result);
		}
		return result;
	}

	/** Read JSON/XON simple simpleValue.
	 * @return parsed simpleValue: String, Number, Boolean or null (or an XON object).
	 * @throws SRuntimeException is an error occurs.
	 */
	private JsonParser.JValue readSimpleValue() throws SRuntimeException {
		SPosition spos = getPosition();
		int i;
		if (isChar('"')) { // string
			return returnValue(spos, JsonTools.readJSONString(this));
		} else if ((i=isOneOfTokens(new String[]{"null","false","true"}))>=0) {
			return returnValue(spos, i > 0 ? (i==2) : null);
		} else {
			int pos = getIndex();
			boolean wasError = false;
			Object result = null;
			char ch;
			if (_xonMode) {
				if (isChar('\'')) { // character
					i = JsonTools.readJSONChar(this);
					if (i == -1) {
						//JSON simpleValue expected
						return returnError(spos, '?', JSON.JSON010, "[]{}");
					}
					ch = (char) i;
					if (!isChar('\'')) {
						setIndex(pos);
						//JSON simpleValue expected
						return returnError(spos, ch, JSON.JSON010, "',[]{}");
					}
					return returnValue(spos, ch);
				} else if (isToken("b(")) {
					try {
						result = SUtils.decodeBase64(this);
						if (isChar(')')) {
							return returnValue(spos, result);
						}
					} catch (SException ex) {
						putReport(ex.getReport());
						return returnValue(spos, null);
					}
					setIndex(pos);
					return returnError(spos, null, JSON.JSON010, "[]{}");
				} else if (isChar('D')) {
					if (isDatetime("yyyy-MM-dd['T'HH:mm:ss[.S]][Z]" +
						"|HH:mm:ss[.S][Z]"+ //time
						"|--MM[-dd][Z]" + //month day
						"|---dd[Z]"+ //day
						"|yyyy-MMZ"+ // year month
						"|yyyyZ"+ // year with zone
						"|yyyy-MM"+ // year month
						"|yyyy")) { // year without zone
						return returnValue(spos, getParsedSDatetime());
					}
					//JSON simpleValue expected
					return returnError(spos, null, JSON.JSON010, "[]{}");
				} else if (isChar('P') || isToken("-P")) {
					setIndex(pos);
					if (isXMLDuration()) {
						return returnValue(spos, getParsedSDuration());
					}
					setIndex(pos);
					//JSON simpleValue expected
					return returnError(spos, null, JSON.JSON010, "[]{}");
				} else if (isToken("p(")) { // currency ammount
					if (isFloat() || isInteger()) {
						double d = Double.parseDouble(getParsedString());
						isChar(' ');
						if ((ch=isLetter()) != SParser.NOCHAR) {
							String code = String.valueOf(ch);
							i = 0;
							for (;;) {
								if (++i < 3	&& (ch=isLetter())!=SParser.NOCHAR){
									code += ch;
								} else {
									break;
								}
							}
							if (isChar(')') && i == 3) {
								try {
									return returnValue(spos, new Price(d,code));
								} catch (SRuntimeException ex) {
									putReport(ex.getReport());//currency error
									return returnValue(spos, null);
								}
							}
						}
					}
					setIndex(pos);
					//JSON simpleValue expected
					return returnError(spos, null, JSON.JSON010, "[]{}");
				} else if (isToken("g(")) { // GPS position
					result = null;
					if (isSignedFloat() || isSignedInteger()) {
						double latitude = getParsedDouble();
						if (isChar(',') && (isChar(' ') || true)
							&& (isSignedFloat() || isSignedInteger())) {
							double longitude = getParsedDouble();
							double altitude = Double.MIN_VALUE;
							String name = null;
							if (isChar(',') && (isChar(' ') || true)) {
								if (isSignedFloat() || isSignedInteger()) {
									altitude = getParsedDouble();
									if (isChar(',') && (isChar(' ') || true)) {
										name = readGPSName();
									}
								} else {
									name = readGPSName();
								}
							}
							if (isChar(')')) {
								try {
									return returnValue(spos, new GPSPosition(
										latitude, longitude, altitude, name));
								} catch(SRuntimeException ex) {
									putReport(ex.getReport()); // invalid GPS
									return returnValue(spos, null);
								}
							}
						}
					}
					setIndex(pos);
					//JSON simpleValue expected
					return returnError(spos, null, JSON.JSON010, "[]{}");
				} else if ((i=isOneOfTokens("NaN", "INF", "-INF")) >= 0) {
					if (isChar('F')) {
						return returnValue(spos, i == 0 ? Float.NaN
							: i == 1 ? Float.POSITIVE_INFINITY
								: Float.NEGATIVE_INFINITY);
					}
					isChar('D');
					return returnValue(spos, i == 0 ? Double.NaN
						: i == 1 ? Double.POSITIVE_INFINITY
							: Double.NEGATIVE_INFINITY);
				}
			}
			setIndex(pos);
			boolean minus = isChar('-');
			if (!minus && isChar('+')) {
				error(JSON.JSON017, "+");//Not allowed character '&{0}'
				wasError = true;
			}
			i = getIndex();
			if (isInteger()) {
				boolean isfloat;
				if (isfloat = isChar('.')) { // decimal point
					if (!isInteger()) {
						error(JSON.JSON017, ".");//Not allowed character '&{0}'
						wasError = true;
					}
				}
				if ((ch = isOneOfChars("eE")) != SParser.NOCHAR) {//exponent
					isfloat = true;
					if (!isSignedInteger()) {//Not allowed character '&{0}'
						error(JSON.JSON017, ch);
						wasError = true;
					}
				}
				String s = getBufferPart(i, getIndex());
				if (s.charAt(0) == '0' && s.length() > 1 &&
					Character.isDigit(s.charAt(1))) {
						error(JSON.JSON014); // Illegal leading zero in number
				}
				if (minus) {
					s = '-' + s;
				}
				if (wasError) {
					return returnValue(spos,0);
				}
				if (_xonMode) {
					try {
						switch(ch = isOneOfChars("FDd")) {
							case 'F':
								return returnValue(spos, Float.parseFloat(s));
							case 'D':
								return returnValue(spos, Double.parseDouble(s));
							case 'd':
								return returnValue(spos, new BigDecimal(s));
						}
					} catch (Exception ex) {
						//Illegal number simpleValue &{0}{ for XON type "}{"}: &{1}
						error(JSON.JSON023, ch, s);
					}
					if (!isfloat) {
						try {
							switch(ch = isOneOfChars("NLISB")) {
								case 'N':
									return returnValue(spos, new BigInteger(s));
								case 'L':
									return returnValue(spos, Long.parseLong(s));
								case 'I':
									return returnValue(spos,
										Integer.parseInt(s));
								case 'S':
									return returnValue(spos,
										Short.parseShort(s));
								case 'B':
									return returnValue(spos, Byte.parseByte(s));
							}
						} catch (Exception ex) {
							//Illegal number simpleValue &{0}{ for XON type "}{"}:&{1}
							error(JSON.JSON023, ch, s);
						}
					}
				}
				try {
					if (isfloat) {
						return returnValue(spos, new BigDecimal(s));
					} else {
						try {
							return returnValue(spos, Long.parseLong(s));
						} catch (Exception exx) {
							return returnValue(spos, new BigInteger(s));
						}
					}
				} catch (Exception ex) {
					//Illegal number simpleValue &{0}{ for XON type "}{"}:&{1}
					error(JSON.JSON023, null, s);
					return returnValue(spos, 0);
				}
			}
			// error
			setIndex(pos);
			//JSON simpleValue expected
			return returnError(spos, null, JSON.JSON010, "[]{}");
		}
	}

	/** Read JSON/XON item.
	 * @return -1 .. end of source or error, 0 .. simpleValue,
	 * 1 .. array, 2 .. map.
	 * 0@throws SRuntimeException is an error occurs.
	 */
	private void readItem() throws SRuntimeException {
		if (eos()) {
			_jp.fatal(this, JSON.JSON007); //unexpected eof
		} else if (isChar('{')) { // Map
			readMap();
		} else if (isChar('[')) {
			readArray();
		} else if (_jdef && isToken(JsonParser.ANY_NAME)) {
			SPosition spos = getPosition(); // xdef $ANY
			spos.setIndex(getIndex() - JsonParser.ANY_NAME.length());
			SBuffer name = new SBuffer(JsonParser.ANY_NAME, spos);
			SBuffer val = new SBuffer(JsonParser.ANY_NAME, spos);
			isSpacesOrComments();
			if (isChar(':')) {
				isSpacesOrComments();
				JsonParser.JValue jv = readSimpleValue();
				if (!(((JsonParser.JValue) jv).getValue() instanceof String)) {
					//After ":" in the command $any must follow simpleValue
					_jp.error(jv.getPosition(), JSON.JSON021);
				} else {
					val = jv.getSBuffer();
				}
			}
			_jp.xdScript(name, val);
		} else {
			JsonParser.JValue jv = readSimpleValue();
			_jp.simpleValue(jv);
		}
	}

////////////////////////////////////////////////////////////////////////////////
// interface XONParsers
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Parse JSON or XON source data (depends on the flag "_xon").
	 * @throws SRuntimeException if an error occurs,
	 */
	public final void parse() throws SRuntimeException {
		isSpacesOrComments();
		readItem();
		isSpacesOrComments();
		if (!eos()) {
			_jp.error(getPosition(), JSON.JSON008);
		}
	}
}