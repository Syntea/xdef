package org.xdef.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xdef.XDConstants;
import static org.xdef.XDConstants.XDEF41_NS_URI;
import static org.xdef.XDConstants.XDEF42_NS_URI;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonUtils;

/** Convertor of X-definition from XML format to JSON format and from JSON to XML.
 * @author trojan
 */
public class XDefToJSON {

    /** Modify string to XML text format. */
    private static String toXmlString(final String s) {
        return KXmlUtils.toXmlText(SUtils.modifyString(SUtils.modifyString(s,"\r\n","\n"),"&#13;\n","\n"), '<', false);
    }

    /** Modify string to JSON format. */
    private static String toJsonString(final String s) {
        return SUtils.modifyString(SUtils.modifyString(SUtils.modifyString(s,"\r\n","\n"),"&#13;\n","\n"),"\"","\\\"");
    }

    /** Remove trailing spaces, */
    private static String removeTrailingSpaces(final String s) {
        for (int i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) > ' ') {
                return s.substring(0, i + 1);
            }
        }
        return s;
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

    private static String adLinePrefixes(final String s) {
        if (s.indexOf('\n') < 0) {
            return s;
        }
        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.StringReader(s));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while((line=br.readLine()) != null) {
                sb.append("  ").append(line).append('\n');
            }
        } catch (IOException ex) { // never happens
            throw new RuntimeException(ex);
        }
        return sb.toString();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  JSON -> XML
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Convert JSON format of xd:def to XML.
     * @param json input JSON data.
     * @return string with XML format.
     */
    @SuppressWarnings({"unchecked", "unchecked"})
    private static String jsonXdefToXml(final List xd) {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> map = (Map) xd.get(0);
        String xdPrefix = "xd";
        String xdNamespace = XDEF42_NS_URI;
        for (String key:  map.keySet()) {
            if (key.startsWith("xmlns:")) {
                String s = (String) map.get(key);
                if (XDEF41_NS_URI.equals(s) || XDEF42_NS_URI.equals(s)) {
                    xdPrefix = key.substring(6);
                    xdNamespace = s;
                    break;
                }

            }
        }
        sb.append("<").append(xdPrefix).append(":def");
        sb.append(" xmlns:").append(xdPrefix).append("='").append(xdNamespace).append("'");
        if (xdNamespace == null || xdNamespace.isEmpty()) {
            throw new RuntimeException("Incorrect X-definition namespace: \"" + map + "\"");
        }
        for (String key: map.keySet()) {
            if (key.equals("xmlns:" + xdPrefix)) {
                continue;
            }
            if (key.equals("%jdef")) {
                String s = map.get(key).toString();
                if (!s.isEmpty()) {
                    sb.append(" name='").append(map.get(key).toString().trim()).append("'");
                }
                continue;
            }
            if (key.equals("%root") || key.equals("%script")) {
                String s = map.get(key).toString().trim();
                if (!s.isEmpty()) {
                    sb.append(" ").append(key.substring(1)).append("='").append(s).append("'");
                }
                continue;
            }
            String s = toXmlString(map.get(key).toString());
            sb.append("\n  ").append(key).append("=\"");
            sb.append(toXmlString(s));
            sb.append("\"");
        }
        if (xd.size() == 1) {
            return sb.append(" />").toString();
        }
        sb.append(">\n");
        for (int i = 1; i < xd.size(); i++) {
            Object o = xd.get(i);
            String s;
            if (o instanceof List) { // xd:json
                List list = (List) o;
                map = (Map<String, Object>) list.get(0);
                if (map.containsKey("%json")) {
                    sb.append("\n  <").append(xdPrefix).append(":json name=");
                    sb.append(toXmlString(XonUtils.toJsonString(map.values().iterator().next(), true)));
                    sb.append(">");
                    s = adLinePrefixes(toXmlString(XonUtils.toJsonString(list.get(1), true)));
                    if (s.endsWith("\n")) {
                        sb.append('\n');
                    }
                    sb.append(s);
                    if (s.endsWith("\n")) {
                        sb.append("  ");
                    }
                    sb.append("</").append(xdPrefix).append(":json>\n");
                } else if (map.containsKey(s="%BNFGrammar") || map.containsKey(s="%local_BNFGrammar")) {
                    sb.append(jsonBNFToXml(list, s, xdPrefix, xdNamespace));
                }
            } else {
                map = (Map<String, Object>) o;
                if ((o = map.get(s="%declaration")) != null || (o = map.get(s="%local_declaration")) != null) {
                    sb.append("\n  <").append(xdPrefix).append(":declaration");
                    if ("%local_declaration".equals(s)) {
                        sb.append(" scope='local'");
                    }
                    sb.append(">");
                    s = toXmlString(removeTrailingSpaces(o.toString()));
                    sb.append(s);
                    if (s.length() > 100 || s.indexOf('\n') >= 0) {
                        sb.append("\n  ");
                    }
                    sb.append("</").append(xdPrefix).append(":declaration>\n");
                } else if ((o = map.get("%component")) != null) {
                    sb.append("\n  <").append(xdPrefix).append(":component>");
                    s = toXmlString(removeTrailingSpaces(getAsXMLText(o)));
                    sb.append(s);
                    if (s.length() > 100 || s.indexOf('\n') >= 0) {
                        sb.append("\n  ");
                    }
                    sb.append("</").append(xdPrefix).append(":component>\n");
                } else if ((o = map.get("%xml")) != null) { // XML model
                    sb.append(removeTrailingSpaces("\n  " + o.toString().trim())).append("\n");
                } else { // declaration
                    throw new RuntimeException("Unexpected object: " + o);
                }
            }
        }
        sb.append("\n</").append(xdPrefix).append(":def>\n");
        return sb.toString();
    }

    /** Convert JSON format of xd:declaration to XML.
     * @param json input JSON data.
     * @return string with XML format.
     */
    @SuppressWarnings("unchecked")
    private static String jsonTextToXml(final List xd,final String key,final String xdPrefix,final String xdNamespace) {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> map = (Map) xd.get(0);
        String xdName = xdPrefix + ':' + key.substring(1);
        sb.append("<").append(xdName);
        sb.append(" xmlns:").append(xdPrefix).append("='").append(xdNamespace).append("'");
        for (Map.Entry o: map.entrySet()) {
            if (!key.equals(o.getKey())) {
                sb.append(" ").append(o.getKey().toString()).append("=\"");
                sb.append(toXmlString(o.getValue().toString())).append("\"");
            }
        }
        sb.append(">");
        String s = toXmlString(xd.get(1).toString());
        if (s.indexOf('\n') == 0 && s.lastIndexOf('\n') != s.length() - 1) {
            s += "\n";
        } else if (s.indexOf('\n') > 0) {
            s = "\n  "+ s + "\n";
        }
        sb.append(s);
        sb.append("</").append(xdName).append(">");
        return sb.toString();
    }

    private static String jsonBNFToXml(final List xd,final String key,final String xdPrefix,final String xdNamespace) {
        Map map = (Map) xd.get(0);
        StringBuilder sb = new StringBuilder();
        sb.append("\n  <").append(xdPrefix).append(":BNFGrammar");
        Object o = map.get(key);
        sb.append(" name='").append(o.toString()).append("'");
        String s;
        if ((s = (String) map.get(s="%extends")) != null) {
            sb.append(" extends='").append(s).append("'");
        }
        if ("%local_BNFGrammar".equals(key)) {
            sb.append(" scope='local'");
        }
        sb.append('>');
        s = toXmlString(removeTrailingSpaces(getAsXMLText(xd.get(1))));
        sb.append(s);
        if (s.length() > 100 || s.indexOf('\n') >= 0) {
            sb.append("\n  ");
        }
        sb.append("</").append(xdPrefix).append(":BNFGrammar>\n");
        return sb.toString();
    }

    /** Convert JSON format of xd:collection to XML.
     * @param json input JSON data.
     * @return string with XML format.
     */
    @SuppressWarnings("unchecked")
    private static String jsonCollectionToXml(final List xd, final String xdName) {
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
            List list = (List) xd.get(i);
            map = (Map) list.get(0);
            for (Object x : map.keySet()) {
                String key = (String) x;
                if (key.equals("%jdef")) {
                   sb.append(jsonXdefToXml(list)); // xd:def
                   break;
                } else if (key.equals("%declaration") || key.equals("%lexicon")) {
                    sb.append(jsonTextToXml(list, key, xdPrefix, xdNamespace));
                   break;
                } else if (key.equals("%BNFGrammar")) {
                    sb.append(jsonBNFToXml(list, key, xdPrefix, xdNamespace));
                } else if (key.equals("%collection")) {
                    throw new RuntimeException("Collection in collection");
                }
            }
        }
        sb.append("\n</").append(xdName).append(">");
        return sb.toString();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  XML -> JSON
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Convert X-definition XML to JSON.
     * @param elem Element with X-definition.
     * @return string with JSON format.
     */
    private static String xmlXdefToJson(final Element elem) {
        String xdPrefix = elem.getPrefix();
        StringBuilder sb = new StringBuilder();
        sb.append("[\n{ \"%jdef\": \"");
        NamedNodeMap nnm = elem.getAttributes();
        Node n = nnm.getNamedItem(xdPrefix + ":name");
        if (n == null) {
            n = nnm.getNamedItem("name");
        }
        if (n != null) {
            sb.append(n.getNodeValue());
            nnm.removeNamedItem(n.getNodeName());
        }
        String nsUri = elem.getNamespaceURI();
        sb.append("\", \"xmlns:").append(xdPrefix).append("\": \"").append(nsUri).append("\"");
        n = nnm.getNamedItem("xmlns:" + xdPrefix);
        if (n != null) {
            nnm.removeNamedItem(n.getNodeName());
        }
        n = nnm.getNamedItem(xdPrefix + ":root");
        if (n == null) {
            n = nnm.getNamedItem("root");
        }
        if (n != null) {
            sb.append(", \"%root\": \"").append(n.getNodeValue()).append("\"");
            nnm.removeNamedItem(n.getNodeName());
        }
        n = nnm.getNamedItem(xdPrefix + ":script");
        if (n == null) {
            n = nnm.getNamedItem("script");
        }
        if (n != null) {
            sb.append(",\n  \"%script\": \"").append(toJsonString(n.getNodeValue())).append("\"");
            nnm.removeNamedItem(n.getNodeName());
        }
        for (int i = 0; i < nnm.getLength(); i++) {
            n = nnm.item(i);
            sb.append(",\n  \"").append(n.getNodeName()).append("\": \"");
            sb.append(toJsonString(n.getNodeValue())).append("\"");
        }
        sb.append("},\n");
        n = getFirstChildElement(elem);
        if (n == null) {
            return sb.append("\n]").toString();
        }
        while (n != null) {
            Element el = (Element) n;
            if (nsUri.equals(n.getNamespaceURI())) {
                String s;
                switch (el.getLocalName()) {
                    case "declaration":
                        sb.append("\n{ \"%");
                        n = el.getAttributeNode(xdPrefix + ":scope");
                        if (n == null) {
                            n = el.getAttributeNode("scope");
                        }
                        sb.append(n != null && "local".equals(n.getNodeValue()) ? "local_declaration" : "declaration");
                        sb.append("\": \"");
                        s = toJsonString(removeTrailingSpaces(el.getTextContent()));
                        sb.append(s);
                        sb.append("\"");
                        if (s.indexOf('\n') >= 0 || s.length() >= 100) {
                            sb.append("\n  ");
                        }
                        sb.append("}");
                        sb.append((n = getNextChildElement(el)) != null ? ",\n" : "\n");
                        continue;
                    case "component":
                        sb.append("\n{ \"%component\": \"");
                        s = toJsonString(removeTrailingSpaces(el.getTextContent()));
                        sb.append(s);
                        sb.append("\"");
                        if (s.indexOf('\n') >= 0 || s.length() >= 100) {
                            sb.append("\n  ");
                        }
                        sb.append("}");
                        sb.append((n = getNextChildElement(el)) != null ? ",\n" : "\n");
                        continue;
                    case "BNFGrammar":
                        n = el.getAttributeNode(xdPrefix + ":scope");
                        if (n == null) {
                            n = el.getAttributeNode("scope");
                        }
                        sb.append("\n[ { \"%");
                        sb.append(n != null && "local".equals(n.getNodeValue()) ? "local_BNFGrammar" : "BNFGrammar");
                        sb.append("\": \"");
                        n = el.getAttributeNode(xdPrefix + ":name");
                        if (n == null) {
                            n = el.getAttributeNode("name");
                        }
                        if (n != null) {
                            sb.append(toJsonString(n.getNodeValue())).append("\"");
                        }
                        n = el.getAttributeNode(xdPrefix + ":extends");
                        if (n == null) {
                            n = el.getAttributeNode("extends");
                        }
                        if (n != null) {
                            sb.append(", \"%extends\": \"").append(toJsonString(n.getNodeValue())).append("\"");
                        }
                        sb.append("}, \"");
                        sb.append(s = toJsonString(removeTrailingSpaces(el.getTextContent())));
                        sb.append("\"");
                        if (s.indexOf('\n') >= 0 || s.length() >= 100) {
                            sb.append("\n");
                        }
                        sb.append("]");
                        sb.append((n = getNextChildElement(el)) != null ? ",\n" : "\n");
                        continue;
                    case "json":
                        n = el.getAttributeNodeNS(nsUri, "name");
                        if (n == null) {
                            n = el.getAttributeNode("name");
                        }
                        if (n != null) {
                            sb.append("\n[ {\"%json\": \"").append(n.getNodeValue());
                            sb.append("\"},");
                            s = removeTrailingSpaces(el.getTextContent());
                            sb.append(s);
                            sb.append(s.length() < 100 && s.indexOf('\n') < 0 ? "]" : "\n]");
                            sb.append((n = getNextChildElement(el)) != null ? ",\n" : "\n");
                        } else {
                            throw new RuntimeException("Expected name of json model");
                        }
                        continue;
                }
            }
            //XML model
            sb.append("\n{ \"%xml\": \"");
            String s = adLinePrefixes(toJsonString(removeTrailingSpaces(KXmlUtils.nodeToString(el, true))));
            int i1 = s.indexOf("xmlns:" + xdPrefix + "=\\\"");
            if (i1 > 0) {
                int i2 = s.indexOf("\"", i1 + xdPrefix.length() + 9);
                if (i2 > 0) {
                    s = s.substring(0, i1).trim() + s.substring(i2+1);
                }
            }
            if (s.indexOf('\n') >= 0 || s.length() >= 100) {
                sb.append("\n").append(s.trim());
            } else {
                sb.append(s);
            }
            sb.append("\"");
            if (s.indexOf('\n') >= 0 || s.length() >= 100) {
                sb.append("\n  ");
            }
            sb.append("}");
            sb.append((n = getNextChildElement(el)) != null ? ",\n" : "\n");
        }
        sb.append("]");
        return sb.toString();
    }

    /** Convert text item to JSON.
     * @param elem Element with test.
     * @return string with JSON format.
     */
    private static String xmlTextToJson(final Element elem) {
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
        String s = elem.getNodeName();
        int ndx = s.indexOf(':');
        s = "%" + (ndx > 0 ? s.substring(ndx + 1) : s);
        sb.append(sep).append("\"").append(s).append("\": \"");
        sb.append(toJsonString(elem.getNamespaceURI())).append("\"}, \"");
        s = elem.getTextContent();
        if (s.endsWith("\n")) {
            s = s.substring(0, s.length() - 1);
        }
        return sb.append(SUtils.modifyString(s, "\"", "\\\"")).append("\"]").toString();
    }

    private static String xmlBNFToJson(final Element el) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n[ { \"%BNFGrammar\": \"");
        String xdPrefix = el.getPrefix();
        Node n = el.getAttributeNode(xdPrefix + ":name");
        if (n == null) {
            n = el.getAttributeNode("name");
        }
        if (n != null) {
            sb.append(toJsonString(n.getNodeValue())).append("\"");
        }
        n = el.getAttributeNode(xdPrefix + ":extends");
        if (n == null) {
            n = el.getAttributeNode("extends");
        }
        if (n != null) {
            sb.append(", \"%extends\": \"").append(toJsonString(n.getNodeValue())).append("\"");
        }
        sb.append("}, \"");
        String s = toJsonString(removeTrailingSpaces(el.getTextContent()));
        sb.append(s);
        sb.append("\"");
        if (s.indexOf('\n') >= 0 || s.length() >= 100) {
            sb.append("\n");
        }
        sb.append("]");
//        sb.append((n = getNextChildElement(el)) != null ? ",\n" : "\n");
        return sb.toString();
    }

    /** Convert collection XML to JSON.
     * @param elem Element with collection.
     * @return string with JSON format.
     */
    private static String xmlCollectionToJson(final Element elem) {
        String xdPrefix = elem.getPrefix();
        String nsURI = elem.getNamespaceURI();
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
            n = getNextChildElement(n);
            String localName = el.getLocalName();
            String ns = getXdefNamespace(el);
            if (ns != null) { // namespace URI belongs to X-definition
                sb.append(",\n");
                switch(localName) {
                    case "def": sb.append("\n").append(xmlXdefToJson(el)); continue;
                    case "declaration":
                    case "component":
                    case "lexicon": sb.append("\n").append(xmlTextToJson(el)); continue;
                    case "BNFGrammar":  sb.append("\n").append(xmlBNFToJson(el)); continue;
                    case "collection": throw new RuntimeException("Error: collection can not be in collection");
                }
            }
            throw new RuntimeException("Illegal element in collectio: " + el.getNodeName());
        }
        sb.append("\n]");
        return sb.toString();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  JSON -> XML
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
            if (key.equals("%jdef")) {
                return jsonXdefToXml(items); // xd:def
            } else if (key.equals("%declaration") || key.equals("%component") || key.equals("%BNFGrammar")
                || key.equals("%lexicon")) {
                return jsonTextToXml(items, key, "xd", (String) item.get(key));
            } else if (key.endsWith(":collection")) {
                return jsonCollectionToXml(items, key);
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
                case "def": return xmlXdefToJson(el); // xd:def
                case "declaration":
                case "lexicon": return xmlTextToJson(el); // xd:declaration
                case "BNFBNFGrammar": return xmlBNFToJson(el);
                case "collection": return xmlCollectionToJson(el); // collection
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
     * </ul>.
     * @throws Exception if an error occurs.
     */
    public static void main(String... args) throws Exception {
        final String info = // String with command line information.
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
        final StringBuilder err = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-h":
                case "/?":
                case "--help": System.out.println(info); return;
                case "-o":
                case "--output":
                    if (output != null) {
                        err.append("Redefinition of ").append(arg).append(".\n");
                    }
                    output = new File(args[++i]);
                    if (output.exists() && output.isDirectory()) {
                        err.append("Output is directory, not file: ").append(args[i]).append(".\n");
                    }
                    continue;
                case "-i":
                case "--input":
                    if (input != null) {
                        err.append("Redefinition of ").append(arg).append(".\n");
                    }
                    input = new File(args[++i]);
                    if (!input.exists() || input.isDirectory()) {
                        err.append("input not exists or it is a directory: ").append(args[i]).append(".\n");
                    }
                    continue;
                default: err.append("Command parameter error: ").append(arg).append(".\n");
            }
        }
        if (output == null) {
            err.append("Missing output file.\n");
        }
        if (input == null) {
            err.append("Missing input file.\n");
        }
        if (err.length() > 0) {
            throw new RuntimeException(err + info);
        }
        String s = SUtils.readString(input, "UTF-8");
        s = (s.trim().startsWith("<")) ? xmlXdefToJson(s) : jsonXdefToXml(s);
        SUtils.writeString(output, s, "UTF-8");
    }
}
