package org.xdef.impl.util.conv.xd.xd_2_0;

import org.xdef.XDConstants;
import org.xdef.impl.util.gencollection.XDParsedScript;
import org.xdef.impl.util.conv.Util;
import org.xdef.impl.util.conv.Util.MyQName;
import org.xdef.impl.util.conv.xd.xd_2_0.domain.XdDef;
import org.xdef.impl.util.conv.xd.xd_2_0.domain.XdElem;
import org.xdef.impl.util.conv.xd.xd_2_0.domain.XdGroup;
import org.xdef.impl.util.conv.xd.xd_2_0.domain.XdModel;
import org.xdef.xml.KXmlUtils;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Provides static methods for working with X-definition version 2.0 documents.
 * @author Ilia Alexandrov
 */
public final class XdUtils {

	/** Private constructor. */
	private XdUtils() {}

	//--------------------------------------------------------------------------
	//                          XDEF NODES UTILS
	//--------------------------------------------------------------------------
	/** Returns <tt>true</tt> if given node is a valid X-definition
	 * <tt>collection</tt> element.
	 * @param node node to test.
	 * @return <tt>true</tt> if given node is a valid X-definition
	 * <tt>collection</tt> element.
	 */
	public static boolean isCollection(final Node node) {
		return Util.isElement(node,
			XDConstants.XDEF20_NS_URI, XdNames.COLLECTION)
			|| Util.isElement(node,
				XDConstants.XDEF31_NS_URI, XdNames.COLLECTION)
			|| Util.isElement(node,
				XDConstants.XDEF32_NS_URI, XdNames.COLLECTION)
			|| Util.isElement(node,
				XDConstants.XDEF40_NS_URI, XdNames.COLLECTION);
	}

	/** Returns <tt>true</tt> if given node is a valid X-definition <tt>def</tt>
	 * element.
	 * @param node node to test.
	 * @return <tt>true</tt> if given node is a valid X-definition <tt>def</tt>
	 * element.
	 */
	public static boolean isDef(final Node node) {
		return Util.isElement(node, XDConstants.XDEF20_NS_URI, XdNames.DEF)
			|| Util.isElement(node, XDConstants.XDEF31_NS_URI, XdNames.DEF)
			|| Util.isElement(node, XDConstants.XDEF32_NS_URI, XdNames.DEF)
			|| Util.isElement(node, XDConstants.XDEF40_NS_URI, XdNames.DEF);
	}

	/** Returns <tt>true</tt> if given node is a valid X-definition <tt>def</tt>
	 * element child.
	 * @param node node to test.
	 * @return <tt>true</tt> if given node is a valid X-definition <tt>def</tt>
	 * element child.
	 */
	public static boolean isModel(final Node node) {
		return Util.isChild(node, XDConstants.XDEF20_NS_URI, XdNames.DEF)
			|| Util.isChild(node, XDConstants.XDEF31_NS_URI, XdNames.DEF)
			|| Util.isChild(node, XDConstants.XDEF32_NS_URI, XdNames.DEF)
			|| Util.isChild(node, XDConstants.XDEF40_NS_URI, XdNames.DEF);
	}

	/** Returns <tt>true</tt> if given node is a valid X-definition
	 * <tt>mixed</tt> element.
	 * @param node node to test.
	 * @return <tt>true</tt> if given node is valid X-definition <tt>mixed</tt>
	 * element.
	 */
	public static boolean isMixed(final Node node) {
		return Util.isElement(node, XDConstants.XDEF20_NS_URI, XdNames.MIXED)
			|| Util.isElement(node, XDConstants.XDEF31_NS_URI, XdNames.MIXED)
			|| Util.isElement(node, XDConstants.XDEF32_NS_URI, XdNames.MIXED)
			|| Util.isElement(node, XDConstants.XDEF40_NS_URI,XdNames.MIXED);
	}

	/** Returns <tt>true</tt> if given node is a valid X-definition
	 * <tt>choice</tt> element.
	 * @param node node to test.
	 * @return <tt>true</tt> if given node is valid X-definition <tt>choice</tt>
	 * element.
	 */
	public static boolean isChoice(final Node node) {
		return Util.isElement(node, XDConstants.XDEF20_NS_URI, XdNames.CHOICE)
			|| Util.isElement(node, XDConstants.XDEF31_NS_URI, XdNames.CHOICE)
			|| Util.isElement(node, XDConstants.XDEF32_NS_URI,XdNames.CHOICE)
			|| Util.isElement(node,XDConstants.XDEF40_NS_URI,XdNames.CHOICE);
	}

