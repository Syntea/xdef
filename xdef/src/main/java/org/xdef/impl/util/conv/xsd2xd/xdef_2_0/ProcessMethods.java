package org.xdef.impl.util.conv.xsd2xd.xdef_2_0;

import org.xdef.sys.SReporter;
import org.xdef.msg.XDEF;
import org.xdef.impl.util.conv.xsd.xsd_1_0.XsdUtils;
import org.xdef.impl.util.conv.xsd2xd.schema_1_0.Processor;
import org.xdef.impl.util.conv.xsd2xd.util.DOMUtils;
import org.xdef.impl.util.conv.xsd.xsd_1_0.type.domain.SimpleType;
import org.xdef.impl.util.conv.xsd.xsd_1_0.type.domain.Type;
import java.net.URL;
import org.xdef.impl.util.conv.xsd2xd.schema_1_0.Utils;
import org.xdef.xml.KXmlUtils;
import java.util.Map;
import java.util.Stack;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** Provides static methods for processing schema items and adding declaration
 * to X-definition items.
 * @author Alexandrov
 */
public class ProcessMethods {

	/** Private constructor.*/
	private ProcessMethods() {}

	/** Processes schema object group item and adds declaration to the given
	 * parent X-definition item. Returns added X-definition element.
	 * @param objectGroupElement schema object group element.
	 * @param xdefContextElement parent X-definition element.
	 * @param xdef implementation of X-definition document.
	 * @return added X-definition element.
	 */
	public static Element processObjectGroup(Element objectGroupElement,
		Element xdefContextElement,
		XdefDocument xdef) {
		String groupType = getObjectGroupType(objectGroupElement);
		// remove unused <xd:sequence> after an element
		Node parentNode = objectGroupElement.getParentNode();
		if (Node.ELEMENT_NODE == parentNode.getNodeType()) {
			Element parentElem = (Element) parentNode;
			if ("sequence".equals(groupType)
				&& Utils.NSURI_SCHEMA.equals(parentElem.getNamespaceURI())
				&& "complexType".equals(parentElem.getLocalName())
				&& 1 == Utils.getMinOccurrence(objectGroupElement)
				&& 1 == Utils.getMaxOccurrence(objectGroupElement)) {
				//add nothing (return current xdefinition context element)
				return xdefContextElement;
			}
		}
		Element element = xdef.addXdefElement(xdefContextElement,
			groupType);
		//is group child that is schema child
		if (Utils.isSchemaGroupChild(objectGroupElement)
			|| Utils.isRedefineSchemaChild(
				(Element) objectGroupElement.getParentNode())) {
			xdef.addXdefAttr(element,
				"name",
				((Element) objectGroupElement.getParentNode())
					.getAttribute("name"));
		} else {
			Integer minOccurs = Utils.getMinOccurrence(objectGroupElement);
			//resolving minimal occurrence for mixed
			if ("mixed".equals(groupType) && minOccurs == 1) {
				minOccurs = 0;
			}
			xdef.addOccurrenceExpression(element, minOccurs,
				Utils.getMaxOccurrence(objectGroupElement));
		}
		return element;
	}

	/** Returns X-definition object group name according to given XML schema
	 * object group.
	 * @param objectGroupElement XML schema object group.
	 * @return X-definition object group name.
	 * @throws IllegalArgumentException not a schema object group item.
	 */
	private static String getObjectGroupType(Element objectGroupElement)
		throws IllegalArgumentException {
		String type = objectGroupElement.getLocalName();
		if ("all".equals(type)) {
			return "mixed";
		} else if ("choice".equals(type)) {
			return "choice";
		} else if ("sequence".equals(type)) {
			return "sequence";
		}
		throw new IllegalArgumentException("Given element '"
			+ objectGroupElement.getLocalName() + "' is not a XML schema "
			+ "object group item!");
	}

	/** Processes <tt>any</tt> schema element and adds declaration
	 * to given parent XDefnition element.
	 * @param anyElement schema <tt>any</tt> element to process.
	 * @param xdefContextElement XDefnition context element.
	 * @param xdef X-definition document representation.
	 * @return added element.
	 */
	public static Element processAny(Element anyElement,
		Element xdefContextElement,
		XdefDocument xdef) {
		Element any = xdef.addXdefElement(xdefContextElement, "any");
		xdef.addOccurrenceExpression(any,
			Utils.getMinOccurrence(anyElement),
			Utils.getMaxOccurrence(anyElement));
		return any;
	}

