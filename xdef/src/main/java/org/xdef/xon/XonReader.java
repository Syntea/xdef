package org.xdef.xon;

import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.util.Currency;
import org.xdef.impl.code.DefEmailAddr;
import org.xdef.impl.code.DefTelephone;
import org.xdef.msg.JSON;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.Price;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SException;
import org.xdef.sys.SParser;
import static org.xdef.sys.SParser.NOCHAR;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import static org.xdef.xon.XonNames.ANY_NAME;
import static org.xdef.xon.XonNames.ANY_OBJECT;
import static org.xdef.xon.XonNames.ONEOF_CMD;
import static org.xdef.xon.XonNames.SCRIPT_CMD;

/** Methods for JSON/XON data.
 * @author Vaclav Trojan
 */
public final class XonReader extends StringParser implements XonParsers {
	private static final String[] XDEF_NAMES =
		new String[]{SCRIPT_CMD, ONEOF_CMD, ANY_OBJECT};
	/** Flag to accept comments (default false; true=accept comments). */
	private boolean _acceptComments;
	/** Flag if parse JSON or XON (default false; false=JSON, true=XON). */
	private boolean _xonMode;
	/** Flag if the parsed data are in X-definition (default false). */
	private boolean _jdef;
	/** Parser of XON source. */
	private final XonParser _jp;

	/** Create instance of parser.
	 * @param jp parser of XON source.
	 */
	XonReader(XonParser jp) {_jp = jp;}

	/** Create instance of parser.
	 * @param jp parser of XON source.
	 * @param source String with source data.
	 */
	public XonReader(final SBuffer source, XonParser jp) {
		super(source);
		_jp = jp;
	}

	/** Create instance of parser.
	 * @param jp parser of XON source.
	 * @param source Reader with source data.
	 */
	public XonReader(final Reader source, XonParser jp) {
		super(source, new ArrayReporter());
		_jp = jp;
	}

	/** Set mode for XON/JSON parsing (with comments). */
	public final void setCommentsMode() {_acceptComments = true;}

	/** Skip white spaces (and comments if accepted). */
	public final void skipSpacesOrComments() {
		StringBuilder sb = null;
		SPosition spos = null;
		for (;;) {
			isSpaces();
			boolean wasLineComment;
			if ((wasLineComment = isChar('#')) || isToken("/*")) {
				if (sb == null) {
					sb = new StringBuilder();
					spos = getPosition();
					if (!_acceptComments) { // omments not allowed
						lightError(JSON.JSON019);//Comments are not allowed here
					}
				}
				if (wasLineComment) {
					while (!isNewLine() && !eos()) {
						sb.append(nextChar());
					}
				} else {
					while (!isToken("*/") && !eos()) {
						if (eos()) {
							error(JSON.JSON015); //Unclosed comment
							return;
						}
						sb.append(nextChar());
					}
				}
			} else {
				if (sb != null) {
					_jp.comment(new SBuffer(sb.toString(), spos));
				}
				return;
			}
		}
	}

