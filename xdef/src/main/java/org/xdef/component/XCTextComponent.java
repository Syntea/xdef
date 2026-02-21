package org.xdef.component;

import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** Implementation of XComponent for text nodes.
 * @author Vaclav Trojan
 */
public class XCTextComponent implements XComponent {
	private String _value;
	private String _xpos;
	private String _model;
	private int _index;

	/** Create new instance of XCTextComponent.
	 * @param value value with text.
	 * @param model XPosition of this text in the model.
	 * @param xpos XPosition of text node (may be null).
	 * @param index index of node.
	 */
	public XCTextComponent(final String value, final String model, final String xpos, final int index) {
		_value = value;
		_model = model;
		_xpos = xpos;
		_index = index;
	}

	/** Get value of text node.
	 * @return parsed value.
	 */
	public Object xGetValue() {return _value;}

	/** Create XML element from this XComponent (marshal). If the argument is null then document is created
	 * with created document element.
	 * @return XML element created from this object.
	 */
	@Override
	public Element toXml() {return null;}

	/** Create XML element or text node from default model as element created from given document.
	 * @param doc XML Document or null. If the argument is null then document is created with created
	 * document element.
	 * @return XML node belonging to given document from default model.
	 */
	@Override
	public Node toXml(final Document doc) {return doc.createTextNode(_value);}

	/** Get name of element model used for construction of this object.
	 * @return name of element model used for construction of this object.
	 */
	@Override
	public String xGetNodeName() {return "$text";}

	/** Get namespace of model used for construction of this object.
	 * @return namespace of model used for construction of this object.
	 */
	@Override
	public String xGetNamespaceURI() {return null;}

	/** Get parent XComponent.
	 * @return parent XComponent object or null if this object is root.
	 */
	@Override
	public XCTextComponent xGetParent() {return null;}

	/** Get XPosition of node.
	 * @return XPosition of node.
	 */
	@Override
	public String xGetXPos() {return _xpos;}

	/** Set XPosition of node.
	 * @param xpos XPosition of node.
	 */
	@Override
	public void xSetXPos(String xpos) {_xpos = xpos;}

	/** Get XDPosition of this XComponent.
	 * @return string withXDPosition of this XComponent.
	 */
	@Override
	public String xGetModelPosition() {return _model;}

	/** Get index of model of this XComponent.
	 * @return index of model of this XComponent.
	 */
	@Override
	public int xGetModelIndex() {return -1;}

	/** Create list of XComponents from which will be created XML.
	 * @return list of XComponents.
	 */
	@Override
	public List<XComponent> xGetNodeList() { return null;}

	/** Get index of node.
	 * @return index of node.
	 */
	@Override
	public int xGetNodeIndex() {return _index;}

	/** Set index of node.
	 * @param index index of node.
	 */
	@Override
	public void xSetNodeIndex(final int index) {_index = index;}

	/** Get user object.
	 * @return user object.
	 */
	@Override
	public Object xGetObject() {return null;}

	/** Set user object.
	 * @param obj user object.
	 */
	@Override
	public void xSetObject(final Object obj) {}

	/** Create instance of child XComponent.
	 * @param xn actual XXNode.
	 * @return new empty child XCopmponent object.
	 */
	@Override
	public XCTextComponent xCreateXChild(final XXNode xn) {return null;}

	/** Set value of text node.
	 * @param xnode Actual XXNode (from text node).
	 * @param value parsed value.
	 */
	@Override
	public void xSetText(final XXNode xnode, final XDParseResult value) {
		if (xnode != null) {
			_model = xnode.getXMNode().getXDPosition();
			_xpos = xnode.getXPos();
		}
		_value = value.getSourceBuffer();
	}

	/** Set value of attribute.
	 * @param xnode Actual XXNode (from attribute model).
	 * @param value parsed value.
	 */
	@Override
	public void xSetAttr(final XXNode xnode, final XDParseResult value) {}

	/** Add XComponent object to local variable.
	 * @param xc XComponent to be added.
	 */
	@Override
	public void xAddXChild(final XComponent xc) {}

	/** Set value of xd:any model.
	 * @param el Element which is value of xd:any model.
	 */
	@Override
	public void xSetAny(final Element el) {}

	/** Set name of element used for construction of this object.
	 * @param name name of element.
	 */
	@Override
	public void xInit(final XComponent p, final String name, final String ns, final String xPos) {}

	/** Get XON object from this X-component.
	 * @return XON object if this X-component.
	 */
	@Override
	public String toXon() {return _value;}
}