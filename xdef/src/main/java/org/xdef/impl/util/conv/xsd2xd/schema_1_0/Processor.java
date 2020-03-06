package org.xdef.impl.util.conv.xsd2xd.schema_1_0;

import org.xdef.xml.KDOMBuilder;
import org.xdef.impl.util.conv.xsd2xd.Convertor;
import org.xdef.xml.KXmlUtils;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import org.w3c.dom.*;
import org.xdef.sys.SUtils;

/** Abstract class that represents converting logic of XML Schema 1.0. Contains
 * abstract methods for processing schema items.
 * @author Alexandrov
 */
public abstract class Processor implements Convertor {

	/** All XML Schema <tt>schema</tt> elements table (URL, Element). */
	protected final Map<URL, Element> _schemaElements;
	/** Stack of URLs of currently processing schema elements (URL). */
	protected final Stack<URL> _schemaURLStack = new Stack<URL>();
/*VT*/
	/** Stack of URLs of currently processing schema elements (URL). */
	protected final String _xdURI;
	/** Stack of URLs of currently processing schema elements (URL). */
	protected final String _xdPrefix;
/*VT*/

	/** Creates instance with root schema at given URL. Initializes all schema
	 * elements.
	 * @param rootSchemaURL URL of root schema.
	 * @param xdURI namespace URI of X-definition.
	 * @param xdPrefix prefix of namespace URI of X-definition.
	 */
	public Processor(URL rootSchemaURL, String xdURI, String xdPrefix) {
/*VT*/
		_xdPrefix = xdPrefix;
		_xdURI = xdURI;
/*VT*/
		try {
			String urlString = rootSchemaURL.toExternalForm();
			URL newURL = SUtils.getExtendedURL(urlString);
			_schemaElements = getSchemaElements(newURL);
		} catch (Exception ex) {
			throw new RuntimeException("Could not create URL from URL!", ex);
		}
	}

	/** Returns set of URLs of schema files related to schema at given URL.
	 * @param schemaURL URL of root schema.
	 * @return set of URL objects related to given schema.
	 * @throws RuntimeException can not create URL or given URL is not schema.
	 */
	private Map<URL, Element> getSchemaElements(URL rootSchemaURL) {
		Map<URL, Element> schemaElements = new HashMap<URL, Element>();
		getSchemaElements(schemaElements, rootSchemaURL);
		return schemaElements;
	}

	/** Recursive method for getting all URLs of related schemas to the schema
	 * at given URL.
	 * @param urlStack stack of already parsed URLs.
	 * @param schemaURL URL of root schema file.
	 * @return set of schema URLs related to given schema.
	 * @throws RuntimeException can not create URL.
	 */
	private Map<URL, Element> getSchemaElements(Map<URL,
		Element>schemaElements,
		URL schemaURL) throws RuntimeException {
		KDOMBuilder kb = new KDOMBuilder();
		//getting schema element
		Element schemaElement = kb.parse(schemaURL).getDocumentElement();
		//checking schema element
		if (!Utils.isSchema(schemaElement)) {
			throw new RuntimeException("Not a schema!");
		}
		//adding to schema elements
		schemaElements.put(schemaURL, schemaElement);
		//getting include, import and redefine elements
		NodeList externals = KXmlUtils.getChildElementsNS(schemaElement,
				Utils.NSURI_SCHEMA, new String[]{Utils.INCLUDE, Utils.IMPORT,
					Utils.REDEFINE});
		for (int i = 0; i < externals.getLength(); i++) {
			//external element
			Element external = (Element) externals.item(i);
			//getting schemaLocation
			String schemaLocation = external.getAttribute("schemaLocation");
			//schema location exists
			if (!"".equals(schemaLocation)) {
				try {
					//creating URL of external schema
					URL url = Utils.getURL(schemaURL, schemaLocation);
					//schemaElement does not contain given url
					if (!schemaElements.containsKey(url)) {
						//calling method
						getSchemaElements(schemaElements, url);
					}
				} catch (Exception ex) {
					throw new RuntimeException("Could not create URL!", ex);
				}
			}
		}
		return schemaElements;
	}

