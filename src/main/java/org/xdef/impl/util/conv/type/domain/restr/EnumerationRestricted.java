package org.xdef.impl.util.conv.type.domain.restr;

import java.util.Set;

/** Represents enumeration restricted type.
 * @author Ilia Alexandrov
 */
public interface EnumerationRestricted {

	/** Adds enumeration.
	 *
	 * @param enumeration enumeration to add.
	 */
	public void addEnumeration(String enumeration);

	/** Gets set of enumerations.
	 *
	 * @return enumeration set.
	 */
	public Set getEnumerations();
}