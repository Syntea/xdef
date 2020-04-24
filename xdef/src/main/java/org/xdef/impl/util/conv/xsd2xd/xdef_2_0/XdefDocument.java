package org.xdef.impl.util.conv.xsd2xd.xdef_2_0;

import org.xdef.xml.KXmlUtils;
import org.xdef.impl.util.conv.xd.xd_2_0.XdNames;
import org.xdef.impl.util.conv.xsd.xsd_1_0.XsdUtils;
import org.xdef.impl.util.conv.xsd2xd.Convertor;
import org.xdef.impl.util.conv.xsd2xd.schema_1_0.Utils;
import org.xdef.impl.util.conv.xsd2xd.util.DOMUtils;
import org.xdef.impl.util.gencollection.XDGenCollection;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/** Represents X-definition document and provides methods for manipulating with
 * X-definition document. Contains table with X-definition elements and table
 * with X-definition names mapped to schema URLs.
 * @author Ilia Alexandrov
 */
public class XdefDocument implements Convertor {

	/** Prefix of X-definition nodes. */
	private final String _xdefPrefix;
	/** Every X-definition as file. */
	private final boolean _separately;
	/** Name space URI of X-definition nodes. */
	private final String _xdefNamespaceURI;
	/** Table of all X-definition elements (URL, Element). */
	private final Map<URL, Element> _xdefElements = new HashMap<URL, Element>();
	/** Table of all X-definition elements names (URL, String). */
	private final Map<URL, String> _xdefNames = new HashMap<URL, String>();

	/** Creates instance of X-definition document representation with given
	 * X-definition nodes prefix, X-definition nodes namespaceURI according
	 * to schema elements table.
	 * @param schemaElements all schema elements.
	 * @param xdefPrefix prefix for X-definition nodes.
	 * @param xdefNamespaceURI name space URI for X-definition nodes.
	 * @param separately every X-definition as file.
	 * @throws RuntimeException cant add <tt>def</tt> element to document
	 * or cant add X-definition name space declaration.
	 */
	public XdefDocument(Map<URL, Element> schemaElements, String xdefPrefix,
		String xdefNamespaceURI, boolean separately) throws RuntimeException {
		_xdefPrefix = xdefPrefix;
		_xdefNamespaceURI = xdefNamespaceURI;
		_separately = separately;
		init(schemaElements, _separately);
	}