	/** Processes XML schema item. Calls proper method according to given schema
	 * item type.
	 * @param schemaItem schema item element.
	 * @param xdefContextElement context parent X-definition element.
	 */
	protected void processSchemaItem(Element schemaItem,
		Element xdefContextElement) {
		//resolving debugging
		resolveDebug(schemaItem, xdefContextElement);
		//element is XML schema element
		if (Utils.NSURI_SCHEMA.equals(schemaItem.getNamespaceURI())) {
			String name = schemaItem.getLocalName();
			if (Utils.ALL.equals(name)) {
				processAll(schemaItem, xdefContextElement);
			} else if (Utils.ANNOTATION.equals(name)) {
				processAnnotation(schemaItem, xdefContextElement);
			} else if (Utils.ANY.equals(name)) {
				processAny(schemaItem, xdefContextElement);
			} else if (Utils.ANY_ATTRIBUTE.equals(name)) {
				processAnyAttribute(schemaItem, xdefContextElement);
			} else if (Utils.APP_INFO.equals(name)) {
				processAppInfo(schemaItem, xdefContextElement);
			} else if (Utils.ATTRIBUTE.equals(name)) {
				processAttribute(schemaItem, xdefContextElement);
			} else if (Utils.ATTRIBUTE_GROUP.equals(name)) {
				processAttributeGroup(schemaItem, xdefContextElement);
			} else if (Utils.CHOICE.equals(name)) {
				processChoice(schemaItem, xdefContextElement);
			} else if (Utils.COMPLEX_CONTENT.equals(name)) {
				processComplexContent(schemaItem, xdefContextElement);
			} else if (Utils.COMPLEX_TYPE.equals(name)) {
				processComplexType(schemaItem, xdefContextElement);
			} else if (Utils.DOCUMENTATION.equals(name)) {
				processDocumentation(schemaItem, xdefContextElement);
			} else if (Utils.ELEMENT.equals(name)) {
				processElement(schemaItem, xdefContextElement);
			} else if (Utils.EXTENSION.equals(name)) {
				processExtension(schemaItem, xdefContextElement);
			} else if (Utils.FIELD.equals(name)) {
				processField(schemaItem, xdefContextElement);
			} else if (Utils.GROUP.equals(name)) {
				processGroup(schemaItem, xdefContextElement);
			} else if (Utils.IMPORT.equals(name)) {
				processImport(schemaItem, xdefContextElement);
			} else if (Utils.INCLUDE.equals(name)) {
				processInclude(schemaItem, xdefContextElement);
			} else if (Utils.KEY.equals(name)) {
				processKey(schemaItem, xdefContextElement);
			} else if (Utils.KEYREF.equals(name)) {
				processKeyref(schemaItem, xdefContextElement);
			} else if (Utils.NOTATION.equals(name)) {
				processNotation(schemaItem, xdefContextElement);
			} else if (Utils.REDEFINE.equals(name)) {
				processRedefine(schemaItem, xdefContextElement);
			} else if (Utils.RESTRICTION.equals(name)) {
				processRestriction(schemaItem, xdefContextElement);
			} else if (Utils.SELECTOR.equals(name)) {
				processSelector(schemaItem, xdefContextElement);
			} else if (Utils.SEQUENCE.equals(name)) {
				processSequence(schemaItem, xdefContextElement);
			} else if (Utils.SIMPLE_CONTENT.equals(name)) {
				processSimpleContent(schemaItem, xdefContextElement);
			} else if (Utils.SIMPLE_TYPE.equals(name)) {
				processSimpleType(schemaItem, xdefContextElement);
			} else if (Utils.UNIQUE.equals(name)) {
				processUnique(schemaItem, xdefContextElement);
			} else {
				processOtherSchemaElement(schemaItem, xdefContextElement);
			}
		} else {
			processOtherElement(schemaItem, xdefContextElement);
		}
	}

	/** Processes child nodes of given XML schema item. Calls processSchemaItem
	 * method for every child.
	 * @param schemaItem schema item element.
	 * @param xdefContextElement context parent X-definition element.
	 */
	protected void processChildren(Element schemaItem,
		Element xdefContextElement) {
		NodeList children = KXmlUtils.getChildElements(schemaItem);
		for (int i = 0; i < children.getLength(); i++) {
/*VT*/
		if ("true".equals(schemaItem.getAttribute("mixed"))) {
			String attName = (_xdPrefix != null && !_xdPrefix.isEmpty())
				? _xdPrefix + ":text" : "text";
			xdefContextElement.setAttributeNS(_xdURI,
				attName,"occurs * string();");
		}
/*VT*/
			Element element = (Element) children.item(i);
			processSchemaItem(element, xdefContextElement);
		}
	}

