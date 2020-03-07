package org.xdef.model;

/** Statement information for debugging.
 * @author Vaclav Trojan
 */
public interface XMStatementInfo {

	/** Get source column.
	 * @return source column.
	 */
	public long getColumn();

	/** Get source line.
	 * @return source line.
	 */
	public long getLine();

	/** Get source name (or URL).
	 * @return source name (or URL) or null.
	 */
	public String getSysId();

	/** Get column of end source.
	 * @return end source column.
	 */
	public long getEndColumn();

	/** Get line of end source.
	 * @return end source line.
	 */
	public long getEndLine();

	/** Update source end position of this item.
	 * @param line end line.
	 * @param column end column.
	 */
	public void updateEndPos(long line, long column);

	/** Get name of X-definition.
	 * @return name of X-definition.
	 */
	public String getXDName();

	/** Get code address.
	 * @return code address.
	 */
	public int getAddr();

	/** Get array of local variables.
	 * @return  array of local variables.
	 */
	public XMVariable[] getLocalVariables();

}