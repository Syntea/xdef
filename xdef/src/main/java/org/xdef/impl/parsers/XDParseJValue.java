package org.xdef.impl.parsers;

import org.xdef.XDContainer;
import org.xdef.XDParser;
import org.xdef.XDValue;

/** Parser of X-Script "jvalue" type.
 * @author Vaclav Trojan
 */
public class XDParseJValue extends XSParseUnion {
	private static final String ROOTBASENAME = "jvalue";

	public XDParseJValue() {
		super();
		_itemTypes = new XDParser[]{
			new XDParseJNull(),
			new XDParseJBoolean(),
			new XDParseJNumber(),
			new XDParseJString()};
	}

	@Override
	public  void initParams() {
		_patterns = null;
		_enumeration = null;
		_itemTypes = new XDParser[]{
			new XDParseJNull(),
			new XDParseJBoolean(),
			new XDParseJNumber(),
			new XDParseJString()};
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
//			LENGTH +
//			MAXLENGTH +
//			MINLENGTH +
//			NORMALIZE +
//			SEPARATOR +
//			ITEM +
			BASE +
			0;
	}
	@Override
	public void addNamedParams(final XDContainer map) {}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (o == null || !(o instanceof XDParseJValue)) {
			return false;
		}
		XDParseJValue x = (XDParseJValue) o;
		if (_enumeration == null || _enumeration.length == 0) {
			if (x._enumeration != null && x._enumeration.length != 0) {
				return false;
			}
		} else if (x._enumeration == null
			|| _enumeration.length != x._enumeration.length) {
			return false;
		}
		for (int i = 0; i < _enumeration.length; i++) {
			if (!_enumeration[i].equals(x._enumeration[i])) {
				return false;
			}
		}
		return getNamedParams().equals(x.getNamedParams());
	}
}