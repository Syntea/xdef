package org.xdef.impl.util.conv.xsd2xd.schema_1_0;

import org.xdef.impl.util.conv.xsd2xd.util.DOMUtils;
import org.xdef.xml.KXmlUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.sys.SUtils;

/** Provides static methods for working with XML Schema 1.0 documents
 * and getting information.
 * @author Alexandrov
 */
public class Utils {

	/** Namespace URI for XML Schema 1.0. */
	public static final String NSURI_SCHEMA ="http://www.w3.org/2001/XMLSchema";
	/** Local name for XML schema <tt>all</tt> element. */
	public static final String ALL = "all";
	/** Local name for XML schema <tt>annotation</tt> element. */
	public static final String ANNOTATION = "annotation";
	/** Local name for XML schema <tt>any</tt> element. */
	public static final String ANY = "any";
	/** Local name for XML schema <tt>anyAttribute</tt> element. */
	public static final String ANY_ATTRIBUTE = "anyAttribute";
	/** Local name for XML schema <tt>appInfo</tt> element. */
	public static final String APP_INFO = "appInfo";
	/** Local name for XML schema <tt>attribute</tt> element. */
	public static final String ATTRIBUTE = "attribute";
	/** Local name for XML schema <tt>attributeGroup</tt> element. */
	public static final String ATTRIBUTE_GROUP = "attributeGroup";
	/** Local name for XML schema <tt>choice</tt> element. */
	public static final String CHOICE = "choice";
	/** Local name for XML schema <tt>complexContent</tt> element. */
	public static final String COMPLEX_CONTENT = "complexContent";
	/** Local name for XML schema <tt>complexType</tt> element. */
	public static final String COMPLEX_TYPE = "complexType";
	/** Local name for XML schema <tt>documentation</tt> element. */
	public static final String DOCUMENTATION = "documentation";
	/** Local name for XML schema <tt>element</tt> element. */
	public static final String ELEMENT = "element";
	/** Local name for XML schema <tt>extension</tt> element. */
	public static final String EXTENSION = "extension";
	/** Local name for XML schema <tt>field</tt> element. */
	public static final String FIELD = "field";
	/** Local name for XML schema <tt>group</tt> element. */
	public static final String GROUP = "group";
	/** Local name for XML schema <tt>key</tt> element. */
	public static final String KEY = "key";
	/** Local name for XML schema <tt>keyref</tt> element. */
	public static final String KEYREF = "keyref";
	/** Local name for XML schema <tt>notation</tt> element. */
	public static final String NOTATION = "notation";
	/** Local name for XML schema <tt>redefine</tt> element. */
	public static final String REDEFINE = "redefine";
	/** Local name for XML schema <tt>restriction</tt> element. */
	public static final String RESTRICTION = "restriction";
	/** Local name for XML schema <tt>schema</tt> element. */
	public static final String SCHEMA = "schema";
	/** Local name for XML schema <tt>selector</tt> element. */
	public static final String SELECTOR = "selector";
	/** Local name for XML schema <tt>sequence</tt> element. */
	public static final String SEQUENCE = "sequence";
	/** Local name for XML schema <tt>simpleContent</tt> element. */
	public static final String SIMPLE_CONTENT = "simpleContent";
	/** Local name for XML schema <tt>simpleType</tt> element. */
	public static final String SIMPLE_TYPE = "simpleType";
	/** Local name for XML schema <tt>unique</tt> element. */
	public static final String UNIQUE = "unique";
	/** Local name for XML schema <tt>import</tt> element. */
	public static final String IMPORT = "import";
	/** Local name for XML schema <tt>include</tt> element. */
	public static final String INCLUDE = "include";

	/** Private constructor. */
	private Utils() {}

	/** Returns namespace of given element according to settings in ancestor
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
			if (isSchemaChild(element)
				|| DOMUtils.isChild(element, NSURI_SCHEMA, REDEFINE)) {
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
	 * @param element XML schema <tt>attribute</tt> or <tt>element</tt>
	 * declaration.
	 * @return true if given node typoe is set to be qualified by default.
	 */
	public static boolean isQualifiedByDefault(Element element) {
		String localName = element.getLocalName();
		if (!ATTRIBUTE.equals(localName) && !ELEMENT.equals(localName)) {
			throw new IllegalArgumentException("Given element is not a XML "
				+ "schema attribute or element declaration");
		}
		return "qualified".equals(getSchemaElement(element)
			.getAttribute(ATTRIBUTE.equals(localName)
				? "attributeFormDefault" : "elementFormDefault"));
	}

