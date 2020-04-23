package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefContainer;
import org.xdef.XDContainer;
import org.xdef.XDValueID;

/** Parser of X-Script "sequence" type.
 * @author Vaclav Trojan
 */
public class XDParseSequence extends XSAbstractParser {
	private static final String ROOTBASENAME = "sequence";

	XDParser[] _itemTypes;
	String _separator;
	XDValue[] _enumeration;
	long _minLength;
	long _maxLength;

	public XDParseSequence() {
		super();
		_minLength = _maxLength = -1;
	}

	@Override
	public void initParams() {
		_patterns = null;
		_enumeration = null;
		_minLength = _maxLength = -1;
		_separator = null;
		_itemTypes = null;
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
//			LENGTH +
//			MAXLENGTH +
//			MINLENGTH +
//			NORMALIZE +
			SEPARATOR +
			ITEM +
			BASE +
			0;
	}
	@Override
	public byte getDefaultWhiteSpace() {return 0;}
	@Override
	public boolean addTypeParser(final XDParser x) {
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
	public void setLength(final long x) {_minLength = _maxLength = x;}
	@Override
	public long getLength() {return _minLength == _maxLength ? _minLength: -1;}
	@Override
	public void setMaxLength(final long x) {_maxLength = x;}
	@Override
	public long getMaxLength() {return _maxLength;}
	@Override
	public void setMinLength(final long x) {_minLength = x;}
	@Override
	public long getMinLength() {return _minLength;}
	@Override
	public XDValue[] getEnumeration() {return _enumeration;}
	@Override
	public void setEnumeration(final Object[] o) {
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
	public void setItem(final XDValue item) {
		if (item.getItemId() != XDValueID.XD_CONTAINER) {
			addTypeParser((XDParser) item);
			return;
		}
		DefContainer c = (DefContainer) item;
		for (int i = 0; i < c.getXDItemsNumber(); i++) {
			addTypeParser((XDParser) c.getXDItem(i));
		}
	}
	@Override
	public void check(final XXNode xnode, final XDParseResult p) {
		parse(xnode, p, true);
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p) {
		parse(xnode, p, false);
	}
	private void parse(final XXNode xnode,
		final XDParseResult p,
		boolean isFinal) {
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos1 = p.getIndex();
		DefContainer val = new DefContainer();
		_itemTypes[0].parseObject(xnode, p);
		String s = p.getParsedBufferPartFrom(pos1);
		if (p.errors()) {
			return;
		}
		if (isFinal) {
			_itemTypes[0].finalCheck(xnode, p);
		}
		val.addXDItem(p.getParsedValue());
		for (int i = 1; i < _itemTypes.length; i++) {
			p.isSpaces();
			pos1 = p.getIndex();
			_itemTypes[i].parseObject(xnode, p);
			if (isFinal) {
				_itemTypes[i].finalCheck(xnode, p);
			}
			if (p.matches()) {
				val.addXDItem(p.getParsedValue());
				s += ' ' + p.getParsedBufferPartFrom(pos1);
			} else {
				 //'&{0}' expected
				p.error(XDEF.XDEF570, _itemTypes[i].parserName());
				return;
			}
		}
		if (isFinal) {
			p.isSpaces();
		}
		p.setParsedValue(val);
		if (_enumeration != null) {
			boolean found = false;
			for (int i = 0; i < _enumeration.length; i++) {
				if (_enumeration[i].equals(p.getParsedValue())) {
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
		if (_minLength != -1 && val.getXDItemsNumber() < _minLength) {
			//Length of value of '&{0}' is too short&{0}'{: }
			p.errorWithString(XDEF.XDEF814, parserName());
		} else if (_maxLength != -1 && val.getXDItemsNumber() > _maxLength) {
			//Length of value of '&{0}' is too long&{0}'{: }
			p.errorWithString(XDEF.XDEF815, parserName());
		}
		p.replaceParsedBufferFrom(pos0, s);
		if (isFinal) {
			if (!p.eos()) {
				//After the item '&{0}' follows an illegal character&{1}{: }
				p.errorWithString(XDEF.XDEF804, parserName());
			}
		}
		//replace source from pos0 to actual position by 's' and set
		//the actual position after it.
	}
	@Override
	public void addNamedParams(final XDContainer map) {
		map.setXDNamedItem("item", new DefContainer(_itemTypes));
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_CONTAINER;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseSequence)) {
			return false;
		}
		XDParseSequence x = (XDParseSequence) o;
		if (_minLength != x._minLength || _maxLength != x._maxLength) {
			return false;
		}
		if (_itemTypes == null || _itemTypes.length == 0) {
			if (x._itemTypes != null && x._itemTypes.length != 0) {
				return false;
			}
		} else {
			if (x._itemTypes==null || _itemTypes.length!=x._itemTypes.length) {
				return false;
			}
			for (int i = 0; i < _enumeration.length; i++) {
				if (!_enumeration[i].equals(x._enumeration[i])) {
					return false;
				}
			}
		}
		if (_enumeration == null || _enumeration.length == 0) {
			return x._enumeration == null || x._enumeration.length == 0;
		} else {
			if (x._enumeration == null ||
				_enumeration.length != x._enumeration.length) {
				return false;
			}
			for (int i = 0; i < _enumeration.length; i++) {
				if (!_enumeration[i].equals(x._enumeration[i])) {
					return false;
				}
			}
			return true;
		}
	}
}