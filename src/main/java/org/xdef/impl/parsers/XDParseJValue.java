package org.xdef.impl.parsers;

import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;

/** Parser of X-Script "jvalue" type.
 * @author Vaclav Trojan
 */
public class XDParseJValue extends XSAbstractParser {
	private static final String ROOTBASENAME = "jvalue";
	private static final XDParser[] ALL_TYPES = new XDParser[]{
		new XDParseJNull(),
		new XDParseJBoolean(),
		new XDParseJNumber(),
		new XDParseJString()};
	private XDValue[] _enumeration;

	public XDParseJValue() {super();}

	@Override
	public  void initParams() {
		_patterns = null;
		_enumeration = null;
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
	public byte getDefaultWhiteSpace() {return 0;}
	@Override
	public XDValue[] getEnumeration() {return _enumeration;}
	@Override
	public void setEnumeration(Object[] o) {
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
	public void check(final XXNode xnode, final XDParseResult p) {
		parse(xnode, p, true);
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		parse(xnode, p, false);
	}
	private void parse(final XXNode xnode,
		final XDParseResult p,
		final boolean isFinal){
		int pos = p.getIndex();
		String source = p.getSourceBuffer();
		_whiteSpace = 0;
		for (int i = 0; i < ALL_TYPES.length; i++) {
			if (isFinal) {
				ALL_TYPES[i].check(xnode, p);
			} else {
				ALL_TYPES[i].parseObject(xnode, p);
			}
			if (p.errors()) {
				p.setSourceBuffer(source);
				p.setBufIndex(pos);
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
					//Doesn't fit enumeration list of '&{0}'
					p.error(XDEF.XDEF810, parserName());
					return;
				}
			}
			if (isFinal) {
				_whiteSpace = ALL_TYPES[i].getWhiteSpaceParam();
				if (_whiteSpace == 'c') {
					p.isSpaces();
				}
				if (!p.eos()) {
					//After the item '&{0}' follows an illegal character
					p.error(XDEF.XDEF804, parserName());
					p.setBufIndex(pos);
					continue;
				}
			}
			checkPatterns(p);
			return;
		}
		p.setBufIndex(pos);
		p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_CONTAINER;}
	@Override
	public boolean equals(final XDValue o) {
		return super.equals(o) && (o instanceof XDParseJValue);
	}
}