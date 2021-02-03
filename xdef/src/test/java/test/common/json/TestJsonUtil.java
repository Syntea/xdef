package test.common.json;

import java.io.File;
import org.w3c.dom.Element;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.json.JsonUtil;
import org.xdef.sys.STester;

/** Test JSON utilities, JSON parser and conversion XML / JSON. */
public class TestJsonUtil extends STester {

	File[] _files;
	int _errors;

	public TestJsonUtil() {super();}

	/** Set variables _files and _errors. */
	private void init(String groupName) {
		File f = new File(getDataDir());
		String s = (f.getAbsolutePath() + File.separator).replace('\\', '/');
		_files = SUtils.getFileGroup(s + groupName + ".json");
		_errors = 0;
	}

	/** Get ID from the file name.
	 * @param f file name
	 * @return ID (string of file name without the prefix "Test"
	 * and without file extension.
	 */
	private  String getId(final File f) {
		String s = f.getName();
		return s.substring(4, s.lastIndexOf('.'));
	}

	/** Test parser of JSON source, toJSONString utility and JsonToXml convertor.
	 * @param f file with JSON source.
	 * @return error message or the empty string if no error was recognized.
	 */
	private String testJParse(final File f) {
		String id = getId(f);
		Object o1, o2;
		Element el;
		try {
			// test toJsonString and parse JSON
			o1 = JsonUtil.parse(f);
			o2 = JsonUtil.parse(JsonUtil.toJsonString(o1, true));
			if (!JsonUtil.jsonEqual(o1, o2)) {
				_errors++;
				return "JSON toString error " + id;
			}
		} catch (Exception ex) {
			_errors++;
			return "JSON error " + id + "\n" + ex;
		}
		try {
			// test JSON to XML and XML to JSON (W3C format) JSON
			el = JsonUtil.jsonToXmlXD(o1);
			o2 = JsonUtil.xmlToJson(el);
			if (!JsonUtil.jsonEqual(o1, o2)) {
				_errors++;
				return "JSON xmlToJson (W33) error " + id
					+ KXmlUtils.nodeToString(el);
			}
		} catch (Exception ex) {
			_errors++;
			return "Error jsonToXml (XD): Test" + id + ".json\n"
				+ ex + "\n" + JsonUtil.toJsonString(o1, true);
		}
		try {
			// test JSON to XML and XML to JSON (W3C format) JSON
			el = JsonUtil.jsonToXml(o1);
			o2 = JsonUtil.xmlToJson(el);
			if (!JsonUtil.jsonEqual(o1, o2)) {
				_errors++;
				return "JSON xmlToJson (W3C) error " + id
					+ KXmlUtils.nodeToString(el);
			}
		} catch (Exception ex) {
			_errors++;
			return "Error XmlToJson (W3C): Test" + id + ".json\n"
				+ ex + "\n" + JsonUtil.toJsonString(o1, true);
		}
		return "";
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		init("Test*"); //init directories and test files
//		init("Test105");
		for (File json: _files) {
			String id = getId(json);
			// test JSOMN parser
			String s = testJParse(json);
			if (!s.isEmpty()) {
				fail(s);
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