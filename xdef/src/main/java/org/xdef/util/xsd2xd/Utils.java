package org.xdef.util.xsd2xd;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Provides static methods for working with XML Schema 1.0 documents
 * and getting information.
 * @author Ilia Alexandrov
 */
public class Utils {
	/** Namespace URI for XML Schema 1.0. */
	public static final String NSURI_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	/** Local name for XML schema all element. */
	public static final String ALL = "all";
	/** Local name for XML schema annotation element. */
	public static final String ANNOTATION = "annotation";
	/** Local name for XML schema any element. */
	public static final String ANY = "any";
	/** Local name for XML schema anyAttribute element. */
	public static final String ANY_ATTRIBUTE = "anyAttribute";
	/** Local name for XML schema appInfo element. */
	public static final String APP_INFO = "appInfo";
	/** Local name for XML schema attribute element. */
	public static final String ATTRIBUTE = "attribute";
	/** Local name for XML schema attributeGroup element. */
	public static final String ATTRIBUTE_GROUP = "attributeGroup";
	/** Local name for XML schema choice element. */
	public static final String CHOICE = "choice";
	/** Local name for XML schema complexContent element. */
	public static final String COMPLEX_CONTENT = "complexContent";
	/** Local name for XML schema complexType element. */
	public static final String COMPLEX_TYPE = "complexType";
	/** Local name for XML schema documentation element. */
	public static final String DOCUMENTATION = "documentation";
	/** Local name for XML schema element element. */
	public static final String ELEMENT = "element";
	/** Local name for XML schema extension element. */
	public static final String EXTENSION = "extension";
	/** Local name for XML schema field element. */
	public static final String FIELD = "field";
	/** Local name for XML schema group element. */
	public static final String GROUP = "group";
	/** Local name for XML schema key element. */
	public static final String KEY = "key";
	/** Local name for XML schema keyref element. */
	public static final String KEYREF = "keyref";
	/** Local name for XML schema notation element. */
	public static final String NOTATION = "notation";
	/** Local name for XML schema redefine element. */
	public static final String REDEFINE = "redefine";
	/** Local name for XML schema restriction element. */
	public static final String RESTRICTION = "restriction";
	/** Local name for XML schema schema element. */
	public static final String SCHEMA = "schema";
	/** Local name for XML schema selector element. */
	public static final String SELECTOR = "selector";
	/** Local name for XML schema sequence element. */
	public static final String SEQUENCE = "sequence";
	/** Local name for XML schema simpleContent element. */
	public static final String SIMPLE_CONTENT = "simpleContent";
	/** Local name for XML schema simpleType element. */
	public static final String SIMPLE_TYPE = "simpleType";
	/** Local name for XML schema unique element. */
	public static final String UNIQUE = "unique";
	/** Local name for XML schema import element. */
	public static final String IMPORT = "import";
	/** Local name for XML schema include element. */
	public static final String INCLUDE = "include";

	/** Private constructor. */
	private Utils() {}

	/** Return namespace of given element according to settings in ancestor
	 * schema. If namespace is not declared returns empty string.
	 * @param element XML schema item element.
	 * @return namespace or empty string.
	 */
	public static String getNamespace(Element element) {
		String targetNamespace = getTargetNamespace(element);
		//target namespace is not declared
		if ("".equals(targetNamespace)) {
			return "";
		} else {
			//is schema child
			if (isSchemaChild(element) || DOMUtils.isChild(element, NSURI_SCHEMA, REDEFINE)) {
				return targetNamespace;
			} else {
				String type = element.getLocalName();
				//is attribute or element
				if (ATTRIBUTE.equals(type) || ELEMENT.equals(type)) {
					String form = element.getAttribute("form");
					if ("qualified".equals(form)) {
						return targetNamespace;
					} else {
						if (isQualifiedByDefault(element)) {
							if ("unqualified".equals(form)) {
								return "";
							}
							return targetNamespace;
						}
						return "";
					}
				} else {
					return "";
				}
			}
		}
	}

	/** Returns true if given element (element or attribute) is set to be
	 * qualified by default. If element is not XML schema attribute or element
	 * declaration it throws exception.
	 * @param element XML schema attribute or element declaration.
	 * @return true if given node typoe is set to be qualified by default.
	 */
	public static boolean isQualifiedByDefault(Element element) {
		String localName = element.getLocalName();
		if (!ATTRIBUTE.equals(localName) && !ELEMENT.equals(localName)) {
			throw new IllegalArgumentException("Given element is not a XML "
				+ "schema attribute or element declaration");
		}
		return "qualified".equals(getSchemaElement(element)
			.getAttribute(ATTRIBUTE.equals(localName) ? "attributeFormDefault" : "elementFormDefault"));
	}

