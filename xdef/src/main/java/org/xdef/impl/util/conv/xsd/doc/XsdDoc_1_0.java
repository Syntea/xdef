package org.xdef.impl.util.conv.xsd.doc;

import org.xdef.sys.SReporter;
import org.xdef.impl.util.conv.Util;
import org.xdef.impl.util.conv.Util.MyQName;
import org.xdef.impl.util.conv.xd.doc.XdDoc_2_0;
import org.xdef.impl.util.conv.xd.xd_2_0.domain.XdDecl;
import org.xdef.impl.util.conv.xd.xd_2_0.domain.XdDef;
import org.xdef.impl.util.conv.xd.xd_2_0.domain.XdElem;
import org.xdef.impl.util.conv.xd.xd_2_0.domain.XdGroup;
import org.xdef.impl.util.conv.xd.xd_2_0.domain.XdModel;
import org.xdef.impl.util.conv.xd.xd_2_0.XdNames;
import org.xdef.impl.util.conv.xd.xd_2_0.XdUtils;
import org.xdef.impl.util.conv.xd.xd_2_0.XdUtils.Occurrence;
import org.xdef.impl.util.conv.xsd.xsd_1_0.domain.XsdCType;
import org.xdef.impl.util.conv.xsd.xsd_1_0.domain.XsdGroup;
import org.xdef.impl.util.conv.xsd.xsd_1_0.domain.XsdModel;
import org.xdef.impl.util.conv.xsd.xsd_1_0.domain.XsdSType;
import org.xdef.impl.util.conv.xsd.xsd_1_0.domain.XsdSchema;
import org.xdef.impl.util.conv.xsd.xsd_1_0.domain.XsdSchemaContainer;
import org.xdef.impl.util.conv.xsd.xsd_1_0.domain.XsdSchemaSet;
import org.xdef.impl.util.conv.xsd.xsd_1_0.XsdNames;
import org.xdef.impl.util.conv.xsd.xsd_1_0.XsdUtils;
import org.xdef.xml.KXmlUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/** Represents implementation of XML Schema document version 1.0.
 * @author Ilia Alexandrov
 */
public class XsdDoc_1_0 extends XsdDoc {

	/** Schema models (XsdModel) to elements (Element) mapping. */
	private final Map<XsdModel, Element> _models =
		new HashMap<XsdModel, Element>();
	/** Schema representation (XsdSchema) to schema element (Element) mapping.*/
	private final Map<XsdSchema, Element> _schemas =
		new HashMap<XsdSchema, Element>();
	/** Map of external namespace (String) to (XsdSchema) mapping. */
	private final Map<String, XsdSchema> _extNSSchemas =
		new HashMap<String, XsdSchema>();
	/** Counter of external schemas. */
	private int _extSchemaCounter;
	/** Counter of external attribute groups. */
	private int _extAttrGrpCounter;
	/** Counter of external groups. */
	private int _extGroupCounter;
	/** Counter of external simple type names. */
	private int _extSTypeCounter;

	/** Creates instance of XML Schema document version 1.0.
	 * @param reporter reporter for reporting warnings and errors.
	 * @param schemaFileExt file extension of schema files.
	 * @param schemaPrefix prefix for schema nodes.
	 */
	public XsdDoc_1_0(final SReporter reporter,
		final String schemaFileExt,
		final String schemaPrefix) {
		super(reporter, schemaFileExt, schemaPrefix);
		_extSchemaCounter = 1;
		_extAttrGrpCounter = 1;
		_extGroupCounter = 1;
		_extSTypeCounter = 1;
	}

	/** Initiates all schema and schema models according to given X-definition
	 * document and returns mapping of models.
	 * @param xdDoc X-definition version 2.0 document representation.
	 * @return map of X-definition models (XdModel) mapped to schema
	 * models (XsdModel).
	 */
	public final Map<XdModel, XsdModel> init(final XdDoc_2_0 xdDoc) {
		Map<XdDef, XsdSchemaContainer> schemas = initSchemas(xdDoc.getXdDefs());
		return initModels(xdDoc.getXdModels(), schemas);
	}

	/** Initializes schema models according to given X-definition models map and
	 * returns models map.
	 * @param models map of X-definition models.
	 * @param schema map of schema.
	 * @return map of models (XdModel) to (XsdModel).
	 * @throws RuntimeErrorException if X-definition model is unknown type or
	 * if schema container is unknown type.
	 */
	private Map<XdModel,XsdModel> initModels(final Map<XdModel,Element> models,
		Map<XdDef, XsdSchemaContainer> schema) {
		Map<XdModel, XsdModel> ret = new HashMap<XdModel, XsdModel>();
		Iterator<XdModel> it = models.keySet().iterator();
		while (it.hasNext()) {
			XdModel xdModel = it.next();
			XdDef xdDef = xdModel.getDef();
			XsdSchemaContainer container =
				(XsdSchemaContainer) schema.get(xdDef);
			XsdModel xsdModel = null;
			switch (xdModel.getType()) {
				case XdModel.Type.DECLARATION: {
					XdDecl xdDecl = (XdDecl) xdModel;
					XsdSchema schm;
					switch (container.getType()) {
						case XsdSchemaContainer.Type.SINGLE_SCHEMA: {
							schm = (XsdSchema) container;
						}
						break;
						case XsdSchemaContainer.Type.SCHEMA_SET: {
							XsdSchemaSet schemaSet = (XsdSchemaSet) container;
							schm = schemaSet.getMainSchema();
						}
						break;
						default:
							throw new RuntimeException(
								"Illegal schema container!");
					}
					xsdModel = initDecl(xdDecl, schm);
				}
				break;
				case XdModel.Type.GROUP: {
					XdGroup xdGroup = (XdGroup) xdModel;
					XsdSchema schm;
					switch (container.getType()) {
						case XsdSchemaContainer.Type.SINGLE_SCHEMA: {
							schm = (XsdSchema) container;
						}
						break;
						case XsdSchemaContainer.Type.SCHEMA_SET: {
							XsdSchemaSet schemaSet = (XsdSchemaSet) container;
							schm = schemaSet.getMainSchema();
						}
						break;
						default:
							throw new RuntimeException(
								"Illegal schema container!");
					}
					xsdModel = initGroup(xdGroup, schm);
				}
				break;
				case XdModel.Type.ELEMENT: {
					XdElem xdElem = (XdElem) xdModel;
					XsdSchema schm;
					switch (container.getType()) {
						case XsdSchemaContainer.Type.SINGLE_SCHEMA: {
							schm = (XsdSchema) container;
						}
						break;
						case XsdSchemaContainer.Type.SCHEMA_SET: {
							XsdSchemaSet schemaSet = (XsdSchemaSet) container;
							schm = xdElem.getNamespace() == null
								? schemaSet.getMainSchema()
								: schemaSet.getSchema(xdElem.getNamespace());
						}
						break;
						default:
							throw new RuntimeException(
								"Illegal schema container!");
					}
					xsdModel = initElem(xdElem, schm);
				}
				break;
				default:
					throw new RuntimeException(
						"X-definition model is unknown type!");
			}
			ret.put(xdModel, xsdModel);
		}
		return ret;
	}

