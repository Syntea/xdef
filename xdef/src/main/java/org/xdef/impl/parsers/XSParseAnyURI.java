package org.xdef.impl.parsers;

import java.net.URI;
import java.net.URISyntaxException;
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
import static org.xdef.XDValueID.XD_ANYURI;
import org.xdef.impl.code.DefURI;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;
import org.xdef.sys.SRuntimeException;

/** Parser of Schema "anyURI" type.
 * @author Vaclav Trojan
 */
public class XSParseAnyURI extends XSAbstractParser {
	private static final String ROOTBASENAME = "anyURI";

	protected long _minLength;
	protected long _maxLength;
	protected DefURI[] _enumeration;

	public XSParseAnyURI() {
		super();
		_whiteSpace = WS_COLLAPSE;
		_minLength = _maxLength = -1;
	}
	@Override
	public void initParams() {
		_patterns = null;
		_enumeration = null;
		_minLength = _maxLength = -1;
		_whiteSpace = WS_COLLAPSE;
	}
	@Override
	public int getLegalKeys() {
		return PATTERN +
			ENUMERATION +
			WHITESPACE +
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
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		String s = p.nextToken();
		try {
			p.setParsedValue(new DefURI(new URI(s)));
		} catch (URISyntaxException ex) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
			return;
		}
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		checkPatterns(p);
		checkLength(p);
		checkEnumeration(p);
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
		DefURI[] e = null;
		//the list of strings must be sorted by length down
	loop:
		for (int i = 0; i < o.length; i++) {
			DefURI x = (DefURI) iObject(null, o[i]);
			if (e == null) {
				e = new DefURI[]{x};
			} else {
				for (int j = 0; j < e.length; j++) {
					if (e[j].equals(x)) {//already is in enumeration
						continue loop;
					}
				}
				DefURI[] old = e;
				e = new DefURI[old.length + 1];
				System.arraycopy(old, 0, e, 0, old.length);
				e[old.length] = x;
			}
		}
		_enumeration = e;
	}
	void checkLength(XDParseResult p) {
		if (p.matches()) {
			String s = p.getParsedValue().toString();
			int len = s.length();
			if (_minLength != -1 && len < _minLength) {
				//Length of value of '&{0}' is too short"&{0}'&{1}
				p.errorWithString(XDEF.XDEF814, parserName());
			} else if (_maxLength != -1 && len > _maxLength) {
				//Length of value of '&{0}' is too long&{0}'{: }
				p.errorWithString(XDEF.XDEF815, parserName());
			}
		}
	}
	void checkEnumeration(XDParseResult p, XXNode xnode) {
		if (p.matches()) {
			if (_enumeration == null || _enumeration.length == 0) {
				return;
			}
			DefURI u = (DefURI) p.getParsedValue();
			for (DefURI x : _enumeration) {
				if (x.equals(u)) {
					return;
				}
			}
			//Doesn't fit enumeration list of &{0}&{1}{: }
			p.errorWithString(XDEF.XDEF810, parserName());
		}
	}
	@Override
	public short parsedType() {return XD_ANYURI;}
	@Override
	public String parserName() {return ROOTBASENAME;}
}