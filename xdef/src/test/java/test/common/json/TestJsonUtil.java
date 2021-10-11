package test.common.json;

import java.io.File;
import org.w3c.dom.Element;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonUtil;
import org.xdef.sys.STester;

/** Test JSON utilities, JSON parser and conversion XML / JSON. */
public class TestJsonUtil extends STester {
	File[] _files;

	public TestJsonUtil() {super();}

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
//		String id = getId(f);
		Object o1, o2;
		Element el;
		try {
			// test toJsonString and parse JSON
			o1 = XonUtil.parse(f);
			o2 = XonUtil.parse(XonUtil.toJsonString(o1, true));
			if (!XonUtil.jsonEqual(o1, o2)) {
				return "JSON toString error " + f.getName();
			}
		} catch (Exception ex) {
			return "JSON error " + f.getName() + "\n" + ex;
		}
		try {
			// test JSON to XML and XML to JSON (W3C format) JSON
			el = XonUtil.jsonToXmlXD(o1);
			o2 = XonUtil.xmlToJson(el);
			if (!XonUtil.jsonEqual(o1, o2)) {
				return "JSON xmlToJson (W3C) error " + f.getName()
					+ "\n" + KXmlUtils.nodeToString(el);
			}
		} catch (Exception ex) {
			return "Error jsonToXml (XD): " + f.getName() + "\n"
				+ ex + "\n" + XonUtil.toJsonString(o1, true);
		}
		try {
			// test JSON to XML and XML to JSON (W3C format) JSON
			el = XonUtil.jsonToXml(o1);
			o2 = XonUtil.xmlToJson(el);
			if (!XonUtil.jsonEqual(o1, o2)) {
				return "JSON xmlToJson (W3C) error " + f.getName()
					+ "\n" + KXmlUtils.nodeToString(el);
			}
		} catch (Exception ex) {
			return "Error XmlToJson (W3C): " + f.getName() + "\n"
				+ ex + "\n" + XonUtil.toJsonString(o1, true);
		}
		return "";
	}

	private String testXConvert(final File f) {
		try {
			Element e1 = KXmlUtils.parseXml(f).getDocumentElement();
			Object jx1 = XonUtil.xmlToJson(e1);
			Element e2 = XonUtil.jsonToXmlXD(jx1);
			Object jx2 = XonUtil.xmlToJson(e2);
			if (KXmlUtils.compareElements(e1, e2, true).errors()) {
				return "XML-error-:  "
					+ KXmlUtils.compareElements(e1, e2, true)
					+ '\n' + KXmlUtils.nodeToString(e1, true)
					+ '\n' + KXmlUtils.nodeToString(e2, true);
			}
			if (!XonUtil.jsonEqual(jx1, jx2)) {
				return "X JSON-error-:  \n" + XonUtil.toJsonString(jx1, true)
					+ '\n' + XonUtil.toJsonString(jx2, true)
					+ '\n' + KXmlUtils.nodeToString(e1, true);
			}
			return KXmlUtils.nodeToString(e1)
				+ '\n' + XonUtil.toJsonString(jx1, true);
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

	@Override
	/** Run test and print error information. */
	public void test() {
		init("Test*"); //init directories and test files
//		init("Test105");
		for (File json: _files) { // test JSON parser
			String s = testJParse(json);
			if (!s.isEmpty()) {
				fail(s);
			}
		}
		_files = SUtils.getFileGroup((new File(getDataDir()).getAbsolutePath()
			+ File.separator).replace('\\', '/') + "TestErr*.json");
		for (File json: _files) { // test JSON erros
			try {
				XonUtil.parse(json);
				fail(json.getName());
			} catch (Exception ex) {}
		}

		File directory = new File(getDataDir() + "../../../xdef/data/json/");
		for (File x: directory.listFiles()) {
			if (x.isFile() && x.getName().endsWith("json")) {
				String s = testJParse(x);
				if (!s.isEmpty()) {
					fail(x + "\n" + s);
				}
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
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}