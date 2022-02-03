package org.xdef.xon;

import java.io.Reader;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.msg.JSON;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import org.xdef.xml.KXmlUtils;

/** Read properties and files.
 * @author Vaclav Trojan
 */
public class IniReader extends StringParser implements XonParsers {
	/** Flag if the parsed data are in X-definition (default false). */
	private boolean _jdef;
	/** Parser of XON source. */
	private final XonParser _jp;

	/** Create instance of parser.
	 * @param jp parser of INI/Properties source.
	 * @param source String with source data.
	 */
	public IniReader(final SBuffer source, final XonParser jp) {
		super(source);
		_jp = jp;
	}

	/** Create instance of parser.
	 * @param jp parser of INI/Properties source.
	 * @param source String with source data.
	 */
	public IniReader(final String source, final XonParser jp) {
		super(source);
		_jp = jp;
	}

	/** Create instance of parser.
	 * @param jp parser of INI/Properties source.
	 * @param source Reader with source data.
	 */
	public IniReader(final Reader source, final XonParser jp) {
		super(source, new ArrayReporter());
		_jp = jp;
	}

	/** Create instance of parser.
	 * @param jp parser of INI/Properties source.
	 * @param source URL with source data.
	 */
	public IniReader(final URL source, final XonParser jp) {
		super(source, new ArrayReporter(), 0);
		_jp = jp;
	}

	/** Get value of parsed data.
	 * @return value of parsed data.
	 */
	public Object getValue() {return _jp.getResult();}

	/** Read line from INI/Property file.
	 * @return SBuffer with the text of the parsed line.
	 */
	private SBuffer readLine() {
		if (eos()) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		SPosition spos = getPosition();
		while (!eos() && !isNewLine()) {
			sb.append(peekChar());
		}
		return new SBuffer(sb.toString(), spos);
	}

	/** Check if the parsed line ends with backslash.
	 * @param s parsed line.
	 * @return true if the parsed line ends with backslash.
	 */
	private static boolean endsWithBackslash(String s) {
		int i = s.length() - 1;
		for (; i <= 0; i--) {
			if (s.charAt(i) > ' ') {
				break;
			}
		}
		int j = i;
		while (i >= 0 && s.charAt(i) == '\\') {
			i--;
		}
		return ((j - i) & 1) > 0;
	}

	/** Read text with property.
	 * @return SBuffer with property text.
	 */
	private SBuffer readPropText() {
		SBuffer sbuf;
		StringParser p;
		for(;;) {
			sbuf = readLine();
			if (sbuf == null) {
				return null;
			}
			p = new StringParser(sbuf);
			p.isSpaces();
			if (!p.eos()
				&& !(p.isChar('#') || p.isChar('!') || p.isChar(';'))) {
				break;
			}
		}
		if (p.getCurrentChar() == '[') {
			return new SBuffer(p.getUnparsedBufferPart(), p.getPosition());
		}
		SPosition spos = p.getPosition();
		while (!p.eos() && p.getCurrentChar() != '=') {
			p.peekChar();
		}
		if (p.isChar('=')) {
			p.isSpaces();
			spos = p.getPosition();
			String s = p.getSourceBuffer();
			if (endsWithBackslash(s)) { // the continue line
				s = s.substring(0, s.length()-1);
				int i = 0;
				String t = readLine().getString();
				for (; i < t.length(); i++) {
					if (t.charAt(i) > ' ') {
						break;
					}
				}
				if (i > 0) {
					t = t.substring(i);
				}
				while(endsWithBackslash(t)) {
					s += t.substring(0, t.length() - 1);
					if (p.eos()) {
						break;
					}
					t = readLine().getString(); // read next line;
					for (; i < t.length(); i++) {
						if (t.charAt(i) > ' ') {
							break;
						}
					}
					if (i > 0) {
						t = t.substring(i);
					}
				}
				s += t;
			}
			return new SBuffer(s, spos);
		}
		return null;
	}

	/** Put property from source. 
	 * @param s string with property.
	 * @return true if it is a property item, false if it is INI section.
	 */
	private boolean putProperty(SBuffer s) {
		if (s == null || s.getString().charAt(0) == '[') {
			return false;
		}
		StringParser p = new StringParser(s);
		p.isSpaces();
		SPosition spos = p.getPosition();
		String key = null;
		if (p.findChar('=')) {
			key = p.getParsedBufferPartFrom(spos.getIndex());
			p.nextChar();
			_jp.namedValue(
				new SBuffer(key.substring(0, key.length()).trim(), spos));
			p.isSpaces();
			spos = p.getPosition();
			String val = "";
			while (!p.eos()) {
				char c = p.peekChar();
				if (c == '\\') {
					c = p.peekChar();
					int i = "u\"\\/bfnrt01234567:".indexOf(c);
					if (i == 0) { //u
						int x = 0;
						for (int j = 0; j < 4; j++) {
							int y = XonTools.hexDigit(p.peekChar());
							if (y < 0) {
								error(JSON.JSON005);//hexadecimal digit expected
								x = '?';
								break;
							} else {
								x = (x << 4) + y;
							}
						}
						val += (char) x;
					} else if (i > 0) { // escaped characters
						val += "u\"\\/\b\f\n\r\t\0\1\2\3\4\5\6\7:".charAt(i);
					} else {
						// Incorrect escape character in string
						error(JSON.JSON006);
						val += '?';
					}
				} else {
					val += c;
				}
			}
			_jp.putValue(new XonTools.JValue(spos, val));
			return true;
		}
		error(JSON.JSON002, "=");//"&{0}"&{1}{ or "}{"} expected
		return false;
	}

