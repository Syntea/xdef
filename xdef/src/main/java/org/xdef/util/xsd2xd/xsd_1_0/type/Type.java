package org.xdef.util.xsd2xd.xsd_1_0.type;

import org.xdef.xml.KDOMUtils;
import org.xdef.util.xsd2xd.Utils;
import org.xdef.util.xsd2xd.DOMUtils;
import org.xdef.util.xsd2xd.xd.GlobalDeclaration;
import java.net.URL;
import java.util.Map;
import org.w3c.dom.*;

/** Represents any XML schema data type. */
public abstract class Type {

	/** Type name. */
	protected String _name;

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
		String prefix = KDOMUtils.getQNamePrefix(fullName);
		String namespace = KDOMUtils.getNSURI(prefix, element);
		//namespace is XML schema namespace - base type
		if (Utils.NSURI_SCHEMA.equals(namespace)) {
			return new BaseType(KDOMUtils.getQNameLocalpart(fullName));
		} else {
			GlobalDeclaration declaration =
				GlobalDeclaration.getGlobalDeclaration(
					fullName, Utils.SIMPLE_TYPE, schemaURL, schemaElements);
			return new SimpleType(declaration.getGlobalDeclarationElement(),
				schemaURL, schemaElements, declaration.isRedefined());
		}
	}

	/** Returns instance of type declaration given to complex type element.
	 * @param complexTypeElement complexType element.
	 * @param schemaURL URL of schema containing element.
	 * @param schemaElements table of all schema elements.
	 * @return type object.
	 */
	public static Type getType(Element complexTypeElement, URL schemaURL,
		Map<URL, Element> schemaElements) {
		//getting context element
		Element content = DOMUtils.firstElementChildNS(complexTypeElement,
			Utils.NSURI_SCHEMA, new String[]{Utils.SIMPLE_CONTENT,
				Utils.COMPLEX_CONTENT});
		//content is not present
		if (content == null) {
			return null;
		}
		//getting derived
		Element derivation = DOMUtils.firstElementChildNS(content,
			Utils.NSURI_SCHEMA, new String[]{Utils.RESTRICTION,
				Utils.EXTENSION});
		//restriciton or extension not present
		if (derivation == null) {
			throw new RuntimeException(
				"Elements <simpleContent> or <complexContent> need to"
				+ " have element <extension> or <restriction> declared");
		}
		String base = derivation.getAttribute("base");
		String prefix = DOMUtils.getQNamePrefix(base);
		String namespace;
		if ("".equals(prefix)) {
			//default namespace
			namespace = DOMUtils.getNSURI("", content);
		} else {
			namespace = DOMUtils.getNSURI(prefix, content);
		}
		//is schema type
		if (Utils.NSURI_SCHEMA.equals(namespace)) {
			String type = DOMUtils.getQNameLocalpart(base);
			if ("anyType".equals(type)) {
				return null; // no simple type
			}
			return new BaseType(type); //base type
		}
		//getting base type
		GlobalDeclaration decl;
		try {
			if (Utils.isRedefineSchemaChild(complexTypeElement)) {
				decl = GlobalDeclaration.getGlobalDeclarationInRedefinedSchema(
					DOMUtils.getQNameLocalpart(base), Utils.COMPLEX_TYPE,
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

	/** Type name getter.
	 * @return  type name.
	 */
	public String getName() {return _name;}

	/** Returns XDefinition style type definition.
	 * @return  String representing type method.
	 */
	public abstract String getTypeMethod();
}