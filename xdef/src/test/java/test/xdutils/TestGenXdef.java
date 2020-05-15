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
		String dataDir = getDataDir() + "test/";
		if (dataDir == null) {
			fail("Data directory is missing, test canceled");
			return;
		}
		ArrayReporter reporter = new ArrayReporter();
		for (String xml: new String[] {
"<a/>",
"<A a='a' b='bb' c='1' d='true'></A>",
"<A>\n"+
"  <B a='11.06.87'/><B a='11.06.1987'/>\n"+
"  <C a='11.06.87'/><C a='11.06.1987'/>\n"+
"</A>",
"<T>\n" +
"  <R A='xx' B='aaa'/>\n" +
"  <R A='xxx' B='aa'/>\n" +
"  <R A='xxx' B='aaa'/>\n" +
"  <R A='xx' B='aa'/>\n" +
"</T>",
"<X x=\"123\">\n" +
"  <Y>\n" +
"    xxxxx\n" +
"  </Y>\n" +
"  <Y/>\n" +
"  <Z/>\n" +
"</X>",
"<Contract Number = \"0123456789\">\n"+
"  <Client Type  = \"1\"\n"+
"          Name = \"Nějaká Firma s.r.o.\"\n"+
"          ID   = \"12345678\" />\n"+
"  <Client Type       = \"2\"\n"+
"          GivenName = \"Jan\"\n"+
"          LastName   = \"Kovář\"\n"+
"          PersonalID = \"311270/1234\" />\n"+
"  <Client Type        = \"3\"\n"+
"          GivenName  = \"František\"\n"+
"          LastName   = \"Bílý\"\n"+
"          PersonalID = \"311270/1234\"\n"+
"          ID         = \"87654321\" />\n"+
"</Contract>",
"<A_ IdFlow=\"181131058\">\n" +
"    <XXX IdDefPartner=\"163\"/>\n" +
"    <YYY DruhSouboru=\"W1A\"/>\n" +
"    <A a=\"Pojistitel_SLP\">\n" +
"        <B b=\"b\">\n" +
"            <Z x=\"3\" />\n" +
"            <Z x=\"4\" />\n" +
"        </B>\n" +
"   </A>\n" +
"</A_>",
"<a>\n" +
"  <DefParams>\n" +
"    <Param Name=\"Jmeno\" Type=\"string()\" />\n" +
"    <Param Type=\"dec()\" Name=\"Vyska\"/>\n" +
"    <Param Name=\"DatumNarozeni\" Type=\"xdatetime('dd.MM.yyyy')\" />\n" +
"  </DefParams>\n" +
"  <Params>\n" +
"    <Param Name=\"Jmeno\" Value=\"Jan\"/>\n" +
"    <Param Name=\"Vyska\" Value=\"14.8\"/>\n" +
"    <Param Name=\"DatumNarozeni\" Value=\"01.02.1987\"/>\n" +
"  </Params>\n" +
"  <Params>\n" +
"    <Param Value=\"14.8a\" Name=\"Vyska\"/>\n" +
"  </Params>\n" +
"</a>",
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
"           Value=\"\n"+
"123456789A123456789A123456789A12123456789A123456789A123456789A12\n"+
"                 \"/>\n" +
"       <xxxx/>\n" +
"       <yyyy/>\n" +
"   </File>\n" +
"   <File Name=\"sss.bb\"\n" +
"           Format=\"TXT\"\n" +
"           Kind=\" \"\n" +
"           RecNum=\"12345678\">\n" +
"   </File>\n" +
"   xxxx\n" +
"   <y><fff attr=\"???\"/></y>\n" +
"   <log cttr=\"xxx\" />\n" +
"</Data>",
dataDir + "Matej3_out.xml",
dataDir + "TestValidate2.xml",
dataDir + "../../data/schema/D1A.xml",
		}) {
			try {
				Element el = GenXDefinition.genXdef(xml);
				String xdef = KXmlUtils.nodeToString(el, true);
//System.out.println("xml:\n" + xml);
//System.out.println("xdef:\n" + xdef);
//System.out.println("===========");
				XDPool xp = compile(xdef);
				assertEq(xml, parse(xp, "", xml, reporter));
				assertNoErrors(reporter);
			} catch (Exception ex) {fail(ex);}
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