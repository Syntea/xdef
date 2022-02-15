package org.xdef.impl.parsers;

import org.xdef.sys.SRuntimeException;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefParseResult;
import org.xdef.msg.XDEF;
import org.xdef.XDContainer;
import static org.xdef.XDParser.BASE;
import static org.xdef.XDParser.ENUMERATION;
import static org.xdef.XDParser.ITEM;
import static org.xdef.XDParser.LENGTH;
import static org.xdef.XDParser.MAXLENGTH;
import static org.xdef.XDParser.MINLENGTH;
import static org.xdef.XDParser.PATTERN;
import static org.xdef.XDParser.WHITESPACE;
import static org.xdef.XDParser.WS_COLLAPSE;
import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.XDValueID.XD_PARSER;

/** Parser of Schema "list" type.
 * @author Vaclav Trojan
 */
public class XSParseList extends XSAbstractParser {
	private static final String ROOTBASENAME = "list";
	XDParser _itemType;
	long _minLength;
	long _maxLength;
	XDValue[] _enumeration;

	public XSParseList() {
		super();
		_whiteSpace = WS_COLLAPSE;
		_minLength = _maxLength = -1;
	}
	@Override
	public  void initParams() {
		_whiteSpace = WS_COLLAPSE;
		_patterns = null;
		_enumeration = null;
		_itemType = null;
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
	@Override
	public byte getDefaultWhiteSpace() {return WS_COLLAPSE;}
	@Override
	public boolean addTypeParser(XDValue x) {
		_itemType = getParserFromValue(x);
		return true;
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
	public void setItem(XDValue item) {
		if (item.getItemId() == XD_PARSER) {
			_itemType = (XDParser) item;
		} else {
			//Value of type '&amp;{0}' expected
			throw new SRuntimeException(XDEF.XDEF423, "Parser");
		}
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
	public void check(final XXNode xnode, final XDParseResult p) {
		parse(xnode, p, true);
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		parse(xnode, p, false);
	}
	private void parse(final XXNode xnode,
		final XDParseResult p,
		boolean isFinal){
		int pos0 = p.getIndex();
		p.isSpaces();
		String t = p.nextToken();
		DefContainer results = new DefContainer();
		if (t == null) {
			if (_minLength > 0) {
				//"Value of '&{0}' doesn't fit to '&{1}'&{2}{: }
				p.error(XDEF.XDEF813, parserName(), "minLength", t);
			}
			return;
		}
		p.setParsedValue(results);
		int count = 0;
		String s = "";
		while(t.length() > 0) {
			count++;
			if (_maxLength != -1 && count > _maxLength) {
				//"Value of '&{0}' doesn't fit to '&{1}'&{2}{: }
				p.error(XDEF.XDEF813, parserName(),
					(_maxLength == _minLength?"length":"maxLength"), t);
				return;
			}
			if (count == 1) {
				s = t;
			} else {
				s += ' ' + t;
			}
			XDParseResult r = new DefParseResult(t);
			_itemType.parseObject(xnode, r);
			if (r.errors()) {
				p.addReports(r.getReporter());
				return;
			} else {
				if (isFinal) {
					_itemType.finalCheck(xnode, r);
					if (r.errors()) {
						p.addReports(r.getReporter());
					}
				}
				XDValue val = r.getParsedValue();
				results.addXDItem(val);
				int pos1 = p.getIndex();
				p.isSpaces();
				t = p.nextToken();
				if (t == null) {
					p.setIndex(pos1);
					break;
				}
			}
		}
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
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
	public void addNamedParams(XDContainer map) {
		if (_itemType != null) {
			map.setXDNamedItem("item", _itemType);
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_CONTAINER;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XSParseList)) {
			return false;
		}
		XSParseList x = (XSParseList) o;
		if (_itemType == null) {
			return false;
		} else {
			return _itemType.equals(x._itemType);
		}
	}
}