	/** Processes schema <tt>anyAttribute</tt> element and adds declaration
	 * to given parent X-definition element.
	 * @param anyAttributeElement schema <tt>anyAttribute</tt> element to
	 * process.
	 * @param xdefContextElement XDefnition context element.
	 * @param xdef the X-definition.
	 */
	public static void processAnyAttribute(Element anyAttributeElement,
		Element xdefContextElement, XdefDocument xdef) {
		xdef.addXdefAttr(xdefContextElement, "attr", "occurs 0..*");
	}

	/** Processes given <tt>complexType</tt> element and adds declaration
	 * to given XDefnition context element. Returns created element.
	 * @param complexTypeElement <tt>complexType</tt> element to process.
	 * @param xdefContextElement X-definition context element.
	 * @param xdef representation of X-definition document.
	 * @return created element.
	 */
	public static Element processComplexType(Element complexTypeElement,
		Element xdefContextElement,
		XdefDocument xdef) {
		String name = complexTypeElement.getAttribute("name");
		String namespace = Utils.getNamespace(complexTypeElement);
/*VT*/
		String s = name.endsWith("_cType") ? name : (name + "_cType");
		Element result = xdef.addElement(xdefContextElement, namespace, s);
		if ("true".equals(complexTypeElement.getAttribute("mixed"))) {
			xdef.addXdefAttr(result, "text", "occurs * string();");
		}
		return result;
/*VT*/
	}

	/** Processes given <tt>simpleType</tt> element and adds declaration
	 * to the given X-definition document context element.
	 * @param simpleTypeElement <tt>simpleType</tt> element.
	 * @param xdefContextElement X-definition document context element.
	 * @param xdef X-definition document representation.
	 * @param schemaURLStack stack of processing schema URLs.
	 * @param schemaElements table of all schema elements.
	 * @param reporter reporter for reporting warnings and errors.
	 */
	public static void processSimpleType(Element simpleTypeElement,
		Element xdefContextElement,
		XdefDocument xdef,
		Stack<URL> schemaURLStack,
		Map<URL, Element> schemaElements,
		SReporter reporter) {
		//is schema child - declaration
		if (Utils.isSchemaChild(simpleTypeElement)) {
			//name of sipmle type
			String name = simpleTypeElement.getAttribute("name");
			//declaration of simple type
			String declaration = new SimpleType(simpleTypeElement,
				schemaURLStack.peek(), schemaElements).getTypeMethod();
			xdef.addTypeDeclaration(xdefContextElement, name, declaration);
		} else {
			//reporting warning
			if (Utils.isRedefineSchemaChild(simpleTypeElement)) {
				//Redefined simple types can not be declared because of names
				//collision.
				reporter.warning(XDEF.XDEF702);
			}
		}
	}

