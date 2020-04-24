package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefString;
import java.util.StringTokenizer;

/** Abstract parser of a strings.
 * @author Vaclav Trojan
 */
public abstract class XSAbstractParseString extends XSAbstractParser {
	protected long _minLength;
	protected long _maxLength;
	protected DefString[] _enumeration;
//	boolean _trimTo = false;

	public XSAbstractParseString() {
		super();
		_minLength = _maxLength = -1;
	}

	@Override
	public  void initParams() {
		_patterns = null;
		_enumeration = null;
		_minLength = _maxLength = -1;
//		_trimTo = false;
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
		DefString[] e = null;
		//the list of strings must be sorted by length down
	loop:
		for (int i = 0; i < o.length; i++) {
			DefString s = (DefString) iObject(null, o[i]);
			if (e == null) {
				e = new DefString[]{s};
			} else {
				for (int j = 0; j < e.length; j++) {
					if (e[j].equals(s)) {//already is in enumeration
						continue loop;
					}
				}
				//longer strings must preceed shorter ones.
				DefString[] old = e;
				e = new DefString[old.length + 1];
				int len = s.toString().length();
				int j = 0;
				while (j < old.length) {
					if (old[j].toString().length() < len) {
						break;
					}
					e[j] = old[j];
					j++;
				}
				e[j] = s;
				for (; j < old.length; j++) {
					e[j + 1] = old[j];
				}
			}
		}
		_enumeration = e;
	}

	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		if (_whiteSpace == 'c') {
			p.isSpaces();
		}
		String s;
		if (_enumeration != null) {
			checkEnumeration(p, xnode);
			if (p.errors()) {
				return;
			}
			s = p.getParsedValue().toString();
			if (_whiteSpace == 'c') {
				p.isSpaces();
			}
		} else if (_whiteSpace == 'c') {//collapse
			StringBuilder sb = new StringBuilder();
			while((s = p.nextToken()) != null) {
				sb.append(s);
				if (p.isSpaces()) {
					if (!p.eos()) {
						sb.append(' ');
					} else {
						break;
					}
				} else {
					break;
				}
			}
			s = sb.toString();
			if (_whiteSpace == 'c') {
				p.isSpaces();
			}
		} else {//preserve or replace
			s = p.getUnparsedBufferPart();
			if (s == null) {
				s = "";
			}
			if (_whiteSpace == 'r') { //replace
				s = s.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
			}
		}
		if (s == null) {
			s = "";
		}
		p.replaceParsedBufferFrom(pos0, s);
		p.setParsedValue(s);
		checkPatterns(p);
		checkLength(p);
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
			boolean found = false;
			int i = 0;
			if (_whiteSpace == 'c') {//collapse
				int start = p.getIndex();
				loop:
				for (; i < _enumeration.length; i++) {
					StringTokenizer t = new StringTokenizer(
						_enumeration[i].toString(), " \t\n\r");
					if (t.hasMoreTokens()) {
						String s = t.nextToken();
						while (p.isToken(s)) {
							if (t.hasMoreTokens()) {
								if (!p.isSpaces()) {
									p.setBufIndex(start);
									break;
								}
								s = t.nextToken();
							} else {
								found = true;
								break loop;
							}
						}
					}
				}
			} else {
				for (; i < _enumeration.length; i++) {
					if (p.isToken(_enumeration[i].toString())) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				//Doesn't fit enumeration list of &{0}&{1}{: }
				p.errorWithString(XDEF.XDEF810, parserName());
			} else {
				p.setParsedValue(_enumeration[i]);
			}
		}
	}
	@Override
	public short parsedType() {return XD_STRING;}
}