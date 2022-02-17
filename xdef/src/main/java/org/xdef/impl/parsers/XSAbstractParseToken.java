package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import static org.xdef.XDParser.BASE;
import static org.xdef.XDParser.ENUMERATION;
import static org.xdef.XDParser.LENGTH;
import static org.xdef.XDParser.MAXLENGTH;
import static org.xdef.XDParser.MINLENGTH;
import static org.xdef.XDParser.PATTERN;
import static org.xdef.XDParser.WHITESPACE;
import static org.xdef.XDParser.WS_COLLAPSE;
import org.xdef.XDValue;
import static org.xdef.XDValueID.XD_STRING;
import org.xdef.sys.SRuntimeException;

/** Abstract parser of tokens.
 * @author Vaclav Trojan
 */
public abstract class XSAbstractParseToken extends XSAbstractParser {
	XDValue[] _enumeration;
	long _minLength;
	long _maxLength;

	XSAbstractParseToken() {
		super();
		_whiteSpace = WS_COLLAPSE;
		_minLength = _maxLength = -1;
	}
	@Override
	public  void initParams() {
		_whiteSpace = WS_COLLAPSE;
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
	public byte getDefaultWhiteSpace() {return WS_COLLAPSE;}
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
	public void setParseSQParams(final Object... params) {
		if (params != null && params.length >= 1) {
			Object par1 = params[0];
			_minLength = Integer.parseInt(par1.toString());
			if (params.length == 1) {
				_maxLength = _minLength;
			} else if (params.length == 2) {
				_maxLength = Integer.parseInt(params[1].toString());
			} else {
				throw new SRuntimeException("Incorrect number of parameters");
			}
		}
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
					//Length of value of '&{0}' is too short&{0}'&{1}
					p.error(XDEF.XDEF814, parserName(), s);
					return;
				} else if (_maxLength!=-1 && s.length() > _maxLength) {
					//Length of value of '&{0}' is too long&{0}'{: }
					p.error(XDEF.XDEF815, parserName(), s);
					return;
				}
				checkEnumeration(p);
			}
		}
	}
	@Override
	public short parsedType() {return XD_STRING;}
}