	/** Initializes given X-definition declaration and returns created schema
	 * model representation.
	 * @param xdDecl X-definition declaration model.
	 * @param xsdSchema schema representation.
	 * @return schema model representation.
	 */
	private XsdModel initDecl(final XdDecl xdDecl, final XsdSchema xsdSchema) {
		Element schema = _schemas.get(xsdSchema);
		String name = xdDecl.getDef().getName();
		String typeName = xdDecl.getName();
		String sTypeName = XsdUtils.getSTypeName(name, typeName);
		XsdSType xsdSType = new XsdSType(xsdSchema, sTypeName);
		Element sTypeElem = addSimpleTypeDecl(schema, sTypeName);
		_models.put(xsdSType, sTypeElem);
		return xsdSType;
	}

	/** Initiates given X-definition group model declaration and returns created
	 * schema model representation.
	 * @param xdGroup X-definition group model.
	 * @param xsSchema schema representation to put model to.
	 * @return schema model representation.
	 * @throws IllegalArgumentException if given X-definition group model is
	 * unknown type.
	 */
	private XsdModel initGroup(final XdGroup xdGroup, final XsdSchema xsSchema){
		Element schema = _schemas.get(xsSchema);
		String groupName;
		switch (xdGroup.getGroupType()) {
			case XdGroup.GroupType.CHOICE: {
				groupName = XsdUtils.getGroupName(xdGroup.getDef().getName(),
						xdGroup.getName(), XdNames.CHOICE);
			}
			break;
			case XdGroup.GroupType.MIXED: {
				groupName = XsdUtils.getGroupName(xdGroup.getDef().getName(),
						xdGroup.getName(), XdNames.MIXED);
			}
			break;
			case XdGroup.GroupType.SEQUENCE: {
				groupName = XsdUtils.getGroupName(xdGroup.getDef().getName(),
						xdGroup.getName(), XdNames.SEQUENCE);
			}
			break;
			default:
				throw new IllegalArgumentException(
					"Given X-definition group model is unknown group type!");
		}
		XsdGroup xsdGroup = new XsdGroup(xsSchema, groupName);
		Element groupElem = addGroupDecl(schema, groupName, null, null, null);
		_models.put(xsdGroup, groupElem);
		return xsdGroup;
	}

	/** Initiates given X-definition element model and returns created schema
	 * model representation.
	 * @param xdElem X-definition element model.
	 * @param xsSchema schema representation.
	 * @return created schema model.
	 */
	private XsdModel initElem(final XdElem xdElem, final XsdSchema xsSchema) {
		Element schemaElem = _schemas.get(xsSchema);
		String cTypeName = XsdUtils.getComplexTypeName(
			xdElem.getDef().getName(), xdElem.getName());
		XsdCType xsdCType = new XsdCType(xsSchema, cTypeName);
		Element cTypeElem = addComplexTypeDecl(schemaElem, cTypeName, null);
		_models.put(xsdCType, cTypeElem);
		return xsdCType;
	}

	/** Initiates all schema elements and returns mapping of X-definition
	 * representation to schema container.
	 * @param xdDefs map of X-definition representations to elements.
	 * @return map of X-definition representation (XdDef) to schema container
	 * (XsdSchemaContainer).
	 */
	private Map<XdDef, XsdSchemaContainer> initSchemas(
		final Map<XdDef, Element> xdDefs) {
		Map<XdDef, XsdSchemaContainer> ret =
			new HashMap<XdDef, XsdSchemaContainer>();
		Iterator<Entry<XdDef, Element>> it = xdDefs.entrySet().iterator();
		while (it.hasNext()) {
			Entry<XdDef, Element> entry = it.next();
			Set<String> namespaces = XdUtils.getModelsNS(entry.getValue());
			XdDef xdDef = entry.getKey();
			String defName = xdDef.getName();
			XsdSchemaContainer xsdSchemaContainer;
			if (namespaces.size() > 1) {
				_reporter.warning("", "X-definition '" + defName + "' contains "
					+ "model declarations with different namespace URIs,"
					+ " therefore single schema can not be created!");
				XsdSchema mainSchema =
					new XsdSchema(getSchemaFileName(defName), null);
				XsdSchemaSet schemaSet = new XsdSchemaSet(mainSchema);
				int namespaceCount = 1;
				Iterator<String> it2 = namespaces.iterator();
				while (it2.hasNext()) {
					String namespace = it2.next();
					if (namespace.length() == 0) {
						continue;
					}
					String schemaFileName = getSchemaFileName(defName + "_"
							+ namespaceCount);
					XsdSchema schema = new XsdSchema(schemaFileName, namespace);
					schemaSet.addSchema(schema);
					namespaceCount++;
				}
				xsdSchemaContainer = schemaSet;
			} else {
				String namespace;
				try {
					namespace = (String) namespaces.iterator().next();
				} catch (NoSuchElementException ex) {
					namespace = null;
				}
				String schemaFileName = getSchemaFileName(defName);
				xsdSchemaContainer = new XsdSchema(schemaFileName, namespace);
			}
			initSchema(xsdSchemaContainer);
			ret.put(xdDef, xsdSchemaContainer);
		}
		return ret;
	}

