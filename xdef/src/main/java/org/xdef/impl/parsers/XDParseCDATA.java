package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.SException;
import org.xdef.XDNamedValue;
import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefLong;
import org.xdef.XDContainer;
import org.xdef.sys.SRuntimeException;

/** Parser of X-Script "CDATA" type.
 * @author Vaclav Trojan
 */
public class XDParseCDATA extends XDParserAbstract {
	int _minLength, _maxLength;

	public XDParseCDATA() {
		super();
		_minLength = 1; _maxLength = -1;
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		String s = p.getUnparsedBufferPart();
		int len = s.length();
		p.setParsedValue(s);
		if (_maxLength >= 0 && len > _maxLength) {
			//Length of value of '&{0}' is too long&{0}'{: }
			p.errorWithString(XDEF.XDEF815, "string");
		} else if (_minLength == -1 && len == 0 ||
			_minLength >= 0 && len < _minLength) {
			//Length of value of '&{0}' is too short&{0}'{: }
			p.errorWithString(XDEF.XDEF814, "string");
		} else {
			p.setEos();
		}
	}
	@Override
	public String toString() {return "string";}
	@Override
	public String parserName() {return "string";}
	@Override
	public final void setNamedParams(final XXNode xnode,
		final XDContainer params) throws SException {
		_minLength = _maxLength = -1;
		XDNamedValue[] pars;
		if (params == null || (pars = params.getXDNamedItems()) == null) {
			return;
		}
		for (XDNamedValue nv: pars) {
			switch (nv.getName()) {
				case "length":
					_minLength = _maxLength = nv.getValue().intValue();
					break;
				case "minLength":
					_minLength = nv.getValue().intValue();
					break;
				case "maxLength":
					_maxLength = nv.getValue().intValue();
			}
		}
		if (_minLength >= 0 && _maxLength >= 0 && _minLength > _maxLength) {
			//Incorrect combination of maximum and minimum
			throw new SException(XDEF.XDEF808);
		}
	}
	@Override
	public void setParseSQParams(final Object... params) {
		if (params == null || params.length == 0) {
			return;
		}
		Object par1 = params[0];
		_minLength = Integer.parseInt(par1.toString());
		switch(params.length) {
			case 1: _maxLength = _minLength; break;
			case 2:
				_maxLength = Integer.parseInt(params[1].toString()); break;
			default: //Too many parameters for method &{0}
				throw new SRuntimeException(XDEF.XDEF461, parserName());
		}
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
