package org.xdef.impl.util.conv.xsd.xsd_1_0;

import org.xdef.impl.util.conv.Util;
import org.xdef.impl.util.conv.Util.MyQName;
import org.xdef.impl.util.conv.xsd.doc.XsdVersion;
import org.xdef.xml.KXmlUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Provides static methods for working with XML Schema version 1.0 document.
 * @author Ilia Alexandrov
 */
public final class XsdUtils {

	/** Private constructor. */
	private XsdUtils() {}

	/** Creates external schema name according to given external schema counter.
	 * @param extSchemaCounter counter of external schemas.
	 * @return external schema name.
	 */
	public static String getExtSchemaName(int extSchemaCounter) {
		return "extSchema_" + Integer.toString(extSchemaCounter);
	}

	/** Creates external group name according to given external group counter.
	 * @param extGroupCounter external group counter.
	 * @return external group name.
	 */
	public static String getExtGroupName(int extGroupCounter) {
		return "group_" + Integer.toString(extGroupCounter);
	}

	/** Creates external attribute group name according to given external
	 * atribute group counter.
	 * @param extAttrGrpCounter counter of external attribute groups.
	 * @return external attribute group name.
	 */
	public static String getExtAttrGrpName(int extAttrGrpCounter) {
		return "attrGrp_" + Integer.toString(extAttrGrpCounter);
	}

	/** Creates external simple type name according to given element local name
	 * and external simple name counter.
	 * @param elemLocalName element local name.
	 * @param counter simple type name counter.
	 * @return external simple type name.
	 */
	public static String getExtSTypeName(String elemLocalName, int counter) {
		return elemLocalName + "_sType_" + Integer.toString(counter);
	}

	/** Creates schema name of group declarations according to given def name.
	 * @param defName def name.
	 * @return schema name of group declarations.
	 */
	public static String getGroupSchemaName(String defName) {
		checkString(defName);
		return defName + "_groups";
	}

	/** Creates schema name of simple type declartions according to given def name.
	 * @param defName def name.
	 * @return schema name of simple type declartions.
	 */
	public static String getSTypeSchemaName(String defName) {
		checkString(defName);
		return defName + "_sTypes";
	}

	/** Creates schema name of element declaration according to given def name
	 * and element name.
	 * @param defName def name.
	 * @param elemName element name.
	 * @return schema name of element declaration.
	 */
	public static String getElemSchemaName(String defName, String elemName) {
		checkString(defName);
		checkString(elemName);
		return defName + "_" + elemName;
	}

	/** Creates full simple type name according to given XDefinition name and
	 * simple type name.
	 * @param defName XDefinition name.
	 * @param sTypeName simple type name.
	 * @return full simple type name.
	 */
	public static String getSTypeName(String defName, String sTypeName) {
		checkString(defName);
		checkString(sTypeName);
		return defName + "_" + sTypeName + "_sType";
	}

	/** Creates full group name according to given XDefinition name, group name
	 * and group type.
	 * @param defName XDefinition name.
	 * @param groupName group name.
	 * @param groupType group type.
	 * @return full group name.
	 */
	public static String getGroupName(String defName,
		String groupName,
		String groupType) {
		checkString(defName);
		checkString(groupName);
		checkString(groupType);
		return defName + "_" + groupName + "_" + groupType;
	}

	/** Creates full complex type name according to given XDefinition name and
	 * complex type name.
	 * @param defName XDefinition name.
	 * @param cTypeName complex type name.
	 * @return full complex type name.
	 */
	public static String getComplexTypeName(String defName, String cTypeName) {
		checkString(defName);
		checkString(cTypeName);
		return defName + "_" + cTypeName + "_cType";
	}

	/** Creates full simple type name according to given XDefinition name and
	 * model name.
	 * @param defName XDefinition name.
	 * @param modelName model name.
	 * @return full simple type name;
	 */
	public static String getModelSimpleTypeName(final String defName,
		final String modelName) {
		checkString(defName);
		checkString(modelName);
		return defName + "_" + modelName + "_sType";
	}

	/** Checks given string.
	 * @param string string to check.
	 * @throws NullPointerException if given string is <tt>null</tt>.
	 * @throws IllegalArgumentException if given string is empty.
	 */
	private static void checkString(String string) {
		if (string == null) {
			throw new NullPointerException("Given string is null!");
		}
		if (string.length() == 0) {
			throw new IllegalArgumentException("Given string is empty!");
		}
	}