	/** Initiates given schema container. Creates proper elements and maps them
	 * to schema.
	 * @param schemaContainer schema container to initiate.
	 * @throws IllegalArgumentException if given schema container
	 * is unknown type.
	 */
	private void initSchema(final XsdSchemaContainer schemaContainer) {
		switch (schemaContainer.getType()) {
			case XsdSchemaContainer.Type.SINGLE_SCHEMA: {
				initSingleSchema((XsdSchema) schemaContainer);
			}
			break;
			case XsdSchemaContainer.Type.SCHEMA_SET: {
				XsdSchemaSet schemaSet = (XsdSchemaSet) schemaContainer;
				initSingleSchema(schemaSet.getMainSchema());
				Iterator<XsdSchema> it = schemaSet.getExtSchemas().iterator();
				while (it.hasNext()) {
					XsdSchema schema = it.next();
					initSingleSchema(schema);
					addImportDecl(_schemas.get(schemaSet.getMainSchema()),
						schema.getTargetNS(), schema.getName());
				}
			}
			break;
			default:
				throw new IllegalArgumentException(
					"Given schema container is unknown type!");
		}
	}

	/** Initiates given schema. Creates proper element and maps it to schema.
	 * @param schema schema to initialize.
	 */
	private void initSingleSchema(final XsdSchema schema) {
		Element schemaElem = createSchemaElem(schema.getTargetNS());
		if (schema.getTargetNS() != null) {
			Util.addNamespaceDecl(schemaElem, "tns", schema.getTargetNS());
		}
		_schemas.put(schema, schemaElem);
	}

	/** Returns qualified name of given <tt>complexType</tt> reference
	 * representation in scope of one schema.
	 * @param xsCType schema <tt>complexType</tt> representation object.
	 * @return <tt>complexType</tt> reference qualified name.
	 */
	public String getQName(final XsdCType xsCType) {
		XsdSchema schema = xsCType.getSchema();
		Element schemaElem = _schemas.get(schema);
		return XsdUtils.getRefQName(schemaElem, xsCType.getName()).getQName();
	}

	/** Returns qualified name of model from given external schema
	 * representation object with given local name from given schema element.
	 *
	 * @param mainSchemaElem <tt>schema</tt> element to put reference.
	 * @param external external schema representation with referred model.
	 * @param modelName local name of referred model.
	 * @return model qualified name.
	 */
	public final String getQName(final Element mainSchemaElem,
		final XsdSchema external,
		final String modelName) {
		return getRefQName(mainSchemaElem,
			external.getName(),	external.getTargetNS(), modelName).getQName();
	}

	/** Returns qualified name of reference to model. Resolves connection
	 * of schema and creating name space declarations.
	 * @param schema schema element.
	 * @param extSchemaFileName file name of external schema.
	 * @param extSchemaTargetNS external schema target name space URI.
	 * @param modelName referenced model local name.
	 * @return  qualified name of reference.
	 * @throws RuntimeException if can't get referenced model qualified name.
	 */
	private MyQName getRefQName(final Element schema,
		final String extSchemaFileName,
		final String extSchemaTargetNS,
		final String modelName) {
		Element extSchemaDecl = XsdUtils.getExtSchemaDecl(
			schema, extSchemaFileName);
		if (schema == extSchemaDecl) {
			return XsdUtils.getRefQName(schema, modelName);
		}
		if (extSchemaDecl == null) {
			extSchemaDecl = addExtSchemaDecl(
				schema, extSchemaFileName, extSchemaTargetNS);
		}
		if (XsdNames.INCLUDE.equals(extSchemaDecl.getLocalName())) {
			return XsdUtils.getRefQName(schema, modelName);
		}
		if (XsdNames.IMPORT.equals(extSchemaDecl.getLocalName())) {
			if (extSchemaTargetNS == null) {
				return new MyQName(modelName);
			}
			String prefix = Util.getNSPrefix(schema, extSchemaTargetNS);
			if (prefix == null) {
				prefix=Util.addNamespaceDeclaration(schema, extSchemaTargetNS);
			}
			return new MyQName(prefix, modelName);
		}
		throw new RuntimeException(
			"Could not get referenced model qualified name!");
	}

	/** Adds external schema declaration element (<tt>include</tt> or
	 * <tt>import</tt> depending on target name space) to given <tt>schema</tt>
	 * element and returns created and added declaration element.
	 * @param schema schema element to add external declaration to.
	 * @param extSchemaFileName file name of external schema.
	 * @param extSchemaTargetNS target name space URI of external schema or
	 * <tt>null</tt>.
	 * @return created and added external schema declaration element.
	 */
	private Element addExtSchemaDecl(final Element schema,
		final String extSchemaFileName,
		final String extSchemaTargetNS) {
		String targetNS = XsdUtils.getSchemaTargetNS(schema);
		if (targetNS == null) {
			if (extSchemaTargetNS == null) {
				return addIncludeDecl(schema, extSchemaFileName);
			} else {
				return addImportDecl(schema,
					extSchemaTargetNS, extSchemaFileName);
			}
		} else {
			if (extSchemaTargetNS == null) {
				return addImportDecl(schema, null, extSchemaFileName);
			} else {
				if (targetNS.equals(extSchemaTargetNS)) {
					return addIncludeDecl(schema, extSchemaFileName);
				} else {
					return addImportDecl(schema,
						extSchemaTargetNS, extSchemaFileName);
				}
			}
		}
	}

