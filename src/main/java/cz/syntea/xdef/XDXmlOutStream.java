/*
 * Copyright 2011 Syntea software group a.s. All rights reserved.
 *
 * File: XDDate.java, created 2011-09-07.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */

package cz.syntea.xdef;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Interface for incremental writing XML data to a stream in script.
 *
 * @author Vaclav Trojan
 */
public interface XDXmlOutStream extends XDValue {

	/** Set output will be indented.
	 * @param indent if <tt>true</tt> then the output will be indented.
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

	/** Write XML node.
	 * @param nl The node list to be written.
	 */
	public void writeNodeList(NodeList nl);

	/** Flush stream writer.*/
	public void flushStream();

	/** Close stream writer.*/
	public void closeStream();

	/** Write nodes after document element (comments or PI's).
	 * @param doc The document node.
	 */
	public void writeXmlTail(Document doc);

}
