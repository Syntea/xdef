package org.xdef.util;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonUtils;

/** Convertor of X-definition from XML format to JSON format and from JSON to XML.
 * @author trojan
 */
public class XDefToJSON {

    /** Modify string to XML text format. */
    private static String toXmlString(final String s) {
        return KXmlUtils.toXmlText(s, '<', false);
    }

    /** Modify string to JSON format. */
    private static String toJsonString(final String s) {
        return SUtils.modifyString(s, "\"", "\\\"");
    }

    /** Remove trailing spaces, */
    private static String removeTrailingSpaces(final String s) {
        String t = s;
        while (t.endsWith(" ") || t.endsWith("\t")) {
            t = t.substring(0, t.length() - 1);
        }
        return t;
    }

    /** Convert JSON format of xd:def to XML.
     * @param json input JSON data.
     * @return string with XML format.
     */
    @SuppressWarnings("unchecked")
    private static String xdefToXml(final List xd, final String xdName) {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> item = (Map) xd.get(0);
        String xdPrefix = xdName.substring(0, xdName.length() - 4);
        String xdNamespace = (String) item.get(xdName);
        sb.append("<").append(xdPrefix).append(":def");
        sb.append(" xmlns:").append(xdPrefix).append("='").append(xdNamespace).append("'");
        if (xdNamespace == null || xdNamespace.isEmpty()) {
            throw new RuntimeException("Incorrect X-definition namespace: \"" + item + "\"");
        }
        Object o = item.get(xdPrefix + ":name");
        if (o == null) {
            o = item.get("name");
        }
        if (o != null && !((String) o).isEmpty()) {
            sb.append(" xd:name='").append(o).append("'");
        }
        o = item.get(xdPrefix + ":root");
        if (o == null) {
            o = item.get("root");
        }
        if (o != null && !((String) o).isEmpty()) {
            sb.append(" xd:root='").append(o).append("'");
        }
        for (String key: item.keySet()) {
            if (key.startsWith("impl-")) {
                sb.append("\n  ").append(key).append("='");
                sb.append(toXmlString(item.get(key).toString()));
                sb.append("'");
            }
        }
        sb.append(">\n");
        for (int i = 1; i < xd.size(); i++) {
            o = xd.get(i);
            if (!(o instanceof Map)) {
                throw new RuntimeException("Unexpected object " + i);
            }
            String s;
            item = (Map<String, Object>) o;
            if ((o = item.get(xdPrefix + ":declaration")) != null) { // declaration
                sb.append("<").append(xdPrefix).append(":declaration");
                s = toXmlString(o.toString());
                o = item.get(xdPrefix + ":scope");
                if (o == null) {
                    o = item.get("scope");
                }
                if (o != null) {
                    sb.append(" scope='").append(o.toString()).append("'");
                }
                sb.append(">");
                sb.append(s).append("</").append(xdPrefix).append(":declaration>\n");
            } else if ((o = item.get(xdPrefix + ":component")) != null) { // component
                sb.append("\n<").append(xdPrefix).append(":component>");
                s = o.toString();
                while (!s.isEmpty() && s.charAt(s.length() - 1) == ' ') {
                    s = s.substring(0, s.length() - 1);
                }
                s = toXmlString(s);
                if (!s.endsWith("\n")) {
                    s += '\n';
                }
                sb.append(s);
                sb.append("</").append(xdPrefix).append(":component>\n");
            } else if ((o = item.get(xdPrefix + ":BNFGrammar")) != null) { // component
                sb.append("\n<").append(xdPrefix).append(":BNFGrammar");
                Object x = item.get("name");
                if (x == null) {
                    x = (String) item.get(xdPrefix + ":name");
                }
                if (x == null) {
                    throw new RuntimeException("BNFGrammar name is missing");
                }
                sb.append(" ").append(xdPrefix).append(":name=\"").append(x).append("\"");
                x = item.get("scope");
                if (x == null) {
                    x = (String) item.get(xdPrefix + ":scope");
                }
                if (x != null) {
                    sb.append(" ").append(xdPrefix).append(":scope=\"").append(x).append("\"");
                }
                x = item.get("extends");
                if (x == null) {
                    x = (String) item.get(xdPrefix + ":extends");
                }
                if (x != null) {
                    sb.append(" ").append(xdPrefix).append(":extends=\"").append(x).append("\"");
                }
                sb.append(">");
                s = o.toString();
                while (!s.isEmpty() && s.charAt(s.length() - 1) == ' ') {
                    s = s.substring(0, s.length() - 1);
                }
                s = toXmlString(s);
                if (!s.endsWith("\n")) {
                    s += '\n';
                }
                sb.append(s);
                sb.append("</").append(xdPrefix).append(":BNFGrammar>\n");
            } else if (item.size() == 1) { // JSON model
                String name = item.keySet().iterator().next();
                sb.append("\n<").append(xdPrefix).append(":json name='").append(name).append("'>\n");
                sb.append(toXmlString(XonUtils.toJsonString(item.get(name), true)));
                sb.append("\n</").append(xdPrefix).append(":json>\n");
            } else { // declaration
                throw new RuntimeException("Unexpected object: " + o);
            }
        }
        sb.append("</").append(xdPrefix).append(":def>\n");
        return sb.toString();
    }