	/** Processes element declaration in schema and adds declaration to
	 * X-definition context element.
	 * @param elementElement schema element declaration element.
	 * @param xdefContextElement X-definition context element.
	 * @param xdef X-definition document object.
	 * @param schemaURLStack stack of processing schema URLs.
	 * @param schemaElements all schema elements.
	 * @return created and added element.
	 */
	public static Element processElement(Element elementElement,
		Element xdefContextElement,
		XdefDocument xdef,
		Stack<URL> schemaURLStack,
		Map<URL, Element> schemaElements) {
		//element representation
		XsdUtils.ElemProps element =
				new XsdUtils.ElemProps();
		//switch for marking that element is filled
		boolean elementFilled = false;
		//not a schema child - local declaration
		if (!Utils.isSchemaChild(elementElement)) {
			//occurrence string
			String occurrence = xdef.getOccurrenceExpression(
					Utils.getMinOccurrence(elementElement),
					Utils.getMaxOccurrence(elementElement));
			//setting occurrence string
			element.setOccurrence(occurrence);
			//element does have ref attribute - reference to another element
			if (elementElement.hasAttribute("ref")) {
				//getting global declaration of element
				GlobalDeclaration decl = GlobalDeclaration.getGlobalDeclaration(
					elementElement.getAttribute("ref"), Utils.ELEMENT,
					schemaURLStack.peek(), schemaElements);
				//setting element name
				element.setName(decl.getName());
				//setting element namespace
				element.setNamespace(decl.getNamespace());
				//setting ref string
				element.setRef(xdef.getRefString(schemaURLStack.peek(),
						xdefContextElement, decl));
				//setting element filled switch to true
				elementFilled = true;
			}
		} else {
			//adding to root
			xdef.addRootElement(xdefContextElement,
				Utils.getNamespace(elementElement),
				elementElement.getAttribute("name"));
		}
		//if element is not filled
		if (!elementFilled) {
			//setting element name
			element.setName(elementElement.getAttribute("name"));
			//setting element namespace
			element.setNamespace(Utils.getNamespace(elementElement));
			//setting nillable
			if ("true".equals(elementElement.getAttribute("nillable"))) {
				element.setNillable(true);
			}
			String value;
			//element has fixed value declared
			if (!"".equals(value = elementElement.getAttribute("fixed"))) {
				element.setFixed(value);
				//element has default value declared
			} else if (!"".equals(
				value = elementElement.getAttribute("default"))) {
				element.setDefault(value);
			}
			//processing type of element
			//element does have type attribute
			if (elementElement.hasAttribute("type")) {
				//type attribute
				String type = elementElement.getAttribute("type");
				//processing namespace of declaration
				//prefix of type declaration
				String prefix = KXmlUtils.getQNamePrefix(type);
				//setting type namespace
				String typeNamespace =
					KXmlUtils.getNSURI(prefix,elementElement);
				//type is a schema type
				if (Utils.NSURI_SCHEMA.equals(typeNamespace)) {
					//getting type declaration
					String typeDeclaration = Type.getType(type, elementElement,
						schemaURLStack.peek(), schemaElements).getTypeMethod();
					//setting text type declaration
					element.setText("required " + typeDeclaration);
				} else {
					GlobalDeclaration decl;
					//getting global declaration
					try {
						decl = GlobalDeclaration.getGlobalDeclaration(type,
							Utils.SIMPLE_TYPE,
							schemaURLStack.peek(),
							schemaElements);
					} catch (RuntimeException e) {
						decl = GlobalDeclaration.getGlobalDeclaration(type,
							Utils.COMPLEX_TYPE,
							schemaURLStack.peek(),
							schemaElements);

					}
					//declaration is complex type
					if (Utils.COMPLEX_TYPE.equals(decl.getType())) {
						//setting ref string
/*VT*/
						String s = xdef.getRefString(schemaURLStack.peek(),
							xdefContextElement, decl);
						if (!s.endsWith("_cType")) {
							s += "_cType";
						}
						element.setRef(s);
/*VT*/
						//declaration is simple type
					} else {
						//getting type declaration
						String typeDeclaration;
						SimpleType sType = new SimpleType(
							decl.getGlobalDeclarationElement(),
							schemaURLStack.peek(), schemaElements);
						if (!sType.isRedefined()) {
							typeDeclaration = sType.getTypeMethod();
						} else {
							typeDeclaration = sType.getName() + "()";
						}
						//setting text type declaration
						element.setText("required " + typeDeclaration);
					}
				}
				//element does not have type attribute
			} else {
				//type element
				Element typeElement;
				//element has complex type as child
				if ((KXmlUtils.firstElementChildNS(elementElement,
					Utils.NSURI_SCHEMA,
					Utils.COMPLEX_TYPE)) != null) {
					//nothing to do here
					//element has simple type as child
				} else if ((typeElement = KXmlUtils.firstElementChildNS(
					elementElement,
					Utils.NSURI_SCHEMA,
					Utils.SIMPLE_TYPE)) != null) {
					//getting type declaration
					String typeDeclaration;
					SimpleType sType = new SimpleType(typeElement,
						schemaURLStack.peek(),
						schemaElements);
					if (!sType.isRedefined()) {
						typeDeclaration = sType.getTypeMethod();
					} else {
						typeDeclaration = sType.getName();
					}
					//setting text type declaration
					element.setText("required " + typeDeclaration);
				} else {
					//setting element to any type
					element.setAnyType(true);
				}
			}
		}
		//return created and added element
		return xdef.addElement(xdefContextElement, element);
	}

