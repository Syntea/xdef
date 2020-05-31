package bugreports;

import org.w3c.dom.Element;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import org.xdef.util.GenXDefinition;
import org.xdef.xml.KXmlUtils;
import test.XDTester;

/** Test generation of X-definition.
 * @author Vaclav Trojan
 */
public class MyTest_1 extends XDTester {

	public MyTest_1() {super();}

////////////////////////////////////////////////////////////////////////////////

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
"<a>\n"+
" <b a='b807728f081d7ca61a282d9377bd'/>\n"+
" <b a='8bd9b807728f081d7ca61a282d9377bd'/>\n"+
" <b a='5dd302b28bd9b807728f081d7ca61a282d9377bd'\n"+
"    c='Mon May 11 23:39:07 CEST 2020'/>\n"+
"</a>",
//"<a/>\n",
//"<a a='a@cz' b='2019-10-20' c='20-10-2019'/>\n",
//"<a>\n"+
//"  <b>\n"+
//"    <c/>\n"+
//"    <d/>\n"+
//"  </b>\n"+
//"  <b>\n"+
//"    <c/>\n"+
//"    <d/>\n"+
//"  </b>\n"+
"<a>\n"+
"  <b c='20190521214531'/>\n"+
"  <b c='1'/>\n"+
"  <b c='9223372036854775807'/>\n"+
"  <b c='-807' d='5dd302b28bd9b807728f081d7ca61a282d9377bd'/>\n"+
"  <b c='0' e='d302b28bd9b807728f081d7ca61a282d'/>\n"+
"  <b c='92233720368547758099' f='0deff0012000012345'/>\n"+
"</a>",
//"<A a='a' b='bb' c='1' d='true'></A>",
//"<xd:X xmlns:xd=\"a.b.c\" xd:x=\"123\">\n"+
//"  <xd:Y xd:y=\"z\"> x  </xd:Y>\n"+
//"  <xd:Y y=\"z\"/>\n"+
//"  <xd:Y/>\n"+
//"</xd:X>",
//"<X xmlns=\"a.b.c\" x=\"123\">\n"+
//"  <Y y=\"z\"> x </Y>\n"+
//"  <Y/>\n"+
//"  <Z/>\n"+
//"</X>",
//"<A>\n"+
//"  <B a='11.06.87'/><B a='11.06.1987'/>\n"+
//"  <C a='11.06.87'/><C a='11.06.1987'/>\n"+
//"</A>",
//"<T>\n"+
//"  <R A='xx' B='aaa'/>\n"+
//"  <R A='xxx' B='aa'/>\n"+
//"  <R A='xxx' B='aaa'/>\n"+
//"  <R A='xx' B='aa'/>\n"+
//"</T>",
//"<X x=\"123\">\n"+
//"  <Y> xxxxx  </Y>\n"+
//"  <Y/>\n"+
//"  <Z/>\n"+
//"</X>",
//"<Contract Number = \"0123456789\" date = \"2011-12-13\" >\n"+
//"  <Client Type = \"1\"\n"+
//"          Name = \"Company LTD\"\n"+
//"          ID   = \"12345678\" />\n"+
//"  <Client Type       = \"2\"\n"+
//"          GivenName  = \"John\"\n"+
//"          LastName   = \"Smith\"\n"+
//"          PersonalID = \"311270/1234\" />\n"+
//"  <Client Type       = \"3\"\n"+
//"          GivenName  = \"Bill\"\n"+
//"          LastName   = \"White\"\n"+
//"          PersonalID = \"311270/1234\"\n"+
//"          ID         = \"87654321\" />\n"+
//"</Contract>",
//"<A_ IdFlow=\"181131058\" date=\"20190521214531\">\n"+
//"    <XXX IdDefPartner=\"163\"/>\n"+
//"    <YYY FileKind=\"W1A\"/>\n"+
//"    <A a=\"SLP\">\n"+
//"        <B b=\"b\">\n"+
//"            <Z x=\"3\" />\n"+
//"            <Z x=\"4\" />\n"+
//"        </B>\n"+
//"   </A>\n"+
//"</A_>",
//"<a>\n"+
//"  <DefParams>\n"+
//"    <Param Name=\"Name\" Type=\"string()\" />\n"+
//"    <Param Type=\"dec()\" Name=\"Height\"/>\n"+
//"    <Param Name=\"Date of birth\" Type=\"xdatetime('dd.MM.yyyy')\" />\n"+
//"  </DefParams>\n"+
//"  <Params>\n"+
//"    <Param Name=\"Name\" Value=\"John\"/>\n"+
//"    <Param Name=\"Height\" Value=\"14.8\"/>\n"+
//"    <Param Name=\"Date of birth\" Value=\"01.02.1987\"/>\n"+
//"  </Params>\n"+
//"  <Params>\n"+
//"    <Param Value=\"14.8a\" Name=\"Height\"/>\n"+
//"  </Params>\n"+
//"</a>",
//"<Values Ver=\"2.0\"\n"+
//"       Valid=\"1.1.2000 00:00:01\"\n"+
//"       Channel=\"A\"\n"+
//"       Seq=\"1\"\n"+
//"       SeqRef=\"1\"\n"+
//"       Date=\"1.1.2000\">\n"+
//"   <File Name=\"abcdef\"\n"+
//"           Format=\"TXT\"\n"+
//"           Kind=\"xyz\"\n"+
//"           RecNum=\"12345678\"\n"+
//"           ref=\"111\">\n"+
//"       <CheckSum Type=\"MD5\"\n"+
//"           Value=\"\n"+
//"123456789A123456789A123456789A12123456789A123456789A123456789A12\n"+
//"                 \"/>\n"+
//"       <xxxx/>\n"+
//"       <yyyy/>\n"+
//"   </File>\n"+
//"   <File Name=\"sss.bb\"\n"+
//"           Format=\"TXT\"\n"+
//"           Kind=\" \"\n"+
//"           RecNum=\"12345678\">\n"+
//"   </File>\n"+
//"   xxxx\n"+
//"   <y><fff attr=\"???\"/></y>\n"+
//"   <log cttr=\"xxx\" />\n"+
//"</Values>",
		}) {
			String xdef = null;
			try {
				Element el = GenXDefinition.genXdef(xml);
				xdef = KXmlUtils.nodeToString(el, true);
System.out.println(KXmlUtils.nodeToString(KXmlUtils.parseXml(xml),true));
System.out.println("xdef:\n"+ xdef);
System.out.println("===========");
				XDPool xp = compile(xdef);
				assertEq(xml, parse(xp, "", xml, reporter));
				assertNoErrors(reporter);
			} catch (Exception ex) {
System.out.println(KXmlUtils.nodeToString(KXmlUtils.parseXml(xml),true));
System.out.println("xdef:\n"+ xdef);
System.out.println("===========");
				fail(ex);
			}
		}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}