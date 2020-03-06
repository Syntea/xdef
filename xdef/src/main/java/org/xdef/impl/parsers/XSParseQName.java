package org.xdef.impl.parsers;

/** Parser of Schema "QName" type.
 * @author Vaclav Trojan
 */
public class XSParseQName extends XSParseName {
	private static final String ROOTBASENAME = "QName";
	public XSParseQName() {super();}
	@Override
	public String parserName() {return ROOTBASENAME;}
}