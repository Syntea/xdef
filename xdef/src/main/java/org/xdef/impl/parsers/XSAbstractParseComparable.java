package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.Report;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.XDParseResult;
import static org.xdef.XDParser.WS_COLLAPSE;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SRuntimeException;

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
        _whiteSpace = WS_COLLAPSE;
    }

    /** Check value of range specification (override if necessary).
     * @param x value of range specification.
     * @throws SRuntimeException if value is incorrect.
     */
    protected void checkValue(final XDValue x) {}

    @Override
    /** Initialize parser  (override if necessary). */
    public void initParams() {
        _whiteSpace = WS_COLLAPSE;
        _patterns = null;
        _enumeration = null;
        _minExcl = _minIncl = _maxExcl = _maxIncl = null;
    }

////////////////////////////////////////////////////////////////////////////////
// final methods
///////////////////////////////////////////////////////////////////////////////

    /** If the XDParseResult from the argument is comparable and put error if it does not fit the condition.
     * @param p XDParseResult to be che3cked.
     */
    protected final void checkComparable(final XDParseResult p) {
        if (p.matches()) {
            XDValue val = p.getParsedValue();
            try {
                if (_minIncl != null && _minIncl.compareTo(val) > 0
                    || _minExcl != null && _minExcl.compareTo(val) >= 0) {
                    p.error(XDEF.XDEF813,//Value of '&{0}' doesn't fit to '&{1}'&{2}{: }
                        parserName(),"min" +(_minIncl!=null ? "Inclusive" : "Exclusive"),p.getParsedString());
                    return;
                }
            } catch (SIllegalArgumentException ex) {
                p.error(XDEF.XDEF813,//Value of '&{0}' doesn't fit to '&{1}'&{2}{: }
                    parserName(), "min" + (_minIncl!=null ? "Inclusive" : "Exclusive"), p.getParsedString());
                Report r = ex.getReport();
                p.error(r.getMsgID(), r.getText(), r.getModification());
                return;
            }
            try {
                if (_maxIncl != null && _maxIncl.compareTo(val) < 0
                    || _maxExcl != null && _maxExcl.compareTo(val) <= 0) {
                    p.error(XDEF.XDEF813,//Value of '&{0}' doesn't fit to '&{1}'&{2}{: }
                        parserName(),"max" +(_maxIncl!=null ? "Inclusive" : "Exclusive"),p.getParsedString());
                    return;
                }
            } catch (SIllegalArgumentException ex) {
                p.error(XDEF.XDEF813,//Value of '&{0}' doesn't fit to '&{1}'&{2}{: }
                    parserName(), "max" + (_maxIncl!=null ? "Inclusive" : "Exclusive"), p.getParsedString());
                Report r = ex.getReport();
                p.error(r.getMsgID(), r.getText(), r.getModification());
                return;
            }
            checkEnumeration(p);
        }
    }

    /** Check if the date is legal and fits
     * @param xnode actual XXNode object.
     * @param p String parser with source and parsed object.
     */
    protected final void checkDate(final XXNode xnode, final XDParseResult p) {
        checkPatterns(p);
        if (!p.matches()) {
            return;
        }
        SDatetime d = (SDatetime) p.getParsedValue().getObject();
        if (_whiteSpace == 'c') {
            p.isSpaces();
        }
        if (!p.eos()) {
            //After the item '&{0}' follows an illegal character&{1}{: }
            p.errorWithString(XDEF.XDEF804, parserName());
            return;
        }
        if (!d.chkDatetime()) {
            p.errorWithString(XDEF.XDEF809, parserName());//Incorrect value of '&{0}'&{1}{: }
            return;
        }
        if (_minIncl==null&&_minExcl==null&&_maxIncl==null&&_maxExcl==null) {
            if (xnode!=null) { // no min, max and xnode != null
                if (!xnode.getXDDocument().isLegalDate(d)) {
                    p.error(XDEF.XDEF818,//Range of values of year of date must be from &{0} to &{1}'
                        xnode.getXDDocument().getMinYear(),xnode.getXDDocument().getMaxYear());
                    return;
                }
            }
            checkEnumeration(p);
        } else {
            checkComparable(p);
        }
    }

////////////////////////////////////////////////////////////////////////////////
// Implementation of final methods from XSAbstractParser
////////////////////////////////////////////////////////////////////////////////

    @Override
    public final byte getDefaultWhiteSpace() {return WS_COLLAPSE;}

    @Override
    public final XDValue getMinExclusive() { return _minExcl; }

    @Override
    public final XDValue getMaxExclusive() { return _maxExcl; }

    @Override
    public final XDValue getMinInclusive() { return _minIncl; }

    @Override
    public final XDValue getMaxInclusive() {return _maxIncl;}

    @Override
    public XDValue[] getEnumeration() {return _enumeration;}

    @Override
    public final void setMinExclusive(final XDValue x) {checkValue(_minExcl = iObject(null, x));}

    @Override
    public final void setMaxExclusive(final XDValue x) {checkValue(_maxExcl = iObject(null, x));}

    @Override
    public final void setMinInclusive(final XDValue x) {checkValue(_minIncl = iObject(null, x));}

    @Override
    public final void setMaxInclusive(final XDValue x) {checkValue(_maxIncl = iObject(null, x));}

    @Override
    public final void setEnumeration(final Object[] o) {
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
}