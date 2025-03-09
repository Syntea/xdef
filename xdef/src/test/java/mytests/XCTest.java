package mytests;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SUtils;
import org.xdef.xon.XonUtils;
import test.XDTester;

/** Tests.
 * @author Vaclav Trojan
 */
public class XCTest extends XDTester {
	public XCTest() {super();}

	@Override
	/** Run test and display error information. */
	@SuppressWarnings("unchecked")
	public void test() {
////////////////////////////////////////////////////////////////////////////////
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
////////////////////////////////////////////////////////////////////////////////

		String test, xdef;
		XDPool xp;
		XComponent xc;
		ArrayReporter reporter = new ArrayReporter();
		File tempDir;
		try {
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" name=\"Test\" root=\"Book\" >\n" +
"  <Book ISBN=\"int(10000000, 999999999);\"  \n" +
"      year = \"gYear();\"  \n" +
"      title = \"string();\" >  \n" +
"      <author xd:script=\"occurs *\"> string(); </author>  \n" +
"  </Book>  \n" +
"  <xd:component>  \n" +
"    %class mytests.Book %link Test#Book;  \n" +
"  </xd:component>\n" +
"</xd:def>";
			tempDir = clearTempDir();
			xp = compile(xdef);
			genXComponent(xp, tempDir);
			XDDocument xd = compile(xdef).createXDDocument("Test");
			String data =
"<Book ISBN=\"123456789\"\n" +
"      year=\"2020\"\n" +
"      title = \"Xdefinitions\" >\n" +
"  <author>Tomáš Šmíd</author>\n" +
"</Book>";
			xc = xd.xparseXComponent(data, null, null);
System.out.println(XonUtils.toXonString(xc.toXon(), true));
//if(true)return;
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='a'>\n"+
"<xd:json name='a'>\n" +
"[\n" +
"  {\n" +
"    a : \"? short()\",\n" +
"    i : [ ],\n" +
"    Towns : [\n" +
"      \"* gps()\"\n" +
"    ],\n" +
"    j : \"? char()\"\n" +
"  },\n" +
"  \"base64Binary()\",\n" +
"  \"price()\",\n" +
"  \"currency()\",\n" +
"  \"* ipAddr()\"\n" +
"]\n" +
"</xd:json>\n" +
"<xd:component>\n"+
"  %class mytests.X_on %link #a;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			tempDir = clearTempDir();
			genXComponent(xp, tempDir);
			test =
"# Start of XON example\n" +
"[ #***** Array *****/\n" +
"  { #***** Map *****/\n" +
"    a : 1,                           # Short\n" +
"    i : [],                          # empty array\n" +
"    Towns : [ # array with GPS locations of towns\n" +
"      g(48.2, 16.37, 151, Wien),\n" +
"      g(51.52, -0.09, 0, London),\n" +
"      g(50.08, 14.42, 399, \"Prague old town\")\n" +
"    ],\n" +
"    j : c\"a\"                        # Character\n" +
"  }, /**** end of map ****/\n" +
"  b(HbRBHbRBHQw=),                   /* byte array (base64) */\n" +
"  p(123.45 CZK),                     /* price */ \n" +
"  C(USD),                            /* currency */\n" +
"  /1080:0:0:0:8:800:200C:417A        /* inetAddr (IPv6)  */\n" +
"] /**** end of array ****/\n" +
"# End of XON example";
			xc = xp.createXDDocument().jparseXComponent(test, null, reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseXON(test),
				SUtils.getValueFromGetter(xc,"toXon")));
			List x = (List) SUtils.getValueFromGetter(xc,"listOf$item_3");
			x.add(InetAddress.getByName("111.22.33.1"));
			SUtils.setValueToSetter(xc, "setitem_3", x);
			assertEq(2, ((List) SUtils.getValueFromGetter(
				xc,"listOf$item_3")).size());
			assertTrue(SUtils.getValueFromGetter(SUtils.getValueFromGetter(
				xc,"getjx$map"), "toXon") instanceof Map);
			assertTrue(((List)SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(SUtils.getValueFromGetter(
					xc,"getjx$map"), "getjx$array"),"toXon")).isEmpty());
			assertEq(3,((List)SUtils.getValueFromGetter(
				SUtils.getValueFromGetter(SUtils.getValueFromGetter(
					xc,"getjx$map"), "getjx$array_1"), "toXon")).size());
			clearTempDir(); // delete temporary files.
		} catch (UnknownHostException | RuntimeException ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}