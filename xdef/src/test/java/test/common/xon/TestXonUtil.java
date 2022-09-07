package test.common.xon;

import java.io.File;
import org.w3c.dom.Element;
import org.xdef.impl.XonXml_X;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonUtils;
import org.xdef.sys.STester;
import static org.xdef.sys.STester.printThrowable;
import static org.xdef.sys.STester.runTest;

/** Test JSON utilities, JSON parser and conversion XML / JSON. */
public class TestXonUtil extends STester {
	private File[] _files;

	public TestXonUtil() {super();}

	/** Set variables _files and _errors. */
	private void init(String groupName) {
		File f = new File(getDataDir());
		String s = (f.getAbsolutePath() + File.separator).replace('\\', '/');
		_files = SUtils.getFileGroup(s + groupName + ".json");
	}

	/** Test parser of JSON source, toJSONString utility and JsonToXml convertor.
	 * @param f file with JSON source.
	 * @return error message or the empty string if no error was recognized.
	 */
	private String testJParse(final File f) {
		Object o1, o2;
		Element el;
		try {
			// test toJsonString and parseJSON JSON
			o1 = XonUtils.parseJSON(f);
			o2 = XonUtils.parseJSON(XonUtils.toJsonString(o1, true));
			if (!XonUtils.xonEqual(o1, o2)) {
				return "JSON toString error " + f.getName();
			}
		} catch (Exception ex) {
			return "JSON error " + f.getName() + "\n" + ex;
		}
		try {
			// test JSON to XML and XML to JSON (W3C format) JSON
			el = XonUtils.xonToXml(o1);
			o2 = XonUtils.xmlToXon(el);
			if (!XonUtils.xonEqual(o1, o2)) {
				return "JSON xmlToJson (W3C) error " + f.getName()
					+ "\n" + KXmlUtils.nodeToString(el);
			}
		} catch (Exception ex) {
			return "Error jsonToXml (XD): " + f.getName() + "\n"
				+ ex + "\n" + XonUtils.toJsonString(o1, true);
		}
		try {
			// test JSON to XML and XML to JSON (W3C format) JSON
			el = XonUtils.xonToXmlW(o1);
			o2 = XonUtils.xmlToXon(el);
			if (!XonUtils.xonEqual(o1, o2)) {
				return "JSON xmlToJson (W3C) error " + f.getName()
					+ "\n" + KXmlUtils.nodeToString(el);
			}
		} catch (Exception ex) {
			return "Error XmlToJson (W3C): " + f.getName() + "\n"
				+ ex + "\n" + XonUtils.toJsonString(o1, true);
		}
		return "";
	}

	private String testXConvert(final File f) {
		try {
			Element e1 = KXmlUtils.parseXml(f).getDocumentElement();
			Object jx1 = XonUtils.xmlToXon(e1);
			Element e2 = XonUtils.xonToXml(jx1);
			Object jx2 = XonUtils.xmlToXon(e2);
			if (KXmlUtils.compareElements(e1, e2, true).errors()) {
				return "XML-error-:  "
					+ KXmlUtils.compareElements(e1, e2, true)
					+ '\n' + KXmlUtils.nodeToString(e1, true)
					+ '\n' + KXmlUtils.nodeToString(e2, true);
			}
			if (!XonUtils.xonEqual(jx1, jx2)) {
				return "X JSON-error-:  \n" + XonUtils.toJsonString(jx1, true)
					+ '\n' + XonUtils.toJsonString(jx2, true)
					+ '\n' + KXmlUtils.nodeToString(e1, true);
			}
			return KXmlUtils.nodeToString(e1)
				+ '\n' + XonUtils.toJsonString(jx1, true);
		} catch (Exception ex) {
			return "-error-:  " + printThrowable(ex);
		}
	}

	private boolean isExcluded(final String[] names, final String name) {
		for (int i = 0; i < names.length; i++) {
			if (names[i].equals(name)) {
				return true;
			}
		}
		return false;
	}

	private static String testX(String s) {return testX1(XonUtils.parseXON(s));}

	private static String testX(File f) {return testX1(XonUtils.parseXON(f));}

	private static String testX1(Object o) {
		Element el = XonXml_X.xonToXmlX(o);
		Object o1 = XonXml_X.toXon(el);
		return XonUtils.xonEqual(o, o1) ? ""
			: ("/n***\n" + KXmlUtils.nodeToString(el, true) +
			"/n***\n" + XonUtils.toXonString(o1, true));
	}

