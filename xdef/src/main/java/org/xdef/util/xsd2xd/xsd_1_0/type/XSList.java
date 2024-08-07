package org.xdef.util.xsd2xd.xsd_1_0.type;

import org.xdef.xml.KDOMUtils;
import org.xdef.util.xsd2xd.Utils;
import java.net.URL;
import java.util.Map;
import org.w3c.dom.*;

/** Represents list of XML schema data type.
 * @author Ilia Alexandrov
 */
public class XSList extends Specification {

	/** Type of list items. */
	private final Type _itemType;

	/** Creates instance of XML schema list construction.
	 * @param listElement list declaration element.
	 * @param schemaURL URL of schema containing list.
	 * @param schemaElements all schema elements.
	 */
	public XSList(Element listElement, URL schemaURL,
		Map<URL, Element> schemaElements) {
		String itemType = listElement.getAttribute("itemType");
		if (!"".equals(itemType)) {
			_itemType = Type.getType(
				itemType, listElement, schemaURL, schemaElements);
		} else {
			Element simpleTypeElement = KDOMUtils.firstElementChildNS(
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