	/** Returns true if given node is a valid XML schema element.
	 * @param node node to test.
	 * @return true if given node is a valid XML schema element.
	 */
	public static boolean isSchema(Node node) {return DOMUtils.isElement(node, NSURI_SCHEMA, SCHEMA);}

	/** Returns true if given node is a valid XML schema element child.
	 * @param node node to test.
	 * @return true if given node is a valid XML schema element child.
	 */
	public static boolean isSchemaChild(Node node) {return DOMUtils.isChild(node, NSURI_SCHEMA, SCHEMA);}

	/** Returns true if given node is element node, that is child of
	 * XML Schema group element, that is child of schema element.
	 * @param node testing node.
	 * @return true if given node is element node, that is child of XML Schema group element, that is child
	 * of schema element.
	 */
	public static boolean isSchemaGroupChild(Node node) {
		return (DOMUtils.isChild(node, NSURI_SCHEMA, GROUP) && isSchemaChild(node.getParentNode()));
	}

	/** Returns true if given node is descendant of schema child, that is attribute group declaration.
	 * @param node node to test.
	 * @return true if given node is descendant of schema child, that is attribute group declaration.
	 */
	public static boolean isSchemaDescendantAttrGroupChild(Node node) {
		return isAttributeGroup(getSchemaChild(node));
	}

	/** Returns true if given node is XML schema attributeGroup element.
	 * @param node node to test.
	 * @return true if given node is XML schema attributeGroup element.
	 */
	private static boolean isAttributeGroup(Node node) {
		return DOMUtils.isElement(node, NSURI_SCHEMA, ATTRIBUTE_GROUP);
	}

	/** Recursive method that returns ancestor of given node that is schema child.
	 * @param schemaChildDescendant schema child descendant node.
	 * @return schema child element.
	 * @throws IllegalArgumentException if node is not a schema descendant.
	 */
	private static Element getSchemaChild(Node schemaChildDescendant) throws IllegalArgumentException {
		//node is schema child
		if (isSchemaChild(schemaChildDescendant)) {
			return (Element) schemaChildDescendant;
		}
		Node parent = schemaChildDescendant.getParentNode();
		if (parent != null) {
			return getSchemaChild(parent);
		}
		throw new IllegalArgumentException("Given node is not a schema child descendant");
	}

	/** Gets ancestor XML schema element of given node or throws exception.
	 * @param node schema descendant node.
	 * @return ancestor XML schema schema element.
	 * @throws IllegalArgumentException if node is not a schema descendant.
	 */
	public static Element getSchemaElement(Node node) throws IllegalArgumentException {
		return DOMUtils.getElement(node, NSURI_SCHEMA, SCHEMA);
	}

	/** Gets target namespace of schema element, that is ancestor of given node.
	 * Return empty string if schema does not have target namespace declared.
	 * @param node schema descendant node.
	 * @return target namespace URI or empty string.
	 * @throws IllegalArgumentException if node is not a schema descendant.
	 */
	public static String getTargetNamespace(Node node) throws IllegalArgumentException {
		return getSchemaElement(node).getAttribute("targetNamespace");
	}

	/** Gets minimal occurrence of given schema item.
	 * @param schemaItem XML schema item.
	 * @return minimal occurrence.
	 */
	public static Integer getMinOccurrence(Element schemaItem) {
		String minOccurs = schemaItem.getAttribute("minOccurs");
		return minOccurs.isEmpty() ? 1 : Integer.valueOf(minOccurs);
	}

	/** Get maximal occurrence of given schema item. If is unbounded  then return -1.
	 * @param schemaItem XML schema item.
	 * @return maximal occurrence, -1 for unbounded.
	 */
	public static Integer getMaxOccurrence(Element schemaItem) {
		String maxOccurs = schemaItem.getAttribute("maxOccurs");
		return maxOccurs.isEmpty() ? 1 : "unbounded".equals(maxOccurs) ? -1 : Integer.valueOf(maxOccurs);
	}

	/** Gets set of URLs of imported schemas in given schema element.
	 * @param schemaElement schema element.
	 * @param url URL of given schema element.
	 * @return set of URLs of imported schemas.
	 * @throws MalformedURLException if cannot create URL.
	 * @throws IllegalArgumentException if not supported type.
	 */
	private static Set<URL> getExternalSchemaURLs(URL schemaURL, Element schemaElement, String type)
		throws MalformedURLException, IllegalArgumentException {
		if (!IMPORT.equals(type) && !INCLUDE.equals(type) && !REDEFINE.equals(type)) {
			throw new IllegalArgumentException("Given type is not supported by this method");
		}
		Set<URL> ret = new HashSet<URL>();
		NodeList externals = DOMUtils.getChildElementsNS(schemaElement, NSURI_SCHEMA, type);
		for (int i = 0; i < externals.getLength(); i++) {
			Element external = (Element) externals.item(i);
			String schemaLocation = external.getAttribute("schemaLocation");
			URL url;
			//there is schemaLocation attribute
			if (!"".equals(schemaLocation)) {
				url = getURL(schemaURL, schemaLocation);
				ret.add(url);
				//type of external schemas is import
			} else if (IMPORT.equals(type)) {
				String namespace = external.getAttribute("namespace");
				if (!"".equals(namespace)) {
					url = new URL(namespace);
					ret.add(url);
				}
			}
		}
		return ret;
	}

