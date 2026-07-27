package org.xdef.web.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
}
