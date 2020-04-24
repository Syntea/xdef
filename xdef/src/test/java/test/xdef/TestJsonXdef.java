package test.xdef;

import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.json.JsonUtil;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.w3c.dom.Element;
import test.XDTester;
import java.util.List;
import org.xdef.json.JsonToXml;
import org.xdef.msg.SYS;
import org.xdef.sys.SRuntimeException;
import static test.XDTester._xdNS;
import static test.XDTester.genXComponent;
import static test.XDTester.getValueFromGetter;
import static test.XDTester.setValueToSetter;

/** Test processing JSON objects with X-definitions and X-components.
 * @author Vaclav Trojan
 */
public class TestJsonXdef extends XDTester {

	public TestJsonXdef() {super();}

	private String _dataDir; // dirsctory with test data
	private File[] _jfiles; // array with jdef files
	private String _tempDir; // directory where to generate files

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
		// Initialize fields, test files and directories
		_dataDir = getDataDir() + "json/";
		_jfiles = SUtils.getFileGroup(_dataDir + filter + ".xdef");
		_tempDir = getTempDir() + "json/";
		new File(_tempDir).mkdirs();
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
					Object json = JsonUtil.parse(f);
					// write JSON as XML (W3C modc)
					el = JsonUtil.jsonToXml(json);
					SUtils.writeString(new File(_tempDir + name + "a.xml"),
						KXmlUtils.nodeToString(el,true),"UTF-8");
					if (!JsonUtil.jsonEqual(JsonUtil.xmlToJson(el),
						JsonUtil.xmlToJson(JsonUtil.jsonToXmlXdef(json)))) {
						throw new RuntimeException(rName +
							" xml transformation to JSON differs:\n" +
							KXmlUtils.nodeToString(JsonUtil.jsonToXml(json),
								true) + "\n" +
							KXmlUtils.nodeToString(JsonUtil.jsonToXmlXdef(json),
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
				SUtils.deleteAll(xdir, true);
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
				json = JsonUtil.parse(_dataDir + basename + ".json");
			} catch (Exception ex) {
				result += (result.isEmpty() ? "" : "\n")
					+ "Incorrect JSON data Test"+id+".json";
				continue;
			}
			// create XDDocument with W3C from X-definition
			try {
				reporter.clear(); // clear reporter
				// parse data with X-definition
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
						Object o = JsonUtil.xmlToJson(
							KXmlUtils.nodeToString(e, true));
						if (!JsonUtil.jsonEqual(json, o)) {
							result += (result.isEmpty() ? "" : "\n")
								+ "ERROR conversion XML to JSON: " + name
								+ "\n" + JsonUtil.toJsonString(json, true)
								+ '\n' + JsonUtil.toJsonString(o, true)
								+ '\n' + KXmlUtils.nodeToString(e, true);
						}
					}
				}
			} catch (Exception ex) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				pw.close();
				result += (result.isEmpty() ? "" : "\n")
					+ "Error " + name + "\n" + sw;
			}
			// parse with jparse
			try {
				Object o = xd.jparse(json, null);
				if (!JsonUtil.jsonEqual(json, o)) {
					result += (result.isEmpty() ? "" : "\n")
						+ "Error jparse Test" + id + "\n"
						+ JsonUtil.toJsonString(json) + "\n"
						+ JsonUtil.toJsonString(o) + "\n";
				}
			} catch (Exception ex) {
				result += (result.isEmpty() ? "" : "\n")
					+ "Incorrect jparse Test"+id+".json";
				continue;
			}
			// parse with X-component
			try {
				xc = xd.parseXComponent(f, Class.forName(
					"test.common.json.component.Test" + id), null);
				reporter.clear();
				e = xc.toXml();
				KXmlUtils.compareElements(e, f.getAbsolutePath(),true,reporter);
				if (reporter.errorWarnings()) {
					result += (result.isEmpty() ? "" : "\n")
						+ "Error X-component " + name + "\n"
						+ reporter.printToString()
						+ "\n"+ KXmlUtils.nodeToString(e, true);
				}
			} catch (Exception ex) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				pw.close();
				result += (result.isEmpty() ? "" : "\n")
					+ "Error X-component " + name + "\n" + sw;
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
				Object o = xc.toJson();
				if (!JsonUtil.jsonEqual(json, o)) {
					result += (result.isEmpty() ? "" : "\n")
						+ "Error X-component toJsjon " + id + "\n"
						+ JsonUtil.toJsonString(json) + "\n"
						+ JsonUtil.toJsonString(o) + "\n";
				}
			} catch (Exception ex) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				pw.close();
				result += (result.isEmpty() ? "" : "\n")
					+ "Error X-component " + id + "\n" + sw;
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
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				pw.close();
				result += (result.isEmpty() ? "" : "\n")
					+ "Error jerr: " + f.getName() + "\n" + sw;
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
//			xp = genAll("Test017");
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
/*xx*/
		try {
			String test;
			XComponent xc;
			Object o;

			test = "Test008";
			xc = getXComponent(xp, test, 0);
			o = getValueFromGetter(xc,"getjs$item");
			assertEq(1, getValueFromGetter(o,"get" + JsonToXml.J_VALUEATTR));
			setValueToSetter(o,"set" + JsonToXml.J_VALUEATTR, 3);
			assertEq(3, getValueFromGetter(o,"get" + JsonToXml.J_VALUEATTR));
			setValueToSetter(o,"set" + JsonToXml.J_VALUEATTR, null);
			assertNull(getValueFromGetter(o,"get" + JsonToXml.J_VALUEATTR));

			test = "Test020";
			xc = getXComponent(xp, test, 0);
			o = getValueFromGetter(xc,"getjs$item");
			assertEq("abc", getValueFromGetter(o,"get" + JsonToXml.J_VALUEATTR));
			setValueToSetter(o,"set" + JsonToXml.J_VALUEATTR, null);
			assertTrue(getValueFromGetter(o,"get"+JsonToXml.J_VALUEATTR)==null);

			xc = getXComponent(xp, test, 1);
			o = getValueFromGetter(xc,"getjs$item");
			assertEq(123, getValueFromGetter(o,"get" + JsonToXml.J_VALUEATTR));
			setValueToSetter(o,"set" + JsonToXml.J_VALUEATTR, "");
			assertEq("", getValueFromGetter(o,"get" + JsonToXml.J_VALUEATTR));
			xc = getXComponent(xp, test, 2);
			o = getValueFromGetter(xc,"getjs$item");
			assertEq(false, getValueFromGetter(o,"get"+JsonToXml.J_VALUEATTR));
			xc = getXComponent(xp, test, 3);
			o = getValueFromGetter(xc,"getjs$item");
			assertTrue(getValueFromGetter(o,"get"+JsonToXml.J_VALUEATTR)!=null);

			test = "Test021";
			xc = getXComponent(xp, test, 0);
			o = getValueFromGetter(xc,"getjs$item");
			assertEq("abc", getValueFromGetter(o,"get" +JsonToXml.J_VALUEATTR));
			xc = getXComponent(xp, test, 1);
			o = getValueFromGetter(xc,"getjs$item");
			assertEq(123, getValueFromGetter(o,"get" +JsonToXml.J_VALUEATTR));
			setValueToSetter(o,"set" + JsonToXml.J_VALUEATTR, "");
			assertEq("", getValueFromGetter(o,"get" + JsonToXml.J_VALUEATTR));
			setValueToSetter(o,"set" + JsonToXml.J_VALUEATTR, " a    b \n ");
			assertEq(" a    b \n ", getValueFromGetter(o,
				"get" + JsonToXml.J_VALUEATTR));
			xc = getXComponent(xp, test, 2);
			o = getValueFromGetter(xc,"getjs$item");
			assertEq(false, getValueFromGetter(o,"get" +JsonToXml.J_VALUEATTR));
			xc = getXComponent(xp, test, 3);
			o = getValueFromGetter(xc,"getjs$item");
			assertTrue(getValueFromGetter(o,"get"+JsonToXml.J_VALUEATTR)!=null);
			xc = getXComponent(xp, test, 4);
			assertNull(getValueFromGetter(xc,"getjs$item"));

			test = "Test025";
			xc = getXComponent(xp, test, 0);
			o = getValueFromGetter(xc,"getjs$item");
			assertEq("null", getValueFromGetter(o,
				"get" + JsonToXml.J_VALUEATTR).toString());
			o = getValueFromGetter(xc,"getjs$item_1");
			assertEq(12, getValueFromGetter(o,"get" + JsonToXml.J_VALUEATTR));
			o = getValueFromGetter(xc,"getjs$item_2");
			assertEq("\" a b \"",
				getValueFromGetter(o,"get" + JsonToXml.J_VALUEATTR));
			xc = getXComponent(xp, test, 1);
			o = getValueFromGetter(xc,"getjs$item");
			assertEq("null", getValueFromGetter(o,
				"get" + JsonToXml.J_VALUEATTR).toString());
			assertNull(getValueFromGetter(xc,"getjs$item_1"));
			assertNull(getValueFromGetter(xc,"getjs$item_2"));

			test = "Test026";
			xc = getXComponent(xp, test, 0);
			o = getValueFromGetter(xc,"listOfjs$item");
			assertEq(2, ((List) o).size());
			o = ((List) o).get(0);
			assertEq("null", getValueFromGetter(o,
				"get" + JsonToXml.J_VALUEATTR).toString());
			o = getValueFromGetter(xc,"listOfjs$item");
			o = ((List) o).get(1);
			assertEq("null", getValueFromGetter(o,
				"get" + JsonToXml.J_VALUEATTR).toString());
			xc = getXComponent(xp, test, 0);
			o = getValueFromGetter(xc,"listOfjs$item_1");
			assertEq(2, ((List) o).size());
			o = ((List) o).get(0);
			assertEq(12, getValueFromGetter(o,"get" + JsonToXml.J_VALUEATTR));
			o = getValueFromGetter(xc,"listOfjs$item_1");
			o = ((List) o).get(1);
			assertEq(13, getValueFromGetter(o,"get" + JsonToXml.J_VALUEATTR));
		} catch (Exception ex) {fail(ex);}

		// If no errors were reported delete all generated data.
		// Otherwise, leave them to be able to see the reason of errors.
		if (getFailCount() == 0) {
			try {
				SUtils.deleteAll(_tempDir, true); //delete all generated data
			} catch (Exception ex) {
				fail(ex);// should not happen; error when delete generated data
			}
		}

		////////////////////////////////////////////////////////////////////////
		// Other tests
		////////////////////////////////////////////////////////////////////////
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='json'>\n"+
"<xd:json name='json'>\n"+
"{\"\": \"optional jstring()\"}\n" +
"</xd:json>\n"+
"</xd:def>";
			xp = compile(xdef);
			json = "{\"\":\"aaa\"}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			json = "{}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='json'>\n"+
"<xd:json name='json'>\n"+
"{\"\": \"optional jstring()\"}\n" +
"</xd:json>\n"+
"</xd:def>";
			xp = compile(xdef);
			json = "{\"\":\"aaa\"}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			json = "{}";
			j = xp.createXDDocument().jparse(json, reporter);
			reporter.checkAndThrowErrors();
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='A|B|json'>\n"+
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
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			el = JsonUtil.jsonToXml(j);
			parse(xp, "", el, reporter);
			assertNoErrors(reporter);
			json = "{\"a\":1}";
			j = xp.createXDDocument().jparse(json, reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			el = JsonUtil.jsonToXml(j);
			parse(xp, "", el, reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='B'>\n"+
"<xd:json name='B'>\n"+
"[$script: \"init out('a'); finally out('b')\", \"int(); finally out('x')\"]\n"+
"</xd:json>\n"+
"</xd:def>\n";
			xd = compile(xdef).createXDDocument();
			strw = new StringWriter();
			xd.setStdOut(strw);
			json = "[123]";
			j = xd.jparse(json, reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j));
			assertEq("axb", strw.toString());
		} catch (Exception ex) {fail(ex);}
/*xx*/
	}

	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}