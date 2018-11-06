package org.xdef.impl.util.conv.type.domain.restr;

import java.util.Set;

/** Represents patterns restricted type.
 * @author Ilia Alexandrov
 */
public interface PatternRestricted {

	/** Adds pattern.
	 * @param patern pattern.
	 */
	public void addPattern(String patern);

	/** Gets set of patterns.
	 * @return patterns set.
	 */
	public Set getPatterns();
}