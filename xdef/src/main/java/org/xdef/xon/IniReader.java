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

	public Object getValue() {return _jp.getResult();}

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
						p.error(JSON.JSON006);
						val += '?';
					}
				} else {
					val += c;
				}
			}
			_jp.putValue(new XonTools.JValue(spos, val));
			return true;
		}
		throw new RuntimeException("'=' expected");
	}

	@SuppressWarnings("unchecked")
	public final static Map<String, Object> parseINI(Reader in, String sysId) {
		XonParser jp = new XonReader.ObjParser();
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
				p.nextChar();
				p.isSpaces();
				SPosition spos1 = p.getPosition();
				if (p.isToken(XonNames.SCRIPT_NAME)) {
					p.isSpaces();
					if (p.isChar('=')) {
						p.isSpaces();
						String t = p.getUnparsedBufferPart();
						int i = t.lastIndexOf(']');
						if (i == t.length() - 1) {
							p1 = new SBuffer(XonNames.SCRIPT_NAME, spos);
							p2 = new SBuffer(t.substring(0,i), p.getPosition());
						} else {
							throw new RuntimeException(" text after \"]\"");
						}
					}
				}
				if (p1 == null) {
//					//Value of $script must be string with X-script
//					error(JSON.JSON018);
					throw new RuntimeException(" script error");
				}
			} else {
				p.setPosition(spos);
				if (!p.findChar(']')) {
					throw new RuntimeException("] missing");
				}
				name = p.getBufferPart(spos.getIndex(), p.getIndex());
				p.nextChar();
				p.isSpaces();
				if (!p.eos()) {
					throw new RuntimeException(" text after \"]\"");
				}
			}
			_jp.namedValue(new SBuffer(name.trim(), spos));
			_jp.mapStart(spos);
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
				case '\t' : sb.append("\\t"); continue;
				case '\n' : sb.append("\\n"); continue;
				default :
					if (ch >= ' ' && ch <= 127) {
						sb.append(ch);
					} else {
						sb.append("\\u");
						for (int x = 12; x >= 0; x -=4) {
							sb.append("0123456789ABCDEF".charAt((ch >> x)&0xf));
						}
					}
			}
		}
	}

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

	@SuppressWarnings("unchecked")
	public final static Element iniToXml(final Object ini) {
		Document doc = KXmlUtils.newDocument(XDConstants.XON_NS_URI_W,
			XDConstants.XON_NS_PREFIX+ ":"+XonNames.X_MAP, null);
		Element el = doc.getDocumentElement();
		iniToXml((Map<String,Object>) ini, el);
		return el;
	}

	@SuppressWarnings("unchecked")
	private static void iniToXml(final Map<String,Object> ini,final Element el){
		for (Map.Entry<String, Object> x: ini.entrySet()) {
			String name = x.getKey();
			Object o = x.getValue();
			if (!(o instanceof Map)) {
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
	public final static Element iniToXmlW(final Object ini) {
		Element el = KXmlUtils.newDocument(XDConstants.XON_NS_URI_W,
			XDConstants.XON_NS_PREFIX + ":"+XonNames.X_MAP, null)
			.getDocumentElement();
		toXmlW((Map<String,Object>) ini, el);
		return el;
	}
}