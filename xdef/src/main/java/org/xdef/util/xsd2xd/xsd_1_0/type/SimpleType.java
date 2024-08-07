package org.xdef.util.xsd2xd.xsd_1_0.type;

import org.xdef.xml.KDOMUtils;
import org.xdef.util.xsd2xd.Utils;
import java.net.URL;
import java.util.Map;
import org.w3c.dom.*;

/** Represents XML schema user-defined simple type.
 * @author Ilia Alexandrov
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
		NodeList children = KDOMUtils.getChildElementsNS(simpleTypeElement,
			Utils.NSURI_SCHEMA, new String[]{"restriction", "union", "list"});
		Element specification = (Element) children.item(0);
		String specificationName = specification.getLocalName();
		switch (specificationName) {
			case "restriction":
				_specification =
					new Restriction(specification, schemaURL, schemaElements);
				break;
			case "union":
				_specification =
					new XSUnion(specification, schemaURL, schemaElements);
				break;
			case "list":
				_specification = new XSList(specification, schemaURL, schemaElements);
				break;
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