	/** Processing attribute declaration and adds proper declaration to given
	 * X-definition document representation.
	 * @param attributeElement attribute declaration.
	 * @param xdefContextElement X-definition context element.
	 * @param xdef representation of X-definition document.
	 * @param refElementStack stack of referencing elements.
	 * @param schemaURLStack stack of processing schema URLs.
	 * @param schemaElements table of all schema elements.
	 * @param namespaceStack stack of namespaces during importing declarations.
	 * @param reporter reporting tool.
	 * @param proc instance of processor.
	 * @return created and added attribute node or null.
	 */
	public static Attr processAttribute(Element attributeElement,
		Element xdefContextElement,
		XdefDocument xdef,
		Stack<Element> refElementStack,
		Stack<URL> schemaURLStack,
		Map<URL, Element> schemaElements,
		Stack<String> namespaceStack,
		SReporter reporter,
		Processor proc) {
		XsdUtils.AttrProps attr = new XsdUtils.AttrProps();
		//X-definition context element is def element
		if (xdef.isXdefElement(xdefContextElement)) {
			if (Utils.isSchemaChild(attributeElement)) {
				//AttrProps declaration with name "&{0}" in schema at URL "&{1}"
				// cannot be declared in X-definition 2.0. Refference will be
				//replaced with attribute declaration.
//				reporter.warning(XDEF.XDEF701,
//					attributeElement.getAttribute("name"),
//					schemaURLStack.peek().getPath());
			}
			return null; //returning null
		}
		//is schema child - global declaration of attribute
		if (Utils.isSchemaChild(attributeElement)) {
			//referencing element stack is not empty
			if (!refElementStack.isEmpty()) {
				attr.setName(attributeElement.getAttribute("name"));
				//getting referenced element
				Element refAttributeElement = refElementStack.peek();
				//resolving default and fixed value
				String value;
				//resolving fixed value
				if (!"".equals(value = attributeElement.getAttribute("fixed"))
					|| !"".equals(
						value = refAttributeElement.getAttribute("fixed"))) {
					attr.setFixed(value);
				} else if (!"".equals(
					value = attributeElement.getAttribute("default"))
					|| !"".equals(
						value = refAttributeElement.getAttribute("default"))) {
					attr.setDefault(value);
				}
				//resolving namespace
				if (namespaceStack.isEmpty()) {
					//setting namespace to target namespace of schema
					//containing attribute declaration
					attr.setNamespace(
						Utils.getTargetNamespace(attributeElement));
				} else {
					//setting namespace from namespaces stack
					attr.setNamespace(namespaceStack.peek());
				}
				//getting occurrence string
				String occurrence = refAttributeElement.getAttribute("use");
				//resolving occurrence
				if ("required".equals(occurrence)) {
					attr.setUse("required");
				} else if ("prohibited".equals(occurrence)) {
					attr.setUse("illegal");
				} else {
					attr.setUse("optional");
				}
				//resolving type
				String type = attributeElement.getAttribute("type");
				Element sType = KXmlUtils.firstElementChildNS(attributeElement,
						Utils.NSURI_SCHEMA, Utils.SIMPLE_TYPE);
				//attribute has type attribute
				if (type != null && !"".equals(type)) {
					Type t = Type.getType(type, attributeElement,
						schemaURLStack.peek(), schemaElements);
					if (t instanceof SimpleType &&
						!((SimpleType) t).isRedefined()) {
						attr.setType(t.getName() + "()");
					} else {
						attr.setType(t.getTypeMethod());
					}
					//attribute has simple type as child
				} else if (sType != null) {
					attr.setType(new SimpleType(sType, schemaURLStack.peek(),
						schemaElements).getTypeMethod());
				}
				return xdef.addAttr(xdefContextElement, attr);
				//ref element stack is empty - global declaration of attribute
			} else {
				//AttrProps declaration with name "&{0}" in schema at URL "&{1}"
				// cannot be declared in X-definition 2.0. Refference will be
				//replaced with attribute declaration.
				reporter.warning(XDEF.XDEF701,
					attributeElement.getAttribute("name"),
					schemaURLStack.peek().getPath());
			}
		} else {
			//attribute declaration has reference to other attribute declaration
			if (attributeElement.hasAttribute("ref")) {
				//getting global declaration
				GlobalDeclaration decl = GlobalDeclaration.getGlobalDeclaration(
					attributeElement.getAttribute("ref"), Utils.ATTRIBUTE,
					schemaURLStack.peek(), schemaElements);
				//adding attribute element to ref elements stack
				refElementStack.push(attributeElement);
				//resolving schema URLs stack
				boolean sameSchema = true;
				if (!schemaURLStack.peek().equals(decl.getSchemaURL())) {
					//adding declaration namespace to the namespaces stack
					namespaceStack.push(decl.getNamespace());
					//adding declaration URL to schema URL stack
					schemaURLStack.push(decl.getSchemaURL());
					sameSchema = false;
				}
				//processing atribute declaration
				proc.processAttribute(decl.getGlobalDeclarationElement(),
						xdefContextElement);
				//resolving schema stack URL
				if (!sameSchema) {
					//removing declaration schema URL from schema URL stack
					schemaURLStack.pop();
					//removing namespace from stack
					namespaceStack.pop();
				}
				//removing attribute element from ref elements stack
				refElementStack.pop();
				//returning created attribute
				return null;
				//does not have reference
			} else {
				//setting attribute name
				attr.setName(attributeElement.getAttribute("name"));
				//resolving namespace
				String namespace;
				//is set to be qualified
				if (("qualified".equals(attributeElement.getAttribute("form"))
					|| Utils.isQualifiedByDefault(attributeElement))
					&& !"unqualified".equals(
						attributeElement.getAttribute("form"))) {
					//schema does have target namespace
					if (!"".equals(namespace = Utils.getTargetNamespace(
							attributeElement))) {
						//setting namespace to target namespace
						attr.setNamespace(namespace);
						//schema does not have target namespace
					} else {
						//namespace stack is not empty
						if (!namespaceStack.isEmpty()) {
							//setting namespace to last namespace on stack
							attr.setNamespace(namespaceStack.peek());
						}
					}
				}
				//resolving default and fixed
				String value;
				//attribute has fixed value declared
				if (!"".equals(value = attributeElement.getAttribute("fixed"))){
					attr.setFixed(value);
					//attribute has default value declared
				} else if (!"".equals(
					value = attributeElement.getAttribute("default"))) {
					attr.setDefault(value);
				}
				//resolving occurrence
				String occurrence = attributeElement.getAttribute("use");
				if ("required".equals(occurrence)) {
					attr.setUse("required");
				} else if ("prohibited".equals(occurrence)) {
					attr.setUse("illegal");
				} else {
					attr.setUse("optional");
				}
				//resolving type
				String type = attributeElement.getAttribute("type");
				Element sType = KXmlUtils.firstElementChildNS(attributeElement,
					Utils.NSURI_SCHEMA, Utils.SIMPLE_TYPE);
				//attribute has type attribute
				if (type != null && !"".equals(type)) {
					//setting attribute type
					Type t = Type.getType(type, attributeElement,
						schemaURLStack.peek(), schemaElements);
					if (t instanceof SimpleType
						&& !((SimpleType) t).isRedefined()) {
						attr.setType(t.getName() + "()");
					} else {
						attr.setType(t.getTypeMethod());
					}
					//attribute has simple type as child
				} else if (sType != null) {
					//setting attribute type
					attr.setType(new SimpleType(sType,
						schemaURLStack.peek(), schemaElements).getTypeMethod());
				}
				//returning xreated attribute
				return xdef.addAttr(xdefContextElement, attr);
			}
		}
		//returning null because none attribute was added
		return null;
	}

