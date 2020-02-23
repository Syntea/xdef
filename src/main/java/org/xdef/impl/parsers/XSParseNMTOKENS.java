package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.StringParser;
import org.xdef.XDParseResult;
import org.xdef.impl.XConstants;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefString;

/** Parser of Schema "NMTOKENS" type.
 * @author Vaclav Trojan
 */
public class XSParseNMTOKENS extends XSAbstractParseToken {
	private static final String ROOTBASENAME = "NMTOKENS";

	public XSParseNMTOKENS() {
		super();
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		StringParser parser = new StringParser(p.getSourceBuffer(), pos);
		if (!parser.isNMToken(XConstants.XML10)) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
			return;
		}
		String s = parser.getParsedString();
		StringBuilder sb = new StringBuilder(s);
		DefContainer val = new DefContainer();
		val.addXDItem(new DefString(s));
		while (parser.isSpaces() && !parser.eos()) {
			if (!parser.isNMToken(XConstants.XML10)) {
				//Incorrect value of '&{0}'&{1}{: }
				p.errorWithString(XDEF.XDEF809, parserName());
				return;
			}
			sb.append(' ').append(s = parser.getParsedString());
			val.addXDItem(new DefString(s));
		}
		p.setBufIndex(parser.getIndex());
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, sb.toString());
		p.setParsedValue(val);
		if (_enumeration != null) {
			boolean found = false;
			for (int i = 0; i < _enumeration.length; i++) {
				if (_enumeration[i].equals(val)){
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
		if (p.errors()) {
			return;
		}
		if (_enumeration != null) {
			boolean found = false;
			for (int i = 0; i < _enumeration.length; i++) {
				if (_enumeration[i].equals(val)){
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
		if (_minLength!=-1 && val.getXDItemsNumber() < _minLength) {
			//Length of value of '&{0}' is too short&{0}'&{1}
			p.errorWithString(XDEF.XDEF814, parserName(), val);
		} else if (_maxLength!=-1 && val.getXDItemsNumber() > _maxLength) {
			//Length of value of '&{0}' is too long&{0}'{: }
			p.errorWithString(XDEF.XDEF815, parserName());
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_CONTAINER;}
}