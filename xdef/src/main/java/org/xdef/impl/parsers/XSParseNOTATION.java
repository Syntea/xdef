package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.SParser;
import org.xdef.sys.StringParser;
import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;
import org.xdef.impl.ext.XExtUtils;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/** Parser of Schema "NOTATION" type.
 * @author Vaclav Trojan
 */
public class XSParseNOTATION extends XSAbstractParseString {
	private static final String ROOTBASENAME = "NOTATION";

	public XSParseNOTATION() {
		super();
		_minLength = _maxLength = -1;
	}
	@Override
	public  void initParams() {
		_patterns = null;
		_enumeration = null;
		_minLength = _maxLength = -1;
		_whiteSpace = 'r';
	}
	@Override
	public byte getDefaultWhiteSpace() {return 'r';}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		if (_whiteSpace == 'c') {
			p.isSpaces();
		}
		if (_enumeration != null) {
			checkEnumeration(p, xnode);
		} else if (_whiteSpace == 'c') {//collapse
			StringBuilder sb = new StringBuilder();
			int pos = p.getIndex();
			StringParser parser = new StringParser(p.getSourceBuffer(), pos);
			while(!parser.eos()) {
				char c;
				while ((c=parser.notOneOfChars("\n\r\t ")) != SParser.NOCHAR) {
					sb.append(c);
				}
				int pos1 = parser.getIndex();
				if (parser.isSpaces()) {
					if (!parser.eos()) {
						sb.append(' ');
					} else {
						parser.setBufIndex(pos1);
						break;
					}
				}
			}
			String s = sb.toString();
			p.setSourceBuffer(s);
			p.setParsedValue(s);
			if (!XSParseENTITY.chkEntity(s, xnode.getElement())) {
				//Incorrect value of '&{0}'&{1}{: }
				p.errorWithString(XDEF.XDEF809, parserName());
			}
		} else {//preserve or replace
			String s = p.getUnparsedBufferPart().trim();
			if (_whiteSpace == 'r') { //replace
				s = s.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
			}
			p.setSourceBuffer(s);
			p.setParsedValue(s);
		}
		p.setEos();
		checkPatterns(p);
		checkLength(p);
	}
	@Override
	public void finalCheck(final XXNode xnode, final XDParseResult p) {
		if (xnode == null) {
			p.error(XDEF.XDEF573, //Null value of &{0}"
				"xnode; in XSParseNOTATION.check(parser, xnode);");
			return;
		}
		Element el = xnode.getElement();
		DocumentType dt = el.getOwnerDocument().getDoctype();
		String id = p.getSourceBuffer();
		NamedNodeMap nm;
		if (dt == null) {
			nm = null;
		} else {
			nm = dt.getNotations();
		}
		boolean notationFound;
		if (nm == null || nm.getLength() == 0) {
			notationFound = false;
		} else {
			String nsURI = XExtUtils.getQnameNSUri(id, el);
			if (nsURI.length() > 0) {
				int ndx = id.indexOf(':');
				String localName = ndx < 0 ? id : id.substring(ndx + 1);
				notationFound = nm.getNamedItemNS(nsURI, localName) != null;
			} else {
				notationFound =  nm.getNamedItem(id) != null;
			}
		}
		if (!notationFound) {
			//Incorrect value of '&{0}'&{1}{: }
			p.error(XDEF.XDEF809, parserName(), id);
		}
	}

	@Override
	/** Get name of value.
	 * @return The name.
	 */
	public String parserName() {return ROOTBASENAME;}
}