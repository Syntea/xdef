package test.xdef;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.msg.SYS;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SException;
import org.xdef.sys.SRuntimeException;
import static org.xdef.sys.STester.printThrowable;
import static org.xdef.sys.STester.runTest;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonNames;
import org.xdef.xon.XonUtils;
import test.XDTester;
import static test.XDTester._xdNS;
import static test.XDTester.chkCompoinentSerializable;

/** Test processing JSON objects with Xdefinitions and X-components.
 * @author Vaclav Trojan
 */
public class TestJsonXdef extends XDTester {
    private String _dataDir; // dirsctory with test data
    private File[] _jfiles; // array with jdef files
    private String _tempDir; // directory where to generate files

    public TestJsonXdef() {super();}

    /** Get ID number of a test from the file name.
     * @param f file name
     * @return ID (string created from the file name without the prefix "Test"
     * and without file extension.
     */
    private static String getId(final File f) {
        String s = f.getName();
        return s.substring(4, s.lastIndexOf('.'));
    }

    /** Generate XML files from JSON data in W3C mode and Xdefinition mode,
     * create Xdefinitions in both modes and generate X-components. Then
     * compile Xdefinitions and X-components.
     * @return XDPool compiled from Xdefinitions.
     * @throws RuntimeException if an error occurs.
     */
    private XDPool genAll(final String filter) {
        try {
            File f = new File(clearTempDir(), "json");
            f.mkdirs();
            _tempDir = f.getCanonicalPath().replace('\"', '/');
            if (!_tempDir.endsWith("/")) {
                _tempDir += "/";
            }
        } catch (IOException ex) {
            fail(ex);
            return null;
        }
        // Initialize fields, test files and directories
        _dataDir = getDataDir() + "json/";
        _jfiles = SUtils.getFileGroup(_dataDir + filter + ".xdef");
        // Generate files and compile Xdefinitions and X-components.
        try {
            boolean rebuild = false;
            String xdir = _tempDir + "x/";
            File fdir = new File(xdir);
            if (fdir.exists() && !fdir.isDirectory()) {
                //Directory doesn't exist or isn't accessible: &{0}
                throw new SRuntimeException(SYS.SYS025, fdir.getAbsolutePath());
            }
            fdir.mkdirs();
            if (fdir.exists()) { // ensure the src directory exists.
                SUtils.deleteAll(fdir, true); // clear this directory
            }
            fdir.mkdirs();
            String components =
                "<xd:component xmlns:xd='" + XDConstants.XDEF32_NS_URI + "'>\n";
            for (File fdef: _jfiles) {
                Element el;
                String id = getId(fdef); // get ID from jdef file name
                // get all json files for this test
                File[] data = SUtils.getFileGroup(_dataDir+"Test"+id +"*.json");
                String rName = "Test"+id;
                for (File f: data) {
                    String name = f.getName();
                    int ndx = name.indexOf(".json");
                    name = name.substring(0, ndx);
                    Object json = XonUtils.parseJSON(f);
                    // write JSON as XML (W3C modc)
                    el = XonUtils.xonToXmlW(json);
                    SUtils.writeString(new File(_tempDir + name + "a.xml"),
                        KXmlUtils.nodeToString(el,true), "UTF-8");
                    if (!XonUtils.xonEqual(XonUtils.xmlToXon(el),
                        XonUtils.xmlToXon(XonUtils.xonToXml(json)))) {
                        throw new RuntimeException(rName + " xml transformation to JSON differs:\n" +
                            KXmlUtils.nodeToString(XonUtils.xonToXmlW(json), true) + "\n" +
                            KXmlUtils.nodeToString(XonUtils.xonToXml(json), true) + "\n");
                    }
                }
                // create X-component items
                String cls = "  %class test.common.json.component.Test" + id;
                KXmlUtils.parseXml(_dataDir + "Test" + id + ".xdef").getDocumentElement();
                components += cls +" %link Test" + id + "#a" + ";\n";
            }
            components += "</xd:component>";
            // write X-component declaration to the file
            File componentFile = new File(_tempDir + "Components.xdef");
            SUtils.writeString(componentFile, components, "UTF-8");
            // compile all Xdefinitions to XDPool
            XDPool xp;
            try {
                File[] files = new File[_jfiles.length + 1];
                System.arraycopy(_jfiles, 0, files, 1, _jfiles.length);
                files[0] = componentFile;
                xp = compile(files);
            } catch (RuntimeException ex) {
                throw new RuntimeException(ex);
            }
            File oldFile, newFile;
            // Generate X-components to the directory test
            genXComponent(xp, fdir);
            String componentDir = _tempDir + "test/common/json/component/";
            new File(componentDir).mkdirs();
            String newComponentDir = xdir + "test/common/json/component/";
            for (File fdef: _jfiles) { // check if something changed
                String id = getId(fdef);
                newFile = new File(newComponentDir + "Test" + id + ".java");
                oldFile = new File(componentDir + "Test" + id + ".java");
                if (!oldFile.exists()||SUtils.compareFile(oldFile,newFile)>=0) {
                    SUtils.copyToFile(newFile, oldFile);
                    rebuild = true; //force rebuild
                }
                newFile.delete();
            }
            try {
                SUtils.deleteAll(xdir, true);// delete X-components java sources
            } catch (SException ex) {}
            if (!rebuild) {
                for (File fdef: _jfiles) {
                    String id = getId(fdef);
                    try { // check if X-components arte compliled
                        Class clazz = Class.forName("test.common.json.component.Test" + id);
                        if (clazz == null) {
                            return null; //force rebuild
                        }
                        clazz = Class.forName("test.common.json.component.Test" + id + 'a');
                        if (clazz == null) {
                            return null;  //force rebuild
                        }
                    } catch (ClassNotFoundException ex) {
                        rebuild = true;  //force rebuild
                        break;
                    }
                }
            }
            if (rebuild) {
                File[] ff =	SUtils.getFileGroup(_tempDir+"test/common/json/component/Test*.java");
                String[] sources = new String[ff.length];
                for (int i = 0; i < ff.length; i++) {
                    sources[i] = ff[i].getPath();
                }
                Class<?> clazz = XDConstants.class;
                String className = clazz.getName().replace('.', '/') + ".class";
                URL u = clazz.getClassLoader().getResource(className);
                String classpath = u.toExternalForm();
                if (classpath.startsWith("jar:file:") && classpath.indexOf('!') > 0) {
                    classpath=classpath.substring(9,classpath.lastIndexOf('!'));
                    classpath = new File(classpath).getAbsolutePath().replace('\\','/');
                } else {
                    classpath = new File(u.getFile()).getAbsolutePath().replace('\\','/');
                    classpath = classpath.substring(0, classpath.indexOf(className));
                }
                // where are compiled classes of tests
                clazz = XDTester.class;
                className = clazz.getName().replace('.', '/') + ".class";
                u = clazz.getClassLoader().getResource(className);
                String classDir = new File(u.getFile()).getAbsolutePath().replace('\\', '/');
                classDir = classDir.substring(0, classDir.indexOf(className));
                compileSources(classpath, classDir, sources);
            }
            return xp; // return XDPool with compiled Xdefinitions
        } catch (RuntimeException ex) {
            throw ex;
        } catch (SException ex) {
            throw new RuntimeException(ex);
        }
    }

