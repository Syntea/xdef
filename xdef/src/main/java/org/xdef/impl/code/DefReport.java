package org.xdef.impl.code;

import org.xdef.sys.Report;
import org.xdef.XDReport;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;

/** Implementation of XDReport
 *
 * @author  Vaclav Trojan
 */
public final class DefReport extends XDValueAbstract implements XDReport {

	/** The value associated with this item. */
	private Report _value;

	public DefReport() {}

	/** Creates a new instance of DefReport with text message.
	 * @param text text of message of report.
	 */
	public DefReport(final String text) {
		_value = Report.text("", text);
	}

	/** Creates a new instance of DefReport with text message.
	 * @param report the value of report.
	 */
	public DefReport(Report report) {_value = report;}

	/** Creates a new instance of DefReport with message.
	 * @param typ The kind of report.
	 * @param id The id of report.
	 * @param text The text of report.
	 * @param mod Message modification parameters.
	 */
	public DefReport(final int typ,
		final String id,
		final String text,
		final Object... mod) {
		_value = Report.text(id, text, mod);
	}

	@Override
	/** Return the value of DefReport object.
	 * @return the org.xdef.sys.Report value of the object.
	 */
	public Report reportValue() {return _value;}

	@Override
	/** Set modification parameter.
	 * @param name name of the parameter.
	 * @param value value of the parameter. If the parameter exists, then
	 *  if this argument is null, the original value of message modification
	 * is removed or if the argument is not null the original value is replaced.
	 * @return new report with modified parameters.
	 */
	public XDReport setParameter(String name, String value) {
		if (_value == null) {
			return new DefReport();
		}
		Report result = new Report(_value.getType(),
				_value.getMsgID(), _value.getText(), _value.getModification());
		result.setParameter(name, value);
		return new DefReport(result);
	}

	@Override
	/** Get value of parameter from modification string.
	 * @param name parameter name.
	 * @return value of the parameter or <tt>null</tt>.
	 */
	public String getParameter(final String name) {
		return _value == null ? null : _value.getParameter(name);
	}

	@Override
	/** Get text created in the specified language from the report.
	 * @param language language ID.
	 * @return text created in the specified language from the report.
	 */
	public String toString(String language) {
		return _value == null ? "null" : _value.toString(language);
	}

	@Override
	/** Get report ID.
	 * @return report ID or <tt>null</tt>.
	 */
	public String getMsgID() {return _value == null ? null : _value.getMsgID();}

	@Override
	/** Get primary text.
	 * @return the primary text or of the report <tt>null</tt>.
	 */
	public String getText() {return _value == null ? null : _value.getText();}

	@Override
	/** Get type of the report.
	 * @return type of the report :
	 * 'E' - Error, 'L' light error, 'F' - Fatal, 'W' - Warning, 'T' - text,
	 * 'S' - string, 'A' - audit, 'M' - message, 'I' - info 'X' exception,
	 * 'D' trace, 'K' kill.
	 */
	public byte getType() {return _value == null ? 0 : _value.getType();}

	@Override
	/** Set type of the report.
	 * @param type of the report:
	 * 'E' - Error, 'L' light error, 'F' - Fatal, 'W' - Warning, 'T' - text,
	 * 'S' - string, 'A' - audit, 'M' - message, 'I' - info 'X' exception,
	 * 'D' trace, 'K' kill. If the value of this argument has other value than
	 * the type is set to 'U'.
	 * @return new report with given type.
	 */
	public XDReport setType(byte type) {
		return _value == null
			? new DefReport(new Report(type, "", null))
			: new DefReport(new Report(type,
				_value.getMsgID(), _value.getText(), _value.getModification()));
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XD_REPORT;}
	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.REPORT;}
	@Override
	/** Check if the object is <tt>null</tt>.
	 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise returns
	 * <tt>false</tt>.
	 */
	public boolean isNull() { return _value == null;}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() {return toString();}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String toString() {
		return _value == null ? "null" : _value.toString();
	}
	@Override
	public int hashCode() {return _value == null ? 0 : _value.hashCode();}
	@Override
	public boolean equals(final Object arg) {
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
	public boolean equals(final XDValue arg) {
		if (isNull()) {
			return arg == null || arg.isNull();
		}
		if (arg == null || arg.isNull() || arg.getItemId() != XD_REPORT) {
			return false;
		}
		DefReport rep = (DefReport) arg;
		if (_value.getType() != rep._value.getType()) {
			return false;
		}
		if (_value.getMsgID() == null) {
			return rep._value.getMsgID() == null;
		}
		return _value.getMsgID().equals(rep._value.getMsgID());
	}
}