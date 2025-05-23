package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.StringParser;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefElement;
import org.xdef.impl.code.DefString;
import org.xdef.impl.ext.XExtUtils;
import org.w3c.dom.Element;

/** Parser of X-script "NCNameURI" type.
 * @author Vaclav Trojan
 */
public class XDParseQNameURI extends XSAbstractParseToken {
	private static final String ROOTBASENAME = "QNameURI";
	Element _elem;

	public XDParseQNameURI() {super();}

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
			SEPARATOR +
//			ITEM +
//			BASE +
			ARGUMENT +
			0;
	}

	@Override
	public void initParams() {super.initParams(); _elem = null;}

	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		Element el = _elem == null ? xnode.getElement() : _elem;
		byte xmlVersion1 = "1.1".equals(el.getOwnerDocument().getXmlVersion())
			? StringParser.XMLVER1_1 : StringParser.XMLVER1_0;
		StringParser parser = new StringParser(p.getSourceBuffer(), pos);
		if (!parser.isXMLName(xmlVersion1)) {
			p.error(XDEF.XDEF546); //QName expected
			return;
		}
		p.setIndex(parser.getIndex());
		String s = p.getParsedBufferPartFrom(pos);
		p.isSpaces();
		if (XExtUtils.getQnameNSUri(s, el).length() == 0) {
			p.error(XDEF.XDEF554);//Namespace not defined
		}
		checkParams(p, pos0, pos);
	}

	XDValue parse(final XXNode xnode, final StringParser parser) {
		if (!parser.isNCName((byte) 10)) {
			return null;
		}
		return new DefString(parser.getParsedString());
	}

	private void checkParams(final XDParseResult p,
		final int pos0,
		final int pos) {
		if (p.matches()) {
			String s = p.getBufferPart(pos, p.getIndex());
			if (_whiteSpace == WS_COLLAPSE) {
				p.isSpaces();
				p.replaceParsedBufferFrom(pos0, s);
			} else {
				if (!p.eos()) {
					p.setEos();
					p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
					return;
				}
				if (_whiteSpace == WS_REPLACE) { //replace
					s = s.replace('\t',' ').replace('\n',' ').replace('\r',' ');
				}
			}
			p.setParsedValue(s);
			int len = s.length();
			if (_maxLength != -1 && len > _maxLength) {
				p.errorWithString(XDEF.XDEF815, ROOTBASENAME);//Length of value of '&{0}' is too long&{0}'{: }
			} else if (_minLength != -1 && len < _minLength) {
				//Length of value of '&{0}' is too short&{0}'{: }
				p.errorWithString(XDEF.XDEF814, ROOTBASENAME, s);
			}
			checkPatterns(p);
			checkEnumeration(p);
		}
	}

	@Override
	public void setArgument(final XDValue x) {_elem = x.getElement();}

	@Override
	public XDValue getArgument() {return new DefElement(_elem);}

	@Override
	public short parsedType() {return XD_STRING;}

	@Override
	public String parserName() {return ROOTBASENAME;}
}