	@SuppressWarnings("unchecked")
	/** Parse INI/Properties from reader
	 * @param in reader with source data.
	 * @param sysId system ID
	 * @return map with parsed data.
	 */
	public final static Map<String, Object> parseINI(Reader in, String sysId) {
		XonParser jp = new XonObjParser();
		IniReader xr = new IniReader(in, jp);
		if (sysId != null) {
			xr.setSysId(sysId);
		}
		xr.parse();
		xr.isSpaces();
		if (!xr.eos()) {
			xr.error(JSON.JSON008);//Text after JSON not allowed
		}
		xr.getReportWriter().checkAndThrowErrorWarnings();
		return (Map<String, Object>) jp.getResult();
	}

	/** Read INI data from source. */
	private void readINI() {
		isSpaces();
		_jp.mapStart(this);
		SBuffer prop;
		while (putProperty(prop = readPropText())) {}
		while (prop != null) {
			StringParser p = new StringParser(prop);
			p.nextChar();
			p.isSpaces();
			SPosition spos = p.getPosition();
			String name;
			boolean isScript = _jdef && p.findChar(';');
			SBuffer p1 = null, p2 = null;
			if (isScript) {
				name = p.getBufferPart(spos.getIndex(), p.getIndex());
				_jp.namedValue(new SBuffer(name.trim(), spos));
				_jp.mapStart(spos);
				if (p1 == null) {
					//Value of $script must be string with X-script
					error(JSON.JSON018);
				}
			} else {
				p.setPosition(spos);
				if (!p.findChar(']')) {
					//"&{0}"&{1}{ or "}{"} expected&
					error(JSON.JSON002, "]");
				}
				name = p.getBufferPart(spos.getIndex(), p.getIndex());
				_jp.namedValue(new SBuffer(name.trim(), spos));
				_jp.mapStart(spos);
				p.nextChar(); //skip ']'
				p.isSpaces();
				SPosition spos1 = p.getPosition();
				if (p.isToken("$script")) {
					p.isSpaces();
					if (p.isChar('=')) {
						p.isSpaces();
						_jp.xdScript(new SBuffer("$script", spos1),
							new SBuffer(p.getUnparsedBufferPart().trim(),
								p.getPosition()));
						p.setEos();
					}
				}
				if (!p.eos()) {
					SPosition sps = getPosition();
					setPosition(p.getPosition());
					//Not allowed character&{0}{ "}{"}
					error(JSON.JSON017);
					setPosition(sps);
				}
			}
			if (p1 != null) {
				_jp.xdScript(p1, p2);
			}
			while (putProperty(prop = readPropText())) {}
			_jp.mapEnd(spos);
		}
		_jp.mapEnd(this);
	}

////////////////////////////////////////////////////////////////////////////////
// interface XONParsers
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Parse INI/Properties source data.
	 * @throws SRuntimeException if an error occurs,
	 */
	public final void parse() throws SRuntimeException {
		readINI();
		if (!eos()) {
			error(JSON.JSON008);//Text after JSON not allowed
		}
	}
	@Override
	/** Set mode that INI file is parsed in X-definition compiler. */
	public final void setXdefMode() { _jdef = true;}

////////////////////////////////////////////////////////////////////////////////
// INI to String
////////////////////////////////////////////////////////////////////////////////

	/** Create INI/Properties source format of a string.
	 * @param sb where to create.
	 * @param s the string to be converted.
	 * @return INI/Properties source format of a string.
	 */
	private static void toPropertyString(final StringBuilder sb,
		final String s) {
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			switch(ch) {
				case '\\' : sb.append("\\\\"); continue;
				case '\b' : sb.append("\\b"); continue;
				case '\f' : sb.append("\\f"); continue;
				case '\n' : sb.append("\\n"); continue;
				case '\r' : sb.append("\\r"); continue;
				case '\t' : sb.append("\\t"); continue;
				default :
					if (ch >= ' ' && ch <= 127) {
						sb.append(ch);
					} else {
						sb.append(XonTools.genCharAsUTF(ch));
					}
			}
		}
	}

	/** Create string from object from INI data.
	 * @param val object from INI data.
	 * @return string created from object from INI data.
	 */
	private static String valueToString(final Object val) {
		if (val == null) {
			return "";
		} else if (val instanceof InetAddress) {
			return ((InetAddress) val).toString().substring(1);
		} else if (val instanceof byte[]) {
			return new String(SUtils.encodeHex((byte[]) val),
				Charset.forName("ISO8859-2"));
		} else {
			return val.toString();
		}
	}

	/** Create the line of INI/Property item.
	 * @param sb where to create.
	 * @param name name of INI/Property item.
	 * @param val string with value of INI/Property item.
	 * @return string with line with INI/Property item.
	 */
	private static void toPropertyLine(final StringBuilder sb,
		final String name,
		final Object val) {
		toPropertyString(sb, name);
		sb.append('=');
		toPropertyString(sb, valueToString(val));
		sb.append('\n');
	}

	@SuppressWarnings("unchecked")
	/** Create string with INI/Property source format.
	 * @param map Map object with INI/Property data.
	 * @return created string with INI/Property source.
	 */
	public final static String toIniString(final Map<String, Object> map) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Object> x: map.entrySet()) {
			Object val = ((Map.Entry)x).getValue();
			if (val == null || !(val instanceof Map)) {
				toPropertyLine(sb, x.getKey(), val);
			}
		}
		for (Map.Entry<String, Object> x: map.entrySet()) {
			Object val = x.getValue();
			if (val != null && (val instanceof Map)) {
				sb.append('[').append(x.getKey()).append("]\n");
				for (Map.Entry<String, Object> y
					: ((Map<String, Object>) val).entrySet()) {
					toPropertyLine(sb, y.getKey(), y.getValue());
				}
			}
		}
		return sb.toString();
	}

