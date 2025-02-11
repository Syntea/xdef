package org.xdef.impl.parsers;

import java.nio.charset.Charset;
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

/** Parser of Xscript "CDATA" type.
 * @author Vaclav Trojan
 */
public class XDParseCDATA extends XDParserAbstract {
	int _minLength, _maxLength;

	public XDParseCDATA() {super(); _minLength = 1; _maxLength = -1;}

	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		String s = p.getUnparsedBufferPart();
		int len = s.length();
		p.setParsedValue(s);
		if (_maxLength >= 0 && len > _maxLength) {
			p.errorWithString(XDEF.XDEF815, "string"); //Length of value of '&{0}' is too long&{0}'{: }
		} else if (_minLength == -1 && len == 0 ||
			_minLength >= 0 && len < _minLength) {
			p.errorWithString(XDEF.XDEF814, "string"); //Length of value of '&{0}' is too short&{0}'{: }
		} else {
			p.setEos();
		}
		Charset[] chsets = xnode != null ? xnode.getXDPool().getLegalStringCharsets() : null;
		if (chsets != null && chsets.length > 0) {
			String err = "";
			for (Charset chset : chsets) {
				byte[] bytes = s.getBytes(chset);
				if (bytes.length != s.length()) {
					err += ' ' + chset.name();
				} else {
					String t = new String(bytes, chset);
					if (!s.equals(t)) {
						err += ' ' + chset.name();
					} else {
						return;
					}
				}
				p.error(XDEF.XDEF823, err.trim());
			}

		}
	}
	@Override
	public String toString() {return "string";}
	@Override
	public String parserName() {return "string";}
	@Override
	public final void setNamedParams(final XXNode xnode, final XDContainer params) throws SException {
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
			throw new SException(XDEF.XDEF808); //Incorrect combination of maximum and minimum
		}
	}
	@Override
	public void setParseSQParams(final Object... params) {
		if (params != null && params.length >= 1) {
			Object par1 = params[0];
			_minLength = Integer.parseInt(par1.toString());
			switch (params.length) {
				case 1:_maxLength=_minLength;return;
				case 2:_maxLength=Integer.parseInt(params[1].toString());return;
			}
			throw new SRuntimeException("Incorrect number of parameters");
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