	/** Processes attribute group and adds proper declaration to given
	 * X-definition context node.
	 * @param attributeGroupElement attribute group declaration element.
	 * @param xdefContextElement X-definition context element.
	 * @param xdef representation of X-definition document.
	 * @param refElementStack stack of referencing elements.
	 * @param schemaURLStack stack of processing URLs.
	 * @param namespaceStack stack of namespaces during importing.
	 * @param schemaElements table of all schema elements.
	 * @param reporter  reporting tool.
	 * @param proc implementation of processor.
	 */
	public static void processAttributeGroup(Element attributeGroupElement,
			Element xdefContextElement, XdefDocument xdef,
			Stack<Element> refElementStack, Stack<URL> schemaURLStack,
			Stack<String> namespaceStack, Map<URL, Element> schemaElements,
			SReporter reporter, Processor proc) {
		//context of X-definition element is not a def element.
		if (!xdef.isXdefElement(xdefContextElement)) {
			String ref = attributeGroupElement.getAttribute("ref");
			//attribute group declaration has a reference to another attribute
			//group
			if (!"".equals(ref)) {
				//getting declaration of attribute group
				GlobalDeclaration decl = GlobalDeclaration.getGlobalDeclaration(
					ref, Utils.ATTRIBUTE_GROUP,
					schemaURLStack.peek(),
					schemaElements);
				//adding attribute group element to ref elements stack
				refElementStack.push(attributeGroupElement);
				//resolving schema URL stack
				boolean sameSchema = true;
				if (!schemaURLStack.peek().equals(decl.getSchemaURL())) {
					//adding namespace to namespace stack
					namespaceStack.push(decl.getNamespace());
					//adding declaration URL to schema URLK stack
					schemaURLStack.push(decl.getSchemaURL());
					sameSchema = false;
				}
				//processing attribute group declaration
				proc.processAttributeGroup(decl.getGlobalDeclarationElement(),
						xdefContextElement);
				if (!sameSchema) {
					//removing URL from stack
					schemaURLStack.pop();
					//removing namespace from stack
					namespaceStack.pop();
				}
				//removing ref element from stack
				refElementStack.pop();
			}
		} else {
			//AttrProps group declaration with name="&amp;{1}" in schema
			//at "&amp;{2}" cannot be converted to X-definition 2.0! reference
			//to this attribute group will replaced with attribute declarations
			//in group.
			reporter.warning(XDEF.XDEF703,
				attributeGroupElement.getAttribute("name"),
					schemaURLStack.peek().getPath());
		}
	}

