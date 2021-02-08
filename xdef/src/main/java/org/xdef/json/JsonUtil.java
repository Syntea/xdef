package org.xdef.json;

import org.xdef.impl.compile.CompileJsonXdef;
import org.xdef.msg.JSON;
import org.xdef.msg.SYS;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.StringParser;
import org.xdef.xml.KXmlUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.impl.XConstants;
import org.xdef.sys.SParser;
import org.xdef.sys.SUtils;

/** JSON utility (parse JSON source to JSON instance, compare JSON instances,
 * and create string with JSON source from JSON object.
 * @author Vaclav Trojan
 */
public class JsonUtil extends StringParser {

	/** JSON map local name. */
	public final static String J_MAP = "map";
	/** JSON array local name. */
	public final static String J_ARRAY = "array";
	/** JSON any item local name (with JSON value. */
	public final static String J_ITEM = "item";
	/** JSON string item. */
	public final static String J_STRING = "string";
	/** JSON number item. */
	public final static String J_NUMBER = "number";
	/** JSON boolean item. */
	public final static String J_BOOLEAN = "boolean";
	/** JSON null item. */
	public final static String J_NULL = "null";
	/** JSON map key attribute name. */
	public final static String J_KEYATTR = "key";
	/** JSON value attribute name. */
	public final static String J_VALUEATTR = "value";

	/** Flag to accept comments in JSON. */
	private boolean _acceptComments; // default value = false
	/** Flag to generate SPositions when parsing JSON. */
	private boolean _genJObjects; // default value = false
	/** Flag if the parsed data are in X-definition. */
	private boolean _jdef; // default value = false
	/** Position of processed item.`*/
	private SPosition _sPosition;

	/** Create instance of A_util. */
	public JsonUtil() {
		_acceptComments = false;
		_genJObjects = false;
		_jdef = false;
	}
////////////////////////////////////////////////////////////////////////////////
// Common public mtethods
////////////////////////////////////////////////////////////////////////////////

	/** Convert character to representation used in XML names.
	 * @param c character to be converted.
	 * @return string with converted character.
	 */
	public final static String genXmlHexChar(final char c) {
		return "_x" + Integer.toHexString(c) + '_';
	}

	/** Generate XML form of string,
	 * @param val Object with value.
	 * @param isAttr if true the value is generated for attribute, otherwise
	 * it is generated for text node value.
	 * @return XML form of string from the argument val,
	 */
	final static String genSimpleValueToXml(final Object x,
		final boolean isAttr){
		if (x == null) {
			return "null";
		} else if (x instanceof String) {// JSON string to XML form
			String s = (String) x;
			if (s.isEmpty() || "null".equals(s)
				|| "true".equals(s) || "false".equals(s)) {
				return '"' + s + '"';
			}
			boolean addQuot = s.indexOf(' ') >= 0 || s.indexOf('\t') >= 0
				|| s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0
				|| s.indexOf('\f') >= 0 || s.indexOf('\b') >= 0
				|| s.indexOf('\\') >= 0 || s.indexOf('"') >= 0;
			if (!addQuot) {
				char ch = s.charAt(0);
				if (ch == '-' || ch >= '0' && ch <= '9') {
					StringParser p = new StringParser(s);
					if ((p.isSignedFloat() || p.isSignedInteger()) && p.eos()) {
						return '"' + s + '"'; // value is number, must be quoted
					}
				}
			}
			if (addQuot) {
				char ch = s.charAt(0);
				if (isAttr && s.equals(s.trim()) && ch != '"' && ch != '[') {
					// For attributes it is not necessary to add quotes if the data
					// does not contain leading or trailing white spaces,
					return s;
				} else {
					return '"' + jstringToSource(s) + '"';
				}
			} else {
				return s;
			}
		}
		return x.toString();
	}