    /** Convert X-definition XML to JSON.
     * @param elem Element with X-definition.
     * @return string with JSON format.
     */
    private static String xdefToJson(final Element elem) {
        String xdPrefix = elem.getPrefix();
        String xdNamespace = elem.getNamespaceURI();
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        sb.append("{\"").append(xdPrefix).append(":def\": \"").append(toJsonString(xdNamespace)).append("\"");
        Attr attr = elem.getAttributeNodeNS(xdNamespace, "name");
        if (attr == null) {
            attr = elem.getAttributeNode("name");
        }
        if (attr != null) {
            sb.append(", \"").append(xdPrefix).append(":name\": \"").append(attr.getNodeValue()).append("\"");
        }
        attr = elem.getAttributeNodeNS(xdNamespace, "root");
        if (attr == null) {
            attr = elem.getAttributeNode("root");
        }
        if (attr != null) {
            sb.append(", \"xd:root\": \"").append(attr.getValue()).append("\"");
        }
        NamedNodeMap nnm = elem.getAttributes();
        for (int i = 0; i < nnm.getLength(); i++) {
            Node n = nnm.item(i);
            if (n.getLocalName().startsWith("impl-")) {
                sb.append(",\n  \"").append(n.getLocalName()).append("\": \"").append(n.getNodeValue()).append("\"");
            }
        }
        sb.append("}");
        NodeList nl = elem.getElementsByTagName("*");
        if (nl.getLength() == 0) {
            return sb.append("\n]").toString();
        }
        sb.append(",\n");
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if (xdNamespace.equals(el.getNamespaceURI())) {
                    if (null == el.getLocalName()) {
                        throw new RuntimeException("Expected namexpace: " + el.getNodeName() + ", " + i);
                    } else {
                        switch (el.getLocalName()) {
                            case "declaration":
                                sb.append("{");
                                attr = el.getAttributeNodeNS(xdNamespace, "scope");
                                if (attr == null) {
                                    attr = el.getAttributeNode("scope");
                                }
                                if (attr != null && !"local".equals(attr.getValue())) {
                                    sb.append("\"").append(xdPrefix).append(":scope\": \"");
                                    sb.append(attr.getValue()).append("\", ");
                                }
                                sb.append("\"").append(el.getTagName()).append("\": \"");
                                sb.append(toJsonString(removeTrailingSpaces(el.getTextContent())));
                                sb.append("\"}");
                                sb.append(i < nl.getLength() - 1 ? ",\n" : "\n");
                                break;
                            case "json":
                                attr = el.getAttributeNodeNS(xdNamespace, "name");
                                if (attr == null) {
                                    attr = el.getAttributeNode("name");
                                }
                                if (attr != null) {
                                    sb.append("{ \"").append(attr.getValue()).append("\":");
                                    sb.append(removeTrailingSpaces(el.getTextContent()));
                                    sb.append("}");
                                    sb.append(i < nl.getLength() - 1 ? ",\n" : "\n");
                                } else {
                                    throw new RuntimeException("Expected name of json model at " + i);
                                }
                                break;
                            case "component":
                                 sb.append("{ \"").append(el.getTagName()).append("\": \"");
                                 sb.append(toJsonString(removeTrailingSpaces(el.getTextContent()))).append("\"}");
                                 sb.append(i < nl.getLength() - 1 ? ",\n" : "\n");
                                break;
                            case "BNFGrammar":
                                sb.append("{");
                                 attr = el.getAttributeNodeNS(xdNamespace, "name");
                                if (attr == null) {
                                    attr = el.getAttributeNode("name");
                                }
                                if (attr != null) {
                                    sb.append(" \"").append("name").append("\": \"");
                                    sb.append(attr.getValue()).append("\",");
                                }
                                attr = el.getAttributeNodeNS(xdNamespace, "scope");
                                if (attr == null) {
                                    attr = el.getAttributeNode("scope");
                                }
                                if (attr != null) {
                                    sb.append(" \"").append("scope").append("\": \"");
                                    sb.append(attr.getValue()).append("\",");
                                }
                                attr = el.getAttributeNodeNS(xdNamespace, "extends");
                                if (attr == null) {
                                   attr = el.getAttributeNode("extends");
                                }
                                if (attr != null) {
                                    sb.append(" \"").append("extends").append("\": \"");
                                    sb.append(attr.getValue()).append("\",");
                                }
                                sb.append(" \"").append(el.getTagName()).append("\": \"");
                                sb.append(toJsonString(removeTrailingSpaces(el.getTextContent()))).append("\"}");
                                sb.append(i < nl.getLength() - 1 ? ",\n" : "\n");
                                break;
                            default: throw new RuntimeException("Expected item: " + el.getNodeName() + ", " + i);
                        }
                    }
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /** Convert JSON format of xd:declaration to XML.
     * @param json input JSON data.
     * @return string with XML format.
     */
    @SuppressWarnings("unchecked")
    private static String textItemToXml(final List xd, final String xdName) {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> item = (Map) xd.get(0);
        String xdPrefix = xdName.substring(0, xdName.indexOf(':'));
        String xdNamespace = (String) item.get(xdName);
        if (xdNamespace == null || xdNamespace.isEmpty()) {
            throw new RuntimeException("Incorrect X-definition namespace: \"" + item + "\"");
        }
        sb.append("<").append(xdName);
        sb.append(" xmlns:").append(xdPrefix).append("='").append(xdNamespace).append("'>");
        sb.append(xd.get(1).toString()).append("</").append(xdName).append(">");
        return sb.toString();
    }

    /** Convert JSON format of xd:collection to XML.
     * @param json input JSON data.
     * @return string with XML format.
     */
    @SuppressWarnings("unchecked")
    private static String collectionToXml(final List xd, final String xdName) {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> item = (Map) xd.get(0);
        String xdPrefix = xdName.substring(0, xdName.indexOf(':'));
        String xdNamespace = (String) item.get(xdName);
        if (xdNamespace == null || xdNamespace.isEmpty()) {
            throw new RuntimeException("Incorrect X-definition namespace: \"" + item + "\"");
        }
        sb.append("<").append(xdName);
        sb.append(" xmlns:").append(xdPrefix).append("='").append(xdNamespace).append("'");
        for (String key : item.keySet()) {
            if (!key.startsWith(xdPrefix, 0) || !key.endsWith(":collection")) {
                sb.append(" ").append(key).append("='").append(item.get(key)).append("'");
            }
        }
        if (xd.size() == 1) {
            return sb.append("/>\n").toString();
        }
        sb.append(">\n");
        for (int i = 1; i < xd.size(); i++) {
            List xd1 = (List) xd.get(i);
            item = (Map) xd1.get(0);
            for (Object x : item.keySet()) {
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
                } else {

                }
            }
        }
        sb.append("\n</").append(xdName).append(">");
        return sb.toString();
    }

    /** Convert JSON format of X-definition source data to XML.
     * @param json input JSON data.
     * @return string with XML format.
     */
    public static String jsonXdefToXml(final String json) {
        Object o = XonUtils.parseXON(json);
        List xd = (List) o;
        o = xd.get(0);
        if (!(o instanceof Map)) {
            throw new RuntimeException("Unexpected root object");
        }
        Map item = (Map) o;
        for (Object x : item.keySet()) {
            String key = (String) x;
            if (key.endsWith(":def")) {
                return xdefToXml(xd, key); // xd:def
            } else if (key.endsWith(":declaration") || key.endsWith(":component") || key.endsWith(":BNFGrammar")
                || key.endsWith(":lexicon")) {
                return textItemToXml(xd, key);
            } else if (key.endsWith(":collection")) {
                return collectionToXml(xd, key);
            }
        }
        throw new RuntimeException("Unexpected root object: " + item);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Convert X-declaration XML to JSON.
     * @param elem Element with X-definition.
     * @return string with JSON format.
     */
    private static String textItemToJson(final Element elem) {
        StringBuilder sb = new StringBuilder();
        sb.append("[ { \"").append(elem.getNodeName()).append("\": \"");
        sb.append(toJsonString(elem.getNamespaceURI())).append("\"},\n\"");
        sb.append(SUtils.modifyString(elem.getTextContent(), "\"", "\\\"")).append("\"]");
        return sb.toString();
    }


    /** Convert X-declaration XML to JSON.
     * @param elem Element with X-definition.
     * @return string with JSON format.
     */
    private static String collectionToJson(final Element elem) {
        String xdPrefix = elem.getPrefix();
        String xdNamespace = elem.getNamespaceURI();
        StringBuilder sb = new StringBuilder();
        sb.append("[ { \"").append(xdPrefix).append(":collection\": \"").append(toJsonString(xdNamespace)).append("\"");
        NamedNodeMap nnm = elem.getAttributes();
        for (int i = 0; i < nnm.getLength(); i++) {
            Node n = (Node) nnm.item(i);
            String name = n.getNodeName();
            if (!name.equals("xmlns:" + xdPrefix)) {
                sb.append(", \"").append(name).append("\": \"").append(n.getNodeValue()).append("\"");
            }
        }
        sb.append("}");
        NodeList nl = elem.getChildNodes();
        Element el = null;
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n != null && n.getNodeType() == Node.ELEMENT_NODE) {
                el = (Element) n;
                break;
            }
        }
        while (el != null) {
            sb.append(",\n");
            String localName = el.getLocalName();
            if ("def".equals(localName)) {
                sb.append("\n").append(xdefToJson(el)); // xd:def
            } else if ("declaration".equals(localName) || "component".equals(localName)
                || "BNFBNFGrammar".equals(localName) || "lexicon".equals(localName)) {
                sb.append("\n").append(textItemToJson(el)); // xd:declaration
            } else if ("collection".equals(localName)) {
                throw new RuntimeException("Collection in collection");
            } else {
                throw new RuntimeException("Expected X-definition root element. Found: " + el.getNodeName());
            }
            Node n = el.getNextSibling();
            while (n != null) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    break;
                }
                n = n.getNextSibling();
            }
            el = (n != null) ? (Element) el : null;
        }
        sb.append("\n]");
        return sb.toString();
    }
    /** Convert XML format of X-definition source data to JSON.
     * @param xml input XML data.
     * @return string with JSON format.
     */
    public static String xmlXdefToJson(final String xml) {
        Element el = KXmlUtils.parseXml(xml).getDocumentElement();
        String localName = el.getLocalName();
        if ("def".equals(localName)) {
            return xdefToJson(el); // xd:def
        } else if ("declaration".equals(localName) || "component".equals(localName)
            || "BNFBNFGrammar".equals(localName) || "lexicon".equals(localName)) {
            return textItemToJson(el); // xd:declaration
        } else if ("collection".equals(localName)) {
            return collectionToJson(el);
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
