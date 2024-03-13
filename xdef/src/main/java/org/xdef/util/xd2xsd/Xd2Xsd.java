package org.xdef.util.xd2xsd;

import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.impl.parsers.XSParseList;
import org.xdef.impl.parsers.XSParseUnion;
import org.xdef.model.XMData;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import org.xdef.model.XMOccurrence;
import org.xdef.model.XMSelector;
import org.xdef.xml.KXmlUtils;

/** Convertor of X-definition to XML Schema.
 * @author  Vaclav Trojan
 */
public class Xd2Xsd {
	/** Prefix used for the XML schema namespace. */
	private static final String SCHEMA_PFX = "xs:";
	/** QName of schema element. */
	private static final QName SCHEMA_QNAME =
		new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "schema");
	/** Name of root XML schema file. */
	private final String _rootName;
	/** Map of file names and XML schema elements.*/
	private final Map<String, Element> _xsdSources;
	/** Switch if generate annotation with documentation information. */
	private final boolean _genInfo;
	/**  Switch generate xdatatime outFormat. */
	private final boolean _genXdateOutFormat;
	/** org.w3c.dom.Document used for creation of nodes. */
	private final Document _doc;

	/** Create new instance of XsdGenerator.
	 * @param xsdName name or XML schema root file
	 * @param genInfo if true the annotations with documentation is generated.
	 * @param genXdateOutFormat if true, from the xdatetime method the outFormat
	 * parameter (the second sequential) is used as mask to validate datetime.
	 */
	private Xd2Xsd(final String xsdName,
		final boolean genInfo,
		final boolean genXdateOutFormat) {
		if (xsdName == null || xsdName.isEmpty()) {
			throw new RuntimeException("The name of xsd file is missing");
		}
		_rootName = xsdName;
		_genInfo = genInfo;
		_genXdateOutFormat = genXdateOutFormat;
		_xsdSources = new HashMap<>();
		_doc = KXmlUtils.newDocument();
	}

	/** Get genXdateOutFormat swithch.
	 * @return genXdateOutFormat swithch.
	 */
	protected final boolean isGenXdateOutFormat() { return _genXdateOutFormat;}

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
	private Element genRestrictions(final Element parent,
		final GenParser parserInfo) {
		addDocumentation(parent, parserInfo.getInfo());
		String typeName = getSchemaTypeName(parserInfo);
		switch(typeName) {
			case "xs:union": {
				XDParser[] parsers =
					((XSParseUnion) parserInfo.getParser()).getParsers();
				Element union = genSchemaElem(parent, "union");
				for (XDValue xval: parsers) {
					XDParser p = (XDParser) xval;
					Element simpletp = genSchemaElem(union, "simpleType");
					genRestrictions(simpletp,
						SCHEMA_PFX+p.parserName(),
						p.getNamedParams().getXDNamedItems());
				}
				return union;
			}
			case "xs:list": {
				Element list = genSchemaElem(parent, "list");
				XDParser p =
					((XSParseList) parserInfo.getParser()).getItemParser();
				Element simpletp = genSchemaElem(list, "simpleType");
				genRestrictions(simpletp,
					SCHEMA_PFX+p.parserName(),
					p.getNamedParams().getXDNamedItems());
				return list;
			}
		}
		XDNamedValue[] xdv =
			parserInfo.getParser().getNamedParams().getXDNamedItems();
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
							param.setAttribute("value",
								xdc.getXDItem(i).toString());
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
		return XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(el.getNamespaceURI())
		 && "sequence".equals(el.getLocalName())
			? el : genSchemaElem(el, "sequence");
	}

	/** Create xd:restriction element as child of argument.
	 * @param el parent of created element.
	 * @return xd:restriction element as child of argument.
	 */
	private Element genRestrictionElement(final Element el) {
		return XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(el.getNamespaceURI())
		 && "restriction".equals(el.getLocalName())
			? el : genSchemaElem(el, "restriction");
	}

	/** Generate group of models.
	 * @param parent element where to add models.
	 * @param xsel group selector.
	 * @param children array with children.
	 * @param endIndex index of the last child.
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
								max =  occ.maxOccurs() == Integer.MAX_VALUE
									? Integer.MAX_VALUE : max + occ.maxOccurs();
							}
					}
				}
				sel = genSequenceElement(parent);
				sel = genSchemaElem(sel, "choice");
				sel.setAttribute("maxOccurs", max == Integer.MAX_VALUE
					? "unbounded" : String.valueOf(max));
				sel.setAttribute("minOccurs", String.valueOf(min));
				return genSequence(complt, sel, children, index, endIndex);
			}
			default:
				throw new RuntimeException(xsel + " not implemented yet");
		}
	}

	/** Generate sequence of models.
	 * @param complt complexType element.
	 * @param parent node where to add models.
	 * @param children array with children.
	 * @param index index of the first child.
	 * @param endIndex index of the last child.
	 * @return the index where to continue.
	 */
	private int genSequence(final Element complt,
		final Element parent,
		final XMNode[] children,
		final int index,
		final int endIndex) {
		int i = index + 1;
		for (; i < endIndex; i++) {
			XMNode x = children[i];
			switch (x.getKind()) {
				case XMNode.XMELEMENT:
					genElem(parent, (XMElement) x);
					continue;
				case XMNode.XMTEXT:
					complt.setAttribute("mixed", "true");
					continue;
				case XMNode.XMMIXED:
				case XMNode.XMCHOICE:
				case XMNode.XMSEQUENCE: {
					XMSelector xsel = (XMSelector) x;
					i = genGroup(complt, parent, xsel, children, i);
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
	 * @param xel X-definition model of element.
	 */
	private static void setOccurrence(final Element el, final XMNode xel) {
		XMOccurrence occ = xel.getOccurence();
		int minOcc = occ.minOccurs();
		int maxOcc = occ.maxOccurs();
		if (minOcc != 1 || maxOcc != 1) {
			el.setAttribute("minOccurs", String.valueOf(minOcc));
			el.setAttribute("maxOccurs", Integer.MAX_VALUE==maxOcc
				? "unbounded":String.valueOf(maxOcc));
		}
	}

	/** Add attributes to schema element.
	 * @param el schema element
	 * @param attrs array with attribute models.
	 */
	private void addAttrs(final Element el, final XMData[] attrs) {
		for (XMNode x: attrs) {
			Element att = genSchemaElem(el, "attribute");
			String targetNs =
				getSchemaRoot(el).getAttribute("targetNamespace");
			XMOccurrence attOcc = x.getOccurence();
			att.setAttribute("use",	attOcc.isRequired()?"required":"optional");
			String nsUri = x.getNSUri();
			if (nsUri != null && !nsUri.isEmpty()) {
				if (!targetNs.equals(nsUri)) {
					att.setAttribute("ref", x.getLocalName());
					att.setAttribute("xmlns", nsUri);
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
				} else {
					att.setAttribute("form", "qualified");
				}
			}
			att.setAttribute("name", x.getLocalName());
			GenParser parserInfo =
				GenParser.genParser((XMData) x, _genXdateOutFormat);
			if (parserInfo.getFixed() != null) {
				att.setAttribute("fixed", parserInfo.getFixed());
			} else if (parserInfo.getDefault() != null) {
				att.setAttribute("default", parserInfo.getDefault());
			}
			String typeName = genDeclaredName(parserInfo);
			if (parserInfo.getParser().getNamedParams().isEmpty()) {
				String parserName =
					SCHEMA_PFX + parserInfo.getParser().parserName();
				if (typeName == null) {
					att.setAttribute("type",parserName);
				} else {
					Element simpletp = findSchematype(el, typeName);
					if (simpletp == null) {
						addDocumentation(att, parserInfo.getInfo());
						simpletp =
							genSchemaElem(getSchemaRoot(el), "simpleType");
						addDocumentation(simpletp, parserInfo.getInfo());
						simpletp.setAttribute("name", typeName);
						Element restr = genRestrictionElement(simpletp);
						restr.setAttribute("base", parserName);
					}
					att.setAttribute("type", typeName);
//					addDocumentation(att, "See simpeType \"" + typeName + '"');
				}
			} else {
				if (typeName == null) {
					Element simpletp = genSchemaElem(att, "simpleType");
					genRestrictions(simpletp, parserInfo);
				} else {
					att.setAttribute("type", typeName);
//					addDocumentation(att, "See simpeType \"" + typeName + '"');
					Element simpletp = findSchematype(el, typeName);
					if (simpletp == null) {
						simpletp =
							genSchemaElem(getSchemaRoot(el), "simpleType");
						genRestrictions(simpletp, parserInfo);
						simpletp.setAttribute("name", typeName);
					}
				}
			}
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
			if (parts!=null&&parts.length>0&&!(s=parts[0].trim()).isEmpty()) {
				int ndx = s.indexOf('#');
				return ndx>=0 ? s.substring(0,ndx)+"_"+s.substring(ndx+1) : s;
			}
		}
		return null;
	}

	/** Find simpleType element with given name.
	 * @param el element where to find.
	 * @param name name of simpleType.
	 * @return simpleType element with given name or null.
	 */
	private Element findSchematype(final Element el, final String name) {
		Element root = getSchemaRoot(el);
		NodeList nl = KXmlUtils.getChildElementsNS(
			root, XMLConstants.W3C_XML_SCHEMA_NS_URI, "simpleType");
		for (int i=0; i < nl.getLength(); i++) {
			Element stype = (Element)nl.item(i);
			if (stype.getAttribute("name").equals(name)) {
				return stype;
			}
		}
		return null;
	}

	/** Get unique schemaType name in this schema element.
	 * @param el element where to the name must be unique.
	 * @param name the name to search.
	 * @return unique type name.
	 */
	private String createSchemaTypeName(final Element el, final String name) {
		if (name == null) {
			return null;
		}
		String typeName = name;
		if (findSchematype(el, name) == null) {
			return typeName;
		}
		int i = 1;
		String s;
		while (findSchematype(el, s = typeName + "_" + i) != null) {
			i++;
		}
		return s;
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
				throw new RuntimeException("Shema not found");
			}
		}
	}

	/** Create XML schema from X-definition model element.
	 * @param parent where add the model.
	 * @param xel X-definition model of element.
	 * @return created XML schema.
	 */
	private Element genElem(final Element parent, final XMElement xel) {
		Element schema = getSchemaRoot(parent);
		String targetNs = schema.getAttribute("targetNamespace");
		if (targetNs == null) {
			targetNs = "";
		}
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
				elem.setAttribute("xmlns", nsUri);
				elem.setAttribute("ref", xel.getLocalName());
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
				elem.setAttribute("ref", xel.getLocalName());
				setOccurrence(elem, xel);
				return elem;
			}
		}
		Element el = genSchemaElem(parent, "element");
		el.setAttribute("name", xel.getLocalName());
		if (!SCHEMA_QNAME.equals(new QName(parent.getNamespaceURI(),
			parent.getLocalName()))) { // skip if root element model
			setOccurrence(el, xel);
		}
		XMData[] attrs = xel.getAttrs();
		XMNode[] children = xel.getChildNodeModels();
		if (children.length == 1 && children[0].getKind() == XMNode.XMTEXT) {
			XMData x = (XMData) children[0];
			GenParser parserInfo = GenParser.genParser(x, _genXdateOutFormat);
			String typeName = genDeclaredName(parserInfo);
			Element simpleType;
			if (attrs.length == 0) {
				if (parserInfo.getFixed() != null) {
					el.setAttribute("fixed", parserInfo.getFixed());
				} else if (parserInfo.getDefault() != null) {
					el.setAttribute("default", parserInfo.getDefault());
				}
				if (typeName == null) {
					if (parserInfo.getParser().getNamedParams().isEmpty()) {
						el.setAttribute("type",
							SCHEMA_PFX + parserInfo.getParser().parserName());
					} else {
						simpleType = genSchemaElem(el, "simpleType");
						genRestrictions(simpleType, parserInfo);
					}
				} else {
					el.setAttribute("type", typeName);
					if (!targetNs.isEmpty()) {
						el.setAttribute("xmlns", targetNs);
					}
//					addDocumentation(el, "See simpleType \"" + typeName + '"');
					if (findSchematype(el, typeName) == null) {
						// named simpletype not exists, create it!
						simpleType = genSchemaElem(schema,"simpleType");
						simpleType.setAttribute("name", typeName);
						genRestrictions(simpleType, parserInfo);
					}
				}
			} else {
				Element complextp = genSchemaElem(el, "complexType");
				Element simplect;
				Element extension = null;
				if (typeName == null) {
					if (parserInfo.getParser().getNamedParams().isEmpty()
						&& (parserInfo.getInfo() == null
						|| parserInfo.getInfo().isEmpty())) {
						simplect = genSchemaElem(complextp, "simpleContent");
						extension = genSchemaElem(simplect, "extension");
						extension.setAttribute("base",
							SCHEMA_PFX + parserInfo.getParser().parserName());
					} else {
						typeName = createSchemaTypeName(el, xel.getLocalName());
						if (!targetNs.isEmpty()) {
							simplect = genSchemaElem(complextp,"simpleContent");
							extension = genSchemaElem(simplect, "extension");
							extension.setAttribute("xmlns", targetNs);
						}
					}
				}
				if (typeName != null && findSchematype(el, typeName) == null) {
					simpleType = genSchemaElem(schema,"simpleType");
					simpleType.setAttribute("name", typeName);
					genRestrictions(simpleType, parserInfo);
					simplect = genSchemaElem(complextp, "simpleContent");
					extension = genSchemaElem(simplect, "extension");
					extension.setAttribute("base", typeName);
					if (!targetNs.isEmpty()) {
						extension.setAttribute("xmlns", targetNs);
					}
				}
				if (extension != null) {
					addAttrs(extension, attrs);
				} else {
					addAttrs(complextp, attrs);
				}
			}
			return el;
		}
		Element complt = genSchemaElem(el, "complexType");
		if (children.length > 0) {
			XMNode x = children[0];
			if (x.getKind()==XMNode.XMMIXED
				&& ((XMSelector) x).getEndIndex()==children.length-1) {
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
					Element all = genSchemaElem(complt, "all");
					for (int i = 1; i < children.length; i++) {
						XMNode y = children[i];
						if (y.getKind() == XMNode.XMELEMENT) {
							genElem(all, (XMElement) y);
						}
					}
					addAttrs(complt, attrs);
					return el;
				}
			}
			switch (x.getKind()) {
				case XMNode.XMCHOICE:
				case XMNode.XMMIXED:
				case XMNode.XMSEQUENCE:
					if (((XMSelector) x).getEndIndex() == children.length - 1) {
						genGroup(complt,
							complt, (XMSelector) children[0], children, 0);
						addAttrs(complt, attrs);
						return el;
					}
			}
			Element seq = genSequenceElement(complt);
			genSequence(complt, seq, children, -1, children.length);
		}
		addAttrs(complt, attrs);
		return el;
	}

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
	public void addSchema(final String name, final Element schema) {
		if (_xsdSources.containsKey(name)) {
			throw new RuntimeException("Schema " + name + " already exists");
		}
		_xsdSources.put(name, schema);
	}

	/** Create new element with XML schema namespace and given name and add
	 * it to the parent node (if parent is not null).
	 * @param parent parent node.
	 * @param name local name of element to be created.
	 * @return created element.
	 */
	private Element genSchemaElem(final Element parent, final String name) {
		Element result = _doc.createElementNS(
			XMLConstants.W3C_XML_SCHEMA_NS_URI, SCHEMA_PFX + name);
		if (parent != null) {
			parent.appendChild(result);
		}
		return result;
	}

	/** Create new Schema element and put it to the map of schema.
	 * @param outName name of item in the map.
	 * @return created element.
	 */
	private Element genNewSchema(final String outName) {
		Element schema = genSchemaElem(null, "schema");
		schema.setAttribute("elementFormDefault", "qualified");
		schema.setAttribute("attributeFormDefault", "unqualified");
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
	 * @param xdName name of root X-definition.
	 * @param modelName name of root model.
	 * @param outName name of root XML schema file.
	 * @param genInfo switch if generate annotation with documentation.
	 * @param genXdateOutFormat if true, from the xdatetime method the outFormat
	 * parameter (the second sequential) is used as mask to validate datetime.
	 * @return map with names of XML schema files and corresponding Elements.
	 */
	public static Map<String, Element> genSchema(final XDPool xp,
		final String xdName,
		final String modelName,
		final String outName,
		final boolean genInfo,
		final boolean genXdateOutFormat) {
		String xname = xdName == null ? xp.getXMDefinitionNames()[0] : xdName;
		XMDefinition xdef = xp.getXMDefinition(xname);
		if (xdef == null) {
			throw new RuntimeException("can't find X-definition " + xname);
		}
		String mname = null;
		XMElement[] roots;
		if (modelName == null || modelName.isEmpty()) {
			roots = xp.getXMDefinition(xname).getRootModels();
			if (roots != null && roots.length > 0) {
				mname = roots[0].getLocalName();
			} else {
				throw new RuntimeException("No XML model specified");
			}
		} else {
			roots = xp.getXMDefinition(xname).getModels();
			String mURI = null;
			for (XMElement xel : roots) {
				if (modelName.equals(xel.getName())) {
					mname = xel.getLocalName();
					mURI = xel.getNSUri();
					break;
				}
			}
			if (mname == null) {
				throw new RuntimeException("Can't find model " + modelName);
			}
			XMDefinition xmdef = xp.getXMDefinition(xname);
			roots = new XMElement[1];
			roots[0] = xmdef.getModel(mURI, mname);
		}
		String oname = outName == null ? mname : outName;
		oname = oname.replace(':', '_');
		Xd2Xsd generator =  new Xd2Xsd(oname, genInfo, genXdateOutFormat);
		Element schema = generator.genNewSchema(oname);
		XMElement xmel = roots[0];
		String nsUri = xmel.getNSUri();
		if (nsUri != null && !nsUri.isEmpty()) {
			schema.setAttribute("targetNamespace", nsUri);
		}
		generator.genElem(schema, xmel);
		for (int i = 1; i < roots.length; i++) {
			generator.genElem(schema, roots[i]);
		}
		return generator._xsdSources;
	}
}