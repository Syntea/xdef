package org.xdef.impl.parsers;

/** Parser of Schema "float" type.
 * @author Vaclav Trojan
 */
public class XSParseFloat extends XSParseDouble {
	private static final String ROOTBASENAME = "float";

	public XSParseFloat() {
		super();
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_FLOAT;}
}