	/** Returns <tt>true</tt> if given node is a valid X-definition
	 * <tt>sequence</tt> element.
	 * @param node node to test.
	 * @return <tt>true</tt> if given node is a valid X-definition
	 * <tt>sequence</tt> element.
	 */
	public static boolean isSequence(final Node node) {
		return Util.isElement(node,XDConstants.XDEF20_NS_URI, XdNames.SEQUENCE)
			|| Util.isElement(node,XDConstants.XDEF31_NS_URI, XdNames.SEQUENCE)
			|| Util.isElement(node,XDConstants.XDEF32_NS_URI, XdNames.SEQUENCE)
			|| Util.isElement(node,XDConstants.XDEF40_NS_URI, XdNames.SEQUENCE);
	}

	/** Returns <tt>true</tt> if given node is a valid X-definition
	 * <tt>declaration</tt> element.
	 * @param node node to test.
	 * @return <tt>true</tt> if given node is a valid X-definition
	 * <tt>declaration</tt> element.
	 */
	public static boolean isDeclaration(final Node node) {
		return Util.isElement(node,
			XDConstants.XDEF20_NS_URI, XdNames.DECLARATION)
			|| Util.isElement(node,
				XDConstants.XDEF31_NS_URI, XdNames.DECLARATION)
			|| Util.isElement(node,
				XDConstants.XDEF32_NS_URI, XdNames.DECLARATION)
			|| Util.isElement(node,
				XDConstants.XDEF40_NS_URI, XdNames.DECLARATION);
	}

	/** Returns <tt>true</tt> if given node is a valid X-macro
	 * <tt>macro</tt> element.
	 * @param node node to test.
	 * @return <tt>true</tt> if given node is a valid X-macro
	 * <tt>macro</tt> element.
	 */
	public static boolean isMacro(final Node node) {
		return Util.isElement(node,
			XDConstants.XDEF20_NS_URI, XdNames.MACRO)
			|| Util.isElement(node,
				XDConstants.XDEF31_NS_URI, XdNames.MACRO)
			|| Util.isElement(node,
				XDConstants.XDEF32_NS_URI, XdNames.MACRO)
			|| Util.isElement(node,
				XDConstants.XDEF40_NS_URI, XdNames.MACRO);
	}

	/** Returns <tt>true</tt> if given model is declared as root model.
	 * @param element model element.
	 * @return <tt>true</tt> if given model is declared as root model.
	 */
	public static boolean isRoot(Element element) {
		if (!isModel(element)) {
			return false;
		}
		Element defElem = (Element) element.getParentNode();
		String  roots   = null;
		try {
			roots = Util.getAttrValue(
				defElem, defElem.getNamespaceURI(), XdNames.ROOT);
		} catch (IllegalArgumentException ex) {/*empty - leaves roots==null*/ }

		if (roots == null || roots.length() == 0) {
			return false;
		}

		String elemNsURI = element.getNamespaceURI();
		String elemName  = element.getLocalName();

		StringTokenizer st = new StringTokenizer(roots, "|");
		while (st.hasMoreTokens()) {
			String root = st.nextToken();
			String trimmedRoot = root.trim();
			MyQName qName = MyQName.parseQName(trimmedRoot);
			if (!qName.getName().equals(elemName)) {
				continue;
			}
			if (qName.getPrefix() != null) {
				String nsURI = KXmlUtils.getNSURI(qName.getPrefix(), element);
				if (nsURI.equals(elemNsURI)) {
					return true;
				}
			} else {
/*VT*/
				String nsURI = KXmlUtils.getNSURI("", element);
				if (nsURI == null || nsURI.length() == 0) {
					if (elemNsURI == null) {
						return true;
					}
				} else if (nsURI.equals(elemNsURI)) {
					return true;
				}
/*VT*/
			}
		}
		return false;
	}

