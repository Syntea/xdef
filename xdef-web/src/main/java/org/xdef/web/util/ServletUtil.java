package org.xdef.web.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonUtils;
import org.yaml.snakeyaml.Yaml;

import jakarta.servlet.http.HttpServletRequest;


/**
 * utilities for servlets, only static usage
 *
 * @author Vaclav Trojan, sisma
 */
public class ServletUtil {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(ServletUtil.class);


    /** only static usage */
    private ServletUtil() {}


    /**
     * Get parameter from servlet request.
     *
     * @param request   servlet request.
     * @param name      name of parameter.
     * @return trimmed value of parameter or an empty string.
     */
    public static final String getParam(final HttpServletRequest request, final String name) {
        String result = request.getParameter(name);
        return null == result ? "" : result.trim();
    }

    /**
     * Create string from preText which can be inserted into HTML.
     *
     * @param preText pre-formatted text to be converted
     * @param pre if true convert to as inside pre-element. Otherwise convert to as inside div-element
     * @return converted data
     */
    private static final String preTextToHtml(final String preText, final boolean pre) {
        if (preText == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder(preText.length() * 2);
        for (int i = 0; i < preText.length(); ++i) {
            char c = preText.charAt(i);
            switch (c) {
                case '\n':
                    sb.append(pre ? "\n" : "<br/>\n");
                    break;
                case ' ':
                    sb.append(pre ? " " : "&nbsp;");
                    break;
                case '\t':
                    sb.append(pre ? "\t" : "&nbsp;&nbsp;&nbsp;&nbsp;"); //four spaces
                    break;
                case '\r':
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                default:
                    sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * see {@link #preTextToHtml(String, boolean)} with pre=true
     *
     * @param preText pre-formatted text to be converted
     * @return converted data
     */
    public static final String preTextToPreCont(final String preText) {
    	return preTextToHtml(preText, true);
    }

    /**
     * see {@link #preTextToHtml(String, boolean)} with pre=false
     *
     * @param preText pre-formatted text to be converted
     * @return converted data
     */
    public static final String preTextToDivCont(final String preText) {
    	return preTextToHtml(preText, false);
    }

    /**
     * Create string from html-string which can be inserted into HTML-quoted-attribute
     *
     * @param html html-string to be converted
     * @return converted html-string
     */
    public static final String htmlToAttrVal(final String html) {
        if (html == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder(html.length() * 2);
        for (int i = 0; i < html.length(); ++i) {
            char c = html.charAt(i);
            switch (c) {
                case '"':
                    sb.append("&quot;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /** matches a "((key))" placeholder; a key is one or more of 'a'-'z', 'A'-'Z', '0'-'9', '_' or '-' */
    private static final Pattern MUSTACHE_PLACEHOLDER = Pattern.compile("\\(\\(([a-zA-Z0-9_-]+)\\)\\)");

    /**
     * Fill a "mustache"-like HTML template by replacing all "((key))" placeholders with values from the
     * given map. A "((...))" sequence whose content is not a valid key, or whose key is not present in
     * the map, is left untouched in the result.
     *
     * @param template HTML template (e.g. playground-response-template.html) with "((key))" placeholders.
     * @param values map of placeholder keys (without the surrounding "((" / "))") to their replacement values.
     * @return template with placeholders replaced.
     */
    public static final String mustache(final String template, final Map<String, String> values) {
        if (template == null) {
            return null;
        }

        Matcher       matcher = MUSTACHE_PLACEHOLDER.matcher(template);
        StringBuilder sb      = new StringBuilder(template.length() * 2);
        while (matcher.find()) {
            String value = values.get(matcher.group(1));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value != null ? value : matcher.group()));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /** generates html-page with given title and body
     * @param title given title
     * @param body  given body
     * @return generated html-page
     */
    public final static String genHtmlMessage(final String title, final String body) {
        return "<html xmlns='http://www.w3.org/1999/xhtml'>\n" +
            "  <head>\n" +
            "     <meta http-equiv='content-type' content='text/html; charset=UTF-8'/>\n"+
            "     <title>" + title + "</title>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <h1>" + title + "</h1>\n" + body +
            "  </body>\n" +
            "</html>"
        ;
    }

    /**
     * read text java-resource, it's supposed encoding UTF-8
     * @param clazz base class for relative path
     * @param resource path to resource
     * @return required java-resource as string
     */
    public final static String readRsrcAsString(final Class<?> clazz, final String resource) {
        return Optional.ofNullable(clazz.getResourceAsStream(resource))
            .map(is -> {
                try {
                    return is.readAllBytes();
                } catch (IOException ex) {
                    throw new RuntimeException(
                        "Unreadable resource \"" + resource + "\" by class " + clazz.getName(), ex);
                }
            })
            .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
            .orElseThrow(() -> new RuntimeException(
                "Non-existent resource \"" + resource + "\" by class " + clazz.getName()))
        ;
    }

    /** Delete all files and subdirectories from argument.
     * @param dir directory to be cleared.
     */
    public static final void clearDirectory(final File dir) {
        if (dir != null && dir.isDirectory()) {
            deleteFiles(dir.listFiles());
        }
    }

    /** Delete all files and subdirectories from argument.
     * @param files files and directories to be cleared.
     */
    public static final void deleteFiles(final File[] files) {
        for (File f: files) {
            if (f.exists()) {
                if (f.isDirectory()) {
                    clearDirectory(f);
                }
                f.delete();
            }
        }
    }

    /**
     * convert result of YAML parser to JSON. Using recursive transformation
     *
     * @param o result of YAML parser
     * @return JSON result
     */
    public static Object convertYamlToJson(final Object o) {
        if (null == o) {
            return null;
        }
        if (o instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> om     = (Map<Object, Object>)o;
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<Object, Object> en : om.entrySet()) {
                result.put(
                    (String)convertYamlToJson(en.getKey()),
                    convertYamlToJson(en.getValue())
                );
            }
            return result;
        } else if (o instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> ol     = (List<Object>)o;
            List<Object> result = new ArrayList<>();
            for (int i=0; i < ol.size(); i++ ) {
                result.add(convertYamlToJson(ol.get(i)));
            }
            return result;
        } else if (o instanceof byte[]) {
            byte[] oba = (byte[])o;
            return new String(oba, StandardCharsets.UTF_8);
        }
        return o;
    }

    /**
     * Converts XON to the format <code>outFormat</code>
     * @param xon       Xon input data
     * @param outFormat output format
     * @return          converted <code>xon</code> in format <code>outFormat</code>
     */
    public static String convertXon2Str(Object xon, XdDataFormat outFormat) {
        if (outFormat == null) {
            return null;
        }

        String result = null;

        switch (outFormat) {
            case json:
                result = XonUtils.toJsonString(xon, true);
                break;
            case yaml:
                Yaml yaml = new Yaml();
                result = yaml.dump(XonUtils.xonToJson(xon));
                break;
            case csv:
                if (xon instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> xonCsv = (List<Object>)xon;
                    try {
                        result = XonUtils.toCsvString(xonCsv);
                    } catch (Exception ex) {
                        //return null
                    }
                }
                break;
            case ini:
                if (xon instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> xonIni = (Map<String, Object>)xon;
                    try {
                        result = XonUtils.toIniString(xonIni);
                    } catch (Exception ex) {
                        //return null
                    }
                }
                break;
            case xon:
                result = XonUtils.toXonString(xon, true);
                break;
            case xml:
                result = KXmlUtils.nodeToString(
                    XonUtils.xonToXml(xon),
                    true, false, true, 120
                );
                /* * /
                //DBG:
                result += "\n\n==json-xml==\n" + KXmlUtils.nodeToString(
                    XonUtils.xonToXml(XonUtils.xonToJson(xon)),
                    true, false, true, 120
                );
                if (xon instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> xonCsv = (List<Object>)xon;
                    result += "\n\n==csv-xml==\n" + KXmlUtils.nodeToString(
                        XonUtils.csvToXml(xonCsv),
                        true, false, true, 120
                    );
                }
                if (xon instanceof Map) {
                    result += "\n\n==ini-xml==\n" + KXmlUtils.nodeToString(
                        XonUtils.iniToXml(XonUtils.xonToJson(xon)),
                        true, false, true, 120
                    );
                }
                /**/
                break;
        }

        return result;
    }

}
