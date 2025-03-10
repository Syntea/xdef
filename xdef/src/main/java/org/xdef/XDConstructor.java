package org.xdef;

import org.xdef.proc.XXNode;

/** Construct Xscript objects from XDResultSet.
 * @author Vaclav Trojan
 */
public interface XDConstructor {

	/** Construct XDValue from an object.
	 * @param resultSet object from which result will be created (may be null).
	 * @param xNode XXnode from which this method was called.
	 * @return created XDObject or null.
	 */
	public XDValue construct(XDResultSet resultSet, XXNode xNode);
}