	/** Processes schema group element and adds declaration to given
	 * X-definition context element.
	 * @param groupElement group element.
	 * @param xdefContextElement X-definition context element.
	 * @param xdef X-definition document object.
	 * @param schemaURL URL of processing schema.
	 * @param schemaElements all schema elements.
	 * @return created and added element or previous X-definition
	 * context element.
	 */
	public static Element processGroup(Element groupElement,
		Element xdefContextElement,
		XdefDocument xdef,
		URL schemaURL,
		Map<URL, Element> schemaElements) {
		//group declaration is not a schema chiuld and has ref attribute
		if (groupElement.hasAttribute("ref")) {
			//getting declaration
			GlobalDeclaration decl = GlobalDeclaration.getGlobalDeclaration(
				groupElement.getAttribute("ref"), Utils.GROUP, schemaURL,
				schemaElements);
			//getting object group name
			String name = decl.getName();
			//getting object group element
			Element objectGroup = KXmlUtils.firstElementChildNS(
				decl.getGlobalDeclarationElement(), Utils.NSURI_SCHEMA,
				new String[]{Utils.ALL, Utils.CHOICE, Utils.SEQUENCE});
			//getting object group type
			String groupType = getObjectGroupType(objectGroup);
			//adding element
			Element element = xdef.addXdefElement(xdefContextElement,groupType);
			//adding ref
			xdef.addRefExpression(element,
				(schemaURL.equals(decl.getSchemaURL())
					? null
					: xdef.getNameFromURL(decl.getSchemaURL())),null,name);
			//returning created element
			return element;
		} else {
			//return X-definition document context element to process children
			return xdefContextElement;
		}
	}