////////////////////////////////////////////////////////////////////////////////
// INI to XML
////////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("unchecked")
	/** Create XML element (W form) from INI object.
	 * @param ini Object with INI data.
	 * @return XML element created from INI object.
	 */
	public final static Element iniToXml(final Object ini) {
		Document doc = KXmlUtils.newDocument(XDConstants.XON_NS_URI_W,
			XDConstants.XON_NS_PREFIX+ ":"+XonNames.X_MAP, null);
		Element el = doc.getDocumentElement();
		iniToXml((Map<String,Object>) ini, el);
		return el;
	}

	@SuppressWarnings("unchecked")
	/** Add XML element to INI element created from INI data.
	 * @param ini INI data to be added.
	 * @param el the element where to add.
	 */
	private static void iniToXml(final Map<String,Object> ini,final Element el){
		for (Map.Entry<String, Object> x: ini.entrySet()) {
			String name = x.getKey();
			Object o = x.getValue();
			if (!(o instanceof Map)) {
				Element item = el.getOwnerDocument().createElementNS(
					XDConstants.XON_NS_URI_W,
					XDConstants.XON_NS_PREFIX + ":" + XonNames.X_ITEM);
				item.setAttribute(XonNames.X_KEYATTR, XonTools.toXmlName(name));
				item.setAttribute(XonNames.X_VALUEATTR, o.toString());
				el.appendChild(item);
			}
		}
		for (Map.Entry<String, Object> x: ini.entrySet()) {
			String name = x.getKey();
			Object o = x.getValue();
			if (o instanceof Map) {
				Element item = el.getOwnerDocument().createElementNS(
					XDConstants.XON_NS_URI_W,
					XDConstants.XON_NS_PREFIX + ":" + XonNames.X_MAP);
				item.setAttribute(XonNames.X_KEYATTR, XonTools.toXmlName(name));
				iniToXml((Map<String, Object>) o, item);
				el.appendChild(item);
			}
		}
	}

	/** Add INI/Properties items from INI/Properties object to an Element.
	 * @param ini INI/Properties object
	 * @param el Element where to  add items.
	 */
	@SuppressWarnings("unchecked")
	private static void toXmlW(final Map<String,Object> ini,final Element el) {
		for (Map.Entry<String, Object> x: ini.entrySet()) {
			String name = x.getKey();
			Object o = x.getValue();
			if (!(o instanceof Map)) {
				// add the element with items
				Element item = el.getOwnerDocument().createElementNS(
					XDConstants.XON_NS_URI_W,
					XDConstants.XON_NS_PREFIX + ":" + XonNames.X_ITEM);
				item.setAttribute(XonNames.X_KEYATTR, name);
				item.setAttribute(XonNames.X_VALUEATTR, o.toString());
				el.appendChild(item);
			}
		}
		for (Map.Entry<String, Object> x: ini.entrySet()) {
			String name = x.getKey();
			Object o = x.getValue();
			if (o instanceof Map) {
				Element item = el.getOwnerDocument().createElementNS(
					XDConstants.XON_NS_URI_W,
					XDConstants.XON_NS_PREFIX + ":" + XonNames.X_MAP);
				item.setAttribute(XonNames.X_KEYATTR, name);
				toXmlW((Map<String, Object>) o, item);
				el.appendChild(item);
			}
		}
	}

	@SuppressWarnings("unchecked")
	/** Create W form of XML element from object with INI data.
	 * @param ini object with INI data.
	 * @return W form of XML element created from object with INI data.
	 */
	public final static Element iniToXmlW(final Object ini) {
		Element el = KXmlUtils.newDocument(XDConstants.XON_NS_URI_W,
			XDConstants.XON_NS_PREFIX + ":"+XonNames.X_MAP, null)
			.getDocumentElement();
		toXmlW((Map<String,Object>) ini, el);
		return el;
	}
}