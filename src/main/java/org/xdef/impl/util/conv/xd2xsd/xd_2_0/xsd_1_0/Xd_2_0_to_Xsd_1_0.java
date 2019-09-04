package org.xdef.impl.util.conv.xd2xsd.xd_2_0.xsd_1_0;

import org.xdef.sys.SReporter;
import org.xdef.impl.util.gencollection.XDParsedScript;
import org.xdef.impl.util.conv.Util;
import org.xdef.impl.util.conv.type.XdefValueTypeParser;
import org.xdef.impl.util.conv.type.XdefValueTypeResolver;
import org.xdef.impl.util.conv.type.domain.Other;
import org.xdef.impl.util.conv.type.domain.ValueType;
import org.xdef.impl.util.conv.type.domain.XsdList;
import org.xdef.impl.util.conv.type.domain.XsdUnion;
import org.xdef.impl.util.conv.xd2xsd.Convertor;
import org.xdef.impl.util.conv.xd.doc.XdDoc_2_0;
import org.xdef.impl.util.conv.xd.xd_2_0.domain.XdDecl;
import org.xdef.impl.util.conv.xd.xd_2_0.domain.XdElem;
import org.xdef.impl.util.conv.xd.xd_2_0.domain.XdGroup;
import org.xdef.impl.util.conv.xd.xd_2_0.domain.XdModel;
import org.xdef.impl.util.conv.xd.xd_2_0.XdNames;
import org.xdef.impl.util.conv.xd.xd_2_0.XdUtils;
import org.xdef.impl.util.conv.xd.xd_2_0.XdUtils.ElemProps;
import org.xdef.impl.util.conv.xd.xd_2_0.XdUtils.Occurrence;
import org.xdef.impl.util.conv.xsd.doc.XsdDoc_1_0;
import org.xdef.impl.util.conv.xsd.xsd_1_0.domain.XsdCType;
import org.xdef.impl.util.conv.xsd.xsd_1_0.domain.XsdModel;
import org.xdef.impl.util.conv.xsd.xsd_1_0.domain.XsdSchema;
import org.xdef.impl.util.conv.xsd.xsd_1_0.XsdUtils;
import org.xdef.impl.util.gencollection.XDGenCollection;
import org.xdef.xml.KXmlUtils;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

/** Represents implementation of convertor that converts X-definition 2.0 to
 * XML Schema 1.0.
 * @author Ilia Alexandrov
 */
public class Xd_2_0_to_Xsd_1_0 extends Convertor {
	/** X-definition version 2.0 document representation.*/
	private final XdDoc_2_0 _xdDoc;
	/** XML Schema version 1.0 document representation. */
	private final XsdDoc_1_0 _xsdDoc;
	/** X-definition model (XdModel) to schema model (XsdModel) mapping. */
	private final Map<XdModel, XsdModel> _models;
	/** Actually processed X-definition model (XdModel) stack.*/
	private final Stack<XdModel> _procModel = new Stack<XdModel>();
	/** Type resolver. */
	private final XdefValueTypeResolver _typeResolver;

	/** Creates instance of X-definition version 2.0 to XML Schema version 1.0.
	 * @param xdDoc X-definition document representation.
	 * @param reporter reporter for reporting warnings and errors.
	 * @param schemaPrefix prefix for schema nodes.
	 * @param schemaFileExt file extension for schema files.
	 */
	public Xd_2_0_to_Xsd_1_0(final XdDoc_2_0 xdDoc,
		final SReporter reporter,
		final String schemaPrefix,
		final String schemaFileExt) {
		super(reporter);
		if (xdDoc == null) {
			throw new NullPointerException("X-definition document is null");
		}
		_xdDoc = xdDoc;
		_xsdDoc = new XsdDoc_1_0(reporter, schemaFileExt, schemaPrefix);
		_models = _xsdDoc.init(xdDoc);
		_typeResolver = new XdefValueTypeResolver(_xdDoc, _xsdDoc, _models);
		process();
	}

	/** Processing all models in models map. */
	private void process() {
		Iterator<XdModel> it = _models.keySet().iterator();
		while (it.hasNext()) {
			XdModel xdModel = it.next();
			processModel(xdModel);
		}
	}

