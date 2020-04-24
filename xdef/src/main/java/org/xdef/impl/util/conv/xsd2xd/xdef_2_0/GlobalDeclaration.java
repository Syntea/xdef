package org.xdef.impl.util.conv.xsd2xd.xdef_2_0;

import org.xdef.impl.util.conv.xsd2xd.schema_1_0.Utils;
import org.xdef.impl.util.conv.xsd2xd.util.DOMUtils;
import org.xdef.xml.KXmlUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Represents global declaration of XML schema item.
 * @author Alexandrov
 */
public class GlobalDeclaration {

	/** Element containing global declaration. */
	private final Element _globalDeclarationElement;
	/** URL of XML schema containing global declaration. */
	private final URL _schemaURL;
	/** Namespace of global declaration. */
	private String _namespace;
	/** Is redefined. */
	private boolean _redefined = false;

	/** Gets instance of global declaration by given full name of given type
	 * from schema element at given URL.
	 * @param fullName  full name of searched global declaration.
	 * @param schemaURL URL of referenced schema.
	 * @param type      type of searched global declaration.
	 * @param schemaElements table of all schema elements.
	 * @return          global declaration object.
	 * @throws RuntimeException declaration could not be found.
	 */
	public static GlobalDeclaration getGlobalDeclaration(String fullName,
		String type, URL schemaURL, Map<URL, Element> schemaElements)
		throws RuntimeException {
		String refPrefix = KXmlUtils.getQNamePrefix(fullName);
		String refLocalName = KXmlUtils.getQNameLocalpart(fullName);
		Element schemaElement = (Element) schemaElements.get(schemaURL);
		//target namespace of schema containing reference
		String targetNamespace = Utils.getTargetNamespace(schemaElement);
		//reference contains prefix
		if (!"".equals(refPrefix)) {
			//namespace of referenced global declaration
			String namespace = KXmlUtils.getNSURI(refPrefix, schemaElement);
			//namespace of referred declaration is same as target namespace
			//of schema containing reference
			if (namespace.equals(targetNamespace)) {
				return getGlobalDeclarationInSchemaAndIncludes(refLocalName,
					schemaURL, type, schemaElements);
				//namespace of referred declaration is other than target
				//namespace of schema containing reference
			} else {
				return getGlobalDeclarationInImports(namespace, refLocalName,
					schemaURL, type, schemaElements);
			}
			//reference does not contain prefix
		} else {
			String defaultNamespace =
				DOMUtils.getDefaultNamespaceURI(schemaElement);
			//no default namespace declaration
			if ("".equals(defaultNamespace)) {
				//no target namespace declared in schema containing reference
				if ("".equals(targetNamespace)) {
					return getGlobalDeclarationInSchemaAndIncludes(refLocalName,
						schemaURL, type, schemaElements);
				} else {
					return getGlobalDeclarationInImports("", refLocalName,
						schemaURL, type, schemaElements);
				}
				//there is default namespace declaration
			} else {
				//default namespace is same as target namespace
				if (defaultNamespace.equals(targetNamespace)) {
					return getGlobalDeclarationInSchemaAndIncludes(refLocalName,
						schemaURL, type, schemaElements);
					//namespace of referred declaration is other than target
					//namespace of schema containing reference
				} else {
					return getGlobalDeclarationInImports(defaultNamespace,
						refLocalName, schemaURL, type, schemaElements);
				}
			}
		}
	}

	/** Gets global declaration of given type and given name in schema element.
	 * @param namespace namespace URI of searched global declaration.
	 * @param localName local name of searched global declaration.
	 * @param schemaURL URL of schema containing reference to global declaration.
	 * @param type      type of searched declaration.
	 * @return          global declaration object.
	 * @throws RuntimeException cannot find declaration or cannot create url.
	 */
	private static GlobalDeclaration getGlobalDeclarationInImports(
		String namespace,
		String localName,
		URL schemaURL,
		String type,
		Map<URL, Element> schemaElements) throws RuntimeException {
		try {
			Set<URL> importedSchemaURLs = Utils.getImportedSchemaURLs(
				schemaURL, (Element) schemaElements.get(schemaURL), namespace);
			Iterator i = importedSchemaURLs.iterator();
			while (i.hasNext()) {
				URL importedSchemaURL = (URL) i.next();
				GlobalDeclaration importedGlobalDeclaration =
					getGlobalDeclarationInSchemaAndIncludes(localName,
						importedSchemaURL, type, schemaElements);
				if (importedGlobalDeclaration != null) {
					return importedGlobalDeclaration;
				}
			}
			throw new RuntimeException("Imported global declaration [name='"
				+ localName + "', type='" + type + "'] was not found on URL " +
				schemaURL.toExternalForm());
		} catch (MalformedURLException ex) {
			throw new RuntimeException("Could not get URL", ex);
		}
	}

