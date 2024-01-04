package test.xdef;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.msg.SYS;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SRuntimeException;
import static org.xdef.sys.STester.printThrowable;
import static org.xdef.sys.STester.runTest;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonNames;
import org.xdef.xon.XonUtils;
import test.XDTester;
import static test.XDTester._xdNS;

/** Test processing JSON objects with X-definitions and X-components.
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

	/** Generate XML files from JSON data in W3C mode and X-definition mode,
	 * create X-definitions in both modes and generate X-components. Then
	 * compile X-definitions and X-components.
	 * @return XDPool compiled from X-definitions.
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
		} catch (Exception ex) {
			fail(ex);
			return null;
		}
		// Initialize fields, test files and directories
		_dataDir = getDataDir() + "json/";
		_jfiles = SUtils.getFileGroup(_dataDir + filter + ".xdef");
		// Generate files and compile X-definitions and X-components.
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
						throw new RuntimeException(rName +
							" xml transformation to JSON differs:\n" +
							KXmlUtils.nodeToString(XonUtils.xonToXmlW(json),
								true) + "\n" +
							KXmlUtils.nodeToString(XonUtils.xonToXml(json),
								true) + "\n");
					}
				}
				// create X-component items
				String cls = "  %class test.common.json.component.Test" + id;
				el = KXmlUtils.parseXml(
					_dataDir + "Test" + id + ".xdef").getDocumentElement();
				components += cls +" %link Test" + id + "#a" + ";\n";
			}
			components += "</xd:component>";
			// write X-component declaration to the file
			File componentFile = new File(_tempDir + "Components.xdef");
			SUtils.writeString(componentFile, components, "UTF-8");
			// compile all X-definitions to XDPool
			XDPool xp;
			try {
				File[] files = new File[_jfiles.length + 1];
				System.arraycopy(_jfiles, 0, files, 1, _jfiles.length);
				files[0] = componentFile;
				xp = compile(files);
			} catch (Exception ex) {
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
			} catch (Exception ex) {}
			if (!rebuild) {
				for (File fdef: _jfiles) {
					String id = getId(fdef);
					try { // check if X-components arte compliled
						Class clazz = Class.forName(
							"test.common.json.component.Test" + id);
						if (clazz == null) {
							return null; //force rebuild
						}
						clazz = Class.forName(
							"test.common.json.component.Test" + id + 'a');
						if (clazz == null) {
							return null;  //force rebuild
						}
					} catch (Exception ex) {
						rebuild = true;  //force rebuild
						break;
					}
				}
			}
			if (rebuild) {
				File[] ff =	SUtils.getFileGroup(
					_tempDir+"test/common/json/component/Test*.java");
				String[] sources = new String[ff.length];
				for (int i = 0; i < ff.length; i++) {
					sources[i] = ff[i].getPath();
				}
				Class<?> clazz = XDConstants.class;
				String className = clazz.getName().replace('.', '/') + ".class";
				URL u = clazz.getClassLoader().getResource(className);
				String classpath = u.toExternalForm();
				if (classpath.startsWith("jar:file:")
					&& classpath.indexOf('!') > 0) {
					classpath=classpath.substring(9,classpath.lastIndexOf('!'));
					classpath =
						new File(classpath).getAbsolutePath().replace('\\','/');
				} else {
					classpath = new File(
						u.getFile()).getAbsolutePath().replace('\\','/');
					classpath = classpath.substring(
						0, classpath.indexOf(className));
				}
				// where are compiled classes of tests
				clazz = XDTester.class;
				className = clazz.getName().replace('.', '/') + ".class";
				u = clazz.getClassLoader().getResource(className);
				String classDir =
					new File(u.getFile()).getAbsolutePath().replace('\\', '/');
				classDir = classDir.substring(0, classDir.indexOf(className));
				compileSources(classpath, classDir, sources);
			}
			return xp; // return XDPool with compiled X-definitions
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/** Provides different tests on files with given ID.
	 * @param xp compiled XDPool from generated X-definitions.
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
			} catch (Exception ex) {
				result += (result.isEmpty() ? "" : "\n")
					+ "Incorrect JSON data Test"+id+".json";
				continue;
			}
			// create XDDocument with W3C from X-definition
			try {
				reporter.clear(); // clear reporter
				// parseJSON data with X-definition
				e = xp.createXDDocument("Test"+id).xparse(f, reporter);
				if (reporter.errorWarnings()) { // check errors
					result += (result.isEmpty() ? "" : "\n")
						+ "ERRORS in " + name
						+ " (xdef: Test" + id +"a.xdef" +"):\n"
						+ reporter.printToString();
				} else {
					KXmlUtils.compareElements(e,
						f.getAbsolutePath(), true, reporter);
					if (reporter.errorWarnings()) {
						result += (result.isEmpty() ? "" : "\n") +
							"ERROR: result differs " + name + '\n' +
							KXmlUtils.nodeToString(e, true) + '\n' +
							KXmlUtils.nodeToString(KXmlUtils.parseXml(f), true);
					} else {
						o = XonUtils.xmlToXon(KXmlUtils.nodeToString(e, true));
						if (!XonUtils.xonEqual(json, o)) {
							result += (result.isEmpty() ? "" : "\n")
								+ "ERROR conversion XML to JSON: " + name
								+ "\n" + XonUtils.toJsonString(json, true)
								+ '\n' + XonUtils.toJsonString(o, true)
								+ '\n' + KXmlUtils.nodeToString(e, true);
						}
					}
				}
			} catch (Exception ex) {
				result += (result.isEmpty() ? "" : "\n") +
					"Error " + name + "\n" + printThrowable(ex);
			}
			// parseJSON with jparse
			try {
				o = xp.createXDDocument("Test"+id).jvalidate(json, null);
				if (!XonUtils.xonEqual(json, XonUtils.xonToJson(o))) {
					result += (result.isEmpty() ? "" : "\n")
						+ "Error jparse Test" + id + "\n"
						+ XonUtils.toJsonString(json) + "\n"
						+ XonUtils.toJsonString(XonUtils.xonToJson(o)) + "\n";
				}
			} catch (Exception ex) {
				result += (result.isEmpty() ? "" : "\n")
					+ "Incorrect jparse Test"+id+".json";
				fail(ex);
				continue;
			}
			// parseJSON with X-component
			try {
				xc = xp.createXDDocument("Test"+id).xparseXComponent(f,
					Class.forName("test.common.json.component.Test"+id), null);
				reporter.clear();
				o = XonUtils.xonToJson(xc.toXon());
				if (!XonUtils.xonEqual(json, o)) {
					result += "Error xc.toXon(): " + name + "\n"
						+  json + "\n" + o + "\n";
				}
				e = xc.toXml();
				KXmlUtils.compareElements(e, f.getAbsolutePath(),true,reporter);
				if (reporter.errorWarnings()) {
					result += (result.isEmpty() ? "" : "\n")
						+ "Error xc.toXml(): " + name + "\n"
						+ KXmlUtils.nodeToString(
							KXmlUtils.parseXml(f).getDocumentElement(), true)
						+ "\n"+ KXmlUtils.nodeToString(e, true);
				}
			} catch (Exception ex) {
				result += (result.isEmpty() ? "" : "\n")
					+ "Error X-component " + name + "\n" + printThrowable(ex);
			}
			// Test X-component.
			try {
				xc = xp.createXDDocument("Test"+id).xparseXComponent(f,
					Class.forName("test.common.json.component.Test"+id), null);
				reporter.clear();
				e = xc.toXml();
				KXmlUtils.compareElements(e, f.getAbsolutePath(),true,reporter);
				if (reporter.errorWarnings()) {
					result += (result.isEmpty() ? "" : "\n")
						+ "Error X-component " + name +"\n"
						+ reporter.printToString()
						+ "\n"+ KXmlUtils.nodeToString(e, true);
				}
				o = XonUtils.xmlToXon(xc.toXml());
				if (!XonUtils.xonEqual(json, XonUtils.xonToJson(o))) { ///S
					result += (result.isEmpty() ? "" : "\n")
						+ "Error X-component toJsjon " + id + "\n"
						+ XonUtils.toJsonString(json) + "\n"
						+ XonUtils.toJsonString(o) + "\n";
				}
				// test to parse cXON from X-component
				xc = xp.createXDDocument("Test"+id).jparseXComponent(xc.toXon(),
					Class.forName("test.common.json.component.Test"+id), null);
				if (!XonUtils.xonEqual(json, XonUtils.xonToJson(xc.toXon()))) {
					result += (result.isEmpty() ? "" : "\n")
						+ "Error X-component toJsjon " + id + "\n"
						+ XonUtils.toJsonString(json) + "\n"
						+ XonUtils.toJsonString(o) + "\n";
				}
			} catch (Exception ex) {
				result += (result.isEmpty() ? "" : "\n")
					+ "Error X-component " + id + "\n" + printThrowable(ex);
			}
		}
		// Test error reporting
		for (File f: SUtils.getFileGroup(_dataDir+"Test"+id+"*.jerr")) {
			try {
				reporter.clear();
				xp.createXDDocument("Test"+id).jparse(f, reporter);
				if (!reporter.errorWarnings()) {
					result += (result.isEmpty() ? "" : "\n")
						+ "Error not reported: "+f.getName();
				}
			} catch (Exception ex) {
				result += (result.isEmpty() ? "" : "\n")
					+ "Error jerr: " + f.getName() + "\n" + printThrowable(ex);
			}
		}
		return result;
	}

	/** Get XComponent with parsed data.
	 * @param xp compiled XDPool from generated X-definitions.
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
		} catch (Exception ex) {
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
			Writer wr =
				new OutputStreamWriter(	new FileOutputStream(f), encoding);
			if (genEncoding) {
				wr.write("%encoding = \"" + encoding + "\"\n");
			}
			wr.write(xon);
			wr.close();
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
		} catch (Exception ex) {
			return printThrowable(ex);
		}
	}

	@Override
	/** Run test and print error information. */
	@SuppressWarnings("unchecked")
	public void test() {
		if (!_xdNS.startsWith("http://www.xdef.org/xdef/4.")) {
			return;
		}
		String fname, ini, json, xdef, xml;
		File file;
		Object x;
		List list;
		Element el;
		ArrayReporter reporter = new ArrayReporter();
		StringWriter swr;
		XComponent xc;
		XDDocument xd;
		XDPool xp;
		// Generate data (X-definitons, X-components, XML source files).
		try {
			xp = genAll("Test*");
//			xp = genAll("Test064");
		} catch (Exception ex) {
			fail(new RuntimeException(ex));
			return;
		}
		// run all tests
		try {
			for (File f: _jfiles) {
				String s = testJdef(xp, getId(f));
				assertTrue(s.isEmpty(), s );
			}
		} catch (Exception ex) {fail(ex);} // should not happen!!!
//		if(true)return;
		// Test X-components
		String xon = XDConstants.XON_NS_PREFIX + "$";
		try {
			fname = "Test008";
			xc = getXComponent(xp, fname, 0);
			x = SUtils.getValueFromGetter(xc,"get"+xon+XonNames.X_VALUE);
			assertEq(1, SUtils.getValueFromGetter(x,"get"+XonNames.X_VALATTR));
			SUtils.setValueToSetter(x,"set" + XonNames.X_VALATTR, 3);
			assertEq(3, SUtils.getValueFromGetter(x,"get" + XonNames.X_VALATTR));
			SUtils.setValueToSetter(x,"set" + XonNames.X_VALATTR, null);
			assertNull(SUtils.getValueFromGetter(x,"get" + XonNames.X_VALATTR));

			fname = "Test020";
			xc = getXComponent(xp, fname, 0);
			assertEq("abc", SUtils.getValueFromGetter(xc,"get$a"));
			SUtils.setValueToSetter(xc,"set$a", null);
			assertTrue(SUtils.getValueFromGetter(xc,"get$a")==null);

			xc = getXComponent(xp, fname, 1);
			assertEq(123, SUtils.getValueFromGetter(xc,"get$a"));
			SUtils.setValueToSetter(xc,"set$a", "");
			assertEq("", SUtils.getValueFromGetter(xc,"get$a"));
			xc = getXComponent(xp, fname, 2);
			assertEq(false, SUtils.getValueFromGetter(xc,"get$a"));
			xc = getXComponent(xp, fname, 3);
			assertTrue(SUtils.getValueFromGetter(xc,"get$a")!=null);

			fname = "Test021";
			xc = getXComponent(xp, fname, 0);
			x = SUtils.getValueFromGetter(xc,"get"+xon+XonNames.X_VALUE);
			assertEq("abc",
				SUtils.getValueFromGetter(x,"get" +XonNames.X_VALATTR));
			xc = getXComponent(xp, fname, 1);
			x = SUtils.getValueFromGetter(xc,"get"+xon+XonNames.X_VALUE);
			assertEq(123,
				SUtils.getValueFromGetter(x,"get" +XonNames.X_VALATTR));
			SUtils.setValueToSetter(x,"set" + XonNames.X_VALATTR, "");
			assertEq("",
				SUtils.getValueFromGetter(x,"get" + XonNames.X_VALATTR));
			SUtils.setValueToSetter(x,
				"set"+XonNames.X_VALATTR, " a    b \n ");
			assertEq(" a    b \n ",
				SUtils.getValueFromGetter(x, "get" + XonNames.X_VALATTR));
			xc = getXComponent(xp, fname, 2);
			x = SUtils.getValueFromGetter(xc,"get"+xon+XonNames.X_VALUE);
			assertEq(false,
				SUtils.getValueFromGetter(x,"get" +XonNames.X_VALATTR));
			xc = getXComponent(xp, fname, 3);
			x = SUtils.getValueFromGetter(xc,"get"+xon+XonNames.X_VALUE);
			assertTrue(SUtils.getValueFromGetter(
				x, "get"+XonNames.X_VALATTR)!=null);
			xc = getXComponent(xp, fname, 4);
			assertNull(
				SUtils.getValueFromGetter(xc,"get"+xon+XonNames.X_VALUE));
			fname = "Test025";
			xc = getXComponent(xp, fname, 0);
			x = SUtils.getValueFromGetter(xc,"get"+xon+XonNames.X_VALUE);
			assertEq("null", SUtils.getValueFromGetter(x,
				"get" + XonNames.X_VALATTR).toString());
			x = SUtils.getValueFromGetter(xc,"get"+xon+XonNames.X_VALUE+"_1");
			assertEq(12,
				SUtils.getValueFromGetter(x,"get" + XonNames.X_VALATTR));
			x = SUtils.getValueFromGetter(xc,"get"+xon+XonNames.X_VALUE+"_2");
			assertEq("\" a b \"",
				SUtils.getValueFromGetter(x,"get" + XonNames.X_VALATTR));
			xc = getXComponent(xp, fname, 1);
			x = SUtils.getValueFromGetter(xc,"get"+xon+XonNames.X_VALUE);
			assertEq("null", SUtils.getValueFromGetter(x,
				"get" + XonNames.X_VALATTR).toString());
			assertNull(
				SUtils.getValueFromGetter(xc,"get"+xon+XonNames.X_VALUE+"_1"));
			assertNull(
				SUtils.getValueFromGetter(xc,"get"+xon+XonNames.X_VALUE+"_2"));

			fname = "Test026";
			xc = getXComponent(xp, fname, 0);
			x = SUtils.getValueFromGetter(xc,"listOf"+xon+XonNames.X_VALUE);
			assertEq(2, ((List) x).size());
			x = ((List) x).get(0);
			assertEq("null", SUtils.getValueFromGetter(x,
				"get" + XonNames.X_VALATTR).toString());
			x = SUtils.getValueFromGetter(xc,"listOf"+xon+XonNames.X_VALUE);
			x = ((List) x).get(1);
			assertEq("null", SUtils.getValueFromGetter(x,
				"get" + XonNames.X_VALATTR).toString());
			xc = getXComponent(xp, fname, 0);
			x = SUtils.getValueFromGetter(xc,"listOf"+xon+XonNames.X_VALUE+"_1");
			assertEq(2, ((List) x).size());
			x = ((List) x).get(0);
			assertEq(12,
				SUtils.getValueFromGetter(x,"get" + XonNames.X_VALATTR));
			x = SUtils.getValueFromGetter(xc,"listOf"+xon+XonNames.X_VALUE+"_1");
			x = ((List) x).get(1);
			assertEq(13,
				SUtils.getValueFromGetter(x,"get" + XonNames.X_VALATTR));
		} catch (Exception ex) {fail(ex);}
		// If no errors were reported delete all generated data.
		// Otherwise, leave them to be able to see the reason of errors.
		if (getFailCount() == 0) {
			clearTempDir(); // delete temporary files.
		}

		// Other tests
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='Person' root='Person'>\n"+
"<xd:xon name=\"Person\">\n"+
"{ \"Person\": { \"Name\": \"jstring(1, 50);\",\n" +
"    \"Pay\": \"int(1000, 99999);\",\n" +
"    \"Birth date.\": \"date();\"\n" +
"  }\n" +
"}\n" +
"</xd:xon>\n"+
"<xd:component>\n"+
"  %class "+_package+".XonPerson %link Person#Person;\n"+
"</xd:component>\n"+
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
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(x, xc.toXon()));
			xd = xp.createXDDocument("Person");
			xd.setXONContext(XonUtils.xonToJson(x));
			xc = xd.jcreateXComponent("Person",
				Class.forName("test.xdef.XonPerson"), reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(x, xc.toXon()));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Person_list'>\n"+