	/** Returns <tt>true</tt> if given node is a valid XML schema element.
	 * @param node node to test.
	 * @return <tt>true</tt> if given node is a valid XML schema element.
	 */
	public static boolean isSchema(Node node) {
		return DOMUtils.isElement(node, NSURI_SCHEMA, SCHEMA);
	}

	/** Returns <tt>true</tt> if given node is a valid XML schema element child.
	 * @param node node to test.
	 * @return <tt>true</tt> if given node is a valid XML schema element child.
	 */
	public static boolean isSchemaChild(Node node) {
		return DOMUtils.isChild(node, NSURI_SCHEMA, SCHEMA);
	}

	/** Returns <tt>true</tt> if given node is element node, that is child of
	 * XML Schema <tt>group</tt> element, that is child of <tt>schema</tt>
	 * element.
	 * @param node testing node.
	 * @return <tt>true</tt> if given node is element node, that is child of
	 * XML Schema <tt>group</tt> element, that is child of <tt>schema</tt>
	 * element.
	 */
	public static boolean isSchemaGroupChild(Node node) {
		return (DOMUtils.isChild(node, NSURI_SCHEMA, GROUP)
			&& isSchemaChild(node.getParentNode()));
	}

	/** Returns <tt>true</tt> if given node is descendant of schema child, that
	 * is attribute group declaration.
	 * @param node node to test.
	 * @return <tt>true</tt> if given node is descendant of schema child, that
	 * is attribute group declaration.
	 */
	public static boolean isSchemaDescendantAttrGroupChild(Node node) {
		return isAttributeGroup(getSchemaChild(node));
	}

	/** Returns <tt>true</tt> if given node is XML schema
	 * <tt>attributeGroup</tt> element.
	 * @param node node to test.
	 * @return <tt>true</tt> if given node is XML schema
	 * <tt>attributeGroup</tt> element.
	 */
	private static boolean isAttributeGroup(Node node) {
		return DOMUtils.isElement(node, NSURI_SCHEMA, ATTRIBUTE_GROUP);
	}

	/** Recursive method that returns ancestor of given node that is schema
	 * child.
	 * @param schemaChildDescendant schema child descendant node.
	 * @return schema child element.
	 * @throws IllegalArgumentException if node is not a schema descendant.
	 */
	private static Element getSchemaChild(Node schemaChildDescendant)
		throws IllegalArgumentException {
		//node is schema child
		if (isSchemaChild(schemaChildDescendant)) {
			return (Element) schemaChildDescendant;
		}
		Node parent = schemaChildDescendant.getParentNode();
		if (parent != null) {
			return getSchemaChild(parent);
		}
		throw new IllegalArgumentException("Given node is not a schema "
				+ "child descendant");
	}

	/** Gets ancestor XML schema element of given node or throws exception.
	 * @param node schema descendant node.
	 * @return ancestor XML schema <tt>schema</tt> element.
	 * @throws IllegalArgumentException if node is not a schema descendant.
	 */
	public static Element getSchemaElement(Node node)
		throws IllegalArgumentException {
		return DOMUtils.getElement(node, NSURI_SCHEMA, SCHEMA);
	}

	/** Gets target namespace of schema element, that is ancestor of given node.
	 * Returns empty string if schema does not have target namespace declared.
	 * Throws exception if node is not a schema descendant.
	 * @param node schema descendant node.
	 * @return target namespace URI or empty string.
	 * @throws IllegalArgumentException if node is not a schema descendant.
	 */
	public static String getTargetNamespace(Node node)
		throws IllegalArgumentException {
		return getSchemaElement(node).getAttribute("targetNamespace");
	}

	/** Gets minimal occurrence of given schema item.
	 * @param schemaItem XML schema item.
	 * @return minimal occurrence.
	 */
	public static Integer getMinOccurrence(Element schemaItem) {
		String minOccurs = schemaItem.getAttribute("minOccurs");
		return minOccurs.isEmpty() ? 1 : Integer.parseInt(minOccurs);
	}

	/** Gets maximal occurrence of given schema item. If is unbounded
	 *  then returns -1.
	 * @param schemaItem XML schema item.
	 * @return maximal occurrence, -1 for unbounded.
	 */
	public static Integer getMaxOccurrence(Element schemaItem) {
		String maxOccurs = schemaItem.getAttribute("maxOccurs");
		return maxOccurs.isEmpty() ? 1 :
			"unbounded".equals(maxOccurs) ? -1 : Integer.parseInt(maxOccurs);
	}

