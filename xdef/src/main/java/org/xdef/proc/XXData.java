package org.xdef.proc;

import org.xdef.model.XMData;

/** Model of text data (attributes and text nodes).
 * @author Vaclav Trojan
 */
public interface XXData extends XXNode {

	/** Get model of the processed data object.
	 * @return model of the processed data object.
	 */
	public XMData getXMData();

	/** Get value of the actual attribute or Text node.
	 * @return The value of attribute or text node.
	 */
	public String getTextValue();

	/** Get value of the actual attribute or Text node.
	 * @param value the value to be set to attribute or text node.
	 */
	public void setTextValue(String value);

}