    /** Provides different tests on files with given ID.
     * @param xp compiled XDPool from generated Xdefinitions.
     * @param id identifier test.
     * @return the empty string if tests are OK, otherwise return the string
     * with error messages.
     */
    private String testJdef(final XDPool xp, final String id) {
        Element e;
        XComponent xc;
        String result = "";
        ArrayReporter reporter = new ArrayReporter();
        // get all json files for this test
        for (File f : SUtils.getFileGroup(_tempDir+"Test"+id+"*a.xml")) {
            Object json;
            Object o;
            String name = f.getName();
            String basename = name.substring(0, name.indexOf("a.xml"));
            // read JSON data
            try {
                json = XonUtils.parseJSON(_dataDir + basename + ".json");
            } catch (SRuntimeException ex) {
                result += (result.isEmpty() ? "" : "\n") + "Incorrect JSON data Test"+id+".json";
                continue;
            }
            // create XDDocument with W3C from Xdefinition
            try {
                reporter.clear(); // clear reporter
                // parseJSON data with Xdefinition
                e = xp.createXDDocument("Test"+id).xparse(f, reporter);
                if (reporter.errorWarnings()) { // check errors
                    result += (result.isEmpty() ? "" : "\n") + "ERRORS in " + name
                        + " (xdef: Test" + id +"a.xdef" +"):\n" + reporter.printToString();
                } else {
                    KXmlUtils.compareElements(e, f.getAbsolutePath(), true, reporter);
                    if (reporter.errorWarnings()) {
                        result += (result.isEmpty() ? "" : "\n") + "ERROR: result differs " + name + '\n' +
                            KXmlUtils.nodeToString(e, true) + '\n' +
                            KXmlUtils.nodeToString(KXmlUtils.parseXml(f), true);
                    } else {
                        o = XonUtils.xmlToXon(KXmlUtils.nodeToString(e, true));
                        if (!XonUtils.xonEqual(json, o)) {
                            result += (result.isEmpty() ? "" : "\n") + "ERROR conversion XML to JSON: " + name
                                + "\n" + XonUtils.toJsonString(json, true)
                                + '\n' + XonUtils.toJsonString(o, true)
                                + '\n' + KXmlUtils.nodeToString(e, true);
                        }
                    }
                }
            } catch (SRuntimeException ex) {
                result += (result.isEmpty() ? "" : "\n") + "Error " + name + "\n" + printThrowable(ex);
            }
            // parseJSON with jparse
            try {
                o = xp.createXDDocument("Test"+id).jvalidate(json, null);
                if (!XonUtils.xonEqual(json, XonUtils.xonToJson(o))) {
                    result += (result.isEmpty() ? "" : "\n") + "Error jparse Test" + id + "\n"
                        + XonUtils.toJsonString(json) + "\n"
                        + XonUtils.toJsonString(XonUtils.xonToJson(o)) + "\n";
                }
            } catch (SRuntimeException ex) {
                result += (result.isEmpty() ? "" : "\n") + "Incorrect jparse Test"+id+".json";
                fail(ex);
                continue;
            }
            // parseJSON with X-component
            try {
                xc = xp.createXDDocument("Test"+id).xparseXComponent(f,
                    Class.forName("test.common.json.component.Test"+id), null);
                assertEq("", chkCompoinentSerializable(xc));
                reporter.clear();
                o = XonUtils.xonToJson(xc.toXon());
                if (!XonUtils.xonEqual(json, o)) {
                    result += "Error xc.toXon(): " + name + "\n" + json + "\n" + o + "\n";
                }
                e = xc.toXml();
                KXmlUtils.compareElements(e, f.getAbsolutePath(),true,reporter);
                if (reporter.errorWarnings()) {
                    result += (result.isEmpty() ? "" : "\n") + "Error 1 xc.toXml(): " + name + "\n"
                        + KXmlUtils.nodeToString(KXmlUtils.parseXml(f).getDocumentElement(), true)
                        + "\n"+ KXmlUtils.nodeToString(e, true);
                }
            } catch (ClassNotFoundException | SRuntimeException ex) {
                result += (result.isEmpty() ? "" : "\n")
                    + "Error X-component " + name + "\n" + printThrowable(ex);
            }
            // Test X-component.
            try {
                xc = xp.createXDDocument("Test"+id).xparseXComponent(f,
                    Class.forName("test.common.json.component.Test"+id), null);
                assertEq("", chkCompoinentSerializable(xc));
                reporter.clear();
                e = xc.toXml();
                KXmlUtils.compareElements(e, f.getAbsolutePath(), true, reporter);
                if (reporter.errorWarnings()) {
                    result += (result.isEmpty() ? "" : "\n") + "Error X-component " + name +"\n"
                        + reporter.printToString() + "\n"+ KXmlUtils.nodeToString(e, true);
                }
                o = XonUtils.xmlToXon(xc.toXml());
                if (!XonUtils.xonEqual(json, XonUtils.xonToJson(o))) { ///S
                    result += (result.isEmpty() ? "" : "\n") + "Error X-component toJsjon " + id + "\n"
                        + XonUtils.toJsonString(json) + "\n" + XonUtils.toJsonString(o) + "\n";
                }
                // test to parse XON from X-component
                xc = xp.createXDDocument("Test"+id).jparseXComponent(xc.toXon(),
                    Class.forName("test.common.json.component.Test"+id), null);
                assertEq("", chkCompoinentSerializable(xc));
                if (!XonUtils.xonEqual(json, XonUtils.xonToJson(xc.toXon()))) {
                    result += (result.isEmpty() ? "" : "\n") + "Error X-component toJsjon " + id + "\n"
                        + XonUtils.toJsonString(json) + "\n" + XonUtils.toJsonString(o) + "\n";
                }
            } catch (ClassNotFoundException | SRuntimeException ex) {
                result += (result.isEmpty()?"":"\n")+ "Error X-component " + id + "\n" + printThrowable(ex);
            }
        }
        // Test error reporting
        for (File f: SUtils.getFileGroup(_dataDir+"Test"+id+"*.jerr")) {
            try {
                reporter.clear();
                xp.createXDDocument("Test"+id).jparse(f, reporter);
                if (!reporter.errorWarnings()) {
                    result += (result.isEmpty() ? "" : "\n") + "Error not reported: "+f.getName();
                }
            } catch (SRuntimeException ex) {
                result += (result.isEmpty() ? "" : "\n")+"Error jerr: "+f.getName()+"\n"+printThrowable(ex);
            }
        }
        return result;
    }

    /** Get XComponent with parsed data.
     * @param xp compiled XDPool from generated Xdefinitions.
     * @param test identifier test file.
     * @param x file number.
     * @return XComponent with parsed data.
     */
    private XComponent getXComponent(final XDPool xp,
        final String test,
        final int x) {
        try {
            File f = new File(_tempDir + test +	(x > 0 ? "_"+x : "") + "a.xml");
            return xp.createXDDocument(test).xparseXComponent(f,
                Class.forName("test.common.json.component." + test), null);
        } catch (ClassNotFoundException | SRuntimeException ex) {
            throw new RuntimeException("XComponent not found: " + test);
        }
    }