	/** Returns name of given X-definition element as declared in <tt>name</tt>
	 * attribute.
	 * @param element element.
	 * @return name of given X-definition element as declared in <tt>name</tt>
	 * attribute.
	 */
	public static String getGroupName(final Element element) {
		String result = Util.getAttrValue(element,
			XDConstants.XDEF20_NS_URI, XdNames.NAME);
		if (result != null) {
			return result;
		}
		result = Util.getAttrValue(element,
			XDConstants.XDEF31_NS_URI, XdNames.NAME);
		if (result != null) {
			return result;
		}
		result = Util.getAttrValue(element,
			XDConstants.XDEF32_NS_URI, XdNames.NAME);
		return (result != null) ? result :
			Util.getAttrValue(element, XDConstants.XDEF40_NS_URI, XdNames.NAME);
	}

	/** Gets instance of element properties of given element.
	 * @param element element to get properties of.
	 * @return instance of element properties of given element.
	 * @throws NullPointerException if given element is <tt>null</tt>.
	 */
	public static ElemProps getElemProps(final Element element) {
		if (element == null) {
			throw new NullPointerException("Given element is null");
		}
		String fixedValue = null;
		String defaultValue = null;
		int minOccurs = 1;
		int maxOccurs = 1;
		boolean nillable = false;
		String ref = null;
		XDParsedScript script = XDParsedScript.getXdScript(element);
		if (script == null) {
			return new ElemProps(
				defaultValue, fixedValue, minOccurs, maxOccurs, nillable, ref);
		}
		//getting fixed and default
		NodeList textValues = KXmlUtils.getChildElementsNS(element,
			XDConstants.XDEF20_NS_URI, XdNames.TEXT);
		if (textValues.getLength() == 0) {
			textValues = KXmlUtils.getChildElementsNS(element,
			XDConstants.XDEF31_NS_URI, XdNames.TEXT);
		}
		if (textValues.getLength() == 0) {
			textValues = KXmlUtils.getChildElementsNS(element,
			XDConstants.XDEF32_NS_URI, XdNames.TEXT);
		}
		if (textValues.getLength() == 0) {
			textValues = KXmlUtils.getChildElementsNS(element,
			XDConstants.XDEF40_NS_URI, XdNames.TEXT);
		}
		//one text value specified
		if (textValues.getLength() == 1) {
			String text = KXmlUtils.getTextContent(textValues.item(0));
			//parse script
			XDParsedScript textScript = XDParsedScript.getXdScript(text, null,
					true);
			//script exists
			if (textScript != null) {
				//occurrence set to fixed
				if (textScript.getxOccurrence().isFixed()) {
					fixedValue = textScript._type;
				} else {
					defaultValue = textScript._default;
				}
			}
		}
		minOccurs = script.getxOccurrence().minOccurs();
		maxOccurs = script.getxOccurrence().maxOccurs() == Integer.MAX_VALUE
				? Occurrence.UNBOUNDED : script.getxOccurrence().maxOccurs();
		nillable = script.hasOption("nillable");
		ref = script._reference;
		return new ElemProps(
			defaultValue, fixedValue, minOccurs, maxOccurs, nillable, ref);
	}

	/** Gets instance of attribute properties of given attribute node.
	 * @param attr attribute node to get properties from.
	 * @return instance of attribute properties.
	 * @throws NullPointerException if given attribute node is <tt>null</tt>.
	 */
	public static AttrProps getAttrProps(final Attr attr) {
		if (attr == null) {
			throw new NullPointerException("Given attribute node is null");
		}
		String fixedValue = null;
		String defaultValue = null;
		String use = AttrProps.REQUIRED;
		String type = null;
		XDParsedScript script = XDParsedScript.getXdScript(attr);
		if (script == null) {
			return new AttrProps(defaultValue, fixedValue, use, type);
		}
		if (script.getxOccurrence().isFixed() ) {
//			fixedValue = script._type;
/*VT*/
			fixedValue = script._default;
/*VT*/
		} else {
			defaultValue = script._default;
		}
		if (script.getxOccurrence().isRequired()) {
			use = AttrProps.REQUIRED;
		} else if (script.getxOccurrence().isOptional()) {
			use = AttrProps.OPTIONAL;
		} else if (script.getxOccurrence().isIllegal()) {
			use = AttrProps.PROHIBITED;
		}
		if (script._type != null && script._type.length() != 0) {
			type = script._type;
		}
		return new AttrProps(defaultValue, fixedValue, use, type);
	}

