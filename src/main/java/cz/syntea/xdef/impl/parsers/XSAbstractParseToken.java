package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.XDValue;

/** Abstract parser of tokens.
 * @author Vaclav Trojan
 */
public abstract class XSAbstractParseToken extends XSAbstractParser {
	XDValue[] _enumeration;
	long _minLength;
	long _maxLength;

	XSAbstractParseToken() {
		super();
		_whiteSpace = 'c';
		_minLength = _maxLength = -1;
	}
	@Override
	public  void initParams() {
		_whiteSpace = 'c';
		_patterns = null;
		_enumeration = null;
		_minLength = _maxLength = -1;
	}
	@Override
	public int getLegalKeys() {
		return PATTERN +
			ENUMERATION +
			WHITESPACE + //fixed to collapse
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
	public byte getDefaultWhiteSpace() {return 'c';}
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
	/** Set value of one "sequential" parameter of parser.
	 * @param par "sequential" parameters.
	 */
	public void setParseParam(Object param) {
		_minLength = _maxLength = Long.parseLong(param.toString());
	}
	@Override
	/** Set value of two "sequential" parameters of parser.
	 * @param par1 the first "sequential" parameter.
	 * @param par2 the second "sequential" parameter.
	 */
	public void setParseParams(final Object par1, final Object par2) {
		_minLength = Long.parseLong(par1.toString());
		_maxLength = "*".equals(par2) ? -1 : Long.parseLong(par2.toString());
	}
	@Override
	public void setEnumeration(Object[] o) {
		_enumeration = null;
		if (o == null || o.length == 0) {
			return;
		}
		XDValue[] e = new XDValue[o.length];
		for (int i = 0; i < o.length; i++) {
			e[i] = iObject(null, o[i]);
		}
		_enumeration = e;
	}
	void checkItem(XDParseResult p) {
		if (p.matches()) {
			checkPatterns(p);
			if (p.matches()) {
				XDValue val = p.getParsedValue();
				String s = val.toString();
				if (_minLength!=-1 && s.length() < _minLength) {
					//Length of value of '&{0}' is too short
					p.error(XDEF.XDEF814, parserName());
					return;
				} else if (_maxLength!=-1 && s.length() > _maxLength) {
					//Length of value of '&{0}' is too long
					p.error(XDEF.XDEF815, parserName());
					return;
				}
				checkEnumeration(p);
			}
		}
	}
	@Override
	public short parsedType() {return XD_STRING;}
}