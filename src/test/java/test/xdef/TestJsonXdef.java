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
import java.lang.reflect.Field;

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
				// read jdef file to string.
				String jdef = SUtils.readString(
					new File(_dataDir + "Test" + id + ".jdef"), "UTF-8");
				// Create X-definition from Jdef (W3C)
				newFile = new File(_tempDir + "Test" + id + "a.xdef");
				xdef = "<xd:def xmlns:xd='"+XDConstants.XDEF32_NS_URI
					+ "'\n xd:name='" + "Test" + id + "a' xd:root='a'>\n"
					+ "<xd:json name='a'>\n"
					+ jdef + "\n</xd:json>\n</xd:def>";
				SUtils.writeString(newFile, xdef, "UTF-8");
				// create X-component items
				String cls = "  %class test.common.json.component.Test" + id;
				el = KXmlUtils.parseXml(
					_tempDir + "Test" + id + "a.xdef").getDocumentElement();
				components += cls +"a %link Test" + id
					+ "a#" + el.getAttribute("xd:root") + ";\n";
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
		xd = xp.createXDDocument("Test" + id + "a");
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
				Object o = xd.jparse(json, "a", null);
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
					"test.common.json.component.Test" + id + "a"), null);
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
				xd = xp.createXDDocument("Test" + id + "a");
				xc = xd.parseXComponent(f, Class.forName(
					"test.common.json.component.Test"+id+"a"), null);
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
				xd.jparse(f, "a", reporter);
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

	/** Get value of the field of the class of an object.
	 * @param o Object where is the filed.
	 * @param name name of filed.
	 * @return value of field.
	 */
	private Object getXCField(Object o, String name) {
		Class<?> cls = o.getClass();
		try {
			Field f = cls.getDeclaredField(name);
			f.setAccessible(true);
			try {
				return f.get(o);
			} catch (Exception ex) {
				return f.get(null); //static
			}
		} catch (Exception ex) {
			throw new RuntimeException("Field not found: " + name);
		}
	}

	/** Set to the field of the class of an object.
	 * @param o Object where is the filed.
	 * @param name name of filed.
	 * @param value the value to be set.
	 */
	private void setXCField(Object o, String name, Object value) {
		Class<?> cls = o.getClass();
		try {
			Field f = cls.getDeclaredField(name);
			f.setAccessible(true);
			try {
				f.set(o, value);
			} catch (Exception ex) {
				f.set(null, value); // static
			}
		} catch (Exception ex) {
			throw new RuntimeException("Field not found: " + name);
		}
	}

	/** Invoke a getter on the object.
	 * @param o object where is getter.
	 * @param name name of setter.
	 * @return value of getter.
	 */
	private Object getXCValue(Object o, String name) {
		Class<?> cls = o.getClass();
		try {
			Method m = cls.getDeclaredMethod(name);
			m.setAccessible(true);
			try {
				return m.invoke(o);
			} catch (Exception ex) {
				return m.invoke(null); //static
			}
		} catch (Exception ex) {
			throw new RuntimeException("Getter not found: " + name);
		}
	}

	/** Invoke a setter on the object.
	 * @param o the object where is setter.
	 * @param name name of setter.
	 * @param val value to be set.
	 */
	private void setXCValue(Object o, String name, Object val) {
		for (Method m: o.getClass().getDeclaredMethods()) {
			Class<?>[] params = m.getParameterTypes();
			if (name.equals(m.getName()) && params!=null && params.length==1) {
				try {
					m.setAccessible(true);
					try {
						m.invoke(o, val);
						return;
					} catch (Exception ex) {
						m.invoke(null, val); // static
						return;
					}
				} catch (Exception ex) {}
			}
		}
		throw new RuntimeException(
			"Setter " + o.getClass().getName() + '.' + name + " not found");
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
		final int x) {
		try {
			File f = new File(_tempDir + test +	(x > 0 ? "_"+x : "") + "a.xml");
			return xp.createXDDocument(test + 'a').parseXComponent(f,
				Class.forName("test.common.json.component." + test + 'a'), null);
		} catch (Exception ex) {
			throw new RuntimeException("XComponent not found: " + test);
		}
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		// save actual value of chkSyntax
//		boolean chkSyntax = getChkSyntax();
		// this code will be removed after GenCollection will process JSON
//		XDTester.setFulltestMode(false);
//		setChkSyntax(false);

		String xdef, xml, json;
		Object j;
		ArrayReporter reporter = new ArrayReporter();
		Element el;
		XDPool xp;

		// Generate data (X-definitons, X-components, XML source files).
		try {
			xp = genAll("Test*");
//			xp = genAll("Test017");
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
		} catch (Exception ex) {fail(ex);} // should not happen!!!
		try {
/*xx*/
			String test;
			XComponent xc;

			test = "Test008";
			xc = getXComponent(xp, test, 0);
			assertEq(1, getXCValue(xc,"jgetnumber"));
			setXCValue(xc,"jsetnumber",3);
			assertEq(3, getXCValue(xc,"jgetnumber"));
			setXCValue(xc,"jsetnumber", null);
			assertNull(getXCValue(xc,"jgetnumber"));

			test = "Test020";
			xc = getXComponent(xp, test, 0);
			assertEq("abc", getXCValue(xc,"jgeta$string"));
			assertNull(getXCValue(xc,"jgeta$number"));
			assertNull(getXCValue(xc,"jgeta$boolean"));
			assertNull(getXCValue(xc,"jgeta$null"));
			setXCValue(xc,"jseta$null", null);
			assertTrue(getXCValue(xc,"jgeta$boolean") == null);

			xc = getXComponent(xp, test, 1);
			assertEq(123, getXCValue(xc,"jgeta$number"));
			setXCValue(xc,"jseta$string", "");
			assertEq("", getXCValue(xc,"jgeta$string"));
			assertNull(getXCValue(xc,"jgeta$number"));
			assertNull(getXCValue(xc,"jgeta$boolean"));
			assertNull(getXCValue(xc,"jgeta$null"));
			xc = getXComponent(xp, test, 2);
			assertEq(false, getXCValue(xc,"jgeta$boolean"));
			xc = getXComponent(xp, test, 3);
			assertTrue(getXCValue(xc,"jgeta$null") != null);

			test = "Test021";
			xc = getXComponent(xp, test, 0);
			assertEq("abc", getXCValue(xc,"jgetstring"));
			assertNull(getXCValue(xc,"jgetnumber"));
			assertNull(getXCValue(xc,"jgetboolean"));
			assertNull(getXCValue(xc,"jgetnull"));
			xc = getXComponent(xp, test, 1);
			assertEq(123, getXCValue(xc,"jgetnumber"));
			setXCValue(xc,"jsetstring", "");
			assertEq("", getXCValue(xc,"jgetstring"));
			assertNull(getXCValue(xc,"jgetnumber"));
			assertNull(getXCValue(xc,"jgetboolean"));
			assertNull(getXCValue(xc,"jgetnull"));
			setXCValue(xc,"jsetstring", " a    b \n ");
			assertEq(" a    b \n ", getXCValue(xc,"jgetstring"));
			xc = getXComponent(xp, test, 2);
			assertEq(false, getXCValue(xc,"jgetboolean"));
			xc = getXComponent(xp, test, 3);
			assertTrue(getXCValue(xc,"jgetnull") != null);
			xc = getXComponent(xp, test, 4);
			assertNull(getXCValue(xc,"jgetnull"));
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
			j = xp.createXDDocument().jparse(json, "json", reporter);
			reporter.checkAndThrowErrors();
			json = "{}";
			j = xp.createXDDocument().jparse(json, "json", reporter);
			reporter.checkAndThrowErrors();
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='json'>\n"+
"<xd:json name='json'>\n"+
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
			el = JsonUtil.jsonToXml(j);
			parse(xp, "", el, reporter);
			assertNoErrors(reporter);
			json = "{\"a\":1}";
			j = xp.createXDDocument().jparse(json, "B", reporter);
			assertNoErrors(reporter);
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			el = JsonUtil.jsonToXml(j);
			parse(xp, "", el, reporter);
			assertNoErrors(reporter);
/*xx*/
		} catch (Exception ex) {fail(ex);}
	}

	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}