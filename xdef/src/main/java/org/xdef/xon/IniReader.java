package org.xdef.xon;

import java.io.Reader;
import java.net.URL;
import java.util.Map;
import org.xdef.msg.JSON;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.StringParser;

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

	/** Set mode that INI file is parsed in X-definition compiler. */
	public final void setXdefMode() { _jdef = true;}

	public Object getValue() {return _jp.getResult();}

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
				if (p.getCurrentChar() =='\\') {
					int i = XonTools.readJChar(p);
					if (i < 0) {
						throw new RuntimeException("");
					}
					val += (char) i;
				} else {
					val += p.peekChar();
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
			if (p.findChar(']')) {
				String s = p.getBufferPart(spos.getIndex(), p.getIndex());
				_jp.namedValue(new SBuffer(s.trim(), spos));
				_jp.mapStart(spos);
				while (putProperty(prop = readPropText())) {}
				_jp.mapEnd(spos);
			} else {
				throw new RuntimeException("] missing");
			}
		}
		_jp.mapEnd(this);
	}
	
////////////////////////////////////////////////////////////////////////////////
// INI to String
////////////////////////////////////////////////////////////////////////////////
	
	/** Create INI/Properties source format of a string.
	 * @param s the string to be converted.
	 * @return INI/Properties source format of a string.
	 */
	private static String toPropertyString(final String s) {
		StringBuilder sb = new StringBuilder();
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
		return sb.toString();
	}
	
	/** Create the line of INI/Property item. 
	 * @param name name of INI/Property item.
	 * @param val string with value of INI/Property item.
	 * @return string with line with INI/Property item.
	 */
	private static String toPropertyLine(final String name, final String val) {
		return toPropertyString(name) + "=" + toPropertyString(val) + "\n";
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
			if (val instanceof String) {
				sb.append(toPropertyLine(x.getKey(), (String) val));
			}
		}
		for (Map.Entry<String, Object> x: map.entrySet()) {
			Object val = x.getValue();
			if (val instanceof Map) {
				sb.append('[').append(x.getKey()).append("]\n");
				for (Map.Entry<String, Object> y 
					: ((Map<String, Object>) val).entrySet()) {
					sb.append(toPropertyLine(y.getKey(), (String) y.getValue()));
				}
			}
		}
		return sb.toString();
	}
}