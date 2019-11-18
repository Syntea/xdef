package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.SException;
import org.xdef.XDNamedValue;
import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefParseResult;
import org.xdef.XDContainer;

/** Parser of X-Script "CDATA" type.
 * @author Vaclav Trojan
 */
public class XDParseCDATA extends XDParserAbstract {
	private static final String ROOTBASENAME = "CDATA";
	int _minLength, _maxLength;

	public XDParseCDATA() {
		super();
		_minLength = _maxLength = -1;
	}
	@Override
	public XDParseResult check(final XXNode xnode, final String s) {
		XDParseResult result = new DefParseResult(s);
		int len = s.length();
		if (_maxLength >= 0 && len > _maxLength) {
			//Length of value of '&{0}' is too long
			result.error(XDEF.XDEF815, ROOTBASENAME);
		} else if (_minLength == -1 && len == 0 ||
			_minLength >= 0 && len < _minLength) {
			//Length of value of '&{0}' is too short
			result.error(XDEF.XDEF814, ROOTBASENAME);
		} else {
			result.setEos();
		}
		return result;
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int len = p.getUnparsedBufferPart().length();
		if (_maxLength >= 0 && len > _maxLength) {
			//Length of value of '&{0}' is too long
			p.error(XDEF.XDEF815, ROOTBASENAME);
		} else if (_minLength == -1 && len == 0 ||
			_minLength >= 0 && len < _minLength) {
			//Length of value of '&{0}' is too short
			p.error(XDEF.XDEF814, ROOTBASENAME);
		} else {
			p.setEos();
		}
	}
	@Override
	public String toString() {return "string";}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public final void setNamedParams(final XXNode xnode, final XDContainer params)
		throws SException {
		_minLength = _maxLength = -1;
		XDNamedValue[] pars;
		if (params == null || (pars = params.getXDNamedItems()) == null) {
			return;
		}
		for (int i = 0; i < pars.length; i++) {
			String name = pars[i].getName();
			if ("length".equals(name)) {
				_minLength = _maxLength = pars[i].getValue().intValue();
			} else if ("minLength".equals(name)) {
				_minLength = pars[i].getValue().intValue();
			} else if ("maxLength".equals(name)) {
				_maxLength = pars[i].getValue().intValue();
			}
		}
		if (_minLength >= 0 && _maxLength >= 0 && _minLength > _maxLength) {
			//Incorrect combination of maximum and minimum
			throw new SException(XDEF.XDEF808);
		}
	}
	@Override
	/** Set value of one "sequential" parameter of parser.
	 * @param par "sequential" parameters.
	 */
	public void setParseParam(Object param) {
		_minLength = _maxLength = Integer.parseInt(param.toString());
	}
	@Override
	/** Set value of two "sequential" parameters of parser.
	 * @param par1 the first "sequential" parameter.
	 * @param par2 the second "sequential" parameter.
	 */
	public void setParseParams(final Object par1, final Object par2) {
		_minLength = Integer.parseInt(par1.toString());
		_maxLength = Integer.parseInt(par2.toString());
	}
	@Override
	public final XDContainer getNamedParams() {
		XDContainer map = new DefContainer();
		if (_minLength == _maxLength) {
			if (_minLength >= 0) {
				map.setXDNamedItem("length", new DefLong(_minLength));
			}
		} else {
			if (_minLength != -1) {
				map.setXDNamedItem("minLength", new DefLong(_minLength));
			}
			if (_maxLength != -1) {
				map.setXDNamedItem("maxLength", new DefLong(_maxLength));
			}
		}
		return map;
	}
}