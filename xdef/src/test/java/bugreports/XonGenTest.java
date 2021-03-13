package bugreports;

import java.io.File;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.json.JsonUtil;
import org.xdef.sys.ArrayReporter;
import test.XDTester;

/** Test of generation of XON
 * @author Vaclav Trojan
 */
public class XonGenTest extends XDTester {

	@Override
	/** Run test and display error information. */
	public void test() {
		String tempDir = getTempDir();
		File f = new File(getTempDir());
		if (f.exists() && !f.isDirectory()) {
			throw new RuntimeException(f.getAbsolutePath()
				+ " is not directory");
		}
		f.mkdir();
		tempDir = f.getAbsolutePath().replace('\\', '/');
		if (!tempDir.endsWith("/")) {
			tempDir += '/';
		}
		if (!f.isDirectory()) {
			fail('\"' + tempDir + "\" is not directory");
			return;
		}
		XDPool xp;
		String xdef;
		String json;
		Object j, o;
		XComponent xc;
		ArrayReporter reporter = new ArrayReporter();
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='A'>\n"+
"<xd:json name='A'>\n"+
//"[\"* jvalue()\"]\n"+
"{\"a\":[\"int()\", \"date\", \"* jvalue()\"], \"b\": \"jvalue()\"}\n"+
"</xd:json>\n"+
"<xd:component>\n"+
"  %class bugreports.data.GJson %link #A;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			XDTester.genXComponent(xp, new File(tempDir));
			json = "{\"a\":[1, \"2021-03-10\", \"334\", null], \"b\": null}";
			j = xp.createXDDocument().jparse(json, reporter);
//			assertTrue(reporter.printToString().contains("XDEF809"));
//	reporter.clear();
			assertTrue(JsonUtil.jsonEqual(JsonUtil.parse(json), j),
				JsonUtil.toJsonString(j, true));
			xc = xp.createXDDocument().jparseXComponent(json,
				null, reporter);
//			assertTrue(reporter.getErrorCount() == 2
//				&& reporter.printToString().contains("XDEF809"));
//			assertTrue(JsonUtil.jsonEqual(
//				JsonUtil.parse(json), XComponentUtil.toJson(xc)),
//				JsonUtil.toJsonString(XComponentUtil.toJson(xc), true));
			o = JsonUtil.xmlToJson(xc.toXml());
			System.out.println(JsonUtil.toXonString(o, true));
			o = XComponentUtil.toXon(xc);
			System.out.println(JsonUtil.toXonString(o, true));
		} catch (Exception ex) {fail(ex);}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}