	/** Read XON/JSON map.
	 * @throws SRuntimeException is an error occurs.
	 */
	private void readMap() throws SRuntimeException {
		_jp.mapStart(getPosition());
		skipSpacesOrComments();
		SPosition spos = getPosition();
		if (isChar('}')) { // empty map
			_jp.mapEnd(spos);
			return;
		}
		boolean wasScript = false;
		boolean wasAnyName = false;
		int i;
		while(!eos()) {
			if (_jdef && !wasScript && (i = isOneOfTokens(XDEF_NAMES)) >= 0) {
				SBuffer name = new SBuffer(XDEF_NAMES[i], spos);
				wasScript = true;
				skipSpacesOrComments();
				SBuffer value = null;
				if (i >= 1) { // $:oneOf or $:anyObj
					if (isChar('=')) {
						skipSpacesOrComments();
						spos = getPosition();
						XonTools.JValue jv = readSimpleValue();
						if (jv.getValue() instanceof String) {
							value = jv.getSBuffer();
						} else {
							//Value must be string with X-script
							error(JSON.JSON018);
						}
					}
					_jp.xdScript(name, value);
				} else {  // $:script
					if (!isChar('=')) {
						error(JSON.JSON002, "=");//"&{0}"&{1}{ or "}{"} expected
					}
					skipSpacesOrComments();
					spos = getPosition();
					XonTools.JValue jv = readSimpleValue();
					if (jv != null && jv.getValue() instanceof String) {
						_jp.xdScript(name, jv.getSBuffer());
					} else {
						error(JSON.JSON018);//Value must be string with X-script
					}
				}
			} else {
				SBuffer name;
				spos = getPosition();
				if (_jdef && isToken(ANY_NAME)) {
					if (wasAnyName) {
						//Value pair &{0} already exists
						error(JSON.JSON022, new SBuffer(ANY_NAME, spos));
					}
					wasAnyName = true;
					skipSpacesOrComments();
					_jp.xdScript(new SBuffer(ANY_NAME, spos), null);
					name = null;
				} else {
					if (isChar('"')) {
						name = new SBuffer(XonTools.readJString(this), spos);
					} else if (_xonMode && isNCName(StringParser.XMLVER1_0)) {
						name = new SBuffer(getParsedString(), spos);
					} else {
						error(JSON.JSON004); //Name of item expected
						_jp.mapEnd(this);
						setEos();
						return;
					}
				}
				skipSpacesOrComments();
				if (!isChar(':')) {
					error(JSON.JSON002, ":"); //"&{0}"&{1}{ or "}{"} expected
				}
				if (name != null) {
					if (_jp.namedValue(name)) {
						//Value pair &{0} already exists
						error(JSON.JSON022, name);
					}
				}
				readItem();
			}
			skipSpacesOrComments();
			if (isChar('}')) {
				skipSpacesOrComments();
				_jp.mapEnd(this);
				return;
			}
			if (isChar(',') || _xonMode) {
				spos = getPosition();
				skipSpacesOrComments();
				if (isChar('}')) {
					if (!_xonMode) {
						SPosition spos1 = getPosition();
						setPosition(spos);
						error(JSON.JSON020); //redundant comma
						setPosition(spos1);
					}
					_jp.mapEnd(this);
					return;
				}
			} else {
				if (eos()) {
					break;
				}
				error(JSON.JSON002, ",", "}");//"&{0}"&{1}{ or "}{"} expected
				if (getCurrentChar() != '"') {
					break;
				}
			}
		}
		fatal(JSON.JSON002, "}");//"&{0}"&{1}{ or "}{"} expected&{#SYS000}
		if (findOneOfChars("[]{}") == NOCHAR) {// skip to next item
			setEos();
		}
		_jp.mapEnd(this);
	}

