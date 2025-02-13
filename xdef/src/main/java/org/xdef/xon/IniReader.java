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
import static org.xdef.xon.XonNames.SCRIPT_DIRECTIVE;
import static org.xdef.xon.XonTools.genXMLString;

/** Methods for INI/Properties data.
 * @author Vaclav Trojan
 */
public class IniReader extends StringParser implements XonParsers, XonNames {
	/** Flag if the parsed data are in Xdefinition (default false). */
	private boolean _jdef;
	/** Parser of XON source. */
	private final XonParser _jp;

	/** Create instance of parser.
	 * @param jp parser of INI/Properties source.
	 * @param source String with source data.
	 */
	public IniReader(final SBuffer source, final XonParser jp) {super(source); _jp = jp;}

	/** Create instance of parser.
	 * @param jp parser of INI/Properties source.
	 * @param source String with source data.
	 */
	public IniReader(final String source, final XonParser jp) {super(source); _jp = jp;}

	/** Create instance of parser.
	 * @param jp parser of INI/Properties source.
	 * @param source Reader with source data.
	 */
	public IniReader(final Reader source, final XonParser jp) {super(source, new ArrayReporter()); _jp = jp;}

	/** Create instance of parser.
	 * @param jp parser of INI/Properties source.
	 * @param source URL with source data.
	 */
	public IniReader(final URL source, final XonParser jp) {super(source, new ArrayReporter(), 0); _jp = jp;}

