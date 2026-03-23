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

    /** Convert JSON format of X-definition to XML.
     * @param json input JSON data.
     * @return string with XML format.
     */
    @SuppressWarnings("unchecked")
    public static String jsonXdefToXml(final String json) {
        StringBuilder sb = new StringBuilder();
        Object o = XonUtils.parseXON(json);
        List xd = (List) o;
        String xdPrefix = null;
        String xdNamespace = null;
        o = xd.get(0);
        if (!(o instanceof Map)) {
            throw new RuntimeException("Unexpected root object");
        }
        Map<String, Object> item = (Map) o;
        for (String key : item.keySet()) {
            if (key.endsWith(":def")) {
                xdPrefix = key.substring(0, key.length() - 4);
                xdNamespace = (String) item.get(key);
                sb.append("<").append(xdPrefix).append(":def");
                sb.append(" xmlns:").append(xdPrefix).append("='").append(xdNamespace).append("'");
                break;
            }
        }
        if (xdNamespace == null || xdNamespace.isEmpty()) {
            throw new RuntimeException("Incorrect X-definition namespace: \"" + item + "\"");
        }
        o = item.get(xdPrefix + ":name");
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
                sb.append("\n  ").append(key).append("='").append(item.get(key).toString()).append("'");
            }
        }
        sb.append(">\n");
        for (int i = 1; i < xd.size(); i++) {
            o = xd.get(i);
            if (!(o instanceof Map)) {
                throw new RuntimeException("Unexpected object " + i);
            }
            String s;
            item = (Map) o;
            if ((o = item.get(xdPrefix + ":declaration")) != null) { // declaration
                sb.append("<").append(xdPrefix).append(":declaration");
                s = o.toString();
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
                if (!s.endsWith("\n")) {
                    s += '\n';
                }
                sb.append(s);
                sb.append("</").append(xdPrefix).append(":component>\n");
            } else if (item.size() == 1) { // JSON model
                String name = item.keySet().iterator().next();
                sb.append("\n<").append(xdPrefix).append(":json name='").append(name).append("'>\n");
                sb.append(XonUtils.toJsonString(item.get(name), true));
                sb.append("\n</").append(xdPrefix).append(":json>\n");
            } else { // declaration
                throw new RuntimeException("Unexpected object: " + o);
            }
        }
        sb.append("</").append(xdPrefix).append(":def>\n");
        return sb.toString();
    }

    /** Convert XML format of X-definition to JSON.
     * @param xml input XML data.
     * @return string with JSON format.
     */
    public static String xmlXdefToJson(final String xml) {
        StringBuilder sb = new StringBuilder();
        Element el = KXmlUtils.parseXml(xml).getDocumentElement();
        String xdPrefix = el.getPrefix();
        String xdNamespace = el.getNamespaceURI();
        if (!el.getLocalName().equals("def")) {
            throw new RuntimeException("Expected root as element <xd:def ...");
        }
        sb.append("[\n");
        sb.append("{\"").append(xdPrefix).append(":def\": \"").append(xdNamespace).append("\"");
        Attr attr = el.getAttributeNodeNS(xdNamespace, "name");
        if (attr == null) {
            attr = el.getAttributeNode("name");
        }
        if (attr != null) {
            sb.append(", \"").append(xdPrefix).append(":name\": \"").append(attr.getNodeValue()).append("\"");
        }
        attr = el.getAttributeNodeNS(xdNamespace, "root");
        if (attr == null) {
            attr = el.getAttributeNode("root");
        }
        if (attr != null) {
            sb.append(", \"xd:root\": \"").append(attr.getValue()).append("\"");
        }
        NamedNodeMap nnm = el.getAttributes();
        for (int i = 0; i < nnm.getLength(); i++) {
            Node n = nnm.item(i);
            if (n.getLocalName().startsWith("impl-")) {
                sb.append(",\n  \"").append(n.getLocalName()).append("\": \"").append(n.getNodeValue()).append("\"");
            }
        }
        sb.append("}");
        NodeList nl = el.getElementsByTagName("*");
        if (nl.getLength() > 0) {
            sb.append(",\n");
        }
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                el = (Element) n;
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
                                sb.append("\"").append(xdPrefix).append(":declaration\": \"");
                                sb.append(SUtils.modifyString(el.getTextContent(), "\"", "\\\""));
                                sb.append("\"\n},\n");
                                break;
                            case "json":
                                attr = el.getAttributeNodeNS(xdNamespace, "name");
                                if (attr == null) {
                                    attr = el.getAttributeNode("name");
                                }
                                if (attr != null) {
                                    sb.append("{ \"").append(attr.getValue()).append("\":");
                                    sb.append(el.getTextContent());
                                    sb.append("}");
                                    if (i < nl.getLength() - 1) {
                                        sb.append(",");
                                    }
                                    sb.append("\n");
                                } else {
                                    throw new RuntimeException("Expected name of json model at " + i);
                                }
                                break;
                            case "component":
                                sb.append("{ \"").append(xdPrefix).append(":component\": \"");
                                sb.append(SUtils.modifyString(el.getTextContent(), "\"", "\\\""));
                                sb.append("\"}");
                                if (i < nl.getLength() - 1) {
                                    sb.append(",");
                                }
                                sb.append("\n");
                                break;
                            default:
                                throw new RuntimeException("Expected item: " + el.getNodeName() + ", " + i);
                        }
                    }
                }
            }
        }
        sb.append("]");
        return sb.toString();
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