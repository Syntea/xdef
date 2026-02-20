package org.xdef.util.xsd2xd.xsd_1_0.type;

import org.xdef.xml.KDOMUtils;
import org.xdef.util.xsd2xd.Utils;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.w3c.dom.*;

/** Represents XML schema construction - union of data types.
 * @author Ilia Alexandrov
 */
public class XSUnion extends Specification {

    /** Set of union types. */
    private final Set<Type> _itemTypes = new HashSet<Type>();

    /** Creates instance of union construction.
     * @param unionElement union declaration element.
     * @param schemaURL URL of schema containing union.
     * @param schemaElements all schema elements.
     */
    public XSUnion(Element unionElement, URL schemaURL,
        Map<URL, Element> schemaElements) {
        StringTokenizer st =
            new StringTokenizer(unionElement.getAttribute("memberTypes"));
        while (st.hasMoreTokens()) {
            String memberType = st.nextToken();
            if (!"".equals(memberType)) {
                _itemTypes.add(Type.getType(memberType,
                    unionElement, schemaURL, schemaElements));
            }
        }
        NodeList localSimpleTypes = KDOMUtils.getChildElementsNS(unionElement,
                Utils.NSURI_SCHEMA, Utils.SIMPLE_TYPE);
        for (int i = 0; i < localSimpleTypes.getLength(); i++) {
            _itemTypes.add(new SimpleType((Element) localSimpleTypes.item(i),
                    schemaURL, schemaElements));
        }
    }

    /** Union item types getter.
     * @return  set of union data types.
     */
    public Set<Type> getItemTypes() {return _itemTypes;}

    @Override
    public String getTypeMethod() {
        String ret = "";
        for (Type type : _itemTypes) {
            ret += ("".equals(ret) ? "" : " | ") + type.getTypeMethod();
        }
        return ret;
    }

    @Override
    public String toString() {
        String ret = "Union [";
        int counter = 0;
        for (Type type : _itemTypes) {
            ret += (counter == 0 ? "type=" + type.toString() : ", type="
                + type.toString());
            counter++;
        }
        return ret;
    }
}