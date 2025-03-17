package org.xdef.impl.parsers;

import org.xdef.XDContainer;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import static org.xdef.XDParser.BASE;
import static org.xdef.XDParser.ENUMERATION;
import static org.xdef.XDParser.PATTERN;
import static org.xdef.XDParser.WS_COLLAPSE;
import static org.xdef.XDParser.WS_PRESERVE;
import org.xdef.XDValue;
import static org.xdef.XDValueID.XD_ANY;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;

/** Parser of X-script "jvalue" type.
 * @author Vaclav Trojan
 */
public class XDParseJValue extends XSAbstractParser {
	private static final String ROOTBASENAME = "jvalue";
	XDValue[] _enumeration;
	private final XDParser[] _itemTypes = new XDParser[] {
		new XDParseJNull(), new XDParseJBoolean(), new XDParseJNumber(), new XDParseJString()};

	public XDParseJValue() {super();}

	@Override
	public  void initParams() {_patterns = null; _enumeration = null;}
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
	public void check(final XXNode xnode, final XDParseResult p) {parse(xnode,p,true);}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p) {parse(xnode,p,false);}
	private void parse(final XXNode xnode, final XDParseResult p, final boolean isFinal) {
		int pos = p.getIndex();
		String source = p.getSourceBuffer();
		for (int i = 0; i < _itemTypes.length; i++) {
			if (isFinal) {
				_itemTypes[i].check(xnode, p);
			} else {
				_itemTypes[i].parseObject(xnode, p);
			}
			if (p.errors()) {
				p.setSourceBuffer(source);
				p.setIndex(pos);
				p.clearReports();
				continue;
			}
			if (isFinal) {
				finalCheck(xnode, p);
			}
			XDValue val = p.getParsedValue();
			if (_enumeration != null) {
				boolean found = false;
				for (int j = 0; j < _enumeration.length; j++) {
					if (_enumeration[j].equals(val)) {
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
			if (isFinal) {
				_whiteSpace = _itemTypes[i].getWhiteSpaceParam();
				if (_whiteSpace == WS_COLLAPSE) {
					p.isSpaces();
				}
				if (!p.eos()) {
					//After the item '&{0}' follows an illegal character&{1}{: }
					p.errorWithString(XDEF.XDEF804, parserName());
					p.setIndex(pos);
					continue;
				}
			}
			checkPatterns(p);
			return;
		}
		p.setIndex(pos);
		p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
	}
	@Override
	public byte getDefaultWhiteSpace() {return WS_PRESERVE;}
	@Override
	public short parsedType() {return XD_ANY;}
	@Override
	public XDValue[] getEnumeration() {return _enumeration;}
	@Override
	public void setEnumeration(final Object[] o) {
		if (o == null || o.length == 0) {
			return;
		}
		XDValue[] e = new XDValue[o.length];
		for (int i = 0; i < o.length; i++) {
			e[i] = iObject(null, o[i]);
		}
		_enumeration = e;
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
		if (_enumeration == null) {
			return x._enumeration == null;
		} else if (x._enumeration == null) {
			return false;
		}
		if (_enumeration.length != x._enumeration.length) {
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