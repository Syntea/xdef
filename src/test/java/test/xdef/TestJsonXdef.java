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
import builtools.XDTester;

/** Test processing JSON objects with X-definitions and X-components.
 * @author Vaclav Trojan
 */
public class TestJsonXdef extends XDTester {

	public TestJsonXdef() {super();}

	private String _dataDir; // dirsctory with test data
	private File[] _jfiles; // array with jdef files
	private String _tempDir; // dirsctory where to generate files
	private int _errors; // error sounter

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
		_errors = 0;
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
					+ "'\n xmlns:jw='" + XDConstants.JSON_NS_URI_W3C
					+ "'\n xd:name='" + "Test" + id + "a' xd:root='jw:json'>\n"
					+ "<jw:json>\n" +jdef+ "\n</jw:json>\n</xd:def>";
				SUtils.writeString(newFile, xdef, "UTF-8");
				// Create X-definition from Jdef (X-definition)
				newFile = new File(_tempDir + "Test" + id + "b.xdef");
				xdef = "<xd:def xmlns:xd='"+XDConstants.XDEF32_NS_URI
					+ "'\n xmlns:js='" + XDConstants.JSON_NS_URI
					+ "'\n xd:name='" + "Test" + id + "b' xd:root='js:json'>\n"
					+ "<js:json>\n" +jdef+ "\n</js:json>\n</xd:def>";
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
				_errors++;
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
					_errors++;
					result += (result.isEmpty() ? "" : "\n")
						+ "ERRORS in " + name
						+ " (xdef: Test" + id + ver +".xdef" +"):\n"
						+ reporter.printToString();
				} else {
					KXmlUtils.compareElements(e,
						f.getAbsolutePath(), true, reporter);
					if (reporter.errorWarnings()) {
						_errors++;
						result += (result.isEmpty() ? "" : "\n")
							+ "ERROR: result differs " + name;
					} else {
						Object o = JsonUtil.xmlToJson(
							KXmlUtils.nodeToString(e, true));
						if (!JsonUtil.jsonEqual(json, o)) {
							_errors++;
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
				_errors++;
				result += (result.isEmpty() ? "" : "\n")
					+ "Error " + name + "\n" + sw;
			}
			// parse with jparse
			try {
				Object o = xd.jparse(json, rootName, null);
				if (!JsonUtil.jsonEqual(json, o)) {
					_errors++;
					result += (result.isEmpty() ? "" : "\n")
						+ "Error jparse Test" + id + ver + "\n"
						+ JsonUtil.toJsonString(json) + "\n"
						+ JsonUtil.toJsonString(o) + "\n";
				}
			} catch (Exception ex) {
				_errors++;
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
					_errors++;
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
				_errors++;
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
					_errors++;
					result += (result.isEmpty() ? "" : "\n")
						+ "Error X-component " + name +"\n"
						+ reporter.printToString()
						+ "\n"+ KXmlUtils.nodeToString(e, true);
				}
				Object o = xc.toJson();
				if (!JsonUtil.jsonEqual(json, o)) {
					_errors++;
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
				_errors++;
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
					_errors++;
					result += (result.isEmpty() ? "" : "\n")
						+ "Error not reported: "+f.getName()+" ("+ver+")";
				}
			} catch (Exception ex) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				pw.close();
				_errors++;
				result += (result.isEmpty() ? "" : "\n")
					+ "Error jerr: " + f.getName() + " (" + ver +")\n" + sw;
			}
		}
		return result;
	}

	/** Run a JSON getter on the X-component.
	 * @param xc X-component.
	 * @param name name of setter.
	 * @return value of getter.
	 */
	private static Object getJValue(XComponent xc, String name) {
		Class<?> cls = xc.getClass();
		try {
			Method m = cls.getDeclaredMethod("jget" + name);
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
	private static void setJValue(XComponent xc, String name, Object val) {
		Class<?> cls = xc.getClass();
		try {
			Method m = cls.getDeclaredMethod("jset" + name, val.getClass());
			m.invoke(xc, val);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
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

	private XComponent getXComponent(final XDPool xp,
		final String id, final String json) throws Exception{
		XDDocument xd = xp.createXDDocument("Test" + id);
		return xd.jparseXComponent(json,
			Class.forName("test.common.json.component.Test" + id), null);
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		// this code will be removed after Gelcollection will process JSON
		boolean chkSyntax = getChkSyntax();
		setChkSyntax(false);

		// Generate all data (X-definitons, X-components, XML ddocuments).
		XDPool xp = genAll("Test*");
//		XDPool xp = genAll("Test001");

		// run tests
		for (File f: _jfiles) {
			String s = testJdef(xp, getId(f));
			assertTrue(s.isEmpty(), s);
		}
		// test getters/setters in X-component
		try {
			XComponent xc;
			String json = "{\"\":\"\"}";
			xc = getXComponent(xp, "004a", json);
			assertEq("", getJValue(xc, "$_"));
			setJValue(xc, "$_", " abc");
			assertEq(" abc", getJValue(xc, "$_"));
			setJValue(xc, "$_", "");
			assertEq("", getJValue(xc, "$_"));

			xc = getXComponent(xp, "004b", json);
			assertEq("", getJValue(xc, "$_"));
			setJValue(xc, "$_", "abc");
			assertEq("abc", getJValue(xc, "$_"));
			setJValue(xc, "$_", "");
			assertEq("", getJValue(xc, "$_"));
		} catch (Exception ex) {fail(ex);}
		// If no errors were reported delete all generated data.
		// Otherwise, leave them to be able to see the reason of errors.
		if (_errors == 0) {
			try {
				SUtils.deleteAll(_tempDir, true); //delete all generated data
			} catch (Exception ex) {
				fail(ex); // error when delete generated data.
			}
		}
		// this code will be removed after Gelcollection will process JSON
		setChkSyntax(chkSyntax);
	}

	public static void main(String[] args) {
		XDTester.setFulltestMode(false);
		if (runTest(args) > 0) {System.exit(1);}
	}
}