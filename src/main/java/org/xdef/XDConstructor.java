package org.xdef;

import org.xdef.proc.XXNode;

/** Constructs XDValue from an iterated object fro XDResultSet. This interface
 * is used to construct x-script objects from XDResultSet.
 *
 * @author Vaclav Trojan
 */
public interface XDConstructor {

	/** Construct XDValue from an object.
	 * @param resultSet object from which result will be created (may be null).
	 * @param xNode XXnode from which this method was called.
	 * @return created XDObject or <tt>null</tt>.
	 */
	public XDValue construct(XDResultSet resultSet, XXNode xNode);

}