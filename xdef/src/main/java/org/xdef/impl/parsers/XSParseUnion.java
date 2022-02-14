package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.SRuntimeException;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefContainer;
import org.xdef.XDContainer;
import static org.xdef.XDParser.BASE;
import static org.xdef.XDParser.ENUMERATION;
import static org.xdef.XDParser.ITEM;
import static org.xdef.XDParser.PATTERN;
import static org.xdef.XDParser.WS_COLLAPSE;
import static org.xdef.XDParser.WS_PRESERVE;
import static org.xdef.XDValueID.XD_ANY;
import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.XDValueID.XD_PARSER;

/** Parser of Schema "union" type.
 * @author Vaclav Trojan
 */
public class XSParseUnion extends XSAbstractParser {
	private static final String ROOTBASENAME = "union";
	XDParser[] _itemTypes = new XDParser[0];
	XDValue[] _enumeration;

	public XSParseUnion() {super();}
	@Override
	public  void initParams() {
		_patterns = null;
		_enumeration = null;
		_itemTypes = null;
	}
	@Override
	public int getLegalKeys() {
		return PATTERN +
			ENUMERATION +
//			WHITESPACE + // depends on items
//			MAXINCLUSIVE +
//			MAXEXCLUSIVE +
//			MININCLUSIVE +
//			MINEXCLUSIVE +
//			TOTALDIGITS +
//			FRACTIONDIGITS +
//			LENGTH +
//			MAXLENGTH +
//			MINLENGTH +
//			NORMALIZE +
//			SEPARATOR +
			ITEM +
			BASE +
			0;
	}
	@Override
	public byte getDefaultWhiteSpace() {return WS_PRESERVE;}
	@Override
	public boolean addTypeParser(XDValue x) {
		if (x.getItemId() != XD_PARSER) {
			//The value type in the named parameter '%item' of the parser '&{0}'
			// must be Parser
			throw new SRuntimeException(XDEF.XDEF474, parserName());
		}
		if (_itemTypes == null) {
			_itemTypes = new XDParser[1];
			_itemTypes[0] = (XDParser) x;
			return true;
		}
		XDParser[] old = _itemTypes;
		_itemTypes = new XDParser[old.length + 1];
		System.arraycopy(old, 0, _itemTypes, 0, old.length);
		_itemTypes[old.length] = (XDParser) x;
		return true;
	}
	@Override
	public void setItem(XDValue item) { //%item
		if (item.getItemId() == XD_CONTAINER) { // array of parsers
			DefContainer c = (DefContainer) item;
			for (int i = 0; i < c.getXDItemsNumber(); i++) {
				addTypeParser(c.getXDItem(i));
			}
		} else { // only one parser.
			addTypeParser(item);
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
		final boolean isFinal) {
		if ( _itemTypes == null) {
			//Incorrect value&{0}{ of '}{'}&{1}{: '}{'}
			p.error(XDEF.XDEF809, "union - parse methods missing");
			p.setSourceBuffer(p.getSourceBuffer());
			p.setEos();
			return;
		}
		int pos = p.getIndex();
		String source = p.getSourceBuffer();
		_whiteSpace = WS_PRESERVE;
		for (int i = 0; i < _itemTypes.length; i++) {
			if (isFinal) {
				_itemTypes[i].check(xnode, p);
			} else {
				_itemTypes[i].parseObject(xnode, p);
			}
			if (p.errors()) {
				p.setSourceBuffer(source);
				p.setIndex(pos);
				p.clearReports();
				continue;
			}
			if (isFinal) {
				finalCheck(xnode, p);
			}
			XDValue val = p.getParsedValue();
			if (_enumeration != null) {
				boolean found = false;
				for (int j = 0; j < _enumeration.length; j++) {
					if (_enumeration[j].equals(val)) {
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
			if (isFinal) {
				_whiteSpace = _itemTypes[i].getWhiteSpaceParam();
				if (_whiteSpace == WS_COLLAPSE) {
					p.isSpaces();
				}
				if (!p.eos()) {
					//After the item '&{0}' follows an illegal character&{1}{: }
					p.errorWithString(XDEF.XDEF804, parserName());
					p.setIndex(pos);
					continue;
				}
			}
			checkPatterns(p);
			return;
		}
		p.setIndex(pos);
		//Incorrect value of '&{0}'&{1}{: }
		p.errorWithString(XDEF.XDEF809, parserName());
	}
	@Override
	public void addNamedParams(XDContainer map) {
		map.setXDNamedItem("item", new DefContainer(_itemTypes));
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_ANY;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XSParseUnion)) {
			return false;
		}
		XSParseUnion x = (XSParseUnion) o;
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
}