	/** Gets occurrence given of X-definition node.
	 * @param node node to get occurrence from.
	 * @return instance of node occurrence.
	 * @throws NullPointerException if given node is <tt>null</tt>.
	 */
	public static Occurrence getOccurrence(final Node node) {
		if (node == null) {
			throw new NullPointerException("Given node is null");
		}
		int min = 1;
		int max = 1;
		XDParsedScript script = XDParsedScript.getXdScript(node);
		if (script == null) {
			return new Occurrence(min, max);
		}
		min = script.getxOccurrence().minOccurs();
		max = script.getxOccurrence().isMaxUnlimited() ? Occurrence.UNBOUNDED
				: script.getxOccurrence().maxOccurs();
		return new Occurrence(min, max);
	}

	//--------------------------------------------------------------------------
	//                          DOMAIN UTILS
	//--------------------------------------------------------------------------
	/** Creates X-definition representation from given X-definition <tt>def</tt>
	 * element.
	 * @param def X-definition <tt>def</tt> element.
	 * @return X-definition representation.
	 * @throws IllegalArgumentException if given element is not a valid
	 * X-definition <tt>def</tt> element.
	 */
	public static XdDef getXdDef(final Element def) {
		if (!isDef(def)) {
			throw new IllegalArgumentException("Given element is not a valid "
				+ "X-definition def element");
		}
		String defName = null;
		try {
			defName = Util.getAttrValue(def,def.getNamespaceURI(),XdNames.NAME);
		} catch (Exception ex) {
			try { // noname X-definition, we use the root name
				defName =
					Util.getAttrValue(def,def.getNamespaceURI(),XdNames.ROOT);
			} catch (Exception exx) {
				throw new RuntimeException(
					"X-definition must have the attribute \"xd:root\"");
			}
		}
		return new XdDef(defName);
	}

	/** Returns set of models name space URIs contained in given X-definition
	 * <tt>def</tt> element.
	 * @param def X-definition <tt>def</tt> element.
	 * @return set of (String) models name space URIs.
	 */
	public static Set<String> getModelsNS(final Element def) {
		Set<String> namespaces = new HashSet<String>();
		NodeList models = KXmlUtils.getChildElements(def);
		String nsUri = def.getNamespaceURI();
		for (int i = 0; i < models.getLength(); i++) {
			Element model = (Element) models.item(i);
			String modelNS = model.getNamespaceURI();
			if (!nsUri.equals(modelNS)) {
				namespaces.add(modelNS == null ? "" : modelNS);
			}
		}
		return namespaces;
	}

	/** Return proper implementation of X-definition model from model element.
	 * @param model model element.
	 * @return implementation of X-definition model.
	 * @throws IllegalArgumentException if given element is not
	 * a valid X-definition model.
	 */
	public static XdModel createXdModel(final Element model) {
		if (!isModel(model)) {
			throw new IllegalArgumentException(
				"Given element is not a valid X-definition model");
		}
		Element e = (Element) model.getParentNode();
		XdDef xdDef = getXdDef(e);
		String xdURI = e.getNamespaceURI();
		if (xdURI.equals(model.getNamespaceURI())) {
			if (isChoice(model)) {
				String name = getGroupName(model);
				return new XdGroup(xdDef, name, XdGroup.GroupType.CHOICE);
			} else if (isMixed(model)) {
				String name = getGroupName(model);
				return new XdGroup(xdDef, name, XdGroup.GroupType.MIXED);
			} else if (isSequence(model)) {
				String name = getGroupName(model);
				return new XdGroup(xdDef, name, XdGroup.GroupType.SEQUENCE);
			} else if (isMacro(model)) {
				return null;
/*VT*/
//			} else if (isDeclaration(model)) {
//				String name = getDeclName(model);
//				return new XdDecl(xdDef, name);
/*VT*/
			}
		} else {
			return new XdElem(xdDef,
				model.getNamespaceURI(), model.getLocalName());
		}
		throw new IllegalArgumentException(
			"Given element is not a valid X-definition model");
	}

