package test.xdef;

import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.component.GenXComponent;
import org.xdef.component.XComponent;
import org.xdef.json.JsonUtil;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import org.w3c.dom.Element;
import buildtools.XDTester;

/** Test processing JSON objects with X-definitions and X-components.
 * @author Vaclav Trojan
 */
public class TestJsonXdef extends XDTester {

	public TestJsonXdef() {super();}

	private String _dataDir; // dirsctory with test data
	private File[] _jfiles; // array with jdef files
	private String _tempDir; // dirsctory where to generate files

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
		_jfiles = SUtils.getFileGroup(_dataDir + filter + ".jdef");
		_tempDir = getTempDir() + "json/";
		new File(_tempDir).mkdirs();
		// Generate files and compile X-definitions and X-components.
		try {
			boolean rebuild = false;
			String xdir = _tempDir + "x/";
			new File(xdir).mkdirs();
			String components =
				"<xd:component xmlns:xd='" + XDConstants.XDEF32_NS_URI + "'>\n";
			for (File fdef: _jfiles) {
				File newFile;
				String xdef;
				Element el;
				String id = getId(fdef); // get ID from jdef file name
				// get all json files for this test
				File[] data = SUtils.getFileGroup(_dataDir+"Test"+id +"*.json");
				for (File f: data) {
					String name = f.getName();
					int ndx = name.indexOf(".json");
					name = name.substring(0, ndx);
					Object json = JsonUtil.parse(f);
					// write JSON as XML (W3C modc)
					el = JsonUtil.jsonToXmlW3C(json);
					SUtils.writeString(new File(_tempDir + name + "a.xml"),
						KXmlUtils.nodeToString(el,true),"UTF-8");
					// Write JSON data as XML (XDEF modc)
					el = JsonUtil.jsonToXml(json);
					SUtils.writeString(new File(_tempDir + name + "b.xml"),
						KXmlUtils.nodeToString(el,true),"UTF-8");
				}
				// read jdef file to string.
				String jdef = SUtils.readString(
					new File(_dataDir + "Test" + id + ".jdef"), "UTF-8");
				// Create X-definition from Jdef (W3C)
				newFile = new File(_tempDir + "Test" + id + "a.xdef");
				xdef = "<xd:def xmlns:xd='"+XDConstants.XDEF32_NS_URI
					+ "'\n xd:name='" + "Test" + id + "a' xd:root='jw:json'>\n"
					+ "<xd:json name='jw:json'>\n"
					+ jdef + "\n</xd:json>\n</xd:def>";
				SUtils.writeString(newFile, xdef, "UTF-8");
				// Create X-definition from Jdef (X-definition)
				newFile = new File(_tempDir + "Test" + id + "b.xdef");
				xdef = "<xd:def xmlns:xd='"+XDConstants.XDEF32_NS_URI
					+ "'\n xd:name='" + "Test" + id + "b' xd:root='js:json'>\n"
					+ "<xd:json mode='xd' name='js:json'>\n"
					+ jdef + "\n</xd:json>\n</xd:def>";
				SUtils.writeString(newFile, xdef, "UTF-8");
				// create X-component items
				String cls = "  %class test.common.json.component.Test" + id;
				el = KXmlUtils.parseXml(
					_tempDir + "Test" + id + "a.xdef").getDocumentElement();
				components += cls +"a %link Test" + id
					+ "a#" + el.getAttribute("xd:root") + ";\n";
				el = KXmlUtils.parseXml(
					_tempDir + "Test" + id + "b.xdef").getDocumentElement();
				components += cls +"b %link Test" + id
					+ "b#" + el.getAttribute("xd:root") + ";\n";
			}
			components += "</xd:component>";
			// write X-component declaration to the file
			File componentFile = new File(_tempDir + "Components.xdef");
			SUtils.writeString(componentFile, components, "UTF-8");
			// compile all X-definitions to XDPool
			XDPool xp;
			try {
				File[] x = SUtils.getFileGroup(_tempDir+"Test*.xdef");
				File[] files = new File[x.length + 1];
				System.arraycopy(x, 0, files, 0, x.length);
				files[x.length] = componentFile;
				xp = compile(files);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			File oldFile, newFile;
			// Generate X-components to the directory test
			ArrayReporter reporter = GenXComponent.genXComponent(xp,
				xdir, "UTF-8", false, true);
			reporter.checkAndThrowErrors();
			String componentDir = _tempDir + "test/common/json/component/";
			new File(componentDir).mkdirs();
			String newComponentDir = xdir + "test/common/json/component/";
			for (File fdef: _jfiles) { // check if something changed
				String id = getId(fdef);
				newFile = new File(newComponentDir + "Test" + id + "a.java");
				oldFile = new File(componentDir + "Test" + id + "a.java");
				if (!oldFile.exists()||SUtils.compareFile(oldFile,newFile)>=0) {
					SUtils.copyToFile(newFile, oldFile);
					rebuild = true; //force rebuild
				}
				newFile.delete();
				newFile = new File(newComponentDir + "Test" + id + "b.java");
				oldFile = new File(componentDir + "Test" + id + "b.java");
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
						clazz = Class.forName(
							"test.common.json.component.Test" + id + 'b');
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
	 * @param ver version of test ('a'..w3c, 'b'.. XDEF)
	 * @return the empty string if tests are OK, otherwise return the string
	 * with error messages.
	 */
	private String testJdef(final XDPool xp,
		final String id,
		final String ver) {
		Element e;
		XDDocument xd;
		XComponent xc;
		String result = "";
		ArrayReporter reporter = new ArrayReporter();
		// get all json files for this test
		xd = xp.createXDDocument("Test" + id + ver);
		String rootName = ("a".equals(ver) ? "jw" : "js") + ":json";
		for (File f : SUtils.getFileGroup(_tempDir+"Test"+id+"*"+ver+".xml")) {
			Object json;
			String name = f.getName();
			String basename = name.substring(0, name.indexOf(ver + ".xml"));
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
						+ " (xdef: Test" + id + ver +".xdef" +"):\n"
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
				Object o = xd.jparse(json, rootName, null);
				if (!JsonUtil.jsonEqual(json, o)) {
					result += (result.isEmpty() ? "" : "\n")
						+ "Error jparse Test" + id + ver + "\n"
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
					"test.common.json.component.Test"+id+ver), null);
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
				xd = xp.createXDDocument("Test" + id + ver);
				xc = xd.parseXComponent(f, Class.forName(
					"test.common.json.component.Test"+id+ver), null);
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
						+ "Error X-component toJsjon " + id + ver + "\n"
						+ JsonUtil.toJsonString(json) + "\n"
						+ JsonUtil.toJsonString(o) + "\n";
				}
			} catch (Exception ex) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				pw.close();
				result += (result.isEmpty() ? "" : "\n")
					+ "Error X-component " + id + ver + "\n" + sw;
			}
		}
		// Test error reporting
		for (File f: SUtils.getFileGroup(_dataDir+"Test"+id+"*.jerr")) {
			try {
				reporter.clear();
				xd.jparse(f, rootName, reporter);
				if (!reporter.errorWarnings()) {
					result += (result.isEmpty() ? "" : "\n")
						+ "Error not reported: "+f.getName()+" ("+ver+")";
				}
			} catch (Exception ex) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				pw.close();
				result += (result.isEmpty() ? "" : "\n")
					+ "Error jerr: " + f.getName() + " (" + ver +")\n" + sw;
			}
		}
		return result;
	}

	/** Provides different tests on files with given ID.
	 * @param xp compiled XDPool from generated X-definitions.
	 * @param id identifier test files.
	 * @return the empty string if tests are OK, otherwise return the string
	 * with error messages.
	 */
	private String testJdef(final XDPool xp, String id) {
		String resulta = testJdef(xp, id, "a");
		String resultb = testJdef(xp, id, "b");
		if (!resulta.isEmpty() && !resultb.isEmpty()) {
			resulta += '\n';
		}
		return resulta + resultb;
	}

	/** Run a JSON getter on the X-component.
	 * @param xc X-component.
	 * @param name name of setter.
	 * @return value of getter.
	 */
	private static Object getXCValue(XComponent xc, String name) {
		Class<?> cls = xc.getClass();
		try {
			Method m = cls.getDeclaredMethod(name);
			m.setAccessible(true);
			return m.invoke(xc);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/** Run a JSON setter on the X-component.
	 * @param xc X-component.
	 * @param name name of setter.
	 * @param val value to be set.
	 */
	private static void setXCValue(XComponent xc, String name, Object val) {
		try {
			Method m = xc.getClass().getDeclaredMethod(name, val.getClass());
			m.setAccessible(true);
			m.invoke(xc, val);
		} catch (Exception ex) {
			try {
				Method m = xc.getClass().getDeclaredMethod(name, Object.class);
				m.setAccessible(true);
				m.invoke(xc, val);
			} catch (Exception exx) {
				throw new RuntimeException(ex);
			}
		}
	}

	/** Get XComponent with parsed data.
	 * @param xp compiled XDPool from generated X-definitions.
	 * @param test identifier test file.
	 * @param p "a" or "b".
	 * @param x file number.
	 * @return XComponent with parsed data.
	 */
	private XComponent getXComponent(final XDPool xp,
		final String test,
		final String p,
		final int x) {
		try {
			File f = new File(_tempDir + test +
				(x > 0 ? "_" + x : "") + p+".xml");
			XDDocument xd = xp.createXDDocument(test + p);
			return xd.parseXComponent(f, Class.forName(
				"test.common.json.component." + test+p), null);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
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

		// Generate data (X-definitons, X-components, XML source files).
		try {
			xp = genAll("Test*");
//			xp = genAll("Test0*");
		} catch (Exception ex) {
			if (ex.getMessage().contains("Java compiler is not available")) {
				getOutStream().println(
					ex.getMessage() + "; TestJsonXdef skipped");
			} else {
				fail(new RuntimeException("Can't generate data", ex));
			}
			return;
		}

		// run all tests
		try {
			for (File f: _jfiles) {
				String s = testJdef(xp, getId(f));
				assertTrue(s.isEmpty(), s);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex); // should not happen!!!
		}
		
		try {
			String test, p;
			XComponent xc;

			test = "Test008";
			p = "a";
			xc = getXComponent(xp, test, p, 0);
			assertEq(1, getXCValue(xc,"jgetnumber"));
			setXCValue(xc,"jsetnumber",3);
			assertEq(3, getXCValue(xc,"jgetnumber"));
//			setXCValue(xc,"jsetnumber",null);
//			assertNull(getXCValue(xc,"jgetnumber"));
//System.out.println(getXCValue(xc,"jgetmap"));
			p = "b";
			xc = getXComponent(xp, test, p, 0);
			assertEq(1, getXCValue(xc,"jgetitem"));
			setXCValue(xc,"jsetitem",3);
			assertEq(3, getXCValue(xc,"jgetitem"));
//			setXCValue(xc,"jsetitem",null);
//			assertNull(getXCValue(xc,"jgetitem"));
//System.out.println(getXCValue(xc,"jgetmap"));

			test = "Test020";
			p = "a";
			xc = getXComponent(xp, test, p, 0);
			assertEq("abc", getXCValue(xc,"jgeta$string"));
			assertNull(getXCValue(xc,"jgeta$number"));
			assertNull(getXCValue(xc,"jgeta$boolean"));
			assertNull(getXCValue(xc,"jgeta$null"));
			xc = getXComponent(xp, test, p, 1);
			assertEq(123, getXCValue(xc,"jgeta$number"));
			setXCValue(xc,"jseta$string", "");
			assertEq("", getXCValue(xc,"jgeta$string"));
			assertNull(getXCValue(xc,"jgeta$number"));
			assertNull(getXCValue(xc,"jgeta$boolean"));
			assertNull(getXCValue(xc,"jgeta$null"));
			xc = getXComponent(xp, test, p, 2);
			assertEq(false, getXCValue(xc,"jgeta$boolean"));
			xc = getXComponent(xp, test, p, 3);
			assertEq("null", getXCValue(xc,"jgeta$null").toString());

			p = "b";
			xc = getXComponent(xp, test, p, 0);
			assertEq("abc", getXCValue(xc,"jgeta"));
			xc = getXComponent(xp, test, p, 1);
//			assertEq(123, getXCValue(xc,"jgeta"));
			assertEq("123", getXCValue(xc,"jgeta"));
			setXCValue(xc,"jseta", "");
			assertEq("", getXCValue(xc,"jgeta"));
			setXCValue(xc,"jseta", 456);
			assertEq(456, getXCValue(xc,"jgeta"));
			xc = getXComponent(xp, test, p, 2);
//			assertEq(false, getXCValue(xc,"jgeta"));
			assertEq("false", getXCValue(xc,"jgeta"));
			xc = getXComponent(xp, test, p, 3);
			assertNull(getXCValue(xc,"jgeta"));

			test = "Test021";
			p = "a";
			xc = getXComponent(xp, test, p, 0);
			assertEq("abc", getXCValue(xc,"jgetstring"));
			assertNull(getXCValue(xc,"jgetnumber"));
			assertNull(getXCValue(xc,"jgetboolean"));
			assertNull(getXCValue(xc,"jgetnull"));
			xc = getXComponent(xp, test, p, 1);
			assertEq(123, getXCValue(xc,"jgetnumber"));
			setXCValue(xc,"jsetstring", "");
			assertEq("", getXCValue(xc,"jgetstring"));
			assertNull(getXCValue(xc,"jgetnumber"));
			assertNull(getXCValue(xc,"jgetboolean"));
			assertNull(getXCValue(xc,"jgetnull"));
			xc = getXComponent(xp, test, p, 2);
			assertEq(false, getXCValue(xc,"jgetboolean"));
			xc = getXComponent(xp, test, p, 3);
			assertEq("null", getXCValue(xc,"jgetnull").toString());
			xc = getXComponent(xp, test, p, 4);
			assertNull(getXCValue(xc,"jgetnull"));

			p = "b";
			xc = getXComponent(xp, test, p, 0);
			assertEq("abc", getXCValue(xc,"jgetitem"));
			xc = getXComponent(xp, test, p, 1);
//			assertEq(123, getXCValue(xc,"jgetitem"));
			assertEq("123", getXCValue(xc,"jgetitem"));
			setXCValue(xc,"jsetitem", "");
			assertEq("", getXCValue(xc,"jgetitem"));
			setXCValue(xc,"jsetitem", 456);
			assertEq(456, getXCValue(xc,"jgetitem"));
			xc = getXComponent(xp, test, p, 2);
//			assertEq(false, getXCValue(xc,"jgetitem"));
			assertEq("false", getXCValue(xc,"jgetitem"));
			xc = getXComponent(xp, test, p, 3);
			assertEq("null", getXCValue(xc,"jgetitem"));
			xc = getXComponent(xp, test, p, 4);
			assertNull(getXCValue(xc,"jgetitem"));
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
"<xd:json mode='w3c'>\n"+
"{\"\": \"optional jstring()\"}\n" +
"</xd:json>\n"+
"</xd:def>";
			xp = compile(xdef);
			json = "{\"\":\"aaa\"}";
			j = xp.createXDDocument().jparse(json, "json", reporter);
			reporter.checkAndThrowErrors();
			json = "{}";
			j = xp.createXDDocument().jparse(json, "json", reporter);
			reporter.checkAndThrowErrors();
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='json'>\n"+
"<xd:json mode='xd'>\n"+
"{\"\": \"optional jstring()\"}\n" +
"</xd:json>\n"+
"</xd:def>";
			xp = compile(xdef);
			json = "{\"\":\"aaa\"}";
			j = xp.createXDDocument().jparse(json, "json", reporter);
			reporter.checkAndThrowErrors();
			json = "{}";
			j = xp.createXDDocument().jparse(json, "json", reporter);
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
			j = xp.createXDDocument().jparse(json, "json", reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			el = JsonUtil.jsonToXmlW3C(j);
			parse(xp, "", el, reporter);
			assertNoErrors(reporter);
			json = "{\"a\":1}";
			j = xp.createXDDocument().jparse(json, "B", reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			el = JsonUtil.jsonToXmlW3C(j);
			parse(xp, "", el, reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
	}

	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}