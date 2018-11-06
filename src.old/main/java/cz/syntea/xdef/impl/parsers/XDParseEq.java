package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.sys.SException;
import cz.syntea.xdef.XDNamedValue;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.XDParserAbstract;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.impl.code.DefContainer;
import cz.syntea.xdef.impl.code.DefParseResult;
import cz.syntea.xdef.impl.code.DefString;
import cz.syntea.xdef.XDContainer;

/** Parser of X-Script "eq" type.
 * @author Vaclav Trojan
 */
public class XDParseEq extends XDParserAbstract {
	private static final String ROOTBASENAME = "eq";
	String _param;
	public XDParseEq() {
		super();
	}
	@Override
	public XDParseResult check(final XXNode xnode, final String s) {
		XDParseResult p = new DefParseResult(s);
		if (!_param.equals(s)) {
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
		} else {
			p.setEos();
		}
		return p;
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		if (p.isToken(_param)) {
			p.setParsedValue(_param);
		} else {
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
		}
	}
	@Override
	/** Set value of one "sequential" parameter of parser.
	 * @param par "sequential" parameters.
	 */
	public void setParseParam(Object param) {
		_param = param.toString();
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
			if ("argument".equals(name)) {
				XDValue val = items[i].getValue();
				if (val == null) {
					//Value of enumeration for 'eq' must be just one
					throw new SException(XDEF.XDEF816);
				}
				_param = val.toString();
			} else {
				//Illegal parameter name '&{0}'
				throw new SException(XDEF.XDEF801, name);
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