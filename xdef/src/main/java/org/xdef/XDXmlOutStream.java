package org.xdef;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** Interface for incremental writing XML data to a stream in Xscript.
 * @author Vaclav Trojan
 */
public interface XDXmlOutStream extends XDValue {

	/** Set output will be indented.
	 * @param indent if true then the output will be indented.
	 */
	public void setIndenting(boolean indent);

	/** Write start of XML (element start tag and attributes).
	 * @param elem element of which the start will be written.
	 */
	public void writeElementStart(Element elem);

	/** Write XML end tag. */
	public void writeElementEnd();

	/** Write string as text node.
	 * @param text The text to be written.
	 */
	public void writeText(String text);

	/** Write XML node.
	 * @param node The node to be written.
	 */
	public void writeNode(Node node);

	/** Flush stream writer.*/
	public void flushStream();

	/** Close stream writer.*/
	public void closeStream();

	/** Write nodes after document element (comments or PI's).
	 * @param doc The document node.
	 */
	public void writeXmlTail(Document doc);
}