    /** Test JSON data with given encoding.
     * @param xp compiled XDPool
     * @param xon string with XON data.
     * @param encoding encoding name
     * @param genEncoding if true the %encoding directive is generated.
     * @return empty string or errors description.
     */
    private String testEncoding(final XDPool xp,
        final String xon,
        final String encoding,
        final boolean genEncoding) {
        String result = "";
        try {
            XDDocument xd = xp.createXDDocument();
            File f = new File(clearTempDir(), "Test201" + encoding + ".xon");
            try (Writer wr = new OutputStreamWriter(
                new FileOutputStream(f), encoding)) {
                if (genEncoding) {
                    wr.write("%encoding = \"" + encoding + "\"\n");
                }
                wr.write(xon);
            }
            Object x = XonUtils.parseXON(f);
            assertEq("ĚŠČŘŽÝÁÍÉÚŮĹ",
                ((Map) ((Map) x).get("ěščřžýáíéúůĺ %u@#$")).get("é"));
            ArrayReporter reporter = new ArrayReporter();
            Object o = xd.jparse(f, reporter);
            if (reporter.errorWarnings()) {
                result += reporter.printToString();
                reporter.clear();
            }
            if (!XonUtils.xonEqual(x, o)) {
                result += "\n *** 1" + XonUtils.toXonString(x, true)
                    + "\n " + XonUtils.toXonString(o, true);
            }
            XComponent xc = xd.jparseXComponent(f, null, reporter);
            if (reporter.errorWarnings()) {
                result +=  "\n *** 2" + reporter.printToString();
                reporter.clear();
            }
            if (!XonUtils.xonEqual(x, xc.toXon())) {
                result += "\n *** 3" + XonUtils.toXonString(x, true)
                    + "\n " + XonUtils.toXonString(xc.toXon(), true);
            }
            return result;
        } catch (IOException | SRuntimeException ex) {
            return printThrowable(ex);
        }
    }

