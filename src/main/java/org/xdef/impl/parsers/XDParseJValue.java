package org.xdef.impl.parsers;

import org.xdef.XDParser;

/** Parser of X-Script "jvalue" type.
 * @author Vaclav Trojan
 */
public class XDParseJValue  extends XSParseUnion {
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
	public String parserName() {return ROOTBASENAME;}
}