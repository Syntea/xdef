package org.xdef.impl.parsers;

import org.xdef.XDContainer;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.code.DefContainer;
import org.xdef.msg.JSON;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SParser;
import org.xdef.sys.SRuntimeException;

/** Parser of X-Script "jlist" type.
 * @author Vaclav Trojan
 */
public class XDParseJList extends XSAbstractParser {
	private static final String ROOTBASENAME = "jlist";
	private static final String ESCCHARS_SRC = "u/\\\"bfnt";
	private static final String ESCCHARS = "u/\\\"bfnt";

	long _minLength;
	long _maxLength;
	XDParser[] _itemTypes;
	XDValue[] _enumeration;

	public XDParseJList() {
		super();
		_whiteSpace = WS_PEESERVE; //preserve!
		_minLength = _maxLength = -1;
	}

	@Override
	public void initParams() {
		_patterns = null;
		_enumeration = null;
		_itemTypes = null;
		_whiteSpace = WS_PEESERVE; //preserve!
		_minLength = _maxLength = -1;
	}
	@Override
	public int getLegalKeys() {
		return PATTERN +
			ENUMERATION +
//			WHITESPACE + //fixed to preserve
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
			ITEM +
//			BASE +
			0;
	}
	private void parse(final XXNode xnode,
		final XDParseResult p,
		boolean isFinal){
		DefContainer results = new DefContainer();
		String source = p.getSourceBuffer().trim();
		p.setSourceBuffer(source);
		ArrayReporter reporter = new ArrayReporter();
		int count = 0;
		while (count < _maxLength && !p.eos()) {
			for(int i = 0; i < _itemTypes.length; i++) {
				XDParser parser = _itemTypes[i];
				p.isSpaces();
				if (p.eos()) {
					break;
				}
				int start, end;
				start = p.getIndex();
				if (p.isChar('"')) {
					StringBuilder sb = new StringBuilder();
					for(;;) {
						char ch = p.nextChar();
						if (ch == SParser.NOCHAR) {
							// end of string ('"') is missing
							reporter.error(JSON.JSON001);
							break;
						} else if (ch == '"') {
							break;
						}
						if (ch == '\\') {
							int c = ESCCHARS_SRC.indexOf(p.nextChar());
							if (c == 0) { // \\u => UTF Character
								int x = 0;
								for (int j = 1; j < 4; j++) {
									int y = "0123456789abcdefABCDEF".indexOf(
										p.nextChar());
									if (y > 16) {
										y -= 6;
									}
									if (y < 0) {
										//hexadecimal digit expected
										reporter.error(JSON.JSON005);
									}
									x = (x << 4) + y;
								}
								sb.append((char) x);
							} else if (c >= 1){
								sb.append(ESCCHARS.charAt(i));
							} else {
								// Incorrect escape character in string
								reporter.error(JSON.JSON006);
							}
						} else {
							sb.append(ch);
						}
					}
					end = p.getIndex();
					if (parser.parserName().charAt(0) == 'j') {
//						end++;
						p.setSourceBuffer(p.getBufferPart(0, end));
					} else {
						p.setSourceBuffer(
							p.getBufferPart(0,start) + sb.toString());
					}
				} else {
					while (!p.eos() && p.getCurrentChar() > ' ') {
						p.nextChar();
					}
					end = p.getIndex();
					p.setSourceBuffer(p.getBufferPart(0, end));
				}
				p.setBufIndex(start);
				parser.parseObject(xnode, p);
				if (p.getReporter() != null && p.getReporter().errors()) {
					break;
				}
				XDValue result = p.getParsedValue();
				if (result != null) {
//					if (result.getItemId() == XD_CONTAINER) {
//						XDContainer c = (XDContainer) result;
//						for (XDValue x: c.getXDItems()) {
//							results.addXDItem(x);
//						}
//					} else {
						results.addXDItem(p.getParsedValue());
//					}
				}
				p.setSourceBuffer(source);
				p.setBufIndex(end);
			}
			count++;
		}
		p.setParsedValue(results);
//		p.isSpaces();
		if (_minLength != -1 && count < _minLength) {
			//Length of value of '&{0}' is too short&{0}'&{1}
			p.errorWithString(XDEF.XDEF814, parserName());
			return;
		}
		if (_enumeration != null) {
			boolean found = false;
			for (int j = 0; j < _enumeration.length; j++) {
				if (_enumeration[j].equals(results)) {
					found = true;
					break;
				}
			}
			if (!found) {
				//Doesn't fit enumeration list of '&{0}'&{1}{: }
				p.errorWithString(XDEF.XDEF810, parserName());
				return;
			}
		}
		checkPatterns(p);
	}
	@Override
	public void setItem(XDValue item) {
		if (item.getItemId() == XD_PARSER) {
			addTypeParser((XDParser) item);
		} else if (item.getItemId() == XD_CONTAINER) {
			DefContainer c = (DefContainer) item;
			for (int i = 0; i < c.getXDItemsNumber(); i++) {
				XDValue v = c.getXDItem(i);
				if (v instanceof XDParser) {
					addTypeParser((XDParser) v);
				} else {
					addTypeParser((XDParser) v);
				}
			}
		} else {
			//Value of type '&amp;{0}' expected
			throw new SRuntimeException(XDEF.XDEF423, "Parser");
		}
	}
	@Override
	public boolean addTypeParser(XDParser x) {
		if (_itemTypes == null) {
			_itemTypes = new XDParser[1];
			_itemTypes[0] = x;
			return true;
		}
		XDParser[] old = _itemTypes;
		_itemTypes = new XDParser[old.length + 1];
		System.arraycopy(old, 0, _itemTypes, 0, old.length);
		_itemTypes[old.length] = x;
		return true;
	}
	@Override
	public void addNamedParams(XDContainer map) {
		map.setXDNamedItem("item", new DefContainer(_itemTypes));
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
	public byte getDefaultWhiteSpace() {return WS_PEESERVE;}
	@Override
	public short parsedType() {return XD_CONTAINER;}
	@Override
	public void parseObject(XXNode xnode, XDParseResult p) {
		parse(xnode, p, false);
	}
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
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseJList)) {
			return false;
		}
		XDParseJList x = (XDParseJList) o;
		if (_itemTypes == null) {
			if (x._itemTypes != null) {
				return false;
			}
		} else {
			if (x._itemTypes == null ||
				_itemTypes.length != x._itemTypes.length) {
				return false;
			}
			for (int i = 0; i < _itemTypes.length; i++) {
				if (!_itemTypes[i].equals(x._itemTypes[i])) {
					return false;
				}
			}
		}
		return true;
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}