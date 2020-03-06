package org.xdef.component;

import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** Interface of XComponent (Java class generated from X-definitions according
 * to a model of Element). Java source of the XComponent is possible to generate
 * by {@link org.xdef.component.GenXComponent}.
 * @author Vaclav Trojan
 */
public interface XComponent {

	/** Create XML element from this XComponent (marshal).
	 * If the argument is null <tt>null</tt> then document is created with
	 * created document element.
	 * @return XML element created from this object.
	 */
	public Element toXml();

	/** Create XML element or text node from default model
	 * as an element created from given document.
	 * @param doc XML Document or <tt>null</tt>.
	 * If the argument is null <tt>null</tt> then document is created with
	 * created document element.
	 * @return XML node belonging to given document from this XComponent.
	 */
	public Node toXml(Document doc);

	/** Create JSON object from this XComponent (marshal to JSON).
	 * @return JSON object created from this XComponent.
	 */
	public Object toJson();

	/** Get name of element used for construction of this object.
	 * @return name of element used for construction of this object.
	 */
	public String xGetNodeName();

	/** Get namespace of model used for construction of this object.
	 * @return namespace of model used for construction of this object.
	 */
	public String xGetNamespaceURI();

	/** Get parent XComponent.
	 * @return parent XComponent object or null if this object is root.
	 */
	public XComponent xGetParent();

	/** Get XPosition of node.
	 * @return XPosition of node.
	 */
	public String xGetXPos();

	/** Set XPosition of node.
	 * @param xpos XPosition of node.
	 */
	public void xSetXPos(String xpos);

	/** Get index of node.
	 * @return index of node.
	 */
	public int xGetNodeIndex();

	/** Set index of node.
	 * @param index index of node.
	 */
	public void xSetNodeIndex(int index);

	/** Get XDPosition of this XComponent.
	 * @return string withXDPosition of this XComponent.
	 */
	public String xGetModelPosition();

	/** Get index of model of this XComponent.
	 * @return index of model of this XComponent.
	 */
	public int xGetModelIndex();

	/** Get user object.
	 * @return user object.
	 */
	public Object xGetObject();

	/** Set user object.
	 * @param obj user object.
	 */
	public void xSetObject(Object obj);

	/** Create list of XComponents from which will be created XML.
	 * @return list of XComponents.
	 */
	public List<XComponent> xGetNodeList();

	/** Create instance of child XComponent.
	 * @param xx actual XXNode.
	 * @return new empty child XCopmponent object.
	 */
	public XComponent xCreateXChild(XXNode xx);

	/** Add XComponent object to local variable.
	 * @param xc XComponent to be added.
	 */
	public void xAddXChild(XComponent xc);

	/** Set value of xd:any model.
	 * @param el Element which is value of xd:any model.
	 */
	public void xSetAny(Element el);

	/** Set value of text node.
	 * @param xx Actual XXNode (from text node).
	 * @param parseResult parsed value.
	 */
	public void xSetText(XXNode xx, XDParseResult parseResult);

	/** Set value of attribute.
	 * @param xx Actual XXNode (from attribute model).
	 * @param parseResult parsed value.
	 */
	public void xSetAttr(XXNode xx, XDParseResult parseResult);

	/** Update parameters of XComponent.
	 * @param p parent XComponent.q
	 * @param name name of element.
	 * @param ns name space.
	 * @param xPos XDPosition.
	 */
	public void xInit(XComponent p, String name, String ns, String xPos);
}