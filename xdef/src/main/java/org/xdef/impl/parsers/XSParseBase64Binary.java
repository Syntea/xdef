package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.SUtils;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefBytes;
import java.io.ByteArrayOutputStream;
import static org.xdef.XDParser.BASE;
import static org.xdef.XDParser.ENUMERATION;
import static org.xdef.XDParser.LENGTH;
import static org.xdef.XDParser.MAXLENGTH;
import static org.xdef.XDParser.MINLENGTH;
import static org.xdef.XDParser.PATTERN;
import static org.xdef.XDParser.WHITESPACE;
import static org.xdef.XDValueID.XD_BYTES;
import org.xdef.sys.SParser;
import org.xdef.sys.SReader;

/** Parser of XML Schema "base64Binary" type.
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
	public void setLength(final long x) { _minLength = _maxLength = x; }
	@Override
	public long getLength() {return _minLength == _maxLength ? _minLength: -1;}
	@Override
	public void setMaxLength(final long x) { _maxLength = x; }
	@Override
	public long getMaxLength() { return _maxLength; }
	@Override
	public void setMinLength(final long x) { _minLength = x; }
	@Override
	public long getMinLength() { return _minLength; }
	@Override
	public XDValue[] getEnumeration() {return _enumeration;}
	@Override
	public void setEnumeration(final Object[] o) {
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
		try {
			XSParseReader r = new XSParseReader(p);
			ByteArrayOutputStream bw = new ByteArrayOutputStream();
			SUtils.decodeBase64(r, bw);
			p.setParsedValue(new DefBytes(bw.toByteArray(), false));
			int i;
			while ((i=r.read()) != -1) {
				if (i != ' ') {
					//Incorrect value of '&{0}'&{1}{: }
					p.errorWithString(XDEF.XDEF809,
						parserName(), p.getSourceBuffer());
					return;
				}
			}
			String s = r.getParsedString();
			r = null;
			p.replaceParsedBufferFrom(pos0, s);
			checkPatterns(p);
			check(p);
		} catch (Exception ex) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName(),
				p.getSourceBuffer());
		}
	}
	/** Check XDParseResult on pattern, enumeration, length.
	 * @param p object to be checked.
	 * @return result with added errors.
	 */
	void check(final XDParseResult p) {
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

	/** This class is used as reader of parsed string in XSParseBase94Binary. */
	static final class XSParseReader implements SReader {
		private final int _quoted;
		private final SParser _p;
		private final StringBuilder _sb;
		XSParseReader(final SParser p) {
			_quoted = (_p = p).isOneOfTokens("b(", "\"");
			_sb = new StringBuilder();
		}
		@Override
		public final int read() {
			if (_p.isSpaces()) {
				if (_p.eos()) {
					return -1;
				}
				_sb.append(' ');
			}
			char ch;
			if ((ch = _p.peekChar()) != SParser.NOCHAR
				&& !(_quoted == 0 && ch == ')' || _quoted == 1  && ch == '"')) {
				_sb.append(ch);
				return ch;
			}
			return -1;
		}

		private String getParsedString() {return _sb.toString();}
	}
}