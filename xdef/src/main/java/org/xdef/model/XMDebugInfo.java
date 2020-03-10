package org.xdef.model;

/** Debug information.
 * @author Vaclav Trojan
 */
public interface XMDebugInfo {

	/** Get statement information corresponding to the code address.
	 * @param codeAdr Code address.
	 * @return XMStatementInfo object or null.
	 */
	public XMStatementInfo getInfo(int codeAdr);

	/** Get statement information of the next statement after argument.
	 * @param si XMStatementInfo object or null.
	 * @return next XMStatementInfo object after argument or null.
	 */
	public XMStatementInfo nextStatementInfo(XMStatementInfo si);

	/** Get statement information of the previous statement before argument.
	 * @param si XMStatementInfo object or null.
	 * @return previous XMStatementInfo object before argument or null.
	 */
	public XMStatementInfo prevStatementInfo(XMStatementInfo si);

	/** Get statement information according to the source position.
	 * @param line source line.
	 * @param column source column.
	 * @param sourceID sourceID (pathname, URL, or sourceID) (may be null).
	 * @param xdName name of X-definition (may be null).
	 * @return XMStatementInfo object or null.
	 */
	public XMStatementInfo getStatementInfo(long line,
		long column,
		String sourceID,
		String xdName);

	/** Get array of statement information objects assigned to given
	 * X-definition and line.
	 * @param line source line.
	 * @param xdName name of X-definition.
	 * @return array XMStatementInfo objects (if no statement information
	 * is found the array is empty).
	 */
	public XMStatementInfo[] getStatementInfo(long line, String xdName);

	/** Get array of all statement information objects.
	 * @return array of XMStatementInfo objects.
	 */
	public XMStatementInfo[] getStatementInfo();

}