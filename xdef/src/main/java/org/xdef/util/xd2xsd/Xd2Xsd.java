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
import org.xdef.impl.XData;
import org.xdef.impl.XVariable;
import org.xdef.impl.parsers.XSParseList;
import org.xdef.impl.parsers.XSParseUnion;
import org.xdef.model.XMData;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import static org.xdef.model.XMNode.XMTEXT;
import org.xdef.model.XMOccurrence;
import org.xdef.model.XMSelector;
import org.xdef.model.XMVariable;
import org.xdef.msg.XDCONV;
import org.xdef.sys.SRuntimeException;
import org.xdef.xml.KXmlUtils;

/** Convertor of X-definition to XML Schema.
 * @author Vaclav Trojan
 */
public class Xd2Xsd {
	/** Prefix used for the XML schema namespace. */
	private static final String SCHEMA_PFX = "xs:";
	/** Target namespace of XML schema with declared user types. */
	private static final String USERTYPES_URI = "$_types_$";
	/** QName of schema element. */
	private static final QName SCHEMA_QNAME =
		new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "schema");
	/** Name of root XML schema file. */
	private final String _rootName;
	/** Name of XML schema file with user declared types. */
	private final String _typesName;
	/** Map of file names and XML schema elements.*/
	private final Map<String, Element> _xsdSources;
	/** Switch if generate annotation with documentation information. */
	private final boolean _genInfo;
	/**  Switch generate xdatatime outFormat. */
	private final boolean _genXdateOutFormat;
	/** org.w3c.dom.Document used for creation of nodes. */
	private final Document _doc;
	/** XML schema element with of user declared types.*/
	private final Element _types;

	/** Create new instance of XsdGenerator.
	 * @param xp XDPool with X-definitions.
	 * @param xsdName name or XML schema root file
	 * @param genInfo if true the annotations with documentation is generated.
	 * @param genXdateOutFormat if true, from the xdatetime method the outFormat
	 * parameter (the second sequential) is used as mask to validate datetime.
	 */
	private Xd2Xsd(final XDPool xp,
		final String xsdName,
		final boolean genInfo,
		final boolean genXdateOutFormat) {
		if (xsdName == null || xsdName.isEmpty()) {
			//The name of xsd file is missing
			throw new SRuntimeException(XDCONV.XDCONV204);
		}
		_rootName = xsdName;
		_genInfo = genInfo;
		_genXdateOutFormat = genXdateOutFormat;
		_xsdSources = new HashMap<>();
		_doc = KXmlUtils.newDocument();
		_types = genDeclaredTypes(xp);
		if (_types != null) {
			_typesName = xsdName;
			addSchema(_typesName, _types);
		} else {
			_typesName = null;
		}
	}

	private Element genDeclaredTypes(final XDPool xp) {
		XMVariable[] vars = xp.getVariableTable().toArray();
		Element types = genNewSchema();
		types.setAttribute("targetNamespace", USERTYPES_URI);
		types.setAttribute("xmlns", USERTYPES_URI);
		for (XMVariable v : vars) {
			if (v.getName().charAt(0) != '$') {
				XData xdata = new XData("$text", null, xp, XMTEXT);
				xdata._check = ((XVariable)v).getParseMethodAddr();
				GenParser parserInfo =
					GenParser.genParser( (XMData) xdata, _genXdateOutFormat);
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
								max = occ.maxOccurs() == Integer.MAX_VALUE
									|| max==Integer.MAX_VALUE
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
				//"&{0}" not implemented
				throw new SRuntimeException(XDCONV.XDCONV205, xsel);
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

	/** Get declared type name.
	 * @param parserInfo object with information about value type.
	 * @return declared type name or null;
	 */
	private static String genDeclaredName(final GenParser parserInfo) {
		String s = parserInfo.getDeclaredName();
		if (s != null) {
			String[] parts = s.split(";");
//System.out.println("=*=*=* " + parserInfo.getParser() + "/" + s);
			if (parts.length>0&&!(s=parts[0].trim()).isEmpty()) {
				int ndx = s.indexOf('#');
				return ndx>=0 ? s.substring(0,ndx)+"_"+s.substring(ndx+1) : s;
			}
		}
		return null;
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
					Element tpel = _types == null ? getSchemaRoot(el) : _types;
					Element simpletp = findSchematype(tpel, typeName);
					if (simpletp == null) {
						simpletp = genSchemaElem(tpel, "simpleType");
						addDocumentation(simpletp, parserInfo.getInfo());
						simpletp.setAttribute("name", typeName);
						Element restr = genRestrictionElement(simpletp);
						restr.setAttribute("base", parserName);
					}
					att.setAttribute("type", typeName);
				}
			} else {
				if (typeName == null) {
					Element simpletp = genSchemaElem(att, "simpleType");
					genRestrictions(simpletp, parserInfo);
				} else {
					att.setAttribute("type", typeName);
					Element tpel = _types == null ? getSchemaRoot(el) : _types;
					Element simpleType = findSchematype(tpel, typeName);
					if (simpleType == null) {
						simpleType = genSchemaElem(tpel, "simpleType");
						genRestrictions(simpleType, parserInfo);
						simpleType.setAttribute("name", typeName);
					}
				}
			}
		}
	}

	/** Find simpleType element with given name.
	 * @param el element where to find.
	 * @param name name of simpleType (may be null).
	 * @return simpleType element with given name or null.
	 */
	private Element findSchematype(final Element el, final String name) {
		if (name != null) {
			Element root = getSchemaRoot(el);
			NodeList nl = KXmlUtils.getChildElementsNS(
				root, XMLConstants.W3C_XML_SCHEMA_NS_URI, "simpleType");
			for (int i=0; i < nl.getLength(); i++) {
				Element stype = (Element)nl.item(i);
				if (stype.getAttribute("name").equals(name)) {
					return stype;
				}
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
				//XML shema not found
				throw new SRuntimeException(XDCONV.XDCONV206);
			}
		}
	}

	/** If data value is optional, generate xs:complexType, xs:simpleContent
	 * and xs:extension.
	 * @param elem element declaration.
	 * @param xData data value model.
	 * @param parserInfo created object.
	 * @return If data value is optional, generate union with given type
	 * and string with pattern xs:simpleContent and xs:extension with attribute
	 * "base". Return the element xs:extension or return null;
	 */
	private Element genOptionalTextType(final Element elem,
		final XMData xData,
		final GenParser parserInfo) {
		if (!xData.getOccurence().isOptional()
			|| parserInfo.getFixed()!=null || parserInfo.getDefault()!=null) {
			return null;
		}
		Element el1 = genSchemaElem(elem, "complexType");
		Element el2 = genSchemaElem(el1, "simpleContent");
		XDParser p = parserInfo.getParser();
		String info = "In the X-definition is declared optional text item";
		if (p.getDeclaredName() != null) {
			info += " (type: '" + p.getDeclaredName() + "')";
		}
		info += " as " + p.parserName();
		info += GenParser.displayParams(p.getNamedParams());
		addDocumentation(el2, info);
		String typeName = genDeclaredName(parserInfo);
		String typeName1 = "_" + elem.getAttribute("name").replace(':', '_');
		Element tpel = _types == null ? getSchemaRoot(elem) : _types;
		Element simpleType;
		if (typeName != null) {
			if (findSchematype(tpel, typeName) == null) {
				simpleType = genSchemaElem(tpel, "simpleType");
				simpleType.setAttribute("name", typeName);
				genRestrictions(simpleType, parserInfo);
				typeName1 = typeName + typeName1;
			}
		}
		Element dummy = genSchemaElem(tpel, "simpleType");
		dummy.setAttribute("name", typeName1);
		Element union = genSchemaElem(dummy, "union");
		Element simpleType2;
		Element restriction;
		simpleType2 = genSchemaElem(union, "simpleType");
		if (typeName != null) {
			restriction = genSchemaElem(simpleType2, "restriction");
			restriction.setAttribute("base", typeName);
		} else {
			genRestrictions(simpleType2, parserInfo);
		}
		simpleType2 =  genSchemaElem(union, "simpleType");
		restriction = genSchemaElem(simpleType2, "restriction");
		restriction.setAttribute("base", "xs:string");
		Element pattern = genSchemaElem(restriction, "pattern");
		pattern.setAttribute("value", "\\s*");
		tpel.removeChild(dummy);
		int i = 0;
		String s = typeName1;
		Element e;
		while ((e = findSchematype(tpel, s)) != null) {
			if (KXmlUtils.compareElements(e, dummy).errors()) {
				s = typeName1 + "_" + (++i);
				dummy.setAttribute("name", s);
			} else {
				break;
			}
		}
		if (e == null) {
			tpel.appendChild(dummy);
		}
		el2 = genSchemaElem(el2, "extension") ;
		el2.setAttribute("base", s);
		return el2;
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
			XMData xData = (XMData) children[0];
			GenParser parserInfo =
				GenParser.genParser(xData, _genXdateOutFormat);
			Element simpleType;
			if (attrs.length == 0) {
				Element extension = genOptionalTextType(el, xData, parserInfo);
				if (extension != null) {
					return el; // optional text item generated (no attributes)
				}
				if (parserInfo.getFixed() != null) {
					el.setAttribute("fixed", parserInfo.getFixed());
				} else if (parserInfo.getDefault() != null) {
					el.setAttribute("default", parserInfo.getDefault());
				}
				String typeName = genDeclaredName(parserInfo);
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
					Element tpel = _types == null ? schema : _types;
					simpleType = findSchematype(tpel, typeName);
					if (simpleType == null) {
						// named simpletype not exists, create it!
						simpleType = genSchemaElem(tpel,"simpleType");
						simpleType.setAttribute("name", typeName);
						genRestrictions(simpleType, parserInfo);
					}
				}
			} else { // attributes are there
				Element extension = genOptionalTextType(el, xData, parserInfo);
				if (extension == null) { // text item is NOT optional!
					Element complexType = genSchemaElem(el, "complexType");
					String typeName = genDeclaredName(parserInfo);
					Element simpleContent;
					if (typeName == null) {
						if (parserInfo.getParser().getNamedParams().isEmpty()
							&& (parserInfo.getInfo() == null
							|| parserInfo.getInfo().isEmpty())) {
							simpleContent =
								genSchemaElem(complexType, "simpleContent");
							extension = genSchemaElem(simpleContent,
								"extension");
							extension.setAttribute("base",
								SCHEMA_PFX+parserInfo.getParser().parserName());
						} else {
							typeName =
								createSchemaTypeName(el, xel.getLocalName());
							if (!targetNs.isEmpty()) {
								simpleContent =
									genSchemaElem(complexType,"simpleContent");
								extension =
									genSchemaElem(simpleContent,"extension");
								extension.setAttribute("xmlns", targetNs);
							}
						}
					}
					if (typeName != null) {
						Element tpel = _types==null ? schema : _types;
						if (findSchematype(tpel, typeName) == null) {
							simpleType = genSchemaElem(tpel, "simpleType");
							simpleType.setAttribute("name", typeName);
							genRestrictions(simpleType, parserInfo);
						}
						simpleContent =
							genSchemaElem(complexType, "simpleContent");
						extension = genSchemaElem(simpleContent, "extension");
						extension.setAttribute("base", typeName);
						if (!targetNs.isEmpty()) {
							extension.setAttribute("xmlns", targetNs);
						}
					}
				}
				addAttrs(extension, attrs); // add declaration of attributes.
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
	public final void addSchema(final String name, final Element schema) {
		if (_xsdSources.containsKey(name)) {
			//XML schema "&{0}" already exists
			throw new SRuntimeException(XDCONV.XDCONV207, name);
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
	 * @param xdName name of root X-definition.
	 * @param modelName name of the root model. May be null, then all values
	 * from "xs:root" parameter are used to create models. If modelName is
	 * "?type", only the file with declared simple types is generated..
	 * @param outName name of root XML schema file.
	 * @param outType name of XML schema file with type declarations. May be
	 * null, then local name of X-definition model is used.
	 * @param genInfo switch if generate annotation with documentation.
	 * @param genXdateOutFormat if true, from the xdatetime method the outFormat
	 * parameter (the second sequential) is used as mask to validate datetime.
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
			//Can't find an X-definition&{0}{ "}{"}
			throw new SRuntimeException(XDCONV.XDCONV201, xname);
		}
		String mname;
		XMElement[] roots = null;
		if (modelName == null || modelName.isEmpty()) {
			roots = xp.getXMDefinition(xname).getRootModels();
			if (roots != null && roots.length > 0) {
				mname = roots[0].getLocalName();
			} else {
				//XML model not specified
				throw new SRuntimeException(XDCONV.XDCONV202);
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
				//Can't find root model&{0}{ "}{"}
				throw new SRuntimeException(XDCONV.XDCONV203, modelName);
			}
			XMDefinition xmdef = xp.getXMDefinition(xname);
			roots = new XMElement[1];
			roots[0] = xmdef.getModel(mURI, mname);
		}
		String oname = outName == null ? mname.replace(':', '_') : outName;
		String otype = outType;
		if (otype != null && !otype.isEmpty()) {
			if ((modelName == null || modelName.isEmpty())
				&& (outName == null || outName.isEmpty())) {
				roots = null;
			}
		} else {
			otype = oname + "$types";
		}
		Xd2Xsd generator = new Xd2Xsd(xp, otype, genInfo, genXdateOutFormat);
		if (roots != null) {
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
		}
		return generator._xsdSources;
	}
}