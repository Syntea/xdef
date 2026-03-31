package org.xdef.util;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xdef.XDConstants;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonUtils;

/** Convertor of X-definition from XML format to JSON format and from JSON to XML.
 * @author trojan
 */
public class XDefToJSON {

    /** Modify string to XML text format. */
    private static String toXmlString(final String s) {return KXmlUtils.toXmlText(s, '<', false);}

    /** Modify string to JSON format. */
    private static String toJsonString(final String s) {return SUtils.modifyString(s, "\"", "\\\"");}

    /** Remove trailing spaces, */
    private static String removeTrailingSpaces(final String s) {
        String t = s;
        while (t.endsWith(" ") || t.endsWith("\t")) {
            t = t.substring(0, t.length() - 1);
        }
        return t;
    }

    /** Get namespace URI of X-definition or return null.
     * @param el Element with namespace.
     * @return accepted namespace URI of X-definition or return null.
     */
    private static String getXdefNamespace(final Element el) {
        String nsURI = el.getNamespaceURI();
        return XDConstants.XDEF31_NS_URI.equals(nsURI) || XDConstants.XDEF32_NS_URI.equals(nsURI)
            || XDConstants.XDEF40_NS_URI.equals(nsURI) || XDConstants.XDEF41_NS_URI.equals(nsURI)
            || XDConstants.XDEF42_NS_URI.equals(nsURI) ? nsURI : null;
    }

    /** Create JSON named item from attribute. */
    private static String attrToJSON(final Node n) {
        return "\"" + n.getNodeName() + "\": \"" + toJsonString(n.getNodeValue()) + "\"";
    }

    /** Get first child element from the child nodes of element el. */
    private static Node getFirstChildElement(final Element el) {
        Node n = el.getFirstChild();
        while(n != null && n.getNodeType() != Node.ELEMENT_NODE) {
            n = n.getNextSibling();
        }
        return n;
    }

    /** Get next element as sibling of node n. */
    private static Node getNextChildElement(final Node n) {
        Node x = n;
        while((x = x.getNextSibling()) != null && x.getNodeType() != Node.ELEMENT_NODE) {}
        return x;
    }

    /** Create X-definition attribuge from given JSON map.
     * @param map the map with named items.
     * @param xdPrefix prexix of X-definition namespace.
     * @param name local name of X-definition attribuge.
     * @return string with attribute declaration or the empty string.
     */
    private static String createXDeNamedvalue(final Map<String, Object> map, final String xdPrefix, final String name) {
        Object o = map.get(xdPrefix + ":" + name);
        if (o == null) {
            o = map.get(name);
        }
        if (o != null && !((String) o).isEmpty()) {
            return " " + xdPrefix + ":" + name + "='" + o + "'";
        }
        return "";
    }

    /** Get given object as XML text. */
    private static String getAsXMLText(final Object o) {
        String s = o.toString();
        while (!s.isEmpty() && s.charAt(s.length() - 1) == ' ') {
            s = s.substring(0, s.length() - 1);
        }
        if (!s.endsWith("\n")) {
            s += '\n';
        }
        return toXmlString(s);
    }

