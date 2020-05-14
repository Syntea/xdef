package test.xdutils;

import org.w3c.dom.Element;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import org.xdef.util.GenXDefinition;
import org.xdef.xml.KXmlUtils;
import test.XDTester;

/** Test of generation of XDefinition from XML.
 * @author Vaclav Trojan
 */
public class TestGenXdef extends XDTester {
	public TestGenXdef() {super();}

	@Override
	/** Run test and print error information. */
	public void test() {
		String xdef;
		String xml;
		Element el;
		XDPool xp;
		ArrayReporter reporter = new ArrayReporter();
		try {
			xml =
"<X x=\"123\">\n" +
"  <Y>\n" +
"    xxxxx\n" +
"  </Y>\n" +
"  <Y/>\n" +
"  <Z/>\n" +
"</X>";
			el = GenXDefinition.genXdef(xml);
			xdef = KXmlUtils.nodeToString(el, true);
			xp = compile(xdef);
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {
			fail(ex);
		}
		try {
			xml =
"<Data Verze=\"2.0\"\n" +
"       PlatnostOd=\"1.1.2000 00:00:01\"\n" +
"       Kanal=\"A\"\n" +
"       Seq=\"1\"\n" +
"       SeqRef=\"1\"\n" +
"       Date=\"1.1.2000\">\n" +
"   <File Name=\"abcdef\"\n" +
"           Format=\"TXT\"\n" +
"           Kind=\"xyz\"\n" +
"           RecNum=\"12345678\"\n" +
"           ref=\"111\">\n" +
"       <CheckSum Type=\"MD5\"\n" +
"           Value=\"123456789A123456789A123456789A12" +
"123456789A123456789A123456789A12\"/>\n" +
"       <x/>\n" +
"       <x/>\n" +
"   </File>\n" +
"   ahoj\n" +
"   <y><fff attr=\"???\"/></y>\n" +
"   <log cttr=\"xxx\" />\n" +
"</Data>";
			el = GenXDefinition.genXdef(xml);
			xdef = KXmlUtils.nodeToString(el, true);
//System.out.println(KXmlUtils.nodeToString(el, true));
			xp = compile(xdef);
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {
			fail(ex);
		}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		runTest();
	}
}