	/** Read source line, skip comment lines.
	 * @return SBuffer with a line or return null.
	 */
	private SBuffer readLine() {
		for (;;) {
			isSpaces();
			if (!isChar('#')) {
				break;
			}
			// skip comment lins
			while (!eos() && !isNewLine()) {
				nextChar();
			}
		}
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

	/** Check if the line ends with backslash character.
	 * @param s string with a line.
	 * @return true if the line ends with backslash character.
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

	/** Read text of proerty.
	 * @return SBuffer with property or return null;
	 */
	private SBuffer readPropText() {
		SBuffer sbuf = readLine();
		if (sbuf == null) {
			return null;
		}
		StringParser p = new StringParser(sbuf);
		p.isSpaces();
		if (p.getCurrentChar() == '[') {
			return new SBuffer(p.getUnparsedBufferPart(), p.getPosition());
		}
		if (p.findChar('=')) {
			p.isSpaces();
			SPosition spos = p.getPosition();
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
		error(JSON.JSON002, "=");//"&{0}"&{1}{ or "}{"} expected
		return null;
	}

	/** Put property to ini object.
	 * @param s String with property.
	 * @return true if a proprty was added to ini object.
	 */
	private boolean putProperty(SBuffer s) {
		if (s == null || s.getString().charAt(0) == '[') {
			return false;
		}
		StringParser p = new StringParser(s);
		p.isSpaces();
		SPosition spos = p.getPosition();
		if (p.findChar('=')) {
			String key = p.getParsedBufferPartFrom(spos.getIndex());
			p.nextChar();
			_jp.namedValue(new SBuffer(key.substring(0, key.length()).trim(), spos));
			p.isSpaces();
			spos = p.getPosition();
			String val = "";
			char c;
			while (!p.eos()) {
				c = p.peekChar();
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
						error(JSON.JSON006); // Incorrect escape character in string
						val += '?';
					}
				} else {
					val += c;
				}
			}
			// remove spaces at the end of line
			int len = val.length();
			int i = len;
			while (i > 0 && ((c=val.charAt(i-1)) == ' ' || c == '\n' || c == '\r' || c == '\t')) {
				i--;
			}
			if (i == 0) {
				val = null;
			} else if (i < len) {
				val = val.substring(0, i);
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
	public static final Map<String, Object> parseINI(Reader in, String sysId) {
		XonParser jp = new XonObjParser(true);
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

	/** Read INI file from source. */
	private void readINI() {
		_jp.mapStart(this);
		SBuffer prop;
		while (putProperty(prop = readPropText())) {}
		while (prop != null) {
			StringParser p = new StringParser(prop);
			p.nextChar();
			p.isSpaces();
			SPosition spos = p.getPosition();
			String name;
			SBuffer p1 = null, p2 = null;
			if (_jdef && p.findChar(';')) {
				name = p.getBufferPart(spos.getIndex(), p.getIndex());
				_jp.namedValue(new SBuffer(name.trim(), spos));
				_jp.mapStart(spos);
				p.isSpaces();
				if (p.isChar(';')) {
					p.isSpaces();
					SPosition spos1 = p.getPosition();
					if (p.isToken(SCRIPT_DIRECTIVE)) {
						p.isSpaces();
						if (p.isChar('=')) {
							p.isSpaces();
							String s = p.getUnparsedBufferPart();
							int ndx = s.lastIndexOf(']');
							if (ndx > 0 ) {
								p1 = new SBuffer(SCRIPT_DIRECTIVE, spos1);
								p2 = new SBuffer(s.substring(0, s.length()-1), p.getPosition());
							}
						}
					}
				}
				if (p1 == null) {
					error(JSON.JSON018); //Value of x:script must be string with Xscript
				}
				p.findChar(']');
			} else {
				p.setPosition(spos);
				if (!p.findChar(']')) {
					error(JSON.JSON002, "]");//"&{0}"&{1}{ or "}{"} expected&
				}
				name = p.getBufferPart(spos.getIndex(), p.getIndex());
				_jp.namedValue(new SBuffer(name.trim(), spos));
				_jp.mapStart(spos);
				p.nextChar(); //skip ']'
				p.isSpaces();
				SPosition spos1 = p.getPosition();
				if (p.isToken(SCRIPT_DIRECTIVE)) {
					p.isSpaces();
					if (p.isChar('=')) {
						p.isSpaces();
						_jp.xdScript(new SBuffer(SCRIPT_DIRECTIVE, spos1),
							new SBuffer(p.getUnparsedBufferPart().trim(), p.getPosition()));
						p.setEos();
					}
				}
				if (!p.eos()) {
					SPosition sps = getPosition();
					setPosition(p.getPosition());
					error(JSON.JSON017); //Not allowed character&{0}{ "}{"}
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
	/** Set mode that INI file is parsed in Xdefinition compiler. */
	public final void setXdefMode() { _jdef = true;}
	@Override
	public void setXonMode() {} // not used here
	@Override
	public void setJsonMode() {} // not used here

////////////////////////////////////////////////////////////////////////////////
// INI to String
////////////////////////////////////////////////////////////////////////////////

	/** Create INI/Properties source format of a string.
	 * @param sb where to create.
	 * @param s the string to be converted.
	 * @return INI/Properties source format of a string.
	 */
	private static void toPropertyString(final StringBuilder sb, final String s) {
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
					if (ch < ' ' || !Character.isDefined(ch)) {
						sb.append(XonTools.genCharAsUTF(ch));
					} else {
						sb.append(ch);
					}
			}
		}
	}

	/** Convert value object to string;
	 * @param val object to be converted.
	 * @return object converted to string
	 */
	private static String valueToString(final Object val) {
		if (val == null) {
			return "";
		} else if (val instanceof InetAddress) {
			return ((InetAddress) val).toString().substring(1);
		} else if (val instanceof byte[]) {
			return new String(SUtils.encodeHex((byte[]) val), Charset.forName("ISO8859-2"));
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
	private static void toPropertyLine(final StringBuilder sb, final String name, final Object val) {
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
	public static final String toIniString(final Map<String, Object> map) {
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
				for (Map.Entry<String, Object> y : ((Map<String, Object>) val).entrySet()) {
					toPropertyLine(sb, y.getKey(), y.getValue());
				}
			}
		}
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	/** Add ini object to XML element element.
	 * @param ini ini object.
	 * @param el XML element where to add items.
	 */
	private static void iniToXml(final Map<String,Object> ini, final Element el){
		Object o;
		for (Map.Entry<String, Object> x: ini.entrySet()) {
			if (!((o = x.getValue()) instanceof Map)) {
				Element item = el.getOwnerDocument().createElementNS(
					XDConstants.XON_NS_URI_W, XDConstants.XON_NS_PREFIX + ":" + XonNames.X_VALUE);
				item.setAttribute(XonNames.X_KEYATTR, XonTools.toXmlName(x.getKey()));
				String s;
				if (o == null) {
					s = "null";
				} else if (o instanceof byte[]) {
					byte[] b = (byte[]) o;
					s = genXMLString(new String(b.length<=32 ? SUtils.encodeHex(b) : SUtils.encodeBase64(b)));
				} else {
					s = o.toString();
				}
				item.setAttribute(XonNames.X_VALATTR, s);
				el.appendChild(item);
			}
		}
		for (Map.Entry<String, Object> x: ini.entrySet()) {
			if ((o = x.getValue()) instanceof Map) {
				Element item = el.getOwnerDocument().createElementNS(
					XDConstants.XON_NS_URI_W, XDConstants.XON_NS_PREFIX + ":" + XonNames.X_MAP);
				item.setAttribute(XonNames.X_KEYATTR, XonTools.toXmlName(x.getKey()));
				iniToXml((Map<String, Object>) o, item);
				el.appendChild(item);
			}
		}
	}

	@SuppressWarnings("unchecked")
	/** Create XML Element from object.
	 * @param ini object wioth Windows ini data.
	 */
	public static final Element iniToXml(final Object ini) {
		Document doc = KXmlUtils.newDocument(
			XDConstants.XON_NS_URI_W, XDConstants.XON_NS_PREFIX + ":" + XonNames.X_MAP, null);
		Element el = doc.getDocumentElement();
		iniToXml((Map<String,Object>) ini, el);
		return el;
	}
}
