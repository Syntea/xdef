package org.xdef.web.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract servlet used for servlet implementation.
 * <p>
 * It sets globally and localy X-definition report-language
 * (ISO-639 two letters or ISO-639-2 three letters, e.g. "eng", "ces", "slk").
 *
 * @author Vaclav Trojan
 */
public abstract class ServletUtil {
    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(ServletUtil.class);

    /**
     * Create string from preText which can be inserted into HTML
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

}
