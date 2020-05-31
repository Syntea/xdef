package test.xdutils;

import org.w3c.dom.Element;
import org.xdef.XDConstants;
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
		String dataDir = getDataDir() + "test/";
		if (dataDir == null) {
			fail("Data directory is missing, test canceled");
			return;
		}
		Element el;
		String xdef;
		String xml;
		XDPool xp;
		ArrayReporter reporter = new ArrayReporter();
		for (String x: new String[] {
"<a/>\n",
"<a>\n"+
"  <b c='20190521214531'/>\n"+
"  <b c='1'/>\n"+
"  <b c='9223372036854775807'/>\n"+
"  <b c='-807' d='5dd302b28bd9b807728f081d7ca61a282d9377bd'/>\n"+
"  <b c='0' e='d302b28bd9b807728f081d7ca61a282d'/>\n"+
"  <b c='92233720368547758099' f='0deff0012000012345'/>\n"+
"</a>",
"<A a='a' b='bb' c='1' d='true'></A>",
"<A>\n"+
"  <B a='11.06.87'/><B a='11.06.1987'/>\n"+
"  <C a='11.06.87'/><C a='11.06.1987'/>\n"+
"</A>",
"<T>\n"+
"  <R A='xx' B='aaa'/>\n"+
"  <R A='xxx' B='aa'/>\n"+
"  <R A='xxx' B='aaa'/>\n"+
"  <R A='xx' B='aa'/>\n"+
"</T>",
"<X xmlns=\"a.b.c\" x=\"123\">\n"+
"  <Y> x </Y>\n"+
"  <Y/>\n"+
"  <Z/>\n"+
"</X>",
"<x:X xmlns:x=\"a.b.c\" x:x=\"123\">\n"+
"  <x:Y x:y=\"z\"> x </x:Y>\n"+
"  <x:Y/>\n"+
"  <x:Z x:y=\"z\"/>\n"+
"</x:X>",
"<Contract Number = \"0123456789\" date = \"2011-12-13\" >\n"+
"  <Client Type = \"1\"\n"+
"          Name = \"Company LTD\"\n"+
"          ID   = \"12345678\" />\n"+
"  <Client Type       = \"2\"\n"+
"          GivenName  = \"John\"\n"+
"          LastName   = \"Smith\"\n"+
"          PersonalID = \"311270/1234\" />\n"+
"  <Client Type       = \"3\"\n"+
"          GivenName  = \"Bill\"\n"+
"          LastName   = \"White\"\n"+
"          PersonalID = \"311270/1234\"\n"+
"          ID         = \"87654321\" />\n"+
"</Contract>",
"<A_ IdFlow=\"181131058\" date=\"20190521214531\">\n"+
"    <XXX IdDefPartner=\"163\"/>\n"+
"    <YYY FileKind=\"W1A\"/>\n"+
"    <A a=\"SLP\">\n"+
"        <B b=\"b\">\n"+
"            <Z x=\"3\" />\n"+
"            <Z x=\"4\" />\n"+
"        </B>\n"+
"   </A>\n"+
"</A_>",
"<a>\n"+
"  <DefParams>\n"+
"    <Param Name=\"Name\" Type=\"string()\" />\n"+
"    <Param Type=\"dec()\" Name=\"Height\"/>\n"+
"    <Param Name=\"Date of birth\" Type=\"xdatetime('dd.MM.yyyy')\" />\n"+
"  </DefParams>\n"+
"  <Params>\n"+
"    <Param Name=\"Name\" Value=\"John\"/>\n"+
"    <Param Name=\"Height\" Value=\"14.8\"/>\n"+
"    <Param Name=\"Date of birth\" Value=\"01.02.1987\"/>\n"+
"  </Params>\n"+
"  <Params>\n"+
"    <Param Value=\"14.8a\" Name=\"Height\"/>\n"+
"  </Params>\n"+
"</a>",
"<Values Ver=\"2.0\"\n"+
"       Valid=\"1.1.2000 00:00:01\"\n"+
"       Channel=\"A\"\n"+
"       Seq=\"1\"\n"+
"       SeqRef=\"1\"\n"+
"       Date=\"1.1.2000\">\n"+
"   <File Name=\"abcdef\"\n"+
"           Format=\"TXT\"\n"+
"           Kind=\"xyz\"\n"+
"           RecNum=\"12345678\"\n"+
"           ref=\"111\">\n"+
"       <CheckSum Type=\"MD5\"\n"+
"           Value=\"\n"+
"123456789A123456789A123456789A12123456789A123456789A123456789A12\n"+
"                 \"/>\n"+
"       <xxxx/>\n"+
"       <yyyy/>\n"+
"   </File>\n"+
"   <File Name=\"sss.bb\"\n"+
"           Format=\"TXT\"\n"+
"           Kind=\"\"\n"+
"           RecNum=\"12345678\">\n"+
"   </File>\n"+
"   xxxx\n"+
"   <y><fff attr=\"???\"/></y>\n"+
"   <log cttr=\"xxx\" />\n"+
"</Values>",
dataDir + "Matej3_out.xml",
dataDir + "TestValidate2.xml",
dataDir + "../../data/schema/D1A.xml",
dataDir + "../../data/schema/L1A.xml",
		}) {
			try {
				el = GenXDefinition.genXdef(x);
				xdef = KXmlUtils.nodeToString(el, true);
				xp = compile(xdef);
				assertEq(x, parse(xp, "", x, reporter));
				assertNoErrors(reporter);
			} catch (Exception ex) {fail(ex);}
		}
		try {
			xml =
"<xd:X xmlns:xd=\"a.b.c\" xd:x=\"123\">\n"+
"  <xd:Y xd:y=\"z\"> x </xd:Y>\n"+
"  <xd:Y y=\"z\"/>\n"+
"  <xd:Y/>\n"+
"</xd:X>";
			el = GenXDefinition.genXdef(xml);
			fail("Exception not thrown");
		} catch (Exception ex) {
			if (!ex.getMessage().contains("XDEF881")) {
				fail(ex);
			}
		}
		try {
			xml =
"<d:X xmlns:d=\""+ XDConstants.XDEF20_NS_URI + "\" d:x=\"123\">\n"+
"  <d:Y d:y=\"z\"> x </d:Y>\n"+
"  <d:Y y=\"z\"/>\n"+
"  <d:Y/>\n"+
"</d:X>";
			el = GenXDefinition.genXdef(xml);
			fail("Exception not thrown");
		} catch (Exception ex) {
			if (!ex.getMessage().contains("XDEF882")) {
				fail(ex);
			}
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