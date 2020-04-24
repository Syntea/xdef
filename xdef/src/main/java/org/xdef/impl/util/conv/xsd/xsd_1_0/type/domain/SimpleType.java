package org.xdef.impl.util.conv.xsd.xsd_1_0.type.domain;

import org.xdef.xml.KXmlUtils;
import org.xdef.impl.util.conv.xsd2xd.schema_1_0.Utils;
import java.net.URL;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Represents XML schema user-defined simple type.
 * @author Alexandrov
 */
public class SimpleType extends Type {

	/** Specification of current simple type. */
	private Specification _specification;
	/** Is redefined. */
	private boolean _redefined = false;

	/** Creates instance of simple type.
	 * @param simpleTypeElement simple type element.
	 * @param schemaURL url of current schema.
	 * @param schemaElements table of all schema elements.
	 */
	public SimpleType(Element simpleTypeElement, URL schemaURL,
		Map<URL, Element> schemaElements) {
		String name = simpleTypeElement.getAttribute("name");
		if (!"".equals(name)) {
			_name = name;
		}
		NodeList children = KXmlUtils.getChildElementsNS(simpleTypeElement,
			Utils.NSURI_SCHEMA, new String[]{"restriction", "union", "list"});
		Element specification = (Element) children.item(0);
		String specificationName = specification.getLocalName();
		if ("restriction".equals(specificationName)) {
			_specification =
				new Restriction(specification, schemaURL, schemaElements);
		} else if ("union".equals(specificationName)) {
			_specification =
				new Union(specification, schemaURL, schemaElements);
		} else if ("list".equals(specificationName)) {
			_specification = new List(specification, schemaURL, schemaElements);
		}
	}

	/** Creates instance of simple type.
	 * @param simpleTypeElement simple type element.
	 * @param schemaURL url of current schema.
	 * @param schemaElements table of all schema elements.
	 * @param redefined redefined property.
	 */
	public SimpleType(Element simpleTypeElement, URL schemaURL,
		Map<URL, Element> schemaElements, boolean redefined) {
		this(simpleTypeElement, schemaURL, schemaElements);
		_redefined = redefined;
	}

	/** Creates instance of simple type with given restriction.
	 * @param restriction restriction.
	 */
	public SimpleType(Restriction restriction) {_specification = restriction;}

	/** Getter for specification of simple type.
	 * @return  specification of simple type.
	 */
	public Specification getSpecification() {return _specification;}

	@Override
	public String getTypeMethod() {	return _specification.getTypeMethod();}

	@Override
	public String toString() {
		return "SimpleType [" + _specification.toString() + "]";
	}

	/** Returns redefined property.
	 * @return redefined property.
	 */
	public boolean isRedefined() {return _redefined;}
}