package org.xdef;

/** Result of regular expression  in x-script.
 * @author Vaclav Trojan
 */
public interface XDRegexResult extends XDValue {

	/** Check if given data matches the regular expression.
	 * @return <tt>true</tt> if and only if the data matches regular expression.
	 */
	public boolean matches();

	/** Get the number of capturing groups in pattern.
	 * @return the number of capturing groups in pattern.
	 */
	public int groupCount();

	/** Get specified group.
	 * @param index index of required group.
	 * @return specified group;
	 */
	public String group(int index);

	/** Get the start index of the subsequence captured by the given group
	 * during the previous match operation.
	 * @param index The index of a capturing group in pattern.
	 * @return the start index of the subsequence captured by the given group
	 * or -1.
	 */
	public int groupStart(int index);

	/** Get the offset after the last character of the subsequence captured
	 * by the given group during the previous match operation or -1.
	 * @param index The index of a capturing group in pattern.
	 * @return offset after the last character of the subsequence or -1.
	 */
	public int groupEnd(int index);

}