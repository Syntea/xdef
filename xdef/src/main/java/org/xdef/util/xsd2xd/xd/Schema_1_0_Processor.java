package org.xdef.util.xsd2xd.xd;

import org.xdef.XDConstants;
import org.xdef.util.xsd2xd.Processor;
import org.xdef.util.xsd2xd.Utils;
import org.xdef.util.xsd2xd.DOMUtils;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.Stack;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.sys.SReporter;


/** Represents XML Schema 1.0 to X-definition 2.0 convertor.
 * @author Ilia Alexandrov
 */
public class Schema_1_0_Processor extends Processor {
	/** X-definition namespace uri of actual version. */
	public static final String XDEF_URI = XDConstants.XDEF42_NS_URI;
	/** Representation of X-definition document. */
	private final XdefDocument _xdef;
	/** Error and warning reporter. */
	private final SReporter _reporter;
	/** Every def as file switch. */
	private final boolean _separately;
	/** Stack of referencing elements (Element). */
	private final Stack<Element> _refElementsStack = new Stack<>();
	/** Stack of namespaces during importing and including declarations. */
	private final Stack<String> _namespaceStack = new Stack<>();

	/** Creates instance of XML Schema 1.0 to X-definition 2.0 convertor.
	 * X-definition elements will have given prefix. X-definition will be
	 * generated from root schema at given URL.
	 *
	 * @param xdefPrefix prefix of X-definition nodes.
	 * @param reporter reporter to report warnings and errors.
	 * @param rootSchemaURL URL of root schema to convert.
	 * @param separately X-definitions as separate files.
	 */
	public Schema_1_0_Processor(String xdefPrefix,
		SReporter reporter,
		URL rootSchemaURL,
		boolean separately) {
		super(rootSchemaURL);
		_reporter = reporter;
		_separately = separately;
		_xdef = new XdefDocument(_schemaElements,
			xdefPrefix, XDEF_URI, _separately);
		processSchemaElements(_xdef.getXdefElements());
	}
	@Override
	protected void processAll(Element allElement, Element parentXdefElement) {
		Element element = ProcessMethods.processObjectGroup(allElement,
			parentXdefElement, _xdef);
		processChildren(allElement, element);
	}
	@Override
	protected void processAnnotation(Element annotationElement,
		Element parentXdefElement) {
//		_reporter.warning("", getReportMessage(annotationElement));
	}
	@Override
	protected void processAny(Element anyElement, Element parentXdefElement) {
		Element any =
			ProcessMethods.processAny(anyElement, parentXdefElement, _xdef);
//		_reporter.warning("", "Any element does not have namespace restriction "
//				+ "declaration in given Xdefnition version!");
		processChildren(anyElement, any);
	}
	@Override
	protected void processAnyAttribute(Element anyAttributeElement,
		Element parentXdefElement) {
		ProcessMethods.processAnyAttribute(anyAttributeElement,
			parentXdefElement, _xdef);
//		_reporter.warning("",
//			"AnyAttribute element does not have namespace restriction "
//				+ "declaration in given Xdefnition version!");
		processChildren(anyAttributeElement, parentXdefElement);
	}
	@Override
	protected void processAppInfo(Element appInfoElement,
		Element parentXdefElement) {
//		_reporter.warning("", getReportMessage(appInfoElement));
	}
	@Override
	public void processAttribute(Element attributeElement,
		Element parentXdefElement) {
		ProcessMethods.processAttribute(attributeElement, parentXdefElement,
			_xdef, _refElementsStack, _schemaURLStack, _schemaElements,
			_namespaceStack, _reporter, this);
		processChildren(attributeElement, parentXdefElement);
	}
	@Override
	public void processAttributeGroup(Element attributeGroupElement,
		Element parentXdefElement) {
		ProcessMethods.processAttributeGroup(attributeGroupElement,
			parentXdefElement, _xdef, _refElementsStack, _schemaURLStack,
			_namespaceStack, _schemaElements, _reporter, this);
		processChildren(attributeGroupElement, parentXdefElement);
	}
	@Override
	protected void processChoice(Element choiceElement,
		Element parentXdefElement) {
		Element element = ProcessMethods.processObjectGroup(choiceElement,
			parentXdefElement, _xdef);
		processChildren(choiceElement, element);
	}
	@Override
	protected void processComplexContent(Element complexContentElement,
		Element parentXdefElement) {
		processChildren(complexContentElement, parentXdefElement);
	}
	@Override
	protected void processComplexType(Element complexTypeElement,
		Element parentXdefElement) {
		//is schema child or redefine child
		if (Utils.isSchemaChild(complexTypeElement)
			|| Utils.isRedefineSchemaChild(complexTypeElement)) {
			Element element = ProcessMethods.processComplexType(
				complexTypeElement, parentXdefElement, _xdef);
			processChildren(complexTypeElement, element);
		} else {
			processChildren(complexTypeElement, parentXdefElement);
		}
	}
	@Override
	protected void processDocumentation(Element documentationElement,
		Element parentXdefElement) {
//		_reporter.warning("", getReportMessage(documentationElement));
	}
	@Override
	protected void processElement(Element elementElement,
		Element parentXdefElement) {
		Element element = ProcessMethods.processElement(elementElement,
			parentXdefElement, _xdef, _schemaURLStack, _schemaElements);
		processChildren(elementElement, element);
	}
	@Override
	protected void processExtension(Element extensionElement,
		Element parentXdefElement) {
		//processing extension
		ProcessMethods.processExtension(extensionElement, parentXdefElement,
			_xdef, _schemaURLStack, _schemaElements);
		//processing extension children
		processChildren(extensionElement, parentXdefElement);
	}
	@Override
	protected void processField(Element fieldElement,
		Element parentXdefElement) {
//		_reporter.warning("", getReportMessage(fieldElement));
	}
	@Override
	protected void processGroup(Element groupElement,
		Element parentXdefElement) {
		Element element = ProcessMethods.processGroup(groupElement,
			parentXdefElement, _xdef, _schemaURLStack.peek(), _schemaElements);
		processChildren(groupElement, element);
	}
	@Override
	protected void processKey(Element keyElement, Element parentXdefElement) {
//		_reporter.warning("", getReportMessage(keyElement));
	}
	@Override
	protected void processKeyref(Element keyrefElement,
		Element parentXdefElement) {
//		_reporter.warning("", getReportMessage(keyrefElement));
	}
	@Override
	protected void processNotation(Element notationElement,
		Element parentXdefElement) {
	}
	@Override
	protected void processRedefine(Element redefineElement,
		Element parentXdefElement) {
		if (_separately) {
			ProcessMethods.processExternal(redefineElement, parentXdefElement,
				_xdef, _schemaURLStack.peek());
		}
		processChildren(redefineElement, parentXdefElement);
	}
	@Override
	protected void processRestriction(Element restrictionElement,
		Element parentXdefElement) {
		ProcessMethods.processRestriction(restrictionElement, parentXdefElement,
			_xdef, _schemaURLStack.peek(), _schemaElements);
		processChildren(restrictionElement, parentXdefElement);
	}
	@Override
	protected void processSelector(Element selectorElement,
		Element parentXdefElement) {
		_reporter.warning("", getReportMessage(selectorElement));
	}
	@Override
	protected void processSequence(Element sequenceElement,
		Element parentXdefElement) {
		Element element = ProcessMethods.processObjectGroup(sequenceElement,
			parentXdefElement, _xdef);
		processChildren(sequenceElement, element);
	}
	@Override
	protected void processSimpleContent(Element simpleContentElement,
		Element parentXdefElement) {
		processChildren(simpleContentElement, parentXdefElement);
	}
	@Override
	protected void processSimpleType(Element simpleTypeElement,
		Element parentXdefElement) {
		ProcessMethods.processSimpleType(simpleTypeElement, parentXdefElement,
			_xdef, _schemaURLStack, _schemaElements, _reporter);
	}
	@Override
	protected void processUnique(Element uniqueElement,
		Element parentXdefElement) {
//		_reporter.warning("", getReportMessage(uniqueElement));
	}
	@Override
	protected void processOtherSchemaElement(Element schemaElement,
		Element parentXdefElement) {}
	@Override
	protected void processOtherElement(Element schemaItem,
		Element parentXdefElement) {}
	@Override
	protected void resolveDebug(Element schemaElement, Element xdefElement) {
//		_reporter.warning("", getTime() + " Processing \""
//			+ DOMUtils.getXPosition(schemaElement) + "\" schema item "
//			+ "and adding declaration to \""
//			+ DOMUtils.getXPosition(xdefElement) + "\" X-definition item...");
	}
	@Override
	public void writeCollection(String collectionFileName)
		throws IOException, IllegalStateException {
		_xdef.writeCollection(collectionFileName);
	}
	@Override
	public void writeXdefFiles(String directoryName)
		throws IOException, IllegalStateException {
		_xdef.writeXdefFiles(directoryName);
	}
	@Override
	public Document getCollectionDocument() throws IllegalStateException {
		return _xdef.getCollectionDocument();
	}
	@Override
	public Set<Document> getXdefDocuments() throws IllegalStateException {
		return _xdef.getXdefDocuments();
	}