	/** Processes given X-definition model representation.
	 * @param xdModel X-definition model representation to process.
	 */
	private void processModel(final XdModel xdModel) {
		if (_procModel.contains(xdModel)) {
			//TODO: Handle the recursive processing...
			throw new IllegalArgumentException(
				"Given X-definition model is already being processed");
		}
		_procModel.push(xdModel);

		switch (xdModel.getType()) {
			case XdModel.Type.DECLARATION:
				_typeResolver.resolveXdDecl((XdDecl) xdModel);
				break;
			case XdModel.Type.GROUP: {
				XdGroup xdGroup = (XdGroup) xdModel;
				Element xdGroupElem = _xdDoc.getXdModels().get(xdGroup);
				XsdModel xsdModel = _models.get(xdModel);
				Element xsdGroupElem = _xsdDoc.getModels().get(xsdModel);
				switch (xdGroup.getGroupType()) {
					case XdGroup.GroupType.CHOICE:
						processChoice(xdGroupElem, xsdGroupElem);
						break;
					case XdGroup.GroupType.MIXED:
						processMixed(xdGroupElem, xsdGroupElem);
						break;
					case XdGroup.GroupType.SEQUENCE:
						processSequence(xdGroupElem, xsdGroupElem);
						break;
					default:
						throw new RuntimeException("Unknown X-definition group");
				}
			}
			break;
			case XdModel.Type.ELEMENT: {
				Element elemElem = _xdDoc.getXdModels().get(xdModel);
				XsdCType xsdCType = (XsdCType) _models.get(xdModel);
				Element schema = _xsdDoc.getSchemas().get(xsdCType.getSchema());
				//element is root
				if (XdUtils.isRoot(elemElem)) {
					XdElem xdElem = (XdElem) xdModel;
					XdUtils.ElemProps elemProps =XdUtils.getElemProps(elemElem);
					//add element declaration
					_xsdDoc.addElementDecl(schema, elemProps.getDefault(),
						elemProps.getFixed(),
						null, null,
						xdElem.getName(), null,
						elemProps.isNillable(), _xsdDoc.getQName(xsdCType),
						null);
				}
				processElementContent(elemElem, schema);
			}
			break;
			default:
				throw new RuntimeException("Unknown X-definition model type");
		}
		//set model as processed
		xdModel.setProcessed();
		_procModel.pop();
	}

	/** Processes given X-definition <tt>any</tt> declaration element and adds
	 * declaration to given schema context element.
	 * @param anyElem X-definition <tt>any</tt> declaration element.
	 * @param schemaContext schema context element to add declaration to.
	 */
	private void processAny(final Element anyElem, Element schemaContext) {
		Occurrence occurrence = XdUtils.getOccurrence(anyElem);
		_xsdDoc.addAnyDecl(schemaContext,
			occurrence.getMinOccurs(), Occurrence.UNBOUNDED);
	}

	/** Processes given X-definition <tt>attr</tt> declaration attribute and adds
	 * declaration to given schema context element.
	 * @param attr X-definition <tt>attr</tt> declaration attribute to process.
	 * @param schemaContext schema context element to add declaration to.
	 */
	private void processAttr(final Attr attr, Element schemaContext) {
		//add any attribute declaration
		_xsdDoc.addAnyAttrDecl(schemaContext);
	}

