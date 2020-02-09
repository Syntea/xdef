package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.SRuntimeException;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefContainer;
import org.xdef.XDContainer;

/** Parser of Schema "union" type.
 * @author Vaclav Trojan
 */
public class XSParseUnion extends XSAbstractParser {
	private static final String ROOTBASENAME = "union";
	XDParser[] _itemTypes;
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
	public byte getDefaultWhiteSpace() {return 0;}
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
	public void setItem(XDValue item) {
		if (item.getItemId() == XD_PARSER) {
			addTypeParser((XDParser) item);
		} else if (item.getItemId() == XD_CONTAINER) {
			DefContainer c = (DefContainer) item;
			for (int i = 0; i < c.getXDItemsNumber(); i++) {
				addTypeParser((XDParser) c.getXDItem(i));
			}
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
		final boolean isFinal){
		int pos = p.getIndex();
		String source = p.getSourceBuffer();
		_whiteSpace = 0;
		for (int i = 0; i < _itemTypes.length; i++) {
			if (isFinal) {
				_itemTypes[i].check(xnode, p);
			} else {
				_itemTypes[i].parseObject(xnode, p);
			}
			if (p.errors()) {
				p.setSourceBuffer(source);
				p.setBufIndex(pos);
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
					//Doesn't fit enumeration list of '&{0}'
					p.error(XDEF.XDEF810, parserName());
					return;
				}
			}
			if (isFinal) {
				_whiteSpace = _itemTypes[i].getWhiteSpaceParam();
				if (_whiteSpace == 'c') {
					p.isSpaces();
				}
				if (!p.eos()) {
					//After the item '&{0}' follows an illegal character
					p.error(XDEF.XDEF804, parserName());
					p.setBufIndex(pos);
					continue;
				}
			}
			checkPatterns(p);
			return;
		}
		p.setBufIndex(pos);
		p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
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