	/** Initialize X-definition elements and names tables. Creates X-definition
	 * <tt>def</tt> element and adds name. X-definition name space declaration
	 * is also added.
	 * @param schemaElements all schema elements.
	 * @param separately every X-definition as file.
	 * @throws RuntimeException cant add <tt>def</tt> element to document
	 * or can't add
	 * X-definiton name space declaration or can't add target name space
	 * declaration.
	 */
	private void init(Map<URL, Element> schemaElements, boolean separately)
		throws RuntimeException {
		//creating document node of element nodes
		Document document = KXmlUtils.newDocument();
		//root node to add def elements to.
		Node root;
		//creating, adding xdef nemaspace and setting to root collection
		//element if not separately and not 1 def element.
		if (!separately && schemaElements.size() > 1) {
			root = addCollectionElement(document);
		} else {
			//setting document node as root
			root = document;
		}
		Iterator<Map.Entry<URL, Element>> i =
			schemaElements.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<URL, Element> entry = i.next();
			//creating name from URL
			String name = getNameFromURL(entry.getKey());
			//adding name to names table
			_xdefNames.put(entry.getKey(), name);
			if (separately) {
				//creating new document for separate xdefinitions
				root = KXmlUtils.newDocument();
			}
			//creating and adding <def> element to root node.
			Element xdef = addDefElement(root);
			//adding xdefinition name attribute
			addXdefAttr(xdef, XdNames.NAME, name);
			//resolving target namespace declaration
			addTargetNamespaceDeclaration(entry.getValue(), xdef);
			//adding <def> element to table
			_xdefElements.put(entry.getKey(), xdef);
		}
	}

	/** Adds target name space to given X-definition <tt>def</tt> element
	 * according to given XML schema <tt>schema</tt> element settings.
	 * @param schemaElement XML schema <tt>schema</tt> element.
	 * @param xdefElement X-definiton <tt>def</tt> element.
	 */
	private void addTargetNamespaceDeclaration(Element schemaElement,
			Element xdefElement) throws IllegalArgumentException {
		//has target namespace declared
		if (schemaElement.hasAttribute("targetNamespace")) {
			//cant add target namespace declaration to <def> element
			if (!DOMUtils.addNamespaceDeclaration(xdefElement, "tns",
				schemaElement.getAttribute("targetNamespace"))) {
				throw new RuntimeException(
					"Cannot add target namespace declaration to given element");
			}
		}
	}

	/** Adds X-definition name space declaration to given element with set
	 * prefix and set name space URI.
	 * @param element element to add name space declaration.
	 * @throws RuntimeException cant add name space declaration.
	 */
	private void addXdefNamespaceDeclaration(Element element)
		throws RuntimeException {
		//cant add xdefiniton namespace declaration
		if (!DOMUtils.addNamespaceDeclaration(element,
			_xdefPrefix, _xdefNamespaceURI)) {
			throw new RuntimeException("Cannot add X-definition namespace "
				+ "declaration to the given element node");
		}
	}

	/** Creates file or files from X-definition elements with given location.
	 * @param location file name of X-definition or directory name.
	 * @throws IOException if file cannot be created.
	 */
	public void createFiles(String location) throws IOException {
		if (_separately) {
			Iterator<Map.Entry<URL, Element>> i =
				_xdefElements.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry<URL, Element> entry = i.next();
				Document doc = entry.getValue().getOwnerDocument();
				File folder = new File(location);
				folder.mkdir();
				File f = new File(
					folder, _xdefNames.get(entry.getKey()) + ".xdef");
				KXmlUtils.writeXml(f.getCanonicalPath(), doc, true, true);
			}
		} else {
			Iterator<Map.Entry<URL, Element>> i =
				_xdefElements.entrySet().iterator();
			Element def = i.next().getValue();
			Document document = def.getOwnerDocument();
			KXmlUtils.writeXml(location + ".xdef", document, true, true);
		}
	}

	/** Returns <tt>true</tt> if given node is valid <tt>def</tt> element.
	 * @param node node to test.
	 * @return  <tt>true</tt> if given node is valid <tt>def</tt> element.
	 */
	public boolean isXdefElement(Node node) {
		return DOMUtils.isElement(node, _xdefNamespaceURI, XdNames.DEF);
	}

	/** Recursive method that gets ancestor <tt>def</tt> element of given
	 * node or throws exception.
	 * @param node context node.
	 * @return X-definition <tt>def</tt> element.
	 * @throws IllegalArgumentException node is not <tt>def</tt> descendant.
	 */
	public Element getXdefElement(Node node) throws IllegalArgumentException {
		return DOMUtils.getElement(node, _xdefNamespaceURI, XdNames.DEF);
	}

	/** Generates unique X-definition name from given URL. Compares names against
	 * X-definition names table as attribute.
	 * @param schemaURL url of schema.
	 * @return unique X-definition name.
	 */
	public String getNameFromURL(URL schemaURL) {
		String name = schemaURL.toExternalForm().replace('\\', '/');
		int ndx = name.lastIndexOf('/');
		if (ndx >= 0) {
			name = name.substring(ndx + 1);
		}
		if ((ndx = name.lastIndexOf('.')) > 0) {
			name = name.substring(0, ndx);
		}
		if (_xdefNames.containsValue(name)) {
			name = name + "_x";
		}
		return name;
	}

	/** Gets X-definition name from X-definition names table according to
	 * given URL as key (identification).
	 * @param url URL of schema.
	 * @return X-definition name created from schema at given URL.
	 */
	public String getXdefName(URL url) {return _xdefNames.get(url);}

	/** Gets X-definition element from X-definition elements table according
	 * to given URL as key (identification).
	 * @param url URL of schema.
	 * @return X-definition element that is created from schema at given URL.
	 */
	public Element getXdefElement(URL url) {return _xdefElements.get(url);}

	/** Gets X-definition elements table.
	 * @return X-definition elements table.
	 */
	public Map<URL, Element> getXdefElements() {return _xdefElements;}

	/**
	 * Adds element with given name space URI and local name to given parent
	 * node and returns added element. Prefix is resolved according name space.
	 * @param parent parent node.
	 * @param namespaceURI name space URI of element.
	 * @param localName local name of element.
	 * @return created element node.
	 * @throws RuntimeException cant get prefix.
	 */
	public Element addElement(Element parent,
		String namespaceURI,
		String localName) throws RuntimeException {
		Document document = parent.getOwnerDocument();
		Element element;
		if (namespaceURI == null || "".equals(namespaceURI)) {
			element = document.createElement(localName);
		} else {
			element = document.createElementNS(namespaceURI,
				getQualifiedName(parent, namespaceURI, localName));
		}
		parent.appendChild(element);
		return element;
	}

	/** Adds element without name space and with local name to given parent node
	 * and returns added element.
	 * @param parent parent node.
	 * @param localName local name of element.
	 * @return created element node.
	 * @throws RuntimeException cant get prefix.
	 */
	public Element addElement(Element parent, String localName)
		throws RuntimeException {
		return addElement(parent, null, localName);
	}

	/** Adds <tt>def</tt> element to the given parent node and returns created
	 * element.
	 * @param parent parent node of <tt>def</tt> element.
	 * @return created and added <tt>def</tt> element.
	 */
	private Element addDefElement(Node parent) {
		//parent node is document node
		if (Node.DOCUMENT_NODE == parent.getNodeType()) {
			Document doc = (Document) parent;
			Element def = doc.createElementNS(_xdefNamespaceURI,
				_xdefPrefix + ":" + XdNames.DEF);
			addXdefNamespaceDeclaration(def);
			parent.appendChild(def);
			return def;
		} else if (Node.ELEMENT_NODE == parent.getNodeType()) {
			Document doc = parent.getOwnerDocument();
			Element def = doc.createElementNS(_xdefNamespaceURI,
				_xdefPrefix + ":" + XdNames.DEF);
			parent.appendChild(def);
			return def;
		} else {
			throw new IllegalArgumentException("Node can not be <def> parent");
		}
	}

	/** Creates <tt>collection</tt> element with X-definition name space
	 * declaration and adds it to given document node.
	 * @param doc owner document node.
	 * @return created and added <tt>collection</tt> element.
	 */
	private Element addCollectionElement(Document doc) {
		Element collection = doc.createElementNS(_xdefNamespaceURI,
			_xdefPrefix + ":" + XdNames.COLLECTION);
		addXdefNamespaceDeclaration(collection);
		doc.appendChild(collection);
		return collection;
	}

	/** Adds element with X-definition name space URI and local name to given
	 * parent node and returns added element. Prefix will be resolved according
	 * given name space.
	 * @param parent parent node.
	 * @param localName X-definition name space element local name.
	 * @return created X-definition element.
	 * @throws RuntimeException cant get prefix.
	 */
	public Element addXdefElement(Element parent, String localName)
		throws RuntimeException {
		return addElement(parent, _xdefNamespaceURI, localName);
	}

	/** Adds attribute node with given name space URI and local name to given
	 * parent element node. If attribute with such name space and name exists
	 * it will throw exception.
	 * @param parent parent element node.
	 * @param namespaceURI attribute name space URI.
	 * @param localName attribute local name.
	 * @return created attribute node.
	 * @throws RuntimeException attribute already exists.
	 */
	public Attr addAttr(Element parent,
		String namespaceURI,
		String localName) throws RuntimeException {
		if (parent.hasAttributeNS(namespaceURI, localName)) {
			throw new RuntimeException("Attribute with given namespace URI ["
				+ namespaceURI + "]" + "and local name [" + localName
				+ "] already exists in given element node ["
				+ KXmlUtils.getXPosition(parent) + "]");
		}
		Attr attr;
		//if namespace is missing
		if (namespaceURI == null || "".equals(namespaceURI)) {
			attr = parent.getOwnerDocument().createAttribute(localName);
			parent.setAttributeNode(attr);
		} else {
			attr = parent.getOwnerDocument().createAttributeNS(namespaceURI,
				getQualifiedName(parent, namespaceURI, localName));
			parent.setAttributeNodeNS(attr);
		}
		return attr;
	}

	/** Adds attribute node with given name space URI, local name and value
	 * to given parent element node. If attribute with such name space name
	 * exists it will throw exception.
	 * @param parent parent element node.
	 * @param namespaceURI attribute name space URI.
	 * @param localName attribute local name.
	 * @param value attribute value.
	 * @return created attribute node.
	 * @throws RuntimeException attribute already exists.
	 */
	public Attr addAttr(Element parent,
		String namespaceURI,
		String localName,
		String value) throws RuntimeException {
		Attr attr = addAttr(parent, namespaceURI, localName);
		attr.setValue(value);
		return attr;
	}

	/** Adds attribute node with X-definition name space URI, given local name
	 * and given value to given parent element node. If attribute with such
	 * name space name exists it will throw exception.
	 * @param parent parent element node.
	 * @param localName X-definition attribute local name.
	 * @param value attribute value.
	 * @return created X-definition attribute node.
	 * @throws RuntimeException attribute already exists.
	 */
	public Attr addXdefAttr(Element parent, String localName, String value)
		throws RuntimeException {
		return addAttr(parent, _xdefNamespaceURI, localName, value);
	}

	/** Adds attribute node with X-definition name space URI and given local name
	 * to given parent element node. If attribute with such name space name
	 * exists, will throw exception.
	 * @param parent parent element node.
	 * @param localName X-definition name space attribute local name.
	 * @return created X-definition attribute node.
	 * @throws RuntimeException attribute already exists.
	 */
	public Attr addXdefAttr(Element parent, String localName)
		throws RuntimeException {
		return addAttr(parent, _xdefNamespaceURI, localName);
	}

	/** Gets prefix of given name space URI in given node context. If prefix
	 * does not exists, it will create one in <tt>def</tt> element and
	 * return prefix.
	 * @param node context node.
	 * @param namespaceURI name space URI.
	 * @return given name space prefix.
	 * @throws RuntimeException cant add name space declaration.
	 */
	public String getPrefix(Node node, String namespaceURI)
		throws RuntimeException {
		//getting def element
		Element xdef = getXdefElement(node);
		String prefix;
		//def element already contains ns declaration with given URI
		if ((prefix = DOMUtils.getNamespaceDeclarationPrefix(xdef,namespaceURI))
			!= null) {
			return prefix;
		}
		//def element does not contain ns URI declaration
		//collection exists and contains declaration (for xd prefix)
		if ((xdef.getParentNode() != null&&xdef.getParentNode().getParentNode()
			!= null) && (prefix = DOMUtils.getNamespaceDeclarationPrefix(
				(Element) xdef.getParentNode(), namespaceURI)) != null) {
			return prefix;
		}
		//namespace URI declaration does not exists
		return DOMUtils.addNamespaceDeclaration(xdef, namespaceURI);
	}

	/** Generates qualified node name from given name space URI and given node
	 * local name in given node context.
	 * @param node context node.
	 * @param namespaceURI name space URI of qualified name.
	 * @param localName local part of qualified name.
	 * @return qualified name.
	 * @throws RuntimeException cant get prefix.
	 */
	public String getQualifiedName(Node node, String namespaceURI,
		String localName) throws RuntimeException {
		return getPrefix(node, namespaceURI) + ":" + localName;
	}

	/** Adds reference expression to script attribute of given element node.
	 * If element node does not contain script attribute it will create one.
	 * @param element element to add script expression.
	 * @param xdefName name of X-definiton that contains referred model.
	 * @param namespaceURI name space URI of referred model.
	 * @param modelName local name of referred model.
	 */
	public void addRefExpression(Element element, String xdefName,
		String namespaceURI, String modelName) {
		String refPref =
			getRefExpression(element, xdefName, namespaceURI, modelName);
		addXdefScriptExpression(element, refPref);
	}

	/** Adds X-definition script expression to given element. If element does
	 * not have script attribute it will make one. If element does have
	 * script attribute it will add expression to the end of script.
	 * @param element element to add script attribute with expression.
	 * @param expression script expression.
	 */
	private void addXdefScriptExpression(Element element, String expression) {
		String xdNS = XDGenCollection.findXDNS(element);
		if (element.hasAttributeNS(xdNS, XdNames.SCRIPT)) {
			Attr script =
				element.getAttributeNodeNS(xdNS, XdNames.SCRIPT);
			String prevous;
			if ("".equals(prevous = script.getValue())) {
				script.setValue(expression);
			} else {
				script.setValue(prevous + "; " + expression);
			}
		} else {
			addXdefAttr(element, XdNames.SCRIPT, expression);
		}
	}

	/** Adds type declaration with given name and given declaration to given
	 * X-definition <tt>def</tt> element.
	 * @param defElement <tt>def</tt> element.
	 * @param name name of type.
	 * @param declaration declaration string.
	 * @return added declaration element.
	 */
	public Element addTypeDeclaration(Element defElement,
		String name,
		String declaration) {
		Element element = addXdefElement(defElement, XdNames.DECLARATION);
/*VT2*/
		element.setAttribute("scope", "global");
		if (declaration.isEmpty()) {
			declaration = "string()";
		}
/*VT2*/
		Text text = defElement.getOwnerDocument().createTextNode("type "
			+ name + " " + declaration + ";");
		element.appendChild(text);
		defElement.appendChild(element);
		return element;
	}

	/** Adding given text to the given node. Supports attribute and element
	 * nodes. If there is some text already, given text will be added to end.
	 * @param parent parent node.
	 * @param text text to add.
	 * @throws IllegalArgumentException not supported node type.
	 */
	public void addText(Node parent, String text)
		throws IllegalArgumentException {
		if (Node.ATTRIBUTE_NODE != parent.getNodeType()
			&& Node.ELEMENT_NODE != parent.getNodeType()) {
			throw new IllegalArgumentException(
				"Given node type is not supported by current method");
		}
		//parent node is attribute
		if (Node.ATTRIBUTE_NODE == parent.getNodeType()) {
			Attr attr = (Attr) parent;
			//getting prevous text
			String prevousText = attr.getValue();
			//adding prevous text with new text
			attr.setValue(prevousText + "" + text);
		} else {
			Element element = (Element) parent;
			//creating text node
			Text textNode = element.getOwnerDocument().createTextNode(text);
			//adding text node
			element.appendChild(textNode);
		}
	}

	/** Generates reference expression to given X-definition by name, given
	 * name space URI and local name of model in given node context.
	 * @param node context node.
	 * @param xdefName name of X-definiiton or null.
	 * @param namespaceURI name space URI of model or null.
	 * @param modelName name of model.
	 * @return reference script expression.
	 * @throws RuntimeException cant get prefix.
	 */
	public String getRefExpression(Node node,
		String xdefName,
		String namespaceURI,
		String modelName) throws RuntimeException {
		String ref = "ref ";
		//adding xdefiniiton name
		if (xdefName != null && !"".equals(xdefName)) {
/*VT*/
			ref += xdefName + '#';
/*VT*/
		}
		//adding prefix
		if (namespaceURI != null && !"".equals(namespaceURI)) {
			ref += getPrefix(node, namespaceURI) + ":";
		}
		//adding model name
		ref += modelName;
		return ref;
	}

	/** Creates ref string according to given parameters.
	 * @param schemaURL URL of schema containing reference.
	 * @param declaration declaration of element.
	 * @param xdefContextElement context of X-definition.
	 * @return ref string.
	 */
	public String getRefString(URL schemaURL,
		Element xdefContextElement,
		GlobalDeclaration declaration) {
		String refString = "ref ";
		//declaration is in other schema - ref to xdef
		if (!schemaURL.equals(declaration.getSchemaURL())) {
			refString += _xdefNames.get(declaration.getSchemaURL()) + '#';
		}
		String namespace =
			Utils.getNamespace(declaration.getGlobalDeclarationElement());
		//namespace is not empty
		if (!"".equals(namespace)) {
			refString += getPrefix(xdefContextElement, namespace) + ':';
		}
		return refString += declaration.getName();
	}

	/** Generates occurrence expression and adds it to script attribute of given
	 * element node.
	 * @param element element to add script expression to script attribute.
	 * @param minOccurs min count of occurrence.
	 * @param maxOccurs max count of occurrence.
	 * @throws RuntimeException illegal occurrence parameters or can create
	 * script attribute.
	 */
	public void addOccurrenceExpression(Element element,
		Integer minOccurs,
		Integer maxOccurs) throws RuntimeException {
		String occurrence = getOccurrenceExpression(minOccurs, maxOccurs);
		addXdefScriptExpression(element, occurrence);
	}

	/** Adds presence and type declaration to given node.
	 * @param node node to add presence and type.
	 * @param presence presence of given node.
	 * @param typeDeclaration declaration of node type.
	 * @throws IllegalArgumentException if type is not legal.
	 */
	public void addType(Node node, String presence, String typeDeclaration)
		throws IllegalArgumentException {
		if (Node.ATTRIBUTE_NODE != node.getNodeType()
			&& Node.TEXT_NODE != node.getNodeType()) {
			throw new IllegalArgumentException("Given node type is illegal");
		}
		String expression = (presence != null || !"".equals(presence))
			? presence + " " + typeDeclaration : typeDeclaration;
		//attribute
		if (Node.ATTRIBUTE_NODE == node.getNodeType()) {
			Attr attr = (Attr) node;
			if (!attr.getValue().isEmpty()) {
				attr.setValue(expression);
			}
		} else {
			addXdefScriptExpression((Element) node, expression);
		}
	}

	/** Generates occurrence expression according given min and max occurrences.
	 * @param minOccurs minimal count.
	 * @param maxOccurs maximal count or -1 for unbounded.
	 * @return occurrence string.
	 * @throws IllegalArgumentException max count is less than min count or
	 * min count is less than 0.
	 */
	public String getOccurrenceExpression(Integer minOccurs, Integer maxOccurs)
		throws IllegalArgumentException {
		//throw exception if min > max
		if (minOccurs < 0 ||
			(maxOccurs != -1 && maxOccurs < minOccurs)) {
			throw new IllegalArgumentException(
				"Max count can not be lesser than min count");
		}
		//initiating occurrence expression
		String occurs = "occurs ";
		//setting fixed value
		if (minOccurs.equals(maxOccurs)) {
			return occurs += minOccurs;
		}
		//adding min count
		occurs += minOccurs + "..";
		//adding max count
		if (maxOccurs == -1) {
			occurs += "*";
		} else {
			occurs += maxOccurs;
		}
		return occurs;
	}

	/** Adds given element object to the given parent element and returns
	 * created element.
	 * @param parentElement element to add given child element.
	 * @param element element object to add.
	 * @return created and added element.
	 */
	public Element addElement(Element parentElement,
		XsdUtils.ElemProps element) {
		//creating element
		Element newElement =
			addElement(parentElement, element.getNamespace(),element.getName());
		//script attribute
		String scriptText = "";
		//resolving occurrence
		if (element.getOccurrence() != null) {
			scriptText += element.getOccurrence();
		}
		//resolving ref
		if (element.getRef() != null) {
			scriptText += ("".equals(scriptText) ? "" : "; ")+element.getRef();
		}
		//resolving options
		if (element.isAnyType() || element.isNillable()) {
			scriptText += ("".equals(scriptText) ? "" : "; ") + "options ";
			boolean nillableDeclared = false;
			//resolving nillable
			if (element.isNillable()) {
				scriptText += "nillable";
				nillableDeclared = true;
			}
			//resolving any type
			if (element.isAnyType()) {
				scriptText += (nillableDeclared ? ", " : "")
					+ "moreElements, moreAttributes, moreText";
			}
		}
		//text node
		String text = "";
		//setting fixed value
		if (element.getText() != null) {
			text = element.getText();
		}
		if (element.getFixed() != null) {
			text += ("".equals(text) ? "" : "; ")
				+ "fixed '" + element.getFixed() + "'";
		} else if (element.getDefault() != null) {
			text += ("".equals(text) ? "" : "; ")
				+ "default('" + element.getDefault() + "')";
		}
		//adding script attribute
		if (!"".equals(scriptText)) {
			addXdefAttr(newElement, XdNames.SCRIPT, scriptText);
		}
		//adding text value
		if (!"".equals(text)) {
			addText(newElement, text);
		}
		return newElement;
	}

	/** Adds attribute to given parent element according to given attribute
	 * representation object.
	 * @param parentElement parent element to add attribute to.
	 * @param attribute attribute representation.
	 * @return created and added attribute.
	 */
	public Attr addAttr(Element parentElement, XsdUtils.AttrProps attribute) {
		String text = ""; //attribute node value
		//attribute is not prohibited
		if (!"illegal".equals(attribute.getUse())) {
			//attribute has fixed value
			if (attribute.getFixed() != null) {
/*VT1*/
				String type = attribute.getType();
				if (!"string(0, $MAXINT)".equals(type)) {
					text += (text.isEmpty() ? "" : " ") + type + ";";
				}
/*VT1*/
				//adding fixed value
				text += (text.isEmpty() ? "" : " ")
					+ "fixed '" + attribute.getFixed() + "'";
				//attribute has not fixed value
			} else {
				//adding use
				text += attribute.getUse();
/*VT1*/
				String type = attribute.getType();
				if (!"string(0, $MAXINT)".equals(type)) {
					text += (text.isEmpty() ? "" : " ") + type + ";";
				}
/*VT1*/
				//adding type declaration
				if (attribute.getDefault() != null) {
					//adding default value
/*VT1*/
					text += (text.isEmpty() ? "" : " ")
						+ "default('" + attribute.getDefault() + "')";
/*VT1*/
				}
			}
			//attributeis prohibited
		} else {
			//adding use
			text += attribute.getUse();
		}
		//returning created and added attribute node
		return addAttr(parentElement,
			attribute.getNamespace(), attribute.getName(), text.trim());
	}

	/** Sets element to be as any type in schema.
	 * @param element X-definition element.
	 */
	public void setAnyType(Element element) {
		addXdefScriptExpression(element,
			"options moreAttributes, moreElements, moreText");
	}

	/** Adds declaration of included X-definition with given name to given
	 * X-definition <tt>def</tt> element.
	 * @param xdefElement X-definition <tt>def</tt> element.
	 * @param schemaURL URL of schema that is X-definition element created from.
	 */
	public void addIncludeXdef(Element xdefElement, URL schemaURL) {
		//included X-definition file name
		String xdefFileName = _xdefNames.get(schemaURL) + ".xdef";
		//include attribute
		Attr include;
		if ((include = xdefElement.getAttributeNodeNS(_xdefNamespaceURI,
			XdNames.INCLUDE)) != null) {
			String prevousText = include.getValue();
			include.setValue(("".equals(prevousText) ? "" : prevousText + ", ")
					+ xdefFileName);
		} else {
			addXdefAttr(xdefElement, XdNames.INCLUDE, xdefFileName);
		}
	}

	/** Adds root element declaration to given <tt>def</tt> element.
	 * @param xdefElement X-definition <tt>def</tt> element.
	 * @param namespaceURI name space URI of root element.
	 * @param localName local name of root element.
	 */
	public void addRootElement(Element xdefElement,
		String namespaceURI,
		String localName) {
		Attr root;
		String name = ("".equals(namespaceURI) ? "" :
			getPrefix(xdefElement, namespaceURI) + ":") + localName;
		if ((root = xdefElement.getAttributeNodeNS(_xdefNamespaceURI,
			XdNames.ROOT)) != null) {
			root.setValue(root.getValue() + " | " + name);
		} else {
			addXdefAttr(xdefElement, XdNames.ROOT, name);
		}
	}

	/** Adds text node with given text to the given parent element.
	 * @param parentElement element to add text node to.
	 * @param text value of text node.
	 * @return created text node.
	 */
	public Text addText(Element parentElement, String text) {
		Text textNode = parentElement.getOwnerDocument().createTextNode(text);
		return (Text) parentElement.appendChild(textNode);
	}

	@Override
	public void writeCollection(String collectionFileName) throws IOException,
			IllegalStateException {
		if (_separately) {
			throw new IllegalStateException(
				"Document state does not support this method");
		}
		Iterator<Map.Entry<URL, Element>> i=_xdefElements.entrySet().iterator();
		Element def = i.next().getValue();
		Document document = def.getOwnerDocument();
		KXmlUtils.writeXml(collectionFileName, document, true, true);
	}

	@Override
	public void writeXdefFiles(String directoryName)
		throws IOException, IllegalStateException {
		if (!_separately) {
			throw new IllegalStateException(
				"Document state does not support this method");
		}
		File folder = new File(directoryName);
		folder.mkdir();
		Iterator<Map.Entry<URL, Element>> i =
			_xdefElements.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<URL, Element> entry = i.next();
			Document doc = entry.getValue().getOwnerDocument();
			String fName = new File(folder,
				(String) _xdefNames.get(entry.getKey())).getCanonicalPath();
			KXmlUtils.writeXml(fName, doc, true, true);
		}
	}

	@Override
	public Document getCollectionDocument() throws IllegalStateException {
		if (_separately) {
			throw new IllegalStateException(
				"Document state does not support this method");
		}
		Iterator<Map.Entry<URL, Element>> i =
			_xdefElements.entrySet().iterator();
		return i.next().getValue().getOwnerDocument();
	}

	@Override
	public Set<Document> getXdefDocuments() throws IllegalStateException {
		if (!_separately) {
			throw new IllegalStateException(
				"Document state does not support this method");
		}
		Set<Document> ret = new HashSet<Document>();
		Iterator<Element> i = _xdefElements.values().iterator();
		while (i.hasNext()) {
			Element defElement = i.next();
			ret.add(defElement.getOwnerDocument());
		}
		return ret;
	}

	@Override
	public void printCollection() {
		if (_separately) {
			throw new IllegalStateException(
				"Document does not support this method");
		}
		try {
			KXmlUtils.writeXml(new OutputStreamWriter(System.out),
				getCollectionDocument(), true, true, true);
		} catch (IOException ex) {
			throw new RuntimeException(
				"Could not write to standart output stream", ex);
		}
	}
}