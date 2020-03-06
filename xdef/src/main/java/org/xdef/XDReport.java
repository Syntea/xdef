package org.xdef;

import org.xdef.sys.Report;

/** *   Reports in script see {@link org.xdef.sys.Report}.
 * @author trojan
 */
public interface XDReport extends XDValue {

	/** Return the value of DefReport object.
	 * @return the org.xdef.xdef.sys.Report value of the object.
	 */
	public Report reportValue();

	/** Set modification parameter.
	 * @param name name of the parameter.
	 * @param value value of the parameter. If the parameter exists, then
	 *  if this argument is null, the original value of message modification
	 * is removed or if the argument is not null the original value is replaced.
	 * @return new report with modified parameters.
	 */
	public XDReport setParameter(String name, String value);

	/** Get value of parameter from modification string.
	 * @param name parameter name.
	 * @return value of the parameter or <tt>null</tt>.
	 */
	public String getParameter(final String name);

	/** Get text created in the specified language from the report.
	 * @param language the language ID.
	 * @return text created in the specified language from the report.
	 */
	public String toString(String language);

	/** Get report ID.
	 * @return report ID or <tt>null</tt>.
	 */
	public String getMsgID();

	/** Get primary text.
	 * @return the primary text or of the report <tt>null</tt>.
	 */
	public String getText();

	/** Get type of the report.
	 * @return type of the report:
	 * 'E' - Error, 'L' light error, 'F' - Fatal, 'W' - Warning, 'T' - text,
	 * 'S' - string, 'A' - audit, 'M' - message, 'I' - info 'X' exception,
	 * 'D' trace, 'K' kill.
	 */
	public byte getType();

	/** Set type of the report.
	 * @param type of the report:
	 * 'E' - Error, 'L' light error, 'F' - Fatal, 'W' - Warning, 'T' - text,
	 * 'S' - string, 'A' - audit, 'M' - message, 'I' - info 'X' exception,
	 * 'D' trace, 'K' kill. If the value of this argument has other value than
	 * the type is set to 'U'.
	 * @return new report with given type.
	 */
	public XDReport setType(byte type);
}