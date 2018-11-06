package cz.syntea.xdef.xml;

import org.w3c.dom.Document;

/** Result of parsed XML.
 * @author Vaclav Trojan
 */
public interface KXmlParsedResult {

	/** Get parsed XML document.
	 * @return parsed XML document.
	 */
	public Document getDocument();

	/** Get encoding of parsed document.
	 * @return encoding of parsed document.
	 */
	public String getXmlEncoding();

	/** Get version of XML document.
	 * @return version of XML document.
	 */
	public String getXmlVersion();
}