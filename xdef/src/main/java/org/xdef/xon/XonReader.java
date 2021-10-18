package org.xdef.xon;

import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.xdef.impl.code.DefEmailAddr;
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
public class XonReader extends StringParser implements XonParsers {
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
	 * @param source String with source data.
	 */
	public XonReader(final String source, XonParser jp) {
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

	/** Create instance of parser.
	 * @param jp parser of XON source.
	 * @param source URL with source data.
	 */
	public XonReader(final URL source, XonParser jp) {
		super(source, new ArrayReporter(), 0);
		_jp = jp;
	}

	/** Set mode that JSON is parsed in X-definition compiler. */
	public final void setXdefMode() {_xonMode = _acceptComments = _jdef = true;}

	/** Set mode that XON is parsed. */
	public final void setXonMode() {
		_jdef = false;
		_acceptComments = _xonMode = true;
	}

	/** Set mode for strict JSON parsing (JSON, no comments). */
	public final void setJsonMode() {_acceptComments=_xonMode=_jdef=false;}

	/** Set mode for JSON parsing (with comments). */
	public final void setCommentsMode() {_acceptComments = true;}

	/** Skip white space separators (and comments if accepted).
	 * @return true if a space or comment was found.
	 */
	public final boolean isSpacesOrComments() {
		boolean result = isSpaces();
		boolean wasLineComment;
		StringBuilder sb = null;
		SPosition spos = null;
		while((wasLineComment = isChar('#')) || isToken("/*") ) {
			result = true;
			if (sb == null) {
				spos = getPosition();
				sb = new StringBuilder();
			}
			if (!_acceptComments) { // omments not allowed
				warning(JSON.JSON019);  //Comments are not allowed here
			}
			if (wasLineComment) {
				while (!isNewLine() && !eos()) {
					sb.append(nextChar());
				}
			} else {
				while (!isToken("*/") && !eos()) {
					sb.append(nextChar());
				}
				if (eos()) {
					error(JSON.JSON015); //Unclosed comment
					break;
				}
			}
			isSpaces();
		}
		if (sb != null) {
			_jp.comment(new SBuffer(sb.toString(), spos));
		}
		return result;
	}

	/** Read JSON/XON map.
	 * @throws SRuntimeException is an error occurs.
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
				&& (i = isOneOfTokens(XonNames.SCRIPT_NAME,
					XonNames.ONEOF_NAME)) >= 0) {
				SBuffer name = new SBuffer(
					i==0 ? XonNames.SCRIPT_NAME : XonNames.ONEOF_NAME,
					spos);
				wasScript = true;
				isSpacesOrComments();
				SBuffer value = null;
				if (i == 1) { // oneOf
					if (isChar(':')) {
						isSpacesOrComments();
						spos = getPosition();
						XonTools.JValue jv = readSimpleValue();
						if (jv.getValue() instanceof String) {
							value = jv.getSBuffer();
						} else {
							//Value of $script must be string with X-script
							error(JSON.JSON018);
						}
					}
					_jp.xdScript(name, value);
				} else {  // xscript
					if (!isChar(':') && i != 1) {
						//"&{0}"&{1}{ or "}{"} expected
						error(JSON.JSON002, ":");
					}
					isSpacesOrComments();
					spos = getPosition();
					Object o = readSimpleValue();
					if (o != null && o instanceof XonTools.JValue) {
						_jp.xdScript(name, ((XonTools.JValue)o).getSBuffer());
					} else {
						//Value of $script must be string with X-script
						error(JSON.JSON018);
					}
				}
			} else {
				SBuffer name;
				spos = getPosition();
				char separator;
				if (isChar('"')) {
					name = new SBuffer(XonTools.readJString(this), spos);
					separator = ':';
				} else if (_xonMode && isXMLName(StringParser.XMLVER1_0)) {
					name = new SBuffer(getParsedString(), spos);
					separator = '=';
				} else {
					fatal(JSON.JSON004); //Name of item expected
					_jp.mapEnd(this);
					return;
				}
				isSpacesOrComments();
				if (!isChar(separator)) {
					//"&{0}"&{1}{ or "}{"} expected
					error(JSON.JSON002, String.valueOf(separator));
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
				//"&{0}"&{1}{ or "}{"} expected
				error(JSON.JSON002, ",", "}");
				if (getCurrentChar() != '"') {
					break;
				}
			}
		}
		//"&{0}"&{1}{ or "}{"} expected&{#SYS000}
		fatal(JSON.JSON002, "}");
		if (findOneOfChars("[]{}") == NOCHAR) {// skip to next item
			setEos();
		}
		_jp.mapEnd(this);
	}

	/** Read JSON/XON array.
	 * @throws SRuntimeException is an error occurs.
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
				&& (i = isOneOfTokens(XonNames.SCRIPT_NAME,
					XonNames.ONEOF_NAME))>=0) {
				SBuffer name = new SBuffer(
					i==0 ? XonNames.SCRIPT_NAME : XonNames.ONEOF_NAME,
					spos);
				wasScript = true;
				SBuffer value = null;
				if (isChar(':')) {
					isSpacesOrComments();
					XonTools.JValue jv = readSimpleValue();
					if (jv.getValue() instanceof String) {
						value = new SBuffer((String) jv.getValue(),
								jv.getPosition());
					} else {
						//Value of $script must be string with X-script
						error(JSON.JSON018);
					}
				} else {
					if (i == 0) { //JsonNames.SCRIPT_NAME
						//"&{0}"&{1}{ or "}{"} expected
					   error(JSON.JSON002, ":");
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

	/** Read JSON/XON simple simpleValue.
	 * @return parsed simpleValue: String, Number, Boolean or null
	 * (or XON object).
	 * @throws SRuntimeException is an error occurs.
	 */
	private XonTools.JValue readSimpleValue() throws SRuntimeException {
		SPosition spos = getPosition();
		int i;
		if (isChar('"')) { // string
			return returnValue(spos, XonTools.readJString(this));
		} else if ((i=isOneOfTokens(new String[]{"null","false","true"}))>=0) {
			return returnValue(spos, i > 0 ? (i==2) : null);
		} else {
			int pos = getIndex();
			boolean wasError = false;
			Object result = null;
			char ch;
			if (_xonMode&&(i=isOneOfTokens(new String[]{"c\"","u\"","e\"","b(",
				"D","p(","g(","P","-P","NaN","INF","-INF","0d","-0d","0i","-0i"
				})) >= 0) {
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
						//JSON value expected
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
					case 4:  // 'D' datetime
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
					case 7:
					case 8:  // 'D' datetime
						setIndex(pos);
						if (isXMLDuration()) {
							return returnValue(spos, getParsedSDuration());
						}
						break;
					case 9:  // "NaN"
					case 10:  // "INF"
					case 11:  // "-INF"
					if (isChar('F')) {
						return returnValue(spos, i == 9 ? Float.NaN
							: i == 10 ? Float.POSITIVE_INFINITY
								: Float.NEGATIVE_INFINITY);
					}
					isChar('D');
					return returnValue(spos, i == 0 ? Double.NaN
						: i == 1 ? Double.POSITIVE_INFINITY
							: Double.NEGATIVE_INFINITY);
					case 12:  // "0d"number
					case 13:  // "-0d"number
						String s = i==13 ? "-" : "";
						i = getIndex();
						if (isFloat() || isInteger()) {
							s += getBufferPart(i, getIndex());
							BigDecimal d = new BigDecimal(s);
							if (s.indexOf('e') > 0 || s.indexOf('E') > 0) {
								// value without exponent specification
								d = new BigDecimal(d.toPlainString());
							}
							return returnValue(spos, d);
						}
						break;
					case 14:  // "0d"number
					case 15:  // "-0d"number
						s = i==15 ? "-" : "";
						i = getIndex();
						if (isInteger()) {
							return returnValue(spos, new BigInteger(
								s + getBufferPart(i, getIndex())));
						}
				}
				setIndex(pos);
				//JSON simpleValue expected
				return returnError(spos, null, JSON.JSON010, "',[]{}");
			}
			// number
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
						switch(ch = isOneOfChars("FD")) {
							case 'F':
								return returnValue(spos, Float.parseFloat(s));
							case 'D':
								return returnValue(spos, Double.parseDouble(s));
						}
					} catch (Exception ex) {
						//Illegal number simpleValue &{0}
						//{ for XON type "}{"}: &{1}
						error(JSON.JSON023, ch, s);
					}
					if (!isfloat) {
						try {
							switch(ch = isOneOfChars("LISB")) {
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
							//Illegal number simpleValue &{0}
							//{ for XON type "}{"}:&{1}
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
			setIndex(pos); // error
			//JSON simpleValue expected
			return returnError(spos, null, JSON.JSON010, "[]{}");
		}
	}

	/** Read JSON/XON item.
	 * @throws SRuntimeException if an error occurs.
	 */
	private void readItem() throws SRuntimeException {
		if (eos()) {
			fatal(JSON.JSON007); //unexpected eof
		} else if (isChar('{')) { // Map
			readMap();
		} else if (isChar('[')) {
			readArray();
		} else if (_jdef && isToken(XonNames.ANY_NAME)) {
			SPosition spos = getPosition(); // xdef $ANY
			spos.setIndex(getIndex() - XonNames.ANY_NAME.length());
			SBuffer name = new SBuffer(XonNames.ANY_NAME, spos);
			SBuffer val = new SBuffer(XonNames.ANY_NAME, spos);
			isSpacesOrComments();
			if (isOneOfChars(":=") != NOCHAR) {
				isSpacesOrComments();
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
				jv = new XonTools.JValue(jv.getPosition(), "" + jv.getValue());
			}
			String name = _jp.putValue(jv);
			if (name != null) {
				error(JSON.JSON022, name); //Value pair &{0} already exists
			}
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
			error(JSON.JSON008);//Text after JSON not allowed
		}
	}

	public final static Object parseJSON(Reader in, String sysId) {
		ObjParser jp = new ObjParser();
		XonReader xr = new XonReader(in, jp);
		xr._acceptComments = true;
		xr._xonMode = true;
		if (sysId != null) {
			xr.setSysId(sysId);
		}
		xr.parse();
		xr.isSpacesOrComments();
		if (!xr.eos()) {
			xr.error(JSON.JSON008);//Text after JSON not allowed
		}
		xr.getReportWriter().checkAndThrowErrorWarnings();
		return jp.getResult();
	}

	public final static Object parseXON(Reader in, String sysId) {
		ObjParser jp = new ObjParser();
		XonReader xr = new XonReader(in, jp);
		xr._acceptComments = true;
		xr._xonMode = true;
		if (sysId != null) {
			xr.setSysId(sysId);
		}
		xr.parse();
		xr.isSpacesOrComments();
		if (!xr.eos()) {
			xr.error(JSON.JSON008);//Text after JSON not allowed
		}
		xr.getReportWriter().checkAndThrowErrorWarnings();
		return jp.getResult();
	}

////////////////////////////////////////////////////////////////////////////////
	/** Implementation of JParser for creating XON/JSON object from source. */
	public static class ObjParser implements XonParser {

		private final Stack<Integer> _kinds = new Stack<Integer>();
		private final Stack<List<Object>> _arrays = new Stack<List<Object>>();
		private final Stack<Map<String, Object>> _maps =
			new Stack<Map<String, Object>>();
		private int _kind; // 0..value, 1..array, 2..map
		private final Stack<String> _names = new Stack<String>();
		private Object _value;

		public ObjParser() { _kinds.push(_kind = 0); }

////////////////////////////////////////////////////////////////////////////////
// JParser interface
////////////////////////////////////////////////////////////////////////////////
		@Override
		/** Put value to result.
		 * @param value X_Value to be added to result object.
		 * @return null or name of pair if value pair already exists in
		 * the currently processed map.
		 */
		public String putValue(XonTools.JValue value) {
			if (_kind == 1) {
				_arrays.peek().add(value.getValue());
			} else if (_kind == 2) {
				String name = _names.pop();
				if (_maps.peek().put(name, value.getValue()) != null) {
					return name;
				}
			} else {
				_value = value.getValue();
			}
			return null;
		}
		@Override
		/** Set name of value pair.
		 * @param name value name.
		 */
		public void namedValue(SBuffer name) {_names.push(name.getString());}
		@Override
		/** Array started.
		 * @param pos source position.
		 */
		public void arrayStart(SPosition pos) {
			_kinds.push(_kind = 1);
			_arrays.push(new ArrayList<Object>());
		}
		@Override
		/** Array ended.
		 * @param pos source position.
		 */
		public void arrayEnd(SPosition pos) {
			_kinds.pop();
			_kind = _kinds.peek();
			_value = _arrays.peek();
			_arrays.pop();
			if (_kind == 2) {
				_maps.peek().put(_names.pop(), _value);
			} else if (_kind == 1) {
				_arrays.peek().add(_value);
			}
		}

		@Override
		/** Map started.
		 * @param pos source position.
		 */
		public void mapStart(SPosition pos) {
			_kinds.push(_kind = 2);
			_maps.push(new LinkedHashMap<String, Object>());
		}
		@Override
		/** Map ended.
		 * @param pos source position.
		 */
		public void mapEnd(SPosition pos) {
			_kinds.pop();
			_kind = _kinds.peek();
			_value = _maps.peek();
			_maps.pop();
			if (_kind == 2) {
				_maps.peek().put(_names.pop(), _value);
			} else if (_kind == 1) {
				_arrays.peek().add(_value);
			}
		}
		@Override
		/** Processed comment.
		 * @param value SBuffer with the value of comment.
		 */
		public void comment(SBuffer value){/*we ingore it here*/}
		@Override
		/** X-script item parsed, not used methods for JSON/XON parsing
		 * (used in X-definition compiler).
		 * @param name name of item.
		 * @param value value of item.
		 */
		public void xdScript(SBuffer name, SBuffer value) {}
		@Override
		/** Get result of parser.
		 * @return parsed object.
		 */
		public final Object getResult() {return _value;}
	}
}