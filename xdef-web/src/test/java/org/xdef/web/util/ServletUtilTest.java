package org.xdef.web.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xdef.web.servlet.Playground;

import jakarta.servlet.ServletException;

/**
 * Test ServletUtil.
 *
 * @author Sisma+Claude
 */
class ServletUtilTest {

    @Test
    void preTextToPreContAndPreTextToDivCont() {
        assertNull(ServletUtil.preTextToPreCont(null));
        assertNull(ServletUtil.preTextToDivCont(null));
        assertEquals("", ServletUtil.preTextToPreCont(""));
        assertEquals("", ServletUtil.preTextToDivCont(""));

        String in = "a<b>c&d\ne f\tg\r\nh";
        assertEquals("a&lt;b&gt;c&amp;d\ne f\tg\nh",
            ServletUtil.preTextToPreCont(in));
        assertEquals("a&lt;b&gt;c&amp;d<br/>\ne&nbsp;f&nbsp;&nbsp;&nbsp;&nbsp;g<br/>\nh",
            ServletUtil.preTextToDivCont(in));

        // '\r' is always removed, regardless of pre/div mode
        assertEquals("ab", ServletUtil.preTextToPreCont("a\rb"));
        assertEquals("ab", ServletUtil.preTextToDivCont("a\rb"));
    }

    @Test
    void htmlToAttrVal() {
        assertNull(ServletUtil.htmlToAttrVal(null));
        assertEquals("", ServletUtil.htmlToAttrVal(""));
        assertEquals("plain text", ServletUtil.htmlToAttrVal("plain text"));
        assertEquals("He said &quot;hi&quot; &lt;b&gt;&amp;amp;&lt;/b&gt;",
            ServletUtil.htmlToAttrVal("He said \"hi\" <b>&amp;</b>"));
    }

    @Test
    void genHtmlMessage() {
        String s = ServletUtil.genHtmlMessage("My Title", "<p>My Body</p>");
        assertTrue(s.contains("<title>My Title</title>"), () -> "missing title element: " + s);
        assertTrue(s.contains("<h1>My Title</h1>"), () -> "missing h1 element: " + s);
        assertTrue(s.contains("<p>My Body</p>"), () -> "missing body content: " + s);
        assertTrue(s.startsWith("<html "), () -> "unexpected start: " + s);
        assertTrue(s.trim().endsWith("</html>"), () -> "unexpected end: " + s);
    }

    @Test
    void readRsrcAsStringExisting() {
        String s = ServletUtil.readRsrcAsString(ServletUtilTest.class, "rsrc-test.txt");
        assertEquals("hello žluťoučký kůň\n", s);
    }

    @Test
    void readRsrcAsStringMissing() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> ServletUtil.readRsrcAsString(ServletUtilTest.class, "non-existent-resource.txt"));
        assertTrue(ex.getMessage().contains("non-existent-resource.txt"));
    }

    @Test
    void clearDirectoryRemovesContentsButKeepsDirectory(@TempDir File tmp) throws IOException {
        File subDir = new File(tmp, "sub");
        assertTrue(subDir.mkdir());
        Files.writeString(new File(tmp, "a.txt").toPath(), "a");
        Files.writeString(new File(subDir, "b.txt").toPath(), "b");

        ServletUtil.clearDirectory(tmp);

        assertTrue(tmp.isDirectory());
        String[] remaining = tmp.list();
        assertEquals(0, remaining == null ? -1 : remaining.length);
    }

    @Test
    void clearDirectoryOnPlainFileIsNoOp(@TempDir File tmp) throws IOException {
        File f = new File(tmp, "plain.txt");
        Files.writeString(f.toPath(), "x");

        ServletUtil.clearDirectory(f);

        assertTrue(f.exists());
    }

    @Test
    void clearDirectoryOnNullIsNoOp() {
        ServletUtil.clearDirectory(null);
    }

    @Test
    void mustacheReplacesAllOccurrencesOfEachPlaceholder() throws ServletException {
        String template = "<title>((status))</title><div class=\"((status))\">((body))</div>";
        Map<String, String> values = Map.of("status", "OK", "body", "hello");
        assertEquals("<title>OK</title><div class=\"OK\">hello</div>", ServletUtil.mustache(template, values));
    }

    @Test
    void mustacheUnknownPlaceholderAndUnusedKeyThrows() throws ServletException {
        String template = "a((known))b((unknown))c";
        ServletException ex = assertThrows(ServletException.class,
            () -> ServletUtil.mustache(template, Map.of("known", "VALUE", "unused", "VALUEX")));
        assertTrue(ex.getMessage().contains("unused-keys: [unused]"));
    }

    @Test
    void mustacheHandlesAdjacentPlaceholders() throws ServletException {
        Map<String, String> values = new HashMap<>(Map.of("a", "X", "b", "Y"));
        values.put("n", null);
        assertEquals("XY", ServletUtil.mustache("((a))((b))((n))((unknown))", values));
    }

    @Test
    void mustacheKeyAllowsLettersDigitsUnderscoreAndHyphen() throws ServletException {
        assertEquals("VAL", ServletUtil.mustache("((a-b_c9))", Map.of("a-b_c9", "VAL")));
    }

    @Test
    void mustacheDoesNotTreatNonKeyContentAsPlaceholder() throws ServletException {
        // a space is not a valid key character - "((" here must be left as literal text,
        // not gobbled up together with the unrelated "))" that follows much later
        String template = "((not a key)) then ((real))";
        Map<String, String> values = Map.of("real", "VALUE");
        assertEquals("((not a key)) then VALUE", ServletUtil.mustache(template, values));
    }

    @Test
    void mustacheOnNullTemplateReturnsNull() throws ServletException {
        assertNull(ServletUtil.mustache(null, Map.of()));
    }

    @Test
    void mustacheFillsRealPlaygroundTemplateCompletely() throws ServletException {
        String template = ServletUtil.readRsrcAsString(
            Playground.class, "webapp/playground/playground-response-template.html");

        Set<String> keys = new HashSet<>();
        Matcher m = Pattern.compile("\\(\\((.+?)\\)\\)").matcher(template);
        while (m.find()) {
            keys.add(m.group(1));
        }
        assertTrue(keys.contains("status"), "template is expected to use ((status)) more than once");
        int statusPlaceholderCount = template.split(Pattern.quote("((status))"), -1).length - 1;
        assertTrue(statusPlaceholderCount > 1, "expected ((status)) more than once, found "
            + statusPlaceholderCount);

        Map<String, String> values = new HashMap<>();
        for (String key : keys) {
            values.put(key, "[" + key + "]");
        }

        String filled = ServletUtil.mustache(template, values);

        assertTrue(!filled.contains("(("), "unresolved placeholder left in: " + filled);
        int statusOccurrences = filled.split(Pattern.quote("[status]"), -1).length - 1;
        assertEquals(statusPlaceholderCount, statusOccurrences,
            "every ((status)) occurrence in the template must be replaced");
    }
}
