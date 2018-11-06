package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.sys.SIllegalArgumentException;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.sys.SRuntimeException;

/** Abstract parser of comparable values.
 * @author Vaclav Trojan
 */
public abstract class XSAbstractParseComparable extends XSAbstractParser {
	protected XDValue _maxIncl;
	protected XDValue _maxExcl;
	protected XDValue _minIncl;
	protected XDValue _minExcl;
	protected XDValue[] _enumeration;

	protected XSAbstractParseComparable() {
		super();
		_whiteSpace = 'c';
	}

	@Override
	public  void initParams() {
		_whiteSpace = 'c';
		_patterns = null;
		_enumeration = null;
		_minExcl = _minIncl = _maxExcl = _maxIncl = null;
	}
	@Override
	public byte getDefaultWhiteSpace() {return 'c';}
	@Override
	public XDValue getMinExclusive() { return _minExcl; }
	@Override
	public XDValue getMaxExclusive() { return _maxExcl; }
	@Override
	public XDValue getMinInclusive() { return _minIncl; }
	@Override
	public XDValue getMaxInclusive() {return _maxIncl;}
	@Override
	public XDValue[] getEnumeration() {return _enumeration;}
	/** Check value of range specification (override if necessary).
	 * @param x value of range specification.
	 * @throws SRuntimeException if value is incorrect.
	 */
	public void checkValue(final XDValue x) {}
	@Override
	public void setMinExclusive(XDValue x) {
		checkValue(_minExcl = iObject(null, x));
	}
	@Override
	public void setMaxExclusive(XDValue x) {
		checkValue(_maxExcl = iObject(null, x));
	}
	@Override
	public void setMinInclusive(XDValue x) {
		checkValue(_minIncl = iObject(null, x));
	}
	@Override
	public void setMaxInclusive(XDValue x) {
		checkValue(_maxIncl = iObject(null, x));
	}
	@Override
	public void setEnumeration(Object[] o) {
		_enumeration = null;
		if (o == null || o.length == 0) {
			return;
		}
		XDValue[] e = new XDValue[o.length];
		for (int i = 0; i < o.length; i++) {
			e[i] = iObject(null, o[i]);
		}
		_enumeration = e;
	}

	protected void checkComparable(XDParseResult p) {
		if (p.matches()) {
			XDValue val = p.getParsedValue();
			try {
				if (_minIncl != null && _minIncl.compareTo(val) > 0 ||
					_minExcl != null && _minExcl.compareTo(val) >= 0) {
					//Value of '&{0}' doesn't fit to '&{1}'
					p.error(XDEF.XDEF813, parserName(),
						"min" +(_minIncl != null ? "Inclusive" : "Exclusive"));
					return;
				}
			} catch (SIllegalArgumentException ex) {
				//Value of '&{0}' doesn't fit to '&{1}'
				p.error(XDEF.XDEF813, parserName(),
					"min" + (_minIncl != null ? "Inclusive" : "Exclusive"));
				Report r = ex.getReport();
				p.error(r.getMsgID(), r.getText(), r.getModification());
				return;
			}
			try {
				if (_maxIncl != null && _maxIncl.compareTo(val) < 0 ||
					_maxExcl != null && _maxExcl.compareTo(val) <= 0) {
					//Value of '&{0}' doesn't fit to '&{1}'
					p.error(XDEF.XDEF813, parserName(),
						"max" + (_maxIncl != null ? "Inclusive" : "Exclusive"));
					return;
				}
			} catch (SIllegalArgumentException ex) {
				//Value of '&{0}' doesn't fit to '&{1}'
				p.error(XDEF.XDEF813, parserName(),
					"max" + (_maxIncl != null ? "Inclusive" : "Exclusive"));
				Report r = ex.getReport();
				p.error(r.getMsgID(), r.getText(), r.getModification());
				return;
			}
			checkEnumeration(p);
		}
	}
}