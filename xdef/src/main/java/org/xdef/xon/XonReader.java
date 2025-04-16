package org.xdef.xon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Currency;
import org.xdef.impl.code.DefBytes;
import org.xdef.impl.code.DefEmailAddr;
import org.xdef.impl.code.DefTelephone;
import org.xdef.impl.xml.Reader_UCS_4_2143;
import org.xdef.impl.xml.Reader_UCS_4_3412;
import org.xdef.impl.xml.XAbstractInputStream;
import static org.xdef.impl.xml.XAbstractInputStream.bytesToString;
import static org.xdef.impl.xml.XAbstractInputStream.detectBOM;
import org.xdef.msg.JSON;
import org.xdef.msg.SYS;
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
import static org.xdef.xon.XonNames.ANY_OBJ;
import static org.xdef.xon.XonNames.SCRIPT_DIRECTIVE;
import static org.xdef.xon.XonNames.ONEOF_DIRECTIVE;
import static org.xdef.impl.xml.XAbstractInputStream.readChar;

/** Methods for JSON/XON data.
 * @author Vaclav Trojan
 */
public final class XonReader extends StringParser implements XonParsers {
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
	 * @param source String with source data.
	 */
	public XonReader(final SBuffer source, XonParser jp) {super(source); _jp = jp;}

	/** Create instance of parser.
	 * @param jp parser of XON source.
	 * @param source Reader with source data.
	 */
	public XonReader(final Reader source, XonParser jp) {super(source, new ArrayReporter()); _jp = jp;}

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

	/** Read a directive.
	 * @return true if a directive was read.
	 */
	private boolean readDirective() {
		if (!_jdef) { // no X-definition model
			return false;
		}
		SPosition spos = getPosition();
		final String[] directives = new String[]{SCRIPT_DIRECTIVE, ONEOF_DIRECTIVE};
		int i = isOneOfTokens(directives);
		if (i < 0) {
			return false;
		}
		SBuffer name = new SBuffer(directives[i], spos);
		skipSpacesOrComments();
		SBuffer value = null;
		if (isChar('=')) {
			skipSpacesOrComments();
			XonTools.JValue jv = readSimpleValue();
			value = jv.getSBuffer();
			if (!(jv.getValue() instanceof String)) {
				error(JSON.JSON018); //Value must be string with X-script
			}
		} else if (i == 0) { // $script
			error(JSON.JSON002, "=");//"&{0}"&{1}{ or "}{"} expected
			value = new SBuffer("", getPosition());
		}
		_jp.xdScript(name, value);
		return true;
	}