	/** Processes all schema elements and adds proper declaration to given
	 * Xdefiniton elements
	 * @param xdefElements X-definition <tt>def</tt> elements.
	 * @throws RuntimeException X-definition elements are not compatible with
	 * schema elements.
	 */
	protected final void processSchemaElements(Map xdefElements)
			throws RuntimeException {
		checkXdefElements(xdefElements);
		Iterator<Map.Entry<URL, Element>> i =
			_schemaElements.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<URL, Element> entry = i.next();
			if (!_schemaURLStack.isEmpty()) {
				throw new RuntimeException("Illegal state of currently "
					+ " processing schema URLs stack!");
			}
			_schemaURLStack.push(entry.getKey());
			resolveDebugURL((URL) entry.getKey());
			processSchema((Element) entry.getValue(),
				(Element) xdefElements.get(entry.getKey()));
			resolveDebugEnd();
			if (_schemaURLStack.size() != 1) {
				throw new RuntimeException("Illegal state of currently "
					+ " processing schema URLs stack!");
			}
			_schemaURLStack.pop();
		}
	}

	/** Processes <tt>schema</tt> element.
	 * @param schemaElement <tt>schema</tt> element to process.
	 * @param xdefElement <tt>def</tt> element to generate content.
	 */
	protected void processSchema(Element schemaElement, Element xdefElement) {
		processChildren(schemaElement, xdefElement);
	}

	/** Checks X-definition elements table if is compatible with schema elements
	 * table.
	 * @param xdefElements X-definition elements table.
	 * @throws RuntimeException not compatible.
	 */
	private void checkXdefElements(Map xdefElements)
			throws RuntimeException {
		Iterator i = _schemaElements.entrySet().iterator();
		while (i.hasNext()) {
			URL url = (URL) ((Map.Entry) i.next()).getKey();
			if (!xdefElements.containsKey(url)) {
				throw new RuntimeException("X-definition elements table"
					+ " is not compatible with schema elements!");
			}
		}
	}

	/** Processes given schema <tt>all</tt> element and adds proper
	 * declaration to given X-definition context element.
	 * @param allElement <tt>all</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processAll(Element allElement,
		Element xdefContextElement);

	/** Processes given schema <tt>annotation</tt> element and adds
	 * proper declaration to given X-definition context element.
	 * @param annotationElement <tt>annotation</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processAnnotation(Element annotationElement,
		Element xdefContextElement);

	/*** Processes given schema <tt>any</tt> element and adds proper
	 * declaration to given X-definition context element.
	 * @param anyElement <tt>any</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processAny(Element anyElement,
		Element xdefContextElement);

	/** Processes given schema <tt>anyAttribute</tt> element and adds
	 * proper declaration to given X-definition context element.
	 * @param anyAttributeElement <tt>anyAttribute</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processAnyAttribute(Element anyAttributeElement,
		Element xdefContextElement);

	/** Processes given schema <tt>appInfo</tt> element and adds
	 * proper declaration to given X-definition context element.
	 * @param appInfoElement <tt>appInfo</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processAppInfo(Element appInfoElement,
		Element xdefContextElement);

	/** Processes given schema <tt>attribute</tt> element and adds
	 * proper declaration to given X-definition context element.
	 * @param attributeElement <tt>attribute</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	public abstract void processAttribute(Element attributeElement,
		Element xdefContextElement);

	/** Processes given schema <tt>attributeGroup</tt> element and adds
	 * proper declaration to given X-definition context element.
	 * @param attributeGroupElement <tt>attributeGroup</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	public abstract void processAttributeGroup(Element attributeGroupElement,
		Element xdefContextElement);

	/** Processes given schema <tt>choice</tt> element and adds
	 * proper declaration to given X-definition context element.
	 * @param choiceElement <tt>choice</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processChoice(Element choiceElement,
		Element xdefContextElement);

	/** Processes given schema <tt>complexContent</tt> element and adds proper
	 * declaration to given X-definition context element.
	 * @param complexContentElement <tt>complexContent</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processComplexContent(Element complexContentElement,
		Element xdefContextElement);

	/** Processes given schema <tt>complexType</tt> element and adds
	 * proper declaration to given X-definition context element.
	 * @param complexTypeElement <tt>complexType</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processComplexType(Element complexTypeElement,
		Element xdefContextElement);

	/** Processes given schema <tt>documentation</tt> element and adds proper
	 * declaration to given X-definition context element.
	 * @param documentationElement <tt>documentation</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processDocumentation(Element documentationElement,
		Element xdefContextElement);

	/** Processes given schema <tt>element</tt> element and adds
	 * proper declaration to given X-definition context element.
	 * @param elementElement <tt>element</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processElement(Element elementElement,
		Element xdefContextElement);

	/** Processes given schema <tt>extension</tt> element and adds
	 * proper declaration to given X-definition context element.
	 * @param extensionElement <tt>extension</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processExtension(Element extensionElement,
		Element xdefContextElement);

	/**Processes given schema <tt>field</tt> element and adds
	 * proper declaration to given X-definition context element.
	 * @param fieldElement <tt>field</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processField(Element fieldElement,
		Element xdefContextElement);

	/** Processes given schema <tt>group</tt> element and adds
	 * proper declaration to given X-definition context element.
	 * @param groupElement <tt>group</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processGroup(Element groupElement,
		Element xdefContextElement);

	/** Processes given schema <tt>key</tt> element and
	 * adds proper declaration to given X-definition context element.
	 * @param keyElement <tt>key</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processKey(Element keyElement,
		Element xdefContextElement);

	/** Processes given schema <tt>keyref</tt> element and adds
	 * proper declaration to given X-definition context element.
	 * @param keyrefElement <tt>keyref</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processKeyref(Element keyrefElement,
		Element xdefContextElement);

	/** Processes given schema <tt>notation</tt> element and adds
	 * proper declaration to given X-definition context element.
	 * @param notationElement <tt>notation</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processNotation(Element notationElement,
		Element xdefContextElement);

	/** Processes given schema <tt>redefine</tt> element and adds
	 * proper declaration to given X-definition context element.
	 * @param redefineElement <tt>redefine</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processRedefine(Element redefineElement,
		Element xdefContextElement);

	/** Processes given schema <tt>restriction</tt> element and adds
	 * proper declaration to given X-definition context element.
	 * @param restrictionElement <tt>restriction</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processRestriction(Element restrictionElement,
		Element xdefContextElement);

	/** Processes given schema <tt>selector</tt> element and adds
	 * proper declaration to given X-definition context element.
	 * @param selectorElement <tt>selector</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processSelector(Element selectorElement,
		Element xdefContextElement);

	/** Processes given schema <tt>sequence</tt> element and adds
	 * proper declaration to given X-definition context element.
	 * @param sequenceElement <tt>sequence</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processSequence(Element sequenceElement,
		Element xdefContextElement);

	/** Processes given schema <tt>simpleContent</tt> element and adds
	 * proper declaration to given X-definition context element.
	 * @param simpleContentElement <tt>simpleContent</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processSimpleContent(Element simpleContentElement,
		Element xdefContextElement);

	/** Processes given schema <tt>simpleType</tt> element and adds
	 * proper declaration to given X-definition context element.
	 * @param simpleTypeElement <tt>simpleType</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processSimpleType(Element simpleTypeElement,
		Element xdefContextElement);

	/** Processes given schema <tt>unique</tt> element and adds
	 * proper declaration to given X-definition context element.
	 * @param uniqueElement <tt>unique</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processUnique(Element uniqueElement,
		Element xdefContextElement);

	/** Processes given schema element, other that defined in methods.
	 * @param schemaElement XML schema namespace element.
	 * @param xdefContextElement context parent X-definition element.
	 */
	protected abstract void processOtherSchemaElement(Element schemaElement,
		Element xdefContextElement);

	/** Processes non XML schema namespace element.
	 * @param schemaItem non XML schema namespace element.
	 * @param xdefContextElement context parent X-definition element.
	 */
	protected abstract void processOtherElement(Element schemaItem,
		Element xdefContextElement);

	/** Resolves debugging.
	 * @param schemaElement processing schema context element.
	 * @param xdefElement X-definition context element.
	 */
	protected abstract void resolveDebug(Element schemaElement,
		Element xdefElement);

	/** Resolves debugging schema URL.
	 * @param schemaURL URL of processing schema.
	 */
	protected abstract void resolveDebugURL(URL schemaURL);

	/** Processes schema <tt>include</tt> element and add
	 * declaration to X-definition context element.
	 * @param includeElement schema <tt>include</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processInclude(Element includeElement,
		Element xdefContextElement);

	/** Processes schema <tt>import</tt> element and adds declaration to
	 * X-definition context element.
	 * @param importElement schema <tt>import</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processImport(Element importElement,
		Element xdefContextElement);

	/** Resolves debugging end of processing schema. */
	protected abstract void resolveDebugEnd();
}