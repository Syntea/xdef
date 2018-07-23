/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: XDParseQNameURIList.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.sys.StringParser;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.impl.code.DefElement;
import cz.syntea.xdef.impl.code.DefString;
import cz.syntea.xdef.impl.ext.XExtUtils;
import org.w3c.dom.Element;
import cz.syntea.xdef.XDContainer;

/** Parser of X-Script "QNameURIList" type.
 * @author Vaclav Trojan
 */
public class XDParseQNameURIList extends XDParseNCNameList {
	private static final String ROOTBASENAME = "QNameURIList";
	Element _elem;
	public XDParseQNameURIList() {
		super();
	}

	@Override
	public int getLegalKeys() {
		return PATTERN +
			ENUMERATION +
			WHITESPACE + //fixed collapse
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
//			ITEM +
//			BASE +
			ARGUMENT +
			SEPARATOR +
			0;
	}
	@Override
	XDValue parse(final XXNode xnode, final StringParser parser) {
		Element el = _elem == null ? xnode.getElement() : _elem;
		byte xmlVersion1 = "1.1".equals(el.getOwnerDocument().getXmlVersion())
			? (byte) 11 : (byte) 10;
		int pos = parser.getIndex();
		if (!parser.isXMLName(xmlVersion1)) {
			parser.error(XDEF.XDEF546); //QName expected
			return null;
		}
		String s = parser.getParsedBufferPartFrom(pos);
		if (XExtUtils.getQnameNSUri(s, el).length() == 0) {
			parser.error(XDEF.XDEF554);//Namespace not defined
			return null;
		}
		return new DefString(parser.getParsedString());
	}
	@Override
	public void addNamedParams(XDContainer map) {
		super.addNamedParams(map);
		if (_elem == null) {
			map.setXDNamedItem("argument", new DefElement(_elem));
		}
	}
	@Override
	public void setArgument(XDValue x) {_elem = x.getElement();}
	@Override
	public XDValue getArgument() {return new DefElement(_elem);}
	@Override
	public String parserName() {return ROOTBASENAME;}
}
