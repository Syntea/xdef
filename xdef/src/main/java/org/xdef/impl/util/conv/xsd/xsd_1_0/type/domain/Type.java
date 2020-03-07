package org.xdef.impl.util.conv.xsd.xsd_1_0.type.domain;

import org.xdef.xml.KXmlUtils;
import org.xdef.impl.util.conv.xsd2xd.schema_1_0.Utils;
import org.xdef.impl.util.conv.xsd2xd.xdef_2_0.GlobalDeclaration;
import java.net.URL;
import java.util.Map;
import org.w3c.dom.*;

/** Represents any XML schema data type.
 * @author Alexandrov
 */
public abstract class Type {

	/** Returns instance of type declaration given to type name and element,
	 * containing reference to that type.
	 * @param fullName full name of type.
	 * @param element element containing reference to type.
	 * @param schemaURL URL of schema containing reference to type.
	 * @param schemaElements all schema elements table.
	 * @return instance of type declaration.
	 */
	public static Type getType(String fullName, Element element, URL schemaURL,
		Map<URL, Element> schemaElements) {
		String prefix = KXmlUtils.getQNamePrefix(fullName);
		String namespace = KXmlUtils.getNSURI(prefix, element);
		//namespace is XML schema namespace - base type
		if (Utils.NSURI_SCHEMA.equals(namespace)) {
			return new BaseType(KXmlUtils.getQNameLocalpart(fullName));
		} else {
			GlobalDeclaration declaration =
				GlobalDeclaration.getGlobalDeclaration(
					fullName, Utils.SIMPLE_TYPE, schemaURL, schemaElements);
			return new SimpleType(declaration.getGlobalDeclarationElement(),
				schemaURL, schemaElements, declaration.isRedefined());
		}
	}

	/** Returns instance of type declaration given to complex type element.
	 * @param complexTypeElement <tt>complexType</tt> element.
	 * @param schemaURL URL of schema containing element.
	 * @param schemaElements table of all schema elements.
	 * @return type object.
	 */
	public static Type getType(Element complexTypeElement, URL schemaURL,
		Map<URL, Element> schemaElements) {
		//getting context element
		Element content = KXmlUtils.firstElementChildNS(complexTypeElement,
			Utils.NSURI_SCHEMA, new String[]{Utils.SIMPLE_CONTENT,
				Utils.COMPLEX_CONTENT});
		//content is not present
		if (content == null) {
			return null;
		}
		//getting derived
		Element derivation = KXmlUtils.firstElementChildNS(content,
			Utils.NSURI_SCHEMA, new String[]{Utils.RESTRICTION,
				Utils.EXTENSION});
		//restriciton or extension not present
		if (derivation == null) {
			throw new RuntimeException(
				"Elements <simpleContent> or <complexContent> need to"
				+ " have element <extension> or <restriction> declared");
		}
		String base = derivation.getAttribute("base");
		String prefix = KXmlUtils.getQNamePrefix(base);
		String namespace;
		if ("".equals(prefix)) {
			//default namespace
			namespace = KXmlUtils.getNSURI("", content);
		} else {
			namespace = KXmlUtils.getNSURI(prefix, content);
		}
		//is schema type
		if (Utils.NSURI_SCHEMA.equals(namespace)) {
			String type = KXmlUtils.getQNameLocalpart(base);
			if ("anyType".equals(type)) {
				// no simple type
				return null;
			}
			//base type
			return new BaseType(type);
		}
		//getting base type
		GlobalDeclaration decl;
		try {
			if (Utils.isRedefineSchemaChild(complexTypeElement)) {
				decl = GlobalDeclaration.getGlobalDeclarationInRedefinedSchema(
					KXmlUtils.getQNameLocalpart(base), Utils.COMPLEX_TYPE,
					schemaURL, (Element) complexTypeElement.getParentNode(),
					schemaElements);
			} else {
				decl = GlobalDeclaration.getGlobalDeclaration(
					base, Utils.COMPLEX_TYPE, schemaURL, schemaElements);
			}
		} catch (RuntimeException ex) {
			decl = GlobalDeclaration.getGlobalDeclaration(
				base, Utils.SIMPLE_TYPE, schemaURL, schemaElements);
		}
		if (Utils.EXTENSION.equals(derivation.getLocalName())) {
			if (Utils.COMPLEX_TYPE.equals(decl.getType())) {
				return getType(decl.getGlobalDeclarationElement(),
					decl.getSchemaURL(), schemaElements);
			} else {
				return new SimpleType(decl.getGlobalDeclarationElement(),
					decl.getSchemaURL(), schemaElements, decl.isRedefined());
			}
		}
		Type baseType;
		if (Utils.COMPLEX_TYPE.equals(decl.getType())) {
			baseType = getType(decl.getGlobalDeclarationElement(),
				decl.getSchemaURL(), schemaElements);
		} else {
			baseType = getType(base, derivation, schemaURL, schemaElements);
		}
		if (baseType == null) {
			return null;
		}
		Restriction restriction = new Restriction(derivation, baseType);
		return new SimpleType(restriction);
	}
	/** Type name. */
	protected String _name;

	/** Type name getter.
	 * @return  type name.
	 */
	public String getName() {return _name;}

	/** Returns XDefinition style type definition.
	 * @return  String representing type method.
	 */
	public abstract String getTypeMethod();
}