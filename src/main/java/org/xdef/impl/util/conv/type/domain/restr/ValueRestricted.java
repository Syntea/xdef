package org.xdef.impl.util.conv.type.domain.restr;

/** Represents value restricted type.
 * @author Ilia Alexandrov
 */
public interface ValueRestricted {

	/** Gets minimal exclusive value.
	 * @return minimal exclusive value or <code>null</code>.
	 */
	public String getMinExclusive();

	/** Gets minimal inclusive value.
	 * @return minimal inclusive value or <code>null</code>.
	 */
	public String getMinInclusive();

	/** Gets maximal inclusive value.
	 * @return maximal inclusive value or <code>null</code>.
	 */
	public String getMaxInclusive();

	/** Gets maximal exclusive value.
	 * @return maximal exclusive value or <code>null</code>.
	 */
	public String getMaxExclusive();

	/** Sets minimal exclusive value.
	 * @param minExclusive minimal exclusive value.
	 * @throws IllegalArgumentException if value is empty.
	 */
	public void setMinExclusive(String minExclusive);

	/** Sets minimal inclusive value.
	 * @param minInclusive minimal inclusive value.
	 * @throws IllegalArgumentException if value is empty.
	 */
	public void setMinInclusive(String minInclusive);

	/** Sets maximal inclusive value.
	 * @param maxInclusive maximal inclusive value.
	 * @throws IllegalArgumentException if value is empty.
	 */
	public void setMaxInclusive(String maxInclusive);

	/** Sets maximal exclsuve value.
	 * @param maxExclusive maximal exclusive value.
	 * @throws IllegalArgumentException if value is empty.
	 */
	public void setMaxExclusive(String maxExclusive);
}