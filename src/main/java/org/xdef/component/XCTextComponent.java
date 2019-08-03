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
public class XCTextComponent implements XComponent {
	private String _value;
	private String _xpos;
	private String _model;
	private int _index;

	public XCTextComponent(String value, String xdPos, String xpos, int index) {
		_value = value;
		_model = xdPos;
		_xpos = xpos;
		_index = index;
	}

	/** Get value of text node.
	 * @return parsed value.
	 */
	public Object xGetValue() {return null;}

	@Override
	/** Create XML element from this XComponent (marshal).
	 * If the argument is null <tt>null</tt> then document is created with
	 * created document element.
	 * @return XML element created from this object.
	 */
	public Element toXml() {return null;}

	@Override
	/** Create XML element or text node from default model
	 * as an element created from given document.
	 * @param doc XML Document or <tt>null</tt>.
	 * If the argument is null <tt>null</tt> then document is created with
	 * created document element.
	 * @return XML node belonging to given document from default model.
	 */
	public Node toXml(Document doc) {return doc.createTextNode(_value);}

	@Override
	/** Create JSON object from this XComponent (marshal to JSON).
	 * @return JSON object created from this XComponent.
	 */
	public Object toJson() {return null;}

	@Override
	/** Get name of element model used for construction of this object.
	 * @return name of element model used for construction of this object.
	 */
	public String xGetNodeName() {return "$text";}

	@Override
	/** Get namespace of model used for construction of this object.
	 * @return namespace of model used for construction of this object.
	 */
	public String xGetNamespaceURI() {return null;}

	@Override
	/** Get parent XComponent.
	 * @return parent XComponent object or null if this object is root.
	 */
	public XCTextComponent xGetParent() {return null;}

	@Override
	/** Get XPosition of node.
	 * @return XPosition of node.
	 */
	public String xGetXPos() {return _xpos;}
	@Override
	/** Set XPosition of node.
	 * @param xpos XPosition of node.
	 */
	public void xSetXPos(String xpos){_xpos = xpos;}

	@Override
	/** Get XDPosition of this XComponent.
	 * @return string withXDPosition of this XComponent.
	 */
	public String xGetModelPosition() {return _model;}

	@Override
	/** Get index of model of this XComponent.
	 * @return index of model of this XComponent.
	 */
	public int xGetModelIndex() {return -1;}

	@Override
	/** Create list of XComponents from which will be created XML.
	 * @return list of XComponents.
	 */
	public List<XComponent> xGetNodeList() { return null;}

	@Override
	/** Get index of node.
	 * @return index of node.
	 */
	public int xGetNodeIndex() {return _index;}

	@Override
	/** Set index of node.
	 * @param index index of node.
	 */
	public void xSetNodeIndex(int index) {_index = index;}

	@Override
	/** Get user object.
	 * @return user object.
	 */
	public Object xGetObject() {return null;}

	@Override
	/** Set user object.
	 * @param obj user object.
	 */
	public void xSetObject(Object obj) {}

	@Override
	/** Create instance of child XComponent.
	 * @param xn actual XXNode.
	 * @return new empty child XCopmponent object.
	 */
	public XCTextComponent xCreateXChild(XXNode xn) {return null;}

	@Override
	/** Set value of text node.
	 * @param xx Actual XXNode (from text node).
	 * @param parseResult parsed value.
	 */
	public void xSetText(XXNode xx, XDParseResult parseResult) {
		if (xx != null) {
			_model = xx.getXMNode().getXDPosition();
			_xpos = xx.getXPos();
		}
		_value = parseResult.getSourceBuffer();
	}

	@Override
	/** Set value of attribute.
	 * @param xx Actual XXNode (from attribute model).
	 * @param parseResult parsed value.
	 */
	public void xSetAttr(XXNode xx, XDParseResult parseResult) {}
	@Override
	public void xAddXChild(XComponent xc) {}
	@Override
	public void xSetAny(Element el) {}

	@Override
	/** Set name of element used for construction of this object.
	 * @param name name of element.
	 */
	public void xInit(XComponent p, String name, String ns, String xPos){}

}