package org.xdef.json;

import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.xdef.impl.compile.CompileJsonXdef;
import org.xdef.msg.JSON;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Price;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.SException;
import org.xdef.sys.SParser;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;

/** Parser of JSON/XON source.
 * @author Vaclav Trojan
 */
public class JsonParser extends StringParser {

	/** Flag to accept comments (default false; true=accept comments). */
	private boolean _acceptComments;
	/** Flag if parse JSON or XON (default false; false=JSON, true=XON). */
	private boolean _xonMode;
	/** Flag if the parsed data are in X-definition (default false). */
	private boolean _jdef;
	/** Flag to generate SPositions (default false; true=generate position). */
	private boolean _genJObjects;
	/** Position of processed item (used if genJObjects is true).`*/
	private SPosition _sPosition;

	/** Create instance of parser. */
	public JsonParser() {}
	/** Create instance of parser.
	 * @param source String with source data.
	 */
	public JsonParser(final String source) {super(source);}
	/** Create instance of parser.
	 * @param source Reader with source data.
	 */
	public JsonParser(final Reader source) {
		super(source, new ArrayReporter());
	}
	/** Create instance of parser.
	 * @param source URL with source data.
	 */
	public JsonParser(final URL source) {
		super(source, new ArrayReporter(), 0);
	}