"<xd:xon name=\"Person_list\">\n"+
"{ \"Seznam\": \n"+
"  [\n"+
"    { %script= \"occurs 1..*;\",\n"+
"      \"Person\": { \"Name\": \"string(1, 50)\",\n" +
"         \"Pay\": \"int(1000, 99999)\",\n" +
"         \"Birth date.\": \"date()\"\n" +
"      }\n" +
 "   }\n"+
"  ]\n"+
"}\n"+
"</xd:xon>\n"+
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
			assertTrue(XonUtils.xonEqual(x,
				jcreate(xd, "Person_list", reporter)));
			assertNoErrorwarningsAndClear(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Person_list'>\n"+
"<xd:xon name=\"Person_list\">\n"+
"{ \"Seznam\": \n"+
"  [\n"+
"    { %script = \"occurs 1..*; ref Person\" }\n"+
"  ]\n"+
"}\n"+
"</xd:xon>\n"+
"<xd:xon name=\"Person\">\n"+
"{ \"Person\": { \"Name\": \"string(1, 50)\",\n" +
"    \"Pay\": \"int(1000, 99999)\",\n" +
"    \"Birth date.\": \"date()\"\n" +
"  }\n" +
"}\n" +
"</xd:xon>\n"+
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
			assertTrue(XonUtils.xonEqual(x,
				jcreate(xd, "Person_list", reporter)));
			assertNoErrorwarningsAndClear(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Matrix'>\n"+
"<xd:xon name=\"Matrix\">\n"+
"  [\n" +
"    [ %script=\"occurs 3;\",\n" +
"      \"occurs 3; float()\"\n" +
"    ]\n" +
"  ]\n"+
"</xd:xon>\n"+
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
"<xd:def xmlns:xd='" + _xdNS + "' root='Skladby'>\n"+
"<xd:xon name=\"Skladby\">\n"+
"  [\n" +
"    { %script= \"occurs 1..*;\",\n" +
"       \"Name\": \"string()\",\n" +
"       \"Style\": [ %oneOf,\n" +
"         \"string()\",\n" +
"         [ \"occurs 2..* string()\" ]\n" +
"       ]\n" +
"    }\n" +
"  ]\n" +
"</xd:xon>\n"+
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
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='json'>\n"+
"<xd:xon name='json'>\n"+
"{\"\": \"optional jstring()\"}\n" +
"</xd:xon>\n"+
"</xd:def>";
			xp = compile(xdef);
			json = "{\"\":\"aaa\"}";
			x = jparse(xp, "", json, reporter);
			assertNoErrorwarningsAndClear(reporter);
			json = "{}";
			x = jparse(xp, "", json, reporter);
			assertNoErrorwarningsAndClear(reporter);
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='json'>\n"+
"<xd:xon name='json'>\n"+
"{\"\": \"optional jstring()\"}\n" +
"</xd:xon>\n"+
"</xd:def>";
			xp = compile(xdef);
			json = "{\"\":\"aaa\"}";
			x = jparse(xp, "", json, reporter);
			assertNoErrorwarningsAndClear(reporter);
			json = "{}";
			x = jparse(xp, "", json, reporter);
			assertNoErrorwarningsAndClear(reporter);
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='A|B|json'>\n"+
"<xd:xon name='json'>\n"+
"[{\"a\":\"boolean\"},\"string()\",\"int()\"]\n" +
"</xd:xon>\n"+
"<xd:xon name='B'>\n"+
"{\"a\":\"int\"}\n"+
"</xd:xon>\n"+
"  <A/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			json = "[{\"a\":true},\"x\",-1]";
			x = jparse(xp, "", json, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseJSON(json), x),
				XonUtils.toJsonString(x, true));
			el = XonUtils.xonToXmlW(x);
			parse(xp, "", el, reporter);
			assertNoErrorwarningsAndClear(reporter);
			json = "{\"a\":1}";
			x = jparse(xp, "", json, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseJSON(json), x),
				XonUtils.toJsonString(x, true));
			el = XonUtils.xonToXmlW(x);
			parse(xp, "", el, reporter);
			assertNoErrorwarningsAndClear(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='B'>\n"+
"<xd:xon name='B'>\n"+
"[%script= \"init out('a'); finally out('b')\", \"int(); finally out('x')\"]\n"+
"</xd:xon>\n"+
"</xd:def>\n";
			xd = compile(xdef).createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(swr);
			json = "[123]";
			x = jparse(xd, json, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseJSON(json), x));
			assertEq("axb", swr.toString());
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd=\""+_xdNS+"\" root=\"root\" >\n" +
"  <xd:xon xd:name='root'>\n"+
"     \"jvalue();\"\n"+
"  </xd:xon>\n"+
"</xd:def>";
			xp = compile(xdef);
			x = 123;
			assertTrue(XonUtils.xonEqual(x,
				jparse(xp, "", (Object) x, reporter)));
			x = -0L;
			assertTrue(XonUtils.xonEqual(x,
				jparse(xp, "", (Object) x, reporter)));
			x = -123.45e-1;
			assertTrue(XonUtils.xonEqual(x,
				jparse(xp, "", (Object) x, reporter)));
			x = -123.45e-1D;
			assertTrue(XonUtils.xonEqual(x,
				jparse(xp, "", (Object) x, reporter)));
			x = new BigInteger("123456789012345678901234567890");
			assertTrue(XonUtils.xonEqual(x,
				jparse(xp, "", (Object) x, reporter)));
			x = new BigDecimal("-123456789012345678901234567890.1e-2");
			assertTrue(XonUtils.xonEqual(x,
				jparse(xp, "", (Object) x, reporter)));
			x = true;
			assertTrue(XonUtils.xonEqual(x,
				jparse(xp, "", (Object) x, reporter)));
			x = null;
			assertTrue(XonUtils.xonEqual(x,
				jparse(xp, "", (Object) x, reporter)));
			x = "true";
			assertTrue(XonUtils.xonEqual(true,
				jparse(xp, "", (Object) x, reporter)));
			x = "-123";
			assertTrue(XonUtils.xonEqual(-123,
				jparse(xp, "", (Object) x, reporter)));
			x = "null";
			assertTrue(XonUtils.xonEqual(null,
				jparse(xp, "", (Object) x, reporter)));
			x = "\"\"";
			assertTrue(XonUtils.xonEqual("",
				jparse(xp, "", (Object) x, reporter)));
			x = "\"abc\"";
			assertTrue(XonUtils.xonEqual("abc",
				jparse(xp, "", (Object) x, reporter)));
			x = "\"ab\nc\"";
			assertTrue(XonUtils.xonEqual("ab\nc",
				jparse(xp, "", (Object) x, reporter)));
			x = "\" ab tc \"";
			assertTrue(XonUtils.xonEqual(" ab tc ",
				jparse(xp, "", (Object) x, reporter)));
			x = "\"ab\\n\\tc\"";
			assertTrue(XonUtils.xonEqual("ab\n\tc",
				jparse(xp, "", (Object) x, reporter)));
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd=\""+_xdNS+"\" name=\"X\" root=\"a\">\n"+
" <xd:ini name='a'>\n"+
"   A=?string(); finally out(\"A\");\n" +
"   B=int(); finally out(\"B\");\n" +
"   C=date(); finally out(\"C\");\n" +
"   D=decimal(); finally out(\"D\");\n" +
"   [E ; %script = optional; finally out(\"[E]\");]\n" +
"     x = ?int(); finally out(\"x\");\n" +
"   [F;%script=finally out(\"[F]\");]\n" +
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
"   [ E-F.G ; %script=?]\n" +
"     x = ?int()\n" +
"   [ F ]\n" +
" </xd:ini>\n"+
" <xd:component>\n" +
"  %class "+_package+".TestINI %link TestINI#a" + ";\n" +
" </xd:component>\n"+
"</xd:def>";
			ini = "A=a\n B=1\n C=2121-10-19\n D=2.34\n[ E-F.G ]\nx=123\n[F]";
			genXComponent(xp = compile(xdef));
			xd = xp.createXDDocument("TestINI");
			xc = xd.iparseXComponent(ini, null, reporter);
			assertEq("a", SUtils.getValueFromGetter(xc,"get$A"));
			assertEq(1,SUtils.getValueFromGetter(xc,"get$B"));
			assertEq(new SDatetime("2121-10-19"),
				SUtils.getValueFromGetter(xc,"get$C"));
			assertEq(0, new BigDecimal("2.34").compareTo(
				(BigDecimal) SUtils.getValueFromGetter(xc,"get$D")));
			SUtils.setValueToSetter(xc,"set$A", "b");
			assertEq("b", SUtils.getValueFromGetter(xc,"get$A"));
			ini = "A=a\n B=1\n C=2121-10-19\n D=2.34\n[F]";
			xc = xd.iparseXComponent(ini, null, reporter);
			assertEq("a", SUtils.getValueFromGetter(xc,"get$A"));
			assertEq(1,SUtils.getValueFromGetter(xc,"get$B"));
			assertEq(new SDatetime("2121-10-19"),
				SUtils.getValueFromGetter(xc,"get$C"));
			assertEq(0, new BigDecimal("2.34").compareTo(
				(BigDecimal) SUtils.getValueFromGetter(xc,"get$D")));
			SUtils.setValueToSetter(xc,"set$A", "b");
			assertEq("b", SUtils.getValueFromGetter(xc,"get$A"));
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='a'>\n"+
"<xd:xon name='a'>\n" +
"[\n" +
"  {\n" +
"    a : \"? short()\",\n" +
"    i : [],\n" +
"    Towns : [\n" +
"      \"* gps()\"\n" +
"    ],\n" +
"    j : \"? char()\"\n" +
"  },\n" +
"  \"base64Binary()\",\n" +
"  \"base64Binary()\",\n" +
"  \"base64Binary()\",\n" +
"  \"price()\",\n" +
"  \"currency()\",\n" +
"  \"* ipAddr()\"\n" +
"]\n" +
"</xd:xon>\n" +
"<xd:component>\n"+
"  %class "+_package+".X_on %link #a;\n"+
"</xd:component>\n"+
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
			assertTrue(XonUtils.xonEqual(XonUtils.parseXON(json),
				SUtils.getValueFromGetter(xc,"toXon")));
			list = (List) SUtils.getValueFromGetter(
				xc, "listOf$"+XonNames.X_VALUE+"_5");
			list.add(InetAddress.getByName("111.22.33.1"));
			SUtils.setValueToSetter(xc, "set"+XonNames.X_VALUE+"_5", list);
			assertEq(2, ((List) SUtils.getValueFromGetter(
				xc,"listOf$"+XonNames.X_VALUE+"_5")).size());
			assertTrue(SUtils.getValueFromGetter(SUtils.getValueFromGetter(
				xc,"getjx$"+XonNames.X_MAP), "toXon") instanceof Map);
			assertTrue(((List)SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(SUtils.getValueFromGetter(
					xc,"getjx$"+XonNames.X_MAP),
					"getjx$"+XonNames.X_ARRAY),"toXon")).isEmpty());
			assertEq(3,((List)SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(SUtils.getValueFromGetter(
					xc,"getjx$"+XonNames.X_MAP),
					"getjx$"+XonNames.X_ARRAY+"_1"), "toXon")).size());
		} catch (Exception ex) {fail(ex);}
		try {
			xdef = // test data with different encodings
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" root=\"a\" >\n" +
"<xd:xon name=\"a\">\n" +
"  # test encodings\n" +
"  { \"ěščřžýáíéúůĺ %u@#$\": {\n" +
"      é: \"string()\",\n" +
"      \"§&amp;&lt;&gt;\": \"date()\",\n" +
"      \"%\": [ \"*; string()\", { \"čřé\": \"string()\" } ]\n" +
"    }\n" +
"  }\n" +
"</xd:xon>\n" +
"<xd:component>\n"+
"  %class "+_package+".TestXonEncoding %link #a;\n"+
"</xd:component>\n"+
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
		} catch (Exception ex) {fail(ex);}

		clearTempDir(); // delete temporary files.
	}

	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}