	private static String testXD(String s) {
		return testXD1(XonUtils.parseXON(s));
	}

	private static String testXD(File f) {
		return testXD1(XonUtils.parseXON(f));
	}

	private static String testXD1(Object o) {
		Element el = XonUtils.xonToXml(o);
		Object o1 = XonUtils.xmlToXon(el);
		return XonUtils.xonEqual(o, o1) ? ""
			: ("/n***\n" + KXmlUtils.nodeToString(el, true) +
			"/n***\n" + XonUtils.toXonString(o1, true));
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		init("Test*"); //init directories and test files
//		init("Test105");
		for (File json: _files) { // test JSON parser
			assertEq("", testJParse(json), json.getAbsolutePath());
//			assertEq("", testX(json), json.getAbsolutePath());
		}
		_files = SUtils.getFileGroup((new File(getDataDir()).getAbsolutePath()
			+ File.separator).replace('\\', '/') + "TestErr*.json");
		for (File json: _files) { // test JSON erros
			try {
				XonUtils.parseJSON(json);
				fail(json.getName());
			} catch (Exception ex) {}
		}

		File directory = new File(getDataDir() + "../../../xdef/data/json/");
		for (File x: directory.listFiles()) {
			if (x.isFile()
				&& (x.getName().endsWith("json")||x.getName().endsWith("xon"))){
				assertEq("", testJParse(x), x.getAbsolutePath());
//				assertEq("", testX(x), x.getAbsolutePath());
			}
		}
		for (File x: directory.listFiles()) {
			String name = x.getName();
			if (x.isFile() && name.endsWith("xdef")) {
				String s = testXConvert(x);
				if (s.indexOf("-error-:  ") >= 0) {
					fail("XX XML " + x + " *\n" + s);
				}
			}
		}
		String[] excluded = new String[] {
			"Test000_01.xml",		// DTD error
			"Test000_08.xml",		// xml error
			"TestInclude_1_3.xml",	// xml include error
			"TestInclude_2_1.xml",	// xml include error
			"TestInclude_3_1.xml",	// xml include error
			"TestInclude_3_2.xml",	// xml include error
			"TestInclude_4_4.xml",	// xml include error
			"TestInclude_6.xdef",	// xml include error
			"TestInclude_7.xdef",	// xml include error
		};
		directory = new File(getDataDir() + "../../../xdef/data/test/");
		for (File x: directory.listFiles()) {
			String name = x.getName();
			if (x.isFile() && (name.endsWith("xdef") || name.endsWith("xml"))) {
				if (!isExcluded(excluded, name)) {
					String s = testXConvert(x);
					if (s.indexOf("-error-:  ") >= 0) {
						fail("XX XML " + x + " *\n" + s);
					}
				}
			}
		}
		for (String s : new String[] {
			"true",
			"\"true\"",
			"null",
			"1l", //long
			"-1.25e-3f",
			"{}",
			"{\"\":\"\"}",
			"[]",
			"[[]]",
			"[[{}]]",
			"[[[]]]",
			"[[[1b]]]",
			"[[],[]]",
			"[[1d,2D],[null,4s]]",
			"[{a:{b:1}}]",
			"[{a:[]}]",
			"[{a:[1]}]",
			"[{a:[[]]}]",
			"[{a:[{}]}]",
			"[{a:[{a:1}]}]",
			"[1, -1.25e-3, true, \"abc\", null]",
			"[1, { _x69_:1, _x5f_map:null, \"a_x\tb\":[null], item:{}}]",
			"[1, { a : 1, b : \"a\", \"\":false, array : [], map:{}}, \"abc\"]",
			"[{a:[{},[1,2],{},[3,4]]}]",
			"[{a:[{a:1},[1,2],{},[3,4]]}]",
			"[{a:[[1,2],{},[3,4]]}]",
			"[{a:[[1,2],{a:1},[30,4]]}]",
			"[{a:[[1,2],[3,4],{}]}]",
			"[{a:[[1,2],[3,4],{a:1,b:2}]}]",
			}) {
			assertEq("", testXD(s), s);
			assertEq("", testX(s), s); //Internal version
		}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}