	/** Returns report message of not supported schema item in current
	 * X-definition version.
	 *
	 * @param notSupportedScemaItem not supported schema item.
	 * @return report message.
	 */
	private String getReportMessage(Element notSupportedSchemaElement) {
		return "Schema item \"" + notSupportedSchemaElement.getLocalName()
			+ "\" (" + DOMUtils.getXPosition(notSupportedSchemaElement)
			+ ") is not supported in X-definition version 2.0";
	}
	@Override
	protected void processInclude(Element includeElement,
		Element xdefContextElement) {
		if (_separately) {
			ProcessMethods.processExternal(includeElement,
				xdefContextElement, _xdef, _schemaURLStack.peek());
		}
	}
	@Override
	protected void processImport(Element importElement,
		Element xdefContextElement) {
		if (_separately) {
			ProcessMethods.processExternal(importElement,
				xdefContextElement, _xdef, _schemaURLStack.peek());
		}
	}

	@Override
	public void printCollection() {_xdef.printCollection();}

	@Override
	protected void resolveDebugURL(URL schemaURL) {
//		_reporter.warning("", getTime()
//			+ " Processing schema at URL '" + schemaURL.getPath() + "'...");
	}

	@Override
	protected void resolveDebugEnd() {
//		_reporter.warning("", getTime() + " End processing schema." + "\n");
	}
}