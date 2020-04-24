package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.SUtils;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefBytes;
import java.io.ByteArrayOutputStream;
import java.io.Reader;

/** Parser of Schema "base64Binary" type.
 * @author Vaclav Trojan
 */
public class XSParseBase64Binary extends XSAbstractParser {
	private static final String ROOTBASENAME = "base64Binary";
	long _minLength;
	long _maxLength;
	XDValue[] _enumeration;

	public XSParseBase64Binary() {
		super();
		_whiteSpace = 'c';
		_minLength = _maxLength = -1;
	}
	@Override
	public void initParams() {
		_whiteSpace = 'c';
		_patterns = null;
		_enumeration = null;
		_minLength = _maxLength = -1;
	}
	@Override
	public byte getDefaultWhiteSpace() {return 'c';}
	@Override
	public int getLegalKeys() {
		return PATTERN +
			ENUMERATION +
			WHITESPACE + //fixed collapse
//			MAXINCLUSIVE +
//			MAXEXCLUSIVE +
//			MININCLUSIVE +
//			MINEXCLUSIVE +
//			TOTALDIGITS +
//			FRACTIONDIGITS +
			LENGTH +
			MAXLENGTH +
			MINLENGTH +
//			NORMALIZE +
//			SEPARATOR +
//			ITEM +
			BASE +
			0;
	}
	@Override
	public void setLength(long x) { _minLength = _maxLength = x; }
	@Override
	public long getLength() {return _minLength == _maxLength ? _minLength: -1;}
	@Override
	public void setMaxLength(long x) { _maxLength = x; }
	@Override
	public long getMaxLength() { return _maxLength; }
	@Override
	public void setMinLength(long x) { _minLength = x; }
	@Override
	public long getMinLength() { return _minLength; }
	@Override
	public XDValue[] getEnumeration() {return _enumeration;}
	@Override
	public void setEnumeration(Object[] o) {
		if (o == null || o.length == 0) {
			return;
		}
		XDValue[] e = new XDValue[o.length];
		for (int i = 0; i < o.length; i++) {
			e[i] = iObject(null, o[i]);
		}
		_enumeration = e;
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		try {
			XSParseReader r = new XSParseReader(p);
			ByteArrayOutputStream bw = new ByteArrayOutputStream();
			SUtils.decodeBase64(r, bw);
			r.close();
			p.setParsedValue(new DefBytes(bw.toByteArray()));
			String s = r.getParsedString();
			p.isSpaces();
			p.replaceParsedBufferFrom(pos0, s);
			checkPatterns(p);
			check(p);
		} catch (Exception ex) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
		}
	}
	/** Check XDParseResult on pattern, enumeration, length.
	 * @param p object to be checked.
	 * @return result with added errors.
	 */
	void check(XDParseResult p) {
		if (p.matches()) {
			checkEnumeration(p);
			checkPatterns(p);
			DefBytes bytes = (DefBytes) p.getParsedValue();
			if (_minLength != -1 && bytes.getBytes().length < _minLength) {
				//Length of value of '&{0}' is too short&{0}'&{1}
				p.errorWithString(XDEF.XDEF814,  parserName());
			} else if (_maxLength != -1 && bytes.getBytes().length>_maxLength) {
				//Length of value of '&{0}' is too long&{0}'{: }
				p.errorWithString(XDEF.XDEF815, parserName());
			}
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_BYTES;}
	/** This class is used as reader of parsed string in XSParseHexBinary
	 * and in XSParseBase94Binary.
	 */
	private static final class XSParseReader extends Reader {
		XDParseResult _p;
		StringBuffer _sb;

		XSParseReader(XDParseResult p) {
			_p = p;
			_sb = new StringBuffer();
		}
		@Override
		public final int read(char[] cbuf, int off, int len) {
			int x;
			int i = off;
			for (; len > 0; i++, len--) {
				if ((x = read()) > 0) {
					cbuf[off] = (char) x;
				} else {
					i--;
					break;
				}

			}
			return i > off ? off - i : -1;
		}
		@Override
		public final int read() {
			if (_p.isSpaces()) {
				if (_p.eos()) {
					return -1;
				}
				_sb.append(' ');
			}
			char result;
			if ((result = _p.peekChar()) > 0) {
				_sb.append(result);
				return result;
			}
			return -1;
		}
		@Override
		public final void close() {}

		final String getParsedString() {
			close();
			String result = _sb.toString().trim();
			_sb = null;
			_p = null;
			return result;
		}
	}
}