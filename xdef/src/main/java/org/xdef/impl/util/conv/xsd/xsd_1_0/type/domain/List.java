package org.xdef.impl.util.conv.xsd.xsd_1_0.type.domain;

import org.xdef.xml.KXmlUtils;
import org.xdef.impl.util.conv.xsd2xd.schema_1_0.Utils;
import java.net.URL;
import java.util.Map;
import org.w3c.dom.Element;

/** Represents list of XML schema data type.
 * @author Alexandrov
 */
public class List extends Specification {

	/** Type of list items. */
	private final Type _itemType;

	/** Creates instance of XML schema list construction.
	 * @param listElement list declaration element.
	 * @param schemaURL URL of schema containing list.
	 * @param schemaElements all schema elements.
	 */
	public List(Element listElement, URL schemaURL,
		Map<URL, Element> schemaElements) {
		String itemType = listElement.getAttribute("itemType");
		if (!"".equals(itemType)) {
			_itemType = Type.getType(
				itemType, listElement, schemaURL, schemaElements);
		} else {
			Element simpleTypeElement = KXmlUtils.firstElementChildNS(
				listElement, Utils.NSURI_SCHEMA, "simpleType");
			_itemType =
				new SimpleType(simpleTypeElement, schemaURL, schemaElements);
		}
	}

	/** Item type getter.
	 * @return  list item type.
	 */
	public Type getItemType() {return _itemType;}

	@Override
	public String getTypeMethod() {
		return "list(%item=" + _itemType.getTypeMethod() + ")";
	}

	@Override
	public String toString() {
		return "List [itemtype=" + _itemType.toString() + "]";
	}
}