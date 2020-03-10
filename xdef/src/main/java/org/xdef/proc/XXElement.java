package org.xdef.proc;

import org.xdef.sys.SRuntimeException;
import org.xdef.model.XMElement;

/** Model of element in X-definition.
 * @author Vaclav Trojan
 */
public interface XXElement extends XXNode {

	/** Set this element will be forgotten after being processed.*/
	public void forgetElement();

////////////////////////////////////////////////////////////////////////////////
// Generation of XML objects
////////////////////////////////////////////////////////////////////////////////

	/** Prepare construction of the new element according to X-definition.
	 * @param qname qualified name of the element (prefixed).
	 * @param ns namespace URI of the element.
	 * @return created check element object.
	 */
	public XXElement prepareXXElementNS(String ns, String qname);

	/** Prepare construction of the new element according to X-definition.
	 * @param name Tag name of the element.
	 * @return created check element object.
	 */
	public XXElement prepareXXElement(String name);

	/** Prepare construction of the new child according to X-definition.
	 * @param model child model.
	 * @return created XXElemnt element object.
	 */
	public XXElement createChildXXElement(XMElement model);

	/** Add constructed element as a child to the XXElement.
	 * Checks all attributes and child nodes for occurrence.
	 * @return <tt>true</tt> if element was added and complies to X-definition.
	 */
	public boolean addElement();

	/** This method is called when the end of the current element attribute list
	 * was parsed. The implementation may check the list of attributes and
	 * may invoke appropriate actions.
	 * @return <tt>true</tt> if element is compliant with definition.
	 */
	public boolean checkElement();

	/** Add new Text node to current element.
	 * @param textValue The value of text node.
	 * @throws SRuntimeException if an error occurs.
	 * @return <tt>true</tt> if text node is compliant with definition.
	 */
	public boolean addText(String textValue);

	/** Add new Comment node to current element.
	 * @param data The value of Comment node.
	 * @return <tt>true</tt> if Comment node is compliant with definition.
	 */
	public boolean addComment(String data);

	/** Add new Processing instruction node to current element.
	 * @param name The name of the PI node.
	 * @param data The value of instruction part of the PI node.
	 * @throws SRuntimeException if an error occurs.
	 * @return <tt>true</tt> if PI node is compliant with definition.
	 */
	public boolean addPI(String name, String data);

	/** Add the new attribute to the current XXElement.
	 * @param qname The qualified name of attribute (including prefix).
	 * @param value The value of attribute.
	 * @param nsURI The value of namespace URI.
	 * @return <tt>true</tt> if attribute was created according to definition.
	 */
	public boolean addAttributeNS(String nsURI, String qname, String value);

	/** Add the new attribute to the current XXElement.
	 * @param name The name of attribute.
	 * @param value The value of attribute.
	 * @return <tt>true</tt> if attribute was created according to definition.
	 */
	public boolean addAttribute(String name, String value);

	/** Check if attribute is legal in the XXElement.
	 * @param name The name of attribute.
	 * @return <tt>true</tt> if and only if the attribute is legal in the
	 * XXElement, otherwise return <tt>false</tt>.
	 */
	public boolean checkAttributeLegal(String name);

	/** Get attribute from XXElement.
	 * @param name The name of attribute.
	 * @return value of attribute or the empty string if the attribute is legal
	 * otherwise throws the SRuntimeException.
	 * @throws SRuntimeException if the attribute is not legal in actual model.
	 */
	public String getAttribute(String name) throws SRuntimeException;

	/** Check if attribute with given namespace is legal in the XXElement.
	 * @param uri The namespace of attribute.
	 * @param name name of attribute (optionally with prefix).
	 * @return <tt>true</tt> if and only if the attribute is legal in the
	 * XXElement, otherwise return <tt>false</tt>.
	 */
	public boolean checkAttributeNSLegal(String uri, final String name);

	/** Get attribute with namespace from XXElement.
	 * @param uri The namespace of attribute.
	 * @param name The name of attribute.
	 * @return value of attribute or the empty string if the attribute is legal
	 * otherwise throws the SRuntimeException.
	 * @throws SRuntimeException if the attribute is not legal in actual model.
	 */
	public String getAttributeNS(String uri, String name)
		throws SRuntimeException;

}