	/** Returns type constant of given X-definition element declaration.
	 * @param element X-definition element declaration.
	 * @return type constant of given X-definition element declaration.
	 * @throws NullPointerException if given element declaration is
	 * <tt>null</tt>.
	 */
	public static int getElemType(Element element) {
		if (element == null) {
			throw new NullPointerException("Given element declaration is null");
		}
		boolean text = hasText(element);
		boolean attr = hasAttributes(element);
		boolean chld = hasChildren(element);
		//text declaraed
		if (text) {
			//attributes declared
			if (attr) {
				//children declared
				if (chld) {
					return XdElem.ElemType.TEXT_ATTR_CHLD;
				} else {
					return XdElem.ElemType.TEXT_ATTR;
				}
			} else {
				if (chld) {
					return XdElem.ElemType.TEXT_CHLD;
				} else {
					return XdElem.ElemType.TEXT;
				}
			}
		} else {
			//attributes declared
			if (attr) {
				//children declared
				if (chld) {
					return XdElem.ElemType.ATTR_CHLD;
				} else {
					return XdElem.ElemType.ATTR;
				}
			} else {
				//children declared
				if (chld) {
					return XdElem.ElemType.CHLD;
				} else {
					return XdElem.ElemType.EMPTY;
				}
			}
		}
	}

	/** Returns <tt>true</tt> if given X-definition element declaration contains
	 * child text nodes declaration.
	 * @param element element to test.
	 * @return <tt>true</tt> if given X-definition element declaration contains
	 * child text nodes declaration.
	 */
	private static boolean hasText(Element element) {
		//TODO: resolve text existence
		//attribute xd:text is present
		if (Util.hasAttrDecl(element, XDConstants.XDEF20_NS_URI, XdNames.TEXT)
			|| Util.hasAttrDecl(element, XDConstants.XDEF31_NS_URI,XdNames.TEXT)
			|| Util.hasAttrDecl(element, XDConstants.XDEF32_NS_URI,XdNames.TEXT)
			|| Util.hasAttrDecl(element, XDConstants.XDEF32_NS_URI,
				XdNames.TEXT)) {
			return true;
		}
		//child element xd:text is present
		return KXmlUtils.getChildElementsNS(element,
			XDConstants.XDEF20_NS_URI, XdNames.TEXT).getLength() > 0
			||  KXmlUtils.getChildElementsNS(element,
				XDConstants.XDEF31_NS_URI, XdNames.TEXT).getLength() > 0
			||  KXmlUtils.getChildElementsNS(element,
				XDConstants.XDEF32_NS_URI, XdNames.TEXT).getLength() > 0
			||  KXmlUtils.getChildElementsNS(element,
				XDConstants.XDEF40_NS_URI, XdNames.TEXT).getLength() > 0;
	}