    /** Add X-definition attribute to the generated JSON source code.
     * @param sb StringBuilder where to add created value.
     * @param el element with X-definition.
     * @param name local name of the X-definition attribute.
     */
    private static void addXDAttrToJSON(final StringBuilder sb, final Element el, final String name) {
        Attr attr = el.getAttributeNodeNS(el.getNamespaceURI(), name);
        if (attr == null) {
            attr = el.getAttributeNode(name);
        }
        if (attr != null) {
            sb.append(attrToJSON(attr)).append(", ");
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Convert JSON format of xd:def to XML.
     * @param json input JSON data.
     * @return string with XML format.
     */
    @SuppressWarnings("unchecked")
    private static String xdefToXml(final List xd, final String xdName) {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> map = (Map) xd.get(0);
        String xdPrefix = xdName.substring(0, xdName.length() - 4);
        String xdNamespace = (String) map.get(xdName);
        sb.append("<").append(xdPrefix).append(":def");
        sb.append(" xmlns:").append(xdPrefix).append("='").append(xdNamespace).append("'");
        if (xdNamespace == null || xdNamespace.isEmpty()) {
            throw new RuntimeException("Incorrect X-definition namespace: \"" + map + "\"");
        }
        for (String key: map.keySet()) {
            if (key.startsWith(xdPrefix + ":")) {
                if (key.endsWith(":def")) {
                    continue;
                }
            } else if (key.equals("def")) {
                continue;
            }
            String s = toXmlString(map.get(key).toString());
            sb.append(sb.length() + s.length() > 110 ? "\n  " : " ").append(key).append("='");
            sb.append(toXmlString(s));
            sb.append("'");
        }
        sb.append(">\n");
        for (int i = 1; i < xd.size(); i++) {
            Object o = xd.get(i);
            if (!(o instanceof Map)) {
                throw new RuntimeException("Unexpected object " + i);
            }
            map = (Map<String, Object>) o;
            if ((o = map.get(xdPrefix + ":declaration")) != null) { // declaration
                sb.append("<").append(xdPrefix).append(":declaration");
                sb.append(createXDeNamedvalue(map, xdPrefix, "scope"));
                sb.append(">");
                sb.append(toXmlString(o.toString())).append("</").append(xdPrefix).append(":declaration>\n");
            } else if ((o = map.get(xdPrefix + ":component")) != null) { // component
                sb.append("\n<").append(xdPrefix).append(":component>");
                sb.append(getAsXMLText(o));
                sb.append("</").append(xdPrefix).append(":component>\n");
            } else if ((o = map.get(xdPrefix + ":BNFGrammar")) != null) { // component
                sb.append("\n<").append(xdPrefix).append(":BNFGrammar");
                sb.append(createXDeNamedvalue(map, xdPrefix, "name"));
                sb.append(createXDeNamedvalue(map, xdPrefix, "scope"));
                sb.append(createXDeNamedvalue(map, xdPrefix, "extends"));
                sb.append(">");
                sb.append(getAsXMLText(o));
                sb.append("</").append(xdPrefix).append(":BNFGrammar>\n");
            } else if ((o = map.get(xdPrefix + ":xml")) != null) { // XML model
                sb.append(o.toString());
            } else if (map.size() == 1) { // JSON model
                String name = map.keySet().iterator().next();
                sb.append("\n<").append(xdPrefix).append(":json name='").append(name).append("'>\n");
                sb.append(toXmlString(XonUtils.toJsonString(map.get(name), true)));
                sb.append("\n</").append(xdPrefix).append(":json>\n");
            } else { // declaration
                throw new RuntimeException("Unexpected object: " + o);
            }
        }
        sb.append("</").append(xdPrefix).append(":def>\n");
        return sb.toString();
    }

    /** Convert JSON format of xd:declaration to XML.
     * @param json input JSON data.
     * @return string with XML format.
     */
    @SuppressWarnings("unchecked")
    private static String textItemToXml(final List xd, final String xdName) {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> map = (Map) xd.get(0);
        String xdPrefix = xdName.substring(0, xdName.indexOf(':'));
        String xdNamespace = (String) map.get(xdName);
        if (xdNamespace == null || xdNamespace.isEmpty()) {
            throw new RuntimeException("Incorrect X-definition namespace: \"" + map + "\"");
        }
        sb.append("<").append(xdName);
        sb.append(" xmlns:").append(xdPrefix).append("='").append(xdNamespace).append("'");
        for (Entry o: map.entrySet()) {
            if (!xdName.equals(o.getKey())) {
                sb.append(" ").append(o.getKey().toString()).append("=\"");
                sb.append(toXmlString(o.getValue().toString())).append("\"");
            }
        }
        sb.append(">").append(toXmlString(xd.get(1).toString())).append("</").append(xdName).append(">\n");
        return sb.toString();
    }

    /** Convert JSON format of xd:collection to XML.
     * @param json input JSON data.
     * @return string with XML format.
     */
    @SuppressWarnings("unchecked")
    private static String collectionToXml(final List xd, final String xdName) {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> map = (Map) xd.get(0);
        String xdPrefix = xdName.substring(0, xdName.indexOf(':'));
        String xdNamespace = (String) map.get(xdName);
        if (xdNamespace == null || xdNamespace.isEmpty()) {
            throw new RuntimeException("Incorrect X-definition namespace: \"" + map + "\"");
        }
        sb.append("<").append(xdName);
        sb.append(" xmlns:").append(xdPrefix).append("='").append(toXmlString(xdNamespace)).append("'");
        for (String key : map.keySet()) {
            if (!key.startsWith(xdPrefix, 0) || !key.endsWith(":collection")) {
                sb.append(" ").append(key).append("='").append(toXmlString(map.get(key).toString())).append("'");
            }
        }
        if (xd.size() == 1) {
            return sb.append("/>\n").toString();
        }
        sb.append(">\n");
        for (int i = 1; i < xd.size(); i++) {
            List xd1 = (List) xd.get(i);
            map = (Map) xd1.get(0);
            for (Object x : map.keySet()) {
                String key = (String) x;
                if (key.endsWith(":def")) {
                   sb.append(xdefToXml(xd1, key)); // xd:def
                   break;
                } else if (key.endsWith(":declaration") || key.endsWith(":component") || key.endsWith(":BNFGrammar")
                    || key.endsWith(":lexicon")) {
                    sb.append(textItemToXml(xd1, key));
                   break;
                } else if (key.endsWith(":collection")) {
                    throw new RuntimeException("Collection in collection");
                }
            }
        }
        sb.append("\n</").append(xdName).append(">");
        return sb.toString();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Convert X-definition XML to JSON.
     * @param elem Element with X-definition.
     * @return string with JSON format.
     */
    private static String xdefToJson(final Element elem, String nsUri) {
        String xdPrefix = elem.getPrefix();
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        sb.append("{\"").append(xdPrefix).append(":def\": \"").append(toJsonString(nsUri)).append("\"");
        NamedNodeMap nnm = elem.getAttributes();
        int len = sb.length();
        for (int i = 0; i < nnm.getLength(); i++) {
            Node n = nnm.item(i);
            if (!n.getNodeName().equals("xmlns:" + xdPrefix)) {
                sb.append(",");
                String s = attrToJSON(n);
                if ((len += s.length() + 2) > 110) {
                    sb.append("\n ");
                    len = 2;
                }
                sb.append(" ").append(s);
            }
        }
        sb.append("}");
        Node n = getFirstChildElement(elem);
        if (n == null) {
            return sb.append("\n]").toString();
        }
        sb.append(",\n");
        while (n != null) {
            Element el = (Element) n;
            if (nsUri.equals(el.getNamespaceURI())) {
                switch (el.getLocalName()) {
                    case "declaration":
                        sb.append("{ ");
                        addXDAttrToJSON(sb, el, "scope");
                        sb.append("\"").append(el.getTagName()).append("\": \"");
                        sb.append(toJsonString(removeTrailingSpaces(el.getTextContent())));
                        sb.append("\"}");
                        sb.append((n = getNextChildElement(el)) != null ? ",\n" : "\n");
                        continue;
                    case "json":
                        n = el.getAttributeNodeNS(nsUri, "name");
                        if (n== null) {
                            n = el.getAttributeNode("name");
                        }
                        if (n != null) {
                            sb.append("{ \"").append(n.getNodeValue()).append("\":");
                            sb.append(removeTrailingSpaces(el.getTextContent()));
                            sb.append("}");
                            sb.append((n = getNextChildElement(el)) != null ? ",\n" : "\n");
                        } else {
                            throw new RuntimeException("Expected name of json model");
                        }
                        continue;
                    case "component":
                        sb.append("{ \"").append(el.getTagName()).append("\": \"");
                        sb.append(toJsonString(removeTrailingSpaces(el.getTextContent()))).append("\"}");
                        sb.append((n = getNextChildElement(el)) != null ? ",\n" : "\n");
                        continue;
                    case "BNFGrammar":
                        sb.append("{ ");
                        addXDAttrToJSON(sb, el, "name");
                        addXDAttrToJSON(sb, el, "scope");
                        addXDAttrToJSON(sb, el, "extends");
                        sb.append(" \"").append(el.getTagName()).append("\": \"");
                        sb.append(toJsonString(removeTrailingSpaces(el.getTextContent()))).append("\"}");
                        sb.append((n = getNextChildElement(el)) != null ? ",\n" : "\n");
                        continue;
                }
            }
            //XML model
            sb.append("{ \"").append(xdPrefix).append(":xml\": \"\n");
            String s = KXmlUtils.nodeToString(el, true);
            String t = " xmlns:" + xdPrefix + "=\"" + nsUri + "\"";
            int ndx = s.indexOf(t);
            if (ndx > 0) { // remove attribute xmlnd with X-definition namespac
                s = s.substring(0, ndx) + s.substring(ndx + t.length());
            }
            sb.append(toJsonString(s)).append("\n\"}");
            sb.append((n = getNextChildElement(el)) != null ? ",\n" : "\n");
        }
        sb.append("]");
        return sb.toString();
    }

    /** Convert X-declaration XML to JSON.
     * @param elem Element with X-definition.
     * @return string with JSON format.
     */
    private static String textItemToJson(final Element elem, final String nsURI) {
        StringBuilder sb = new StringBuilder();
        sb.append("[ {");
        NamedNodeMap nnm = elem.getAttributes();
        String sep = " ";
        for (int i = 0; i < nnm.getLength(); i++) {
            Node n = nnm.item(i);
            if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
                String name = n.getNodeName();
                if (name.startsWith("xmlns:")) {
                    continue;
                }
                sb.append(sep);
                sb.append("\"").append(name).append("\": \"").append(toJsonString(n.getNodeValue())).append("\"");
                sep = ", ";
            }
        }
        sb.append(sep);
        sb.append("\"").append(elem.getNodeName()).append("\": \"");
        sb.append(toJsonString(nsURI)).append("\"},\n\"");
        sb.append(SUtils.modifyString(elem.getTextContent(), "\"", "\\\"")).append("\"]");
        return sb.toString();
    }


    /** Convert X-declaration XML to JSON.
     * @param elem Element with X-definition.
     * @return string with JSON format.
     */
    private static String collectionToJson(final Element elem, final String nsURI) {
        String xdPrefix = elem.getPrefix();
        StringBuilder sb = new StringBuilder();
        sb.append("[ { \"").append(xdPrefix).append(":collection\": \"").append(toJsonString(nsURI)).append("\"");
        NamedNodeMap nnm = elem.getAttributes();
        for (int i = 0; i < nnm.getLength(); i++) {
            Node n = (Node) nnm.item(i);
            String name = n.getNodeName();
            if (!name.equals("xmlns:" + xdPrefix)) {
                sb.append(", \"").append(name).append("\": \"").append(n.getNodeValue()).append("\"");
            }
        }
        sb.append("}");
        Node n = getFirstChildElement(elem);
        while (n != null) {
            Element el = (Element) n;
            sb.append(",\n");
            String localName = el.getLocalName();
            String ns = getXdefNamespace(el);
            if (ns != null) {
                switch(localName) {
                    case "def": sb.append("\n").append(xdefToJson(el, ns)); break;// xd:def
                    case "declaration":
                    case "component":
                    case "BNFGrammar":
                    case "lexicon": sb.append("\n").append(textItemToJson(el, ns)); break;
                    case "collection": throw new RuntimeException("Collection in collection");
                    default: new RuntimeException("Incorrect element in collectio: " + el.getNodeName());
                }
            } else {
                throw new RuntimeException("Incorrect element in collectio: " + el.getNodeName());
            }
            n = getNextChildElement(n);
        }
        sb.append("\n]");
        return sb.toString();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Convert JSON format of X-definition source data to XML.
     * @param json input JSON data.
     * @return string with XML format.
     */
    public static String jsonXdefToXml(final String json) {
        List items = (List) XonUtils.parseXON(json);
        Object o = items.get(0);
        if (!(o instanceof Map)) {
            throw new RuntimeException("Unexpected root object");
        }
        Map item = (Map) o;
        for (Object x : item.keySet()) {
            String key = (String) x;
            if (key.endsWith(":def")) {
                return xdefToXml(items, key); // xd:def
            } else if (key.endsWith(":declaration") || key.endsWith(":component") || key.endsWith(":BNFGrammar")
                || key.endsWith(":lexicon")) {
                return textItemToXml(items, key);
            } else if (key.endsWith(":collection")) {
                return collectionToXml(items, key);
            }
        }
        throw new RuntimeException("Unexpected root object: " + item);
    }

    /** Convert XML format of X-definition source data to JSON.
     * @param xml input XML data.
     * @return string with JSON format.
     */
    public static String xmlXdefToJson(final String xml) {
        Element el = KXmlUtils.parseXml(xml).getDocumentElement();
        String nsURI = getXdefNamespace(el);
        if (nsURI != null) {
            switch (el.getLocalName()) {
                case "def": return xdefToJson(el, nsURI); // xd:def
                case "declaration":
                case "component":
                case "BNFBNFGrammar":
                case "lexicon": return textItemToJson(el, nsURI); // xd:declaration
                case "collection": return collectionToJson(el, nsURI); // collection
            }
        }
        throw new RuntimeException("Expected X-definition root element. Found: " + el.getNodeName());
    }

    /** Run XML schema (XSD) generator from command line.
     * @param args array of string with command line arguments:
     * <ul>
     * <li>-h or --help: display help information.</li>
     * <li>-i or --input: pathname of the file with input data.</li>
     * <li>-o or --output: pathname of output file with converted data.</li>
     * </ul>
     * @throws Exception if an error occurs.
     */
    public static void main(String... args) throws Exception {
        final String info =
"XDefToJSON - convertor of X-definition XML -> JSON or JSON -> XML.\n" +
"Parameters:\n" +
" -i or --input:  pathname of the file with input data (XML or JSON)\n" +
" -o or --outDir: pathname of output file with converted data\n" +
" -h or --help or /?: display help information";
        File input = null; // input file
        File output = null; // output file
        if (args == null || args.length < 1) {
            throw new RuntimeException("Error: parameters missing.\n" + info);
        }
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-h":
                case "/?":
                case "--help": System.out.println(info); return;
                case "-o":
                case "--output":
                    if (output != null) {
                        throw new RuntimeException("Redefinition of " + arg + ".\n" + info);
                    }
                    output = new File(args[++i]);
                    if (output.exists() && output.isDirectory()) {
                        throw new RuntimeException("output is directory: " + args[i] + ".\n" + info);
                    }
                    continue;
                case "-i":
                case "--input":
                    if (input != null) {
                        throw new RuntimeException("Redefinition of " + arg + ".\n" + info);
                    }
                    input = new File(args[++i]);
                    if (!input.exists() || input.isDirectory()) {
                        throw new RuntimeException("input not exists or it is a directory: " + args[i] + ".\n" + info);
                    }
                    continue;
                default: throw new RuntimeException("Switch error " + arg + ".\n" + info);
            }
        }
        if (output == null) {
            throw new RuntimeException("Missing output file.\n" + info);
        }
        if (input == null) {
            throw new RuntimeException("Missing input file.\n" + info);
        }
        String s = SUtils.readString(input, "UTF-8");
        s = (s.trim().startsWith("<")) ? xmlXdefToJson(s) : jsonXdefToXml(s);
        SUtils.writeString(output, s, "UTF-8");
    }
}