	/** Create URL from given URL context and given relative schema location.
	 * @param parrentURL context URL.
	 * @param schemaLocation relative position to given URL.
	 * @return URL object.
	 * @throws RuntimeException if cannot create URL.
	 */
	public static URL getURL(URL parrentURL, String schemaLocation) throws RuntimeException {
		String sourceBase;
		String s = parrentURL.toExternalForm().replace('\\', '/');
		int ndx = s.lastIndexOf('/');
		if (ndx < 0) {
			sourceBase = "";
		} else {
			sourceBase = s.substring(0, ndx + 1);
		}
		String location = schemaLocation.trim().replace('\\', '/');
		if (location.startsWith("..")) {
			location = location.substring(2, location.length());
			sourceBase = sourceBase.substring(0, sourceBase.length() - 1);
			sourceBase = sourceBase.substring(0, sourceBase.lastIndexOf("/"));
			location = sourceBase + location;
		} else {
			location = sourceBase + location;
		}
		try {
			return new URL(location);
		} catch (MalformedURLException ex) {
			throw new RuntimeException("Error creating URL from String: " + location, ex);
		}
	}

	/** Gets all imported schema URLs of given schema at given URL.
	 * @param schemaURL URL of schema element.
	 * @param schemaElement schema element.
	 * @return set of imported schema URLs.
	 * @throws MalformedURLException if cannot create URL.
	 */
	public static Set getImportedSchemaURLs(URL schemaURL,Element schemaElement) throws MalformedURLException{
		return getExternalSchemaURLs(schemaURL, schemaElement, IMPORT);
	}

	/** Gets all included schema URLs of given schema at given URL.
	 * @param schemaURL URL of schema element.
	 * @param schemaElement schema element.
	 * @return set of included schema URLs.
	 * @throws MalformedURLException if cannot create URL.
	 */
	public static Set<URL> getIncludedSchemaURLs(URL schemaURL, Element schemaElement)
		throws MalformedURLException {
		return getExternalSchemaURLs(schemaURL, schemaElement, INCLUDE);
	}

	/** Get all redefined schema URLs of given schema at given URL.
	 * @param schemaURL URL of schema element.
	 * @param schemaElement schema element.
	 * @return set of redefined schema URLs.
	 * @throws MalformedURLException if cannot create URL.
	 */
	public static Set<URL> getRedefinedSchemaURLs(URL schemaURL, Element schemaElement)
		throws MalformedURLException {
		return getExternalSchemaURLs(schemaURL, schemaElement, REDEFINE);
	}

	/** Gets set of schema URLs of given schema at given URL with namespace.
	 * @param schemaURL URL of schema.
	 * @param schemaElement schema element.
	 * @param namespace namespace of imported schema.
	 * @return set of URLs of imported schemas.
	 * @throws MalformedURLException if cannot create URL.
	 */
	public static Set<URL> getImportedSchemaURLs(URL schemaURL, Element schemaElement, String namespace)
		throws MalformedURLException {
		Set<URL> ret = new HashSet<URL>();
		NodeList imports = DOMUtils.getChildElementsNS(schemaElement, NSURI_SCHEMA, IMPORT);
		for (int i = 0; i < imports.getLength(); i++) {
			Element importElement = (Element) imports.item(i);
			String namespaceAttribute = importElement.getAttribute("namespace");
			if (namespaceAttribute.equals(namespace)) {
				String schemaLocation = importElement.getAttribute("schemaLocation");
				if (!"".equals(schemaLocation)) {
					URL url = Utils.getURL(schemaURL, schemaLocation);
					ret.add(url);
				} else {
					URL url;
					try {
						url = new URL(URLDecoder.decode(namespaceAttribute, "UTF-8"));
					} catch (Exception ex) {
						url = new URL(namespaceAttribute);
					}

					ret.add(url);
				}
			}
		}
		return ret;
	}

	/** Returns true if given element is child of redefine element,
	 * that is child of schema element.
	 * @param element checked element.
	 * @return true if given element is child of redefine element,
	 * that is child of schema element.
	 */
	public static boolean isRedefineSchemaChild(Element element) {
		return DOMUtils.isChild(element, NSURI_SCHEMA, REDEFINE) && isSchemaChild(element.getParentNode());
	}
}