package org.xdef.impl.parsers;

import org.xdef.sys.SException;
import org.xdef.XDNamedValue;
import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.XDRegex;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefString;
import org.xdef.XDContainer;
import static org.xdef.XDValueID.XD_STRING;
import org.xdef.msg.XDEF;

/** Parser of X-script "regex" type.
 * @author Vaclav Trojan
 */
public class XDParseRegex extends XDParserAbstract {
	private static final String ROOTBASENAME = "regex";
	private XDRegex _regex;

	public XDParseRegex() {super();}

	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		String s = p.getUnparsedBufferPart();
		if (!_regex.matches(s)) {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
		} else {
			p.setParsedValue(s);
			checkCharset(xnode, p);
			p.setEos();
		}
	}
	@Override
	public void setNamedParams(final XXNode xnode, final XDContainer params)
		throws SException {
		int num;
		if (params == null || (num = params.getXDNamedItemsNumber()) == 0) {
			return;
		}
		_regex = null;
		XDNamedValue[] items = params.getXDNamedItems();
		for (int i = 0; i < num; i++) {
			String name = items[i].getName();
			if ("argument".equals(name)) {
				XDValue val = items[i].getValue();
				if (val == null) {
					throw new SException(XDEF.XDEF816); //Value of enumeration for 'eq' must be just one
				}
				_regex = new XDRegex(val.toString(), false);
			} else {
				throw new SException(XDEF.XDEF801, name); //Illegal parameter name '&{0}'
			}
		}
	}
	@Override
	public final XDContainer getNamedParams() {
		XDContainer map = new DefContainer();
		if (_regex != null) {
			map.setXDNamedItem("argument", new DefString(_regex.toString()));
		}
		return map;
	}
	@Override
	public short parsedType() {return XD_STRING;}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseRegex) ) {
			return false;
		}
		XDParseRegex x = (XDParseRegex) o;
		return _regex == null && x._regex == null || _regex != null && _regex.equals(x._regex);
	}
}