	/** Get external schema declaration (<tt>import</tt> or <tt>include</tt>)
	 * element of given external schema file name in given main schema element.
	 * @param mainSchema main schema element to search in.
	 * @param extSchemaFileName external schema file name to search for.
	 * @return external schema declaration or <tt>null</tt> if given schema does
	 * not contain given external schema declaration.
	 * @throws NullPointerException if given main schema element or external
	 * schema file name is <tt>null</tt>.
	 * @throws IllegalArgumentException if given external schema file name is
	 * empty.
	 */
	public static Element getExtSchemaDecl(final Element mainSchema,
		final String extSchemaFileName) {
		if (mainSchema == null) {
			throw new NullPointerException("Given main schema is null!");
		}
		if (extSchemaFileName == null) {
			throw new NullPointerException(
				"Given external schema file name is null!");
		}
		if (extSchemaFileName.length() == 0) {
			throw new IllegalArgumentException(
				"Given external schema file name is empty!");
		}
		NodeList extDecls = KXmlUtils.getChildElementsNS(mainSchema,
			XsdVersion.SCHEMA_1_0.getNSURI(),
			new String[]{XsdNames.IMPORT, XsdNames.INCLUDE});
		for (int i = 0; i < extDecls.getLength(); i++) {
			Element extDecl = (Element) extDecls.item(i);
			if (extSchemaFileName.equals(
				extDecl.getAttribute(XsdNames.SCHEMA_LOCATION))) {
				return extDecl;
			}
		}
		return null;
	}

	/** Returns qualified name of model contained in given schema element
	 * and has given local name.
	 * @param schema schema element.
	 * @param modelName model name.
	 * @return qualified reference name.
	 */
	public static MyQName getRefQName(Element schema, String modelName) {
		String targetNS = getSchemaTargetNS(schema);
		if (targetNS == null) {
			return new MyQName(modelName);
		}
		return new MyQName(Util.getNSPrefix(schema, targetNS), modelName);
	}

	/** Returns XML Schema <tt>annotation</tt> child element of given schema
	 * context element or <tt>null</tt> if given context element has no
	 * <tt>annotation</tt> element as child element.
	 * @param schemaContextElem schema context element.
	 * @return child <tt>annotation</tt> element or <tt>null</tt>.
	 */
	public static Element getAnnotationElem(final Element schemaContextElem) {
		NodeList children = KXmlUtils.getChildElementsNS(schemaContextElem,
				XsdVersion.SCHEMA_1_0.getNSURI());
		for (int i = 0; i < children.getLength(); i++) {
			Element child = (Element) children.item(i);
			if (Util.isElement(child,
				XsdVersion.SCHEMA_1_0.getNSURI(), XsdNames.ANNOTATION)) {
				return child;
			}
		}
		return null;
	}

	/** Returns <tt>true</tt> if given node is valid XML Schema <tt>schema</tt>
	 * element.
	 * @param node node to test.
	 * @return <tt>true</tt> if given node is valid XML Schema <tt>schema</tt>
	 * element.
	 */
	public static final boolean isSchema(final Node node) {
		return Util.isElement(node,
			XsdVersion.SCHEMA_1_0.getNSURI(), XsdNames.SCHEMA);
	}

	/** Returns <tt>true</tt> if given node is a valid schema
	 * <tt>complexType</tt> declaration element.
	 * @param node node to test.
	 * @return <tt>true</tt> if given node is a valid schema
	 * <tt>complexType</tt> declaration element.
	 */
	public static final boolean isComplexType(final Node node) {
		return Util.isElement(node,
			XsdVersion.SCHEMA_1_0.getNSURI(), XsdNames.COMPLEX_TYPE);
	}

	/** Gets ancestor <tt>schema</tt> element of given node.
	 * @param node schema descendant node.
	 * @return ancestor <tt>schema</tt> element.
	 * @throws NullPointerException if given node is <tt>null</tt>.
	 * @throws IllegalArgumentException if given node is not a valid
	 * <tt>schema</tt> descendant.
	 */
	public static final Element getAncestorSchema(final Node node) {
		if (isSchema(node)) {
			return (Element) node;
		}
		if (Node.ATTRIBUTE_NODE == node.getNodeType()) {
			Attr attr = (Attr) node;
			Element parent = attr.getOwnerElement();
			return getAncestorSchema(parent);
		} else {
			Node parent = node.getParentNode();
			if (Node.DOCUMENT_NODE != parent.getNodeType()) {
				return getAncestorSchema(parent);
			}
		}
		throw new IllegalArgumentException(
			"Given node is not a valid schema descendant node!");
	}