	/** Creates schema representation of external schema according to given
	 * name space URI, maps it to schema element and returns schema.
	 * @param namespaceURI name space URI of external schema.
	 * @return external schema.
	 */
	public final XsdSchema getExtNSSchema(final String namespaceURI) {
		XsdSchema extSchema = _extNSSchemas.get(namespaceURI);
		if (extSchema == null) {
			String schemaName =
				getSchemaFileName(XsdUtils.getExtSchemaName(_extSchemaCounter));
			extSchema = new XsdSchema(schemaName, namespaceURI);
			Element schemaElem = createSchemaElem(namespaceURI);
			_schemas.put(extSchema, schemaElem);
			_extNSSchemas.put(namespaceURI, extSchema);
			_extSchemaCounter++;
		}
		return extSchema;
	}

	/** Creates external schema, maps to schema element and returns created
	 * schema.
	 * @return external schema.
	 */
	public final XsdSchema getExtSchema() {
		String extSchemaName =
			getSchemaFileName(XsdUtils.getExtSchemaName(_extSchemaCounter));
		XsdSchema extSchema = new XsdSchema(extSchemaName, null);
		Element extSchemaElem = createSchemaElem(null);
		_schemas.put(extSchema, extSchemaElem);
		_extSchemaCounter++;
		return extSchema;
	}

	/** Creates external schema with given name space URI and returns created
	 * schema. Does not use external name space schema map!
	 * @param namespaceURI external schema name space URI.
	 * @return created external schema.
	 */
	public final XsdSchema getExtSchema(final String namespaceURI) {
		String extSchemaName =
			getSchemaFileName(XsdUtils.getExtSchemaName(_extSchemaCounter));
		XsdSchema extSchema = new XsdSchema(extSchemaName, namespaceURI);
		Element extSchemaElem = createSchemaElem(namespaceURI);
		_schemas.put(extSchema, extSchemaElem);
		_extSchemaCounter++;
		return extSchema;
	}

	//--------------------------------------------------------------------------
	//                      WORKING WITH SCHEMA NODES
	//--------------------------------------------------------------------------
	/** Creates XML Schema <tt>schema</tt> element with XML Schema name space
	 * declaration and adds schema target name space declaration if given
	 * target name space is not <tt>null</tt>.
	 * @param targetNamespace schema target name space or <tt>null</tt>.
	 * @return created <tt>schema</tt> element.
	 */
	public final Element createSchemaElem(final String targetNamespace) {
		Document doc = Util.getBuilder().newDocument(
			XsdVersion.SCHEMA_1_0.getNSURI(),
				getSchemaNodeName(XsdNames.SCHEMA), null);
		Element schema = doc.getDocumentElement();
		Util.addNamespaceDecl(schema, _schemaPrefix,
			XsdVersion.SCHEMA_1_0.getNSURI());
		if (targetNamespace != null && targetNamespace.length() != 0) {
			Util.addAttr(schema, XsdNames.TARGET_NAMESPACE, targetNamespace);
		}
		return schema;
	}

