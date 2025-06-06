package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefString;
import java.util.StringTokenizer;
import static org.xdef.XDParser.BASE;
import static org.xdef.XDParser.ENUMERATION;
import static org.xdef.XDParser.LENGTH;
import static org.xdef.XDParser.MAXLENGTH;
import static org.xdef.XDParser.MINLENGTH;
import static org.xdef.XDParser.PATTERN;
import static org.xdef.XDParser.WHITESPACE;
import static org.xdef.XDParser.WS_COLLAPSE;
import static org.xdef.XDParser.WS_REPLACE;
import static org.xdef.XDParserAbstract.checkCharset;
import static org.xdef.XDValueID.XD_STRING;
import org.xdef.sys.SRuntimeException;

/** Abstract parser of a strings.
 * @author Vaclav Trojan
 */
public abstract class XSAbstractParseString extends XSAbstractParser {
	protected long _minLength;
	protected long _maxLength;
	protected DefString[] _enumeration;

	public XSAbstractParseString() {super(); _minLength = _maxLength = -1;}

	@Override
	public  void initParams() {_patterns = null; _enumeration = null; _minLength = _maxLength = -1;}

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
	public void setParseSQParams(final Object... params) {
		if (params != null && params.length >= 1) {
			Object par1 = params[0];
			_minLength = Integer.parseInt(par1.toString());
			switch(params.length) {
				case 1: _maxLength = _minLength; break;
				case 2: _maxLength = Integer.parseInt(params[1].toString()); break;
				default: //Too many parameters for method &{0}
					throw new SRuntimeException(XDEF.XDEF461, parserName());
			}
		}
	}

	@Override
	public void setEnumeration(final Object[] o) {
		_enumeration = null;
		if (o == null || o.length == 0) {
			return;
		}
		DefString[] e = null;
		//the list of strings must be sorted by length down
	loop:
		for (Object o1 : o) {
			DefString s = (DefString) iObject(null, o1);
			if (e == null) {
				e = new DefString[]{s};
			} else {
				for(DefString d: e) {
					if (d.equals(s)) continue loop; //already is in enumeration
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
	public void parseObject(final XXNode xn, final XDParseResult p){
		int pos0 = p.getIndex();
		boolean quotedString = xn != null && xn.getXonMode() > 0 && p.isChar('"');
		if (_whiteSpace == WS_COLLAPSE) {
			p.isSpaces();
		}
		String s;
		if (_enumeration != null) {
			checkEnumeration(p, xn);
			if (p.errors()) {
				return;
			}
			s = p.getParsedValue().toString();
			if (quotedString && s.endsWith("\"") &&  s.length() > 1) {
				s = s.substring(0, s.length() - 1);
			}
			if (_whiteSpace == WS_COLLAPSE) {
				p.isSpaces();
			}
		} else if (_whiteSpace == WS_COLLAPSE) {//collapse
			StringBuilder sb = new StringBuilder();
			while((s = p.nextToken()) != null) {
				sb.append(s);
				if (p.isSpaces()) {
					if (quotedString) {
						if  (p.isChar('"')) {
							sb.append('"');
							if (p.eos()) {
								break;
							}
						} else {
							sb.append(" \"");
						}
					}
					if (!p.eos()) {
						sb.append(' ');
					} else {
						break;
					}
				}
			}
			p.isSpaces();
			s = sb.toString();
			if (quotedString && s.endsWith("\"")) {
				s = s.substring(0, s.length() - 1).trim(); // remove '"' from the end of string
			}
		} else {//preserve or replace
			s = p.getUnparsedBufferPart();
			if (quotedString && s.endsWith("\"")) {
				s = s.substring(0, s.length() - 1);
			}
			if (_whiteSpace == WS_REPLACE) { //replace
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
		if (quotedString) { // JSON string
			s = '"' + s + '"';
			p.replaceParsedBufferFrom(pos0, s);
			p.setParsedValue(s);
		}
		checkCharset(xn, p);
	}

	void checkLength(XDParseResult p) {
		if (p.matches()) {
			String s = p.getParsedValue().toString();
			int len = s.length();
			if (_minLength != -1 && len < _minLength) {
				//Length of value of '&{0}' is too short"&{0}'&{1}
				p.errorWithString(XDEF.XDEF814, parserName());
			} else if (_maxLength != -1 && len > _maxLength) {
				p.errorWithString(XDEF.XDEF815, parserName());//Length of value of '&{0}' is too long&{0}'{: }
			}
		}
	}

	void checkEnumeration(final XDParseResult p, final XXNode xnode) {
		if (p.matches()) {
			boolean found = false;
			int i = 0;
			if (_whiteSpace == WS_COLLAPSE) {//collapse
				int start = p.getIndex();
				loop:
				for (; i < _enumeration.length; i++) {
					StringTokenizer t = new StringTokenizer(_enumeration[i].toString(), " \t\n\r");
					if (t.hasMoreTokens()) {
						String s = t.nextToken();
						while (p.isToken(s)) {
							if (t.hasMoreTokens()) {
								if (!p.isSpaces()) {
									p.setIndex(start);
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
				p.errorWithString(XDEF.XDEF810, parserName());//Doesn't fit enumeration list of &{0}&{1}{: }
			} else {
				p.setParsedValue(_enumeration[i]);
			}
		}
	}

	@Override
	public short parsedType() {return XD_STRING;}
}
