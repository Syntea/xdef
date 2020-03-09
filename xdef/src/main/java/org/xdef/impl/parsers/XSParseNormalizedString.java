package org.xdef.impl.parsers;

import org.xdef.sys.SUtils;
import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefString;

/** Parser of Schema "normalizedString" type.
 * @author Vaclav Trojan
 */
public class XSParseNormalizedString extends XSAbstractParseString {
	private static final String ROOTBASENAME = "normalizedString";

	public XSParseNormalizedString() {
		super();
		_whiteSpace = 'r';
		_minLength = _maxLength = -1;
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
			LENGTH +
			MAXLENGTH +
			MINLENGTH +
//			NORMALIZE +
//			SEPARATOR +
//			ITEM +
			BASE +
			0;
	}
	@Override
	public  void initParams() {
		_patterns = null;
		_enumeration = null;
		_minLength = _maxLength = -1;
		_whiteSpace = 'r';
	}
/*
The lexical space of xsd:normalizedString is unconstrained (any valid XML
character may be used). Its value space is the set of strings after whitespace
replacement—i.e., after any occurrence of #x9 (tab), #xA (linefeed), and #xD
(carriage return) have been replaced by an occurrence of #x20 (space) without
any whitespace collapsing.

This is the only datatype that performs whitespace replacement without
collapsing. When whitespace isn't significant, xsd:token is preferred.

This datatype corresponds neither to the XPath function normalize-space() (which
performs whitespace trimming and collapsing) nor to the DOM normalize method
(which is a merge of adjacent text objects).

<title lang="en">
  Being a Dog Is
  a Full-Time Job
</title>
"  Being a Dog Is a Full-Time Job ", in which all whitespace has been replaced
by spaces, if the title element is a type xsd:normalizedString.
*/
	@Override
	public void setEnumeration(Object[] o) {
		if (o == null || o.length == 0) {
			return;
		}
		DefString[] e = new DefString[o.length];
		for (int i = 0; i < o.length; i++) {
			String s = iObject(null, o[i]).stringValue();
			if (s != null) {
				e[i] = new DefString(SUtils.modifyString(SUtils.modifyString(
					SUtils.modifyString(s, "\n", " "), "\t", " "), "\r", " "));
			} else {
				e[i] = new DefString(""); // to prevent exception
			}

		}
		_enumeration = e;
	}

	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p) {
		int pos0 = p.getIndex();
		String oldsource = p.getSourceBuffer();
		String s = p.getUnparsedBufferPart();
		s = (s == null) ? ""
			: SUtils.modifyString(SUtils.modifyString(SUtils.modifyString(
				s, "\n", " "), "\t", " "), "\r", " ");
		p.setBufIndex(pos0);
		s = p.getParsedBufferPartFrom(0) + s;
		p.setSourceBuffer(s);
		if (_whiteSpace == 'c') {
			p.isSpaces();
		}
		if (_enumeration != null) {
			checkEnumeration(p, xnode);
			if (p.errors()) {
				p.setSourceBuffer(oldsource);
				return;
			}
			s = p.getParsedValue().toString();
			if (_whiteSpace == 'c') {//collapse
				p.isSpaces();
			}
		} else if (_whiteSpace == 'c') {//collapse
			StringBuilder sb = new StringBuilder();
			while((s = p.nextToken()) != null) {
				sb.append(s);
				if (p.isSpaces()) {
					if (p.eos()) {
						break;
					}
					sb.append(' ');
				} else {
					break;
				}
			}
			s = sb.toString();
			if (_whiteSpace == 'c') {
				p.isSpaces();
			}
		} else {
			p.setEos();
		}
		if (s == null) {
			s = "";
		}
		p.setSourceBuffer(oldsource);
		p.replaceParsedBufferFrom(pos0, s);
		p.setParsedValue(s);
		checkPatterns(p);
		checkLength(p);
	}
	@Override
	public byte getDefaultWhiteSpace() {return 'r';}
	@Override
	public String parserName() {return ROOTBASENAME;}
}