	/** Generate XML form from JSON string value.
	 * @param val Object with value.
	 * @param mode 0 .. text node, 1.. attribute, 2.. array of simple items
	 * @return XML form of string from the argument val,
	 */
	final static String jstringToXML(final Object val, final int mode) {
		if (val == null) {
			return "null";
		}
		if (val instanceof String) {
			String s = (String) val;
			if (s.isEmpty()) {
				return mode == 1 ? "" : "\"\"";
			}
			if ("true".equals(s) || "false".equals(s) || "null".equals(s)) {
				return '"' + s + '"';
			}
			char ch = s.charAt(0);
			int len = s.length();
			boolean addQuot = mode == 2 || mode == 1 && (ch == '[' || ch == '"')
				|| mode == 0 && (ch <= ' ' || s.charAt(len-1) <= ' ');
			if (!addQuot) {
				int i = 0;
				if (ch == '-' && len > 1) {
					i = 1;
					ch = s.charAt(1);
				}
				if (ch >= '0' && ch <= '9') { //if it is a number => qoute
					if (i + 1 == len) {
						return '"' + s + '"';
					}
					if (ch!='0'||(ch=s.charAt(i+1))=='.'||ch=='E'||ch=='e') {
						// number without redundant leading zeroes
						StringParser p = new StringParser(s);
						if ((p.isFloat() || p.isInteger()) && p.eos()) {
							return '"' + s + '"';
						}
					}
				}
			}
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < len; i++) {
				switch (ch = s.charAt(i)) {
					case '\t':
						if (mode == 1 || addQuot) { // force quote attributes
							if (!addQuot) {
								SUtils.modifyStringBuilder(sb, "\\", "\\\\");
								SUtils.modifyStringBuilder(sb, "\"", "\\\"");
								addQuot = true;
							}
							sb.append("\\t");
						} else {
							sb.append(ch);
						}
						break;
					case '\n':
						if (mode == 1 || addQuot) {  // force quote attributes
							if (!addQuot) {
								SUtils.modifyStringBuilder(sb, "\\", "\\\\");
								SUtils.modifyStringBuilder(sb, "\"", "\\\"");
								addQuot = true;
							}
							sb.append("\\n");
						} else {
							sb.append(ch);
						}
						break;
					case '\r':
						if (!addQuot) { // force quote
							SUtils.modifyStringBuilder(sb, "\\", "\\\\");
							SUtils.modifyStringBuilder(sb, "\"", "\\\"");
							addQuot = true;
						}
						sb.append("\\r");
						break;
					case '\f':
						if (!addQuot) { // force quote
							SUtils.modifyStringBuilder(sb, "\\", "\\\\");
								SUtils.modifyStringBuilder(sb, "\"", "\\\"");
							addQuot = true;
						}
						sb.append("\\f");
						break;
					case '\b':
						if (!addQuot) { // force quote
							SUtils.modifyStringBuilder(sb, "\\", "\\\\");
							SUtils.modifyStringBuilder(sb, "\"", "\\\"");
							addQuot = true;
						}
						sb.append("\\b");
						break;
					case '\\':
						if (mode == 2 || addQuot) { // force quote array items
							if (!addQuot) {
								SUtils.modifyStringBuilder(sb, "\\", "\\\\");
								SUtils.modifyStringBuilder(sb, "\"", "\\\"");
								addQuot = true;
							}
							sb.append("\\\\");
						} else {
							sb.append(ch);
						}
						break;
					case '"':
						if (mode == 2 || addQuot) { // force quote array items
							if (!addQuot) {
								SUtils.modifyStringBuilder(sb, "\\", "\\\\");
								SUtils.modifyStringBuilder(sb, "\"", "\\\"");
								addQuot = true;
							}
							sb.append("\\\"");
						} else {
							sb.append(ch);
						}
						break;
					default:
						if (ch < ' '
							|| StringParser.getXmlCharType(ch,XConstants.XML10)
								== XML_CHAR_ILLEGAL) {
							if (!addQuot) { // force quote
								SUtils.modifyStringBuilder(sb, "\\", "\\\\");
								SUtils.modifyStringBuilder(sb, "\"", "\\\"");
								addQuot = true;
							}
							sb.append("\\u");
							for (int x = 12; x >= 0; x -=4) {
								sb.append("0123456789abcdef"
									.charAt((ch >> x) & 0xf));
							}
						} else {
							sb.append(ch);
						}
				}
			}
			if (!addQuot) {
				return s;
			}
			return '"' + sb.toString() + '"';
		} else {// Number or Boolean
			return val.toString();
		}
	}

	final static String sourceToJstring(final String s)  {
		StringBuilder sb = new StringBuilder();
		for (int i = 0;  i < s.length(); i++) {
			char ch = s.charAt(i);
			if (ch == '\\') { // escaped characters
				if (++i >= s.length()) {
					return s; // missing escape char (error)
				}
				switch (ch = s.charAt(i)) {
					case '"':
						ch = '"';
						break;
					case '\\':
						ch = '\\';
						break;
					case '/':
						ch = '/';
						break;
					case 'b':
						ch = '\b';
						break;
					case 'f':
						ch = '\f';
						break;
					case 'n':
						ch = '\n';
						break;
					case 'r':
						ch = '\r';
						break;
					case 't':
						ch = '\t';
						break;
					case 'u':
						try {
							ch = (char) Short.parseShort(
								s.substring(i+1, i+5), 16);
							i += 4;
							break;
						} catch (Exception ex) {
							return s; // incorrect UTF-8 char (error)
						}
					default: return s; // illegal escape char (error)
				}
			}
			sb.append(ch);
		}
		return sb.toString();
	}

	/** Create string from JSON string.
	 * @param s JSON string.
	 * @return string created from JSON string.
	 */
	public final static String jstringToSource(final String s) {
		StringBuilder sb = new StringBuilder();
		char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char ch = chars[i];
			switch (ch) {
				case '\\':
					sb.append("\\\\");
					continue;
				case '"':
					sb.append("\\\"");
					continue;
				case '\b':
					sb.append("\\b");
					continue;
				case '\f':
					sb.append("\\f");
					continue;
				case '\n':
					sb.append("\\n");
					continue;
				case '\r':
					sb.append("\\r");
					continue;
				case '\t':
					sb.append("\\t");
					continue;
				default:
					if (ch >= ' ' && Character.isDefined(ch)) {
						sb.append(ch);
					} else { // create \\uxxxx
						sb.append("\\u");
						for (int x = 12; x >= 0; x -=4) {
							sb.append("0123456789abcdef"
								.charAt((ch >> x) & 0xf));
						}
					}
			}
		}
		return sb.toString();
	}

	/** Replace colon in XML name with "_x3a_".
	 * @param s raw XML name.
	 * @return name with colon replaced by "_x3a_".
	 */
	public final static String replaceColonInXMLName(final String s) {
		int i = s.indexOf(':');
		return i >= 0 ? s.substring(0, i) + "_x3a_" + s.substring(i + 1) : s;
	}

	/** Get prefix of XML name.
	 * @param s XML name.
	 * @return prefix of XML name.
	 */
	public final static String getNamePrefix(final String s) {
		int i = s.indexOf(':');
		return (i >= 0) ? s.substring(0, i) : "";
	}

	/** Get XML name created from JSOM pair name.
	 * @param s JSOM pair name.
	 * @return XML name.
	 */
	public final static String toXmlName(final String s) {
		if (s.isEmpty()) {
			return "_x00_"; // empty string
		} else if (("_x00_").equals(s)) {
			return "_x5f_x00_";
		}
		StringBuilder sb = new StringBuilder();
		char ch = s.charAt(0);
		sb.append(ch == ':' || isJChar(s, 0)
			|| StringParser.getXmlCharType(ch, XConstants.XML10)
			 != StringParser.XML_CHAR_NAME_START
			? genXmlHexChar(ch) : ch);
		byte firstcolon = 0;
		for (int i = 1; i < s.length(); i++) {
			ch = s.charAt(i);
			if (isJChar(s, i)) {
				sb.append(genXmlHexChar(ch));
			} else if (ch == ':' && firstcolon == 0) {
				firstcolon = 1;
				sb.append(':');
				if (i + 1 < s.length()) {
					ch=s.charAt(++i);
					sb.append(isJChar(s,i)
						|| StringParser.getXmlCharType(ch, XConstants.XML10)
							< StringParser.XML_CHAR_COLON
					? genXmlHexChar(ch) : ch);
				} else {
					i--;
				}
			} else {
				sb.append(isJChar(s,i)
					|| StringParser.getXmlCharType(ch, XConstants.XML10)
					< StringParser.XML_CHAR_COLON ? genXmlHexChar(ch) : ch);
			}
		}
		return sb.toString();
	}
	/** Create JSON string from source string.
	 * @param src XML form of string.
	 * @return XML form of string converted to JSON.
	 */
	public final static String jstringFromSource(final String src) {
		if (src == null || src.isEmpty()) {
			return src;
		}
		// remove starting and ending '"'
		String s = (src.charAt(0)=='"' && src.charAt(src.length() - 1)=='"')
//			&& src.charAt(src.length() - 1) != '\\')
			? src.substring(1, src.length() - 1) : src;
		return jstringToSource(s);
	}

