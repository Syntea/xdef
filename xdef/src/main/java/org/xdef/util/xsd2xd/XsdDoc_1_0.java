package org.xdef.util.xsd2xd;

import org.xdef.sys.SReporter;
import org.xdef.util.xsd2xd.xd.Utils;
import org.xdef.util.xsd2xd.xd.Utils.MyQName;
import org.xdef.util.xsd2xd.xd.XdDoc_2_0;
import org.xdef.util.xsd2xd.xd.XdDecl;
import org.xdef.util.xsd2xd.xd.XdDef;
import org.xdef.util.xsd2xd.xd.XdElem;
import org.xdef.util.xsd2xd.xd.XdGroup;
import org.xdef.util.xsd2xd.xd.XdModel;
import org.xdef.util.xsd2xd.xd.XdNames;
import org.xdef.util.xsd2xd.xd.XdUtils;
import org.xdef.util.xsd2xd.xd.XdUtils.Occurrence;
import org.xdef.util.xsd2xd.xsd_1_0.domain.XsdCType;
import org.xdef.util.xsd2xd.xsd_1_0.domain.XsdGroup;
import org.xdef.util.xsd2xd.xsd_1_0.domain.XsdModel;
import org.xdef.util.xsd2xd.xsd_1_0.domain.XsdSType;
import org.xdef.util.xsd2xd.xsd_1_0.domain.XsdSchema;
import org.xdef.util.xsd2xd.xsd_1_0.domain.XsdSchemaContainer;
import org.xdef.util.xsd2xd.xsd_1_0.domain.XsdSchemaSet;
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
public class XsdDoc_1_0 {
	/** Reporter for reporting warnings and errors. */
	protected final SReporter _reporter;
	/** Schema file extension. */
	protected final String _schemaFileExt;
	/** Schema nodes namespace prefix. */
	protected final String _schemaPrefix;
	/** Switch to generate documentation. */
	protected final boolean _genDocumentation;

