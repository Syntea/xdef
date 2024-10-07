package org.xdef;

import static org.xdef.XDValueID.XD_REPORT;
import static org.xdef.XDValueType.REPORT;
import org.xdef.sys.Report;

/** Implementation of X-script value of org.xdef.sys.Report value of the object.
 * @author  Vaclav Trojan
 */
public final class XDReport extends XDValueAbstract {

	/** The value associated with this item. */
	private final Report _value;

	/** Creates instance of null XDReport. */
	public XDReport() {_value = null;}

	/** Creates a new instance of XDReport with text message.
	 * @param text text of message of report.
	 */
	public XDReport(final String text) {
		_value = Report.text("", text);
	}

	/** Creates a new instance of XDReport with text message.
	 * @param report the value of report.
	 */
	public XDReport(Report report) {_value = report;}

	/** Creates a new instance of XDReport with message.
	 * @param typ The kind of report.
	 * @param id The id of report.
	 * @param text The text of report.
	 * @param mod Message modification parameters.
	 */
	public XDReport(final int typ,
		final String id,
		final String text,
		final Object... mod) {
		_value = Report.text(id, text, mod);
	}

////////////////////////////////////////////////////////////////////////////////
// Implemented methods of XDReport
////////////////////////////////////////////////////////////////////////////////

	/** Return the value of DefReport object.
	 * @return the org.xdef.sys.Report value of the object.
	 */
	public final Report reportValue() {return _value;}

	/** Set modification parameter.
	 * @param name name of the parameter.
	 * @param value value of the parameter. If the parameter exists, then
	 *  if this argument is null, the original value of message modification
	 * is removed or if the argument is not null the original value is replaced.
	 * @return new report with modified parameters.
	 */
	public final XDReport setParameter(final String name, final String value) {
		if (_value == null) {
			return new XDReport();
		}
		Report result = new Report(_value.getType(),
				_value.getMsgID(), _value.getText(), _value.getModification());
		result.setParameter(name, value);
		return new XDReport(result);
	}

	/** Get value of parameter from modification string.
	 * @param name parameter name.
	 * @return value of the parameter or <i>null</i>.
	 */
	public final String getParameter(final String name) {
		return _value == null ? null : _value.getParameter(name);
	}

	/** Get text created in the specified language from the report.
	 * @param language language ID.
	 * @return text created in the specified language from the report.
	 */
	public final String toString(final String language) {
		return _value == null ? "null" : _value.toString(language);
	}

	/** Get report ID.
	 * @return report ID or <i>null</i>.
	 */
	public final String getMsgID() {return _value==null?null:_value.getMsgID();}

	/** Get primary text.
	 * @return the primary text or of the report <i>null</i>.
	 */
	public final String getText() {return _value==null?null:_value.getText();}

	/** Get type of the report.
	 * @return type of the report :
	 * 'E' - Error, 'L' light error, 'F' - Fatal, 'W' - Warning, 'T' - text,
	 * 'S' - string, 'A' - audit, 'M' - message, 'I' - info 'X' exception,
	 * 'D' trace, 'K' kill.
	 */
	public final byte getType() {return _value == null ? 0 : _value.getType();}

	/** Set type of the report.
	 * @param type of the report:
	 * 'E' - Error, 'L' light error, 'F' - Fatal, 'W' - Warning, 'T' - text,
	 * 'S' - string, 'A' - audit, 'M' - message, 'I' - info 'X' exception,
	 * 'D' trace, 'K' kill. If the value of this argument has other value than
	 * the type is set to 'U'.
	 * @return new report with given type.
	 */
	public final XDReport setType(final byte type) {
		return _value == null
			? new XDReport(new Report(type, "", null))
			: new XDReport(new Report(type,
				_value.getMsgID(), _value.getText(), _value.getModification()));
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public final short getItemId() {return XD_REPORT;}
	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public final XDValueType getItemType() {return REPORT;}
	@Override
	/** Check if the object is null.
	 * @return <i>true</i> if the object is null otherwise returns false.
	 */
	public final boolean isNull() { return _value == null;}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public final String stringValue() {return toString();}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public final String toString() {
		return _value == null ? "null" : _value.toString();
	}
	@Override
	public final int hashCode() {return _value == null ? 0 : _value.hashCode();}
	@Override
	public final boolean equals(final Object arg) {
		if (arg instanceof XDValue) {
			return equals((XDValue) arg);
		}
		return false;
	}
	@Override
	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return true if argument is same type as this XDValue and the value
	 * of the object is comparable and equals to this one.
	 */
	public final boolean equals(final XDValue arg) {
		if (isNull()) {
			return arg == null || arg.isNull();
		}
		if (arg == null || arg.isNull() || arg.getItemId() != XD_REPORT) {
			return false;
		}
		XDReport rep = (XDReport) arg;
		if (_value.getType() != rep._value.getType()) {
			return false;
		}
		if (_value.getMsgID() == null) {
			return rep._value.getMsgID() == null;
		}
		return _value.getMsgID().equals(rep._value.getMsgID());
	}
}