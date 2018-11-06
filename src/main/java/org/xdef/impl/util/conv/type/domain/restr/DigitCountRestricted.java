package org.xdef.impl.util.conv.type.domain.restr;

/** Represents digit count restricted type.
 * @author Ilia Alexandrov
 */
public interface DigitCountRestricted {

	/** Sets fraction digits count restriction.
	 *
	 * @param fractionDigits fraction digits count.
	 * @throws IllegalArgumentException if fraction digits count is negative.
	 */
	public void setFractionDigits(int fractionDigits);

	/** Sets total digits count restriction.
	 *
	 * @param totalDigits total digits count.
	 * @throws IllegalArgumentException if total digits count is negative.
	 */
	public void setTotalDigits(int totalDigits);

	/** Gets fraction digits count restriction.
	 *
	 * @return fraction digits count or <code>null</code>.
	 */
	public Integer getFractionDigits();

	/** Gets total digits count restriction.
	 *
	 * @return total digits count or <code>null</code>.
	 */
	public Integer getTotalDigits();
}