	/** Processes given attribute node and adds declaration to given schema
	 * context element.
	 * @param attr attribute node to process.
	 * @param schemaContext schema context element to add declaration to.
	 */
	private void processAttribute(final Attr attr,
		final Element schemaContext) {
		//getschema target namespace
		String targetNS = XsdUtils.getTargetNS(schemaContext);
		//get atttribute local name
		String name = Util.getAttrLocalName(attr);
		//get attribute namespace
		String namespace = attr.getNamespaceURI();
		// name of X-definition
		String xdname = XDGenCollection.getXDName(attr.getOwnerElement());
		//get attribute properties
		XdUtils.AttrProps attrProps = XdUtils.getAttrProps(attr);
		//schema has no target namespace
		if (targetNS == null || targetNS.length() == 0) {
			//attribute has no namespace
			if (namespace == null || namespace.length() == 0) {
				//add attribute declaration
				Element attrDecl = _xsdDoc.addAttributeDecl(schemaContext,
					attrProps.getDefault(), attrProps.getFixed(), name,
					xdname, null, null, attrProps.getUse(), null);
				String type = attrProps.getType();
/*VT3*/
				if ("int()".equals(type)) { // int() without parameters
					String fixed = attrProps.getFixed();
					if (fixed == null) {
						type = "long()";
					} else {
						long l = Long.parseLong(fixed);
						if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
							type = "long()";
						}
					}
				}
/*VT3*/
				_typeResolver.resolveAttrType(type, xdname, attrDecl);
			} else {
				//process external namespace attribute
				processExtNSAttribute(schemaContext,
					attrProps, namespace, name, xdname);
			}
		} else {
			//attribute has no namespace
			if (namespace == null || namespace.length() == 0) {
				//add attribute declaration
				Element attrDecl = _xsdDoc.addAttributeDecl(schemaContext,
					attrProps.getDefault(), attrProps.getFixed(),
					name, xdname, null, null, attrProps.getUse(), null);
				//TODO: resolve type
				//TypeResolver.addAttrType(attrDecl, attrProps.getType(),
				//  _xsdDoc, _xdDoc, _models);
				_typeResolver.resolveAttrType(attrProps.getType(),
					xdname, attrDecl);
			} else {
				//attribute namespace is same as schema target namespace
				if (namespace.equals(targetNS)) {
					//add qualified attribute declaration
					Element attrDecl = _xsdDoc.addAttributeDecl(schemaContext,
						attrProps.getDefault(), attrProps.getFixed(), name,
						xdname, null, null, attrProps.getUse(), true);
					//TODO: resolve type
					_typeResolver.resolveAttrType(attrProps.getType(),
						xdname, attrDecl);
				} else {
					//process external namespace attribute
					processExtNSAttribute(schemaContext,
						attrProps, namespace, name, xdname);
				}
			}
		}
	}

	/** Processes given external attribute and adds declaration to given schema
	 * context element.
	 * @param schemaContext schema context to add declaration to.
	 * @param attrProps external attribute properties.
	 * @param attrNS external attribute namespace.
	 * @param attrName external attribute local name.
	 * @param xdname name of X-definition where attribute model is declared.
	 */
	private void processExtNSAttribute(final Element schemaContext,
			final XdUtils.AttrProps attrProps,
			final String attrNS,
			final String attrName,
			final String xdname) {
		//get external schema with given namespace
		XsdSchema extSchema = _xsdDoc.getExtNSSchema(attrNS);
		Element extSchemaElem = _xsdDoc.getSchemas().get(extSchema);
		//external schema already contains attriobute model
		if (XsdUtils.hasAttributeDecl(extSchemaElem, attrName)) {
			//create external schema
			extSchema = _xsdDoc.getExtSchema(attrNS);
			extSchemaElem = _xsdDoc.getSchemas().get(extSchema);
			//create attribute declaration
			Element attrElem = _xsdDoc.addAttributeDecl(extSchemaElem,
				attrProps.getDefault(), attrProps.getFixed(), attrName, xdname,
				null, null, null, null);
			//TODO: resolve type
			//TypeResolver.addAttrType(
			//	attrElem, attrProps.getType(), _xsdDoc, _xdDoc, _models);
			_typeResolver.resolveAttrType(attrProps.getType(), xdname,attrElem);
			XsdSchema extAttrGrpSchema = _xsdDoc.getExtSchema();
			Element extAttrGrpSchemaElem =
				_xsdDoc.getSchemas().get(extAttrGrpSchema);
			//create attribute group name
			String attrGrpName =
				XsdUtils.getExtAttrGrpName(_xsdDoc.getExtAttrGrpCounter());
			//create attribute group element
			Element attrGrpElem = _xsdDoc.addAttrGroupDecl(
				extAttrGrpSchemaElem, attrGrpName, null);
			//get attribute ref name
			String attrRef =
				_xsdDoc.getQName(extAttrGrpSchemaElem, extSchema, attrName);
			//add attribute ref
			_xsdDoc.addAttributeDecl(attrGrpElem, null, null, null,
				attrRef, xdname, null, attrProps.getUse(), null);
			//get attribute group ref name
			String attrGrpRef = _xsdDoc.getQName(extSchemaElem,
				extAttrGrpSchema, attrGrpName);
			//add attribute group ref
			_xsdDoc.addAttrGroupDecl(schemaContext, null, attrGrpRef);
		} else {
			//add attribute decl
			Element attrElem = _xsdDoc.addAttributeDecl(extSchemaElem,
				attrProps.getDefault(), attrProps.getFixed(), attrName,
				xdname, null, null, null, null);
			//TODO: resolve type
			_typeResolver.resolveAttrType(attrProps.getType(), xdname,attrElem);
			//get attr ref string
			String attrRef = _xsdDoc.getQName(
				XsdUtils.getAncestorSchema(schemaContext), extSchema, attrName);
			//add attribute ref
			_xsdDoc.addAttributeDecl(schemaContext, null, null, null,
				xdname, attrRef, null, attrProps.getUse(), null);
		}
	}

	/** Processes attribute nodes of given element and adds declarations to
	 * given schema context element.
	 * @param element element to process attribute nodes from.
	 * @param schemaContext schema context element to add declarations to.
	 */
	private void processAttributes(final Element element,
		final Element schemaContext){
		//get attribute nodes
		String xdNS = XDGenCollection.findXDNS(element);
		if (xdNS == null) {
			throw new RuntimeException("X-definifion namespace not found!");
		}
/*VT*/
		NamedNodeMap attrs = element.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			//get attribute
			Attr attr = (Attr) attrs.item(i);
			if (!xdNS.equals(attr.getNamespaceURI())) {
				//attribute is not X-definition attribute, process attribute
				processAttribute(attr, schemaContext);
			} else {
				String attrName = Util.getAttrLocalName(attr);
				//any attribute declaration
				if (XdNames.ATTR.equals(attrName)) {
					processAttr(attr, schemaContext);
				} else {
					//TODO:
				}
			}
		}
	}

	/** Processes children nodes of given element and adds declarations to
	 * given schema context element.
	 * @param element element to process children.
	 * @param schemaContext schema context element to add declarations to.
	 */
	private void processChildren(final Element element,
		final Element schemaContext) {
		//get child elements
		NodeList children = KXmlUtils.getChildElements(element);
		String xdNS = XDGenCollection.findXDNS(element);
		if (xdNS == null) {
			throw new RuntimeException("X-definifion namespace not found!");
		}
		for (int i = 0; i < children.getLength(); i++) {
			//get child
			Element child = (Element) children.item(i);
			//is X-definition element
			if (xdNS.equals(child.getNamespaceURI())) {
				if (XdNames.CHOICE.equals(child.getLocalName())) {
					//process choice
					processChoice(child, schemaContext);
				} else if (XdNames.MIXED.equals(child.getLocalName())) {
					//process mixed
					processMixed(child, schemaContext);
				} else if (XdNames.SEQUENCE.equals(child.getLocalName())) {
					//process sequence
					processSequence(child, schemaContext);
				} else if (XdNames.ANY.equals(child.getLocalName())) {
					//process any declaration
					processAny(child, schemaContext);
				} else {
					//TODO:
				}
			} else {
				//process element
				processElement(child, schemaContext);
			}
		}
	}

	/** Processes given X-definition <tt>choice</tt> declaration element and
	 * adds declaration to given schema context element.
	 * @param choice X-definition <tt>choice</tt> declaration element to process.
	 * @param schemaContext schema context element to add declaration to.
	 */
	private void processChoice(final Element choice,
		final Element schemaContext) {
		Occurrence choiceOcc = XdUtils.getOccurrence(choice);
		//resolve ref
		Element xsdChoiceElem = _xsdDoc.addChoiceDecl(
			schemaContext, choiceOcc.getMinOccurs(), choiceOcc.getMaxOccurs());
		processChildren(choice, xsdChoiceElem);
	}

	/** Processes given X-definition element declaration and adds declaration to
	 * given schema context element.
	 * @param element X-definition element declaration element to process.
	 * @param schemaContext schema context element to add declaration to.
	 */
	private void processElement(final Element element,
		final Element schemaContext) {
		//get schema target namespace
		String targetNS = XsdUtils.getTargetNS(schemaContext);
		//get element namespace
		String elemNS = element.getNamespaceURI();
		//get element local name
		String elemName = element.getLocalName();
		//get element properties
		ElemProps elemProps = XdUtils.getElemProps(element);
		//schema target namesapce is empty
		if (targetNS == null || targetNS.length() == 0) {
			//element namespace is empty
			if (elemNS == null || elemNS.length() == 0) {
				//add element declaration
				Element elemDecl = _xsdDoc.addElementDecl(schemaContext,
					elemProps.getDefault(), elemProps.getFixed(),
					elemProps.getMinOccurs(), elemProps.getMaxOccurs(),
					elemName, null, elemProps.isNillable(), null, null);
				processElementContent(element, elemDecl);
			} else {
				//process external namespace element
				processExtNSElement(element, schemaContext, elemProps, elemNS,
						elemName);
			}
		} else {
			//element namespace is empty
			if (elemNS == null || elemNS.length() == 0) {
				//add element declaration
				Element elemDecl = _xsdDoc.addElementDecl(schemaContext,
					elemProps.getDefault(), elemProps.getFixed(),
					elemProps.getMinOccurs(), elemProps.getMaxOccurs(),
					elemName, null, elemProps.isNillable(), null, null);
				processElementContent(element, elemDecl);
			} else {
				//target namespace is same as element namespace
				if (targetNS.equals(elemNS)) {
					//add qualified element declaration
					Element elemDecl = _xsdDoc.addElementDecl(schemaContext,
						elemProps.getDefault(), elemProps.getFixed(),
						elemProps.getMinOccurs(), elemProps.getMaxOccurs(),
						elemName, null, elemProps.isNillable(), null, true);
					processElementContent(element, elemDecl);
				} else {
					//process external namespace element
					processExtNSElement(
						element, schemaContext, elemProps, elemNS, elemName);
				}
			}
		}
	}

	/** Processes given external element and adds proper declaration to given
	 * schema context element.
	 * @param element external element to process.
	 * @param schemaContext schema context element to add declaration to.
	 * @param elemProps external element properties.
	 * @param elemNS external element name space URI.
	 * @param elemName external element local name.
	 */
	private void processExtNSElement(final Element element,
		final Element schemaContext,
		final ElemProps elemProps,
		final String elemNS,
		final String elemName) {
		//get external schema with element namespace
		XsdSchema extNSSchema = _xsdDoc.getExtNSSchema(elemNS);
		Element extNSSchemaElem = _xsdDoc.getSchemas().get(extNSSchema);
		//external schema contains element with given name
		if (XsdUtils.hasElementDecl(extNSSchemaElem, elemName)) {
			//create external schema
			XsdSchema extSchema = _xsdDoc.getExtSchema(elemNS);
			Element extSchemaElem = _xsdDoc.getSchemas().get(extSchema);
			//add element declaration
			Element elemDecl = _xsdDoc.addElementDecl(extSchemaElem,
				elemProps.getDefault(), elemProps.getFixed(),
				null, null, elemName, null, elemProps.isNillable(), null, null);
			//process element
			processElementContent(element, elemDecl);
			//create group schema
			XsdSchema groupSchema = _xsdDoc.getExtSchema();
			Element groupSchemaElem = _xsdDoc.getSchemas().get(groupSchema);
			//create group name
			String groupName =
				XsdUtils.getExtGroupName(_xsdDoc.getExtGroupCounter());
			//add group element
			Element groupDecl = _xsdDoc.addGroupDecl(
				groupSchemaElem, groupName, null, null, null);
			//add sequence elem
			Element seqDecl = _xsdDoc.addSequenceDecl(groupDecl, null, null);
			//get element ref
			String elemRef = _xsdDoc.getQName(
				groupSchemaElem, extSchema, elemName);
			//add element ref
			_xsdDoc.addElementDecl(seqDecl, null, null,
				elemProps.getMinOccurs(), elemProps.getMaxOccurs(),
				null, elemRef, null, null, null);
			//create group ref
			String groupRef = _xsdDoc.getQName(
				XsdUtils.getAncestorSchema(schemaContext), groupSchema,
				groupName);
			//add group ref
			_xsdDoc.addGroupDecl(schemaContext, null, groupRef, null, null);
		} else {
			//add element to external schema
			Element elemDecl = _xsdDoc.addElementDecl(extNSSchemaElem,
				elemProps.getDefault(), elemProps.getFixed(),
				null, null, elemName, null, elemProps.isNillable(), null, null);
			//process element content
			processElementContent(element, elemDecl);
			//get element ref string
			String elemRef = _xsdDoc.getQName(
				XsdUtils.getAncestorSchema(schemaContext),
				extNSSchema,
				elemName);
			//add element ref
			_xsdDoc.addElementDecl(schemaContext, null, null,
				elemProps.getMinOccurs(), elemProps.getMaxOccurs(),
				null, elemRef, null, null, null);
		}
	}

	/** Processes element content of given X-definition element declaration and
	 * adds declaration to given schema context element.
	 *
	 * @param element element to process element content.
	 * @param schemaContext schema context element to add declaration to.
	 */
	private void processElementContent(final Element element,
		final Element schemaContext) {
		//get element content type
		int elemType = XdUtils.getElemType(element);
		//get ref
		XdElem refXdElem = XdUtils.getRefXdElem(element);
		//no refference
		if (refXdElem == null) {
			switch (elemType) {
				//element type is empty
				case XdElem.ElemType.EMPTY: {
					if (XdUtils.isModel(element)) {
						//do nothing
					} else {
						//add complex type
						_xsdDoc.addComplexTypeDecl(schemaContext, null, null);
					}
					break;
				}
				//element type is text only
				case XdElem.ElemType.TEXT: {
					//TODO: parse text value
					ValueType elemSType = getElementSimpleType(element);
					if (XdUtils.isModel(element)) {
						XsdCType xsdCType = (XsdCType) _models.get(
							XdUtils.createXdModel(element));
						if (xsdCType == null) {
							break;
						}
						Element cTypeElem = _xsdDoc.getModels().get(xsdCType);
						String sTypeName;
						if ((sTypeName = xsdCType.getSType()) != null) {
							sTypeName = XsdUtils.getRefQName(
								_xsdDoc.getSchemas().get(xsdCType.getSchema()),
								sTypeName).getQName();
						} else {
							if (ValueType.OTHER == elemSType.getKind()
								&& ((Other) elemSType).isSimple()) {
								Other otherType = (Other) elemSType;
								XdDecl xdDecl;
								String typename = otherType.getName();
								String xdname = otherType.getXdefName();
								if (otherType.isSimple() && xdname != null) {
									xdDecl = _xdDoc.getXdDecl('_' + xdname
										+ '.' + typename);
									if (xdDecl == null) {
										xdDecl = _xdDoc.getXdDecl(typename);
									}
								} else {
									xdDecl = _xdDoc.getXdDecl(typename);
								}
								XsdModel xsdModel = _models.get(xdDecl);
								Element schemaElem =
									XsdUtils.getAncestorSchema(schemaContext);
								sTypeName = _xsdDoc.getQName(schemaElem,
									xsdModel.getSchema(),xsdModel.getName());
							} else {
								//create simple type name
								sTypeName = XsdUtils.getModelSimpleTypeName(
									_procModel.peek().getDef().getName(),
									element.getLocalName());
								//create simple type element
								Element schemaElem = _xsdDoc.getSchemas().get(
									xsdCType.getSchema());
								//TypeResolver.addSimpleType(schemaElem,
								//  sTypeName,elemSType,_xsdDoc,_xdDoc,_models);
								_typeResolver.createSimpleType(
									elemSType, sTypeName, schemaElem);
								//add to model
								xsdCType.setSType(sTypeName);
								sTypeName = XsdUtils.getRefQName(
									schemaElem, sTypeName).getQName();
							}
						}
						Element simpleContent =
							_xsdDoc.addSimpleContentDecl(cTypeElem);
						_xsdDoc.addExtensionDecl(simpleContent, sTypeName);
					} else {
						//TypeResolver.addElemType(schemaContext, elemSType,
						//  _xsdDoc, _xdDoc, _models);
						_typeResolver.resolveElemType(elemSType, schemaContext);
					}
					break;
				}
				//element type is attributes only
				case XdElem.ElemType.ATTR: {
					Element cTypeElem;
					if (XdUtils.isModel(element)) {
						cTypeElem = getXdElemCType(element);
					} else {
						//add complex type element
						cTypeElem = _xsdDoc.addComplexTypeDecl(schemaContext,
							null, null);
					}
					processAttributes(element, cTypeElem);
					break;
				}
				//element type is children only
				case XdElem.ElemType.CHLD: {
					Element cTypeElem;
					if (XdUtils.isModel(element)) {
						cTypeElem = getXdElemCType(element);
					} else {
						//add complex type element
						cTypeElem = _xsdDoc.addComplexTypeDecl(schemaContext,
							null, null);
					}
					//add sequence
					Element seqElem =
						_xsdDoc.addSequenceDecl(cTypeElem, null, null);
					//process children
					processChildren(element, seqElem);
					break;
				}
				//element type is text and attributes
				case XdElem.ElemType.TEXT_ATTR: {
					//TODO: resolve type and add simple content
					Element cTypeElem;
					String sTypeName;
					ValueType elemSType = getElementSimpleType(element);
					if (XdUtils.isModel(element)) {
						XsdCType xsdCType = (XsdCType) _models.get(
							XdUtils.createXdModel(element));
						if (xsdCType == null) {
							break;
						}
						cTypeElem = _xsdDoc.getModels().get(xsdCType);
						if ((sTypeName = xsdCType.getSType()) != null) {
							sTypeName = XsdUtils.getRefQName(
								_xsdDoc.getSchemas().get(xsdCType.getSchema()),
								sTypeName).getQName();
						} else {
							//create simple type name
							sTypeName = XsdUtils.getModelSimpleTypeName(
								_procModel.peek().getDef().getName(),
								element.getLocalName());
							//create simple type element
							Element schemaElem =
								_xsdDoc.getSchemas().get(xsdCType.getSchema());
							//TypeResolver.addSimpleType(schemaElem, sTypeName,
							//   elemSType, _xsdDoc, _xdDoc, _models);
							_typeResolver.createSimpleType(elemSType,
								sTypeName, schemaElem);
							//add to model
							xsdCType.setSType(sTypeName);
							sTypeName = XsdUtils.getRefQName(schemaElem,
								sTypeName).getQName();
						}
					} else {
						cTypeElem = _xsdDoc.addComplexTypeDecl(
							schemaContext, null, null);
						sTypeName = XsdUtils.getExtSTypeName(
							element.getLocalName(),
							_xsdDoc.getExtSTypeCounter());
						Element schemaElem =
							XsdUtils.getAncestorSchema(schemaContext);
						//TypeResolver.addSimpleType(schemaElem, sTypeName,
						//	elemSType, _xsdDoc, _xdDoc, _models);
						_typeResolver.createSimpleType(elemSType,
							sTypeName, schemaElem);
						sTypeName = XsdUtils.getRefQName(schemaElem,
							sTypeName).getQName();
					}
					Element simpleContent =
						_xsdDoc.addSimpleContentDecl(cTypeElem);
					Element extension =
						_xsdDoc.addExtensionDecl(simpleContent, sTypeName);
					//add attributes
					processAttributes(element, extension);
					break;
				}
				//element type is attributes and children
				case XdElem.ElemType.ATTR_CHLD: {
					Element cTypeElem;
					if (XdUtils.isModel(element)) {
						cTypeElem = getXdElemCType(element);
					} else {
						//add complex type element
						cTypeElem =
							_xsdDoc.addComplexTypeDecl(schemaContext,null,null);
					}
					//process attributes
					processAttributes(element, cTypeElem);
					//add sequence
					Element seqElem =
						_xsdDoc.addSequenceDecl(cTypeElem, null,null);
					//process children
					processChildren(element, seqElem);
					break;
				}
				//element type is text and children
				case XdElem.ElemType.TEXT_CHLD: {
					Element cTypeElem;
					if (XdUtils.isModel(element)) {
						cTypeElem = getXdElemCType(element);
						//set mixed to true
						_xsdDoc.setMixed(cTypeElem, true);
					} else {
						//add complex type element
						cTypeElem =
							_xsdDoc.addComplexTypeDecl(schemaContext,null,true);
					}
					//add sequence
					Element seqElem =
						_xsdDoc.addSequenceDecl(cTypeElem, null, null);
					//process children
					processChildren(element, seqElem);
					break;
				}
				//element type is text, attributes and children
				case XdElem.ElemType.TEXT_ATTR_CHLD: {
					Element cTypeElem;
					if (XdUtils.isModel(element)) {
						cTypeElem = getXdElemCType(element);
						//set mixed to true
						_xsdDoc.setMixed(cTypeElem, true);
					} else {
						//add complex type element
						cTypeElem =
							_xsdDoc.addComplexTypeDecl(schemaContext,null,true);
					}
					//process attributes
					processAttributes(element, cTypeElem);
					//add sequence
					Element seqElem =
						_xsdDoc.addSequenceDecl(cTypeElem, null, null);
					//process children
					processChildren(element, seqElem);
					break;
				}
				default: {
					throw new RuntimeException("Given element type is unknown");
				}
			}
		} else {
			if (elemType == XdElem.ElemType.EMPTY &&
				!XdUtils.isModel(element)
			) {
				//set type attribute
				setElementCType(schemaContext, refXdElem);
			} else {
				//set as extension type
				extendElement(element, schemaContext, refXdElem);
			}
		}
	}

	/** Extend Element.
	 * @param element
	 * @param schemaContext
	 * @param refXdElem
	 * @param procAttrs
	 * @param procChld
	 */
	private void extendElement(final Element element,
		final Element schemaContext,
		final XdElem refXdElem) {
		Element cTypeElem;
		if (XdUtils.isModel(element)) {
			cTypeElem = getXdElemCType(element);
		} else {
			cTypeElem = _xsdDoc.addComplexTypeDecl(schemaContext, null, null);
		}

		int     elemType  = XdUtils.getElemType(element);
		boolean procAttrs = false;
		boolean procChld  = false;
		//TODO: mixed is unused and set to false only for now
		boolean mixed     = false;

		switch (elemType) {
			case XdElem.ElemType.ATTR:
			case XdElem.ElemType.TEXT_ATTR:
			case XdElem.ElemType.ATTR_CHLD:
			case XdElem.ElemType.TEXT_ATTR_CHLD:
				procAttrs = true;
				break;
		}

		switch (elemType) {
			case XdElem.ElemType.CHLD:
			case XdElem.ElemType.ATTR_CHLD:
			case XdElem.ElemType.TEXT_CHLD:
			case XdElem.ElemType.TEXT_ATTR_CHLD:
				procChld = true;
				break;
		}

		Element extension = addComplexContExtension(cTypeElem, refXdElem, mixed);
		if (procAttrs) {
			processAttributes(element, extension);
		}
		if (procChld) {
			Element seqElem = _xsdDoc.addSequenceDecl(extension, null, null);
			processChildren(element, seqElem);
		}
	}

	/** Returns schema <tt>complexType</tt> element mapped to given X-definition
	 * model element.
	 * @param xdModelElem X-definition model element.
	 * @return schema <tt>complexType</tt> element.
	 */
	private Element getXdElemCType(final Element xdModelElem) {
		XdModel xdModel;
		try {
			xdModel = XdUtils.createXdModel(xdModelElem);
		} catch (Exception ex) {
			throw new RuntimeException(
				"Could not get model complex type element", ex);
		}
		XsdModel xsdModel = _models.get(xdModel);
		XsdCType xsdCType;
		try {
			xsdCType = (XsdCType) xsdModel;
		} catch (ClassCastException ex) {
			throw new RuntimeException(
				"Given model element is not mapped to "
				+ "a complex type representation object", ex);
		}
		return _xsdDoc.getModels().get(xsdCType);
	}

	/** Sets <tt>type</tt> attribute to given XML Schema <tt>element</tt>
	 * declaration according to given referred model.
	 * @param xsdElemDecl Schema <tt>element</tt> declaration.
	 * @param referredXdModel referred X-definition model representation object.
	 */
	private void setElementCType(final Element xsdElemDecl,
		final XdModel referredXdModel) {
		//get main schema element
		Element schemaElem = XsdUtils.getAncestorSchema(xsdElemDecl);
		//get external schema
		XsdModel refXsdModel = _models.get(referredXdModel);
		XsdSchema refSchema = refXsdModel.getSchema();
		//get external complex type name
		XsdCType refCType;
		try {
			refCType = (XsdCType) refXsdModel;
		} catch (ClassCastException ex) {
			throw new RuntimeException("Model is mapped to wrong class", ex);
		}
		_xsdDoc.setType(xsdElemDecl,
			_xsdDoc.getQName(schemaElem, refSchema, refCType.getName()));
	}

	private ValueType getElementSimpleType(final Element element) {
		String xdNS = XDGenCollection.findXDNS(element);
		if (xdNS == null) {
			throw new RuntimeException("X-definifion namespace not found!");
		}
		Attr textAttr = element.getAttributeNodeNS(xdNS, XdNames.TEXT);
		//element has xd:text attribute
		if (textAttr != null) {
			String typeDecl = textAttr.getValue();
			ValueType t;
			try {
				t = XdefValueTypeParser.parse(typeDecl);
			} catch (Exception ex) {
				throw new RuntimeException(
					"Could not parse type declaration", ex);
			}
			return t;
		}
		//get <xd:text> children
		NodeList textElems = KXmlUtils.getChildElementsNS(
			element, xdNS, XdNames.TEXT);
		switch (textElems.getLength()) {
			case 0:
				return null;
			case 1: {
				Element textElem = (Element) textElems.item(0);
				String typeDecl = KXmlUtils.getTextValue(textElem);
				XDParsedScript script =
					XDParsedScript.getXdScript(typeDecl, null, true);
				typeDecl = script._type;
				ValueType t;
				try {
					t = XdefValueTypeParser.parse(typeDecl);
					if (t.getKind() == ValueType.OTHER) {
						((Other) t).setXdefName(
							XDGenCollection.getXDName(element));
					}
				} catch (Exception ex) {
					throw new RuntimeException(
						"Could not parse type declaration", ex);
				}
				return t;
			}
			default:
				XsdUnion union = new XsdUnion();
				StringBuilder sb = new StringBuilder();
				int i;
				for (i = 0; i < textElems.getLength(); i++) {
					Element textElem = (Element) textElems.item(i);
					String textDecl = KXmlUtils.getTextValue(textElem);
					XDParsedScript script =
						XDParsedScript.getXdScript(textDecl, null, true);
					textDecl = script._type;
					ValueType t;
					try {
						t = XdefValueTypeParser.parse(textDecl);
					} catch (Exception ex) {
						throw new RuntimeException(
							"Could not parse type declaration", ex);
					}
					union.addMemberType(t);
					if (i > 0) {
						sb.append(" OR ");
					}
					sb.append(t.getTypeString());
				}
				union.setTypeString(sb.toString());
				XsdList list = new XsdList();
				list.setItemType(union);
				list.setMaxLength(++i);
				return list;
		}
	}

	/** Processes given X-definition <tt>mixed</tt> declaration element and
	 * adds declaration to given schema context element.
	 * @param mixed X-definition <tt>mixed</tt> declaration element to process.
	 * @param schemaContext schema context element to process.
	 */
	private void processMixed(final Element mixed, final Element schemaContext){
		//Occurrence mixedOcc = XdUtils.getOccurrence(mixed);
		//TODO: resolve ref
		//TODO: resolve mixed occurrence
		Element seqElem = _xsdDoc.addSequenceDecl(schemaContext, null, null);
		Element choiceElem =
			_xsdDoc.addChoiceDecl(seqElem, 0, Occurrence.UNBOUNDED);
		processChildren(mixed, choiceElem);
	}

	/** Processes given X-definition <tt>sequence</tt> declaration element and
	 * adds declaration to given schema context element.
	 * @param sequence X-definition <tt>sequence</tt> declaration element to
	 * process.
	 * @param schemaContext schema context element to add declaration to.
	 */
	private void processSequence(final Element sequence,
		final Element schemaContext) {
		//TODO: resolve ref
		Occurrence seqOccurrence = XdUtils.getOccurrence(sequence);
		Element xsdSeqElem = _xsdDoc.addSequenceDecl(schemaContext,
			seqOccurrence.getMinOccurs(), seqOccurrence.getMaxOccurs());
		processChildren(sequence, xsdSeqElem);
	}

	/** Add complex Extension.
	 * @param complexType
	 * @param refElem
	 * @param hasText
	 * @return element
	 */
	private Element addComplexContExtension(final Element complexType,
		final XdElem refElem,
		final boolean hasText) {
		Element complexContent =
			_xsdDoc.addComplexContentDecl(complexType, hasText);
		//get referred complex type qualified name
		XdModel actProcModel = _procModel.peek();
		XsdModel xsdModel = _models.get(actProcModel);
		Element mainSchemaElem = _xsdDoc.getSchemas().get(xsdModel.getSchema());
		XsdModel refXsdModel = _models.get(refElem);
		XsdCType refCType = (XsdCType) refXsdModel;
		String refCTypeQName = _xsdDoc.getQName(mainSchemaElem,
			refXsdModel.getSchema(), refCType.getName());
		return _xsdDoc.addExtensionDecl(complexContent, refCTypeQName);
	}

	@Override
	public Map<String, Document> getSchemaDocuments() {
		return _xsdDoc.getSchemaDocuments();
	}
}