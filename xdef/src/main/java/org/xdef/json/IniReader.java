package org.xdef.json;

import java.io.Reader;
import java.net.URL;
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
	 * @param jp parser of XON source.
	 * @param source String with source data.
	 */
	public IniReader(final String source, final XonParser jp) {
		super(source);
		_jp = jp;
	}

	/** Create instance of parser.
	 * @param jp parser of XON source.
	 * @param source Reader with source data.
	 */
	public IniReader(final Reader source, final XonParser jp) {
		super(source, new ArrayReporter());
		_jp = jp;
	}

	/** Create instance of parser.
	 * @param jp parser of XON source.
	 * @param source URL with source data.
	 */
	public IniReader(final URL source, final XonParser jp) {
		super(source, new ArrayReporter(), 0);
		_jp = jp;
	}

	/** Set mode that Ini file is parsed in X-definition compiler. */
	public final void setXdefMode() { _jdef = true;}

	public Object getValue() {return _jp.getResult();}

////////////////////////////////////////////////////////////////////////////////
// interface XONParsers
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Parse JSON or XON source data (depends on the flag "_xon").
	 * @throws SRuntimeException if an error occurs,
	 */
	public final void parse() throws SRuntimeException {
		readIni();
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
			_jp.namedValue(new SBuffer(
				key.substring(0, key.length()-1).trim(), spos));
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

	private void readIni() {
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
}