	/** Set mode that JSON is parsed in X-definition compiler. */
	public final void setXdefMode() {
		_genJObjects = _acceptComments = _jdef = true;
		_xonMode = false;
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
	public final void setJsonCommentsMode() {
		_xonMode = _genJObjects = _jdef = false;
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
		while(isToken("/*")) {
			result = true;
			if (!_acceptComments) { // omments not allowed
				warning(JSON.JSON019);  //Comments are not allowed here
			}
			if (!findTokenAndSkip("*/")) {
				error(JSON.JSON015); //Unclosed comment
				setEos();
				return result;
			}
			isSpaces();
		}
		return result;
	}

	/** Returns parsed value.
	 * @param x parsed value to be returned.
	 * @return parsed value. If the switch _genJObjects is true, then
	 * the parsed value contains source position.
	 */
	private Object returnValue(final Object x) {
		return _genJObjects ? new CompileJsonXdef.JValue(_sPosition, x) : x;
	}

	/** Returns error and parsed value.
	 * @param x parsed value to be returned.
	 * @param code currencyCode of error message.
	 * @param skipChars string with characters to which source will be skipped.
	 * @param params list od error message parameters (may be empty.)
	 * @return parsed value. If the switch _genJObjects is true, then
	 * the parsed value contains source position.
	 */
	private Object returnError(final Object x,
		final long code,
		final String skipChars,
		final Object... params) {
		error(code, params);
		if (findOneOfChars(skipChars) == NOCHAR) {// skip to next item
			setEos();
		}
		return returnValue(x);
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

	/** Read JSON value.
	 * @return parsed value: List, Map, String, Number, Boolean or null.
	 * @throws SRuntimeException is an error occurs.
	 */
	private Object readValue() throws SRuntimeException {
		if (eos()) {
			fatal(JSON.JSON007); //unexpected eof
			return _genJObjects
				? new CompileJsonXdef.JValue(_sPosition, null) : null;
		}
		if (_genJObjects) {
			_sPosition = getPosition();
		}
		int i;
		if (isChar('{')) { // Map
			Map<String, Object> map;
			map = _genJObjects ? new CompileJsonXdef.JMap(_sPosition)
				: new LinkedHashMap<String,Object>();
			isSpacesOrComments();
			if (isChar('}')) { // empty map
				return map;
			}
			boolean wasScript = false;
			while(!eos()) {
				if (_jdef && !wasScript
					&& (i = isOneOfTokens(CompileJsonXdef.SCRIPT_NAME,
						CompileJsonXdef.ONEOF_NAME)) >= 0) {
					wasScript = true;
					SPosition spos = getPosition();
					isSpacesOrComments();
					Object o;
					if (i == 1) {
						String s;
						if (isChar(':')) {
							spos.setIndex(spos.getIndex() - 1);
							isSpacesOrComments();
							o = readValue();
							if (o instanceof CompileJsonXdef.JValue
								&& ((CompileJsonXdef.JValue)o).getValue()
								instanceof String) {
								s = CompileJsonXdef.ONEOF_KEY +
									((CompileJsonXdef.JValue)o).getValue();
							} else {
								//Value of $script must be string with X-script
								error(JSON.JSON018);
								s = CompileJsonXdef.ONEOF_KEY;
							}
						} else {
							s = CompileJsonXdef.ONEOF_KEY;
						}
						o = new CompileJsonXdef.JValue(spos, s);
					} else {
						if (!isChar(':') && i != 1) {
							//"&{0}"&{1}{ or "}{"} expected
							error(JSON.JSON002, ":");
						}
						isSpacesOrComments();
						o = readValue();
					}
					if (o != null && o instanceof CompileJsonXdef.JValue
						&& ((CompileJsonXdef.JValue)o).getValue()
							instanceof String) {
						if (map.containsKey(CompileJsonXdef.SCRIPT_KEY)) {
							//Value pair &{0} already exists
							error(JSON.JSON022, CompileJsonXdef.SCRIPT_KEY);
						} else {
							map.put(CompileJsonXdef.SCRIPT_KEY, o);
						}
					} else {
						//Value of $script must be string with X-script
						error(JSON.JSON018);
					}
				} else {
					Object o;
					String name;
					if (getCurrentChar() != '"'
						&& _xonMode && isNCName(StringParser.XMLVER1_0)) { //XON
						// parse XON named pair
						name = getParsedString(); /*xx*/
						isSpacesOrComments();
						if (!isChar('=')) {
							//"&{0}"&{1}{ or "}{"} expected
							error(JSON.JSON002,"=");
						}
					} else { // JSON
						o = readValue();
						if (o != null && (o instanceof String ||
							(_genJObjects&&o instanceof CompileJsonXdef.JValue)
							&& ((CompileJsonXdef.JValue) o).getValue()
								instanceof String)) {
							name = _genJObjects ? o.toString() : (String) o;
							isSpacesOrComments();
						} else {
							fatal(JSON.JSON004); //Name of item expected
							return map;
						}
						if (!isChar(':')) {
							//"&{0}"&{1}{ or "}{"} expected
							error(JSON.JSON002, ":");
						}
					}
					isSpacesOrComments();
					o = readValue();
					if (map.containsKey(name)) {
						String s;
						if (_xonMode) {
							s = name;
						} else {
							s = JsonTools.jstringToSource(name);
							if (!s.startsWith("\"") || !s.endsWith("\"")) {
								s = '"' + s + '"';
							}
						}
						//Value pair &{0} already exists
						error(JSON.JSON022, s);
					} else {
						map.put(name, o);
					}
				}
				isSpacesOrComments();
				if (isChar('}')) {
					isSpacesOrComments();
					return map;
				}
				if (isChar(',') || _xonMode) {
					SPosition spos = getPosition();
					isSpacesOrComments();
					if (isChar('}')) {
						if (!_xonMode) {
							SPosition spos1 = getPosition();
							setPosition(spos);
							error(JSON.JSON020); //redundant comma
							setPosition(spos1);
						}
						return map;
					}
//				} else if (!_xonMode) {
				} else {
					if (eos()) {
						break;
					}
					//"&{0}"&{1}{ or "}{"} expected
					error(JSON.JSON002, ",", "}");
					if (getCurrentChar() != '"') {
						break;
					}
				}
			}
			//"&{0}"&{1}{ or "}{"} expected&{#SYS000}
			error(JSON.JSON002, "}");
			if (findOneOfChars("[]{}") == NOCHAR) {// skip to next item
				setEos();
			}
			return map;
		} else if (isChar('[')) {
			List<Object> list;
			list = _genJObjects ? new CompileJsonXdef.JArray(_sPosition)
				: new ArrayList<Object>();
			isSpacesOrComments();
			if (isChar(']')) { // empty array
				return list;
			}
			boolean wasScript = false;
			boolean wasErrorReported = false;
			while(!eos()) {
				if (!wasScript &&_jdef
					&& (i = isOneOfTokens(CompileJsonXdef.SCRIPT_NAME,
						CompileJsonXdef.ONEOF_NAME)) >= 0) {
					wasScript = true;
					if (isChar(':')) {
						isSpacesOrComments();
						Object o = readValue();
						if (o instanceof CompileJsonXdef.JValue
							&& ((CompileJsonXdef.JValue)o).getValue()
							instanceof String) {
							CompileJsonXdef.JValue jv =
								(CompileJsonXdef.JValue) o;
							if (i == 1) {
								SPosition spos = jv.getPosition();
								spos.setIndex(spos.getIndex() - 1);
								String s =
									CompileJsonXdef.ONEOF_KEY + jv.getValue();
								jv = new CompileJsonXdef.JValue(spos, s);
							}
							list.add(new CompileJsonXdef.JValue(null, jv));
						} else {
							//Value of $script must be string with X-script
							error(JSON.JSON018);
						}
					} else {
						if (i == 0) {
							//"&{0}"&{1}{ or "}{"} expected
						   error(JSON.JSON002, ":");
						} else {
							SPosition spos = getPosition();
							spos.setIndex(spos.getIndex() - 1);
							list.add(new CompileJsonXdef.JValue(null,
								new CompileJsonXdef.JValue(spos,
									CompileJsonXdef.ONEOF_KEY)));
						}
					}
				} else {
					list.add(readValue());
				}
				isSpacesOrComments();
				if (isChar(']')) {
					return list;
				}
				if (isChar(',')) {
					SPosition spos = getPosition();
					isSpacesOrComments();
					if (isChar(']')) {
						if (!_xonMode) {
							SPosition spos1 = getPosition();
							setPosition(spos);
							error(JSON.JSON020); //redundant comma
							setPosition(spos1);
						}
						return list;
					}
//				} else if (!_xonMode) {
				} else {
					if (wasErrorReported) {
						break;
					}
					error(JSON.JSON002,",","]"); //"&{0}"&{1}{ or "}{"} expected
					if (eos()) {
						break;
					}
					wasErrorReported = true;
				}
			}
			error(JSON.JSON002, "]"); //"&{0}"&{1}{ or "}{"} expected
			if (findOneOfChars("[]{}") == NOCHAR) {// skip to next item
				setEos();
			}
			return list;
		} else if (isChar('"')) { // string
			return returnValue(JsonTools.readJSONString(this));
		} else if ((i=isOneOfTokens(new String[]{"null","false","true"})) >= 0){
			return returnValue(i > 0 ? (i==2) : null);
		} else if (_jdef && isToken(CompileJsonXdef.ANY_NAME)) {
			isSpacesOrComments();
			if (isChar(':')) {
				isSpacesOrComments();
				Object val = readValue();
				if (!(val instanceof CompileJsonXdef.JValue)
					|| (((CompileJsonXdef.JValue) val).getValue()
						instanceof String)) {
					//After ":" in the command $any must follow a string value
					error(JSON.JSON021);
				} else {
					return new CompileJsonXdef.JAny(
						_sPosition,((CompileJsonXdef.JValue) val).getSBuffer());
				}
			}
			return new CompileJsonXdef.JAny(_sPosition, null);
		} else {
			int pos = getIndex();
			boolean wasError = false;
			Object result = null;
			char ch;
			if (_xonMode) {
				if (isChar('\'')) { // character
					ch = getCurrentChar();
					if (ch == '\\') {
						if ((i="u\\bfnrt\"".indexOf(ch=nextChar())) < 0) {
							//JSON value expected
							return returnError('?', JSON.JSON010, "[]{}");
						} else if (i > 0) {
							ch = "?\\\b\f\n\r\t\"".charAt(i);
							nextChar();
						} else {
							nextChar();
							int x = 0;
							for (int j = 0; j < 4; j++) {
								int y = JsonTools.hexDigit(peekChar());
								if (y < 0) {
									//hexadecimal digit expected
									return returnError(null,
										JSON.JSON005, "[]{}");
								}
								x = (x << 4) + y;
							}
							ch = (char) x;
						}
					} else {
						nextChar();
					}
					if (!isChar('\'')) {
						setIndex(pos);
						//JSON value expected
						return returnError(ch, JSON.JSON010, "',[]{}");
					}
					return returnValue(ch);
				} else if (isToken("b(")) {
					try {
						result = SUtils.decodeBase64(this);
						if (isChar(')')) {
							return returnValue(result);
						}
					} catch (SException ex) {
						putReport(ex.getReport());
						return returnValue(null);
					}
					setIndex(pos);
					return returnError(null, JSON.JSON010, "[]{}");
				} else if (isToken("d(")) {
					if (isDatetime("--M[-d][Z]" + //month day
						"|---d[Z]"+ //day
						"|H:m:s[.S][Z]"+ //time
						"|y-M-d['T'H:m:s[.S][Z]]" +
						"|y-MZ"+ // year month
						"|yZ"+ // year with zone
						"|y-M"+ // year month
						"|y")) { // year without zone
						if (isChar(')')) {
							return returnValue(getParsedSDatetime());
						}
					}
//					setIndex(pos);
					//JSON value expected
					return returnError(null, JSON.JSON010, "[]{}");
				} else if (isToken("p(")) {
					if (isXMLDuration()) {
						if (isChar(')')) {
							return returnValue(getParsedSDuration());
						}
					}
					setIndex(pos);
					//JSON value expected
					return returnError(null, JSON.JSON010, "[]{}");
				} else if (isToken("#(")) { // currency ammount
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
									return returnValue(new Price(d, code));
								} catch (SRuntimeException ex) {
									putReport(ex.getReport());//currency error
									return returnValue(null);
								}
							}
						}
					}
					setIndex(pos);
					//JSON value expected
					return returnError(null, JSON.JSON010, "[]{}");
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
									return returnValue(new GPSPosition(
										latitude, longitude, altitude, name));
								} catch(SRuntimeException ex) {
									putReport(ex.getReport()); // invalid GPS
									return returnValue(null);
								}
							}
						}
					}
					setIndex(pos);
					//JSON value expected
					return returnError(null, JSON.JSON010, "[]{}");
				}
			}
			setIndex(pos);
			boolean minus = false;
			if (isChar('+')) {
				error(JSON.JSON017, "+");//Not allowed character '&{0}'
				wasError = true;
				pos = getIndex();
			} else {
				pos = getIndex();
				minus = isChar('-');
			}
			int firstDigit =  getIndex() - pos; // offset of first digit
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
				String s = getBufferPart(pos, getIndex());
				if (s.charAt(firstDigit) == '0' && s.length() > 1 &&
					Character.isDigit(s.charAt(firstDigit + 1))) {
						error(JSON.JSON014); // Illegal leading zero in number
				}
				if (wasError) {
					return returnValue(0);
				}
				if (_xonMode) {
					try {
						switch(ch = isOneOfChars("FDd")) {
							case 'F':
								return returnValue(Float.parseFloat(s));
							case 'D':
								return returnValue(Double.parseDouble(s));
							case 'd':
								return returnValue(new BigDecimal(s));
						}
					} catch (Exception ex) {
						//Illegal number value &{0}{ for XON type "}{"}: &{1}
						error(JSON.JSON023, ch, s);
					}
					if (!isfloat) {
						try {
							switch(ch = isOneOfChars("NLISB")) {
								case 'N':
									return returnValue(new BigInteger(s));
								case 'L':
									return returnValue(Long.parseLong(s));
								case 'I':
									return returnValue(Integer.parseInt(s));
								case 'S':
									return returnValue(Short.parseShort(s));
								case 'B':
									return returnValue(Byte.parseByte(s));
							}
						} catch (Exception ex) {
							//Illegal number value &{0}{ for XON type "}{"}:&{1}
							error(JSON.JSON023, ch, s);
						}
					}
				}
				try {
					if (isfloat) {
						return returnValue(new BigDecimal(s));
					} else {
						try {
							return returnValue(Long.parseLong(s));
						} catch (Exception exx) {
							return returnValue(new BigInteger(s));
						}
					}
				} catch (Exception ex) {
					//Illegal number value &{0}{ for XON type "}{"}:&{1}
					error(JSON.JSON023, null, s);
					return returnValue(0);
				}
			}
			setIndex(pos);
			//JSON value expected
			return returnError(null, JSON.JSON010, "[]{}");
		}
	}

	/** Parse JSON or XON source data (depends on the flag "_xon").
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs,
	 */
	public final Object parse() throws SRuntimeException {
		isSpacesOrComments();
		Object result = readValue();
		isSpacesOrComments();
		_sPosition = getPosition();
		if (!eos()) {
			error(JSON.JSON008, genPosMod()); //Text after JSON not allowed
		}
		return result;
	}
}