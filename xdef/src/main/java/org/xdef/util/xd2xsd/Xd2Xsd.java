package org.xdef.util.xd2xsd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.impl.XData;
import org.xdef.impl.XVariable;
import org.xdef.impl.compile.CompileVariable;
import org.xdef.impl.parsers.XSParseList;
import org.xdef.impl.parsers.XSParseUnion;
import org.xdef.model.XMData;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import static org.xdef.model.XMNode.XMCHOICE;
import static org.xdef.model.XMNode.XMELEMENT;
import static org.xdef.model.XMNode.XMSELECTOR_END;
import static org.xdef.model.XMNode.XMTEXT;
import org.xdef.model.XMOccurrence;
import org.xdef.model.XMSelector;
import org.xdef.model.XMVariable;
import org.xdef.msg.XDCONV;
import org.xdef.sys.Report;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.xml.KXmlUtils;

/** Convertor of Xdefinition to XML Schema.
 * @author Vaclav Trojan, Anna Kchmascheva
 */
public class Xd2Xsd {
	/** Prefix used for the XML schema namespace. */
	private static final String SCHEMA_PFX = "xs:";
	/** Target namespace of XML schema with declared user types. */
	private static final String USERTYPES_URI = "$_types_$";
	/** QName of schema element. */
	private static final QName SCHEMA_QNAME = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "schema");
	/** Prefix used for the type in XML schema. */
	private static final String TYPE = "_Type";
	/** Map of file names and XML schema elements.*/
	private final Map<String, Element> _xsdSources = new HashMap<>();
	/** org.w3c.dom.Document used for creation of nodes. */
	private final Document _doc = KXmlUtils.newDocument();
	/** Switch if generate annotation with documentation information. */
	private final boolean _genInfo;
	/** If true use mask to validate XML data parameter describing output format from "xdatetime" method.*/
	private final boolean _genXdateOutFormat;
	/** Name of root XML schema file. */
	private final String _rootName;
	/** Name of XML schema file with user declared types. */
	private final String _typesName;
	/** XML schema element with of user declared types.*/
	private final Element _types;

	/** Create new instance of XsdGenerator.
	 * @param xp XDPool with Xdefinitions.
	 * @param outName name base of XML schema names.
	 * @param outType name of XML schema file with type declarations.
	 * @param genInfo if true the annotations with documentation is generated.
	 * @param genXdateOutFormat if true, use as mask to validate XML data the parameter describing output
	 * format from the "xdatetime" method.
	 */
	private Xd2Xsd(final XDPool xp,
		final String outName,
		final String outType,
		final boolean genInfo,
		final boolean genXdateOutFormat) {
		_genInfo = genInfo;
		_genXdateOutFormat = genXdateOutFormat;
		Element types;
		if (null != outType && !outType.isEmpty()
			&& (types = genDeclaredTypes(xp)).getChildNodes().getLength() > 0) {
			// generate declared types to the separate file
			addSchema(_typesName = _rootName = outType, _types = types);
		} else {
			_typesName = null;
			_types = null;
			_rootName = outName != null ? outName : outType;
		}
	}

	/** Prepare XML schema element with declared types for XML schema file.
	 * @param xp compiled pool with Xdefinitions.
	 * @return Element with XML schema with declared types or null.
	 */
	private Element genDeclaredTypes(final XDPool xp) {
		XMVariable[] vars = xp.getVariableTable().toArray();
		Element types = genNewSchema();
		types.setAttribute("targetNamespace", USERTYPES_URI);
		types.setAttribute("xmlns", USERTYPES_URI);
		for (XMVariable v : vars) {
			if (v.getName().charAt(0) != '$') {
				XData xdata = new XData("$text", null, xp, XMTEXT);
				xdata._check = ((XVariable)v).getParseMethodAddr();
				GenParser parserInfo = GenParser.genParser( (XMData) xdata, _genXdateOutFormat);
				String typeName = genDeclaredName(parserInfo);
				String vName =  v.getName().replace('#', '_');
				if (typeName != null && !typeName.isEmpty()
					&& !vName.equals(typeName)) {
					if (findSchematype(types, v.getName()) == null) {
						Element simpleType = genSchemaElem(types, "simpleType");
						simpleType.setAttribute("name", vName);
						Element restr = genRestrictionElement(simpleType);
						restr.setAttribute("base", typeName);
					}
				}
				if (typeName != null && !typeName.isEmpty()) {
					if (findSchematype(types, typeName) != null) {
						continue;
					}
					Element simpleType = genSchemaElem(types, "simpleType");
					simpleType.setAttribute("name", typeName);
					if (parserInfo.getParser().getNamedParams().isEmpty()) {
						addDocumentation(simpleType, parserInfo.getInfo());
						Element restr = genRestrictionElement(simpleType);
						String parserName =
							SCHEMA_PFX + parserInfo.getParser().parserName();
						restr.setAttribute("base", parserName);
					} else {
						genRestrictions(simpleType, parserInfo);
					}
				}
			}
		}
		return types.getChildNodes().getLength() > 0 ? types : null;
	}

	/** If genInfo switch is on, then Generate annotations with documentation.
	 * @param parent node where to add annotation.
	 * @param text of documentation.
	 */
	private void addDocumentation(final Element parent, final String text) {
		if (_genInfo && text != null && !text.trim().isEmpty()) {
			NodeList nl = KXmlUtils.getChildElementsNS(parent,
				XMLConstants.W3C_XML_SCHEMA_NS_URI, "annotation");
			Element anotation =  (nl != null && nl.getLength() > 0)
				? (Element) nl.item(0) : genSchemaElem(parent, "annotation");
			Element documentation = genSchemaElem(anotation, "documentation");
			documentation.appendChild(_doc.createTextNode(text.trim()));
		}
	}

	/** Get name of XML schema type.
	 * @param parserInfo parser information.
	 * @return schema type name.
	 */
	private static String getSchemaTypeName(final GenParser parserInfo) {
		return SCHEMA_PFX + parserInfo.getParser().parserName();
	}

	/** Create element with restrictions,
	 * @param parent element where to add restrictions
	 * @param parserInfo object containing restriction information.
	 * @return element with restrictions.
	 */
	private Element genRestrictions(final Element parent, final GenParser parserInfo) {
		addDocumentation(parent, parserInfo.getInfo());
		String typeName = getSchemaTypeName(parserInfo);
		switch(typeName) {
			case "xs:union": {
				XDParser[] parsers = ((XSParseUnion) parserInfo.getParser()).getParsers();
				Element union = genSchemaElem(parent, "union");
				for (XDValue xval: parsers) {
					XDParser p = (XDParser) xval;
					Element simpletp = genSchemaElem(union, "simpleType");
					genRestrictions(simpletp, SCHEMA_PFX+p.parserName(),p.getNamedParams().getXDNamedItems());
				}
				return union;
			}
			case "xs:list": {
				Element list = genSchemaElem(parent, "list");
				XDParser p = ((XSParseList) parserInfo.getParser()).getItemParser();
				Element simpletp = genSchemaElem(list, "simpleType");
				genRestrictions(simpletp, SCHEMA_PFX+p.parserName(), p.getNamedParams().getXDNamedItems());
				return list;
			}
		}
		XDNamedValue[] xdv = parserInfo.getParser().getNamedParams().getXDNamedItems();
		return genRestrictions(parent, typeName, xdv);
	}

	/** Create element with restrictions,
	 * @param parent element where to add restrictions
	 * @param typeName name of type.
	 * @param namedParams named parameters.
	 * @return element with restrictions.
	 */
	private Element genRestrictions(final Element parent,
		final String typeName,
		final XDNamedValue[] namedParams) {
		Element restr = genSchemaElem(parent, "restriction");
		restr.setAttribute("base", typeName);
		for (XDNamedValue x: namedParams) {
			XDValue xval = x.getValue();
			if (xval==null) {
				continue;
			}
			String paramName = x.getName();
			Element param;
			switch(paramName) {
				case "pattern":
				case "enumeration":
					if (xval instanceof XDContainer) {
						XDContainer xdc = (XDContainer) xval;
						for (int i = 0; i < xdc.getXDItemsNumber(); i++) {
							param = genSchemaElem(restr, paramName);
							param.setAttribute("value", xdc.getXDItem(i).toString());
						}
					} else {
						param = genSchemaElem(restr, paramName);
						param.setAttribute("value", xval.toString());
					}
					continue;
				case "minLength":
				case "maxLength":
				case "length":
				case "minInclusive":
				case "minExclusive":
				case "maxInclusive":
				case "maxExclusive":
				case "totalDigits":
				case "fractionDigits":
					param = genSchemaElem(restr, paramName);
					param.setAttribute("value", xval.toString());
			}
		}
		return restr;
	}

	/** Create xd:sequence element as child of argument.
	 * @param el parent of created element.
	 * @return xd:sequence element as child of argument.
	 */
	private Element genSequenceElement(final Element el) {
		/* AK */
		Element element = el;
		NodeList children = element.getChildNodes();
		if (children.getLength() > 0 && children.item(0).getNodeName().endsWith("complexContent")) {
			children = children.item(0).getChildNodes();
			if (children.getLength() > 0) {
				Node firstNode = children.item(0);
				String firstNodeName = firstNode.getNodeName();
				if (firstNodeName.contains("extension")) {
					children = firstNode.getChildNodes();
					element = (Element) firstNode;
				}
			}
		}

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child instanceof Element && child.getNodeName().contains("sequence")) {
				return (Element) child;
			}
		}

		return XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(element.getNamespaceURI())
			&& "sequence".equals(element.getLocalName()) ? element : genSchemaElem(element, "sequence");
		/* AK */
	}

	/** Create xd:restriction element as child of argument.
	 * @param el parent of created element.
	 * @return xd:restriction element as child of argument.
	 */
	private Element genRestrictionElement(final Element el) {
		return XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(el.getNamespaceURI())
		 && "restriction".equals(el.getLocalName()) ? el : genSchemaElem(el, "restriction");
	}

	/** Generate group of models.
	 * @param parent element where to add models.
	 * @param xsel group selector.
	 * @param children array with children.
	 * @param index index of the first child.
	 * @return the index where to continue.
	 */
	private int genGroup(final Element complt,
		final Element parent,
		final XMSelector xsel,
		final XMNode[] children,
		final int index) {
		int endIndex = xsel.getEndIndex();
		Element sel;
		switch (xsel.getKind()) {
			case XMNode.XMSEQUENCE:
				sel = genSequenceElement(parent);
				setOccurrence(sel, xsel);
				return genSequence(complt, sel, children, index, endIndex);
			case XMNode.XMCHOICE:
				sel = genSchemaElem(parent, "choice");
				setOccurrence(sel, xsel);
				return genSequence(complt, sel, children, index, endIndex);
			case XMNode.XMMIXED: {
				int min = 0;
				int max = 0;
				for (int i = index + 1; i < endIndex; i++) {
					XMNode x = children[i];
					switch (x.getKind()) {
						case XMNode.XMMIXED:
						case XMNode.XMCHOICE:
						case XMNode.XMSEQUENCE:
						case XMNode.XMELEMENT:
							XMOccurrence occ = x.getOccurence();
							if (occ.minOccurs() > 0) {
								min++;
							}
							if (occ.maxOccurs() >= 1) {
								max = occ.maxOccurs() == Integer.MAX_VALUE || max==Integer.MAX_VALUE
									? Integer.MAX_VALUE : max + occ.maxOccurs();
							}
					}
				}
				sel = genSequenceElement(parent);
				sel = genSchemaElem(sel, "choice");
				sel.setAttribute("maxOccurs", max == Integer.MAX_VALUE ? "unbounded" : String.valueOf(max));
				sel.setAttribute("minOccurs", String.valueOf(min));
				return genSequence(complt, sel, children, index, endIndex);
			}
			default:
				throw new SRuntimeException(XDCONV.XDCONV205, xsel); //"&{0}" not implemented
		}
	}

	/** Generate sequence of models.
	 * @param complextype complexType element.
	 * @param parent node where to add models.
	 * @param children array with children.
	 * @param index index of the first child.
	 * @param endIndex index of the last child.
	 * @return the index where to continue.
	 */
	private int genSequence(final Element complextype,
		final Element parent,
		final XMNode[] children,
		final int index,
		final int endIndex) {
		int i = index + 1;
		for (; i < endIndex; i++) {
			XMNode x = children[i];
			switch (x.getKind()) {
				case XMNode.XMELEMENT:
					if (!x.getOccurence().isIllegal()) {
						genElem(parent, (XMElement) x);
					}
					continue;
				case XMNode.XMTEXT:
					if (!x.getOccurence().isIllegal()) {
						complextype.setAttribute("mixed", "true");
					}
					continue;
				case XMNode.XMMIXED:
				case XMNode.XMCHOICE:
				case XMNode.XMSEQUENCE: {
					if (!x.getOccurence().isIllegal()) {
						XMSelector xsel = (XMSelector) x;
						i = genGroup(complextype, parent, xsel, children, i);
					}
					continue;
				}
				case XMNode.XMSELECTOR_END:
					return i + 1; // sholdn't happen!
			}
		}
		return i;
	}

	/** Set occurrence restrictions to an element.
	 * @param el Element where to set restrictions.
	 * @param xel Xdefinition model of element.
	 */
	private static void setOccurrence(final Element el, final XMNode xel) {
		XMOccurrence occ = xel.getOccurence();
		int minOcc = occ.minOccurs();
		int maxOcc = occ.maxOccurs();
		if (minOcc != 1 || maxOcc != 1) {
			el.setAttribute("minOccurs", String.valueOf(minOcc));
			el.setAttribute("maxOccurs", Integer.MAX_VALUE==maxOcc ? "unbounded":String.valueOf(maxOcc));
		}
	}

	/** Get declared type name.
	 * @param parserInfo object with information about value type.
	 * @return declared type name or null;
	 */
	private static String genDeclaredName(final GenParser parserInfo) {
		String s = parserInfo.getDeclaredName();
		if (s != null) {
			String[] parts = s.split(";");
			if (parts.length>0&&!(s=parts[0].trim()).isEmpty()) {
				int ndx = s.indexOf('#');
				return ndx>=0 ? s.substring(0,ndx) + "_" + s.substring(ndx + 1) : s;
			}
		}
		return null;
	}

	/** Add attributes to schema element.
	 * @param el schema element
	 * @param attrs array with attribute models.
	 */
	private void addAttrs(final Element el, final XMData[] attrs) {
		Element element = getElement(el);
		for (XMData x : attrs) {
			XMOccurrence attOcc = x.getOccurence();
			if (attOcc.isIllegal()) {
				continue;
			}
			String baseName = x.getRefTypeName();
			if (baseName == null) {
				baseName = getNameElement(x.getXDPosition());
			} else {
				String declaration = "";
				CompileVariable xmVariable =
					(CompileVariable) x.getXDPool().getVariableTable().getVariable(baseName);
				if (xmVariable != null) {
					SPosition sPosition = xmVariable.getSourcePosition();
					String sysId = sPosition.getSysId();
					int lastH = sysId.lastIndexOf("/");
					if (lastH >= 0) {
						declaration = sysId.substring(lastH + 1).replace(".xdef", "") + "_";
					}
				}
				baseName = declaration + baseName;
			}
			String baseType = baseName + TYPE;
			String nsUri = x.getNSUri();
			String schemaName = findSchemaItem(nsUri);
			Element schema = (nsUri == null) ? getSchemaRoot(element) : _xsdSources.get(schemaName);
			String targetNs = schema.getAttribute("targetNamespace");
			String namespace = getNamespace(nsUri);
			String nameNamespace = namespace.replaceAll(":", "");
			if (!nameNamespace.isEmpty()) {
				nameNamespace = ":" + nameNamespace;
			}
			Element exElement = findChild(schema, x.getName() + TYPE);
			if (exElement != null) {
				String name = exElement.getAttribute("name");
				baseType = namespace + name;
			}
			Element att = genSchemaElem(el, "attribute");
			att.setAttribute("use",	attOcc.isRequired()?"required":"optional");
			if (nsUri != null && !nsUri.isEmpty()) {
				if (!targetNs.equals(nsUri)) {
					att.setAttribute("ref", namespace + x.getLocalName());
					namespace = getNamespace(nsUri);
					if (!namespace.isEmpty()) {
						att.setAttribute("xmlns" + nameNamespace, nsUri);
					}
					String outName = findSchemaItem(nsUri);
					Element schemaItem;
					if (outName == null) {
						outName = genNewName();
						schemaItem = genNewSchema(outName);
						schemaItem.setAttribute("targetNamespace", nsUri);
					} else {
						schemaItem = _xsdSources.get(outName);
					}
					att = genSchemaElem(schemaItem, "attribute");
				}
			}
			/* AK */
			if (nsUri != null && !nsUri.isEmpty()) {
				att.setAttribute("xmlns", nsUri);
				att.setAttribute("ref", x.getLocalName());
				Element attribute = genSchemaElem(schema, "attribute");
				attribute.setAttribute("name", x.getLocalName());
				attribute.setAttribute("type", "tns:" + baseType);
			} else if (!targetNs.isEmpty()) {
				String tnsSchema = schema.getAttribute("xmlns:tns");
				if (tnsSchema.equals(targetNs)) {
					schema.setAttribute("xmlns:tns", targetNs);
					att.setAttribute("type", "tns:" + baseType);
				} else {
					att.setAttribute("type", namespace + baseType);
				}
				att.setAttribute("name", x.getLocalName());
			} else {
				att.setAttribute("type", baseType);
				att.setAttribute("name", x.getLocalName());
			}
			GenParser parserInfo = GenParser.genParser(x, _genXdateOutFormat); /* AK */
			if (parserInfo.getFixed() != null) {
				att.setAttribute("fixed", parserInfo.getFixed());
			} else if (parserInfo.getDefault() != null) {
				att.setAttribute("default", parserInfo.getDefault());
			}
			String typeName = genDeclaredName(parserInfo);
			if (parserInfo.getParser().getNamedParams().isEmpty()) {
				String parserName = SCHEMA_PFX + parserInfo.getParser().parserName();
				if (typeName == null) {
					att.setAttribute("type",parserName);
				} else {
					Element tpel = _types == null ? getSchemaRoot(el) : _types;
					Element simpletp = findSchematype(tpel, typeName);
					if (simpletp == null) {
						simpletp = genSchemaElem(tpel, "simpleType");
						addDocumentation(simpletp, parserInfo.getInfo());
						simpletp.setAttribute("name", baseType);
						Element restr = genRestrictionElement(simpletp);
						restr.setAttribute("base", parserName);
					}
					att.setAttribute("type", parserName);
				}
			} else {
				if (typeName == null) {
					Element simpletp = genSchemaElem(schema, "simpleType");
					simpletp.setAttribute("name", baseType);
					genRestrictions(simpletp, parserInfo);
				} else {
					Element tpel = _types == null ? getSchemaRoot(el) : _types;
					Element simpleType = findSchematype(tpel, typeName);
					if (simpleType == null) {
						simpleType = genSchemaElem(getSchemaRoot(tpel), "simpleType");
						genRestrictions(simpleType, parserInfo);
						simpleType.setAttribute("name", baseType);
					}
				}
			}
		}
	}
	/* AK */

	/** Adds a namespace declaration to both the parent schema element and the child element
	 * @param schema the parent {@link Element} to which the namespace declaration is added.
	 * @param child the child {@link Element} to which the namespace declaration is also added.
	 * @param nsUri the namespace URI to be declared.
	 */
	private void addXmlns(Element schema, Element child, String nsUri) {
		String namespace = getNamespace(nsUri);
		if (!namespace.isEmpty()) {
			String ns = namespace.replaceAll(":", "");
			schema.setAttribute("xmlns:" + ns, nsUri);
			child.setAttribute("xmlns:" + ns, nsUri);
		}
	}

	/** Retrieves a specific {@link Element} from a given XML element.
	 * @param element The starting XML {@link Element} to search within.
	 * @return The found {@link Element} corresponding to the "extension" node if it exists;
	 * otherwise, the original {@link Element} if no "extension" node is found.
	 */
	private Element getElement(Element element) {
		if (element.getNodeName().contains("complexType")) {
			NodeList children = element.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (child.getNodeName().contains("complexContent")) {
					element = (Element) child;
					NodeList children2 = element.getChildNodes();
					for (int j = 0; j < children2.getLength(); j++) {
						Node child2 = children2.item(j);
						if (children2.item(j).getNodeName().contains("extension")) {
							return (Element) child2;
						}
					}
				}
			}
		}
		return element;
	}

	/** Finds a child element with a specific node name and attribute value.
	 * @param element The parent element to search within.
	 * @param name The value of the "name" attribute to match.
	 * @return the child element that matches the given node name and name attribute,
	 * or null if no such element is found.
	 */
	private Element findChild(Element element, String name) {
		NodeList childrenSchema = element.getChildNodes();
		for (int i = 0; i < childrenSchema.getLength(); i++) {
			Node node = childrenSchema.item(i);
			String nameNode = node.getNodeName();
			if (node instanceof Element && nameNode.contains("simpleType")) {
				Element elem = (Element) node;
				String nameNodeElement = elem.getAttribute("name");
				if ((name).equals(nameNodeElement)) {
					return elem;
				}
			}
		}
		return null;
	}

	/** Extracts and formats the name element from the given XD position.
	 * @param xdPos The XD position string to be processed.
	 * @return The formatted name element. If the input is null, returns null.
	 */
	private String getNameElement(final String xdPos) {
		if (xdPos == null) {
			return null;
		}
		String name = xdPos;
		int lastIndexH = xdPos.lastIndexOf("#");
		if (lastIndexH != -1) {
			name = name.substring(lastIndexH + 1);
		}
		String resultName = "";
		String[] parts = name.split("/");
		for (String part : parts) {
			part = part.replaceAll("\\@", "");
			part = part.replaceAll("\\$", "");
			if (part.contains(":")) {
				int indexL = part.indexOf(":");
				String namespace = part.substring(0, indexL + 1);
				part = part.replaceFirst(namespace, "");
			}
			resultName += part + "_";
		}
		if (resultName.endsWith("_")) {
			resultName = resultName.substring(0, resultName.length() - 1);
		}
		return resultName;
	}

	/** Extracts the namespace from the given ns URI.
	 * @param nsUri The namespace URI to be processed.
	 * @return The extracted namespace with a colon appended. If nsUri is null or empty, return empty string.
	 */
	private String getNamespace(String nsUri) {
		if (nsUri == null || nsUri.isEmpty()) {
			return "";
		}
		if (nsUri.contains("/")) {
			int lastIndexN = nsUri.lastIndexOf("/");
			return nsUri.substring(lastIndexN + 1) + ":";
		}
		return nsUri + ":";
	}

	private String getNamespace(String targetNs, String nsUri) {
		String namespace = "";
		if (targetNs != null && !targetNs.isEmpty()) {
			namespace = "tns:";
		}
		if (nsUri != null && !nsUri.isEmpty() && !nsUri.equals(targetNs)) {
			namespace = getNamespace(nsUri);
		}
		return namespace;
	}
	/* AK */

	/** Find simpleType element with given name.
	 * @param el element where to find.
	 * @param name name of simpleType (may be null).
	 * @return simpleType element with given name or null.
	 */
	private Element findSchematype(final Element el, final String name) {
		if (name != null) {
			Element root = getSchemaRoot(el);
			NodeList nl = KXmlUtils.getChildElementsNS(root, XMLConstants.W3C_XML_SCHEMA_NS_URI,"simpleType");
			for (int i=0; i < nl.getLength(); i++) {
				Element stype = (Element)nl.item(i);
				if (stype.getAttribute("name").equals(name)) {
					return stype;
				}
			}
		}
		return null;
	}

	/** Get root schema element.
	 * @param el actual element,
	 * @return root schema element
	 */
	private Element getSchemaRoot(final Element el) {
		Element schema = el;
		for (;;) {
			if ((SCHEMA_PFX+"schema").equals(schema.getNodeName())) {
				return schema;
			}
			Node node = schema.getParentNode();
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				schema = (Element) node;
			} else {
				throw new SRuntimeException(XDCONV.XDCONV206); //XML shema not found
			}
		}
	}

	/** Create XML schema from Xdefinition model element.
	 * @param parent where add the model.
	 * @param xel Xdefinition model of element.
	 * @return created XML schema.
	 */
	private Element genElem(final Element parent, final XMElement xel) {
		Element schema = getSchemaRoot(parent);
		String targetNs = schema.getAttribute("targetNamespace");
		if (targetNs == null) {
			targetNs = "";
		}
		/* AK */
		String xdPos = xel.getXDPosition();
		String name = getNameElement(xdPos);
		/* AK */
		String nsUri = xel.getNSUri();
		if (nsUri != null && !nsUri.isEmpty()) {
			if (!nsUri.equals(targetNs)) {
				String outName = findSchemaItem(nsUri);
				Element schemaItem;
				if (outName == null) {
					outName = genNewName();
					schemaItem = genNewSchema(outName);
					schemaItem.setAttribute("targetNamespace", nsUri);
					Element imprt = genSchemaElem(null, "import");
					imprt.setAttribute("schemaLocation", outName + ".xsd");
					imprt.setAttribute("namespace", nsUri);
					schema.insertBefore(imprt, schema.getFirstChild());
				} else {
					schemaItem = _xsdSources.get(outName);
				}
				genElem(schemaItem, xel);
				Element elem = genSchemaElem(parent, "element");
				addXmlns(schema, elem, nsUri);
				String namespace = getNamespace(nsUri);
				elem.setAttribute("ref", namespace + xel.getLocalName());
				setOccurrence(elem, xel);
				return elem;
			}
		} else {
			if (!targetNs.isEmpty()) {
				String outName = findSchemaItem("");
				Element newSchema;
				if (outName == null) {
					outName = genNewName();
					newSchema = genNewSchema(outName);
					Element imprt = genSchemaElem(null, "import");
					imprt.setAttribute("schemaLocation", outName + ".xsd");
					schema.insertBefore(imprt, schema.getFirstChild());
				} else {
					newSchema = _xsdSources.get(outName);
				}
				genElem(newSchema, xel);
				Element elem = genSchemaElem(parent,"element");
				addXmlns(schema, elem, nsUri);
				elem.setAttribute("ref", xel.getLocalName());
				setOccurrence(elem, xel);
				return elem;
			}
		}
		Element el = genSchemaElem(parent, "element");
		el.setAttribute("name", xel.getLocalName());
		if (!SCHEMA_QNAME.equals(KXmlUtils.getQName(parent))) {
			setOccurrence(el, xel); // skip if root element model
		}
		XMData[] attrs = xel.getAttrs();
		XMNode[] children = xel.getChildNodeModels();
		/* AK */
		String refPos = xel.getReferencePos();
		if (refPos != null) {
			genRef(el, xel);
			return null;
		}
		String nameEl = name + TYPE;
		if (children.length > 0 || attrs.length > 0) {
			if (!targetNs.isEmpty()) {
				schema.setAttribute("xmlns:tns", targetNs);
				el.setAttribute("type", "tns:" + nameEl);
			} else {
				el.setAttribute("type", nameEl);
			}
		}
		if (children.length == 1 && attrs.length == 0 && children[0].getKind() == XMTEXT
			&& !children[0].isOptional()) {
			return genTextElement(schema, el, nameEl, children[0]);
		}
		Element complexType = genSchemaElem(schema, "complexType");
		complexType.setAttribute("name", nameEl);
		for (XMNode child : children) {
			if (child.getKind() == XMTEXT) {
				complexType.setAttribute("mixed", "true");
				break;
			}
		}
		if (children.length > 0) {
			XMNode x = children[0];
			if (x.getKind()==XMNode.XMMIXED && ((XMSelector) x).getEndIndex()==children.length-1) {
				// try tu generate xs:all
				boolean allPossible = true;
				for (int i = 1; i < children.length; i++) {
					XMNode y = children[i];
					if (y.getKind() == XMNode.XMELEMENT) {
						XMOccurrence occ = y.getOccurence();
						if (occ.minOccurs() > 1 || occ.maxOccurs() > 1) {
							allPossible = false;
							break;
						}
					} else {
						break;
					}
				}
				if (allPossible) { // generate xs:all
					/* AK */
//					Element all = genSchemaElem(complt, "all");
					Element all = genSchemaElem(complexType, "all");
					/* AK */
					for (int i = 1; i < children.length; i++) {
						XMNode y = children[i];
						if (y.getKind() == XMNode.XMELEMENT) {
							if (!y.getOccurence().isIllegal()) {
								genElem(all, (XMElement) y);
							}
						}
					}
					addAttrs(complexType, attrs);
					return el;
				}
			}
			switch (x.getKind()) {
				case XMNode.XMCHOICE:
				case XMNode.XMMIXED:
				case XMNode.XMSEQUENCE:
					if (((XMSelector) x).getEndIndex() == children.length - 1) {
						genGroup(complexType, complexType, (XMSelector) children[0], children, 0);
						addAttrs(complexType, attrs);
						return el;
					}
			}
			Element seq = genSequenceElement(complexType);
			genSequence(complexType, seq, children, -1, children.length);
		}
		addAttrs(complexType, attrs);
		return el;
	}

	/* AK */
	/** Generates a reference element within the provided element schema.
	 * @param element The element to be modified or extended with references.
	 * @param xel The XMElement containing reference and attribute data.
	 */
	private void genRef(Element element, XMElement xel) {
		Element schema = getSchemaRoot(element);
		String targetNs = schema.getAttribute("targetNamespace");
		String refPos = xel.getReferencePos();
		XMElement refElement = (XMElement) xel.getXDPool().findModel(refPos);
		if (refElement == null) {
			createEndNotRefElement(element, xel);
		} else {
			createRefElement(element, schema, targetNs, refElement, xel);
		}
	}

	/** Creates a new complex type element for the given XMElement that does not reference another element.
	 * @param element The element to which the new complex type will be added.
	 * @param xel The XMElement containing child nodes and attributes.
	 */
	private void createEndNotRefElement(Element element, XMElement xel) {
		Element schema = getSchemaRoot(element);
		String targetNs = schema.getAttribute("targetNamespace");
		String namespace = getNamespace(targetNs, xel.getNSUri());
		if (namespace.equals("tns:")) {
			schema.setAttribute("xmlns:tns", targetNs);
		}
		XMNode[] children = xel.getChildNodeModels();
		if (children.length == 1 && children[0].getKind() == XMTEXT && xel.getAttrs().length == 0) {
			XMData xmData = (XMData) children[0];
			String nameText = getNameElement(xmData.getXDPosition());
			genTextElement(schema, element, nameText, xmData);
		} else {
			Element complexType = genSchemaElem(schema, "complexType");
			complexType.setAttribute("name", xel.getLocalName());
			Element sequence = null;
			Element choice = null;
			for (XMNode child : children) {
				switch (child.getKind()) {
					case XMNode.XMTEXT: complexType.setAttribute("mixed", "true"); break;
					case XMCHOICE: choice = genSchemaElem(sequence, "choice"); sequence = choice; break;
					case XMSELECTOR_END:
						if (choice != null) {
							sequence = (Element) choice.getParentNode();
						}
						break;
					case XMELEMENT:
						if (sequence == null) {
							sequence = genSequenceElement(complexType);
						}
						String childRefPos = ((XMElement) child).getReferencePos();
						boolean alreadyDefined = false;
						if (childRefPos != null) {
							XMElement refXMElem = (XMElement) xel.getXDPool().findModel(childRefPos);
							NodeList nodes = schema.getChildNodes();
							for (int i = 0; i < nodes.getLength(); i++) {
								Node n = nodes.item(i);
								if (n instanceof Element) {
									Element el = (Element) n;
									String elName = el.getAttribute("name");
									if (elName.equals(refXMElem.getLocalName())) {
										alreadyDefined = true;
										Element refElem;
										refElem = genSchemaElem(sequence, "element");
										refElem.setAttribute("name", child.getLocalName());
										setOccurrence(refElem, child);
										if (child.getKind() == XMELEMENT
											&& ((XMElement) child).getReferencePos() != null) {
											genRef(refElem, (XMElement) child);
										} else {
											refElem.setAttribute("type",namespace+refXMElem.getLocalName());
										}
										break;
									}
									setOccurrence(el, xel);
								}
							}
						}	if (!alreadyDefined) {
							genElem(sequence, (XMElement) child);
						}
						break;
				}
			}
			addAttrs(complexType, xel.getAttrs());
		}
	}

	private void createRefElement(Element element,
		Element schema,
		String targetNs,
		XMElement refElement,
		XMElement xel) {
		String namespace = getNamespace(targetNs, xel.getNSUri());
		if (namespace.equals("tns:")) {
			schema.setAttribute("xmlns:tns", targetNs);
		}
		String refName = refElement.getLocalName();
		String xelName = xel.getLocalName();
		boolean cycle = isCycleDetected(element, refName);
		String refRefPos = refElement.getReferencePos();
		boolean isRef = (refRefPos != null);
		XMElement refRefElem = null;
		if (isRef) {
			refRefElem = (XMElement) xel.getXDPool().findModel(refRefPos);
		}
		XMNode[] refChildren = refElement.getChildNodeModels();
		Set<XMNode> filteredRefChildren = Arrays.stream(refChildren).collect(Collectors.toSet());
		if (refRefElem != null) {
			Set<XMNode> newFilteredChildren = new LinkedHashSet<>();
			for (XMNode xmNode : filteredRefChildren) {
				if (xmNode.getKind() == XMELEMENT) {
					processChildren(refName, newFilteredChildren, xmNode);
				}
			}
			filteredRefChildren = newFilteredChildren;
		}
		XMNode[] xelChildren = xel.getChildNodeModels();
		Set<XMNode> filteredXelChildren = new LinkedHashSet<>();
		for (XMNode xmNode : xelChildren) {
			if (xmNode.getKind() == XMELEMENT) {
				processChildren(xelName, filteredXelChildren, xmNode);
			} else {
				filteredXelChildren.add(xmNode);
			}
		}
		filteredXelChildren.removeAll(filteredRefChildren);
		XMData[] xelAttrs = xel.getAttrs();
		XMData[] refAttrs = refElement.getAttrs();
		Set<String> refAttrNames =
			Arrays.stream(refAttrs).map(XMData::getLocalName).collect(Collectors.toSet());
		Set<XMData> filteredRefAttrs =
			Arrays.stream(xelAttrs).filter(xelAttr -> refAttrNames.contains(xelAttr.getLocalName()))
			.collect(Collectors.toCollection(LinkedHashSet::new));
		Set<XMData> filteredXelAttrs =
			Arrays.stream(xelAttrs).collect(Collectors.toCollection(LinkedHashSet::new));
		filteredXelAttrs.removeAll(filteredRefAttrs);
		if (cycle) {
			element.setAttribute("type", namespace + refName);
		} else if (filteredXelAttrs.isEmpty() && filteredXelChildren.size() == 1
			&& filteredXelChildren.iterator().next().getKind() == XMTEXT) {
			String nameText = getNameElement(refElement.getXDPosition());
			element.setAttribute("type", namespace + nameText + TYPE);
			XMData xmData = (XMData) filteredXelChildren.iterator().next();
			GenParser parserInfo = GenParser.genParser(xmData, _genXdateOutFormat);
			createSimpleTypeElement(schema, namespace + nameText + TYPE, parserInfo);
		} else {
			if (!filteredXelAttrs.isEmpty() || !filteredXelChildren.isEmpty()) {
				Element complexType = genSchemaElem(schema, "complexType");
				String name = getNameElement(xel.getXDPosition());
				Element complexContent = genSchemaElem(complexType, "complexContent");
				Element extension = genSchemaElem(complexContent, "extension");
				extension.setAttribute("base", namespace + refName);
				if (element.getAttribute("type").isEmpty()) {
					element.setAttribute("type", namespace + name);
				}
				complexType.setAttribute("name", name);
				Element sequence = null;
				for (XMNode xmNode : filteredXelChildren) {
					if (xmNode.getKind() == XMTEXT) {
						complexType.setAttribute("mixed", "true");
					} else if (xmNode.getKind() == XMELEMENT) {
						if (sequence == null) {
							sequence = genSequenceElement(extension);
						}
						genElem(sequence, (XMElement) xmNode);
					}
				}
				addAttrs(extension, filteredXelAttrs.toArray(new XMData[0]));
			} else if (element.getAttribute("type").isEmpty()) {
				element.setAttribute("type", namespace + refName);
			} else {
				String type = element.getAttribute("type");
				if (type.contains(":")) {
					int index = type.indexOf(":");
					type = type.substring(index + 1);
				}
				if (!type.equals(refName)) {
					Element complexType = genSchemaElem(schema, "complexType");
					complexType.setAttribute("name", type);
					Element complexContent = genSchemaElem(complexType,"complexContent");
					Element extension = genSchemaElem(complexContent, "extension");
					extension.setAttribute("base", namespace + refName);
				}
			}
			genRef(element, refElement);
		}
	}

	/** Processes a set of XML nodes by checking if the given node is a child of a specified element name.
	 * @param xelName The name of the element to match against the parent name of the `xmNode`.
	 * @param xmNodes A set of `XMNode` objects where matching nodes will be added.
	 * @param xmNode  The `XMNode` object to be processed and potentially added to the set.
	 */
	private void processChildren(String xelName, Set<XMNode> xmNodes, XMNode xmNode) {
		String xmNodeXdPos = xmNode.getXDPosition();
		String[] parts = xmNodeXdPos.split("/");
		if (parts.length >= 2) {
			String parentName = parts[parts.length - 2];
			if (parentName.contains("#")) {
				int lastH = parentName.lastIndexOf("#");
				parentName = parentName.substring(lastH + 1);
			}
			if (parentName.equals(xelName)) {
				xmNodes.add(xmNode);
			}
		}
	}

	/** Checks if there is a cycle detected in the XML by comparing the name  of the given
	 * @param element The {@link Element} from which to start checking for cycles.
	 * @param refName The reference name used to detect a cycle.
	 * @return {@code true} if a cycle is detected (i.e., a parent element with a matching name
	 * or concatenated name is found); {@code false} otherwise.
	 */
	private boolean isCycleDetected(Element element, String refName) {
		Node parentNode = element.getParentNode();
		while (parentNode != null) {
			if (parentNode.getNodeType() == Node.ELEMENT_NODE) {
				Element parentElement = (Element) parentNode;
				String parentName = parentElement.getAttribute("name");
				if (parentName.equals(refName) || parentName.equals(refName + TYPE)) {
					return true; // cycle detected
				}
			}
			parentNode = parentNode.getParentNode();
		}
		return false;
	}

	/** Creates a new simple type element within the provided schema element.
	 * @param schema     The schema element to which the new el will be added.
	 * @param nameEl     The name of the new simple type element.
	 * @param parserInfo The parser information used to generate restrictions.
	 * @return The newly created simple type element.
	 */
	private Element createSimpleTypeElement(Element schema, String nameEl, GenParser parserInfo) {
		Element simpleElement = genSchemaElem(schema, "simpleType");
		genRestrictions(simpleElement, parserInfo);
		simpleElement.setAttribute("name", nameEl);
		return simpleElement;
	}

	/* AK */
	/** Generate a new unique XML schema file name.
	 * @return new unique XML schema file name.
	 */
	private String genNewName() {
		for (int i = 1;;i++) {
			String s = _rootName + "_" + (i<10?"0":"") + i;
			if (!_xsdSources.containsKey(s)) {
				return s;
			}
		}
	}

	/** Add schema to the map of schema.
	 * @param name name of item.
	 * @param schema the XML element with schema.
	 */
	public final void addSchema(final String name, final Element schema) {
		if (_xsdSources.containsKey(name)) {
			throw new SRuntimeException(XDCONV.XDCONV207, name); //XML schema "&{0}" already exists
		}
		_xsdSources.put(name, schema);
	}

	/** Create new element with XML schema namespace and given name and add it to the parent node (if parent
	 * is not null).
	 * @param parent parent node.
	 * @param name local name of element to be created.
	 * @return created element.
	 */
	private Element genSchemaElem(final Element parent, final String name) {
		Element result = _doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, SCHEMA_PFX + name);
		if (parent != null) {
			if (name.equals("sequence")) {
				Node firstChild = parent.getFirstChild();
				parent.insertBefore(result, firstChild);
			} else {
				parent.appendChild(result);
			}
		}
		return result;
	}

	/** Create new Schema element.
	 * @return created element.
	 */
	private Element genNewSchema() {
		Element schema = genSchemaElem(null, "schema");
		schema.setAttribute("elementFormDefault", "qualified");
		schema.setAttribute("attributeFormDefault", "unqualified");
		if (_typesName!= null) {
			schema.setAttribute("xmlns", USERTYPES_URI);
			Element imprt = genSchemaElem(schema, "import");
			imprt.setAttribute("namespace", USERTYPES_URI);
			imprt.setAttribute("schemaLocation", _typesName + ".xsd");
		}
		return schema;
	}

	/** Create new Schema element and put it to the map of schema.
	 * @param outName name of item in the map.
	 * @return created element.
	 */
	private Element genNewSchema(final String outName) {
		Element schema = genNewSchema();
		addSchema(outName, schema);
		return schema;
	}

	/** Find schema with given namespace in the map of schema.
	 * @param uri namespace to be found.
	 * @return found schema or null.
	 */
	private String findSchemaItem(final String uri) {
		for (Map.Entry<String, Element> en: _xsdSources.entrySet()) {
			Element schema = en.getValue();
			String ns = schema.getAttribute("targetNamespace");
			if ((ns.isEmpty()&&(uri==null||uri.isEmpty())) || ns.equals(uri)) {
				return en.getKey();
			}
		}
		return null;
	}

	/** Run XML schema generator.
	 * @param xp compiled XDPool.
	 * @param xdName name of root Xdefinition.
	 * @param modelName name of the root model. May be null, then all values from "xs:root" parameter
	 * are used to create models.
	 * @param outName name of root XML schema file.
	 * @param outType name of XML schema file with type declarations (may be null, then declared simple
	 * types are generated to the file with model).
	 * @param genInfo switch if generate annotation with documentation.
	 * @param genXdateOutFormat if true, from the xdatetime method the outFormat parameter (the second
	 * sequential) is used as mask to validate datetime.
	 * @return map with names of XML schema files and corresponding Elements.
	 */
	public static Map<String, Element> genSchema(final XDPool xp,
		final String xdName,
		final String modelName,
		final String outName,
		final String outType,
		final boolean genInfo,
		final boolean genXdateOutFormat) {
		String xname = xdName == null ? xp.getXMDefinitionNames()[0] : xdName;
		XMDefinition xdef = xp.getXMDefinition(xname);
		if (xdef == null) {
			throw new SRuntimeException(XDCONV.XDCONV201, xname); //Can't find an Xdefinition&{0}{ "}{"}
		}
		String mname;
		XMElement[] roots;
		if (modelName == null || modelName.isEmpty()) {
			roots = xp.getXMDefinition(xname).getRootModels();
			if (roots != null && roots.length > 0) {
				mname = roots[0].getLocalName();
			} else {
				throw new SRuntimeException(XDCONV.XDCONV202); //XML model not specified
			}
		} else {
			roots = xp.getXMDefinition(xname).getModels();
			String mURI = null;
			mname = null;
			for (XMElement xel : roots) {
				if (modelName.equals(xel.getName())) {
					mname = xel.getLocalName();
					mURI = xel.getNSUri();
					break;
				}
			}
			if (mname == null) {
				throw new SRuntimeException(XDCONV.XDCONV203, modelName); //Can't find root model&{0}{ "}{"}
			}
			XMDefinition xmdef = xp.getXMDefinition(xname);
			roots = new XMElement[1];
			roots[0] = xmdef.getModel(mURI, mname);
		}
		String oname = outName == null ? mname.replace(':', '_') : outName;
		String otype = outType;
		if (otype != null && !otype.isEmpty()) {
			if ((modelName == null || modelName.isEmpty()) && (outName == null || outName.isEmpty())) {
				roots = null;
			}
		} else {
			otype = null;
			if (mname == null || mname.isEmpty()) {
				throw new SRuntimeException(XDCONV.XDCONV204); //The name of xsd file is missing
			}
		}
		Xd2Xsd generator = new Xd2Xsd(xp, oname, otype, genInfo, genXdateOutFormat);
		if (roots != null) {
			Element schema = generator.genNewSchema(oname);
			XMElement xmel = roots[0];
			String nsUri = xmel.getNSUri();
			if (nsUri != null && !nsUri.isEmpty()) {
				schema.setAttribute("targetNamespace", nsUri);
			}
			if (!xmel.getOccurence().isIllegal()) {
				generator.genElem(schema, xmel);
			}
			for (int i = 1; i < roots.length; i++) {
				if (!roots[i].getOccurence().isIllegal()) {
					generator.genElem(schema, roots[i]);
				}
			}
		}
		Map<String, Element> sources = generator._xsdSources;
		for (Map.Entry<String, Element> en : sources.entrySet()) {
			String name = en.getKey();
			Element schema = en.getValue();
			schema = clearSchema(schema);
			generator._xsdSources.put(name, schema);
		}
		return generator._xsdSources;
	}

	/* AK */
	/** Clears and optimizes an XML schema element by removing duplicates and adjusting attribute values.
	 * @param schema The XML schema element to be cleared and optimized.
	 * @return A cleaned and optimized version of the input XML schema element.
	 */
	private static Element clearSchema(final Element schema) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document newDoc = builder.newDocument();
			List<Node> imports = new LinkedList<>();
			List<Node> elements = new LinkedList<>();
			List<Node> simpleTypes = new LinkedList<>();
			List<Node> complexTypes = new LinkedList<>();
			List<Node> attributes = new LinkedList<>();
			Node rootNode = newDoc.importNode(schema, false);
			newDoc.appendChild(rootNode);
			NodeList children = schema.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					Node copiedN = copyNode(newDoc, child);
					Element copiedE = (Element) copiedN;
					Element element = (Element) child;
					String tagName = element.getNodeName();
					if (tagName.contains("import")) {
						imports.add(copiedE);
					} else if (tagName.contains("attribute")) {
						attributes.add(copiedE);
					} else if (tagName.contains("element")) {
						elements.add(copiedE);
					} else {
						if (tagName.contains("simpleType")) {
							simpleTypes.add(copiedE);
						}
						if (tagName.contains("complexType")) {
							boolean isAlreadyExists = false;
							String copiedElName = copiedE.getAttribute("name");
							for (Node complexType : complexTypes) {
								if (complexType.getNodeType() == Node.ELEMENT_NODE) {
									Element complexElement = (Element) complexType;
									String name = complexElement.getAttribute("name");
									if (copiedElName.equals(name)) {
										isAlreadyExists = true;
										break;
									}
								}
							}
							if (!isAlreadyExists) {
								complexTypes.add(copiedE);
							}
						}
					}
				}
			}
			List<String> complexNames = complexTypes.stream().map(el -> ((Element) el)
				.getAttribute("name")).collect(Collectors.toList());
			List<Node> newSimpleNodes = new ArrayList<>();
			boolean[] visited = new boolean[simpleTypes.size()];
			for (int i = 0; i < simpleTypes.size(); i++) {
				if (!visited[i]) {
					visited[i] = true;
					Element simpleEl1 = (Element) simpleTypes.get(i);
					String nameEl1 = simpleEl1.getAttribute("name");
					String oldName1 = getAttributeName(nameEl1);
					String newName1 = nameEl1;
					int baseNum = 0;
					if (complexNames.contains(newName1)) {
						baseNum++;
					}
					int count = 0;
					for (Node newE : newSimpleNodes) {
						String nameNewE = ((Element) newE).getAttribute("name");
						String unique = nameNewE.replace(oldName1, "");
						boolean isNum = unique.matches("-?\\d+");
						if (isNum) {
							int num = Integer.parseInt(unique);
							if (num > count) {
								count = num;
							}
						}
						if (unique.isEmpty()) {
							count = 1;
						}
					}
					if (count == 0) {
						count++;
						newSimpleNodes.add(simpleEl1);
					}
					baseNum += count;
					if (baseNum > 1) {
						newName1 = oldName1 + baseNum;
					}
					simpleEl1.setAttribute("name", newName1);
					resetTypeBase(simpleTypes, nameEl1, newName1);
					resetTypeBase(newSimpleNodes, nameEl1, newName1);
					resetTypeBase(complexTypes, nameEl1, newName1);
					resetTypeBase(elements, nameEl1, newName1);
				}
			}
			simpleTypes = orderElements(newSimpleNodes);
			complexTypes = orderElements(complexTypes);
			addElements(rootNode, imports, simpleTypes, complexTypes, elements, attributes);
			return newDoc.getDocumentElement();
		} catch (ParserConfigurationException e) {
/*#if DEBUG*#/
			e.printStackTrace();
/*#end*/
			return schema;
		}
	}

	/** Extracts and returns the attribute name from the given string.
	 * @param name The input string from which the attribute name is to be extracted.
	 * @return The extracted attribute name if both "$" and "@" are present, otherwise the original string.
	 */
	private static String getAttributeName(final String name) {
		int indexJ = name.lastIndexOf("$");
		if (indexJ > 0) {
			int indexS = name.lastIndexOf("@");
			if (indexS > 0) {
				return name.substring(indexS + 1);
			}
		}
		return name;
	}

	/** Orders a list of XML nodes by their "name" attribute.
	 * @param nodes The list of nodes to be ordered.
	 * @return a new list of nodes ordered by the "name" attribute.
	 */
	private static List<Node> orderElements(final List<Node> nodes) {
		return nodes.stream()
			.sorted(Comparator.comparing(Xd2Xsd::getAttributeValue)).collect(Collectors.toList());
	}

	/** Retrieves the value of a specified attribute from an XML node.
	 * @param node The XML node from which to retrieve the attribute value.
	 * @return the value of the attribute, or an empty string if the attribute is not found
	 */
	private static String getAttributeValue(final Node node) {
		NamedNodeMap attributes = node.getAttributes();
		if (attributes != null) {
			Node attr = attributes.getNamedItem("name");
			if (attr != null) {
				return attr.getNodeValue();
			}
		}
		return "";
	}

	/** Adds elements to the given XML root node in a structured manner.
	 * @param root      	The root node of the XML document.
	 * @param imports   	List of imports.
	 * @param simples   	List of unique SimpleType elements.
	 * @param complexes 	Map of unique ComplexType elements.
	 * @param elements  	Map of Elements to add.
	 * @param attributes  	Map of Attributes to add.
	 */
	private static void addElements(final Node root,
		final List<Node> imports,
		final List<Node> simples,
		final List<Node> complexes,
		final List<Node> elements,
		final List<Node> attributes) {
		addElements(root, imports);
		if (!simples.isEmpty()) {
			addComment(root, XDCONV.XDCONV301); //Definition of simple types
			addDividerLine(root);
			addElements(root, simples);
		}
		if (!attributes.isEmpty()) {
			addComment(root, XDCONV.XDCONV302); //Definition of attributes
			addDividerLine(root);
			addElements(root, attributes);
		}
		if (!elements.isEmpty()) {
			addComment(root, XDCONV.XDCONV303); //Element Body structures
			addDividerLine(root);
			addElements(root, elements);
		}
		if (!complexes.isEmpty()) {
			addComment(root, XDCONV.XDCONV304); //Partial substantive structures
			addDividerLine(root);
			addElements(root, complexes);
		}
	}

	/** Adds a comment node with the specified content to the given XML node.
	 * @param parentNode     The parent XML node where the comment will be added.
	 * @param id 		 	 XDCONV id ref on the content of the comment.
	 */
	private static void addComment(final Node parentNode, final long id) {
		Report message = Report.info(id);
		Comment comment = parentNode.getOwnerDocument().createComment(message.getLocalizedText());
		parentNode.appendChild(comment);
	}

	/** Adds a comment node as a divider line to the given XML node.
	 * @param parentNode The parent XML node where the comment will be added.
	 */
	private static void addDividerLine(Node parentNode) {
		String line = "=====================================================";
		Comment commentLine = parentNode.getOwnerDocument().createComment(line);
		parentNode.appendChild(commentLine);
	}

	/** Adds a set of Node elements to a parent Node.
	 * @param root     The parent Node to which elements will be added.
	 * @param elements The set of Node elements to be added.
	 */
	private static void addElements(final Node root, final List<Node> elements) {
		for (Node node : elements) {
			root.appendChild(node);
		}
	}

	/** Resets the type attribute of XML elements and attributes from an old name to a new name.
	 * This method recursively processes the given element and all its child elements.
	 * @param elements The XML elements to process.
	 * @param oldName  The old type name to be replaced.
	 * @param newName  The new type name to replace with.
	 */
	private static void resetTypeBase(final List<Node> elements, final String oldName, final String newName) {
		elements.forEach(el -> resetTypeBase((Element) el, oldName, newName));
	}

	private static void resetTypeBase(final Element element, final String oldName, final String newName) {
		String elTagName = element.getTagName();
		if (elTagName.contains("element") || elTagName.contains("attribute")) {
			String type = element.getAttribute("type");
			String namespace = extractNamespace(type);
			if (type.equals(oldName)) {
				element.setAttribute("type", namespace + newName);
			}
		} else if (elTagName.contains("restriction")) {
			String base = element.getAttribute("base");
			String namespace = extractNamespace(base);
			if (base.equals(oldName)) {
				element.setAttribute("base", namespace + newName);
			}
		}
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				resetTypeBase((Element) child, oldName, newName);
			}
		}
	}

	/** Extracts the namespace from a given string that represents a position.
	 * @param position the qualified name string from which to extract the namespace.
	 * @return the extracted namespace followed by a colon if a namespace is present and non-empty;
	 * otherwise, returns an empty string.
	 */
	private static String extractNamespace(final String position) {
		String namespace = "";
		if (position.contains(":")) {
			String[] parts = position.split(":");
			if (parts.length == 2) {
				namespace = parts[0];
				if (!namespace.isEmpty()) {
					namespace += ":";
				}
			}
		}
		return namespace;
	}

	/** Copies Node from source Document to a destination Document, including its attributes and child nodes.
	 * @param destDoc The destination XML Document where the copied Node  will be created.
	 * @param sourceNode The source Node to be copied.
	 * @return A copied Node in the destination Document with attributes and child nodes preserved.
	 */
	private static Node copyNode(final Document destDoc, final Node sourceNode) {
		Node copiedNode = null;
		switch (sourceNode.getNodeType()) {
			case Node.ELEMENT_NODE:
				Element sourceElement = (Element) sourceNode;
				Element newElement =
					destDoc.createElementNS(sourceElement.getNamespaceURI(), sourceElement.getTagName());
				NamedNodeMap attributes = sourceElement.getAttributes();
				for (int i = 0; i < attributes.getLength(); i++) {
					Attr attribute = (Attr) attributes.item(i);
					if (attribute.getValue() != null) {
						newElement.setAttribute(attribute.getName(), attribute.getValue());
					}
				}
				NodeList childNodes = sourceElement.getChildNodes();
				for (int i = 0; i < childNodes.getLength(); i++) {
					Node childNode = childNodes.item(i);
					Node copiedChild = copyNode(destDoc, childNode);
					newElement.appendChild(copiedChild);
				}
				copiedNode = newElement;
				break;
			case Node.TEXT_NODE: copiedNode = destDoc.createTextNode(sourceNode.getNodeValue()); break;
		}
		return copiedNode;
	}

	/** Generates a text element within the provided schema element if it does not already exist.
	 * @param schema The schema element to which the new element will be added.
	 * @param el     The element that will be modified with attributes.
	 * @param nameEl The name of the element to be generated.
	 * @param child  The child XMNode from which data is derived.
	 * @return The modified element with added attributes,
	 * or null if the element already exists.
	 */
	private Element genTextElement(final Element schema,
		final Element el,
		final String nameEl,
		final XMNode child) {
		String nameElement = nameEl.replace("_text", "_Text");
		if (child.getKind() == XMNode.XMTEXT) {
			XMData xmData = (XMData) child;
			GenParser parserInfo = GenParser.genParser(xmData, _genXdateOutFormat);
			String complexName = getNameElement(child.getXDPosition());
			Element existingElement = findChild(schema, "simpleType", nameElement);
			String targetNs = schema.getAttribute("targetNamespace");
			String namespace = getNamespace(targetNs, child.getNSUri());
			if (namespace.equals("tns:")) {
				schema.setAttribute("xmlns:tns", targetNs);
			}
			boolean isOptional = xmData.isOptional();
			if (isOptional) {
				if (existingElement != null) {
					el.setAttribute("type", namespace + nameElement);
				} else {
					Element simpleType = genSchemaElem(schema, "simpleType");
					simpleType.setAttribute("name", complexName);
					el.setAttribute("type", namespace + complexName);
					Element union = genSchemaElem(simpleType, "union");
					Element simpleType1 = genSchemaElem(union, "simpleType");
					Element restriction = genSchemaElem(simpleType1, "restriction");
					restriction.setAttribute("base", "xs:string");
					Element length = genSchemaElem(restriction, "length");
					length.setAttribute("value", "0");
					Element simpleType2 = genSchemaElem(union, "simpleType");
					Element restriction2 = genSchemaElem(simpleType2, "restriction");
					restriction2.setAttribute("base", namespace + nameElement);
					createSimpleTypeElement(schema, nameElement, parserInfo);
					setElementValueType(el, parserInfo);
					el.setAttribute("type", namespace + complexName);
				}
			} else {
				if (existingElement == null) {
					createSimpleTypeElement(schema, complexName, parserInfo);
					setElementValueType(el, parserInfo);
					el.setAttribute("type", namespace + complexName);
				} else {
					el.setAttribute("type", namespace + complexName);
				}
			}
		}
		return el;
	}

	/** Sets the fixed or default attribute on the provided element based on the parser.
	 * @param el         The element to which the attributes will be added.
	 * @param parserInfo The parser information containing the fixed or default values.
	 */
	private void setElementValueType(final Element el, final GenParser parserInfo) {
		if (parserInfo.getFixed() != null) {
			el.setAttribute("fixed", parserInfo.getFixed());
		} else if (parserInfo.getDefault() != null) {
			el.setAttribute("default", parserInfo.getDefault());
		}
	}

	/** Finds a child element with a specific node name and attribute value.
	 * @param element  The parent element to search within
	 * @param nodeName The name of the node to look for (part of the node name)
	 * @param name     The value of the "name" attribute to match
	 * @return the child element that matches the given node name and name attribute,
	 * or {@code null} if no such element is found
	 */
	private Element findChild(final Element element, final String nodeName, final String name) {
		NodeList childrenSchema = element.getChildNodes();
		for (int i = 0; i < childrenSchema.getLength(); i++) {
			Node node = childrenSchema.item(i);
			String nameNode = node.getNodeName();
			if (node instanceof Element && nameNode.contains(nodeName)) {
				Element elem = (Element) node;
				String nameNodeElement = elem.getAttribute("name");
				if ((name).equals(nameNodeElement)) {
					return elem;
				}
			}
		}
		return null;
	}
	/* AK */
}