	/** Gets <tt>targetNamesapce</tt> attribute value of given <tt>schema</tt>
	 * element.
	 * @param schema <tt>schema</tt> element to get target namesapce from.
	 * @return target namespace URI or <tt>null</tt> if given schema does not
	 * contain <tt>targetNamespace</tt> attribute.
	 * @throws NullPointerException if given schema element is <tt>null</tt>.
	 * @throws IllegalArgumentException if given element is not a valid
	 * <tt>schema</tt> element.
	 */
	public static final String getSchemaTargetNS(final Element schema) {
		if (schema == null) {
			throw new NullPointerException("Given schema element is null!");
		}
		if (!isSchema(schema)) {
			throw new IllegalArgumentException(
				"Given element is not a valid schema element!");
		}
		String targetNS = schema.getAttribute(XsdNames.TARGET_NAMESPACE);
		if (targetNS == null || targetNS.length() == 0) {
			return null;
		}
		return targetNS;
	}

	/** Get <tt>targetNamespace</tt> attribute value of ancestor <tt>schema</tt>
	 * element of given schema context element.
	 * @param schemaContext schema context element.
	 * @return <tt>targetNamespace</tt> attribute value.
	 * @throws NullPointerException if given schema context element is null.
	 */
	public static final String getTargetNS(final Element schemaContext) {
		if (schemaContext == null) {
			throw new NullPointerException(
				"Given schema context element is null!");
		}
		Element schema = getAncestorSchema(schemaContext);
		return getSchemaTargetNS(schema);
	}

	/** Returns <tt>true</tt> if given schema context element contains
	 * <tt>attribute</tt> declaration element with given attribute name.
	 * @param schemaContext schema context element to search in.
	 * @param attrLocalName attribute local name.
	 * @return <tt>true</tt> if given schema context element contains
	 */
	public static final boolean hasAttributeDecl(final Element schemaContext,
		final String attrLocalName) {
		return hasDeclaration(schemaContext, XsdNames.ATTRIBUTE, attrLocalName);
	}

	/** Returns <tt>true</tt> if given schema context element contains
	 * <tt>element</tt> declaration element with given name.
	 * @param schemaContext schema context element to search in.
	 * @param elementLocalName element local name.
	 * @return <tt>true</tt> if given schema context element contains
	 * <tt>element</tt> declaration element with given name.
	 */
	public static final boolean hasElementDecl(final Element schemaContext,
		final String elementLocalName) {
		return hasDeclaration(schemaContext, XsdNames.ELEMENT,elementLocalName);
	}

	/** Returns <tt>true</tt> if given schema context element contains
	 * declaration of given type and with given local name.
	 * @param schemaContext schema context element to search in.
	 * @param nodeType node declaration type to search.
	 * @param localName node local name.
	 * @return <tt>true</tt> if given schema context element contains
	 * declaration of given type and with given local name.
	 * @throws NullPointerException if given schema context element, node type
	 * or attribute local name is <tt>null</tt>.
	 * @throws IllegalArgumentException if given node type or attribute local
	 * name is empty.
	 */
	private static final boolean hasDeclaration(final Element schemaContext,
		final String nodeType,
		final String localName) {
		if (schemaContext == null) {
			throw new NullPointerException(
				"Given schema context element is null!");
		}
		if (nodeType == null) {
			throw new NullPointerException("Given node type is null!");
		}
		if (nodeType.length() == 0) {
			throw new IllegalArgumentException("Given node type is empty!");
		}
		if (localName == null) {
			throw new NullPointerException(
				"Given attribute local name is null!");
		}
		if (localName.length() == 0) {
			throw new IllegalArgumentException(
				"Given attribute local name is empty!");
		}
		NodeList attrDecls = KXmlUtils.getChildElementsNS(schemaContext,
			XsdVersion.SCHEMA_1_0.getNSURI(), nodeType);
		for (int i = 0; i < attrDecls.getLength(); i++) {
			Element attrDecl = (Element) attrDecls.item(i);
			if (localName.equals(attrDecl.getAttribute(XsdNames.NAME))) {
				return true;
			}
		}
		return false;
	}

	/** Representation of an element.
	 * @author Alexandrov
	 */
	public static class ElemProps {
		/** Name of element. */
		private String _name;
		/** Namespace of element. */
		private String _namespace;
		/** String containing refference to another element. */
		private String _ref;
		/** Fixed value of element. */
		private String _fixed;
		/** Default value of element. */
		private String _default;
		/** ElemProps occurrence string. */
		private String _occurrence;
		/** Declaration of text node. */
		private String _text;
		/** Is nillable. */
		private boolean _nillable = false;
		/** Any type switch. */
		private boolean _isAnyType = false;

