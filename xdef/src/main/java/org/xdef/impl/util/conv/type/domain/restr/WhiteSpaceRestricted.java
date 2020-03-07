package org.xdef.impl.util.conv.type.domain.restr;

/** Represents white space restricted type.
 * @author Ilia Alexandrov
 */
public interface WhiteSpaceRestricted {

	/** White space restriction <code>preserve</code> string constant. */
	public static final String PRESERVE_STR = "preserve";
	/** White space restriction <code>collapse</code> string constant. */
	public static final String COLLAPSE_STR = "collapse";
	/** White space restriction <code>replace</code> string constant. */
	public static final String REPLACE_STR = "replace";

	/** Sets white space restriction.
	 * @param whiteSpace white space restriction.
	 * @throws IllegalArgumentException if white space restriction is invalid.
	 */
	public void setWhiteSpace(String whiteSpace);

	/** Gets white space restriction.
	 * @return white space restriction or <code>null</code>.
	 */
	public String getWhiteSpace();
}