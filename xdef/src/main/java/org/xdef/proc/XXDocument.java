package org.xdef.proc;

import org.w3c.dom.Element;

/** Model of document in X-definition.
 * @author Vaclav Trojan
 */
public interface XXDocument extends XXNode {

	/** Prepare construction of the new document according to X-definition.
	 * @return created check document object.
	 */
	public XXDocument prepareXXDocument();

	/** Add constructed element as a child to the XXElement.
	 * Checks all attributes and child nodes for occurrence.
	 * @param el element to be added.
	 * @return <tt>true</tt> if element was added and complies to X-definition.
	 */
	public boolean addElement(Element el);

	/** This method is called when the end of the current element attribute list
	 * was parsed. The implementation may check the list of attributes and
	 * may invoke appropriate actions.
	 * @return <tt>true</tt> if element is compliant with definition.
	 */
	public boolean checkDocument();

	/** Add new Comment node to current element.
	 * @param data The value of Comment node.
	 * @return <tt>true</tt> if Comment node is compliant with definition.
	 */
	public boolean addComment(String data);

	/** Add new Processing instruction node to current element.
	 * @param name The name of the PI node.
	 * @param data The value of instruction part of the PI node.
	 * @return <tt>true</tt> if PI node is compliant with definition.
	 */
	public boolean addPI(String name, String data);

}