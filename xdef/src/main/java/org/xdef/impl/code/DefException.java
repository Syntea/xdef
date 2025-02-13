package org.xdef.impl.code;

import org.xdef.sys.Report;
import org.xdef.XDException;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.EXCEPTION;

/** Implementation of Exception in Xscript code.
 * @author  Vaclav Trojan
 */
public final class DefException extends XDValueAbstract implements XDException {
	/** The value of this item. */
	private final Report _value;
	/** Address of code. */
	private final int _codeAddr;
	/** Address of code. */
	private final String _xpos;

	/** Creates "null" instance of XDException. */
	public DefException() {_value = null; _codeAddr = -1; _xpos = null;}

	/** Creates a new instance of XDException
	 * @param report The message to be assigned with the item.
	 * @param codeAddr Address of code.
	 * @param xpos path of the actual node.
	 */
	public DefException(final Report report, final String xpos, final int codeAddr) {
		_value = report;
		_codeAddr = codeAddr;
		_xpos = xpos;
	}

	/** Creates a new instance of XDException
	 * @param id The message ID.
	 * @param msg The message to be assigned with the item.
	 * @param codeAddr Address of code.
	 * @param xpos path of the actual node.
	 * @param mod Message modification parameters.
	 */
	public DefException(final String id,
		final String msg,
		final String xpos,
		final int codeAddr,
		final Object... mod) {
		this(Report.error(id, msg, mod), xpos, codeAddr);
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of methods of XDException
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Return assigned Report.
	 * @return the value of assigned Report.
	 */
	public final Report reportValue() {return _value;}

	@Override
	/** Get script code address.
	 * @return script code address.
	 */
	public final int getCodeAddr() {return _codeAddr;}

	@Override
	/** Get position of actual XML node.
	 * @return position of actual XML node.
	 */
	public final String getXPos() {return _xpos;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	public short getItemId() {return XD_EXCEPTION;}
	@Override
	public XDValueType getItemType() {return EXCEPTION;}
	@Override
	public String toString() {
		return "PC:" + _codeAddr + (_xpos != null ? ", xpos:" + _xpos : "") + "; " + _value.toString();
	}
	@Override
	public String stringValue() {return toString();}
	@Override
	public XDValue cloneItem() {return this;}
	@Override
	public boolean isNull() {return _codeAddr == -1;}
}