	/** Processing schema extension element and adds declaration to X-definition
	 * context element.
	 * @param extensionElement schema extension element.
	 * @param xdefContextElement X-definition context element.
	 * @param xdef X-definition document object.
	 * @param schemaURLStack stack of processing schema URLs.
	 * @param schemaElements all schema elements.
	 */
	public static void processExtension(Element extensionElement,
		Element xdefContextElement,
		XdefDocument xdef,
		Stack<URL> schemaURLStack,
		Map<URL, Element> schemaElements) {
		//getting base type
		String base = extensionElement.getAttribute("base");
		//getting base type namespace
		String prefix = KXmlUtils.getQNamePrefix(base);
		String typeNamespace = KXmlUtils.getNSURI(prefix, extensionElement);
		//type namespace is same as schema namespace
		if (Utils.NSURI_SCHEMA.equals(typeNamespace)) {
			//base type is schema anyType
			if ("anyType".equals(KXmlUtils.getQNameLocalpart(base))) {
				xdef.setAnyType(xdefContextElement);
				//base type is build-in schema simple type
			} else {
				//getting type declaration string
				String type;
				Type t = Type.getType(base, extensionElement,
					schemaURLStack.peek(), schemaElements);
				if (t instanceof SimpleType && !((SimpleType) t).isRedefined()) {
					type = t.getName() + "()";
				} else {
					type = t.getTypeMethod();
				}
				//adding text node containing text type declaration
				xdef.addText(xdefContextElement,
					("string()".equals(type) ? "optional " : "required ")+type);
			}
			//type namespace is other than schema namespace
		} else {
			//getting global declaration
			GlobalDeclaration decl;
			try {
				//getting simple type declaration
				decl = GlobalDeclaration.getGlobalDeclaration(
					base,
					Utils.SIMPLE_TYPE,
					schemaURLStack.peek(),
					schemaElements);
			} catch (RuntimeException ex) {
				//getting complex type declaration
				if (Utils.isRedefineSchemaChild(
					(Element)extensionElement.getParentNode().getParentNode())){
					Element redefine = DOMUtils.getElement(extensionElement,
						Utils.NSURI_SCHEMA, Utils.REDEFINE);
					decl =
						GlobalDeclaration.getGlobalDeclarationInRedefinedSchema(
							KXmlUtils.getQNameLocalpart(base),
							Utils.COMPLEX_TYPE,
							schemaURLStack.peek(),
							redefine,
							schemaElements);
				} else {
					decl = GlobalDeclaration.getGlobalDeclaration(
						base,
						Utils.COMPLEX_TYPE,
						schemaURLStack.peek(),
						schemaElements);
				}
			}
			//declaration is simple type declartation
			if (Utils.SIMPLE_TYPE.equals(decl.getType())) {
				//adding text node type declaration
				String text;
				if (decl.isRedefined()) {
					text = new SimpleType(decl.getGlobalDeclarationElement(),
						schemaURLStack.peek(), schemaElements).getTypeMethod();
				} else {
					text = decl.getName() + "()";
				}
				xdef.addText(xdefContextElement, "required " + text);
				//declaration is complex type declaration
			} else {
				//adding ref expression to xdef context element
/*VT*/
				String declName = decl.getName();
				if (!declName.endsWith("_cType")) {
					declName += "_cType";
				}
				xdef.addRefExpression(xdefContextElement,
					(decl.getSchemaURL().equals(schemaURLStack.peek())
						? "" : xdef.getXdefName(decl.getSchemaURL())),
					decl.getNamespace(), declName);
//						? "" : xdef.getNameFromURL(decl.getSchemaURL())),
//					decl.getNamespace(), decl.getName() + "_cType");
/*VT*/
			}
		}
	}

	/** Processes include import and redefine schema elements and adds
	 * declaration to X-definition context element.
	 * @param externalElement schema include, import or redefine element.
	 * @param xdefContextElement X-definition context element
	 * @param xdef X-definition document object.
	 * @param schemaURL URL of current schema.
	 */
	public static void processExternal(Element externalElement,
		Element xdefContextElement,
		XdefDocument xdef,
		URL schemaURL) {
		//schema location
		String location = externalElement.getAttribute("schemaLocation");
		try {
			//url of external schema
			URL url = Utils.getURL(schemaURL, location);
			xdef.addIncludeXdef(xdefContextElement, url);
		} catch (Exception ex) {
			throw new RuntimeException("Could not create URL!", ex);
		}
	}

	/** Processes restriction element and if it contains restrictions on simple
	 * type it adds declaration to X-definition context element.
	 * @param restrictionElement restriction element.
	 * @param xdefContextElement X-definition context element.
	 * @param xdef implementation of X-definition document.
	 * @param schemaURL current schema URL.
	 * @param schemaElements table of all schema elements.
	 */
	public static void processRestriction(Element restrictionElement,
		Element xdefContextElement,
		XdefDocument xdef,
		URL schemaURL,
		Map<URL, Element> schemaElements) {
		Element cType = DOMUtils.getElement(restrictionElement,
			Utils.NSURI_SCHEMA,
			Utils.COMPLEX_TYPE);
		Type type = Type.getType(cType, schemaURL, schemaElements);
		if (type != null) {
			String text;
			if (type.getName() == null) {
				text = type.getTypeMethod();
			} else {
				text = type.getName() + "()";
			}
			xdef.addText(xdefContextElement, "required " + text);
		}
	}
}