		/** Creates empty element. */
		public ElemProps() {}

		/** ElemProps default value getter.
		 * @return default value or null.
		 */
		public String getDefault() {return _default;}

		/** ElemProps default value setter.
		 * @param defaultValue element default value.
		 */
		public void setDefault(String defaultValue) {_default = defaultValue;}

		/** ElemProps fixed value getter.
		 * @return fixed value or null.
		 */
		public String getFixed() {return _fixed;}

		/** ElemProps fixed value setter.
		 * @param fixed fixed value.
		 */
		public void setFixed(String fixed) {_fixed = fixed;}

		/** ElemProps local name getter.
		 * @return local name of element.
		 */
		public String getName() {return _name;}

		/** ElemProps local name setter.
		 * @param name local name of element.
		 */
		public void setName(String name) {_name = name;}

		/** ElemProps namespace getter.
		 * @return element namespace or null.
		 */
		public String getNamespace() {return _namespace;}

		/** ElemProps namespace setter.
		 * @param namespace element namesapce.
		 */
		public void setNamespace(String namespace) {_namespace = namespace;}

		/** Nillable switch getter.
		 * @return nillable switch value.
		 */
		public boolean isNillable() {return _nillable;}

		/** Nillable switch setter.
		 * @param nillable nillable switch value.
		 */
		public void setNillable(boolean nillable) {_nillable = nillable;}

		/** ElemProps occurrence string getter.
		 * @return element occurrence string or null.
		 */
		public String getOccurrence() {return _occurrence;}

		/** ElemProps occurrence string setter.
		 * @param occurrence element occurrence string.
		 */
		public void setOccurrence(String occurrence) {_occurrence = occurrence;}

		/** ElemProps ref string getter.
		 * @return ref string or null.
		 */
		public String getRef() {return _ref;}

		/** ElemProps ref string setter.
		 * @param ref ref string.
		 */
		public void setRef(String ref) {_ref = ref;}

		/** ElemProps text content declaration getter.
		 * @return text content declaration string or null.
		 */
		public String getText() {return _text;}

		/** ElemProps text content declaration setter.
		 * @param text content declaration string.
		 */
		public void setText(String text) {_text = text;}

		/** Any type switch value getter.
		 * @return any type switch value.
		 */
		public boolean isAnyType() {return _isAnyType;}

		/** Any type switch value setter.
		 * @param isAnyType any type switch value.
		 */
		public void setAnyType(boolean isAnyType) {_isAnyType = isAnyType;}
	}

	/** Representation of an attribute.
	 * @author Alexandrov
	 */
	public static class AttrProps {
		/** AttrProps name. */
		private String _name;
		/** AttrProps namespace. */
		private String _namespace;
		/** AttrProps value type. */
		private String _type = "string(0, $MAXINT)";
		/** AttrProps default value. */
		private String _default;
		/** AttrProps fixed value. */
		private String _fixed;
		/** AttrProps occurrence. */
		private String _use = "optional";

		/** Creates empty attribute representation object. */
		public AttrProps() {}

		/** Default attribute value setter.
		 * @param defaultValue default attribute value.
		 */
		public void setDefault(String defaultValue) {
			_default = defaultValue;
			_use = "optional";
		}

		/** Fixed attribute value setter.
		 * @param fixed fixed attribute value.
		 */
		public void setFixed(String fixed) {_fixed = fixed;}

		/** AttrProps name setter.
		 * @param name name of attribute
		 */
		public void setName(String name) {_name = name;}

		/** AttrProps namespace setter.
		 * @param namespace attribute namespace.
		 */
		public void setNamespace(String namespace) {_namespace = namespace;}

		/** AttrProps type declaration setter.
		 * @param type attribute type declaration.
		 */
		public void setType(String type) {_type = type;}

		/** Use declaration setter.
		 * @param use use declaration.
		 */
		public void setUse(String use) {_use = use;}

		/** Default value getter.
		 * @return default value or null.
		 */
		public String getDefault() {return _default;}

		/** Fixed value getter.
		 * @return fixed value or null.
		 */
		public String getFixed() {return _fixed;}

		/** AttrProps name getter.
		 * @return name of attribute.
		 */
		public String getName() {return _name;}

		/** AttrProps namespace getter.
		 * @return namespace of attribute.
		 */
		public String getNamespace() {return _namespace;}

		/** AttrProps type getter.
		 * @return type declaration of attribute.
		 */
		public String getType() {return _type;}

		/** AttrProps occurrence getter.
		 * @return occurrence declaration.
		 */
		public String getUse() {return _use;}
	}
}