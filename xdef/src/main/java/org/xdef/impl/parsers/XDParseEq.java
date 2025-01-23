package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.SException;
import org.xdef.XDNamedValue;
import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefParseResult;
import org.xdef.impl.code.DefString;
import org.xdef.XDContainer;
import org.xdef.sys.SRuntimeException;
import org.xdef.xon.XonTools;

/** Parser of Xscript "eq" type.
 * @author Vaclav Trojan
 */
public class XDParseEq extends XDParserAbstract {
	private static final String ROOTBASENAME = "eq";
	String _param;

	public XDParseEq() {super();}

	@Override
	public XDParseResult check(final XXNode xn, final String s) {
		XDParseResult p = new DefParseResult(s);
		parseObject(xn, p);
		return p;
	}
	@Override
	public void parseObject(final XXNode xn, final XDParseResult p){
		boolean quoted = xn != null && xn.getXonMode() > 0 && p.isChar('"');
		if (quoted) {
			if (!_param.equals(XonTools.readJString(p))) {
				p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
			}
			return;
		}
		if (!p.isToken(_param)) {
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
		}
	}
	@Override
	public void setParseSQParams(final Object... param) {
		if (param.length == 1) {
			_param = param[0].toString();
		} else {
			throw new SRuntimeException("Incorrect number of parameters");
		}
	}
	@Override
	public void setNamedParams(final XXNode xnode, final XDContainer params)
		throws SException {
		int num;
		if (params == null || (num = params.getXDNamedItemsNumber()) == 0) {
			return;
		}
		_param = null;
		XDNamedValue[] items = params.getXDNamedItems();
		for (int i = 0; i < num; i++) {
			String name = items[i].getName();
			XDValue val = items[i].getValue();
			if ("argument".equals(name)) {
				if (val == null) {
					throw new SException(XDEF.XDEF816); //Value of enumeration for 'eq' must be just one
				}
				_param = val.toString();
			} else {
				throw new SException(XDEF.XDEF801, name); //Illegal parameter name '&{0}'
			}
		}
	}
	@Override
	public final XDContainer getNamedParams() {
		XDContainer map = new DefContainer();
		if (_param != null) {
			map.setXDNamedItem("argument", new DefString(_param));
		}
		return map;
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {
		if (!super.equals(o) || !(o instanceof XDParseEq) ) {
			return false;
		}
		XDParseEq x = (XDParseEq) o;
		return _param == null && x._param == null || _param.equals(x._param);
	}
}