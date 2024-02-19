package org.xdef.util.xsd2xd.schema_1_0;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import org.xdef.xml.KDOMBuilder;
import org.xdef.util.xsd2xd.Convertor;
import org.xdef.util.xsd2xd.utils.DOMUtils;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import org.w3c.dom.*;

/** Abstract class that represents converting logic of XML Schema 1.0. Contains
 * abstract methods for processing schema items.
 * @author Ilia Alexandrov
 */
public abstract class Processor implements Convertor {

	/** All XML Schema schema elements table (URL, Element). */
	protected final Map<URL, Element> _schemaElements;
	/** Stack of URLs of currently processing schema elements (URL). */
	protected final Stack<URL> _schemaURLStack = new Stack<URL>();

	/** Creates instance with root schema at given URL. Initializes all schema
	 * elements.
	 * @param rootSchemaURL URL of root schema.
	 */
	public Processor(URL rootSchemaURL) {
		try {
			String urlString = rootSchemaURL.toExternalForm();
//			String replaced = urlString.replace('\\', '/');
			String replaced =
				URLDecoder.decode(urlString.replace('\\', '/'), "UTF-8");
			URL newURL = new URL(replaced);
			_schemaElements = getSchemaElements(newURL);
		} catch (UnsupportedEncodingException | MalformedURLException ex) {
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
		NodeList externals = DOMUtils.getChildElementsNS(schemaElement,
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
				} catch (RuntimeException ex) {
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
			if (null == name) {
				processOtherSchemaElement(schemaItem, xdefContextElement);
			} else switch (name) {
				case Utils.ALL:
					processAll(schemaItem, xdefContextElement);
					break;
				case Utils.ANNOTATION:
					processAnnotation(schemaItem, xdefContextElement);
					break;
				case Utils.ANY:
					processAny(schemaItem, xdefContextElement);
					break;
				case Utils.ANY_ATTRIBUTE:
					processAnyAttribute(schemaItem, xdefContextElement);
					break;
				case Utils.APP_INFO:
					processAppInfo(schemaItem, xdefContextElement);
					break;
				case Utils.ATTRIBUTE:
					processAttribute(schemaItem, xdefContextElement);
					break;
				case Utils.ATTRIBUTE_GROUP:
					processAttributeGroup(schemaItem, xdefContextElement);
					break;
				case Utils.CHOICE:
					processChoice(schemaItem, xdefContextElement);
					break;
				case Utils.COMPLEX_CONTENT:
					processComplexContent(schemaItem, xdefContextElement);
					break;
				case Utils.COMPLEX_TYPE:
					processComplexType(schemaItem, xdefContextElement);
					break;
				case Utils.DOCUMENTATION:
					processDocumentation(schemaItem, xdefContextElement);
					break;
				case Utils.ELEMENT:
					processElement(schemaItem, xdefContextElement);
					break;
				case Utils.EXTENSION:
					processExtension(schemaItem, xdefContextElement);
					break;
				case Utils.FIELD:
					processField(schemaItem, xdefContextElement);
					break;
				case Utils.GROUP:
					processGroup(schemaItem, xdefContextElement);
					break;
				case Utils.IMPORT:
					processImport(schemaItem, xdefContextElement);
					break;
				case Utils.INCLUDE:
					processInclude(schemaItem, xdefContextElement);
					break;
				case Utils.KEY:
					processKey(schemaItem, xdefContextElement);
					break;
				case Utils.KEYREF:
					processKeyref(schemaItem, xdefContextElement);
					break;
				case Utils.NOTATION:
					processNotation(schemaItem, xdefContextElement);
					break;
				case Utils.REDEFINE:
					processRedefine(schemaItem, xdefContextElement);
					break;
				case Utils.RESTRICTION:
					processRestriction(schemaItem, xdefContextElement);
					break;
				case Utils.SELECTOR:
					processSelector(schemaItem, xdefContextElement);
					break;
				case Utils.SEQUENCE:
					processSequence(schemaItem, xdefContextElement);
					break;
				case Utils.SIMPLE_CONTENT:
					processSimpleContent(schemaItem, xdefContextElement);
					break;
				case Utils.SIMPLE_TYPE:
					processSimpleType(schemaItem, xdefContextElement);
					break;
				case Utils.UNIQUE:
					processUnique(schemaItem, xdefContextElement);
					break;
				default:
					processOtherSchemaElement(schemaItem, xdefContextElement);
					break;
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
		NodeList children = DOMUtils.getChildElements(schemaItem);
		for (int i = 0; i < children.getLength(); i++) {
			Element element = (Element) children.item(i);
			processSchemaItem(element, xdefContextElement);
		}
	}

	/** Processes all schema elements and adds proper declaration to given
	 * Xdefiniton elements
	 * @param xdefElements X-definition def elements.
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
				throw new RuntimeException(
					"Illegal state of currently processing schema URLs stack!");
			}
			_schemaURLStack.push(entry.getKey());
			resolveDebugURL((URL) entry.getKey());
			processSchema((Element) entry.getValue(),
				(Element) xdefElements.get(entry.getKey()));
			resolveDebugEnd();
			if (_schemaURLStack.size() != 1) {
				throw new RuntimeException(
					"Illegal state of currently processing schema URLs stack!");
			}
			_schemaURLStack.pop();
		}
	}

	/** Processes schema element.
	 * @param schemaElement schema element to process.
	 * @param xdefElement def element to generate content.
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
				throw new RuntimeException(
					"Not compatible X-def elements with schema elements");
			}
		}
	}

	/** Processes given schema all element and adds proper
	 * declaration to given X-definition context element.
	 * @param allElement all element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processAll(Element allElement,
		Element xdefContextElement);

	/** Processes given schema annotation element and adds
	 * proper declaration to given X-definition context element.
	 * @param annotationElement annotation element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processAnnotation(Element annotationElement,
		Element xdefContextElement);

	/*** Processes given schema any element and adds proper
	 * declaration to given X-definition context element.
	 * @param anyElement any element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processAny(Element anyElement,
		Element xdefContextElement);

	/** Processes given schema anyAttribute element and adds
	 * proper declaration to given X-definition context element.
	 * @param anyAttributeElement anyAttribute element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processAnyAttribute(Element anyAttributeElement,
		Element xdefContextElement);

	/** Processes given schema appInfo element and adds
	 * proper declaration to given X-definition context element.
	 * @param appInfoElement appInfo element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processAppInfo(Element appInfoElement,
		Element xdefContextElement);

	/** Processes given schema attribute element and adds
	 * proper declaration to given X-definition context element.
	 * @param attributeElement attribute element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	public abstract void processAttribute(Element attributeElement,
		Element xdefContextElement);

	/** Processes given schema attributeGroup element and adds
	 * proper declaration to given X-definition context element.
	 * @param attributeGroupElement attributeGroup element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	public abstract void processAttributeGroup(Element attributeGroupElement,
		Element xdefContextElement);

	/** Processes given schema choice element and adds
	 * proper declaration to given X-definition context element.
	 * @param choiceElement choice element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processChoice(Element choiceElement,
		Element xdefContextElement);

	/** Processes given schema complexContent element and adds proper
	 * declaration to given X-definition context element.
	 * @param complexContentElement complexContent element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processComplexContent(Element complexContentElement,
		Element xdefContextElement);

	/** Processes given schema complexType element and adds
	 * proper declaration to given X-definition context element.
	 * @param complexTypeElement complexType element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processComplexType(Element complexTypeElement,
		Element xdefContextElement);

	/** Processes given schema documentation element and adds proper
	 * declaration to given X-definition context element.
	 * @param documentationElement documentation element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processDocumentation(Element documentationElement,
		Element xdefContextElement);

	/** Processes given schema element element and adds
	 * proper declaration to given X-definition context element.
	 * @param elementElement element element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processElement(Element elementElement,
		Element xdefContextElement);

	/** Processes given schema extension element and adds
	 * proper declaration to given X-definition context element.
	 * @param extensionElement extension element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processExtension(Element extensionElement,
		Element xdefContextElement);

	/**Processes given schema field element and adds
	 * proper declaration to given X-definition context element.
	 * @param fieldElement field element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processField(Element fieldElement,
		Element xdefContextElement);

	/** Processes given schema group element and adds
	 * proper declaration to given X-definition context element.
	 * @param groupElement group element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processGroup(Element groupElement,
		Element xdefContextElement);

	/** Processes given schema key element and
	 * adds proper declaration to given X-definition context element.
	 * @param keyElement key element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processKey(Element keyElement,
		Element xdefContextElement);

	/** Processes given schema keyref element and adds
	 * proper declaration to given X-definition context element.
	 * @param keyrefElement keyref element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processKeyref(Element keyrefElement,
		Element xdefContextElement);

	/** Processes given schema notation element and adds
	 * proper declaration to given X-definition context element.
	 * @param notationElement notation element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processNotation(Element notationElement,
		Element xdefContextElement);

	/** Processes given schema redefine element and adds
	 * proper declaration to given X-definition context element.
	 * @param redefineElement redefine element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processRedefine(Element redefineElement,
		Element xdefContextElement);

	/** Processes given schema restriction element and adds
	 * proper declaration to given X-definition context element.
	 * @param restrictionElement restriction element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processRestriction(Element restrictionElement,
		Element xdefContextElement);

	/** Processes given schema selector element and adds
	 * proper declaration to given X-definition context element.
	 * @param selectorElement selector element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processSelector(Element selectorElement,
		Element xdefContextElement);

	/** Processes given schema sequence element and adds
	 * proper declaration to given X-definition context element.
	 * @param sequenceElement sequence element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processSequence(Element sequenceElement,
		Element xdefContextElement);

	/** Processes given schema simpleContent element and adds
	 * proper declaration to given X-definition context element.
	 * @param simpleContentElement simpleContent element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processSimpleContent(Element simpleContentElement,
		Element xdefContextElement);

	/** Processes given schema simpleType element and adds
	 * proper declaration to given X-definition context element.
	 * @param simpleTypeElement simpleType element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processSimpleType(Element simpleTypeElement,
		Element xdefContextElement);

	/** Processes given schema unique element and adds
	 * proper declaration to given X-definition context element.
	 * @param uniqueElement unique element to process.
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

	/** Processes schema include element and add
	 * declaration to X-definition context element.
	 * @param includeElement schema include element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processInclude(Element includeElement,
		Element xdefContextElement);

	/** Processes schema import element and adds declaration to
	 * X-definition context element.
	 * @param importElement schema import element to process.
	 * @param xdefContextElement X-definition context element.
	 */
	protected abstract void processImport(Element importElement,
		Element xdefContextElement);

	/** Resolves debugging end of processing schema. */
	protected abstract void resolveDebugEnd();
}