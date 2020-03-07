package org.xdef.model;

/** Interface of occurrence property of models of objects.
 * @author Vaclav Trojan
 */
public interface XMOccurrence {

	/** Data object is not required, but accepted. */
	public static final int OPTIONAL = 0;
	/** Data object is required. */
	public static final int REQUIRED = 1;
	/** Data object is fixed. */
	public static final int FIXED = 2;

	/** Get min occurrence.
	 * @return min occurrence.
	 */
	public int minOccurs();

	/** Get max occurrence.
	 * @return max occurrence.
	 */
	public int maxOccurs();

	/** Return true if value of occurrence is specified.
	 * @return <tt>true</tt> if and only if occurrence is specified.
	 */
	public boolean isSpecified();

	/** Return true if value of occurrence is set as illegal.
	 * @return <tt>true</tt> if and only if occurrence is set as illegal.
	 */
	public boolean isIllegal();

	/** Return true if value of occurrence is set as ignored.
	 * @return <tt>true</tt> if and only if occurrence is set as ignored.
	 */
	public boolean isIgnore();

	/** Return true if value of occurrence is set as fixed.
	 * @return <tt>true</tt> if and only if occurrence is set as fixed.
	 */
	public boolean isFixed();

	/** Return true if value of occurrence is set as required.
	 * @return <tt>true</tt> if and only if occurrence is set as required.
	 */
	public boolean isRequired();

	/** Return true if value of occurrence is set as optional.
	 * @return <tt>true</tt> if and only if occurrence is set as optional.
	 */
	public boolean isOptional();

	/** Return true if value of occurrence is set as unbounded.
	 * @return <tt>true</tt> if and only if occurrence is set as unbounded.
	 */
	public boolean isUnbounded();

	/** Return true if minimum is greater then 0 and maximum is unbounded.
	 * @return <tt>true</tt> if and only if minimum is greater then 0 and
	 * maximum is unbounded..
	 */
	public boolean isMaxUnlimited();

	@Override
	/** Get X-definition source form of Occurrence.
	 * @return X-Definition source form of Occurrence.
	 */
	public String toString();
}