	/** Read JSON/XON array.
	 * @throws SRuntimeException is an error occurs.
	 */
	private void readArray() throws SRuntimeException {
		skipSpacesOrComments();
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
			if (!wasScript &&_jdef && (i = isOneOfTokens(XDEF_NAMES))>=0) {
				SBuffer name = new SBuffer(XDEF_NAMES[i], spos);
				wasScript = true;
				SBuffer value = null;
				skipSpacesOrComments();
				if (isChar('=')) {
					skipSpacesOrComments();
					XonTools.JValue jv = readSimpleValue();
					if (jv.getValue() instanceof String) {
						value = new SBuffer(
							(String) jv.getValue(), jv.getPosition());
					} else {
						error(JSON.JSON018);//Value must be string with X-script
					}
				} else {
					if (i == 0) { //JsonNames.SCRIPT_CMD
					   error(JSON.JSON002, "=");//"&{0}"&{1}{ or "}{"} expected
					}
				}
				_jp.xdScript(name, value);
			} else {
				readItem();
			}
			skipSpacesOrComments();
			if (isChar(']')) {
				_jp.arrayEnd(this);
				return;
			}
			if (isChar(',')) {
				spos = getPosition();
				skipSpacesOrComments();
				if (isChar(']')) {
					if (!_xonMode) {
						SPosition spos1 = getPosition();
						setPosition(spos);
						error(JSON.JSON020); //redundant comma
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
				error(JSON.JSON002,",","]");
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
		_jp.arrayEnd(this);
	}

	/** Returns parsed simpleValue.
	 * @param x parsed simpleValue to be returned.
	 * @return parsed simpleValue. If the switch _genJObjects is true, then
	 * the parsed simpleValue contains source position.
	 */
	private XonTools.JValue returnValue(SPosition spos, final Object x) {
		return new XonTools.JValue(spos, x);
	}

	/** Returns error and parsed simpleValue.
	 * @param x parsed simpleValue to be returned.
	 * @param code currencyCode of error message.
	 * @param skipChars string with characters to which source will be skipped.
	 * @param params list od error message parameters (may be empty.)
	 * @return parsed simpleValue. If the switch _genJObjects is true, then
	 * the parsed simpleValue contains source position.
	 */
	private XonTools.JValue returnError(SPosition spos,
		final Object x,
		final long code,
		final String skipChars,
		final Object... params) {
		error(code, params);
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

	/** Read XON/JSON simple simpleValue.
	 * @return parsed simpleValue: String, Number, Boolean or null
	 * (or XON object).
	 * @throws SRuntimeException is an error occurs.
	 */
	private XonTools.JValue readSimpleValue() throws SRuntimeException {
		SPosition spos = getPosition();
		int i;
		boolean minus, floatNumber;
		int pos = getIndex();
		if (isChar('"')) { // string
			String s = XonTools.readJString(this);
			if (!_xonMode) {
				return returnValue(spos, s);
			} else {
				char ch;
				if ((ch=isOneOfChars("cuebdpgiCtP"))== NOCHAR) {
					return returnValue(spos, s);
				}
				switch(ch) {
					case 'c': // character
						return returnValue(spos, s.charAt(0));
					case 'u': // URI
						try {
							return returnValue(spos, new URI(s));
						} catch (Exception ex) {}
						setIndex(pos);
						break;
					case 'e':
						try {
							return returnValue(spos, new DefEmailAddr(s));
						} catch (Exception ex) {}
						break;
					case 'b':
						try {
							return returnValue(spos, SUtils.decodeBase64(s));
						} catch (SException ex) {}
						break;
					case 'd':
						try {
							return returnValue(spos, SDatetime.parse(s,
								"yyyy-MM-dd['T'HH:mm:ss[.S]][Z]" +
								"|HH:mm:ss[.S][Z]"+ //time
								"|--MM[-dd][Z]" + //month day
								"|---dd[Z]"+ //day
								"|yyyy-MM[Z]"+ // year month
								"|yyyy[Z]")); // year
						} catch (Exception ex) {}
						break;
					case 'p':
						try {
							return returnValue(spos, new Price(s));
						} catch (Exception ex) {}
						break;
					case 'g':
					case 'i':
					case 'C':
					case 't':
					case 'P':
					default:
						return returnValue(spos, s);
				}
			}
		} else if ((i=isOneOfTokens(new String[]{"null","false","true"}))>=0) {
			return returnValue(spos, i > 0 ? (i==2) : null);
		} else if (_xonMode
			&& (i=isOneOfTokens(new String[]{"NaN","INF","-INF"})) >= 0) {
			if (isChar('f')) {
				switch(i) {
					case 0: return returnValue(spos, Float.NaN);
					case 1: return returnValue(spos, Float.POSITIVE_INFINITY);
					default: return returnValue(spos, Float.NEGATIVE_INFINITY);
				}
			} else {
				switch(i) {
					case 0: return returnValue(spos, Double.NaN);
					case 1: return returnValue(spos, Double.POSITIVE_INFINITY);
					default: return returnValue(spos, Double.NEGATIVE_INFINITY);
				}
			}
		} else if ((minus=isChar('-')) && ((floatNumber=isFloat())||isInteger())
			|| ((floatNumber=isFloat()) || isInteger())) {
			String s = getBufferPart(minus ? pos + 1 : pos, getIndex());
			if (s.charAt(0) == '0' && s.length() > 1 &&
				Character.isDigit(s.charAt(1))) {
					error(JSON.JSON014); // Illegal leading zero in number
			}
			if (minus) {
				s = '-' + s;
			}
			if (_xonMode) {
				char ch;
				if (floatNumber) {
					switch(ch = isOneOfChars("fDd")) {
						case 'f':
							return returnValue(spos, Float.parseFloat(s));
						case 'd':
							return returnValue(spos, Double.parseDouble(s));
						case 'D':
							return returnValue(spos, new BigDecimal(s));
						default:
							return returnValue(spos, Double.parseDouble(s));
					}
				} else {
					switch(ch = isOneOfChars("lisbNfDd")) {
						case 'l':
							return returnValue(spos, Long.parseLong(s));
						case 'i':
							return returnValue(spos, Integer.parseInt(s));
						case 's':
							return returnValue(spos, Short.parseShort(s));
						case 'b':
							return returnValue(spos, Byte.parseByte(s));
						case 'N':
							return returnValue(spos, new BigInteger(s));
						case 'f':
							return returnValue(spos, Float.parseFloat(s));
						case 'D':
							return returnValue(spos, new BigDecimal(s));
						case 'd':
							return returnValue(spos, Double.parseDouble(s));
						default:
						try {
							return returnValue(spos, Long.parseLong(s));
						} catch (Exception ex) {
							try {
								return returnValue(spos, new BigInteger(s));
							} catch (Exception exx) {}
						}
					}
				}
			} else {
				if (floatNumber) {
					return returnValue(spos, Float.parseFloat(s));
				} else {
					try {
						return returnValue(spos, Long.parseLong(s));
					} catch (Exception exx) {
						return returnValue(spos, new BigInteger(s));
					}
				}
			}
			//Illegal number simpleValue &{0}{ for XON type "}{"}:&{1}
			error(JSON.JSON023, null, s);
			return returnValue(spos, 0);
		} else {
			Object result;
			char ch;
			if (_xonMode&&(i=isOneOfTokens(new String[]{"c\"","u\"","e\"","b(",
				"d","p(","g(","/","C(","t\"","P","-P","NaN","INF","-INF"}))>=0){
				switch(i) {
					case 0: // character
						i = XonTools.readJChar(this);
						if (i != -1) {
							ch = (char) i;
							if (isChar('"')) {
								return returnValue(spos, ch);
							}
						}
						break;
					case 1: // URI
						try {
							return returnValue(spos,
								new URI(XonTools.readJString(this)));
						} catch (Exception ex) {}
						setIndex(pos);
						//XON/JSON value expected
						return returnError(spos, null, JSON.JSON010, "[]{}");
					case 2:  // Email address
						try {
							return returnValue(spos, new DefEmailAddr(
								XonTools.readJString(this)));
						} catch (Exception ex) {}
						break;
					case 3: // base64 (byte array)
						try {
							result = SUtils.decodeBase64(this);
							if (isChar(')')) {
								return returnValue(spos, result);
							}
						} catch (SException ex) {}
						break;
					case 4:  // 'd' datetime
						if (isDatetime("yyyy-MM-dd['T'HH:mm:ss[.S]][Z]" +
							"|HH:mm:ss[.S][Z]"+ //time
							"|--MM[-dd][Z]" + //month day
							"|---dd[Z]"+ //day
							"|yyyy-MM[Z]"+ // year month
							"|yyyy[Z]")) { // year
								return returnValue(spos, getParsedSDatetime());
						}
						break;
					case 5: //"p(" - currency ammount
						if (isFloat() || isInteger()) {
							BigDecimal d = new BigDecimal(getParsedString());
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
						break;
					case 6: // "g(" - GPS position
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
										return returnValue(spos,new GPSPosition(
											latitude,longitude,altitude,name));
									} catch(SRuntimeException ex) {
										putReport(ex.getReport()); //invalid GPS
										return returnValue(spos, null);
									}
								}
							}
						}
						break;
					case 7: {// "/" ipAddr
						String s = "";
						while ("0123456789abcdefABCDEF:."
							.indexOf(getCurrentChar()) >= 0) {
							s += peekChar();
						}
						try {
							return returnValue(spos, InetAddress.getByName(s));
						} catch(Exception ex) {
							//invalid InetAddr
							error(XDEF.XDEF809,	"ipAddr", s);
							return returnValue(spos, null);
						}
					}
					case 8: {// "c(" currency
						int pos1 = getIndex();
						while ((ch = peekChar()) > ' ' && ch != ')') {}
						int pos2 = getIndex() -1;
						if (ch == ')' && pos2 > pos1) {
							String s = getBufferPart(pos1,pos2);
							Currency curr = Currency.getInstance(s);
							if (curr != null) {
								return returnValue(spos, curr);
							}
							//invalid currency
							error(XDEF.XDEF809,	"currency",	s);
							return returnValue(spos, null);
						}
						break;
					}
					case 9: {// "t telephone
						int pos1 = getIndex();
						while ((ch = peekChar()) >= ' ' && ch != '"') {}
						int pos2 = getIndex() -1;
						String s = getBufferPart(pos1,pos2);
						if (ch =='"') {
							return returnValue(spos, new DefTelephone(s));
						}
						//invalid telephone number
						error(XDEF.XDEF809,	"telephone", s);
						return returnValue(spos, null);
					}
					case 10:  // 'P' duration
					case 11:  // '-P' duration
						setIndex(pos);
						if (isXMLDuration()) {
							return returnValue(spos, getParsedSDuration());
						}
						break;
					case 12:  // "NaN"
					case 13:  // "INF"
					case 14:  // "-INF"
					if (isChar('F')) {
						return returnValue(spos, i == 9 ? Float.NaN
							: i == 10 ? Float.POSITIVE_INFINITY
								: Float.NEGATIVE_INFINITY);
					}
					isChar('D');
					return returnValue(spos, i == 0 ? Double.NaN
						: i == 1 ? Double.POSITIVE_INFINITY
							: Double.NEGATIVE_INFINITY);
				}
			}
		}
		setIndex(pos); // error
		//JSON simpleValue expected
		return returnError(spos, null, JSON.JSON010, "[]{}");
	}

	/** Read XON/JSON item.
	 * @throws SRuntimeException if an error occurs.
	 */
	private void readItem() throws SRuntimeException {
		skipSpacesOrComments();
		if (eos()) {
			fatal(JSON.JSON007); //unexpected eof
		} else if (isChar('{')) { // Map
			readMap();
		} else if (isChar('[')) {
			readArray();
		} else if (_jdef && isToken(ANY_OBJECT)) {
			SPosition spos = getPosition(); // xdef $:any
			spos.setIndex(getIndex() - ANY_OBJECT.length());
			SBuffer name = new SBuffer(ANY_OBJECT, spos);
			SBuffer val = new SBuffer(ANY_OBJECT, spos);
			skipSpacesOrComments();
			if (isChar('=')) {
				skipSpacesOrComments();
				XonTools.JValue jv = readSimpleValue();
				if (!(((XonTools.JValue) jv).getValue() instanceof String)) {
					//After ":" in the command $any must follow simpleValue
					error(JSON.JSON021);
				} else {
					val = jv.getSBuffer();
				}
			}
			_jp.xdScript(name, val);
		} else {
			XonTools.JValue jv = readSimpleValue();
			if (_jdef && (jv == null || jv.getValue() == null
				|| !(jv.getValue() instanceof String))) {
				//Value in X-definition must be a string with X-script
				error(JSON.JSON018);
				Object val = jv.getValue();
				jv = new XonTools.JValue(jv.getPosition(),
					val == null ? "null" : val.toString());
			}
			_jp.putValue(jv);
		}
	}

////////////////////////////////////////////////////////////////////////////////
// interface XONParsers
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Set mode that XON/JSON is parsed in X-definition compiler. */
	public final void setXdefMode() {_xonMode = _acceptComments = _jdef = true;}

	@Override
	/** Set mode that XON is parsed. */
	public final void setXonMode() {
		_jdef = false;
		_acceptComments = _xonMode = true;
	}

	@Override
	/** Set mode for strict JSON parsing (JSON, no comments). */
	public final void setJsonMode() {_acceptComments=_xonMode=_jdef=false;}

	@Override
	/** Parse XON/JSON source data (depends on the flag "_xon").
	 * @throws SRuntimeException if an error occurs,
	 */
	public final void parse() throws SRuntimeException {
		readItem();
		skipSpacesOrComments();
		if (!eos()) {
			error(JSON.JSON008);//Text after JSON not allowed
		}
	}

////////////////////////////////////////////////////////////////////////////////

	/** Parse XON/JSON source data.
	 * @param in Reader with XON/JSON source data.
	 * @param sysId System ID of source position or null.
	 * @param xonMode if true then XON, if false JSON.
	 * @return parsed XON or JSON object.
	 */
	private static Object parseXonJson(final Reader in,
		final String sysId,
		final boolean xonMode) {
		XonObjParser jp = new XonObjParser();
		XonReader xr = new XonReader(in, jp);
		xr._acceptComments = xonMode;
		xr._xonMode = xonMode; // XON/JSON mode
		if (sysId != null) {
			xr.setSysId(sysId);
		}
		xr.parse();
		xr.skipSpacesOrComments();
		if (!xr.eos()) {
			xr.error(JSON.JSON008);//Text after JSON not allowed
		}
		xr.getReportWriter().checkAndThrowErrorWarnings();
		return jp.getResult();
	}

	/** Parse XON source data.
	 * @param in Reader with XON source data.
	 * @param sysId System ID of source position or null.
	 * @return parsed XON object.
	 */
	public final static Object parseXON(final Reader in, final String sysId) {
		return parseXonJson(in, sysId, true);
	}

	/** Parse JSON source data.
	 * @param in Reader with JSON source data.
	 * @param sysId System ID of source position or null.
	 * @return parsed JSON object.
	 */
	public final static Object parseJSON(Reader in, String sysId) {
		return parseXonJson(in, sysId, false);
	}
}
