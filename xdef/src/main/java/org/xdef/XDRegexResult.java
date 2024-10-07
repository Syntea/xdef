package org.xdef;

import java.util.regex.Matcher;
import static org.xdef.XDValueID.XD_REGEXRESULT;
import static org.xdef.XDValueType.REGEXRESULT;

/** Result of regular expression  in x-script.
 * @author Vaclav Trojan
 */
public class XDRegexResult extends XDValueAbstract {

	/** Matcher object generated from the source expression. */
	private Matcher _value;

	/** Creates a new instance of XDRegexResult. */
	public XDRegexResult() {}

	/** Creates a new instance of XDRegexResult.
	 * @param matcher The matcher object.
	 */
	public XDRegexResult(final Matcher matcher) {_value = matcher;}

	/** Check if given data matches the regular expression.
	 * @return true if and only if the data matches regular expression.
	 */
	public final boolean matches() {return _value.matches();}

	/** Get the number of capturing groups in pattern.
	 * @return the number of capturing groups in pattern.
	 */
	public final int groupCount() {return _value.groupCount() + 1;}

	/** Get specified group.
	 * @param index index of required group.
	 * @return specified group;
	 */
	public final String group(final int index) {
		if (_value == null) return "";
		try {
			return _value.group(index);
		} catch (Exception ex) {
			return "";
		}
	}

	/** Get the start index of the subsequence captured by the given group
	 * during the previous match operation.
	 * @param index The index of a capturing group in pattern.
	 * @return the start index of the subsequence captured by the given group
	 * or -1.
	 */
	public final int groupStart(final int index) {
		try {
			return _value.start(index);
		} catch (Exception ex) {
			return -1;
		}
	}

	/** Get the offset after the last character of the subsequence captured
	 * by the given group during the previous match operation or -1.
	 * @param index The index of a capturing group in pattern.
	 * @return offset after the last character of the subsequence or -1.
	 */
	public final int groupEnd(final int index) {
		try {
			return _value.end(index);
		} catch (Exception ex) {
			return -1;
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XD_REGEXRESULT;}
	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return REGEXRESULT;}
	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {
		if (_value == null) return "";
		if (_value.matches()) return _value.group();
		return "";
	}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() {return String.valueOf(matches());}
}