	/** Gets global declaration of given type and given name in schema element
	 * at given URL and it's includes.
	 * @param localName local name of searched global declaration.
	 * @param schemaURL URL of schema containing reference to global declaration.
	 * @param type      type of searched global declaration.
	 * @return          global declaration object.
	 */
	private static GlobalDeclaration getGlobalDeclarationInSchemaAndIncludes(
		String localName,
		URL schemaURL,
		String type,
		Map<URL, Element> schemaElements) {
		GlobalDeclaration ret = getGlobalDeclarationInSchemaAndIncludes(
			localName, schemaURL, type, new HashSet<URL>(), schemaElements);
		if (ret == null) {
			throw new RuntimeException("Global declaration [name='"
				+ localName + "', " + "type='" + type
				+ "'] was not found in schema and includes at "
				+ "URL " + schemaURL.toExternalForm());
		}
		ret.setNamespace(Utils.getTargetNamespace(
			(Element) schemaElements.get(schemaURL)));
		return ret;
	}

	/** Gets global declaration of given type and given name in schema element
	 * at given URL and it's includes.
	 * @param localName     local name of searched global declaration.
	 * @param schemaURL     URL of schema containing reference to global
	 *                      declaration.
	 * @param type          type of searched global declaration.
	 * @param searchedURLs  set of parsed schema URLs.
	 * @return              global declaration object.
	 */
	private static GlobalDeclaration getGlobalDeclarationInSchemaAndIncludes(
		String localName,
		URL schemaURL,
		String type,
		Set<URL> searchedURLs,
		Map<URL, Element> schemaElements) throws RuntimeException {
		GlobalDeclaration schemaGlobalDeclaration =
			getGlobalDeclarationInSchema(localName, schemaURL, type,
				schemaElements);
		//current schema contains global declaration
		if (schemaGlobalDeclaration != null) {
			return schemaGlobalDeclaration;
		}
		searchedURLs.add(schemaURL);
		try {
			Set<URL> includes = Utils.getIncludedSchemaURLs(schemaURL,
				(Element) schemaElements.get(schemaURL));
			includes.addAll(Utils.getRedefinedSchemaURLs(schemaURL,
				(Element) schemaElements.get(schemaURL)));
			Iterator i = includes.iterator();
			while (i.hasNext()) {
				URL includedSchemaURL = (URL) i.next();
				if (!searchedURLs.contains(includedSchemaURL)) {
					GlobalDeclaration includedGlobalDeclaration =
						getGlobalDeclarationInSchemaAndIncludes(localName,
							includedSchemaURL, type, searchedURLs,
							schemaElements);
					//included schema contains global declaration
					if (includedGlobalDeclaration != null) {
						return includedGlobalDeclaration;
					}
				}
			}
		} catch (MalformedURLException ex) {
			throw new RuntimeException("Could not get URL", ex);
		}
		return null;
	}

