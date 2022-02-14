package org.xdef.impl.parsers;

import org.xdef.XDContainer;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import static org.xdef.XDParser.BASE;
import static org.xdef.XDParser.ENUMERATION;
import static org.xdef.XDParser.ITEM;
import static org.xdef.XDParser.LENGTH;
import static org.xdef.XDParser.MAXLENGTH;
import static org.xdef.XDParser.MINLENGTH;
import static org.xdef.XDParser.PATTERN;
import static org.xdef.XDParser.WHITESPACE;
import static org.xdef.XDParser.WS_COLLAPSE;
import org.xdef.XDValue;
import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.XDValueID.XD_PARSER;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefParseResult;
import org.xdef.xon.XonTools;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;
import org.xdef.sys.SRuntimeException;

/** Parser of X-Script "jlist" type.
 * @author Vaclav Trojan
 */
public class XDParseJList extends XSAbstractParser {
	/** Name of parser. */
	private static final String ROOTBASENAME = "jlist";

	long _minLength;
	long _maxLength;
	XDParser _itemType;
	XDValue[] _enumeration;

	public XDParseJList() {
		super();
		_whiteSpace = WS_COLLAPSE;
		_minLength = _maxLength = -1;
	}

	@Override
	public void initParams() {
		_patterns = null;
		_enumeration = null;
		_itemType = null;
		_whiteSpace = WS_COLLAPSE;
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
			ITEM +
			BASE +
			0;
	}
	private void parse(final XXNode xnode,
		final XDParseResult p,
		boolean isFinal) {
		XDParser itemParser = _itemType != null ? _itemType // item parser
			: new XDParseJValue(); // default parser jvalue
		DefContainer results = new DefContainer();
		String source = p.getSourceBuffer();
		p.setSourceBuffer(source);
		int count = 0;
		p.isSpaces();
		if (p.isChar('[')) {
			for (;;) {
				p.isSpaces();
				if (p.eos()) {
					//Incorrect value of '&{0}'&{1}{: }
					p.errorWithString(XDEF.XDEF809, parserName());
					break; //error
				}
				if (p.isChar(']')) {
					break;
				}
				int start, end;
				start = p.getIndex();
				if (p.getCurrentChar() == '[') {
					DefParseResult q = new DefParseResult(source);
					q.setIndex(p.getIndex());
					parse(xnode, q, isFinal);
					p.addReports(q.getReporter());
					p.setParsedValue(q.getParsedValue());
					end = q.getIndex();
				} else if (p.isChar('"')) {
					String s = XonTools.readJString(p);
					end = p.getIndex();
					if (itemParser.parserName().charAt(0) == 'j') {
						p.setSourceBuffer(p.getBufferPart(0, end));
					} else {
						if (s == null) {
							s = "";
						}
						p.setSourceBuffer(p.getBufferPart(0,start) + s);
					}
					p.setIndex(start);
					itemParser.parseObject(xnode, p);
				} else {
					DefParseResult q;
					if (p.isToken("g(") || p.isToken("p(") || p.isToken("c(")) {
						p.setIndex(p.getIndex() - 2); //gps, price, char
						q = new DefParseResult(p.getUnparsedBufferPart());
						itemParser.parseObject(xnode, q);
						p.addReports(q.getReporter());
						p.setParsedValue(q.getParsedValue());
						end = start + q.getIndex();
					} else {
						char ch = 0;
						while (!p.eos() && (ch=p.getCurrentChar())>' '
							&& ch!=',' && ch!='[' && ch!=']') {
							p.nextChar();
						}
						end = p.getIndex();
						q = new DefParseResult(p.getBufferPart(start, end));
						itemParser.parseObject(xnode, q);
					}
					p.addReports(q.getReporter());
					p.setParsedValue(q.getParsedValue());
				}
				if (p.getReporter() != null && p.getReporter().errors()) {
					break;
				}
				XDValue result = p.getParsedValue();
				if (result != null) {
					results.addXDItem(p.getParsedValue());
				}
				p.setSourceBuffer(source);
				p.setIndex(end);
				count++;
				p.isSpaces();
				if (!p.isChar(',')) {
					if (!p.isChar(']')) {
						//Incorrect value of '&{0}'&{1}{: }
						p.errorWithString(XDEF.XDEF809, parserName());
					}
					break;
				}
			}
		}
		p.setParsedValue(results);
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
	public byte getDefaultWhiteSpace() {return WS_COLLAPSE;}
	@Override
	public boolean addTypeParser(XDValue x) {
		if (x.getItemId() != XD_PARSER) {
			//The value type in the named parameter '%item' in the parser '&{0}'
			// must be Parser
			throw new SRuntimeException(XDEF.XDEF474, parserName());
		}
		_itemType = (XDParser) x;
		return true;
	}
	@Override
	public void addNamedParams(XDContainer map) {
		if (_itemType != null) {
			map.setXDNamedItem("item", _itemType);
		}
	}
	@Override
	public void setItem(XDValue item) {
		if (item.getItemId() == XD_PARSER) {
			_itemType = (XDParser) item;
		} else {
			//Value of type '&amp;{0}' expected
			throw new SRuntimeException(XDEF.XDEF423, "Parser");
		}
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
	public short parsedType() {return XD_CONTAINER;}
	@Override
	public void parseObject(XXNode xnode, XDParseResult p) {
		parse(xnode, p, false);
	}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XSParseList)) {
			return false;
		}
		XDParseJList x = (XDParseJList) o;
		return _itemType == null ? false : _itemType.equals(x._itemType);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}