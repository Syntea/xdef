package org.xdef.sys;

/** Contains string connected with source position.
 *
 * @author  Vaclav Trojan
 */
public class SBuffer extends SPosition {
	/** Source buffer. */
	private String _source;

	/** Creates a new empty instance of SBuffer. */
	SBuffer() {}

	/** Creates a new instance of SBuffer.
	 * @param source the string with the value.
	 */
	public SBuffer(final String source) {
		_source = source;
		setLineNumber(1L);
	}

	/** Creates a new instance of SBuffer.
	 * @param source the string with the value.
	 * @param position the position of the value.
	 */
	public SBuffer(final String source, final SPosition position) {
		super(position);
		if (position != null) {
			setFilePos(getFilePos() + position.getIndex());
			setIndex(0);
		} else {
			setLineNumber(1L);
		}
		_source = source;
	}

	/** Append to this object the value of string from argument.
	 * @param value the value to be added.
	 */
	public void appendToBuffer(final SBuffer value) {
		appendPos(_source.length(),
			(SPosition) value, value._source.length(), false);
		_source += value._source;
	}

	/** Get part of source buffer from given position.
	 * @param pos starting position.
	 * @return the parsed string.
	 */
	public final String getStringFromPosition(int pos) {
		return _source.substring(pos, getIndex());
	}

	/** Get part of source buffer from given position.
	 * @param start starting position.
	 * @param end starting position.
	 * @return the parsed string.
	 */
	public final String getStringFromPosition(int start, int end) {
		return _source.substring(start, end);
	}

	/** Add string to buffer.
	 * @param s string to be added.
	 */
	public final void addString(final String s) {_source += s;}

	/** Get value of buffer as string.
	 * @return string with the value of buffer.
	 */
	public final String getString() {return _source;}

	/** Set string to buffer.
	 * @param s string to be set.
	 */
	public final void setString(final String s) {_source = s;}

	@Override
	public String toString() {return super.toString() + ";\n" + _source;}

}