	/** Gets global declaration of given type and given name in schema element
	 * at given URL.
	 * @param localName local name part of searched global declaration.
	 * @param schemaURL schema element URL containing searched global
	 *                  declaration.
	 * @param type      type of searched global declaration.
	 * @return          searched global declaration object or <tt>null</tt>
	 *  if declaration could not be found.
	 */
	private static GlobalDeclaration getGlobalDeclarationInSchema(
		String localName,
		URL schemaURL,
		String type,
		Map schemaElements) {
		NodeList globalDeclarations = KXmlUtils.getChildElementsNS(
			(Element) schemaElements.get(schemaURL), Utils.NSURI_SCHEMA, type);
		//iterating global declarations of given type
		for (int i = 0; i < globalDeclarations.getLength(); i++) {
			GlobalDeclaration globalDeclaration = new GlobalDeclaration(
					(Element) globalDeclarations.item(i), schemaURL);
			if (localName.equals(globalDeclaration.getName())
					&& type.equals(globalDeclaration.getType())) {
				return globalDeclaration;
			}
		}
		NodeList redefines = KXmlUtils.getChildElementsNS(
			(Element) schemaElements.get(schemaURL),
			Utils.NSURI_SCHEMA,
			Utils.REDEFINE);
		//iterating redefines
		for (int i = 0; i < redefines.getLength(); i++) {
			Element redefine = (Element) redefines.item(i);
			//getting redefine declarations
			NodeList redefinedDeclarations = KXmlUtils.getChildElementsNS(
				redefine, Utils.NSURI_SCHEMA, type);
			//iterating redefined declarations
			for (int j = 0; j < redefinedDeclarations.getLength(); j++) {
				//getting redefined declaration
				Element redefinedDeclaration =
					(Element) redefinedDeclarations.item(j);
				//declaration is searched declaration
				if (redefinedDeclaration.getAttribute("name")
					.equals(localName)) {
					GlobalDeclaration decl =
						new GlobalDeclaration(redefinedDeclaration, schemaURL);
					decl.setRedefined(true);
					return decl;
				}
			}
		}
		return null;
	}

	/** Gets global declaration with given type and name in schema at given URL
	 * in given redefine element.
	 * @param localName local name of searched declaration.
	 * @param type type of searched declaration.
	 * @param schemaURL current schema URL.
	 * @param redefineElement redefine element containing redefined declaration.
	 * @param schemaElements all schema elements.
	 * @return global declaration containing element.
	 * @throws RuntimeException global declaration could not be found.
	 */
	public static GlobalDeclaration getGlobalDeclarationInRedefinedSchema(
		String localName,
		String type,
		URL schemaURL,
		Element redefineElement,
		Map schemaElements) throws RuntimeException {
		try {
			URL url = Utils.getURL(schemaURL,
				redefineElement.getAttribute("schemaLocation"));
			GlobalDeclaration decl = getGlobalDeclarationInSchema(localName,
				url, type, schemaElements);
			if (decl == null) {
				throw new RuntimeException("Declaration with local name '"
					+ localName + "' , of type '" + type
					+ "' was not found in schema at URL '"
					+ schemaURL.getPath() + "'");
			}
			return decl;
		} catch (Exception ex) {
			throw new RuntimeException(
				"Global declaration with given properties was not found", ex);
		}
	}

	/** Creates instance of global declaration with given element and
	 * on given URL.
	 * @param element   element of global declaration.
	 * @param schemaURL URL of global declaratiion
	 */
	public GlobalDeclaration(Element element, URL schemaURL) {
		_globalDeclarationElement = element;
		_schemaURL = schemaURL;
		String targetNamespace =
			Utils.getTargetNamespace(_globalDeclarationElement);
		//element or attribute global declaration
		if ("attribute".equals(_globalDeclarationElement.getLocalName())
			|| "element".equals(_globalDeclarationElement.getLocalName())) {
			//element or attribute is set as unqualified
			if ("unqualified".equals(
				_globalDeclarationElement.getAttribute("form"))) {
				_namespace = "";
			} else {
				_namespace = targetNamespace;
			}
		} else {
			_namespace = targetNamespace;
		}
	}

	/** Global declaration element getter.
	 * @return  global declaration element.
	 */
	public Element getGlobalDeclarationElement() {
		return _globalDeclarationElement;
	}

	/** Schema containing global declaration element URL getter.
	 * @return  schema URL.
	 */
	public URL getSchemaURL() {return _schemaURL;}

	/** Global declaration namespace getter.
	 * @return  global declaration namespace.
	 */
	public String getNamespace() {return _namespace;}

	/** Sets namesapce of global declaration.
	 * @param namespace namespace URI.
	 */
	public void setNamespace(String namespace) {_namespace = namespace;}

	/** Gets type of global declaration.
	 * @return type of global declaration.
	 */
	public String getType() {return _globalDeclarationElement.getLocalName();}

	/** Gets name of global declaration.
	 * @return  name of global declaration.
	 */
	public String getName() {
		return _globalDeclarationElement.getAttribute("name");
	}

	/** Redefined property getter.
	 * @return redefined property.
	 */
	public boolean isRedefined() {return _redefined;}

	/** Sets redefined property.
	 * @param redefined redefined property.
	 */
	public void setRedefined(boolean redefined) {_redefined = redefined;}
}