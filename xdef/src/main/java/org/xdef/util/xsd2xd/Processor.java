package org.xdef.util.xsd2xd;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import org.w3c.dom.*;
import org.xdef.xml.KXmlUtils;

/** Abstract class that represents converting logic of XML Schema 1.0. Contains
 * abstract methods for processing schema items.
 * @author Ilia Alexandrov
 */
public abstract class Processor implements Convertor {
    /** All XML Schema schema elements table (URL, Element). */
    protected final Map<URL, Element> _schemaElements;
    /** Stack of URLs of currently processing schema elements (URL). */
    protected final Stack<URL> _schemaURLStack;

    /** Creates instance with root schema at given URL. Initializes all schema elements.
     * @param rootSchemaURL URL of root schema.
     */
    public Processor(URL rootSchemaURL) {
        try {
            _schemaURLStack = new Stack<URL>();
            String urlString = rootSchemaURL.toExternalForm();
            String replaced = URLDecoder.decode(urlString.replace('\\', '/'), "UTF-8");
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

    /** Recursive method for getting all URLs of related schemas to the schema at given URL.
     * @param urlStack stack of already parsed URLs.
     * @param schemaURL URL of root schema file.
     * @return set of schema URLs related to given schema.
     */
    private Map<URL, Element> getSchemaElements(Map<URL, Element>schemaElements, URL schemaURL) {
        //getting schema element
        Element schemaElement = KXmlUtils.parseXml(schemaURL).getDocumentElement();
        //checking schema element
        if (!Utils.isSchema(schemaElement)) {
            throw new RuntimeException("Not a schema!");
        }
        //adding to schema elements
        schemaElements.put(schemaURL, schemaElement);
        //getting include, import and redefine elements
        NodeList externals = DOMUtils.getChildElementsNS(schemaElement,
            Utils.NSURI_SCHEMA, new String[] {Utils.INCLUDE, Utils.IMPORT, Utils.REDEFINE});
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

    /** Process XML schema item. Call proper method according to given schema item type.
     * @param schemaItem schema item element.
     * @param xdefContextElement context parent X-definition element.
     */
    protected void processSchemaItem(Element schemaItem, Element xdefContextElement) {
        //resolving debugging
        resolveDebug(schemaItem, xdefContextElement);
        //element is XML schema element
        if (Utils.NSURI_SCHEMA.equals(schemaItem.getNamespaceURI())) {
            String name = schemaItem.getLocalName();
            if (null == name) {
                processOtherSchemaElement(schemaItem, xdefContextElement);
            } else switch (name) {
                case Utils.ALL: processAll(schemaItem, xdefContextElement); break;
                case Utils.ANNOTATION: processAnnotation(schemaItem, xdefContextElement); break;
                case Utils.ANY: processAny(schemaItem, xdefContextElement); break;
                case Utils.ANY_ATTRIBUTE: processAnyAttribute(schemaItem, xdefContextElement); break;
                case Utils.APP_INFO: processAppInfo(schemaItem, xdefContextElement); break;
                case Utils.ATTRIBUTE: processAttribute(schemaItem, xdefContextElement); break;
                case Utils.ATTRIBUTE_GROUP: processAttributeGroup(schemaItem, xdefContextElement); break;
                case Utils.CHOICE: processChoice(schemaItem, xdefContextElement); break;
                case Utils.COMPLEX_CONTENT: processComplexContent(schemaItem, xdefContextElement); break;
                case Utils.COMPLEX_TYPE: processComplexType(schemaItem, xdefContextElement); break;
                case Utils.DOCUMENTATION: processDocumentation(schemaItem, xdefContextElement); break;
                case Utils.ELEMENT: processElement(schemaItem, xdefContextElement); break;
                case Utils.EXTENSION: processExtension(schemaItem, xdefContextElement); break;
                case Utils.FIELD: processField(schemaItem, xdefContextElement); break;
                case Utils.GROUP: processGroup(schemaItem, xdefContextElement); break;
                case Utils.IMPORT: processImport(schemaItem, xdefContextElement); break;
                case Utils.INCLUDE: processInclude(schemaItem, xdefContextElement); break;
                case Utils.KEY: processKey(schemaItem, xdefContextElement); break;
                case Utils.KEYREF: processKeyref(schemaItem, xdefContextElement); break;
                case Utils.NOTATION: processNotation(schemaItem, xdefContextElement); break;
                case Utils.REDEFINE: processRedefine(schemaItem, xdefContextElement); break;
                case Utils.RESTRICTION: processRestriction(schemaItem, xdefContextElement); break;
                case Utils.SELECTOR: processSelector(schemaItem, xdefContextElement); break;
                case Utils.SEQUENCE: processSequence(schemaItem, xdefContextElement); break;
                case Utils.SIMPLE_CONTENT: processSimpleContent(schemaItem, xdefContextElement); break;
                case Utils.SIMPLE_TYPE: processSimpleType(schemaItem, xdefContextElement); break;
                case Utils.UNIQUE: processUnique(schemaItem, xdefContextElement); break;
                default: processOtherSchemaElement(schemaItem, xdefContextElement);
            }
        } else {
            processOtherElement(schemaItem, xdefContextElement);
        }
    }

    /** Process child nodes of given XML schema item. Call processSchemaItem method for every child.
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

    /** Process all schema elements and add proper declaration to given Xdefiniton elements.
     * @param xdefElements X-definition def elements.
     * @throws RuntimeException X-definition elements are not compatible with schema elements.
     */
    protected final void processSchemaElements(Map xdefElements) throws RuntimeException {
        checkXdefElements(xdefElements);
        for (Map.Entry<URL, Element> entry : _schemaElements.entrySet()) {
            if (!_schemaURLStack.isEmpty()) {
                throw new RuntimeException( "Illegal state of currently processing schema URLs stack!");
            }
            _schemaURLStack.push(entry.getKey());
            resolveDebugURL((URL) entry.getKey());
            processSchema((Element) entry.getValue(), (Element) xdefElements.get(entry.getKey()));
            resolveDebugEnd();
            if (_schemaURLStack.size() != 1) {
                throw new RuntimeException( "Illegal state of currently processing schema URLs stack!");
            }
            _schemaURLStack.pop();
        }
    }

    /** Process schema element.
     * @param schemaElement schema element to process.
     * @param xdefElement def element to generate content.
     */
    protected void processSchema(Element schemaElement, Element xdefElement) {
        processChildren(schemaElement, xdefElement);
    }

    /** Checks X-definition elements table if is compatible with schema elements table.
     * @param xdefElements X-definition elements table.
     * @throws RuntimeException not compatible.
     */
    private void checkXdefElements(Map xdefElements) throws RuntimeException {
        Iterator i = _schemaElements.entrySet().iterator();
        while (i.hasNext()) {
            URL url = (URL) ((Map.Entry) i.next()).getKey();
            if (!xdefElements.containsKey(url)) {
                throw new RuntimeException( "Not compatible Xdef elements with schema elements");
            }
        }
    }

    /** Process given schema all element and adds proper declaration to given X-definition context element.
     * @param allElement all element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processAll(Element allElement, Element xdefContextElement);

    /** Process given schema annotation element and add proper declaration to X-definition context element.
     * @param annotationElement annotation element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processAnnotation(Element annotationElement, Element xdefContextElement);

    /*** Process given schema any element and add proper declaration to given X-definition context element.
     * @param anyElement any element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processAny(Element anyElement, Element xdefContextElement);

    /** Process given schema anyAttribute element and add proper declaration to X-definition context element.
     * @param anyAttributeElement anyAttribute element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processAnyAttribute(Element anyAttributeElement, Element xdefContextElement);

    /** Process given schema appInfo element and add proper declaration to given X-definition context element.
     * @param appInfoElement appInfo element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processAppInfo(Element appInfoElement, Element xdefContextElement);

    /** Process given schema attribute element and add proper declaration to X-definition context element.
     * @param attributeElement attribute element to process.
     * @param xdefContextElement X-definition context element.
     */
    public abstract void processAttribute(Element attributeElement, Element xdefContextElement);

    /** Process given schema attributeGroup element and add proper
     * declaration to X-definition context element.
     * @param attributeGroupElement attributeGroup element to process.
     * @param xdefContextElement X-definition context element.
     */
    public abstract void processAttributeGroup(Element attributeGroupElement, Element xdefContextElement);

    /** Process given schema choice element and add proper declaration to given X-definition context element.
     * @param choiceElement choice element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processChoice(Element choiceElement, Element xdefContextElement);

    /** Process schema complexContent element and add proper declaration to X-definition context element.
     * @param complexContentElement complexContent element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processComplexContent(Element complexContentElement, Element xdefContextElement);

    /** Process schema complexType element and add proper declaration to given X-definition context element.
     * @param complexTypeElement complexType element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processComplexType(Element complexTypeElement, Element xdefContextElement);

    /** Process schema documentation element and add proper declaration to given X-definition context element.
     * @param documentationElement documentation element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processDocumentation(Element documentationElement, Element xdefContextElement);

    /** Process schema element element and add proper declaration to given X-definition context element.
     * @param elementElement element element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processElement(Element elementElement, Element xdefContextElement);

    /** Process schema extension element and add proper declaration to given X-definition context element.
     * @param extensionElement extension element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processExtension(Element extensionElement, Element xdefContextElement);

    /**Process schema field element and add proper declaration to given X-definition context element.
     * @param fieldElement field element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processField(Element fieldElement, Element xdefContextElement);

    /** Process given schema group element and add proper declaration to given X-definition context element.
     * @param groupElement group element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processGroup(Element groupElement, Element xdefContextElement);

    /** Process given schema key element and add proper declaration to given X-definition context element.
     * @param keyElement key element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processKey(Element keyElement, Element xdefContextElement);

    /** Process given schema keyref element and add proper declaration to given X-definition context element.
     * @param keyrefElement keyref element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processKeyref(Element keyrefElement, Element xdefContextElement);

    /** Process schema notation element and add proper declaration to given X-definition context element.
     * @param notationElement notation element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processNotation(Element notationElement, Element xdefContextElement);

    /** Process schema redefine element and add proper declaration to given X-definition context element.
     * @param redefineElement redefine element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processRedefine(Element redefineElement, Element xdefContextElement);

    /** Process schema restriction element and add proper declaration to given X-definition context element.
     * @param restrictionElement restriction element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processRestriction(Element restrictionElement, Element xdefContextElement);

    /** Process schema selector element and add proper declaration to given X-definition context element.
     * @param selectorElement selector element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processSelector(Element selectorElement, Element xdefContextElement);

    /** Process schema sequence element and add proper declaration to given X-definition context element.
     * @param sequenceElement sequence element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processSequence(Element sequenceElement, Element xdefContextElement);

    /** Process schema simpleContent element and add proper declaration to given X-definition context element.
     * @param simpleContentElement simpleContent element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processSimpleContent(Element simpleContentElement, Element xdefContextElement);

    /** Process schema simpleType element and add proper declaration to given X-definition context element.
     * @param simpleTypeElement simpleType element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processSimpleType(Element simpleTypeElement, Element xdefContextElement);

    /** Process schema unique element and add proper declaration to given X-definition context element.
     * @param uniqueElement unique element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processUnique(Element uniqueElement, Element xdefContextElement);

    /** Process given schema element, other that defined in methods.
     * @param schemaElement XML schema namespace element.
     * @param xdefContextElement context parent X-definition element.
     */
    protected abstract void processOtherSchemaElement(Element schemaElement, Element xdefContextElement);

    /** Process non XML schema namespace element.
     * @param schemaItem non XML schema namespace element.
     * @param xdefContextElement context parent X-definition element.
     */
    protected abstract void processOtherElement(Element schemaItem, Element xdefContextElement);

    /** Resolves debugging.
     * @param schemaElement processing schema context element.
     * @param xdefElement X-definition context element.
     */
    protected abstract void resolveDebug(Element schemaElement, Element xdefElement);

    /** Resolves debugging schema URL.
     * @param schemaURL URL of processing schema.
     */
    protected abstract void resolveDebugURL(URL schemaURL);

    /** Process schema include element and add declaration to X-definition context element.
     * @param includeElement schema include element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processInclude(Element includeElement, Element xdefContextElement);

    /** Process schema import element and add declaration to X-definition context element.
     * @param importElement schema import element to process.
     * @param xdefContextElement X-definition context element.
     */
    protected abstract void processImport(Element importElement, Element xdefContextElement);

    /** Resolve debugging end of processing schema. */
    protected abstract void resolveDebugEnd();
}