	/** Gets set of URLs of imported schemas in given schema element.
	 * @param schemaElement schema element.
	 * @param url URL of given schema element.
	 * @return set of URLs of imported schemas.
	 * @throws MalformedURLException if cannot create URL.
	 * @throws IllegalArgumentException if not supported type.
	 */
	private static Set<URL> getExternalSchemaURLs(URL schemaURL,
		Element schemaElement, String type) throws MalformedURLException,
		IllegalArgumentException {
		if (!IMPORT.equals(type) && !INCLUDE.equals(type)
			&& !REDEFINE.equals(type)) {
			throw new IllegalArgumentException(
				"Given type is not supported by this method");
		}
		Set<URL> ret = new HashSet<URL>();
		NodeList externals = KXmlUtils.getChildElementsNS(schemaElement,
			NSURI_SCHEMA, type);
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
					url = SUtils.getExtendedURL(namespace);
					ret.add(url);
				}
			}
		}
		return ret;
	}

	/** Creates URL from given URL context and given relative schema location.
	 * @param parrentURL context URL.
	 * @param schemaLocation relative position to given URL.
	 * @return URL object.
	 * @throws RuntimeException if cannot create URL.
	 */
	public static URL getURL(URL parrentURL, String schemaLocation)
		throws RuntimeException {
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
			return SUtils.getExtendedURL(location);
		} catch (MalformedURLException ex) {
			throw new RuntimeException(
				"Error creating URL from String: " + location, ex);
		}
	}

	/** Gets all imported schema URLs of given schema at given URL.
	 * @param schemaURL URL of schema element.
	 * @param schemaElement schema element.
	 * @return set of imported schema URLs.
	 * @throws MalformedURLException if cannot create URL.
	 */
	public static Set getImportedSchemaURLs(URL schemaURL,
		Element schemaElement) throws MalformedURLException {
		return getExternalSchemaURLs(schemaURL, schemaElement, IMPORT);
	}

	/** Gets all included schema URLs of given schema at given URL.
	 * @param schemaURL URL of schema element.
	 * @param schemaElement schema element.
	 * @return set of included schema URLs.
	 * @throws MalformedURLException if cannot create URL.
	 */
	public static Set<URL> getIncludedSchemaURLs(URL schemaURL,
		Element schemaElement) throws MalformedURLException {
		return getExternalSchemaURLs(schemaURL, schemaElement, INCLUDE);
	}

	/** Gets all redefined schema URLs of given schema at given URL.
	 * @param schemaURL URL of schema element.
	 * @param schemaElement schema element.
	 * @return set of redefined schema URLs.
	 * @throws MalformedURLException if cannot create URL.
	 */
	public static Set<URL> getRedefinedSchemaURLs(URL schemaURL,
		Element schemaElement) throws MalformedURLException {
		return getExternalSchemaURLs(schemaURL, schemaElement, REDEFINE);
	}

	/** Gets set of schema URLs of given schema at given URL with namespace.
	 * @param schemaURL URL of schema.
	 * @param schemaElement schema element.
	 * @param namespace namespace of imported schema.
	 * @return set of URLs of imported schemas.
	 * @throws MalformedURLException if cannot create URL.
	 */
	public static Set<URL> getImportedSchemaURLs(URL schemaURL,
		Element schemaElement, String namespace) throws MalformedURLException {
		Set<URL> ret = new HashSet<URL>();
		NodeList imports = KXmlUtils.getChildElementsNS(schemaElement,
			NSURI_SCHEMA, IMPORT);
		for (int i = 0; i < imports.getLength(); i++) {
			Element importElement = (Element) imports.item(i);
			String namespaceAttribute = importElement.getAttribute("namespace");
			if (namespaceAttribute.equals(namespace)) {
				String schemaLocation =
					importElement.getAttribute("schemaLocation");
				if (!"".equals(schemaLocation)) {
					URL url = Utils.getURL(schemaURL, schemaLocation);
					ret.add(url);
				} else {
					URL url = SUtils.getExtendedURL(namespaceAttribute);
					ret.add(url);
				}
			}
		}
		return ret;
	}

	/** Returns <tt>true</tt> if given element is child of redefine element,
	 * that is child of schema element.
	 * @param element checked element.
	 * @return <tt>true</tt> if given element is child of redefine element,
	 * that is child of schema element.
	 */
	public static boolean isRedefineSchemaChild(Element element) {
		return DOMUtils.isChild(element, NSURI_SCHEMA, REDEFINE)
			&& isSchemaChild(element.getParentNode());
	}
}
