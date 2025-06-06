package org.xdef.impl.parsers;

import org.xdef.sys.SUtils;
import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefString;

/** Parser of XML Schema (XSD) "normalizedString" type.
 * @author Vaclav Trojan
 */
public class XSParseNormalizedString extends XSAbstractParseString {
	private static final String ROOTBASENAME = "normalizedString";

	public XSParseNormalizedString() {super(); _whiteSpace = WS_REPLACE; _minLength = _maxLength = -1;}

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
		_whiteSpace = WS_REPLACE;
	}

	@Override
	public void setEnumeration(final Object[] o) {
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
		s = SUtils.modifyString(SUtils.modifyString(SUtils.modifyString(
				s, "\n", " "), "\t", " "), "\r", " ");
		p.setIndex(pos0);
		s = p.getParsedBufferPartFrom(0) + s;
		p.setSourceBuffer(s);
		if (_whiteSpace == WS_COLLAPSE) {
			p.isSpaces();
		}
		if (_enumeration != null) {
			checkEnumeration(p, xnode);
			if (p.errors()) {
				p.setSourceBuffer(oldsource);
				return;
			}
			s = p.getParsedValue().toString();
			if (_whiteSpace == WS_COLLAPSE) {//collapse
				p.isSpaces();
			}
		} else if (_whiteSpace == WS_COLLAPSE) {//collapse
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
			if (_whiteSpace == WS_COLLAPSE) {
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
	public byte getDefaultWhiteSpace() {return WS_REPLACE;}

	@Override
	public String parserName() {return ROOTBASENAME;}
}