	/** Schema models (XsdModel) to elements (Element) mapping. */
	private final Map<XsdModel, Element> _models = new HashMap<>();
	/** Schema representation (XsdSchema) to schema element (Element) mapping.*/
	private final Map<XsdSchema, Element> _schemas = new HashMap<>();
	/** Map of external namespace (String) to (XsdSchema) mapping. */
	private final Map<String, XsdSchema> _extNSSchemas = new HashMap<>();
	/** Counter of external schema. */
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
	 * @param genDocumentation if true documentation is generated.
	 */
	public XsdDoc_1_0(SReporter reporter,
		String schemaFileExt,
		String schemaPrefix,
		boolean genDocumentation) {
		if (reporter == null) {
			throw new NullPointerException("Given reporter is null!");
		}
		if (schemaFileExt == null) {
			throw new NullPointerException(
				"Given schema file extenson is null");
		}
		if (schemaFileExt.length() == 0) {
			throw new IllegalArgumentException(
				"Given schema file extension is empty");
		}
		_reporter = reporter;
		_schemaFileExt = schemaFileExt;
		if (schemaPrefix != null && schemaPrefix.length() == 0) {
			_schemaPrefix = null;
		} else {
			_schemaPrefix = schemaPrefix;
		}
		_genDocumentation = genDocumentation;
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
	public Map<XdModel, XsdModel> init(XdDoc_2_0 xdDoc) {
		Map<XdDef, XsdSchemaContainer> schemas = initSchemas(xdDoc.getXdDefs());
		return initModels(xdDoc.getXdModels(), schemas);
	}

	/** Initializes schema models according to given X-definition models map and
	 * returns models map.
	 * @param xdModels map of X-definition models.
	 * @param schemas map of schemas.
	 * @return map of models (XdModel) to (XsdModel).
	 * @throws RuntimeErrorException if X-definition model is unknown type or
	 * if schema container is unknown type.
	 */
	private Map<XdModel, XsdModel> initModels(Map xdModels, Map schemas) {
		Map<XdModel, XsdModel> ret = new HashMap<>();
		Iterator it = xdModels.keySet().iterator();
		while (it.hasNext()) {
			XdModel xdModel = (XdModel) it.next();
			XdDef xdDef = xdModel.getDef();
			XsdSchemaContainer container =
				(XsdSchemaContainer) schemas.get(xdDef);
			XsdModel xsdModel = null;
			switch (xdModel.getType()) {
				case XdModel.Type.DECLARATION: {
					XdDecl xdDecl = (XdDecl) xdModel;
					XsdSchema schema;
					switch (container.getType()) {
						case XsdSchemaContainer.Type.SINGLE_SCHEMA: {
							schema = (XsdSchema) container;
						}
						break;
						case XsdSchemaContainer.Type.SCHEMA_SET: {
							XsdSchemaSet schemaSet = (XsdSchemaSet) container;
							schema = schemaSet.getMainSchema();
						}
						break;
						default:
							throw new RuntimeException(
								"Illegal schema container!");
					}
					xsdModel = initDecl(xdDecl, schema);
				}
				break;
				case XdModel.Type.GROUP: {
					XdGroup xdGroup = (XdGroup) xdModel;
					XsdSchema schema;
					switch (container.getType()) {
						case XsdSchemaContainer.Type.SINGLE_SCHEMA: {
							schema = (XsdSchema) container;
						}
						break;
						case XsdSchemaContainer.Type.SCHEMA_SET: {
							XsdSchemaSet schemaSet = (XsdSchemaSet) container;
							schema = schemaSet.getMainSchema();
						}
						break;
						default:
							throw new RuntimeException(
								"Illegal schema container!");
					}
					xsdModel = initGroup(xdGroup, schema);
				}
				break;
				case XdModel.Type.ELEMENT: {
					XdElem xdElem = (XdElem) xdModel;
					XsdSchema schema;
					switch (container.getType()) {
						case XsdSchemaContainer.Type.SINGLE_SCHEMA: {
							schema = (XsdSchema) container;
						}
						break;
						case XsdSchemaContainer.Type.SCHEMA_SET: {
							XsdSchemaSet schemaSet = (XsdSchemaSet) container;
							schema = xdElem.getNamespace() == null
								? schemaSet.getMainSchema()
								: schemaSet.getSchema(xdElem.getNamespace());
						}
						break;
						default:
							throw new RuntimeException(
								"Illegal schema container!");
					}
					xsdModel = initElem(xdElem, schema);
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
	private XsdModel initDecl(XdDecl xdDecl, XsdSchema xsdSchema) {
		Element schema = _schemas.get(xsdSchema);
		String sTypeName = XsdUtils.getSTypeName(
			xdDecl.getDef().getName(), xdDecl.getName());
		XsdSType xsdSType = new XsdSType(xsdSchema, sTypeName);
		Element sTypeElem = addSimpleTypeDecl(schema, sTypeName);
		_models.put(xsdSType, sTypeElem);
		return xsdSType;
	}

	/** Initiates given X-definition group model declaration and returns created
	 * schema model representation.
	 * @param xdGroup X-definition group model.
	 * @param xsdSchema schema representation to put model to.
	 * @return schema model representation.
	 * @throws IllegalArgumentException if given X-definition group model is
	 * unknown type.
	 */
	private XsdModel initGroup(XdGroup xdGroup, XsdSchema xsdSchema) {
		Element schema = _schemas.get(xsdSchema);
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
		XsdGroup xsdGroup = new XsdGroup(xsdSchema, groupName);
		Element groupElem = addGroupDecl(schema, groupName, null, null, null);
		_models.put(xsdGroup, groupElem);
		return xsdGroup;
	}

	/** Initiates given X-definition element model and returns created schema
	 * model representation.
	 * @param xdElem X-definition element model.
	 * @param xsdSchema schema representation.
	 * @return created schema model.
	 */
	private XsdModel initElem(XdElem xdElem, XsdSchema xsdSchema) {
		Element schemaElem = _schemas.get(xsdSchema);
		String cTypeName = XsdUtils.getComplexTypeName(
			xdElem.getDef().getName(), xdElem.getName());
		XsdCType xsdCType = new XsdCType(xsdSchema, cTypeName);
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
	private Map<XdDef, XsdSchemaContainer> initSchemas(Map xdDefs) {
		Map<XdDef, XsdSchemaContainer> ret = new HashMap<>();
		Iterator it = xdDefs.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Entry) it.next();
			Set namespaces = XdUtils.getModelsNS((Element) entry.getValue());
			XdDef xdDef = (XdDef) entry.getKey();
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
				Iterator it2 = namespaces.iterator();
				while (it2.hasNext()) {
					String namespace = (String) it2.next();
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
	 * to schemas.
	 * @param schemaContainer schema container to initiate.
	 * @throws IllegalArgumentException if given schema container
	 * is unknown type.
	 */
	private void initSchema(XsdSchemaContainer schemaContainer) {
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
	private void initSingleSchema(XsdSchema schema) {
		Element schemaElem = createSchemaElem(schema.getTargetNS());
		if (schema.getTargetNS() != null) {
			Utils.addNamespaceDecl(schemaElem, "tns", schema.getTargetNS());
		}
		_schemas.put(schema, schemaElem);
	}

	/** Returns qualified name of given complexType reference
	 * representation in scope of one schema.
	 * @param xsdCType schema complexType representation object.
	 * @return complexType reference qualified name.
	 */
	public String getQName(XsdCType xsdCType) {
		XsdSchema schema = xsdCType.getSchema();
		Element schemaElem = _schemas.get(schema);
		return XsdUtils.getRefQName(schemaElem, xsdCType.getName()).getQName();
	}

	/** Returns qualified name of model from given external schema
	 * representation object with given local name from given schema element.
	 *
	 * @param mainSchemaElem schema element to put reference.
	 * @param external external schema representation with referred model.
	 * @param modelName local name of referred model.
	 * @return model qualified name.
	 */
	public String getQName(Element mainSchemaElem,
		XsdSchema external,
		String modelName) {
		return getRefQName(mainSchemaElem,
			external.getName(),	external.getTargetNS(), modelName).getQName();
	}

	/** Returns qualified name of reference to model. Resolves connection
	 * of schemas and creating name space declarations.
	 * @param schema schema element.
	 * @param extSchemaFileName file name of external schema.
	 * @param extSchemaTargetNS external schema target name space URI.
	 * @param modelName referenced model local name.
	 * @return  qualified name of reference.
	 * @throws RuntimeException if can't get referenced model qualified name.
	 */
	private MyQName getRefQName(Element schema,
		String extSchemaFileName,
		String extSchemaTargetNS,
		String modelName) {
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
			String prefix = Utils.getNSPrefix(schema, extSchemaTargetNS);
			if (prefix == null) {
				prefix=Utils.addNamespaceDeclaration(schema, extSchemaTargetNS);
			}
			return new MyQName(prefix, modelName);
		}
		throw new RuntimeException(
			"Could not get referenced model qualified name!");
	}

	/** Adds external schema declaration element (include or
	 * import depending on target name space) to given schema
	 * element and returns created and added declaration element.
	 * @param schema schema element to add external declaration to.
	 * @param extSchemaFileName file name of external schema.
	 * @param extSchemaTargetNS target name space URI of external schema or
	 * null.
	 * @return created and added external schema declaration element.
	 */
	private Element addExtSchemaDecl(Element schema,
		String extSchemaFileName,
		String extSchemaTargetNS) {
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
	public XsdSchema getExtNSSchema(String namespaceURI) {
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
	public XsdSchema getExtSchema() {
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
	public XsdSchema getExtSchema(String namespaceURI) {
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
	/** Creates XML Schema schema element with XML Schema name space
	 * declaration and adds schema target name space declaration if given
	 * target name space is not null.
	 * @param targetNamespace schema target name space or null.
	 * @return created schema element.
	 */
	public Element createSchemaElem(String targetNamespace) {
		Document doc = Utils.getBuilder().newDocument(
			XsdVersion.SCHEMA_1_0.getNSURI(),
				getSchemaNodeName(XsdNames.SCHEMA), null);
		Element schema = doc.getDocumentElement();
		Utils.addNamespaceDecl(schema, _schemaPrefix,
			XsdVersion.SCHEMA_1_0.getNSURI());
		if (targetNamespace != null && targetNamespace.length() != 0) {
			Utils.addAttr(schema, XsdNames.TARGET_NAMESPACE, targetNamespace);
		}
		return schema;
	}

	/** Adds XML Schema element declaration element with given default
	 * value, given fixed value, maximal occurrence and minimal occurrence,
	 * given name, nillable switch and given type name to given parent element.
	 * @param parent parent element.
	 * @param defaultValue element default value.
	 * @param fixedValue element fixed value.
	 * @param maxOccurs element maximal occurrence.
	 * @param minOccurs element minimal occurrence.
	 * @param name element local name.
	 * @param ref referenced element declaration qualified name.
	 * @param nillable nillable switch.
	 * @param type element type name.
	 * @param qualified qualified element name switch.
	 * @return created and added element declaration element.
	 */
	public Element addElementDecl(Element parent,
		String defaultValue,
		String fixedValue,
		Integer minOccurs, Integer maxOccurs,
		String name,
		String ref,
		Boolean nillable,
		String type,
		Boolean qualified) {
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
			minOccurs = 0; // must be optional
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
		if (minOccurs != null && minOccurs != 1) {
			addAttr(elemDecl, XsdNames.MIN_OCCURS, minOccurs.toString());
		}
		//resolving maxOccurs
		if (maxOccurs != null && maxOccurs != 1) {
			setMaxOccurs(elemDecl, maxOccurs);
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

	/** Adds XML Schema complexType declaration element with given name
	 * and given mixed switch to given parent element.
	 * @param parent parent element.
	 * @param name complex type name.
	 * @param mixed mixed type switch.
	 * @return created and added complexType element.
	 */
	public Element addComplexTypeDecl(Element parent,
		String name,
		Boolean mixed) {
		Element cTypeDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(),
			getSchemaNodeName(XsdNames.COMPLEX_TYPE));
		//resolving name
		if (name != null && name.length() != 0) {
			addAttr(cTypeDecl, XsdNames.NAME, name);
		}
		//resolvin mixed
		if (mixed != null && mixed != false) {
			addAttr(cTypeDecl, XsdNames.MIXED, "true");
		}
		parent.appendChild(cTypeDecl);
		return cTypeDecl;
	}

	/** Adds XML Schema any declaration element with given minimal and
	 * maximal occurrence to given parent element and returns created and added
	 * element.
	 * @param parent parent element.
	 * @param minOccurs minimal occurrence.
	 * @param maxOccurs maximal occurrence.
	 * @return created and added any declaration element.
	 */
	public Element addAnyDecl(Element parent,
		Integer minOccurs,
		Integer maxOccurs) {
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

	/** Returns annotation child element of given schema context
	 * element. If given parent element already has annotation child
	 * element, returns that element, otherwise creates one and returns it.
	 * @param parent parent element to add or get child annotation
	 * element.
	 * @return child annotation element.
	 */
	private Element addAnnotationDecl(Element parent) {
		Element annotationElem = XsdUtils.getAnnotationElem(parent);
		if (annotationElem == null) {
			annotationElem = parent.getOwnerDocument().createElementNS(
				XsdVersion.SCHEMA_1_0.getNSURI(),
				getSchemaNodeName(XsdNames.ANNOTATION));
			Utils.insertFirst(parent, annotationElem);
		}
		return annotationElem;
	}

	/** Adds documentation child element to given annotation
	 * element and returns added documentation element.
	 * @param annotationElem schema annotation element.
	 * @param documentation documentation string to add.
	 * @return created and added documentation element.
	 */
	private Element addDocumentationDecl(Element annotationElem,
		String documentation) {
		Element documentationElem =
			annotationElem.getOwnerDocument().createElementNS(
				XsdVersion.SCHEMA_1_0.getNSURI(),
				getSchemaNodeName(XsdNames.DOCUMENTATION));
		annotationElem.appendChild(documentationElem);
		Text documentationText =
			documentationElem.getOwnerDocument().createTextNode(documentation);
		documentationElem.appendChild(documentationText);
		return documentationElem;
	}

	/** Adds documentation element to annotation child element
	 * of given schema context element.
	 * @param contextElem schema context element to add documentation to.
	 * @param documentation documentation string to add.
	 */
	public void addDocumentation(Element contextElem, String documentation) {
		if (_genDocumentation) {
			Element annotElem = addAnnotationDecl(contextElem);
			addDocumentationDecl(annotElem, documentation);
		}
	}

	/** Adds XML Schema anyAttribute declaration element to given
	 * parent element.
	 * @param parent parent element.
	 * @return created and added anyAttribute declaration element.
	 */
	public Element addAnyAttrDecl(Element parent) {
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

	/** Adds XML Schema attribute declaration element with given
	 * default value, fixed value, local name, type name and use to given
	 * parent element and returns created and added element.
	 * @param parent parent element.
	 * @param defaultValue default attribute value.
	 * @param fixedValue fixed attribute value.
	 * @param name attribute local name.
	 * @param ref reference string.
	 * @param type attribute value type.
	 * @param use attribute use.
	 * @param qualified qualified attribute name switch.
	 * @return created and added attribute declaration element.
	 */
	public Element addAttributeDecl(Element parent, String defaultValue,
		String fixedValue, String name, String ref, String type, String use,
		Boolean qualified) {
		Element attrDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(),
			getSchemaNodeName(XsdNames.ATTRIBUTE));
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

	/** Adds XML Schema attributeGroup declaration element with given
	 * name or given reference name to given parent element.
	 * @param parent parent element.
	 * @param name attribute group local name.
	 * @param ref attribute group reference name.
	 * @return created and added attributeGroup declaration element.
	 */
	public Element addAttrGroupDecl(Element parent, String name, String ref) {
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

	/** Adds XML Schema complexContent declaration element with given
	 * mixed switch to given parent element and returns added element.
	 * @param parent parent element.
	 * @param mixed mixed content switch.
	 * @return created and added complexContent declaration element.
	 */
	public Element addComplexContentDecl(Element parent, Boolean mixed) {
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

	/** Adds XML Schema choice declaration element with given minimal
	 * and maximal occurrence to given parent element and returns created and
	 * added element.
	 * @param parent parent element.
	 * @param minOccurs minimal occurrence.
	 * @param maxOccurs maximal occurrence.
	 * @return created and added choice declaration element.
	 */
	public Element addChoiceDecl(Element parent,
		Integer minOccurs,
		Integer maxOccurs) {
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

	/** Adds XML Schema extension declaration element with given
	 * base type to given parent element and returns created and added element.
	 * @param parent parent element.
	 * @param base extension base type name.
	 * @return created and added extension declaration element.
	 */
	public Element addExtensionDecl(Element parent, String base) {
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

	/** Adds XML Schema group declaration element with given name,
	 * given reference, given minimal and maximal occurrence and returns
	 * created and added element.
	 * @param parent parent element.
	 * @param name group name.
	 * @param ref reference name.
	 * @param minOccurs minimal occurrence.
	 * @param maxOccurs maximal occurrence.
	 * @return created and added group declaration element.
	 */
	public Element addGroupDecl(Element parent,
		String name,
		String ref,
		Integer minOccurs,
		Integer maxOccurs) {
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
		//todo insert before attribute groups
		if (!XsdUtils.isSchema(parent)) {
			insertBeforeAttributes(parent, groupDecl);
		} else {
			parent.appendChild(groupDecl);
		}
		return groupDecl;
	}

	/** Adds XML Schema import declaration element with given
	 * name space and schema location to given parent element and returns
	 * created and added element.
	 * @param parent parent element.
	 * @param namespace imported name space.
	 * @param schemaLocation imported schema location.
	 * @return created and added import declaration element.
	 */
	public Element addImportDecl(Element parent,
		String namespace,
		String schemaLocation) {
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
		Utils.insertFirst(parent, importDecl);
		return importDecl;
	}

	/** Adds XML Schema includeImport declaration element with given schema
	 * location to given parent element and returns created and added element.
	 * @param parent parent element.
	 * @param schemaLocation included schema location.
	 * @return created and added includeImport declaration element.
	 */
	public Element addIncludeDecl(Element parent, String schemaLocation) {
		Element includeDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(),
			getSchemaNodeName(XsdNames.INCLUDE));
		//resolving schema location
		if (schemaLocation != null && schemaLocation.length() != 0) {
			addAttr(includeDecl, XsdNames.SCHEMA_LOCATION, schemaLocation);
		}
		Utils.insertFirst(parent, includeDecl);
		return includeDecl;
	}

	/** Adds XML Schema restriction declaration element with given base
	 * type name to given parent element and returns created and added element.
	 * @param parent parent element.
	 * @param base restriction base type name.
	 * @return created and added restriction element.
	 */
	public Element addRestrictionDecl(Element parent, String base) {
		Element restrictionDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(),
			getSchemaNodeName(XsdNames.RESTRICTION));
		//resolving base
		if (base != null && base.length() != 0) {
			addAttr(restrictionDecl, XsdNames.BASE, base);
		}
		parent.appendChild(restrictionDecl);
		return restrictionDecl;
	}

	/** Adds XML Schema sequence declaration element with given
	 * minimal and maximal occurrence to given parent element and returns
	 * created and added element.
	 * @param parent parent element.
	 * @param minOccurs sequence minimal occurrence.
	 * @param maxOccurs sequence maximal occurrence.
	 * @return created and added sequence declaration element.
	 */
	public Element addSequenceDecl(Element parent, Integer minOccurs,
		Integer maxOccurs) {
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

	/** Adds XML Schema simpleContent declaration element to given
	 * parent element and returns created and added element.
	 * @param parent parent element.
	 * @return created and added simpleContent element.
	 */
	public Element addSimpleContentDecl(Element parent) {
		Element simpleContDecl = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(),
			getSchemaNodeName(XsdNames.SIMPLE_CONTENT));
		parent.appendChild(simpleContDecl);
		return simpleContDecl;
	}

	/** Adds XML Schema simpleType declaration element with given
	 * simple type name to given parent element and returns created and added
	 * element.
	 * @param parent parent element.
	 * @param name simple type name .
	 * @return created and added simpleType declaration element.
	 */
	public Element addSimpleTypeDecl(Element parent, String name) {
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

	/** Adds XML Schema union declaration element with given member
	 * types string to given parent element and returns created and added
	 * union declaration element.
	 * @param parent parent element to add union declaration to.
	 * @param memberTypes member types string.
	 * @return created and added union declaration element.
	 */
	public Element addUnionDecl(Element parent, String memberTypes) {
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

	/** Adds XML Schema list declaration element with given item type
	 * qualified name to given parent element and returns created and added
	 * list declaration element.
	 * @param parent parent element ot add list declaration to.
	 * @param itemType item type qualified name.
	 * @return created and added list declaration element.
	 */
	public Element addListDecl(Element parent, String itemType) {
		Element listElem = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(), getSchemaNodeName(XsdNames.LIST));
		//resolving item type
		if (itemType != null && itemType.length() != 0) {
			addAttr(listElem, XsdNames.ITEM_TYPE, itemType);
		}
		parent.appendChild(listElem);
		return listElem;
	}

	/** Adds XML Schema simple type restriction facet declaration element with
	 * given facet name and given facet value to given parent node and
	 * returns created and added facet declaration element.
	 * @param parent parent element to add facet declaration to.
	 * @param name name of facet.
	 * @param value facet value.
	 * @return created and added facet declaration element.
	 */
	public Element addFacet(Element parent, String name, String value) {
		Element facetElem = parent.getOwnerDocument().createElementNS(
			XsdVersion.SCHEMA_1_0.getNSURI(), getSchemaNodeName(name));
		addAttr(facetElem, XsdNames.VALUE, value);
		parent.appendChild(facetElem);
		return facetElem;
	}

	/** Adds mixed attribute to given schema complexType
	 * element with given value.
	 * @param complexType schema complexType element.
	 * @param mixed value for mixed attribute.
	 */
	public void setMixed(Element complexType, boolean mixed) {
		Utils.setAttr(complexType, XsdNames.MIXED, String.valueOf(mixed));
	}

	/** Adds minOccurs attribute to given element with given value.
	 * @param element element ot add attribute.
	 * @param minOccurs minimal occurrence.
	 */
	public void setMinOccurs(Element element, int minOccurs) {
		Utils.setAttr(element, XsdNames.MIN_OCCURS,Integer.toString(minOccurs));
	}

	/** Adds maxOccurs attribute to given element with given value.
	 * @param element element to add attribute.
	 * @param maxOccurs maximal occurrence.
	 */
	public void setMaxOccurs(Element element, int maxOccurs) {
		String maxOccursString;
		if (Occurrence.UNBOUNDED == maxOccurs) {
			maxOccursString = XsdNames.UNBOUNDED;
		} else {
			maxOccursString = Integer.toString(maxOccurs);
		}
		Utils.setAttr(element, XsdNames.MAX_OCCURS, maxOccursString);
	}

	/** Adds type attribute to given element with given type name.
	 * @param element element to add type attribute.
	 * @param type type name to add.
	 */
	public void setType(Element element, String type) {
		Utils.setAttr(element, XsdNames.TYPE, type);
	}

	/** Adds or sets memberTypes attribute with given string.
	 * @param unionElem schema union element to add member types.
	 * @param memberTypes member types string to add.
	 */
	public void setMemeberTypes(Element unionElem, String memberTypes) {
		Utils.setAttr(unionElem, XsdNames.MEMBER_TYPES, memberTypes);
	}

	/** Returns XML Schema qualified name according to given schema type local
	 * name.
	 * @param typeLocalName local name of type.
	 * @return type qualified name.
	 * @throws NullPointerException if given type local name is null.
	 * @throws IllegalArgumentException if given type local name is empty.
	 */
	public String getSchemaTypeQName(String typeLocalName) {
		if (typeLocalName == null) {
			throw new NullPointerException("Given type local name is null!");
		}
		if (typeLocalName.length() == 0) {
			throw new IllegalArgumentException(
				"Given type local name is empty!");
		}
		return getSchemaNodeName(typeLocalName);
	}

	/** Adds type attribute to given element with given schema type
	 * local name.
	 * @param element element to add type attribute.
	 * @param schemaType schema type local name.
	 */
	public void setSchemaType(Element element, String schemaType) {
		Utils.setAttr(element, XsdNames.TYPE, getSchemaNodeName(schemaType));
	}

	/** Adds attribute node with given local name and given value to given
	 * parent element.
	 * @param parent parent element.
	 * @param name attribute local name.
	 * @param value attribute value.
	 * @return created and added attribute node.
	 */
	private Attr addAttr(Element parent, String name, String value) {
		return Utils.addAttr(parent, name, value);
	}

	/** Inserts given element node before attribute nodes declarations to given
	 * parent element.
	 * @param parent parent element to add element.
	 * @param element element to add.
	 */
	private void insertBeforeAttributes(Element parent, Element element) {
		Element attrDecl = Utils.firstElementChildNS(parent,
			XsdVersion.SCHEMA_1_0.getNSURI(),
			new String[]{XsdNames.ANY_ATTRIBUTE,
				XsdNames.ATTRIBUTE,
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
	public Map<XsdModel, Element> getModels() {return _models;}

	/** Schemas map getter.
	 * @return schemas map (XsdSchema) to (Element).
	 */
	public Map<XsdSchema, Element> getSchemas() {return _schemas;}

	/** External attribute group counter getter.
	 * @return external attribute group counter.
	 */
	public int getExtAttrGrpCounter() {return _extAttrGrpCounter++;}

	/** External group counter getter.
	 * @return external group counter.
	 */
	public int getExtGroupCounter() {return _extGroupCounter++;}

	/** External simple type name counter getter.
	 * @return external simple type name counter.
	 */
	public int getExtSTypeCounter() {return _extSTypeCounter++;}

	public XsdVersion getVersion() {return XsdVersion.SCHEMA_1_0;}

	public Map<String, Document> getSchemaDocuments() {
		Map<String, Document> ret = new HashMap<>();
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

	/** Creates schema node qualified name according to schema nodes prefix.
	 * @param nodeLocalName schema node local name.
	 * @return schema node qualified name.
	 */
	protected final String getSchemaNodeName(String nodeLocalName) {
		return Utils.getNodeQName(_schemaPrefix, nodeLocalName);
	}

	/** Creates full schema file name according to schema files extension.
	 * @param schemaName name of schema.
	 * @return full schema fiel name with extension.
	 */
	protected final String getSchemaFileName(String schemaName) {
		return schemaName + "." + _schemaFileExt;
	}
}