	/** Read XON/JSON map. */
	private void readMap() {
		_jp.mapStart(getPosition());
		skipSpacesOrComments();
		SPosition spos = getPosition();
		if (isChar('}')) { // empty map
			_jp.mapEnd(spos);
			return;
		}
		boolean wasItem = false;
		boolean wasAnyName = false;
		while(!eos()) {
			if (wasItem || !readDirective()) {
				wasItem = true;
				SBuffer name;
				spos = getPosition();
				if (_jdef && isToken(ANY_NAME)) {
					if (wasAnyName) {
						error(JSON.JSON022, new SBuffer(ANY_NAME, spos)); //Value pair &{0} already exists
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
						error(JSON.JSON022, name); //Value pair &{0} already exists
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

	/** Read JSON/XON array. */
	private void readArray() {
		skipSpacesOrComments();
		_jp.arrayStart(getPosition());
		if (isChar(']')) { // empty array
			_jp.arrayEnd(getPosition());
			return;
		}
		boolean wasItem = false;
		boolean wasErrorReported = false;
		while(!eos()) {
			if (wasItem || !readDirective()) {
				readItem();
				wasItem = true;
			}
			skipSpacesOrComments();
			if (isChar(']')) {
				_jp.arrayEnd(this);
				return;
			}
			if (isChar(',')) {
				SPosition spos = getPosition();
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
		_jp.arrayEnd(this);
	}

	/** Returns parsed simpleValue.
	 * @param x parsed simpleValue to be returned.
	 * @return parsed simpleValue. If _genJObjects is true the parsed value contains the source position.
	 */
	private XonTools.JValue returnValue(SPosition spos, final Object x) {return new XonTools.JValue(spos, x);}

	/** Returns error and parsed simpleValue.
	 * @param x parsed simpleValue to be returned.
	 * @param code currencyCode of error message.
	 * @param skipChars string with characters to which source will be skipped.
	 * @param params list od error message parameters (may be empty.)
	 * @return parsed simpleValue. If switch _genJObjects is true the simpleValue contains source position.
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

	/** Read name of GPS position.
	 * @return GPS position name.
	 */
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
		} else if ((ch = isLetter()) != SParser.NOCHAR || (isChar('_') && (ch = '_') == '_')) { //not quoted
			sb.append(ch);
			while ((ch = getCurrentChar()) != SParser.NOCHAR && (Character.isLetter(ch)
				|| ch=='-' || ch==' ' || ch=='_' || ch=='.' || ch==',' || ch=='/' || (ch>='0' && ch<='9'))) {
				sb.append(peekChar());
			}
		}
		String result = sb.toString().trim();
		if (result.isEmpty()) {
			throw new SRuntimeException(XDEF.XDEF222, "name: " + result); // Incorrect GPosition &{0}{: }
		}
		return result;
	}

	/** Read XON/JSON simple simpleValue.
	 * @return parsed simpleValue: String, Number, Boolean or null (or XON object).
	 */
	private XonTools.JValue readSimpleValue() {
		SPosition spos = getPosition();
		int i;
		boolean minus, floatNumber;
		int pos = getIndex();
		if (isChar('"')) { // string
			String s = XonTools.readJString(this);
			if (_jdef) { //JSON string in X-definition JSON model
				s = SUtils.modifyString(s, "\\", "\\\\");
				s = SUtils.modifyString(s, "\"", "\\\"");
				return returnValue(spos, s);
			}
			if (!_xonMode) {
				return returnValue(spos, s);
			} else {
				char ch;
				if ((ch=isOneOfChars("cuebxdpgiCtP"))== NOCHAR) {
					return returnValue(spos, s);
				}
				try {
					switch(ch) {
						case 'c': return returnValue(spos, s.charAt(0)); // character
						case 'u': // URI
							try {
								return returnValue(spos, new URI(s));
							} catch (URISyntaxException ex) {}
							setIndex(pos);
							break;
						case 'e': return returnValue(spos, new DefEmailAddr(s));
						case 'b': return returnValue(spos, SUtils.decodeBase64(s));
						case 'x': return returnValue(spos, SUtils.decodeHex(s));
						case 'd':
							return returnValue(spos, SDatetime.parse(s,
								"yyyy-MM-dd['T'HH:mm:ss[.S]][Z]" +
								"|HH:mm:ss[.S][Z]"+ //time
								"|--MM[-dd][Z]" + //month day
								"|---dd[Z]"+ //day
								"|yyyy-MM[Z]"+ // year month
								"|yyyy[Z]")); // year
						case 'p': return returnValue(spos, new Price(s));
						case 'g':
						case 'i':
						case 'C':
						case 't':
						case 'P':
						default: return returnValue(spos, s);
					}
				} catch (SException ex) {}
			}
		} else if ((i=isOneOfTokens(new String[]{"null","false","true"}))>=0) {
			return returnValue(spos, i > 0 ? (i==2) : null);
		} else if ((minus = isChar('-')) && ((floatNumber = isFloat()) || isInteger())
			|| ((floatNumber = isFloat()) || isInteger())) {
			String s = getBufferPart(minus ? pos + 1 : pos, getIndex());
			if (s.charAt(0) == '0' && s.length() > 1 && Character.isDigit(s.charAt(1))) {
				error(JSON.JSON014); // Illegal leading zero in number
			}
			if (minus) {
				s = '-' + s;
			}
			if (_xonMode) {
				if (floatNumber) {
					switch(isOneOfChars("fDd")) {
						case 'f': return returnValue(spos, Float.valueOf(s));
						case 'D': return returnValue(spos, new BigDecimal(s));
						case 'd':
						default: return returnValue(spos, Double.valueOf(s));
					}
				} else {
					switch(isOneOfChars("lisbNfDd")) {
						case 'l': return returnValue(spos, Long.valueOf(s));
						case 'i': return returnValue(spos, Integer.valueOf(s));
						case 's': return returnValue(spos, Short.valueOf(s));
						case 'b': return returnValue(spos, Byte.valueOf(s));
						case 'N': return returnValue(spos, new BigInteger(s));
						case 'f': return returnValue(spos, Float.valueOf(s));
						case 'D': return returnValue(spos, new BigDecimal(s));
						case 'd': return returnValue(spos, Double.valueOf(s));
						default:
							try {
								return returnValue(spos, Long.valueOf(s));
							} catch (NumberFormatException ex) {
								try {
									return returnValue(spos, new BigInteger(s));
								} catch (Exception exx) {}
							}
					}
				}
			} else {
				if (floatNumber) {
					return returnValue(spos, Double.valueOf(s));
				} else {
					try {
						return returnValue(spos, Long.valueOf(s));
					} catch (NumberFormatException exx) {
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
			final String[] tokens = {"c\"","u\"","e\"","b(","x(","d","p(","g(","/","C(","t\"","P","-P"};
			if (_xonMode&&(i=isOneOfTokens(tokens))>=0){
				switch(tokens[i]) {
					case "c\"": // character
						i = XonTools.readJChar(this);
						if (i != -1) {
							ch = (char) i;
							if (isChar('"')) {
								return returnValue(spos, ch);
							}
						}
						break;
					case "u\"": // URI
						try {
							return returnValue(spos, new URI(XonTools.readJString(this)));
						} catch (URISyntaxException ex) {}
						setIndex(pos);
						return returnError(spos, null, JSON.JSON010, "[]{}"); //XON/JSON value expected
					case "e\"": // Email address
						try {
							return returnValue(spos, new DefEmailAddr(XonTools.readJString(this)));
						} catch (Exception ex) {}
						break;
					case "b(": // base64 byte array
						try {
							StringBuilder sb = new StringBuilder();
							while ((ch = peekChar()) != ')' && ch != NOCHAR) {
								if (ch > ' ') {
									sb.append(ch);
								}
							}
							result = new DefBytes(SUtils.decodeBase64(sb.toString()), true);
							if (ch == ')') {
								return returnValue(spos, result);
							}
						} catch (SException ex) {}
						break;
					case "x(": // hexadecimal byte array
						try {
							StringBuilder sb = new StringBuilder();
							while ((ch=isOneOfChars(" \n\r0123456789ABCDEFabcdef")) != NOCHAR) {
								if (ch > ' ') {
									sb.append(ch);
								}
							}
							result = new DefBytes(SUtils.decodeHex(sb.toString()), false);
							if (isChar(')')) {
								return returnValue(spos, result);
							}
						} catch (SException ex) {}
						break;
					case "d": // datetime
						if (isDatetime("yyyy-MM-dd['T'HH:mm:ss[.S]][Z]" +
							"|HH:mm:ss[.S][Z]"+ //time
							"|--MM[-dd][Z]" + //month day
							"|---dd[Z]"+ //day
							"|yyyy-MM[Z]"+ // year month
							"|yyyy[Z]")) { // year
								return returnValue(spos, getParsedSDatetime());
						}
						break;
					case "p(": // price (currency ammount)
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
					case "g(": // GPS position
						if (isSignedFloat() || isSignedInteger()) {
							double latitude = getParsedDouble();
							if (isChar(',')&& (isChar(' ')|| true) && (isSignedFloat()|| isSignedInteger())) {
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
										return returnValue(
											spos, new GPSPosition(latitude, longitude, altitude,name));
									} catch(SRuntimeException ex) {
										putReport(ex.getReport()); //invalid GPS
										return returnValue(spos, null);
									}
								}
							}
						}
						break;
					case "/": { // ipAddr
						String s = "";
						while ("0123456789abcdefABCDEF:.".indexOf(getCurrentChar()) >= 0) {
							s += peekChar();
						}
						try {
							return returnValue(spos, InetAddress.getByName(s));
						} catch(UnknownHostException ex) {
							error(XDEF.XDEF809,	"ipAddr", s); //invalid InetAddr
							return returnValue(spos, null);
						}
					}
					case "C(": { // currency
						int pos1 = getIndex();
						while ((ch = peekChar()) > ' ' && ch != ')') {}
						int pos2 = getIndex() -1;
						if (ch == ')' && pos2 > pos1) {
							String s = getBufferPart(pos1,pos2);
							Currency curr = Currency.getInstance(s);
							if (curr != null) {
								return returnValue(spos, curr);
							}
							error(XDEF.XDEF809,	"currency",	s); //invalid currency
							return returnValue(spos, null);
						}
						break;
					}
					case "t\"": { // telephone
						int pos1 = getIndex();
						while ((ch = peekChar()) >= ' ' && ch != '"') {}
						int pos2 = getIndex() -1;
						String s = getBufferPart(pos1,pos2);
						if (ch =='"') {
							return returnValue(spos, new DefTelephone(s));
						}
						error(XDEF.XDEF809,	"telephone", s); //invalid telephone number
						return returnValue(spos, null);
					}
					case "P":  // 'P' duration
					case "-P":  // '-P' duration
						setIndex(pos);
						if (isXMLDuration()) {
							return returnValue(spos, getParsedSDuration());
						}
				}
			}
		}
		setIndex(pos); // error
		return returnError(spos, null, JSON.JSON010, "[]{}"); //JSON simpleValue expected
	}

	/** Read XON/JSON item. */
	private void readItem() {
		skipSpacesOrComments();
		if (eos()) {
			fatal(JSON.JSON007); //unexpected eof
		} else if (isChar('{')) { // Map
			readMap();
		} else if (isChar('[')) {
			readArray();
		} else if (_jdef && isToken(ANY_OBJ)) {
			SPosition spos = getPosition(); // xdef %anyObj
			spos.setIndex(getIndex() - ANY_OBJ.length());
			SBuffer name = new SBuffer(ANY_OBJ, spos);
			SBuffer val = new SBuffer("", spos);
			skipSpacesOrComments();
			if (isChar('=')) {
				skipSpacesOrComments();
				XonTools.JValue jv = readSimpleValue();
				if (!(((XonTools.JValue) jv).getValue() instanceof String)) {
					error(JSON.JSON021); //After ":" in the command %anyObj must follow simpleValue
				} else {
					val = jv.getSBuffer();
				}
			}
			_jp.xdScript(name, val);
		} else {
			XonTools.JValue jv = readSimpleValue();
			if (_jdef && (jv == null || jv.getValue() == null || !(jv.getValue() instanceof String))) {
				error(JSON.JSON018); //Value in X-definition must be a string with X-script
				if (jv != null) {
					Object val = jv.getValue();
					jv = new XonTools.JValue(jv.getPosition(), val == null ? "null" : val.toString());
				}
			}
			_jp.putValue(jv);
		}
	}
	/** Parse XON/JSON source data.0
	 * @param in Reader with XON/JSON source data.
	 * @param sysId System ID of source position or null.
	 * @param xonMode if true then XON, if false JSON.
	 * @param convertXDBytes flag if XDBytes objects are converted to byte[].
	 * @return parsed XON or JSON object.
	 */
	private static Object parseXonJson(final Reader in,
		final String sysId,
		final boolean xonMode,
		final boolean convertXDBytes) {
		XonObjParser jp = new XonObjParser(convertXDBytes);
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
	 * @param convertXDBytes flag if XDBytes objects are converted to byte[].
	 * @return parsed XON object.
	 */
	public static final Object parseXON(final Reader in, final String sysId, final boolean convertXDBytes) {
		return parseXonJson(in, sysId, true, convertXDBytes);
	}

	/** Parse JSON source data.
	 * @param in Reader with JSON source data.
	 * @param sysId System ID of source position or null.
	 * @param convertXDBytes flag if XDBytes objects are converted to byte[].
	 * @return parsed JSON object.
	 */
	public static final Object parseJSON(final Reader in, final String sysId, final boolean convertXDBytes) {
		return parseXonJson(in, sysId, false, convertXDBytes);
	}

	/** Parse XON source data.
	 * @param in input stream with XON source data.
	 * @param sysId System ID of source position or null.
	 * @param convertBytes flag if XDBytes objects are converted to byte[].
	 * @return parsed XON object.
	 */
	public static final Object parseXON(final InputStream in, final String sysId, final boolean convertBytes){
		return parseXonJson(getXonReader(in),sysId,true, convertBytes);
	}

	/** Parse JSON source data.
	 * @param in Reader with JSON source data.
	 * @param sysId System ID of source position or null.
	 * @param convertBytes flag if XDBytes objects are converted to byte[].
	 * @return parsed JSON object.
	 */
	public static final Object parseJSON(final InputStream in, final String sysId,final boolean convertBytes){
		return parseXonJson(getXonReader(in),sysId,false, convertBytes);
	}

	/** This class reads charset directive and creates Reader if the data
	 * starts with a directive.
	 */
	private static final class XonInputStream extends XAbstractInputStream {
		private final String _encoding; // encoding name.
		private XonInputStream(final InputStream in) throws IOException {
			super(in);
			byte[] buf = new byte[4];
			String encoding = detectBOM(in, buf);
			int len = encoding.charAt(0) - '0'; // number of bytes read
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			if (len > 0) {
				baos.write(buf, 0, len);
			}
			int count = encoding.charAt(1) - '0'; // bytes nead to read next
			encoding = encoding.substring(2);
			if (count > 0 && !"X-ISO-10646-UCS-4-2143".equals(encoding)
				&& !"X-ISO-10646-UCS-4-3412".equals(encoding)) {
				if ("41CP037".equals(encoding)) {
					encoding = "UTF-8"; // never should happen...
				}
				String s = bytesToString(buf, 0, len, encoding);
				int i = -1;
				while (s.length() < XonNames.ENCODING_DIRECTIVE.length()
					&& XonNames.ENCODING_DIRECTIVE.startsWith(s)
					&& (i = readChar(in, encoding, buf, count, baos)) != -1) {
					s += (char) i;
				}
				if (XonNames.ENCODING_DIRECTIVE.equals(s)) {
					while((i = readChar(in,encoding,buf,count,baos)) == ' ' || i == '\t') {} // skip spaces
					if (i == '=') {
						while((i=readChar(in,encoding,buf,count,baos)) == ' ' || i == '\t') {}
					} else { // missing eq sign
						//Incorrect %encoding directive: "&{0}"
						throw new SRuntimeException(JSON.JSON081, baos.toByteArray());
					}
					String enc = "";
					if (i == '"') { // is quote
						i = readChar(in, encoding, buf, count, baos);
						while(i > ' ' && i != '"') { //read encoding name
							enc += (char) i;
							i = readChar(in, encoding, buf, count, baos);
						}
						if (i != '"') { // missing end quote
							//Incorrect %encoding directive: "&{0}"
							throw new SRuntimeException(JSON.JSON081, baos.toByteArray());
						}
					}
					if (enc.isEmpty()) {
						throw new SRuntimeException(JSON.JSON083); //Charset name is missing
					}
					encoding = enc;
				}
			}
			_encoding = encoding;
			setBuffer(baos.toByteArray());
		}
	}

	/** Creates Reader from input stream. If data starts with %encoding directive the reader is created with
	 * the specified encoding. Otherwise, the UTF-8 encoding is used.
	 * @param in input stream wit XON/JSON data.
	 * @return reader with detected encoding.
	 */
	public static final Reader getXonReader(final InputStream in) {
		try {
			XonInputStream x = new XonInputStream(in);
			if ("X-ISO-10646-UCS-4-2143".equals(x._encoding)) {
				return new Reader_UCS_4_2143(x.getInputStream());
			} else if ("X-ISO-10646-UCS-4-3412".equals(x._encoding)) {
				return new Reader_UCS_4_3412(x.getInputStream());
			}
			return new InputStreamReader(x.getInputStream(), Charset.forName(x._encoding));
		} catch (IOException ex) {
			throw new SRuntimeException(SYS.SYS052, ex); //Unsupported encoding name&{0}{: "}{"}
		}
	}

////////////////////////////////////////////////////////////////////////////////
// interface XONParsers
////////////////////////////////////////////////////////////////////////////////

	/** Set mode that XON/JSON is parsed in X-definition compiler. */
	@Override
	public final void setXdefMode() {_xonMode = _acceptComments = _jdef = true;}

	/** Set mode that XON is parsed. */
	@Override
	public final void setXonMode() {_jdef = false; _acceptComments = _xonMode = true;}

	/** Set mode for strict JSON parsing (JSON, no comments). */
	@Override
	public final void setJsonMode() {_acceptComments=_xonMode=_jdef=false;}

	/** Parse XON/JSON source data (depends on the flag "_xon"). */
	@Override
	public final void parse() {
		if (!_jdef && isToken(XonNames.ENCODING_DIRECTIVE)) { //encoding
			int pos1 = getIndex() - XonNames.ENCODING_DIRECTIVE.length();
			while(isOneOfChars(" \t") > 0){}
			boolean wasEq = isChar('=');
			while(isOneOfChars(" \t") > 0){}
			int pos2 = getIndex();
			boolean wasName;
			String enc = "";
			if ((wasName = isChar('"')) && wasEq) {
				while (!(wasName = isChar('"')) && !eos()) {
					enc += getCurrentChar(); // read charset name
					nextChar();
				}
			}
			if (!wasEq || !wasName) { // write error message
				if ((pos2 = getIndex()) - pos1 > 40) {
					pos2 = pos1 + 40; // message would be too long
				}
				//Incorrect specification of the %chars0et directive: "&{0}"
				error(JSON.JSON081, getBufferPart(pos1, pos2));
			} else {
				try {//check charset
					Charset.forName(enc);
				} catch (Exception ex) {
					//Incorrect specification of the %chars0et directive: "&{0}"
					error(JSON.JSON081, getBufferPart(pos2, getIndex()));
				}
			}
		}
		readItem();
		skipSpacesOrComments();
		if (!eos()) {
			error(JSON.JSON008);//Text after JSON not allowed
		}
	}
}