	/** Returns <tt>true</tt> if given X-definition element declaration contains
	 * attribute or <tt>attr</tt> node declaration.
	 * @param element X-definition element declaration.
	 * @return <tt>true</tt> if given X-definition element declaration contains
	 * attribute or <tt>attr</tt> node declaration.
	 */
	private static boolean hasAttributes(Element element) {
		NamedNodeMap attrs = element.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			Attr attr = (Attr) attrs.item(i);
			String attrNS = attr.getNamespaceURI();
			if (!XDConstants.XDEF20_NS_URI.equals(attrNS)
				&& !XDConstants.XDEF31_NS_URI.equals(attrNS)
				&& !XDConstants.XDEF32_NS_URI.equals(attrNS)
				&& !XDConstants.XDEF40_NS_URI.equals(attrNS)) {
				return true;
			} else {
				if (XdNames.ATTR.equals(Util.getAttrLocalName(attr))) {
					return true;
				}
			}
		}
		return false;
	}

	/** Returns <tt>true</tt> if given X-definition element declaration contains
	 * children element, <tt>any</tt>, <tt>choice</tt>, <tt>mixed</tt>
	 *  or <tt>sequence</tt> declaration.
	 * @param element X-definition element declaration.
	 * @return <tt>true</tt> if given X-definition element declaration contains
	 * children element, <tt>any</tt>, <tt>choice</tt>, <tt>mixed</tt>
	 *  or <tt>sequence</tt> declaration.
	 */
	private static boolean hasChildren(Element element) {
		NodeList children = KXmlUtils.getChildElements(element);
		for (int i = 0; i < children.getLength(); i++) {
			Element child = (Element) children.item(i);
			if (!XDConstants.XDEF20_NS_URI.equals(child.getNamespaceURI())
				&& !XDConstants.XDEF31_NS_URI.equals(child.getNamespaceURI())
				&& !XDConstants.XDEF32_NS_URI.equals(child.getNamespaceURI())
				&& !XDConstants.XDEF40_NS_URI.equals(child.getNamespaceURI())) {
				return true;
			} else {
				String elemName = child.getLocalName();
				if (XdNames.ANY.equals(elemName)
					|| XdNames.CHOICE.equals(elemName)
					|| XdNames.MIXED.equals(elemName)
					|| XdNames.SEQUENCE.equals(elemName)) {
					return true;
				}
			}
		}
		return false;
	}

	/** Returns X-definition reference string from given element
	 * or <tt>null</tt>if given element does not contain reference.
	 * @param element element to search for reference.
	 * @return reference string from given element or <tt>null</tt>
	 * if given element does not contain reference.
	 * @throws NullPointerException if given element is <tt>null</tt>.
	 */
	public static String getRef(Element element) {
		if (element == null) {
			throw new NullPointerException("Given element is null");
		}
		XDParsedScript script = XDParsedScript.getXdScript(element);
		if (script != null) {
			return script._reference;
		}
		return null;
	}

	/** Creates X-definition element model representation of referenced element
	 * from given element node or <tt>null</tt> if given element does not
	 * contain reference.
	 * @param element element to get referenced element representation from.
	 * @return X-definition element model representation or <tt>null</tt>.
	 * @throws NullPointerException if given element is <tt>null</tt>.
	 */
	public static XdElem getRefXdElem(Element element) {
		if (element == null) {
			throw new NullPointerException("Given element is null");
		}
		String ref = getRef(element);
		if (ref != null && ref.length() != 0) {
			XdDef xdDef;
			int ndx;
			if ((ndx = ref.indexOf('#')) != -1) {
				String defName = ref.substring(0, ndx);
				xdDef = new XdDef(defName);
				ref = ref.substring(ndx + 1);
			} else {
				Element defElem = getAncestorDef(element);
				xdDef = getXdDef(defElem);
			}
			String namespace;
			if ((ndx = ref.indexOf(":")) != -1) {
				String prefix = ref.substring(0, ndx);
				namespace = KXmlUtils.getNSURI(prefix, element);
				ref = ref.substring(ndx + 1);
			} else {
				namespace = KXmlUtils.getNSURI("", element);
			}
			return new XdElem(xdDef, namespace, ref);
		}
		return null;
	}

	/** Gets element type constant from union of given types.
	 * @param elemType1 first element type.
	 * @param elemType2 second element type.
	 * @return element type constant from union of given types.
	 * @throws IllegalArgumentException if any of given type is unknown constant
	 */
	public static int getElemTypeUnion(int elemType1, int elemType2) {
		switch (elemType1) {
			case XdElem.ElemType.EMPTY:
				return elemType2;
			case XdElem.ElemType.ATTR:
				switch (elemType2) {
					case XdElem.ElemType.EMPTY:
					case XdElem.ElemType.ATTR:
						return XdElem.ElemType.ATTR;
					case XdElem.ElemType.ATTR_CHLD:
					case XdElem.ElemType.CHLD:
						return XdElem.ElemType.ATTR_CHLD;
					case XdElem.ElemType.TEXT_ATTR:
					case XdElem.ElemType.TEXT:
						return XdElem.ElemType.TEXT_ATTR;
					case XdElem.ElemType.TEXT_CHLD:
					case XdElem.ElemType.TEXT_ATTR_CHLD:
						return XdElem.ElemType.TEXT_ATTR_CHLD;
				}
				break;
			case XdElem.ElemType.TEXT:
				switch (elemType2) {
					case XdElem.ElemType.EMPTY:
					case XdElem.ElemType.TEXT:
						return XdElem.ElemType.TEXT;
					case XdElem.ElemType.ATTR:
					case XdElem.ElemType.TEXT_ATTR:
						return XdElem.ElemType.TEXT_ATTR;
					case XdElem.ElemType.CHLD:
					case XdElem.ElemType.TEXT_CHLD:
						return XdElem.ElemType.TEXT_CHLD;
					case XdElem.ElemType.ATTR_CHLD:
					case XdElem.ElemType.TEXT_ATTR_CHLD:
						return XdElem.ElemType.TEXT_ATTR_CHLD;
				}
				break;
			case XdElem.ElemType.CHLD:
				switch (elemType2) {
					case XdElem.ElemType.EMPTY:
					case XdElem.ElemType.CHLD:
						return XdElem.ElemType.CHLD;
					case XdElem.ElemType.ATTR:
					case XdElem.ElemType.ATTR_CHLD:
						return XdElem.ElemType.ATTR_CHLD;
					case XdElem.ElemType.TEXT:
					case XdElem.ElemType.TEXT_CHLD:
						return XdElem.ElemType.TEXT_CHLD;
					case XdElem.ElemType.TEXT_ATTR:
					case XdElem.ElemType.TEXT_ATTR_CHLD:
						return XdElem.ElemType.TEXT_ATTR_CHLD;
				}
				break;
			case XdElem.ElemType.TEXT_ATTR:
				switch (elemType2) {
					case XdElem.ElemType.EMPTY:
					case XdElem.ElemType.TEXT:
					case XdElem.ElemType.ATTR:
					case XdElem.ElemType.TEXT_ATTR:
						return XdElem.ElemType.TEXT_ATTR;
					case XdElem.ElemType.CHLD:
					case XdElem.ElemType.ATTR_CHLD:
					case XdElem.ElemType.TEXT_CHLD:
					case XdElem.ElemType.TEXT_ATTR_CHLD:
						return XdElem.ElemType.TEXT_ATTR_CHLD;
				}
				break;
			case XdElem.ElemType.TEXT_CHLD:
				switch (elemType2) {
					case XdElem.ElemType.EMPTY:
					case XdElem.ElemType.TEXT:
					case XdElem.ElemType.CHLD:
					case XdElem.ElemType.TEXT_CHLD:
						return XdElem.ElemType.TEXT_CHLD;
					case XdElem.ElemType.ATTR:
					case XdElem.ElemType.TEXT_ATTR:
					case XdElem.ElemType.ATTR_CHLD:
					case XdElem.ElemType.TEXT_ATTR_CHLD:
						return XdElem.ElemType.TEXT_ATTR_CHLD;
				}
				break;
			case XdElem.ElemType.ATTR_CHLD:
				switch (elemType2) {
					case XdElem.ElemType.EMPTY:
					case XdElem.ElemType.ATTR:
					case XdElem.ElemType.CHLD:
					case XdElem.ElemType.ATTR_CHLD:
						return XdElem.ElemType.ATTR_CHLD;
					case XdElem.ElemType.TEXT:
					case XdElem.ElemType.TEXT_ATTR:
					case XdElem.ElemType.TEXT_CHLD:
					case XdElem.ElemType.TEXT_ATTR_CHLD:
						return XdElem.ElemType.TEXT_ATTR_CHLD;
				}
				break;
			case XdElem.ElemType.TEXT_ATTR_CHLD:
				return XdElem.ElemType.TEXT_ATTR_CHLD;
		}
		throw new IllegalArgumentException("Unknown type constant");
	}

	/** Gets ancestor <tt>def</tt> element of given node.
	 * @param node node to get ancestor <tt>def</tt> element from.
	 * @return ancestor <tt>def</tt> element.
	 * @throws NullPointerException if given node is <tt>null</tt>.
	 * @throws IllegalArgumentException if given node is not valid X-definition
	 * <tt>def</tt> element descendant.
	 */
	public static Element getAncestorDef(Node node) {
		if (node == null) {
			throw new NullPointerException("Given node is null!");
		}
		if (isDef(node)) {
			return (Element) node;
		}
		if (Node.ATTRIBUTE_NODE == node.getNodeType()) {
			Attr attr = (Attr) node;
			Element parent = attr.getOwnerElement();
			return getAncestorDef(parent);
		} else {
			Node parent = node.getParentNode();
			if (Node.DOCUMENT_NODE != parent.getNodeType()) {
				return getAncestorDef(parent);
			}
		}
		throw new IllegalArgumentException("Given node is not a valid def "
				+ "descendant node");
	}

	/** Represents node occurrence. */
	public static final class Occurrence {

		/** Constant for unbounded maximal occurrence. */
		public static final int UNBOUNDED = -1;
		/** Minimal node occurrence. */
		private final int _minOccurs;
		/** Maximal node occurrence. */
		private final int _maxOccurs;

		/** Creates instance of node occurrence.
		 * @param minOccurs minimal occurrence.
		 * @param maxOccurs maximal occurrence.
		 */
		public Occurrence(int minOccurs, int maxOccurs) {
			_minOccurs = minOccurs;
			_maxOccurs = maxOccurs;
		}

		/** Minimal occurrence getter.
		 * @return minimal occurrence as string.
		 */
		public int getMinOccurs() {return _minOccurs;}

		/** Maximal occurrence getter.
		 * @return maximal occurrence as string.
		 */
		public int getMaxOccurs() {return _maxOccurs;}
	}

	/** Represents X-definition attribute properties. */
	public final static class AttrProps {

		/** Attribute <code>required</code> occurrence. */
		public static final String REQUIRED = "required";
		/** Attribute <code>optional</code> occurrence. */
		public static final String OPTIONAL = "optional";
		/** Attribute <code>prohibited</code> occurrence. */
		public static final String PROHIBITED = "prohibited";
		/** Attribute default value. */
		private final String _default;
		/** Attribute fixed value. */
		private final String _fixed;
		/** Attribute use. */
		private final String _use;
		/** Attribute type declaration. */
		private final String _type;

		/** Creates instance of X-definition attribute properties.
		 * @param defaultValue attribute default value.
		 * @param fixedValue attribute fixed value.
		 * @param use attribute use.
		 * @param type attribute type declaration.
		 * @throws IllegalArgumentException if given attribute use is unknown.
		 */
		public AttrProps(String defaultValue,
			String fixedValue,
			String use,
			String type) {
			if (!REQUIRED.equals(use) && !OPTIONAL.equals(use)
					&& !PROHIBITED.equals(use)) {
				throw new IllegalArgumentException("Unknown attribute use");
			}
			_default = defaultValue;
			_fixed = fixedValue;
			_use = use;
			_type = type;
		}

		/** Attribute default value getter.
		 * @return attribute default value or <tt>null</tt>.
		 */
		public String getDefault() {return _default;}

		/** Attribute fixed value getter.
		 * @return attribute fixed value or <tt>null</tt>.
		 */
		public String getFixed() {return _fixed;}

		/** Attribute use getter.
		 * @return atttribute use.
		 */
		public String getUse() {return _use;}

		/** Attribute type declaration getter.
		 * @return attribute type declaration string.
		 */
		public String getType() {return _type;}
	}

	/** Represents X-definition element properties.*/
	public final static class ElemProps {

		/** Element default value. */
		private final String _default;
		/** Element fixed value. */
		private final String _fixed;
		/** Element minimal occurrence. */
		private final int _minOccurs;
		/** Element maximal occurrence. */
		private final int _maxOccurs;
		/** Element nillability switch. */
		private final boolean _nillable;
		/** Reference to other element. */
		private final String _ref;

		/** Creates instance of element properties.
		 * @param defaultValue element default value.
		 * @param fixedValue element fixed value.
		 * @param minOccurs element minimal occurrence.
		 * @param maxOccurs element maximal occurrence.
		 * @param nillable element nillability switch.
		 * @param ref reference string.
		 */
		public ElemProps(String defaultValue,
			String fixedValue,
			final int minOccurs,
			final int maxOccurs,
			final boolean nillable,
			final String ref) {
			_default = defaultValue;
			_fixed = fixedValue;
			_minOccurs = minOccurs;
			_maxOccurs = maxOccurs;
			_nillable = nillable;
			_ref = ref;
		}

		/** Element default value getter.
		 * @return element dfault value.
		 */
		public String getDefault() {return _default;}

		/** Element fixed value getter.
		 * @return element fixed value.
		 */
		public String getFixed() {return _fixed;}

		/** Element maximal occurrence getter.
		 * @return element maximal occurrence.
		 */
		public int getMaxOccurs() {return _maxOccurs;}

		/** Element minimal occurrence getter.
		 * @return element minimal occurrence.
		 */
		public int getMinOccurs() {return _minOccurs;}

		/** Element nillability switch getter.
		 * @return element nillability switch.
		 */
		public boolean isNillable() {return _nillable;}

		/** Element reference getter.
		 * @return element reference.
		 */
		public String getRef() {return _ref;}
	}
}