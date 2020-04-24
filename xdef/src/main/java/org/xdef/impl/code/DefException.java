package org.xdef.impl.code;

import org.xdef.sys.Report;
import org.xdef.XDException;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;

/** CodeException
 * @author  Vaclav Trojan
 */
public final class DefException extends XDValueAbstract implements XDException {

	/** The value of this item. */
	private Report _value;
	/** Address of code. */
	private int _codeAddr;

	/** Address of code. */
	private String _xpos;

	DefException() {}

	/** Creates a new instance of CodeException
	 * @param report The message to be assigned with the item.
	 * @param codeAddr Address of code.
	 * @param xpos path of the actual node.
	 */
	public DefException(final Report report,
		final String xpos,
		final int codeAddr) {
		_value = report;
		_codeAddr = codeAddr;
		_xpos = xpos;
	}

	/** Creates a new instance of CodeException
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
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XD_EXCEPTION;}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.EXCEPTION;}

	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {
		return "PC:" + _codeAddr +
			(_xpos != null ? ", xpos:" + _xpos : "") + "; " + _value.toString();
	}

	@Override
	public String stringValue() {return toString();}

	@Override
	/** Clone the item (returns this object here).
	 * @return this object.
	 */
	public XDValue cloneItem() {return this;}

}