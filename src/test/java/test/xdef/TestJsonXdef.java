package test.xdef;

import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.component.GenXComponent;
import org.xdef.component.XComponent;
import org.xdef.json.JsonUtil;
import org.xdef.json.JsonToXml;
import org.xdef.json.XmlToJson;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.w3c.dom.Element;
import static test.utils.STester.runTest;
import test.utils.XDTester;

/** Test processing JSON objects with X-definitions.
 * @author Vaclav Trojan
 */
public class TestJsonXdef extends XDTester {

	public TestJsonXdef() {super();}

	private String _dataDir;
	private File[] _jfiles;
	private String _tempDir;
	private int _errors;

	/** Get ID from the file name.
	 * @param f file name
	 * @return ID (string of file name without the prefix "Test"
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
				String id = getId(fdef); // get ID from def file name
				// write JSON as XML (W3C modc)
				el = JsonToXml.toXmlW3C(
					JsonToXml.parse(_dataDir + "Test"+id+".json"));
				SUtils.writeString(new File(_tempDir + "Test" + id + "a.xml"),
					KXmlUtils.nodeToString(el,true),"UTF-8");
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
				// Write JSON data as XML (XDEF modc)
				el = JsonToXml.toXmlXD(JsonUtil.parse(
					_dataDir + "Test" + id + ".json"));
				SUtils.writeString(new File(_tempDir + "Test" + id + "b.xml"),
					KXmlUtils.nodeToString(el,true),"UTF-8");
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
				xp = XDFactory.compileXD(null,
					SUtils.getFileGroup(_tempDir+"Test*.xdef"), componentFile);
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
	 * @param id identifier test files.
	 * @return the empty string if tests are OK, otherwise return the string
	 * with error messages.
	 */
	private String testJdef(final XDPool xp, String id) {
		String testFile = _tempDir + "Test" + id;
		ArrayReporter reporter = new ArrayReporter();
		Element e;
		XDDocument xd;
		String result = "";
		Object o1, o2;
		Class<?> clazz;
		XComponent xc;
		// parse JSON data to JSON object
		try {
			o1 = JsonUtil.parse(_dataDir + "Test"+id+".json");
		} catch (Exception ex) {
			return "Incorrect JSON data Test"+id+".json";
		}

		// create XDDocument with W3C form X-definition
		try {
			xd = xp.createXDDocument("Test" + id + "a");
			reporter.clear(); // clear reporter
			// parse data with X-definition
			e = xd.xparse(testFile + "a.xml", reporter);
			if (reporter.errorWarnings()) { // check errors
				_errors++;
				result += (result.isEmpty() ? "" : "\n")
					+ "ERRORS in Test" + id + "a.xml"
					+ " (xdef: Test" + id + "a.xdef" +"):\n"
					+ reporter.printToString();
			} else {
				KXmlUtils.compareElements(e, KXmlUtils.parseXml(
					testFile + "a.xml").getDocumentElement(), true, reporter);
				if (reporter.errorWarnings()) {
					_errors++;
					result +=  (result.isEmpty() ? "" : "\n")
						+ "ERROR: result differs + Test" + id + "b";
				} else {
					o2 = XmlToJson.toJson(e);
					if (!JsonUtil.jsonEqual(o1, o2)) {
						result +=  (result.isEmpty() ? "" : "\n")
							+ "ERROR conversion XML to JSON: Test" + id + "a\n"
							+ JsonUtil.toJSONString(o1, true)
							+ '\n' + JsonUtil.toJSONString(o2, true)
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
				+ "Error Test" + id + "a:\n" + sw;
		}

		// create XDDocument with X-definition generated from jdef
		try {
			reporter.clear();
			xd = xp.createXDDocument("Test" + id + "b");
			// parse data with X-definition
			e = xd.xparse(testFile + "b.xml", reporter);
			// check errors
			if (reporter.errorWarnings()) {
				_errors++;
				result += (result.isEmpty() ? "" : "\n")
					+ "ERRORS in Test" + id + "b.xml"
					+ " (xdef: Test" + id + "b.xdef" +"):\n"
					+ reporter.printToString();
			} else {
				KXmlUtils.compareElements(e, KXmlUtils.parseXml(
					testFile + "b.xml").getDocumentElement(), true, reporter);
				if (reporter.errorWarnings()) {
					_errors++;
					result +=  (result.isEmpty() ? "" : "\n")
						+ "ERROR: result differs + Test" + id + "b";
				} else {
					o2 = XmlToJson.toJson(e);
					if (!JsonUtil.jsonEqual(o1, o2)) {
						result +=  (result.isEmpty() ? "" : "\n")
							+ "ERROR conversion XML to JSON: Test" + id + "b\n"
							+ JsonUtil.toJSONString(o1, true)
							+ '\n' + JsonUtil.toJSONString(o2, true)
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
			result +=  (result.isEmpty() ? "" : "\n")
				+ "Error Test" + id + "b:\n" + sw;
		}

		// Test X-component in W3C mode.
		try {
			xd = xp.createXDDocument("Test" + id + 'a');
			clazz = Class.forName("test.common.json.component.Test"+id+"a");
			xc = xd.parseXComponent(_tempDir + "Test"+id+"a.xml", clazz, null);
			reporter.clear();
			e = xc.toXml();
			KXmlUtils.compareElements(e, KXmlUtils.parseXml(
				testFile + "a.xml").getDocumentElement(), true, reporter);
			if (reporter.errorWarnings()) {
				_errors++;
				result += (result.isEmpty() ? "" : "\n")
					+ "Error X-component Test"
					+ id  +"a:\n" + reporter.printToString()
					+ "\n"+ KXmlUtils.nodeToString(e, true);
			}
		} catch (Exception ex) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			pw.close();
			_errors++;
			result += (result.isEmpty() ? "" : "\n")
				+ "Error X-component Test" + id + "a:\n" + sw;
		}
		// Test X-component in XDEF mode.
		try {
			xd = xp.createXDDocument("Test" + id + 'b');
			clazz = Class.forName("test.common.json.component.Test" + id + 'b');
			xc = xd.parseXComponent(_tempDir + "Test"+id+"b.xml", clazz, null);
			reporter.clear();
			e = xc.toXml();
			KXmlUtils.compareElements(e, KXmlUtils.parseXml(
				testFile + "b.xml").getDocumentElement(), true, reporter);
			if (reporter.errorWarnings()) {
				_errors++;
				result += (result.isEmpty() ? "" : "\n")
					+ "Error X-component Test"
					+ id  +"b:\n" + reporter.printToString()
					+ "\n"+ KXmlUtils.nodeToString(e, true);
			}
		} catch (Exception ex) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			pw.close();
			_errors++;
			result += (result.isEmpty() ? "" : "\n")
				+ "Error X-component Test" + id + "b:\n" + sw;
		}
		return result;
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		// Generate all data (X-definitons, X-components, XML ddocuments).
		XDPool xp = genAll("Test*");
//		XDPool xp = genAll("Test001");
		// run tests
		for (File f: _jfiles) {
			String s = testJdef(xp, getId(f));
			if (!s.isEmpty()) {
				fail(s);
			}
		}

		// If no errors were reported delete all generated data.
		// Otherwise, leave them to allow to see the reason of errors.
		if (_errors == 0) {
			try {
				SUtils.deleteAll(_tempDir, true); //delete all generated data
			} catch (Exception ex) {
				fail(ex); // error when delete generated data.
			}
		}
	}

	public static void main(String[] args) {
		XDTester.setFulltestMode(false);
		if (runTest(args) > 0) {System.exit(1);}
	}

}