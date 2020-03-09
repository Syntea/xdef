package org.xdef;

/** Elements in script
 * @author Vaclav Trojan
 */
public interface XDElement extends XDContainer {

	/** Get name of underlying element or empty string.
	 * @return element name or empty string.
	 */
	public String getName();

	/** Get namespace URI of underlying element or null.
	 * @return namespace URI of underlying element or null.
	 */
	public String getNamespaceURI();

	/** Get local name of underlying element or the empty string.
	 * @return local name of underlying element or the empty string.
	 */
	public String getLocalName();

	/** Create XDContainer from this object.
	 * @return XDContainer constructed from this object.
	 */
	public XDContainer toContext();

}