	/** Adds XML Schema <tt>element</tt> declaration element with given default
	 * value, given fixed value, maximal occurrence and minimal occurrence,
	 * given name, nillable switch and given type name to given parent element.
	 * @param parent parent element.
	 * @param defaultValue element default value.
	 * @param fixedValue element fixed value.
	 * @param minOccurs element minimal occurrence.
	 * @param maxOccurs element maximal occurrence.
	 * @param name element local name.
	 * @param ref referenced element declaration qualified name.
	 * @param nillable nillable switch.
	 * @param type element type name.
	 * @param qualified qualified element name switch.
	 * @return created and added <tt>element</tt> declaration element.
	 */
	public final Element addElementDecl(final Element parent,
		final String defaultValue,
		final String fixedValue,
		final Integer minOccurs,
		final Integer maxOccurs,
		final String name,
		final String ref,
		final Boolean nillable,
		final String type,
		final Boolean qualified) {
		Integer min = minOccurs, max = maxOccurs;
		//create element declaration
		Element elemDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(),
			getSchemaNodeName(XsdNames.ELEMENT));
		//resolving element name
		if (name != null && name.length() != 0) {
			addAttr(elemDecl, XsdNames.NAME, name);
		} else if (ref != null && ref.length() != 0) {
			addAttr(elemDecl, XsdNames.REF, ref);
		}
		//resolving fixed and default values
		if (defaultValue != null && defaultValue.length() > 0) {
			addAttr(elemDecl, XsdNames.DEFAULT, defaultValue);
/*VT*/
			addAttr(elemDecl, XsdNames.DEFAULT,
				(defaultValue.startsWith("\"") && defaultValue.endsWith("\"")
				|| defaultValue.startsWith("'") && defaultValue.endsWith("'"))
					? defaultValue.substring(1, defaultValue.length()-1)
					: defaultValue);
			min = 0; // must be optional
/*VT*/
		} else if (fixedValue != null && fixedValue.length() > 0) {
//			addAttr(elemDecl, XsdNames.FIXED, fixedValue);
/*VT*/
			addAttr(elemDecl, XsdNames.FIXED,
				(defaultValue.startsWith("\"") && defaultValue.endsWith("\"")
				|| defaultValue.startsWith("'") && defaultValue.endsWith("'"))
					? defaultValue.substring(1, defaultValue.length()-1)
					: defaultValue);
//			minOccurs = 0; // must be optional
/*VT*/
		}
		//resolving minOccurs
		if (min != null && min != 1) {
			addAttr(elemDecl, XsdNames.MIN_OCCURS, min.toString());
		}
		//resolving maxOccurs
		if (max != null && max != 1) {
			setMaxOccurs(elemDecl, max);
		}
		//resolving nillable
		if (nillable != null && nillable != false) {
			addAttr(elemDecl, XsdNames.NILLABLE, "true");
		}
		//resolving type
		if (type != null && type.length() != 0) {
			addAttr(elemDecl, XsdNames.TYPE, type);
		}
		//resolving form
		if (qualified != null) {
			addAttr(elemDecl, XsdNames.FORM,
				(qualified ? XsdNames.QUALIFIED : XsdNames.UNQUALIFIED));
		}
		parent.appendChild(elemDecl);
		return elemDecl;
	}

	/** Adds XML Schema <tt>complexType</tt> declaration element with given name
	 * and given mixed switch to given parent element.
	 * @param parent parent element.
	 * @param name complex type name.
	 * @param mixed mixed type switch.
	 * @return created and added <tt>complexType</tt> element.
	 */
	public final Element addComplexTypeDecl(final Element parent,
		final String name,
		final Boolean mixed) {
		Element cTypeDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(),
			getSchemaNodeName(XsdNames.COMPLEX_TYPE));
		//resolving name
		if (name != null && name.length() != 0) {
			addAttr(cTypeDecl, XsdNames.NAME, name);
		}
		//resolving mixed
		if (mixed != null && mixed != false) {
			addAttr(cTypeDecl, XsdNames.MIXED, "true");
		}
		parent.appendChild(cTypeDecl);
		return cTypeDecl;
	}

	/** Adds XML Schema <tt>any</tt> declaration element with given minimal and
	 * maximal occurrence to given parent element and returns created and added
	 * element.
	 * @param parent parent element.
	 * @param minOccurs minimal occurrence.
	 * @param maxOccurs maximal occurrence.
	 * @return created and added <tt>any</tt> declaration element.
	 */
	public final Element addAnyDecl(final Element parent,
		final Integer minOccurs,
		final Integer maxOccurs) {
		Element anyDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(), getSchemaNodeName(XsdNames.ANY));
		//resolving min occurs
		if (minOccurs != null && minOccurs != 1) {
			addAttr(anyDecl, XsdNames.MIN_OCCURS, minOccurs.toString());
		}
		//resolving max occurs
		if (maxOccurs != null && maxOccurs != 1) {
			setMaxOccurs(anyDecl, maxOccurs);
		}
		//add any namespace declaration
		addAttr(anyDecl, XsdNames.NAMESPACE, "##any");
		//add lax process contents
		addAttr(anyDecl, XsdNames.PROCESS_CONTENTS, "lax");
		insertBeforeAttributes(parent, anyDecl);
		return anyDecl;
	}

	/** Returns <tt>annotation</tt> child element of given schema context
	 * element. If given parent element already has <tt>annotation</tt> child
	 * element, returns that element, otherwise creates one and returns it.
	 * @param parent parent element to add or get child <tt>annotation</tt>
	 * element.
	 * @return child <tt>annotation</tt> element.
	 */
	private Element addAnnotationDecl(final Element parent) {
		Element annotationElem = XsdUtils.getAnnotationElem(parent);
		if (annotationElem == null) {
			annotationElem = parent.getOwnerDocument().createElementNS(
				XsdVersion.SCHEMA_1_0.getNSURI(),
				getSchemaNodeName(XsdNames.ANNOTATION));
			Util.insertFirst(parent, annotationElem);
		}
		return annotationElem;
	}

	/** Adds <tt>documentation</tt> child element to given <tt>annotation</tt>
	 * element and returns added <tt>documentation</tt> element.
	 * @param annotationElem schema <tt>annotation</tt> element.
	 * @param documentation documentation string to add.
	 * @return created and added <tt>documentation</tt> element.
	 */
	private Element addDocumentationDecl(final Element annotationElem,
		final String documentation) {
		Element documentationElem =	annotationElem.getOwnerDocument()
			.createElementNS(XsdVersion.SCHEMA_1_0.getNSURI(),
				getSchemaNodeName(XsdNames.DOCUMENTATION));
		annotationElem.appendChild(documentationElem);
		Text documentationText =
			documentationElem.getOwnerDocument().createTextNode(documentation);
		documentationElem.appendChild(documentationText);
		return documentationElem;
	}

	/** Adds <tt>documentation</tt> element to <tt>annotation</tt> child element
	 * of given schema context element.
	 * @param contextElem schema context element to add documentation to.
	 * @param documentation documentation string to add.
	 */
	public final void addDocumentation(final Element contextElem,
		final String documentation) {
		Element annotElem = addAnnotationDecl(contextElem);
		addDocumentationDecl(annotElem, documentation);
	}

	/** Adds XML Schema <tt>anyAttribute</tt> declaration element to given
	 * parent element.
	 * @param parent parent element.
	 * @return created and added <tt>anyAttribute</tt> declaration element.
	 */
	public final Element addAnyAttrDecl(final Element parent) {
		Element anyAttrDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(),
			getSchemaNodeName(XsdNames.ANY_ATTRIBUTE));
		//add any namespace declaration
		addAttr(anyAttrDecl, XsdNames.NAMESPACE, "##any");
		//add lax process contents
		addAttr(anyAttrDecl, XsdNames.PROCESS_CONTENTS, "lax");
		parent.appendChild(anyAttrDecl);
		return anyAttrDecl;
	}

	/** Adds XML Schema <tt>attribute</tt> declaration element with given
	 * default value, fixed value, local name, type name and use to given
	 * parent element and returns created and added element.
	 * @param parent parent element.
	 * @param defaultValue default attribute value.
	 * @param fixedValue fixed attribute value.
	 * @param name attribute local name.
	 * @param xdname name of X-definition where attribute model is declared.
	 * @param ref reference string.
	 * @param type attribute value type.
	 * @param attrUse attribute use.
	 * @param qualified qualified attribute name switch.
	 * @return created and added <tt>attribute</tt> declaration element.
	 */
	public final Element addAttributeDecl(final Element parent,
		final String defaultValue,
		final String fixedValue,
		final String name,
		final String xdname,
		final String ref,
		final String type,
		final String attrUse,
		final Boolean qualified) {
		Element attrDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(),
			getSchemaNodeName(XsdNames.ATTRIBUTE));
		String use = attrUse;
		//resolving default and fixed vlues
		if (defaultValue != null && defaultValue.length() > 0) {
//			addAttr(attrDecl, XsdNames.DEFAULT, defaultValue);
/*VT*/
			addAttr(attrDecl, XsdNames.DEFAULT,
				(defaultValue.startsWith("\"") && defaultValue.endsWith("\"")
				|| defaultValue.startsWith("'") && defaultValue.endsWith("'"))
					? defaultValue.substring(1, defaultValue.length()-1)
					: defaultValue);
			use = XsdNames.OPTIONAL; // must be optional
/*VT*/
		} else if (fixedValue != null && fixedValue.length() > 0) {
//			addAttr(attrDecl, XsdNames.FIXED, fixedValue);
/*VT*/
			if (!XsdNames.OPTIONAL.equals(use)) {
				use = XsdNames.OPTIONAL; // will be optional
			}
			addAttr(attrDecl, XsdNames.FIXED,
				(fixedValue.startsWith("\"") && fixedValue.endsWith("\"")
				|| fixedValue.startsWith("'") && fixedValue.endsWith("'"))
					? fixedValue.substring(1, fixedValue.length()-1)
					: fixedValue);
/*VT*/
		}
		//resolving name and ref
		if (name != null && name.length() != 0) {
			addAttr(attrDecl, XsdNames.NAME, name);
		} else if (ref != null && ref.length() != 0) {
			addAttr(attrDecl, XsdNames.REF, ref);
		}
		//resolving type
		if (type != null && type.length() != 0) {
			addAttr(attrDecl, XsdNames.TYPE, type);
		}
		//resolving use
		if (use != null && !use.equals(XsdNames.OPTIONAL)) {
			addAttr(attrDecl, XsdNames.USE, use);
		}
		//resolving form
		if (qualified != null) {
			addAttr(attrDecl, XsdNames.FORM,
				(qualified ? XsdNames.QUALIFIED	: XsdNames.UNQUALIFIED));
		}
		parent.appendChild(attrDecl);
		return attrDecl;
	}

	/** Adds XML Schema <tt>attributeGroup</tt> declaration element with given
	 * name or given reference name to given parent element.
	 * @param parent parent element.
	 * @param name attribute group local name.
	 * @param ref attribute group reference name.
	 * @return created and added <tt>attributeGroup</tt> declaration element.
	 */
	public final Element addAttrGroupDecl(final Element parent,
		final String name,
		final String ref) {
		Element attrGrpDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(),
			getSchemaNodeName(XsdNames.ATTRIBUTE_GROUP));
		//resolving name
		if (name != null && name.length() != 0) {
			addAttr(attrGrpDecl, XsdNames.NAME, name);
		} else if (ref != null && ref.length() != 0) {
			addAttr(attrGrpDecl, XsdNames.REF, ref);
		}
		parent.appendChild(attrGrpDecl);
		return attrGrpDecl;
	}

	/** Adds XML Schema <tt>complexContent</tt> declaration element with given
	 * mixed switch to given parent element and returns added element.
	 * @param parent parent element.
	 * @param mixed mixed content switch.
	 * @return created and added <tt>complexContent</tt> declaration element.
	 */
	public final Element addComplexContentDecl(final Element parent,
		final Boolean mixed) {
		Element complContDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(),
			getSchemaNodeName(XsdNames.COMPLEX_CONTENT));
		//resolving mixed
		if (mixed != null && mixed != false) {
			addAttr(complContDecl, XsdNames.MIXED, "true");
		}
		parent.appendChild(complContDecl);
		return complContDecl;
	}

	/** Adds XML Schema <tt>choice</tt> declaration element with given minimal
	 * and maximal occurrence to given parent element and returns created and
	 * added element.
	 * @param parent parent element.
	 * @param minOccurs minimal occurrence.
	 * @param maxOccurs maximal occurrence.
	 * @return created and added <tt>choice</tt> declaration element.
	 */
	public final Element addChoiceDecl(final Element parent,
		final Integer minOccurs,
		final Integer maxOccurs) {
		Element choiceDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(),
			getSchemaNodeName(XsdNames.CHOICE));
		//resolving min occurrence
		if (minOccurs != null && minOccurs != 1) {
			addAttr(choiceDecl, XsdNames.MIN_OCCURS, minOccurs.toString());
		}
		//resolving max occurrence
		if (maxOccurs != null && maxOccurs != 1) {
			setMaxOccurs(choiceDecl, maxOccurs);
		}
		insertBeforeAttributes(parent, choiceDecl);
		return choiceDecl;
	}

	/** Adds XML Schema <tt>extension</tt> declaration element with given
	 * base type to given parent element and returns created and added element.
	 * @param parent parent element.
	 * @param base extension base type name.
	 * @return created and added <tt>extension</tt> declaration element.
	 */
	public final Element addExtensionDecl(final Element parent,
		final String base) {
		Element extensionDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(),
			getSchemaNodeName(XsdNames.EXTENSION));
		//resolving base
		if (base != null && base.length() != 0) {
			addAttr(extensionDecl, XsdNames.BASE, base);
		}
		parent.appendChild(extensionDecl);
		return extensionDecl;
	}

	/** Adds XML Schema <tt>group</tt> declaration element with given name,
	 * given reference, given minimal and maximal occurrence and returns
	 * created and added element.
	 * @param parent parent element.
	 * @param name group name.
	 * @param ref reference name.
	 * @param minOccurs minimal occurrence.
	 * @param maxOccurs maximal occurrence.
	 * @return created and added <tt>group</tt> declaration element.
	 */
	public final Element addGroupDecl(final Element parent,
		final String name,
		final String ref,
		final Integer minOccurs,
		final Integer maxOccurs) {
		Element groupDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(),
			getSchemaNodeName(XsdNames.GROUP));
		//resolving name and ref
		if (name != null && name.length() != 0) {
			addAttr(groupDecl, XsdNames.NAME, name);
		} else if (ref != null && ref.length() != 0) {
			addAttr(groupDecl, XsdNames.REF, ref);
		}
		//resolvin min occurrence
		if (minOccurs != null && minOccurs != 1) {
			addAttr(groupDecl, XsdNames.MIN_OCCURS, minOccurs.toString());
		}
		//resolving max occurs
		if (maxOccurs != null && maxOccurs != 1) {
			setMaxOccurs(groupDecl, maxOccurs);
		}
		//TODO: insert before attribute groups
		if (!XsdUtils.isSchema(parent)) {
			insertBeforeAttributes(parent, groupDecl);
		} else {
			parent.appendChild(groupDecl);
		}
		return groupDecl;
	}

	/** Adds XML Schema <tt>import</tt> declaration element with given
	 * name space and schema location to given parent element and returns
	 * created and added element.
	 * @param parent parent element.
	 * @param namespace imported name space.
	 * @param schemaLocation imported schema location.
	 * @return created and added <tt>import</tt> declaration element.
	 */
	public final Element addImportDecl(final Element parent,
		final String namespace,
		final String schemaLocation) {
		Element importDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(),
			getSchemaNodeName(XsdNames.IMPORT));
		//resolving namespace
		if (namespace != null && namespace.length() != 0) {
			addAttr(importDecl, XsdNames.NAMESPACE, namespace);
		}
		//resolving schema location
		if (schemaLocation != null && schemaLocation.length() != 0) {
			addAttr(importDecl, XsdNames.SCHEMA_LOCATION, schemaLocation);
		}
		Util.insertFirst(parent, importDecl);
		return importDecl;
	}

	/** Add XML Schema "includeImport" declaration element with given schema
	 * location to given parent element and returns created and added element.
	 * @param parent parent element.
	 * @param schemaLocation included schema location.
	 * @return created and added <tt>includeImport</tt> declaration element.
	 */
	public final Element addIncludeDecl(final Element parent,
		final String schemaLocation) {
		Element includeDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(),
			getSchemaNodeName(XsdNames.INCLUDE));
		//resolving schema location
		if (schemaLocation != null && schemaLocation.length() != 0) {
			addAttr(includeDecl, XsdNames.SCHEMA_LOCATION, schemaLocation);
		}
		Util.insertFirst(parent, includeDecl);
		return includeDecl;
	}

	/** Adds XML Schema <tt>restriction</tt> declaration element with given base
	 * type name to given parent element and returns created and added element.
	 * @param parent parent element.
	 * @param base restriction base type name.
	 * @return created and added <tt>restriction</tt> element.
	 */
	public final Element addRestrictionDecl(final Element parent,
		final String base) {
		Element restrictionDecl = parent.getOwnerDocument()
			.createElementNS(XsdVersion.SCHEMA_1_0.getNSURI(),
				getSchemaNodeName(XsdNames.RESTRICTION));
		//resolving base
		if (base != null && base.length() != 0) {
			addAttr(restrictionDecl, XsdNames.BASE, base);
		}
		parent.appendChild(restrictionDecl);
		return restrictionDecl;
	}

	/** Add XML Schema <tt>sequence</tt> declaration element with given
	 * minimal and maximal occurrence to given parent element and returns
	 * created and added element.
	 * @param parent parent element.
	 * @param minOccurs sequence minimal occurrence.
	 * @param maxOccurs sequence maximal occurrence.
	 * @return created and added <tt>sequence</tt> declaration element.
	 */
	public final Element addSequenceDecl(final Element parent,
		final Integer minOccurs,
		final Integer maxOccurs) {
		Element sequenceDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(),
			getSchemaNodeName(XsdNames.SEQUENCE));
		//resolving min occurrence
		if (minOccurs != null && minOccurs != 1) {
			addAttr(sequenceDecl, XsdNames.MIN_OCCURS, minOccurs.toString());
		}
		//resolving max occurrence
		if (maxOccurs != null && maxOccurs != 1) {
			setMaxOccurs(sequenceDecl, maxOccurs);
		}
		insertBeforeAttributes(parent, sequenceDecl);
		return sequenceDecl;
	}

	/** Add XML Schema <tt>simpleContent</tt> declaration element to given
	 * parent element and returns created and added element.
	 * @param parent parent element.
	 * @return created and added <tt>simpleContent</tt> element.
	 */
	public final Element addSimpleContentDecl(final Element parent) {
		Element simpleContDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(),
			getSchemaNodeName(XsdNames.SIMPLE_CONTENT));
		parent.appendChild(simpleContDecl);
		return simpleContDecl;
	}

	/** Add XML Schema <tt>simpleType</tt> declaration element with given
	 * simple type name to given parent element and returns created and added
	 * element.
	 * @param parent parent element.
	 * @param name simple type name .
	 * @return created and added <tt>simpleType</tt> declaration element.
	 */
	public final Element addSimpleTypeDecl(final Element parent,
		final String name) {
		Element simpleTypeDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(),
			getSchemaNodeName(XsdNames.SIMPLE_TYPE));
		//resolving name
		if (name != null && name.length() != 0) {
			addAttr(simpleTypeDecl, XsdNames.NAME, name);
		}
		parent.appendChild(simpleTypeDecl);
		return simpleTypeDecl;
	}

	/** Add XML Schema <tt>union</tt> declaration element with given member
	 * types string to given parent element and returns created and added
	 * <tt>union</tt> declaration element.
	 * @param parent parent element to add <tt>union</tt> declaration to.
	 * @param memberTypes member types string.
	 * @return created and added <tt>union</tt> declaration element.
	 */
	public final Element addUnionDecl(final Element parent,
		final String memberTypes) {
		Element unionDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(),
			getSchemaNodeName(XsdNames.UNION));
		//resolving member types
		if (memberTypes != null && memberTypes.length() != 0) {
			addAttr(unionDecl, XsdNames.MEMBER_TYPES, memberTypes);
		}
		parent.appendChild(unionDecl);
		return unionDecl;
	}

	/** Add XML Schema <tt>list</tt> declaration element with given item type
	 * qualified name to given parent element and returns created and added
	 * <tt>list</tt> declaration element.
	 * @param parent parent element ot add <tt>list</tt> declaration to.
	 * @param itemType item type qualified name.
	 * @return created and added <tt>list</tt> declaration element.
	 */
	public final Element addListDecl(final Element parent,
		final String itemType) {
		Element listElem = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(), getSchemaNodeName(XsdNames.LIST));
		//resolving item type
		if (itemType != null && itemType.length() != 0) {
			addAttr(listElem, XsdNames.ITEM_TYPE, itemType);
		}
		parent.appendChild(listElem);
		return listElem;
	}

	/** Add XML Schema simple type restriction facet declaration element with
	 * given facet name and given facet value to given parent node and
	 * returns created and added facet declaration element.
	 * @param parent parent element to add facet declaration to.
	 * @param name name of facet.
	 * @param value facet value.
	 * @return created and added facet declaration element.
	 */
	public final Element addFacet(final Element parent,
		final String name,
		final String value) {
		Element facetElem = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(), getSchemaNodeName(name));
		addAttr(facetElem, XsdNames.VALUE, value);
		parent.appendChild(facetElem);
		return facetElem;
	}

	/** Add <tt>mixed</tt> attribute to given schema <tt>complexType</tt>
	 * element with given value.
	 * @param complexType schema <tt>complexType</tt> element.
	 * @param mixed value for <tt>mixed</tt> attribute.
	 */
	public final void setMixed(final Element complexType, final boolean mixed) {
		Util.setAttr(complexType, XsdNames.MIXED, String.valueOf(mixed));
	}

	/** Add <tt>minOccurs</tt> attribute to given element with given value.
	 * @param element element of add attribute.
	 * @param minOccurs minimal occurrence.
	 */
	public final void setMinOccurs(final Element element, final int minOccurs) {
		Util.setAttr(element, XsdNames.MIN_OCCURS, Integer.toString(minOccurs));
	}

	/** Add <tt>maxOccurs</tt> attribute to given element with given value.
	 * @param element element to add attribute.
	 * @param maxOccurs maximal occurrence.
	 */
	public final void setMaxOccurs(final Element element, final int maxOccurs) {
		String maxOccursString = Occurrence.UNBOUNDED == maxOccurs
			? XsdNames.UNBOUNDED : Integer.toString(maxOccurs);
		Util.setAttr(element, XsdNames.MAX_OCCURS, maxOccursString);
	}

	/** Add <tt>type</tt> attribute to given element with given type name.
	 * @param element element to add <tt>type</tt> attribute.
	 * @param type type name to add.
	 */
	public final void setType(final Element element, final String type) {
		Util.setAttr(element, XsdNames.TYPE, type);
	}

	/** Add or sets <tt>memberTypes</tt> attribute with given string.
	 * @param unionElem schema <tt>union</tt> element to add member types.
	 * @param memberTypes member types string to add.
	 */
	public final void setMemeberTypes(final Element unionElem,
		final String memberTypes) {
		Util.setAttr(unionElem, XsdNames.MEMBER_TYPES, memberTypes);
	}

	/** Get XML Schema qualified name according to schema type local name.
	 * @param typeLocalName local name of type.
	 * @return type qualified name.
	 * @throws NullPointerException if given type local name is <tt>null</tt>.
	 * @throws IllegalArgumentException if given type local name is empty.
	 */
	public final String getSchemaTypeQName(final String typeLocalName) {
		if (typeLocalName == null) {
			throw new NullPointerException("Given type local name is null!");
		}
		if (typeLocalName.length() == 0) {
			throw new IllegalArgumentException(
				"Given type local name is empty!");
		}
		return getSchemaNodeName(typeLocalName);
	}

	/** Add <tt>type</tt> attribute to given element with given schema type
	 * local name.
	 * @param element element to add <tt>type</tt> attribute.
	 * @param schemaType schema type local name.
	 */
	public final void setSchemaType(final Element element,
		final String schemaType) {
		Util.setAttr(element, XsdNames.TYPE, getSchemaNodeName(schemaType));
	}

	/** Add attribute node with given local name and given value to given
	 * parent element.
	 * @param parent parent element.
	 * @param name attribute local name.
	 * @param value attribute value.
	 * @return created and added attribute node.
	 */
	private Attr addAttr(final Element parent,
		final String name,
		final String value) {
		return Util.addAttr(parent, name, value);
	}

	/** Insert given element node before attribute nodes declarations to given
	 * parent element.
	 * @param parent parent element to add element.
	 * @param element element to add.
	 */
	private void insertBeforeAttributes(final Element parent,
		final Element element) {
		Element attrDecl = KXmlUtils.firstElementChildNS(parent,
			XsdVersion.SCHEMA_1_0.getNSURI(),
			new String[]{XsdNames.ANY_ATTRIBUTE, XsdNames.ATTRIBUTE,
					XsdNames.ATTRIBUTE_GROUP});
		if (attrDecl != null) {
			parent.insertBefore(element, attrDecl);
		} else {
			parent.appendChild(element);
		}
	}

	/** Schema models map getter.
	 * @return schema models map (XsdModel) to (Element).
	 */
	public final Map<XsdModel, Element> getModels() {return _models;}

	/** Schema map getter.
	 * @return schema map (XsdSchema) to (Element).
	 */
	public final Map<XsdSchema, Element> getSchemas() {return _schemas;}

	/** External attribute group counter getter.
	 * @return external attribute group counter.
	 */
	public final int getExtAttrGrpCounter() {return _extAttrGrpCounter++;}

	/** External group counter getter.
	 * @return external group counter.
	 */
	public final int getExtGroupCounter() {return _extGroupCounter++;}

	/** External simple type name counter getter.
	 * @return external simple type name counter.
	 */
	public final int getExtSTypeCounter() {return _extSTypeCounter++;}

	@Override
	public final XsdVersion getVersion() {return XsdVersion.SCHEMA_1_0;}

	@Override
	public final Map<String, Document> getSchemaDocuments() {
		Map<String, Document> ret = new HashMap<String, Document>();
		Iterator<Map.Entry<XsdSchema, Element>> it =
			_schemas.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<XsdSchema, Element> schemaEntry = it.next();
			XsdSchema xsdSchema = schemaEntry.getKey();
			Element element = schemaEntry.getValue();
			ret.put(xsdSchema.getName(), element.getOwnerDocument());
		}
		return ret;
	}
}