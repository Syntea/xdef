package test.xdef;

import java.io.File;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.xon.XonUtil;
import org.xdef.msg.SYS;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import test.XDTester;
import static test.XDTester._xdNS;
import static test.XDTester.genXComponent;
import org.xdef.xon.XonNames;

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
					Object json = XonUtil.parseJSON(f);
					// write JSON as XML (W3C modc)
					el = XonUtil.xonToXml(json);
					SUtils.writeString(new File(_tempDir + name + "a.xml"),
						KXmlUtils.nodeToString(el,true),"UTF-8");
					if (!XonUtil.xonEqual(XonUtil.xmlToJson(el),
						XonUtil.xmlToJson(XonUtil.xonToXmlXD(json)))) {
						throw new RuntimeException(rName +
							" xml transformation to JSON differs:\n" +
							KXmlUtils.nodeToString(XonUtil.xonToXml(json),
								true) + "\n" +
							KXmlUtils.nodeToString(XonUtil.xonToXmlXD(json),
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
				XDTester.compileSources(sources);
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
		XDDocument xd;
		XComponent xc;
		String result = "";
		ArrayReporter reporter = new ArrayReporter();
		// get all json files for this test
		xd = xp.createXDDocument("Test" + id);
		for (File f : SUtils.getFileGroup(_tempDir+"Test"+id+"*a.xml")) {
			Object json;
			String name = f.getName();
			String basename = name.substring(0, name.indexOf("a.xml"));
			// read JSON data
			try {
				json = XonUtil.parseJSON(_dataDir + basename + ".json");
			} catch (Exception ex) {
				result += (result.isEmpty() ? "" : "\n")
					+ "Incorrect JSON data Test"+id+".json";
				continue;
			}
			// create XDDocument with W3C from X-definition
			try {
				reporter.clear(); // clear reporter
				// parseJSON data with X-definition
				e = xd.xparse(f, reporter);
				if (reporter.errorWarnings()) { // check errors
					result += (result.isEmpty() ? "" : "\n")
						+ "ERRORS in " + name
						+ " (xdef: Test" + id +"a.xdef" +"):\n"
						+ reporter.printToString();
				} else {
					KXmlUtils.compareElements(e,
						f.getAbsolutePath(), true, reporter);
					if (reporter.errorWarnings()) {
						result += (result.isEmpty() ? "" : "\n")
							+ "ERROR: result differs " + name;
					} else {
						Object o = XonUtil.xmlToJson(
							KXmlUtils.nodeToString(e, true));
						if (!XonUtil.xonEqual(json, o)) {
							result += (result.isEmpty() ? "" : "\n")
								+ "ERROR conversion XML to JSON: " + name
								+ "\n" + XonUtil.toJsonString(json, true)
								+ '\n' + XonUtil.toJsonString(o, true)
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
				Object o = xd.jvalidate(json, null);
				if (!XonUtil.xonEqual(json, XonUtil.xonToJson(o))) {
					result += (result.isEmpty() ? "" : "\n")
						+ "Error jparse Test" + id + "\n"
						+ XonUtil.toJsonString(json) + "\n"
						+ XonUtil.toJsonString(XonUtil.xonToJson(o)) + "\n";
				}
			} catch (Exception ex) {
				fail(ex);
				result += (result.isEmpty() ? "" : "\n")
					+ "Incorrect jparse Test"+id+".json";
				continue;
			}
			// parseJSON with X-component
			try {
				xc = xd.parseXComponent(f, Class.forName(
					"test.common.json.component.Test" + id), null);
				reporter.clear();
				e = xc.toXml();
				KXmlUtils.compareElements(e, f.getAbsolutePath(),true,reporter);
				if (reporter.errorWarnings()) {
					result += (result.isEmpty() ? "" : "\n")
						+ "Error X-component " + name + "\n"
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
				xd = xp.createXDDocument("Test" + id);
				xc = xd.parseXComponent(f, Class.forName(
					"test.common.json.component.Test"+id), null);
				reporter.clear();
				e = xc.toXml();
				KXmlUtils.compareElements(e, f.getAbsolutePath(),true,reporter);
				if (reporter.errorWarnings()) {
					result += (result.isEmpty() ? "" : "\n")
						+ "Error X-component " + name +"\n"
						+ reporter.printToString()
						+ "\n"+ KXmlUtils.nodeToString(e, true);
				}
				Object o = XonUtil.xmlToJson(xc.toXml());
				if (!XonUtil.xonEqual(json, o)) { ///S
					result += (result.isEmpty() ? "" : "\n")
						+ "Error X-component toJsjon " + id + "\n"
						+ XonUtil.toJsonString(json) + "\n"
						+ XonUtil.toJsonString(o) + "\n";
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
				xd.jparse(f, reporter);
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
			return xp.createXDDocument(test).parseXComponent(f,
				Class.forName("test.common.json.component." + test), null);
		} catch (Exception ex) {
			throw new RuntimeException("XComponent not found: " + test);
		}
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		String xdef, xml, json;
		Object j;
		ArrayReporter reporter = new ArrayReporter();
		Element el;
		XDPool xp;
		XDDocument xd;
		StringWriter strw;
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
				assertTrue(s.isEmpty(), s);
			}
		} catch (Exception ex) {fail(ex);} // should not happen!!!
//		if(true)return;
		// Test X-components
		String xon = XDConstants.XON_NS_PREFIX + "$";
		try {
			String test;
			XComponent xc;
			test = "Test008";
			xc = getXComponent(xp, test, 0);
			j = SUtils.getValueFromGetter(xc,"get"+xon+"item");
			assertEq(1, SUtils.getValueFromGetter(j,"get"+XonNames.X_VALUEATTR));
			SUtils.setValueToSetter(j,"set" + XonNames.X_VALUEATTR, 3);
			assertEq(3, SUtils.getValueFromGetter(j,"get" + XonNames.X_VALUEATTR));
			SUtils.setValueToSetter(j,"set" + XonNames.X_VALUEATTR, null);
			assertNull(SUtils.getValueFromGetter(j,"get" + XonNames.X_VALUEATTR));

			test = "Test020";
			xc = getXComponent(xp, test, 0);
			j = SUtils.getValueFromGetter(xc,"get"+xon+"item");
			assertEq("abc", SUtils.getValueFromGetter(j,"get" + XonNames.X_VALUEATTR));
			SUtils.setValueToSetter(j,"set" + XonNames.X_VALUEATTR, null);
			assertTrue(SUtils.getValueFromGetter(j,"get"+XonNames.X_VALUEATTR)==null);

			xc = getXComponent(xp, test, 1);
			j = SUtils.getValueFromGetter(xc,"get"+xon+"item");
			assertEq(123, SUtils.getValueFromGetter(j,"get" + XonNames.X_VALUEATTR));
			SUtils.setValueToSetter(j,"set" + XonNames.X_VALUEATTR, "");
			assertEq("", SUtils.getValueFromGetter(j,"get" + XonNames.X_VALUEATTR));
			xc = getXComponent(xp, test, 2);
			j = SUtils.getValueFromGetter(xc,"get"+xon+"item");
			assertEq(false, SUtils.getValueFromGetter(j,"get"+XonNames.X_VALUEATTR));
			xc = getXComponent(xp, test, 3);
			j = SUtils.getValueFromGetter(xc,"get"+xon+"item");
			assertTrue(SUtils.getValueFromGetter(j,"get"+XonNames.X_VALUEATTR)!=null);

			test = "Test021";
			xc = getXComponent(xp, test, 0);
			j = SUtils.getValueFromGetter(xc,"get"+xon+"item");
			assertEq("abc", SUtils.getValueFromGetter(j,"get" +XonNames.X_VALUEATTR));
			xc = getXComponent(xp, test, 1);
			j = SUtils.getValueFromGetter(xc,"get"+xon+"item");
			assertEq(123, SUtils.getValueFromGetter(j,"get" +XonNames.X_VALUEATTR));
			SUtils.setValueToSetter(j,"set" + XonNames.X_VALUEATTR, "");
			assertEq("", SUtils.getValueFromGetter(j,"get" + XonNames.X_VALUEATTR));
			SUtils.setValueToSetter(j,"set" + XonNames.X_VALUEATTR, " a    b \n ");
			assertEq(" a    b \n ", SUtils.getValueFromGetter(j, "get" + XonNames.X_VALUEATTR));
			xc = getXComponent(xp, test, 2);
			j = SUtils.getValueFromGetter(xc,"get"+xon+"item");
			assertEq(false, SUtils.getValueFromGetter(j,"get" +XonNames.X_VALUEATTR));
			xc = getXComponent(xp, test, 3);
			j = SUtils.getValueFromGetter(xc,"get"+xon+"item");
			assertTrue(SUtils.getValueFromGetter(j,"get"+XonNames.X_VALUEATTR)!=null);
			xc = getXComponent(xp, test, 4);
			assertNull(SUtils.getValueFromGetter(xc,"get"+xon+"item"));

			test = "Test025";
			xc = getXComponent(xp, test, 0);
			j = SUtils.getValueFromGetter(xc,"get"+xon+"item");
			assertEq("null", SUtils.getValueFromGetter(j,
				"get" + XonNames.X_VALUEATTR).toString());
			j = SUtils.getValueFromGetter(xc,"get"+xon+"item_1");
			assertEq(12, SUtils.getValueFromGetter(j,"get" + XonNames.X_VALUEATTR));
			j = SUtils.getValueFromGetter(xc,"get"+xon+"item_2");
			assertEq("\" a b \"",
				SUtils.getValueFromGetter(j,"get" + XonNames.X_VALUEATTR));
			xc = getXComponent(xp, test, 1);
			j = SUtils.getValueFromGetter(xc,"get"+xon+"item");
			assertEq("null", SUtils.getValueFromGetter(j,
				"get" + XonNames.X_VALUEATTR).toString());
			assertNull(SUtils.getValueFromGetter(xc,"get"+xon+"item_1"));
			assertNull(SUtils.getValueFromGetter(xc,"get"+xon+"item_2"));

			test = "Test026";
			xc = getXComponent(xp, test, 0);
			j = SUtils.getValueFromGetter(xc,"listOf"+xon+"item");
			assertEq(2, ((List) j).size());
			j = ((List) j).get(0);
			assertEq("null", SUtils.getValueFromGetter(j,
				"get" + XonNames.X_VALUEATTR).toString());
			j = SUtils.getValueFromGetter(xc,"listOf"+xon+"item");
			j = ((List) j).get(1);
			assertEq("null", SUtils.getValueFromGetter(j,
				"get" + XonNames.X_VALUEATTR).toString());
			xc = getXComponent(xp, test, 0);
			j = SUtils.getValueFromGetter(xc,"listOf"+xon+"item_1");
			assertEq(2, ((List) j).size());
			j = ((List) j).get(0);
			assertEq(12, SUtils.getValueFromGetter(j,"get" + XonNames.X_VALUEATTR));
			j = SUtils.getValueFromGetter(xc,"listOf"+xon+"item_1");
			j = ((List) j).get(1);
			assertEq(13, SUtils.getValueFromGetter(j,"get" + XonNames.X_VALUEATTR));
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
"<xd:json name=\"Person\">\n"+
"{ \"Person\": { \"Name\": \"jstring(1, 50);\",\n" +
"    \"Pay\": \"int(1000, 99999);\",\n" +
"    \"Birth date.\": \"date();\"\n" +
"  }\n" +
"}\n" +
"</xd:json>\n"+
"</xd:def>";
			xp = compile(xdef);
			json =
"{ \"Person\": {\n" +
"    \"Name\":\"Václav Novák\",\n" +
"    \"Pay\":12345,\n" +
"    \"Birth date.\":\"1980-11-07\"\n" +
"  }\n" +
"}";
			xd = xp.createXDDocument("Person");
			j = jparse(xd, json, reporter);
			assertNoErrors(reporter);
			xd = xp.createXDDocument("Person");
			xd.setJSONContext(XonUtil.xonToJson(j));
			assertTrue(XonUtil.xonEqual(j, jcreate(xd, "Person", reporter)));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Person_list'>\n"+
"<xd:json name=\"Person_list\">\n"+
"{ \"Seznam\": \n"+
"  [\n"+
"    { $script= \"occurs 1..*;\",\n"+
"      \"Person\": { \"Name\": \"string(1, 50)\",\n" +
"         \"Pay\": \"int(1000, 99999)\",\n" +
"         \"Birth date.\": \"date()\"\n" +
"      }\n" +
 "   }\n"+
"  ]\n"+
"}\n"+
"</xd:json>\n"+
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
			j = jparse(xd, json, reporter);
			assertNoErrors(reporter);
			xd = xp.createXDDocument("");
			xd.setJSONContext(j);
			assertTrue(XonUtil.xonEqual(j,
				jcreate(xd, "Person_list", reporter)));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Person_list'>\n"+
"<xd:json name=\"Person_list\">\n"+
"{ \"Seznam\": \n"+
"  [\n"+
"    { $script = \"occurs 1..*; ref Person\" }\n"+
"  ]\n"+
"}\n"+
"</xd:json>\n"+
"<xd:json name=\"Person\">\n"+
"{ \"Person\": { \"Name\": \"string(1, 50)\",\n" +
"    \"Pay\": \"int(1000, 99999)\",\n" +
"    \"Birth date.\": \"date()\"\n" +
"  }\n" +
"}\n" +
"</xd:json>\n"+
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
			j = jparse(xd, json, reporter);
			assertNoErrors(reporter);
			xd = xp.createXDDocument("");
			xd.setJSONContext(j);
			assertTrue(XonUtil.xonEqual(j,
				jcreate(xd, "Person_list", reporter)));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Matrix'>\n"+
"<xd:json name=\"Matrix\">\n"+
"  [\n" +
"    [ $script=\"occurs 3;\",\n" +
"      \"occurs 3; float()\"\n" +
"    ]\n" +
"  ]\n"+
"</xd:json>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument("");
			json =
"[\n" +
"  [123.4, -56, 1],\n" +
"  [0, 0, 1],\n" +
"  [-5, 33, 0.5]\n" +
"]";
			j = jparse(xd, json, reporter);
			assertNoErrors(reporter);
			xd = xp.createXDDocument("");
			xd.setJSONContext(j);
			assertTrue(XonUtil.xonEqual(j, jcreate(xd, "Matrix", reporter)));
			assertNoErrors(reporter);
////////////////////////////////////////////////////////////////////////////////
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Skladby'>\n"+
"<xd:json name=\"Skladby\">\n"+
"  [\n" +
"    { $script= \"occurs 1..*;\",\n" +
"       \"Name\": \"string()\",\n" +
"       \"Style\": [ $oneOf,\n" +
"         \"string()\",\n" +
"         [ \"occurs 2..* string()\" ]\n" +
"       ]\n" +
"    }\n" +
"  ]\n" +
"</xd:json>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument("");
			json =
"[\n" +
"  { \"Name\": \"Beethoven, Symfonie No 5\",\n" +
"    \"Style\": \"Classic\"\n" +
"  },\n" +
"  { \"Name\": \"A Day at the Races\",\n" +
"    \"Style\": [\"jazz\", \"pop\" ]\n" +
"  }\n" +
"]";
			j = jparse(xd, json, reporter);
			assertNoErrors(reporter);
			xd = xp.createXDDocument("");
			xd.setJSONContext(j);
// TODO!!! - problem of construction of mixed with elements with matches
//			assertTrue(XonUtil.xonEqual(j, jcreate(xd, "Skladby", reporter)));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='json'>\n"+
"<xd:json name='json'>\n"+
"{\"\": \"optional jstring()\"}\n" +
"</xd:json>\n"+
"</xd:def>";
			xp = compile(xdef);
			json = "{\"\":\"aaa\"}";
			j = jparse(xp, "", json, reporter);
			assertNoErrors(reporter);
			json = "{}";
			j = jparse(xp, "", json, reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='json'>\n"+
"<xd:json name='json'>\n"+
"{\"\": \"optional jstring()\"}\n" +
"</xd:json>\n"+
"</xd:def>";
			xp = compile(xdef);
			json = "{\"\":\"aaa\"}";
			j = jparse(xp, "", json, reporter);
			assertNoErrors(reporter);
			json = "{}";
			j = jparse(xp, "", json, reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A|B|json'>\n"+
"<xd:json name='json'>\n"+
"[{\"a\":\"boolean\"},\"string()\",\"int()\"]\n" +
"</xd:json>\n"+
"<xd:json name='B'>\n"+
"{\"a\":\"int\"}\n"+
"</xd:json>\n"+
"  <A/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			json = "[{\"a\":true},\"x\",-1]";
			j = jparse(xp, "", json, reporter);
			assertNoErrors(reporter);
			assertTrue(XonUtil.xonEqual(XonUtil.parseJSON(json), j),
				XonUtil.toJsonString(j, true));
			el = XonUtil.xonToXml(j);
			parse(xp, "", el, reporter);
			assertNoErrors(reporter);
			json = "{\"a\":1}";
			j = jparse(xp, "", json, reporter);
			assertNoErrors(reporter);
			assertTrue(XonUtil.xonEqual(XonUtil.parseJSON(json), j),
				XonUtil.toJsonString(j, true));
			el = XonUtil.xonToXml(j);
			parse(xp, "", el, reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='B'>\n"+
"<xd:json name='B'>\n"+
"[$script= \"init out('a'); finally out('b')\", \"int(); finally out('x')\"]\n"+
"</xd:json>\n"+
"</xd:def>\n";
			xd = compile(xdef).createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(strw);
			json = "[123]";
			j = jparse(xd, json, reporter);
			assertNoErrors(reporter);
			assertTrue(XonUtil.xonEqual(XonUtil.parseJSON(json), j));
			assertEq("axb", strw.toString());
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" root=\"root\" >\n" +
"  <xd:json xd:name='root'>\n"+
"     \"jvalue();\"\n"+
"  </xd:json>\n"+
"</xd:def>" +
"";
			xp = compile(xdef);
			j = 123;
			assertTrue(XonUtil.xonEqual(j,
				jparse(xp, "", (Object) j, reporter)));
			j = -0L;
			assertTrue(XonUtil.xonEqual(j,
				jparse(xp, "", (Object) j, reporter)));
			j = -123.45e-1;
			assertTrue(XonUtil.xonEqual(j,
				jparse(xp, "", (Object) j, reporter)));
			j = -123.45e-1D;
			assertTrue(XonUtil.xonEqual(j,
				jparse(xp, "", (Object) j, reporter)));
			j = new BigInteger("123456789012345678901234567890");
			assertTrue(XonUtil.xonEqual(j,
				jparse(xp, "", (Object) j, reporter)));
			j = new BigDecimal("-123456789012345678901234567890e-2");
			assertTrue(XonUtil.xonEqual(j,
				jparse(xp, "", (Object) j, reporter)));
			j = true;
			assertTrue(XonUtil.xonEqual(j,
				jparse(xp, "", (Object) j, reporter)));
			j = "";
			assertTrue(XonUtil.xonEqual(j,
				jparse(xp, "", (Object) j, reporter)));
			j = "abc";
			assertTrue(XonUtil.xonEqual(j,
				jparse(xp, "", (Object) j, reporter)));
			j = "ab\nc";
			assertTrue(XonUtil.xonEqual(j,
				jparse(xp, "", (Object) j, reporter)));
			j = " ab tc ";
			assertTrue(XonUtil.xonEqual(j,
				jparse(xp, "", (Object) j, reporter)));
			j = "ab\n\tc";
			assertTrue(XonUtil.xonEqual(j,
				jparse(xp, "", (Object) j, reporter)));
			j = null;
			assertTrue(XonUtil.xonEqual(j,
				jparse(xp, "", (Object) j, reporter)));
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' name='TestINI' root='a'>\n"+
" <xd:ini name='a'>\n"+
"   A=?string()\n" +
"   B=int()\n" +
"   C=date()\n" +
"   D=decimal()\n" +
"   [E; $script=?]\n" +
"     x = ?int()\n" +
"   [F]\n" +
" </xd:ini>\n"+
" <xd:component>\n" +
"  %class test.common.json.component.TestINI %link TestINI#a" + ";\n" +
" </xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			String ini = "A=a\n B=1\n C=2121-10-19\n D=2.34\n[E]\nx=123\n[F]";
			xd = xp.createXDDocument("TestINI");
			String xdir = _tempDir + "x/";
			File fdir = new File(xdir);
			fdir.mkdirs();
			genXComponent(xp, fdir);
			XComponent xc = xd.iparseXComponent(ini, null, reporter);
			assertEq("a",SUtils.getValueFromGetter(xc,"get$A"));
			assertEq(1,SUtils.getValueFromGetter(xc,"get$B"));
			assertEq(new SDatetime("2121-10-19"),
				SUtils.getValueFromGetter(xc,"get$C"));
			assertEq(0, new BigDecimal("2.34").compareTo(
					(BigDecimal) SUtils.getValueFromGetter(xc,"get$D")));
		} catch (Exception ex) {fail(ex);}
	}

	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}