////////////////////////////////////////////////////////////////////////////////
// JSON parser
////////////////////////////////////////////////////////////////////////////////

	/** Set genJObjects flag (and the _acceptComments flag is also set). */
	public final void setGenJObjects() {
		_genJObjects = true;
		_acceptComments = true;
	}

	/** Set mode that JSON is parsed in X-definition compiler. */
	public final void setXJsonMode() {
		setGenJObjects();
		_jdef = true;
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

	/** Check and get hexadecimal digit as integer.
	 * @param ch character with hexadecimal digit.
	 * @return hexadecimal digit as an integer number 0..15
	 * or return -1 if the argument is not hexadecimal digit.
	 */
	public final static int hexDigit(final char ch) {
		int i = "0123456789abcdefABCDEF".indexOf(ch);
		return i > 15 ? i - 6 : i;
	}

	/** Check if on the position given by index in a string it is the
	 * form of hexadecimal character representation.
	 * @param s inspected string.
	 * @param index index where to start inspection.
	 * @return true if index position represents hexadecimal form of character.
	 */
	public final static boolean isJChar(final String s, final int index) {
		if (index + 3 > s.length() ||
			s.charAt(index) != '_' || s.charAt(index+1) != 'x' ||
			hexDigit(s.charAt(index+2)) < 0) {
			return false;
		}
		// parse hexdigits
		for (int i = index + 3; i < index + 7 && i < s.length(); i++) {
			char ch = s.charAt(i);
			if (hexDigit(ch) < 0) {
				// not hexadecimal digit.
				return ch == '_'; //if '_' return true otherwise return false
			}
		}
		return false;
	}

	/** Read value of JSON string.
	 * @param p parser where the string is on the actual position.
	 * @return the parsed string.
	 */
	public final static String readString(final SParser p) {
		StringBuilder sb = new StringBuilder();
		while (!p.eos()) {
			if (p.isChar('"')) {
				return sb.toString();
			} else if (p.isChar('\\')) {
				char c = p.peekChar();
				if (c == 'u') {
					int x = 0;
					for (int j = 1; j < 4; j++) {
						int y = hexDigit(p.peekChar());
						if (y < 0) {
							p.error(JSON.JSON005);//hexadecimal digit expected
							break;
						}
						x = (x << 4) + y;
					}
					sb.append((char) x);
				} else {
					int i = "\"\\/bfnrt".indexOf(c);
					if (i >= 0) {
						sb.append("\"\\/\b\f\n\r\t".charAt(i));
					} else {
						 // Incorrect escape character in string
						p.error(JSON.JSON006);
						return null;
					}
				}
			} else {
				sb.append(p.peekChar());
			}
		}
		p.error(JSON.JSON001); // end of string ('"') is missing
		return null;
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
		if (isChar('{')) { // Map
			Map<String, Object> result;
			result = _genJObjects ? new CompileJsonXdef.JMap(_sPosition)
				: new LinkedHashMap<String,Object>();
			isSpacesOrComments();
			if (isChar('}')) { // empty map
				return result;
			}
			boolean wasScript = false;
			while(!eos()) {
				int i;
				if (!wasScript && _jdef
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
							error(JSON.JSON002, ",", "}");
						}
						isSpacesOrComments();
						o = readValue();
					}
					if (o != null && o instanceof CompileJsonXdef.JValue
						&& ((CompileJsonXdef.JValue)o).getValue()
							instanceof String) {
						if (result.containsKey(CompileJsonXdef.SCRIPT_KEY)) {
							//Value pair &{0} already exists
							error(JSON.JSON022, CompileJsonXdef.SCRIPT_KEY);
						} else {
							result.put(CompileJsonXdef.SCRIPT_KEY, o);
						}
					} else {
						//Value of $script must be string with X-script
						error(JSON.JSON018);
					}
				} else {
					Object o = readValue();
					if (o != null && (o instanceof String ||
						(_genJObjects && o instanceof CompileJsonXdef.JValue)
						&& ((CompileJsonXdef.JValue) o).getValue()
							instanceof String)) {
						 // parse JSON named pair
						String name = _genJObjects ? o.toString() : (String) o;
						isSpacesOrComments();
						if (!isChar(':')) {
							//"&{0}"&{1}{ or "}{"} expected
							error(JSON.JSON002, ",", "}");
						}
						isSpacesOrComments();
						o = readValue();
						if (result.containsKey(name)) {
							String s = jstringToSource(name);
							if (!s.startsWith("\"") || !s.endsWith("\"")) {
								s = '"' + s + '"';
							}
							//Value pair &{0} already exists
							error(JSON.JSON022, s);
						} else {
							result.put(name, o);
						}
					} else {
						fatal(JSON.JSON004); //String with name of item expected
						return result;
					}
				}
				isSpacesOrComments();
				if (isChar('}')) {
					isSpacesOrComments();
					return result;
				}
				if (isChar(',')) {
					SPosition spos = getPosition();
					isSpacesOrComments();
					if (isChar('}')) {
						SPosition spos1 = getPosition();
						setPosition(spos);
						error(JSON.JSON020); //redundant comma
						setPosition(spos1);
						return result;
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
			return result;
		} else if (isChar('[')) {
			List<Object> result;
			result = _genJObjects ? new CompileJsonXdef.JArray(_sPosition)
				: new ArrayList<Object>();
			isSpacesOrComments();
			if (isChar(']')) { // empty array
				return result;
			}
			boolean wasScript = false;
			boolean wasErrorReported = false;
			while(!eos()) {
				int i;
				if (!wasScript &&_jdef
					&& (i = isOneOfTokens(CompileJsonXdef.SCRIPT_NAME,
						CompileJsonXdef.ONEOF_NAME)) >= 0) {
					wasScript = true;
					if (isChar(':')) {
						isSpacesOrComments();
						Object o = readValue();
						if (o instanceof CompileJsonXdef.JValue
							&& ((CompileJsonXdef.JValue)o).getValue()
							instanceof String){
							CompileJsonXdef.JValue jv =
								(CompileJsonXdef.JValue) o;
							if (i == 1) {
								SPosition spos = jv.getPosition();
								spos.setIndex(spos.getIndex() - 1);
								String s =
									CompileJsonXdef.ONEOF_KEY + jv.getValue();
								jv = new CompileJsonXdef.JValue(spos, s);
							}
							result.add(new CompileJsonXdef.JValue(null, jv));
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
							result.add(new CompileJsonXdef.JValue(null,
								new CompileJsonXdef.JValue(spos,
									CompileJsonXdef.ONEOF_KEY)));
						}
					}
				} else {
					result.add(readValue());
				}
				isSpacesOrComments();
				if (isChar(']')) {
					return result;
				}
				if (isChar(',')) {
					SPosition spos = getPosition();
					isSpacesOrComments();
					if (isChar(']')) {
						SPosition spos1 = getPosition();
						setPosition(spos);
						error(JSON.JSON020); //redundant comma
						setPosition(spos1);
						return result;
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
			return result;
		} else if (isChar('"')) { // string
			String s = readString(this);
			return _genJObjects ? new CompileJsonXdef.JValue(_sPosition, s) : s;
		} else if (isToken("null")) {
			return _genJObjects ? new CompileJsonXdef.JValue(_sPosition, null)
				: null;
		} else if (isToken("true")) {
			return _genJObjects ? new CompileJsonXdef.JValue(_sPosition, true)
				: true;
		} else if (isToken("false")) {
			return _genJObjects ? new CompileJsonXdef.JValue(_sPosition, false)
				: false;
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
			int pos;
			boolean wasError = false;
			if (isChar('+')) {
				error(JSON.JSON017, "+");//Not allowed character '&{0}'
				wasError = true;
				pos = getIndex();
			} else {
				pos = getIndex();
				isChar('-');
			}
			int pos1 = getIndex() - pos;
			if (isInteger()) {
				Number number;
				boolean isfloat = false;
				if (isChar('.')) { // decimal point
					isfloat = true;
					if (!isInteger()) {
						error(JSON.JSON017, ".");//Not allowed character '&{0}'
						wasError = true;
					}
				}
				char ch = getCurrentChar();
				if (isChar('e') || isChar('E')) {//exponent
					isfloat = true;
					if (!isSignedInteger()) {
						error(JSON.JSON017,""+ch);//Not allowed character '&{0}'
						wasError = true;
					}
				}
				String s = getBufferPart(pos, getIndex());
				if (s.charAt(pos1) == '0' && s.length() > 1 &&
					Character.isDigit(s.charAt(pos1 + 1))) {
						error(JSON.JSON014); // Illegal leading zero in number
				}
				number = wasError ? 0
					: isfloat ? new BigDecimal(s) : new BigInteger(s);
				return _genJObjects
					? new CompileJsonXdef.JValue(_sPosition,number) : number;
			}
			error(JSON.JSON010); //JSON value expected
			findOneOfChars(",[]{}"); // skip to next item
			return _genJObjects
				? new CompileJsonXdef.JValue(_sPosition, null) : null;
		}
	}

	/** Parse JSON source data.
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

	/** Parse JSON document from input source data.
	 * The source data may be either file pathname or URL or JSON source.
	 * @param source file pathname or URL or string with JSON source.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parse(final String source)
		throws SRuntimeException{
		Object result;
		if (source.charAt(0) == '{' && source.endsWith("}")
			|| (source.charAt(0) == '[' && source.endsWith("]"))) {
			JsonUtil jx = new JsonUtil();
			jx.setSourceBuffer(source);
			result = jx.parse();
			jx.getReportWriter().checkAndThrowErrors();
			return result;
		}
		InputStream in = null;
		String sysId = null;
		try {
			URL url = SUtils.getExtendedURL(source);
			in = url.openStream();
			sysId = url.toExternalForm();
			result = parse(in, sysId);
		} catch (Exception ex) {
			if (!new File(source).exists()) {
				JsonUtil jx = new JsonUtil();
				jx.setSourceBuffer(source);
				result = jx.parse();
				jx.getReportWriter().checkAndThrowErrors();
				return result;
			}
			try {
				result = parse(new File(source));
			} catch (Exception x) {
				if (x instanceof RuntimeException) {
					throw (RuntimeException) x;
				}
				//IO error detected on &{0}&{1}{, reason: }
				throw new SRuntimeException(SYS.SYS034, source, x);
			}
		}
		try {
			if (in != null) {
				in.close();
			}
		} catch (IOException x) {
			//IO error detected on &{0}&{1}{, reason: }
			throw new SRuntimeException(SYS.SYS034, source, x);
		}
		return result;
	}

	/** Parse JSON document from input source data in file.
	 * @param f input file.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parse(final File f)
		throws SRuntimeException {
		try {
			FileInputStream in = new FileInputStream(f);
			return parse(in, f.getCanonicalPath());
		} catch (Exception ex) {
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			//IO error detected on &{0}&{1}{, reason: }
			throw new SRuntimeException(SYS.SYS034, f, ex);
		}
	}

	/** Parse JSON document from input source data in InputStream.
	 * @param in input data.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parse(final InputStream in)
		throws SRuntimeException {
		Object result = parse(in, null);
		try {
			in.close();
		} catch (IOException ex) {}
		return result;
	}

	/** Parse JSON document from input source data in InputStream.
	 * @param in input data.
	 * @param sysid System id.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parse(final InputStream in,
		final String sysid) throws SRuntimeException{
		try {
			int i = in.read(); //1st byte from input stream
			int j = in.read(); //2nd byte from input stream
			if (i < 0 || i == 0 && j < 0) {//EOF
				//Unexpected eof&{#SYS000}
				throw new SRuntimeException(JSON.JSON007, "&{line}1&{column}1");
			}
			String s;
			Reader reader;
			if (i > 0 && j > 0) {
				// xx xx xx xx  UTF-8
				s = String.valueOf((char) i) + (char)j;
				reader = new InputStreamReader(in, Charset.forName("UTF-8"));
			} else {
				int k = in.read();
				int l = in.read();
				if (l < 0) {//EOF
					//Unexpected eof&{#SYS000}
					throw new SRuntimeException(JSON.JSON007,
						"&{line}1&{column}1");
				}
				if (i == 0 && j == 0 && k == 0) {
					if (l == 0) {// not a character
						// JSON object or array expected"
						throw new SRuntimeException(JSON.JSON009,
							"&{line}1&{column}1");
					}
					// 00 00 00 xx  UTF-32BE
					s = String.valueOf((char) l);
					reader = new InputStreamReader(in, "UTF-32BE");
				} else if (i == 0 && k == 0) {// 00 xx 00 xx  UTF-16BE
					s = String.valueOf((char) j) + (char) l;
					reader = new InputStreamReader(in, "UTF-16BE");
				} else if (k != 0) { // xx 00 xx 00  UTF-16LE
					s = String.valueOf((char) i) + (char) k;
					reader = new InputStreamReader(in, "UTF-16LE");
				} else { // xx 00 00 00  UTF-32LE
					s = String.valueOf((char) i);
					reader = new InputStreamReader(in, "UTF-32LE");
				}
			}
			JsonUtil jx = new JsonUtil();
			jx.setSourceReader(reader, 0L, s);
			if (sysid != null && !sysid.isEmpty()) {
				jx.setSysId(sysid);
			}
			Object result = jx.parse();
			jx.getReportWriter().checkAndThrowErrors();
			return result;
		} catch (Exception ex) { // never should happen
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			throw new SRuntimeException(SYS.SYS036, ex);//Program exception &{0}
		}
	}

	/** Parse source URL to JSON.
	 * @param in source URL
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs,
	 */
	public final static Object parse(final URL in) throws SRuntimeException{
		try {
			URLConnection u = in.openConnection();
			InputStream is = u.getInputStream();
			try {
				return parse(is, in.toExternalForm());
			} finally {
				is.close();
			}
		} catch (IOException ex) {
			//URL &{0} error: &{1}{; }
			throw new SRuntimeException(SYS.SYS076,in.toExternalForm(), ex);
		}
	}

////////////////////////////////////////////////////////////////////////////////
// JSON to string
////////////////////////////////////////////////////////////////////////////////

	/** Add the string created from JSON jvalue object to StringBuilder.
	 * @param obj JSON object to be created to String.
	 * @return sb created string.
	 */
	private static String jvalueToString(final Object obj){
		if (obj == null) {
			return "null";
		} else if (obj instanceof String) {
			return '"' + jstringToSource((String) obj) + '"';
		}
		return obj.toString();
	}

	/** Add the string created from JSON object to StringBuilder.
	 * @param obj JSON object to be created to String.
	 * @param indent indentation of result,
	 * @param sb StringBuilder where to append the created string.
	 */
	private static void objToJsonString(final Object obj,
		final String indent,
		final StringBuilder sb) {
		if (obj instanceof List) {
			List x = (List) obj;
			arrayToJsonString(x, indent, sb);
		} else if (obj instanceof Map) {
			Map x = (Map) obj;
			mapToJsonString(x, indent, sb);
		} else {
			sb.append(jvalueToString(obj));
		}
	}

	/** Add the string created from JSON array to StringBuilder.
	 * @param array JSON array to be created to String.
	 * @param indent indentation of result,
	 * @param sb StringBuilder where to append the created string.
	 */
	private static void arrayToJsonString(final List array,
		final String indent,
		final StringBuilder sb) {
		if (array.isEmpty()) {
			sb.append("[]");
			return;
		}
		if (indent != null && indent.length() > 0 && array.size() == 1) {
			Object o = array.get(0);
			if (!(o instanceof Map) && !(o instanceof List)) {
				String s = jvalueToString(o);
				if (s.length() + indent.length() < 72) {
					sb.append('[').append(s).append(']');
					return;
				}
			}
		}
		int lastValuePosition = sb.length();
		sb.append('[');
		String ind = (indent != null) ? indent + "  " : null;
		boolean first = true;
		for (Object o: array) {
			if (first) {
				first = false;
				if (ind != null && array.size() > 1) {
					sb.append(ind);
				}
			} else {
				sb.append(',');
				if (ind != null) {
					sb.append(ind);
				}
			}
			objToJsonString(o, ind, sb);
		}
		if (ind != null
			&&  (array.size() > 1 || sb.lastIndexOf("\n") > lastValuePosition)){
			sb.append(indent);
		}
		sb.append(']');
	}

	/** Add the string created from JSON map to StringBuilder.
	 * @param map JSON map to be created to String.
	 * @param indent indentation of result,
	 * @param sb StringBuilder where to append the created string.
	 */
	private static void mapToJsonString(final Map map,
		final String indent,
		final StringBuilder sb) {
		sb.append('{');
		if (map.isEmpty()) {
			sb.append('}');
			return;
		}
		String ind = (indent != null) ? indent + "  " : null;
		boolean first = true;
		int lastValuePosition = sb.length();
		for (Object x: map.entrySet()) {
			Map.Entry e = (Map.Entry) x;
			if (first) {
				first = false;
				if (map.size() > 1) {
					sb.append(' ');
				}
				objToJsonString(e.getKey(), "", sb);
			} else {
				lastValuePosition = sb.length();
				sb.append(',');
				if (ind != null) {
					sb.append(ind);
				}
				objToJsonString(e.getKey(), ind, sb);
			}
			lastValuePosition = sb.length();
			sb.append(':');
			objToJsonString(e.getValue(), ind, sb);
		}
		if (ind != null
			&&  (map.size() > 1 || sb.lastIndexOf("\n") > lastValuePosition)) {
			sb.append(indent);
		}
		sb.append('}');
	}

	/** Create JSON string from object (no indentation).
	 * @param obj JSON object.
	 * @return string with JSON source format.
	 */
	public final static String toJsonString(final Object obj) {
		return toJsonString(obj, false);
	}

	/** Create JSON string from object. Indentation depends on argument.
	 * @param obj JSON object.
	 * @param indent if true then result will be indented.
	 * @return string with JSON source format.
	 */
	public final static String toJsonString(final Object obj, boolean indent) {
		StringBuilder sb = new StringBuilder();
		String indt = indent ? "\n" : null;
		if (obj instanceof List) {
			arrayToJsonString((List) obj, indt, sb);
		} else if (obj instanceof Map) {
			mapToJsonString((Map) obj, indt, sb);
		} else {
			return jvalueToString(obj);
		}
		return sb.toString();
	}

////////////////////////////////////////////////////////////////////////////////
// Compare two JSON objects.
////////////////////////////////////////////////////////////////////////////////

	/** Check if JSON arrays from arguments are equal.
	 * @param a1 first array.
	 * @param a2 second array.
	 * @return true if and only if both arrays are equal.
	 */
	private static boolean equalArray(final List a1, final List a2) {
		if (a1.size() == a2.size()) {
			for (int i = 0; i < a1.size(); i++) {
				if (!equalValue(a1.get(i), a2.get(i))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/** Check if JSON maps from arguments are equal.
	 * @param m1 first map.
	 * @param m2 second map.
	 * @return true if and only if both maps are equal.
	 */
	private static boolean equalMap(final Map m1, final Map m2) {
		if (m1.size() != m2.size()) {
			return false;
		}
		for (Object k: m1.keySet()) {
			if (!m2.containsKey(k)) {
				return false;
			}
			if (!equalValue(m1.get(k), m2.get(k))) {
				return false;
			}
		}
		return true;
	}

	/** Check if JSON numbers from arguments are equal.
	 * @param n1 first number.
	 * @param n2 second number.
	 * @return true if and only if both numbers are equal.
	 * @throws SRuntimeException if objects are incomparable
	 */
	private static boolean equalNumber(final Number n1, final Number n2) {
		if (n1 instanceof BigDecimal) {
			if (n2 instanceof BigDecimal) {
				return ((BigDecimal) n1).compareTo((BigDecimal) n2) == 0;
			} else if (n2 instanceof BigInteger) {
				return equalNumber(n1, new BigDecimal((BigInteger) n2));
			} else if (n2 instanceof Long || n2 instanceof Integer
				|| n2 instanceof Short || n2 instanceof Byte) {
				return equalNumber(n1, new BigDecimal(n2.longValue()));
			} else if (n2 instanceof Double || n2 instanceof Float) {
				//this is real equality, decimal can't be exactly converted!
				return n1.doubleValue() == n2.doubleValue();
			}
		} else if (n1 instanceof BigInteger) {
			if (n2 instanceof BigInteger) {
				return ((BigInteger) n1).compareTo((BigInteger) n2) == 0;
			} else if (n2 instanceof BigDecimal || n2 instanceof BigInteger) {
				return equalNumber(new BigDecimal((BigInteger)n1), n2);
			} else if (n2 instanceof Long || n2 instanceof Integer
				|| n2 instanceof Short || n2 instanceof Byte) {
				return equalNumber(n1, new BigInteger(n2.toString()));
			} else if (n2 instanceof Double || n2 instanceof Float) {
				return equalNumber(new BigDecimal((BigInteger)n1), n2);
			}
		} else if (n1 instanceof Long || n1 instanceof Integer
			|| n1 instanceof Short || n1 instanceof Byte) {
			if (n2 instanceof Long || n2 instanceof Integer
				|| n2 instanceof Short || n2 instanceof Byte) {
				return n1.longValue() == n2.longValue();
			} else if (n2 instanceof Double || n2 instanceof Float
				|| n2 instanceof BigInteger || n2 instanceof BigDecimal) {
				return equalNumber(n2, n1);
			}
		} else if (n1 instanceof Double || n1 instanceof Float) {
			if (n2 instanceof BigInteger || n2 instanceof BigDecimal) {
				return equalNumber(n2, n1);
			}
			return n1.doubleValue() == n2.doubleValue();
		}
		//Incomparable objects &{0} and &{1}
		throw new SRuntimeException(JSON.JSON012,
			n1.getClass().getName(), n2.getClass().getName());
	}

	/** Check if JSON values from arguments are equal.
	 * @param o1 first value.
	 * @param o2 second value.
	 * @return true if and only if both values are equal.
	 * @throws SRuntimeException if objects are incomparable
	 */
	private static boolean equalValue(final Object o1, final Object o2) {
		if (o1 == null || o2 instanceof JNull) {
			return o2 == null || o2 instanceof JNull;
		} else if (o2 == null || o1 instanceof JNull) {
			return o1 == null || o1 instanceof JNull;
		}
		if (o1 instanceof Map) {
			return o2 instanceof Map ? equalMap((Map)o1, (Map)o2) : false;
		}
		if (o1 instanceof List) {
			return o2 instanceof List ? equalArray((List) o1, (List) o2) :false;
		}
		if (o1 instanceof String) {
			return ((String) o1).equals(o2);
		}
		if (o1 instanceof Number) {
			return (o2 instanceof Number)
				? equalNumber((Number) o1, (Number) o2) : false;
		}
		if (o1 instanceof Boolean) {
			return ((Boolean) o1).equals(o2);
		}
		// Incomparable objects &{0} and &{1}
		throw new SRuntimeException(JSON.JSON012,
			o1.getClass().getName(), o2.getClass().getName());
	}

	/** Compare two JSON objects.
	 * @param j1 first object with JSON data.
	 * @param j2 second object with JSON data.
	 * @return true if and only if both objects contains equal data.
	 */
	public final static boolean jsonEqual(final Object j1, final Object j2) {
		return (j1 == null && j2 == null)
			|| (j1 != null && j2 != null && equalValue(j1,j2));
	}

////////////////////////////////////////////////////////////////////////////////

	/** Convert XML element to JSON object.
	 * @param node XML element or document.
	 * @return JSON object.
	 */
	public final static Object xmlToJson(final Node node) {
		return JsonFromXml.toJson(node);
	}

	/** Convert XML document to JSON object.
	 * @param source path or string with source of XML document.
	 * @return object with JSON data.
	 */
	public final static Object xmlToJson(final String source) {
		return xmlToJson(KXmlUtils.parseXml(source).getDocumentElement());
	}

	/** Convert XML document to JSON object.
	 * @param file file with XML document.
	 * @return object with JSON data.
	 */
	public final static Object xmlToJson(final File file) {
		return xmlToJson(KXmlUtils.parseXml(file).getDocumentElement());
	}

	/** Convert XML document to JSON object.
	 * @param url URL containing XML document.
	 * @return object with JSON data.
	 */
	public final static Object xmlToJson(final URL url) {
		return xmlToJson(KXmlUtils.parseXml(url).getDocumentElement());
	}

	/** Convert XML document to JSON object.
	 * @param in InputStream with XML document.
	 * @return object with JSON data.
	 */
	public final static Object xmlToJson(final InputStream in) {
		return xmlToJson(KXmlUtils.parseXml(in).getDocumentElement());
	}

	/** Create XML from JSON object in W3C mode.
	 * @param json path to JSON source data.
	 * @return XML element created from JSON data.
	 */
	public final static Element jsonToXml(final String json) {
		return JsonToXml.toXmlW3C(parse(json));
	}

	/** Create XML from JSON object in W3C mode.
	 * @param json file with JSON source data.
	 * @return XML element created from JSON data.
	 */
	public final static Element jsonToXml(final File json) {
		return JsonToXml.toXmlW3C(parse(json));
	}

	/** Create XML from JSON object in W3C mode.
	 * @param json URL where is JSON source data.
	 * @return XML element created from JSON data.
	 */
	public final static Element jsonToXml(final URL json) {
		return JsonToXml.toXmlW3C(parse(json));
	}

	/** Create XML from JSON object in W3C mode.
	 * @param json Input stream where is JSON source data.
	 * @return XML element created from JSON data.
	 */
	public final static Element jsonToXml(final InputStream json) {
		return JsonToXml.toXmlW3C(parse(json));
	}

	/** Create XML from JSON object in X-Definition mode.
	 * @param json JSON object.
	 * @return XML element created from JSON data.
	 */
	public final static Element jsonToXml(final Object json) {
		return JsonToXml.toXmlW3C(json);
	}

	/** Create XML from JSON object in X-Definition mode.
	 * @param json path to JSON source data.
	 * @return XML element created from JSON data.
	 */
	public final static Element jsonToXmlXD(final String json) {
		return JsonToXml.toXmlXD(parse(json));
	}

	/** Create XML from JSON object in X-Definition mode.
	 * @param json File with JSON source data.
	 * @return XML element created from JSON data.
	 */
	public final static Element jsonToXmlXD(final File json) {
		return JsonToXml.toXmlXD(parse(json));
	}

	/** Create XML from JSON object in X-Definition mode.
	 * @param json URL with JSON source data.
	 * @return XML element created from JSON data.
	 */
	public final static Element jsonToXmlXD(final URL json) {
		return JsonToXml.toXmlXD(parse(json));
	}

	/** Create XML from JSON object in X-Definition mode.
	 * @param json InputStream with JSON source data.
	 * @return XML element created from JSON data.
	 */
	public final static Element jsonToXmlXD(final InputStream json) {
		return JsonToXml.toXmlXD(parse(json));
	}

	/** Create XML from JSON object in X-Definition mode.
	 * @param json JSON object.
	 * @return XML element created from JSON data.
	 */
	public final static Element jsonToXmlXD(final Object json) {
		return JsonToXml.toXmlXD(json);
	}
}