    /** Run test and print error information. */
    @Override
    @SuppressWarnings("unchecked")
    public void test() {
        if (!_xdNS.startsWith("http://www.xdef.org/xdef/4.")) {
            return;
        }
        String fname, ini, json, xdef, xml;
        Object x;
        List list;
        Element el;
        ArrayReporter reporter = new ArrayReporter();
        StringWriter swr;
        XComponent xc, y;
        XDDocument xd;
        XDPool xp;
        try {
            xp = genAll("Test*");// Generate Xdefinitons, X-components, sources
//			xp = genAll("Test064");
            for (File f: _jfiles) { // run all tests
                String s = testJdef(xp, getId(f));
                assertTrue(s.isEmpty(), s );
            }
        } catch (RuntimeException ex) {
            fail(ex); // should not happen!!!
            return;
        }
        // Test X-components
        String xon = XDConstants.XON_NS_PREFIX + "$";
        try {
            fname = "Test008";
            xc = getXComponent(xp, fname, 0);
            assertEq("", chkCompoinentSerializable(xc));
            y = (XComponent) XComponentUtil.get(xc, xon+XonNames.X_VALUE);
            assertEq(1, XComponentUtil.get(y, XonNames.X_VALATTR));
            XComponentUtil.set(y, XonNames.X_VALATTR, 3);
            assertEq(3, XComponentUtil.get(y, XonNames.X_VALATTR));
            XComponentUtil.set(y, XonNames.X_VALATTR, null);
            assertNull(XComponentUtil.get(y, XonNames.X_VALATTR));
            fname = "Test020";
            xc = getXComponent(xp, fname, 0);
            assertEq("", chkCompoinentSerializable(xc));
            assertEq("abc", XComponentUtil.get(xc, "$a"));
            XComponentUtil.set(xc, "$a", null);
            assertTrue(XComponentUtil.get(xc, "$a")==null);
            xc = getXComponent(xp, fname, 1);
            assertEq("", chkCompoinentSerializable(xc));
            assertEq(123, XComponentUtil.get(xc, "$a"));
            XComponentUtil.set(xc, "$a", "");
            assertEq("", XComponentUtil.get(xc, "$a"));
            xc = getXComponent(xp, fname, 2);
            assertEq("", chkCompoinentSerializable(xc));
            assertEq(false, XComponentUtil.get(xc, "$a"));
            xc = getXComponent(xp, fname, 3);
            assertEq("", chkCompoinentSerializable(xc));
            assertTrue(XComponentUtil.get(xc, "$a")!=null);
            fname = "Test021";
            xc = getXComponent(xp, fname, 0);
            assertEq("", chkCompoinentSerializable(xc));
            y = (XComponent) XComponentUtil.get(xc, xon+XonNames.X_VALUE);
            assertEq("abc", XComponentUtil.get(y, XonNames.X_VALATTR));
            xc = getXComponent(xp, fname, 1);
            assertEq("", chkCompoinentSerializable(xc));
            y = (XComponent) XComponentUtil.get(xc, xon+XonNames.X_VALUE);
            assertEq(123, XComponentUtil.get(y, XonNames.X_VALATTR));
            XComponentUtil.set(y, XonNames.X_VALATTR, "");
            assertEq("", XComponentUtil.get(y, XonNames.X_VALATTR));
            XComponentUtil.set(y, XonNames.X_VALATTR, " a    b \n ");
            assertEq(" a    b \n ", XComponentUtil.get(y, XonNames.X_VALATTR));
            xc = getXComponent(xp, fname, 2);
            assertEq("", chkCompoinentSerializable(xc));
            y = (XComponent) XComponentUtil.get(xc, xon+XonNames.X_VALUE);
            assertEq(false, XComponentUtil.get(y, XonNames.X_VALATTR));
            xc = getXComponent(xp, fname, 3);
            assertEq("", chkCompoinentSerializable(xc));
            y = (XComponent) XComponentUtil.get(xc, xon+XonNames.X_VALUE);
            assertTrue(XComponentUtil.get(y, XonNames.X_VALATTR) != null);
            xc = getXComponent(xp, fname, 4);
            assertEq("", chkCompoinentSerializable(xc));
            assertNull(XComponentUtil.get(xc, xon+XonNames.X_VALUE));
            fname = "Test025";
            xc = getXComponent(xp, fname, 0);
            assertEq("", chkCompoinentSerializable(xc));
            y = (XComponent) XComponentUtil.get(xc, xon+XonNames.X_VALUE);
            assertEq("null", XComponentUtil.get(y, XonNames.X_VALATTR).toString());
            y = (XComponent) XComponentUtil.get(xc, xon+XonNames.X_VALUE+"_1");
            assertEq(12, XComponentUtil.get(y, XonNames.X_VALATTR));
            y = (XComponent) XComponentUtil.get(xc, xon+XonNames.X_VALUE+"_2");
            assertEq("\" a b \"", XComponentUtil.get(y, XonNames.X_VALATTR));
            xc = getXComponent(xp, fname, 1);
            assertEq("", chkCompoinentSerializable(xc));
            y = (XComponent) XComponentUtil.get(xc, xon+XonNames.X_VALUE);
            assertEq("null", XComponentUtil.get(y, XonNames.X_VALATTR).toString());
            assertNull(XComponentUtil.get(xc, xon+XonNames.X_VALUE+"_1"));
            assertNull(XComponentUtil.get(xc, xon+XonNames.X_VALUE+"_2"));
            fname = "Test026";
            xc = getXComponent(xp, fname, 0);
            assertEq("", chkCompoinentSerializable(xc));
            assertEq("", chkCompoinentSerializable(xc));
            list = (List) XComponentUtil.getx(xc, "listOf" + xon+XonNames.X_VALUE);
            assertEq(2, list.size());
            y = (XComponent) list.get(0);
            assertEq("null", XComponentUtil.get(y, XonNames.X_VALATTR).toString());
            list = (List) XComponentUtil.getx(xc, "listOf" + xon+XonNames.X_VALUE);
            y = (XComponent) list.get(1);
            assertEq("null", XComponentUtil.get(y, XonNames.X_VALATTR).toString());
            xc = getXComponent(xp, fname, 0);
            assertEq("", chkCompoinentSerializable(xc));
            x = XComponentUtil.getx(xc, "listOf"+xon+XonNames.X_VALUE + "_1");
            assertEq(2, ((List) x).size());
            y = (XComponent) ((List) x).get(0);
            assertEq(12, XComponentUtil.get(y, XonNames.X_VALATTR));
            x = XComponentUtil.getx(xc, "listOf"+xon+XonNames.X_VALUE + "_1");
            y = (XComponent) ((List) x).get(1);
            assertEq(13, XComponentUtil.get(y, XonNames.X_VALATTR));
        } catch (RuntimeException ex) {fail(ex);}
        // If no errors were reported delete all generated data.
        // Otherwise, leave them to be able to see the reason of errors.
        if (getFailCount() == 0) {
            clearTempDir(); // delete temporary files.
        }
        // Other tests
        try {
            xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name='Person' root='Person'>\n"+
"  <xd:json name=\"Person\">\n"+
"    { \"Person\": { \"Name\": \"jstring(1, 50);\",\n" +
"        \"Pay\": \"int(1000, 99999);\",\n" +
"        \"Birth date.\": \"date();\"\n" +
"      }\n" +
"    }\n" +
"  </xd:json>\n"+
"  <xd:component>\n"+
"    %class "+_package+".XonPerson %link Person#Person;\n"+
"  </xd:component>\n"+
"</xd:def>";
            genXComponent(xp = compile(xdef));
            json =
"{ \"Person\": {\n" +
"    \"Name\":\"Václav Novák\",\n" +
"    \"Pay\":12345,\n" +
"    \"Birth date.\":\"1980-11-07\"\n" +
"  }\n" +
"}";
            xd = xp.createXDDocument("Person");
            x = jparse(xd, json, reporter);
            assertNoErrorwarningsAndClear(reporter);
            xd = xp.createXDDocument("Person");
            xd.setXONContext(XonUtils.xonToJson(x));
            assertTrue(XonUtils.xonEqual(x, jcreate(xd, "Person", reporter)));
            assertNoErrorwarningsAndClear(reporter);
            xd = xp.createXDDocument("Person");
            xd.setXONContext(XonUtils.xonToJson(x));
            xc = xd.jcreateXComponent("Person", null, reporter);
            assertEq("", chkCompoinentSerializable(xc));
            assertNoErrorwarningsAndClear(reporter);
            assertTrue(XonUtils.xonEqual(x, xc.toXon()));
            xd = xp.createXDDocument("Person");
            xd.setXONContext(XonUtils.xonToJson(x));
            xc = xd.jcreateXComponent("Person", null, reporter);
            assertEq("", chkCompoinentSerializable(xc));
            assertNoErrorwarningsAndClear(reporter);
            assertTrue(XonUtils.xonEqual(x, xc.toXon()));
            xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='Person_list'>\n"+
"  <xd:json name=\"Person_list\">\n"+
"    { \"Seznam\": \n"+
"      [\n"+
"        { \"%script\": \"occurs 1..*;\",\n"+
"          \"Person\": { \"Name\": \"string(1, 50)\",\n" +
"             \"Pay\": \"int(1000, 99999)\",\n" +
"             \"Birth date.\": \"date()\"\n" +
"          }\n" +
"       }\n"+
"      ]\n"+
"    }\n"+
"  </xd:json>\n"+
"</xd:def>";
            xp = compile(xdef);
            xd = xp.createXDDocument("");
            json =
"{\"Seznam\":\n"+
" [\n" +
"    { \"Person\":{\n" +
"        \"Name\":\"Václav Novák\",\n" +
"        \"Pay\":12345,\n" +
"        \"Birth date.\":\"1980-11-07\"\n" +
"      }\n" +
"    },\n" +
"    { \"Person\":{\n" +
"        \"Name\":\"Ivan Bílý\",\n" +
"        \"Pay\":23450,\n" +
"        \"Birth date.\":\"1977-01-17\"\n" +
"      }\n" +
"    },\n" +
"    { \"Person\":{\n" +
"        \"Name\":\"Karel Kuchta\",\n" +
"        \"Pay\":1340,\n" +
"        \"Birth date.\":\"1995-10-06\"\n" +
"      }\n" +
"    }\n" +
"  ]\n" +
"}";
            x = jparse(xd, json, reporter);
            assertNoErrorwarningsAndClear(reporter);
            xd = xp.createXDDocument("");
            xd.setXONContext(x);
            assertTrue(XonUtils.xonEqual(x, jcreate(xd, "Person_list", reporter)));
            assertNoErrorwarningsAndClear(reporter);
            xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='Person_list'>\n"+
"  <xd:json name=\"Person_list\">\n"+
"    { \"Seznam\": \n"+
"      [\n"+
"        { \"%script\": \"occurs 1..*; ref Person\" }\n"+
"      ]\n"+
"    }\n"+
"  </xd:json>\n"+
"  <xd:json name=\"Person\">\n"+
"    { \"Person\": { \"Name\": \"string(1, 50)\",\n" +
"        \"Pay\": \"int(1000, 99999)\",\n" +
"        \"Birth date.\": \"date()\"\n" +
"      }\n" +
"    }\n" +
"  </xd:json>\n"+
"</xd:def>";
            xp = compile(xdef);
            xd = xp.createXDDocument("");
            json =
"{\"Seznam\":\n"+
" [\n" +
"    { \"Person\":{\n" +
"        \"Name\":\"Václav Novák\",\n" +
"        \"Pay\":12345,\n" +
"        \"Birth date.\":\"1980-11-07\"\n" +
"      }\n" +
"    },\n" +
"    { \"Person\":{\n" +
"        \"Name\":\"Ivan Bílý\",\n" +
"        \"Pay\":23450,\n" +
"        \"Birth date.\":\"1977-01-17\"\n" +
"      }\n" +
"    },\n" +
"    { \"Person\":{\n" +
"        \"Name\":\"Karel Kuchta\",\n" +
"        \"Pay\":1340,\n" +
"        \"Birth date.\":\"1995-10-06\"\n" +
"      }\n" +
"    }\n" +
"  ]\n" +
"}";
            x = jparse(xd, json, reporter);
            assertNoErrorwarningsAndClear(reporter);
            xd = xp.createXDDocument("");
            xd.setXONContext(x);
            assertTrue(XonUtils.xonEqual(x, jcreate(xd, "Person_list", reporter)));
            assertNoErrorwarningsAndClear(reporter);
            xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='Matrix'>\n"+
"  <xd:json name=\"Matrix\">\n"+
"    [\n" +
"      [ \"%script:occurs 3;\",\n" +
"        \"occurs 3; float()\"\n" +
"      ]\n" +
"    ]\n"+
"  </xd:json>\n"+
"</xd:def>";
            xp = compile(xdef);
            xd = xp.createXDDocument("");
            json =
"[\n" +
"  [123.4, -56, 1],\n" +
"  [0, 0, 1],\n" +
"  [-5, 33, 0.5]\n" +
"]";
            x = jparse(xd, json, reporter);
            assertNoErrorwarningsAndClear(reporter);
            xd = xp.createXDDocument("");
            xd.setXONContext(x);
            assertTrue(XonUtils.xonEqual(x, jcreate(xd, "Matrix", reporter)));
            assertNoErrorwarningsAndClear(reporter);
            xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='Skladby'>\n"+
"  <xd:json name=\"Skladby\">\n"+
"    [\n" +
"      { \"%script\": \"occurs 1..*;\",\n" +
"         \"Name\": \"string()\",\n" +
"         \"Style\": [ \"%oneOf\",\n" +
"           \"string()\",\n" +
"           [ \"occurs 2..* string()\" ]\n" +
"         ]\n" +
"      }\n" +
"    ]\n" +
"  </xd:json>\n"+
"</xd:def>";
            xp = compile(xdef);
            xd = xp.createXDDocument();
            json =
"[\n" +
"  { \"Name\": \"Beethoven, Symfonie No 5\",\n" +
"    \"Style\": \"Classic\"\n" +
"  },\n" +
"  { \"Name\": \"A Day at the Races\",\n" +
"    \"Style\": [\"jazz\", \"pop\" ]\n" +
"  }\n" +
"]";
            x = jparse(xd, json, reporter);
            assertNoErrorwarningsAndClear(reporter);
            xd = xp.createXDDocument("");
            xd.setXONContext(x);
            assertTrue(XonUtils.xonEqual(x, jcreate(xd, "Skladby", reporter)));
            assertNoErrorwarningsAndClear(reporter);
            xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='json'>\n"+
"  <xd:json name='json'>\n"+
"    {\"\": \"optional jstring()\"}\n" +
"  </xd:json>\n"+
"</xd:def>";
            xp = compile(xdef);
            json = "{\"\":\"aaa\"}";
            jparse(xp, "", json, reporter);
            assertNoErrorwarningsAndClear(reporter);
            json = "{}";
            jparse(xp, "", json, reporter);
            assertNoErrorwarningsAndClear(reporter);
            xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='A|B|json'>\n"+
"  <xd:json name='json'>\n"+
"    [{\"a\":\"boolean\"},\"string()\",\"int()\"]\n" +
"  </xd:json>\n"+
"  <xd:json name='B'>\n"+
"    {\"a\":\"int\"}\n"+
"  </xd:json>\n"+
"  <A/>\n"+
"</xd:def>";
            xp = compile(xdef);
            xml = "<A/>";
            assertEq(xml, parse(xp, "", xml, reporter));
            assertNoErrorwarningsAndClear(reporter);
            json = "[{\"a\":true},\"x\",-1]";
            x = jparse(xp, "", json, reporter);
            assertNoErrorwarningsAndClear(reporter);
            assertTrue(XonUtils.xonEqual(XonUtils.parseJSON(json), x), XonUtils.toJsonString(x, true));
            el = XonUtils.xonToXmlW(x);
            parse(xp, "", el, reporter);
            assertNoErrorwarningsAndClear(reporter);
            json = "{\"a\":1}";
            x = jparse(xp, "", json, reporter);
            assertNoErrorwarningsAndClear(reporter);
            assertTrue(XonUtils.xonEqual(XonUtils.parseJSON(json), x), XonUtils.toJsonString(x, true));
            el = XonUtils.xonToXmlW(x);
            parse(xp, "", el, reporter);
            assertNoErrorwarningsAndClear(reporter);
            xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='B'>\n"+
"  <xd:json name='B'> [\"%script:init out('a');finally out('b')\",\"int();finally out('x')\"] </xd:json>\n"+
"</xd:def>\n";
            xd = compile(xdef).createXDDocument();
            swr = new StringWriter();
            xd.setStdOut(swr);
            json = "[123]";
            x = jparse(xd, json, reporter);
            assertNoErrorwarningsAndClear(reporter);
            assertTrue(XonUtils.xonEqual(XonUtils.parseJSON(json), x));
            assertEq("axb", swr.toString());
            xdef =
"<xd:def xmlns:xd=\""+_xdNS+"\" root=\"root\" >\n" +
"  <xd:json xd:name='root'> \"jvalue();\" </xd:json>\n"+
"</xd:def>";
            xp = compile(xdef);
            x = 123;
            assertTrue(XonUtils.xonEqual(x, jparse(xp, "", (Object) x, reporter)));
            assertTrue(XonUtils.xonEqual(x = -0L, jparse(xp, "", (Object) x, reporter)));
            assertTrue(XonUtils.xonEqual(x = -123.45e-1, jparse(xp, "", (Object) x, reporter)));
            assertTrue(XonUtils.xonEqual(x = -123.45e-1D, jparse(xp, "", (Object) x, reporter)));
            x = new BigInteger("123456789012345678901234567890");
            assertTrue(XonUtils.xonEqual(x, jparse(xp, "", (Object) x, reporter)));
            x = new BigDecimal("-123456789012345678901234567890.1e-2");
            assertTrue(XonUtils.xonEqual(x, jparse(xp, "", (Object) x, reporter)));
            assertTrue(XonUtils.xonEqual(x = true, jparse(xp, "", (Object) x, reporter)));
            assertTrue(XonUtils.xonEqual(x = null, jparse(xp, "", (Object) x, reporter)));
            assertTrue(XonUtils.xonEqual(false, jparse(xp, "", (Object) "false", reporter)));
            assertTrue(XonUtils.xonEqual(-123, jparse(xp, "", (Object) "-123", reporter)));
            assertTrue(XonUtils.xonEqual(null, jparse(xp, "", (Object) "null", reporter)));
            assertTrue(XonUtils.xonEqual("", jparse(xp, "", (Object) "\"\"", reporter)));
            assertTrue(XonUtils.xonEqual("abc", jparse(xp, "", (Object) "\"abc\"", reporter)));
            assertTrue(XonUtils.xonEqual("ab\nc", jparse(xp, "", (Object) "\"ab\nc\"", reporter)));
            assertTrue(XonUtils.xonEqual(" ab tc ", jparse(xp, "", (Object) "\" ab tc \"", reporter)));
            assertTrue(XonUtils.xonEqual("ab\n\tc", jparse(xp, "", (Object) "\"ab\\n\\tc\"", reporter)));
            xdef =
"<xd:def xmlns:xd=\""+_xdNS+"\" name=\"X\" root=\"a\">\n"+
" <xd:ini name='a'>\n"+
"   A=?string(); finally out(\"A\");\n" +
"   B=int(); finally out(\"B\");\n" +
"   C=date(); finally out(\"C\");\n" +
"   D=decimal(); finally out(\"D\");\n" +
"   [E] optional; finally out(\"[E]\");\n" +
"     x = ?int(); finally out(\"x\");\n" +
"   [F]finally out(\"[F]\");\n" +
" </xd:ini>\n"+
"</xd:def>";
            xp = compile(xdef);
            xd = xp.createXDDocument();
            swr = new StringWriter();
            xd.setStdOut(XDFactory.createXDOutput(swr, false));
            ini = "#a\nA=a\n\n B= 1\n C=2121-10-19\nD =2.1\n[E]\nx=3\n[F]\n#b";
            xd.iparse(ini, reporter);
            assertNoErrorwarningsAndClear(reporter);
            assertEq("ABCDx[E][F]", swr.toString());
            swr = new StringWriter();
            xd.setStdOut(XDFactory.createXDOutput(swr, false));
            xd.iparse(ini, reporter);
            assertNoErrorwarningsAndClear(reporter);
            assertEq("ABCDx[E][F]", swr.toString());
            swr = new StringWriter();
            xd.setStdOut(XDFactory.createXDOutput(swr, false));
            ini = "#\n B = 1 \n C=2121-10-19\n D=2.121\n [E] \n[F]\n#";
            xd.iparse(ini, reporter);
            assertNoErrorwarningsAndClear(reporter);
            assertEq("BCD[E][F]", swr.toString());
            swr = new StringWriter();
            xd.setStdOut(XDFactory.createXDOutput(swr, false));
            xd.iparse(ini, reporter);
            assertNoErrorwarningsAndClear(reporter);
            assertEq("BCD[E][F]", swr.toString());
            swr = new StringWriter();
            xd.setStdOut(XDFactory.createXDOutput(swr, false));
            xd.iparse(ini, reporter);
            assertNoErrorwarningsAndClear(reporter);
            assertEq("BCD[E][F]", swr.toString());
            swr = new StringWriter();
            xd.setStdOut(XDFactory.createXDOutput(swr, false));
            xd.iparse(ini, reporter);
            assertNoErrorwarningsAndClear(reporter);
            assertEq("BCD[E][F]", swr.toString());
            ini = "\n B = 1 \n C=2121-10-19\n D=2.121\n[F]";
            swr = new StringWriter();
            xd.setStdOut(XDFactory.createXDOutput(swr, false));
            xd.iparse(ini, reporter);
            assertNoErrorwarningsAndClear(reporter);
            assertEq("BCD[F]", swr.toString());
            swr = new StringWriter();
            xd.setStdOut(XDFactory.createXDOutput(swr, false));
            xd.iparse(ini, reporter);
            assertNoErrorwarningsAndClear(reporter);
            assertEq("BCD[F]", swr.toString());
            swr = new StringWriter();
            xd.setStdOut(XDFactory.createXDOutput(swr, false));
            xd.iparse(ini, reporter);
            assertNoErrorwarningsAndClear(reporter);
            assertEq("BCD[F]", swr.toString());
            xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name='TestINI' root='a'>\n"+
" <xd:ini name='a'>\n"+
"   A=?string()\n" +
"   B=int()\n" +
"   C=date()\n" +
"   D=decimal()\n" +
"   [E-F.G] ? /*this is optional section*/\n" +
"     x = ?int()\n" +
"   [ F ] /*this is required section*/\n" +
" </xd:ini>\n"+
" <xd:component>\n" +
"  %class "+_package+".TestINI %link TestINI#a" + ";\n" +
" </xd:component>\n"+
"</xd:def>";
            ini = "A=a\n B=1\n C=2121-10-19\n D=2.34\n[ E-F.G ]\nx=123\n[F]";
            genXComponent(xp = compile(xdef));
            xd = xp.createXDDocument("TestINI");
            xc = xd.iparseXComponent(ini, null, reporter);
            assertNoErrorwarningsAndClear(reporter);
            assertEq("", chkCompoinentSerializable(xc));
            assertEq("a", XComponentUtil.get(xc, "$A"));
            assertEq(1, XComponentUtil.get(xc, "$B"));
            assertEq(new SDatetime("2121-10-19"), XComponentUtil.get(xc,"$C"));
            assertEq(0, new BigDecimal("2.34").compareTo((BigDecimal) XComponentUtil.get(xc, "$D")));
            XComponentUtil.set(xc, "$A", "b");
            assertEq("b", XComponentUtil.get(xc, "$A"));
            ini = "A=a\n B=1\n C=2121-10-19\n D=2.34\n[F]";
            xc = xd.iparseXComponent(ini, null, reporter);
            assertNoErrorwarningsAndClear(reporter);
            assertEq("", chkCompoinentSerializable(xc));
            assertEq("a", XComponentUtil.get(xc, "$A"));
            assertEq(1, XComponentUtil.get(xc, "$B"));
            assertEq(new SDatetime("2121-10-19"), XComponentUtil.get(xc, "$C"));
            assertEq(0, new BigDecimal("2.34").compareTo((BigDecimal) XComponentUtil.get(xc, "$D")));
            XComponentUtil.set(xc,"$A", "b");
            assertEq("b", XComponentUtil.get(xc, "$A"));
            xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='a'>\n"+
"  <xd:json name='a'>\n" +
"    [\n" +
"      {\n" +
"        a : \"? short()\",\n" +
"        i : [],\n" +
"        Towns : [\n" +
"          \"* gps()\"\n" +
"        ],\n" +
"        j : \"? char()\"\n" +
"      },\n" +
"      \"base64Binary()\",\n" +
"      \"base64Binary()\",\n" +
"      \"base64Binary()\",\n" +
"      \"price()\",\n" +
"      \"currency()\",\n" +
"      \"* ipAddr()\"\n" +
"    ]\n" +
"  </xd:json>\n" +
"  <xd:component> %class "+_package+".X_on %link #a; </xd:component>\n"+
"</xd:def>";
            genXComponent(xp = compile(xdef));
            json =
"# Start of XON example\n" +
"[ #***** Array *****\n" +
"  { #***** Map *****\n" +
"    a : 1s,                          # Short\n" +
"    i:[],                            # empty array\n" +
"    Towns : [ # array with GPS locations of towns\n" +
"      g(48.2, 16.37, 151, Wien),\n" +
"      g(51.52, -0.09, 0, London),\n" +
"      g(50.08, 14.42, 399, \"Prague old town\")\n" +
"    ],\n" +
"    j : c\"a\",                        # Character\n" +
"  }, /**** end of map ****/\n" +
"  b(),                               /* byte array (base64) */\n"+
"  b(AA==),                           /* byte array (base64) */\n"+
"  b(true),                           /* byte array (base64) */\n" +
"  p(123.45 CZK),                     /* price */ \n" +
"  C(USD),                            /* currency */\n" +
"  /1080:0:0:0:8:800:200C:417A        /* inetAddr (IPv6)  */\n" +
"] /**** end of array ****/\n" +
"# End of XON example";
            xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
            assertNoErrorwarningsAndClear(reporter);
            assertEq("", chkCompoinentSerializable(xc));
            assertTrue(XonUtils.xonEqual(XonUtils.parseXON(json), xc.toXon()));
            list = (List) XComponentUtil.get(xc, "$"+XonNames.X_VALUE+"_5");
            try {
                list.add(InetAddress.getByName("111.22.33.1"));
            } catch (UnknownHostException ex) {}
            XComponentUtil.set(xc, XonNames.X_VALUE+"_5", list);
            assertEq(2, ((List) XComponentUtil.get(xc, "$"+XonNames.X_VALUE+"_5")).size());
            assertTrue(((XComponent) XComponentUtil.get(xc, "jx$"+XonNames.X_MAP)).toXon() instanceof Map);
            assertTrue(((List) ((XComponent) XComponentUtil.get(
                (XComponent) XComponentUtil.get(xc, "jx$"+XonNames.X_MAP),
                "jx$"+XonNames.X_ARRAY)).toXon()).isEmpty());
            assertEq(3,
                ((List) ((XComponent) XComponentUtil.get((XComponent) XComponentUtil.get(
                    xc, "jx$"+XonNames.X_MAP),
                "jx$"+XonNames.X_ARRAY+"_1")).toXon()).size());
            xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='z'>\n" +
"  <xd:json name='z'> [\"* int();\"] </xd:json>\n" +
"  <xd:component>%class "+_package+".X_int %link #z;</xd:component>\n" +
"</xd:def>";
            xp = compile(xdef);
            genXComponent(xp, clearTempDir());
            json = "[1,2,3]";
            xd = xp.createXDDocument("");
            x = XonUtils.parseJSON(json);
            assertTrue(XonUtils.xonEqual(x, xd.jparse(json, reporter)));
            xc = xd.jparseXComponent(json, null, reporter);
            assertEq("", chkCompoinentSerializable(xc));
            assertTrue(XonUtils.xonEqual(x, XonUtils.xmlToXon(xc.toXml())),
                XonUtils.toJsonString(XonUtils.xmlToXon(xc.toXml()), true));
            assertNoErrorwarningsAndClear(reporter);
            assertEq(1, ((List) XComponentUtil.get(xc, "$item")).get(0));
            assertEq(3, ((List) XComponentUtil.get(xc, "$item")).size());
            xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='z'>\n" +
"  <xd:json name='z'> [\"* jvalue();\"] </xd:json>\n" +
"  <xd:component>%class "+_package+".X_jval %link #z;</xd:component>\n" +
"</xd:def>";
            xp = compile(xdef);
            genXComponent(xp, clearTempDir());
            json = "[1, \"a b\", null]";
            xd = xp.createXDDocument("");
            x = XonUtils.parseJSON(json);
            assertTrue(XonUtils.xonEqual(x, xd.jparse(json, reporter)));
            xc = xd.jparseXComponent(json, null, reporter);
            assertNoErrorwarningsAndClear(reporter);
            assertEq("", chkCompoinentSerializable(xc));
            assertEq(3, ((List) XComponentUtil.get(xc, "$item")).size());
            assertEq(1, ((List) XComponentUtil.get(xc, "$item")).get(0));
            assertEq("a b", ((List) XComponentUtil.get(xc, "$item")).get(1));
            assertNull(((List)XComponentUtil.get(xc, "$item")).get(2));
            xdef = // test data with different encodings
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" root=\"a\" >\n" +
"  <xd:json name=\"a\">\n" +
"      # test encodings\n" +
"      { \"ěščřžýáíéúůĺ %u@#$\": {\n" +
"          é: \"string()\",\n" +
"          \"§&amp;&lt;&gt;\": \"date()\",\n" +
"          \"%\": [ \"*; string()\", { \"čřé\": \"string()\" } ]\n" +
"        }\n" +
"      }\n" +
"  </xd:json>\n" +
"  <xd:component>\n"+
"    %class "+_package+".TestXonEncoding %link #a;\n"+
"  </xd:component>\n"+
"</xd:def>";
            genXComponent(xp = compile(xdef));
            json =
"{ \"ěščřžýáíéúůĺ %u@#$\" : {\n" +
"     \"é\" : \"ĚŠČŘŽÝÁÍÉÚŮĹ\",\n" +
"    \"§&<>\" : d1990-01-01,\n" +
"    \"%\" : [\"Ščř\", \"ŠŮÚ\", {čřé : \"%§\"}]\n" +
"  }\n" +
"}";
            assertEq("", testEncoding(xp, json, "windows-1250", true));
            assertEq("", testEncoding(xp, json, "UTF-8", true));
            assertEq("", testEncoding(xp, json, "UTF-8", false)); // authomatic
            assertEq("", testEncoding(xp, json, "UTF-16", true));
            assertEq("", testEncoding(xp, json,"UTF-16", false)); // authomatic
            assertEq("", testEncoding(xp, json, "UTF-16LE", true));
            assertEq("", testEncoding(xp, json, "UTF-16LE",false)); //authomatic
            assertEq("", testEncoding(xp, json, "UTF-16BE", true));
            assertEq("", testEncoding(xp, json,"UTF-16BE", false));// authomatic
            assertEq("", testEncoding(xp, json, "UTF-32", true));
            assertEq("", testEncoding(xp, json,"UTF-32", false)); // authomatic
            assertEq("", testEncoding(xp, json, "UTF-32LE", true));
            assertEq("", testEncoding(xp, json, "UTF-32LE", false));//authomatic
            assertEq("", testEncoding(xp, json, "UTF-32BE", true));
            assertEq("", testEncoding(xp, json,"UTF-32BE", false));// authomatic
        } catch (RuntimeException ex) {fail(ex);}
        try { // test extension of map and correct reporting.
            xd = compile(
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"  <xd:json name='A'>{ \"address\": { \"%script\": \"optional; ref addr\", \"x\": \"int()\"} }</xd:json>\n" +
"  <xd:json name='addr'> { \"d\": \"string()\" } </xd:json>\n" +
"</xd:def>").createXDDocument();
            jparse(xd, "{ }", reporter);
            assertNoErrorsAndClear(reporter); //OK
            jparse(xd, "{ \"address\": { \"d\": \"cde\", \"x\": 1 } }", reporter);
            assertNoErrorsAndClear(reporter); //OK
            jparse(xd, "{ \"address\": { \"d\": \"dd\" } }", reporter);
            if (reporter.size() != 1 || !reporter.toString().contains("'x'")) {
                fail(reporter.toString()); // should be XDEF539: Required element 'x' is missing
            }
            jparse(xd, "{ \"address\": { \"x\": 1 } }", reporter);
            if (reporter.size() != 1 || !reporter.toString().contains("'d'")) {
                fail(reporter.toString()); // should be XDEF539: Required element 'd' is missing
            }
            jparse(xd, "{ \"address\": { } }", reporter);
            if (reporter.size() != 2) {
                fail(reporter.toString()); // should be XDEF539, elements 'd' and 'x' is missing
            }
        } catch (RuntimeException ex) {fail(ex);}
        try {
            xp = compile(
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" xd:name=\"X9\" xd:root=\"CaseFile\">\n" +
" <xd:json name=\"LossEventBase\">\n" +
"{  \"lossEventNumber\": \"integer();\", \"lossEventCauseCode\": \"int();\" }\n" +
"</xd:json>\n" +
" <xd:json name=\"LossEventX9\">\n" +
"{  \"%script\": \"ref LossEventBase;\",\n" +
"   \"disbursementRecords\": [ \"%script:optional; \", \"occurs 0..*; string();\" ]\n" +
"}\n" +
"</xd:json>\n" +
" <xd:json name=\"CaseFile\">\n" +
"{  \"%script\": \"ref LossEventX9;\",\n" +
"   \"isSpecialAttention\": \"optional; boolean();\",\n" +
"   \"originalCaseFileNumber\": \"optional; string();\"\n" +
"}\n" +
"</xd:json>\n" +
"</xd:def>");
            json =
"{ \"lossEventNumber\":1012,\n" +
"  \"lossEventCauseCode\":1,\n" +
"  \"disbursementRecords\":[ ],\n" +
"  \"isSpecialAttention\":false,\n" +
"  \"originalCaseFileNumber\":\"76\"\n" +
"}";
            jparse(xp, "X9", json, reporter);
            assertNoErrorsAndClear(reporter);
        } catch (RuntimeException ex) {fail(ex);}
        try {
            xd = compile(
"<xd:def xmlns:xd = \"http://www.xdef.org/xdef/4.2\" xd:root = \"SynPLscript\">\n" +
"  <xd:json name = \"SynPLscript\"> \n" +
"    { \"Statuses\": [\n" +
"        {  \"Status\": \"string()\",\n" +
"           \"Events\": [\n" +
"              {\"%script\": \"+\",\n" +
"                 \"Event\": \"string()\",\n" +
"                 \"UserRoleAny\": [\"1..* string()\"]\n" +
"              }\n" +
"           ]\n" +
"        }\n" +
"      ]\n" +
"    }\n" +
"  </xd:json>\n" +
"</xd:def>").createXDDocument();
            xd.jparse(
"{ \"Statuses\": [\n" +
"    { \"Status\": \"INIT\",\n" +
"      \"Events\": [{\"Event\": \"Start\", \"UserRoleAny\": [\"Reporter\", \"PM\"] }]\n" +
"    },\n" +
"    {\"Status\": \"ANY\",\n" +
"      \"Events\": [\n" +
"        {\"Event\": \"TimeOver_\", \"UserRoleAny\": [\"#WF\"]},\n" +
"        {\"Event\": \"TaskRole\", \"UserRoleAny\": [\"Reporter\",\"PM\"]}\n" +
"      ]\n" +
"    }\n" +
"  ]\n" +
"}", reporter);
            assertErrorsAndClear(reporter); //E XDEF507: Not allowed item in array
        } catch (RuntimeException ex) {fail(ex);}
        try { // test reference to map
            xp = XDFactory.compileXD(null,
"<xd:collection xmlns:xd='"+_xdNS+"'>\n" +
"<xd:def name='A' root='A'>\n" +
"  <xd:json name = \"A\">\n" +
"    {\"A\":\n" +
"      { \"B\": { \"%script\": \"ref B#B\", \"x\": \"?; string()\" } }\n" +
"    }\n" +
"  </xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".Mates_A0 %link A; %interface "+_package+".Mates_A0_I %link A;\n" +
"  </xd:component>\n" +
"</xd:def>\n" +
"<xd:def name='B' root='B'>\n" +
"  <xd:json name='B'>\n" +
"    { \"p\":  \"string()\" }\n" +
"  </xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".Mates_B0 %link B; %interface "+_package+".Mates_B0_I %link B;\n" +
"  </xd:component>\n" +
"</xd:def>\n" +
"</xd:collection>");
            genXComponent(xp);
            json = "{\"A\":{ \"B\": { \"p\": \"P\", \"x\": \"x\" } } }\n";
            xd = xp.createXDDocument("A");
            x = jparse(xd, json, reporter);
            assertNoErrorsAndClear(reporter);
            xc = xd.jparseXComponent(json, null, reporter);
            assertNoErrorsAndClear(reporter);
            assertTrue(XonUtils.xonEqual(x, xc.toXon()));
            json = "{\"A\":{ \"B\": { \"x\": \"x\", \"p\": \"P\" } } }\n";
            x = jparse(xd, json, reporter);
            assertNoErrorsAndClear(reporter);
            xc = xd.jparseXComponent(json, null, reporter);
            assertNoErrorsAndClear(reporter);
            assertTrue(XonUtils.xonEqual(x, xc.toXon()));
            xp = compile(
"<xd:collection xmlns:xd='"+_xdNS+"'>\n" +
"<xd:def name='A' root='A'>\n" +
"  <xd:json name = \"A\">\n" +
"    {\"A\":\n" +
"      { \"B\": { \"%script\": \"ref B#B\", \"x\": \"?; string()\" } }\n" +
"    }\n" +
"  </xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".Mates_AX %link A; %interface "+_package+".Mates_AX_I %link A;\n" +
"  </xd:component>\n" +
"</xd:def>\n" +
"<xd:def name='B' root='B'>\n" +
"  <xd:json name='B'>\n" +
"    { \"p\":  \"string()\", \"q\":  \"? string()\" }\n" +
"  </xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".Mates_BX %link B; %interface "+_package+".Mates_BX_I %link B;\n" +
"  </xd:component>\n" +
"</xd:def>\n" +
"</xd:collection>");
            genXComponent(xp);
            xd = xp.createXDDocument("A");
            json = "{\"A\": { \"B\": { \"p\": \"P\", \"q\": \"Q\", \"x\": \"x\" } } }\n";
            x = jparse(xd, json, reporter);
            assertNoErrorsAndClear(reporter);
            xc = xd.jparseXComponent(json, null, reporter);
            assertNoErrorsAndClear(reporter);
            assertTrue(XonUtils.xonEqual(x, xc.toXon()));
            json = "{\"A\": { \"B\": { \"x\": \"x\", \"q\": \"Q\", \"p\": \"P\", } } }\n";
            x = jparse(xd, json, reporter);
            assertNoErrorsAndClear(reporter);
            xc = xd.jparseXComponent(json, null, reporter);
            assertNoErrorsAndClear(reporter);
            assertTrue(XonUtils.xonEqual(x, xc.toXon()));
            xp = compile(
"<xd:collection xmlns:xd='"+_xdNS+"'>\n" +
"<xd:def name='A' root='A'>\n" +
"  <xd:json name = \"A\">\n" +
"    {\"A\":\n" +
"      { \"B\": { \"%script\": \"ref B#B\", \"x\": \"?; string()\", \"y\": \"?; string()\" } }\n" +
"    }\n" +
"  </xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".Mates_A %link A; %interface "+_package+".Mates_A_I %link A;\n" +
"  </xd:component>\n" +
"</xd:def>\n" +
"<xd:def name='B' root='B'>\n" +
"  <xd:json name='B'>\n" +
"    { \"p\":  \"string()\", \"q\":  \"? string()\" }\n" +
"  </xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".Mates_B %link B; %interface "+_package+".Mates_B_I %link B;\n" +
"  </xd:component>\n" +
"</xd:def>\n" +
"</xd:collection>");
            genXComponent(xp);
            json = "{\"A\":{ \"B\": { \"p\": \"P\", \"q\": \"Q\", \"x\": \"x\", \"y\": \"y\" } } }\n";
            xd = xp.createXDDocument("A");
            x = jparse(xd, json, reporter);
            assertNoErrorsAndClear(reporter);
            xc = xd.jparseXComponent(json, null, reporter);
            assertNoErrorsAndClear(reporter);
            assertTrue(XonUtils.xonEqual(x, xc.toXon()));
            json = "{\"A\": { \"B\": { \"y\": \"y\", \"x\": \"x\", \"q\": \"Q\", \"p\": \"P\" } } }\n";
            x = jparse(xd, json, reporter);
            assertNoErrorsAndClear(reporter);
            xc = xd.jparseXComponent(json, null, reporter);
            assertNoErrorsAndClear(reporter);
            assertTrue(XonUtils.xonEqual(x, xc.toXon()));
        } catch (RuntimeException ex) {fail(ex);}
        try {
            xp = compile(
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root='a'>\n" +
"  <xd:json name='a'>\n" +
"    {  \"%script\": \"finally now();\",\n"+
"       \"a\":  \"int()\", \"b\":  \"string();\", \"c\": \"string();\",\n" +
"    }\n" +
"  </xd:json>\n" +
"</xd:def>");
            jparse(xp, "", "{\"c\":\"LMN0H7\",\"a\":1,\"b\":\"MA5800-X17\"}", reporter);
            assertNoErrorsAndClear(reporter);
        } catch (RuntimeException ex) {fail(ex);}

        clearTempDir(); // delete temporary files.
    }

    public static void main(String[] args) {
        XDTester.setFulltestMode(true);
        if (runTest(args) > 0) {System.exit(1);}
    }
}